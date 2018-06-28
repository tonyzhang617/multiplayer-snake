package com.tianyi.zhang.multiplayer.snake.states.server;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.InputProcessor;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.esotericsoftware.kryonet.Connection;
import com.esotericsoftware.kryonet.Listener;
import com.tianyi.zhang.multiplayer.snake.App;
import com.tianyi.zhang.multiplayer.snake.agents.Server;
import com.tianyi.zhang.multiplayer.snake.agents.messages.Packet;
import com.tianyi.zhang.multiplayer.snake.elements.ServerSnapshot;
import com.tianyi.zhang.multiplayer.snake.elements.Snake;
import com.tianyi.zhang.multiplayer.snake.helpers.Constants;
import com.tianyi.zhang.multiplayer.snake.helpers.Utils;
import com.tianyi.zhang.multiplayer.snake.states.GameState;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

public class SVMainGameState extends GameState implements InputProcessor {
    private static final String TAG = SVMainGameState.class.getCanonicalName();
    private final ServerSnapshot serverSnapshot;
    private final ScheduledExecutorService executor;
    private final long startTimestamp;

    private final ShapeRenderer renderer;

    /**
     *
     * @param app a reference to App
     * @param connectionIds a sorted, ascending list of integers representing client IDs
     */
    public SVMainGameState(App app, List<Integer> connectionIds) {
        super(app);
        Gdx.input.setInputProcessor(this);
        Gdx.graphics.setContinuousRendering(false);

        int[] tmpIds = new int[connectionIds.size()+1];
        tmpIds[0] = 0;
        for (int i = 0; i < connectionIds.size(); i++) {
            tmpIds[i+1] = connectionIds.get(i);
        }

        _app.getAgent().send(buildFirstPacket(tmpIds));
        startTimestamp = Utils.getNanoTime();
        serverSnapshot = new ServerSnapshot(startTimestamp, tmpIds);
        _app.getAgent().setListener(new Listener() {
            @Override
            public void received(Connection connection, Object object) {
                if (object instanceof byte[]) {
                    serverSnapshot.onClientUpdate(Server.parseReceived(object));
                }
            }
        });
        executor = Executors.newSingleThreadScheduledExecutor();
        executor.scheduleAtFixedRate(new Runnable() {
            @Override
            public void run() {
                try {
                    if (serverSnapshot.update()) {
                        Gdx.graphics.requestRendering();
                    }
                    _app.getAgent().send(serverSnapshot.buildPacket());
                } catch (Exception e) {
                    Gdx.app.error(TAG, "Error encountered inside scheduled task: ", e);
                }
            }
        }, 0, 100, TimeUnit.MILLISECONDS);

        Gdx.app.debug(TAG, "Server main game loaded");
        renderer = new ShapeRenderer();
    }

    private Packet.Update buildFirstPacket(int[] snakeIds) {
        Packet.Update.Builder builder = Packet.Update.newBuilder();
        int id = 0;
        for (int index = 0; index < snakeIds.length; ++index) {
            while (id <= snakeIds[index]) {
                Packet.Update.PSnake.Builder snakeBuilder = Packet.Update.PSnake.newBuilder();
                // TODO: add actual coordinates
                snakeBuilder.setId(id).addAllCoords(new ArrayList());
                snakeBuilder.setLastInput(Packet.Update.PInput.newBuilder().setId(0).setDirection(Constants.RIGHT).setTimestamp(0).setStep(0));
                builder.addSnakes(snakeBuilder);
                id += 1;
            }
        }
        builder.setState(Packet.Update.PState.READY).setTimestamp(0).setVersion(0);
        return builder.build();
    }

    @Override
    public void render(float delta) {
        Gdx.gl.glClearColor(0, 0, 1, 1);
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);

        Snake[] snakes = serverSnapshot.getSnakes();
        for (Snake snake : snakes) {
            Gdx.app.debug(TAG, snake.toString());
        }

        renderer.setColor(Color.WHITE);
        renderer.begin(ShapeRenderer.ShapeType.Filled);

        for (int s = 0; s < snakes.length; ++s) {
            List<Integer> coords = snakes[s].COORDS;
            for (int c = 0; c < coords.size() / 2; ++c) {
                renderer.rect(coords.get(2*c)*Constants.UNIT_WIDTH, coords.get(2*c+1)*Constants.UNIT_HEIGHT, Constants.UNIT_WIDTH, Constants.UNIT_HEIGHT);
            }
        }
        renderer.end();
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
            serverSnapshot.onServerInput(Constants.LEFT);
        } else if (keycode == Input.Keys.UP) {
            serverSnapshot.onServerInput(Constants.UP);
        } else if (keycode == Input.Keys.RIGHT) {
            serverSnapshot.onServerInput(Constants.RIGHT);
        } else if (keycode == Input.Keys.DOWN) {
            serverSnapshot.onServerInput(Constants.DOWN);
        }
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
