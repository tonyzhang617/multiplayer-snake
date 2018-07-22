package com.tianyi.zhang.multiplayer.snake.states.server;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.utils.ClickListener;
import com.badlogic.gdx.utils.viewport.ExtendViewport;
import com.esotericsoftware.kryonet.Connection;
import com.esotericsoftware.kryonet.Listener;
import com.kotcrab.vis.ui.widget.VisLabel;
import com.kotcrab.vis.ui.widget.VisTable;
import com.kotcrab.vis.ui.widget.VisTextButton;
import com.tianyi.zhang.multiplayer.snake.App;
import com.tianyi.zhang.multiplayer.snake.states.GameState;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class BroadcastState extends GameState {
    private final Object connectionIdsLock;
    private final List<Integer> connectionIds;
    private static final String TAG = BroadcastState.class.getCanonicalName();

    // UI elements
    private Stage stage;
    private VisTable table;
    private VisTextButton btnStart;
    private VisLabel lblPlayerCount;

    private static final String WAITING_FOR_PLAYERS = "Waiting for other snakes to join the game...";
    private static final String PLAYERS_CONNECTED_FORMAT = "%d other snakes have joined the game";

    public BroadcastState(App app) {
        super(app);

        connectionIdsLock = new Object();
        connectionIds = new ArrayList<Integer>();

        // Set up UI element layout
        float w = Gdx.graphics.getWidth(), h = Gdx.graphics.getHeight();
        stage = new Stage();
        stage.setViewport(new ExtendViewport(w, h));

        table = new VisTable(true);
        table.setFillParent(true);
        stage.addActor(table);

        btnStart = new VisTextButton("Start the game");
        btnStart.addListener(new ClickListener() {
            @Override
            public void clicked(InputEvent event, float x, float y) {
                super.clicked(event, x, y);
                Gdx.app.debug(TAG, "START GAME button clicked");
                synchronized (connectionIdsLock) {
                    if (connectionIds.size() > 0) {
                        _app.pushState(new SVMainGameState(_app, connectionIds));
                    }
                }
            }
        });
        btnStart.setDisabled(true);
        table.add(btnStart).row();
        lblPlayerCount = new VisLabel(WAITING_FOR_PLAYERS);
        lblPlayerCount.setScale(0.5f);
        table.add(lblPlayerCount).row();
        Gdx.input.setInputProcessor(stage);

        // Start broadcasting
        try {
            _app.getAgent().broadcast(new Listener() {
                @Override
                public void connected(Connection connection) {
                    synchronized (connectionIdsLock) {
                        connectionIds.add(Integer.valueOf(connection.getID()));
                        if (connectionIds.size() == 0) {
                            btnStart.setDisabled(true);
                            lblPlayerCount.setText(WAITING_FOR_PLAYERS);
                        } else {
                            btnStart.setDisabled(false);
                            lblPlayerCount.setText(String.format(PLAYERS_CONNECTED_FORMAT, connectionIds.size()));
                        }
                    }
                }

                @Override
                public void disconnected(Connection connection) {
                    synchronized (connectionIdsLock) {
                        connectionIds.remove(Integer.valueOf(connection.getID()));
                        if (connectionIds.size() == 0) {
                            btnStart.setDisabled(true);
                            lblPlayerCount.setText(WAITING_FOR_PLAYERS);
                        } else {
                            btnStart.setDisabled(false);
                            lblPlayerCount.setText(String.format(PLAYERS_CONNECTED_FORMAT, connectionIds.size()));
                        }
                    }
                }
            });
        } catch (IOException e) {
            // TODO: Display error message
            Gdx.app.error(TAG, e.getMessage());
        }
    }

    @Override
    public void render(float delta) {
        super.render(delta);
        stage.act();
        stage.draw();
    }

    @Override
    public void show() {

    }

    @Override
    public void resize(int width, int height) {
        stage.getViewport().update(width, height, true);
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
