package ru.ptrff.motiondesk;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.Pixmap;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.scenes.scene2d.Actor;

public class ImageActor extends Actor {

    private final TextureRegion region;
    private final Texture stroke;
    private boolean hasStroke;

    public ImageActor(TextureRegion region) {
        this.region = region;
        hasStroke=false;
        setSize(region.getRegionWidth(), region.getRegionHeight());
        setBounds(region.getRegionX(), region.getRegionY(),
                region.getRegionWidth(), region.getRegionHeight());

        stroke = generateStroke();
        stroke.setFilter(Texture.TextureFilter.Linear, Texture.TextureFilter.Linear);
    }

    public ImageActor(int width, int height, Color color) {
        Pixmap pixmap = new Pixmap(width, height, Pixmap.Format.RGBA4444);
        pixmap.setColor(color);
        pixmap.fillRectangle(0, 0, width, height);
        Texture texture = new Texture(pixmap);
        texture.setFilter(Texture.TextureFilter.Linear, Texture.TextureFilter.Linear);
        region = new TextureRegion(texture);
        pixmap.dispose();

        setSize(width, height);
        setBounds(region.getRegionX(), region.getRegionY(),
                region.getRegionWidth(), region.getRegionHeight());

        stroke = generateStroke();
        stroke.setFilter(Texture.TextureFilter.Linear, Texture.TextureFilter.Linear);
    }

    private Texture generateStroke(){
        Pixmap pixmap = new Pixmap((int) getWidth(), (int) getHeight(), Pixmap.Format.RGBA4444);
        pixmap.setColor(Color.WHITE);
        pixmap.fillRectangle(0, 0, (int) getWidth(), (int) getHeight());
        return new Texture(pixmap);
    }

    @Override
    public void draw(Batch batch, float parentAlpha) {
        super.draw(batch, parentAlpha);
        if (hasStroke) {

            batch.draw(
                    stroke,
                    getX() - 6, getY() - 6,
                    getWidth() + 12, getHeight() + 12
            );
        }
        batch.draw(region, getX(), getY(), getWidth(), getHeight());
    }

    public void addStroke() {
        hasStroke = true;
    }

    public void removeStroke() {
        hasStroke = false;
    }
}
