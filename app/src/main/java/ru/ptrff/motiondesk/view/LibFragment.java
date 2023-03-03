package ru.ptrff.motiondesk.view;

import android.app.AlertDialog;
import android.content.Intent;
import android.content.res.ColorStateList;
import android.content.res.Configuration;
import android.content.res.Resources;
import android.os.Bundle;
import android.transition.Explode;
import android.util.DisplayMetrics;
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

import ru.ptrff.motiondesk.utils.Converter;
import ru.ptrff.motiondesk.R;
import ru.ptrff.motiondesk.data.WallpaperItem;
import ru.ptrff.motiondesk.databinding.FragmentLibBinding;
import ru.ptrff.motiondesk.adapters.WpprsAdapter;
import ru.ptrff.motiondesk.viewmodel.LibViewModel;


public class LibFragment extends Fragment {

    private static final int RecyclerElementWidth = 140;
    private FragmentLibBinding binding;
    private LibViewModel viewModel;
    private GridLayoutManager sGrid;
    private WpprsAdapter adapter;

    @Override
    public void onCreate(Bundle savedInstanceState) {super.onCreate(savedInstanceState);}

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        binding = FragmentLibBinding.inflate(inflater);
        viewModel = new ViewModelProvider(this).get(LibViewModel.class);
        adapter = new WpprsAdapter(viewModel.getItemsLiveData().getValue(), itemClick, itemLongClickListener,  getActivity());
        binding.libRecycler.setHasFixedSize(true);
        binding.libRecycler.setItemViewCacheSize(0);

        applyGridToAdapter();
        setupActionBarButtons();
        setupPullToRefresh();
        observeContent();
        generateWhileScrolling();

        return binding.getRoot();
    }

    private void generateWhileScrolling() {
        binding.libRecycler.addOnScrollListener(new RecyclerView.OnScrollListener() {
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
        final SwipeRefreshLayout pullToRefresh = binding.libRefresh;
        pullToRefresh.setOnRefreshListener(() -> {
            viewModel.init(15);
            pullToRefresh.setRefreshing(false);
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

    public OnItemLongClickListener itemLongClickListener = (item, position) -> {
        AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
        TypedValue typedValue = new TypedValue();
        Resources.Theme theme = requireContext().getTheme();

        LinearLayout dialogLayout = new LinearLayout(getContext());
        dialogLayout.setOrientation(LinearLayout.VERTICAL);
        dialogLayout.setGravity(Gravity.CENTER);
        dialogLayout.setPadding(Converter.dpToPx(15), Converter.dpToPx(15), Converter.dpToPx(15), Converter.dpToPx(15));

        theme.resolveAttribute(R.attr.foregroundBlackWhite, typedValue, true);
        TextView title = new TextView(getContext());
        title.setText("Выберите действие:");
        title.setTextColor(typedValue.data);
        title.setTextSize(16);
        title.setPadding(0, 0, 0, Converter.dpToPx(15));
        dialogLayout.addView(title);

        LinearLayout buttonsLayout = new LinearLayout(getContext());
        buttonsLayout.setOrientation(LinearLayout.HORIZONTAL);


        theme.resolveAttribute(androidx.appcompat.R.attr.colorAccent, typedValue, true);

        ImageButton button = new ImageButton(getContext());
        button.setImageResource(R.drawable.ic_image);
        button.setImageTintList(ColorStateList.valueOf(typedValue.data));
        button.setBackgroundResource(R.drawable.rounded_outline_button);
        button.setBackgroundTintList(ColorStateList.valueOf(typedValue.data));
        LinearLayout.LayoutParams v = new LinearLayout.LayoutParams(Converter.dpToPx(50), Converter.dpToPx(50));
        v.setMargins(0, 0, Converter.dpToPx(15), 0);
        button.setLayoutParams(v);
        buttonsLayout.addView(button);

        button = new ImageButton(getContext());
        button.setImageResource(R.drawable.ic_star);
        button.setImageTintList(ColorStateList.valueOf(typedValue.data));
        button.setBackgroundResource(R.drawable.rounded_outline_button);
        button.setBackgroundTintList(ColorStateList.valueOf(typedValue.data));
        button.setLayoutParams(v);
        buttonsLayout.addView(button);

        button = new ImageButton(getContext());
        button.setImageResource(R.drawable.ic_delete);
        button.setImageTintList(ColorStateList.valueOf(getResources().getColor(R.color.red, theme)));
        button.setBackgroundResource(R.drawable.rounded_outline_button);
        button.setBackgroundTintList(ColorStateList.valueOf(getResources().getColor(R.color.red, theme)));
        button.setLayoutParams(v);
        buttonsLayout.addView(button);

        dialogLayout.addView(buttonsLayout);
        builder.setView(dialogLayout);

        AlertDialog dialog = builder.create();
        dialog.getWindow().setBackgroundDrawableResource(R.drawable.rounded_bottom_dialog);
        dialog.show();
    };

    public OnItemClickListener itemClick = (item, position) -> {
        InfoFragment infoFragment = new InfoFragment();
        infoFragment.setAuthor(item.getAuthor());
        infoFragment.setName(item.getName());
        infoFragment.setDescription(item.getDescription());
        infoFragment.setStars(item.getStars());
        infoFragment.setRating(item.getRating());
        //infoFragment.setPreviewBitmap(item.getImage());
        infoFragment.setButtonOnClickListener(view -> {
            startPreview(item);
        });
        infoFragment.show(requireActivity().getSupportFragmentManager(), "Info");
    };

    private void startPreview(WallpaperItem item) {
        Intent i = new Intent(getActivity(), WallpaperPreview.class);
        i.putExtra("Name", item.getName());
        requireActivity().getWindow().setExitTransition(new Explode());
        startActivity(i);
    }
}