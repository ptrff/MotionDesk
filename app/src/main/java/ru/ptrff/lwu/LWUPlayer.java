package ru.ptrff.lwu;

import com.badlogic.gdx.Game;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Screen;
import com.badlogic.gdx.assets.AssetManager;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.GL30;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.Sprite;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.utils.ScreenUtils;

public class LWUPlayer implements Screen {
    Game game;

    OrthographicCamera camera;
    SpriteBatch batch;
    ShapeRenderer shapes;
    public Simulation sim;
    private BitmapFont font;
    public Sprite circle;

    public LWUPlayer(final Game game) {
        this.game = game;

        camera = new OrthographicCamera(320, 480);
        camera.position.set(camera.viewportWidth / 2, camera.viewportHeight / 2, 0);

        AssetManager manager = new AssetManager();
        manager.load("background.jpg", Texture.class);
        shapes = new ShapeRenderer();
        batch = new SpriteBatch();
        sim = new Simulation();
        sim.init();
        font = new BitmapFont();
        manager.finishLoading();
        circle = new Sprite(manager.get("background.jpg", Texture.class));
        circle.setCenter(20, 20);

        Gdx.input.setInputProcessor(new MyInputProcessor(this));
    }

    @Override
    public void dispose() {
        //nado inache pamyat pizda
    }

    @Override
    public void hide() {
        // TODO Auto-generated method stub

    }

    @Override
    public void pause() {
        // TODO Auto-generated method stub

    }

    private void draw(float delta) {
        Gdx.gl.glClearColor(1, 0, 0, 1);
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);


        shapes.begin(ShapeRenderer.ShapeType.Filled);
        shapes.setColor(Color.BLACK);
        shapes.rect(0, 0, Gdx.graphics.getWidth(), Gdx.graphics.getHeight());
        sim.render(shapes, circle);
        shapes.end();

        Gdx.gl.glEnable(GL20.GL_BLEND);
        Gdx.gl.glBlendFunc(GL20.GL_ONE, GL20.GL_ONE);
        batch.begin();

        sim.overlay(batch,font);
        batch.end();
        sim.update(Gdx.graphics.getDeltaTime());
        Gdx.gl.glDisable(GL20.GL_BLEND);

    }

    private void update(float delta) {
    }

    @Override
    public void render(float delta) {
        //ScreenUtils.clear(0f, 0f, 1f, 1f);
        update(delta);
        draw(delta);
    }

    @Override
    public void resize(int width, int height) {
        // TODO Auto-generated method stub

    }

    @Override
    public void resume() {
        // TODO Auto-generated method stub

    }

    @Override
    public void show() {
        // TODO Auto-generated method stub
    }
}