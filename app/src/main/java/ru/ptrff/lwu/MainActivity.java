package ru.ptrff.lwu;

import androidx.annotation.ColorRes;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.constraintlayout.motion.widget.MotionScene;
import androidx.core.graphics.drawable.DrawableCompat;
import androidx.fragment.app.FragmentTransaction;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.transition.Transition;
import androidx.transition.TransitionValues;

import android.app.ActionBar;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.graphics.PorterDuff;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.os.Handler;
import android.text.Layout;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import android.widget.RadioGroup;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.material.bottomsheet.BottomSheetBehavior;
import com.google.android.material.bottomsheet.BottomSheetDialog;
import com.google.android.material.navigation.NavigationBarView;
import com.google.android.material.snackbar.Snackbar;

import java.util.ArrayList;
import java.util.List;

import ru.ptrff.lwu.databinding.ActivityMainBinding;
import ru.ptrff.lwu.fragments.FragmentBrowse;
import ru.ptrff.lwu.fragments.FragmentLib;
import ru.ptrff.lwu.fragments.FragmentProfile;
import ru.ptrff.lwu.recycler_wpprs.WPPRSAdapter;
import ru.ptrff.lwu.recycler_wpprs.WPPRSListEntity;


/*Intent intent = new Intent(
        WallpaperManager.ACTION_CHANGE_LIVE_WALLPAPER);
intent.putExtra(WallpaperManager.EXTRA_LIVE_WALLPAPER_COMPONENT,
        new ComponentName(this, LWUService.class));
startActivity(intent);*/

public class MainActivity extends AppCompatActivity {

    private ActivityMainBinding binding;
    private BottomSheetDialog wallpaperTypeSelectorDialog;


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_lib, menu);

        MenuItem menuItem = menu.findItem(R.id.add_wallpapers);

        if (menuItem != null) {
            //tintMenuIcon(MainActivity.this, menuItem, R.color.background);
        }
        return true;
    }

    public static void tintMenuIcon(Context context, MenuItem item, @ColorRes int color) {
        Drawable normalDrawable = item.getIcon();
        Drawable wrapDrawable = DrawableCompat.wrap(normalDrawable);
        DrawableCompat.setTint(wrapDrawable, context.getResources().getColor(color));

        item.setIcon(wrapDrawable);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) { switch(item.getItemId()) {
        case R.id.add_wallpapers:
            wallpaperTypeSelectorDialog = new BottomSheetDialog(this);
            wallpaperTypeSelectorDialog.setContentView(R.layout.create_choose_bottom_sheet);
            wallpaperTypeSelectorDialog.setDismissWithAnimation(false);
            wallpaperTypeSelectorDialog.show();
            return true;
    }
        return(super.onOptionsItemSelected(item));
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityMainBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        binding.navigation.setOnItemSelectedListener(selectedListener);

        FragmentLib fragment = new FragmentLib();
        FragmentTransaction fragmentTransaction = getSupportFragmentManager().beginTransaction();
        fragmentTransaction.replace(R.id.content, fragment, "");
        fragmentTransaction.commit();

    }

    private  NavigationBarView.OnItemSelectedListener selectedListener = menuItem -> {
        switch (menuItem.getItemId()) {

            case R.id.navigation_lib:
                FragmentLib fragment = new FragmentLib();
                FragmentTransaction fragmentTransaction = getSupportFragmentManager().beginTransaction();
                fragmentTransaction.replace(R.id.content, fragment);
                fragmentTransaction.commit();
                return true;

            case R.id.navigation_browse:
                FragmentBrowse fragment1 = new FragmentBrowse();
                FragmentTransaction fragmentTransaction1 = getSupportFragmentManager().beginTransaction();
                fragmentTransaction1.replace(R.id.content, fragment1);
                fragmentTransaction1.commit();
                return true;

            case R.id.navigation_profile:
                FragmentProfile fragment2 = new FragmentProfile();
                FragmentTransaction fragmentTransaction2 = getSupportFragmentManager().beginTransaction();
                fragmentTransaction2.replace(R.id.content, fragment2);
                fragmentTransaction2.commit();
                return true;
        }
        return false;
    };
}