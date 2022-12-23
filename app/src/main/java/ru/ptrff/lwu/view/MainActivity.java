package ru.ptrff.lwu.view;

import androidx.annotation.ColorInt;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;

import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.util.TypedValue;

import com.google.android.material.navigation.NavigationBarView;

import ru.ptrff.lwu.R;
import ru.ptrff.lwu.databinding.ActivityMainBinding;


/*Intent intent = new Intent(
        WallpaperManager.ACTION_CHANGE_LIVE_WALLPAPER);
intent.putExtra(WallpaperManager.EXTRA_LIVE_WALLPAPER_COMPONENT,
        new ComponentName(this, LWUService.class));
startActivity(intent);*/

public class MainActivity extends AppCompatActivity  {

    private ActivityMainBinding binding;
    private LibFragment libFragment;
    private BrowseFragment browseFragment;
    private ProfileFragment profileFragment;
    private int currentFragment;
    final FragmentManager fm = getSupportFragmentManager();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityMainBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        TypedValue typedValue = new TypedValue();
        getTheme().resolveAttribute(R.attr.backgroundBlackWhite, typedValue, true);
        @ColorInt int color = typedValue.data;
        getSupportActionBar().setBackgroundDrawable(new ColorDrawable(color));
        getSupportActionBar().setElevation(0);

        libFragment = new LibFragment();
        browseFragment = new BrowseFragment();
        profileFragment = new ProfileFragment();
        binding.navigation.setOnItemSelectedListener(navBarSelectListener);
        fm.beginTransaction().add(binding.content.getId(), libFragment).commit();
        currentFragment=0;
    }

    private final NavigationBarView.OnItemSelectedListener navBarSelectListener = menuItem -> {
        System.out.println(menuItem.getItemId());
        switch (menuItem.getItemId()) {
            case R.id.navigation_lib:
                if(currentFragment!=0) {
                    FragmentTransaction transaction = fm.beginTransaction();
                    transaction.setCustomAnimations(R.anim.enter_from_right, R.anim.exit_to_left);
                    transaction.replace(binding.content.getId(), libFragment);
                    transaction.commit();
                    currentFragment=0;
                }
                return true;

            case R.id.navigation_browse:
                if(currentFragment!=1) {
                    FragmentTransaction transaction = fm.beginTransaction();
                    System.out.println(binding.content.getId());
                    if(currentFragment==0) transaction.setCustomAnimations(R.anim.enter_from_left, R.anim.exit_to_right);
                    else transaction.setCustomAnimations(R.anim.enter_from_right, R.anim.exit_to_left);
                    transaction.replace(binding.content.getId(), browseFragment);
                    transaction.commit();
                    currentFragment=1;
                }
                return true;

            case R.id.navigation_profile:
                if(currentFragment!=2) {
                    FragmentTransaction transaction = fm.beginTransaction();
                    transaction.setCustomAnimations(R.anim.enter_from_left, R.anim.exit_to_right);
                    transaction.replace(binding.content.getId(), profileFragment);
                    transaction.commit();
                    currentFragment=2;
                }
                return true;
        }
        return false;
    };

    @Override
    protected void onSaveInstanceState(@NonNull Bundle outState) {
        super.onSaveInstanceState(outState);
    }
}