package com.tianyi.zhang.multiplayer.snake.helpers;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Camera;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.tianyi.zhang.multiplayer.snake.elements.Grid;

import static com.tianyi.zhang.multiplayer.snake.elements.Grid.Block.*;
import static com.tianyi.zhang.multiplayer.snake.elements.Grid.Block.CRATE;
import static com.tianyi.zhang.multiplayer.snake.elements.Grid.Block.GROUND;
import static com.tianyi.zhang.multiplayer.snake.helpers.Constants.*;

public class Utils {
    private Utils() {

    }

    public static long getNanoTime() {
        return System.nanoTime();
    }

    public static Integer positionFromXy(Integer x, Integer y) {
        return y*WIDTH + x;
    }

    public static Integer xFromPosition(Integer position) {
        return position - yFromPosition(position) * WIDTH;
    }

    public static Integer yFromPosition(Integer position) {
        return position / WIDTH;
    }

    public static Texture newTextureWithLinearFilter(String imagePath) {
        Texture texture = new Texture(Gdx.files.internal(imagePath));
        texture.setFilter(Texture.TextureFilter.Linear, Texture.TextureFilter.Linear);
        return texture;
    }

    public static void clear() {
        Gdx.gl.glClearColor(0, 0, 0, 1);
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);
    }

    public static void renderGrid(Grid grid, Camera camera, SpriteBatch spriteBatch) {
        camera.update();

        spriteBatch.setProjectionMatrix(camera.combined);
        spriteBatch.begin();

        for (int y = Constants.HEIGHT - 1; y >= 0; --y) {
            for (int x = 0; x < Constants.WIDTH; ++x) {
                if (grid.checkCoordinate(x, y, PLAYER_SNAKE_BODY)) {
                    spriteBatch.draw(AssetManager.INSTANCE.getSpriteByType(PLAYER_SNAKE_BODY), x - Constants.BLOCK_OFFSET, y, BLOCK_LENGTH + BLOCK_OFFSET, BLOCK_LENGTH + BLOCK_OFFSET);
                } else if (grid.checkCoordinate(x, y, SNAKE_BODY)) {
                    spriteBatch.draw(AssetManager.INSTANCE.getSpriteByType(SNAKE_BODY), x - Constants.BLOCK_OFFSET, y, BLOCK_LENGTH + BLOCK_OFFSET, BLOCK_LENGTH + BLOCK_OFFSET);
                } else {
                    if (grid.checkCoordinate(x, y, GROUND)) {
                        spriteBatch.draw(AssetManager.INSTANCE.getSpriteByType(GROUND), x, y, BLOCK_LENGTH, BLOCK_LENGTH);
                    }
                    if (grid.checkCoordinate(x, y, FOOD)) {
                        spriteBatch.draw(AssetManager.INSTANCE.getSpriteByType(FOOD), x - Constants.BLOCK_OFFSET, y, BLOCK_LENGTH + BLOCK_OFFSET, BLOCK_LENGTH + BLOCK_OFFSET);
                    }
                    if (grid.checkCoordinate(x, y, CRATE)) {
                        spriteBatch.draw(AssetManager.INSTANCE.getSpriteByType(CRATE), x - Constants.BLOCK_OFFSET, y, BLOCK_LENGTH + BLOCK_OFFSET, BLOCK_LENGTH + BLOCK_OFFSET);
                    }
                    if (grid.checkCoordinate(x, y, DEAD_SNAKE_BODY)) {
                        spriteBatch.draw(AssetManager.INSTANCE.getSpriteByType(DEAD_SNAKE_BODY), x - Constants.BLOCK_OFFSET, y, BLOCK_LENGTH + BLOCK_OFFSET, BLOCK_LENGTH + BLOCK_OFFSET);
                    }
                    if (grid.checkCoordinate(x, y, DEAD_PLAYER_SNAKE_BODY)) {
                        spriteBatch.draw(AssetManager.INSTANCE.getSpriteByType(DEAD_PLAYER_SNAKE_BODY), x - Constants.BLOCK_OFFSET, y, BLOCK_LENGTH + BLOCK_OFFSET, BLOCK_LENGTH + BLOCK_OFFSET);
                    }
                }
            }
        }

        spriteBatch.end();
    }
}
