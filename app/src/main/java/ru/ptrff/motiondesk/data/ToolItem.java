package ru.ptrff.motiondesk.data;

public class ToolItem {
    private final int imageResourse;
    private final String label;

    public ToolItem(int imageResourse, String label){
        this.imageResourse=imageResourse;
        this.label=label;
    }

    public int getImageResourse(){
        return imageResourse;
    }

    public String getLabel(){
        return label;
    }
}
