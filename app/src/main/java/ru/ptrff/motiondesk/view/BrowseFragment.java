package ru.ptrff.motiondesk.view;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.content.res.Resources;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.core.view.MenuProvider;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.Lifecycle;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.GridLayoutManager;

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

import androidx.appcompat.widget.SearchView;

import com.google.android.material.snackbar.BaseTransientBottomBar;
import com.google.android.material.snackbar.Snackbar;

import io.reactivex.Observable;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.schedulers.Schedulers;
import ru.ptrff.motiondesk.R;
import ru.ptrff.motiondesk.adapters.WpprsAdapter;
import ru.ptrff.motiondesk.databinding.FragmentBrowseBinding;
import ru.ptrff.motiondesk.models.WallpaperItem;
import ru.ptrff.motiondesk.utils.ProjectManager;
import ru.ptrff.motiondesk.viewmodel.BrowseViewModel;

public class BrowseFragment extends Fragment {

    private static final int RecyclerElementWidth = 140;
    private WpprsAdapter adapter;
    private FragmentBrowseBinding binding;
    private GridLayoutManager sGrid;
    private BrowseViewModel viewModel;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        viewModel = new ViewModelProvider(this).get(BrowseViewModel.class);
        adapter = new WpprsAdapter(itemClick, getActivity());
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        binding = FragmentBrowseBinding.inflate(inflater);

        binding.browseRecycler.setHasFixedSize(true);
        binding.browseRecycler.setItemViewCacheSize(0);


        applyGridToAdapter();
        setupActionBarButtons();
        setupPullToRefresh();
        observeContent();


        return binding.getRoot();
    }

    private void setupPullToRefresh() {
        Resources.Theme theme = requireActivity().getTheme();
        TypedValue foregroundValue = new TypedValue();
        TypedValue backgroundValue = new TypedValue();
        theme.resolveAttribute(R.attr.foregroundBlackWhite, foregroundValue, false);
        theme.resolveAttribute(R.attr.backgroundDarkLight, backgroundValue, false);
        binding.refresh.setColorSchemeResources(foregroundValue.data);
        binding.refresh.setProgressBackgroundColorSchemeResource(backgroundValue.data);

        binding.refresh.setOnRefreshListener(() -> {
            viewModel.refresh();
            binding.refresh.setRefreshing(false);
        });
    }

    private void observeContent() {
        viewModel.getWallpaperItemsLiveData().observe(getViewLifecycleOwner(), wallpaperItems -> {
            adapter.submitList(null);
            adapter.submitList(wallpaperItems);
        });
    }

    private void setupActionBarButtons() {

        MenuProvider menuProvider = new MenuProvider() {
            @Override
            public void onCreateMenu(@NonNull Menu menu, @NonNull MenuInflater menuInflater) {
                menuInflater.inflate(R.menu.menu_browse, menu);

                MenuItem searchItem = menu.findItem(R.id.search);

                SearchView searchView = (SearchView) searchItem.getActionView();

                searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
                    @Override
                    public boolean onQueryTextSubmit(String query) {
                        return true;
                    }

                    @Override
                    public boolean onQueryTextChange(String newText) {
                        viewModel.searchWallpaperItems(newText);
                        return true;
                    }
                });
            }

            @Override
            public boolean onMenuItemSelected(@NonNull MenuItem menuItem) {

                return false;
            }
        };
        requireActivity().addMenuProvider(menuProvider, getViewLifecycleOwner(), Lifecycle.State.RESUMED);
    }

    private int calculateColumnCount() {
        Display display = requireActivity().getWindowManager().getDefaultDisplay();
        DisplayMetrics outMetrics = new DisplayMetrics();
        display.getMetrics(outMetrics);

        float density = getResources().getDisplayMetrics().density;
        float dpWidth = outMetrics.widthPixels / density;
        return Math.round(dpWidth / RecyclerElementWidth);
    }

    private void applyGridToAdapter() {
        sGrid = new GridLayoutManager(getContext(), calculateColumnCount());
        binding.browseRecycler.setLayoutManager(sGrid);
        binding.browseRecycler.setAdapter(adapter);
        //binding.browseRecycler.scrollToPosition(viewModel.getScrollPosition().getValue());
    }

    /*private void applyItemsSize() {
        binding.recycler.addItemDecoration(new RecyclerView.ItemDecoration() {
            @Override
            public void getItemOffsets(Rect outRect, View view, RecyclerView parent, RecyclerView.State state) {
                super.getItemOffsets(outRect, view, parent, state);
                int itemCount = calculateColumnCount() + 3;
                int parentWidth = ((View) binding.recycler.getParent()).getWidth();
                if (parentWidth != 0) {
                    view.getLayoutParams().width = (parentWidth / itemCount);
                }
            }
        });
    }*/

    public void onConfigurationChanged(@NonNull Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        //viewModel.setScrollPosition(sGrid.findFirstVisibleItemPosition());
        applyGridToAdapter();
    }

    public OnItemClickListener itemClick = (item, position) -> {
        InfoFragmentEvents events = () -> viewModel.refresh(); //TODO removing & updating
        InfoFragment infoFragment = new InfoFragment(item, () -> {});
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