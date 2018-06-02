package com.tianyi.zhang.multiplayer.snake.states.server;

import com.badlogic.gdx.Gdx;
import com.esotericsoftware.kryonet.Connection;
import com.esotericsoftware.kryonet.Listener;
import com.tianyi.zhang.multiplayer.snake.App;
import com.tianyi.zhang.multiplayer.snake.states.GameState;

import java.io.IOException;

public class BroadcastState extends GameState {
    public BroadcastState(App app) {
        super(app);
        try {
            this.app.getAgent().broadcast(new Listener() {
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
        } catch (IOException e) {
            // TODO: Display error message
            Gdx.app.error("SERVER BROADCAST ERROR", e.getMessage());
        }
    }

    @Override
    public void render() {
        super.render();
    }

    @Override
    public void input() {

    }
}
