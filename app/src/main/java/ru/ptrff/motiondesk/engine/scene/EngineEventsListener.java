package ru.ptrff.motiondesk.engine.scene;

public interface EngineEventsListener {
    void onObjectSelected(String type, int index);
    void onObjectNotSelected();
    void onObjectAdded(int position);
    void onObjectRemoved(int position);
    void onStartDrawingMask(int index);
    void onStopDrawingMask();
    void onEffectAdded();
    void onSceneLoaded();
    void snackMessage(String message);
}
