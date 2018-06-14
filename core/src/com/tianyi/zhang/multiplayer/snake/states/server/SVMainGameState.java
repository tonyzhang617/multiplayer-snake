package com.tianyi.zhang.multiplayer.snake.states.server;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.InputProcessor;
import com.badlogic.gdx.graphics.GL20;
import com.esotericsoftware.kryonet.Connection;
import com.esotericsoftware.kryonet.Listener;
import com.tianyi.zhang.multiplayer.snake.App;
import com.tianyi.zhang.multiplayer.snake.agents.Server;
import com.tianyi.zhang.multiplayer.snake.agents.messages.Packet;
import com.tianyi.zhang.multiplayer.snake.elements.Snake;
import com.tianyi.zhang.multiplayer.snake.elements.Snapshot;
import com.tianyi.zhang.multiplayer.snake.helpers.Constants;
import com.tianyi.zhang.multiplayer.snake.helpers.Helpers;
import com.tianyi.zhang.multiplayer.snake.states.GameState;

import java.util.LinkedList;
import java.util.List;
import java.util.Set;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

import static com.tianyi.zhang.multiplayer.snake.helpers.Constants.*;

public class SVMainGameState extends GameState implements InputProcessor {
    private static final String TAG = SVMainGameState.class.getCanonicalName();
//    private final List<Snapshot> snapshots;
//    private final Object snapshotsLock;
    private long lastUpdateTime;
    private AtomicInteger inputId;

    /**
     *
     * @param app a reference to App
     * @param connectionIds a sorted, ascending list of integers representing client IDs
     */
    public SVMainGameState(App app, List<Integer> connectionIds) {
        super(app);
        Gdx.input.setInputProcessor(this);

//        int[] snakeIds = new int[connectionIds.size()+1];
//        // Server has snake ID 0
//        snakeIds[0] = 0;
//        for (int i = 1; i <= connectionIds.size(); ++i) {
//            snakeIds[i] = connectionIds.get(i-1);
//        }

//        snapshotsLock = new Object();
//        snapshots = new LinkedList<Snapshot>();
//        synchronized (snapshotsLock) {
//            snapshots.add(new Snapshot(snakeIds));
//            for (int i = 0; i < FUTURE_STATES; ++i) {
//                snapshots.add(snapshots.get(i).next());
//            }
//        }

        // TODO: Implement intermediate state where server sends out first packet and waits for clients to reply READY
//        _app.getAgent().send(buildFirstPacket());

//        lastUpdateTime = 0;
//        inputId = new AtomicInteger(0);

        _app.getAgent().send(Packet.Update.newBuilder().setState(Packet.Update.PState.READY).build());
        Gdx.app.debug(TAG, String.valueOf(Helpers.getNanoTime()));

        Gdx.app.debug(TAG, "Server main game loaded");
    }

//    private Snapshot getCurrentSnapshot() {
//        synchronized (snapshotsLock) {
//            return snapshots.get(snapshots.size() - FUTURE_STATES);
//        }
//    }
//
//    private void nextStep() {
//        synchronized (snapshotsLock) {
//            int size = snapshots.size();
//            snapshots.add(snapshots.get(size-1).next());
//            if (size == PAST_STATES + 1 + FUTURE_STATES) {
//                snapshots.remove(0);
//            }
//        }
//    }
//
//    private Packet.Update buildFirstPacket() {
//        Packet.Update.PSnapshot.Builder snapshotBuilder= Packet.Update.PSnapshot.newBuilder();
//        snapshotBuilder.setStep(0);
//
//        Snapshot firstSnapshot = null;
//        synchronized (snapshotsLock) {
//            firstSnapshot = snapshots.get(0);
//        }
//        Set<Integer> keys = firstSnapshot.getSnakeIds();
//        for (Integer i : keys) {
//            Snake s = firstSnapshot.getSnakeById(i);
//            snapshotBuilder.addSnakes(Packet.Update.PSnake.newBuilder().setId(s.ID)
//                    .setInputId(0).setDirection(s.DIRECTION).addAllCoords(s.COORDS).build());
//        }
//        return Packet.Update.newBuilder().setState(Packet.Update.PState.READY).addSnapshots(snapshotBuilder).build();
//    }
//
//    private void processClientPacket(Packet.Update update) {
//        // TODO: Implement Server reconciliation
//        int step = update.getSnapshots(0).getStep();
//        Packet.Update.PSnake pSnake = update.getSnapshots(0).getSnakes(0);
//        synchronized (snapshotsLock) {
//            int size = snapshots.size();
//            int lastStep = snapshots.get(size-1).getStep();
//            int index = size - lastStep + step - 1;
//            if (index >= 0 && index < size) {
//                Snake snake = snapshots.get(index).getSnakeById(pSnake.getId());
//                if (pSnake.getInputId() > snake.INPUT_ID) {
//                    snapshots.get(index).updateDirection(pSnake.getId(), pSnake.getDirection(), pSnake.getInputId());
//                    if (pSnake.getDirection() != snake.DIRECTION) {
//                        for (; index < size-1; ++index) {
//                            snapshots.set(index+1, snapshots.get(index).next());
//                        }
//                    }
//                }
//            }
//        }
//    }
//
//    private void sendUpdate() {
//        try {
//            Packet.Update.Builder builder = Packet.Update.newBuilder();
//            builder.setState(Packet.Update.PState.GAME_IN_PROGRESS);
//            synchronized (snapshotsLock) {
//                int size = snapshots.size();
//                for (int i = 0; i < size - FUTURE_STATES; ++i) {
//                    Snapshot snapshot = snapshots.get(i);
//                    Set<Integer> keys = snapshot.getSnakeIds();
//                    Packet.Update.PSnapshot.Builder snapshotBuilder = Packet.Update.PSnapshot.newBuilder();
//                    snapshotBuilder.setStep(snapshot.getStep());
//                    for (Integer k : keys) {
//                        Snake s = snapshot.getSnakeById(k);
//                        // TODO: remove
//                        if (i == size - FUTURE_STATES - 1 && k.intValue() == 0) {
//                            Gdx.app.debug(TAG, String.valueOf(s.DIRECTION));
//                        }
//                        snapshotBuilder.addSnakes(Packet.Update.PSnake.newBuilder().setId(s.ID).setInputId(s.INPUT_ID).setDirection(s.DIRECTION).build());
//                    }
//                    builder.addSnapshots(snapshotBuilder.build());
//                }
//            }
//            _app.getAgent().send(builder.build());
//        } catch (Exception e) {
//            Gdx.app.error(TAG, "Error while sending update", e);
//        }
//    }

    @Override
    public void render(float delta) {
//        nextStep();
        Gdx.gl.glClearColor(0, 0, 1, 1);
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);
//        lastUpdateTime = System.nanoTime();
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

    @Override
    public boolean keyDown(int keycode) {
//        Snapshot snapshot = getCurrentSnapshot();
//        if (keycode == Input.Keys.LEFT) {
//            snapshot.updateDirection(0, LEFT, inputId.incrementAndGet());
//        } else if (keycode == Input.Keys.UP) {
//            Gdx.app.debug(TAG, "Up pressed");
//            snapshot.updateDirection(0, UP, inputId.incrementAndGet());
//        } else if (keycode == Input.Keys.RIGHT) {
//            snapshot.updateDirection(0, RIGHT, inputId.incrementAndGet());
//        } else if (keycode == Input.Keys.DOWN) {
//            snapshot.updateDirection(0, DOWN, inputId.incrementAndGet());
//        }
//        return true;
        return false;
    }

    @Override
    public boolean keyUp(int keycode) {
        return false;
    }

    @Override
    public boolean keyTyped(char character) {
        return false;
    }

    @Override
    public boolean touchDown(int screenX, int screenY, int pointer, int button) {
        return false;
    }

    @Override
    public boolean touchUp(int screenX, int screenY, int pointer, int button) {
        return false;
    }

    @Override
    public boolean touchDragged(int screenX, int screenY, int pointer) {
        return false;
    }

    @Override
    public boolean mouseMoved(int screenX, int screenY) {
        return false;
    }

    @Override
    public boolean scrolled(int amount) {
        return false;
    }
}
