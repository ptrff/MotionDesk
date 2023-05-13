package ru.ptrff.motiondesk.viewmodel;

import android.annotation.SuppressLint;
import android.app.Application;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.MutableLiveData;

import java.util.List;
import java.util.Observable;

import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.CompositeDisposable;
import io.reactivex.disposables.Disposable;
import io.reactivex.schedulers.Schedulers;
import ru.ptrff.motiondesk.data.local.WallpaperItemRepository;
import ru.ptrff.motiondesk.models.WallpaperItem;

public class LibViewModel extends AndroidViewModel {

    private final CompositeDisposable compositeDisposable = new CompositeDisposable();
    private final MutableLiveData<List<WallpaperItem>> wallpaperItemsLiveData;
    private final MutableLiveData<Integer> scrollPosition;
    private final WallpaperItemRepository repo;

    @SuppressLint("CheckResult")
    public LibViewModel(@NonNull Application application) {
        super(application);
        wallpaperItemsLiveData = new MutableLiveData<>();

        repo = new WallpaperItemRepository(application);

        repo
                .getAllWallpaperItems()
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(wallpaperItems -> {
                    wallpaperItemsLiveData.postValue(wallpaperItems);
                });

        scrollPosition = new MutableLiveData<>();
        scrollPosition.setValue(0);
    }

    public MutableLiveData<List<WallpaperItem>> getWallpaperItemsLiveData() {
        return wallpaperItemsLiveData;
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

    public MutableLiveData<Integer> getScrollPosition() {
        return scrollPosition;
    }

    public void setScrollPosition(int position) {
        scrollPosition.setValue(position);
    }
}