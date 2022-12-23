package ru.ptrff.lwu;

import androidx.annotation.ColorInt;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;

import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.util.TypedValue;
import android.view.ViewGroup;
import android.widget.TextView;

import com.badlogic.gdx.backends.android.AndroidFragmentApplication;

import ru.ptrff.lwu.view.GameFragment;

public class WallpaperPreview extends AppCompatActivity implements  AndroidFragmentApplication.Callbacks{

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_wallpaper_preview);

        TypedValue typedValue = new TypedValue();
        getTheme().resolveAttribute(R.attr.backgroundBlackWhite, typedValue, true);
        @ColorInt int color = typedValue.data;
        ActionBar actionBar = getSupportActionBar();
        actionBar.setBackgroundDrawable(new ColorDrawable(color));
        actionBar.setHomeAsUpIndicator(R.drawable.ic_back);
        actionBar.setHomeActionContentDescription("Назад");
        actionBar.setDisplayHomeAsUpEnabled(true);
        //actionBar.setTitle(getIntent().getStringExtra("Name"));
        ViewGroup lin = (ViewGroup) getLayoutInflater().inflate(R.layout.info_header, null);
        actionBar.setCustomView(lin);
        lin.setLayoutParams(new ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT));
        actionBar.setDisplayShowCustomEnabled(true);
        actionBar.setTitle("");
        lin.<TextView>findViewById(R.id.header).setText(getIntent().getStringExtra("Name"));

        GameFragment libgdxFragment = new GameFragment();

        getSupportFragmentManager().beginTransaction().
                add(R.id.preview, libgdxFragment).
                commit();
    }

    @Override
    public boolean onSupportNavigateUp() {
        finish();
        return true;
    }

    @Override
    public void exit() {

    }
}