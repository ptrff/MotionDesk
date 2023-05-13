package ru.ptrff.motiondesk.viewmodel;

import android.annotation.SuppressLint;
import android.app.Application;
import android.content.Context;
import android.content.SharedPreferences;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.MutableLiveData;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

import io.reactivex.Observable;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.schedulers.Schedulers;
import ru.ptrff.motiondesk.R;
import ru.ptrff.motiondesk.engine.WallpaperLibGdxFragment;
import ru.ptrff.motiondesk.engine.effects.BaseEffect;
import ru.ptrff.motiondesk.engine.scene.WallpaperEditorEngine;
import ru.ptrff.motiondesk.models.ParameterField;
import ru.ptrff.motiondesk.models.ToolItem;
import ru.ptrff.motiondesk.models.WallpaperItem;
import ru.ptrff.motiondesk.utils.BitmapProcessor;
import ru.ptrff.motiondesk.utils.IDGenerator;
import ru.ptrff.motiondesk.utils.ProjectManager;
import ru.ptrff.motiondesk.view.ProjectInfoFragment;

public class EditorViewModel extends AndroidViewModel {

    //General
    private final MutableLiveData<String> appBarTextLiveData = new MutableLiveData<>();
    private final MutableLiveData<Boolean> loadingState = new MutableLiveData<>();
    private final MutableLiveData<String> snackMessageLiveData = new MutableLiveData<>();
    private final MutableLiveData<Boolean> bottomSheetOpenedLiveData = new MutableLiveData<>();
    private final MutableLiveData<Void> pickImageLiveData = new MutableLiveData<>();
    private final MutableLiveData<Void> startPreviewLiveData = new MutableLiveData<>();
    private final Resources resources;
    private String toolbarStatus = "default";

    //Dialogs
    private final MutableLiveData<Void> showProjectParameters = new MutableLiveData<>();
    private final MutableLiveData<Void> showProjectInformation = new MutableLiveData<>();
    private final MutableLiveData<Void> showObjectParameters = new MutableLiveData<>();
    private final MutableLiveData<Void> showEffectParameters = new MutableLiveData<>();
    private ProjectInfoFragment projectInfoFragment;

    //EditorEngine
    private final MutableLiveData<WallpaperLibGdxFragment> libgdxFragmentLiveData = new MutableLiveData<>();
    private WallpaperEditorEngine engine;
    private WallpaperItem wallpaperItem;
    private int width;
    private int height;
    private String name;
    private BaseEffect currentEffect;

    //EditorEngineEvents
    private final MutableLiveData<Void> updateEffectsList = new MutableLiveData<>();

    //Tools
    private final MutableLiveData<List<ToolItem>> toolbarToolsLiveData = new MutableLiveData<>();
    private final Map<String, Runnable> toolsRunnables = new HashMap<>();
    private final List<ToolItem> defaultTools = new ArrayList<>();
    private final List<ToolItem> editObjectTools = new ArrayList<>();
    private final List<ToolItem> drawingMaskTools = new ArrayList<>();
    private final List<ToolItem> editEffectTools = new ArrayList<>();

    //BottomSheetFragments
    private final MutableLiveData<String> currentBottomContentLiveData = new MutableLiveData<>();
    private final MutableLiveData<Void> bottomSheetUpdateActionLiveData = new MutableLiveData<>();
    private final MutableLiveData<Integer> bottomSheetRemovePositionLiveData = new MutableLiveData<>();
    private final MutableLiveData<Integer> bottomSheetInsertPositionLiveData = new MutableLiveData<>();

    @SuppressLint("CheckResult")
    public EditorViewModel(@NonNull Application application) {
        super(application);

        resources = getApplication().getResources();

        //setup toolbar
        setupToolsRunnables();
        setupToolbar();
        resetTools();
    }

    public void resetTools() {
        toolbarStatus = "default";
        appBarTextLiveData.postValue(name);
        toolbarToolsLiveData.postValue(defaultTools);
        bottomSheetOpenedLiveData.postValue(false);
    }

    public void showEditObjectTools() {
        toolbarStatus = "image_params";
        toolbarToolsLiveData.postValue(editObjectTools);
        appBarTextLiveData.postValue(resources.getString(R.string.object_appbar_letter) + engine.getObject(engine.getDraggedSpriteId()).getName());
    }

    public void showDrawingMaskTools() {
        toolbarStatus = "drawing_mask";
        toolbarToolsLiveData.postValue(drawingMaskTools);
    }

    public void showEditEffectTools() {
        toolbarStatus = "edit_effect";
        toolbarToolsLiveData.postValue(editEffectTools);
        appBarTextLiveData.postValue(resources.getString(R.string.effect_appbar_letter) + currentEffect.getName());
    }

    public void addEffect(String name) {
        engine.addEffect(name);
    }

    private void setupToolsRunnables() {
        toolsRunnables.put(
                resources.getString(R.string.remove),
                () -> {
                    if (Objects.equals(toolbarStatus, "image_params"))
                        engine.removeObject();
                    if (Objects.equals(toolbarStatus, "edit_effect")) {
                        engine.getObject(engine.getDraggedSpriteId()).removeEffect(currentEffect);
                        showEditObjectTools();
                        bottomSheetUpdateActionLiveData.postValue(null);
                        //effectsListFragment.onResume();
                    }
                }
        );
        toolsRunnables.put(
                resources.getString(R.string.layers),
                () -> selectBottomContent("layers")
        );
        toolsRunnables.put(
                resources.getString(R.string.center_obj),
                () -> {
                    if (engine.isObjectSelected()) engine.centerCamera(engine.getDraggedSpriteId());
                    else engine.centerCamera(-1);
                }
        );
        toolsRunnables.put(
                resources.getString(R.string.information),
                () -> {
                    showProjectInformation.postValue(null);
                }
        );
        toolsRunnables.put(
                resources.getString(R.string.parameters),
                () -> {
                    if (Objects.equals(toolbarStatus, "default")) {
                        showProjectParameters.postValue(null);
                    }
                    if (Objects.equals(toolbarStatus, "image_params"))
                        showObjectParameters.postValue(null);
                    if (Objects.equals(toolbarStatus, "edit_effect"))
                        showEffectParameters.postValue(null);
                }
        );
        toolsRunnables.put(
                resources.getString(R.string.mask),
                () -> engine.startDrawingMask()
        );
        toolsRunnables.put(
                resources.getString(R.string.effects),
                () -> selectBottomContent("effects")
        );
        toolsRunnables.put(
                resources.getString(R.string.apply),
                () -> {
                    engine.stopDrawingMask();
                    showEditObjectTools();
                }
        );
        toolsRunnables.put(
                resources.getString(R.string.save),
                () -> {
                    loadingState.postValue(true);
                    saveProject();
                }
        );
    }

    public void onToolbarToolClicked(ToolItem tool) {
        Runnable action = toolsRunnables.get(tool.getLabel());
        if (action != null) {
            action.run();
        } else
            snackMessageLiveData.postValue(resources.getString(R.string.instrumentGettingError));
    }

    private void selectBottomContent(String contentName) {
        currentBottomContentLiveData.postValue(contentName);
        System.out.println("VIEEEEEE");
        if (Boolean.FALSE.equals(bottomSheetOpenedLiveData.getValue()))
            bottomSheetOpenedLiveData.setValue(true);
        //bottomSheetFragmentLiveData.postValue(fragment);
    }

    public void setBottomSheetClosed() {
        bottomSheetOpenedLiveData.postValue(false);
    }

    private void setupToolbar() {
        defaultTools.add(new ToolItem(R.drawable.ic_save, resources.getString(R.string.save)));
        defaultTools.add(new ToolItem(R.drawable.ic_info, resources.getString(R.string.information)));
        defaultTools.add(new ToolItem(R.drawable.ic_parameters, resources.getString(R.string.parameters)));
        defaultTools.add(new ToolItem(R.drawable.ic_layers, resources.getString(R.string.layer)));
        defaultTools.add(new ToolItem(R.drawable.ic_center_cam, resources.getString(R.string.center_obj)));

        editObjectTools.add(new ToolItem(R.drawable.ic_parameters, resources.getString(R.string.parameters)));
        editObjectTools.add(new ToolItem(R.drawable.ic_mask, resources.getString(R.string.mask)));
        editObjectTools.add(new ToolItem(R.drawable.ic_effects, resources.getString(R.string.effects)));
        editObjectTools.add(new ToolItem(R.drawable.ic_delete, resources.getString(R.string.remove)));

        drawingMaskTools.add(new ToolItem(R.drawable.ic_done, resources.getString(R.string.apply)));
        drawingMaskTools.add(new ToolItem(R.drawable.ic_center_cam, resources.getString(R.string.center_obj)));
        drawingMaskTools.add(new ToolItem(R.drawable.ic_brush, resources.getString(R.string.brush)));
        drawingMaskTools.add(new ToolItem(R.drawable.ic_negative, resources.getString(R.string.negative)));
        drawingMaskTools.add(new ToolItem(R.drawable.ic_delete, resources.getString(R.string.remove)));

        editEffectTools.add(new ToolItem(R.drawable.ic_parameters, resources.getString(R.string.parameters)));
        editEffectTools.add(new ToolItem(R.drawable.ic_delete, resources.getString(R.string.remove)));
    }

    public void loadWallpaperItem(WallpaperItem wallpaperItem) {
        width = wallpaperItem.getWidth();
        height = wallpaperItem.getHeight();
        name = wallpaperItem.getName();
        appBarTextLiveData.postValue(name);
        this.wallpaperItem = wallpaperItem;
    }

    public void createNewWallpaperItem() {
        wallpaperItem = new WallpaperItem(
                true,
                IDGenerator.generateID(),
                name,
                "",
                "",
                width,
                height,
                "2d_scene",
                0,
                false
        );
    }

    public void onEffectParameterChanged(ParameterField field) {
        if (field.getTypeName().equals("name")) {
            if (toolbarStatus.equals("edit_effect"))
                appBarTextLiveData.postValue(resources.getString(R.string.effect_appbar_letter) + currentEffect.getName());

            bottomSheetUpdateActionLiveData.postValue(null);
        }
    }

    public void onImagePicked(int code, Bitmap bitmap) {
        if (code == 100) {
            engine.addImage(bitmap);
        }
    }

    private Observable<Bitmap> resizeImage(Bitmap bitmap) {
        return Observable.fromCallable(() -> BitmapProcessor.scaleBitmap(bitmap, 360, 540));
    }

    @SuppressLint("CheckResult")
    public void saveProject() {
        Observable
                .just(engine.getZipMaster())
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(zipMaster -> {
                    ProjectManager.createProject(
                            getApplication(),
                            wallpaperItem,
                            zipMaster
                    );
                    loadingState.postValue(false);
                }, throwable -> {
                    loadingState.postValue(false);
                    snackMessageLiveData.postValue(resources.getString(R.string.saving_project_error));
                    Log.e("WallpaperEditor", resources.getString(R.string.saving_project_error), throwable);
                });
    }

    @SuppressLint("CheckResult")
    public void startPreview(Context context) {
        loadingState.postValue(true);
        Observable.fromCallable(() -> {
                    saveProject();
                    return true;
                })
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .flatMap(result -> Observable.fromCallable(() -> {
                    ProjectManager.unpackProjectToCurrent(context, wallpaperItem.getId());
                    SharedPreferences sharedPreferences = context.getSharedPreferences("MotionDesk", Context.MODE_PRIVATE);
                    SharedPreferences.Editor editor = sharedPreferences.edit();
                    editor.putString("current", wallpaperItem.getId());
                    editor.apply();
                    return true;
                }))
                .subscribe(result -> {
                    startPreviewLiveData.postValue(null);
                }, error -> {
                    snackMessageLiveData.postValue(context.getString(R.string.error_start_preview));
                    Log.e("WallpaperEditor", "Error starting preview", error);
                });
    }

    public void setWidth(int width) {
        this.width = width;
    }

    public void setHeight(int height) {
        this.height = height;
    }

    public void setName(String name) {
        this.name = name;
        appBarTextLiveData.postValue(name);
    }

    public void setEngine(WallpaperEditorEngine engine) {
        this.engine = engine;
    }

    public void setCurrentEffect(BaseEffect effect) {
        currentEffect = effect;
    }

    public void onProjectInfoChanged() {
        if (width != wallpaperItem.getWidth() || height != wallpaperItem.getHeight()) {
            width = wallpaperItem.getWidth();
            height = wallpaperItem.getHeight();
            engine.updateWorkingAreaResolution(width, height);
        }
        if (!Objects.equals(wallpaperItem.getName(), name)) {
            name = wallpaperItem.getName();
            appBarTextLiveData.postValue(name);
        }
    }

    public void playPauseEngine() {
        engine.playPause();
    }

    public String getName() {
        return name;
    }

    public int getWidth() {
        return width;
    }

    public int getHeight() {
        return height;
    }

    public String getToolbarStatus() {
        return toolbarStatus;
    }

    public WallpaperItem getWallpaperItem() {
        return wallpaperItem;
    }

    public MutableLiveData<Void> getStartPreviewLiveData() {
        return startPreviewLiveData;
    }

    public MutableLiveData<Integer> getBottomSheetRemovePositionLiveData() {
        return bottomSheetRemovePositionLiveData;
    }

    public MutableLiveData<Integer> getBottomSheetInsertPositionLiveData() {
        return bottomSheetInsertPositionLiveData;
    }

    public MutableLiveData<Boolean> getLoadingState() {
        return loadingState;
    }

    public BaseEffect getCurrentEffect() {
        return currentEffect;
    }

    public MutableLiveData<Void> getShowEffectParameters() {
        return showEffectParameters;
    }

    public ProjectInfoFragment getProjectParametersFragment() {
        return projectInfoFragment;
    }

    public MutableLiveData<Void> getShowProjectParameters() {
        return showProjectParameters;
    }

    public MutableLiveData<WallpaperLibGdxFragment> getLibgdxFragmentLiveData() {
        return libgdxFragmentLiveData;
    }

    public MutableLiveData<List<ToolItem>> getToolbarToolsLiveData() {
        return toolbarToolsLiveData;
    }

    public MutableLiveData<String> getCurrentBottomContentLiveData() {
        return currentBottomContentLiveData;
    }

    public MutableLiveData<Void> getBottomSheetUpdateActionLiveData() {
        return bottomSheetUpdateActionLiveData;
    }

    public MutableLiveData<Boolean> getBottomSheetOpenedLiveData() {
        return bottomSheetOpenedLiveData;
    }

    public MutableLiveData<String> getAppBarTextLiveData() {
        return appBarTextLiveData;
    }

    public MutableLiveData<String> getSnackMessageLiveData() {
        return snackMessageLiveData;
    }

    public MutableLiveData<Void> getUpdateEffectsList() {
        return updateEffectsList;
    }

    public MutableLiveData<Void> getShowObjectParameters() {
        return showObjectParameters;
    }

    public MutableLiveData<Void> getShowProjectInformation() {
        return showProjectInformation;
    }

    public MutableLiveData<Void> getPickImageLiveData() {
        return pickImageLiveData;
    }
}