package ru.ptrff.motiondesk.engine;

import android.graphics.Bitmap;
import android.opengl.GLES20;
import android.opengl.GLUtils;

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
import com.badlogic.gdx.scenes.scene2d.Group;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.utils.ScreenUtils;
import com.badlogic.gdx.utils.viewport.ScreenViewport;
import com.crashinvaders.vfx.effects.OldTvEffect;
import com.crashinvaders.vfx.scene2d.VfxWidgetGroup;

public class WallpaperEditorEngine extends Group implements Screen, GestureDetector.GestureListener {

    //Base
    private final EngineEventsListener engineEventsListener;
    private final int width;
    private final int height;
    private final boolean nightTheme;
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
    VfxWidgetGroup objects;

    //Gestures
    private int draggedSpriteId = 0;
    private boolean isTwoFinger;
    private boolean dragSprite = false;
    private boolean canDragSprite = false;
    private float lastZoom = 0;
    private float lastScaleX = 0;
    private float lastScaleY = 0;
    private boolean isZooming = false;
    private final Vector2 initialTouch1 = new Vector2();
    private final Vector2 initialTouch2 = new Vector2();


    public WallpaperEditorEngine(int width, int height, boolean nightTheme, EngineEventsListener listener) {
        engineEventsListener = listener;
        this.nightTheme = nightTheme;
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

        //vfxManager = new VfxManager(Pixmap.Format.RGBA8888);

        objects = new VfxWidgetGroup(Pixmap.Format.RGBA8888);
        objects.setFillParent(false);

        stage.addActor(objects);

//        widgetGroup = new WidgetGroup();
//        widgetGroup.setFillParent(true);
//        stage.addActor(widgetGroup);

        createImage("kitik.jpg");

        glitchEffect = new GlitchEffect();
        oldTvEffect = new OldTvEffect();
        //vfxManager.addEffect(glitchEffect);
        //vfxManager.addEffect(oldTvEffect);

        objects.getVfxManager().addEffect(glitchEffect);
        //vfxWidgetGroup.getVfxManager().addEffect(oldTvEffect);

        createWorkingAreaBackground(width, height);
        Gdx.input.setInputProcessor(new GestureDetector(this));
    }

    public void createImage(String imagePath) {
        TextureRegion texture = new TextureRegion(new Texture(imagePath));
        ImageActor image = new ImageActor(texture, "Имечко");
        objects.addActor(image);
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
            ImageActor image = new ImageActor(new TextureRegion(texture), "Имечко");
            objects.addActor(image);
        });

    }

    public ImageActor getObject(int index){
        return (ImageActor) objects.getChild(index);
    }

    public VfxWidgetGroup getObjectList(){
        return objects;
    }

    public void chooseObject(ImageActor object){
        int selectedObjectIndex = objects.getChildren().indexOf(object, false);
        ImageActor sprite = (ImageActor) objects.getChild(selectedObjectIndex);
        sprite.addStroke();
        draggedSpriteId=selectedObjectIndex;
        engineEventsListener.onObjectSelected("Image", draggedSpriteId);
        canDragSprite=true;
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
        if (nightTheme) ScreenUtils.clear(0.1f, 0.1f, 0.1f, 1);
        else ScreenUtils.clear(0.9f, 0.9f, 0.9f, 1);

        camera.update();
        batch.setProjectionMatrix(camera.combined);
        batch.begin();
        if (backgroundTexture != null) {
            batch.draw(backgroundTexture, Gdx.graphics.getWidth() / 2 - workingArea.getWidth() / 2, Gdx.graphics.getHeight() / 2 - workingArea.getHeight() / 2);
        }
        batch.end();

        objects.setWidth(Gdx.graphics.getWidth() * camera.zoom);
        objects.setHeight(Gdx.graphics.getHeight() * camera.zoom);
        objects.setPosition(camera.position.x - Gdx.graphics.getWidth() * camera.zoom / 2, camera.position.y - Gdx.graphics.getHeight() * camera.zoom / 2);

        if(playing) stage.act(delta);
        stage.draw();
    }

    @Override
    public void show() {

    }

    private int checkForSprites(float x, float y) {
        for (int i = 0; i<objects.getChildren().size;i++) {
            ImageActor sprite = (ImageActor) objects.getChild(objects.getChildren().size-1-i);
            if (sprite != null) {
                Vector2 screenCoordinates = new Vector2(x, y);
                Vector3 worldCoordinates = camera.unproject(new Vector3(screenCoordinates.x, screenCoordinates.y, 0));
                if (sprite.getX() < worldCoordinates.x && worldCoordinates.x < sprite.getX() + sprite.getWidth()
                        && sprite.getY() < worldCoordinates.y && worldCoordinates.y < sprite.getY() + sprite.getHeight()) {
                    sprite.addStroke();
                    canDragSprite = true;
                    return objects.getChildren().indexOf(sprite, false);
                }
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

        if (canDragSprite && checkForSprites(x, y) == draggedSpriteId) {
            dragSprite = true;
            engineEventsListener.onObjectSelected("Image", draggedSpriteId);
        } else if (checkForSprites(x, y) != -1) {
            ((ImageActor) objects.getChild(draggedSpriteId)).removeStroke();
            draggedSpriteId = checkForSprites(x, y);
            engineEventsListener.onObjectSelected("Image", draggedSpriteId);
            ((ImageActor) objects.getChild(draggedSpriteId)).setZoomAmount(camera.zoom);
        } else {
            dragSprite = false;
            canDragSprite = false;
            ((ImageActor) objects.getChild(draggedSpriteId)).removeStroke();
            engineEventsListener.onObjectNotSelected();
        }

        return true;
    }

    @Override
    public boolean pan(float x, float y, float deltaX, float deltaY) {
        if (Math.abs(deltaX) < 0.1f && Math.abs(deltaY) < 0.1f) return false;

        if (canDragSprite && !dragSprite) {
            engineEventsListener.onObjectNotSelected();
            ((ImageActor) objects.getChild(draggedSpriteId)).removeStroke();
            canDragSprite = false;
        }
        if (dragSprite) {
            float newX = objects.getChild(draggedSpriteId).getX() + (deltaX) * camera.zoom;
            float newY = objects.getChild(draggedSpriteId).getY() - (deltaY) * camera.zoom;

            if (newX > -10 && newX < 10)
                newX = 0;

            if (newX > width - 10 && newX < width + 10)
                newX = width;

            if (newY > -10 && newY < 10)
                newY = 0;

            if (newY > height - 10 && newY < height + 10)
                newY = height;

            objects.getChild(draggedSpriteId).setPosition(newX, newY);


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
        //if (Math.abs(normalizedZoomAmount) < 0.1f) return false;

        if (dragSprite) {
            ((ImageActor) objects.getChild(draggedSpriteId)).setZoomAmount(camera.zoom);
            objects.getChild(draggedSpriteId).setSize(
                    objects.getChild(draggedSpriteId).getWidth() - normalizedZoomAmount * objects.getChild(draggedSpriteId).getWidth() + lastScaleX,
                    objects.getChild(draggedSpriteId).getHeight() - normalizedZoomAmount * objects.getChild(draggedSpriteId).getHeight() + lastScaleY
            );
            objects.getChild(draggedSpriteId).setPosition(
                    objects.getChild(draggedSpriteId).getX() + normalizedZoomAmount/2 * objects.getChild(draggedSpriteId).getWidth() - lastScaleX/2,
                    objects.getChild(draggedSpriteId).getY() + normalizedZoomAmount/2 * objects.getChild(draggedSpriteId).getHeight() - lastScaleY/2
            );

            lastScaleX = normalizedZoomAmount * objects.getChild(draggedSpriteId).getWidth();
            lastScaleY = normalizedZoomAmount * objects.getChild(draggedSpriteId).getHeight();

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
