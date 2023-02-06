package ru.ptrff.motiondesk.view;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.content.res.Configuration;
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
import android.view.Display;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;

import ru.ptrff.motiondesk.OnItemClickListener;
import ru.ptrff.motiondesk.R;
import ru.ptrff.motiondesk.WallpaperPreview;
import ru.ptrff.motiondesk.databinding.FragmentProfileBinding;
import ru.ptrff.motiondesk.adapters.WpprsAdapter;
import ru.ptrff.motiondesk.data.WallpaperItem;
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
        adapter = new WpprsAdapter(viewModel.getItemsLiveData().getValue(), itemClick, getActivity());
        binding.profileRecycler.setHasFixedSize(true);
        binding.profileRecycler.setItemViewCacheSize(0);

        applyGridToAdapter();
        setupActionBarButtons();
        setupPullToRefresh();
        observeContent();
        generateWhileScrolling();

        return binding.getRoot();
    }


    private void generateWhileScrolling() {
        binding.profileRecycler.addOnScrollListener(new RecyclerView.OnScrollListener() {
            @Override
            public void onScrolled(@NonNull RecyclerView recyclerView, int dx, int dy) {
                super.onScrolled(recyclerView, dx, dy);
                if(sGrid.findFirstVisibleItemPosition()!=viewModel.getScrollPosition().getValue()){
                    viewModel.init(sGrid.findLastVisibleItemPosition());
                }
            }
        });
    }


    private void observeContent(){
        viewModel.getItemsLiveData().observe(getViewLifecycleOwner(), wallpaperItems -> {
            adapter.notifyItemInserted(wallpaperItems.size());
        });
    }

    private void setupPullToRefresh() {
        final SwipeRefreshLayout pullToRefresh = binding.profileRefresh;
        pullToRefresh.setOnRefreshListener(() -> {
            viewModel.init(15);
            pullToRefresh.setRefreshing(false);
        });
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
        sGrid = new GridLayoutManager(getContext(), calculateColumnCount());
        binding.profileRecycler.setLayoutManager(sGrid);
        binding.profileRecycler.setAdapter(adapter);
        binding.profileRecycler.scrollToPosition(viewModel.getScrollPosition().getValue());
    }

    @Override
    public void onConfigurationChanged(@NonNull Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        viewModel.setScrollPosition(sGrid.findFirstVisibleItemPosition());
        applyGridToAdapter();
    }

    public OnItemClickListener itemClick = (item, position) -> {
        InfoFragment infoFragment = new InfoFragment();
        infoFragment.setAuthor(item.getAuthor());
        infoFragment.setDescription(item.getDescription());
        infoFragment.setRating(item.getRating());
        //infoFragment.setPreviewBitmap(item.getImage());
        infoFragment.setButtonOnClickListener(view -> {
            startPreview(item);
        });
        infoFragment.show(requireActivity().getSupportFragmentManager(), "Info");
    };

    private void startPreview(WallpaperItem item) {
        Intent i = new Intent(getActivity(), WallpaperPreview.class);
        i.putExtra("Name", item.getAuthor());
        requireActivity().getWindow().setExitTransition(new Explode());
        startActivity(i);
    }
}