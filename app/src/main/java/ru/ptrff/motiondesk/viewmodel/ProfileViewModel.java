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

import ru.ptrff.motiondesk.data.WallpaperItem;

public class ProfileViewModel extends AndroidViewModel {

    private final MutableLiveData<List<WallpaperItem>> itemsLiveData;
    private final MutableLiveData<Integer> scrollPosition;
    private List<WallpaperItem> itemsList;
    private final Executor executor = Executors.newSingleThreadExecutor();


    public ProfileViewModel(@NonNull Application application) {
        super(application);
        itemsLiveData = new MutableLiveData<>();
        scrollPosition = new MutableLiveData<>();
        itemsList = new ArrayList<>();
        scrollPosition.setValue(0);
        addFirst();
    }

    public MutableLiveData<List<WallpaperItem>> getItemsLiveData(){
        return itemsLiveData;
    }

    public void init(int pos) {
        executor.execute(() -> {
            while (pos+8>itemsList.size()) {
                itemsList.add(
                        new WallpaperItem(itemsList.size()+1, "Название обоев",
                                "i_petroff",
                                "description",
                                150 + new Random().nextInt(500),
                                "Для всех",
                                "link"
                        )
                );
                itemsLiveData.postValue(itemsList);
            }
        });
    }

    private void addFirst() {
        itemsList = new ArrayList<>();
        itemsList.add(
                new WallpaperItem(itemsList.size()+1, "Название обоев",
                        "i_petroff",
                        "description",
                        150 + new Random().nextInt(500),
                        "Для всех",
                        "link"
                )
        );
        itemsLiveData.setValue(itemsList);
    }

    public MutableLiveData<Integer> getScrollPosition() {
        return scrollPosition;
    }

    public void setScrollPosition(int position) {
        scrollPosition.setValue(position);
    }
}
