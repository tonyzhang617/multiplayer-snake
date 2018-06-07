package com.tianyi.zhang.multiplayer.snake.elements;

import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;

import static com.tianyi.zhang.multiplayer.snake.helpers.Constants.MAX_STATES;

public class Snapshots {
    private final List<Snake> pastStates;
    private AtomicInteger currentStep;

    public Snapshots() {
        this.pastStates = Collections.synchronizedList(new LinkedList<Snake>());
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
                pastStates.add(tmp, snake.changeDirection(direction, inputIndex));
                for (int i = step; i < pastStates.size()-2; ++i) {
                    pastStates.set(i+1, pastStates.get(i).nextStep());
                }
            }
        }
    }
}
