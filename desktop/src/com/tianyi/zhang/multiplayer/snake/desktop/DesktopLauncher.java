package com.tianyi.zhang.multiplayer.snake.desktop;

import com.badlogic.gdx.Files;
import com.badlogic.gdx.backends.lwjgl.LwjglApplication;
import com.badlogic.gdx.backends.lwjgl.LwjglApplicationConfiguration;
import com.tianyi.zhang.multiplayer.snake.App;
import com.tianyi.zhang.multiplayer.snake.helpers.Constants;

public class DesktopLauncher {
    public static void main(String[] arg) {
        LwjglApplicationConfiguration config = new LwjglApplicationConfiguration();
        config.title = "snakeLANd";
        config.addIcon("ic_game.png", Files.FileType.Internal);
        config.width = Constants.WINDOW_WIDTH;
        config.height = Constants.WINDOW_HEIGHT;
        new LwjglApplication(new App(), config);
    }
}
