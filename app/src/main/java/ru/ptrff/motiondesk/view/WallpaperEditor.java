package ru.ptrff.motiondesk.view;

import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.content.Intent;
import android.content.res.ColorStateList;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.graphics.ColorSpace;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.transition.Explode;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.Toast;

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
import com.jaredrummler.android.colorpicker.ColorPickerDialog;
import com.jaredrummler.android.colorpicker.ColorPickerDialogListener;

import java.util.Objects;

import io.reactivex.Observable;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.CompositeDisposable;
import io.reactivex.schedulers.Schedulers;
import ru.ptrff.motiondesk.R;
import ru.ptrff.motiondesk.adapters.EffectsListAdapter;
import ru.ptrff.motiondesk.adapters.ToolbarAdapter;
import ru.ptrff.motiondesk.databinding.ActivityWallpaperEditorBinding;
import ru.ptrff.motiondesk.databinding.ColorPickerViewBinding;
import ru.ptrff.motiondesk.databinding.StateViewBinding;
import ru.ptrff.motiondesk.engine.WallpaperLibGdxFragment;
import ru.ptrff.motiondesk.engine.effects.BaseEffect;
import ru.ptrff.motiondesk.engine.scene.ActorHandler;
import ru.ptrff.motiondesk.engine.scene.EngineEventsListener;
import ru.ptrff.motiondesk.engine.scene.WallpaperEditorEngine;
import ru.ptrff.motiondesk.engine.scene.WallpaperEngine;
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
            engine = new WallpaperEditorEngine(viewModel.getWidth(), viewModel.getHeight(), this);
        } else {
            WallpaperItem wallpaperItem = (WallpaperItem) getIntent().getSerializableExtra("wallpaper_item");
            viewModel.loadWallpaperItem(wallpaperItem);
            engine = new WallpaperEditorEngine(wallpaperItem, wallpaperItem.getId(), this, this);
        }

        setupEditorEngine();

        binding.loadingIndicator.startShimmerAnimation();

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
    public void setupEditorEngine() {
        viewModel.setEngine(engine);
        setupEnvironment(engine);
        engine.setResources(getResources());
        WallpaperLibGdxFragment libgdxFragment = new WallpaperLibGdxFragment(engine);
        getSupportFragmentManager().beginTransaction().
                add(R.id.preview, libgdxFragment).
                commit();
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
                message -> {
                    snackMessage(message, binding.getRoot());
                }
        );

        viewModel.getToastMessageLiveData().observe(this,
                message -> {
                    Toast.makeText(WallpaperEditor.this, message, Toast.LENGTH_SHORT).show();
                }
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
    public void onSceneLoaded(){
        viewModel.getLoadingState().postValue(false);
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
                    case R.id.save:
                        viewModel.saveProject();
                        break;
                    case R.id.play_pause:
                        if (engine.playPause()) {
                            menuItem.setTitle(R.string.stop);
                            menuItem.setIcon(R.drawable.ic_pause);
                        } else {
                            menuItem.setTitle(R.string.resume);
                            menuItem.setIcon(R.drawable.ic_play);
                        }
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
    private void showSaveChangesDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);

        StateViewBinding dialogBinding = StateViewBinding.bind(getLayoutInflater().inflate(R.layout.state_view, null));
        builder.setView(dialogBinding.getRoot());

        dialogBinding.title.setText(R.string.save_before_exit);
        dialogBinding.description.setText(R.string.unsaved_data_remove);
        dialogBinding.image.setVisibility(View.GONE);
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
    private void showSceneParameters() {
        Observable
                .just(new SimpleInputBottomDialog.Builder(this)
                        .setTitle(getString(R.string.scene_parameters))
                        .addTextField("backgroundColor", getString(R.string.background_color), "color", Integer.MIN_VALUE, Integer.MAX_VALUE, engine.getBackgroundColor())
                        .build())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribeOn(Schedulers.io())
                .subscribe(dialog -> {
                    dialog.show(getSupportFragmentManager(), "scene parameters");
                    dialog.setInputFieldCallback((typeName, value) -> {});
                    dialog.setColorPickerCallback(editText -> {
                        AlertDialog.Builder builder = new AlertDialog.Builder(this);

                        ColorPickerViewBinding dialogBinding = ColorPickerViewBinding.bind(getLayoutInflater().inflate(R.layout.color_picker_view, null));
                        builder.setView(dialogBinding.getRoot());

                        AlertDialog colorPickerDialog = builder.create();
                        colorPickerDialog.getWindow().setBackgroundDrawableResource(R.drawable.rounded_bottom_dialog);
                        colorPickerDialog.show();

                        dialogBinding.colorPicker.setColor(engine.getBackgroundColor().toIntBits());

                        dialogBinding.apply.setOnClickListener(v -> {
                            String color = String.format("#%06X", (0xFFFFFF & (dialogBinding.colorPicker.getColor())));
                            editText.setText(color);
                            engine.setBackgroundColor(color);
                            colorPickerDialog.dismiss();
                        });

                        dialogBinding.cancel.setOnClickListener(v -> colorPickerDialog.dismiss());
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
                            actor.getImageActor().setName(value.toString());
                            getSupportActionBar().setTitle(getString(R.string.object_appbar_letter) + value);
                            if(Boolean.TRUE.equals(viewModel.getBottomSheetOpenedLiveData().getValue()) &&
                                    Objects.equals(viewModel.getCurrentBottomContentLiveData().getValue(), "layers")){
                                layerListFragment.onResume();
                            }
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
    }

    private void showLoading() {
        binding.loadingWindow.setVisibility(View.VISIBLE);
        binding.loadingIndicator.startShimmerAnimation();
    }
}