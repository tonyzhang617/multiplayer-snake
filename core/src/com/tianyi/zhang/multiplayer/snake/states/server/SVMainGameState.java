package com.tianyi.zhang.multiplayer.snake.states.server;

import com.badlogic.gdx.Gdx;
import com.esotericsoftware.kryonet.Connection;
import com.esotericsoftware.kryonet.Listener;
import com.tianyi.zhang.multiplayer.snake.App;
import com.tianyi.zhang.multiplayer.snake.agents.Server;
import com.tianyi.zhang.multiplayer.snake.agents.messages.Packet;
import com.tianyi.zhang.multiplayer.snake.elements.Snake;
import com.tianyi.zhang.multiplayer.snake.elements.Snapshot;
import com.tianyi.zhang.multiplayer.snake.helpers.Constants;
import com.tianyi.zhang.multiplayer.snake.states.GameState;

import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import static com.tianyi.zhang.multiplayer.snake.helpers.Constants.FUTURE_STATES;

public class SVMainGameState extends GameState {
    private static final String TAG = SVMainGameState.class.getCanonicalName();
    private final List<Snapshot> snapshots;
    private final Object snapshotsLock;

    public SVMainGameState(App app, List<Integer> connectionIds) {
        super(app);

        int[] snakeIds = new int[connectionIds.size()+1];
        // Server has snake ID 0
        snakeIds[0] = 0;
        for (int i = 1; i <= connectionIds.size(); ++i) {
            snakeIds[i] = connectionIds.get(i-1);
        }

        snapshotsLock = new Object();
        snapshots = new LinkedList<Snapshot>();
        synchronized (snapshotsLock) {
            snapshots.add(new Snapshot(snakeIds));
            for (int i = 0; i < FUTURE_STATES; ++i) {
                snapshots.add(snapshots.get(i).next());
            }
        }

        _app.getAgent().setListener(new Listener() {
            @Override
            public void received(Connection connection, Object object) {
                if (object instanceof byte[]) {
                    processClientPacket(Server.parseReceived(object));
                } else {
                    Gdx.app.debug(TAG, "KeepAlive object received");
                }
            }
        });

        // TODO: Implement intermediate state where server sends out first packet and waits for clients to reply READY
        _app.getAgent().send(buildFirstPacket());

        ScheduledExecutorService executor = Executors.newSingleThreadScheduledExecutor();
        executor.scheduleAtFixedRate(new Runnable() {
            @Override
            public void run() {
                sendUpdate();
            }
        }, Constants.SERVER_SEND_EVERY_MS, Constants.SERVER_SEND_EVERY_MS, TimeUnit.MILLISECONDS);

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

    private void processClientPacket(Packet.Update update) {
        // TODO: Very inefficient implementation; should store incoming packets in a priority queue and process them later
        int step = update.getSnapshots(0).getStep();
        Packet.Update.PSnake pSnake = update.getSnapshots(0).getSnakes(0);
        synchronized (snapshotsLock) {
            int size = snapshots.size();
            int lastStep = snapshots.get(size-1).getStep();
            int index = size - lastStep + step - 1;
            if (index >= 0 && index < size) {
                Snake snake = snapshots.get(index).getSnakes()[pSnake.getId()];
                if (pSnake.getInputId() > snake.INPUT_ID) {
                    snapshots.get(index).updateDirection(pSnake.getId(), pSnake.getDirection(), pSnake.getInputId());
                    if (pSnake.getDirection() != snake.DIRECTION) {
                        for (; index < size-1; ++index) {
                            snapshots.set(index+1, snapshots.get(index).next());
                        }
                    }
                }
            }
        }
    }

    private void sendUpdate() {
        Packet.Update.Builder builder = Packet.Update.newBuilder();
        builder.setState(Packet.Update.PState.GAME_IN_PROGRESS);
        synchronized (snapshotsLock) {
            int size = snapshots.size();
            for (int i = 0; i < size - FUTURE_STATES; ++i) {
                Snapshot snapshot = snapshots.get(i);
                Packet.Update.PSnapshot.Builder snapshotBuilder = Packet.Update.PSnapshot.newBuilder();
                snapshotBuilder.setStep(snapshot.getStep());
                Snake[] snakes = snapshot.getSnakes();
                for (int j = 0; j < snakes.length; ++j) {
                    snapshotBuilder.addSnakes(Packet.Update.PSnake.newBuilder().setId(snakes[j].ID).setInputId(snakes[j].INPUT_ID).setDirection(snakes[j].DIRECTION).build());
                }
                builder.addSnapshots(snapshotBuilder.build());
            }
        }
        _app.getAgent().send(builder.build());
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
