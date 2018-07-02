package com.tianyi.zhang.multiplayer.snake.helpers;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Camera;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.Sprite;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.tianyi.zhang.multiplayer.snake.elements.Grid;

import java.util.Map;

import static com.tianyi.zhang.multiplayer.snake.elements.Grid.Block.PLAYER_SNAKE_BODY;

public class RenderingUtils {
    public static Texture newTextureWithLinearFilter(String imagePath) {
        Texture texture = new Texture(Gdx.files.internal(imagePath));
        texture.setFilter(Texture.TextureFilter.Linear, Texture.TextureFilter.Linear);
        return texture;
    }

    public static void clear() {
        Gdx.gl.glClearColor(0, 0, 0, 1);
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);
    }

    public static void renderGrid(Grid grid, Camera camera, SpriteBatch batch, Map<Grid.Block, Sprite> sprites) {
        camera.update();

        batch.setProjectionMatrix(camera.combined);
        batch.begin();

        for (int y = Grid.HEIGHT - 1; y >= 0; --y) {
            for (int x = 0; x < Grid.WIDTH; ++x) {
                switch (grid.getBlockByCoordinate(x, y)) {
                    case PLAYER_SNAKE_BODY:
                    case SNAKE_BODY:
                        sprites.get(PLAYER_SNAKE_BODY).setPosition(x - Constants.BLOCK_OFFSET, y);
                        sprites.get(PLAYER_SNAKE_BODY).draw(batch);
                        break;
                }
            }
        }

        batch.end();
    }
}
