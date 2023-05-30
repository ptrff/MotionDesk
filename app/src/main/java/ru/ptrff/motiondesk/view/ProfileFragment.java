package ru.ptrff.motiondesk.view;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.content.res.Configuration;
import android.content.res.Resources;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.core.view.MenuProvider;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.Lifecycle;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import android.transition.Explode;
import android.util.DisplayMetrics;
import android.util.TypedValue;
import android.view.Display;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;

import com.google.android.material.snackbar.BaseTransientBottomBar;
import com.google.android.material.snackbar.Snackbar;

import ru.ptrff.motiondesk.R;
import ru.ptrff.motiondesk.databinding.FragmentProfileBinding;
import ru.ptrff.motiondesk.adapters.WpprsAdapter;
import ru.ptrff.motiondesk.models.WallpaperItem;
import ru.ptrff.motiondesk.viewmodel.MainViewModel;
import ru.ptrff.motiondesk.viewmodel.ProfileViewModel;

public class ProfileFragment extends Fragment {

    private static final int RecyclerElementWidth = 140;
    private FragmentProfileBinding binding;
    private ProfileViewModel viewModel;
    private GridLayoutManager sGrid;
    private WpprsAdapter adapter;

    @Override
    public void onCreate(Bundle savedInstanceState) {super.onCreate(savedInstanceState);}

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        binding = FragmentProfileBinding.inflate(inflater);
        viewModel = new ViewModelProvider(this).get(ProfileViewModel.class);
        adapter = new WpprsAdapter(itemClick, getActivity());
        binding.profileRecycler.setHasFixedSize(true);
        binding.profileRecycler.setItemViewCacheSize(0);

        applyGridToAdapter();
        setupActionBarButtons();
        setupPullToRefresh();
        observeContent();

        return binding.getRoot();
    }


    private void observeContent(){
        viewModel.getItemsLiveData().observe(getViewLifecycleOwner(), wallpaperItems -> {
            adapter.submitList(wallpaperItems);
        });
    }

    private void setupPullToRefresh() {
        Resources.Theme theme = requireActivity().getTheme();
        TypedValue foregroundValue = new TypedValue();
        TypedValue backgroundValue = new TypedValue();
        theme.resolveAttribute(R.attr.foregroundBlackWhite, foregroundValue, false);
        theme.resolveAttribute(R.attr.backgroundDarkLight, backgroundValue, false);
        binding.profileRefresh.setColorSchemeResources(foregroundValue.data);
        binding.profileRefresh.setProgressBackgroundColorSchemeResource(backgroundValue.data);

        binding.profileRefresh.setOnRefreshListener(() -> binding.profileRefresh.setRefreshing(false));
    }

    private void setupActionBarButtons() {
        MenuProvider menuProvider = new MenuProvider() {
            @Override
            public void onCreateMenu(@NonNull Menu menu, @NonNull MenuInflater menuInflater) {
                menuInflater.inflate(R.menu.menu_profile, menu);
            }

            @SuppressLint("NonConstantResourceId")
            @Override
            public boolean onMenuItemSelected(@NonNull MenuItem menuItem) {
                MainViewModel mainViewModel = new ViewModelProvider(requireActivity()).get(MainViewModel.class);
                switch (menuItem.getItemId()){
                    case R.id.edit_profile:
                        mainViewModel.setCurrentFragment(3);
                        break;
                    case R.id.settings:
                        mainViewModel.setCurrentFragment(4);
                        break;
                }
                return false;
            }
        };
        requireActivity().addMenuProvider(menuProvider, getViewLifecycleOwner(), Lifecycle.State.RESUMED);
    }

    private int calculateColumnCount(){
        Display display = requireActivity().getWindowManager().getDefaultDisplay();
        DisplayMetrics outMetrics = new DisplayMetrics();
        display.getMetrics(outMetrics);

        float density  = getResources().getDisplayMetrics().density;
        float dpWidth  = outMetrics.widthPixels / density;
        return Math.round(dpWidth/RecyclerElementWidth);
    }

    private void applyGridToAdapter(){
        sGrid = new GridLayoutManager(requireContext(), calculateColumnCount());
        binding.profileRecycler.setLayoutManager(sGrid);
        binding.profileRecycler.setAdapter(adapter);
    }

    @Override
    public void onConfigurationChanged(@NonNull Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        applyGridToAdapter();
    }

    public OnItemClickListener itemClick = (item, position) -> {
        Snackbar.make(binding.getRoot(), "В скором времени..", BaseTransientBottomBar.LENGTH_SHORT).show();
    };
}