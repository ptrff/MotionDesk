package ru.ptrff.lwu;

import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.FragmentTransaction;

import android.app.WallpaperManager;
import android.content.ComponentName;
import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.TextView;

import com.google.android.material.navigation.NavigationBarView;

import ru.ptrff.lwu.fragments.FragmentBrowse;
import ru.ptrff.lwu.fragments.FragmentLib;
import ru.ptrff.lwu.fragments.FragmentProfile;

public class MainActivity extends AppCompatActivity {
    String myuid;
    NavigationBarView navigationView;
    TextView title;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        navigationView = findViewById(R.id.navigation);
        navigationView.setOnItemSelectedListener(selectedListener);

        // When we open the application first
        // time the fragment should be shown to the user
        // in this case it is home fragment
        FragmentLib fragment = new FragmentLib();
        FragmentTransaction fragmentTransaction = getSupportFragmentManager().beginTransaction();
        fragmentTransaction.replace(R.id.content, fragment, "");
        fragmentTransaction.commit();

        title = findViewById(R.id.fragment_title);
    }

    private  NavigationBarView.OnItemSelectedListener selectedListener = menuItem -> {
        switch (menuItem.getItemId()) {

            case R.id.navigation_lib:
                FragmentLib fragment = new FragmentLib();
                FragmentTransaction fragmentTransaction = getSupportFragmentManager().beginTransaction();
                fragmentTransaction.replace(R.id.content, fragment, "");
                title.setText(R.string.title_lib);

                Intent intent = new Intent(
                        WallpaperManager.ACTION_CHANGE_LIVE_WALLPAPER);
                intent.putExtra(WallpaperManager.EXTRA_LIVE_WALLPAPER_COMPONENT,
                        new ComponentName(this, LWUService.class));
                startActivity(intent);

                fragmentTransaction.commit();
                return true;

            case R.id.navigation_browse:
                FragmentBrowse fragment1 = new FragmentBrowse();
                FragmentTransaction fragmentTransaction1 = getSupportFragmentManager().beginTransaction();
                fragmentTransaction1.replace(R.id.content, fragment1);
                title.setText(R.string.title_browse);
                fragmentTransaction1.commit();
                return true;

            case R.id.navigation_profile:
                FragmentProfile fragment2 = new FragmentProfile();
                FragmentTransaction fragmentTransaction2 = getSupportFragmentManager().beginTransaction();
                fragmentTransaction2.replace(R.id.content, fragment2, "");
                title.setText(R.string.title_profile);
                fragmentTransaction2.commit();
                return true;
        }
        return false;
    };
}