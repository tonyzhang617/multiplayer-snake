package com.tianyi.zhang.multiplayer.snake.states;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.utils.ClickListener;
import com.badlogic.gdx.utils.Align;
import com.badlogic.gdx.utils.viewport.ScreenViewport;
import com.kotcrab.vis.ui.widget.VisLabel;
import com.kotcrab.vis.ui.widget.VisTable;
import com.kotcrab.vis.ui.widget.VisTextButton;
import com.tianyi.zhang.multiplayer.snake.App;

public class ErrorState extends GameState {
    private static final String TAG = TitleScreenState.class.getCanonicalName();

    private final Stage stage;
    private final VisTable table;
    private final VisLabel lblError;
    private final VisTextButton btnToTitleScreen;

    public ErrorState(App app, String errorMessage) {
        super(app);

        stage = new Stage();
        stage.setViewport(new ScreenViewport(stage.getCamera()));
        table = new VisTable(true);
        table.setFillParent(true);
        stage.addActor(table);
        lblError = new VisLabel(errorMessage);
        lblError.setAlignment(Align.center);
        table.add(lblError).row();
        btnToTitleScreen = new VisTextButton("Return to main screen");
        btnToTitleScreen.addListener(new ClickListener() {
            @Override
            public void clicked(InputEvent event, float x, float y) {
                _app.gotoTitleScreen();
            }
        });
        table.add(btnToTitleScreen).row();

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

    }

    @Override
    public void hide() {

    }

    @Override
    public void dispose() {
        stage.dispose();
    }
}
