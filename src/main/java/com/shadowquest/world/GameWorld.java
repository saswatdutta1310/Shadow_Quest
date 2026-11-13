package com.shadowquest.world;

import java.util.List;

/** Immutable rectangular world dimensions, independent of the visible viewport. */
public final class GameWorld {
    private final double width;
    private final double height;
    private final List<EnvironmentObject> objects;

    public GameWorld(double width, double height) {
        this(width, height, List.of());
    }

    public GameWorld(double width, double height, List<EnvironmentObject> objects) {
        if (!Double.isFinite(width) || width <= 0
                || !Double.isFinite(height) || height <= 0) {
            throw new IllegalArgumentException("World dimensions must be finite and positive.");
        }
        this.width = width;
        this.height = height;
        this.objects = List.copyOf(objects);
        for (var object : this.objects) {
            if (object.x() + object.width() > width || object.y() + object.height() > height) {
                throw new IllegalArgumentException("Environment object must fit inside the world.");
            }
        }
    }

    public List<EnvironmentObject> getObjects() {
        return objects;
    }

    public double getWidth() {
        return width;
    }

    public double getHeight() {
        return height;
    }
}
