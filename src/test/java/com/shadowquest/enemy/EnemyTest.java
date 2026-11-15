package com.shadowquest.enemy;

import com.shadowquest.player.Player;
import com.shadowquest.world.EnvironmentObject;
import com.shadowquest.world.GameWorld;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("Enemy Base Class & AI Mechanics")
class EnemyTest {
    private GameWorld testWorld;

    private static class ConcreteTestEnemy extends Enemy {
        ConcreteTestEnemy(double startX, double startY, double speed, double detectionRadius,
                          double size, double stopDistance, int rewardCoins, double patrolRadius) {
            super("TestEnemy", 50, 5, 2, speed, detectionRadius, size, stopDistance, rewardCoins, startX, startY, patrolRadius);
        }
    }

    @BeforeEach
    void setUp() {
        testWorld = new GameWorld(1000, 1000);
    }

    @Test
    @DisplayName("Validates constructor parameters and initial values")
    void validatesConstructorParameters() {
        assertThrows(IllegalArgumentException.class, () ->
                new ConcreteTestEnemy(100, 100, -10, 100, 24, 20, 5, 50));
        assertThrows(IllegalArgumentException.class, () ->
                new ConcreteTestEnemy(100, 100, 100, -10, 24, 20, 5, 50));
        assertThrows(IllegalArgumentException.class, () ->
                new ConcreteTestEnemy(100, 100, 100, 100, -24, 20, 5, 50));
        assertThrows(IllegalArgumentException.class, () ->
                new ConcreteTestEnemy(100, 100, 100, 100, 24, -5, 5, 50));
        assertThrows(IllegalArgumentException.class, () ->
                new ConcreteTestEnemy(100, 100, 100, 100, 24, 20, -1, 50));
    }

    @Test
    @DisplayName("Initial state has correct coordinates, full health, and default orientation")
    void initialState() {
        ConcreteTestEnemy enemy = new ConcreteTestEnemy(200, 300, 100, 150, 24, 25, 5, 50);
        assertEquals(200, enemy.getX());
        assertEquals(300, enemy.getY());
        assertEquals(50, enemy.getHealth());
        assertEquals(50, enemy.getMaxHealth());
        assertEquals(100, enemy.getSpeed());
        assertEquals(150, enemy.getDetectionRadius());
        assertEquals(24, enemy.getSize());
        assertEquals(25, enemy.getStopDistance());
        assertEquals(5, enemy.getRewardCoins());
        assertTrue(enemy.isFacingRight());
        assertFalse(enemy.isMoving());
        assertEquals(0.0, enemy.getAnimationTime());
    }

    @Test
    @DisplayName("Detects player when within detection radius and alive")
    void detectsPlayerWithinRadius() {
        ConcreteTestEnemy enemy = new ConcreteTestEnemy(200, 200, 100, 150, 24, 25, 5, 50);
        Player player = new Player("Arin");
        player.spawnAt(250, 200, testWorld); // 50 units away

        assertTrue(enemy.isPlayerDetected(player));

        // Move player outside 150 units radius
        player.spawnAt(400, 200, testWorld); // 200 units away
        assertFalse(enemy.isPlayerDetected(player));

        // Null player returns false
        assertFalse(enemy.isPlayerDetected(null));

        // Dead player returns false
        player.spawnAt(250, 200, testWorld);
        player.takeDamage(100);
        assertFalse(player.isAlive());
        assertFalse(enemy.isPlayerDetected(player));
    }

    @Test
    @DisplayName("Chases player towards center coordinates and stops at stop distance")
    void chasesPlayerAndStopsAtStopDistance() {
        ConcreteTestEnemy enemy = new ConcreteTestEnemy(100, 100, 100, 200, 24, 30, 5, 0);
        Player player = new Player("Arin");
        player.spawnAt(200, 100, testWorld); // 100 units to the right

        // Step 1: chase
        enemy.update(0.5, player, testWorld); // Moves 50 units right (100 speed * 0.5s)
        assertTrue(enemy.getX() > 100);
        assertTrue(enemy.isFacingRight());
        assertTrue(enemy.isMoving());
        assertTrue(enemy.getAnimationTime() > 0);

        // Move player close, within stopDistance
        player.spawnAt(enemy.getX() + 20, enemy.getY(), testWorld);
        enemy.update(0.1, player, testWorld);
        assertFalse(enemy.isMoving());
    }

    @Test
    @DisplayName("Faces left when chasing player positioned to the left")
    void facesLeftWhenChasingLeftward() {
        ConcreteTestEnemy enemy = new ConcreteTestEnemy(300, 200, 100, 200, 24, 30, 5, 0);
        Player player = new Player("Arin");
        player.spawnAt(150, 200, testWorld);

        enemy.update(0.1, player, testWorld);
        assertFalse(enemy.isFacingRight());
        assertTrue(enemy.getX() < 300);
    }

    @Test
    @DisplayName("Patrols when player is null or outside detection radius")
    void patrolsWhenPlayerOutsideRadius() {
        ConcreteTestEnemy enemy = new ConcreteTestEnemy(200, 200, 100, 100, 24, 25, 5, 40);
        Player farPlayer = new Player("Arin");
        farPlayer.spawnAt(800, 800, testWorld);

        double initialX = enemy.getX();
        // Update 0.5 seconds: cycle < 1.5 moves right
        enemy.update(0.5, farPlayer, testWorld);
        assertTrue(enemy.getX() > initialX);
        assertTrue(enemy.isFacingRight());
        assertTrue(enemy.isMoving());
    }

    @Test
    @DisplayName("Movement is frame-rate independent across different delta time steps")
    void frameRateIndependentMovement() {
        ConcreteTestEnemy enemy1 = new ConcreteTestEnemy(100, 100, 100, 300, 24, 20, 5, 0);
        ConcreteTestEnemy enemy2 = new ConcreteTestEnemy(100, 100, 100, 300, 24, 20, 5, 0);

        Player player = new Player("Arin");
        player.spawnAt(400, 100, testWorld);

        // 1 step of 0.4s
        enemy1.update(0.4, player, testWorld);

        // 4 steps of 0.1s
        for (int i = 0; i < 4; i++) {
            enemy2.update(0.1, player, testWorld);
        }

        assertEquals(enemy1.getX(), enemy2.getX(), 1e-4);
        assertEquals(enemy1.getY(), enemy2.getY(), 1e-4);
    }

    @Test
    @DisplayName("Constrains enemy within world boundaries")
    void constrainsToWorldBoundaries() {
        ConcreteTestEnemy enemy = new ConcreteTestEnemy(10, 10, 200, 300, 24, 10, 5, 0);
        Player player = new Player("Arin");
        player.spawnAt(-500, -500, testWorld); // Player out of world bounds

        enemy.update(1.0, player, testWorld);
        assertTrue(enemy.getX() >= 0);
        assertTrue(enemy.getY() >= 0);

        // Clamping to right/bottom bounds
        ConcreteTestEnemy enemyNearEdge = new ConcreteTestEnemy(990, 990, 200, 300, 24, 10, 5, 0);
        Player playerFarRight = new Player("Arin");
        playerFarRight.spawnAt(2000, 2000, testWorld);

        enemyNearEdge.update(1.0, playerFarRight, testWorld);
        assertTrue(enemyNearEdge.getX() <= 1000 - 24);
        assertTrue(enemyNearEdge.getY() <= 1000 - 24);
    }

    @Test
    @DisplayName("Resolves collision with solid environment obstacles")
    void resolvesCollisionWithObstacles() {
        // Create world with a solid tree obstacle
        EnvironmentObject tree = new EnvironmentObject(EnvironmentObject.Kind.TREE, 200, 150, 100, 150);
        GameWorld worldWithTree = new GameWorld(1000, 1000, List.of(tree));

        // Position enemy just left of the tree's hitbox and chase player on the right
        double treeHitboxLeft = tree.hitbox().x();
        ConcreteTestEnemy enemy = new ConcreteTestEnemy(treeHitboxLeft - 30, tree.hitbox().y() + 5, 100, 300, 24, 10, 5, 0);

        Player player = new Player("Arin");
        player.spawnAt(treeHitboxLeft + 100, tree.hitbox().y() + 5, worldWithTree);

        // Move into tree
        enemy.update(0.5, player, worldWithTree);

        // Enemy must not penetrate the tree's solid hitbox
        assertTrue(enemy.getX() + enemy.getSize() <= treeHitboxLeft + 1e-6,
                "Enemy right edge (" + (enemy.getX() + enemy.getSize()) + ") should stop before tree hitbox left (" + treeHitboxLeft + ")");
    }

    @Test
    @DisplayName("Dead enemy does not move or update animation")
    void deadEnemyDoesNotMove() {
        ConcreteTestEnemy enemy = new ConcreteTestEnemy(100, 100, 100, 300, 24, 20, 5, 50);
        enemy.takeDamage(100);
        assertFalse(enemy.isAlive());

        Player player = new Player("Arin");
        player.spawnAt(200, 100, testWorld);

        enemy.update(1.0, player, testWorld);
        assertEquals(100, enemy.getX());
        assertEquals(100, enemy.getY());
        assertFalse(enemy.isMoving());
    }

    @ParameterizedTest
    @ValueSource(doubles = {-0.1, Double.NaN, Double.NEGATIVE_INFINITY, Double.POSITIVE_INFINITY})
    @DisplayName("Rejects invalid delta time during update")
    void rejectsInvalidDeltaTime(double invalidDelta) {
        ConcreteTestEnemy enemy = new ConcreteTestEnemy(100, 100, 100, 300, 24, 20, 5, 50);
        assertThrows(IllegalArgumentException.class, () -> enemy.update(invalidDelta, null, testWorld));
    }
}
