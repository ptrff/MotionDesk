package ru.ptrff.motiondesk.view;

import android.app.Dialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;

import androidx.annotation.NonNull;

import com.google.android.material.bottomsheet.BottomSheetBehavior;
import com.google.android.material.bottomsheet.BottomSheetDialog;
import com.google.android.material.bottomsheet.BottomSheetDialogFragment;

import ru.ptrff.motiondesk.databinding.FragmentParametersBinding;

public class ParametersFragment extends BottomSheetDialogFragment {

    private FragmentParametersBinding binding;
    private BottomSheetCallback bottomSheetCallback;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        binding = FragmentParametersBinding.inflate(inflater);



        return binding.getRoot();
    }


    public interface BottomSheetCallback {
        void onDismiss();
    }
    @Override
    public void onDismiss(DialogInterface dialog) {
        super.onDismiss(dialog);
        if(bottomSheetCallback!=null)
            bottomSheetCallback.onDismiss();
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

    public void setBottomSheetCallback(BottomSheetCallback callback) {
        this.bottomSheetCallback = callback;
    }
}

