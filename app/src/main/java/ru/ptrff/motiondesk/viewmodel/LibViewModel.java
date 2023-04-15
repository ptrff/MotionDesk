package ru.ptrff.motiondesk.viewmodel;

import android.annotation.SuppressLint;
import android.app.Application;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.MutableLiveData;

import com.google.gson.Gson;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import io.reactivex.Observable;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.schedulers.Schedulers;
import ru.ptrff.motiondesk.data.WallpaperItem;
import ru.ptrff.motiondesk.utils.ProjectManager;

public class LibViewModel extends AndroidViewModel {

    private final MutableLiveData<List<WallpaperItem>> itemsLiveData;
    private final MutableLiveData<Integer> scrollPosition;
    private final List<WallpaperItem> itemList;

    public LibViewModel(@NonNull Application application) {
        super(application);
        itemsLiveData = new MutableLiveData<>();
        scrollPosition = new MutableLiveData<>();
        itemList = new ArrayList<>();
        scrollPosition.setValue(0);
    }

    public MutableLiveData<List<WallpaperItem>> getItemsLiveData() {
        return itemsLiveData;
    }

    @SuppressLint("CheckResult")
    public void init() {
        Observable
                .just(ProjectManager.getProjectFiles(getApplication().getApplicationContext()))
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(projectFiles -> {
                    Gson gson = new Gson();
                    for (File project : projectFiles) {
                        try {
                            itemList.add(gson.fromJson(ProjectManager.getWallpaperItemJsonString(project), WallpaperItem.class));
                        } catch (IOException ignored) {
                        }
                    }
                    itemsLiveData.postValue(itemList);
                });
    }

    public void refresh() {
        itemList.clear();
        itemsLiveData.postValue(itemList);
        init();
    }

    public MutableLiveData<Integer> getScrollPosition() {
        return scrollPosition;
    }

    public void setScrollPosition(int position) {
        scrollPosition.setValue(position);
    }
}
