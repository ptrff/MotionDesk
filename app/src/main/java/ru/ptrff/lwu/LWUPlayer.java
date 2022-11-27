package ru.ptrff.lwu;

import com.badlogic.gdx.Game;
import com.badlogic.gdx.Screen;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.utils.ScreenUtils;

public class LWUPlayer implements Screen {
    Game game;

    OrthographicCamera camera;
    Texture img;
    TextureRegion background;
    SpriteBatch batch;

    public LWUPlayer(final Game game) {
        this.game = game;

        camera = new OrthographicCamera(320, 480);
        camera.position.set(camera.viewportWidth / 2, camera.viewportHeight / 2, 0);

        //textureBg = new Texture("badlogic.jpg");
        //textureBg.setFilter(Texture.TextureFilter.Linear, Texture.TextureFilter.Linear);
        //background = new TextureRegion(textureBg, 0, 0, 256, 512);
        batch = new SpriteBatch();
        img = new Texture("badlogic.jpg");
    }

    @Override
    public void dispose() {
        // TODO Auto-generated method stub

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
        //GLCommon gl = Gdx.gl;
        //gl.glClearColor(0, 0, 0, 1);
        //gl.glClear(GL10.GL_COLOR_BUFFER_BIT);
        //camera.update();

        //batch.setProjectionMatrix(camera.combined);
        //batch.begin();
        //batch.draw(background, 0, 0,camera.viewportWidth, camera.viewportHeight);
        //batch.end();
        batch.begin();
        batch.draw(img, 0f, 0f);
        batch.end();
    }

    private void update(float delta) {
    }

    @Override
    public void render(float delta) {
        ScreenUtils.clear(0f, 0f, 1f, 1f);
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