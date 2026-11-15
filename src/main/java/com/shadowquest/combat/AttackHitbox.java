package com.shadowquest.combat;

/**
 * Directional rectangular attack hitbox in world coordinates.
 * Evaluates AABB intersection against targets.
 */
public record AttackHitbox(double x, double y, double width, double height) {
    public AttackHitbox {
        if (!Double.isFinite(x) || !Double.isFinite(y) || !Double.isFinite(width) || !Double.isFinite(height)
                || width < 0 || height < 0) {
            throw new IllegalArgumentException("Hitbox coordinates and dimensions must be finite and non-negative.");
        }
    }

    public double right() {
        return x + width;
    }

    public double bottom() {
        return y + height;
    }

    /**
     * Determines whether this attack hitbox intersects a square entity bounding box.
     */
    public boolean intersects(double targetX, double targetY, double targetSize) {
        return x < targetX + targetSize && right() > targetX
                && y < targetY + targetSize && bottom() > targetY;
    }
}
