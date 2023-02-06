package ru.ptrff.motiondesk.engine;

import android.media.Image;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.graphics.g2d.Sprite;
import com.badlogic.gdx.graphics.g2d.TextureRegion;

import ru.ptrff.motiondesk.ImageActor;

public class ImprovedActor extends ImageActor {
    private final TextureRegion texture;
    private boolean hasStroke;

    public ImprovedActor(TextureRegion texture) {
        super(texture);
        this.texture = texture;
        this.hasStroke = false;
    }

    @Override
    public void draw(Batch batch, float parentAlpha) {
        super.draw(batch, parentAlpha);
        if (hasStroke) {
            batch.setColor(Color.WHITE);
            batch.draw(
                    texture,
                    getX() - 3, getY() - 3,
                    getWidth() + 6, getHeight() + 6
            );
        }
    }

    public void addStroke() {
        hasStroke = true;
    }

    public void removeStroke() {
        hasStroke = false;
    }
}
