package com.tianyi.zhang.multiplayer.snake.states.client;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.utils.Align;
import com.badlogic.gdx.utils.viewport.ScreenViewport;
import com.esotericsoftware.kryonet.Connection;
import com.esotericsoftware.kryonet.Listener;
import com.kotcrab.vis.ui.widget.VisImage;
import com.kotcrab.vis.ui.widget.VisLabel;
import com.kotcrab.vis.ui.widget.VisTable;
import com.tianyi.zhang.multiplayer.snake.App;
import com.tianyi.zhang.multiplayer.snake.helpers.AssetManager;
import com.tianyi.zhang.multiplayer.snake.helpers.Utils;
import com.tianyi.zhang.multiplayer.snake.states.GameState;

public class LookForServerState extends GameState {
    private static final String TAG = LookForServerState.class.getCanonicalName();

    private final Stage stage;
    private final VisTable table;
    private final VisImage imgTitle;
    private final VisLabel lblInfo;

    public LookForServerState(App app) {
        super(app);

        stage = new Stage();
        stage.setViewport(new ScreenViewport(stage.getCamera()));

        table = new VisTable(true);
        table.setFillParent(true);
        stage.addActor(table);

        imgTitle = new VisImage(AssetManager.INSTANCE.getTitleTexture());
        table.add(imgTitle).row();

        lblInfo = new VisLabel("Looking for a host...");
        lblInfo.setAlignment(Align.center);
        table.add(lblInfo).row();

        Gdx.input.setInputProcessor(stage);

        _app.getAgent().lookForServer(new Listener() {
            @Override
            public void connected(Connection connection) {
                lblInfo.setText("Joined successfully\nWaiting for host to start the game...");
            }

            @Override
            public void disconnected(Connection connection) {
                Gdx.app.postRunnable(new Runnable() {
                    @Override
                    public void run() {
                        _app.gotoErrorScreen("NOOO! We lost contact with the host!");
                    }
                });
            }

            @Override
            public void received(Connection connection, Object object) {
                if (object instanceof byte[]) {
                    // The MainGameState instance must be created by Gdx, because it initializes an OpenGL renderer,
                    // which must be created on the thread that has an OpenGL context.
                    final int id = connection.getID();
                    final long receivedNanoTime = Utils.getNanoTime();
                    final byte[] update = (byte[]) object;
                    Gdx.app.postRunnable(new Runnable() {
                        @Override
                        public void run() {
                            _app.setState(new MainGameState(_app, id, receivedNanoTime, update));
                        }
                    });
                }
            }
        }, new Runnable() {
            @Override
            public void run() {
                _app.gotoErrorScreen("Having trouble finding a host\nIs there someone hosting?\nPlease ensure you are playing on home Wi-fi");
            }
        });
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
