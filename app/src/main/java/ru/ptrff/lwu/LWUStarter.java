package ru.ptrff.lwu;

import com.badlogic.gdx.Game;

public class LWUStarter extends Game {

    @Override
    public void create() {
        setScreen(new LWUPlayer(this));
    }

}