package com.tianyi.zhang.multiplayer.snake.states.server;

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
import com.tianyi.zhang.multiplayer.snake.agents.Server;
import com.tianyi.zhang.multiplayer.snake.agents.messages.Packet;
import com.tianyi.zhang.multiplayer.snake.elements.Grid;
import com.tianyi.zhang.multiplayer.snake.elements.ServerSnapshot;
import com.tianyi.zhang.multiplayer.snake.helpers.Constants;
import com.tianyi.zhang.multiplayer.snake.helpers.RenderingUtils;
import com.tianyi.zhang.multiplayer.snake.helpers.Utils;
import com.tianyi.zhang.multiplayer.snake.states.GameState;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

public class SVMainGameState extends GameState {
    private static final String TAG = SVMainGameState.class.getCanonicalName();
    private final ServerSnapshot serverSnapshot;
    private final ScheduledExecutorService executor;

    private final OrthographicCamera camera;
    private final SpriteBatch batch;

    private final Map<Grid.Block, Sprite> spriteMap;

    /**
     *
     * @param app a reference to App
     * @param connectionIds a sorted, ascending list of integers representing client IDs
     */
    public SVMainGameState(App app, List<Integer> connectionIds) {
        super(app);

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
                if (Math.abs(velocityX) > Math.abs(velocityY)) {
                    if (velocityX > 0) {
                        serverSnapshot.onServerInput(Constants.RIGHT);
                    } else {
                        serverSnapshot.onServerInput(Constants.LEFT);
                    }
                } else {
                    if (velocityY > 0) {
                        serverSnapshot.onServerInput(Constants.DOWN);
                    } else {
                        serverSnapshot.onServerInput(Constants.UP);
                    }
                }
                return true;
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
                Gdx.app.debug(TAG, "Keycode " + keycode + " pressed");
                if (keycode == Input.Keys.LEFT) {
                    serverSnapshot.onServerInput(Constants.LEFT);
                } else if (keycode == Input.Keys.UP) {
                    serverSnapshot.onServerInput(Constants.UP);
                } else if (keycode == Input.Keys.RIGHT) {
                    serverSnapshot.onServerInput(Constants.RIGHT);
                } else if (keycode == Input.Keys.DOWN) {
                    serverSnapshot.onServerInput(Constants.DOWN);
                }
                return true;
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

        Gdx.graphics.setContinuousRendering(false);

        int[] tmpIds = new int[connectionIds.size()+1];
        tmpIds[0] = 0;
        for (int i = 0; i < connectionIds.size(); i++) {
            tmpIds[i+1] = connectionIds.get(i);
        }

        executor = Executors.newSingleThreadScheduledExecutor();

        serverSnapshot = new ServerSnapshot(Utils.getNanoTime(), tmpIds);

        executor.scheduleAtFixedRate(new Runnable() {
            @Override
            public void run() {
                try {
                    if (serverSnapshot.update()) {
                        Gdx.graphics.requestRendering();
                    }
                    _app.getAgent().send(serverSnapshot.buildPacket());
                } catch (Exception e) {
                    Gdx.app.error(TAG, "Error encountered inside scheduled task: ", e);
                }
            }
        }, 0, 100, TimeUnit.MILLISECONDS);

        _app.getAgent().setListener(new Listener() {
            @Override
            public void received(Connection connection, Object object) {
                if (object instanceof byte[]) {
                    Packet.Update update = Server.parseReceived(object);
                    if (update.getType() == Packet.Update.PType.INPUT_UPDATE) {
                        serverSnapshot.onClientUpdate(Server.parseReceived(object));
                    }
                }
            }
        });

        Gdx.app.debug(TAG, "Server main game loaded");

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
        Grid grid = serverSnapshot.getGrid();

        RenderingUtils.clear();
        RenderingUtils.renderGrid(grid, camera, batch, spriteMap);
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
