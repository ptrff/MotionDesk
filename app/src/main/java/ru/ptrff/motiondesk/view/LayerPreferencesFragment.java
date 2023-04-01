package ru.ptrff.motiondesk.view;

import android.os.Bundle;
import android.preference.PreferenceFragment;
import android.view.View;

import ru.ptrff.motiondesk.R;

public class LayerPreferencesFragment extends PreferenceFragment {

    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        addPreferencesFromResource(R.xml.layer_prefs);
    }
}