package com.tianyi.zhang.multiplayer.snake.states.server;

import com.badlogic.gdx.Gdx;
import com.esotericsoftware.kryonet.Connection;
import com.esotericsoftware.kryonet.Listener;
import com.tianyi.zhang.multiplayer.snake.App;
import com.tianyi.zhang.multiplayer.snake.agents.Server;
import com.tianyi.zhang.multiplayer.snake.agents.messages.Packet;
import com.tianyi.zhang.multiplayer.snake.elements.Snake;
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

        int[] snakeIds = new int[connectionIds.size()+1];
        // Server has snake ID 0
        snakeIds[0] = 0;
        for (int i = 1; i <= connectionIds.size(); ++i) {
            snakeIds[i] = connectionIds.get(i-1);
        }
        snapshots = Collections.synchronizedList(new LinkedList<Snapshot>());
        snapshots.add(new Snapshot(snakeIds));

        _app.getAgent().setListener(new Listener() {
            @Override
            public void received(Connection connection, Object object) {
                if (object instanceof byte[]) {
                    Gdx.app.debug(TAG, "Update from client " + String.valueOf(Server.parseReceived(object).getSnapshots(0).getSnakes(0).getId()) + " received.");
                } else {
                    Gdx.app.debug(TAG, "KeepAlive object received");
                }
            }
        });

        // TODO: Implement intermediate state where server sends out first packet and waits for clients to reply READY
        _app.getAgent().send(buildFirstPacket());

        Gdx.app.debug(TAG, "Server main game loaded");
    }

    private Packet.Update buildFirstPacket() {
        Snake[] snakes = snapshots.get(0).getSnakes();
        Packet.Update.PSnapshot.Builder snapshotBuilder= Packet.Update.PSnapshot.newBuilder();
        snapshotBuilder.setStep(0);
        for (Snake s : snakes) {
            snapshotBuilder.addSnakes(Packet.Update.PSnake.newBuilder().setId(s.ID).setInputId(0).setDirection(s.DIRECTION).addAllCoords(s.COORDS).build());
        }
        return Packet.Update.newBuilder().setState(Packet.Update.PState.READY).addSnapshots(snapshotBuilder).build();
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
