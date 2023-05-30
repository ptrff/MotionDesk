package ru.ptrff.motiondesk.data.local;

import android.content.Context;

import java.util.List;

import io.reactivex.Completable;
import io.reactivex.Flowable;
import io.reactivex.Observable;
import io.reactivex.Single;
import ru.ptrff.motiondesk.models.WallpaperItem;

public class WallpaperItemRepository {

    private final WallpaperItemDao wallpaperItemDao;

    public WallpaperItemRepository(Context context) {
        WallpaperItemDatabase database = WallpaperItemDatabase.getInstance(context);
        wallpaperItemDao = database.wallpaperItemDao();
    }

    public Flowable<List<WallpaperItem>> getAllWallpaperItems() {
        return wallpaperItemDao.getAllWallpaperItems();
    }

    public Completable insertWallpaperItem(WallpaperItem wallpaperItem) {
        return wallpaperItemDao.insertWallpaperItem(wallpaperItem);
    }

    public Completable removeWallpaperItemById(String id) {
        return wallpaperItemDao.removeWallpaperItemById(id);
    }
}
