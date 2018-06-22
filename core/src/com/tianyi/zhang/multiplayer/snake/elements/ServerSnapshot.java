package com.tianyi.zhang.multiplayer.snake.elements;

import com.tianyi.zhang.multiplayer.snake.agents.messages.Packet;
import com.tianyi.zhang.multiplayer.snake.helpers.Constants;
import com.tianyi.zhang.multiplayer.snake.helpers.Utils;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.TimeUnit;

public class ServerSnapshot extends Snapshot {
    private static final String TAG = ServerSnapshot.class.getCanonicalName();
    private final long startTimestamp;
    private final int id;
    private static final long SNAKE_MOVE_EVERY_NS = TimeUnit.MILLISECONDS.toNanos(Constants.MOVE_EVERY_MS);
    private static final long LAG_TOLERANCE_NS = TimeUnit.MILLISECONDS.toNanos(Constants.LAG_TOLERANCE_MS);

    private final Object lock;
    /**
     * Makes up the last game step, guarded by stateLock
     */
    private final List<Snake> snakes;
    private final List<List<Input>> inputBuffers;
    private long lastUpdateNsSinceStart;
    private int stateStep;
    private int nextInputId;
    private int version;

    public ServerSnapshot(long startTimestamp, int[] snakeIds) {
        this.startTimestamp = startTimestamp;
        id = 0;
        lock = new Object();
        lastUpdateNsSinceStart = 0;
        stateStep = 0;
        nextInputId = 1;
        version = 0;
        int tmpSize = snakeIds[snakeIds.length-1]+1;
        snakes = new ArrayList<Snake>(tmpSize);
        inputBuffers = new ArrayList<List<Input>>(tmpSize);
        for (int i = 0; i < tmpSize; ++i) {
            snakes.add(new Snake(id, new int[]{3, 3, 2, 3}, new Input(Constants.RIGHT, 0, 0, 0, true)));
            inputBuffers.add(new LinkedList<Input>());
        }
    }

    @Override
    public boolean update() {
        synchronized (lock) {
            long currentNsSinceStart = Utils.getNanoTime() - startTimestamp;
            int currentStep = (int) (currentNsSinceStart / SNAKE_MOVE_EVERY_NS);
            int lastUpdateStep = (int) (lastUpdateNsSinceStart / SNAKE_MOVE_EVERY_NS);
            if (currentStep > lastUpdateStep) {
                long newStateNsSinceStart = currentNsSinceStart - LAG_TOLERANCE_NS;
                int newStateStep = (int) (newStateNsSinceStart / SNAKE_MOVE_EVERY_NS);
                int stepDiff = newStateStep - stateStep;

                if (stepDiff > 0) {
                    for (int i = 0; i < snakes.size(); ++i) {
                        Snake tmpSnake = snakes.get(i);
                        List<Input> inputs = inputBuffers.get(i);
                        Input tmpInput;
                        for (int j = 0; j < stepDiff; ++j) {
                            while (!inputs.isEmpty() && inputs.get(0).step == stateStep + j) {
                                tmpInput = inputs.remove(0);
                                tmpSnake = tmpSnake.changeDirection(tmpInput);
                            }
                            tmpSnake = tmpSnake.next();
                        }
                        snakes.set(i, tmpSnake);
                    }
                    stateStep = newStateStep;
                }

                lastUpdateNsSinceStart = currentNsSinceStart;
                return true;
            } else {
                return false;
            }
        }
    }

    @Override
    public void onServerInput(int direction) {
        synchronized (lock) {
            long nsSinceStart = Utils.getNanoTime() - startTimestamp;
            int currentStep = (int) (nsSinceStart / SNAKE_MOVE_EVERY_NS);
            Input newInput = new Input(direction, nextInputId++, nsSinceStart, currentStep, true);
            inputBuffers.get(id).add(newInput);
        }
    }

    @Override
    public void onClientUpdate(Packet.Update update) {
        super.onClientUpdate(update);
    }

    @Override
    public Snake[] getSnakes() {
        synchronized (lock) {
            int currentStep = (int) (lastUpdateNsSinceStart / SNAKE_MOVE_EVERY_NS);
            Snake[] newSnakes = new Snake[snakes.size()];
            newSnakes = snakes.toArray(newSnakes);
            if (currentStep > stateStep) {
                int diff = currentStep - stateStep;
                for (int i = 0; i < newSnakes.length; ++i) {
                    Snake newSnake = newSnakes[i];
                    List<Input> inputs = inputBuffers.get(i);
                    Input tmpInput;
                    int index = 0;
                    for (int j = 0; j < diff; ++j) {
                        while (inputs.size() > index && (tmpInput = inputs.get(index)).step == stateStep + j) {
                            newSnake = newSnake.changeDirection(tmpInput);
                            index += 1;
                        }
                        newSnake = newSnake.next();
                    }
                    newSnakes[i] = newSnake;
                }
            }
            return newSnakes;
        }
    }
}
