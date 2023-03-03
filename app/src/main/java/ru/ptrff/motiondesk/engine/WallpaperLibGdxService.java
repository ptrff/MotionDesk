package ru.ptrff.motiondesk.engine;

import com.badlogic.gdx.ApplicationListener;
import com.badlogic.gdx.Game;
import com.badlogic.gdx.backends.android.AndroidApplicationConfiguration;
import com.badlogic.gdx.backends.android.AndroidLiveWallpaperService;


public class WallpaperLibGdxService extends AndroidLiveWallpaperService{

    private final WallpaperEditorEngine engine;

    public WallpaperLibGdxService(WallpaperEditorEngine engine){
        this.engine=engine;
    }

    @Override
    public void onCreateApplication() {
        super.onCreateApplication();

        AndroidApplicationConfiguration config = new AndroidApplicationConfiguration();
        config.useCompass = false;
        config.useWakelock = false;
        config.useAccelerometer = false;
        config.getTouchEventsForLiveWallpaper = true;

        WallpaperLibGdxScreen screen = new WallpaperLibGdxScreen(engine);
        initialize(screen, config);
    }

}