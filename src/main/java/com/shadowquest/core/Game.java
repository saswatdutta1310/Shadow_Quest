package com.shadowquest.core;

import com.shadowquest.audio.AudioManager;
import com.shadowquest.audio.AudioTrack;
import com.shadowquest.audio.SoundEffect;
import com.shadowquest.combat.CombatSystem;
import com.shadowquest.enemy.Enemy;
import com.shadowquest.enemy.GiantGoblin;
import com.shadowquest.enemy.ShadowLord;
import com.shadowquest.items.HealthPotion;
import com.shadowquest.items.PowerUpItem;
import com.shadowquest.items.SwordItem;
import com.shadowquest.player.Player;
import com.shadowquest.world.GameWorld;
import com.shadowquest.world.MysticForest;
import com.shadowquest.world.Portal;
import com.shadowquest.world.ShadowCastle;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;

/** Coordinates one session across the two-level game without depending on JavaFX or running a game loop. */
public class Game {
    public static final double TRANSITION_DURATION = 2.0;
    public static final int SWORD_UPGRADE_ATTACK_BONUS = 5;

    private GameState state = GameState.MENU;
    private int currentLevel = 1;
    private Player player;
    private GameWorld world;
    private final List<Enemy> enemies;
    private SwordItem swordItem;
    private final List<HealthPotion> healthPotions;
    private PowerUpItem powerUpItem;
    private Portal portal;
    private final CombatSystem combatSystem;

    private boolean swordUpgraded = false;
    private double swordUpgradeMessageTimer = 0.0;

    private boolean transitioning = false;
    private double transitionTimer = 0.0;

    public static final double BOSS_POTION_COOLDOWN = 15.0;
    public static final double BOSS_POTION_MIN_DIST = 120.0;
    public static final double BOSS_POTION_MAX_DIST = 220.0;
    public static final double BOSS_POTION_TRIGGER_HP = 80.0;

    private double bossPotionCooldownTimer = 0.0;
    private HealthPotion bossFightPotion = null;
    private final java.util.Random bossPotionRandom = new java.util.Random(42);

    public Game() {
        this(MysticForest.create(), MysticForest.createEnemies(), MysticForest.createSwordItem(), MysticForest.createPotions());
    }

    public Game(GameWorld world) {
        this(world, List.of(), null, List.of());
    }

    public Game(GameWorld world, List<Enemy> enemies) {
        this(world, enemies, null, List.of());
    }

    public Game(GameWorld world, List<Enemy> enemies, SwordItem swordItem) {
        this(world, enemies, swordItem, List.of());
    }

    public Game(GameWorld world, List<Enemy> enemies, SwordItem swordItem, List<HealthPotion> healthPotions) {
        this.world = Objects.requireNonNull(world, "world");
        this.enemies = enemies != null ? new ArrayList<>(enemies) : new ArrayList<>();
        this.swordItem = swordItem;
        this.healthPotions = healthPotions != null ? new ArrayList<>(healthPotions) : new ArrayList<>();
        this.combatSystem = new CombatSystem();
        this.portal = new Portal(1440, 480);
    }

    public int getCurrentLevel() {
        return currentLevel;
    }

    public GameWorld getWorld() {
        return world;
    }

    public GameState getState() {
        return state;
    }

    public List<Enemy> getEnemies() {
        return Collections.unmodifiableList(enemies);
    }

    public SwordItem getSwordItem() {
        return swordItem;
    }

    public List<HealthPotion> getHealthPotions() {
        return Collections.unmodifiableList(healthPotions);
    }

    public PowerUpItem getPowerUpItem() {
        return powerUpItem;
    }

    public Portal getPortal() {
        return portal;
    }

    public CombatSystem getCombatSystem() {
        return combatSystem;
    }

    public boolean isSwordUpgraded() {
        return swordUpgraded;
    }

    public double getSwordUpgradeMessageTimer() {
        return swordUpgradeMessageTimer;
    }

    public boolean isTransitioning() {
        return transitioning;
    }

    public double getTransitionTimer() {
        return transitionTimer;
    }

    /** Returns the session's player, or null before gameplay starts (menu or cutscene). */
    public Player getPlayer() {
        return player;
    }

    /** Applies movement only to the active session; other states ignore the command. */
    public void movePlayer(int horizontal, int vertical, double deltaTime) {
        if (state == GameState.PLAYING && !transitioning && player != null) {
            player.move(horizontal, vertical, deltaTime, world);
        }
    }

    /** Executes a sword attack in the active session if the player has an equipped weapon. */
    public boolean attackPlayer() {
        if (state == GameState.PLAYING && !transitioning && player != null) {
            boolean started = combatSystem.triggerPlayerAttack(player, enemies);
            if (started) {
                AudioManager.getInstance().playSfx(SoundEffect.SWORD_SWING);
            }
            return started;
        }
        return false;
    }

    /**
     * Attempts to enter the portal to transition from Level 1 (Mystic Forest) to Level 2 (Shadow Castle).
     *
     * @return true if the transition began; false if conditions were not met
     */
    public boolean enterPortal() {
        if (state == GameState.PLAYING && portal != null && portal.canEnter(player) && !transitioning) {
            portal.setEntered(true);
            transitioning = true;
            transitionTimer = TRANSITION_DURATION;
            AudioManager.getInstance().playSfx(SoundEffect.PORTAL_ENTER);
            return true;
        }
        return false;
    }

    /**
     * Initializes and loads Level 2: Shadow Castle.
     * Preserves player health and upgraded sword attack power.
     */
    public void loadShadowCastle() {
        currentLevel = 2;
        transitioning = false;
        transitionTimer = 0.0;
        this.world = ShadowCastle.create();
        this.enemies.clear();
        this.enemies.addAll(ShadowCastle.createEnemies());
        this.healthPotions.clear();
        this.healthPotions.addAll(ShadowCastle.createPotions());
        this.swordItem = null;
        this.portal = null;
        this.bossFightPotion = null;
        this.bossPotionCooldownTimer = 0.0;
        if (player != null) {
            player.spawnAt(ShadowCastle.PLAYER_SPAWN_X, ShadowCastle.PLAYER_SPAWN_Y, world);
        }
        AudioManager.getInstance().playMusic(AudioTrack.CASTLE, true);
        combatSystem.reset();
    }

    /**
     * Resets the current level following defeat.
     * In Level 1, resets Mystic Forest and base stats.
     * In Level 2, resets Shadow Castle, restoring full health and preserving upgraded sword power.
     */
    public void restartLevel() {
        if (currentLevel == 1) {
            spawnPlayer();
            if (world != null) {
                player.spawnAt(60, 110, world);
            }
            enemies.clear();
            enemies.addAll(MysticForest.createEnemies());
            this.swordItem = MysticForest.createSwordItem();
            this.healthPotions.clear();
            this.healthPotions.addAll(MysticForest.createPotions());
            this.powerUpItem = null;
            this.portal = new Portal(1440, 480);
            this.swordUpgraded = false;
            this.swordUpgradeMessageTimer = 0.0;
            this.transitioning = false;
            this.transitionTimer = 0.0;
            this.combatSystem.reset();
            this.state = GameState.PLAYING;
        } else {
            this.world = ShadowCastle.create();
            if (player != null) {
                player.heal(100.0);
                player.spawnAt(ShadowCastle.PLAYER_SPAWN_X, ShadowCastle.PLAYER_SPAWN_Y, world);
            } else {
                spawnPlayer();
                player.upgradeSword(SWORD_UPGRADE_ATTACK_BONUS);
                player.spawnAt(ShadowCastle.PLAYER_SPAWN_X, ShadowCastle.PLAYER_SPAWN_Y, world);
            }
            enemies.clear();
            enemies.addAll(ShadowCastle.createEnemies());
            this.healthPotions.clear();
            this.healthPotions.addAll(ShadowCastle.createPotions());
            this.swordItem = null;
            this.powerUpItem = null;
            this.portal = null;
            this.bossFightPotion = null;
            this.bossPotionCooldownTimer = 0.0;
            this.transitioning = false;
            this.transitionTimer = 0.0;
            this.combatSystem.reset();
            this.state = GameState.PLAYING;
        }
    }

    /** Starts from the menu or restarts after defeat; other states are unchanged. */
    public void startNewGame() {
        if (state == GameState.MENU || state == GameState.GAME_OVER || state == GameState.VICTORY) {
            currentLevel = 1;
            spawnPlayer();
            state = GameState.PLAYING;
        }
    }

    /** Enters the opening cinematic from the menu; the player is created only when it completes. */
    public void startOpeningCinematic() {
        if (state == GameState.MENU) {
            state = GameState.CUTSCENE;
        }
    }

    /**
     * Ends the opening cinematic and starts gameplay exactly once.
     * The CUTSCENE state guard means repeat calls (double skip, extra frames) do nothing.
     */
    public void completeCutscene() {
        if (state == GameState.CUTSCENE) {
            spawnPlayer();
            state = GameState.PLAYING;
        }
    }

    /** Leaves the opening cinematic without starting gameplay, returning to the menu. */
    public void cancelCutscene() {
        if (state == GameState.CUTSCENE) {
            state = GameState.MENU;
        }
    }

    /** Shared session-start rule: Arin spawns at the world's upper-left boundary. */
    private void spawnPlayer() {
        player = new Player("Arin");
        player.constrainToWorld(world);
    }

    /** Pauses an active session. Calls in other states have no effect. */
    public void pause() {
        if (state == GameState.PLAYING && !transitioning) {
            state = GameState.PAUSED;
        }
    }

    /** Resumes a paused session. Calls in other states have no effect. */
    public void resume() {
        if (state == GameState.PAUSED) {
            state = GameState.PLAYING;
        }
    }

    /**
     * Updates game rules, timers, combat, enemy AI, and state transitions for one frame.
     *
     * @param deltaTime elapsed seconds, finite and non-negative
     * @throws IllegalArgumentException if elapsed time is invalid
     */
    public void update(double deltaTime) {
        if (!Double.isFinite(deltaTime) || deltaTime < 0) {
            throw new IllegalArgumentException("Elapsed time must be finite and non-negative.");
        }
        if (state == GameState.PAUSED) {
            return;
        }
        if (transitioning) {
            transitionTimer -= deltaTime;
            if (transitionTimer <= 0) {
                transitionTimer = 0.0;
                loadShadowCastle();
            }
            return;
        }
        if (swordUpgradeMessageTimer > 0) {
            swordUpgradeMessageTimer -= deltaTime;
            if (swordUpgradeMessageTimer < 0) {
                swordUpgradeMessageTimer = 0.0;
            }
        }
        if (bossPotionCooldownTimer > 0) {
            bossPotionCooldownTimer -= deltaTime;
            if (bossPotionCooldownTimer < 0) {
                bossPotionCooldownTimer = 0.0;
            }
        }
        if (state == GameState.PLAYING) {
            if (player != null && !player.isAlive()) {
                state = GameState.GAME_OVER;
                AudioManager.getInstance().playMusic(AudioTrack.GAME_OVER, false);
                return;
            }
            if (swordItem != null && player != null) {
                if (swordItem.checkPickup(player)) {
                    AudioManager.getInstance().playSfx(SoundEffect.SWORD_PICKUP);
                }
            }
            if (player != null) {
                for (HealthPotion potion : healthPotions) {
                    if (potion.checkPickup(player)) {
                        AudioManager.getInstance().playSfx(SoundEffect.HEAL);
                    }
                }
                if (powerUpItem != null) {
                    powerUpItem.checkPickup(player);
                }
            }
            combatSystem.update(deltaTime, player, enemies);
            if (player != null && !player.isAlive()) {
                state = GameState.GAME_OVER;
                AudioManager.getInstance().playMusic(AudioTrack.GAME_OVER, false);
                return;
            }
            checkBossActivation();
            for (Enemy enemy : enemies) {
                if (enemy.isAlive() && enemy.isActive()) {
                    enemy.update(deltaTime, player, world);
                }
            }
            if (currentLevel == 2 && player != null && player.isAlive()) {
                for (Enemy enemy : enemies) {
                    if (enemy instanceof ShadowLord boss && boss.isActive() && boss.isAlive()) {
                        if (boss.isPlayerDetected(player)) {
                            if (AudioManager.getInstance().getCurrentTrack() != AudioTrack.BOSS) {
                                AudioManager.getInstance().playSfx(SoundEffect.SHADOW_LORD_INTRO);
                                AudioManager.getInstance().playMusic(AudioTrack.BOSS, true);
                            }
                        }
                        break;
                    }
                }
            }
            checkGiantGoblinDefeat();
            checkBossDefeatPowerUp();
            checkShadowLordDefeat();
            updateBossPotionSpawn(deltaTime);
            if (player != null && !player.isAlive()) {
                state = GameState.GAME_OVER;
                AudioManager.getInstance().playMusic(AudioTrack.GAME_OVER, false);
            }
        }
    }

    /**
     * Checks if the Level 1 Giant Goblin boss has been defeated:
     * 1. Awards the permanent magical sword upgrade (+5 ATK).
     * 2. Triggers the "SWORD UPGRADED — ATTACK POWER +5" notification.
     * 3. Activates the ancient portal to Shadow Castle.
     */
    public void checkGiantGoblinDefeat() {
        if (currentLevel == 1) {
            for (Enemy enemy : enemies) {
                if (enemy instanceof GiantGoblin boss && boss.isActive() && !boss.isAlive()) {
                    if (!swordUpgraded) {
                        swordUpgraded = true;
                        if (player != null) {
                            player.upgradeSword(SWORD_UPGRADE_ATTACK_BONUS);
                        }
                        swordUpgradeMessageTimer = 4.0;
                        AudioManager.getInstance().playSfx(SoundEffect.SWORD_UPGRADE);
                        AudioManager.getInstance().playSfx(SoundEffect.PORTAL_UNLOCK);
                        AudioManager.getInstance().playMusic(AudioTrack.FOREST, true);
                    }
                    if (portal != null && !portal.isActive()) {
                        portal.setActive(true);
                    }
                    break;
                }
            }
        }
    }

    public void checkBossDefeatPowerUp() {
        if (powerUpItem == null) {
            for (Enemy enemy : enemies) {
                if (enemy instanceof GiantGoblin boss && boss.isActive() && !boss.isAlive()) {
                    powerUpItem = new PowerUpItem(boss.getX() + boss.getSize() / 4.0, boss.getY() + boss.getSize() / 4.0);
                    break;
                }
            }
        }
    }

    /**
     * Checks if the Level 2 Shadow Lord final boss has been defeated:
     * Triggers the final VICTORY state.
     */
    public void checkShadowLordDefeat() {
        if (currentLevel == 2) {
            for (Enemy enemy : enemies) {
                if (enemy instanceof ShadowLord boss && !boss.isAlive()) {
                    if (state != GameState.VICTORY) {
                        state = GameState.VICTORY;
                        AudioManager.getInstance().playSfx(SoundEffect.SHADOW_LORD_DEATH);
                        AudioManager.getInstance().playMusic(AudioTrack.VICTORY, false);
                    }
                    break;
                }
            }
        }
    }

    public HealthPotion getBossFightPotion() {
        return bossFightPotion;
    }

    public double getBossPotionCooldownTimer() {
        return bossPotionCooldownTimer;
    }

    /**
     * Periodically spawns a restorative health potion during the Level 2 Shadow Lord fight
     * when Arin's health drops below 80 HP.
     */
    public void updateBossPotionSpawn(double deltaTime) {
        if (currentLevel != 2 || player == null || !player.isAlive()) {
            return;
        }
        // Only trigger when fighting a living, active Shadow Lord
        ShadowLord activeBoss = null;
        for (Enemy enemy : enemies) {
            if (enemy instanceof ShadowLord lord && lord.isActive() && lord.isAlive()) {
                activeBoss = lord;
                break;
            }
        }
        if (activeBoss == null) {
            return;
        }

        // Trigger condition: Arin's HP < 80.0
        if (player.getHealth() >= BOSS_POTION_TRIGGER_HP) {
            return;
        }

        // Cooldown check
        if (bossPotionCooldownTimer > 0) {
            return;
        }

        // At most one active spawned boss potion at a time
        if (bossFightPotion != null && !bossFightPotion.isPickedUp()) {
            return;
        }

        // Spawn a 35 HP potion nearby (120 - 220 units from Arin) in valid playable space
        HealthPotion potion = findSafeBossPotionSpawn(player, activeBoss);
        if (potion != null) {
            bossFightPotion = potion;
            healthPotions.add(potion);
            bossPotionCooldownTimer = BOSS_POTION_COOLDOWN;
        }
    }

    private HealthPotion findSafeBossPotionSpawn(Player player, ShadowLord boss) {
        double px = player.getX();
        double py = player.getY();
        double potionSize = 20.0;

        for (int attempt = 0; attempt < 30; attempt++) {
            double angle = bossPotionRandom.nextDouble() * 2 * Math.PI;
            double dist = BOSS_POTION_MIN_DIST + bossPotionRandom.nextDouble() * (BOSS_POTION_MAX_DIST - BOSS_POTION_MIN_DIST);
            double potX = px + Math.cos(angle) * dist;
            double potY = py + Math.sin(angle) * dist;

            // Check world bounds with margin
            if (world != null) {
                if (potX < 40 || potX + potionSize > world.getWidth() - 40
                        || potY < 40 || potY + potionSize > world.getHeight() - 40) {
                    continue;
                }
                // Check solid obstacles
                boolean hitsSolid = false;
                for (var obj : world.getObjects()) {
                    if (obj.isSolid()) {
                        var b = obj.hitbox();
                        if (potX < b.right() && potX + potionSize > b.x()
                                && potY < b.bottom() && potY + potionSize > b.y()) {
                            hitsSolid = true;
                            break;
                        }
                    }
                }
                if (hitsSolid) continue;
            }

            // Check not directly on player (< 60 units) or boss (< 80 units)
            double distToPlayer = Math.hypot(potX - px, potY - py);
            double distToBoss = Math.hypot(potX - boss.getX(), potY - boss.getY());
            if (distToPlayer < 60.0 || distToBoss < 80.0) {
                continue;
            }

            return new HealthPotion(potX, potY, 35.0);
        }
        return null;
    }

    /**
     * Checks if boss activation conditions have been met in the current session.
     * Awakens inactive bosses (such as the Giant Goblin) when their prerequisites are satisfied.
     */
    public void checkBossActivation() {
        for (Enemy enemy : enemies) {
            if (enemy instanceof GiantGoblin boss && !boss.isActive()) {
                if (boss.checkActivationConditions(enemies)) {
                    boss.setActive(true);
                    AudioManager.getInstance().playMusic(AudioTrack.BOSS, true);
                }
            }
        }
    }
}
