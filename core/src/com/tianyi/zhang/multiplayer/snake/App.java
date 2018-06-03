package com.tianyi.zhang.multiplayer.snake;

import com.badlogic.gdx.ApplicationAdapter;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.kotcrab.vis.ui.VisUI;
import com.tianyi.zhang.multiplayer.snake.agents.Client;
import com.tianyi.zhang.multiplayer.snake.agents.IAgent;
import com.tianyi.zhang.multiplayer.snake.agents.Server;
import com.tianyi.zhang.multiplayer.snake.states.GameState;
import com.tianyi.zhang.multiplayer.snake.states.client.LookForServerState;
import com.tianyi.zhang.multiplayer.snake.states.server.BroadcastState;

import java.io.IOException;
import java.util.Stack;

public class App extends ApplicationAdapter {
	protected Stack<GameState> stateStack;
	protected IAgent agent;

	// TODO: remove this variable
    private boolean isServer = false;

	SpriteBatch batch;
	Texture img;

	public IAgent getAgent() {
	    return agent;
    }

    public synchronized GameState currentState() {
	    return stateStack.peek();
    }

	@Override
	public void create () {
        VisUI.load();

	    stateStack = new Stack<GameState>();
	    if (isServer) {
	        agent = new Server();
	        agent.init();
	        stateStack.push(new BroadcastState(this));
        } else {
	        agent = new Client();
	        agent.init();
	        stateStack.push(new LookForServerState(this));
        }
	}

	@Override
	public void render () {
	    stateStack.peek().render();
//		batch.begin();
//		batch.draw(img, 0, 0);
//		batch.end();
	}
	
	@Override
	public void dispose () {
	    agent.destroy();
	    while (!stateStack.empty()) {
	        stateStack.pop().destroy();
        }

        VisUI.dispose();
//		batch.dispose();
//		img.dispose();
	}
}
