package com.tianyi.zhang.multiplayer.snake.agents;

import com.esotericsoftware.kryonet.Listener;
import com.google.protobuf.InvalidProtocolBufferException;
import com.tianyi.zhang.multiplayer.snake.agents.messages.Packet;

import java.io.IOException;

public abstract class IAgent {
    public abstract void setListener(Listener listener);
    public abstract void broadcast(Listener listener) throws IOException;
    public abstract void lookForServer(Listener listener);
    public abstract void send(Packet.Update update);
    public abstract void updateReturnTripTime();
    public abstract int getReturnTripTime();
    public abstract void destroy();

    public static Packet.Update parseReceived(Object object) throws IllegalArgumentException {
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
}
