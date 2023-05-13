package ru.ptrff.motiondesk.engine;

import android.content.Context;

import com.badlogic.gdx.backends.android.AndroidApplicationConfiguration;
import com.badlogic.gdx.backends.android.AndroidLiveWallpaperService;

import ru.ptrff.motiondesk.engine.scene.WallpaperEditorEngine;
import ru.ptrff.motiondesk.engine.scene.WallpaperEngine;
import ru.ptrff.motiondesk.engine.scene.WallpaperEngineBase;
import ru.ptrff.motiondesk.models.WallpaperItem;


public class WallpaperLibGdxService extends AndroidLiveWallpaperService{

    public WallpaperLibGdxService(){}

    @Override
    public void onCreateApplication() {
        super.onCreateApplication();

        AndroidApplicationConfiguration config = new AndroidApplicationConfiguration();
        config.useCompass = false;
        config.useAccelerometer = true;
        config.getTouchEventsForLiveWallpaper = true;

        Context applicationContext = getApplicationContext();
        WallpaperEngine engine = new WallpaperEngine(applicationContext);

        WallpaperLibGdxScreen screen = new WallpaperLibGdxScreen(engine);
        initialize(screen, config);
    }
}