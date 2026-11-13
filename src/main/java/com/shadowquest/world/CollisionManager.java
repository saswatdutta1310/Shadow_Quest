package com.shadowquest.world;

/** Continuous swept AABB movement with sliding; independent of JavaFX. */
public final class CollisionManager {
    private CollisionManager() { }
    public record Position(double x, double y) { }
    private record Contact(double time, boolean horizontal, boolean vertical, double faceX, double faceY) { }

    public static Position resolve(GameWorld world, double oldX, double oldY,
                                   double targetX, double targetY, double size) {
        double x = oldX, y = oldY;
        double dx = targetX - oldX, dy = targetY - oldY;
        // Each contact removes at least one movement component; two suffice in 2D.
        for (int pass = 0; pass < 2 && (dx != 0 || dy != 0); pass++) {
            Contact nearest = null;
            for (var object : world.getObjects()) {
                if (!object.isSolid()) continue;
                Contact hit = sweep(x, y, dx, dy, size, object.hitbox());
                if (hit != null && (nearest == null || hit.time() < nearest.time())) {
                    nearest = hit;
                } else if (hit != null && nearest != null && hit.time() == nearest.time()) {
                    nearest = new Contact(hit.time(), hit.horizontal() || nearest.horizontal(),
                            hit.vertical() || nearest.vertical(),
                            hit.horizontal() ? hit.faceX() : nearest.faceX(),
                            hit.vertical() ? hit.faceY() : nearest.faceY());
                }
            }
            if (nearest == null) { x += dx; y += dy; break; }
            x += dx * nearest.time();
            y += dy * nearest.time();
            // Snap the blocked axis exactly to its face; rounded penetration must not
            // make next frame's sweep start inside the obstacle and miss that contact.
            if (nearest.horizontal()) x = nearest.faceX();
            if (nearest.vertical()) y = nearest.faceY();
            dx = nearest.horizontal() ? 0 : dx * (1 - nearest.time());
            dy = nearest.vertical() ? 0 : dy * (1 - nearest.time());
        }
        return new Position(x, y);
    }

    /**
     * Checks whether a straight line-of-sight between two points is clear of any solid obstacles.
     */
    public static boolean hasLineOfSight(GameWorld world, double x1, double y1, double x2, double y2) {
        if (world == null) return true;
        double dx = x2 - x1, dy = y2 - y1;
        if (Math.hypot(dx, dy) < 1e-6) return true;
        for (var object : world.getObjects()) {
            if (!object.isSolid()) continue;
            if (sweep(x1, y1, dx, dy, 0, object.hitbox()) != null) {
                return false;
            }
        }
        return true;
    }

    private static Contact sweep(double x, double y, double dx, double dy, double size,
                                 EnvironmentObject.Hitbox b) {
        // Expand the obstacle by body size, then sweep the body's top-left as a point.
        double left = b.x() - size, top = b.y() - size;
        if (dx == 0 && (x <= left || x >= b.right())) return null;
        if (dy == 0 && (y <= top || y >= b.bottom())) return null;
        double xEntry = dx == 0 ? Double.NEGATIVE_INFINITY
                : ((dx > 0 ? left : b.right()) - x) / dx;
        double xExit = dx == 0 ? Double.POSITIVE_INFINITY
                : ((dx > 0 ? b.right() : left) - x) / dx;
        double yEntry = dy == 0 ? Double.NEGATIVE_INFINITY
                : ((dy > 0 ? top : b.bottom()) - y) / dy;
        double yExit = dy == 0 ? Double.POSITIVE_INFINITY
                : ((dy > 0 ? b.bottom() : top) - y) / dy;
        double entry = Math.max(xEntry, yEntry);
        double exit = Math.min(xExit, yExit);
        if (entry < 0 || entry > 1 || entry >= exit) return null;
        return new Contact(entry, xEntry >= yEntry, yEntry >= xEntry,
                dx > 0 ? left : b.right(), dy > 0 ? top : b.bottom());
    }
}
