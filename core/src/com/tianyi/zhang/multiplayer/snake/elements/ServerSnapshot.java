package com.tianyi.zhang.multiplayer.snake.elements;

public class ServerSnapshot {
    public void onTimeElapsed(int ms) {

    }

    public boolean shouldRenderNewFrame() {
        return true;
    }

    public void onClientInput(int clientId, int direction, int msAgo) {

    }

    public void onServerInput(int direction) {

    }

    public Snake[] getSnakes() {
        return null;
    }
}
