package com.shadowquest.world;

import java.util.Objects;

/** World-space visual bounds; only the three solid kinds expose a smaller hitbox. */
public record EnvironmentObject(Kind kind, double x, double y, double width, double height) {
    public enum Kind { TREE, ROCK, LARGE_ROOT, SMALL_ROOT, GRASS, BUSH, PATH, MIST, FIREFLY }

    public EnvironmentObject {
        Objects.requireNonNull(kind, "kind");
        if (!Double.isFinite(x) || !Double.isFinite(y) || x < 0 || y < 0
                || !Double.isFinite(width) || !Double.isFinite(height) || width <= 0 || height <= 0
                || !Double.isFinite(x + width) || !Double.isFinite(y + height)) {
            throw new IllegalArgumentException("Environment bounds must be finite and positive.");
        }
    }

    public boolean isSolid() {
        return kind == Kind.TREE || kind == Kind.ROCK || kind == Kind.LARGE_ROOT;
    }

    public Hitbox hitbox() {
        return switch (kind) {
            case TREE -> new Hitbox(x + width * .40, y + height * .65, width * .20, height * .25);
            case ROCK -> new Hitbox(x + width * .12, y + height * .25, width * .76, height * .65);
            case LARGE_ROOT -> new Hitbox(x + width * .08, y + height * .20, width * .84, height * .65);
            default -> throw new IllegalStateException("Decorations have no collision area.");
        };
    }

    public record Hitbox(double x, double y, double width, double height) {
        public double right() { return x + width; }
        public double bottom() { return y + height; }
    }
}
