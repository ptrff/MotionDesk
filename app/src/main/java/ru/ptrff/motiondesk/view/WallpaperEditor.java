package ru.ptrff.motiondesk.view;

import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.app.WallpaperManager;
import android.content.ComponentName;
import android.content.Intent;
import android.content.res.ColorStateList;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.graphics.Point;
import android.graphics.drawable.ColorDrawable;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.MediaStore;
import android.util.Log;
import android.view.Display;
import android.view.Gravity;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.PopupWindow;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;
import androidx.core.view.MenuProvider;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;
import androidx.lifecycle.Lifecycle;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.badlogic.gdx.backends.android.AndroidFragmentApplication;
import com.google.android.material.bottomsheet.BottomSheetBehavior;
import com.google.android.material.snackbar.BaseTransientBottomBar;
import com.google.android.material.snackbar.Snackbar;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.TimeUnit;

import io.reactivex.Observable;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.schedulers.Schedulers;
import ru.ptrff.motiondesk.R;
import ru.ptrff.motiondesk.adapters.ToolbarAdapter;
import ru.ptrff.motiondesk.data.ToolItem;
import ru.ptrff.motiondesk.data.WallpaperItem;
import ru.ptrff.motiondesk.databinding.ActivityWallpaperEditorBinding;
import ru.ptrff.motiondesk.databinding.ObjectParametersViewBinding;
import ru.ptrff.motiondesk.databinding.StateViewBinding;
import ru.ptrff.motiondesk.engine.EngineEventsListener;
import ru.ptrff.motiondesk.engine.WallpaperEditorEngine;
import ru.ptrff.motiondesk.engine.WallpaperLibGdxFragment;
import ru.ptrff.motiondesk.engine.WallpaperLibGdxService;
import ru.ptrff.motiondesk.utils.BitmapProcessor;
import ru.ptrff.motiondesk.utils.Converter;
import ru.ptrff.motiondesk.utils.IDGenerator;
import ru.ptrff.motiondesk.utils.ProjectManager;

public class WallpaperEditor extends AppCompatActivity implements AndroidFragmentApplication.Callbacks {

    private ActivityWallpaperEditorBinding binding;
    private ToolbarAdapter toolbarAdapter;
    private final List<ToolItem> tools = new ArrayList<>();
    private WallpaperLibGdxFragment libgdxFragment;
    private WallpaperEditorEngine engine;
    private WallpaperItem wallpaperItem;
    private String toolbarStatus;
    private String name;
    private int width;
    private int height;
    private PopupWindow loadingWindow;

    //BottomSheetFragments
    private final FragmentManager fm = getSupportFragmentManager();
    private LayerListFragment layerListFragment;
    private EffectsListFragment effectsListFragment;

    //Tools
    private final Map<String, OnToolClickListener> toolsRunnables = new HashMap<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityWallpaperEditorBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        try {
            setSupportActionBar(binding.toolbarTitle);
        } catch (Exception e) {
            e.printStackTrace();
        }

        if(getIntent().getBooleanExtra("new_project", true)){
            height = getIntent().getIntExtra("Height", 2340);
            width = getIntent().getIntExtra("Width", 2340);
            name = getIntent().getStringExtra("Name");
        }else{
            wallpaperItem = (WallpaperItem) getIntent().getSerializableExtra("wallpaper_item");
            height = wallpaperItem.getHeight();
            width = wallpaperItem.getWidth();
            name = wallpaperItem.getName();
        }

        engine = new WallpaperEditorEngine(width, height, engineEventsListener);
        engine.setResources(getResources());

        layerListFragment = new LayerListFragment(engine);
        effectsListFragment = new EffectsListFragment(engine);

        libgdxFragment = new WallpaperLibGdxFragment(engine);
        getSupportFragmentManager().beginTransaction().
                add(R.id.preview, libgdxFragment).
                commit();


        getSupportActionBar().setHomeAsUpIndicator(R.drawable.ic_back);
        getSupportActionBar().setHomeActionContentDescription(R.string.back);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setTitle(name);

        setupActionBarButtons();
        setupBottomSheet();

        setupToolsRunnables();
        setupToolbar();

        resetTools();
    }


    private void chooseImage(int code) {
        Intent intent = new Intent();
        intent.setType("image/*");
        intent.setAction(Intent.ACTION_GET_CONTENT);
        startActivityForResult(Intent.createChooser(intent, getString(R.string.choose_image)), code);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == RESULT_OK && data != null && data.getData() != null) {
            Uri uri = data.getData();
            try {
                Bitmap bitmap = MediaStore.Images.Media.getBitmap(getContentResolver(), uri);
                if (requestCode == 100) {
                    engine.addImage(bitmap);
                }
                if (requestCode == 101) {
                    saveProject(bitmap);
                }
            } catch (IOException e) {
                Log.e("WallpaperEditor", "Error getting image from gallery", e);
            }
        }
    }

    private void resetTools() {
        clearTools();
        toolbarStatus = "default";
        tools.add(new ToolItem(R.drawable.ic_add, getString(R.string.add)));
        tools.add(new ToolItem(R.drawable.ic_layers, getString(R.string.layer)));
        tools.add(new ToolItem(R.drawable.ic_center_cam, getString(R.string.center_obj)));
        setupToolbar();
        hideBottomSheet();
    }

    private void setupToolbar() {
        toolbarAdapter = new ToolbarAdapter(tools, toolClickListener);
        LinearLayoutManager sGrid = new LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false);
        binding.toolbarTools.setLayoutManager(sGrid);
        binding.toolbarTools.setAdapter(toolbarAdapter);
    }

    private void showEditObjectTools() {
        clearTools();
        toolbarStatus = "image_params";
        tools.add(new ToolItem(R.drawable.ic_parameters, getString(R.string.parameters)));
        tools.add(new ToolItem(R.drawable.ic_mask, getString(R.string.mask)));
        tools.add(new ToolItem(R.drawable.ic_effects, getString(R.string.effects)));
        tools.add(new ToolItem(R.drawable.ic_delete, getString(R.string.remove)));
        setupToolbar();
    }

    private void showDrawingMaskTools() {
        clearTools();
        toolbarStatus = "drawing_mask";
        tools.add(new ToolItem(R.drawable.ic_done, getString(R.string.apply)));
        tools.add(new ToolItem(R.drawable.ic_center_cam, getString(R.string.center_obj)));
        tools.add(new ToolItem(R.drawable.ic_brush, getString(R.string.brush)));
        tools.add(new ToolItem(R.drawable.ic_negative, getString(R.string.negative)));
        tools.add(new ToolItem(R.drawable.ic_delete, getString(R.string.remove)));
        setupToolbar();
    }

    private void clearTools() {
        tools.clear();
        toolbarAdapter.notifyDataSetChanged();
    }

    private void setupToolsRunnables() {
        toolsRunnables.put(
                getString(R.string.add),
                tool -> chooseImage(100)
        );
        toolsRunnables.put(
                getString(R.string.remove),
                tool -> engine.removeObject()
        );
        toolsRunnables.put(
                getString(R.string.layers),
                tool -> selectBottomContent(tool, layerListFragment)
        );
        toolsRunnables.put(
                getString(R.string.center_obj),
                tool -> {
                    if (engine.isObjectSelected()) engine.centerCamera(engine.getDraggedSpriteId());
                    else engine.centerCamera(-1);
                }
        );
        toolsRunnables.put(
                getString(R.string.parameters),
                tool -> selectBottomContent(tool, new LibFragment())
        );
        toolsRunnables.put(
                getString(R.string.mask),
                tool -> engine.startDrawingMask()
        );
        toolsRunnables.put(
                getString(R.string.effects),
                tool -> selectBottomContent(tool, effectsListFragment)
        );
        toolsRunnables.put(
                getString(R.string.apply),
                tool -> {
                    engine.stopDrawingMask();
                    showEditObjectTools();
                }
        );
        toolsRunnables.put(
                getString(R.string.parameters),
                tool -> showObjectParameters()
        );
    }

    private final ToolbarAdapter.OnImageClickListener toolClickListener = tool -> {
        OnToolClickListener action = toolsRunnables.get(tool.getLabel());
        if (action != null) {
            action.onClick(tool);
        } else {
            snackMessage(getString(R.string.instrumentGettingError), binding.getRoot());
        }
    };

    private void selectBottomContent(ToolItem tool, Fragment fragment) {
        BottomSheetBehavior.from(binding.bottomView).setState(BottomSheetBehavior.STATE_COLLAPSED);
        binding.title.setText(tool.getLabel());
        binding.icon.setImageResource(tool.getImageResourse());
        FragmentTransaction transaction = fm.beginTransaction();
        transaction.replace(binding.content.getId(), fragment);
        transaction.setTransition(FragmentTransaction.TRANSIT_FRAGMENT_FADE);
        transaction.addToBackStack(null);
        transaction.commit();
    }

    private final EngineEventsListener engineEventsListener = new EngineEventsListener() {
        @Override
        public void onObjectSelected(String type, int index) {
            if (!Objects.equals(toolbarStatus, "image_params") || engine.getObject(index).getName() != getSupportActionBar().getTitle())
                runOnUiThread(() -> {
                    showEditObjectTools();
                    getSupportActionBar().setTitle(engine.getObject(index).getName());
                });
        }

        @Override
        public void onObjectNotSelected() {
            if (!Objects.equals(toolbarStatus, "default"))
                runOnUiThread(() -> {
                    resetTools();
                    getSupportActionBar().setTitle(name);
                });
        }

        @Override
        public void onObjectAdded(int position) {
            if (binding.title.getText().equals(getString(R.string.layers))) {
                layerListFragment.notifyItemInserted(position);
            }
        }

        @Override
        public void onObjectRemoved(int position) {
            BottomSheetBehavior behavior = BottomSheetBehavior.from(binding.bottomView);
            if (binding.title.getText().equals(getString(R.string.layers)) && behavior.getState() != BottomSheetBehavior.STATE_HIDDEN) {
                layerListFragment.notifyItemRemoved(position);
            }
        }

        @Override
        public void onStartDrawingMask(int index) {
            showDrawingMaskTools();
            getSupportActionBar().setTitle(engine.getObject(index).getName());
        }

        @Override
        public void onStopDrawingMask() {

        }
    };

    private void snackMessage(String message, View root) {
        Snackbar.make(root, message, BaseTransientBottomBar.LENGTH_SHORT)
                .setBackgroundTint(getColor(R.color.darkest_grey))
                .setTextColor(getColor(R.color.white))
                .show();
    }

    private void hideBottomSheet() {
        BottomSheetBehavior.from(binding.bottomView).setState(BottomSheetBehavior.STATE_HIDDEN);
    }

    private void setupBottomSheet() {
        binding.bottomView.setZ(binding.bottomView.getZ() + 1);
        BottomSheetBehavior behavior = BottomSheetBehavior.from(binding.bottomView);
        behavior.setDraggable(true);
        behavior.setPeekHeight(1000);
        behavior.setState(BottomSheetBehavior.STATE_HIDDEN);
        behavior.addBottomSheetCallback(new BottomSheetBehavior.BottomSheetCallback() {
            @Override
            public void onStateChanged(@NonNull View bottomSheet, int newState) {
                if (newState == BottomSheetBehavior.STATE_HIDDEN) {
                    //binding.content.removeAllViews();
                }
                if (newState == BottomSheetBehavior.STATE_EXPANDED || newState == BottomSheetBehavior.STATE_COLLAPSED) {

                }
            }

            @Override
            public void onSlide(@NonNull View bottomSheet, float slideOffset) {

            }
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
                        Intent intent = new Intent(
                                WallpaperManager.ACTION_CHANGE_LIVE_WALLPAPER);
                        intent.putExtra(WallpaperManager.EXTRA_LIVE_WALLPAPER_COMPONENT, new ComponentName(WallpaperEditor.this, WallpaperLibGdxService.class));
                        startActivity(intent);
                        break;
                    case R.id.pause:
                        engine.playPause();
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
    private void showObjectParameters() {
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
    }

    public boolean checkInfoFields(EditText editText) {
        String text = editText.getText().toString().trim();

        if (text.isEmpty()) return false;

        try {
            Float.parseFloat(text);
        } catch (NumberFormatException e) {
            return false;
        }

        return true;
    }

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
            chooseImage(101);
            dialog.dismiss();
        });
    }

    @Override
    public void onBackPressed() {
        showSaveChangesDialog();
    }

    @Override
    public void exit() {

    }

    @SuppressLint("CheckResult")
    private void saveProject(Bitmap preview) {
        showLoading();

        if (wallpaperItem == null) {
            createNewWallpaperItem();
        }

        Observable.just(BitmapProcessor.scaleBitmap(preview, 360, 540))
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .delay(1, TimeUnit.SECONDS)
                .subscribe(bitmap -> {
                    hideLoading();
                    ProjectManager.createProject(
                            this,
                            bitmap,
                            wallpaperItem,
                            engine.getZipMaster()
                    );
                    finish();
                }, throwable -> {
                    hideLoading();
                    snackMessage(getString(R.string.saving_project_error), binding.getRoot());
                    Log.e("WallpaperEditor", getString(R.string.saving_project_error), throwable);
                });
    }

    private void createNewWallpaperItem() {
        String author = "";
        String description = "";
        String rating = "";
        wallpaperItem = new WallpaperItem(
                false,
                IDGenerator.generateID(),
                name,
                author,
                description,
                width,
                height,
                "scene",
                rating,
                "");
    }

    private void hideLoading(){
        runOnUiThread(() -> loadingWindow.dismiss());
    }

    private void showLoading() {
        Display display = getWindowManager().getDefaultDisplay();
        Point size = new Point();
        display.getSize(size);
        int windowSize = Converter.dpToPx(150);

        LinearLayout rootView = new LinearLayout(this);
        rootView.setGravity(Gravity.CENTER);

        CardView cardMask = new CardView(this);
        cardMask.setRadius(50);

        ShimmerView shimmerView = new ShimmerView(this, true);
        shimmerView.setLayoutParams(new LinearLayout.LayoutParams(windowSize, windowSize));
        shimmerView.startShimmerAnimation();
        shimmerView.setRadius(20);
        shimmerView.setShimmerAnimationDuration(1500);

        cardMask.addView(shimmerView);
        rootView.addView(cardMask);

        loadingWindow = new PopupWindow(rootView, LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.MATCH_PARENT);
        ColorDrawable colorDrawable = new ColorDrawable(Color.BLACK);
        colorDrawable.setAlpha(128);
        loadingWindow.setBackgroundDrawable(colorDrawable);
        loadingWindow.setBackgroundDrawable(colorDrawable);
        loadingWindow.setElevation(10);

        loadingWindow.setAnimationStyle(android.R.style.Animation_Toast);
        loadingWindow.setOutsideTouchable(false);
        loadingWindow.showAtLocation(binding.content, Gravity.CENTER, 0, 0);
    }
}