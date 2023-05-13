package ru.ptrff.motiondesk.data.local;

import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;

import java.util.List;

import io.reactivex.Completable;
import io.reactivex.Flowable;
import io.reactivex.Observable;
import io.reactivex.Single;
import ru.ptrff.motiondesk.models.WallpaperItem;

@Dao
public interface WallpaperItemDao {

    @Query("SELECT * FROM wallpaper_items")
    Flowable<List<WallpaperItem>> getAllWallpaperItems();

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    Completable insertWallpaperItem(WallpaperItem wallpaperItem);

    @Query("DELETE FROM wallpaper_items WHERE id = :id")
    Completable removeWallpaperItemById(String id);

    @Query("SELECT * FROM wallpaper_items WHERE id = :id")
    Single<WallpaperItem> getWallpaperItemById(String id);
}
