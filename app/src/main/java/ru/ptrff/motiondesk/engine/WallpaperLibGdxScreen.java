package ru.ptrff.motiondesk.engine;

import com.badlogic.gdx.Game;

public class WallpaperLibGdxScreen extends Game {
    private final WallpaperEditorEngine engine;

    public WallpaperLibGdxScreen(WallpaperEditorEngine engine){
        this.engine=engine;
    }

    @Override
    public void create() {
        setScreen(engine);
        engine.init();
    }
}