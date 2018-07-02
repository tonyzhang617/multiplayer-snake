package com.tianyi.zhang.multiplayer.snake.elements;

import com.tianyi.zhang.multiplayer.snake.agents.messages.Packet;

public abstract class Snapshot {
    public abstract boolean update();

    public void onClientInput(int direction) {

    }

    public void onServerInput(int direction) {

    }

    public void onClientUpdate(Packet.Update update) {

    }

    public void onServerUpdate(Packet.Update update) {

    }

    public abstract Snake[] getSnakes();

    public abstract Grid getGrid();
}
