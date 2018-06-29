package com.tianyi.zhang.multiplayer.snake.elements;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import static com.tianyi.zhang.multiplayer.snake.helpers.Constants.*;

/**
 * Immutable class encapsulating coordinates and direction of a snake,
 * as well as the last user input.
 */
public class Snake {
    public final int ID;
    public final List<Integer> COORDS;
    public final Input LAST_INPUT;
    public final boolean DEAD;

    public Snake(int id, int[] coords, Input lastInput) {
        ID = id;
        List<Integer> tmpCoords = new ArrayList<Integer>(coords.length);
        for (int i = 0; i < coords.length; ++i) {
            tmpCoords.add(new Integer(coords[i]));
        }
        COORDS = Collections.unmodifiableList(tmpCoords);
        LAST_INPUT = lastInput;
        DEAD = false;
    }

    public Snake(int id, List<Integer> coords, Input lastInput) {
        ID = id;
        if (coords.getClass().getSimpleName().equals("UnmodifiableCollection")) {
            COORDS = coords;
        } else {
            COORDS = Collections.unmodifiableList(coords);
        }
        LAST_INPUT = lastInput;
        DEAD = false;
    }

    public Snake(int id, List<Integer> coords, Input lastInput, boolean dead) {
        ID = id;
        if (coords.getClass().getSimpleName().equals("UnmodifiableCollection")) {
            COORDS = coords;
        } else {
            COORDS = Collections.unmodifiableList(coords);
        }
        LAST_INPUT = lastInput;
        DEAD = dead;
    }

    public Snake changeDirection(Input newInput) {
        if (!DEAD && LAST_INPUT.isValidNewInput(newInput)) {
            return new Snake(ID, COORDS, newInput);
        } else {
            return this;
        }
    }

    public Snake next() {
        if (DEAD) return this;

        int[] coords = new int[COORDS.size()];
        for (int i = 0; i < COORDS.size()-2; ++i) {
            coords[i+2] = COORDS.get(i).intValue();
        }
        int x0 = COORDS.get(0).intValue(), y0 = COORDS.get(1).intValue();
        switch (LAST_INPUT.direction) {
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
        return new Snake(ID, coords, LAST_INPUT);
    }

    public Snake die() {
        return new Snake(ID, COORDS, LAST_INPUT, true);
    }

    @Override
    public String toString() {
        String str = String.format("%s snake %d, direction %d, last input ID %d, head coordinates (%d, %d).",
                DEAD ? "Dead" : "Live", ID, LAST_INPUT.direction, LAST_INPUT.id, COORDS.get(0), COORDS.get(1));
        return str;
    }
}
