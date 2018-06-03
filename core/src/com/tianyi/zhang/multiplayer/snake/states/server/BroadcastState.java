package com.tianyi.zhang.multiplayer.snake.states.server;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.utils.ClickListener;
import com.esotericsoftware.kryonet.Connection;
import com.esotericsoftware.kryonet.Listener;
import com.kotcrab.vis.ui.widget.VisTable;
import com.kotcrab.vis.ui.widget.VisTextButton;
import com.tianyi.zhang.multiplayer.snake.App;
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
        Gdx.input.setInputProcessor(stage);

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

        // Start broadcasting
        try {
            this.app.getAgent().broadcast(new Listener() {
                @Override
                public void connected(Connection connection) {
                    super.connected(connection);
                    Gdx.app.debug("BROADCAST", "started");
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
                    synchronized (BroadcastState.this) {
                        while (!shouldGameStart) {
                            try {
                                BroadcastState.this.wait();
                            } catch (InterruptedException e) {
                                e.printStackTrace();
                            }
                        }

                        Gdx.app.debug("GAME START", "The game should start now");
                    }
                }
            });
        } catch (IOException e) {
            // TODO: Display error message
            Gdx.app.error("SERVER BROADCAST ERROR", e.getMessage());
        }
    }

    @Override
    public void render() {
        super.render();

        stage.act();
        stage.draw();
    }

    @Override
    public void input() {

    }
}
