package com.shadowquest.world;

import java.util.Objects;

/** Computes the viewport's top-left world position without changing the world. */
public final class Camera {
    private final GameWorld world;
    private double x;
    private double y;

    public Camera(GameWorld world) {
        this.world = Objects.requireNonNull(world, "world");
    }

    public void follow(double targetX, double targetY, double viewportWidth, double viewportHeight) {
        if (!Double.isFinite(targetX) || !Double.isFinite(targetY)) {
            throw new IllegalArgumentException("Camera target must be finite.");
        }
        if (!Double.isFinite(viewportWidth) || viewportWidth <= 0
                || !Double.isFinite(viewportHeight) || viewportHeight <= 0) {
            throw new IllegalArgumentException("Viewport dimensions must be finite and positive.");
        }
        double maxX = Math.max(0, world.getWidth() - viewportWidth);
        double maxY = Math.max(0, world.getHeight() - viewportHeight);
        x = Math.max(0, Math.min(targetX - viewportWidth / 2, maxX));
        y = Math.max(0, Math.min(targetY - viewportHeight / 2, maxY));
    }

    public double getX() {
        return x;
    }

    public double getY() {
        return y;
    }

    public double worldToScreenX(double worldX) {
        return worldX - x;
    }

    public double worldToScreenY(double worldY) {
        return worldY - y;
    }
}
