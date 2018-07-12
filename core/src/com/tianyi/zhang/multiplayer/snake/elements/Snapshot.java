package com.tianyi.zhang.multiplayer.snake.elements;

import com.tianyi.zhang.multiplayer.snake.protobuf.generated.ClientPacket;
import com.tianyi.zhang.multiplayer.snake.protobuf.generated.ServerPacket;

public abstract class Snapshot {
    public abstract boolean update();

    public void onClientInput(int direction) {

    }

    public void onServerInput(int direction) {

    }

    public void onClientMessage(int clientId, ClientPacket.Message message) {

    }

    public void onServerUpdate(ServerPacket.Update update) {

    }

    public abstract Grid getGrid();
}
