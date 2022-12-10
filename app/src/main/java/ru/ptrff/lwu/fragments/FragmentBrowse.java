package ru.ptrff.lwu.fragments;

import android.os.Bundle;

import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.StaggeredGridLayoutManager;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import ru.ptrff.lwu.R;
import ru.ptrff.lwu.databinding.FragmentBrowseBinding;
import ru.ptrff.lwu.databinding.FragmentProfileBinding;
import ru.ptrff.lwu.recycler_wpprs.WPPRSAdapter;
import ru.ptrff.lwu.recycler_wpprs.WPPRSListEntity;

public class FragmentBrowse extends Fragment {

    private FragmentBrowseBinding binding;
    public FragmentBrowse() {}

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        binding = FragmentBrowseBinding.inflate(inflater);

        reloadList();

        return binding.getRoot();
    }

    void reloadList() {
        List<WPPRSListEntity> strings = new ArrayList<>();
        WPPRSAdapter adapter;
        for (int i = 0; i < 10; i++)
            strings.add(
                    new WPPRSListEntity(i+"",
                            i+"",
                            150 + new Random().nextInt(500)
                    )
            );

        adapter = new WPPRSAdapter(getContext(), strings);

        StaggeredGridLayoutManager sGrid = new StaggeredGridLayoutManager(2, StaggeredGridLayoutManager.VERTICAL);
        sGrid.setGapStrategy(StaggeredGridLayoutManager.GAP_HANDLING_MOVE_ITEMS_BETWEEN_SPANS);
        binding.browseRecycler.setLayoutManager(sGrid);
        binding.browseRecycler.setAdapter(adapter);
    }
}