package ru.ptrff.motiondesk.engine;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.Pixmap;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.graphics.g2d.Sprite;
import com.badlogic.gdx.scenes.scene2d.Actor;

public class MaskActor extends Actor {
    private final Pixmap mask;
    private final Texture texture;

    private float penSize;
    private float penTransparency;
    private float maskTransparency;
    private float penStiffness;

    public MaskActor(int width, int height, int x, int y) {
        mask = new Pixmap(width, height, Pixmap.Format.Alpha);
        mask.setColor(1, 1, 1, 1);
        mask.fill();

        setPosition(x, y);

        texture = new Texture(mask);

        penSize = 1f;
        penTransparency = 0.5f;
        maskTransparency = 0.8f;
        penStiffness = 0.5f;
    }

    public void pan(float x, float y, float deltaX, float deltaY) {
        float distance = (float) Math.sqrt(deltaX * deltaX + deltaY * deltaY);
        float directionX = deltaX / distance;
        float directionY = deltaY / distance;

        for (float i = 0; i < distance; i += penStiffness) {
            float drawX = x + directionX * i;
            float drawY = y + directionY * i;
            for (float j = -penSize / 2; j < penSize / 2; j += penStiffness) {
                for (float k = -penSize / 2; k < penSize / 2; k += penStiffness) {
                    int pixelX = (int) (drawX + j);
                    int pixelY = (int) (drawY + k);
                    if (pixelX >= 0 && pixelX < mask.getWidth() && pixelY >= 0 && pixelY < mask.getHeight()) {
                        float alpha = (penSize / 2 - (float) Math.sqrt(j * j + k * k)) / (penSize / 2) * penTransparency;
                        alpha = Math.max(0, Math.min(1, alpha));
                        int color = (int) (alpha * 255) << 24;
                        mask.drawPixel(pixelX, pixelY, color);
                    }
                }
            }
        }

        texture.draw(mask, 0, 0);
    }

    @Override
    public void draw(Batch batch, float parentAlpha) {
        batch.setBlendFunction(GL20.GL_ONE, GL20.GL_ONE_MINUS_SRC_ALPHA);
        batch.setColor(getColor().r, getColor().g, getColor().b, getColor().a * maskTransparency);
        batch.draw(texture, getX(), getY(), getWidth(), getHeight());
        batch.setColor(1, 1, 1, 1);
        batch.setBlendFunction(GL20.GL_SRC_ALPHA, GL20.GL_ONE_MINUS_SRC_ALPHA);
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