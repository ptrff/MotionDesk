package ru.ptrff.lwu.view;

import android.os.Bundle;

import androidx.fragment.app.Fragment;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import ru.ptrff.lwu.databinding.FragmentPreviewBinding;

public class PreviewFragment extends Fragment {

    private FragmentPreviewBinding binding;
    public PreviewFragment() {}
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        binding = FragmentPreviewBinding.inflate(inflater);



        return binding.getRoot();
    }

}