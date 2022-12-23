package ru.ptrff.lwu.view;

import android.app.Dialog;
import android.graphics.Bitmap;
import android.os.Bundle;

import androidx.annotation.NonNull;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;

import com.google.android.material.bottomsheet.BottomSheetBehavior;
import com.google.android.material.bottomsheet.BottomSheetDialog;
import com.google.android.material.bottomsheet.BottomSheetDialogFragment;

import ru.ptrff.lwu.databinding.FragmentInfoBinding;

public class InfoFragment extends BottomSheetDialogFragment {
    private FragmentInfoBinding binding;
    private Bitmap previewBitmap;
    private String name;
    private String description;
    private float rating;
    private View.OnClickListener click;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        binding = FragmentInfoBinding.inflate(inflater);
        bindData();
        return binding.getRoot();
    }

    private void bindData() {
        binding.wallpaperPreview.setImageBitmap(previewBitmap);
        binding.author.setText(name);
        binding.description.setText(description);
        binding.rating.setText(rating + "/10");
        binding.buttonNextStep.setOnClickListener(click);
    }

    @NonNull
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        BottomSheetDialog dialog = (BottomSheetDialog) super.onCreateDialog(savedInstanceState);
        dialog.setOnShowListener(dialogInterface -> {
            BottomSheetDialog d = (BottomSheetDialog) dialogInterface;
            FrameLayout bottomSheet = d.findViewById(com.google.android.material.R.id.design_bottom_sheet);
            BottomSheetBehavior.from(bottomSheet).setPeekHeight(bottomSheet.getHeight());
        });
        return dialog;
    }

    public void setPreviewBitmap(Bitmap bitmap) {
        if (bitmap != null) {
            previewBitmap = bitmap;
        }
    }

    public void setAuthor(String name) {
        this.name = name;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public void setRating(float rating) {
        this.rating = rating;
    }

    public void setButtonOnClickListener(View.OnClickListener listener) {
        click = listener;
    }
}