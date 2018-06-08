package com.tianyi.zhang.multiplayer.snake.states.client;

import com.badlogic.gdx.Gdx;
import com.esotericsoftware.kryonet.Connection;
import com.esotericsoftware.kryonet.Listener;
import com.tianyi.zhang.multiplayer.snake.App;
import com.tianyi.zhang.multiplayer.snake.states.GameState;

public class LookForServerState extends GameState {
    private static final String TAG = LookForServerState.class.getCanonicalName();

    public LookForServerState(App app) {
        super(app);
        _app.getAgent().lookForServer(new Listener() {
            @Override
            public void connected(Connection connection) {
                Gdx.app.debug(TAG, "started");
                _app.pushState(new MainGameState(_app));
            }

            @Override
            public void disconnected(Connection connection) {
                Gdx.app.debug(TAG, "disconnected");
            }
        });
    }

    @Override
    public void show() {

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

    }
}
