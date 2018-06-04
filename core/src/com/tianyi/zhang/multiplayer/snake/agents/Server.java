package com.tianyi.zhang.multiplayer.snake.agents;

import com.esotericsoftware.kryonet.Connection;
import com.esotericsoftware.kryonet.Listener;
import com.google.protobuf.InvalidProtocolBufferException;
import com.tianyi.zhang.multiplayer.snake.agents.messages.Packet;
import com.tianyi.zhang.multiplayer.snake.helpers.Constants;

import java.io.IOException;
import java.util.ArrayList;

public class Server extends com.esotericsoftware.kryonet.Server implements IAgent {
    private Listener listener;
    private ArrayList<Connection> clients;

    @Override
    public void init() {
        getKryo().register(byte[].class);
        start();
        clients = new ArrayList<Connection>();
    }

    @Override
    public void setListener(Listener l) {
        addListener(l);
        if (this.listener != null) {
            removeListener(this.listener);
        }
        this.listener = l;
    }

    @Override
    public void broadcast(Listener listener) throws IOException {
        setListener(listener);
        addListener(new Listener() {
            @Override
            public void connected(Connection connection) {
                clients.add(connection);
            }

            @Override
            public void disconnected(Connection connection) {
                if (clients.contains(connection)) clients.remove(connection);
            }
        });
        bind(Constants.TCP_PORT, Constants.UDP_PORT);
    }

    @Override
    public void lookForServer(Listener listener) {

    }

    @Override
    public void send(Packet.Update update) {
        sendToAllUDP(update.toByteArray());
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
