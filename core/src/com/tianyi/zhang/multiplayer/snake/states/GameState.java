package com.tianyi.zhang.multiplayer.snake.states;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Screen;
import com.badlogic.gdx.graphics.GL20;
import com.tianyi.zhang.multiplayer.snake.App;

public abstract class GameState implements Screen {
    protected final App _app;

    public GameState(App app) {
        _app = app;
    }

    @Override
    public void render(float delta) {
        Gdx.gl.glClearColor(1, 0, 0, 1);
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);
    }
}
