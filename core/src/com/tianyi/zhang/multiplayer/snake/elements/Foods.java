package com.tianyi.zhang.multiplayer.snake.elements;

import com.tianyi.zhang.multiplayer.snake.helpers.Constants;

import java.util.*;

public class Foods {
    public static final int WIDTH = Constants.WIDTH;
    public static final int HEIGHT = Constants.HEIGHT;

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
        for (Snake s : snakes) {
            List<Integer> coords = s.getCoordinates();
            for (int i = 0; i < coords.size(); i += 2) {
                exclude.add(indexFromXy(coords.get(i), coords.get(i+1)));
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
        Integer headIndex = indexFromXy(snake.getHeadX(), snake.getHeadY());
        if (locations.contains(headIndex)) {
            locations.remove(headIndex);
            snake.grow();
        }
    }

    public boolean shouldGenerate() {
        return getQuantity() <= Constants.MIN_FOOD_QUANTITY;
    }

    public Integer indexFromXy(Integer x, Integer y) {
        return Integer.valueOf(y*WIDTH + x);
    }

    public Integer xFromIndex(int index) {
        return index - index / WIDTH * WIDTH;
    }

    public int yFromIndex(int index) {
        return index / WIDTH;
    }

    public List<Integer> getLocations() {
        List<Integer> result = new ArrayList<Integer>(locations.size() * 2);
        Iterator<Integer> iterator = locations.iterator();
        while (iterator.hasNext()) {
            Integer index = iterator.next();
            result.add(Integer.valueOf(xFromIndex(index)));
            result.add(Integer.valueOf(yFromIndex(index)));
        }
        return Collections.unmodifiableList(result);
    }

    public void setLocations(List<Integer> xyLocations) {
        this.locations.clear();
        for (int i = 0; i < xyLocations.size(); i += 2) {
            this.locations.add(indexFromXy(xyLocations.get(i), xyLocations.get(i+1)));
        }
    }
}
