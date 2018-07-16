package com.tianyi.zhang.multiplayer.snake.elements;

import java.util.ArrayList;
import java.util.List;

import static com.tianyi.zhang.multiplayer.snake.helpers.Constants.WIDTH;
import static com.tianyi.zhang.multiplayer.snake.helpers.Constants.HEIGHT;

public class Grid {
    public final long timestamp;
    public final List<Snake> snakes;
    public final Foods foods;

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

        for (int i = 0; i < snakes.size(); ++i) {
            coords = snakes.get(i).getCoordinates();
            if (i != playerIndex) {
                for (int j = 0; j < coords.size(); j += 2) {
                    int x = coords.get(j), y = coords.get(j+1);
                    if (x >= 0 && x < WIDTH && y >= 0 && y < HEIGHT) {
                        blocks.get(y).set(x, Block.SNAKE_BODY);
                    }
                }
            }
        }

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
}
