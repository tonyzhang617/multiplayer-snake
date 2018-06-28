package com.tianyi.zhang.multiplayer.snake.states.client;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.InputProcessor;
import com.badlogic.gdx.graphics.GL20;
import com.esotericsoftware.kryonet.Connection;
import com.esotericsoftware.kryonet.FrameworkMessage;
import com.esotericsoftware.kryonet.Listener;
import com.tianyi.zhang.multiplayer.snake.App;
import com.tianyi.zhang.multiplayer.snake.agents.Client;
import com.tianyi.zhang.multiplayer.snake.agents.messages.Packet;
import com.tianyi.zhang.multiplayer.snake.elements.ClientSnapshot;
import com.tianyi.zhang.multiplayer.snake.elements.Snake;
import com.tianyi.zhang.multiplayer.snake.helpers.Constants;
import com.tianyi.zhang.multiplayer.snake.helpers.Utils;
import com.tianyi.zhang.multiplayer.snake.states.GameState;

import java.util.List;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

public class MainGameState extends GameState implements InputProcessor {
    private static final String TAG = MainGameState.class.getCanonicalName();
    private final ScheduledExecutorService executor;
    private final int clientId;
    private volatile int roundTripMs;
    private final ClientSnapshot snapshot;

    public MainGameState(App app, final int id) {
        super(app);
        snapshot = new ClientSnapshot(id);
        clientId = id;

        executor = Executors.newScheduledThreadPool(2);

        Gdx.input.setInputProcessor(this);
        Gdx.graphics.setContinuousRendering(false);
        _app.getAgent().updateRoundTripTime();
        _app.getAgent().setListener(new Listener() {
            @Override
            public void received(Connection connection, Object object) {
                if (object instanceof FrameworkMessage.Ping) {
                    roundTripMs = _app.getAgent().getRoundTripTime();
                } else if (object instanceof byte[]) {
                    long startTimestamp = Utils.getNanoTime() - TimeUnit.MILLISECONDS.toNanos(roundTripMs) / 2;
                    Packet.Update update = Client.parseReceived(object);
                    if (update.getState() == Packet.Update.PState.READY) {
                        List<Packet.Update.PSnake> pSnakes = update.getSnakesList();
                        // TODO: Pass snakes as argument to constructor of ClientSnapshot
                        int[] snakeIds = new int[pSnakes.size()];
                        for (int i = 0; i < pSnakes.size(); ++i) {
                            snakeIds[i] = pSnakes.get(i).getId();
                        }
                        snapshot.init(startTimestamp, snakeIds);
                        executor.scheduleAtFixedRate(new Runnable() {
                            @Override
                            public void run() {
                                try {
                                    if (snapshot.update()) {
                                        Gdx.graphics.requestRendering();
                                    }
                                    com.tianyi.zhang.multiplayer.snake.elements.Input[] inputs = snapshot.getNewInputs();
                                    if (inputs.length > 0) {
                                        Packet.Update.Builder builder = Packet.Update.newBuilder();
                                        builder.setState(Packet.Update.PState.GAME_IN_PROGRESS).setSnakeId(id);
                                        for (com.tianyi.zhang.multiplayer.snake.elements.Input tmpInput : inputs) {
                                            builder.addInputs(Packet.Update.PInput.newBuilder().setId(tmpInput.id).setDirection(tmpInput.direction).setTimestamp(tmpInput.timestamp).setStep(tmpInput.step));
                                        }
                                        _app.getAgent().send(builder.build());
                                    }
                                } catch (Exception e) {
                                    Gdx.app.error(TAG, "Error encountered while running scheduled rendering task: ", e);
                                }
                            }
                        }, 0, 30, TimeUnit.MILLISECONDS);
                    } else {
                        snapshot.onServerUpdate(update);
                    }
                }
            }
        });
        Gdx.app.debug(TAG, "Main game loaded");
    }

    @Override
    public void render(float delta) {
        Gdx.gl.glClearColor(0, 0, 1, 1);
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);

        Snake[] snakes = snapshot.getSnakes();

        for (Snake snake : snakes) {
            Gdx.app.debug(TAG, snake.toString());
        }
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
        Gdx.app.debug(TAG, "Keycode " + keycode + " pressed");
        if (keycode == Input.Keys.LEFT) {
            snapshot.onClientInput(Constants.LEFT);
        } else if (keycode == Input.Keys.UP) {
            snapshot.onClientInput(Constants.UP);
        } else if (keycode == Input.Keys.RIGHT) {
            snapshot.onClientInput(Constants.RIGHT);
        } else if (keycode == Input.Keys.DOWN) {
            snapshot.onClientInput(Constants.DOWN);
        }
        return true;
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
