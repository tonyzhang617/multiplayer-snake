package com.tianyi.zhang.multiplayer.snake.elements;

import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;

import static com.tianyi.zhang.multiplayer.snake.helpers.Constants.MAX_STATES;
import static com.tianyi.zhang.multiplayer.snake.helpers.Constants.RIGHT;

public class Snapshots {
    private final List<Snake> pastStates;
    private final AtomicInteger currentStep;

    public Snapshots() {
        pastStates = new LinkedList<Snake>();
        Short[] coords = new Short[4];
        coords[0] = 3;
        coords[1] = 3;
        coords[2] = 2;
        coords[3] = 3;
        pastStates.add(new Snake(coords, RIGHT, 0, 0));
        currentStep = new AtomicInteger(0);
    }

    public synchronized void step() {
        int size = pastStates.size();
        if (size >= MAX_STATES) {
            pastStates.remove(0);
        }
        pastStates.add(pastStates.get(size-1).nextStep());
        currentStep.incrementAndGet();
    }

    public synchronized void changePastInput(byte direction, int step, int inputIndex) {
        if (step > currentStep.get() - MAX_STATES) {
            int tmp = step - currentStep.get() + MAX_STATES - 1;
            Snake snake = pastStates.get(tmp);
            if (snake.getInputIndex() < inputIndex && snake.getDirection() != direction) {
                pastStates.set(tmp, snake.changeDirection(direction, inputIndex));
                for (int i = step; i < pastStates.size()-2; ++i) {
                    pastStates.set(i+1, pastStates.get(i).nextStep());
                }
            }
        }
    }

    public synchronized void newInput(byte direction, int inputIndex) {
        int lastIndex = pastStates.size()-1;
        Snake snake = pastStates.get(lastIndex);
        if (snake.getDirection() != direction) {
            pastStates.set(lastIndex, snake.changeDirection(direction, inputIndex));
        }
    }

    public int getStep() {
        return currentStep.get();
    }
}
