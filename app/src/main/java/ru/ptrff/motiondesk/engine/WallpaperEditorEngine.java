package ru.ptrff.motiondesk.engine;

import android.graphics.Bitmap;
import android.opengl.GLES20;
import android.opengl.GLUtils;
import android.os.Build;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Screen;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.Pixmap;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.input.GestureDetector;
import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.Group;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.ScreenUtils;
import com.badlogic.gdx.utils.viewport.ScreenViewport;
import com.crashinvaders.vfx.effects.ChromaticAberrationEffect;
import com.crashinvaders.vfx.effects.FisheyeEffect;
import com.crashinvaders.vfx.effects.OldTvEffect;
import com.crashinvaders.vfx.effects.WaterDistortionEffect;
import com.crashinvaders.vfx.effects.ZoomEffect;
import com.crashinvaders.vfx.scene2d.VfxWidgetGroup;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

public class WallpaperEditorEngine extends Group implements Screen, GestureDetector.GestureListener {

    //Base
    private final EngineEventsListener engineEventsListener;
    private final int width;
    private final int height;
    private boolean playing=true;

    //Scene
    private OrthographicCamera camera;
    private Rectangle workingArea;
    private Texture backgroundTexture;
    private Batch batch;
    private Stage stage;

    //Shaders
    GlitchEffect glitchEffect;
    OldTvEffect oldTvEffect;
    List<ActorHandler> objects;

    //Gestures
    private int draggedSpriteId = 0;
    private boolean isTwoFinger;
    private boolean dragSprite = false;
    private boolean canDragObject = false;
    private float lastZoom = 0;
    private float lastScaleX = 0;
    private float lastScaleY = 0;
    private boolean isZooming = false;
    private final Vector2 initialTouch1 = new Vector2();
    private final Vector2 initialTouch2 = new Vector2();


    public WallpaperEditorEngine(int width, int height, EngineEventsListener listener) {
        engineEventsListener = listener;
        this.width = width;
        this.height = height;
    }

    public void init() {
        //Camera
        camera = new OrthographicCamera();
        camera.setToOrtho(false, Gdx.graphics.getWidth(), Gdx.graphics.getHeight());

        //Batch
        batch = new SpriteBatch();

        //Scene
        stage = new Stage(new ScreenViewport(camera), batch);

        objects = new ArrayList<>();
        objects.add(createImage("kitik.jpg"));
        stage.addActor(objects.get(0));
        glitchEffect = new GlitchEffect();
        oldTvEffect = new OldTvEffect();
        objects.get(0).getVfxManager().addEffect(glitchEffect);

        createWorkingAreaBackground(width, height);
        Gdx.input.setInputProcessor(new GestureDetector(this));
    }

    public ActorHandler createImage(String imagePath) {
        TextureRegion texture = new TextureRegion(new Texture(imagePath));
        ImageActor image = new ImageActor(texture, "Имечко"+objects.size());
        return new ActorHandler(image);
    }

    private void createWorkingAreaBackground(int width, int height) {
        workingArea = new Rectangle();
        workingArea.setSize(width, height);
        backgroundTexture = createCheckerboardTexture(width, height);
    }

    public Texture createCheckerboardTexture(int width, int height) {
        Pixmap pixmap = new Pixmap(width, height, Pixmap.Format.RGBA8888);
        pixmap.setColor(Color.rgba8888(100, 100, 100, 128));
        pixmap.fill();
        pixmap.setColor(Color.rgba8888(200, 200, 200, 128));
        int tileSize = 50;
        for (int y = 0; y < height; y += tileSize) {
            for (int x = 0; x < width; x += tileSize) {
                if (((x / tileSize + y / tileSize) % 2) == 0) {
                    pixmap.fillRectangle(x, y, tileSize, tileSize);
                }
            }
        }
        pixmap.setColor(Color.YELLOW);
        pixmap.drawRectangle(0, 0, width - 2, height - 2);
        pixmap.drawRectangle(1, 1, width - 2, height - 2);
        Texture texture = new Texture(pixmap);
        pixmap.dispose();
        return texture;
    }

    public void addImage(Bitmap bitmap) {
        Gdx.app.postRunnable(() -> {
            Texture texture = new Texture(bitmap.getWidth(), bitmap.getHeight(), Pixmap.Format.RGBA8888);
            GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, texture.getTextureObjectHandle());
            GLUtils.texImage2D(GLES20.GL_TEXTURE_2D, 0, bitmap, 0);
            GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, 0);
            bitmap.recycle();
            ImageActor image = new ImageActor(new TextureRegion(texture), "Имечко"+objects.size());
            objects.add(new ActorHandler(image));
            objects.get(objects.size()-1).getVfxManager().addEffect(new ChromaticAberrationEffect(5));
            stage.addActor(objects.get(objects.size()-1));
            engineEventsListener.onObjectAdded(objects.size()-1);
        });
    }

    public void removeObject(){
        //objects.get(draggedSpriteId).dispose();
        objects.get(draggedSpriteId).getImageActor().remove();
        objects.get(draggedSpriteId).remove();
        objects.remove(objects.get(draggedSpriteId));
        engineEventsListener.onObjectRemoved(draggedSpriteId);
        engineEventsListener.onObjectNotSelected();
        canDragObject=false;
        dragSprite=false;
    }

    public ActorHandler getObject(int index){
        return objects.get(index);
    }

    public List<ActorHandler> getObjectList(){
        return objects;
    }

    public Array<Actor> getStageActorArray(){
        return stage.getActors();
    }

    public void chooseObject(ActorHandler object){
        int selectedObjectIndex = objects.indexOf(object);
        ActorHandler sprite = objects.get(selectedObjectIndex);
        sprite.addStroke();
        draggedSpriteId=selectedObjectIndex;
        engineEventsListener.onObjectSelected("Image", draggedSpriteId);
        canDragObject =true;
    }

    @Override
    public void dispose() {
        batch.dispose();
        stage.dispose();
    }

    @Override
    public void resize(int width, int height) {
        if (stage != null) stage.getViewport().update(width, height, true);
    }

    @Override
    public void hide() {

    }

    @Override
    public void pause() {
        playing=false;
    }

    @Override
    public void resume() {
        playing=true;
    }

    public void playPause(){
        if(playing) pause();
        else resume();
    }

    @Override
    public void render(float delta) {
        ScreenUtils.clear(0.1f, 0.1f, 0.1f, 1);

        camera.update();
        batch.setProjectionMatrix(camera.combined);
        batch.begin();
        if (backgroundTexture != null) {
            batch.draw(backgroundTexture, Gdx.graphics.getWidth() / 2 - workingArea.getWidth() / 2, Gdx.graphics.getHeight() / 2 - workingArea.getHeight() / 2);
        }
        batch.end();

        for(ActorHandler object:objects){
            object.setWidth(Gdx.graphics.getWidth() * camera.zoom);
            object.setHeight(Gdx.graphics.getHeight() * camera.zoom);
            object.setPosition(camera.position.x - Gdx.graphics.getWidth() * camera.zoom / 2, camera.position.y - Gdx.graphics.getHeight() * camera.zoom / 2);
        }

        if(playing) stage.act(delta);
        stage.draw();
    }

    @Override
    public void show() {

    }

    private int checkForObjects(float x, float y) {
        for (int i = 0; i<objects.size();i++) {
            ActorHandler sprite = objects.get(objects.size()-1-i);
            Vector2 screenCoordinates = new Vector2(x, y);
            Vector3 worldCoordinates = camera.unproject(new Vector3(screenCoordinates.x, screenCoordinates.y, 0));
            if (sprite.getActorX() < worldCoordinates.x && worldCoordinates.x < sprite.getActorX() + sprite.getActorWidth()
                    && sprite.getActorY() < worldCoordinates.y && worldCoordinates.y < sprite.getActorY() + sprite.getActorHeight()
                    && sprite.getVisibility() && !sprite.getLockStatus()) {
                sprite.addStroke();
                canDragObject = true;
                return objects.indexOf(sprite);
            }
        }
        return -1;
    }

    private final Vector2 sum2 = new Vector2();

    @Override
    public boolean touchDown(float x, float y, int pointer, int button) {
        if (pointer == 0) {
            initialTouch1.set(x, y);
        }
        if (pointer == 1)
            initialTouch2.set(x, y);

        if (canDragObject && checkForObjects(x, y) == draggedSpriteId) {
            dragSprite = true;
            engineEventsListener.onObjectSelected("Image", draggedSpriteId);
        } else if (checkForObjects(x, y) != -1) {
            if(objects.size()!=0 && objects.size()>draggedSpriteId)
                objects.get(draggedSpriteId).removeStroke();
            draggedSpriteId = checkForObjects(x, y);
            engineEventsListener.onObjectSelected("Image", draggedSpriteId);
            objects.get(draggedSpriteId).setZoomAmount(camera.zoom);
        } else {
            dragSprite = false;
            canDragObject = false;
            if(objects.size()!=0 && objects.size()>draggedSpriteId)
                objects.get(draggedSpriteId).removeStroke();
            engineEventsListener.onObjectNotSelected();
        }

        return true;
    }

    @Override
    public boolean pan(float x, float y, float deltaX, float deltaY) {
        if (Math.abs(deltaX) < 0.1f && Math.abs(deltaY) < 0.1f) return false;

        if (canDragObject && !dragSprite) {
            engineEventsListener.onObjectNotSelected();
            objects.get(draggedSpriteId).removeStroke();
            canDragObject = false;
        }
        if (dragSprite) {
            float newX = objects.get(draggedSpriteId).getActorX() + (deltaX) * camera.zoom;
            float newY = objects.get(draggedSpriteId).getActorY() - (deltaY) * camera.zoom;

            objects.get(draggedSpriteId).setActorPosition(newX, newY);


        } else {
            camera.translate(-deltaX * camera.zoom, deltaY * camera.zoom);
        }
        return true;
    }

    @Override
    public boolean panStop(float x, float y, int pointer, int button) {

        if (isTwoFinger) isTwoFinger = false;
        if (isZooming) {
            isZooming = false;
            lastZoom = 0;
            lastScaleY = 0;
            lastScaleX = 0;
            sum2.set(0, 0);
        }

        return true;
    }

    @Override
    public boolean zoom(float initialDistance, float distance) {
        float zoomAmount = initialDistance - distance;
        isZooming = (zoomAmount != 0);
        float normalizedZoomAmount = zoomAmount / (float) Math.sqrt(Math.pow(Gdx.graphics.getWidth(), 2) + Math.pow(Gdx.graphics.getHeight(), 2));

        if (dragSprite) {
            objects.get(draggedSpriteId).setZoomAmount(camera.zoom);
            objects.get(draggedSpriteId).setActorSize(
                    objects.get(draggedSpriteId).getActorWidth() - normalizedZoomAmount * objects.get(draggedSpriteId).getActorWidth() + lastScaleX,
                    objects.get(draggedSpriteId).getActorHeight() - normalizedZoomAmount * objects.get(draggedSpriteId).getActorHeight() + lastScaleY
            );

//            objects.get(draggedSpriteId).getImageActor().setPosition(
//                    objects.get(draggedSpriteId).getX() + normalizedZoomAmount/2 * objects.get(draggedSpriteId).getWidth() - lastScaleX/2,
//                    objects.get(draggedSpriteId).getY() + normalizedZoomAmount/2 * objects.get(draggedSpriteId).getHeight() - lastScaleY/2
//            );

            lastScaleX = normalizedZoomAmount * objects.get(draggedSpriteId).getActorWidth();
            lastScaleY = normalizedZoomAmount * objects.get(draggedSpriteId).getActorHeight();

        } else {
            camera.zoom += normalizedZoomAmount * camera.zoom - lastZoom;
            lastZoom = normalizedZoomAmount * camera.zoom;
            camera.zoom = Math.max(camera.zoom, 0.1f);
            camera.zoom = Math.min(camera.zoom, 10.0f);
        }

        return true;
    }

    @Override
    public boolean tap(float x, float y, int count, int button) {
        return false;
    }

    @Override
    public boolean longPress(float x, float y) {
        return false;
    }

    @Override
    public boolean fling(float velocityX, float velocityY, int button) {
        return false;
    }

    @Override
    public boolean pinch(Vector2 initialPointer1, Vector2 initialPointer2, Vector2 pointer1, Vector2 pointer2) {
        return false;
    }

    @Override
    public void pinchStop() {

    }
}
