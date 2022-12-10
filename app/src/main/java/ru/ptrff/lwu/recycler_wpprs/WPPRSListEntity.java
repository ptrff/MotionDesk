package ru.ptrff.lwu.recycler_wpprs;

public class WPPRSListEntity {
    private final String header;
    private final String description;
    private int height;

    public WPPRSListEntity(String header, String description, int height) {
        this.header = header;
        this.description = description;
        this.height=height;
    }

    public String getHeader() {
        return header;
    }

    public String getDescription() {
        return description;
    }

    public int getHeight(){return height;}
}
