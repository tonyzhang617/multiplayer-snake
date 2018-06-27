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

    private final AtomicInteger serverUpdateVersion;

    private final Object lock;
    private final AtomicLong lastUpdateNsSinceStart;
    private final AtomicBoolean gameInitialized;
    /**
     * Makes up the last game step, guarded by stateLock
     */
    private List<Snake> snakes;
    private int stateStep;
    private int nextInputId;
    private List<Input> unackInputs;

    public ClientSnapshot(int clientId) {
        this.clientId = clientId;
        lock = new Object();
        snakes = new LinkedList<Snake>();
        nextInputId = 1;
        unackInputs = new LinkedList<Input>();
        lastUpdateNsSinceStart = new AtomicLong(0);
        gameInitialized = new AtomicBoolean(false);
        serverUpdateVersion = new AtomicInteger(Integer.MIN_VALUE);
    }

    /**
     * Initializes the snakes
     * @param startTimestamp
     * @param snakeIds
     */
    public void init(long startTimestamp, int[] snakeIds) {
        // TODO: Take snakes as an argument and initialize the snakes
        this.startTimestamp = startTimestamp;
        this.stateStep = 0;
        this.lastUpdateNsSinceStart.set(0);
        int id = 0;
        for (int index = 0; index < snakeIds.length; ++index) {
            while (id <= snakeIds[index]) {
                snakes.add(new Snake(id, new int[]{3, 3, 2, 3}, new Input(RIGHT, 0, 0, true)));
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
        int tmpStep = (int) (currentNs / SNAKE_MOVE_EVERY_NS);
        int lastUpdateStep = (int) (lastUpdateNsSinceStart.get() / SNAKE_MOVE_EVERY_NS);
        // TODO: remove unacknowledged inputs
        if (tmpStep - lastUpdateStep > 0) {
            lastUpdateNsSinceStart.set(currentNs);
            return true;
        } else {
            return false;
        }
    }

    @Override
    public void onClientInput(int direction) {
        Input input;
        synchronized (lock) {
            long tmpNs = Utils.getNanoTime() - startTimestamp;
            int tmpStep = (int) (tmpNs / SNAKE_MOVE_EVERY_NS);
            input = new Input(direction, nextInputId++, tmpNs, false);
            unackInputs.add(input);
        }
    }

    @Override
    public void onServerUpdate(Packet.Update update) {
        if (update.getState() == Packet.Update.PState.GAME_IN_PROGRESS && update.getVersion() > serverUpdateVersion.get()) {
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

                    stateStep = (int) (update.getTimestamp() / SNAKE_MOVE_EVERY_NS);

                    if (tmpId == clientId) {
                        int lastAckInputId = pInput.getId();
                        while (!unackInputs.isEmpty() && lastAckInputId <= unackInputs.get(0).id) {
                            unackInputs.remove(0);
                        }
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
            Snake[] results;
            synchronized (lock) {
                int currentStep = (int) (lastUpdateNsSinceStart.get() / SNAKE_MOVE_EVERY_NS);
                int stepsBehind = currentStep - stateStep;
                results = new Snake[snakes.size()];
                results = snakes.toArray(results);
                for (int j = 0; j < results.length; ++j) {
                    int index = 0;
                    Snake newSnake = results[j];
                    for (int i = 0; i <= stepsBehind; ++i) {
                        if (j == clientId) {
                            while (unackInputs.size() > index && unackInputs.get(index).step < stateStep + i) {
                                index += 1;
                            }
                            while (unackInputs.size() > index && unackInputs.get(index).step == stateStep + i) {
                                newSnake = newSnake.changeDirection(unackInputs.get(index));
                                index += 1;
                            }
                        }
                        if (i != stepsBehind){
                            newSnake = newSnake.next();
                        }
                    }
                    results[j] = newSnake;
                }
            }
            return results;
        } else {
            return new Snake[0];
        }
    }
}
