package com.tianyi.zhang.multiplayer.snake.states.client;

import com.badlogic.gdx.Gdx;
import com.esotericsoftware.kryonet.Connection;
import com.esotericsoftware.kryonet.Listener;
import com.tianyi.zhang.multiplayer.snake.App;
import com.tianyi.zhang.multiplayer.snake.states.GameState;

public class LookForServerState extends GameState {
    public LookForServerState(App app) {
        super(app);
        this.app.getAgent().lookForServer(new Listener() {
            @Override
            public void connected(Connection connection) {
                super.connected(connection);
                Gdx.app.debug("BROADCAST", "started");
            }

            @Override
            public void disconnected(Connection connection) {
                super.disconnected(connection);
                Gdx.app.debug("BROADCAST", "disconnected");
            }

            @Override
            public void received(Connection connection, Object object) {
                super.received(connection, object);
                Gdx.app.debug("BROADCAST", "received");
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
