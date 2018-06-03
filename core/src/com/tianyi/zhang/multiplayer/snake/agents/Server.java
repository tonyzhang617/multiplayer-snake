package com.tianyi.zhang.multiplayer.snake.agents;

import com.esotericsoftware.kryonet.Listener;
import com.google.protobuf.InvalidProtocolBufferException;
import com.tianyi.zhang.multiplayer.snake.agents.messages.Packet;
import com.tianyi.zhang.multiplayer.snake.helpers.Constants;

import java.io.IOException;

public class Server extends com.esotericsoftware.kryonet.Server implements IAgent {
    @Override
    public void init() {
        getKryo().register(byte[].class);
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
