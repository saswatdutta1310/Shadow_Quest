package com.shadowquest.render;

import com.shadowquest.enemy.DarkArcher;
import com.shadowquest.enemy.Enemy;
import com.shadowquest.enemy.GiantGoblin;
import com.shadowquest.enemy.Goblin;
import com.shadowquest.enemy.ShadowKnight;
import com.shadowquest.enemy.ShadowLord;
import com.shadowquest.enemy.ShadowWolf;
import com.shadowquest.enemy.Wraith;
import com.shadowquest.items.HealthPotion;
import com.shadowquest.items.PowerUpItem;
import com.shadowquest.items.SwordItem;
import com.shadowquest.player.Player;
import com.shadowquest.world.Camera;
import com.shadowquest.world.EnvironmentObject;
import com.shadowquest.world.GameWorld;
import com.shadowquest.world.Portal;
import com.shadowquest.world.ShadowCastle;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import javafx.geometry.VPos;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.image.Image;
import javafx.scene.paint.Color;
import javafx.scene.paint.CycleMethod;
import javafx.scene.paint.LinearGradient;
import javafx.scene.paint.RadialGradient;
import javafx.scene.paint.Stop;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.scene.text.TextAlignment;

/** Hand-painted 2D fantasy environment and character renderer for Mystic Forest gameplay. */
public final class WorldRenderer {
    // --- Pre-loaded hand-painted visual assets for Arin ---
    private static final Image IMG_ARIN_STAND = loadImage("/assets/cutscene/characters/arin_stand.png");
    private static final Image IMG_ARIN_WALK1 = loadImage("/assets/cutscene/characters/arin_walk1.png");
    private static final Image IMG_ARIN_WALK2 = loadImage("/assets/cutscene/characters/arin_walk2.png");
    private static final Image IMG_ARIN_WALK3 = loadImage("/assets/cutscene/characters/arin_walk3.png");
    private static final Image IMG_ARIN_WALK4 = loadImage("/assets/cutscene/characters/arin_walk4.png");
    private static final Image[] WALK_CYCLE = {IMG_ARIN_WALK1, IMG_ARIN_WALK2, IMG_ARIN_WALK3, IMG_ARIN_WALK4};
    private static final double WALK_FPS = 7.0;

    /** Gameplay sprite dimensions proportional to character aspect ratio (548x1103). */
    private static final double SPRITE_HEIGHT = 80.0;
    private static final double SPRITE_WIDTH = 40.0;

    private final GameWorld world;
    private final List<EnvironmentObject> scenery;

    public WorldRenderer(GameWorld world) {
        this.world = world;
        scenery = world.getObjects().stream().sorted(Comparator.comparingDouble(
                object -> object.isSolid() ? object.hitbox().bottom() : object.y() + object.height())).toList();
    }

    public void render(GraphicsContext g, Camera camera, Player player, double width, double height) {
        render(g, camera, player, List.of(), null, width, height);
    }

    public void render(GraphicsContext g, Camera camera, Player player, List<Enemy> enemies, double width, double height) {
        render(g, camera, player, enemies, null, width, height);
    }

    public void render(GraphicsContext g, Camera camera, Player player, List<Enemy> enemies, SwordItem swordItem, double width, double height) {
        render(g, camera, player, enemies, swordItem, List.of(), null, width, height);
    }

    public void render(GraphicsContext g, Camera camera, Player player, List<Enemy> enemies,
                       SwordItem swordItem, List<HealthPotion> potions, PowerUpItem powerUp,
                       double width, double height) {
        render(g, camera, player, enemies, swordItem, potions, powerUp, null, 0.0, width, height);
    }

    public void render(GraphicsContext g, Camera camera, Player player, List<Enemy> enemies,
                       SwordItem swordItem, List<HealthPotion> potions, PowerUpItem powerUp,
                       Portal portal, double swordUpgradeMessageTimer,
                       double width, double height) {
        g.save();
        try {
            g.translate(-camera.getX(), -camera.getY());
            boolean isCastle = (world.getWidth() <= 2400.0 && world.getHeight() <= 1400.0 && world.getWidth() != 2880);
            if (isCastle) {
                // Dark gothic castle flagstone flooring
                g.setFill(Color.web("#0d081a"));
                g.fillRect(0, 0, world.getWidth(), world.getHeight());
                for (int y = (int) (camera.getY() / 80) * 80; y < camera.getY() + height; y += 80) {
                    for (int x = (int) (camera.getX() / 100) * 100; x < camera.getX() + width; x += 100) {
                        g.setFill(Color.web((x / 100 + y / 80) % 2 == 0 ? "#161029" : "#110b20"));
                        g.fillRect(x + 2, y + 2, 96, 76);
                        g.setStroke(Color.web("#22183d", 0.60));
                        g.strokeRect(x + 2, y + 2, 96, 76);
                    }
                }
            } else {
                g.setFill(Color.web("#071a18"));
                g.fillRect(0, 0, world.getWidth(), world.getHeight());
                // Soft, atmospheric forest floor with subtle, non-intrusive mossy mottling
                for (int y = (int) (camera.getY() / 120) * 120; y < camera.getY() + height; y += 120) {
                    for (int x = (int) (camera.getX() / 150) * 150; x < camera.getX() + width; x += 150) {
                        g.setFill(Color.web((x / 150 + y / 120) % 2 == 0 ? "#0a2623" : "#081f1d"));
                        g.fillOval(x - 10, y - 10, 100, 60);
                    }
                }
            }
            for (var object : scenery) {
                if (visible(object, camera, width, height) && !object.isSolid()
                        && object.kind() != EnvironmentObject.Kind.MIST
                        && object.kind() != EnvironmentObject.Kind.FIREFLY) draw(g, object);
            }

            // Depth sorting: collect all visible entities (ground sword, potions, power-up, portal, player, and active enemies)
            List<DrawableEntity> entities = new ArrayList<>();
            if (swordItem != null && !swordItem.isPickedUp() && visible(swordItem.getX() - 10, swordItem.getY() - 10,
                    48, 48, camera, width, height)) {
                entities.add(new DrawableEntity(swordItem.getY() + 14, () -> drawGroundSword(g, swordItem)));
            }
            if (potions != null) {
                for (HealthPotion potion : potions) {
                    if (potion != null && !potion.isPickedUp() && visible(potion.getX() - 10, potion.getY() - 10,
                            36, 36, camera, width, height)) {
                        entities.add(new DrawableEntity(potion.getY() + 16, () -> drawHealthPotion(g, potion)));
                    }
                }
            }
            if (powerUp != null && !powerUp.isPickedUp() && visible(powerUp.getX() - 20, powerUp.getY() - 20,
                    64, 64, camera, width, height)) {
                entities.add(new DrawableEntity(powerUp.getY() + 24, () -> drawPowerUpItem(g, powerUp)));
            }
            if (portal != null && portal.isActive() && visible(portal.getX() - 50, portal.getY() - 80,
                    100, 160, camera, width, height)) {
                entities.add(new DrawableEntity(portal.getY() + 30, () -> drawPortal(g, portal, player)));
            }
            if (player != null) {
                entities.add(new DrawableEntity(player.getY() + player.getSize(), () -> drawPlayer(g, player)));
            }
            if (enemies != null) {
                for (Enemy enemy : enemies) {
                    if (enemy != null && enemy.isActive() && visible(enemy.getX() - 60, enemy.getY() - 60,
                            enemy.getSize() + 120, enemy.getSize() + 120, camera, width, height)) {
                        entities.add(new DrawableEntity(enemy.getY() + enemy.getSize(), () -> drawEnemy(g, enemy)));
                    }
                }
            }
            entities.sort(Comparator.comparingDouble(DrawableEntity::groundY));

            int entityIdx = 0;
            for (var object : scenery) {
                if (!object.isSolid() || !visible(object, camera, width, height)) continue;
                double objBaseY = object.hitbox().bottom();
                while (entityIdx < entities.size() && entities.get(entityIdx).groundY() <= objBaseY) {
                    entities.get(entityIdx).drawAction().run();
                    entityIdx++;
                }
                draw(g, object);
            }
            while (entityIdx < entities.size()) {
                entities.get(entityIdx).drawAction().run();
                entityIdx++;
            }

            for (var object : scenery) {
                if (visible(object, camera, width, height)
                        && (object.kind() == EnvironmentObject.Kind.MIST
                        || object.kind() == EnvironmentObject.Kind.FIREFLY)) draw(g, object);
            }
        } finally {
            g.restore();
        }
        // Atmospheric edge vignette framing the camera view
        g.setFill(new RadialGradient(0, 0, .5, .5, .75, true, CycleMethod.NO_CYCLE,
                new Stop(0, Color.TRANSPARENT),
                new Stop(0.65, Color.TRANSPARENT),
                new Stop(1.0, Color.web("#010b12", .45))));
        g.fillRect(0, 0, width, height);

        // Minimal, readable Player Health Bar
        if (player != null) {
            drawHealthBar(g, player);
        }

        // Dedicated Boss Health Bar (for Giant Goblin in Level 1 or Shadow Lord in Level 2)
        Enemy activeBoss = null;
        if (enemies != null) {
            for (Enemy e : enemies) {
                if (e instanceof ShadowLord lord && lord.isAlive()) {
                    activeBoss = lord;
                    break;
                } else if (e instanceof GiantGoblin giant && giant.isActive() && giant.isAlive()) {
                    activeBoss = giant;
                    break;
                }
            }
        }
        if (activeBoss != null) {
            drawBossHealthBar(g, activeBoss, width);
        }

        // Sword Upgrade notification banner
        if (swordUpgradeMessageTimer > 0) {
            drawSwordUpgradeNotification(g, width);
        }
    }

    private record DrawableEntity(double groundY, Runnable drawAction) {}

    private static boolean visible(double x, double y, double w, double h, Camera c, double viewW, double viewH) {
        return x < c.getX() + viewW && x + w > c.getX()
                && y < c.getY() + viewH && y + h > c.getY();
    }

    private static boolean visible(EnvironmentObject o, Camera c, double w, double h) {
        return o.x() < c.getX() + w && o.x() + o.width() > c.getX()
                && o.y() < c.getY() + h && o.y() + o.height() > c.getY();
    }

    /**
     * Draws Arin using hand-painted character sprites, animated walk cycle,
     * directional facing, and contact shadow aligned with the 24x24 domain hitbox.
     */
    private static void drawPlayer(GraphicsContext g, Player p) {
        // Soft ground contact shadow centered under player's feet
        double shadowW = 34.0;
        double shadowH = 14.0;
        double shadowX = p.getX() + (p.getSize() - shadowW) / 2.0;
        double shadowY = p.getY() + p.getSize() - 9.0;
        g.setFill(Color.web("#000e15", 0.42));
        g.fillOval(shadowX, shadowY, shadowW, shadowH);

        // Select sprite and bobbing based on movement state
        Image sprite;
        double bob = 0;
        if (p.isMoving()) {
            double walkPhase = p.getAnimationTime() * WALK_FPS;
            int frameIdx = ((int) walkPhase) % WALK_CYCLE.length;
            sprite = walkFrame(frameIdx);
            double cyclePos = (walkPhase % WALK_CYCLE.length) / WALK_CYCLE.length;
            bob = Math.abs(Math.sin(cyclePos * Math.PI * 2)) * 2.0;
        } else {
            sprite = IMG_ARIN_STAND;
        }

        // Align feet with the bottom of the 24x24 hitbox: y + size
        double spriteX = p.getX() + (p.getSize() - SPRITE_WIDTH) / 2.0;
        double spriteY = p.getY() + p.getSize() - SPRITE_HEIGHT;

        g.save();
        try {
            double drawX = spriteX;
            double drawY = spriteY - bob;

            // Invulnerability i-frame flicker
            if (p.isInvulnerable() && !p.isDamageFlashing()) {
                if (((int) (p.getInvulnerabilityTimer() * 16)) % 2 == 0) {
                    g.setGlobalAlpha(0.60);
                }
            }

            if (!p.isFacingRight()) {
                g.translate(drawX + SPRITE_WIDTH, drawY);
                g.scale(-1, 1);
                drawImage(g, sprite, 0, 0, SPRITE_WIDTH, SPRITE_HEIGHT);
                if (p.isSwordEquipped()) {
                    drawEquippedSword(g, p, 0, 0);
                }
                if (p.isDamageFlashing()) {
                    g.setFill(Color.web("#ff2222", 0.45));
                    g.fillRoundRect(0, 0, SPRITE_WIDTH, SPRITE_HEIGHT, 8, 8);
                }
            } else {
                drawImage(g, sprite, drawX, drawY, SPRITE_WIDTH, SPRITE_HEIGHT);
                if (p.isSwordEquipped()) {
                    drawEquippedSword(g, p, drawX, drawY);
                }
                if (p.isDamageFlashing()) {
                    g.setFill(Color.web("#ff2222", 0.45));
                    g.fillRoundRect(drawX, drawY, SPRITE_WIDTH, SPRITE_HEIGHT, 8, 8);
                }
            }
        } finally {
            g.restore();
        }
    }

    private static void drawHealthBar(GraphicsContext g, Player p) {
        double barX = 24.0;
        double barY = 20.0;
        double barW = 180.0;
        double barH = 18.0;

        // Shadow frame
        g.setFill(Color.web("#000000", 0.60));
        g.fillRoundRect(barX - 2, barY - 2, barW + 4, barH + 4, 6, 6);

        // Dark background plate
        g.setFill(Color.web("#0d131f"));
        g.fillRoundRect(barX, barY, barW, barH, 4, 4);

        // Dynamic health fill
        double maxHp = Math.max(1.0, p.getMaxHealth());
        double currHp = Math.max(0.0, Math.min(maxHp, p.getHealth()));
        double fillRatio = currHp / maxHp;
        double fillW = Math.max(0, (barW - 4) * fillRatio);

        if (fillW > 0) {
            Color topColor;
            Color bottomColor;
            if (fillRatio > 0.5) {
                topColor = Color.web("#4ade80");
                bottomColor = Color.web("#15803d");
            } else if (fillRatio > 0.25) {
                topColor = Color.web("#facc15");
                bottomColor = Color.web("#ca8a04");
            } else {
                topColor = Color.web("#ef4444");
                bottomColor = Color.web("#991b1b");
            }
            LinearGradient hpGrad = new LinearGradient(
                    barX + 2, barY + 2, barX + 2, barY + barH - 2,
                    false, CycleMethod.NO_CYCLE,
                    new Stop(0.0, topColor),
                    new Stop(1.0, bottomColor)
            );
            g.setFill(hpGrad);
            g.fillRoundRect(barX + 2, barY + 2, fillW, barH - 4, 3, 3);
        }

        // Gold border or flashing red border on damage
        if (p.isDamageFlashing()) {
            g.setStroke(Color.web("#ff3838"));
            g.setLineWidth(2.0);
            g.strokeRoundRect(barX - 1, barY - 1, barW + 2, barH + 2, 5, 5);
        } else {
            g.setStroke(Color.web("#d4af37", 0.85));
            g.setLineWidth(1.5);
            g.strokeRoundRect(barX, barY, barW, barH, 4, 4);
        }

        // Clear health text overlay
        g.setFont(Font.font("System", FontWeight.BOLD, 11));
        g.setTextAlign(TextAlignment.CENTER);
        g.setTextBaseline(VPos.CENTER);
        g.setFill(Color.web("#000000", 0.90));
        String hpText = "HP: " + (int) Math.ceil(currHp) + " / " + (int) maxHp;
        g.fillText(hpText, barX + barW / 2.0 + 1, barY + barH / 2.0 + 1);
        g.setFill(Color.web("#f3e6bf"));
        g.fillText(hpText, barX + barW / 2.0, barY + barH / 2.0);

        // Attack power indicator badge
        double badgeX = barX + barW + 12.0;
        double badgeY = barY;
        double badgeW = p.getAttackBonus() > 0 ? (p.isSwordUpgraded() ? 172.0 : 156.0) : 88.0;
        double badgeH = barH;

        g.setFill(Color.web("#000000", 0.60));
        g.fillRoundRect(badgeX - 2, badgeY - 2, badgeW + 4, badgeH + 4, 6, 6);
        g.setFill(Color.web("#0d131f"));
        g.fillRoundRect(badgeX, badgeY, badgeW, badgeH, 4, 4);

        if (p.getAttackBonus() > 0) {
            g.setStroke(Color.web("#f59e0b", 0.90));
            g.setLineWidth(1.5);
            g.strokeRoundRect(badgeX, badgeY, badgeW, badgeH, 4, 4);

            g.setFont(Font.font("System", FontWeight.BOLD, 11));
            g.setTextAlign(TextAlignment.CENTER);
            g.setTextBaseline(VPos.CENTER);
            g.setFill(Color.web("#000000", 0.90));
            String atkText = p.isSwordUpgraded() ? "⚔ ATK: " + p.getAttackPower() + " (Upgraded Sword +5)" : "⚔ ATK: " + p.getAttackPower() + " (+8 Power Up)";
            g.fillText(atkText, badgeX + badgeW / 2.0 + 1, badgeY + badgeH / 2.0 + 1);
            g.setFill(Color.web("#facc15"));
            g.fillText(atkText, badgeX + badgeW / 2.0, badgeY + badgeH / 2.0);
        } else {
            g.setStroke(Color.web("#475569", 0.80));
            g.setLineWidth(1.2);
            g.strokeRoundRect(badgeX, badgeY, badgeW, badgeH, 4, 4);

            g.setFont(Font.font("System", FontWeight.BOLD, 11));
            g.setTextAlign(TextAlignment.CENTER);
            g.setTextBaseline(VPos.CENTER);
            g.setFill(Color.web("#000000", 0.90));
            String atkText = "⚔ ATK: " + p.getAttackPower();
            g.fillText(atkText, badgeX + badgeW / 2.0 + 1, badgeY + badgeH / 2.0 + 1);
            g.setFill(Color.web("#cbd5e1"));
            g.fillText(atkText, badgeX + badgeW / 2.0, badgeY + badgeH / 2.0);
        }
    }

    private static void drawHealthPotion(GraphicsContext g, HealthPotion potion) {
        double px = potion.getX();
        double py = potion.getY();

        // Soft ground contact shadow
        g.setFill(Color.web("#000e15", 0.45));
        g.fillOval(px - 1, py + 15, 22, 8);

        // Mystical ruby shimmer aura
        double pulse = 0.35 + 0.15 * Math.sin(System.currentTimeMillis() * 0.006);
        g.setFill(Color.web("#ef4444", pulse * 0.35));
        g.fillOval(px - 6, py - 4, 32, 28);

        g.save();
        try {
            g.translate(px, py);

            // Glass bottle body (rounded potion flask)
            g.setFill(Color.web("#b91c1c")); // Deep ruby healing elixir
            g.fillRoundRect(2, 6, 16, 14, 8, 8);

            // Shimmer highlight inside liquid
            g.setFill(Color.web("#f87171"));
            g.fillOval(4, 8, 5, 8);

            // Glass bottle outline
            g.setStroke(Color.web("#fee2e2", 0.75));
            g.setLineWidth(1.2);
            g.strokeRoundRect(2, 6, 16, 14, 8, 8);

            // Bottle neck
            g.setFill(Color.web("#fee2e2", 0.85));
            g.fillRect(7, 2, 6, 5);

            // Wooden cork stopper
            g.setFill(Color.web("#92400e"));
            g.fillRoundRect(6, 0, 8, 3, 2, 2);

            // Healing cross insignia
            g.setFill(Color.web("#ffffff", 0.90));
            g.fillRect(9, 10, 2, 6);
            g.fillRect(7, 12, 6, 2);
        } finally {
            g.restore();
        }
    }

    private static void drawPowerUpItem(GraphicsContext g, PowerUpItem powerUp) {
        double px = powerUp.getX();
        double py = powerUp.getY();

        // Soft ground shadow
        g.setFill(Color.web("#000e15", 0.50));
        g.fillOval(px - 4, py + 26, 40, 12);

        // Radiant pulsing golden halo
        double time = System.currentTimeMillis() * 0.005;
        double pulse = 0.40 + 0.20 * Math.sin(time);
        g.setFill(Color.web("#f59e0b", pulse * 0.45));
        g.fillOval(px - 14, py - 10, 60, 50);

        g.save();
        try {
            double floatY = Math.sin(time * 2.0) * 3.5;
            g.translate(px + 16, py + 14 + floatY);

            // Rotating diamond runic star
            g.rotate(time * 25.0);
            g.setFill(Color.web("#fbbf24"));
            g.fillRect(-10, -10, 20, 20);

            g.rotate(45.0);
            g.setFill(Color.web("#f59e0b"));
            g.fillRect(-9, -9, 18, 18);

            // Brilliant white energy core
            g.setFill(Color.web("#ffffff"));
            g.fillOval(-5, -5, 10, 10);

            g.restore();
            g.save();

            // Floating power-up label
            g.setFont(Font.font("System", FontWeight.BOLD, 10));
            g.setTextAlign(TextAlignment.CENTER);
            g.setTextBaseline(VPos.CENTER);
            g.setFill(Color.web("#000000", 0.85));
            g.fillText("⚔ +8 ATK POWER UP", px + 17, py - 11 + floatY);
            g.setFill(Color.web("#facc15"));
            g.fillText("⚔ +8 ATK POWER UP", px + 16, py - 12 + floatY);
        } finally {
            g.restore();
        }
    }

    private static Image walkFrame(int index) {
        if (index >= 0 && index < WALK_CYCLE.length && WALK_CYCLE[index] != null) {
            return WALK_CYCLE[index];
        }
        return IMG_ARIN_STAND;
    }

    private static void drawImage(GraphicsContext g, Image img, double x, double y, double w, double h) {
        if (img != null) {
            g.drawImage(img, x, y, w, h);
        } else {
            g.setFill(Color.web("#f3e6bf"));
            g.fillOval(x, y + h - 24, 24, 24);
        }
    }

    private static void drawGroundSword(GraphicsContext g, SwordItem sword) {
        double sx = sword.getX();
        double sy = sword.getY();

        // Soft ground shadow
        g.setFill(Color.web("#000e15", 0.45));
        g.fillOval(sx - 2, sy + 14, 32, 10);

        // Mystical shimmer aura
        double pulse = 0.35 + 0.15 * Math.sin(System.currentTimeMillis() * 0.005);
        g.setFill(Color.web("#64e5ff", pulse * 0.3));
        g.fillOval(sx - 6, sy - 4, 40, 24);

        g.save();
        try {
            g.translate(sx + 14, sy + 8);
            g.rotate(35); // Diagonally placed on ground

            // Steel blade
            g.setFill(Color.web("#e3edf7"));
            g.fillRect(-2, -16, 4, 20);
            // Blade central fuller / highlight
            g.setFill(Color.web("#ffffff"));
            g.fillRect(-0.5, -16, 1, 18);
            // Blade tip
            double[] tipX = {-2, 0, 2};
            double[] tipY = {-16, -21, -16};
            g.fillPolygon(tipX, tipY, 3);

            // Brass crossguard
            g.setFill(Color.web("#d4af37"));
            g.fillRoundRect(-7, 4, 14, 3, 2, 2);

            // Leather hilt
            g.setFill(Color.web("#3d2817"));
            g.fillRect(-1.5, 7, 3, 6);

            // Gold pommel
            g.setFill(Color.web("#e5c158"));
            g.fillOval(-2.5, 13, 5, 5);
        } finally {
            g.restore();
        }
    }

    private static void drawEquippedSword(GraphicsContext g, Player p, double drawX, double drawY) {
        g.save();
        try {
            double handX = drawX + 27.0;
            double handY = drawY + 48.0;

            if (p.isAttacking()) {
                double progress = 1.0 - (p.getAttackTimer() / Player.ATTACK_DURATION);
                progress = Math.max(0.0, Math.min(1.0, progress));
                double swingAngle = -50.0 + progress * 130.0;

                g.translate(handX, handY);
                g.rotate(swingAngle);

                // Blade during swing
                g.setFill(Color.web("#e6f2fa"));
                g.fillRect(-2, -28, 4, 28);
                g.setFill(Color.web("#ffffff"));
                g.fillRect(-0.5, -28, 1, 26);
                // Blade tip
                g.fillPolygon(new double[]{-2, 0, 2}, new double[]{-28, -33, -28}, 3);
                // Brass guard
                g.setFill(Color.web("#d4af37"));
                g.fillRoundRect(-6, 0, 12, 3, 2, 2);
                // Leather hilt
                g.setFill(Color.web("#3d2817"));
                g.fillRect(-1.5, 3, 3, 6);
                // Pommel
                g.setFill(Color.web("#e5c158"));
                g.fillOval(-2.5, 9, 5, 5);

                g.restore(); // restore rotation for slash arc
                g.save();

                // Luminous directional slash arc / blade trail in front of Arin
                double arcX = drawX + 16.0;
                double arcY = drawY + 12.0;
                double startAngle = -35.0 + progress * 25.0;

                // Outer cyan blade trail
                g.setStroke(Color.web("#38efdf", 0.75));
                g.setLineWidth(5.0);
                g.strokeArc(arcX, arcY, 44.0, 52.0, startAngle, 85.0, javafx.scene.shape.ArcType.OPEN);

                // Inner brilliant white cutting edge
                g.setStroke(Color.web("#ffffff", 0.95));
                g.setLineWidth(2.5);
                g.strokeArc(arcX + 2, arcY + 2, 40.0, 48.0, startAngle + 5, 75.0, javafx.scene.shape.ArcType.OPEN);

                // Upgraded magical sword golden flame cutting arc
                if (p.isSwordUpgraded()) {
                    g.setStroke(Color.web("#fbbf24", 0.85));
                    g.setLineWidth(6.5);
                    g.strokeArc(arcX - 3, arcY - 3, 50.0, 58.0, startAngle - 4, 92.0, javafx.scene.shape.ArcType.OPEN);
                }
            } else {
                // Idle or walking sword at hip/hand
                g.translate(handX, handY);
                g.rotate(32.0);

                // Upgraded magical aura around blade
                if (p.isSwordUpgraded()) {
                    g.setFill(Color.web("#f59e0b", 0.45));
                    g.fillRoundRect(-4, -28, 8, 30, 4, 4);
                    g.setFill(Color.web("#38bdf8", 0.35));
                    g.fillRoundRect(-3, -26, 6, 28, 3, 3);
                }

                // Blade
                g.setFill(Color.web("#dce8f2"));
                g.fillRect(-1.5, -24, 3, 24);
                g.setFill(Color.web("#ffffff"));
                g.fillRect(-0.5, -24, 1, 22);
                // Tip
                g.fillPolygon(new double[]{-1.5, 0, 1.5}, new double[]{-24, -28, -24}, 3);
                // Guard
                g.setFill(Color.web("#d4af37"));
                g.fillRoundRect(-5, 0, 10, 3, 2, 2);
                // Hilt
                g.setFill(Color.web("#3d2817"));
                g.fillRect(-1, 3, 2, 5);
                // Pommel
                g.setFill(Color.web("#e5c158"));
                g.fillOval(-2, 8, 4, 4);
            }
        } finally {
            g.restore();
        }
    }

    private static void drawEnemy(GraphicsContext g, Enemy enemy) {
        if (!enemy.isAlive()) {
            drawDefeatedRemnant(g, enemy);
            return;
        }
        if (enemy instanceof ShadowLord lord) {
            drawShadowLord(g, lord);
        } else if (enemy instanceof ShadowKnight knight) {
            drawShadowKnight(g, knight);
        } else if (enemy instanceof DarkArcher archer) {
            drawDarkArcher(g, archer);
        } else if (enemy instanceof Wraith wraith) {
            drawWraith(g, wraith);
        } else if (enemy instanceof GiantGoblin giant) {
            drawGiantGoblin(g, giant);
        } else if (enemy instanceof Goblin goblin) {
            drawGoblin(g, goblin);
        } else if (enemy instanceof ShadowWolf wolf) {
            drawShadowWolf(g, wolf);
        }
    }

    private static void drawDefeatedRemnant(GraphicsContext g, Enemy enemy) {
        double baseX = enemy.getX() + enemy.getSize() / 2.0;
        double baseY = enemy.getY() + enemy.getSize();
        double w = enemy.getSize() * 1.2;
        g.setFill(Color.web("#000e15", 0.28));
        g.fillOval(baseX - w / 2.0, baseY - 5, w, 8);
        g.setFill(Color.web("#333b47", 0.40));
        g.fillOval(baseX - 6, baseY - 7, 12, 5);
    }

    private static void drawShadowLord(GraphicsContext g, ShadowLord lord) {
        double baseX = lord.getX() + lord.getSize() / 2.0;
        double baseY = lord.getY() + lord.getSize();

        // 1. Swirling dark void vortex on the ground
        double anim = lord.getAnimationTime();
        double pulse = 0.50 + 0.20 * Math.sin(anim * 4.0);
        g.setFill(Color.web("#050014", 0.70));
        g.fillOval(baseX - 42, baseY - 14, 84, 28);
        g.setFill(Color.web("#3b0764", pulse * 0.40));
        g.fillOval(baseX - 34, baseY - 11, 68, 22);

        double hover = Math.sin(anim * 3.5) * 4.0;

        g.save();
        try {
            if (!lord.isFacingRight()) {
                g.translate(baseX, 0);
                g.scale(-1, 1);
                g.translate(-baseX, 0);
            }

            // Dark magical energy aura around body
            g.setFill(Color.web("#581c87", pulse * 0.30));
            g.fillOval(baseX - 32, baseY - 100 - hover, 64, 100);

            // Flowing ragged black & dark violet robes
            g.setFill(Color.web("#090214")); // Pitch shadow core
            double[] robeX = {baseX - 22, baseX - 34, baseX - 26, baseX, baseX + 26, baseX + 34, baseX + 22};
            double[] robeY = {baseY - 76 - hover, baseY - 12 - hover, baseY - 4 - hover, baseY - 14 - hover, baseY - 4 - hover, baseY - 12 - hover, baseY - 76 - hover};
            g.fillPolygon(robeX, robeY, 7);

            // Shading folds on robes
            g.setFill(Color.web("#1e0a38"));
            g.fillPolygon(new double[]{baseX - 16, baseX - 20, baseX, baseX + 10, baseX + 8},
                          new double[]{baseY - 72 - hover, baseY - 10 - hover, baseY - 14 - hover, baseY - 10 - hover, baseY - 72 - hover}, 5);
            g.setStroke(Color.web("#38126b", 0.8));
            g.setLineWidth(1.5);
            g.strokeLine(baseX - 8, baseY - 70 - hover, baseX - 12, baseY - 12 - hover);
            g.strokeLine(baseX + 6, baseY - 70 - hover, baseX + 10, baseY - 12 - hover);

            // High gothic shoulder cowl / mantle
            g.setFill(Color.web("#130626"));
            g.fillPolygon(new double[]{baseX - 28, baseX - 38, baseX - 16, baseX, baseX + 16, baseX + 38, baseX + 28},
                          new double[]{baseY - 70 - hover, baseY - 62 - hover, baseY - 46 - hover, baseY - 50 - hover, baseY - 46 - hover, baseY - 62 - hover, baseY - 70 - hover}, 7);

            // Deep Hood Void
            g.setFill(Color.web("#05000c"));
            g.fillOval(baseX - 18, baseY - 102 - hover, 36, 38);

            // Hood cloth drape
            g.setFill(Color.web("#1a0833"));
            g.beginPath();
            g.moveTo(baseX, baseY - 108 - hover);
            g.bezierCurveTo(baseX + 22, baseY - 106 - hover, baseX + 24, baseY - 80 - hover, baseX + 18, baseY - 66 - hover);
            g.lineTo(baseX - 18, baseY - 66 - hover);
            g.bezierCurveTo(baseX - 24, baseY - 80 - hover, baseX - 22, baseY - 106 - hover, baseX, baseY - 108 - hover);
            g.closePath();
            g.fill();

            // Two large curved demonic horns curling from the hood
            // Left horn
            g.setFill(Color.web("#221538"));
            double[] leftHornX = {baseX - 14, baseX - 34, baseX - 30, baseX - 10};
            double[] leftHornY = {baseY - 96 - hover, baseY - 122 - hover, baseY - 110 - hover, baseY - 90 - hover};
            g.fillPolygon(leftHornX, leftHornY, 4);
            g.setStroke(Color.web("#4c2882", 0.85));
            g.setLineWidth(1.2);
            g.strokePolygon(leftHornX, leftHornY, 4);

            // Right horn
            g.setFill(Color.web("#2c1b47"));
            double[] rightHornX = {baseX + 10, baseX + 30, baseX + 34, baseX + 14};
            double[] rightHornY = {baseY - 90 - hover, baseY - 110 - hover, baseY - 122 - hover, baseY - 96 - hover};
            g.fillPolygon(rightHornX, rightHornY, 4);
            g.setStroke(Color.web("#5b329c", 0.85));
            g.setLineWidth(1.2);
            g.strokePolygon(rightHornX, rightHornY, 4);

            // Piercing glowing fiery orange eyes in the hood void
            g.setFill(Color.web("#ff5500"));
            g.fillOval(baseX - 10, baseY - 86 - hover, 6, 4);
            g.fillOval(baseX + 4, baseY - 86 - hover, 6, 4);
            g.setFill(Color.web("#ffea00"));
            g.fillOval(baseX - 8, baseY - 85 - hover, 3, 2);
            g.fillOval(baseX + 6, baseY - 85 - hover, 3, 2);

            // Fiery orange magic flame in hand
            double flameHandX = baseX + 24;
            double flameHandY = baseY - 50 - hover;
            g.setFill(Color.web("#ff3b00", 0.40));
            g.fillOval(flameHandX - 14, flameHandY - 14, 28, 28);
            g.setFill(Color.web("#ff7700"));
            g.fillPolygon(new double[]{flameHandX - 7, flameHandX, flameHandX + 7, flameHandX + 4, flameHandX - 4},
                          new double[]{flameHandY + 4, flameHandY - 12 + Math.sin(anim * 10.0) * 3, flameHandY + 4, flameHandY + 7, flameHandY + 7}, 5);
            g.setFill(Color.web("#ffea00"));
            g.fillOval(flameHandX - 3, flameHandY - 4, 6, 8);

            // Attack animations
            if (lord.isAttacking()) {
                if (lord.getCurrentAttackMode() == ShadowLord.AttackMode.MELEE) {
                    // Spectral Void Scythe
                    g.save();
                    g.translate(baseX + 18, baseY - 56 - hover);
                    double scytheAngle = (lord.getAttackTimer() < lord.getAttackWindup()) ? -45 : 60;
                    g.rotate(scytheAngle);
                    g.setFill(Color.web("#1e1133"));
                    g.fillRect(-3, -40, 6, 50); // shaft
                    g.setFill(Color.web("#8b5cf6"));
                    // Curved blade
                    g.beginPath();
                    g.moveTo(0, -40);
                    g.bezierCurveTo(24, -60, 48, -48, 52, -28);
                    g.bezierCurveTo(40, -36, 16, -38, 0, -40);
                    g.closePath();
                    g.fill();
                    g.restore();

                    // Melee slash wave on hit frame
                    if (lord.getAttackTimer() >= lord.getAttackWindup()) {
                        g.setStroke(Color.web("#a855f7", 0.90));
                        g.setLineWidth(5.0);
                        g.strokeArc(baseX + 16, baseY - 70 - hover, 60, 60, -35, 95, javafx.scene.shape.ArcType.OPEN);
                        g.setStroke(Color.web("#ef4444", 0.85));
                        g.setLineWidth(2.5);
                        g.strokeArc(baseX + 18, baseY - 68 - hover, 54, 54, -30, 85, javafx.scene.shape.ArcType.OPEN);
                    }
                }
            }

            // Hit reaction flash
            if (lord.isHitFlashing()) {
                g.setFill(Color.web("#ffffff", 0.80));
                g.fillOval(baseX - 28, baseY - 106 - hover, 56, 100);
                g.setFill(Color.web("#a855f7", 0.50));
                g.fillOval(baseX - 24, baseY - 100 - hover, 48, 90);
            }
        } finally {
            g.restore();
        }

        // Flamethrower attack visual in world coordinates (unflipped)
        if (lord.isAttacking() && lord.isFlamethrowerMode()) {
            double fireOriginX = baseX + (lord.isFacingRight() ? 24 : -24);
            double fireOriginY = baseY - 50 - hover;
            if (lord.isFlamethrowerCharging()) {
                drawFlamethrowerTelegraph(g, fireOriginX, fireOriginY, lord.getFlamethrowerAngle(),
                        lord.getAttackTimer() / lord.getAttackWindup());
            } else if (lord.isFlamethrowerFiring()) {
                drawFlamethrowerStream(g, fireOriginX, fireOriginY, lord.getFlamethrowerAngle(), lord.getAnimationTime());
            }
        }
    }

    private static void drawFlamethrowerStream(GraphicsContext g, double originX, double originY, double angle, double anim) {
        g.save();
        try {
            g.translate(originX, originY);
            g.rotate(Math.toDegrees(angle));

            double range = ShadowLord.FLAMETHROWER_RANGE; // 280.0
            double halfAngle = ShadowLord.FLAMETHROWER_CONE_HALF_ANGLE; // 35 deg

            // 1. Outer turbulent roaring flame cone
            int steps = 14;
            double[] polyX = new double[steps + 2];
            double[] polyY = new double[steps + 2];
            polyX[0] = 0;
            polyY[0] = 0;
            for (int i = 0; i <= steps; i++) {
                double a = -halfAngle + (2.0 * halfAngle * i / steps);
                double r = range * (0.85 + 0.15 * Math.sin(anim * 18.0 + i * 1.5));
                polyX[i + 1] = Math.cos(a) * r;
                polyY[i + 1] = Math.sin(a) * r;
            }
            g.setFill(Color.web("#f97316", 0.55));
            g.fillPolygon(polyX, polyY, steps + 2);

            // 2. Mid concentrated fire torrent
            double midHalfAngle = Math.toRadians(20.0);
            double[] midX = new double[steps + 2];
            double[] midY = new double[steps + 2];
            midX[0] = 0;
            midY[0] = 0;
            for (int i = 0; i <= steps; i++) {
                double a = -midHalfAngle + (2.0 * midHalfAngle * i / steps);
                double r = (range - 25.0) * (0.90 + 0.10 * Math.sin(anim * 24.0 + i * 2.0));
                midX[i + 1] = Math.cos(a) * r;
                midY[i + 1] = Math.sin(a) * r;
            }
            g.setFill(Color.web("#fbbf24", 0.78));
            g.fillPolygon(midX, midY, steps + 2);

            // 3. White-hot concentrated core beam
            double coreHalfAngle = Math.toRadians(8.0);
            double[] coreX = new double[steps + 2];
            double[] coreY = new double[steps + 2];
            coreX[0] = 0;
            coreY[0] = 0;
            for (int i = 0; i <= steps; i++) {
                double a = -coreHalfAngle + (2.0 * coreHalfAngle * i / steps);
                double r = (range - 50.0);
                coreX[i + 1] = Math.cos(a) * r;
                coreY[i + 1] = Math.sin(a) * r;
            }
            g.setFill(Color.web("#ffffff", 0.95));
            g.fillPolygon(coreX, coreY, steps + 2);

            // 4. Flying fire sparks and flame embers
            for (int s = 0; s < 10; s++) {
                double sparkDist = 40.0 + ((anim * 400.0 + s * 30.0) % (range - 40.0));
                double sparkSpread = Math.sin(anim * 10.0 + s) * (sparkDist * 0.35);
                g.setFill(s % 2 == 0 ? Color.web("#ffea00") : Color.web("#ff3838"));
                g.fillOval(sparkDist, sparkSpread - 2, 4 + (s % 3), 4 + (s % 3));
            }
        } finally {
            g.restore();
        }
    }

    private static void drawFlamethrowerTelegraph(GraphicsContext g, double originX, double originY, double angle, double progress) {
        g.save();
        try {
            g.translate(originX, originY);
            g.rotate(Math.toDegrees(angle));

            double range = ShadowLord.FLAMETHROWER_RANGE;
            double halfAngle = ShadowLord.FLAMETHROWER_CONE_HALF_ANGLE;

            // Telegraph cone boundary lines
            g.setStroke(Color.web("#ef4444", 0.30 + progress * 0.45));
            g.setLineWidth(1.5);
            double lx = Math.cos(halfAngle) * range;
            double ly = Math.sin(halfAngle) * range;
            g.strokeLine(0, 0, lx, ly);
            g.strokeLine(0, 0, lx, -ly);

            // Faint warning cone area
            double[] polyX = {0, lx, lx, 0};
            double[] polyY = {0, ly, -ly, 0};
            g.setFill(Color.web("#ff4400", 0.12 * progress));
            g.fillPolygon(polyX, polyY, 4);

            // Gathering energy orb at hand
            double orbRadius = 8.0 + progress * 14.0;
            g.setFill(Color.web("#ff5500", 0.50 + progress * 0.40));
            g.fillOval(-orbRadius, -orbRadius, orbRadius * 2, orbRadius * 2);
            g.setFill(Color.web("#ffea00", 0.85));
            g.fillOval(-orbRadius * 0.5, -orbRadius * 0.5, orbRadius, orbRadius);
        } finally {
            g.restore();
        }
    }

    private static void drawShadowKnight(GraphicsContext g, ShadowKnight knight) {
        double baseX = knight.getX() + knight.getSize() / 2.0;
        double baseY = knight.getY() + knight.getSize();

        // Ground shadow
        g.setFill(Color.web("#000e15", 0.45));
        g.fillOval(baseX - 18, baseY - 7, 36, 12);

        double bob = knight.isMoving() ? Math.abs(Math.sin(knight.getAnimationTime() * 7.5)) * 2.5 : 0;

        g.save();
        try {
            if (!knight.isFacingRight()) {
                g.translate(baseX, 0);
                g.scale(-1, 1);
                g.translate(-baseX, 0);
            }

            // Dark-steel greaves & sabatons
            g.setFill(Color.web("#171a21"));
            g.fillRoundRect(baseX - 11, baseY - 16 - bob, 8, 14, 2, 2);
            g.fillRoundRect(baseX + 3, baseY - 16 - bob, 8, 14, 2, 2);

            // Plate torso / cuirass (dark steel with violet runes)
            g.setFill(Color.web("#1f242d"));
            g.fillRoundRect(baseX - 14, baseY - 38 - bob, 28, 24, 4, 4);
            g.setFill(Color.web("#2c333e"));
            g.fillRect(baseX - 10, baseY - 36 - bob, 20, 10);

            // Violet engraved rune lines on armor
            g.setStroke(Color.web("#a855f7", 0.85));
            g.setLineWidth(1.2);
            g.strokeLine(baseX - 6, baseY - 32 - bob, baseX, baseY - 24 - bob);
            g.strokeLine(baseX + 6, baseY - 32 - bob, baseX, baseY - 24 - bob);
            g.strokeLine(baseX, baseY - 34 - bob, baseX, baseY - 22 - bob);

            // Heavy steel pauldrons (shoulders)
            g.setFill(Color.web("#2c333e"));
            g.fillRoundRect(baseX - 18, baseY - 40 - bob, 9, 12, 3, 3);
            g.fillRoundRect(baseX + 9, baseY - 40 - bob, 9, 12, 3, 3);

            // Greathelm (full-face dark steel helmet)
            g.setFill(Color.web("#171a21"));
            g.fillRoundRect(baseX - 9, baseY - 52 - bob, 18, 16, 4, 4);
            g.setFill(Color.web("#2c333e"));
            g.fillRect(baseX - 7, baseY - 50 - bob, 14, 4);

            // Glowing violet visor slit
            g.setFill(Color.web("#c084fc"));
            g.fillRect(baseX - 6, baseY - 44 - bob, 12, 2.5);
            g.setFill(Color.web("#ffffff"));
            g.fillRect(baseX - 2, baseY - 44 - bob, 4, 2);

            // Heavy two-handed broadsword
            g.save();
            g.translate(baseX + 12, baseY - 32 - bob);
            double swordAngle = 25.0;
            if (knight.isAttacking()) {
                if (knight.getAttackTimer() < knight.getAttackWindup()) {
                    swordAngle = -65.0;
                } else {
                    swordAngle = 70.0;
                }
            }
            g.rotate(swordAngle);
            g.setFill(Color.web("#334155"));
            g.fillRect(-2.5, -34, 5, 34);
            g.setFill(Color.web("#a855f7"));
            g.fillRect(-0.5, -32, 1, 30);
            g.fillPolygon(new double[]{-2.5, 0, 2.5}, new double[]{-34, -38, -34}, 3);
            g.setFill(Color.web("#171a21"));
            g.fillRect(-7, 0, 14, 3);
            g.fillRect(-1.5, 3, 3, 6);
            g.fillOval(-2.5, 9, 5, 5);
            g.restore();

            // Slash arc on hit frame
            if (knight.isAttacking() && knight.getAttackTimer() >= knight.getAttackWindup()) {
                g.setStroke(Color.web("#a855f7", 0.90));
                g.setLineWidth(4.0);
                g.strokeArc(baseX + 8, baseY - 46 - bob, 38, 38, -30, 85, javafx.scene.shape.ArcType.OPEN);
            }

            // Hit reaction flash
            if (knight.isHitFlashing()) {
                g.setFill(Color.web("#ffffff", 0.75));
                g.fillOval(baseX - 16, baseY - 54 - bob, 32, 50);
                g.setFill(Color.web("#ff3838", 0.40));
                g.fillOval(baseX - 12, baseY - 48 - bob, 24, 40);
            }
        } finally {
            g.restore();
        }
    }

    private static void drawDarkArcher(GraphicsContext g, DarkArcher archer) {
        double baseX = archer.getX() + archer.getSize() / 2.0;
        double baseY = archer.getY() + archer.getSize();

        // Ground shadow
        g.setFill(Color.web("#000e15", 0.42));
        g.fillOval(baseX - 14, baseY - 6, 28, 10);

        double bob = archer.isMoving() ? Math.abs(Math.sin(archer.getAnimationTime() * 8.0)) * 2.0 : 0;

        g.save();
        try {
            if (!archer.isFacingRight()) {
                g.translate(baseX, 0);
                g.scale(-1, 1);
                g.translate(-baseX, 0);
            }

            // Boots & legs
            g.setFill(Color.web("#18181b"));
            g.fillRoundRect(baseX - 9, baseY - 14 - bob, 6, 12, 2, 2);
            g.fillRoundRect(baseX + 2, baseY - 14 - bob, 6, 12, 2, 2);

            // Dark ash leather tunic
            g.setFill(Color.web("#27272a"));
            g.fillRoundRect(baseX - 11, baseY - 32 - bob, 22, 20, 3, 3);

            // Quiver slung across back with dark arrows
            g.setFill(Color.web("#3f3f46"));
            g.fillRect(baseX - 14, baseY - 34 - bob, 5, 16);
            g.setFill(Color.web("#71717a"));
            g.fillRect(baseX - 15, baseY - 38 - bob, 2, 5);
            g.fillRect(baseX - 12, baseY - 39 - bob, 2, 6);

            // Hooded cloak & cowl
            g.setFill(Color.web("#18181b"));
            g.beginPath();
            g.moveTo(baseX - 12, baseY - 24 - bob);
            g.lineTo(baseX - 8, baseY - 44 - bob);
            g.lineTo(baseX + 8, baseY - 44 - bob);
            g.lineTo(baseX + 12, baseY - 24 - bob);
            g.closePath();
            g.fill();

            // Hood void & glowing amber eyes
            g.setFill(Color.web("#09090b"));
            g.fillOval(baseX - 6, baseY - 41 - bob, 12, 12);
            g.setFill(Color.web("#fbbf24"));
            g.fillOval(baseX - 2, baseY - 37 - bob, 3, 2);
            g.fillOval(baseX + 3, baseY - 37 - bob, 3, 2);

            // Longbow
            double bowHandX = baseX + 10;
            double bowHandY = baseY - 26 - bob;
            g.setStroke(Color.web("#451a03"));
            g.setLineWidth(3.0);
            g.strokeArc(bowHandX - 8, bowHandY - 20, 16, 38, -60, 120, javafx.scene.shape.ArcType.OPEN);

            // Bowstring
            g.setStroke(Color.web("#e4e4e7", 0.75));
            g.setLineWidth(1.0);
            if (archer.isAttacking() && archer.getAttackTimer() < archer.getAttackWindup()) {
                g.strokeLine(bowHandX - 3, bowHandY - 18, bowHandX - 10, bowHandY);
                g.strokeLine(bowHandX - 10, bowHandY, bowHandX - 3, bowHandY + 16);

                g.setStroke(Color.web("#18181b"));
                g.setLineWidth(1.5);
                g.strokeLine(bowHandX - 10, bowHandY, bowHandX + 14, bowHandY);
                g.setFill(Color.web("#c084fc"));
                g.fillPolygon(new double[]{bowHandX + 14, bowHandX + 18, bowHandX + 14},
                              new double[]{bowHandY - 2.5, bowHandY, bowHandY + 2.5}, 3);
            } else {
                g.strokeLine(bowHandX - 3, bowHandY - 18, bowHandX - 3, bowHandY + 16);
            }

            // Arrow release projectile streak on hit frame
            if (archer.isAttacking() && archer.getAttackTimer() >= archer.getAttackWindup()) {
                double arrowProgress = (archer.getAttackTimer() - archer.getAttackWindup()) / (archer.getAttackDuration() - archer.getAttackWindup());
                double arrowDist = arrowProgress * archer.getAttackRange();
                double ax = bowHandX + arrowDist;
                double ay = bowHandY;

                g.setStroke(Color.web("#18181b"));
                g.setLineWidth(2.0);
                g.strokeLine(ax - 20, ay, ax, ay);
                g.setFill(Color.web("#c084fc"));
                g.fillPolygon(new double[]{ax, ax + 7, ax}, new double[]{ay - 3, ay, ay + 3}, 3);
                g.setStroke(Color.web("#a855f7", 0.60));
                g.setLineWidth(1.2);
                g.strokeLine(ax - 35, ay, ax - 15, ay);
            }

            // Hit reaction flash
            if (archer.isHitFlashing()) {
                g.setFill(Color.web("#ffffff", 0.75));
                g.fillOval(baseX - 14, baseY - 46 - bob, 28, 44);
                g.setFill(Color.web("#ff3838", 0.40));
                g.fillOval(baseX - 10, baseY - 40 - bob, 20, 36);
            }
        } finally {
            g.restore();
        }
    }

    private static void drawWraith(GraphicsContext g, Wraith wraith) {
        double baseX = wraith.getX() + wraith.getSize() / 2.0;
        double baseY = wraith.getY() + wraith.getSize();

        double hover = Math.sin(wraith.getAnimationTime() * 4.5) * 6.0;

        g.setFill(Color.web("#000e15", 0.25));
        g.fillOval(baseX - 16, baseY - 4, 32, 8);

        g.save();
        try {
            if (!wraith.isFacingRight()) {
                g.translate(baseX, 0);
                g.scale(-1, 1);
                g.translate(-baseX, 0);
            }

            double pulse = 0.40 + 0.20 * Math.sin(wraith.getAnimationTime() * 6.0);
            g.setFill(Color.web("#14b8a6", pulse * 0.35));
            g.fillOval(baseX - 22, baseY - 46 - hover, 44, 46);

            g.setFill(Color.web("#0f172a", 0.85));
            double[] shroudX = {baseX - 14, baseX - 18, baseX - 8, baseX, baseX + 8, baseX + 18, baseX + 14};
            double[] shroudY = {baseY - 36 - hover, baseY - 6 - hover, baseY - 2 - hover, baseY - 8 - hover, baseY - 2 - hover, baseY - 6 - hover, baseY - 36 - hover};
            g.fillPolygon(shroudX, shroudY, 7);

            g.setFill(Color.web("#0d9488", 0.50));
            g.fillPolygon(new double[]{baseX - 10, baseX - 14, baseX, baseX + 10},
                          new double[]{baseY - 32 - hover, baseY - 8 - hover, baseY - 12 - hover, baseY - 32 - hover}, 4);

            g.setFill(Color.web("#1e293b"));
            g.fillOval(baseX - 11, baseY - 46 - hover, 22, 22);

            g.setFill(Color.web("#0f172a"));
            g.fillOval(baseX - 7, baseY - 42 - hover, 14, 14);
            g.setFill(Color.web("#2dd4bf"));
            g.fillOval(baseX - 4, baseY - 39 - hover, 3.5, 3.5);
            g.fillOval(baseX + 2, baseY - 39 - hover, 3.5, 3.5);
            g.setFill(Color.web("#ffffff"));
            g.fillOval(baseX - 3, baseY - 38.5 - hover, 1.5, 1.5);
            g.fillOval(baseX + 3, baseY - 38.5 - hover, 1.5, 1.5);

            g.setFill(Color.web("#2dd4bf", 0.70));
            g.fillOval(baseX + 9, baseY - 28 - hover, 7, 7);
            g.fillOval(baseX - 16, baseY - 28 - hover, 7, 7);

            if (wraith.isAttacking() && wraith.getAttackTimer() >= wraith.getAttackWindup()) {
                g.setStroke(Color.web("#2dd4bf", 0.85));
                g.setLineWidth(3.0);
                g.strokeOval(baseX - 22, baseY - 48 - hover, 44, 44);
                g.setStroke(Color.web("#c084fc", 0.70));
                g.setLineWidth(1.5);
                g.strokeOval(baseX - 28, baseY - 54 - hover, 56, 56);
            }

            if (wraith.isHitFlashing()) {
                g.setFill(Color.web("#ffffff", 0.80));
                g.fillOval(baseX - 18, baseY - 50 - hover, 36, 50);
                g.setFill(Color.web("#38bdf8", 0.50));
                g.fillOval(baseX - 14, baseY - 44 - hover, 28, 40);
            }
        } finally {
            g.restore();
        }
    }

    private static void drawPortal(GraphicsContext g, Portal portal, Player player) {
        double px = portal.getX();
        double py = portal.getY();

        // 1. Ancient stone/tree arch structure framing the portal
        g.setFill(Color.web("#061f21"));
        g.fillRoundRect(px - 34, py - 60, 14, 90, 6, 6);
        g.setStroke(Color.web("#164e4a", 0.7));
        g.setLineWidth(1.5);
        g.strokeRoundRect(px - 34, py - 60, 14, 90, 6, 6);

        g.setFill(Color.web("#061f21"));
        g.fillRoundRect(px + 20, py - 60, 14, 90, 6, 6);
        g.setStroke(Color.web("#164e4a", 0.7));
        g.setLineWidth(1.5);
        g.strokeRoundRect(px + 20, py - 60, 14, 90, 6, 6);

        g.setFill(Color.web("#08282b"));
        g.fillRoundRect(px - 38, py - 72, 76, 16, 6, 6);
        g.setStroke(Color.web("#22d3ee", 0.6));
        g.setLineWidth(1.2);
        g.strokeRoundRect(px - 38, py - 72, 76, 16, 6, 6);

        // 2. Vertical supernatural tear / rift
        double time = System.currentTimeMillis() * 0.004;
        double pulse = 0.55 + 0.25 * Math.sin(time);

        g.setFill(Color.web("#06b6d4", pulse * 0.30));
        g.fillOval(px - 28, py - 56, 56, 80);

        g.setFill(Color.web("#22d3ee", pulse * 0.45));
        g.fillOval(px - 20, py - 52, 40, 72);

        g.setFill(Color.web("#03131a"));
        g.beginPath();
        g.moveTo(px, py - 56);
        g.bezierCurveTo(px + 14 + Math.sin(time * 3) * 3, py - 35, px + 16, py - 10, px, py + 18);
        g.bezierCurveTo(px - 16 - Math.cos(time * 3) * 3, py - 10, px - 14, py - 35, px, py - 56);
        g.closePath();
        g.fill();

        // Irregular jagged tear edges (supernatural rift)
        g.setStroke(Color.web("#38bdf8", 0.90));
        g.setLineWidth(2.5);
        g.beginPath();
        g.moveTo(px, py - 58);
        g.lineTo(px + 8, py - 46);
        g.lineTo(px + 15, py - 30);
        g.lineTo(px + 11, py - 15);
        g.lineTo(px + 13, py + 2);
        g.lineTo(px, py + 20);
        g.lineTo(px - 12, py + 4);
        g.lineTo(px - 15, py - 16);
        g.lineTo(px - 9, py - 35);
        g.lineTo(px - 7, py - 48);
        g.closePath();
        g.stroke();

        g.setFill(Color.web("#e0f2fe", 0.85));
        g.fillOval(px - 4, py - 48, 8, 64);
        g.setFill(Color.web("#ffffff", 0.95));
        g.fillOval(px - 2, py - 40, 4, 48);

        // Rising swirling particles
        for (int i = 0; i < 5; i++) {
            double pTime = time * 2.0 + i * 1.3;
            double sparkY = (py + 10) - ((pTime * 20.0) % 70.0);
            double sparkX = px + Math.sin(pTime * 3.0) * 12.0;
            g.setFill(Color.web(i % 2 == 0 ? "#67e8f9" : "#ffffff", 0.85));
            g.fillOval(sparkX, sparkY, 3, 3);
        }

        // 3. Interaction hint prompt badge when Arin is close
        if (player != null && portal.isPlayerInRange(player)) {
            double badgeW = 168.0;
            double badgeH = 22.0;
            double badgeX = px - badgeW / 2.0;
            double badgeY = py - 96.0 + Math.sin(time * 3.0) * 3.0;

            g.setFill(Color.web("#021319", 0.85));
            g.fillRoundRect(badgeX - 1, badgeY - 1, badgeW + 2, badgeH + 2, 6, 6);
            g.setStroke(Color.web("#38bdf8", 0.90));
            g.setLineWidth(1.5);
            g.strokeRoundRect(badgeX, badgeY, badgeW, badgeH, 5, 5);

            g.setFont(Font.font("System", FontWeight.BOLD, 10.5));
            g.setTextAlign(TextAlignment.CENTER);
            g.setTextBaseline(VPos.CENTER);
            g.setFill(Color.web("#ffffff"));
            g.fillText("✦ PRESS E TO ENTER PORTAL ✦", px, badgeY + badgeH / 2.0);
        }
    }

    private static void drawBossHealthBar(GraphicsContext g, Enemy boss, double width) {
        double barW = 420.0;
        double barH = 18.0;
        double barX = (width - barW) / 2.0;
        double barY = 20.0;

        g.setFill(Color.web("#000000", 0.75));
        g.fillRoundRect(barX - 4, barY - 4, barW + 8, barH + 8, 8, 8);

        g.setFill(Color.web("#0b0617"));
        g.fillRoundRect(barX, barY, barW, barH, 4, 4);

        double maxHp = Math.max(1.0, boss.getMaxHealth());
        double currHp = Math.max(0.0, Math.min(maxHp, boss.getHealth()));
        double fillRatio = currHp / maxHp;
        double fillW = Math.max(0, (barW - 4) * fillRatio);

        if (fillW > 0) {
            Color c1 = boss instanceof ShadowLord ? Color.web("#8b5cf6") : Color.web("#f59e0b");
            Color c2 = boss instanceof ShadowLord ? Color.web("#dc2626") : Color.web("#991b1b");
            LinearGradient grad = new LinearGradient(barX, barY, barX + barW, barY, false, CycleMethod.NO_CYCLE,
                    new Stop(0, c1), new Stop(1, c2));
            g.setFill(grad);
            g.fillRoundRect(barX + 2, barY + 2, fillW, barH - 4, 3, 3);
        }

        g.setStroke(Color.web("#d4af37", 0.90));
        g.setLineWidth(1.5);
        g.strokeRoundRect(barX, barY, barW, barH, 4, 4);

        String bossTitle = boss instanceof ShadowLord ? "✦ SHADOW LORD — FINAL BOSS ✦" : "✦ GIANT GOBLIN — LEVEL 1 BOSS ✦";
        g.setFont(Font.font("System", FontWeight.BOLD, 12));
        g.setTextAlign(TextAlignment.CENTER);
        g.setTextBaseline(VPos.BOTTOM);
        g.setFill(Color.web("#000000", 0.90));
        g.fillText(bossTitle, width / 2.0 + 1, barY - 3);
        g.setFill(Color.web("#f59e0b"));
        g.fillText(bossTitle, width / 2.0, barY - 4);

        String hpText = "HP: " + (int) Math.ceil(currHp) + " / " + (int) maxHp;
        g.setFont(Font.font("System", FontWeight.BOLD, 11));
        g.setTextBaseline(VPos.CENTER);
        g.setFill(Color.web("#000000", 0.90));
        g.fillText(hpText, width / 2.0 + 1, barY + barH / 2.0 + 1);
        g.setFill(Color.web("#f3e6bf"));
        g.fillText(hpText, width / 2.0, barY + barH / 2.0);
    }

    private static void drawSwordUpgradeNotification(GraphicsContext g, double width) {
        double bannerW = 440.0;
        double bannerH = 34.0;
        double bannerX = (width - bannerW) / 2.0;
        double bannerY = 68.0;

        g.setFill(Color.web("#000000", 0.85));
        g.fillRoundRect(bannerX - 2, bannerY - 2, bannerW + 4, bannerH + 4, 8, 8);
        g.setFill(Color.web("#140d24"));
        g.fillRoundRect(bannerX, bannerY, bannerW, bannerH, 6, 6);

        g.setStroke(Color.web("#f59e0b", 0.95));
        g.setLineWidth(2.0);
        g.strokeRoundRect(bannerX, bannerY, bannerW, bannerH, 6, 6);

        g.setFont(Font.font("System", FontWeight.BOLD, 15));
        g.setTextAlign(TextAlignment.CENTER);
        g.setTextBaseline(VPos.CENTER);
        g.setFill(Color.web("#000000", 0.90));
        g.fillText("✦ SWORD UPGRADED — ATTACK POWER +5 ✦", width / 2.0 + 1, bannerY + bannerH / 2.0 + 1);
        g.setFill(Color.web("#facc15"));
        g.fillText("✦ SWORD UPGRADED — ATTACK POWER +5 ✦", width / 2.0, bannerY + bannerH / 2.0);
    }

    private static void drawGiantGoblin(GraphicsContext g, GiantGoblin giant) {
        double baseX = giant.getX() + giant.getSize() / 2.0;
        double baseY = giant.getY() + giant.getSize();
        // Massive ground contact shadow
        g.setFill(Color.web("#000e15", 0.52));
        g.fillOval(baseX - 32, baseY - 12, 64, 20);

        double anim = giant.getAnimationTime();
        double bob = giant.isMoving() ? Math.abs(Math.sin(anim * 6.0)) * 3.5 : 0;

        g.save();
        try {
            if (!giant.isFacingRight()) {
                g.translate(baseX, 0);
                g.scale(-1, 1);
                g.translate(-baseX, 0);
            }

            // Thick muscular legs with iron greaves
            g.setFill(Color.web("#244227")); // dark olive skin
            g.fillRoundRect(baseX - 22, baseY - 32 - bob, 16, 28, 6, 6);
            g.fillRoundRect(baseX + 6, baseY - 32 - bob, 16, 28, 6, 6);

            // Iron greaves / plates
            g.setFill(Color.web("#383e44"));
            g.fillRoundRect(baseX - 24, baseY - 24 - bob, 18, 18, 4, 4);
            g.fillRoundRect(baseX + 6, baseY - 24 - bob, 18, 18, 4, 4);
            // Studs/rivets
            g.setFill(Color.web("#828a91"));
            g.fillOval(baseX - 20, baseY - 20 - bob, 3, 3);
            g.fillOval(baseX + 10, baseY - 20 - bob, 3, 3);

            // Armored boots
            g.setFill(Color.web("#1e1710"));
            g.fillOval(baseX - 26, baseY - 9 - bob, 22, 10);
            g.fillOval(baseX + 4, baseY - 9 - bob, 22, 10);

            // Massive torso / heavy studded armor
            g.setFill(Color.web("#2c1e13")); // dark studded leather
            g.fillRoundRect(baseX - 28, baseY - 68 - bob, 56, 42, 10, 10);
            // Iron breastplate plate
            g.setFill(Color.web("#383e44"));
            g.fillRoundRect(baseX - 20, baseY - 64 - bob, 40, 26, 6, 6);
            // Skull/boss emblem in center of chest
            g.setFill(Color.web("#8a2323")); // blood sigil
            g.fillOval(baseX - 6, baseY - 56 - bob, 12, 12);
            g.setFill(Color.web("#b83030"));
            g.fillOval(baseX - 4, baseY - 54 - bob, 8, 8);

            // Heavy spiked belt
            g.setFill(Color.web("#1d2226"));
            g.fillRect(baseX - 28, baseY - 38 - bob, 56, 9);
            g.setFill(Color.web("#828a91"));
            g.fillRoundRect(baseX - 8, baseY - 40 - bob, 16, 13, 3, 3);

            // Spiked Iron Pauldrons (shoulders)
            g.setFill(Color.web("#2b3036"));
            g.fillOval(baseX - 38, baseY - 74 - bob, 24, 18);
            g.fillOval(baseX + 14, baseY - 74 - bob, 24, 18);
            // Pauldron spikes
            g.setFill(Color.web("#9ea6ad"));
            double[] spike1X = {baseX - 32, baseX - 38, baseX - 26};
            double[] spike1Y = {baseY - 74 - bob, baseY - 86 - bob, baseY - 74 - bob};
            g.fillPolygon(spike1X, spike1Y, 3);
            double[] spike2X = {baseX + 20, baseX + 26, baseX + 32};
            double[] spike2Y = {baseY - 74 - bob, baseY - 86 - bob, baseY - 74 - bob};
            g.fillPolygon(spike2X, spike2Y, 3);

            // Giant Goblin Head (brutish, massive jaw)
            g.setFill(Color.web("#2e5833")); // dark olive boss hide
            g.fillOval(baseX - 18, baseY - 90 - bob, 38, 30);

            // War helm / brow ridge
            g.setFill(Color.web("#383e44"));
            g.fillArc(baseX - 18, baseY - 92 - bob, 38, 20, 0, 180, javafx.scene.shape.ArcType.ROUND);

            // Jagged, scarred goblin ears
            double[] leftEarX = {baseX - 16, baseX - 34, baseX - 12};
            double[] leftEarY = {baseY - 82 - bob, baseY - 90 - bob, baseY - 72 - bob};
            g.setFill(Color.web("#244227"));
            g.fillPolygon(leftEarX, leftEarY, 3);
            double[] rightEarX = {baseX + 12, baseX + 32, baseX + 16};
            double[] rightEarY = {baseY - 80 - bob, baseY - 88 - bob, baseY - 70 - bob};
            g.setFill(Color.web("#2e5833"));
            g.fillPolygon(rightEarX, rightEarY, 3);

            // Glowing fiery crimson eyes
            g.setFill(Color.web("#ff2222"));
            g.fillOval(baseX + 4, baseY - 82 - bob, 7, 5);
            g.setFill(Color.web("#ffea55")); // intense fiery pupil
            g.fillOval(baseX + 6, baseY - 81 - bob, 3, 3);

            // Massive protruding lower fangs / tusks
            g.setFill(Color.web("#e6dec8"));
            double[] tusk1X = {baseX + 8, baseX + 12, baseX + 16};
            double[] tusk1Y = {baseY - 68 - bob, baseY - 77 - bob, baseY - 68 - bob};
            g.fillPolygon(tusk1X, tusk1Y, 3);
            double[] tusk2X = {baseX + 16, baseX + 19, baseX + 23};
            double[] tusk2Y = {baseY - 68 - bob, baseY - 75 - bob, baseY - 68 - bob};
            g.fillPolygon(tusk2X, tusk2Y, 3);

            // Massive Arm & Spiked Giant Battle-Club
            g.setFill(Color.web("#2e5833")); // muscular arm
            g.fillRoundRect(baseX + 16, baseY - 60 - bob, 14, 26, 6, 6);
            // Arm bracer
            g.setFill(Color.web("#383e44"));
            g.fillRoundRect(baseX + 14, baseY - 50 - bob, 16, 12, 3, 3);

            // Giant Spiked Club (massive wooden pillar with iron bands and heavy spikes)
            g.save();
            g.translate(baseX + 26, baseY - 54 - bob);
            double clubAngle = 28;
            if (giant.isAttacking()) {
                if (giant.getAttackTimer() < giant.getAttackWindup()) {
                    clubAngle = -60; // Raised high overhead in wind-up telegraph
                } else {
                    clubAngle = 75; // Smashed down onto ground on hit frame
                }
            }
            g.rotate(clubAngle);
            // Club shaft
            g.setFill(Color.web("#422b17"));
            g.fillRoundRect(-6, -38, 15, 54, 7, 7);
            // Iron reinforcing bands
            g.setFill(Color.web("#383e44"));
            g.fillRect(-7, -30, 17, 6);
            g.fillRect(-7, -18, 17, 6);
            // Large iron spikes
            g.setFill(Color.web("#a8b0b8"));
            g.fillPolygon(new double[]{8, 18, 8}, new double[]{-32, -30, -26}, 3);
            g.fillPolygon(new double[]{8, 20, 8}, new double[]{-20, -18, -14}, 3);
            g.fillPolygon(new double[]{-6, -16, -6}, new double[]{-26, -24, -20}, 3);
            g.restore();

            // Ground shockwave ring on heavy impact
            if (giant.isAttacking() && giant.getAttackTimer() >= giant.getAttackWindup()) {
                g.setStroke(Color.web("#e6a13d", 0.85));
                g.setLineWidth(4.0);
                g.strokeOval(baseX + 12, baseY - 14, 46, 18);
            }

            // Hit reaction flash
            if (giant.isHitFlashing()) {
                g.setFill(Color.web("#ffffff", 0.75));
                g.fillOval(baseX - 30, baseY - 85 - bob, 60, 75);
                g.setFill(Color.web("#ff3838", 0.45));
                g.fillOval(baseX - 22, baseY - 75 - bob, 44, 55);
            }
        } finally {
            g.restore();
        }
    }

    private static void drawGoblin(GraphicsContext g, Goblin goblin) {
        double baseX = goblin.getX() + goblin.getSize() / 2.0;
        double baseY = goblin.getY() + goblin.getSize();
        // Ground contact shadow
        g.setFill(Color.web("#000e15", 0.42));
        g.fillOval(baseX - 16, baseY - 7, 32, 12);

        double bob = goblin.isMoving() ? Math.abs(Math.sin(goblin.getAnimationTime() * 8.0)) * 2.5 : 0;

        g.save();
        try {
            if (!goblin.isFacingRight()) {
                g.translate(baseX, 0);
                g.scale(-1, 1);
                g.translate(-baseX, 0);
            }
            // Legs (moss green skin)
            g.setFill(Color.web("#2d5532"));
            g.fillRoundRect(baseX - 12, baseY - 16 - bob, 8, 14, 4, 4);
            g.fillRoundRect(baseX + 4, baseY - 16 - bob, 8, 14, 4, 4);
            // Foot wraps (leather)
            g.setFill(Color.web("#3d291a"));
            g.fillOval(baseX - 14, baseY - 5 - bob, 11, 6);
            g.fillOval(baseX + 3, baseY - 5 - bob, 11, 6);

            // Torso / ragged leather tunic
            g.setFill(Color.web("#4e3825"));
            g.fillRoundRect(baseX - 14, baseY - 34 - bob, 26, 20, 6, 6);
            // Belt & buckle
            g.setFill(Color.web("#2a1b10"));
            g.fillRect(baseX - 14, baseY - 20 - bob, 26, 4);
            g.setFill(Color.web("#c4a242"));
            g.fillRect(baseX - 3, baseY - 21 - bob, 6, 6);

            // Goblin head (hunched slightly forward)
            g.setFill(Color.web("#427848"));
            g.fillOval(baseX - 10, baseY - 48 - bob, 22, 19);

            // Pointed goblin ears
            double[] earX1 = {baseX - 8, baseX - 19, baseX - 6};
            double[] earY1 = {baseY - 42 - bob, baseY - 47 - bob, baseY - 37 - bob};
            g.setFill(Color.web("#36633b"));
            g.fillPolygon(earX1, earY1, 3);
            double[] earX2 = {baseX + 8, baseX + 21, baseX + 9};
            double[] earY2 = {baseY - 41 - bob, baseY - 45 - bob, baseY - 35 - bob};
            g.setFill(Color.web("#427848"));
            g.fillPolygon(earX2, earY2, 3);
            g.setFill(Color.web("#6a8a65"));
            g.fillOval(baseX + 10, baseY - 42 - bob, 6, 4);

            // Glowing yellow/amber eye
            g.setFill(Color.web("#ffe066"));
            g.fillOval(baseX + 2, baseY - 42 - bob, 5, 4);
            g.setFill(Color.web("#ffffff"));
            g.fillOval(baseX + 4, baseY - 41 - bob, 2, 2);

            // Snout / mouth with sharp fangs
            g.setFill(Color.web("#2d5532"));
            g.fillOval(baseX + 4, baseY - 36 - bob, 7, 5);
            g.setFill(Color.web("#f5eedc"));
            g.fillRect(baseX + 6, baseY - 36 - bob, 2, 3);

            // Arm & wooden club
            g.setFill(Color.web("#427848"));
            g.fillRoundRect(baseX + 5, baseY - 31 - bob, 6, 12, 3, 3);
            g.setFill(Color.web("#664a2b"));
            g.save();
            g.translate(baseX + 10, baseY - 28 - bob);
            double clubRot = 25;
            if (goblin.isAttacking()) {
                if (goblin.getAttackTimer() < goblin.getAttackWindup()) {
                    clubRot = -35; // raised back in wind-up telegraph
                } else {
                    clubRot = 65;  // swung forward striking
                }
            }
            g.rotate(clubRot);
            g.fillRoundRect(-3, -16, 7, 24, 4, 4);
            g.setFill(Color.web("#8c9296"));
            g.fillOval(2, -14, 3, 3);
            g.fillOval(-4, -10, 3, 3);
            g.restore();

            // Attack slash visual on hit frame
            if (goblin.isAttacking() && goblin.getAttackTimer() >= goblin.getAttackWindup()) {
                g.setStroke(Color.web("#f59e0b", 0.85));
                g.setLineWidth(3.0);
                g.strokeArc(baseX + 8, baseY - 38 - bob, 24, 26, -30, 80, javafx.scene.shape.ArcType.OPEN);
            }

            // Hit reaction flash
            if (goblin.isHitFlashing()) {
                g.setFill(Color.web("#ffffff", 0.75));
                g.fillOval(baseX - 16, baseY - 48 - bob, 32, 42);
                g.setFill(Color.web("#ff3838", 0.45));
                g.fillOval(baseX - 12, baseY - 42 - bob, 24, 30);
            }
        } finally {
            g.restore();
        }
    }

    private static void drawShadowWolf(GraphicsContext g, ShadowWolf wolf) {
        double baseX = wolf.getX() + wolf.getSize() / 2.0;
        double baseY = wolf.getY() + wolf.getSize();
        // Ground contact shadow
        g.setFill(Color.web("#000e15", 0.45));
        g.fillOval(baseX - 24, baseY - 8, 48, 14);

        double anim = wolf.getAnimationTime();
        double bob = wolf.isMoving() ? Math.abs(Math.sin(anim * 10.0)) * 2.5 : 0;
        double legSwing = wolf.isMoving() ? Math.sin(anim * 10.0) * 6.0 : 0;

        g.save();
        try {
            if (!wolf.isFacingRight()) {
                g.translate(baseX, 0);
                g.scale(-1, 1);
                g.translate(-baseX, 0);
            }
            // Shadow aura wisps
            g.setFill(Color.web("#14243b", 0.35));
            g.fillOval(baseX - 26, baseY - 36 - bob, 48, 26);

            // Back legs (far side)
            g.setFill(Color.web("#12141f"));
            g.fillRoundRect(baseX - 20 - legSwing, baseY - 20 - bob, 7, 18, 3, 3);
            g.fillRoundRect(baseX + 10 + legSwing, baseY - 20 - bob, 7, 18, 3, 3);

            // Tail
            g.setFill(Color.web("#181b2a"));
            double[] tailX = {baseX - 18, baseX - 34, baseX - 26};
            double[] tailY = {baseY - 26 - bob, baseY - 34 - bob + Math.sin(anim * 5.0) * 3, baseY - 20 - bob};
            g.fillPolygon(tailX, tailY, 3);

            // Main body (sleek dark predator torso)
            g.setFill(Color.web("#1b1e2e"));
            g.fillRoundRect(baseX - 22, baseY - 29 - bob, 38, 18, 9, 9);
            // Chest ruff
            g.setFill(Color.web("#262b42"));
            g.fillOval(baseX + 2, baseY - 31 - bob, 16, 16);

            // Front legs (near side)
            g.setFill(Color.web("#181b2a"));
            g.fillRoundRect(baseX - 16 + legSwing, baseY - 19 - bob, 7, 18, 3, 3);
            g.fillRoundRect(baseX + 14 - legSwing, baseY - 19 - bob, 7, 18, 3, 3);
            g.setFill(Color.web("#0e1017"));
            g.fillOval(baseX - 18 + legSwing, baseY - 4 - bob, 9, 5);
            g.fillOval(baseX + 13 - legSwing, baseY - 4 - bob, 9, 5);

            // Wolf Head
            g.setFill(Color.web("#1e2235"));
            g.fillOval(baseX + 10, baseY - 36 - bob, 19, 16);
            // Snout
            double[] snoutX = {baseX + 22, baseX + 34, baseX + 22};
            double[] snoutY = {baseY - 33 - bob, baseY - 27 - bob, baseY - 23 - bob};
            g.fillPolygon(snoutX, snoutY, 3);
            // Nose
            g.setFill(Color.web("#0a0c10"));
            g.fillOval(baseX + 32, baseY - 28 - bob, 3, 3);

            // Pointed wolf ears
            double[] earX = {baseX + 14, baseX + 19, baseX + 23};
            double[] earY = {baseY - 34 - bob, baseY - 44 - bob, baseY - 34 - bob};
            g.setFill(Color.web("#161824"));
            g.fillPolygon(earX, earY, 3);

            // Spectral turquoise glowing eye (turns predator crimson on attack)
            g.setFill(wolf.isAttacking() ? Color.web("#ff2222") : Color.web("#38efdf"));
            g.fillOval(baseX + 21, baseY - 33 - bob, 5, 3);
            g.setFill(wolf.isAttacking() ? Color.web("#ffea55") : Color.web("#c4fffa"));
            g.fillOval(baseX + 23, baseY - 32 - bob, 2, 2);

            // Fangs
            g.setFill(Color.web("#e6f4f1"));
            g.fillRect(baseX + 26, baseY - 25 - bob, 2, 3);

            // Bite / lunge slash visual during attack hit frame
            if (wolf.isAttacking() && wolf.getAttackTimer() >= wolf.getAttackWindup()) {
                g.setStroke(Color.web("#ef4444", 0.90));
                g.setLineWidth(3.0);
                g.strokeArc(baseX + 22, baseY - 36 - bob, 24, 24, -40, 90, javafx.scene.shape.ArcType.OPEN);
            }

            // Hit reaction flash
            if (wolf.isHitFlashing()) {
                g.setFill(Color.web("#ffffff", 0.75));
                g.fillOval(baseX - 24, baseY - 34 - bob, 48, 30);
                g.setFill(Color.web("#ff3838", 0.45));
                g.fillOval(baseX - 18, baseY - 30 - bob, 36, 22);
            }
        } finally {
            g.restore();
        }
    }

    private static Image loadImage(String path) {
        try (InputStream is = WorldRenderer.class.getResourceAsStream(path)) {
            if (is != null) {
                return new Image(is);
            }
        } catch (Exception ignored) {
        }
        return null;
    }

    private static void draw(GraphicsContext g, EnvironmentObject o) {
        double x = o.x(), y = o.y(), w = o.width(), h = o.height();
        switch (o.kind()) {
            case PATH -> {
                g.setFill(Color.web("#253f3d"));
                g.fillPolygon(new double[]{x, x+w*.6, x+w, x+w*.85, x+w*.22},
                        new double[]{y+h*.4, y, y+h*.22, y+h*.87, y+h}, 5);
                g.setStroke(Color.web("#42605a", .6));
                g.setLineWidth(1);
                g.strokeLine(x+w*.2, y+h*.7, x+w*.65, y+h*.75);
                g.setFill(Color.web("#185247"));
                g.fillOval(x+w*.12, y+h*.55, 14, 5);
            }
            case TREE -> {
                g.setFill(Color.web("#021a1d", .5));
                g.fillOval(x+w*.1, y+h*.55, w*.85, h*.45);
                // Buttress roots and twisted branches extend beyond the narrow trunk hitbox.
                g.setStroke(Color.web("#092024"));
                g.setLineWidth(w*.09);
                for (int i = 0; i < 5; i++) {
                    double endX = x+w*(.10+i*.19);
                    g.strokeLine(x+w*.50, y+h*.79, endX, y+h*(.90+(i%2)*.05));
                }
                g.setFill(Color.web("#081d23"));
                g.beginPath();
                g.moveTo(x+w*.39, y+h*.9);
                g.bezierCurveTo(x+w*.6, y+h*.65, x+w*.26, y+h*.4, x+w*.46, y+h*.13);
                g.lineTo(x+w*.61, y+h*.15);
                g.bezierCurveTo(x+w*.43, y+h*.46, x+w*.7, y+h*.65, x+w*.61, y+h*.9);
                g.closePath(); g.fill();
                g.setStroke(Color.web("#28605c", .8)); g.setLineWidth(2);
                g.strokeLine(x+w*.57, y+h*.85, x+w*.53, y+h*.45);
                for (int i = 0; i < 6; i++) {
                    double side = i % 2 == 0 ? .09 : .9;
                    g.setStroke(Color.web("#081d23")); g.setLineWidth(10-i);
                    g.strokeLine(x+w*.52, y+h*(.57-i*.06), x+w*side, y+h*(.16+i*.025));
                }
                for (int i = 0; i < 24; i++) {
                    double angle = i * 2.4;
                    double leafX = x+w*.5 + Math.cos(angle)*w*.34 - w*.16;
                    double leafY = y+h*.23 + Math.sin(angle)*h*.17 - h*.08;
                    g.setFill(Color.web(i%3 == 0 ? "#164540" : "#0b3332", .86));
                    g.fillOval(leafX, leafY, w*.34, h*.18);
                    g.setStroke(Color.web("#35736a", .28)); g.setLineWidth(1);
                    g.strokeArc(leafX, leafY, w*.34, h*.18, 25, 85, javafx.scene.shape.ArcType.OPEN);
                }
                g.setStroke(Color.web("#1d514b", .6)); g.setLineWidth(1);
                for (int i = 0; i < 7; i++) {
                    g.strokeLine(x+w*(.12+i*.12), y+h*.23,
                            x+w*(.10+i*.12), y+h*(.46+(i%3)*.07));
                }
            }
            case ROCK -> {
                g.setFill(Color.web("#051d21", .6)); g.fillOval(x, y+h*.3, w, h*.8);
                g.setFill(Color.web("#294548"));
                g.fillPolygon(new double[]{x+w*.08,x+w*.2,x+w*.68,x+w*.95,x+w*.8,x+w*.25},
                        new double[]{y+h*.65,y+h*.2,y+h*.1,y+h*.5,y+h*.87,y+h*.92},6);
                g.setFill(Color.web("#3a5c59"));
                g.fillPolygon(new double[]{x+w*.2,x+w*.68,x+w*.83,x+w*.43},
                        new double[]{y+h*.2,y+h*.1,y+h*.4,y+h*.52},4);
                g.setFill(Color.web("#24584a")); g.fillOval(x+w*.15,y+h*.2,w*.55,h*.2);
            }
            case LARGE_ROOT, SMALL_ROOT -> {
                boolean large = o.kind() == EnvironmentObject.Kind.LARGE_ROOT;
                g.setStroke(Color.web(large ? "#173a36" : "#17372e"));
                g.setLineWidth(large ? h*.50 : 3);
                g.beginPath(); g.moveTo(x+w*.06,y+h*.65);
                g.bezierCurveTo(x+w*.30,y+h*.15,x+w*.67,y+h*.95,x+w*.95,y+h*.4); g.stroke();
                g.setStroke(Color.web(large ? "#54826a" : "#254336")); g.setLineWidth(large ? 2 : 1);
                g.strokeLine(x+w*.12,y+h*.51,x+w*.85,y+h*.49);
            }
            case BUSH -> {
                for (int i = 0; i < 7; i++) {
                    g.setFill(Color.web(i%2 == 0 ? "#10463b" : "#153d35"));
                    g.fillOval(x+(i%3)*w*.23,y+(i%2)*h*.3,w*.50,h*.65);
                }
            }
            case GRASS -> {
                g.setStroke(Color.web("#28624e", .65)); g.setLineWidth(1);
                for (int i = 0; i < 5; i++) {
                    g.strokeLine(x+w*.5,y+h,x+i*w*.24,y+h*(i%3)*.25);
                }
            }
            case MIST -> {
                g.setFill(new RadialGradient(0,0,.5,.5,.5,true,CycleMethod.NO_CYCLE,
                        new Stop(0,Color.web("#36c7bf",.10)),new Stop(1,Color.TRANSPARENT)));
                g.fillOval(x,y,w,h);
            }
            case FIREFLY -> {
                g.setFill(new RadialGradient(0,0,.5,.5,.5,true,CycleMethod.NO_CYCLE,
                        new Stop(0,Color.web("#69fff0",.65)),new Stop(1,Color.TRANSPARENT)));
                g.fillOval(x-4,y-4,w+8,h+8);
                g.setFill(Color.web("#91eee1", .85)); g.fillOval(x+4,y+4,2,2);
            }
        }
    }
}
