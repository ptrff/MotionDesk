package ru.ptrff.motiondesk.models;

import android.graphics.Bitmap;

import androidx.annotation.NonNull;
import androidx.room.Entity;
import androidx.room.Ignore;
import androidx.room.PrimaryKey;
import androidx.room.TypeConverters;

import com.google.gson.annotations.Expose;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import ru.ptrff.motiondesk.utils.ListStringConverter;

@Entity(tableName = "wallpaper_items")
public class WallpaperItem implements Serializable {
    @PrimaryKey
    @NonNull
    private String id;
    private String name;
    private String author;
    private String description;
    @TypeConverters(ListStringConverter.class)
    private List<String> tags = new ArrayList<>();
    private boolean local;
    private int width;
    private int height;
    private final String type;
    private int rating;
    private boolean hasPreviewImage;
    @Ignore
    private Bitmap image = null;
    private float stars;
    public WallpaperItem(boolean local, String id, String name, String author, String description, int width, int height, String type, int rating, boolean hasPreviewImage) {
        this.local = local;
        this.id = id;
        this.name = name;
        this.author = author;
        this.description = description;
        this.width = width;
        this.height = height;
        this.type = type;
        this.rating = rating;
        this.hasPreviewImage = hasPreviewImage;
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

    public boolean hasPreviewImage() {
        return hasPreviewImage;
    }

    public int getRating() {
        return rating;
    }

    public String getType() {
        return type;
    }

    public float getStars() {
        return stars;
    }

    public String getId() {
        return id;
    }

    public Bitmap getImage(){
        return image;
    }

    public void setHasPreviewImage(boolean hasPreviewImage) {
        this.hasPreviewImage = hasPreviewImage;
    }

    public void setImage(Bitmap image) {
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

    public void setRating(int rating) {
        this.rating = rating;
    }

    public void setStars(float stars) {
        this.stars = stars;
    }

    public void setTags(List<String> tags) {
        this.tags = tags;
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

    @Override
    public String toString() {
        return "WallpaperItem:\n" +
                "id=" + id + '\n' +
                "name=" + name + '\n' +
                "author=" + author + '\n' +
                "description=" + description + '\n' +
                "tags=" + tags +'\n' +
                "local=" + local +'\n' +
                "width=" + width +'\n' +
                "height=" + height +'\n' +
                "type=" + type + '\n' +
                "rating=" + rating +'\n' +
                "hasPreviewImage=" + hasPreviewImage +'\n' +
                "image=" + image +'\n' +
                "stars=" + stars +'\n';
    }
}
