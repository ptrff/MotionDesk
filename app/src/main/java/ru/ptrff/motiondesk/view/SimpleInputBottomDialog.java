package ru.ptrff.motiondesk.view;

import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.res.Resources;
import android.os.Bundle;
import android.text.Editable;
import android.text.InputType;
import android.text.TextWatcher;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import android.widget.Space;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.widget.NestedScrollView;
import androidx.fragment.app.DialogFragment;

import com.badlogic.gdx.graphics.Color;
import com.google.android.material.bottomsheet.BottomSheetBehavior;
import com.google.android.material.bottomsheet.BottomSheetDialog;
import com.google.android.material.bottomsheet.BottomSheetDialogFragment;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;
import com.google.android.material.textview.MaterialTextView;

import java.util.List;
import java.util.Objects;

import ru.ptrff.motiondesk.R;
import ru.ptrff.motiondesk.models.ParameterField;
import ru.ptrff.motiondesk.utils.Validation;

public class SimpleInputBottomDialog extends BottomSheetDialogFragment {

    private final NestedScrollView rootLayout;
    private final LinearLayout root;
    private final TextView title;
    private ParameterFieldCallback parameterFieldCallback;
    private ImageButtonCallback imageButtonCallback;
    private InputFieldCallback inputFieldCallback;
    private ColorPickerCallback colorPickerCallback;

    public SimpleInputBottomDialog(Context context) {
        int distance_l = (int) TypedValue.applyDimension(
                TypedValue.COMPLEX_UNIT_PX,
                context.getResources().getDimension(R.dimen.distance_l),
                context.getResources().getDisplayMetrics()
        );

        int distance_m = (int) TypedValue.applyDimension(
                TypedValue.COMPLEX_UNIT_PX,
                context.getResources().getDimension(R.dimen.distance_m),
                context.getResources().getDisplayMetrics()
        );

        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);

        root = new LinearLayout(context);
        root.setOrientation(LinearLayout.VERTICAL);
        root.setLayoutParams(params);

        title = new TextView(context);
        title.setPadding(0, 0, 0, distance_m);
        title.setTextSize(20);
        TypedValue typedValue = new TypedValue();
        Resources.Theme theme = context.getTheme();
        theme.resolveAttribute(R.attr.foregroundBlackWhite, typedValue, true);
        title.setTextColor(typedValue.data);
        title.setLayoutParams(params);
        title.setGravity(Gravity.CENTER);
        root.addView(title);

        LinearLayout.LayoutParams params_margin = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        params_margin.setMargins(distance_l, distance_l, distance_l, distance_l);
        rootLayout = new NestedScrollView(context);
        rootLayout.setLayoutParams(params_margin);
        rootLayout.setPadding(distance_l, distance_l, distance_l, distance_l);
        rootLayout.setBackground(context.getDrawable(R.drawable.rounded_bottom_dialog));
        rootLayout.addView(root);
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return rootLayout;
    }

    public static class Builder {
        private final SimpleInputBottomDialog dialog;
        private final Context context;
        private final int accentColor;
        private final int distance_s;

        public Builder(Context context) {
            this.context = context;
            distance_s = (int) TypedValue.applyDimension(
                    TypedValue.COMPLEX_UNIT_PX,
                    context.getResources().getDimension(R.dimen.distance_s),
                    context.getResources().getDisplayMetrics()
            );

            dialog = new SimpleInputBottomDialog(context);
            dialog.setStyle(DialogFragment.STYLE_NORMAL, R.style.BottomSheetDialogTheme);

            TypedValue typedValue = new TypedValue();
            Resources.Theme theme = context.getTheme();
            theme.resolveAttribute(androidx.appcompat.R.attr.colorAccent, typedValue, true);
            accentColor = typedValue.data;
        }

        public Builder setBottomSheetCallback(ParameterFieldCallback callback) {
            dialog.setParameterFieldCallback(callback);
            return this;
        }

        public Builder setTitle(String text) {
            dialog.title.setText(text);
            return this;
        }

        public Builder addLabel(String text) {

            TextView label = new TextView(context);
            label.setText(text);
            label.setTextSize(18);
            label.setTextColor(accentColor);
            label.setPadding(2, 0, 2, 0);


            View view = new View(context);
            view.setLayoutParams(new LinearLayout.LayoutParams(
                    ViewGroup.LayoutParams.MATCH_PARENT, (int) TypedValue.applyDimension(
                    TypedValue.COMPLEX_UNIT_DIP,
                    1,
                    context.getResources().getDisplayMetrics()
            )));
            view.setBackgroundColor(accentColor);
            view.setPadding(2, 0, 2, 0);

            dialog.root.addView(label);
            dialog.root.addView(view);
            return this;
        }

        public Builder addTextField(String typeName, String hint, String type, Object min, Object max, Object value) {
            View view = LayoutInflater.from(context).inflate(R.layout.simple_input_bottom_dialog_input_field, null);
            TextInputEditText editText = view.findViewById(R.id.input_field);
            TextInputLayout inputLayout = view.findViewById(R.id.input_field_layout);
            String minVal;
            String maxVal;
            if(Integer.parseInt(min.toString()) == Integer.MIN_VALUE){
                minVal = "-∞";
            }else {
                minVal = min.toString();
            }
            if(Integer.parseInt(max.toString()) == Integer.MAX_VALUE){
                maxVal = "∞";
            }else {
                maxVal = max.toString();
            }
            inputLayout.setHint(hint + "  (" + minVal + "; " + maxVal + ")");



            int inputType = InputType.TYPE_CLASS_TEXT;
            if (type.equals("int")) {
                editText.setText(Integer.parseInt(value.toString()));
                inputType = InputType.TYPE_CLASS_NUMBER;
            }
            if (type.equals("float")) {
                editText.setText(Float.parseFloat(value.toString()) + "");
                inputType = InputType.TYPE_CLASS_NUMBER | InputType.TYPE_NUMBER_FLAG_DECIMAL;
            }
            if (type.equals("string")) {
                editText.setText(value.toString());
            }
            if (type.equals("color")) {
                editText.setText(String.format("#%06X", (0xFFFFFF & ((Color) value).toIntBits())));

                inputLayout.setEndIconMode(TextInputLayout.END_ICON_CUSTOM);
                inputLayout.setEndIconDrawable(R.drawable.ic_pick_color);
                inputLayout.setHint(hint);


                inputLayout.setEndIconOnClickListener(v -> {
                    dialog.colorPickerCallback.showColorPickerDialog(editText);
                });
            }

            if ((min + "").toCharArray()[0] == '-') {
                inputType = inputType | InputType.TYPE_NUMBER_FLAG_SIGNED;
            }

            editText.setInputType(inputType);


            Space space = new Space(context);
            space.setLayoutParams(new LinearLayout.LayoutParams(0, distance_s));

            editText.addTextChangedListener(new TextWatcher() {
                @Override
                public void beforeTextChanged(CharSequence s, int start, int count, int after) {
                }

                @Override
                public void onTextChanged(CharSequence s, int start, int before, int count) {
                }

                @Override
                public void afterTextChanged(Editable s) {
                    if (!s.toString().equals("") && !s.toString().equals("-")) {
                        Object checkedValue = Validation.checkValue(s.toString(), type, min, max);
                        if (checkedValue != null) {
                            if (Objects.equals(typeName, "name")) {
                                dialog.title.setText(checkedValue.toString());
                            }
                            dialog.inputFieldCallback.onInputFieldChanged(typeName, s.toString());
                        }
                    }
                }
            });

            dialog.root.addView(view);
            dialog.root.addView(space);
            return this;
        }

        public Builder addTextField(ParameterField parameter) {
            View view = LayoutInflater.from(context).inflate(R.layout.simple_input_bottom_dialog_input_field, null);
            TextInputEditText editText = view.findViewById(R.id.input_field);
            TextInputLayout inputLayout = view.findViewById(R.id.input_field_layout);
            inputLayout.setHint(
                    parameter.getName() +
                            "  (" + parameter.getMin() +
                            " - " + parameter.getMax() + ")"
            );

            editText.setText(parameter.getValue().toString());


            int inputType = InputType.TYPE_CLASS_TEXT;
            if (parameter.getType().equals("int")) {
                inputType = InputType.TYPE_CLASS_NUMBER;
            }
            if (parameter.getType().equals("float")) {
                inputType = InputType.TYPE_CLASS_NUMBER | InputType.TYPE_NUMBER_FLAG_DECIMAL;
            }
            if (parameter.getType().equals("color")) {
                editText.setCompoundDrawablesRelativeWithIntrinsicBounds(0, 0, R.drawable.ic_pick_color, 0);
            }


            if (parameter.getMin().toString().toCharArray()[0] == '-') {
                inputType = inputType | InputType.TYPE_NUMBER_FLAG_SIGNED;
            }

            editText.setInputType(inputType);


            Space space = new Space(context);
            space.setLayoutParams(new LinearLayout.LayoutParams(0, distance_s));

            editText.addTextChangedListener(new TextWatcher() {
                @Override
                public void beforeTextChanged(CharSequence s, int start, int count, int after) {
                }

                @Override
                public void onTextChanged(CharSequence s, int start, int before, int count) {
                }

                @Override
                public void afterTextChanged(Editable s) {
                    if (!s.toString().equals("") && !s.toString().equals("-")) {
                        Object checkedValue = Validation.checkValue(s.toString(), parameter.getType(), parameter.getMin(), parameter.getMax());
                        if (checkedValue != null) {
                            if (Objects.equals(parameter.getTypeName(), "name"))
                                dialog.title.setText(checkedValue.toString());
                            parameter.setValue(checkedValue);
                            dialog.parameterFieldCallback.onParameterFieldChanged(parameter);
                        }
                    }
                }
            });

            dialog.root.addView(view);
            dialog.root.addView(space);
            return this;
        }

        public Builder fromParameters(List<ParameterField> parameters) {
            for (ParameterField parameter : parameters) {
                if (parameter.getFieldType().equals("text")) {
                    addTextField(parameter);
                }
            }
            return this;
        }

        public Builder addImageButton(int picture, String text) {


            MaterialTextView textView = (MaterialTextView) LayoutInflater.from(context).inflate(R.layout.simple_input_bottom_dialog_image_button, null);
            textView.setCompoundDrawablesRelativeWithIntrinsicBounds(context.getDrawable(picture), null, null, null);
            textView.setText(text);

            textView.setOnClickListener(v -> dialog.imageButtonCallback.onImageButtonClicked(text));

            Space space = new Space(context);
            space.setLayoutParams(new LinearLayout.LayoutParams(0, distance_s));

            dialog.root.addView(textView);
            dialog.root.addView(space);

            return this;
        }

        public SimpleInputBottomDialog build() {
            dialog.root.removeViewAt(dialog.root.getChildCount() - 1); //remove last space
            return dialog;
        }
    }

    @Override
    public void onDismiss(DialogInterface dialog) {
        super.onDismiss(dialog);
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

    public void setParameterFieldCallback(ParameterFieldCallback callback) {
        this.parameterFieldCallback = callback;
    }

    public void setInputFieldCallback(InputFieldCallback callback) {
        this.inputFieldCallback = callback;
    }

    public void setImageButtonCallback(ImageButtonCallback callback) {
        this.imageButtonCallback = callback;
    }

    public void setColorPickerCallback(ColorPickerCallback callback) {
        this.colorPickerCallback = callback;
    }

    public interface ColorPickerCallback {
        void showColorPickerDialog(TextInputEditText editText);
    }

    public interface ParameterFieldCallback {
        void onParameterFieldChanged(ParameterField field);
    }

    public interface InputFieldCallback {
        void onInputFieldChanged(String typeName, Object value);
    }

    public interface ImageButtonCallback {
        void onImageButtonClicked(String text);
    }
}
