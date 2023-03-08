package ru.ptrff.motiondesk.engine;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.Pixmap;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.crashinvaders.vfx.VfxManager;
import com.crashinvaders.vfx.effects.ChainVfxEffect;
import com.crashinvaders.vfx.scene2d.VfxWidgetGroup;

import okhttp3.Interceptor;

public class ImageActor extends Actor {

    private String name;
    private final TextureRegion region;
    private final Texture stroke;
    private boolean hasStroke;
    private float zoomAmount = 6;

    public ImageActor(TextureRegion region, String name) {
        this.region = region;
        this.name = name;
        hasStroke=false;
        setSize(region.getRegionWidth(), region.getRegionHeight());
        setBounds(region.getRegionX(), region.getRegionY(),
                region.getRegionWidth(), region.getRegionHeight());

        stroke = generateStroke();
        stroke.setFilter(Texture.TextureFilter.Linear, Texture.TextureFilter.Linear);
    }

    public String getName(){
        return name;
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

    @Override
    public void setSize(float width, float height){
        setWidth(width);
        setHeight(height);
        setBounds(getX(), getY(), width, height);
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
                    getX() - zoomAmount, getY() - zoomAmount,
                    getWidth() + zoomAmount*2, getHeight() + zoomAmount*2
            );
        }
        batch.draw(region, getX(), getY(), getWidth(), getHeight());


    }

    public void setZoomAmount(float zoom){
        zoomAmount=6*zoom;
    }

    public void addStroke() {
        hasStroke = true;
    }

    public void removeStroke() {
        hasStroke = false;
    }
}
