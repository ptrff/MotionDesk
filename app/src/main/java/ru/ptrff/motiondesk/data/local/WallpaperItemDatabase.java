package ru.ptrff.motiondesk.data.local;

import android.content.Context;

import androidx.room.Database;
import androidx.room.Room;
import androidx.room.RoomDatabase;

import ru.ptrff.motiondesk.models.WallpaperItem;

@Database(entities = {WallpaperItem.class}, version = 1)
public abstract class WallpaperItemDatabase extends RoomDatabase {

    private static WallpaperItemDatabase INSTANCE;

    public abstract WallpaperItemDao wallpaperItemDao();

    public static synchronized WallpaperItemDatabase getInstance(Context context) {
        if (INSTANCE == null) {
            INSTANCE = Room.databaseBuilder(
                            context.getApplicationContext(),
                            WallpaperItemDatabase.class, "wallpaper_items")
                    .build();
        }
        return INSTANCE;
    }
}
