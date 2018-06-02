package com.tianyi.zhang.multiplayer.snake.states;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.GL20;
import com.tianyi.zhang.multiplayer.snake.App;

public abstract class GameState {
    protected App app;

    public GameState(App app) {
        this.app = app;
    }

    public void render() {
        Gdx.gl.glClearColor(1, 0, 0, 1);
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);
    }

    public abstract void input();

    public void destroy() {

    }
}
