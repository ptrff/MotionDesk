package ru.ptrff.lwu.view;


import android.content.Intent;
import android.content.res.Configuration;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.os.Bundle;
import android.transition.Explode;
import android.util.DisplayMetrics;
import android.view.Display;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.core.view.MenuProvider;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.Lifecycle;
import androidx.recyclerview.widget.StaggeredGridLayoutManager;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import com.google.android.material.bottomsheet.BottomSheetBehavior;
import com.google.android.material.bottomsheet.BottomSheetDialog;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import io.reactivex.disposables.Disposable;
import ru.ptrff.lwu.OnItemClickListener;
import ru.ptrff.lwu.R;
import ru.ptrff.lwu.WallpaperPreview;
import ru.ptrff.lwu.databinding.FragmentLibBinding;
import ru.ptrff.lwu.recycler_wpprs.WPPRSAdapter;
import ru.ptrff.lwu.model.WallpaperItem;

public class LibFragment extends Fragment {

    private FragmentLibBinding binding;
    private int RecyclerElementWidth = 140;
    Disposable disposable;
    WPPRSAdapter adapter;
    List<WallpaperItem> strings;


    public LibFragment() {}

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        binding = FragmentLibBinding.inflate(inflater);

        setupPullToResresh();
        loadContent();
        applyGridToAdapter();
        setupActionBarButtons();
        return binding.getRoot();
    }

    private void setupPullToResresh(){
        final SwipeRefreshLayout pullToRefresh = binding.libRefresh;
        pullToRefresh.setOnRefreshListener(() -> pullToRefresh.setRefreshing(false));
    }

    private void setupActionBarButtons() {
        MenuProvider menuProvider = new MenuProvider() {
            @Override
            public void onCreateMenu(@NonNull Menu menu, @NonNull MenuInflater menuInflater) {
                menuInflater.inflate(R.menu.menu_lib, menu);
            }

            @Override
            public boolean onMenuItemSelected(@NonNull MenuItem menuItem) {
                BottomSheetDialog bottomSheetDialog = new BottomSheetDialog(getContext());
                LayoutInflater inflater = getLayoutInflater();
                View sheetView = inflater.inflate(R.layout.create_choose_bottom_sheet,
                        (ViewGroup) getActivity().getWindow().getDecorView().getRootView(), false);
                sheetView.<Button>findViewById(R.id.button_next_step).setOnClickListener(view -> {
                    //TODO
                });
                bottomSheetDialog.setContentView(sheetView);
                BottomSheetBehavior<View> bottomSheetBehavior = BottomSheetBehavior.from((View) sheetView.getParent());
                bottomSheetDialog.setOnShowListener(dialogInterface -> {
                    bottomSheetBehavior.setPeekHeight(sheetView.getHeight());
                });
                bottomSheetDialog.show();
                return true;
            }
        };
        requireActivity().addMenuProvider(menuProvider, getViewLifecycleOwner(), Lifecycle.State.RESUMED);
    }

    private void loadContent(){
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
        binding.libRecycler.setLayoutManager(sGrid);
        binding.libRecycler.setAdapter(adapter);
    }

    @Override
    public void onConfigurationChanged(@NonNull Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        applyGridToAdapter();
    }

    public OnItemClickListener itemClick = (item, position) -> {
        InfoFragment infoFragment = new InfoFragment();
        infoFragment.setAuthor(item.getHeader());
        infoFragment.setDescription(item.getDescription());
        infoFragment.setRating(item.getRating());
        infoFragment.setPreviewBitmap(item.getImage());
        infoFragment.setButtonOnClickListener(view -> {
            startPreview(item);
        });
        infoFragment.show(getActivity().getSupportFragmentManager(), "Info");
    };

    private void startPreview(WallpaperItem item) {
        Intent i = new Intent(getActivity(), WallpaperPreview.class);
        i.putExtra("Name", item.getHeader());
        getActivity().getWindow().setExitTransition(new Explode());
        startActivity(i);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
    }

    @Override
    public void onResume() {
        super.onResume();
    }
}