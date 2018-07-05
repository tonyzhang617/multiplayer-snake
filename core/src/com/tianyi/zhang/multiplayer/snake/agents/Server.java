package com.tianyi.zhang.multiplayer.snake.agents;

import com.esotericsoftware.kryonet.FrameworkMessage;
import com.esotericsoftware.kryonet.Listener;
import com.tianyi.zhang.multiplayer.snake.helpers.Constants;

import java.io.IOException;

public class Server extends IAgent {
    private static final String TAG = Server.class.getCanonicalName();
    private com.esotericsoftware.kryonet.Server server;
    private Listener listener;

    public Server() {
        server = new com.esotericsoftware.kryonet.Server();
        server.getKryo().register(byte[].class);
        server.getKryo().register(FrameworkMessage.Ping.class);
        server.start();
    }

    @Override
    public void setListener(Listener l) {
        server.addListener(l);
        if (this.listener != null) {
            server.removeListener(this.listener);
        }
        this.listener = l;
    }

    @Override
    public void broadcast(Listener listener) throws IOException {
        setListener(listener);
        server.bind(Constants.TCP_PORT, Constants.UDP_PORT);
    }

    @Override
    public void lookForServer(Listener listener) {

    }

    @Override
    public void send(byte[] packet) {
        server.sendToAllUDP(packet);
    }

    @Override
    public void destroy() {
        server.close();
    }
}
