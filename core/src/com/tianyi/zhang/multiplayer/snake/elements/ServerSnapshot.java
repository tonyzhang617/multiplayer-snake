package com.tianyi.zhang.multiplayer.snake.elements;

import com.badlogic.gdx.Gdx;
import com.tianyi.zhang.multiplayer.snake.agents.messages.Packet;
import com.tianyi.zhang.multiplayer.snake.helpers.Constants;
import com.tianyi.zhang.multiplayer.snake.helpers.Utils;

import java.util.*;
import java.util.concurrent.TimeUnit;

public class ServerSnapshot extends Snapshot {
    private static final String TAG = ServerSnapshot.class.getCanonicalName();
    private final long startTimestamp;
    private final int serverId;
    private static final long SNAKE_MOVE_EVERY_NS = TimeUnit.MILLISECONDS.toNanos(Constants.MOVE_EVERY_MS);
    private static final long LAG_TOLERANCE_NS = TimeUnit.MILLISECONDS.toNanos(Constants.LAG_TOLERANCE_MS);

    private final Object lock;
    /**
     * Makes up the last game step, guarded by stateLock
     */
    private final List<Snake> snakes;
    private final List<Set<Input>> inputBuffers;
    private long lastUpdateNsSinceStart;
    private int stateStep;
    private int nextInputId;
    private int version;

    public ServerSnapshot(long startTimestamp, int[] snakeIds) {
        this.startTimestamp = startTimestamp;
        serverId = 0;
        lock = new Object();
        lastUpdateNsSinceStart = 0;
        stateStep = 0;
        nextInputId = 1;
        version = 0;
        int tmpSize = snakeIds[snakeIds.length-1]+1;
        snakes = new ArrayList<Snake>(tmpSize);
        inputBuffers = new ArrayList<Set<Input>>(tmpSize);
        for (int i = 0; i < tmpSize; ++i) {
            snakes.add(new Snake(serverId, new int[]{3, 3, 2, 3}, new Input(Constants.RIGHT, 0, 0, 0, true)));
            inputBuffers.add(new HashSet<Input>());
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
                        Set<Input> inputBuffer = inputBuffers.get(i);
                        Input[] inputs = new Input[inputBuffer.size()];
                        inputs = inputBuffer.toArray(inputs);
                        Arrays.sort(inputs);
                        Input tmpInput;
                        int index = 0;
                        for (int j = 0; j < stepDiff; ++j) {
                            while (inputs.length > index && (tmpInput = inputs[index]).step <= stateStep + j) {
                                if (tmpInput.step == stateStep + j) {
                                    // TODO: Remove processed inputs
                                    tmpSnake = tmpSnake.changeDirection(tmpInput);
                                }
                                index += 1;
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
            inputBuffers.get(serverId).add(newInput);
            version += 1;
        }
    }

    @Override
    public void onClientUpdate(Packet.Update update) {
        List<Packet.Update.PInput> newPInputs = update.getInputsList();
        List<Input> newInputs = new ArrayList<Input>(newPInputs.size());
        for (Packet.Update.PInput pInput : newPInputs) {
            newInputs.add(new Input(pInput.getDirection(), pInput.getId(), pInput.getTimestamp(), pInput.getStep(), true));
        }
        int id = update.getSnakeId();
        synchronized (lock) {
            inputBuffers.get(id).addAll(newInputs);
            version += 1;
        }
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
                    Set<Input> inputBuffer = inputBuffers.get(i);
                    Input[] inputs = new Input[inputBuffer.size()];
                    inputs = inputBuffer.toArray(inputs);
                    Arrays.sort(inputs);

                    // TODO: remove logging
                    if (i == 1) {
                        StringBuilder builder = new StringBuilder();
                        for (Input in : inputs) {
                            builder.append(in.step);
                            builder.append(": ");
                            builder.append(in.direction);
                            builder.append("  ");
                        }
                        Gdx.app.debug(TAG, builder.toString());
                    }

                    Input tmpInput;
                    int index = 0;
                    for (int j = 0; j < diff; ++j) {
                        while (inputs.length > index && (tmpInput = inputs[index]).step <= stateStep + j) {
                            if (tmpInput.step == stateStep + j) {
                                newSnake = newSnake.changeDirection(tmpInput);
                            }
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
