package ru.ptrff.motiondesk.engine.scene;

import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.opengl.GLES20;
import android.opengl.GLUtils;
import android.util.Log;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.Pixmap;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.TextureData;
import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.graphics.glutils.FrameBuffer;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.scenes.scene2d.Actor;

import java.util.Timer;
import java.util.TimerTask;

public class ImageActor extends Actor {

    private String name;
    private final Texture region;
    private final Texture stroke;
    private boolean hasStroke;
    private float zoomAmount = 1;

    public ImageActor(Texture region, String name) {
        this.region = region;
        this.name = name;
        hasStroke=false;

        setSize(region.getWidth(), region.getHeight());
        setBounds(0, 0,
                region.getWidth(), region.getHeight());

        stroke = generateStroke();
        stroke.setFilter(Texture.TextureFilter.Linear, Texture.TextureFilter.Linear);
    }

    public String getName(){
        return name;
    }

    @Override
    public void setSize(float width, float height){
        setWidth(width);
        setHeight(height);
        setBounds(getX(), getY(), width, height);
    }

    private Texture generateStroke(){
        Pixmap pixmap = new Pixmap((int) getWidth(), (int) getHeight(), region.getTextureData().getFormat());
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
                    getX() - zoomAmount*6, getY() - zoomAmount*6,
                    getWidth() + zoomAmount*6*2, getHeight() + zoomAmount*6*2
            );
        }

        batch.draw(region, getX(), getY(), getWidth(), getHeight());

    }

    public Texture getTexture(){
        return region;
    }

    public void setName(String name){
        this.name = name;
    }

    public void setZoomAmount(float zoom){
        zoomAmount=zoom;
    }

    public void addStroke() {
        hasStroke = true;
    }

    public void removeStroke() {
        hasStroke = false;
    }

    public float getZoomAmount() {
        return zoomAmount;
    }
}