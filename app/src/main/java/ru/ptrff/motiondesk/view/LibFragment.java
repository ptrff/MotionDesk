package ru.ptrff.motiondesk.view;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Intent;
import android.content.res.ColorStateList;
import android.content.res.Configuration;
import android.content.res.Resources;
import android.os.Bundle;
import android.transition.Explode;
import android.util.DisplayMetrics;
import android.util.Log;
import android.util.TypedValue;
import android.view.Display;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.core.view.MenuProvider;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.Lifecycle;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import java.io.File;
import java.util.List;

import io.reactivex.Observable;
import io.reactivex.Observer;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.Disposable;
import io.reactivex.schedulers.Schedulers;
import ru.ptrff.motiondesk.utils.Converter;
import ru.ptrff.motiondesk.R;
import ru.ptrff.motiondesk.data.WallpaperItem;
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

    /*private void generateWhileScrolling() {
        binding.libRecycler.addOnScrollListener(new RecyclerView.OnScrollListener() {
            @Override
            public void onScrolled(@NonNull RecyclerView recyclerView, int dx, int dy) {
                super.onScrolled(recyclerView, dx, dy);
                if(sGrid.findFirstVisibleItemPosition()!=viewModel.getScrollPosition().getValue()){
                    viewModel.init(); //sGrid.findLastVisibleItemPosition()
                }
            }
        });
    }*/

    private void observeContent(){
        viewModel.getItemsLiveData().observe(getViewLifecycleOwner(), wallpaperItems -> {
            adapter.submitList(null);
            adapter.submitList(wallpaperItems);
            //binding.libRecycler.setAdapter(adapter);
        });
    }

    private void setupPullToRefresh() {
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

    private void startPreview(WallpaperItem item) {
        Intent i = new Intent(getActivity(), WallpaperPreview.class);
        i.putExtra("Name", item.getName());
        requireActivity().getWindow().setExitTransition(new Explode());
        startActivity(i);
    }
}