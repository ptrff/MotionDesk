package ru.ptrff.motiondesk.models;

public class SceneParameters {
    String backgroundColor;

    public SceneParameters(String backgroundColor) {
        this.backgroundColor = backgroundColor;
    }

    public String getBackgroundColor() {
        return backgroundColor;
    }

    public void setBackgroundColor(String backgroundColor) {
        this.backgroundColor = backgroundColor;
    }

    @Override
    public String toString() {
        return "SceneParameters{" +
                "backgroundColor='" + backgroundColor + '\'' +
                '}';
    }
}
