package com.tianyi.zhang.multiplayer.snake.elements;

import com.badlogic.gdx.Gdx;
import com.tianyi.zhang.multiplayer.snake.helpers.Constants;
import com.tianyi.zhang.multiplayer.snake.helpers.Utils;

import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.TimeUnit;

public class ClientSnapshot extends Snapshot {
    private final Object stateLock;
    /**
     * Constitutes the latest game state, guarded by stateLock
     */
    private long stateTimestamp;
    /**
     * Constitutes the latest game state, guarded by stateLock
     */
    private List<Snake> snakes;
    /**
     * Constitutes the latest game state, guarded by stateLock
     */
    private int step;

    /**
     * Effectively immutable long storing the nano time of the start of game
     */
    private volatile long startTimestamp;

    private final int clientId;
    private static final long SNAKE_MOVE_EVERY_NS = TimeUnit.MILLISECONDS.toNanos(Constants.MOVE_EVERY_MS);

    private List<Input> inputBuffer;

    private static final class Input {
        public final int direction;
        public final int id;
        public final long timestamp;

        public Input(int direction, int id, long timestamp) {
            this.direction = direction;
            this.id = id;
            this.timestamp = timestamp;
        }
    }

    public ClientSnapshot(int clientId) {
        this.clientId = clientId;
        stateLock = new Object();
        snakes = new LinkedList<Snake>();
    }

    /**
     * Initializes the snakes
     * @param startTimestamp
     * @param snakeIds
     */
    public void init(long startTimestamp, int[] snakeIds) {
        // TODO: Take snakes as an argument and initialize the snakes
        this.startTimestamp = startTimestamp;
        this.step = -1;
        int id = 0;
        for (int index = 0; index < snakeIds.length; ++index) {
            while (id <= snakeIds[index]) {
                snakes.add(new Snake(id, new int[]{3, 3, 2, 3}, Constants.RIGHT, 0));
                id += 1;
            }
        }
    }

    /**
     *
     * @return true if a new frame should be rendered, false otherwise
     */
    @Override
    public boolean update() {
        long currentTs = Utils.getNanoTime();
        if (step == -1) {
            step = 0;
            return true;
        } else if ((currentTs - startTimestamp) / SNAKE_MOVE_EVERY_NS > step) {
            // TODO: Update internal state
            step = (int) ((currentTs - startTimestamp) / SNAKE_MOVE_EVERY_NS);
            Gdx.app.debug("ClientSnapshot", String.valueOf(currentTs));
            return true;
        } else {
            return false;
        }
    }

    @Override
    public Snake[] getSnakes() {
        Snake[] results = new Snake[snakes.size()];
        results = snakes.toArray(results);
        return results;
    }
}
