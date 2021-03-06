package com.tianyi.zhang.multiplayer.snake.states.server;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.utils.ClickListener;
import com.badlogic.gdx.utils.viewport.ScreenViewport;
import com.esotericsoftware.kryonet.Connection;
import com.esotericsoftware.kryonet.Listener;
import com.kotcrab.vis.ui.widget.VisImage;
import com.kotcrab.vis.ui.widget.VisLabel;
import com.kotcrab.vis.ui.widget.VisTable;
import com.kotcrab.vis.ui.widget.VisTextButton;
import com.tianyi.zhang.multiplayer.snake.App;
import com.tianyi.zhang.multiplayer.snake.helpers.AssetManager;
import com.tianyi.zhang.multiplayer.snake.states.GameState;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;

public class BroadcastState extends GameState {
    private final Object connectionIdsLock;
    private final List<Integer> connectionIds;
    private static final String TAG = BroadcastState.class.getCanonicalName();

    // UI elements
    private final Stage stage;
    private final VisTable table;
    private final VisImage imgTitle;
    private final VisTextButton btnStart;
    private final VisLabel lblPlayerCount;

    private final AtomicBoolean success;

    private static final String WAITING_FOR_PLAYERS = "Waiting for other snakes to join the game...";
    private static final String PLAYERS_CONNECTED_FORMAT = "%d other snakes have joined the game";

    public BroadcastState(App app) {
        super(app);

        connectionIdsLock = new Object();
        connectionIds = new ArrayList<Integer>();

        // Set up UI element layout
        stage = new Stage();
        stage.setViewport(new ScreenViewport(stage.getCamera()));

        table = new VisTable(true);
        table.setFillParent(true);
        stage.addActor(table);

        imgTitle = new VisImage(AssetManager.INSTANCE.getTitleTexture());
        table.add(imgTitle).row();

        btnStart = new VisTextButton("Start the game");
        btnStart.addListener(new ClickListener() {
            @Override
            public void clicked(InputEvent event, float x, float y) {
                super.clicked(event, x, y);
                Gdx.app.debug(TAG, "START GAME button clicked");
                synchronized (connectionIdsLock) {
                    if (connectionIds.size() > 0) {
                        _app.setState(new SVMainGameState(_app, connectionIds));
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

        success = new AtomicBoolean();

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

            success.set(true);
        } catch (IOException e) {
            Gdx.app.error(TAG, e.getMessage());
            success.set(false);
        }
    }

    @Override
    public void render(float delta) {
        super.render(delta);
        stage.act();
        stage.draw();

        if (!success.get()) {
            _app.gotoErrorScreen("Cannot host game\nIs there someone already hosting a game?");
        }
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
