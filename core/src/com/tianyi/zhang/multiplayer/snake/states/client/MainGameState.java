package com.tianyi.zhang.multiplayer.snake.states.client;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.InputProcessor;
import com.badlogic.gdx.graphics.GL20;
import com.esotericsoftware.kryonet.Connection;
import com.esotericsoftware.kryonet.Listener;
import com.tianyi.zhang.multiplayer.snake.App;
import com.tianyi.zhang.multiplayer.snake.agents.Client;
import com.tianyi.zhang.multiplayer.snake.agents.messages.Packet;
import com.tianyi.zhang.multiplayer.snake.helpers.Constants;
import com.tianyi.zhang.multiplayer.snake.states.GameState;

import java.util.concurrent.atomic.AtomicInteger;

public class MainGameState extends GameState implements InputProcessor {
    private static final String TAG = MainGameState.class.getCanonicalName();
    private AtomicInteger direction;
    private volatile boolean serverReady = false;
    private final int connectionId;

    public MainGameState(App app, int id) {
        super(app);
        connectionId = id;

        Gdx.input.setInputProcessor(this);
        direction = new AtomicInteger(Constants.RIGHT);
        _app.getAgent().setListener(new Listener() {
            @Override
            public void received(Connection connection, Object object) {
                if (object instanceof byte[]) {
                    Packet.Update update = Client.parseReceived(object);
                    if (update.hasServerState() && update.getServerState().getNumber() == Packet.Update.ServerState.SERVER_READY_VALUE) {
                        serverReady = true;
                    }
                }
            }
        });
        Gdx.app.debug(TAG, "Main game loaded");
    }


    @Override
    public void render(float delta) {
        if (!serverReady) {
            super.render(delta);
        } else {
            Gdx.gl.glClearColor(0, 0, 1, 1);
            Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);
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
            direction.set(Constants.LEFT);
            _app.getAgent().send(Packet.Update.newBuilder().addSnakes(Packet.Update.Snake.newBuilder().setDir(Constants.LEFT).build()).build());
        } else if (keycode == Input.Keys.UP) {
            direction.set(Constants.UP);
            _app.getAgent().send(Packet.Update.newBuilder().addSnakes(Packet.Update.Snake.newBuilder().setDir(Constants.UP).build()).build());
        } else if (keycode == Input.Keys.RIGHT) {
            direction.set(Constants.RIGHT);
            _app.getAgent().send(Packet.Update.newBuilder().addSnakes(Packet.Update.Snake.newBuilder().setDir(Constants.RIGHT).build()).build());
        } else if (keycode == Input.Keys.DOWN) {
            direction.set(Constants.DOWN);
            _app.getAgent().send(Packet.Update.newBuilder().addSnakes(Packet.Update.Snake.newBuilder().setDir(Constants.DOWN).build()).build());
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
