package com.tianyi.zhang.multiplayer.snake.elements;

import com.tianyi.zhang.multiplayer.snake.helpers.Constants;

import java.util.ArrayList;
import java.util.List;

public class Grid {
    public static final int WIDTH = Constants.WIDTH;
    public static final int HEIGHT = Constants.HEIGHT;

    public enum Block {
        PLAYER_SNAKE_BODY, SNAKE_BODY, CRATE, FOOD, GROUND
    }

    private final List<List<Block>> blocks;

    public Grid(Snake[] snakes, int playerIndex, List<Integer> foods, List<Integer> obstacles) {
        blocks = new ArrayList<List<Block>>(HEIGHT);
        for (int i = 0; i < HEIGHT; ++i) {
            blocks.add(new ArrayList<Block>(WIDTH));
            for (int j = 0; j < WIDTH; ++j) {
                blocks.get(i).add(Block.GROUND);
            }
        }

        for (int i = 0; i < snakes.length; ++i) {
            List<Integer> coords = snakes[i].getCoordinates();
            if (i == playerIndex) {
                for (int j = 0; j < coords.size(); j += 2) {
                    int x = coords.get(j), y = coords.get(j+1);
                    if (x >= 0 && x < WIDTH && y >= 0 && y < HEIGHT) {
                        blocks.get(coords.get(j + 1)).set(coords.get(j), Block.PLAYER_SNAKE_BODY);
                    }
                }
            } else {
                for (int j = 0; j < coords.size(); j += 2) {
                    int x = coords.get(j), y = coords.get(j+1);
                    if (x >= 0 && x < WIDTH && y >= 0 && y < HEIGHT) {
                        blocks.get(coords.get(j + 1)).set(coords.get(j), Block.SNAKE_BODY);
                    }
                }
            }
        }
    }

    public Block getBlockByCoordinate(int x, int y) {
        return blocks.get(y).get(x);
    }
}
