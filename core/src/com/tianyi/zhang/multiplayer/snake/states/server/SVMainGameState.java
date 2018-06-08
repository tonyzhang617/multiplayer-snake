package com.tianyi.zhang.multiplayer.snake.states.server;

import com.badlogic.gdx.Gdx;
import com.esotericsoftware.kryonet.Connection;
import com.esotericsoftware.kryonet.FrameworkMessage;
import com.esotericsoftware.kryonet.Listener;
import com.tianyi.zhang.multiplayer.snake.App;
import com.tianyi.zhang.multiplayer.snake.agents.Server;
import com.tianyi.zhang.multiplayer.snake.agents.messages.Packet;
import com.tianyi.zhang.multiplayer.snake.elements.Snapshots;
import com.tianyi.zhang.multiplayer.snake.states.GameState;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class SVMainGameState extends GameState {
    private static final String TAG = SVMainGameState.class.getCanonicalName();
    private final Map<Integer, Snapshots> clientSnapshots;

    public SVMainGameState(App app, List<Integer> connectionIds) {
        super(app);

        clientSnapshots = new HashMap<Integer, Snapshots>();
        for (Integer i : connectionIds) {
            clientSnapshots.put(new Integer(i), new Snapshots());
        }

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
