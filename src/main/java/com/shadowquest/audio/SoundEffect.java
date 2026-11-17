package com.shadowquest.audio;

/**
 * Playable sound effects for player, enemies, environment, and interface.
 */
public enum SoundEffect {
    // Player
    SWORD_PICKUP("/assets/audio/sfx/player/sword_pickup.wav"),
    SWORD_SWING("/assets/audio/sfx/player/sword_swing.wav"),
    SWORD_HIT("/assets/audio/sfx/player/sword_hit.wav"),
    PLAYER_HURT("/assets/audio/sfx/player/player_hurt.wav"),
    PLAYER_DEATH("/assets/audio/sfx/player/player_death.wav"),
    HEAL("/assets/audio/sfx/player/heal.wav"),
    SWORD_UPGRADE("/assets/audio/sfx/player/sword_upgrade.wav"),

    // Enemies
    GOBLIN_ATTACK("/assets/audio/sfx/enemies/goblin_attack.wav"),
    GOBLIN_DEATH("/assets/audio/sfx/enemies/goblin_death.wav"),
    WOLF_ATTACK("/assets/audio/sfx/enemies/wolf_attack.wav"),
    WOLF_DEATH("/assets/audio/sfx/enemies/wolf_death.wav"),
    GIANT_GOBLIN_ATTACK("/assets/audio/sfx/enemies/giant_goblin_attack.wav"),
    GIANT_GOBLIN_DEATH("/assets/audio/sfx/enemies/giant_goblin_death.wav"),
    SHADOW_KNIGHT_ATTACK("/assets/audio/sfx/enemies/shadow_knight_attack.wav"),
    SHADOW_KNIGHT_DEATH("/assets/audio/sfx/enemies/shadow_knight_death.wav"),
    DARK_ARCHER_ATTACK("/assets/audio/sfx/enemies/dark_archer_attack.wav"),
    DARK_ARCHER_DEATH("/assets/audio/sfx/enemies/dark_archer_death.wav"),
    WRAITH_ATTACK("/assets/audio/sfx/enemies/wraith_attack.wav"),
    WRAITH_DEATH("/assets/audio/sfx/enemies/wraith_death.wav"),

    // Portal
    PORTAL_UNLOCK("/assets/audio/sfx/portal/portal_unlock.wav"),
    PORTAL_ENTER("/assets/audio/sfx/portal/portal_enter.wav"),
    PORTAL_AMBIENT("/assets/audio/sfx/portal/portal_ambient.wav"),

    // Boss (Shadow Lord)
    SHADOW_LORD_INTRO("/assets/audio/sfx/boss/shadow_lord_intro.wav"),
    SHADOW_LORD_MELEE("/assets/audio/sfx/boss/shadow_lord_melee.wav"),
    SHADOW_LORD_FIRE("/assets/audio/sfx/boss/shadow_lord_fire.wav"),
    SHADOW_LORD_FIRE_HIT("/assets/audio/sfx/boss/shadow_lord_fire_hit.wav"),
    SHADOW_LORD_DEATH("/assets/audio/sfx/boss/shadow_lord_death.wav"),

    // Interface
    UI_PAUSE("/assets/audio/sfx/ui/pause.wav"),
    UI_RESUME("/assets/audio/sfx/ui/resume.wav"),
    UI_CLICK("/assets/audio/sfx/ui/button_click.wav"),
    UI_CONFIRM("/assets/audio/sfx/ui/confirm.wav");

    private final String resourcePath;

    SoundEffect(String resourcePath) {
        this.resourcePath = resourcePath;
    }

    public String getResourcePath() {
        return resourcePath;
    }
}
