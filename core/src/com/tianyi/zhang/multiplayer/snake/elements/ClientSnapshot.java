package com.tianyi.zhang.multiplayer.snake.elements;

import com.badlogic.gdx.Gdx;
import com.tianyi.zhang.multiplayer.snake.agents.messages.Packet;
import com.tianyi.zhang.multiplayer.snake.helpers.Constants;
import com.tianyi.zhang.multiplayer.snake.helpers.Utils;

import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicLong;

import static com.tianyi.zhang.multiplayer.snake.helpers.Constants.RIGHT;

public class ClientSnapshot extends Snapshot {
    private static final String TAG = ClientSnapshot.class.getCanonicalName();
    /**
     * Effectively immutable long storing the nano time of the start of game
     */
    private volatile long startTimestamp;
    private final int clientId;
    private static final long SNAKE_MOVE_EVERY_NS = TimeUnit.MILLISECONDS.toNanos(Constants.MOVE_EVERY_MS);
    private static final long UPDATE_AFTER_INACTIVE_NS = TimeUnit.MILLISECONDS.toNanos(Constants.UPDATE_AFTER_INACTIVE_MS);

    private final AtomicInteger serverUpdateVersion;

    private final Object lock;
    private final AtomicLong nextRenderTime;
    private final AtomicBoolean gameInitialized;
    /**
     * Makes up the last game step, guarded by stateLock
     */
    private List<Snake> snakes;
    private long stateTime;
    private int nextInputId;
    private List<Input> unackInputs;

    public ClientSnapshot(int clientId) {
        this.clientId = clientId;
        lock = new Object();
        snakes = new LinkedList<Snake>();
        nextInputId = 1;
        unackInputs = new LinkedList<Input>();
        gameInitialized = new AtomicBoolean(false);
        stateTime = 0;
        serverUpdateVersion = new AtomicInteger(Integer.MIN_VALUE);
        nextRenderTime = new AtomicLong(0);
    }

    /**
     * Initializes the snakes
     * @param startTimestamp
     * @param snakeIds
     */
    public void init(long startTimestamp, int[] snakeIds) {
        // TODO: Take snakes as an argument and initialize the snakes
        this.startTimestamp = startTimestamp;

        int id = 0;
        for (int index = 0; index < snakeIds.length; ++index) {
            while (id <= snakeIds[index]) {
                snakes.add(new Snake(id, new int[]{3, 3, 2, 3, 1, 3, 0, 3}, new Input(RIGHT, 0, 0, true)));
                id += 1;
            }
        }
        gameInitialized.set(true);
    }

    /**
     *
     * @return true if a new frame should be rendered, false otherwise
     */
    @Override
    public boolean update() {
        long currentNs = Utils.getNanoTime() - startTimestamp;
        if (currentNs >= nextRenderTime.get()) {
            return true;
        } else {
            return false;
        }
    }

    @Override
    public void onClientInput(int direction) {
        synchronized (lock) {
            long tmpNs = Utils.getNanoTime() - startTimestamp;
            Input input = new Input(direction, nextInputId++, tmpNs, false);
            Input lastInput = (unackInputs.isEmpty() ? snakes.get(clientId).LAST_INPUT : unackInputs.get(unackInputs.size()-1));
            if (lastInput.isValidNewInput(input)) {
                unackInputs.add(input);
            } else {
                Gdx.app.debug(TAG, "Input " + input.id + " rejected");
            }
        }
    }

    @Override
    public void onServerUpdate(Packet.Update update) {
        if (gameInitialized.get() && update.getState() == Packet.Update.PState.GAME_IN_PROGRESS) {
            if (update.getVersion() > serverUpdateVersion.get()) {
                Gdx.app.debug(TAG, "Server update version " + update.getVersion() + " received.");
                Gdx.app.debug(TAG, update.toString());
                serverUpdateVersion.set(update.getVersion());
                synchronized (lock) {
                    List<Packet.Update.PSnake> pSnakes = update.getSnakesList();
                    for (int i = 0; i < pSnakes.size(); ++i) {
                        Packet.Update.PSnake pSnake = pSnakes.get(i);
                        int tmpId = pSnake.getId();
                        Packet.Update.PInput pInput = pSnake.getLastInput();
                        Input newInput = new Input(pInput.getDirection(), pInput.getId(), pInput.getTimestamp(), true);
                        Snake newSnake = new Snake(tmpId, pSnake.getCoordsList(), newInput);
                        snakes.set(tmpId, newSnake);

                        stateTime = update.getTimestamp();

                        if (tmpId == clientId) {
                            int lastAckInputId = pInput.getId();
                            while (!unackInputs.isEmpty() && lastAckInputId <= unackInputs.get(0).id) {
                                unackInputs.remove(0);
                            }
                        }
                    }
                }
            } else {
                synchronized (lock) {
                    long updatedStateTime;
                    if (unackInputs.isEmpty()) {
                        updatedStateTime = update.getTimestamp();
                    } else {
                        updatedStateTime = (unackInputs.get(0).timestamp < update.getTimestamp()) ? unackInputs.get(0).timestamp : update.getTimestamp();
                    }
                    int updatedStateStep = (int) (updatedStateTime / SNAKE_MOVE_EVERY_NS);

                    int stateStep = (int) (stateTime / SNAKE_MOVE_EVERY_NS);
                    int stepDiff = updatedStateStep - stateStep;

                    if (stepDiff >= UPDATE_AFTER_INACTIVE_NS / SNAKE_MOVE_EVERY_NS) {
                        for (int i = 0; i < stepDiff; ++i) {
                            for (int j = 0; j < snakes.size(); ++j) {
                                snakes.set(j, snakes.get(j).next());
                            }
                        }

                        stateTime = updatedStateTime;
                    }
                }
            }
        }
    }

    public Input[] getNewInputs() {
        synchronized (lock) {
            Input[] inputs = new Input[unackInputs.size()];
            inputs = unackInputs.toArray(inputs);
            return inputs;
        }
    }

    @Override
    public Snake[] getSnakes() {
        if (gameInitialized.get()) {
            long currentTime = Utils.getNanoTime() - startTimestamp;
            int currentStep = (int) (currentTime / SNAKE_MOVE_EVERY_NS);

            synchronized (lock) {
                Snake[] resultSnakes = new Snake[snakes.size()];
                resultSnakes = snakes.toArray(resultSnakes);

                int stateStep = (int) (stateTime / SNAKE_MOVE_EVERY_NS);
                Gdx.app.debug(TAG, "stateStep: " + stateStep);
                int stepDiff = currentStep - stateStep;

                int inputIndex = 0;
                for (int i = 0; i <= stepDiff; ++i) {
                    long upper = (i == stepDiff ? currentTime : SNAKE_MOVE_EVERY_NS * (stateStep + i + 1));
                    for (int j = 0; j < resultSnakes.length; ++j) {
                        if (j == clientId) {
                            // Apply inputs
                            Input tmpInput;
                            while (unackInputs.size() > inputIndex && (tmpInput = unackInputs.get(inputIndex)).timestamp < upper) {
                                resultSnakes[j] = resultSnakes[j].changeDirection(tmpInput);
                                inputIndex += 1;
                            }
                        }
                        if (i != stepDiff) {
                            // Move snakes forward
                            resultSnakes[j] = resultSnakes[j].next();
                        }
                    }
                }

                nextRenderTime.set(SNAKE_MOVE_EVERY_NS * (currentStep + 1));

                return resultSnakes;
            }
        } else {
            return new Snake[0];
        }
    }
}
