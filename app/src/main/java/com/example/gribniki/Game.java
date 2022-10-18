package com.example.gribniki;

import android.graphics.Point;

import java.util.Arrays;
import java.util.Random;

public class Game {
    final private int MUSHROOMS_COUNT = 7;
    final private int BONUS_COUNT = 3;
    final private int TRAPS_COUNT = 2;
    final private int NOTHING_COUNT = 3;
    final int OBJECTS_COUNT = MUSHROOMS_COUNT + BONUS_COUNT + TRAPS_COUNT + NOTHING_COUNT;

    final int BUSH_WIDTH = 150;
    final int BUSH_HEIGHT = 150;

    final private int width, height;
    private int lives = 5, points = 0;
    private boolean isActive = true;

    final private Point[] coordinates = new Point[OBJECTS_COUNT];
    final private int[] objects = new int[OBJECTS_COUNT];
    private boolean[] opened = new boolean[OBJECTS_COUNT];

    final Random random;

    Game(int width, int height) {
        this.width = width;
        this.height = height;
        random = new Random();
        init();
    }

    private void init() {
        // Generate coordinates
        int x, y;
        for (int i = 0; i < OBJECTS_COUNT; ++i) {
            x = random.nextInt((width + 1 - BUSH_WIDTH / 2) - BUSH_WIDTH / 2) + BUSH_WIDTH / 2;
            y = (int) (random.nextInt((int) ((height - 2 * BUSH_HEIGHT) - 2 * height / 3f)) + 2 * height / 3f);
            coordinates[i] = new Point(x, y);
        }

        // Generate objects type
        for (int i = 0; i < MUSHROOMS_COUNT; ++i)
            objects[i] = ObjectType.MUSHROOM.getType();
        for (int i = MUSHROOMS_COUNT; i < MUSHROOMS_COUNT + BONUS_COUNT; ++i)
            objects[i] = ObjectType.BONUS.getType();
        for (int i = MUSHROOMS_COUNT + BONUS_COUNT; i < MUSHROOMS_COUNT + BONUS_COUNT + TRAPS_COUNT; ++i)
            objects[i] = ObjectType.TRAP.getType();
        for (int i = OBJECTS_COUNT - 1; i > OBJECTS_COUNT - 1 - NOTHING_COUNT; --i)
            objects[i] = ObjectType.NOTHING.getType();

        Arrays.fill(opened, Boolean.FALSE); // False - object not opened yet
    }

    public void restart() {
        init();
        lives = 5;
        points = 0;
        isActive = true;
    }

    // Getters
    public int getLives() {
        return lives;
    }

    public int getPoints() {
        return points;
    }

    public Point[] getCoordinates() {
        return coordinates;
    }

    public int[] getObjects() {
        return objects;
    }

    public boolean[] getOpened() {
        return opened;
    }

    public boolean isActive() {
        return isActive;
    }

    public Random getRandom() {
        return random;
    }

    // Setters
    public void setLives(int lives) {
        this.lives = lives;
    }

    public void setPoints(int points) {
        this.points = points;
    }

    public void setActive(boolean active) {
        isActive = active;
    }

    public void openObject(int index) {
        if (index > -1 && index < OBJECTS_COUNT) opened[index] = true;
    }
}
