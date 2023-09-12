package ru.ptrff.motiondesk.viewmodel;

import android.annotation.SuppressLint;
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

import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.CompositeDisposable;
import io.reactivex.disposables.Disposable;
import io.reactivex.schedulers.Schedulers;
import ru.ptrff.motiondesk.data.local.WallpaperItemRepository;
import ru.ptrff.motiondesk.models.BrowseSector;
import ru.ptrff.motiondesk.models.WallpaperItem;
import ru.ptrff.motiondesk.utils.IDGenerator;

public class BrowseViewModel extends AndroidViewModel {

    private final MutableLiveData<List<WallpaperItem>> wallpaperItemsLiveData;
    private final CompositeDisposable compositeDisposable = new CompositeDisposable();
    private final WallpaperItemRepository repo;

    @SuppressLint("CheckResult")
    public BrowseViewModel(@NonNull Application application) {
        super(application);
        wallpaperItemsLiveData = new MutableLiveData<>();

        repo = new WallpaperItemRepository(application);

        repo
                .getAllWallpaperItems()
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(wallpaperItemsLiveData::postValue);
    }

    public MutableLiveData<List<WallpaperItem>> getWallpaperItemsLiveData() {
        return wallpaperItemsLiveData;
    }

    public void searchWallpaperItems(String query) {
        repo
                .getWallpaperItemsByName(query)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(wallpaperItemsLiveData::postValue);
    }

    public void refresh() {
        Disposable disposable = repo.getAllWallpaperItems()
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(wallpaperItemsLiveData::postValue);

        compositeDisposable.add(disposable);
    }

    @Override
    protected void onCleared() {
        compositeDisposable.clear();
        super.onCleared();
    }


    /*public void init(int pos) {
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
    }*/

   /* public MutableLiveData<List<BrowseSector>> getSectorsListLiveData() {
        return sectorsLiveData;
    }

    public MutableLiveData<List<WallpaperItem>> getWallpapersLiveData() {
        return wallpapersLiveData;
    }*/
}
