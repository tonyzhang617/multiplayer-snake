package com.tianyi.zhang.multiplayer.snake.elements;

import com.badlogic.gdx.Gdx;
import com.tianyi.zhang.multiplayer.snake.agents.messages.Packet;
import com.tianyi.zhang.multiplayer.snake.helpers.Constants;
import com.tianyi.zhang.multiplayer.snake.helpers.Utils;

import java.util.*;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicLong;
import java.util.concurrent.atomic.AtomicReference;

public class ServerSnapshot extends Snapshot {
    private static final String TAG = ServerSnapshot.class.getCanonicalName();
    private final long startTimestamp;
    private final int serverId;
    private static final long SNAKE_MOVE_EVERY_NS = TimeUnit.MILLISECONDS.toNanos(Constants.MOVE_EVERY_MS);
    private static final long LAG_TOLERANCE_NS = TimeUnit.MILLISECONDS.toNanos(Constants.LAG_TOLERANCE_MS);

    private final AtomicReference<Packet.Update.Builder> lastPacket;

    private final AtomicLong nextRenderTime;

    private final Object lock;
    /**
     * Makes up the last game step, guarded by lock
     */
    private final List<Snake> snakes;
    private final List<SortedSet<Input>> inputBuffers;
    private long stateTime;
    private int nextInputId;
    private int version;

    public ServerSnapshot(long startTimestamp, int[] snakeIds) {
        this.startTimestamp = startTimestamp;
        serverId = 0;
        lock = new Object();
        stateTime = 0;
        nextInputId = 1;
        version = 0;
        int tmpSize = snakeIds[snakeIds.length-1]+1;
        snakes = new ArrayList<Snake>(tmpSize);
        inputBuffers = new ArrayList<SortedSet<Input>>(tmpSize);
        for (int i = 0; i < tmpSize; ++i) {
            snakes.add(new Snake(i, new int[]{3, 3, 2, 3, 1, 3, 0, 3}, new Input(Constants.RIGHT, 0, 0)));
            inputBuffers.add(new TreeSet<Input>(Input.comparator));
        }
        lastPacket = new AtomicReference<Packet.Update.Builder>(null);
        nextRenderTime = new AtomicLong(0);
    }

    @Override
    public boolean update() {
        long currentTime = Utils.getNanoTime() - startTimestamp;
        if (currentTime >= nextRenderTime.get()) {
            return true;
        } else {
            return false;
        }
    }

    @Override
    public void onServerInput(int direction) {
        synchronized (lock) {
            long inputTime = Utils.getNanoTime() - startTimestamp;
            Input newInput = new Input(direction, nextInputId++, inputTime);
            SortedSet<Input> serverInputBuffer = inputBuffers.get(serverId);
            Input lastInput = serverInputBuffer.isEmpty() ? snakes.get(serverId).LAST_INPUT : serverInputBuffer.last();
            if (lastInput.isValidNewInput(newInput)) {
                serverInputBuffer.add(newInput);
                version += 1;
            }
        }
    }

    @Override
    public void onClientUpdate(Packet.Update update) {
        long currentTime = Utils.getNanoTime() - startTimestamp;

        List<Packet.Update.PInput> newPInputs = update.getInputsList();
        List<Input> newInputs = new ArrayList<Input>(newPInputs.size());
        for (Packet.Update.PInput pInput : newPInputs) {
            if (currentTime - pInput.getTimestamp() <= LAG_TOLERANCE_NS) {
                newInputs.add(new Input(pInput.getDirection(), pInput.getId(), pInput.getTimestamp()));
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
        long currentTime = Utils.getNanoTime() - startTimestamp;
        int currentStep = (int) (currentTime / SNAKE_MOVE_EVERY_NS);
        Gdx.app.debug(TAG, "Step " + currentStep);
        long updatedStateTime = (currentTime - LAG_TOLERANCE_NS) > stateTime ? currentTime - LAG_TOLERANCE_NS : stateTime;
        int updatedStateStep = (int) (updatedStateTime / SNAKE_MOVE_EVERY_NS);
        Snake[] resultSnakes;

        synchronized (lock) {
            resultSnakes = new Snake[snakes.size()];
            resultSnakes = snakes.toArray(resultSnakes);
            int stateStep = (int) (stateTime / SNAKE_MOVE_EVERY_NS);
            int stepDiff = updatedStateStep - stateStep;
            for (int i = 0; i <= stepDiff; ++i) {
                for (int j = 0; j < resultSnakes.length; ++j) {
                    // apply and remove inputs
                    Input tmpInput;
                    long upper = (i == stepDiff ? updatedStateTime : SNAKE_MOVE_EVERY_NS * (stateStep + i + 1));
                    while (!inputBuffers.get(j).isEmpty() && (tmpInput = inputBuffers.get(j).first()).timestamp < upper) {
                        resultSnakes[j] = resultSnakes[j].changeDirection(tmpInput);
                        inputBuffers.get(j).remove(tmpInput);
                    }
                    if (i != stepDiff) {
                        // move forward
                        resultSnakes[j] = resultSnakes[j].next();
                    }
                }
            }
            // Assign snakes
            for (int j = 0; j < resultSnakes.length; ++j) {
                snakes.set(j, resultSnakes[j]);
            }

            stepDiff = currentStep - updatedStateStep;
            Queue<Input>[] inputQueues = new Queue[inputBuffers.size()];
            for (int i = 0; i < inputQueues.length; ++i) {
                inputQueues[i] = new ArrayDeque<Input>(inputBuffers.get(i));
            }
            for (int i = 0; i <= stepDiff; ++i) {
                for (int j = 0; j < resultSnakes.length; ++j) {
                    // only apply inputs
                    long upper = (i == stepDiff ? currentTime : SNAKE_MOVE_EVERY_NS * (updatedStateStep + i + 1));
                    while (!inputQueues[j].isEmpty() && inputQueues[j].peek().timestamp < upper) {
                        resultSnakes[j] = resultSnakes[j].changeDirection(inputQueues[j].poll());
                    }
                    if (i != stepDiff) {
                        // move forward
                        resultSnakes[j] = resultSnakes[j].next();
                    }
                }
            }
            stateTime = updatedStateTime;
        }

        nextRenderTime.set((currentStep + 1) * SNAKE_MOVE_EVERY_NS);
        return resultSnakes;
    }

    public Packet.Update buildPacket() {
        long currentTime = Utils.getNanoTime() - startTimestamp;
        Packet.Update.Builder tmpPacket = lastPacket.get();
        synchronized (lock) {
            if (tmpPacket != null && tmpPacket.getVersion() == version) {
                tmpPacket.setTimestamp(currentTime);
                return tmpPacket.build();
            } else {
                Packet.Update.Builder builder = Packet.Update.newBuilder();
                builder.setState(Packet.Update.PState.GAME_IN_PROGRESS).setVersion(version).setTimestamp(currentTime);
                for (Snake snake : getSnakes()) {
                    Input input = snake.LAST_INPUT;
                    Packet.Update.PSnake.Builder pSnakeBuilder = Packet.Update.PSnake.newBuilder();
                    Packet.Update.PInput.Builder pInputBuilder = Packet.Update.PInput.newBuilder();
                    pInputBuilder.setId(input.id).setDirection(input.direction).setTimestamp(input.timestamp).setStep(input.step);
                    pSnakeBuilder.setId(snake.ID).addAllCoords(snake.COORDS).setLastInput(pInputBuilder);
                    builder.addSnakes(pSnakeBuilder);
                }
                Gdx.app.debug(TAG, "New update sent to clients: " + builder.toString());
                lastPacket.set(builder);
                return builder.build();
            }
        }
    }
}
