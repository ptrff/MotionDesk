package ru.ptrff.motiondesk.engine.scene;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.transition.Scene;
import android.util.Log;
import android.util.Pair;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Screen;
import com.badlogic.gdx.graphics.Color;
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

import io.reactivex.Completable;
import io.reactivex.Observable;
import io.reactivex.Single;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.schedulers.Schedulers;
import ru.ptrff.motiondesk.data.local.WallpaperItemRepository;
import ru.ptrff.motiondesk.models.SceneParameters;
import ru.ptrff.motiondesk.models.WallpaperItem;
import ru.ptrff.motiondesk.utils.JSONFormatter;
import ru.ptrff.motiondesk.utils.ProjectManager;

public class WallpaperEngine extends WallpaperEngineBase implements Screen {

    //Base
    private static final String TAG = "WallpaperEngine";
    private boolean playing = true;
    private final String currentProjectId;
    private final Context context;

    //Scene
    private OrthographicCamera camera;
    private SpriteBatch batch;
    private StageWrapper stage;
    private SceneParameters sceneParameters;
    private Color backgroundColor = Color.BLACK;
    private boolean loading = true;

    private WallpaperItem wallpaperItem;

    //Loading
    private BitmapFont font;
    private BitmapFont infoFont;
    private float dotAnimationTime;
    private String loadingText;
    private String loadingInfo;
    float scaleFactor = 1;

    public WallpaperEngine(Context context) {
        this.context = context;
        SharedPreferences sharedPreferences = context.getSharedPreferences("MotionDesk", Context.MODE_PRIVATE);
        currentProjectId = sharedPreferences.getString("current", "null");
    }

    @SuppressLint("CheckResult")
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
        infoFont = new BitmapFont();
        font.getData().setScale(4);
        infoFont.getData().setScale(2);
        dotAnimationTime = 0;
        loadingText = "Loading";
        loadingInfo = "initialization";

        //Load scene
        if (!currentProjectId.equals("null")) {
            Observable
                    .fromCallable(() -> loadScene(context))
                    .subscribeOn(Schedulers.io())
                    .observeOn(AndroidSchedulers.mainThread())
                    .subscribe(
                            result -> loading = result,
                            throwable -> Log.e("WallpaperEngine", "Error getting current wallpaper item from db", throwable)
                    );
        } else {
            Log.e("WallpaperEngine", "Error getting current wallpaper item id");
            loadingText = "Error";
            loadingInfo = "project not set";
        }
    }

    private boolean loadScene(Context context) {
        JsonObject object = ProjectManager.getSceneJsonFromFolder(context, "Current");

        if (object != null) {
            loadSceneParameters(object);
            addActorsFromJsonObject(object, context);
            return false;
        } else {
            loadingText = "Error";
            loadingInfo = "no scene.json in project files";
            return true;
        }
    }

    private void loadSceneParameters(JsonObject jsonObject) {
        loadingInfo = "loading scene parameters";
        JsonObject general = jsonObject.get("general").getAsJsonObject();
        sceneParameters = JSONFormatter.getSceneParametersFromJson(general);
        backgroundColor = Color.valueOf(sceneParameters.getBackgroundColor());
    }

    private void addActorsFromJsonObject(JsonObject jsonObject, Context context) {
        JsonArray objects = jsonObject.get("objects").getAsJsonArray();

        loadingInfo = "loading objects textures";

        Array<Pair<Pixmap, JsonObject>> pairs = JSONFormatter.JsonArrayToPairs(objects, context, "Current");

        Log.i(TAG, "Textures uploaded and converted, adding actors");

        Gdx.app.postRunnable(() -> {
            for (Pair<Pixmap, JsonObject> pair : pairs) {
                JsonObject actorData = pair.second;
                Log.i(TAG, "Adding  " + actorData.get("name").getAsString());
                loadingInfo = "Adding  " + actorData.get("name").getAsString();

                Texture texture = new Texture(pair.first);

                ImageActor imageActor = new ImageActor(texture, actorData.get("name").getAsString());

                ActorHandler actor = new ActorHandler(imageActor);

                actor.setActorPosition(
                        actorData.get("x").getAsFloat(),
                        actorData.get("y").getAsFloat()
                );
                actor.setActorRotation(
                        actorData.get("rotation").getAsFloat()
                );
                actor.setActorSize(
                        actorData.get("width").getAsFloat(),
                        actorData.get("height").getAsFloat()
                );
                actor.setVisibility(
                        actorData.get("visibility").getAsBoolean()
                );
                actor.setLockStatus(
                        actorData.get("locked").getAsBoolean()
                );

                if (!actorData.get("effects").getAsJsonArray().isEmpty()) {
                    actor.addEffects(JSONFormatter.JsonArrayToEffects(actorData.get("effects").getAsJsonArray()));
                }

                stage.add(actor);
                stage.addActor(actor);
            }
        });

        Log.i(TAG, "Scene loaded");
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
        ScreenUtils.clear(backgroundColor);
        camera.update();

        for (ActorHandler object : stage.getObjects()) {
            object.setWidth(Gdx.graphics.getWidth());
            object.setHeight(Gdx.graphics.getHeight());
            object.setPosition(camera.position.x - Gdx.graphics.getWidth() / 2f, camera.position.y - Gdx.graphics.getHeight() / 2f);
        }

        if (loading) {
            if (!loadingText.equals("Error")) {
                dotAnimationTime += delta;
                if (dotAnimationTime > 0.5f) {
                    dotAnimationTime = 0;
                    loadingText += ".";
                    if (loadingText.length() > 10) {
                        loadingText = "Loading";
                        scaleFactor *= -1;
                    }
                }
                float scale = font.getData().scaleX;
                font.getData().setScale(scale + scaleFactor * dotAnimationTime * 0.05f);
            }

            Pair<Float, Float> loadingTextSize = recalculateFontLayout(font, loadingText);
            Pair<Float, Float> infoTextSize = recalculateFontLayout(infoFont, loadingInfo);


            batch.begin();
            font.draw(
                    batch,
                    loadingText,
                    Gdx.graphics.getWidth() / 2f - loadingTextSize.first / 2f,
                    Gdx.graphics.getHeight() / 2f + loadingTextSize.second / 2f
            );

            infoFont.draw(
                    batch,
                    loadingInfo,
                    Gdx.graphics.getWidth() / 2f - infoTextSize.first / 2f,
                    Gdx.graphics.getHeight() / 2f - loadingTextSize.second * 2
            );

            batch.end();
        } else if (playing) {
            stage.act(delta);
            stage.draw();
        }
    }

    private Pair<Float, Float> recalculateFontLayout(BitmapFont font, String text) {
        GlyphLayout layout = new GlyphLayout();
        layout.setText(font, text);
        return new Pair<>(layout.width, layout.height);
    }
}
