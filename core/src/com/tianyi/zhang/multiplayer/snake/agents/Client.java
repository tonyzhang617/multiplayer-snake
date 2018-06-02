package com.tianyi.zhang.multiplayer.snake.agents;

import com.badlogic.gdx.Gdx;
import com.esotericsoftware.kryonet.Listener;
import com.tianyi.zhang.multiplayer.snake.helpers.Constants;

import java.io.IOException;
import java.net.InetAddress;

public class Client extends com.esotericsoftware.kryonet.Client implements IAgent {
    @Override
    public void init() {
        start();
    }

    @Override
    public void broadcast(Listener listener) {

    }

    @Override
    public void lookForServer(Listener listener) {
        addListener(listener);
        new Thread(new Runnable() {
            @Override
            public void run() {
                InetAddress serverAddress = discoverHost(Constants.UDP_PORT, 5000);
                if (serverAddress != null) {
                    Gdx.app.log("LOOK FOR SERVER ERROR", "Server discovered at " + serverAddress.getHostAddress());
                    try {
                        connect(5000, serverAddress, Constants.TCP_PORT, Constants.UDP_PORT);
                    } catch (IOException e) {
                        Gdx.app.error("LOOK FOR SERVER ERROR", e.getMessage());
                    }
                } else {
                    Gdx.app.log("LOOK FOR SERVER ERROR", "No server found");
                }
            }
        }).start();
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
