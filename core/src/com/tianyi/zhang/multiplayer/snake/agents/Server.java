package com.tianyi.zhang.multiplayer.snake.agents;

import com.badlogic.gdx.Gdx;
import com.esotericsoftware.kryonet.Listener;
import com.tianyi.zhang.multiplayer.snake.helpers.Constants;

import java.io.IOException;

public class Server extends com.esotericsoftware.kryonet.Server implements IAgent {
    @Override
    public void init() {
        start();
    }

    @Override
    public void broadcast(Listener listener) throws IOException {
        addListener(listener);
        bind(Constants.TCP_PORT, Constants.UDP_PORT);
    }

    @Override
    public void lookForServer(Listener listener) {

    }

    @Override
    public void send() {

    }

    @Override
    public void receive() {

    }

    @Override
    public void destroy() {
        close();
    }
}
