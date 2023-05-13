package ru.ptrff.motiondesk.engine.scene;


import android.graphics.Color;

import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.Pixmap;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.graphics.g2d.Sprite;
import com.badlogic.gdx.scenes.scene2d.Actor;

public class MaskActor extends Actor {
    private final Pixmap pixmap;
    private final Texture texture;

    private float penSize;
    private float penTransparency;
    private float maskTransparency;
    private float penStiffness;

    public MaskActor(int width, int height, int x, int y) {
        pixmap = new Pixmap(width, height, Pixmap.Format.RGBA8888);
        pixmap.setColor(1, 1, 1, 1);
        pixmap.fill();

        setPosition(x, y);

        texture = new Texture(pixmap);

        penSize = 1f;
        penTransparency = 0.5f;
        maskTransparency = 0.8f;
        penStiffness = 0.5f;
    }

    public void pan(float x, float y, float deltaX, float deltaY) {
        drawPixmap(x, y, 10, 10);
    }

    public void drawPixmap(float x, float y, int size, float sharpness) {
        // Set the color to draw with
        pixmap.setColor(Color.BLACK);
        // Set the brush size
        pixmap.fillCircle((int)x, (int)y, size);
    }

    @Override
    public void draw(Batch batch, float parentAlpha) {
        //batch.setBlendFunction(GL20.GL_ONE, GL20.GL_ONE_MINUS_SRC_ALPHA);
        //batch.setColor(getColor().r, getColor().g, getColor().b, getColor().a * maskTransparency);
        batch.draw(new Texture(pixmap), getX(), getY(), getWidth(), getHeight());
        //batch.setColor(1, 1, 1, 1);
        //batch.setBlendFunction(GL20.GL_SRC_ALPHA, GL20.GL_ONE_MINUS_SRC_ALPHA);
    }

    public void setPenSize(float size) {
        penSize = size;
    }

    public void setPenTransparency(float transparency) {
        penTransparency = transparency;
    }

    public void setMaskTransparency(float transparency){
        maskTransparency = transparency;
    }

    public void setPenStiffness(float stiffness) {
        penStiffness = stiffness;
    }
}