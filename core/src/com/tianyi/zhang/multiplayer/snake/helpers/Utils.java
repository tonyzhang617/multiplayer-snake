package com.tianyi.zhang.multiplayer.snake.helpers;

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
}
