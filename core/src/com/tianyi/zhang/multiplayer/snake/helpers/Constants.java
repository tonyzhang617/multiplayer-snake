package com.tianyi.zhang.multiplayer.snake.helpers;

public class Constants {
    public static final int TCP_PORT = 21345;
    public static final int UDP_PORT = 21346;

    public static final byte LEFT = 1;
    public static final byte RIGHT = 4;
    public static final byte UP = 2;
    public static final byte DOWN = 3;

    /**
     * Server would acknowledge inputs sent within LAG_TOLERANCE_MS
     */
    public static final int LAG_TOLERANCE_MS = 500;

    public static final int MOVE_EVERY_MS = 300;
    public static final int SEARCH_TIMEOUT_MS = 2000;

    public static final long SEED = Utils.getNanoTime();

    public static final int INITIAL_SNAKE_LENGTH = 3;
    public static final int MAX_FOOD_QUANTITY = 10;
    public static final int MIN_FOOD_QUANTITY = 3;

    public static final int WINDOW_WIDTH = 800, WINDOW_HEIGHT = 600;
    public static final int WIDTH = 40, HEIGHT = 30;

    public static final float BLOCK_LENGTH = 1f;
    public static final float BLOCK_OFFSET = 8f / 24f;

    public enum GameResult {
        WON, LOST
    }

    public static final String GAME_OVER = "Game over. You\'ll get better next time!";
    public static final String CONGRATS = "Congratulations! You won the game!";

    public static final String VERSION = "v0.9.0";
}
