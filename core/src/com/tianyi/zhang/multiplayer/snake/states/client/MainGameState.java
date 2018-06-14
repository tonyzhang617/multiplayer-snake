package com.tianyi.zhang.multiplayer.snake.states.client;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.InputProcessor;
import com.badlogic.gdx.graphics.GL20;
import com.esotericsoftware.kryonet.Connection;
import com.esotericsoftware.kryonet.FrameworkMessage;
import com.esotericsoftware.kryonet.Listener;
import com.tianyi.zhang.multiplayer.snake.App;
import com.tianyi.zhang.multiplayer.snake.elements.Snapshot;
import com.tianyi.zhang.multiplayer.snake.helpers.Utils;
import com.tianyi.zhang.multiplayer.snake.states.GameState;

import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.TimeUnit;

public class MainGameState extends GameState implements InputProcessor {
    private static final String TAG = MainGameState.class.getCanonicalName();
    private final List<Snapshot> snapshots;
    private final Object snapshotsLock;
    private volatile boolean serverReady = false;
    private final int snakeId;
    private long lastUpdateTime;
    private volatile int roundTripMs;

    public MainGameState(App app, int id) {
        super(app);
        snapshots = new LinkedList<Snapshot>();
        snakeId = id;

        Gdx.input.setInputProcessor(this);
        _app.getAgent().updateRoundTripTime();
        _app.getAgent().setListener(new Listener() {
            @Override
            public void received(Connection connection, Object object) {
                if (object instanceof FrameworkMessage.Ping) {
                    roundTripMs = _app.getAgent().getRoundTripTime();
                } else if (object instanceof byte[]) {
                    Gdx.app.debug(TAG, String.valueOf(Utils.getNanoTime() - TimeUnit.MILLISECONDS.toNanos(roundTripMs) / 2));
                }
            }
        });
//        _app.getAgent().setListener(new Listener() {
//            @Override
//            public void received(Connection connection, Object object) {
//                if (object instanceof byte[]) {
//                    Packet.Update update = Client.parseReceived(object);
//                    Packet.Update.PState serverState = update.getState();
//
//                    if (serverState == Packet.Update.PState.READY) {
//                        List<Packet.Update.PSnake> pSnakes = update.getSnapshots(0).getSnakesList();
//                        int[] snakeIds = new int[pSnakes.size()];
//                        for (int i = 0; i < pSnakes.size(); ++i) {
//                            snakeIds[i] = pSnakes.get(i).getId();
//                        }
//                        synchronized (snapshotsLock) {
//                            snapshots.add(new Snapshot(snakeIds));
//                            for (int i = 0; i < FUTURE_STATES; ++i) {
//                                snapshots.add(snapshots.get(i).next());
//                            }
//                        }
//
//                        ScheduledExecutorService executor = Executors.newSingleThreadScheduledExecutor();
//                        executor.scheduleAtFixedRate(new Runnable() {
//                            @Override
//                            public void run() {
//                                sendUpdate();
//                            }
//                        }, Constants.CLIENT_SEND_EVERY_MS, Constants.CLIENT_SEND_EVERY_MS, TimeUnit.MILLISECONDS);
//
//                        serverReady = true;
//                    } else if (serverState == Packet.Update.PState.GAME_IN_PROGRESS && serverReady) {
//                        processServerPacket(update);
//                    }
//                }
//            }
//        });

        snapshotsLock = new Object();
        lastUpdateTime = 0;

        Gdx.app.debug(TAG, "Main game loaded");
    }

//    private Snapshot getCurrentSnapshot() {
//        synchronized (snapshotsLock) {
//            return snapshots.get(snapshots.size() - FUTURE_STATES);
//        }
//    }

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
//    private void sendUpdate() {
//        Snapshot currentSnapshot = getCurrentSnapshot();
//        Snake thisSnake = currentSnapshot.getSnakeById(snakeId);
//        _app.getAgent().send(Packet.Update.newBuilder().setState(Packet.Update.PState.GAME_IN_PROGRESS).addSnapshots(
//                Packet.Update.PSnapshot.newBuilder().setStep(currentSnapshot.getStep()).addSnakes(
//                        Packet.Update.PSnake.newBuilder().setId(snakeId).setInputId(thisSnake.INPUT_ID)
//                                .setDirection(thisSnake.DIRECTION).build()).build()).build());
//    }
//
//    private void processServerPacket(Packet.Update update) {
//        Gdx.app.debug(TAG, "Update from server received");
//        synchronized (snapshotsLock) {
//            int size = snapshots.size(), firstStep = snapshots.get(0).getStep();
//            List<Packet.Update.PSnapshot> pSnapshots = update.getSnapshotsList();
//            int pSize = pSnapshots.size(), pFirstStep = pSnapshots.get(0).getStep();
//            int offset = pFirstStep - firstStep, startIndex = offset >= 0 ? offset : 0;
//            for (int i = startIndex, p = 0; i < size && p < pSize; ++i, ++p) {
//                // Compare Snapshots with PSnapshots
//                Snapshot snapshot = snapshots.get(i);
//                Packet.Update.PSnapshot pSnapshot = pSnapshots.get(p);
//                boolean shouldUpdate = false;
//                for (int j = 0; j < pSnapshot.getSnakesCount(); ++j) {
//                    Packet.Update.PSnake pSnake = pSnapshot.getSnakes(j);
//                    Snake snake = snapshot.getSnakeById(j);
//                    if (snake.INPUT_ID < pSnake.getInputId()) {
//                        snapshot.updateDirection(j, pSnake.getDirection(), pSnake.getInputId());
//                        if (snake.DIRECTION != pSnake.getDirection()) {
//                            shouldUpdate = true;
//                        }
//                    }
//                }
//                if (shouldUpdate && i+1 < size) {
//                    // TODO: Implement Client Prediction with an input queue
//                    snapshots.set(i+1, snapshot.next());
//                }
//            }
//        }
//    }

    @Override
    public void render(float delta) {
//        if (TimeUnit.NANOSECONDS.toMillis(System.nanoTime() - lastUpdateTime) > MOVE_EVERY_MS || lastUpdateTime == 0) {
//            if (!serverReady) {
//                super.render(delta);
//            } else {
//                nextStep();
                Gdx.gl.glClearColor(0, 0, 1, 1);
                Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);

//                Snake snake = getCurrentSnapshot().getSnakeById(snakeId);
//                StringBuilder builder = new StringBuilder();
//                for (Integer i : snake.COORDS) {
//                    builder.append(i.intValue());
//                    builder.append(' ');
//                }
//                Gdx.app.debug(TAG, builder.toString());
//            }
//            lastUpdateTime = System.nanoTime();
//        }
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
//        Gdx.app.debug(TAG, "Keycode " + keycode + " pressed");
//        if (keycode == Input.Keys.LEFT) {
//            direction.set(Constants.LEFT);
//            _app.getAgent().send(Packet.Update.newBuilder().addSnakes(Packet.Update.Snake.newBuilder().setDir(Constants.LEFT).build()).build());
//        } else if (keycode == Input.Keys.UP) {
//            direction.set(Constants.UP);
//            _app.getAgent().send(Packet.Update.newBuilder().addSnakes(Packet.Update.Snake.newBuilder().setDir(Constants.UP).build()).build());
//        } else if (keycode == Input.Keys.RIGHT) {
//            direction.set(Constants.RIGHT);
//            _app.getAgent().send(Packet.Update.newBuilder().addSnakes(Packet.Update.Snake.newBuilder().setDir(Constants.RIGHT).build()).build());
//        } else if (keycode == Input.Keys.DOWN) {
//            direction.set(Constants.DOWN);
//            _app.getAgent().send(Packet.Update.newBuilder().addSnakes(Packet.Update.Snake.newBuilder().setDir(Constants.DOWN).build()).build());
//        }
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
