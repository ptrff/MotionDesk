package ru.ptrff.motiondesk.view;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.view.MenuProvider;
import androidx.lifecycle.Lifecycle;

import android.app.ActivityManager;
import android.app.WallpaperManager;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.Window;
import android.view.WindowManager;

import com.badlogic.gdx.backends.android.AndroidFragmentApplication;

import java.util.List;

import ru.ptrff.motiondesk.R;
import ru.ptrff.motiondesk.models.WallpaperItem;
import ru.ptrff.motiondesk.databinding.ActivityWallpaperPreviewBinding;
import ru.ptrff.motiondesk.engine.WallpaperLibGdxFragment;
import ru.ptrff.motiondesk.engine.WallpaperLibGdxService;

public class WallpaperPreview extends AppCompatActivity implements AndroidFragmentApplication.Callbacks {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        ru.ptrff.motiondesk.databinding.ActivityWallpaperPreviewBinding binding = ActivityWallpaperPreviewBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
                WindowManager.LayoutParams.FLAG_FULLSCREEN);

        WallpaperLibGdxFragment libgdxFragment = new WallpaperLibGdxFragment();

        getSupportFragmentManager().beginTransaction().
                add(R.id.preview, libgdxFragment).
                commit();

        binding.cancel.setOnClickListener(v -> {
            finish();
        });

        binding.apply.setOnClickListener(v -> {
            if (isServiceRunning(WallpaperLibGdxService.class)) {
                Intent intent = new Intent();
                intent.setAction(WallpaperLibGdxService.ACTION_UPDATE);
                sendBroadcast(intent);
            }
            Intent intent = new Intent(
                    WallpaperManager.ACTION_CHANGE_LIVE_WALLPAPER);
            intent.putExtra(WallpaperManager.EXTRA_LIVE_WALLPAPER_COMPONENT, new ComponentName(WallpaperPreview.this, WallpaperLibGdxService.class));
            startActivity(intent);
            finish();
        });
    }

    public boolean isServiceRunning(Class<?> serviceClass) {
        ActivityManager manager = (ActivityManager) getSystemService(Context.ACTIVITY_SERVICE);
        List<ActivityManager.RunningServiceInfo> runningServices = manager.getRunningServices(Integer.MAX_VALUE);
        for (ActivityManager.RunningServiceInfo service : runningServices) {
            if (serviceClass.getName().equals(service.service.getClassName())) {
                return true;
            }
        }
        return false;
    }

    @Override
    public void exit() {

    }
}