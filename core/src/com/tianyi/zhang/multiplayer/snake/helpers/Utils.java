package com.tianyi.zhang.multiplayer.snake.helpers;

import static com.tianyi.zhang.multiplayer.snake.helpers.Constants.WIDTH;

public class Utils {
    private static final Object timeLock = new Object();

    /**
     * Thread-safe implementation of System.nanoTime() function
     * @return a long number denoting elapsed nanoseconds since an arbitrary point of time
     */
    public static long getNanoTime() {
        synchronized (timeLock) {
            return System.nanoTime();
        }
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
}
