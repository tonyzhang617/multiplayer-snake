package com.tianyi.zhang.multiplayer.snake.agents;

import com.esotericsoftware.kryo.NotNull;
import com.esotericsoftware.kryonet.Listener;

import java.io.IOException;

public interface IAgent {
    void init();
    void broadcast(Listener listener) throws IOException;
    void lookForServer(Listener listener);
    void send();
    void receive();
    void destroy();
}
