package ru.ptrff.motiondesk.view;

import android.animation.ValueAnimator;
import android.app.AlertDialog;
import android.content.res.ColorStateList;
import android.content.res.Configuration;
import android.graphics.Color;
import android.graphics.Point;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.TypedValue;
import android.view.Display;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.AccelerateDecelerateInterpolator;
import android.widget.LinearLayout;
import android.widget.PopupWindow;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.cardview.widget.CardView;
import androidx.fragment.app.Fragment;

import java.util.Timer;
import java.util.TimerTask;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import ru.ptrff.motiondesk.R;
import ru.ptrff.motiondesk.databinding.FragmentAuthBinding;
import ru.ptrff.motiondesk.databinding.StateViewBinding;

public class AuthFragment extends Fragment {

    private FragmentAuthBinding binding;
    private boolean passwordFilled = false;
    private boolean nicknameFilled = false;
    private final AuthCloser authCloser;
    private PopupWindow loadingWindow;

    AuthFragment(AuthCloser closer){
        authCloser = closer;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        binding = FragmentAuthBinding.inflate(getLayoutInflater());

        setupOrientation(getResources().getConfiguration().orientation);

        animateFieldOnStart();
        addFieldsTextListeners();
        addHintsClickListeners();
        setLoginOnClickListener();

        return binding.getRoot();
    }


    private void setupOrientation(int orientation){
        if (orientation == Configuration.ORIENTATION_LANDSCAPE) {
            binding.rootLinearLayout.setOrientation(LinearLayout.HORIZONTAL);
        } else {
            binding.rootLinearLayout.setOrientation(LinearLayout.VERTICAL);
        }
    }

    private void setLoginOnClickListener(){
        binding.login.setOnClickListener(view -> {
            if(nicknameFilled && passwordFilled){

            }else if(nicknameFilled && checkEmail(binding.nickname.getText().toString())){
                AlertDialog.Builder builder = new AlertDialog.Builder(requireContext());

                StateViewBinding dialogBinding = StateViewBinding.bind(getLayoutInflater().inflate(R.layout.state_view, null));
                builder.setView(dialogBinding.getRoot());

                dialogBinding.title.setText("Восстановление доступа");
                dialogBinding.description.setText("Вы точно хотите сбросить пароль аккаунта?");
                dialogBinding.image.setVisibility(View.GONE);
                dialogBinding.button.setText("Нет");
                dialogBinding.button.setBackgroundTintList(ColorStateList.valueOf(requireContext().getColor(R.color.red)));
                dialogBinding.button.setTextColor(ColorStateList.valueOf(requireContext().getColor(R.color.red)));
                dialogBinding.button2.setText("Да");
                dialogBinding.button2.setVisibility(View.VISIBLE);
                dialogBinding.buttonsSpace.setVisibility(View.VISIBLE);

                AlertDialog dialog = builder.create();
                dialogBinding.button.setOnClickListener(v -> dialog.dismiss());
                dialogBinding.button2.setOnClickListener(v -> dialog.dismiss());

                dialog.getWindow().setBackgroundDrawableResource(R.drawable.rounded_bottom_dialog);
                dialog.show();

            }else if(nicknameFilled){
                Toast.makeText(requireContext(), "Для восстановления пароля введите почту", Toast.LENGTH_SHORT).show();
            }else{
                showLoading();
                Timer t = new Timer();
                t.schedule(new TimerTask() {
                    @Override
                    public void run() {
                        authCloser.closeAuthPage();
                        closeLoading();
                        t.cancel();
                    }
                }, 2000);
            }
        });
    }

    private void addFieldsTextListeners() {
        binding.nickname.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {
            }

            @Override
            public void afterTextChanged(Editable editable) {
            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
                nicknameFilled = checkString(charSequence.toString(), 5, 30, 0);
                setFieldsAndButtonColor();
            }
        });

        binding.password.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {
            }

            @Override
            public void afterTextChanged(Editable editable) {
            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
                passwordFilled = checkString(charSequence.toString(), 8, 30, 10);
                setFieldsAndButtonColor();
            }
        });
    }

    private void setFieldsAndButtonColor() {
        int loginButtonColor;
        int textfieldColor;
        if (nicknameFilled && !passwordFilled) {
            TypedValue typedValue = new TypedValue();
            requireContext().getTheme().resolveAttribute(com.google.android.material.R.attr.colorSecondary, typedValue, true);
            loginButtonColor = typedValue.data;
            requireContext().getTheme().resolveAttribute(androidx.appcompat.R.attr.colorAccent, typedValue, true);
            textfieldColor = typedValue.data;
        } else if (nicknameFilled) {
            TypedValue typedValue = new TypedValue();
            requireContext().getTheme().resolveAttribute(androidx.appcompat.R.attr.colorAccent, typedValue, true);
            loginButtonColor = typedValue.data;
            textfieldColor = typedValue.data;
        } else {
            TypedValue typedValue = new TypedValue();
            requireContext().getTheme().resolveAttribute(R.attr.bottomNavBarNotChecked, typedValue, true);
            loginButtonColor = typedValue.data;
            textfieldColor = typedValue.data;
        }
        binding.login.setBackgroundTintList(ColorStateList.valueOf(loginButtonColor));
        binding.password.setBackgroundTintList(ColorStateList.valueOf(loginButtonColor));
        binding.password.setTextColor(ColorStateList.valueOf(loginButtonColor));
        binding.password.setHintTextColor(ColorStateList.valueOf(loginButtonColor));
        binding.passwordInputLayout.setStartIconTintList(ColorStateList.valueOf(loginButtonColor));
        binding.passwordInputLayout.setEndIconTintList(ColorStateList.valueOf(loginButtonColor));
        binding.nickname.setBackgroundTintList(ColorStateList.valueOf(textfieldColor));
        binding.nickname.setTextColor(ColorStateList.valueOf(textfieldColor));
        binding.nicknameInputLayout.setStartIconTintList(ColorStateList.valueOf(textfieldColor));
    }


    public static boolean checkEmail(String email) {
        Pattern p = Pattern.compile("^[A-Za-z0-9._%+-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}$");
        Matcher m = p.matcher(email);
        return m.matches();
    }

    private boolean checkString(String input, int lenght, int maxlength, int maxSpecialChars) {
        int letterCount = 0;
        int specialCharCount = 0;
        int at = 0;
        for (int i = 0; i < input.length(); i++) {
            char c = input.charAt(i);
            if (Character.isLetter(c)) {
                letterCount++;
            }else if (!Character.isDigit(c) && !Character.isWhitespace(c) && c != '_' && c != '.' && c!='@') {
                specialCharCount++;
            }
        }
        return letterCount >= lenght && input.length() <= maxlength && specialCharCount <= maxSpecialChars;
    }

    private void animateFieldOnStart() {
        binding.nickname.post(() -> {
            TypedValue typedValue = new TypedValue();
            requireContext().getTheme().resolveAttribute(androidx.appcompat.R.attr.colorAccent, typedValue, true);
            int initialColor = typedValue.data;
            requireContext().getTheme().resolveAttribute(R.attr.bottomNavBarNotChecked, typedValue, true);
            ValueAnimator anim = ValueAnimator.ofArgb(initialColor, typedValue.data);
            anim.setDuration(500);
            anim.addUpdateListener(animation -> {
                int color = (int) animation.getAnimatedValue();
                binding.nickname.setBackgroundTintList(ColorStateList.valueOf(color));
                binding.nicknameInputLayout.setStartIconTintList(ColorStateList.valueOf(color));
                binding.password.setBackgroundTintList(ColorStateList.valueOf(color));
                binding.passwordInputLayout.setStartIconTintList(ColorStateList.valueOf(color));
                binding.passwordInputLayout.setEndIconTintList(ColorStateList.valueOf(color));

            });
            anim.setInterpolator(new AccelerateDecelerateInterpolator());
            anim.setRepeatCount(2);
            anim.setRepeatMode(ValueAnimator.REVERSE);
            anim.start();
        });
    }

    private void addHintsClickListeners() {
        binding.textCreate.setOnClickListener(v -> {
            TextView popupText = new TextView(requireContext());
            popupText.setTextColor(Color.WHITE);
            popupText.setText("Введите e-mail, пароль и войдите впервые, чтобы создать аккаунт!");
            popupText.setPadding(15, 15, 15, 15);

            Display display = requireActivity().getWindowManager().getDefaultDisplay();
            Point size = new Point();
            display.getSize(size);

            int[] location = new int[2];
            binding.nickname.getLocationOnScreen(location);
            int xOffset = location[0] + binding.nickname.getWidth() / 2;
            int yOffset = location[1] + binding.nickname.getHeight() / 2;

            PopupWindow popupWindow = new PopupWindow(popupText, size.x/5*2, ViewGroup.LayoutParams.WRAP_CONTENT);
            popupWindow.setBackgroundDrawable(requireContext().getDrawable(R.drawable.rounded_hint));
            popupWindow.setElevation(10);
            popupWindow.setAnimationStyle(android.R.style.Animation_Dialog);
            popupWindow.setOutsideTouchable(true);
            popupWindow.showAtLocation(binding.textCreate, Gravity.NO_GRAVITY, xOffset, yOffset);
        });

        binding.textRecover.setOnClickListener(v -> {
            TextView popupText = new TextView(requireContext());
            popupText.setTextColor(Color.WHITE);
            popupText.setText("Оставьте поле пароля пустым, чтобы сбросить его!");
            popupText.setPadding(15, 15, 15, 15);

            Display display = requireActivity().getWindowManager().getDefaultDisplay();
            Point size = new Point();
            display.getSize(size);

            int[] location = new int[2];
            binding.password.getLocationOnScreen(location);
            int xOffset = location[0] + binding.password.getWidth() / 2;
            int yOffset = location[1] + binding.password.getHeight() / 2;

            PopupWindow popupWindow = new PopupWindow(popupText, size.x/5*2, ViewGroup.LayoutParams.WRAP_CONTENT);
            popupWindow.setBackgroundDrawable(requireContext().getDrawable(R.drawable.rounded_hint));
            popupWindow.setElevation(10);
            popupWindow.setAnimationStyle(android.R.style.Animation_Dialog);
            popupWindow.setOutsideTouchable(true);
            popupWindow.showAtLocation(binding.textCreate, Gravity.NO_GRAVITY, xOffset, yOffset);
        });
    }

    private void showLoading(){
        Display display = requireActivity().getWindowManager().getDefaultDisplay();
        Point size = new Point();
        display.getSize(size);
        int windowSize = (int) TypedValue.applyDimension(
                TypedValue.COMPLEX_UNIT_DIP,
                150,
                getResources().getDisplayMetrics()
        );

        LinearLayout rootView = new LinearLayout(requireContext());
        rootView.setGravity(Gravity.CENTER);

        CardView cardMask = new CardView(requireContext());
        cardMask.setRadius(50);

        ShimmerView shimmerView = new ShimmerView(requireContext(), true);
        shimmerView.setLayoutParams(new LinearLayout.LayoutParams(windowSize, windowSize));
        shimmerView.startShimmerAnimation();
        shimmerView.setRadius(20);
        shimmerView.setShimmerAnimationDuration(1500);

        cardMask.addView(shimmerView);
        rootView.addView(cardMask);

        loadingWindow = new PopupWindow(rootView, LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.MATCH_PARENT);
        ColorDrawable colorDrawable = new ColorDrawable(Color.BLACK);
        colorDrawable.setAlpha(128);
        loadingWindow.setBackgroundDrawable(colorDrawable);
        loadingWindow.setBackgroundDrawable(colorDrawable);
        loadingWindow.setElevation(10);

        loadingWindow.setAnimationStyle(android.R.style.Animation_Toast);
        loadingWindow.setOutsideTouchable(false);
        loadingWindow.showAtLocation(binding.textCreate, Gravity.CENTER, 0, 0);
    }

    private void closeLoading(){
        requireActivity().runOnUiThread(() -> loadingWindow.dismiss());
    }


    @Override
    public void onConfigurationChanged(@NonNull Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        setupOrientation(newConfig.orientation);
    }

    public interface AuthCloser{
        void closeAuthPage();
    }
}