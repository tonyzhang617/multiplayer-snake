package com.tianyi.zhang.multiplayer.snake.agents;

import com.badlogic.gdx.Gdx;
import com.esotericsoftware.kryonet.Listener;
import com.tianyi.zhang.multiplayer.snake.agents.messages.Packet;
import com.tianyi.zhang.multiplayer.snake.helpers.Constants;

import java.io.IOException;
import java.net.InetAddress;

public class Client extends IAgent {
    private com.esotericsoftware.kryonet.Client client;
    private Listener listener;

    public Client() {
        client = new com.esotericsoftware.kryonet.Client();
        client.getKryo().register(byte[].class);
        client.start();
    }

    @Override
    public void setListener(Listener l) {
        client.addListener(l);
        if (this.listener != null) {
            client.removeListener(this.listener);
        }
        this.listener = l;
    }

    @Override
    public void broadcast(Listener listener) {

    }

    @Override
    public void lookForServer(Listener listener) {
        setListener(listener);
        new Thread(new Runnable() {
            @Override
            public void run() {
                InetAddress serverAddress = client.discoverHost(Constants.UDP_PORT, 5000);
                if (serverAddress != null) {
                    Gdx.app.log("LOOK FOR SERVER ERROR", "Server discovered at " + serverAddress.getHostAddress());
                    try {
                        client.connect(5000, serverAddress, Constants.TCP_PORT, Constants.UDP_PORT);
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
    public void send(Packet.Update update) {
        client.sendTCP(update.toByteArray());
    }

    @Override
    public void destroy() {
        client.close();
    }
}
