package ru.ptrff.motiondesk.engine;

import android.content.Context;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;

import com.badlogic.gdx.backends.android.AndroidFragmentApplication;

import ru.ptrff.motiondesk.engine.scene.WallpaperEditorEngine;
import ru.ptrff.motiondesk.engine.scene.WallpaperEngine;
import ru.ptrff.motiondesk.engine.scene.WallpaperEngineBase;
import ru.ptrff.motiondesk.models.WallpaperItem;

public class WallpaperLibGdxFragment extends AndroidFragmentApplication {
    private WallpaperEngineBase engine;
    private boolean engineGiven = false;

    public WallpaperLibGdxFragment(WallpaperEngineBase engine) {
        engineGiven = true;
        this.engine = engine;
    }

    public WallpaperLibGdxFragment(){}


    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        WallpaperLibGdxScreen starter = new WallpaperLibGdxScreen(engine);

        return initializeForView(starter);
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        if(!engineGiven) {
            engine = new WallpaperEngine(context);
        }
    }

}
