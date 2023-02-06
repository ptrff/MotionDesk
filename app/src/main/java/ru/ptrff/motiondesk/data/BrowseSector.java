package ru.ptrff.motiondesk.data;

import java.util.List;

import ru.ptrff.motiondesk.adapters.WpprsAdapter;

public class BrowseSector {
    private final String sectionName;
    private final WpprsAdapter adapter;
    private final List<WallpaperItem> items;
    private final int position;


    public BrowseSector(String sectionName, WpprsAdapter adapter, List<WallpaperItem> items, int position) {
        this.sectionName = sectionName;
        this.adapter = adapter;
        this.items = items;
        this.position = position;
    }

    public String getName() {
        return sectionName;
    }

    public List<WallpaperItem> getItemList(){
        return items;
    }

    public int getPosition() {
        return position;
    }

    public WpprsAdapter getAdapter() {
        return adapter;
    }

}
