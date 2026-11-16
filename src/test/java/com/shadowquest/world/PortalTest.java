package com.shadowquest.world;

import com.shadowquest.player.Player;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("Magical Realm Portal")
class PortalTest {

    @Test
    @DisplayName("Portal initializes with given coordinates and is inactive by default")
    void initialState() {
        Portal portal = new Portal(1440, 480);

        assertEquals(1440, portal.getX());
        assertEquals(480, portal.getY());
        assertEquals(Portal.DEFAULT_INTERACTION_RADIUS, portal.getInteractionRadius());
        assertFalse(portal.isActive(), "Portal must be inactive before boss defeat");
        assertFalse(portal.isEntered(), "Portal must not be entered initially");
    }

    @Test
    @DisplayName("Player in range check")
    void playerInRange() {
        Portal portal = new Portal(1440, 480, 60.0);
        Player player = new Player("Arin");

        // Inside interaction radius
        player.spawnAt(1430, 470, null);
        assertTrue(portal.isPlayerInRange(player));

        // Outside interaction radius
        player.spawnAt(100, 100, null);
        assertFalse(portal.isPlayerInRange(player));
    }

    @Test
    @DisplayName("Cannot enter portal while it is inactive (before boss defeat)")
    void cannotEnterWhileInactive() {
        Portal portal = new Portal(1440, 480);
        Player player = new Player("Arin");
        player.spawnAt(1440, 480, null);

        assertFalse(portal.isActive());
        assertFalse(portal.canEnter(player), "Player cannot enter an inactive portal");
    }

    @Test
    @DisplayName("Player can enter portal once activated after boss defeat")
    void canEnterWhenActive() {
        Portal portal = new Portal(1440, 480);
        Player player = new Player("Arin");
        player.spawnAt(1440, 480, null);

        portal.setActive(true);
        assertTrue(portal.canEnter(player), "Player in range must be able to enter active portal");

        portal.setEntered(true);
        assertTrue(portal.isEntered());
        assertFalse(portal.canEnter(player), "Cannot enter an already-entered portal (prevents duplicate transitions)");
    }

    @Test
    @DisplayName("Dead or null player cannot enter portal")
    void deadOrNullPlayer() {
        Portal portal = new Portal(1440, 480);
        portal.setActive(true);

        assertFalse(portal.canEnter(null));

        Player deadPlayer = new Player("Arin");
        deadPlayer.spawnAt(1440, 480, null);
        deadPlayer.takeDamage(100);
        assertFalse(deadPlayer.isAlive());

        assertFalse(portal.canEnter(deadPlayer));
    }

    @Test
    @DisplayName("Constructor parameter validation")
    void constructorValidation() {
        assertThrows(IllegalArgumentException.class, () -> new Portal(Double.NaN, 100));
        assertThrows(IllegalArgumentException.class, () -> new Portal(100, Double.POSITIVE_INFINITY));
        assertThrows(IllegalArgumentException.class, () -> new Portal(100, 100, 0));
        assertThrows(IllegalArgumentException.class, () -> new Portal(100, 100, -10));
    }
}
