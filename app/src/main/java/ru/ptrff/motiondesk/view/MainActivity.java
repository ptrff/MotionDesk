package ru.ptrff.motiondesk.view;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;
import androidx.lifecycle.ViewModelProvider;

import android.annotation.SuppressLint;
import android.os.Bundle;
import android.view.View;

import com.badlogic.gdx.graphics.g2d.Batch;
import com.google.android.material.appbar.AppBarLayout;

import ru.ptrff.motiondesk.utils.Converter;
import ru.ptrff.motiondesk.R;
import ru.ptrff.motiondesk.databinding.ActivityMainBinding;
import ru.ptrff.motiondesk.viewmodel.MainViewModel;




public class MainActivity extends AppCompatActivity {
    private ActivityMainBinding binding;
    private MainViewModel viewModel;
    private AuthFragment authFragment;
    private final FragmentManager fm = getSupportFragmentManager();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityMainBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        try {
            setSupportActionBar(binding.toolbar);
        } catch (Exception e) {
            e.printStackTrace();
        }

        new Converter(this);

        viewModel = new ViewModelProvider(this).get(MainViewModel.class);

        addBottomNavigationHiding();

        if(false){
            binding.appbar.setExpanded(false);
            authFragment = new AuthFragment(setupAuthCloser());
            showAuthPage();
        }else {
            observeContent();
            addBottomNavigationSelectionListener();
        }
    }

    @SuppressLint("NonConstantResourceId")
    private void addBottomNavigationSelectionListener() {
        binding.navigation.setOnItemSelectedListener(menuItem -> {
            switch (menuItem.getItemId()) {
                case R.id.navigation_lib:
                    viewModel.setCurrentFragment(0);
                    changeToolbarShowing(false);
                    return true;
                case R.id.navigation_browse:
                    viewModel.setCurrentFragment(1);
                    changeToolbarShowing(true);
                    return true;
                case R.id.navigation_profile:
                    viewModel.setCurrentFragment(2);
                    changeToolbarShowing(false);
                    return true;
            }
            return false;
        });
    }

    private void changeToolbarShowing(boolean always) {
        AppBarLayout.LayoutParams params = (AppBarLayout.LayoutParams) binding.toolbar.getLayoutParams();
        if(always){
            params.setScrollFlags(AppBarLayout.LayoutParams.SCROLL_FLAG_ENTER_ALWAYS);
        }else{
            params.setScrollFlags(AppBarLayout.LayoutParams.SCROLL_FLAG_SCROLL | AppBarLayout.LayoutParams.SCROLL_FLAG_ENTER_ALWAYS | AppBarLayout.LayoutParams.SCROLL_FLAG_SNAP);
        }
        binding.toolbar.setLayoutParams(params);
    }

    private void observeContent() {
        viewModel.getCurrentFragment().observe(this, fragment -> {
            FragmentTransaction transaction = fm.beginTransaction();
            if(viewModel.getCurrentFragmentId()<3) {
                switch (viewModel.getPreviousFragmentId()) {
                    case 0:
                        transaction.setCustomAnimations(R.anim.enter_from_left, R.anim.exit_to_right);
                        break;
                    case 1:
                        if (viewModel.getCurrentFragmentId() == 0)
                            transaction.setCustomAnimations(R.anim.enter_from_right, R.anim.exit_to_left);
                        else
                            transaction.setCustomAnimations(R.anim.enter_from_left, R.anim.exit_to_right);
                        break;
                    case 2:
                        transaction.setCustomAnimations(R.anim.enter_from_right, R.anim.exit_to_left);
                        break;
                }
            }else{
                transaction.setTransition(FragmentTransaction.TRANSIT_FRAGMENT_FADE);
            }
            transaction.replace(binding.content.getId(), fragment);
            transaction.addToBackStack(null);
            transaction.commit();
        });
    }

    private void showAuthPage() {
        FragmentTransaction transaction = fm.beginTransaction();
        transaction.replace(binding.content.getId(), viewModel.getLibFragment());
        transaction.add(binding.content.getId(), authFragment);
        transaction.addToBackStack(null);
        transaction.commit();
    }

    private AuthFragment.AuthCloser setupAuthCloser() {
        return () -> {
            runOnUiThread(() -> {
                binding.appbar.setExpanded(true);
                FragmentTransaction transaction = fm.beginTransaction();
                transaction.remove(authFragment);
                transaction.setTransition(FragmentTransaction.TRANSIT_FRAGMENT_FADE);
                transaction.commit();

                observeContent();
                addBottomNavigationSelectionListener();
            });
        };
    }


    private void addBottomNavigationHiding() {
        binding.appbar.addOnOffsetChangedListener((appBarLayout, verticalOffset) -> {
            int actionBarY = appBarLayout.getHeight() + verticalOffset;
            binding.navigation.setTranslationY(-actionBarY+binding.navigation.getHeight());
        });
    }

    @Override
    public void onBackPressed() {
        if(viewModel.getCurrentFragmentId()>2) {
            super.onBackPressed();
            viewModel.setCurrentFragment(viewModel.getPreviousFragmentId());
        }
    }

    @Override
    protected void onSaveInstanceState(@NonNull Bundle outState) {
        super.onSaveInstanceState(outState);
    }
}