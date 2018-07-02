package com.tianyi.zhang.multiplayer.snake.helpers;

public class Constants {
    public static final int TCP_PORT = 21345;
    public static final int UDP_PORT = 21346;

    public static final byte NO_INPUT = 0;
    public static final byte LEFT = 1;
    public static final byte RIGHT = 4;
    public static final byte UP = 2;
    public static final byte DOWN = 3;

    /**
     * Server would acknowledge inputs sent within LAG_TOLERANCE_MS
     */
    public static final int LAG_TOLERANCE_MS = 200;

    public static final int MOVE_EVERY_MS = 333;
    public static final int UPDATE_AFTER_INACTIVE_MS = MOVE_EVERY_MS * 3;
    public static final int CLIENT_SEND_EVERY_MS = 50;
    public static final int SERVER_SEND_EVERY_MS = 80;

    public static final int INITIAL_SNAKE_LENGTH = 3;

    public static final int WINDOW_WIDTH = 640, WINDOW_HEIGHT = 640;
    public static final int WIDTH = 50, HEIGHT = 50;
    public static final int UNIT_WIDTH = WINDOW_WIDTH / WIDTH, UNIT_HEIGHT = WINDOW_HEIGHT / HEIGHT;

    public static final float BLOCK_LENGTH = (8f + 24f) / 24f;
    public static final float BLOCK_OFFSET = 8f / 24f;
}
