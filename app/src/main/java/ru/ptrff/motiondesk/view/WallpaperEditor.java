package ru.ptrff.motiondesk.view;

import android.app.AlertDialog;
import android.app.UiModeManager;
import android.app.WallpaperManager;
import android.content.ComponentName;
import android.content.Intent;
import android.content.res.ColorStateList;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.view.MenuProvider;
import androidx.fragment.app.FragmentManager;
import androidx.lifecycle.Lifecycle;
import androidx.recyclerview.widget.ItemTouchHelper;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.badlogic.gdx.backends.android.AndroidFragmentApplication;
import com.google.android.material.bottomsheet.BottomSheetBehavior;

import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import ru.ptrff.motiondesk.R;
import ru.ptrff.motiondesk.adapters.LayerListAdapter;
import ru.ptrff.motiondesk.adapters.ToolbarAdapter;
import ru.ptrff.motiondesk.data.ToolItem;
import ru.ptrff.motiondesk.databinding.ActivityWallpaperEditorBinding;
import ru.ptrff.motiondesk.databinding.StateViewBinding;
import ru.ptrff.motiondesk.engine.ActorHandler;
import ru.ptrff.motiondesk.engine.EngineEventsListener;
import ru.ptrff.motiondesk.engine.ImageActor;
import ru.ptrff.motiondesk.engine.WallpaperEditorEngine;
import ru.ptrff.motiondesk.engine.WallpaperLibGdxFragment;
import ru.ptrff.motiondesk.engine.WallpaperLibGdxService;

public class WallpaperEditor extends AppCompatActivity implements AndroidFragmentApplication.Callbacks, LayerListAdapter.LayerListeners {

    private ActivityWallpaperEditorBinding binding;
    private ToolbarAdapter toolbarAdapter;
    private final List<ToolItem> tools = new ArrayList<>();
    private WallpaperLibGdxFragment libgdxFragment;
    private WallpaperEditorEngine engine;
    private String toolbarStatus;
    private String name;
    private int width;
    private int height;

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
        width = getIntent().getIntExtra("Width", 1080);
        height = getIntent().getIntExtra("Height", 2340);
        name = getIntent().getStringExtra("Name");

        engine = new WallpaperEditorEngine(width, height, engineEventsListener);

        libgdxFragment = new WallpaperLibGdxFragment(engine);
        getSupportFragmentManager().beginTransaction().
                add(R.id.preview, libgdxFragment).
                commit();

        getSupportActionBar().setHomeAsUpIndicator(R.drawable.ic_back);
        getSupportActionBar().setHomeActionContentDescription("Назад");
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setTitle(name);

        setupActionBarButtons();
        setupBottomSheet();

        setupToolbar();

        resetTools();
    }


    private void chooseImage() {
        Intent intent = new Intent();
        intent.setType("image/*");
        intent.setAction(Intent.ACTION_GET_CONTENT);
        startActivityForResult(Intent.createChooser(intent, "Выберите картинку"), 100);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == 100 && resultCode == RESULT_OK && data != null && data.getData() != null) {
            Uri uri = data.getData();

            try {
                Bitmap bitmap = MediaStore.Images.Media.getBitmap(getContentResolver(), uri);
                engine.addImage(bitmap);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    private void resetTools() {
        clearTools();
        toolbarStatus = "default";
        tools.add(new ToolItem(R.drawable.ic_add, "Добавить"));
        tools.add(new ToolItem(R.drawable.ic_layers, "Слои"));
        setupToolbar();
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
        tools.add(new ToolItem(R.drawable.ic_info, "Информация"));
        tools.add(new ToolItem(R.drawable.ic_parameters, "Параметры"));
        tools.add(new ToolItem(R.drawable.ic_effects, "Эффекты"));
        tools.add(new ToolItem(R.drawable.ic_delete, "Удалить"));
        setupToolbar();
    }

    private void clearTools() {
        tools.clear();
        toolbarAdapter.notifyDataSetChanged();
    }

    ItemTouchHelper touchHelper;
    private final ToolbarAdapter.OnImageClickListener toolClickListener = new ToolbarAdapter.OnImageClickListener() {
        @Override
        public void onImageClick(ToolItem tool) {
            switch (tool.getLabel()) {
                case "Добавить":
                    chooseImage();
                    break;
                case "Удалить":
                    engine.removeObject();
                    break;
                default:
                    BottomSheetBehavior.from(binding.bottomView).setState(BottomSheetBehavior.STATE_COLLAPSED);
                    binding.title.setText(tool.getLabel());
                    binding.icon.setImageResource(tool.getImageResourse());
                    binding.content.addView(getLayoutInflater().inflate(R.layout.fragment_editor_layers, null));
                    RecyclerView recyclerView = binding.content.findViewById(R.id.layer_list);
                    LayerListAdapter adapter = new LayerListAdapter(engine.getObjectList(), engine.getStageActorArray(), WallpaperEditor.this);
                    recyclerView.setLayoutManager(new LinearLayoutManager(getApplicationContext(), LinearLayoutManager.VERTICAL, true));
                    recyclerView.setAdapter(adapter);

                    ItemTouchHelper.Callback callback = new ItemMoveCallback(adapter);
                    touchHelper = new ItemTouchHelper(callback);
                    touchHelper.attachToRecyclerView(recyclerView);
                    break;
            }
        }
    };

    private final EngineEventsListener engineEventsListener = new EngineEventsListener() {
        @Override
        public void onObjectSelected(String type, int index) {
            if (!Objects.equals(toolbarStatus, "image_params"))
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
            if(binding.title.getText().equals("Слои")){
                ((RecyclerView) binding.content.findViewById(R.id.layer_list))
                        .getAdapter().notifyItemInserted(position);
            }
        }

        @Override
        public void onObjectRemoved(int position) {
            BottomSheetBehavior behavior = BottomSheetBehavior.from(binding.bottomView);
            if(binding.title.getText().equals("Слои") && behavior.getState()!=BottomSheetBehavior.STATE_HIDDEN){
                RecyclerView recyclerView = binding.content.findViewById(R.id.layer_list);
                recyclerView.getAdapter().notifyItemRemoved(position);
                LayerListAdapter adapter = new LayerListAdapter(engine.getObjectList(), engine.getStageActorArray(), WallpaperEditor.this);
                recyclerView.setAdapter(adapter);

                ItemTouchHelper.Callback callback = new ItemMoveCallback(adapter);
                touchHelper = new ItemTouchHelper(callback);
                touchHelper.attachToRecyclerView(recyclerView);
            }
        }
    };

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
                    binding.content.removeAllViews();
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
                        menuItem.setTitle("Возобновить");
                        break;
                    default:
                        break;
                }
                return false;
            }
        };
        addMenuProvider(menuProvider, this, Lifecycle.State.RESUMED);
    }

    private boolean isNightMode() {
        UiModeManager uiModeManager = (UiModeManager) getSystemService(UI_MODE_SERVICE);
        return uiModeManager.getNightMode() == UiModeManager.MODE_NIGHT_YES;
    }

    @Override
    public boolean onSupportNavigateUp() {
        showSaveChangesDialog();
        return true;
    }

    private void showSaveChangesDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);

        StateViewBinding dialogBinding = StateViewBinding.bind(getLayoutInflater().inflate(R.layout.state_view, null));
        builder.setView(dialogBinding.getRoot());

        dialogBinding.title.setText("Сохранить проект перед выходом?");
        dialogBinding.description.setText("Несохранённые данные будут утеряны навсегда.");
        dialogBinding.image.setImageResource(R.drawable.test_mascot);
        dialogBinding.button.setText("Нет");
        dialogBinding.button.setBackgroundTintList(ColorStateList.valueOf(getColor(R.color.red)));
        dialogBinding.button.setTextColor(ColorStateList.valueOf(getColor(R.color.red)));
        dialogBinding.button2.setText("Да");
        dialogBinding.button2.setVisibility(View.VISIBLE);
        dialogBinding.buttonsSpace.setVisibility(View.VISIBLE);

        dialogBinding.button.setOnClickListener(view -> finish());
        dialogBinding.button2.setOnClickListener(view -> finish());

        AlertDialog dialog = builder.create();
        dialog.getWindow().setBackgroundDrawableResource(R.drawable.rounded_bottom_dialog);
        dialog.show();
    }

    @Override
    public void onBackPressed() {
        showSaveChangesDialog();
    }

    @Override
    public void exit() {

    }

    @Override
    public void onLayerClick(ActorHandler object) {
        engine.chooseObject(object);
    }

    @Override
    public void onStartDrag(RecyclerView.ViewHolder viewHolder) {
        touchHelper.startDrag(viewHolder);
    }

    private void saveProject(){
        FileOutputStream fos = null;
        try {
            fos = openFileOutput(name, MODE_PRIVATE);
            // fos.write(text.getBytes());      jsonchik
            Toast.makeText(this, "Файл сохранен", Toast.LENGTH_SHORT).show();
        } catch(IOException ex) {
            Toast.makeText(this, "Error: "+ ex.getMessage(), Toast.LENGTH_SHORT).show();
        } finally{
            try{
                if(fos!=null)
                    fos.close();
            }
            catch(IOException ex){
                Toast.makeText(this,"Error: "+ ex.getMessage(), Toast.LENGTH_SHORT).show();
            }
        }
    }
}