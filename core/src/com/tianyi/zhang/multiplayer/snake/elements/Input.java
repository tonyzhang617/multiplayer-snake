package com.tianyi.zhang.multiplayer.snake.elements;

import com.tianyi.zhang.multiplayer.snake.agents.messages.Packet;
import com.tianyi.zhang.multiplayer.snake.helpers.Constants;

import java.util.Comparator;
import java.util.concurrent.TimeUnit;

public class Input implements Comparable<Input> {
    public final int direction;
    public final int id;
    public final long timestamp;
    public final int step;

    public static final Comparator<Input> comparator = new InputComparator();
    private static final long STEP_LENGTH = TimeUnit.MILLISECONDS.toNanos(Constants.MOVE_EVERY_MS);

    public Input(int direction, int id, long timestamp) {
        this.direction = direction;
        this.id = id;
        this.timestamp = timestamp;
        this.step = (int) (timestamp / STEP_LENGTH);
    }

    public static Input fromProtoInput(Packet.Update.PInput pInput) {
        return new Input(pInput.getDirection(), pInput.getId(), pInput.getTimestamp());
    }

    public Packet.Update.PInput.Builder toProtoInput() {
        Packet.Update.PInput.Builder inputBuilder = Packet.Update.PInput.newBuilder();
        inputBuilder.setId(id).setDirection(direction).setTimestamp(timestamp);
        return inputBuilder;
    }

    public boolean isValidNewInput(Input newInput) {
        if (newInput == null) {
            return false;
        }

        if (this.direction == newInput.direction || this.id >= newInput.id || this.step > newInput.step || this.timestamp >= newInput.timestamp) {
            return false;
        }

        return true;
    }

    @Override
    public int compareTo(Input input) {
        if (this.id < input.id) {
            return -1;
        } else if (this.id > input.id) {
            return 1;
        } else {
            return 0;
        }
    }

    private static final class InputComparator implements Comparator<Input> {
        @Override
        public int compare(Input input, Input t1) {
            return input.compareTo(t1);
        }
    }

    @Override
    public int hashCode() {
        return this.id;
    }

    @Override
    public boolean equals(Object o) {
        if (o == null) {
            return false;
        } else if (this == o) {
            return true;
        } else if (o instanceof Input) {
            return this.id == ((Input) o).id;
        } else {
            return false;
        }
    }

    @Override
    public String toString() {
        String str = String.format("Input: ID %d, direction %d, step %d", id, direction, step);
        return str;
    }
}
