package com.tianyi.zhang.multiplayer.snake.agents;

import com.esotericsoftware.kryonet.Listener;
import com.google.protobuf.InvalidProtocolBufferException;
import com.tianyi.zhang.multiplayer.snake.protobuf.generated.ClientPacket;
import com.tianyi.zhang.multiplayer.snake.protobuf.generated.ServerPacket;

import java.io.IOException;

public abstract class IAgent {
    public abstract void setListener(Listener listener);
    public abstract void broadcast(Listener listener) throws IOException;
    public abstract void lookForServer(Listener listener);
    public abstract void send(byte[] packet);
    public abstract void destroy();

    public static ServerPacket.Update parseServerUpdate(Object object) throws IllegalArgumentException {
        if (object instanceof byte[]) {
            try {
                return ServerPacket.Update.parseFrom((byte[]) object);
            } catch (InvalidProtocolBufferException e) {
                throw new IllegalArgumentException("Failed to parse object into ProtoBuf", e);
            }
        } else {
            throw new IllegalArgumentException("Attempting to parse an object that is not of type byte[]");
        }
    }

    public static ClientPacket.Message parseClientMessage(Object object) throws IllegalArgumentException {
        if (object instanceof byte[]) {
            try {
                return ClientPacket.Message.parseFrom((byte[]) object);
            } catch (InvalidProtocolBufferException e) {
                throw new IllegalArgumentException("Failed to parse object into ProtoBuf", e);
            }
        } else {
            throw new IllegalArgumentException("Attempting to parse an object that is not of type byte[]");
        }
    }
}
