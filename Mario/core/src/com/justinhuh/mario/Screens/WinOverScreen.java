package com.justinhuh.mario.Screens;

import com.badlogic.gdx.Game;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Screen;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.utils.viewport.FitViewport;
import com.badlogic.gdx.utils.viewport.Viewport;
import com.justinhuh.mario.MarioDemo;

/**
 * Created by Justin on 10/8/2016.
 */
public class WinOverScreen implements Screen {
    private Viewport viewport;
    private Stage stage;

    private Game game;

    public WinOverScreen(Game game, int score){
        this.game = game;
        viewport = new FitViewport(MarioDemo.V_WIDTH, MarioDemo.V_HEIGHT, new OrthographicCamera());
        stage = new Stage(viewport, ((MarioDemo) game).batch);

        Label.LabelStyle font = new Label.LabelStyle(new BitmapFont(), Color.WHITE);

        Table table = new Table();
        table.center();
        table.setFillParent(true);

        Label gameOverLabel = new Label("YOU WIN!", font);
        Label scoreLabel = new Label("Score: " + score, font);
        Label playAgainLabel = new Label("Touch to Play Again", font);

        table.add(gameOverLabel).expandX();
        table.row();
        table.add(scoreLabel).expandX().padTop(10f);
        table.row();
        table.add(playAgainLabel).expandX().padTop(10f);

        stage.addActor(table);
    }
    @Override
    public void show() {

    }

    @Override
    public void render(float delta) {
        if(Gdx.input.justTouched()) {
            game.setScreen(new PlayScreen((MarioDemo) game));
            dispose();
        }
        Gdx.gl.glClearColor(0, 0, 0, 1);
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);
        stage.draw();
    }

    @Override
    public void resize(int width, int height) {

    }

    @Override
    public void pause() {

    }

    @Override
    public void resume() {

    }

    @Override
    public void hide() {

    }

    @Override
    public void dispose() {
        stage.dispose();
    }
}
