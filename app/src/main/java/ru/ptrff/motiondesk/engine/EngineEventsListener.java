package ru.ptrff.motiondesk.engine;

public interface EngineEventsListener {
    void onObjectSelected(String type, int index);
    void onObjectNotSelected();
}
