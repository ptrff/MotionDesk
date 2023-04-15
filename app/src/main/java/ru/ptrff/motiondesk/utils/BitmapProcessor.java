package ru.ptrff.motiondesk.utils;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Rect;

import java.io.ByteArrayOutputStream;

public class BitmapProcessor {
    private static byte[] getBitmapData(Bitmap bitmap) {
        ByteArrayOutputStream stream = new ByteArrayOutputStream();
        bitmap.compress(Bitmap.CompressFormat.PNG, 100, stream);
        return stream.toByteArray();
    }

    public static Bitmap scaleBitmap(Bitmap bitmap,  int newWidth, int newHeight){
        byte[] data = getBitmapData(bitmap);
        Bitmap b = BitmapFactory.decodeByteArray(data, 0, data.length);
        return Bitmap.createScaledBitmap(b, newWidth, newHeight, true);
    }
}
