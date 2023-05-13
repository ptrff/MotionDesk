package ru.ptrff.motiondesk.view;

import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.content.Intent;
import android.content.res.ColorStateList;
import android.graphics.Bitmap;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.transition.Explode;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.LinearLayout;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.view.MenuProvider;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;
import androidx.lifecycle.Lifecycle;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.badlogic.gdx.backends.android.AndroidFragmentApplication;
import com.google.android.material.bottomsheet.BottomSheetBehavior;
import com.google.android.material.snackbar.BaseTransientBottomBar;
import com.google.android.material.snackbar.Snackbar;

import java.util.Objects;

import io.reactivex.Observable;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.CompositeDisposable;
import io.reactivex.schedulers.Schedulers;
import ru.ptrff.motiondesk.R;
import ru.ptrff.motiondesk.adapters.EffectsListAdapter;
import ru.ptrff.motiondesk.adapters.ToolbarAdapter;
import ru.ptrff.motiondesk.databinding.ActivityWallpaperEditorBinding;
import ru.ptrff.motiondesk.databinding.StateViewBinding;
import ru.ptrff.motiondesk.engine.WallpaperLibGdxFragment;
import ru.ptrff.motiondesk.engine.effects.BaseEffect;
import ru.ptrff.motiondesk.engine.scene.ActorHandler;
import ru.ptrff.motiondesk.engine.scene.EngineEventsListener;
import ru.ptrff.motiondesk.engine.scene.WallpaperEditorEngine;
import ru.ptrff.motiondesk.models.WallpaperItem;
import ru.ptrff.motiondesk.utils.BitmapProcessor;
import ru.ptrff.motiondesk.viewmodel.EditorViewModel;

public class WallpaperEditor extends AppCompatActivity implements AndroidFragmentApplication.Callbacks, EngineEventsListener {

    private ActivityWallpaperEditorBinding binding;
    private EditorViewModel viewModel;
    private ToolbarAdapter toolbarAdapter;
    private final CompositeDisposable disposables = new CompositeDisposable();

    private WallpaperEditorEngine engine;

    private ProjectInfoFragment projectInfoFragment;

    //BottomSheetFragments
    private final FragmentManager fm = getSupportFragmentManager();
    private LayerListFragment layerListFragment;
    private EffectsListFragment effectsListFragment;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityWallpaperEditorBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        setSupportActionBar(binding.toolbarTitle);

        showLoading();

        viewModel = new ViewModelProvider(this).get(EditorViewModel.class);
        if (getIntent().getBooleanExtra("new_project", true)) {
            viewModel.setHeight(getIntent().getIntExtra("Height", 2340));
            viewModel.setWidth(getIntent().getIntExtra("Width", 1080));
            viewModel.setName(getIntent().getStringExtra("Name"));
            viewModel.createNewWallpaperItem();
        } else
            viewModel.loadWallpaperItem((WallpaperItem) getIntent().getSerializableExtra("wallpaper_item"));

        loadEditorEngine();

        observeContent();


        getSupportActionBar().setHomeAsUpIndicator(R.drawable.ic_back);
        getSupportActionBar().setHomeActionContentDescription(R.string.back);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setTitle(viewModel.getName());


        setupActionBarButtons();
        setupBottomSheet();
        setupToolbar();
    }

    @SuppressLint("CheckResult")
    public void loadEditorEngine() {
        Observable
                .just(new WallpaperEditorEngine(viewModel.getWidth(), viewModel.getHeight(), this))
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(engine -> {
                    this.engine = engine;
                    viewModel.setEngine(engine);
                    setupEnvironment(engine);
                    engine.setResources(getResources());
                    WallpaperLibGdxFragment libgdxFragment = new WallpaperLibGdxFragment(engine);
                    getSupportFragmentManager().beginTransaction().
                            add(R.id.preview, libgdxFragment).
                            commit();

                }, throwable -> {
                    snackMessage(getString(R.string.error_creating_editor), binding.getRoot());
                    Log.e("EditorViewModel", getString(R.string.error_creating_editor), throwable);
                });
    }

    private void setupEnvironment(WallpaperEditorEngine engine) {
        layerListFragment = new LayerListFragment(engine);
        effectsListFragment = new EffectsListFragment(engine, effectListeners);

        projectInfoFragment = new ProjectInfoFragment(viewModel.getWallpaperItem(), new ProjectInfoFragmentEvents() {
            @Override
            public void chooseImageFromGallery() {
                chooseImage(101);
            }

            @Override
            public void onProjectInfoChanged() {
                viewModel.onProjectInfoChanged();
            }
        });

        viewModel.getLoadingState().postValue(false);
    }

    @SuppressLint("CheckResult")
    private void observeContent() {
        viewModel.getToolbarToolsLiveData().observe(this, toolbarItems -> {
            toolbarAdapter.submitListWithAnimation(null);
            toolbarAdapter.submitListWithAnimation(toolbarItems);
        });

        viewModel.getBottomSheetOpenedLiveData().observe(this, opened -> {
            if (opened)
                BottomSheetBehavior.from(binding.bottomView).setState(BottomSheetBehavior.STATE_COLLAPSED);
            else
                BottomSheetBehavior.from(binding.bottomView).setState(BottomSheetBehavior.STATE_HIDDEN);
        });

        viewModel.getSnackMessageLiveData().observe(this,
                snackMessage -> snackMessage(snackMessage, binding.getRoot())
        );

        viewModel.getAppBarTextLiveData().observe(this, appBarText -> {
            getSupportActionBar().setTitle(appBarText);
        });

        viewModel.getShowProjectParameters().observe(this, unused -> {
            showSceneParameters();
        });

        viewModel.getShowEffectParameters().observe(this, unused -> {
            showEffectParameters(viewModel.getCurrentEffect());
        });

        viewModel.getShowObjectParameters().observe(this, unused -> {
            showObjectParameters(engine.getObject(engine.getDraggedSpriteId()));
        });

        viewModel.getLoadingState().observe(this, isLoading -> {
            if (isLoading) binding.loadingWindow.setVisibility(View.VISIBLE);
            else binding.loadingWindow.setVisibility(View.GONE);
        });

        viewModel.getPickImageLiveData().observe(this, unused -> {
            viewModel.getLoadingState().postValue(true);
            chooseImage(100);
        });

        viewModel.getShowProjectInformation().observe(this, unused -> {
            projectInfoFragment.show(getSupportFragmentManager(), "project info");
        });

        viewModel.getStartPreviewLiveData().observe(this, unused -> {
            Intent intent = new Intent(WallpaperEditor.this, WallpaperPreview.class);
            intent.putExtra("wallpaper_item", viewModel.getWallpaperItem());
            getWindow().setExitTransition(new Explode());
            startActivity(intent);
        });

        viewModel.getCurrentBottomContentLiveData().observe(this, currentBottomContent -> {
            System.out.println("WAWALWALLWAWA");
            if (currentBottomContent.equals("layers")) {
                selectBottomContent(
                        layerListFragment,
                        getString(R.string.layers),
                        getDrawable(R.drawable.ic_layers)
                );
            }
            if (currentBottomContent.equals("effects")) {
                selectBottomContent(
                        effectsListFragment,
                        getString(R.string.effects),
                        getDrawable(R.drawable.ic_effects)
                );
            }
        });

        viewModel.getBottomSheetUpdateActionLiveData().observe(this, unused -> {
            String currentBottomContent = viewModel.getCurrentBottomContentLiveData().getValue();
            if (currentBottomContent != null) {
                if (currentBottomContent.equals("layers")) {
                    layerListFragment.onResume();
                }
                if (currentBottomContent.equals("effects")) {
                    effectsListFragment.onResume();
                }
            }
        });
    }

    @Override
    public void onObjectSelected(String type, int index) {
        if (viewModel.getToolbarStatus().equals("image_params") ||
                !engine.getObject(index).getName().equals(viewModel.getToolbarStatus())) {
            viewModel.showEditObjectTools();
        }

        if (Objects.equals(viewModel.getCurrentBottomContentLiveData().getValue(), "effects")) {
            runOnUiThread(() -> effectsListFragment.onResume());
        }
    }

    @Override
    public void onObjectNotSelected() {
        if (!viewModel.getToolbarStatus().equals("default")) {
            viewModel.resetTools();
        }
    }

    @Override
    public void onObjectAdded(int position) {
        viewModel.getLoadingState().postValue(false);
        runOnUiThread(() -> layerListFragment.notifyItemInserted(position));
    }

    @Override
    public void onObjectRemoved(int position) {
        runOnUiThread(() -> layerListFragment.notifyItemRemoved(position));
    }

    @Override
    public void onStartDrawingMask(int index) {
        viewModel.showDrawingMaskTools();
        getSupportActionBar().setTitle(engine.getObject(index).getName());
    }

    @Override
    public void onStopDrawingMask() {

    }

    @Override
    public void onEffectAdded() {
        runOnUiThread(() -> effectsListFragment.onResume());
    }

    @Override
    public void snackMessage(String message) {
        WallpaperEditor.this.snackMessage(message, binding.getRoot());
    }

    private final EffectsListAdapter.EffectListeners effectListeners = new EffectsListAdapter.EffectListeners() {
        @Override
        public void onEffectClick(BaseEffect effect) {
            viewModel.setCurrentEffect(effect);
            viewModel.showEditEffectTools();
        }

        @Override
        public void onStartDrag(RecyclerView.ViewHolder viewHolder) {
        }
    };

    private void chooseImage(int code) {
        Intent intent = new Intent();
        intent.setType("image/*");
        intent.setAction(Intent.ACTION_GET_CONTENT);
        startActivityForResult(Intent.createChooser(intent, getString(R.string.choose_image)), code);
    }

    @SuppressLint("CheckResult")
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == RESULT_OK && data != null && data.getData() != null) {
            Uri uri = data.getData();

            if (requestCode == 100) {
                disposables.add(loadImage(uri)
                        .subscribeOn(Schedulers.io())
                        .observeOn(AndroidSchedulers.mainThread())
                        .subscribe(bitmap -> viewModel.onImagePicked(requestCode, bitmap),
                                error -> {
                                    viewModel.getLoadingState().postValue(false);
                                    snackMessage(getString(R.string.error_image_import), binding.getRoot());
                                    Log.e("WallpaperEditor", getString(R.string.error_image_import), error);
                                }));
            }
            if (requestCode == 101) {
                loadImage(uri)
                        .flatMap(this::resizeImage)
                        .subscribeOn(Schedulers.io())
                        .observeOn(AndroidSchedulers.mainThread())
                        .subscribe(resizedBitmap -> {
                                    projectInfoFragment.setNewImage(resizedBitmap);
                                }, error -> {
                                    projectInfoFragment.newImageLoadingFailed();
                                    viewModel.getLoadingState().postValue(false);
                                    Log.e("WallpaperEditor", "Error getting image from gallery", error);
                                }
                        );
            }
        } else {
            viewModel.getLoadingState().postValue(false);
            if (requestCode == 101) {
                projectInfoFragment.newImageLoadingFailed();
            }
        }
    }

    private Observable<Bitmap> resizeImage(Bitmap bitmap) {
        return Observable.fromCallable(() -> BitmapProcessor.scaleBitmap(bitmap, 360, 540));
    }

    private Observable<Bitmap> loadImage(Uri uri) {
        return Observable.fromCallable(() -> MediaStore.Images.Media.getBitmap(getContentResolver(), uri));
    }

    private void setupToolbar() {
        toolbarAdapter = new ToolbarAdapter(tool -> viewModel.onToolbarToolClicked(tool));

        LinearLayoutManager sGrid = new LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false);
        binding.toolbarTools.setLayoutManager(sGrid);
        binding.toolbarTools.setAdapter(toolbarAdapter);
    }


    private void selectBottomContent(Fragment fragment, String name, Drawable icon) {
        binding.title.setText(name);
        binding.icon.setImageDrawable(icon);
        fragment.onResume();
        FragmentTransaction transaction = fm.beginTransaction();
        transaction.replace(binding.content.getId(), fragment);
        transaction.setTransition(FragmentTransaction.TRANSIT_FRAGMENT_FADE);
        transaction.addToBackStack(null);
        transaction.commit();
    }

    private void snackMessage(String message, View root) {
        Snackbar.make(root, message, BaseTransientBottomBar.LENGTH_SHORT)
                .setBackgroundTint(getColor(R.color.darkest_grey))
                .setTextColor(getColor(R.color.white))
                .show();
    }

    private void setupBottomSheet() {
        binding.bottomView.setZ(binding.bottomView.getZ() + 1);
        BottomSheetBehavior<LinearLayout> behavior = BottomSheetBehavior.from(binding.bottomView);
        behavior.setDraggable(true);
        behavior.setPeekHeight(1000);
        behavior.setState(BottomSheetBehavior.STATE_HIDDEN);

        behavior.addBottomSheetCallback(new BottomSheetBehavior.BottomSheetCallback() {
            @Override
            public void onStateChanged(@NonNull View bottomSheet, int newState) {
                if (newState == BottomSheetBehavior.STATE_HIDDEN)
                    viewModel.setBottomSheetClosed();
            }

            @Override
            public void onSlide(@NonNull View bottomSheet, float slideOffset) {
            }
        });

        binding.add.setOnClickListener(v -> {
            if (binding.title.getText().toString().equals(getString(R.string.layers))) {
                viewModel.getLoadingState().postValue(true);
                chooseImage(100);
            }
            if (binding.title.getText().toString().equals(getString(R.string.effects)))
                showChooseEffectDialog();
        });
    }

    private void setupActionBarButtons() {
        MenuProvider menuProvider = new MenuProvider() {
            @Override
            public void onCreateMenu(@NonNull Menu menu, @NonNull MenuInflater menuInflater) {
                menuInflater.inflate(R.menu.menu_preview, menu);
            }

            @Override
            public boolean onMenuItemSelected(@NonNull MenuItem menuItem) {
                switch (menuItem.getItemId()) {
                    case R.id.apply:
                        viewModel.startPreview(WallpaperEditor.this);
                        break;
                    case R.id.pause:
                        viewModel.playPauseEngine();
                        menuItem.setTitle(R.string.resume);
                        break;
                    default:
                        break;
                }
                return false;
            }
        };
        addMenuProvider(menuProvider, this, Lifecycle.State.RESUMED);
    }

    @Override
    public boolean onSupportNavigateUp() {
        showSaveChangesDialog();
        return true;
    }

    @SuppressLint("CheckResult")
    /*private void showObjectParameters() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);

        ObjectParametersViewBinding dialogBinding = ObjectParametersViewBinding.bind(getLayoutInflater().inflate(R.layout.object_parameters_view, null));
        builder.setView(dialogBinding.getRoot());

        dialogBinding.title.setText(R.string.loading);
        dialogBinding.loadingIndicator.startShimmerAnimation();

        AlertDialog dialog = builder.create();
        dialog.getWindow().setBackgroundDrawableResource(R.drawable.rounded_bottom_dialog);
        dialog.show();

        Observable.just(engine.getDraggedSpriteId())
                .subscribeOn(Schedulers.io())
                .map(engine::getObject)
                .observeOn(AndroidSchedulers.mainThread())
                .delay(500, TimeUnit.MILLISECONDS, AndroidSchedulers.mainThread())
                .subscribe(actor -> {
                    //Position
                    dialogBinding.x.setText(String.valueOf(actor.getActorX()));
                    dialogBinding.y.setText(String.valueOf(actor.getActorY()));

                    //Scale
                    dialogBinding.scaleX.setText(String.valueOf(actor.getActorWidth()));
                    dialogBinding.scaleY.setText(String.valueOf(actor.getActorHeight()));

                    //Rotation
                    dialogBinding.rotation.setText(String.valueOf(actor.getActorRotation()));

                    dialogBinding.title.setText(actor.getName());
                    dialogBinding.button.setText(R.string.cancel);
                    dialogBinding.button.setBackgroundTintList(ColorStateList.valueOf(getColor(R.color.red)));
                    dialogBinding.button.setTextColor(ColorStateList.valueOf(getColor(R.color.red)));
                    dialogBinding.button2.setText(R.string.apply);

                    dialogBinding.button.setOnClickListener(view -> dialog.dismiss());
                    dialogBinding.button2.setOnClickListener(view -> {
                        if (checkInfoFields(dialogBinding.x) &&
                                checkInfoFields(dialogBinding.y) &&
                                checkInfoFields(dialogBinding.scaleX) &&
                                checkInfoFields(dialogBinding.scaleY) &&
                                checkInfoFields(dialogBinding.rotation)
                        ) {
                            actor.setActorPosition(
                                    Float.parseFloat(dialogBinding.x.getText().toString()),
                                    Float.parseFloat(dialogBinding.y.getText().toString())
                            );
                            actor.setActorHeight(
                                    Float.parseFloat(dialogBinding.scaleY.getText().toString())
                            );
                            actor.setActorWidth(
                                    Float.parseFloat(dialogBinding.scaleX.getText().toString())
                            );
                            actor.setActorRotation(
                                    Float.parseFloat(dialogBinding.rotation.getText().toString())
                            );
                            dialog.dismiss();
                        } else {
                            snackMessage(getString(R.string.errorEmptyFields), dialogBinding.getRoot());
                        }
                    });

                    dialogBinding.loadingIndicator.stopShimmerAnimation();
                    dialogBinding.loadingIndicator.setVisibility(View.GONE);
                    dialogBinding.positionLayout.setVisibility(View.VISIBLE);
                    dialogBinding.scaleLayout.setVisibility(View.VISIBLE);
                    dialogBinding.rotationLayout.setVisibility(View.VISIBLE);
                    dialogBinding.buttonsLayout.setVisibility(View.VISIBLE);
                }, throwable -> {
                    dialogBinding.loadingIndicator.stopShimmerAnimation();
                    dialogBinding.loadingIndicator.setVisibility(View.GONE);
                    dialogBinding.title.setText(R.string.error_getting_data);
                    dialogBinding.buttonsLayout.setVisibility(View.VISIBLE);
                    dialogBinding.button2.setVisibility(View.GONE);
                    dialogBinding.buttonsSpace.setVisibility(View.GONE);
                    dialogBinding.button.setBackgroundTintList(ColorStateList.valueOf(getColor(R.color.red)));
                    dialogBinding.button.setText(R.string.cancel);
                    dialogBinding.button.setTextColor(ColorStateList.valueOf(getColor(R.color.red)));
                    dialogBinding.button.setOnClickListener(v -> dialog.dismiss());
                });
    }*/



    private void showSaveChangesDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);

        StateViewBinding dialogBinding = StateViewBinding.bind(getLayoutInflater().inflate(R.layout.state_view, null));
        builder.setView(dialogBinding.getRoot());

        dialogBinding.title.setText(R.string.save_before_exit);
        dialogBinding.description.setText(R.string.unsaved_data_remove);
        dialogBinding.image.setImageResource(R.drawable.test_mascot);
        dialogBinding.button.setText(R.string.no);
        dialogBinding.button.setBackgroundTintList(ColorStateList.valueOf(getColor(R.color.red)));
        dialogBinding.button.setTextColor(ColorStateList.valueOf(getColor(R.color.red)));
        dialogBinding.button2.setText(R.string.yes);
        dialogBinding.button2.setVisibility(View.VISIBLE);
        dialogBinding.buttonsSpace.setVisibility(View.VISIBLE);

        AlertDialog dialog = builder.create();
        dialog.getWindow().setBackgroundDrawableResource(R.drawable.rounded_bottom_dialog);
        dialog.show();

        dialogBinding.button.setOnClickListener(view -> {
            dialog.dismiss();
            finish();
        });
        dialogBinding.button2.setOnClickListener(view -> {
            dialog.dismiss();
            viewModel.getLoadingState().postValue(true);
            Observable
                    .fromCallable(() -> {
                        viewModel.saveProject();
                        return true;
                    })
                    .subscribeOn(Schedulers.io())
                    .observeOn(AndroidSchedulers.mainThread())
                    .subscribe(nothing -> {
                        finish();
                    });
        });
    }

    @SuppressLint("CheckResult")
    private void showSceneParameters(){
        Observable
                .just(new SimpleInputBottomDialog.Builder(this)
                        .setTitle(getString(R.string.scene_parameters))
                        .addLabel("fdsfdsf")
                        .build())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribeOn(Schedulers.io())
                .subscribe(dialog -> {
                    dialog.show(getSupportFragmentManager(), "scene parameters");
                    dialog.setInputFieldCallback((typeName, value) -> {
                    });
                });
    }

    @SuppressLint("CheckResult")
    private void showObjectParameters(ActorHandler actor) {
        Observable
                .just(new SimpleInputBottomDialog.Builder(this)
                        .setTitle(actor.getName())
                        .addTextField("name", getString(R.string.name), "string", 1, Integer.MAX_VALUE, actor.getName())
                        .addTextField("xpos", "X", "float", Integer.MIN_VALUE, Integer.MAX_VALUE, actor.getActorX())
                        .addTextField("ypos", "Y", "float", Integer.MIN_VALUE, Integer.MAX_VALUE, actor.getActorY())
                        .addTextField("rot", getString(R.string.rotation), "float", 0, 360, actor.getActorRotation())
                        .addTextField("width", getString(R.string.width), "float", Integer.MIN_VALUE, Integer.MAX_VALUE, actor.getActorWidth())
                        .addTextField("height", getString(R.string.height), "float", Integer.MIN_VALUE, Integer.MAX_VALUE, actor.getActorHeight())
                        .build())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribeOn(Schedulers.io())
                .subscribe(dialog -> {
                    dialog.show(getSupportFragmentManager(), actor.getName() + " parameters");
                    dialog.setInputFieldCallback((typeName, value) -> {
                        if (typeName.equals("name")) {
                            actor.setName(value.toString());
                            actor.getImageActor().setName(value.toString());
                            getSupportActionBar().setTitle(getString(R.string.object_appbar_letter) + value);
                            //todo fix name setting
                        }
                        if (typeName.equals("xpos")) {
                            actor.setActorPosition(Float.parseFloat(value.toString()), actor.getActorY());
                        }
                        if (typeName.equals("ypos")) {
                            actor.setActorPosition(actor.getActorX(), Float.parseFloat(value.toString()));
                        }
                        if (typeName.equals("rot")) {
                            actor.setActorRotation(Float.parseFloat(value.toString()));
                        }
                        if (typeName.equals("width")) {
                            actor.setActorWidth(Float.parseFloat(value.toString()));
                        }
                        if (typeName.equals("height")) {
                            actor.setActorHeight(Float.parseFloat(value.toString()));
                        }
                    });
                });
    }

    @SuppressLint("CheckResult")
    private void showEffectParameters(BaseEffect effect) {
        Observable
                .just(new SimpleInputBottomDialog.Builder(this)
                        .setTitle(effect.getName())
                        .fromParameters(effect.getParameters())
                        .build())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribeOn(Schedulers.io())
                .subscribe(dialog -> {
                    dialog.show(getSupportFragmentManager(), effect.getName() + " parameters");
                    dialog.setParameterFieldCallback(field -> {
                        effect.onParameterChanged(field);
                        viewModel.onEffectParameterChanged(field);
                    });
                });
    }

    @SuppressLint("CheckResult")
    private void showChooseEffectDialog() {
        Observable
                .just(new SimpleInputBottomDialog.Builder(this)
                        .setTitle(getString(R.string.choose_effect))
                        .addImageButton(R.drawable.ic_tv, getString(R.string.glitch))
                        .addImageButton(R.drawable.ic_wind, getString(R.string.windy_swings))
                        .addImageButton(R.drawable.ic_move, getString(R.string.parallax))
                        .addImageButton(R.drawable.ic_shake, getString(R.string.shake))
                        .build())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribeOn(Schedulers.io())
                .subscribe(dialog -> {
                    dialog.show(getSupportFragmentManager(), getString(R.string.choose_effect));
                    dialog.setImageButtonCallback(text -> viewModel.addEffect(text));
                });
    }

    @Override
    public void onBackPressed() {
        showSaveChangesDialog();
    }

    @Override
    public void exit() {

    }

    private void hideLoading() {
        binding.loadingWindow.setVisibility(View.GONE);
        binding.loadingIndicator.stopShimmerAnimation();
    }

    private void showLoading() {
        binding.loadingWindow.setVisibility(View.VISIBLE);
        binding.loadingIndicator.startShimmerAnimation();
    }
}