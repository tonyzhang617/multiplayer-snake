package com.tianyi.zhang.multiplayer.snake.helpers;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.Sprite;
import com.tianyi.zhang.multiplayer.snake.elements.Grid;

import java.util.HashMap;
import java.util.Map;

import static com.tianyi.zhang.multiplayer.snake.elements.Grid.Block.*;
import static com.tianyi.zhang.multiplayer.snake.elements.Grid.Block.CRATE;

public enum AssetManager {
    INSTANCE;

    private final Map<Grid.Block, Sprite> spriteMap;
    private final Texture title;

    private final Color backgroundColor;

    private AssetManager() {
        Sprite playerSnakeBody = new Sprite(Utils.newTextureWithLinearFilter("player_snake_body.png"));
        Sprite snakeBody = new Sprite(Utils.newTextureWithLinearFilter("snake_body.png"));
        Sprite food = new Sprite(Utils.newTextureWithLinearFilter("cake.png"));
        Sprite ground = new Sprite(Utils.newTextureWithLinearFilter("ground.png"));
        Sprite crate = new Sprite(Utils.newTextureWithLinearFilter("wooden_crate.png"));

        spriteMap = new HashMap<Grid.Block, Sprite>();
        spriteMap.put(PLAYER_SNAKE_BODY, playerSnakeBody);
        spriteMap.put(SNAKE_BODY, snakeBody);
        spriteMap.put(FOOD, food);
        spriteMap.put(GROUND, ground);
        spriteMap.put(CRATE, crate);

        title = Utils.newTextureWithLinearFilter("game_title.png");

        backgroundColor = new Color(0xc08048ff);
    }

    public Sprite getSpriteByType(Grid.Block type) {
        if (type == null) return null;
        return spriteMap.get(type);
    }

    public Texture getTitleTexture() {
        return title;
    }

    public Color getBackgroundColor() {
        return backgroundColor;
    }
}
