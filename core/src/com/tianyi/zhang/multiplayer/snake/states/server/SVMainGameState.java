package com.tianyi.zhang.multiplayer.snake.states.server;

import com.badlogic.gdx.Gdx;
import com.esotericsoftware.kryonet.Connection;
import com.esotericsoftware.kryonet.FrameworkMessage;
import com.esotericsoftware.kryonet.Listener;
import com.tianyi.zhang.multiplayer.snake.App;
import com.tianyi.zhang.multiplayer.snake.agents.Server;
import com.tianyi.zhang.multiplayer.snake.agents.messages.Packet;
import com.tianyi.zhang.multiplayer.snake.elements.Snapshot;
import com.tianyi.zhang.multiplayer.snake.states.GameState;

import java.util.Collections;
import java.util.LinkedList;
import java.util.List;

public class SVMainGameState extends GameState {
    private static final String TAG = SVMainGameState.class.getCanonicalName();
    private final List<Snapshot> snapshots;

    public SVMainGameState(App app, List<Integer> connectionIds) {
        super(app);

        snapshots = Collections.synchronizedList(new LinkedList<Snapshot>());
        snapshots.set(0, new Snapshot(connectionIds));

        _app.getAgent().setListener(new Listener() {
            @Override
            public void received(Connection connection, Object object) {
                if (!(object instanceof FrameworkMessage.KeepAlive)) {
                    Gdx.app.debug(TAG, Integer.toString(Server.parseReceived(object).getSnakes(0).getDir()));
                } else {
                    Gdx.app.debug(TAG, "KeepAlive object received");
                }
            }
        });

        // TODO: Send a snapshot of all snakes to clients
        // Send SERVER_READY packet to all clients
        Packet.Update update = Packet.Update.newBuilder()
                .setServerState(Packet.Update.ServerState.SERVER_READY).build();
        _app.getAgent().send(update);

        Gdx.app.debug(TAG, "Server main game loaded");
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
