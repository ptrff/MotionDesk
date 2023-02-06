package ru.ptrff.motiondesk;

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
import com.crashinvaders.vfx.VfxManager;
import com.crashinvaders.vfx.effects.OldTvEffect;

public class LWUPlayer extends Group implements Screen, EditorEvents, GestureDetector.GestureListener {

    private final OrthographicCamera camera;
    private final ImageActor[] sprites = new ImageActor[10];
    private Rectangle workingArea;
    private Texture backgroundTexture;
    private final VfxManager vfxManager;
    private boolean isTwoFinger;
    private final Stage stage;
    private int dragI = 0;
    private boolean dragSprite = false;
    private final OnObjectSelectedListener objectSelectedListener;

    private final Vector2 initialTouch1 = new Vector2();
    private final Vector2 initialTouch2 = new Vector2();

    private final Batch batch;
    GlitchEffect glitchEffect;
    OldTvEffect oldTvEffect;

    public LWUPlayer(int width, int height, OnObjectSelectedListener listener) {

        objectSelectedListener = listener;

        //Camera
        camera = new OrthographicCamera();
        camera.setToOrtho(false, Gdx.graphics.getWidth(), Gdx.graphics.getHeight());

        //Batch
        batch = new SpriteBatch();

        //Scene
        stage = new Stage(new ScreenViewport(camera), batch);


        createImage("a.png");

        vfxManager = new VfxManager(Pixmap.Format.RGBA8888);

        glitchEffect = new GlitchEffect();
        oldTvEffect = new OldTvEffect();

        vfxManager.addEffect(glitchEffect);
        vfxManager.addEffect(oldTvEffect);


        setResolution(width, height);
        Gdx.input.setInputProcessor(new GestureDetector(this));
    }

    public void setResolution(int width, int height) {
        workingArea = new Rectangle();
        workingArea.setSize(width, height);
        Pixmap backgroundPixmap = new Pixmap(width + 2, height + 2, Pixmap.Format.RGBA4444);
        backgroundPixmap.setColor(Color.WHITE);
        backgroundPixmap.fillRectangle(0, 0, width + 2, height + 2);
        backgroundPixmap.setColor(Color.BLACK);
        backgroundPixmap.fillRectangle(1, 1, width, height);
        backgroundTexture = new Texture(backgroundPixmap);
        backgroundPixmap.dispose();
    }

    public void createImage(String imagePath) {
        for (int i = 0; i < sprites.length; i++) {
            if (sprites[i] == null) {
                TextureRegion texture = new TextureRegion(new Texture(imagePath));
                ImageActor image = new ImageActor(texture);
                sprites[i] = image;
                stage.addActor(sprites[i]);
                break;
            }
        }
    }

    @Override
    public void dispose() {

        batch.dispose();
        stage.dispose();

        vfxManager.dispose();
        glitchEffect.dispose();
//        oldTvEffect.dispose();
    }

    @Override
    public void resize(int width, int height) {
//        vfxManager.resize(width, height);
        stage.getViewport().update(width, height, true);
    }

    @Override
    public void hide() {

    }

    @Override
    public void pause() {

    }

    @Override
    public void render(float delta) {
//        vfxManager.update(Gdx.graphics.getDeltaTime());
//        vfxManager.cleanUpBuffers();
//        vfxManager.beginInputCapture();
        ScreenUtils.clear(0.1f, 0.1f, 0.1f, 1);

        camera.update();
        batch.setProjectionMatrix(camera.combined);
        batch.begin();

        if (backgroundTexture != null)
            batch.draw(backgroundTexture, Gdx.graphics.getWidth() / 2 - workingArea.getWidth() / 2, Gdx.graphics.getHeight() / 2 - workingArea.getHeight() / 2);

        batch.end();

        stage.act(delta);
        stage.draw();

//        vfxManager.endInputCapture();
//        vfxManager.applyEffects();
//        vfxManager.renderToScreen();
    }


    @Override
    public void resume() {


    }

    @Override
    public void show() {

    }

    @Override
    public boolean touchDown(float x, float y, int pointer, int button) {
        if (pointer == 0) {
            initialTouch1.set(x, y);
        }
        if (pointer == 1)
            initialTouch2.set(x, y);

        for (int i = 0; i != sprites.length; i++) {
            if (sprites[i] != null) {
                Vector2 screenCoordinates = new Vector2(x, y);
                Vector3 worldCoordinates = camera.unproject(new Vector3(screenCoordinates.x, screenCoordinates.y, 0));
                if (sprites[i].getX() < worldCoordinates.x && worldCoordinates.x < sprites[i].getX() + sprites[i].getWidth()
                        && sprites[i].getY() < worldCoordinates.y && worldCoordinates.y < sprites[i].getY() + sprites[i].getHeight()) {
                    sprites[i].addStroke();
                    objectSelectedListener.onObjectSelected("Image");
                    dragI = i;
                    dragSprite = true;
                    break;
                }
            }
        }
        return true;
    }

    @Override
    public boolean pan(float x, float y, float deltaX, float deltaY) {
        if (dragSprite) {
            sprites[dragI].setPosition(
                    sprites[dragI].getX() + (deltaX) * camera.zoom,
                    sprites[dragI].getY() - (deltaY) * camera.zoom
            );
        } else {
            camera.translate(-deltaX * camera.zoom, deltaY * camera.zoom);
        }
        return true;
    }

    @Override
    public boolean panStop(float x, float y, int pointer, int button) {
        if (dragSprite) {
            dragSprite = false;
            sprites[dragI].removeStroke();
            objectSelectedListener.onObjectNotSelected();
        }
        if (isTwoFinger) isTwoFinger = false;
        if (zooming) {
            zooming = false;
            lastzoom = 0;
        }
        return true;
    }

    float lastzoom = 0;
    boolean zooming = false;

    @Override
    public boolean zoom(float initialDistance, float distance) {
        float zoomAmount = initialDistance - distance;
        zooming = (zoomAmount != 0);
        float normalizedZoomAmount = zoomAmount / (float) Math.sqrt(Math.pow(Gdx.graphics.getWidth(), 2) + Math.pow(Gdx.graphics.getHeight(), 2));
        //if (Math.abs(normalizedZoomAmount) < 0.1f) return false;


        camera.zoom += normalizedZoomAmount * camera.zoom - lastzoom;
        lastzoom = normalizedZoomAmount * camera.zoom;

        camera.zoom = Math.max(camera.zoom, 0.1f);
        camera.zoom = Math.min(camera.zoom, 10.0f);
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

    public interface OnObjectSelectedListener {
        void onObjectSelected(String type);
        void onObjectNotSelected();
    }
}
