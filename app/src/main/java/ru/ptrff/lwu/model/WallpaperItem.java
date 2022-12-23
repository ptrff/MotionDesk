package ru.ptrff.lwu.model;

import android.graphics.Bitmap;

public class WallpaperItem {
    private final String header;
    private final String description;
    private int height;
    private Bitmap image;
    private float rating;


    public WallpaperItem(String header, String description, int height, Bitmap image, float rating) {
        this.header = header;
        this.description = description;
        this.height=height;
        this.image=image;
        this.rating=rating;
    }

    public String getHeader() {
        return header;
    }

    public String getDescription() {
        return description;
    }

    public int getHeight(){return height;}

    public Bitmap getImage(){return image;}

    public float getRating(){return rating;}

}
