package ru.ptrff.lwu.view;

import android.content.res.Configuration;
import android.graphics.BitmapFactory;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.core.view.MenuProvider;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.Lifecycle;
import androidx.recyclerview.widget.StaggeredGridLayoutManager;
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

import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.concurrent.ExecutionException;

import ru.ptrff.lwu.OnItemClickListener;
import ru.ptrff.lwu.R;
import ru.ptrff.lwu.databinding.FragmentProfileBinding;
import ru.ptrff.lwu.recycler_wpprs.WPPRSAdapter;
import ru.ptrff.lwu.model.WallpaperItem;

public class ProfileFragment extends Fragment {

    private FragmentProfileBinding binding;
    private int RecyclerElementWidth = 140;
    WPPRSAdapter adapter;
    List<WallpaperItem> strings;

    public ProfileFragment() {}

    @Override
    public void onCreate(Bundle savedInstanceState) {super.onCreate(savedInstanceState);}
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        binding = FragmentProfileBinding.inflate(inflater);
        setupActionBarButtons();


        final SwipeRefreshLayout pullToRefresh = binding.profileRefresh;
        pullToRefresh.setOnRefreshListener(() -> pullToRefresh.setRefreshing(false));

        try {
            loadContent();
        } catch (Exception e) {
            e.printStackTrace();
        }
        applyGridToAdapter();

        return binding.getRoot();
    }

    private void setupActionBarButtons() {
        MenuProvider m = new MenuProvider() {
            @Override
            public void onCreateMenu(@NonNull Menu menu, @NonNull MenuInflater menuInflater) {
                menuInflater.inflate(R.menu.menu_profile, menu);
            }

            @Override
            public boolean onMenuItemSelected(@NonNull MenuItem menuItem) {
                switch(menuItem.getItemId()) {
                    case R.id.settings:
                        System.out.println("bafdfds");
                        return true;
                    case R.id.edit_profile:
                        System.out.println("bafdfds");
                        return true;
                }
                return false;
            }
        };
        requireActivity().addMenuProvider(m, getViewLifecycleOwner(), Lifecycle.State.RESUMED);
    }

    private void loadContent() throws ExecutionException, InterruptedException {
        strings = new ArrayList<>();
        for (int i = 0; i < 2; i++)
            strings.add(
                    new WallpaperItem(i+"",
                            i+"",
                            150 + new Random().nextInt(500),
                            BitmapFactory.decodeResource(getResources(),
                                    R.drawable.kitik),
                            Math.round(new Random().nextInt(90)+new Random().nextFloat()*10)/10.0f
                    )
            );
        adapter = new WPPRSAdapter(strings, itemClick, getResources().getDisplayMetrics());
    }

    private int calculateColumnCount(){
        Display display = getActivity().getWindowManager().getDefaultDisplay();
        DisplayMetrics outMetrics = new DisplayMetrics();
        display.getMetrics(outMetrics);

        float density  = getResources().getDisplayMetrics().density;
        float dpWidth  = outMetrics.widthPixels / density;
        return Math.round(dpWidth/RecyclerElementWidth);
    }

    private void applyGridToAdapter(){
        StaggeredGridLayoutManager sGrid = new StaggeredGridLayoutManager(calculateColumnCount(), StaggeredGridLayoutManager.VERTICAL);
        sGrid.setGapStrategy(StaggeredGridLayoutManager.GAP_HANDLING_MOVE_ITEMS_BETWEEN_SPANS);
        binding.profileRecycler.setLayoutManager(sGrid);
        binding.profileRecycler.setAdapter(adapter);
    }

    @Override
    public void onConfigurationChanged(@NonNull Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        applyGridToAdapter();
    }
    public OnItemClickListener itemClick = new OnItemClickListener() {
        @Override
        public void onItemClick(WallpaperItem item, int position) {
            Toast.makeText(getContext(), item.getHeader(), Toast.LENGTH_SHORT).show();
        }
    };
}