package com.tianyi.zhang.multiplayer.snake.elements;

import com.badlogic.gdx.Gdx;
import com.tianyi.zhang.multiplayer.snake.agents.messages.Packet;
import com.tianyi.zhang.multiplayer.snake.helpers.Constants;
import com.tianyi.zhang.multiplayer.snake.helpers.Utils;

import java.util.*;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicReference;

public class ServerSnapshot extends Snapshot {
    private static final String TAG = ServerSnapshot.class.getCanonicalName();
    private final long startTimestamp;
    private final int serverId;
    private static final long SNAKE_MOVE_EVERY_NS = TimeUnit.MILLISECONDS.toNanos(Constants.MOVE_EVERY_MS);
    private static final long LAG_TOLERANCE_NS = TimeUnit.MILLISECONDS.toNanos(Constants.LAG_TOLERANCE_MS);

    private final AtomicReference<Packet.Update.Builder> lastPacket;

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
            snakes.add(new Snake(i, new int[]{3, 3, 2, 3}, new Input(Constants.RIGHT, 0, 0, 0, true)));
            inputBuffers.add(new HashSet<Input>());
        }
        lastPacket = new AtomicReference<Packet.Update.Builder>(null);
    }

    @Override
    public boolean update() {
        synchronized (lock) {
            long currentNsSinceStart = Utils.getNanoTime() - startTimestamp;
            int currentStep = (int) (currentNsSinceStart / SNAKE_MOVE_EVERY_NS);
            int lastUpdateStep = (int) (lastUpdateNsSinceStart / SNAKE_MOVE_EVERY_NS);
            if (currentStep > lastUpdateStep) {
                Gdx.app.debug(TAG, "Step: " + currentStep);
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
        long currentTs = Utils.getNanoTime() - startTimestamp;

        List<Packet.Update.PInput> newPInputs = update.getInputsList();
        List<Input> newInputs = new ArrayList<Input>(newPInputs.size());
        for (Packet.Update.PInput pInput : newPInputs) {
            if (currentTs - pInput.getTimestamp() < LAG_TOLERANCE_NS) {
                newInputs.add(new Input(pInput.getDirection(), pInput.getId(), pInput.getTimestamp(), pInput.getStep(), true));
            }
        }
        int id = update.getSnakeId();
        if (!newInputs.isEmpty()) {
            synchronized (lock) {
                if (inputBuffers.get(id).addAll(newInputs)) {
                    Gdx.app.debug(TAG, "New update received from client " + id);
                    Gdx.app.debug(TAG, update.toString());
                    version += 1;
                }
            }
        }
    }

    @Override
    public Snake[] getSnakes() {
        synchronized (lock) {
            int currentStep = (int) (lastUpdateNsSinceStart / SNAKE_MOVE_EVERY_NS);
            Snake[] newSnakes = new Snake[snakes.size()];
            newSnakes = snakes.toArray(newSnakes);
            Gdx.app.debug(TAG, "Snake before: " + newSnakes[1].toString());

            int diff = currentStep - stateStep;
            for (int i = 0; i < newSnakes.length; ++i) {
                Snake newSnake = newSnakes[i];
                Set<Input> inputBuffer = inputBuffers.get(i);
                Input[] inputs = new Input[inputBuffer.size()];
                inputs = inputBuffer.toArray(inputs);
                Arrays.sort(inputs);
                Gdx.app.debug(TAG, Arrays.toString(inputs));

                Input tmpInput;
                int index = 0;
                for (int j = 0; j <= diff; ++j) {
                    while (inputs.length > index && (tmpInput = inputs[index]).step <= stateStep + j) {
                        if (tmpInput.step == stateStep + j) {
                            newSnake = newSnake.changeDirection(tmpInput);
                        }
                        index += 1;
                    }
                    if (j != diff) {
                        newSnake = newSnake.next();
                    }
                }
                newSnakes[i] = newSnake;
            }
            return newSnakes;
        }
    }

    public Packet.Update buildUpdate() {
        Packet.Update.Builder tmpPacket = lastPacket.get();
        synchronized (lock) {
            if (tmpPacket != null && tmpPacket.getVersion() == version) {
                tmpPacket.setTimestamp(lastUpdateNsSinceStart);
                return tmpPacket.build();
            } else {
                Packet.Update.Builder builder = Packet.Update.newBuilder();
                builder.setState(Packet.Update.PState.GAME_IN_PROGRESS).setVersion(version).setTimestamp(lastUpdateNsSinceStart);
                for (Snake snake : getSnakes()) {
                    Input input = snake.LAST_INPUT;
                    Packet.Update.PSnake.Builder pSnakeBuilder = Packet.Update.PSnake.newBuilder();
                    Packet.Update.PInput.Builder pInputBuilder = Packet.Update.PInput.newBuilder();
                    pInputBuilder.setId(input.id).setDirection(input.direction).setTimestamp(input.timestamp).setStep(input.step);
                    pSnakeBuilder.setId(snake.ID).addAllCoords(snake.COORDS).setLastInput(pInputBuilder);
                    builder.addSnakes(pSnakeBuilder);
                }
                Gdx.app.debug(TAG, builder.toString());
                lastPacket.set(builder);
                return builder.build();
            }
        }
    }
}
