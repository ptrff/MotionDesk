package ru.ptrff.motiondesk.viewmodel;

import android.app.Application;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.MutableLiveData;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;

import ru.ptrff.motiondesk.adapters.WpprsAdapter;
import ru.ptrff.motiondesk.data.BrowseSector;
import ru.ptrff.motiondesk.data.WallpaperItem;

public class BrowseViewModel extends AndroidViewModel {

    private final MutableLiveData<List<BrowseSector>> sectorsLiveData;
    private final MutableLiveData<List<Integer>> scrollPositions;
    private List<BrowseSector> sectorsList;
    List<WallpaperItem> itemsList;
    private final List<Integer> positionsList;
    private final Executor executor = Executors.newSingleThreadExecutor();


    public BrowseViewModel(@NonNull Application application) {
        super(application);
        sectorsLiveData = new MutableLiveData<>();
        scrollPositions = new MutableLiveData<>();
        itemsList = new ArrayList<>();
        positionsList = new ArrayList();
        addFirst();
    }

    public MutableLiveData<List<BrowseSector>> getSectorsLiveData(){
        return sectorsLiveData;
    }

    public void init(int pos) {
        executor.execute(() -> {
            while (pos+5>itemsList.size()) {
                itemsList.add(
                        new WallpaperItem(itemsList.size()+1, "Название обоев",
                                "i_petroff",
                                "description",
                                new Random().nextInt(100)/10f,
                                "Для всех",
                                "link"
                        )
                );
            }

            while (pos+5>sectorsList.size()) {
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

    private void addFirst() {
        itemsList = new ArrayList<>();
        sectorsList = new ArrayList<>();
        itemsList.add(
                new WallpaperItem(itemsList.size()+1, "Название обоев",
                        "i_petroff",
                        "description",
                        150 + new Random().nextInt(500),
                        "Для всех",
                        "link"
                )
        );
        sectorsList.add(
                new BrowseSector("Реки емае",
                        null,
                        itemsList,
                        sectorsList.size()
                )
        );
        sectorsLiveData.setValue(sectorsList);
    }

    public MutableLiveData<List<Integer>> getScrollPosition() {
        return scrollPositions;
    }

    public void setScrollPosition(int sectorId, int position) {
        positionsList.set(sectorId, position);
        scrollPositions.setValue(positionsList);
    }
}
