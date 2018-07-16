package com.tianyi.zhang.multiplayer.snake.elements;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Camera;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.Sprite;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.tianyi.zhang.multiplayer.snake.helpers.Constants;

import java.util.HashMap;
import java.util.Map;

import static com.tianyi.zhang.multiplayer.snake.elements.Grid.Block.*;
import static com.tianyi.zhang.multiplayer.snake.helpers.Constants.BLOCK_LENGTH;
import static com.tianyi.zhang.multiplayer.snake.helpers.Constants.BLOCK_OFFSET;

public enum GameRenderer {
    INSTANCE;

    private Map<Grid.Block, Sprite> spriteMap;
    private SpriteBatch batch;

    private GameRenderer() {
        Sprite playerSnakeBody = new Sprite(newTextureWithLinearFilter("player_snake_body.png"));
        Sprite snakeBody = new Sprite(newTextureWithLinearFilter("snake_body.png"));
        Sprite food = new Sprite(newTextureWithLinearFilter("cake.png"));
        Sprite ground = new Sprite(newTextureWithLinearFilter("ground.png"));
        Sprite crate = new Sprite(newTextureWithLinearFilter("wooden_crate.png"));

        spriteMap = new HashMap<Grid.Block, Sprite>();
        spriteMap.put(PLAYER_SNAKE_BODY, playerSnakeBody);
        spriteMap.put(SNAKE_BODY, snakeBody);
        spriteMap.put(FOOD, food);
        spriteMap.put(GROUND, ground);
        spriteMap.put(CRATE, crate);

        batch = new SpriteBatch();
    }

    private static Texture newTextureWithLinearFilter(String imagePath) {
        Texture texture = new Texture(Gdx.files.internal(imagePath));
        texture.setFilter(Texture.TextureFilter.Linear, Texture.TextureFilter.Linear);
        return texture;
    }

    public void clear() {
        Gdx.gl.glClearColor(0, 0, 0, 1);
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);
    }

    public void render(Grid grid, Camera camera) {
        camera.update();

        batch.setProjectionMatrix(camera.combined);
        batch.begin();

        for (int y = Constants.HEIGHT - 1; y >= 0; --y) {
            for (int x = 0; x < Constants.WIDTH; ++x) {
                switch (grid.getBlockByCoordinate(x, y)) {
                    case PLAYER_SNAKE_BODY:
                        batch.draw(spriteMap.get(PLAYER_SNAKE_BODY), x - Constants.BLOCK_OFFSET, y, BLOCK_LENGTH + BLOCK_OFFSET, BLOCK_LENGTH + BLOCK_OFFSET);
                        break;
                    case SNAKE_BODY:
                        batch.draw(spriteMap.get(SNAKE_BODY), x - Constants.BLOCK_OFFSET, y, BLOCK_LENGTH + BLOCK_OFFSET, BLOCK_LENGTH + BLOCK_OFFSET);
                        break;
                    case FOOD:
                        batch.draw(spriteMap.get(GROUND), x, y, BLOCK_LENGTH, BLOCK_LENGTH);
                        batch.draw(spriteMap.get(FOOD), x - Constants.BLOCK_OFFSET, y, BLOCK_LENGTH + BLOCK_OFFSET, BLOCK_LENGTH + BLOCK_OFFSET);
                        break;
                    case GROUND:
                        batch.draw(spriteMap.get(GROUND), x, y, BLOCK_LENGTH, BLOCK_LENGTH);
                        break;
                    case CRATE:
                        batch.draw(spriteMap.get(CRATE), x - Constants.BLOCK_OFFSET, y, BLOCK_LENGTH + BLOCK_OFFSET, BLOCK_LENGTH + BLOCK_OFFSET);
                        break;
                }
            }
        }

        batch.end();
    }

    public void dispose() {
        batch.dispose();
    }
}
