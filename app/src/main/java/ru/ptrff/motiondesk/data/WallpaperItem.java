package ru.ptrff.motiondesk.data;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class WallpaperItem implements Serializable {
    private String id;
    private String name;
    private String author;
    private String description;
    private final List<String> tags = new ArrayList<>();
    private boolean local;
    private int width;
    private int height;
    private final String type;
    private String rating;
    private String image;
    private float stars;

    public WallpaperItem(boolean local, String id, String name, String author, String description, int width, int height, String type, String rating, String image) {
        this.local = local;
        this.id = id;
        this.name = name;
        this.author = author;
        this.description = description;
        this.width = width;
        this.height = height;
        this.type = type;
        this.image = image;
        this.rating = rating;
    }

    public WallpaperItem(boolean local, String id, String name, String author, String description, int width, int height, String type, float stars, String rating, String image) {
        this.local = local;
        this.id = id;
        this.name = name;
        this.author = author;
        this.description = description;
        this.width = width;
        this.height = height;
        this.type = type;
        this.stars = stars;
        this.image = image;
        this.rating = rating;
    }

    public boolean isLocal() {
        return local;
    }

    public int getWidth() {
        return width;
    }

    public int getHeight() {
        return height;
    }

    public List<String> getTags() {
        return tags;
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

    public String getImage() {
        return image;
    }

    public String getRating() {
        return rating;
    }

    public float getStars() {
        return stars;
    }

    public String getId() {
        return id;
    }

    public void setImage(String image) {
        this.image = image;
    }

    public void addTag(String tag) {
        tags.add(tag);
    }

    public void setId(String id) {
        this.id = id;
    }

    public void setName(String name) {
        this.name = name;
    }

    public void setAuthor(String author) {
        this.author = author;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public void setLocal(boolean local) {
        this.local = local;
    }

    public void setWidth(int width) {
        this.width = width;
    }

    public void setHeight(int height) {
        this.height = height;
    }

    public void setRating(String rating) {
        this.rating = rating;
    }

    public void setStars(float stars) {
        this.stars = stars;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof WallpaperItem)) return false;
        WallpaperItem that = (WallpaperItem) o;
        return id == that.id;
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }
}
