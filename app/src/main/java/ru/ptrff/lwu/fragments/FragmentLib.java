package ru.ptrff.lwu.fragments;


import static android.content.Context.MODE_PRIVATE;

import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.AsyncTask;
import android.os.Bundle;

import androidx.fragment.app.Fragment;
import androidx.loader.content.AsyncTaskLoader;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.StaggeredGridLayoutManager;

import android.os.Environment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import ru.ptrff.lwu.MainActivity;
import ru.ptrff.lwu.R;
import ru.ptrff.lwu.databinding.ActivityMainBinding;
import ru.ptrff.lwu.databinding.FragmentLibBinding;
import ru.ptrff.lwu.recycler_wpprs.WPPRSAdapter;
import ru.ptrff.lwu.recycler_wpprs.WPPRSListEntity;

public class FragmentLib extends Fragment {

    private FragmentLibBinding binding;


    public FragmentLib() {}

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        binding = FragmentLibBinding.inflate(inflater);

        binding.reloadButton.setOnClickListener(view -> {
            reloadList();
        });

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
        binding.libRecycler.setLayoutManager(sGrid);
        binding.libRecycler.setAdapter(adapter);
    }
}