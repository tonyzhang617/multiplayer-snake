package com.tianyi.zhang.multiplayer.snake;

import com.badlogic.gdx.Application;
import com.badlogic.gdx.Game;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.kotcrab.vis.ui.VisUI;
import com.tianyi.zhang.multiplayer.snake.agents.Client;
import com.tianyi.zhang.multiplayer.snake.agents.IAgent;
import com.tianyi.zhang.multiplayer.snake.agents.Server;
import com.tianyi.zhang.multiplayer.snake.states.GameState;
import com.tianyi.zhang.multiplayer.snake.states.client.LookForServerState;
import com.tianyi.zhang.multiplayer.snake.states.server.BroadcastState;

import java.util.Stack;

public class App extends Game {
	protected Stack<GameState> stateStack;
	protected IAgent agent;

	// TODO: remove this variable
    private boolean isServer = false;

	SpriteBatch batch;
	Texture img;

	public IAgent getAgent() {
	    return agent;
    }

	@Override
	public void create () {
        Gdx.app.setLogLevel(Application.LOG_DEBUG);
        VisUI.load();

	    stateStack = new Stack<GameState>();
	    if (isServer) {
	        agent = new Server();
	        agent.init();
	        pushState(new BroadcastState(this));
        } else {
	        agent = new Client();
	        agent.init();
	        pushState(new LookForServerState(this));
        }
	}

	@Override
	public void render () {
	    super.render();
//		batch.begin();
//		batch.draw(img, 0, 0);
//		batch.end();
	}
	
	@Override
	public void dispose () {
	    agent.destroy();
	    while (!stateStack.empty()) {
	        stateStack.pop().dispose();
        }

        VisUI.dispose();
//		batch.dispose();
//		img.dispose();
	}

	public void pushState(GameState gameState) {
	    stateStack.push(gameState);
	    setScreen(gameState);
    }

    public void popState() {
	    stateStack.pop().dispose();
    }

    public void replaceState(GameState gameState) {
	    stateStack.pop().dispose();
	    pushState(gameState);
    }
}
