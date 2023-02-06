package ru.ptrff.motiondesk;

import android.app.WallpaperManager;
import android.content.ComponentName;
import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.view.MenuProvider;
import androidx.lifecycle.Lifecycle;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.badlogic.gdx.backends.android.AndroidFragmentApplication;
import com.google.android.material.bottomsheet.BottomSheetBehavior;

import java.util.ArrayList;
import java.util.List;

import ru.ptrff.motiondesk.adapters.ToolbarAdapter;
import ru.ptrff.motiondesk.data.ToolItem;
import ru.ptrff.motiondesk.databinding.ActivityWallpaperEditorBinding;
import ru.ptrff.motiondesk.view.GameFragment;

public class WallpaperEditor extends AppCompatActivity implements AndroidFragmentApplication.Callbacks {

    private ActivityWallpaperEditorBinding binding;
    private ToolbarAdapter toolbarAdapter;
    private final List<ToolItem> tools = new ArrayList<>();
    private GameFragment libgdxFragment;
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
        width=getIntent().getIntExtra("Width", 1080);
        height=getIntent().getIntExtra("Height", 2340);
        name=getIntent().getStringExtra("Name");


        libgdxFragment = new GameFragment(width, height, objectSelectedListener);
        getSupportFragmentManager().beginTransaction().
                add(R.id.preview, libgdxFragment).
                commit();




        getSupportActionBar().setHomeAsUpIndicator(R.drawable.ic_back);
        getSupportActionBar().setHomeActionContentDescription("Назад");
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setTitle(name+" "+height+" "+width);

        setupActionBarButtons();
        setupBottomSheet();
        resetTools();
    }

    private void resetTools(){
        tools.clear();
        tools.add(new ToolItem(R.drawable.ic_add, "Добавить"));
        tools.add(new ToolItem(R.drawable.ic_2d_square, "Сцена"));

        setupToolbar();
    }

    private void setupToolbar() {
        toolbarAdapter = new ToolbarAdapter(tools, toolClickListener);
        LinearLayoutManager sGrid = new LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false);
        binding.toolbarTools.setLayoutManager(sGrid);
        binding.toolbarTools.setAdapter(toolbarAdapter);
    }

    private void showEditObjectTools(){
        tools.clear();
        tools.add(new ToolItem(R.drawable.ic_info, "Информация"));
        tools.add(new ToolItem(R.drawable.ic_parameters, "Параметры"));
        tools.add(new ToolItem(R.drawable.ic_effects, "Эффекты"));
        tools.add(new ToolItem(R.drawable.ic_delete, "Удалить"));
        setupToolbar();
    }

    private final ToolbarAdapter.OnImageClickListener toolClickListener = new ToolbarAdapter.OnImageClickListener() {
        @Override
        public void onImageClick(ToolItem tool) {
            BottomSheetBehavior.from(binding.bottomView).setState(BottomSheetBehavior.STATE_COLLAPSED);
            binding.title.setText(tool.getLabel());
        }
    };

    private final LWUPlayer.OnObjectSelectedListener objectSelectedListener = new LWUPlayer.OnObjectSelectedListener() {
        @Override
        public void onObjectSelected(String type) {
            runOnUiThread(() -> {
                showEditObjectTools();
            });
        }

        @Override
        public void onObjectNotSelected() {
            runOnUiThread(() -> {
                resetTools();
            });
        }
    };

    private void setupBottomSheet() {
        binding.bottomView.setZ(binding.bottomView.getZ()+1);
        BottomSheetBehavior behavior = BottomSheetBehavior.from(binding.bottomView);
        behavior.setDraggable(true);
        behavior.setPeekHeight(1000);
        behavior.setState(BottomSheetBehavior.STATE_HIDDEN);
        behavior.addBottomSheetCallback(new BottomSheetBehavior.BottomSheetCallback() {
            @Override
            public void onStateChanged(@NonNull View bottomSheet, int newState) {
                if(newState==BottomSheetBehavior.STATE_HIDDEN){

                }
                System.out.println(newState);
                if(newState==BottomSheetBehavior.STATE_EXPANDED || newState==BottomSheetBehavior.STATE_COLLAPSED){

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
                if (menuItem.getItemId() == R.id.apply) {
                    Intent intent = new Intent(
                            WallpaperManager.ACTION_CHANGE_LIVE_WALLPAPER);
                    intent.putExtra(WallpaperManager.EXTRA_LIVE_WALLPAPER_COMPONENT, new ComponentName(WallpaperEditor.this, LWUService.class));
                    startActivity(intent);
                }
                return false;
            }
        };
        addMenuProvider(menuProvider, this, Lifecycle.State.RESUMED);
    }

    @Override
    public boolean onSupportNavigateUp() {
        finish();
        return true;
    }

    @Override
    public void exit() {

    }
}