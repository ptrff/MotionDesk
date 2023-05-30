package ru.ptrff.motiondesk.view;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.content.res.Resources;
import android.os.Bundle;
import android.transition.Explode;
import android.util.DisplayMetrics;
import android.util.Log;
import android.util.TypedValue;
import android.view.Display;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.core.view.MenuProvider;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.Lifecycle;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.GridLayoutManager;

import io.reactivex.Observable;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.schedulers.Schedulers;
import ru.ptrff.motiondesk.R;
import ru.ptrff.motiondesk.models.WallpaperItem;
import ru.ptrff.motiondesk.databinding.FragmentLibBinding;
import ru.ptrff.motiondesk.adapters.WpprsAdapter;
import ru.ptrff.motiondesk.utils.ProjectManager;
import ru.ptrff.motiondesk.viewmodel.LibViewModel;


public class LibFragment extends Fragment {

    private static final int RecyclerElementWidth = 140;
    private FragmentLibBinding binding;
    private LibViewModel viewModel;
    private GridLayoutManager sGrid;
    private WpprsAdapter adapter;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        viewModel = new ViewModelProvider(this).get(LibViewModel.class);
        adapter = new WpprsAdapter(itemClick, getActivity());
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        binding = FragmentLibBinding.inflate(inflater);
        binding.libRecycler.setHasFixedSize(true);
        binding.libRecycler.setItemViewCacheSize(0);

        applyGridToAdapter();
        setupActionBarButtons();
        setupPullToRefresh();
        observeContent();

        return binding.getRoot();
    }

    @Override
    public void onResume() {
        super.onResume();
        viewModel.refresh();
    }

    private void observeContent(){
        viewModel.getWallpaperItemsLiveData().observe(getViewLifecycleOwner(), wallpaperItems -> {
            adapter.submitList(null);
            adapter.submitList(wallpaperItems);
            if(wallpaperItems.isEmpty()) {
                binding.noProjects.setVisibility(View.VISIBLE);
            }else{
                binding.noProjects.setVisibility(View.GONE);
            }
        });
    }

    private void setupPullToRefresh() {
        Resources.Theme theme = requireActivity().getTheme();
        TypedValue foregroundValue = new TypedValue();
        TypedValue backgroundValue = new TypedValue();
        theme.resolveAttribute(R.attr.foregroundBlackWhite, foregroundValue, false);
        theme.resolveAttribute(R.attr.backgroundDarkLight, backgroundValue, false);
        binding.libRefresh.setColorSchemeResources(foregroundValue.data);
        binding.libRefresh.setProgressBackgroundColorSchemeResource(backgroundValue.data);

        binding.libRefresh.setOnRefreshListener(() -> {
            viewModel.refresh();
            binding.libRefresh.setRefreshing(false);
        });
    }

    private void setupActionBarButtons() {
        MenuProvider menuProvider = new MenuProvider() {
            @Override
            public void onCreateMenu(@NonNull Menu menu, @NonNull MenuInflater menuInflater) {
                menuInflater.inflate(R.menu.menu_lib, menu);
            }

            @Override
            public boolean onMenuItemSelected(@NonNull MenuItem menuItem) {
                CreateProjectFragment configureProject = new CreateProjectFragment();
                configureProject.show(requireActivity().getSupportFragmentManager(), "New project");
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
        sGrid = new GridLayoutManager(getContext(), calculateColumnCount());
        binding.libRecycler.setLayoutManager(sGrid);
        binding.libRecycler.setAdapter(adapter);
        binding.libRecycler.scrollToPosition(viewModel.getScrollPosition().getValue());
    }

    @Override
    public void onConfigurationChanged(@NonNull Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        viewModel.setScrollPosition(sGrid.findFirstVisibleItemPosition());
        applyGridToAdapter();
    }

    public OnItemClickListener itemClick = (item, position) -> {
        InfoFragmentEvents events = () -> viewModel.refresh(); //TODO removing & updating
        InfoFragment infoFragment = new InfoFragment(item, events);
        infoFragment.setButtonOnClickListener(view -> startPreview(item));
        infoFragment.show(requireActivity().getSupportFragmentManager(), "Info");
    };

    @SuppressLint("CheckResult")
    private void startPreview(WallpaperItem item) {
        Observable.fromCallable(() -> {
                    ProjectManager.unpackProjectToFolder(requireContext(), item.getId(), "Current");
                    SharedPreferences sharedPreferences = requireContext().getSharedPreferences("MotionDesk", Context.MODE_PRIVATE);
                    SharedPreferences.Editor editor = sharedPreferences.edit();
                    editor.putString("current", item.getId());
                    editor.apply();
                    return true;
                })
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(result -> {
                    Intent intent = new Intent(requireActivity(), WallpaperPreview.class);
                    intent.putExtra("wallpaper_item", item);
                    requireActivity().getWindow().setExitTransition(new Explode());
                    startActivity(intent);
                }, error -> {
                    Log.e("LibFragment", "Error starting preview", error);
                });
    }
}