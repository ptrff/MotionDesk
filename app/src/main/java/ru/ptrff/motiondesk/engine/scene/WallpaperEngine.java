package ru.ptrff.motiondesk.engine.scene;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.util.Log;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Screen;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.Pixmap;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.Animation;
import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.GlyphLayout;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.TextureAtlas;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.ScreenUtils;
import com.badlogic.gdx.utils.viewport.ScreenViewport;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;

import java.io.ByteArrayOutputStream;
import java.util.Objects;

import io.reactivex.Observable;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.schedulers.Schedulers;
import ru.ptrff.motiondesk.data.local.WallpaperItemRepository;
import ru.ptrff.motiondesk.models.WallpaperItem;
import ru.ptrff.motiondesk.utils.JSONFormatter;
import ru.ptrff.motiondesk.utils.ProjectManager;

public class WallpaperEngine extends WallpaperEngineBase implements Screen {

    //Base
    private boolean playing = true;

    //Scene
    private OrthographicCamera camera;
    private SpriteBatch batch;
    private StageWrapper stage;

    private Array<ActorHandler> preparedActors;
    private boolean loadAfterPrepare = false;

    private WallpaperItem wallpaperItem;

    //Loading
    private BitmapFont font;
    private float dotAnimationTime;
    private String loadingText;

    float textWidth;
    float textHeight;
    float scaleFactor = 1;

    @SuppressLint("CheckResult")
    public WallpaperEngine(Context context) {
        SharedPreferences sharedPreferences = context.getSharedPreferences("MotionDesk", Context.MODE_PRIVATE);
        String currentProjectId = sharedPreferences.getString("current", "null");
        if (!currentProjectId.equals("null")) {
            WallpaperItemRepository repo = new WallpaperItemRepository(context);
            repo
                    .getWallpaperItemById(currentProjectId)
                    .subscribeOn(Schedulers.io())
                    .observeOn(AndroidSchedulers.mainThread())
                    .subscribe(wallpaperItem -> {
                        this.wallpaperItem = wallpaperItem;
                        loadScene(context);
                    }, throwable -> Log.e("WallpaperEngine", "Error getting current wallpaper item from db", throwable));
        } else {
            Log.e("WallpaperEngine", "Error getting current wallpaper item id");
        }
    }

    @Override
    public void init() {
        //Camera
        camera = new OrthographicCamera();
        camera.setToOrtho(false, Gdx.graphics.getWidth(), Gdx.graphics.getHeight());

        //Batch
        batch = new SpriteBatch();

        //Scene
        stage = new StageWrapper(new ScreenViewport(camera), batch);

        //Loading text
        font = new BitmapFont();
        font.getData().setScale(4);
        dotAnimationTime = 0;
        loadingText = "Loading";

        recalculateFontLayout();


        if (preparedActors != null) {
            loadPrepared();
        } else {
            loadAfterPrepare = true;
        }
    }

    @SuppressLint("CheckResult")
    private void loadScene(Context context) {
        System.out.println(wallpaperItem.toString());

        Observable
                .just(Objects.requireNonNull(ProjectManager.getSceneJsonFromCurrent(context)))
                .flatMap(jsonObject -> Observable.fromCallable(() -> {
                    Gdx.app.postRunnable(() -> addActorsFromJsonObject(jsonObject, context));
                    return true;
                }))
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe();
    }

    private void addActorsFromJsonObject(JsonObject jsonObject, Context context) {
        JsonArray objects = jsonObject.get("objects").getAsJsonArray();
        preparedActors = JSONFormatter.JsonArrayToActors(objects, context);
        if (loadAfterPrepare) {
            loadPrepared();
        }
    }

    private void loadPrepared() {
        for (ActorHandler actor : preparedActors) {
            stage.add(actor);
            stage.addActor(actor);
        }
        loadAfterPrepare = false;
    }

    @Override
    public void dispose() {
        batch.dispose();
        stage.dispose();
        font.dispose();
    }

    @Override
    public void resize(int width, int height) {
        if (stage != null) stage.getViewport().update(width, height, true);
    }

    @Override
    public void pause() {
        playing = false;
    }

    @Override
    public void resume() {
        playing = true;
    }

    @Override
    public void render(float delta) {
        ScreenUtils.clear(0.1f, 0.1f, 0.1f, 1);
        camera.update();

        for (ActorHandler object : stage.getObjects()) {
            object.setWidth(Gdx.graphics.getWidth() * camera.zoom);
            object.setHeight(Gdx.graphics.getHeight() * camera.zoom);
            object.setPosition(camera.position.x - Gdx.graphics.getWidth() * camera.zoom / 2, camera.position.y - Gdx.graphics.getHeight() * camera.zoom / 2);
        }

        if (loadAfterPrepare) {
            dotAnimationTime += delta;
            if (dotAnimationTime > 0.5f) {
                dotAnimationTime = 0;
                loadingText += ".";
                if (loadingText.length() > 10) {
                    loadingText = "Loading";
                    scaleFactor*=-1;
                }
            }
            float scale = font.getData().scaleX;
            font.getData().setScale(scale+scaleFactor*dotAnimationTime*0.05f);
            recalculateFontLayout();

            batch.begin();
            font.draw(batch, loadingText, Gdx.graphics.getWidth() / 2f - textWidth / 2f, Gdx.graphics.getHeight() / 2f + textHeight / 2f);
            batch.end();
        } else if (playing) {
            stage.act(delta);
            stage.draw();
        }
    }

    private void recalculateFontLayout(){
        GlyphLayout layout = new GlyphLayout();
        layout.setText(font, "Loading");
        textWidth = layout.width;
        textHeight = layout.height;
    }
}
