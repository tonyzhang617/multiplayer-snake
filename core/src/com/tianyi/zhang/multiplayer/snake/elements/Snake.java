package com.tianyi.zhang.multiplayer.snake.elements;

import com.tianyi.zhang.multiplayer.snake.helpers.Constants;
import com.tianyi.zhang.multiplayer.snake.protobuf.generated.ServerPacket;

import java.util.Collections;
import java.util.LinkedList;
import java.util.List;

public class Snake {
    public final int id;
    private final List<Integer> coords;
    private int lastDirection;
    private Input lastInput;
    private boolean isDead;

    public Snake(int id, int[] coords, int lastDirection, Input input) {
        this.id = id;
        this.coords = new LinkedList<Integer>();
        for (int i = 0; i < coords.length; ++i) {
            this.coords.add(new Integer(coords[i]));
        }
        this.lastDirection = lastDirection;
        this.lastInput = input;
        this.isDead = false;
    }

    public Snake(int id, List<Integer> coords, int lastDirection, Input input) {
        this.id = id;
        this.coords = new LinkedList<Integer>(coords);
        this.lastDirection = lastDirection;
        this.lastInput = input;
        this.isDead = false;
    }

    public Snake(int id, int headX, int headY, int length, Input input) {
        this.id = id;
        this.lastInput = input;

        this.coords = new LinkedList<Integer>();
        int x = headX, y = headY;
        for (int i = 0; i < length; ++i) {
            coords.add(Integer.valueOf(x));
            coords.add(Integer.valueOf(y));
            switch (input.direction) {
                case Constants.LEFT:
                    x += 1;
                    break;
                case Constants.UP:
                    y -= 1;
                    break;
                case Constants.RIGHT:
                    x -= 1;
                    break;
                case Constants.DOWN:
                    y += 1;
                    break;
            }
        }
        this.lastDirection = input.direction;
        this.isDead = false;
    }

    public static Snake fromProtoSnake(ServerPacket.Update.PSnake pSnake) {
        Input input = new Input(pSnake.getInputDirection(), pSnake.getInputId(), pSnake.getInputTimestamp());
        Snake snake = new Snake(pSnake.getId(), pSnake.getCoordsList(), pSnake.getLastDirection(), input);
        return snake;
    }

    public ServerPacket.Update.PSnake.Builder toProtoSnake() {
        ServerPacket.Update.PSnake.Builder snakeBuilder = ServerPacket.Update.PSnake.newBuilder();
        snakeBuilder.setId(id).setLastDirection(lastDirection).addAllCoords(coords).setInputId(lastInput.id).setInputDirection(lastInput.direction).setInputTimestamp(lastInput.timestamp);
        return snakeBuilder;
    }

    public Snake(Snake snake) {
        this.id = snake.id;
        this.coords = new LinkedList<Integer>(snake.coords);
        this.lastInput = snake.lastInput;
        this.lastDirection = snake.lastDirection;
        this.isDead = snake.isDead;
    }

    public void forward() {
        if (!isDead) {
            lastDirection = lastInput.direction;

            int size = coords.size();
            coords.remove(size - 1);
            coords.remove(size - 2);
            int x0 = coords.get(0).intValue(), y0 = coords.get(1).intValue();
            switch (lastInput.direction) {
                case Constants.LEFT:
                    --x0;
                    break;
                case Constants.UP:
                    ++y0;
                    break;
                case Constants.RIGHT:
                    ++x0;
                    break;
                case Constants.DOWN:
                    --y0;
                    break;
            }
            coords.add(0, y0);
            coords.add(0, x0);
        }
    }

    public List<Integer> getCoordinates() {
        return Collections.unmodifiableList(coords);
    }

    public void die() {
        this.isDead = true;
    }

    public void handleInput(Input input) {
        if (!isDead && lastInput.isValidNewInput(input) && lastDirection + input.direction != 5) {
            this.lastInput = input;
        }
    }

    public Input getLastInput() {
        return lastInput;
    }

    @Override
    public String toString() {
        String str = String.format("%s snake %d, direction %d, last input ID %d, head coordinates (%d, %d).",
                isDead ? "Dead" : "Live", id, lastInput.direction, lastInput.id, coords.get(0), coords.get(1));
        return str;
    }
}
