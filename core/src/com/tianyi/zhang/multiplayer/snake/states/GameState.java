package com.tianyi.zhang.multiplayer.snake.states;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Screen;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.GL20;
import com.tianyi.zhang.multiplayer.snake.App;
import com.tianyi.zhang.multiplayer.snake.helpers.AssetManager;

public abstract class GameState implements Screen {
    protected final App _app;

    public GameState(App app) {
        _app = app;
    }

    @Override
    public void render(float delta) {
        Color color = AssetManager.INSTANCE.getBackgroundColor();
        Gdx.gl.glClearColor(color.r, color.g, color.b, color.a);
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);
    }
}
