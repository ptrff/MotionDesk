package ru.ptrff.motiondesk.view;


import android.app.Dialog;

import android.content.DialogInterface;
import android.os.Bundle;
import android.transition.Explode;
import android.transition.Slide;
import android.transition.Transition;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.view.animation.AnticipateOvershootInterpolator;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.RatingBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.DialogFragment;

import ru.ptrff.motiondesk.R;

public class RatingDialog extends DialogFragment {
    private final int myRating;
    private TextView textView;
    private RatingBar rating;

    public RatingDialog(int myRating) {
        this.myRating = myRating;
    }


    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        AlertDialog.Builder builder = new AlertDialog.Builder(requireActivity());

        builder.setTitle("Оценить");
        builder.setMessage("Поставьте оценку обоям от 1 до 5.\nЭто может поможет пользователям избежать загрузки некачественных обоев.\n");

        rating = new RatingBar(requireContext());
        rating.setNumStars(5);
        rating.setStepSize(0.5f);
        rating.setLayoutParams(new ViewGroup.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT,
                ViewGroup.LayoutParams.WRAP_CONTENT));

        textView = new TextView(requireContext());
        textView.setGravity(Gravity.CENTER);
        textView.setTextSize(20);
        if(myRating!=0) {
            rating.setRating(myRating/2f);
            textView.setText(ratingText(myRating));
        }else{
            textView.setText("0");
            textView.setVisibility(View.GONE);
        }

        LinearLayout parent = new LinearLayout(requireContext());
        parent.setGravity(Gravity.CENTER);
        parent.setLayoutParams(new ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.MATCH_PARENT));
        parent.setOrientation(LinearLayout.VERTICAL);
        parent.addView(rating);
        parent.addView(textView);

        builder.setView(parent);

        rating.setOnRatingBarChangeListener((ratingBar, v, b) -> {
            if(v!=0) {
                if (textView.getVisibility() == View.GONE) textView.setVisibility(View.VISIBLE);
                textView.setText(ratingText( (int) (v*2) ));
            } else if (textView.getVisibility() == View.VISIBLE){
                rating.setRating(0);
                textView.setVisibility(View.GONE);
            }

        });

        builder.setNegativeButton("Отмена", (dialog, which) -> dialog.cancel());
        builder.setPositiveButton("Ок", (dialog, which) ->{});

        AlertDialog dialog = builder.create();
        dialog.getWindow().setBackgroundDrawableResource(R.drawable.rounded_bottom_dialog);
        return dialog;
    }

    private String ratingText(int rating){
        switch (rating){
            case 1:
                return "1/10 ХУЖЕ НЕКУДА";
            case 2:
                return "2/10 УЖАСНО";
            case 3:
                return "3/10 ОЧЕНЬ ПЛОХО";
            case 4:
                return "4/10 ПЛОХО";
            case 5:
                return "5/10 БОЛЕЕ-МЕНЕЕ";
            case 6:
                return "6/10 НОРМАЛЬНО";
            case 7:
                return "7/10 ХОРОШО";
            case 8:
                return "8/10 ОТЛИЧНО";
            case 9:
                return "9/10 ВЕЛИКОЛЕПНО";
            case 10:
                return "10/10 ЭПИК ВИН!";
            default:
                return "как это получилось..?";
        }
    }

    @Override public void onStart() {
        super.onStart();
        AlertDialog dialog = (AlertDialog) getDialog();
        textView.setTextColor(dialog.getButton(DialogInterface.BUTTON_POSITIVE).getTextColors());

        Button button = ((AlertDialog) dialog).getButton(AlertDialog.BUTTON_POSITIVE);
        button.setOnClickListener(view -> {
            if(textView.getVisibility()==View.GONE){
                Toast.makeText(getContext(), "Поставьте оценку", Toast.LENGTH_SHORT).show();
            }else{
                Toast.makeText(getContext(), "Ваша оценка: "+((int) (rating.getRating()*2)), Toast.LENGTH_SHORT).show();
                dialog.dismiss();
            }
        });

    }
}
