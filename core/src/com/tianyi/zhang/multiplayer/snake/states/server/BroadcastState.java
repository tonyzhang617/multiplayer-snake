package com.tianyi.zhang.multiplayer.snake.states.server;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.utils.ClickListener;
import com.esotericsoftware.kryonet.Connection;
import com.esotericsoftware.kryonet.Listener;
import com.kotcrab.vis.ui.widget.VisTable;
import com.kotcrab.vis.ui.widget.VisTextButton;
import com.tianyi.zhang.multiplayer.snake.App;
import com.tianyi.zhang.multiplayer.snake.agents.messages.Packet;
import com.tianyi.zhang.multiplayer.snake.states.GameState;

import java.io.IOException;

public class BroadcastState extends GameState {
    private volatile boolean shouldGameStart = false;

    // UI elements
    private Stage stage;
    private VisTable table;
    private VisTextButton btnStart;

    public BroadcastState(App app) {
        super(app);

        // Set up UI element layout
        stage = new Stage();

        table = new VisTable(true);
        table.setFillParent(true);
        stage.addActor(table);

        btnStart = new VisTextButton("Start the game");
        btnStart.addListener(new ClickListener() {
            @Override
            public void clicked(InputEvent event, float x, float y) {
                super.clicked(event, x, y);
                Gdx.app.debug("START GAME", "Button clicked");

                synchronized (BroadcastState.this) {
                    shouldGameStart = true;
                    BroadcastState.this.notify();
                }
            }
        });
        table.row();
        table.add(btnStart);
        Gdx.input.setInputProcessor(stage);

        // Start broadcasting
        try {
            this.app.getAgent().broadcast(new Listener() {
                @Override
                public void connected(Connection connection) {
                    super.connected(connection);
                    Gdx.app.debug("BROADCAST", "started");
                    synchronized (BroadcastState.this) {
                        while (!shouldGameStart) {
                            try {
                                BroadcastState.this.wait();
                            } catch (InterruptedException e) {
                                e.printStackTrace();
                            }
                        }
                    }
                    Gdx.app.debug("GAME START", "The game should start now");

                    Packet.Update update = Packet.Update.newBuilder()
                            .setServerState(Packet.Update.ServerState.SERVER_READY).build();
                    BroadcastState.this.app.getAgent().send(update);
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

                    Gdx.app.debug("RECEIVED OBJECT", object.toString());
                }
            });
        } catch (IOException e) {
            // TODO: Display error message
            Gdx.app.error("SERVER BROADCAST ERROR", e.getMessage());
        }
    }

    @Override
    public void render(float delta) {
//        super.render(delta);
        Gdx.gl.glClearColor(1, 0, 0, 1);
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);
        stage.act();
        stage.draw();
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
        stage.dispose();
    }
}
