package com.tianyi.zhang.multiplayer.snake.agents;

import com.badlogic.gdx.Gdx;
import com.esotericsoftware.kryonet.Listener;
import com.google.protobuf.InvalidProtocolBufferException;
import com.tianyi.zhang.multiplayer.snake.agents.messages.Packet;
import com.tianyi.zhang.multiplayer.snake.helpers.Constants;

import java.io.IOException;
import java.net.InetAddress;

public class Client extends com.esotericsoftware.kryonet.Client implements IAgent {
    @Override
    public void init() {
        getKryo().register(byte[].class);
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
    public void send(Packet.Update update) {

    }

    @Override
    public Packet.Update parseReceived(Object object) throws IllegalArgumentException {
        if (object instanceof byte[]) {
            try {
                return Packet.Update.parseFrom((byte[]) object);
            } catch (InvalidProtocolBufferException e) {
                throw new IllegalArgumentException("Failed to parse object into ProtoBuf", e);
            }
        } else {
            throw new IllegalArgumentException("Attempting to parse an object that is not of type byte[]");
        }
    }

    @Override
    public void destroy() {
        close();
    }
}
