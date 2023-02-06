package ru.ptrff.motiondesk;

import android.content.Context;

public class Converter {
    public static float density;

    public Converter(Context context){
        density = context.getResources().getDisplayMetrics().density;
    }

    public static int dpToPx(int dp) {
        return Math.round(dp * density);
    }
}