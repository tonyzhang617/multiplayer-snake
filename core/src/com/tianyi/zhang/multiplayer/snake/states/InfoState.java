package com.tianyi.zhang.multiplayer.snake.states;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.utils.ClickListener;
import com.badlogic.gdx.utils.Align;
import com.badlogic.gdx.utils.viewport.ScreenViewport;
import com.kotcrab.vis.ui.widget.*;
import com.tianyi.zhang.multiplayer.snake.App;
import com.tianyi.zhang.multiplayer.snake.helpers.AssetManager;
import com.tianyi.zhang.multiplayer.snake.helpers.Constants;

public class InfoState extends GameState {
    private static final String TAG = InfoState.class.getCanonicalName();

    private final Stage stage;
    private final VisTable table;
    private final VisImage imgTitle;
    private final VisLabel lblGameInfo, lblAcknowledgements;
    private final LinkLabel lkGnu, lkLibGdx, lkKryonet, lkProtoBuf;
    private final VisTextButton btnToTitleScreen;

    public InfoState(App app) {
        super(app);

        stage = new Stage();
        stage.setViewport(new ScreenViewport(stage.getCamera()));
        table = new VisTable(true);
        table.setFillParent(true);
        stage.addActor(table);

        imgTitle = new VisImage(AssetManager.INSTANCE.getTitleTexture());
        table.add(imgTitle).row();
        lblGameInfo = new VisLabel(Constants.VERSION + "\nA game by Tony Zhang, licensed under");
        lblGameInfo.setAlignment(Align.center);
        table.add(lblGameInfo).row();
        lkGnu = new LinkLabel("GNU GPLv3", "https://www.gnu.org/licenses/gpl-3.0.txt");
        table.add(lkGnu).row();
        lblAcknowledgements = new VisLabel("Acknowledgements: ");
        table.add(lblAcknowledgements).row();
        lkLibGdx = new LinkLabel("libGDX (license)", "http://www.apache.org/licenses/LICENSE-2.0.html");
        table.add(lkLibGdx).row();
        lkKryonet = new LinkLabel("Kryonet (license)", "https://github.com/EsotericSoftware/kryonet/blob/master/license.txt");
        table.add(lkKryonet).row();
        lkProtoBuf = new LinkLabel("Protocol Buffers (license)", "https://github.com/google/protobuf/blob/master/LICENSE");
        table.add(lkProtoBuf).row();
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
