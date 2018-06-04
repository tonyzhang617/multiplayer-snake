package com.tianyi.zhang.multiplayer.snake.agents;

import com.esotericsoftware.kryonet.Listener;
import com.tianyi.zhang.multiplayer.snake.agents.messages.Packet;

import java.io.IOException;

public interface IAgent {
    void init();
    void setListener(Listener listener);
    void broadcast(Listener listener) throws IOException;
    void lookForServer(Listener listener);
    void send(Packet.Update update);
    Packet.Update parseReceived(Object object) throws IllegalArgumentException;
    void destroy();
}
