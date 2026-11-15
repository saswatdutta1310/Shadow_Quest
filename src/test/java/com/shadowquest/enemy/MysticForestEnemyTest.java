package com.shadowquest.enemy;

import com.shadowquest.core.Game;
import com.shadowquest.core.GameState;
import com.shadowquest.world.EnvironmentObject;
import com.shadowquest.world.GameWorld;
import com.shadowquest.world.MysticForest;
import java.util.List;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("Mystic Forest Enemy Spawning, Boss Activation & Session Integration")
class MysticForestEnemyTest {

    @Test
    @DisplayName("Spawns exactly 3 normal Goblins, 2-3 ShadowWolves, and 1 Giant Goblin boss")
    void spawnsControlledGroupWithExactCounts() {
        List<Enemy> enemies = MysticForest.createEnemies();

        assertNotNull(enemies);
        assertFalse(enemies.isEmpty());

        long normalGoblinCount = enemies.stream()
                .filter(e -> e instanceof Goblin && !(e instanceof GiantGoblin))
                .count();
        long wolfCount = enemies.stream()
                .filter(e -> e instanceof ShadowWolf)
                .count();
        long giantGoblinCount = enemies.stream()
                .filter(e -> e instanceof GiantGoblin)
                .count();

        assertEquals(3, normalGoblinCount, "Must spawn exactly 3 normal Goblins");
        assertTrue(wolfCount >= 2 && wolfCount <= 3, "Must spawn 2-3 ShadowWolves (found: " + wolfCount + ")");
        assertEquals(1, giantGoblinCount, "Must spawn exactly 1 Giant Goblin boss");
    }

    @Test
    @DisplayName("Giant Goblin boss starts inactive upon world creation")
    void giantGoblinStartsInactive() {
        List<Enemy> enemies = MysticForest.createEnemies();
        GiantGoblin boss = enemies.stream()
                .filter(e -> e instanceof GiantGoblin)
                .map(e -> (GiantGoblin) e)
                .findFirst()
                .orElseThrow();

        assertFalse(boss.isActive(), "Giant Goblin boss must start inactive");
    }

    @Test
    @DisplayName("Giant Goblin boss activates only after all 3 normal Goblins are defeated")
    void giantGoblinActivatesOnlyAfterAllNormalGoblinsDefeated() {
        Game game = new Game();
        game.startNewGame();
        assertEquals(GameState.PLAYING, game.getState());

        List<Enemy> enemies = game.getEnemies();
        GiantGoblin boss = enemies.stream()
                .filter(e -> e instanceof GiantGoblin)
                .map(e -> (GiantGoblin) e)
                .findFirst()
                .orElseThrow();

        List<Goblin> normalGoblins = enemies.stream()
                .filter(e -> e instanceof Goblin && !(e instanceof GiantGoblin))
                .map(e -> (Goblin) e)
                .toList();

        assertEquals(3, normalGoblins.size());
        assertFalse(boss.isActive());

        // Update when all 3 normal goblins are alive: boss remains inactive
        game.update(0.1);
        assertFalse(boss.isActive());

        // Defeat first normal goblin: boss remains inactive
        normalGoblins.get(0).takeDamage(100);
        assertFalse(normalGoblins.get(0).isAlive());
        game.update(0.1);
        assertFalse(boss.isActive());

        // Defeat second normal goblin: boss remains inactive
        normalGoblins.get(1).takeDamage(100);
        assertFalse(normalGoblins.get(1).isAlive());
        game.update(0.1);
        assertFalse(boss.isActive());

        // Defeat third normal goblin: all 3 normal goblins now dead -> boss awakens!
        normalGoblins.get(2).takeDamage(100);
        assertFalse(normalGoblins.get(2).isAlive());
        game.update(0.1);
        assertTrue(boss.isActive(), "Giant Goblin boss must activate when all 3 normal Goblins are defeated");
    }

    @Test
    @DisplayName("None of the spawned enemies overlap any solid obstacle in Mystic Forest")
    void enemySpawnsDoNotOverlapSolidObstacles() {
        GameWorld forest = MysticForest.create();
        List<Enemy> enemies = MysticForest.createEnemies();

        List<EnvironmentObject> solidObjects = forest.getObjects().stream()
                .filter(EnvironmentObject::isSolid)
                .toList();

        for (Enemy enemy : enemies) {
            double ex = enemy.getX();
            double ey = enemy.getY();
            double es = enemy.getSize();

            for (EnvironmentObject obj : solidObjects) {
                var hb = obj.hitbox();
                boolean overlaps = ex < hb.right() && ex + es > hb.x()
                        && ey < hb.bottom() && ey + es > hb.y();
                assertFalse(overlaps, "Enemy " + enemy.getName() + " at (" + ex + ", " + ey + ") overlaps solid "
                        + obj.kind() + " at hitbox (" + hb.x() + ", " + hb.y() + ", " + hb.width() + ", " + hb.height() + ")");
            }
        }
    }

    @Test
    @DisplayName("None of the enemies spawn directly on Arin's initial spawn point")
    void enemySpawnsDoNotOverlapArinSpawn() {
        List<Enemy> enemies = MysticForest.createEnemies();
        double arinSpawnX = 60;
        double arinSpawnY = 110;

        for (Enemy enemy : enemies) {
            double dist = Math.hypot(enemy.getX() - arinSpawnX, enemy.getY() - arinSpawnY);
            assertTrue(dist > 100, "Enemy " + enemy.getName() + " is too close to Arin's spawn point (distance: " + dist + ")");
        }
    }

    @Test
    @DisplayName("Game initializes with Mystic Forest enemies and exposes unmodifiable list")
    void gameInitializesWithEnemies() {
        Game game = new Game();
        List<Enemy> enemies = game.getEnemies();

        assertNotNull(enemies);
        assertFalse(enemies.isEmpty());
        assertThrows(UnsupportedOperationException.class, () -> enemies.add(new Goblin(0, 0)));
    }

    @Test
    @DisplayName("Game update advances enemy AI when playing and preserves them when paused")
    void gameUpdateAdvancesEnemiesWhenPlaying() {
        Game game = new Game();
        game.startNewGame();
        assertEquals(GameState.PLAYING, game.getState());

        List<Enemy> enemies = game.getEnemies();
        assertFalse(enemies.isEmpty());
        Enemy firstEnemy = enemies.get(0);
        double initialAnim = firstEnemy.getAnimationTime();

        // When playing, update updates living active enemies
        game.update(0.1);
        assertTrue(firstEnemy.getAnimationTime() >= initialAnim);

        // When paused, update does not advance enemies
        game.pause();
        double pausedAnim = firstEnemy.getAnimationTime();
        double pausedX = firstEnemy.getX();
        game.update(0.5);
        assertEquals(pausedAnim, firstEnemy.getAnimationTime());
        assertEquals(pausedX, firstEnemy.getX());
    }
}
