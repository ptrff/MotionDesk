package ru.ptrff.motiondesk.view;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.badlogic.gdx.backends.android.AndroidFragmentApplication;

import ru.ptrff.motiondesk.EditorEvents;
import ru.ptrff.motiondesk.LWUPlayer;
import ru.ptrff.motiondesk.LWUStarter;

public class GameFragment extends AndroidFragmentApplication implements EditorEvents {
    private LWUStarter starter;
    private LWUPlayer.OnObjectSelectedListener listener;
    private final int width;
    private final int height;

    public GameFragment(int width, int height, LWUPlayer.OnObjectSelectedListener listener) {
        this.width = width;
        this.height = height;
        this.listener = listener;
    }

    public GameFragment(int width, int height) {
        this.width = width;
        this.height = height;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        starter = new LWUStarter(width, height, listener);
        return initializeForView(starter);
    }

    @Override
    public void setResolution(int width, int height) {
        starter.setResolution(width, height);
    }
}
