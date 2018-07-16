package com.tianyi.zhang.multiplayer.snake.elements;

import com.tianyi.zhang.multiplayer.snake.helpers.Utils;
import com.tianyi.zhang.multiplayer.snake.protobuf.generated.ClientPacket;
import com.tianyi.zhang.multiplayer.snake.protobuf.generated.ServerPacket;

import java.util.*;

public abstract class Snapshot {
    public abstract boolean update();

    public void onClientInput(int direction) {

    }

    public void onServerInput(int direction) {

    }

    public void onClientMessage(int clientId, ClientPacket.Message message) {

    }

    public void onServerUpdate(ServerPacket.Update update) {

    }

    /**
     *
     * @param snakes
     * @return whether some snake has collided with itself, other snakes or wooden crates
     */
    public boolean collisionDetection(List<Snake> snakes) {
        SortedMap<Integer, Integer> positions = new TreeMap<Integer, Integer>();
        boolean result = false;
        for (int i = 0; i < snakes.size(); ++i) {
            if (!snakes.get(i).isDead()) {
                List<Integer> coordinates = snakes.get(i).getCoordinates();
                for (int j = 0; j < coordinates.size(); j += 2) {
                    Integer position = Utils.positionFromXy(coordinates.get(j), coordinates.get(j+1));
                    Integer count = positions.get(position);
                    if (count != null) {
                        positions.put(position, count + 1);
                    } else {
                        positions.put(position, 1);
                    }
                }
            }
        }
        for (int i = 0; i < snakes.size(); ++i) {
            if (!snakes.get(i).isDead()) {
                Integer headPosition = Utils.positionFromXy(snakes.get(i).getHeadX(), snakes.get(i).getHeadY());
                Integer count = positions.get(headPosition);
                if (count > 1) {
                    snakes.get(i).die();
                    result = true;
                }
            }
        }
        return result;
    }

    public abstract Grid getGrid();
}
