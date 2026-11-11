package com.shadowquest.player;

import com.shadowquest.audio.AudioManager;
import com.shadowquest.audio.SoundEffect;
import com.shadowquest.combat.AttackHitbox;
import com.shadowquest.utils.Direction;
import com.shadowquest.world.GameWorld;
import com.shadowquest.world.CollisionManager;
import java.util.Objects;

/** A player with initial stats and movement in world coordinates. */
public class Player extends Character {
    private static final double INITIAL_MAX_HEALTH = 100;
    private static final int INITIAL_ATTACK_POWER = 10;
    private static final int INITIAL_DEFENSE = 5;
    private static final double MOVEMENT_SPEED = 200;
    private static final double SIZE = 24;

    public static final double ATTACK_DURATION = 0.15;
    public static final double ATTACK_COOLDOWN = 0.35;
    public static final double ATTACK_HITBOX_WIDTH = 48.0;
    public static final double ATTACK_HITBOX_HEIGHT = 40.0;

    public static final double INVULNERABILITY_DURATION = 0.90;
    public static final double DAMAGE_FLASH_DURATION = 0.20;

    private final double speed;
    private boolean facingRight = true;
    private boolean moving = false;
    private double animationTime = 0.0;

    private boolean swordEquipped = false;
    private boolean attacking = false;
    private double attackTimer = 0.0;
    private double attackCooldownTimer = 0.0;

    private int attackBonus = 0;
    private boolean swordUpgraded = false;

    private double invulnerabilityTimer = 0.0;
    private double damageFlashTimer = 0.0;

    /** Creates a full-health player at the world origin. Stats are initial tuning values. */
    public Player(String name) {
        super(name, INITIAL_MAX_HEALTH, INITIAL_ATTACK_POWER, INITIAL_DEFENSE);
        speed = MOVEMENT_SPEED;
    }

    /** Returns movement speed in world units per second. */
    public double getSpeed() {
        return speed;
    }

    /** Width and height of the body whose top-left is (getX(), getY()). */
    public double getSize() {
        return SIZE;
    }

    /** Whether the player is currently facing towards the right. */
    public boolean isFacingRight() {
        return facingRight;
    }

    /** Whether the player is actively moving this frame. */
    public boolean isMoving() {
        return moving;
    }

    /** Cumulative animation time in seconds during active movement. */
    public double getAnimationTime() {
        return animationTime;
    }

    /** Whether the sword has been collected and equipped. */
    public boolean isSwordEquipped() {
        return swordEquipped;
    }

    /** Equips the sword on Arin. */
    public void equipSword() {
        this.swordEquipped = true;
    }

    /** Additional attack power gained through power-ups. */
    public int getAttackBonus() {
        return attackBonus;
    }

    /** Whether the sword has received the permanent magical upgrade (+5 ATK). */
    public boolean isSwordUpgraded() {
        return swordUpgraded;
    }

    /** Upgrades the sword with a permanent attack power bonus (awarded exactly once). */
    public void upgradeSword(int bonus) {
        if (!swordUpgraded && bonus > 0) {
            swordUpgraded = true;
            addAttackBonus(bonus);
        }
    }

    /** Adds a permanent attack power bonus (e.g. from the defeated level boss). */
    public void addAttackBonus(int bonus) {
        if (bonus > 0) {
            this.attackBonus += bonus;
        }
    }

    /** Resets the attack bonus upon starting a fresh session or restarting the level. */
    public void resetAttackBonus() {
        this.attackBonus = 0;
        this.swordUpgraded = false;
    }

    /**
     * Total effective attack power including base player stats and boss power-ups.
     */
    @Override
    public int getAttackPower() {
        return super.getAttackPower() + attackBonus;
    }

    /** Whether Arin is currently executing a sword attack swing. */
    public boolean isAttacking() {
        return attacking;
    }

    /** Remaining time of the active attack swing in seconds. */
    public double getAttackTimer() {
        return attackTimer;
    }

    /** Remaining cooldown time until the next attack can be initiated. */
    public double getAttackCooldownTimer() {
        return attackCooldownTimer;
    }

    /** Whether the player is ready to perform an attack. */
    public boolean canAttack() {
        return swordEquipped && isAlive() && attackCooldownTimer <= 0 && !attacking;
    }

    /**
     * Initiates a melee sword attack if equipped, alive, and off cooldown.
     *
     * @return true if the attack began; false otherwise
     */
    public boolean attack() {
        if (!canAttack()) {
            return false;
        }
        attacking = true;
        attackTimer = ATTACK_DURATION;
        attackCooldownTimer = ATTACK_COOLDOWN;
        return true;
    }

    /** Whether Arin is currently in an invulnerability recovery period after taking damage. */
    public boolean isInvulnerable() {
        return invulnerabilityTimer > 0;
    }

    /** Remaining duration of the invulnerability period in seconds. */
    public double getInvulnerabilityTimer() {
        return invulnerabilityTimer;
    }

    /** Whether Arin is currently displaying the red damage flash effect. */
    public boolean isDamageFlashing() {
        return damageFlashTimer > 0;
    }

    /** Remaining duration of the damage flash in seconds. */
    public double getDamageFlashTimer() {
        return damageFlashTimer;
    }

    /**
     * Applies incoming combat damage to the player if alive and not currently invulnerable.
     * Triggers the invulnerability grace period and damage flash.
     *
     * @param amount health reduction to apply, must be positive
     * @return true if damage was applied; false if ignored due to invulnerability, death, or non-positive value
     */
    public boolean takeCombatDamage(double amount) {
        if (!isAlive() || isInvulnerable() || amount <= 0) {
            return false;
        }
        boolean wasAlive = isAlive();
        takeDamage(amount);
        invulnerabilityTimer = INVULNERABILITY_DURATION;
        damageFlashTimer = DAMAGE_FLASH_DURATION;
        if (wasAlive && !isAlive()) {
            AudioManager.getInstance().playSfx(SoundEffect.PLAYER_DEATH);
        } else if (isAlive()) {
            AudioManager.getInstance().playSfx(SoundEffect.PLAYER_HURT);
        }
        return true;
    }

    /**
     * Updates attack, cooldown, invulnerability, and damage flash timers using delta time.
     */
    public void update(double deltaTime) {
        if (!Double.isFinite(deltaTime) || deltaTime < 0) {
            throw new IllegalArgumentException("Elapsed time must be finite and non-negative.");
        }
        if (attackTimer > 0) {
            attackTimer -= deltaTime;
            if (attackTimer <= 0) {
                attacking = false;
                attackTimer = 0.0;
            }
        }
        if (attackCooldownTimer > 0) {
            attackCooldownTimer -= deltaTime;
            if (attackCooldownTimer < 0) {
                attackCooldownTimer = 0.0;
            }
        }
        if (invulnerabilityTimer > 0) {
            invulnerabilityTimer -= deltaTime;
            if (invulnerabilityTimer < 0) {
                invulnerabilityTimer = 0.0;
            }
        }
        if (damageFlashTimer > 0) {
            damageFlashTimer -= deltaTime;
            if (damageFlashTimer < 0) {
                damageFlashTimer = 0.0;
            }
        }
    }

    /**
     * Computes the directional attack hitbox in front of Arin based on current facing direction.
     */
    public AttackHitbox getAttackHitbox() {
        double centerY = getY() + SIZE / 2.0;
        double hitboxY = centerY - ATTACK_HITBOX_HEIGHT / 2.0;
        double hitboxX = facingRight ? getX() + SIZE : getX() - ATTACK_HITBOX_WIDTH;
        return new AttackHitbox(hitboxX, hitboxY, ATTACK_HITBOX_WIDTH, ATTACK_HITBOX_HEIGHT);
    }

    /** Sets the player's world position; clamped to world boundaries if world is provided. */
    public void spawnAt(double startX, double startY, GameWorld world) {
        setPosition(startX, startY);
        if (world != null) {
            constrainToWorld(world);
        }
    }

    /** Session movement: retain normalization, then sweep against solid world geometry. */
    public void move(int horizontal, int vertical, double deltaTime, GameWorld world) {
        Objects.requireNonNull(world, "world");
        if (world.getWidth() < SIZE || world.getHeight() < SIZE) {
            throw new IllegalArgumentException("World must be large enough to contain the player.");
        }
        double oldX = getX();
        double oldY = getY();
        move(horizontal, vertical, deltaTime);
        constrainToWorld(world);
        var position = CollisionManager.resolve(world, oldX, oldY, getX(), getY(), SIZE);
        setPosition(position.x(), position.y());
    }

    /** Keeps the whole body inside the world; Game calls this after movement. */
    public void constrainToWorld(GameWorld world) {
        Objects.requireNonNull(world, "world");
        if (world.getWidth() < SIZE || world.getHeight() < SIZE) {
            throw new IllegalArgumentException("World must be large enough to contain the player.");
        }
        setPosition(Math.max(0, Math.min(getX(), world.getWidth() - SIZE)),
                Math.max(0, Math.min(getY(), world.getHeight() - SIZE)));
    }

    /**
     * Moves a living player in one direction by speed multiplied by elapsed seconds.
     * Game applies world boundaries after movement; this primitive has no world context.
     *
     * @throws IllegalArgumentException for a null direction, invalid time, or overflow
     */
    public void move(Direction direction, double deltaTime) {
        if (direction == null) {
            throw new IllegalArgumentException("Movement direction must not be null.");
        }
        switch (direction) {
            case UP -> move(0, -1, deltaTime);
            case DOWN -> move(0, 1, deltaTime);
            case LEFT -> move(-1, 0, deltaTime);
            case RIGHT -> move(1, 0, deltaTime);
        }
    }

    /** Moves along digital input axes (-1, 0, 1), normalizing diagonals. */
    public void move(int horizontal, int vertical, double deltaTime) {
        if (horizontal < -1 || horizontal > 1 || vertical < -1 || vertical > 1) {
            throw new IllegalArgumentException("Movement axes must be -1, 0, or 1.");
        }
        if (!Double.isFinite(deltaTime) || deltaTime < 0) {
            throw new IllegalArgumentException("Elapsed time must be finite and non-negative.");
        }
        double distance = speed * deltaTime;
        if (!Double.isFinite(distance)) {
            throw new IllegalArgumentException("Elapsed time is too large for movement.");
        }
        if (horizontal > 0) {
            facingRight = true;
        } else if (horizontal < 0) {
            facingRight = false;
        }
        moving = (horizontal != 0 || vertical != 0) && isAlive();
        if (moving) {
            animationTime += deltaTime;
        }

        if (!isAlive() || (horizontal == 0 && vertical == 0)) {
            return;
        }
        double length = Math.hypot(horizontal, vertical);
        moveBy(horizontal / length * distance, vertical / length * distance);
    }
}
