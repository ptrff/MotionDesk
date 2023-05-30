package ru.ptrff.motiondesk.viewmodel;

import android.app.Application;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.MutableLiveData;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;

import ru.ptrff.motiondesk.models.WallpaperItem;
import ru.ptrff.motiondesk.utils.IDGenerator;

public class ProfileViewModel extends AndroidViewModel {

    private final MutableLiveData<List<WallpaperItem>> itemsLiveData;
    private final List<WallpaperItem> itemsList;
    private final Executor executor = Executors.newSingleThreadExecutor();


    public ProfileViewModel(@NonNull Application application) {
        super(application);
        itemsLiveData = new MutableLiveData<>();
        itemsList = new ArrayList<>();
        init(6);
    }

    public void init(int pos) {
        executor.execute(() -> {
            while (pos >itemsList.size()) {
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
                itemsLiveData.postValue(itemsList);
            }
        });
    }

    public MutableLiveData<List<WallpaperItem>> getItemsLiveData(){
        return itemsLiveData;
    }

}
