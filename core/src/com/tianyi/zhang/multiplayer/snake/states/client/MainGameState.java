package com.tianyi.zhang.multiplayer.snake.states.client;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.InputMultiplexer;
import com.badlogic.gdx.InputProcessor;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.input.GestureDetector;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.scenes.scene2d.utils.ClickListener;
import com.badlogic.gdx.utils.viewport.ScreenViewport;
import com.esotericsoftware.kryonet.Connection;
import com.esotericsoftware.kryonet.Listener;
import com.kotcrab.vis.ui.widget.VisLabel;
import com.kotcrab.vis.ui.widget.VisTable;
import com.kotcrab.vis.ui.widget.VisTextButton;
import com.kotcrab.vis.ui.widget.VisWindow;
import com.tianyi.zhang.multiplayer.snake.App;
import com.tianyi.zhang.multiplayer.snake.agents.Client;
import com.tianyi.zhang.multiplayer.snake.elements.ClientSnapshot;
import com.tianyi.zhang.multiplayer.snake.elements.GameRenderer;
import com.tianyi.zhang.multiplayer.snake.elements.Grid;
import com.tianyi.zhang.multiplayer.snake.helpers.Constants;
import com.tianyi.zhang.multiplayer.snake.protobuf.generated.ClientPacket;
import com.tianyi.zhang.multiplayer.snake.protobuf.generated.ServerPacket;
import com.tianyi.zhang.multiplayer.snake.states.GameState;

import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicReference;

public class MainGameState extends GameState {
    private static final String TAG = MainGameState.class.getCanonicalName();
    private static final float GRID_RATIO = (float) Constants.WIDTH / Constants.HEIGHT;

    private final ScheduledExecutorService executor;
    private final int clientId;
    private final AtomicBoolean gameInitialized;
    private volatile ClientSnapshot clientSnapshot;

    private final OrthographicCamera camera;
    private final Stage stage;
    private final VisWindow window;
    private final Table table;
    private final VisTextButton btnToTitleScreen;
    private final VisLabel lblResult;

    private final AtomicReference<Constants.GameResult> gameResult;

    public MainGameState(App app, int id) {
        super(app);
        clientId = id;
        clientSnapshot = null;
        gameInitialized = new AtomicBoolean(false);

        InputMultiplexer inputMultiplexer = new InputMultiplexer();
        inputMultiplexer.addProcessor(new GestureDetector(new GestureDetector.GestureListener() {
            @Override
            public boolean touchDown(float x, float y, int pointer, int button) {
                return false;
            }

            @Override
            public boolean tap(float x, float y, int count, int button) {
                return false;
            }

            @Override
            public boolean longPress(float x, float y) {
                return false;
            }

            @Override
            public boolean fling(float velocityX, float velocityY, int button) {
                if (gameInitialized.get()) {
                    if (Math.abs(velocityX) > Math.abs(velocityY)) {
                        if (velocityX > 0) {
                            clientSnapshot.onClientInput(Constants.RIGHT);
                        } else {
                            clientSnapshot.onClientInput(Constants.LEFT);
                        }
                    } else {
                        if (velocityY > 0) {
                            clientSnapshot.onClientInput(Constants.DOWN);
                        } else {
                            clientSnapshot.onClientInput(Constants.UP);
                        }
                    }
                    return true;
                } else {
                    return false;
                }
            }

            @Override
            public boolean pan(float x, float y, float deltaX, float deltaY) {
                return false;
            }

            @Override
            public boolean panStop(float x, float y, int pointer, int button) {
                return false;
            }

            @Override
            public boolean zoom(float initialDistance, float distance) {
                return false;
            }

            @Override
            public boolean pinch(Vector2 initialPointer1, Vector2 initialPointer2, Vector2 pointer1, Vector2 pointer2) {
                return false;
            }

            @Override
            public void pinchStop() {

            }
        }));
        inputMultiplexer.addProcessor(new InputProcessor() {
            @Override
            public boolean keyDown(int keycode) {
                if (gameInitialized.get()) {
                    Gdx.app.debug(TAG, "Keycode " + keycode + " pressed");
                    if (keycode == Input.Keys.LEFT) {
                        clientSnapshot.onClientInput(Constants.LEFT);
                    } else if (keycode == Input.Keys.UP) {
                        clientSnapshot.onClientInput(Constants.UP);
                    } else if (keycode == Input.Keys.RIGHT) {
                        clientSnapshot.onClientInput(Constants.RIGHT);
                    } else if (keycode == Input.Keys.DOWN) {
                        clientSnapshot.onClientInput(Constants.DOWN);
                    }
                    return true;
                } else {
                    return false;
                }
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
        });
        Gdx.input.setInputProcessor(inputMultiplexer);

        executor = Executors.newSingleThreadScheduledExecutor();

        Gdx.graphics.setContinuousRendering(false);
        _app.getAgent().setListener(new Listener() {
            @Override
            public void received(Connection connection, Object object) {
                if (object instanceof byte[]) {
                    ServerPacket.Update update = Client.parseServerUpdate(object);
                    if (!gameInitialized.get()) {
                        clientSnapshot = new ClientSnapshot(clientId, update);

                        executor.scheduleAtFixedRate(new Runnable() {
                            @Override
                            public void run() {
                                try {
                                    if (clientSnapshot.update()) {
                                        Gdx.graphics.requestRendering();
                                    }
                                    ClientPacket.Message message = clientSnapshot.buildMessage();
                                    if (message != null) {
                                        _app.getAgent().send(message.toByteArray());
                                    }
                                } catch (Exception e) {
                                    Gdx.app.error(TAG, "Error encountered while running scheduled rendering task: ", e);
                                }
                            }
                        }, 0, 30, TimeUnit.MILLISECONDS);

                        gameInitialized.set(true);
                    } else if (gameInitialized.get()) {
                        clientSnapshot.onServerUpdate(update);
                    }
                }
            }
        });

        float w = Gdx.graphics.getWidth(), h = Gdx.graphics.getHeight(), ratio = w / h;
        camera = new OrthographicCamera();
        if (ratio >= GRID_RATIO) {
            // Black bars on both sides
            camera.viewportWidth = Constants.HEIGHT * ratio;
            camera.viewportHeight = Constants.HEIGHT;
        } else {
            // Black bars on top and bottom
            camera.viewportWidth = Constants.WIDTH;
            camera.viewportHeight = Constants.WIDTH / ratio;
        }
        camera.position.set(Constants.WIDTH / 2f, Constants.HEIGHT / 2f, 0);
        camera.update();

        stage = new Stage();
        stage.setViewport(new ScreenViewport(stage.getCamera()));
        window = new VisWindow("GG", false);
        window.setColor(0, 1f, 0, 0.64f);
        window.setSize(w * 0.9f, h * 0.3f);
        window.setPosition((w - window.getWidth()) / 2f, (h - window.getHeight()) / 2f);
        table = new VisTable(true);
        btnToTitleScreen = new VisTextButton("Return to main screen");
        btnToTitleScreen.addListener(new ClickListener() {
            @Override
            public void clicked(InputEvent event, float x, float y) {
                _app.popState();
                _app.destroyAgent();
            }
        });
        lblResult = new VisLabel();
        table.add(lblResult).row();
        table.add(btnToTitleScreen).row();
        window.add(table).row();
        stage.addActor(window);

        gameResult = new AtomicReference<Constants.GameResult>(null);

        Gdx.app.debug(TAG, "Main game loaded");
    }

    @Override
    public void render(float delta) {
        GameRenderer.INSTANCE.clear();

        if (gameInitialized.get()) {
            Grid grid = clientSnapshot.getGrid();
            GameRenderer.INSTANCE.render(grid, camera);

            if (gameResult.get() == null) {
                if (grid.isSnakeDead(clientId)) {
                    // Game over... GG
                    gameResult.set(Constants.GameResult.LOST);
                    lblResult.setText(Constants.GAME_OVER);
                    Gdx.input.setInputProcessor(stage);
                } else if (grid.getAliveCount() == 1) {
                    // You are the last snake alive
                    gameResult.set(Constants.GameResult.WON);
                    lblResult.setText(Constants.CONGRATS);
                    Gdx.input.setInputProcessor(stage);
                }
            }

            if (gameResult.get() != null) {
                stage.act();
                stage.draw();
            }
        }
    }

    @Override
    public void show() {

    }

    @Override
    public void resize(int width, int height) {
        float ratio = (float) width / height;
        if (ratio >= GRID_RATIO) {
            // Black bars on both sides
            camera.viewportWidth = Constants.HEIGHT * ratio;
            camera.viewportHeight = Constants.HEIGHT;
        } else {
            // Black bars on top and bottom
            camera.viewportWidth = Constants.WIDTH;
            camera.viewportHeight = Constants.WIDTH / ratio;
        }
        camera.position.set(Constants.WIDTH / 2f, Constants.HEIGHT / 2f, 0);
        camera.update();

        stage.getViewport().update(width, height, true);
        window.setSize(width * 0.9f, height * 0.3f);
        window.setPosition((width - window.getWidth()) / 2f, (height - window.getHeight()) / 2f);
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
        executor.shutdown();
        stage.dispose();
        Gdx.graphics.setContinuousRendering(true);
    }
}
