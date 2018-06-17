package com.tianyi.zhang.multiplayer.snake.elements;

import com.badlogic.gdx.Gdx;
import com.tianyi.zhang.multiplayer.snake.agents.messages.Packet;
import com.tianyi.zhang.multiplayer.snake.helpers.Constants;
import com.tianyi.zhang.multiplayer.snake.helpers.Utils;

import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

import static com.tianyi.zhang.multiplayer.snake.helpers.Constants.NO_INPUT;
import static com.tianyi.zhang.multiplayer.snake.helpers.Constants.TOLERANCE_STEPS;

public class ClientSnapshot extends Snapshot {
    private static final String TAG = ClientSnapshot.class.getCanonicalName();
    private final Object stateLock;
    /**
     * Makes up the last game step, guarded by stateLock
     */
    private int stateStep;
    private long stateTimestamp;
    private List<Snake> snakes;
    private int currentStep;

    /**
     * Effectively immutable long storing the nano time of the start of game
     */
    private volatile long startTimestamp;

    private final int clientId;
    private static final long SNAKE_MOVE_EVERY_NS = TimeUnit.MILLISECONDS.toNanos(Constants.MOVE_EVERY_MS);

    private final Object inputLock;
    /**
     * Guarded by inputLock
     */
    private int nextInputId;
    private List<Input> inputBuffer;

    private AtomicInteger newInput;

    private static final class Input {
        public final int direction;
        public final int id;
        public final long timestamp;
        public final int step;

        public Input(int direction, int id, long timestamp, int step) {
            this.direction = direction;
            this.id = id;
            this.timestamp = timestamp;
            this.step = step;
        }
    }

    public ClientSnapshot(int clientId) {
        this.clientId = clientId;
        stateLock = new Object();
        snakes = new LinkedList<Snake>();
        inputLock = new Object();
        nextInputId = 1;
        inputBuffer = new LinkedList<Input>();
        newInput = new AtomicInteger(NO_INPUT);
    }

    /**
     * Initializes the snakes
     * @param startTimestamp
     * @param snakeIds
     */
    public void init(long startTimestamp, int[] snakeIds) {
        // TODO: Take snakes as an argument and initialize the snakes
        this.startTimestamp = startTimestamp;
        this.stateTimestamp = startTimestamp;
        this.stateStep = 0;
        this.currentStep = -1;
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
        int tmpStep = (int) ((currentTs - startTimestamp) / SNAKE_MOVE_EVERY_NS);
        synchronized (stateLock) {
            if (tmpStep - currentStep > 0) {
                Gdx.app.debug(TAG, String.valueOf(tmpStep - currentStep));
                currentStep = tmpStep;

                int stepDiff = tmpStep - stateStep - TOLERANCE_STEPS;
                if (stepDiff > 0) {
                    for (int i = 0; i < stepDiff; ++i) {
                        for (int j = 0; j < snakes.size(); ++j) {
                            snakes.set(j, snakes.get(j).next());
                        }
                    }
                    stateStep = stateStep + stepDiff;
                    stateTimestamp = startTimestamp + stateStep * SNAKE_MOVE_EVERY_NS;
                }
                return true;
            } else {
                return false;
            }
        }
    }

    @Override
    public void onClientInput(int direction) {
        newInput.set(direction);

        long tmpTimestamp = Utils.getNanoTime();
        int tmpStep = (int) ((tmpTimestamp - startTimestamp) / SNAKE_MOVE_EVERY_NS);
        synchronized (inputLock) {
            Input input = new Input(direction, nextInputId++, tmpTimestamp, tmpStep);
            int lastIndex = inputBuffer.size() - 1;
            if (lastIndex >= 0 && inputBuffer.get(lastIndex).step == tmpStep) {
                inputBuffer.set(lastIndex, input);
            } else {
                inputBuffer.add(input);
            }
        }
    }

    @Override
    public void onServerUpdate(Packet.Update update) {

    }

    public int getNewInput() {
        return newInput.getAndSet(NO_INPUT);
    }

    @Override
    public Snake[] getSnakes() {
        int tmpStateStep, stepsBehind;
        long tmpStateTs;
        Snake[] results;
        synchronized (stateLock) {
            tmpStateStep = stateStep;
            stepsBehind = currentStep - tmpStateStep;
            tmpStateTs = stateTimestamp;
            results = new Snake[snakes.size()];
            results = snakes.toArray(results);
        }
        synchronized (inputLock) {
            while (!inputBuffer.isEmpty() && inputBuffer.get(0).timestamp < tmpStateTs) {
                inputBuffer.remove(0);
            }
            int index = 0;
            for (int i = 0; i < stepsBehind; ++i) {
                for (int j = 0; j < results.length; ++j) {
                    if (j == clientId) {
                        Input input;
                        while (index < inputBuffer.size() && (input = inputBuffer.get(index)).step == tmpStateStep + i) {
                            results[j] = results[j].changeDirection(input.direction, input.id);
                            index += 1;
                        }
                    }
                    results[j] = results[j].next();
                }
            }
        }
        return results;
    }
}
