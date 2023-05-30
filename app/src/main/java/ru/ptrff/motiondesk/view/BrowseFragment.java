package ru.ptrff.motiondesk.view;

import android.content.res.Configuration;
import android.content.res.Resources;
import android.graphics.Rect;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.core.view.MenuProvider;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.Lifecycle;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import android.util.DisplayMetrics;
import android.util.TypedValue;
import android.view.Display;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.Toast;

import com.google.android.material.snackbar.BaseTransientBottomBar;
import com.google.android.material.snackbar.Snackbar;

import java.util.Objects;

import ru.ptrff.motiondesk.R;
import ru.ptrff.motiondesk.adapters.BrowseSectorAdapter;
import ru.ptrff.motiondesk.adapters.WpprsAdapter;
import ru.ptrff.motiondesk.databinding.FragmentBrowseBinding;
import ru.ptrff.motiondesk.viewmodel.BrowseViewModel;

public class BrowseFragment extends Fragment {

    private static final int RecyclerElementWidth = 140 * 3;
    private WpprsAdapter adapter;
    private FragmentBrowseBinding binding;
    private BrowseViewModel viewModel;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        binding = FragmentBrowseBinding.inflate(inflater);

        viewModel = new ViewModelProvider(this).get(BrowseViewModel.class);
        viewModel.init(0);


        applyGridToAdapter();

        setupActionBarButtons();
        observeContent();
        setupPullToRefresh();
        binding.recycler.post(this::applyItemsSize);


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

        binding.refresh.setOnRefreshListener(() -> binding.refresh.setRefreshing(false));
    }

    private void observeContent() {
        viewModel.getWallpapersLiveData().observe(getViewLifecycleOwner(), wallpaperItems -> {
            adapter.submitList(null);
            adapter.submitList(wallpaperItems);
        });
    }

    private void setupActionBarButtons() {
        MenuProvider menuProvider = new MenuProvider() {
            @Override
            public void onCreateMenu(@NonNull Menu menu, @NonNull MenuInflater menuInflater) {
                menuInflater.inflate(R.menu.menu_browse, menu);
            }

            @Override
            public boolean onMenuItemSelected(@NonNull MenuItem menuItem) {
                Toast.makeText(getContext(), "Search", Toast.LENGTH_SHORT).show();
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
        adapter = new WpprsAdapter(itemClick, requireContext());
        LinearLayoutManager layoutManager = new LinearLayoutManager(requireContext());
        layoutManager.setOrientation(LinearLayoutManager.HORIZONTAL);
        binding.recycler.setLayoutManager(layoutManager);
        binding.recycler.setAdapter(adapter);
    }

    private void applyItemsSize() {
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
    }

    @Override
    public void onConfigurationChanged(@NonNull Configuration newConfig) {
        super.onConfigurationChanged(newConfig);

        binding.recycler.post(this::applyItemsSize);
    }

    public OnItemClickListener itemClick = (item, position) -> {
        Snackbar.make(binding.getRoot(), "В скором времени..", BaseTransientBottomBar.LENGTH_SHORT).show();
    };
}