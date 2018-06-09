package com.tianyi.zhang.multiplayer.snake.elements;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import static com.tianyi.zhang.multiplayer.snake.helpers.Constants.*;

/**
 * Immutable class encapsulating coordinates and direction of a snake,
 * as well as the id of the input.
 */
public class Snake {
    public final int ID;
    public final List<Short> COORDS;
    public final byte DIRECTION;
    public final int INPUT_ID;

    public Snake(int id, short[] coords, byte direction, int inputId) {
        ID = id;
        List<Short> tmpCoords = new ArrayList<Short>(coords.length);
        for (int i = 0; i < coords.length; ++i) {
            tmpCoords.set(i, new Short(coords[i]));
        }
        COORDS = Collections.unmodifiableList(tmpCoords);
        DIRECTION = direction;
        INPUT_ID = inputId;
    }

    private Snake(int id, List<Short> coords, byte direction, int inputId) {
        ID = id;
        COORDS = coords;
        DIRECTION = direction;
        INPUT_ID = inputId;
    }

    public Snake changeDirection(byte direction, int inputId) {
        if (DIRECTION == direction || DIRECTION + direction == 5 || inputId <= INPUT_ID) {
            return this;
        }
        return new Snake(ID, COORDS, direction, inputId);
    }

    public Snake next() {
        short[] coords = new short[COORDS.size()];
        for (int i = 0; i < COORDS.size()-2; ++i) {
            coords[i+2] = COORDS.get(i).shortValue();
        }
        short x0 = COORDS.get(0).shortValue(), y0 = COORDS.get(1).shortValue();
        switch (DIRECTION) {
            case LEFT:
                --x0;
                break;
            case UP:
                ++y0;
                break;
            case RIGHT:
                ++x0;
                break;
            case DOWN:
                --y0;
                break;
        }
        coords[0] = x0;
        coords[1] = y0;
        return new Snake(ID, coords, DIRECTION, INPUT_ID);
    }
}
