package com.tianyi.zhang.multiplayer.snake.elements;

public abstract class Snapshot {
    public abstract boolean update();

    public void onClientInput(int direction) {

    }

    public void onServerInput(int direction) {

    }

    public void onClientUpdate(int id, int direction, int msAgo) {

    }

    public void onServerUpdate(Snake[] snakes, int msAgo) {

    }

    public abstract Snake[] getSnakes();
}
