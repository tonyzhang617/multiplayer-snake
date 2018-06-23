package com.tianyi.zhang.multiplayer.snake.elements;

import java.util.concurrent.atomic.AtomicBoolean;

public class Input implements Comparable<Input> {
    public final int direction;
    public final int id;
    public final long timestamp;
    public final int step;

    private AtomicBoolean isAck;

    public Input(int direction, int id, long timestamp, int step, boolean isAck) {
        this.direction = direction;
        this.id = id;
        this.timestamp = timestamp;
        this.step = step;
        this.isAck = new AtomicBoolean(isAck);
    }

    public boolean isAcknowledged() {
        return isAck.get();
    }

    public void setAcknowledged(boolean isAcknowledged) {
        this.isAck.set(isAcknowledged);
    }

    public boolean isValidNewInput(Input newInput) {
        if (newInput == null) {
            return false;
        }

        if (this.direction == newInput.direction || this.direction + newInput.direction == 5 || this.id >= newInput.id
                || this.step > newInput.step || this.timestamp >= newInput.timestamp) {
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
}
