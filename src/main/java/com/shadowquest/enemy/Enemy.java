package com.shadowquest.enemy;

import com.shadowquest.audio.AudioManager;
import com.shadowquest.audio.SoundEffect;
import com.shadowquest.combat.CombatSystem;
import com.shadowquest.player.Character;
import com.shadowquest.player.Player;
import com.shadowquest.world.CollisionManager;
import com.shadowquest.world.GameWorld;

/**
 * Abstract base class for all hostile non-player characters.
 *
 * Implements deterministic AI (detection radius, chase, idle/patrol),
 * frame-rate-independent movement, world boundary containment, and obstacle collision.
 * Domain logic is strictly independent of presentation and JavaFX.
 */
public abstract class Enemy extends Character {
    private final double speed;
    private final double detectionRadius;
    private final double size;
    private final double stopDistance;
    private final int rewardCoins;
    private final double attackRange;
    private final double attackWindup;
    private final double attackDuration;
    private final double attackCooldown;

    private double patrolStartX;
    private double patrolStartY;
    private double patrolRadius;
    private double patrolTimer;

    public static final double HIT_FLASH_DURATION = 0.15;

    private boolean facingRight = true;
    private boolean moving = false;
    private double animationTime = 0.0;
    private boolean active = true;
    private double hitFlashTimer = 0.0;

    private boolean attacking = false;
    private double attackTimer = 0.0;
    private double attackCooldownTimer = 0.0;
    private boolean attackDamageApplied = false;

    protected Enemy(String name, double maxHealth, int attackPower, int defense,
                    double speed, double detectionRadius, double size,
                    double stopDistance, int rewardCoins,
                    double startX, double startY, double patrolRadius) {
        this(name, maxHealth, attackPower, defense, speed, detectionRadius, size,
                stopDistance, rewardCoins, startX, startY, patrolRadius,
                stopDistance + 8.0, 0.25, 0.40, 1.20);
    }

    protected Enemy(String name, double maxHealth, int attackPower, int defense,
                    double speed, double detectionRadius, double size,
                    double stopDistance, int rewardCoins,
                    double startX, double startY, double patrolRadius,
                    double attackRange, double attackWindup, double attackDuration, double attackCooldown) {
        super(name, maxHealth, attackPower, defense);
        if (!Double.isFinite(speed) || speed <= 0) {
            throw new IllegalArgumentException("Enemy speed must be finite and positive.");
        }
        if (!Double.isFinite(detectionRadius) || detectionRadius <= 0) {
            throw new IllegalArgumentException("Detection radius must be finite and positive.");
        }
        if (!Double.isFinite(size) || size <= 0) {
            throw new IllegalArgumentException("Enemy size must be finite and positive.");
        }
        if (!Double.isFinite(stopDistance) || stopDistance < 0) {
            throw new IllegalArgumentException("Stop distance must be finite and non-negative.");
        }
        if (rewardCoins < 0) {
            throw new IllegalArgumentException("Reward coins must be non-negative.");
        }
        if (!Double.isFinite(attackRange) || attackRange <= 0) {
            throw new IllegalArgumentException("Attack range must be finite and positive.");
        }
        if (!Double.isFinite(attackWindup) || attackWindup < 0) {
            throw new IllegalArgumentException("Attack windup must be finite and non-negative.");
        }
        if (!Double.isFinite(attackDuration) || attackDuration <= 0 || attackDuration < attackWindup) {
            throw new IllegalArgumentException("Attack duration must be positive and at least equal to windup.");
        }
        if (!Double.isFinite(attackCooldown) || attackCooldown < 0) {
            throw new IllegalArgumentException("Attack cooldown must be finite and non-negative.");
        }
        this.speed = speed;
        this.detectionRadius = detectionRadius;
        this.size = size;
        this.stopDistance = stopDistance;
        this.rewardCoins = rewardCoins;
        this.patrolStartX = startX;
        this.patrolStartY = startY;
        this.patrolRadius = patrolRadius;
        this.attackRange = attackRange;
        this.attackWindup = attackWindup;
        this.attackDuration = attackDuration;
        this.attackCooldown = attackCooldown;
        setPosition(startX, startY);
    }

    public double getSpeed() {
        return speed;
    }

    public double getDetectionRadius() {
        return detectionRadius;
    }

    public double getSize() {
        return size;
    }

    public double getStopDistance() {
        return stopDistance;
    }

    public int getRewardCoins() {
        return rewardCoins;
    }

    public boolean isFacingRight() {
        return facingRight;
    }

    protected void setFacingRight(boolean facingRight) {
        this.facingRight = facingRight;
    }

    public boolean isMoving() {
        return moving;
    }

    protected void setMoving(boolean moving) {
        this.moving = moving;
    }

    public double getAnimationTime() {
        return animationTime;
    }

    public double getPatrolStartX() {
        return patrolStartX;
    }

    public double getPatrolStartY() {
        return patrolStartY;
    }

    public double getPatrolRadius() {
        return patrolRadius;
    }

    public double getAttackRange() {
        return attackRange;
    }

    public double getAttackWindup() {
        return attackWindup;
    }

    public double getAttackDuration() {
        return attackDuration;
    }

    public double getAttackCooldown() {
        return attackCooldown;
    }

    public boolean isAttacking() {
        return attacking;
    }

    public double getAttackTimer() {
        return attackTimer;
    }

    public double getAttackCooldownTimer() {
        return attackCooldownTimer;
    }

    public boolean isAttackDamageApplied() {
        return attackDamageApplied;
    }

    public boolean isActive() {
        return active;
    }

    public void setActive(boolean active) {
        this.active = active;
    }

    public void triggerHitFlash() {
        this.hitFlashTimer = HIT_FLASH_DURATION;
    }

    public boolean isHitFlashing() {
        return hitFlashTimer > 0;
    }

    public double getHitFlashTimer() {
        return hitFlashTimer;
    }

    public void updateHitFlash(double deltaTime) {
        if (hitFlashTimer > 0) {
            hitFlashTimer -= deltaTime;
            if (hitFlashTimer < 0) {
                hitFlashTimer = 0.0;
            }
        }
    }

    /**
     * Determines whether the player is within this enemy's detection radius.
     */
    public boolean isPlayerDetected(Player player) {
        if (player == null || !player.isAlive()) {
            return false;
        }
        double centerX = getX() + size / 2.0;
        double centerY = getY() + size / 2.0;
        double playerCenterX = player.getX() + player.getSize() / 2.0;
        double playerCenterY = player.getY() + player.getSize() / 2.0;
        return Math.hypot(playerCenterX - centerX, playerCenterY - centerY) <= detectionRadius;
    }

    /**
     * Determines whether the player is within this enemy's attack reach.
     */
    public boolean isPlayerInAttackRange(Player player) {
        if (player == null || !player.isAlive()) {
            return false;
        }
        double centerX = getX() + size / 2.0;
        double centerY = getY() + size / 2.0;
        double playerCenterX = player.getX() + player.getSize() / 2.0;
        double playerCenterY = player.getY() + player.getSize() / 2.0;
        return Math.hypot(playerCenterX - centerX, playerCenterY - centerY) <= attackRange;
    }

    /**
     * Updates AI decision making, combat attacks, and movement for one frame.
     *
     * @param deltaTime elapsed seconds
     * @param player the active player instance (may be null)
     * @param world the active game world for bounds and collision (may be null)
     */
    public void update(double deltaTime, Player player, GameWorld world) {
        if (!Double.isFinite(deltaTime) || deltaTime < 0) {
            throw new IllegalArgumentException("Elapsed time must be finite and non-negative.");
        }
        if (!active || !isAlive()) {
            moving = false;
            attacking = false;
            attackTimer = 0.0;
            return;
        }
        if (attackCooldownTimer > 0) {
            attackCooldownTimer -= deltaTime;
            if (attackCooldownTimer < 0) {
                attackCooldownTimer = 0.0;
            }
        }
        if (!attacking && player != null && player.isAlive() && isPlayerInAttackRange(player)) {
            moving = false;
            facingRight = (player.getX() + player.getSize() / 2.0) >= (getX() + size / 2.0);
            if (attackCooldownTimer <= 0) {
                attacking = true;
                attackTimer = 0.0;
                attackDamageApplied = false;
                onAttackStart(player);
            }
        }

        if (attacking) {
            attackTimer += deltaTime;
            moving = false;
            if (player != null) {
                facingRight = (player.getX() + player.getSize() / 2.0) >= (getX() + size / 2.0);
            }
            onAttackTick(deltaTime, player, world);
            if (attackTimer >= getAttackWindup() && !attackDamageApplied) {
                attackDamageApplied = true;
                applyAttackDamage(player, world);
            }
            if (attackTimer >= getAttackDuration()) {
                attacking = false;
                attackTimer = 0.0;
                attackCooldownTimer = getAttackCooldown();
                onAttackEnd();
            }
            return;
        }

        if (shouldStandGround(player)) {
            moving = false;
            facingRight = (player.getX() + player.getSize() / 2.0) >= (getX() + size / 2.0);
            return;
        }

        if (isPlayerDetected(player)) {
            chasePlayer(deltaTime, player, world);
        } else {
            patrol(deltaTime, world);
        }
    }

    /**
     * Determines whether the enemy should pause movement because it is in attack position.
     */
    protected boolean shouldStandGround(Player player) {
        return player != null && player.isAlive() && isPlayerInAttackRange(player);
    }

    /**
     * Hook invoked once when initiating an attack.
     */
    protected void onAttackStart(Player player) {
        if (this instanceof GiantGoblin) {
            AudioManager.getInstance().playSfx(SoundEffect.GIANT_GOBLIN_ATTACK);
        } else if (this instanceof Goblin) {
            AudioManager.getInstance().playSfx(SoundEffect.GOBLIN_ATTACK);
        } else if (this instanceof ShadowWolf) {
            AudioManager.getInstance().playSfx(SoundEffect.WOLF_ATTACK);
        } else if (this instanceof ShadowKnight) {
            AudioManager.getInstance().playSfx(SoundEffect.SHADOW_KNIGHT_ATTACK);
        } else if (this instanceof DarkArcher) {
            AudioManager.getInstance().playSfx(SoundEffect.DARK_ARCHER_ATTACK);
        } else if (this instanceof Wraith) {
            AudioManager.getInstance().playSfx(SoundEffect.WRAITH_ATTACK);
        }
    }

    /**
     * Hook invoked every frame while an attack is active.
     */
    protected void onAttackTick(double deltaTime, Player player, GameWorld world) {}

    /**
     * Hook invoked to apply attack damage on the hit frame.
     */
    protected void applyAttackDamage(Player player, GameWorld world) {
        if (player != null && player.isAlive() && isPlayerInAttackRange(player)) {
            int damage = CombatSystem.calculateDamage(this, player);
            player.takeCombatDamage(damage);
        }
    }

    /**
     * Hook invoked when an attack completes.
     */
    protected void onAttackEnd() {}

    /**
     * Chases the player towards their center coordinates, stopping at stopDistance.
     */
    protected void chasePlayer(double deltaTime, Player player, GameWorld world) {
        double centerX = getX() + size / 2.0;
        double centerY = getY() + size / 2.0;
        double playerCenterX = player.getX() + player.getSize() / 2.0;
        double playerCenterY = player.getY() + player.getSize() / 2.0;
        double dx = playerCenterX - centerX;
        double dy = playerCenterY - centerY;
        double distance = Math.hypot(dx, dy);

        if (distance > stopDistance && distance > 1e-6) {
            double dirX = dx / distance;
            double dirY = dy / distance;
            facingRight = dirX >= 0;
            moving = true;
            animationTime += deltaTime;
            moveWithCollision(dirX * speed * deltaTime, dirY * speed * deltaTime, world);
        } else {
            moving = false;
            facingRight = dx >= 0;
        }
    }

    /**
     * Deterministic patrol behavior when the player is not detected.
     */
    protected void patrol(double deltaTime, GameWorld world) {
        if (patrolRadius <= 0) {
            moving = false;
            return;
        }
        patrolTimer += deltaTime;
        double cycle = patrolTimer % 4.0;
        double moveDirX = 0;
        if (cycle < 1.5) {
            moveDirX = 1.0;
        } else if (cycle < 2.0) {
            moveDirX = 0;
        } else if (cycle < 3.5) {
            moveDirX = -1.0;
        } else {
            moveDirX = 0;
        }

        if (moveDirX > 0 && getX() >= patrolStartX + patrolRadius) {
            moveDirX = 0;
        } else if (moveDirX < 0 && getX() <= patrolStartX - patrolRadius) {
            moveDirX = 0;
        }

        if (moveDirX != 0) {
            facingRight = moveDirX > 0;
            moving = true;
            animationTime += deltaTime;
            moveWithCollision(moveDirX * speed * 0.5 * deltaTime, 0, world);
        } else {
            moving = false;
        }
    }

    /**
     * Moves by delta coordinates while checking world boundaries and solid obstacle collision.
     */
    protected void moveWithCollision(double deltaX, double deltaY, GameWorld world) {
        double oldX = getX();
        double oldY = getY();
        moveBy(deltaX, deltaY);
        constrainToWorld(world);
        if (world != null) {
            var position = CollisionManager.resolve(world, oldX, oldY, getX(), getY(), size);
            setPosition(position.x(), position.y());
        }
    }

    /**
     * Clamps this enemy within rectangular world boundaries.
     */
    public void constrainToWorld(GameWorld world) {
        if (world != null) {
            setPosition(Math.max(0, Math.min(getX(), world.getWidth() - size)),
                    Math.max(0, Math.min(getY(), world.getHeight() - size)));
        }
    }
}
