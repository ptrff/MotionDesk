package ru.ptrff.motiondesk.engine.effects;

public class LowPassFilter {
    private final float alpha;
    private final float[] lastValues;

    public LowPassFilter(int size, float alpha) {
        this.alpha = alpha;
        this.lastValues = new float[size];
    }

    public float[] filter(float[] values) {
        for (int i = 0; i < values.length; i++) {
            lastValues[i] = lastValues[i] + alpha * (values[i] - lastValues[i]);
        }
        return lastValues;
    }
}