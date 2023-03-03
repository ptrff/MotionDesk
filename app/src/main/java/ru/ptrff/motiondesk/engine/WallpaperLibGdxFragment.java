package ru.ptrff.motiondesk.engine;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;

import com.badlogic.gdx.backends.android.AndroidFragmentApplication;

public class WallpaperLibGdxFragment extends AndroidFragmentApplication {
    private final WallpaperEditorEngine engine;

    public WallpaperLibGdxFragment(WallpaperEditorEngine engine) {
        this.engine = engine;
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        WallpaperLibGdxScreen starter = new WallpaperLibGdxScreen(engine);
        return initializeForView(starter);
    }

}
