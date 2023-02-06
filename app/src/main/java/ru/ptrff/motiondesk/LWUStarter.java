package ru.ptrff.motiondesk;

import com.badlogic.gdx.Game;
import com.badlogic.gdx.Screen;

public class LWUStarter extends Game implements EditorEvents {
    private LWUPlayer player;
    private final LWUPlayer.OnObjectSelectedListener listener;
    private final int width;
    private final int height;

    public LWUStarter(int width, int height, LWUPlayer.OnObjectSelectedListener listener){
        this.width=width;
        this.height=height;
        this.listener=listener;
    }

    @Override
    public void create() {
        player = new LWUPlayer(width, height, listener);
        setScreen(player);
    }

    @Override
    public void setResolution(int width, int height) {
        player.setResolution(width, height);
    }
}