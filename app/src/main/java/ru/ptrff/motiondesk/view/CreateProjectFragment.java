package ru.ptrff.motiondesk.view;

import android.animation.TimeInterpolator;
import android.animation.ValueAnimator;
import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.content.res.ColorStateList;
import android.content.res.Resources;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.transition.Explode;
import android.util.DisplayMetrics;
import android.util.TypedValue;
import android.view.Display;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.view.animation.AccelerateDecelerateInterpolator;
import android.view.animation.LinearInterpolator;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.Toast;

import androidx.annotation.NonNull;

import com.google.android.material.bottomsheet.BottomSheetBehavior;
import com.google.android.material.bottomsheet.BottomSheetDialog;
import com.google.android.material.bottomsheet.BottomSheetDialogFragment;
import com.google.android.material.snackbar.BaseTransientBottomBar;
import com.google.android.material.snackbar.Snackbar;

import ru.ptrff.motiondesk.R;
import ru.ptrff.motiondesk.databinding.FragmentCreateProjectBinding;
import ru.ptrff.motiondesk.utils.Validation;

public class CreateProjectFragment extends BottomSheetDialogFragment {

    private FragmentCreateProjectBinding binding;
    private boolean typeChecked = false;
    private boolean nameWritten = false;
    private boolean animating = false;
    private String name;
    private int height;
    private int width;
    private int firstPartHeight;
    private int secondPartHeight;
    private int thirdPartHeight;
    private TextWatcher nameTextWatcher;
    private TextWatcher resTextWatcher;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        binding = FragmentCreateProjectBinding.inflate(inflater);

        fillVars();
        initFirstListeners();

        getDialog().getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_ADJUST_RESIZE);
        return binding.getRoot();
    }

    private void fillVars() {
        binding.secondPart.post(() -> {
            firstPartHeight = binding.firstPart.getHeight();
            secondPartHeight = binding.secondPart.getHeight();
            thirdPartHeight = binding.thirdPart.getHeight();
            animateView(
                    0,
                    binding.firstPart.getHeight(),
                    1,
                    new LinearInterpolator(),
                    (value -> {
                        ViewGroup.LayoutParams newLayoutParams = binding.partsBg.getLayoutParams();
                        newLayoutParams.height = value;
                        binding.partsBg.setLayoutParams(newLayoutParams);
                    })
            );
        });

        nameTextWatcher = new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {
            }

            @Override
            public void afterTextChanged(Editable editable) {
            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
                name = normalizeString(charSequence.toString());
                binding.shortName.setText(name);
                if (Validation.checkString(name)) {
                    nameWritten = true;
                    recolorNext(true);
                    recolorEditText(binding.nameEdit, true);
                } else {
                    nameWritten = false;
                    recolorNext(false);
                    recolorEditText(binding.nameEdit, false);
                }
            }
        };

        resTextWatcher = new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {
            }

            @Override
            public void afterTextChanged(Editable editable) {
            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
                width=getTypedWidth();
                height=getTypedHeight();
                binding.shortResolution.setText(width+ "\nx\n" + height);
            }
        };
    }


    private int getTypedWidth() {
        if (!binding.width.getText().toString().equals("") &&
                Integer.parseInt(binding.width.getText().toString())>=640 &&
                Integer.parseInt(binding.width.getText().toString())<=9000
        ) {
            recolorEditText(binding.width, true);
            return Integer.parseInt(binding.width.getText().toString());
        }else {
            recolorEditText(binding.width, false);
            WindowManager wm = (WindowManager) requireContext().getSystemService(Context.WINDOW_SERVICE);
            Display display = wm.getDefaultDisplay();
            DisplayMetrics metrics = new DisplayMetrics();
            display.getMetrics(metrics);
            return metrics.widthPixels;
        }
    }

    private int getTypedHeight() {
        if (!binding.height.getText().toString().equals("") &&
                Integer.parseInt(binding.height.getText().toString())>=640 &&
                Integer.parseInt(binding.height.getText().toString())<=9000
        ) {
            recolorEditText(binding.height, true);
            return Integer.parseInt(binding.height.getText().toString());
        }else {
            recolorEditText(binding.height, false);
            WindowManager wm = (WindowManager) requireContext().getSystemService(Context.WINDOW_SERVICE);
            Display display = wm.getDefaultDisplay();
            DisplayMetrics metrics = new DisplayMetrics();
            display.getMetrics(metrics);
            return metrics.heightPixels;
        }
    }

    private void initFirstListeners() {

        binding.backButton.setOnClickListener(view -> dismiss());

        binding.typeScene2d.setOnClickListener(view -> {
            binding.typeScene2d.setChecked(true);
            binding.typeGif.setChecked(false);
            binding.typeWeb.setChecked(false);
            binding.shortType.setImageResource(R.drawable.ic_2d_square);
            typeChecked = true;
            recolorNext(true);
        });
        binding.typeGif.setOnClickListener(view -> {
            /*binding.typeScene2d.setChecked(false);
            binding.typeGif.setChecked(true);
            binding.typeWeb.setChecked(false);
            binding.shortType.setImageResource(R.drawable.ic_image);
            typeChecked = true;
            recolorNext(true);*/
            binding.typeGif.setChecked(false);
            Snackbar.make(binding.getRoot(), "В скором времени..", BaseTransientBottomBar.LENGTH_SHORT).show();
        });
        binding.typeWeb.setOnClickListener(view -> {
            /*binding.typeScene2d.setChecked(false);
            binding.typeGif.setChecked(false);
            binding.typeWeb.setChecked(true);
            binding.shortType.setImageResource(R.drawable.ic_code);
            typeChecked = true;
            recolorNext(true);*/
            binding.typeWeb.setChecked(false);
            Snackbar.make(binding.getRoot(), "В скором времени..", BaseTransientBottomBar.LENGTH_SHORT).show();
        });
        binding.nextButton.setOnClickListener(view -> {
            if (typeChecked && !animating) {
                animateToNaming();
            }
        });
    }

    private void animateToNaming() {
        animating = true;
        recolorNext(nameWritten);
        binding.backButton.setImageResource(R.drawable.ic_back);

        binding.secondPart.setVisibility(View.VISIBLE);
        binding.firstPart.animate().alpha(0).setDuration(250).withEndAction(() -> {
            binding.firstPart.setVisibility(View.INVISIBLE);
            binding.secondPart.animate().alpha(1).setDuration(250).withEndAction(() -> {
                animating = false;
            }).start();
        }).start();

        animateView(
                firstPartHeight,
                secondPartHeight,
                500,
                new AccelerateDecelerateInterpolator(),
                (value -> {
                    ViewGroup.LayoutParams newLayoutParams = binding.partsBg.getLayoutParams();
                    newLayoutParams.height = value;
                    binding.partsBg.setLayoutParams(newLayoutParams);
                })
        );

        binding.shortType.animate().alpha(1).setDuration(250).start();

        initSecondListeners();
    }

    private void returnToFirst() {
        animating = true;
        initFirstListeners();
        binding.backButton.setImageResource(R.drawable.ic_close);
        recolorNext(typeChecked);

        binding.firstPart.setVisibility(View.VISIBLE);
        binding.secondPart.animate().alpha(0).setDuration(250).withEndAction(() -> {
            binding.secondPart.setVisibility(View.INVISIBLE);
            binding.firstPart.animate().alpha(1).setDuration(250).withEndAction(() -> {
                animating = false;
            }).start();
        }).start();

        animateView(
                secondPartHeight,
                firstPartHeight,
                500,
                new AccelerateDecelerateInterpolator(),
                (value -> {
                    ViewGroup.LayoutParams newLayoutParams = binding.partsBg.getLayoutParams();
                    newLayoutParams.height = value;
                    binding.partsBg.setLayoutParams(newLayoutParams);
                })
        );

        binding.nameEdit.removeTextChangedListener(nameTextWatcher);
    }

    private void initSecondListeners() {

        binding.backButton.setOnClickListener(view -> {
            if (!animating) returnToFirst();
        });

        binding.nameEdit.addTextChangedListener(nameTextWatcher);

        binding.nextButton.setOnClickListener(view -> {
            if (!animating && nameWritten) animateToResolution();
        });

        binding.nameEdit.setOnEditorActionListener((textView, i, keyEvent) -> {
            if (!animating && nameWritten) animateToResolution();
            return true;
        });
    }

    private void animateToResolution() {
        animating = true;
        recolorNext(true);
        binding.nextButton.setImageResource(R.drawable.ic_done);

        binding.shortResolution.setText(getTypedWidth() + "\nx\n" + getTypedHeight());
        height=getTypedHeight();
        width=getTypedWidth();
        binding.shortName.animate().alpha(1).setDuration(250).withEndAction(() -> {
            binding.shortResolution.animate().alpha(1).setDuration(250).start();
        }).start();



        binding.thirdPart.setVisibility(View.VISIBLE);
        binding.secondPart.animate().alpha(0).setDuration(250).withEndAction(() -> {
            binding.secondPart.setVisibility(View.INVISIBLE);
            binding.thirdPart.animate().alpha(1).setDuration(250).withEndAction(() -> {
                animating = false;
            }).start();
        }).start();

        binding.width.requestFocus();

        animateView(
                secondPartHeight,
                thirdPartHeight,
                500,
                new AccelerateDecelerateInterpolator(),
                (value -> {
                    ViewGroup.LayoutParams newLayoutParams = binding.partsBg.getLayoutParams();
                    newLayoutParams.height = value;
                    binding.partsBg.setLayoutParams(newLayoutParams);
                })
        );

        initThirdListeners();
    }

    private void returnToSecond() {
        animating = true;
        binding.nextButton.setImageResource(R.drawable.ic_forward);

        binding.nameEdit.requestFocus();

        binding.secondPart.setVisibility(View.VISIBLE);
        binding.thirdPart.animate().alpha(0).setDuration(250).withEndAction(() -> {
            binding.thirdPart.setVisibility(View.INVISIBLE);
            binding.secondPart.animate().alpha(1).setDuration(250).withEndAction(() -> {
                animating = false;
            }).start();
        }).start();

        animateView(
                thirdPartHeight,
                secondPartHeight,
                500,
                new AccelerateDecelerateInterpolator(),
                (value -> {
                    ViewGroup.LayoutParams newLayoutParams = binding.partsBg.getLayoutParams();
                    newLayoutParams.height = value;
                    binding.partsBg.setLayoutParams(newLayoutParams);
                })
        );
        initSecondListeners();
        binding.height.removeTextChangedListener(resTextWatcher);
        binding.height.removeTextChangedListener(resTextWatcher);
    }

    private void initThirdListeners() {
        binding.backButton.setOnClickListener(view -> {
            if (!animating) returnToSecond();
        });

        binding.height.addTextChangedListener(resTextWatcher);
        binding.width.addTextChangedListener(resTextWatcher);

        binding.nextButton.setOnClickListener(view -> {
            if (!animating) createProject();
        });

        binding.height.setOnEditorActionListener((textView, i, keyEvent) -> {
            if (!animating) createProject();
            return true;
        });

    }

    private void createProject(){
        Intent i = new Intent(getActivity(), WallpaperEditor.class);
        i.putExtra("Name", name);
        i.putExtra("Width", width);
        i.putExtra("Height", height);
        requireActivity().getWindow().setExitTransition(new Explode());
        startActivity(i);

        dismiss();
    }

    private String normalizeString(String input) {
        return input.trim().replaceAll("\\s+", " ");
    }

    private void recolorNext(boolean state) {
        TypedValue typedValue = new TypedValue();
        Resources.Theme theme = requireContext().getTheme();
        if (state) theme.resolveAttribute(androidx.appcompat.R.attr.colorAccent, typedValue, true);
        else theme.resolveAttribute(R.attr.bottomNavBarNotChecked, typedValue, true);
        binding.nextButton.setBackgroundTintList(ColorStateList.valueOf(typedValue.data));
        binding.nextButton.setImageTintList(ColorStateList.valueOf(typedValue.data));
    }

    private void recolorEditText(EditText editText, boolean state){
        TypedValue typedValue = new TypedValue();
        Resources.Theme theme = requireContext().getTheme();
        if (state) theme.resolveAttribute(androidx.appcompat.R.attr.colorAccent, typedValue, true);
        else theme.resolveAttribute(R.attr.bottomNavBarNotChecked, typedValue, true);
        editText.setBackgroundTintList(ColorStateList.valueOf(typedValue.data));
        editText.setCompoundDrawableTintList(ColorStateList.valueOf(typedValue.data));
    }

    private void animateView(int from, int to, int duration, TimeInterpolator interpolator, AnimationExecutor executor) {
        ValueAnimator animator = ValueAnimator.ofInt(from, to);
        animator.addUpdateListener(valueAnimator -> {
            int value = (int) valueAnimator.getAnimatedValue();
            executor.execute(value);
        });
        animator.setDuration(duration);
        animator.setInterpolator(interpolator);
        animator.start();
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

    private interface AnimationExecutor {
        void execute(int value);
    }
}
