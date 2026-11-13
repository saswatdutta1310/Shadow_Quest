package com.shadowquest.world;

import com.shadowquest.core.GameConfig;
import com.shadowquest.enemy.Enemy;
import com.shadowquest.enemy.GiantGoblin;
import com.shadowquest.enemy.Goblin;
import com.shadowquest.enemy.ShadowWolf;
import com.shadowquest.items.HealthPotion;
import com.shadowquest.items.SwordItem;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import static com.shadowquest.world.EnvironmentObject.Kind.*;

/** Repeatable balanced forest layout; optimized for visual clarity, open navigation, and zero enemy spawn overlap. */
public final class MysticForest {
    private MysticForest() { }

    /** Creates the starter sword lying along the opening trail. */
    public static SwordItem createSwordItem() {
        return new SwordItem(180, 140);
    }

    /** Creates restorative health potions distributed along the forest trail and clearings. */
    public static List<HealthPotion> createPotions() {
        return List.of(
            new HealthPotion(480, 150, 35.0),
            new HealthPotion(900, 310, 35.0),
            new HealthPotion(1280, 480, 35.0)
        );
    }

    /** Creates a controlled group of woodland enemies and level boss for Mystic Forest. */
    public static List<Enemy> createEnemies() {
        return List.of(
            new Goblin(360, 120, 40),
            new ShadowWolf(580, 190, 50),
            new Goblin(780, 260, 50),
            new ShadowWolf(1020, 360, 60),
            new Goblin(1200, 430, 50),
            new ShadowWolf(680, 380, 40),
            new GiantGoblin(1400, 520, 40)
        );
    }

    public static GameWorld create() {
        var objects = new ArrayList<EnvironmentObject>();
        // Indices 0 to 75: A winding, walkable stone path begins beside the unchanged player origin.
        for (int i = 0; i < 76; i++) {
            double x = 35 + i * 35;
            double y = 100 + i * 17 + Math.sin(i * .19) * 95;
            objects.add(new EnvironmentObject(PATH, x, y, 90, 45));
        }
        // Index 76: Required atmospheric showcase tree verified by ForestCollisionTest
        objects.add(new EnvironmentObject(TREE, 190, 80, 180, 230));
        // Showcase rock and large root
        objects.add(new EnvironmentObject(ROCK, 420, 165, 82, 62));
        objects.add(new EnvironmentObject(LARGE_ROOT, 540, 310, 150, 52));

        Random random = new Random(3401);

        // Strategically placed trees: spaced out generously to provide open navigation and visibility
        for (int row = 0; row < 5; row++) {
            for (int col = 0; col < 8; col++) {
                // Skip dense placement along the primary trail and enemy battle corridors
                if ((row + col) % 2 != 0) continue;
                double x = 120 + col * 340 + (random.nextDouble() - 0.5) * 40;
                double y = 60 + row * 330 + (random.nextDouble() - 0.5) * 40;
                // Keep the entry clearing and early encounter zones open
                if (x < 760 && y < 450) continue;
                // Keep central clearing for Giant Goblin arena (x: 1300-1550, y: 440-640) clear
                if (x >= 1300 && x <= 1550 && y >= 440 && y <= 640) continue;

                if (x + 200 <= GameConfig.WORLD_WIDTH && y + 230 <= GameConfig.WORLD_HEIGHT) {
                    objects.add(new EnvironmentObject(TREE, x, y, 170, 230));
                }
                if (col % 3 == 0 && x + 180 <= GameConfig.WORLD_WIDTH && y + 260 <= GameConfig.WORLD_HEIGHT) {
                    objects.add(new EnvironmentObject(ROCK, x + 90, y + 210, 60, 40));
                }
            }
        }

        // Additional natural boundary trees framing the north and south edges without blocking paths
        for (int c = 3; c < 7; c++) {
            double x = c * 380;
            if (x + 160 <= GameConfig.WORLD_WIDTH) {
                objects.add(new EnvironmentObject(TREE, x, 1500, 160, 230));
            }
        }

        // Reduced clutter (bushes, small roots, grass tufts) down to 90 well-dispersed decorative objects
        for (int i = 0; i < 90; i++) {
            var kind = i % 7 == 0 ? BUSH : (i % 5 == 0 ? SMALL_ROOT : GRASS);
            double w = (kind == BUSH) ? 50 : 26;
            double h = (kind == BUSH) ? 34 : 20;
            double x = random.nextDouble() * (GameConfig.WORLD_WIDTH - w);
            double y = random.nextDouble() * (GameConfig.WORLD_HEIGHT - h);
            objects.add(new EnvironmentObject(kind, x, y, w, h));
        }

        // Soft, drifting atmospheric mist (reduced from 55 to 14)
        for (int i = 0; i < 14; i++) {
            double x = random.nextDouble() * (GameConfig.WORLD_WIDTH - 360);
            double y = random.nextDouble() * (GameConfig.WORLD_HEIGHT - 130);
            objects.add(new EnvironmentObject(MIST, x, y, 360, 130));
        }

        // Mystical glowing fireflies (reduced from 180 to 36)
        for (int i = 0; i < 36; i++) {
            double x = random.nextDouble() * (GameConfig.WORLD_WIDTH - 12);
            double y = random.nextDouble() * (GameConfig.WORLD_HEIGHT - 12);
            objects.add(new EnvironmentObject(FIREFLY, x, y, 10, 10));
        }

        return new GameWorld(GameConfig.WORLD_WIDTH, GameConfig.WORLD_HEIGHT, objects);
    }
}
