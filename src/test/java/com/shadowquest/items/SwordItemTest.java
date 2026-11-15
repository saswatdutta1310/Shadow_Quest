package com.shadowquest.items;

import com.shadowquest.player.Player;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("Sword Item Pickup & Equipment")
class SwordItemTest {

    @Test
    @DisplayName("Sword initially exists on the ground and is not picked up")
    void initialStateOnGround() {
        SwordItem sword = new SwordItem(180, 140);

        assertEquals(180, sword.getX());
        assertEquals(140, sword.getY());
        assertEquals(SwordItem.DEFAULT_PICKUP_RADIUS, sword.getPickupRadius());
        assertFalse(sword.isPickedUp());
    }

    @Test
    @DisplayName("Player picks up and equips sword when entering pickup range")
    void picksUpWhenInRange() {
        SwordItem sword = new SwordItem(180, 140);
        Player player = new Player("Arin");
        assertFalse(player.isSwordEquipped());

        // Spawn player right beside the sword (within 36 units)
        player.spawnAt(180, 140, null);

        boolean pickedUp = sword.checkPickup(player);

        assertTrue(pickedUp, "Sword should be picked up when in range");
        assertTrue(sword.isPickedUp(), "Sword state should be marked as picked up");
        assertTrue(player.isSwordEquipped(), "Player must now have the sword equipped");
    }

    @Test
    @DisplayName("Sword remains on ground when player is outside pickup range")
    void remainsWhenOutsideRange() {
        SwordItem sword = new SwordItem(180, 140);
        Player player = new Player("Arin");

        // Player is far away at (60, 110)
        player.spawnAt(60, 110, null);

        boolean pickedUp = sword.checkPickup(player);

        assertFalse(pickedUp, "Sword should not be picked up from far away");
        assertFalse(sword.isPickedUp());
        assertFalse(player.isSwordEquipped());
    }

    @Test
    @DisplayName("Sword cannot be picked up repeatedly")
    void cannotBePickedUpRepeatedly() {
        SwordItem sword = new SwordItem(180, 140);
        Player player1 = new Player("Arin");
        player1.spawnAt(180, 140, null);

        assertTrue(sword.checkPickup(player1));
        assertTrue(sword.isPickedUp());

        // Second check with another or same player fails
        Player player2 = new Player("Clone");
        player2.spawnAt(180, 140, null);

        assertFalse(sword.checkPickup(player2), "Cannot pick up an already-claimed sword");
        assertFalse(player2.isSwordEquipped());
    }

    @Test
    @DisplayName("Dead or null player cannot pick up sword")
    void deadOrNullPlayerCannotPickUp() {
        SwordItem sword = new SwordItem(180, 140);

        assertFalse(sword.checkPickup(null));
        assertFalse(sword.isPickedUp());

        Player deadPlayer = new Player("Arin");
        deadPlayer.spawnAt(180, 140, null);
        deadPlayer.takeDamage(100);
        assertFalse(deadPlayer.isAlive());

        assertFalse(sword.checkPickup(deadPlayer));
        assertFalse(sword.isPickedUp());
        assertFalse(deadPlayer.isSwordEquipped());
    }

    @Test
    @DisplayName("Constructor rejects invalid or non-finite parameters")
    void constructorValidation() {
        assertThrows(IllegalArgumentException.class, () -> new SwordItem(Double.NaN, 100));
        assertThrows(IllegalArgumentException.class, () -> new SwordItem(100, Double.POSITIVE_INFINITY));
        assertThrows(IllegalArgumentException.class, () -> new SwordItem(100, 100, -5));
        assertThrows(IllegalArgumentException.class, () -> new SwordItem(100, 100, 0));
    }
}
