package com.shadowquest.world;

import com.shadowquest.enemy.DarkArcher;
import com.shadowquest.enemy.Enemy;
import com.shadowquest.enemy.ShadowKnight;
import com.shadowquest.enemy.ShadowLord;
import com.shadowquest.enemy.Wraith;
import com.shadowquest.items.HealthPotion;
import java.util.ArrayList;
import java.util.List;
import static com.shadowquest.world.EnvironmentObject.Kind.*;

/**
 * Level 2: Shadow Castle environment, layout, and enemy distribution.
 *
 * A dark gothic fortress of ruined arches, cracked flagstones, stone pillars,
 * pale green ghostly mist, and burning torch sconces, culminating in the throne room
 * of the final boss: the Shadow Lord.
 *
 * Denizens: Shadow Knights, Dark Archers, and Wraiths (no forest goblins/wolves).
 * Domain logic is strictly decoupled from presentation and JavaFX.
 */
public final class ShadowCastle {
    public static final double WORLD_WIDTH = 2400.0;
    public static final double WORLD_HEIGHT = 1400.0;

    public static final double PLAYER_SPAWN_X = 100.0;
    public static final double PLAYER_SPAWN_Y = 300.0;

    private ShadowCastle() {}

    /**
     * Creates restorative health potions distributed along the castle corridors and courtyard.
     */
    public static List<HealthPotion> createPotions() {
        return List.of(
            new HealthPotion(520, 280, 35.0),
            new HealthPotion(1100, 460, 35.0),
            new HealthPotion(1600, 620, 35.0)
        );
    }

    /**
     * Creates the hostile castle denizens (Shadow Knight, Dark Archer, Wraith)
     * and the final boss: Shadow Lord.
     */
    public static List<Enemy> createEnemies() {
        return List.of(
            new ShadowKnight(480, 310, 40),
            new DarkArcher(850, 420, 45),
            new Wraith(1150, 380, 50),
            new ShadowKnight(1400, 580, 50),
            new DarkArcher(1620, 700, 45),
            new Wraith(1750, 500, 40),
            new ShadowLord(1950, 650, 50)
        );
    }

    /**
     * Creates the complete solid boundary, ruined gothic pillars, arches, and stone pathways.
     */
    public static GameWorld create() {
        var objects = new ArrayList<EnvironmentObject>();

        // 1. Stone pathways and flagstone floor corridors
        for (int i = 0; i < 50; i++) {
            double x = 70 + i * 42;
            double y = 260 + Math.sin(i * 0.15) * 100 + (i > 25 ? (i - 25) * 6.0 : 0);
            objects.add(new EnvironmentObject(PATH, x, y, 64, 48));
        }

        // 2. North fortress wall (solid boundary barrier)
        for (int x = 0; x < 2400; x += 100) {
            double w = Math.min(100, 2400 - x);
            objects.add(new EnvironmentObject(ROCK, x, 0, w, 80));
        }

        // 3. South fortress wall (solid boundary barrier)
        for (int x = 0; x < 2400; x += 100) {
            double w = Math.min(100, 2400 - x);
            objects.add(new EnvironmentObject(ROCK, x, 1320, w, 80));
        }

        // 4. West wall flanking the entrance (spawn at 100, 300)
        objects.add(new EnvironmentObject(ROCK, 0, 80, 80, 160));
        objects.add(new EnvironmentObject(ROCK, 0, 400, 80, 920));

        // 5. East wall behind the throne room
        for (int y = 80; y < 1320; y += 100) {
            double h = Math.min(100, 1320 - y);
            objects.add(new EnvironmentObject(ROCK, 2320, y, 80, h));
        }

        // 6. Ruined gothic stone pillars and obstacles in the halls and courtyards
        double[][] pillars = {
            {350, 180}, {350, 440},
            {680, 200}, {680, 520},
            {980, 220}, {980, 580},
            {1280, 260}, {1280, 680},
            {1580, 320}, {1580, 780},
            {1780, 420}, {1780, 880},
            {2150, 450}, {2150, 850}
        };
        for (double[] p : pillars) {
            objects.add(new EnvironmentObject(ROCK, p[0], p[1], 75, 75));
        }

        // 7. Gothic grand arches framing the courtyard transitions
        objects.add(new EnvironmentObject(TREE, 650, 100, 160, 220));
        objects.add(new EnvironmentObject(TREE, 1350, 120, 160, 220));
        objects.add(new EnvironmentObject(TREE, 1850, 180, 180, 240));

        // 8. Pale green ghostly mist drifting through the castle halls
        double[][] mistLocations = {
            {200, 320}, {500, 480}, {850, 350}, {1200, 520},
            {1600, 450}, {1950, 720}, {2100, 600}
        };
        for (double[] m : mistLocations) {
            objects.add(new EnvironmentObject(MIST, m[0], m[1], 180, 110));
        }

        // 9. Burning torch sconces and orange embers
        double[][] torches = {
            {120, 230}, {120, 390},
            {400, 200}, {720, 220},
            {1020, 240}, {1320, 280},
            {1620, 340}, {1820, 440},
            {2180, 480}, {2180, 820}
        };
        for (double[] t : torches) {
            objects.add(new EnvironmentObject(FIREFLY, t[0], t[1], 18, 18));
        }

        return new GameWorld(WORLD_WIDTH, WORLD_HEIGHT, objects);
    }
}
