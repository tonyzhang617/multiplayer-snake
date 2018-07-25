package com.tianyi.zhang.multiplayer.snake.elements;

import com.tianyi.zhang.multiplayer.snake.helpers.Constants;
import com.tianyi.zhang.multiplayer.snake.helpers.Utils;

import java.util.*;

import static com.tianyi.zhang.multiplayer.snake.helpers.Constants.WIDTH;
import static com.tianyi.zhang.multiplayer.snake.helpers.Constants.HEIGHT;

public class Foods {
    private final SortedSet<Integer> locations;
    private final Random random;

    public Foods() {
        locations = new TreeSet<Integer>();
        random = new Random(Constants.SEED);
    }

    public Foods(Foods other) {
        locations = new TreeSet<Integer>(other.locations);
        random = new Random(Constants.SEED);
    }

    public int getQuantity() {
        return locations.size();
    }

    /**
     *
     * @param exclude an ascending sorted array of Integers to be excluded from the random generation
     * @return
     */
    public Integer generateHelper(List<Integer> exclude) {
        Integer randomInt = Integer.valueOf(random.nextInt(HEIGHT * WIDTH - exclude.size()));
        for (Integer e : exclude) {
            if (randomInt.compareTo(e) < 0) {
                break;
            }
            randomInt += 1;
        }
        return randomInt;
    }

    public void generate(List<Snake> snakes) {
        List<Integer> exclude = new LinkedList<Integer>();
        for (int i = 0; i < Constants.WIDTH; ++i) {
            exclude.add(Utils.positionFromXy(i, 0));
            exclude.add(Utils.positionFromXy(i, Constants.HEIGHT - 1));
        }
        for (int i = 1; i < Constants.HEIGHT - 1; ++i) {
            exclude.add(Utils.positionFromXy(0, i));
            exclude.add(Utils.positionFromXy(Constants.WIDTH - 1, i));
        }
        for (Snake s : snakes) {
            List<Integer> coords = s.getCoordinates();
            for (int i = 0; i < coords.size(); i += 2) {
                exclude.add(Utils.positionFromXy(coords.get(i), coords.get(i+1)));
            }
        }
        exclude.addAll(locations);
        Collections.sort(exclude);
        for (int i = 1; i <= Constants.MAX_FOOD_QUANTITY - getQuantity(); ++i) {
            Integer newLoc = generateHelper(exclude);
            locations.add(newLoc);
            int size = exclude.size();
            for (int j = 0; j <= size; ++j) {
                if (j == size) {
                    exclude.add(newLoc);
                } else if (newLoc < exclude.get(j)) {
                    exclude.add(j, newLoc);
                    break;
                }
            }
        }
    }

    public void consumedBy(Snake snake) {
        Integer headIndex = Utils.positionFromXy(snake.getHeadX(), snake.getHeadY());
        if (locations.contains(headIndex)) {
            locations.remove(headIndex);
            snake.grow();
        }
    }

    public boolean shouldGenerate() {
        return getQuantity() <= Constants.MIN_FOOD_QUANTITY;
    }

    public List<Integer> getLocations() {
        List<Integer> result = new ArrayList<Integer>(locations.size() * 2);
        Iterator<Integer> iterator = locations.iterator();
        while (iterator.hasNext()) {
            Integer position = iterator.next();
            result.add(Utils.xFromPosition(position));
            result.add(Utils.yFromPosition(position));
        }
        return Collections.unmodifiableList(result);
    }

    public List<Integer> getLinearPositions() {
        List<Integer> positions = new ArrayList<Integer>(locations);
        return Collections.unmodifiableList(positions);
    }

    public void setLocations(List<Integer> xyLocations) {
        this.locations.clear();
        for (int i = 0; i < xyLocations.size(); i += 2) {
            this.locations.add(Utils.positionFromXy(xyLocations.get(i), xyLocations.get(i+1)));
        }
    }
}
