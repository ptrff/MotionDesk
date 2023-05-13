package ru.ptrff.motiondesk.engine;

import com.badlogic.gdx.Game;

import ru.ptrff.motiondesk.engine.scene.WallpaperEngineBase;

public class WallpaperLibGdxScreen extends Game {
    private final WallpaperEngineBase engine;

    public WallpaperLibGdxScreen(WallpaperEngineBase engine){
        this.engine= engine;
    }

    @Override
    public void create() {
        setScreen(engine);
        engine.init();
    }
}