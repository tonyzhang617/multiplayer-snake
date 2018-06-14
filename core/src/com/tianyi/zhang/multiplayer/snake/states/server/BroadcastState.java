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
import com.tianyi.zhang.multiplayer.snake.helpers.Constants;
import com.tianyi.zhang.multiplayer.snake.states.GameState;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

import static com.tianyi.zhang.multiplayer.snake.helpers.Constants.MOVE_EVERY_MS;

public class BroadcastState extends GameState {
    private final Object connectionIdsLock;
    private final List<Integer> connectionIds;
    private static final String TAG = BroadcastState.class.getCanonicalName();

    private long nsSinceLastFrame;

    // UI elements
    private Stage stage;
    private VisTable table;
    private VisTextButton btnStart;

    public BroadcastState(App app) {
        super(app);

        connectionIdsLock = new Object();
        connectionIds = new ArrayList<Integer>();

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
                Gdx.app.debug(TAG, "START GAME button clicked");
                synchronized (connectionIdsLock) {
                    if (connectionIds.size() > 0) {
                        _app.pushState(new SVMainGameState(_app, connectionIds));
                    }
                }
            }
        });
        table.row();
        table.add(btnStart);
        Gdx.input.setInputProcessor(stage);

        // Start broadcasting
        try {
            _app.getAgent().broadcast(new Listener() {
                @Override
                public void connected(Connection connection) {
                    synchronized (connectionIdsLock) {
                        connectionIds.add(new Integer(connection.getID()));
                    }
                }

                @Override
                public void disconnected(Connection connection) {
                    synchronized (connectionIdsLock) {
                        connectionIds.remove(new Integer(connection.getID()));
                    }
                }
            });
        } catch (IOException e) {
            // TODO: Display error message
            Gdx.app.error(TAG, e.getMessage());
        }

//        nsSinceLastFrame = System.nanoTime();
    }

    @Override
    public void render(float delta) {
        super.render(delta);
        stage.act();
        stage.draw();
//        int msSinceLastFrame = (int) TimeUnit.NANOSECONDS.toMillis(System.nanoTime() - nsSinceLastFrame);
//        int frames = msSinceLastFrame / MOVE_EVERY_MS;
//        nsSinceLastFrame += frames * TimeUnit.MILLISECONDS.toNanos(Constants.MOVE_EVERY_MS);
//        Gdx.app.log(TAG, String.valueOf(frames));
//        Gdx.app.log(TAG, String.valueOf(nsSinceLastFrame));
//        if (frames > 0) {
//            Gdx.app.log(TAG, String.valueOf(msSinceLastFrame));
//            Gdx.app.log(TAG, "Before " + nsSinceLastFrame);
//            nsSinceLastFrame -= TimeUnit.MILLISECONDS.toNanos(Constants.MOVE_EVERY_MS) * frames;
//            Gdx.app.log(TAG, "After " + nsSinceLastFrame);
//
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
        stage.dispose();
    }
}
