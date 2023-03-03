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

import ru.ptrff.motiondesk.R;
import ru.ptrff.motiondesk.data.WallpaperItem;

public class LibViewModel extends AndroidViewModel {

    private final MutableLiveData<List<WallpaperItem>> itemsLiveData;
    private final MutableLiveData<Integer> scrollPosition;
    private List<WallpaperItem> itemsList;
    private final Executor executor = Executors.newSingleThreadExecutor();



    public LibViewModel(@NonNull Application application) {
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

    private final String[] urls = {
            "https://sun1-84.userapi.com/impg/9D00twvA-LNaoWs7uwp0IOot7tBNLS3YQ4SJlQ/4sw_XvQkChY.jpg?size=2215x2160&quality=96&sign=c94b72a95733dd3554a55164deff8dc0&type=album",
            "https://sun9-59.userapi.com/impg/NwvnNJv3HouuyB8G-qoiv34UXvFzqHOAKRBFcA/Mt5LoZITNRc.jpg?size=811x1136&quality=96&sign=951edc2eb4d09c41afb8fc9dfbf4209d&type=album",
            "https://sun9-76.userapi.com/impg/OlJ_hvSO4E3hIBR4J0KWipY2eDpZGKj1FrCGGw/MhabNj1HuF8.jpg?size=1442x2048&quality=96&sign=5f9d34febcf61055b2a4ad9ec200e56a&type=album",
            "https://sun9-59.userapi.com/impg/zamFauGN89eGFFZoc4gZPa9lo_kpkx8-ETFxLg/iBWBFxG8EZ0.jpg?size=850x505&quality=96&sign=d6f5bea8d90795449c65ea2bfc125cfe&type=album",
            "https://sun9-65.userapi.com/impg/ydeOA2yvVUy-wSflhKBng7gGYD_TyR5hGmi9RA/8Qb4jZbRMAQ.jpg?size=1142x919&quality=96&sign=b1702e565dff5b10004a44584740554c&type=album",
            "https://sun1-56.userapi.com/impg/s9zBqHaUsaa5a1xFz5CYdCZJvvA40S7jjhGIZA/2WjF_kEQZ28.jpg?size=947x832&quality=95&sign=d97aa05d94d53ba0e50321d91404ed14&type=album"
    };

    public void init(int pos) {
        executor.execute(() -> {
            while (pos+5>itemsList.size()) {
                int r = new Random().nextInt(1);
                itemsList.add(
                        new WallpaperItem(itemsList.size()+1, "A little name \\nA little name \\nA little name \\nA little name \\nA little name sdhfjkshdfj skjdhfksjdhf skjhfkjsdh fhskjd fhksj djs",
                                "i_petroff",
                                "description",
                                new Random().nextInt(100)/10f,
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
        int r = new Random().nextInt(1);
        itemsList.add(
                new WallpaperItem(itemsList.size()+1, "Название обоев",
                        "i_petroff",
                        "description",
                        new Random().nextInt(100)/10f,
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
