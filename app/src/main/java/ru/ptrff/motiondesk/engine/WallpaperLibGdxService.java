package ru.ptrff.motiondesk.engine;

import android.app.WallpaperManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Build;
import android.util.Log;
import android.view.SurfaceHolder;
import android.view.WindowManager;

import com.badlogic.gdx.backends.android.AndroidApplicationConfiguration;
import com.badlogic.gdx.backends.android.AndroidLiveWallpaperService;

import java.io.FileDescriptor;
import java.io.IOException;
import java.io.PrintWriter;

import ru.ptrff.motiondesk.engine.scene.WallpaperEditorEngine;
import ru.ptrff.motiondesk.engine.scene.WallpaperEngine;
import ru.ptrff.motiondesk.engine.scene.WallpaperEngineBase;
import ru.ptrff.motiondesk.models.WallpaperItem;


public class WallpaperLibGdxService extends AndroidLiveWallpaperService {
    public static final String ACTION_UPDATE = "ru.ptrff.motiondesk.engine.UPDATE_VALUES";
    public static final String TAG = "WallpaperLibGdxService";

    private WallpaperEngine engine;
    private WallpaperLibGdxScreen screen;

    public WallpaperLibGdxService() {
        super();
    }

    @Override
    public void onCreateApplication() {
        super.onCreateApplication();
        init();

        IntentFilter filter = new IntentFilter();
        filter.addAction(ACTION_UPDATE);
        registerReceiver(updateValuesReceiver, filter);
    }

    private final BroadcastReceiver updateValuesReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            if (intent.getAction() != null && intent.getAction().equals(ACTION_UPDATE)) {
                WallpaperManager wallpaperManager = WallpaperManager.getInstance(getApplicationContext());
                try {
                    wallpaperManager.clear();
                } catch (IOException e) {
                    Log.e(TAG, "Wallpaper clearing error", e);
                }
            }
        }
    };

    private void init() {
        if(engine!=null){
            engine.dispose();
            screen.dispose();
        }

        AndroidApplicationConfiguration config = new AndroidApplicationConfiguration();
        config.useCompass = false;
        config.useAccelerometer = true;
        config.getTouchEventsForLiveWallpaper = true;

        Context applicationContext = getApplicationContext();
        engine = new WallpaperEngine(applicationContext);

        screen = new WallpaperLibGdxScreen(engine);
        initialize(screen, config);
    }
}