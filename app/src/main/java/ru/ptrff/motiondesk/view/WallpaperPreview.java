package ru.ptrff.motiondesk.view;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.view.MenuProvider;
import androidx.lifecycle.Lifecycle;

import android.app.WallpaperManager;
import android.content.ComponentName;
import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;

import com.badlogic.gdx.backends.android.AndroidFragmentApplication;

import ru.ptrff.motiondesk.R;
import ru.ptrff.motiondesk.databinding.ActivityWallpaperPreviewBinding;
import ru.ptrff.motiondesk.engine.EngineEventsListener;
import ru.ptrff.motiondesk.engine.WallpaperLibGdxFragment;
import ru.ptrff.motiondesk.engine.WallpaperEditorEngine;
import ru.ptrff.motiondesk.engine.WallpaperLibGdxService;

public class WallpaperPreview extends AppCompatActivity implements  AndroidFragmentApplication.Callbacks{

    private ActivityWallpaperPreviewBinding binding;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityWallpaperPreviewBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        try {
            setSupportActionBar(binding.toolbar);
        } catch (Exception e) {
            e.printStackTrace();
        }

        getSupportActionBar().setHomeAsUpIndicator(R.drawable.ic_back);
        getSupportActionBar().setHomeActionContentDescription("Назад");
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setTitle(getIntent().getStringExtra("Name"));

        setupActionBarButtons();

        WallpaperEditorEngine engine = new WallpaperEditorEngine(150, 150, new EngineEventsListener() {
            @Override
            public void onObjectSelected(String type, int index) {

            }

            @Override
            public void onObjectNotSelected() {

            }

            @Override
            public void onObjectAdded(int position) {

            }

            @Override
            public void onObjectRemoved(int position) {

            }
        });

        WallpaperLibGdxFragment libgdxFragment = new WallpaperLibGdxFragment(engine);

        getSupportFragmentManager().beginTransaction().
                add(R.id.preview, libgdxFragment).
                commit();

        binding.floatingActionButton.setOnClickListener(view -> {
            ParametersFragment parametersFragment = new ParametersFragment();
            parametersFragment.show(getSupportFragmentManager(), "Parameters");

            parametersFragment.setBottomSheetCallback(() -> {
                binding.floatingActionButton.animate().scaleX(1).setDuration(400).start();
                binding.floatingActionButton.animate().scaleY(1).setDuration(400).start();
                binding.floatingActionButton.animate().alpha(0.5f).setDuration(400).start();
            });


            binding.floatingActionButton.animate().scaleX(0.2f).setDuration(400).start();
            binding.floatingActionButton.animate().scaleY(0.2f).setDuration(400).start();
            binding.floatingActionButton.animate().alpha(0).setDuration(400).start();
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
                if(menuItem.getItemId()==R.id.apply) {
                    Intent intent = new Intent(
                            WallpaperManager.ACTION_CHANGE_LIVE_WALLPAPER);
                    intent.putExtra(WallpaperManager.EXTRA_LIVE_WALLPAPER_COMPONENT, new ComponentName(WallpaperPreview.this, WallpaperLibGdxService.class));
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