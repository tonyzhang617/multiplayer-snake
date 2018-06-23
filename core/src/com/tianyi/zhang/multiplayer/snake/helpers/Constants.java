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
    public static final int LAG_TOLERANCE_MS = 1500;

    public static final int MOVE_EVERY_MS = 500;
    public static final int CLIENT_SEND_EVERY_MS = 50;
    public static final int SERVER_SEND_EVERY_MS = 80;
}
