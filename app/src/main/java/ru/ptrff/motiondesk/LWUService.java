package ru.ptrff.motiondesk;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.backends.android.AndroidApplicationConfiguration;
import com.badlogic.gdx.backends.android.AndroidLiveWallpaperService;


public class LWUService extends AndroidLiveWallpaperService{

    private LWUPlayer.OnObjectSelectedListener objectSelectedListener;
    private final int width;
    private final int height;

    public LWUService(int width, int height){
        this.width=width;
        this.height=height;
    }

    public LWUService(){
        this.width= Gdx.graphics.getWidth();
        this.height=Gdx.graphics.getHeight();
    }

    public LWUService(int width, int height, LWUPlayer.OnObjectSelectedListener listener){
        this.width= width;
        this.height=height;
        this.objectSelectedListener=listener;
    }

    @Override
    public void onCreateApplication() {
        super.onCreateApplication();

        AndroidApplicationConfiguration config = new AndroidApplicationConfiguration();
        config.useCompass = false;
        config.useWakelock = false;
        config.useAccelerometer = false;
        config.getTouchEventsForLiveWallpaper = true;

        LWUStarter listener = new LWUStarter(width, height, objectSelectedListener);
        initialize(listener, config);
    }

}