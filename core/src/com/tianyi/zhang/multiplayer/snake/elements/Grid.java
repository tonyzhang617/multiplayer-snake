package com.tianyi.zhang.multiplayer.snake.elements;

import java.util.ArrayList;
import java.util.List;

import static com.tianyi.zhang.multiplayer.snake.helpers.Constants.WIDTH;
import static com.tianyi.zhang.multiplayer.snake.helpers.Constants.HEIGHT;

public class Grid {
    public final long timestamp;
    public final List<Snake> snakes;
    public final Foods foods;

    private final int aliveCount;

    public enum Block {
        PLAYER_SNAKE_BODY, SNAKE_BODY, CRATE, FOOD, GROUND
    }

    private final List<List<Block>> blocks;

    public Grid(long timestamp, List<Snake> snakes, int playerIndex, Foods foods) {
        this.timestamp = timestamp;
        this.snakes = snakes;
        this.foods = foods;

        blocks = new ArrayList<List<Block>>(HEIGHT);
        for (int i = 0; i < HEIGHT; ++i) {
            blocks.add(new ArrayList<Block>(WIDTH));
            if (i == 0 || i == HEIGHT - 1) {
                for (int j = 0; j < WIDTH; ++j) {
                    blocks.get(i).add(Block.CRATE);
                }
            } else {
                for (int j = 0; j < WIDTH; ++j) {
                    if (j == 0 || j == WIDTH - 1) {
                        blocks.get(i).add(Block.CRATE);
                    } else {
                        blocks.get(i).add(Block.GROUND);
                    }
                }
            }
        }

        List<Integer> coords;
        coords = foods.getLocations();
        for (int i = 0; i < coords.size(); i += 2) {
            int x = coords.get(i), y = coords.get(i+1);
            blocks.get(y).set(x, Block.FOOD);
        }

        int count = 0;
        for (int i = 0; i < snakes.size(); ++i) {
            if (!snakes.get(i).isDead()) {
                count += 1;
            }
            if (i != playerIndex) {
                coords = snakes.get(i).getCoordinates();
                for (int j = 0; j < coords.size(); j += 2) {
                    int x = coords.get(j), y = coords.get(j+1);
                    if (x >= 0 && x < WIDTH && y >= 0 && y < HEIGHT) {
                        blocks.get(y).set(x, Block.SNAKE_BODY);
                    }
                }
            }
        }
        aliveCount = count;

        coords = snakes.get(playerIndex).getCoordinates();
        for (int j = 0; j < coords.size(); j += 2) {
            int x = coords.get(j), y = coords.get(j+1);
            if (x >= 0 && x < WIDTH && y >= 0 && y < HEIGHT) {
                blocks.get(y).set(x, Block.PLAYER_SNAKE_BODY);
            }
        }
    }

    public Block getBlockByCoordinate(int x, int y) {
        return blocks.get(y).get(x);
    }

    public boolean isSnakeDead(int index) {
        return snakes.get(index).isDead();
    }

    public int getAliveCount() {
        return aliveCount;
    }
}
