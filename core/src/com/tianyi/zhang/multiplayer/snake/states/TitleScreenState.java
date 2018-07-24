package com.tianyi.zhang.multiplayer.snake.states;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.utils.ClickListener;
import com.badlogic.gdx.utils.viewport.ScreenViewport;
import com.kotcrab.vis.ui.widget.VisImage;
import com.kotcrab.vis.ui.widget.VisTable;
import com.kotcrab.vis.ui.widget.VisTextButton;
import com.tianyi.zhang.multiplayer.snake.App;
import com.tianyi.zhang.multiplayer.snake.helpers.AssetManager;
import com.tianyi.zhang.multiplayer.snake.states.client.LookForServerState;
import com.tianyi.zhang.multiplayer.snake.states.server.BroadcastState;

public class TitleScreenState extends GameState {
    private static final String TAG = TitleScreenState.class.getCanonicalName();

    private final Stage stage;
    private final VisTable table;
    private final VisImage imgTitle;
    private final VisTextButton btnHost;
    private final VisTextButton btnJoin;
    private final VisTextButton btnAcknowledgements;

    public TitleScreenState(App app) {
        super(app);
        stage = new Stage();
        stage.setViewport(new ScreenViewport(stage.getCamera()));
        table = new VisTable(true);
        table.setFillParent(true);
        stage.addActor(table);
        imgTitle = new VisImage(AssetManager.INSTANCE.getTitleTexture());
        table.add(imgTitle).row();
        btnHost = new VisTextButton("Host a Game");
        btnHost.addListener(new ClickListener() {
            @Override
            public void clicked(InputEvent event, float x, float y) {
                Gdx.app.postRunnable(new Runnable() {
                    @Override
                    public void run() {
                        _app.initAgent(true);
                        _app.pushState(new BroadcastState(_app));
                    }
                });
            }
        });
        table.add(btnHost).row();
        btnJoin = new VisTextButton("Join a Game");
        btnJoin.addListener(new ClickListener() {
            @Override
            public void clicked(InputEvent event, float x, float y) {
                Gdx.app.postRunnable(new Runnable() {
                    @Override
                    public void run() {
                        _app.initAgent(false);
                        _app.pushState(new LookForServerState(_app));
                    }
                });
            }
        });
        table.add(btnJoin).row();
        btnAcknowledgements = new VisTextButton("More Info");
        btnAcknowledgements.addListener(new ClickListener() {
            @Override
            public void clicked(InputEvent event, float x, float y) {
                _app.pushState(new InfoState(_app));
            }
        });
        table.add(btnAcknowledgements).row();

        Gdx.input.setInputProcessor(stage);
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
        Gdx.input.setInputProcessor(stage);
    }

    @Override
    public void hide() {

    }

    @Override
    public void dispose() {
        stage.dispose();
    }
}
