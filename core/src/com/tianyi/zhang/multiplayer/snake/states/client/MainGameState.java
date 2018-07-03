package com.tianyi.zhang.multiplayer.snake.states.client;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.InputMultiplexer;
import com.badlogic.gdx.InputProcessor;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.g2d.Sprite;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.input.GestureDetector;
import com.badlogic.gdx.math.Vector2;
import com.esotericsoftware.kryonet.Connection;
import com.esotericsoftware.kryonet.Listener;
import com.tianyi.zhang.multiplayer.snake.App;
import com.tianyi.zhang.multiplayer.snake.agents.Client;
import com.tianyi.zhang.multiplayer.snake.agents.messages.Packet;
import com.tianyi.zhang.multiplayer.snake.elements.ClientSnapshot;
import com.tianyi.zhang.multiplayer.snake.elements.Grid;
import com.tianyi.zhang.multiplayer.snake.helpers.Constants;
import com.tianyi.zhang.multiplayer.snake.helpers.RenderingUtils;
import com.tianyi.zhang.multiplayer.snake.helpers.Utils;
import com.tianyi.zhang.multiplayer.snake.states.GameState;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;

public class MainGameState extends GameState {
    private static final String TAG = MainGameState.class.getCanonicalName();
    private final ScheduledExecutorService executor;
    private final int clientId;
    private volatile long roundTripNs;
    private final AtomicBoolean gameInitialized;
    private volatile ClientSnapshot clientSnapshot;

    private final OrthographicCamera camera;
    private final SpriteBatch batch;

    private final Map<Grid.Block, Sprite> spriteMap;

    public MainGameState(App app, int id) {
        super(app);
        clientId = id;
        clientSnapshot = null;
        roundTripNs = 0;
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

        executor = Executors.newScheduledThreadPool(2);

        Gdx.graphics.setContinuousRendering(false);
        _app.getAgent().setListener(new Listener() {
            @Override
            public void received(Connection connection, Object object) {
                if (object instanceof byte[]) {
                    Packet.Update update = Client.parseReceived(object);
                    if (!gameInitialized.get()) {
                        if (update.getType() == Packet.Update.PType.PING_REPLY) {
                            roundTripNs = Utils.getNanoTime() - update.getTimestamp();
                        } else if (update.getType() == Packet.Update.PType.GAME_UPDATE) {
                            clientSnapshot = new ClientSnapshot(clientId, roundTripNs / 2, update);

                            executor.scheduleAtFixedRate(new Runnable() {
                                @Override
                                public void run() {
                                    try {
                                        if (clientSnapshot.update()) {
                                            Gdx.graphics.requestRendering();
                                        }
                                        Packet.Update newUpdate = clientSnapshot.buildPacket();
                                        if (newUpdate != null) {
                                            _app.getAgent().send(newUpdate);
                                        }
                                    } catch (Exception e) {
                                        Gdx.app.error(TAG, "Error encountered while running scheduled rendering task: ", e);
                                    }
                                }
                            }, 0, 30, TimeUnit.MILLISECONDS);

                            gameInitialized.set(true);
                        }
                    } else {
                        clientSnapshot.onServerUpdate(update);
                    }
                }
            }
        });
        _app.getAgent().send(Packet.Update.newBuilder().setType(Packet.Update.PType.PING).setTimestamp(Utils.getNanoTime()).build());
        Gdx.app.debug(TAG, "Main game loaded");

        float w = Gdx.graphics.getWidth(), h = Gdx.graphics.getHeight();
        camera = new OrthographicCamera(Constants.HEIGHT * (w / h), Constants.HEIGHT);
        camera.position.set(camera.viewportWidth / 2f, camera.viewportHeight / 2f, 0);
        camera.update();

        batch = new SpriteBatch();

        Sprite playerSnakeBody = new Sprite(RenderingUtils.newTextureWithLinearFilter("player_snake_body.png"));
        playerSnakeBody.setSize(Constants.BLOCK_LENGTH, Constants.BLOCK_LENGTH);
        spriteMap = new HashMap<Grid.Block, Sprite>();
        spriteMap.put(Grid.Block.PLAYER_SNAKE_BODY, playerSnakeBody);
    }

    @Override
    public void render(float delta) {
        RenderingUtils.clear();

        if (gameInitialized.get()) {
            Grid grid = clientSnapshot.getGrid();
            RenderingUtils.renderGrid(grid, camera, batch, spriteMap);
        }
    }

    @Override
    public void show() {

    }

    @Override
    public void resize(int width, int height) {
        camera.viewportWidth = Constants.HEIGHT * width/height;
        camera.viewportHeight = Constants.HEIGHT;
        camera.update();
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
        batch.dispose();
    }
}
