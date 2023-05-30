package ru.ptrff.motiondesk.viewmodel;

import android.app.Application;
import android.util.Log;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.MutableLiveData;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;

import ru.ptrff.motiondesk.models.BrowseSector;
import ru.ptrff.motiondesk.models.WallpaperItem;
import ru.ptrff.motiondesk.utils.IDGenerator;

public class BrowseViewModel extends AndroidViewModel {

    private final MutableLiveData<List<BrowseSector>> sectorsLiveData;
    private final MutableLiveData<List<WallpaperItem>> wallpapersLiveData;
    private final List<BrowseSector> sectorsList;
    private final List<WallpaperItem> itemsList;
    private final Executor executor = Executors.newSingleThreadExecutor();


    public BrowseViewModel(@NonNull Application application) {
        super(application);
        sectorsLiveData = new MutableLiveData<>();
        sectorsList = new ArrayList<>();
        itemsList = new ArrayList<>();
        wallpapersLiveData = new MutableLiveData<>();
    }

    public void init(int pos) {
        executor.execute(() -> {
            while (pos + 5 > itemsList.size()) {
                itemsList.add(
                        new WallpaperItem(true, IDGenerator.generateID(),
                                "Название",
                                "i_petroff",
                                "",
                                1080,
                                1920,
                                "scene2d",
                                0,
                                false
                        )
                );
                wallpapersLiveData.postValue(itemsList);
            }

            while (pos + 5 > sectorsList.size()) {
                sectorsList.add(
                        new BrowseSector("Реки емае",
                                null,
                                itemsList,
                                sectorsList.size()
                        )
                );
                sectorsLiveData.postValue(sectorsList);
            }
        });
    }

    public MutableLiveData<List<BrowseSector>> getSectorsListLiveData() {
        return sectorsLiveData;
    }

    public MutableLiveData<List<WallpaperItem>> getWallpapersLiveData() {
        return wallpapersLiveData;
    }
}
