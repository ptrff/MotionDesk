package ru.ptrff.motiondesk.engine;

import android.content.res.Resources;
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
import com.badlogic.gdx.input.GestureDetector;
import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.scenes.scene2d.Group;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.ScreenUtils;
import com.badlogic.gdx.utils.viewport.ScreenViewport;

import ru.ptrff.motiondesk.R;

public class WallpaperEditorEngine extends Group implements Screen, GestureDetector.GestureListener {

    //Base
    private final EngineEventsListener engineEventsListener;
    private final int height;
    private final int width;
    private boolean playing = true;
    private Resources resources;

    //Scene
    private OrthographicCamera camera;
    private Rectangle workingArea;
    private Texture backgroundTexture;
    private Batch batch;
    private StageWrapper stage;
    //List<ActorHandler> objects;

    //Gestures
    private int draggedSpriteId = 0;
    private boolean isTwoFinger;
    private boolean dragSprite = false;
    private boolean canDragObject = false;
    private float lastZoom = 0;
    private float lastScaleX = 0;
    private float lastScaleY = 0;
    private boolean isZooming = false;
    private boolean twoFingerMovement = false;
    private boolean drawingMask = false;
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
        stage = new StageWrapper(new ScreenViewport(camera), batch);
        //stage = new ArrayList<>();


        ActorHandler a = createImage("kitik.jpg");
        a.setActorPosition(100, 100);
        stage.addActor(a);
        ActorHandler b = createImage("kitik.jpg");
        b.setActorPosition(200, 200);
        stage.addActor(b);
        ActorHandler c = createImage("kitik.jpg");
        c.setActorPosition(300, 300);
        stage.addActor(c);

        createWorkingAreaBackground(width, height);
        centerCamera(-1);

        Gdx.input.setInputProcessor(new GestureDetector(this));
    }

    public ActorHandler createImage(String imagePath) {
        Texture texture = new Texture(imagePath);
        ImageActor image = new ImageActor(texture, "fdsf" + stage.getActors().size);
        ActorHandler actorHandler = new ActorHandler(image);
        //actorHandler.addEffect(new ShakeEffect("Effect name", new Texture("mask.png")));
        return actorHandler;
    }

    public boolean isObjectSelected(){
        return dragSprite || canDragObject;
    }

    public void startDrawingMask() {
        twoFingerMovement=true;
        drawingMask=true;
        centerCamera(draggedSpriteId);
        stage.get(draggedSpriteId).addMask();
        engineEventsListener.onStartDrawingMask(draggedSpriteId);
    }

    public void stopDrawingMask(){
        twoFingerMovement=false;
        drawingMask=false;
        engineEventsListener.onStopDrawingMask();
    }

    public void centerCamera(int id) {
        if (id == -1) {
            camera.position.x = Gdx.graphics.getWidth()/2;
            camera.position.y = Gdx.graphics.getHeight()/2;
            if(workingArea.getHeight()>workingArea.getWidth()) camera.zoom = (workingArea.getHeight()/Gdx.graphics.getHeight())*0.25f+1;
            else camera.zoom = (workingArea.getWidth()/Gdx.graphics.getWidth())*0.25f+1;
        } else {
            ActorHandler actor = getObject(id);

            camera.position.x = actor.getActorX() + actor.getActorWidth() / 2;
            camera.position.y = actor.getActorY() + actor.getActorHeight() / 2;
            if(actor.getActorHeight()>actor.getActorWidth()) camera.zoom = (actor.getActorHeight()/Gdx.graphics.getHeight())*0.25f+1;
            else camera.zoom = (actor.getActorWidth()/Gdx.graphics.getWidth())*0.25f+1;
        }
        if(dragSprite) stage.get(draggedSpriteId).setActorZoomAmount(camera.zoom);
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
            ImageActor image = new ImageActor(texture, resources.getString(R.string.new_layer) + " " + stage.getActors().size);
            ActorHandler actorHandler = new ActorHandler(image);

            stage.add(actorHandler);
            stage.addActor(actorHandler);
            engineEventsListener.onObjectAdded(stage.size() - 1);
        });
    }

    public void removeObject() {
        //stage.get(draggedSpriteId).dispose();
        stage.get(draggedSpriteId).getImageActor().remove();
        stage.get(draggedSpriteId).remove();
        //objects.remove(stage.get(draggedSpriteId));
        engineEventsListener.onObjectRemoved(draggedSpriteId);
        engineEventsListener.onObjectNotSelected();
        canDragObject = false;
        dragSprite = false;
    }

    public ActorHandler getObject(int index) {
        return stage.get(index);
    }

//    public List<ActorHandler> getObjectList(){
//        return stage;
//    }

    public Array<ActorHandler> getStageActorArray() {
        return stage.getObjects();
    }

    public void chooseObject(ActorHandler object) {
        if (canDragObject) {
            stage.get(draggedSpriteId).removeStroke();
        }
        draggedSpriteId = stage.indexOf(object);
        stage.get(draggedSpriteId).addStroke();
        engineEventsListener.onObjectSelected("Image", draggedSpriteId);
        canDragObject = true;
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
        playing = false;
    }

    @Override
    public void resume() {
        playing = true;
    }

    public void playPause() {
        if (playing) pause();
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


        for (ActorHandler object : stage.getObjects()) {
            object.setWidth(Gdx.graphics.getWidth() * camera.zoom);
            object.setHeight(Gdx.graphics.getHeight() * camera.zoom);
            object.setPosition(camera.position.x - Gdx.graphics.getWidth() * camera.zoom / 2, camera.position.y - Gdx.graphics.getHeight() * camera.zoom / 2);
        }

        if (playing) stage.act(delta);
        stage.draw();
    }

    @Override
    public void show() {

    }

    private int checkForActors(float x, float y) {
        for (int i = 0; i < stage.size(); i++) {
            ActorHandler sprite = stage.get(stage.size() - 1 - i);
            Vector2 screenCoordinates = new Vector2(x, y);
            Vector3 worldCoordinates = camera.unproject(new Vector3(screenCoordinates.x, screenCoordinates.y, 0));
            if (sprite.getActorX() < worldCoordinates.x && worldCoordinates.x < sprite.getActorX() + sprite.getActorWidth()
                    && sprite.getActorY() < worldCoordinates.y && worldCoordinates.y < sprite.getActorY() + sprite.getActorHeight()
                    && sprite.getVisibility() && !sprite.getLockStatus()) {
                sprite.addStroke();
                canDragObject = true;
                return stage.indexOf(sprite);
            }
        }
        return -1;
    }

    @Override
    public boolean touchDown(float x, float y, int pointer, int button) {
        if (pointer == 0) {
            initialTouch1.set(x, y);
        }
        if (pointer == 1) {
            initialTouch2.set(x, y);
            isTwoFinger=true;
        }
        if(!drawingMask) {
            if (canDragObject && checkForActors(x, y) == draggedSpriteId) {
                dragSprite = true;
                engineEventsListener.onObjectSelected("Image", draggedSpriteId);
            } else if (checkForActors(x, y) != -1) {
                if (stage.size() != 0 && stage.size() > draggedSpriteId)
                    stage.get(draggedSpriteId).removeStroke();
                draggedSpriteId = checkForActors(x, y);
                engineEventsListener.onObjectSelected("Image", draggedSpriteId);
                stage.get(draggedSpriteId).setActorZoomAmount(camera.zoom);
            } else {
                dragSprite = false;
                canDragObject = false;
                if (stage.size() != 0 && stage.size() > draggedSpriteId)
                    stage.get(draggedSpriteId).removeStroke();
                engineEventsListener.onObjectNotSelected();
            }
        }

        return true;
    }

    @Override
    public boolean pan(float x, float y, float deltaX, float deltaY) {
        if (Math.abs(deltaX) < 0.1f && Math.abs(deltaY) < 0.1f) return false;

        if(!twoFingerMovement) {
            if (canDragObject && !dragSprite) {
                engineEventsListener.onObjectNotSelected();
                stage.get(draggedSpriteId).removeStroke();
                canDragObject = false;
            }
            if (dragSprite && !twoFingerMovement) {
                float newX = stage.get(draggedSpriteId).getActorX() + (deltaX) * camera.zoom;
                float newY = stage.get(draggedSpriteId).getActorY() - (deltaY) * camera.zoom;

                stage.get(draggedSpriteId).setActorPosition(newX, newY);


            } else if (!twoFingerMovement) {
                camera.translate(-deltaX * camera.zoom, deltaY * camera.zoom);
            }
        } else {
            stage.get(draggedSpriteId).drawMask(x, y, deltaX, deltaY);
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
        }
        if(twoFingerMovement){
            lastTwoFingerMoveY=0;
            lastTwoFingerMoveX=0;
        }

        return true;
    }

    @Override
    public boolean zoom(float initialDistance, float distance) {
        float zoomAmount = initialDistance - distance;
        isZooming = (zoomAmount != 0);
        float normalizedZoomAmount = zoomAmount / (float) Math.sqrt(Math.pow(Gdx.graphics.getWidth(), 2) + Math.pow(Gdx.graphics.getHeight(), 2));

        if (dragSprite) {
            stage.get(draggedSpriteId).setActorZoomAmount(camera.zoom);
            stage.get(draggedSpriteId).setActorSize(
                    stage.get(draggedSpriteId).getActorWidth() - normalizedZoomAmount * stage.get(draggedSpriteId).getActorWidth() + lastScaleX,
                    stage.get(draggedSpriteId).getActorHeight() - normalizedZoomAmount * stage.get(draggedSpriteId).getActorHeight() + lastScaleY
            );

            stage.get(draggedSpriteId).setActorPosition(
                    stage.get(draggedSpriteId).getActorX() + normalizedZoomAmount/2 * stage.get(draggedSpriteId).getActorWidth() - lastScaleX/2,
                    stage.get(draggedSpriteId).getActorY() + normalizedZoomAmount/2 * stage.get(draggedSpriteId).getActorHeight() - lastScaleY/2
            );

            lastScaleX = normalizedZoomAmount * stage.get(draggedSpriteId).getActorWidth();
            lastScaleY = normalizedZoomAmount * stage.get(draggedSpriteId).getActorHeight();

        } else {
            camera.zoom += normalizedZoomAmount * camera.zoom - lastZoom;
            lastZoom = normalizedZoomAmount * camera.zoom;
            camera.zoom = Math.max(camera.zoom, 0.1f);
            camera.zoom = Math.min(camera.zoom, 10.0f);
        }

        return true;
    }

    public void setResources(Resources resources) {
        this.resources = resources;
    }

    public int getDraggedSpriteId() {
        return draggedSpriteId;
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

    float lastTwoFingerMoveX = 0;
    float lastTwoFingerMoveY = 0;

    @Override
    public boolean pinch(Vector2 initialPointer1, Vector2 initialPointer2, Vector2 pointer1, Vector2 pointer2) {
        if(twoFingerMovement && isTwoFinger){
            float newX = (pointer1.x-initialPointer1.x + pointer2.x-initialPointer2.x) * camera.zoom/2;
            float newY = (pointer1.y-initialPointer1.y + pointer2.y-initialPointer2.y) * camera.zoom/2;
            stage.get(draggedSpriteId).setActorPosition(
                    stage.get(draggedSpriteId).getActorX()+newX-lastTwoFingerMoveX,
                    stage.get(draggedSpriteId).getActorY()-newY+lastTwoFingerMoveY
            );
            lastTwoFingerMoveX = newX;
            lastTwoFingerMoveY = newY;
        }

        return false;
    }

    @Override
    public void pinchStop() {

    }
}
