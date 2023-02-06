package ru.ptrff.motiondesk.data;

public class WallpaperItem {
    private final int id;
    private final String name;
    private final String author;
    private final String description;
    private final String rating;
    private final String image;
    private final float stars;


    public WallpaperItem(int id, String name, String author, String description, float stars, String rating, String image) {
        this.id=id;
        this.name = name;
        this.author = author;
        this.description = description;
        this.stars=stars;
        this.image=image;
        this.rating=rating;
    }

    public String getName() {
        return name;
    }

    public String getAuthor() {
        return author;
    }

    public String getDescription() {
        return description;
    }

    public String getImage(){
        return image;
    }

    public String getRating(){
        return rating;
    }

    public float getStars(){
        return stars;
    }

    public int getId(){
        return id;
    }

}
