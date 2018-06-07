package com.tianyi.zhang.multiplayer.snake.elements;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static com.tianyi.zhang.multiplayer.snake.helpers.Constants.*;

/**
 * Immutable class encapsulating coordinates and direction of a snake,
 * as well as a counter for step and a counter for input.
 */
public class Snake {
    private final List<Short> _coords;
    private final byte _direction;

    private final int _step;
    private final int _inputIndex;

    public Snake(Short[] coords, byte direction, int step, int inputIndex) {
        this._coords = Collections.unmodifiableList(new ArrayList<Short>(Arrays.asList(coords)));
        this._direction = direction;
        this._step = step;
        this._inputIndex = inputIndex;
    }

    private Snake(List<Short> coords, byte direction, int step, int inputIndex) {
        this._coords = coords;
        this._direction = direction;
        this._step = step;
        this._inputIndex = inputIndex;
    }

    public Snake changeDirection(byte direction, int inputIndex) {
        return new Snake(_coords, direction, _step, inputIndex);
    }

    public Snake nextStep() {
        Short[] coords = new Short[_coords.size()];
        int i;
        for (i = 0; i < _coords.size()-2; ++i) {
            coords[i+2] = _coords.get(i);
        }
        Short x0 = _coords.get(0), y0 = _coords.get(1);
        switch (_direction) {
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
        return new Snake(coords, _direction, _step+1, _inputIndex);
    }

    public List<Short> getCoords() {
        return _coords;
    }

    public byte getDirection() {
        return _direction;
    }

    public int getStep() {
        return _step;
    }

    public int getInputIndex() {
        return _inputIndex;
    }
}
