package ru.ptrff.motiondesk.utils;

import android.content.Context;

public class Converter {
    public static float density;

    public Converter(float density){
        Converter.density = density;
    }
    public static int dpToPx(int dp) {
        return Math.round(dp * density);
    }
}