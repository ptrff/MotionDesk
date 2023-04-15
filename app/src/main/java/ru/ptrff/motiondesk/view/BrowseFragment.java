package ru.ptrff.motiondesk.view;

import android.content.res.Configuration;
import android.graphics.Rect;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.core.view.MenuProvider;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.Lifecycle;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import android.util.DisplayMetrics;
import android.view.Display;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import java.util.Objects;

import ru.ptrff.motiondesk.R;
import ru.ptrff.motiondesk.adapters.BrowseSectorAdapter;
import ru.ptrff.motiondesk.databinding.FragmentBrowseBinding;
import ru.ptrff.motiondesk.viewmodel.BrowseViewModel;

public class BrowseFragment extends Fragment {

    private static final int RecyclerElementWidth = 140*3;
    private BrowseSectorAdapter sectorAdapter;
    private FragmentBrowseBinding binding;
    private BrowseViewModel viewModel;
    private GridLayoutManager layoutManager;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        binding = FragmentBrowseBinding.inflate(inflater);
        viewModel = new ViewModelProvider(this).get(BrowseViewModel.class);
        sectorAdapter = new BrowseSectorAdapter(viewModel.getSectorsLiveData().getValue(), requireActivity(), binding.browseBody);

        applyGridToAdapter(sectorAdapter, binding.browseBody);
        createSectors();
        setupActionBarButtons();
        observeContent(sectorAdapter,binding.browseBody);
        setupPullToRefresh();


        viewModel.init(0);

        return binding.getRoot();
    }

    private void setupPullToRefresh() {
        final SwipeRefreshLayout pullToRefresh = binding.refresh;
        pullToRefresh.setOnRefreshListener(() -> {
            Toast.makeText(getContext(), Objects.requireNonNull(viewModel.getSectorsLiveData().getValue()).get(1).getName(), Toast.LENGTH_SHORT).show();
            pullToRefresh.setRefreshing(false);
        });
    }

    private void createSectors(){
        /*for(int i = 0; i<sectorCount;i++){
            LayoutInflater inflater = LayoutInflater.from(getContext());
            LinearLayout layout = (LinearLayout) inflater.inflate(R.layout.browse_sector, null, false);
            WpprsAdapter adapter = new WpprsAdapter(viewModel.getSectorsLiveData().getValue(), itemClick, getActivity());
            RecyclerView recyclerView = layout.findViewById(R.id.recycler);
            recyclerView.setHasFixedSize(true);
            recyclerView.setItemViewCacheSize(0);
            recyclerView.post(() -> applyItemsSize(recyclerView));
            applyGridToAdapter(adapter, recyclerView);
            observeContent(adapter, recyclerView);
            OverscrollLayout overscrollLayout = layout.findViewById(R.id.overscroll);
            overscrollLayout.setOnOverScrollReleaseListener(() -> {
                MainViewModel mainViewModel = new ViewModelProvider(requireActivity()).get(MainViewModel.class);
                mainViewModel.setCurrentFragment(0);
            });

            adapters.add(adapter);
            sectors.add(layout);
            binding.browseBody.addView(layout);
        }*/
    }

    private void applyItemsSize(RecyclerView recyclerView) {
        recyclerView.addItemDecoration(new RecyclerView.ItemDecoration() {
            @Override
            public void getItemOffsets(Rect outRect, View view, RecyclerView parent, RecyclerView.State state) {
                super.getItemOffsets(outRect, view, parent, state);
                int itemCount = calculateColumnCount()+1;
                int parentWidth = ((View) recyclerView.getParent()).getWidth();
                if(parentWidth!=0) {
                    view.getLayoutParams().width = (parentWidth / itemCount);
                }
            }
        });
    }

    private void scrollPositionRecovery(Bundle savedInstanceState, RecyclerView recyclerView) {
        if (savedInstanceState != null) {
            int scrollPosition = savedInstanceState.getInt("scroll_position", 0);
            recyclerView.scrollToPosition(scrollPosition);
        }
    }

    private void observeContent(BrowseSectorAdapter adapter, RecyclerView recyclerView){
        viewModel.getSectorsLiveData().observe(getViewLifecycleOwner(), wallpaperItems -> {
            adapter.notifyItemInserted(wallpaperItems.size());
        });

        //viewModel.getScrollPosition().observe(getViewLifecycleOwner(), recyclerView::scrollToPosition);
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

    private int calculateColumnCount(){
        Display display = requireActivity().getWindowManager().getDefaultDisplay();
        DisplayMetrics outMetrics = new DisplayMetrics();
        display.getMetrics(outMetrics);

        float density  = getResources().getDisplayMetrics().density;
        float dpWidth  = outMetrics.widthPixels / density;
        return Math.round(dpWidth/RecyclerElementWidth);
    }

    private void applyGridToAdapter(BrowseSectorAdapter adapter, RecyclerView recyclerView) {
//        layoutManager = new GridLayoutManager(getContext(), calculateColumnCount(), LinearLayoutManager.VERTICAL, false);

        recyclerView.setLayoutManager(layoutManager);
        recyclerView.setAdapter(adapter);
        //recyclerView.scrollToPosition(viewModel.getScrollPosition().getValue());
    }

    @Override
    public void onConfigurationChanged(@NonNull Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        //viewModel.setScrollPosition(sGrid.findFirstVisibleItemPosition());
        applyGridToAdapter(sectorAdapter, binding.browseBody);
    }

    public OnItemClickListener itemClick = (item, position) -> {
        InfoFragment infoFragment = new InfoFragment(item, null);
        infoFragment.setButtonOnClickListener(view -> {
            Toast.makeText(getContext(), "tapped", Toast.LENGTH_SHORT).show();
        });
        infoFragment.show(requireActivity().getSupportFragmentManager(), "Info");
    };
}