# Implementation log

## November 15, 2026: Phase 5 Two-Level Progression: Mystic Forest to Shadow Castle & Final Victory

Finalized the complete two-level game structure (Ancient Ruins completely removed), implemented restorative Health Potions (35 HP), Giant Goblin Boss Reward (+5 Permanent Sword Upgrade to 15 ATK), Ancient Realm Portal & 2.0-second black screen transition, Level 2: Shadow Castle environment, Final Boss: Shadow Lord with dual attack styles (melee void scythe and ranged hellfire), and the final Victory screen.

**Finalized Two-Level Game Scope:**
1. **Level 1: Mystic Forest** — Boss: **Giant Goblin** (Awakens after 3 normal Goblins are defeated; upon defeat, awards +5 Sword Upgrade and unlocks the Ancient Realm Portal).
2. **Level 2: Shadow Castle** — Final Boss: **Shadow Lord** (Ancient dark sorcerer/reaper of the void; defeating him restores Eldoria and triggers the Victory Screen).

**Implementation details:**
- **Health Potion System (`HealthPotion.java`):**
  - Collection restores **35.0 HP** (clamped cleanly to Arin's max health of 100.0 HP).
  - Preserved on ground if Arin is already at full health (100 HP) to prevent accidental waste.
  - Automatic proximity collection (`pickupRadius = 32.0`).
  - Distributed across both levels:
    - Mystic Forest: `(480, 150)`, `(900, 310)`, and `(1280, 480)`.
    - Shadow Castle: `(520, 280)`, `(1100, 460)`, and `(1600, 620)`.
  - Reusable, disappears upon pickup, prevents duplicate collection.
- **Mystic Forest Boss Reward — Permanent Sword Upgrade:**
  - Upon defeating the Giant Goblin, Arin's sword attack power increases permanently by **+5** (`Player.upgradeSword(5)`), scaling attack power from **10 $\to$ 15**.
  - Awarded exactly once; does not stack or repeat on subsequent frames.
  - Carries over into Level 2 (Shadow Castle); persists during level restart in Castle; resets to base 10 on a fresh game from main menu.
  - Visuals: Arin's sword radiates an intensified golden-cyan magical flame aura, and a golden top notification banner displays: `✦ SWORD UPGRADED — ATTACK POWER +5 ✦`.
- **Ancient Realm Portal (`Portal.java`):**
  - Unlocked upon the defeat of the Giant Goblin near the boss arena at `(1440, 480)`.
  - Visuals match the opening cinematic: vertical supernatural tear embedded in an ancient stone/tree arch, glowing turquoise/cyan (`#22d3ee`, `#06b6d4`, `#e0f2fe`), with jagged torn edges and swirling cosmic particles (not a circular ring).
  - Walk within interaction radius (60 units) to see floating hint: `✦ PRESS E TO ENTER PORTAL ✦`.
  - Guarded against premature entry before boss defeat and duplicate transitions.
- **Portal Transition to Shadow Castle:**
  - Pressing E triggers an intentional 2.0-second black screen transition (`Game.isTransitioning()`).
  - Screen displays `ENTERING SHADOW CASTLE...` with subtitle `The final confrontation awaits.`
  - Frame-rate independent delta time countdown without blocking the JavaFX Application Thread (`no Thread.sleep()`).
  - Loads Level 2 upon completion, spawning Arin at castle entrance `(100, 300)`.
  - **Preserves Arin's current health and upgraded sword power (15 ATK).**
- **Shadow Castle Environment (`ShadowCastle.java`):**
  - Dimensions: 2400 $\times$ 1400 world units with camera following and clamping.
  - Dark gothic fortress: cracked purple-gray stone flagstones (`#161029`, `#110b20`), solid perimeter fortress walls, ruined stone arches, gothic pillars, pale green ghostly mist, and flickering torch sconces.
  - Castle enemies: Shadow Goblins, Castle ShadowWolves, and the final boss.
- **Final Boss — Shadow Lord (`ShadowLord.java`):**
  - Tall, terrifying hooded reaper silhouette (size 64, rendered height 110px).
  - Two large curved obsidian horns curling from hood, pitch-black hood void, piercing glowing fiery orange/amber eyes, flowing tattered black/violet robes, and fiery orange magic flame in hand.
  - Stats: Health 350, Attack 24, Defense 8, Speed 70, Detection Radius 340, Attack Range 260.
  - Dual attack patterns:
    1. Close-range Melee: Spectral Void Scythe strike (range 65, windup 0.40s, violet/crimson crescent cutting arc).
    2. Mid-range Ranged: Hellfire Surge (range 260, windup 0.40s, dark orange flame burst).
  - Dedicated Boss Health Bar: Rendered at top-center (`✦ SHADOW LORD — FINAL BOSS ✦`) with violet-to-crimson gradient health bar.
- **Final Victory State & Overlay (`GameScreen.java`):**
  - Defeating the Shadow Lord transitions game to `GameState.VICTORY`.
  - Displays rich victory banner: `VICTORY ACHIEVED!`, `THE SHADOW LORD HAS BEEN VANQUISHED`, `Eldoria is saved from darkness. Arin has restored light to the realm.`, with options `Play Again (Enter)` and `Main Menu (Esc)`.

**Files created:**
- `src/main/java/com/shadowquest/world/Portal.java`
- `src/main/java/com/shadowquest/world/ShadowCastle.java`
- `src/main/java/com/shadowquest/enemy/ShadowLord.java`
- `src/test/java/com/shadowquest/world/PortalTest.java` (6 unit tests)
- `src/test/java/com/shadowquest/enemy/ShadowLordTest.java` (6 unit tests)
- `src/test/java/com/shadowquest/core/LevelProgressionAndShadowCastleTest.java` (9 unit tests)

**Files modified:**
- `src/main/java/com/shadowquest/player/Player.java`
- `src/main/java/com/shadowquest/world/MysticForest.java`
- `src/main/java/com/shadowquest/core/Game.java`
- `src/main/java/com/shadowquest/render/WorldRenderer.java`
- `src/main/java/com/shadowquest/ui/GameScreen.java`
- `src/main/java/com/shadowquest/ui/ScreenManager.java`
- `src/test/java/com/shadowquest/core/GameOverAndRestartTest.java`
- `docs/PROJECT_PLAN.md`
- `docs/IMPLEMENTATION_LOG.md`
- `docs/PROJECT_DEVELOPMENT_REPORT.md`

**Test results:** 377/377 JUnit tests passing (356 baseline + 21 new tests, 0 failures, 0 errors, 0 skipped). BUILD SUCCESS.


## November 15, 2026: Phase 4.3.1 Consumables & Boss Rewards: Health Potions & Damage Power-Up

Implemented restorative Health Potions distributed across Mystic Forest and a legendary damage-boosting Power-Up dropped upon defeating the Level 1 final boss (Giant Goblin), scaling Arin's attack damage for subsequent combat and the next level.

**Three-Level Game Scope & Boss Structure (Completion Target: November 17, 2026):**
1. **Level 1: Mystic Forest** — Boss: **Giant Goblin** (Awakens after all 3 normal Goblins are defeated; drops Titan Strength Crest upon defeat)
2. **Level 2: Ancient Ruins** — Boss: **Ancient Guardian** (Planned)
3. **Level 3: Shadow Castle** — Boss: **Shadow Lord** (Final Boss, Planned)

**Implementation details:**
- **Health Potions (`com.shadowquest.items.HealthPotion`):**
  - Restores player health (`healAmount = 40.0`, clamped to max health `100.0`).
  - **Quality of Life Ground Preservation:** If Arin is already at full health (100 HP), walking over a potion does **not** consume it, preserving valuable healing for when wounded.
  - Automatic proximity pickup with `pickupRadius = 32.0` units.
  - Three potions positioned in Mystic Forest:
    - Potion 1 at `(480, 150)` along the upper trail clearing.
    - Potion 2 at `(900, 310)` mid-forest clearing near the stone cluster.
    - Potion 3 at `(1280, 480)` approach clearing before the Giant Goblin grove.
  - Visuals: Ruby flask with golden stopper, luminous crimson liquid, white curative cross icon, ambient magical healing aura, and overhead `HP +40` pickup indicator.
- **Boss Defeat Power-Up (`com.shadowquest.items.PowerUpItem`):**
  - Dropped dynamically at the exact defeat coordinates of the Giant Goblin boss upon its defeat.
  - Grants a permanent attack boost (`attackBonus = +8`), increasing Arin's attack power from 10 to 18 (`Player.getAttackPower()`).
  - Combat damage scaling:
    - Normal Goblin (DEF 2): damage increases from 9 to 17.
    - ShadowWolf (DEF 1): damage increases from 10 to 18.
    - Future Level 2 enemies will receive this increased base damage immediately.
  - Visuals: Radiant runic diamond crest with golden core, rotating outer light rays, golden magical aura, and overhead `⚔ +8 ATK POWER` collection badge.
- **HUD Attack Badge Integration (`WorldRenderer.java`):**
  - Updated the top-left HUD to feature a sleek weapon badge next to the health bar showing current attack power: `⚔ ATK: 10`.
  - When the boss power-up is claimed, the badge dynamically transforms to an energized amber/gold badge: `⚔ ATK: 18 (+8 Power Up)`.
- **Game Lifecycle & Restart Integration (`Game.java`):**
  - Integrated `healthPotions` and `powerUpItem` into the game loop.
  - On `restartLevel()`, potions are refreshed, any dropped power-up is reset, and player bonus attack power returns to 0.

**Files created:**
- `src/main/java/com/shadowquest/items/HealthPotion.java`
- `src/main/java/com/shadowquest/items/PowerUpItem.java`
- `src/test/java/com/shadowquest/items/HealthPotionTest.java` (9 unit tests)
- `src/test/java/com/shadowquest/items/PowerUpItemTest.java` (7 unit tests)

**Files modified:**
- `src/main/java/com/shadowquest/player/Player.java`
- `src/main/java/com/shadowquest/world/MysticForest.java`
- `src/main/java/com/shadowquest/core/Game.java`
- `src/main/java/com/shadowquest/render/WorldRenderer.java`
- `src/main/java/com/shadowquest/ui/GameScreen.java`
- `src/main/java/com/shadowquest/ui/ScreenManager.java`
- `src/test/java/com/shadowquest/core/GameOverAndRestartTest.java`
- `docs/PROJECT_PLAN.md`
- `docs/IMPLEMENTATION_LOG.md`
- `docs/PROJECT_DEVELOPMENT_REPORT.md`

**Test results:** 356/356 JUnit tests passing (339 baseline + 17 new tests, 0 failures, 0 errors, 0 skipped). BUILD SUCCESS.


## November 15, 2026: Phase 4.2 Player Sword Combat, Enemy Damage & Defeat

Implemented real-time player sword combat, ground sword pickup & equipment, directional attack hitboxes, combat damage calculations, duplicate-hit prevention, enemy hit flashes, enemy defeat lifecycle, and integrated Giant Goblin boss awakening.

**Three-Level Game Scope & Boss Structure (Completion Target: November 17, 2026):**
1. **Level 1: Mystic Forest** — Boss: **Giant Goblin** (Awakens after all 3 normal Goblins are defeated in combat)
2. **Level 2: Ancient Ruins** — Boss: **Ancient Guardian** (Planned)
3. **Level 3: Shadow Castle** — Boss: **Shadow Lord** (Final Boss, Planned)

**Implementation details:**
- **Sword Pickup & Equip System (`com.shadowquest.items.SwordItem`):**
  - Starter sword placed at `(180, 140)` along the opening forest trail, clearly visible in the trailhead clearing ~120px from Arin's spawn `(60, 110)`.
  - Automatic proximity pickup: when Arin steps within `pickupRadius = 36.0` units, `sword.checkPickup(player)` marks the sword picked up and equips it on Arin (`player.equipSword()`).
  - Pickup occurs exactly once; sword disappears from the ground.
  - **Prerequisite for combat:** Arin cannot attack before picking up the sword (`player.canAttack()` requires `isSwordEquipped()`). Pressing Space before pickup does nothing.
- **Player Sword Attack:**
  - Pressing Space triggers a directional melee sword swing.
  - Direction matches Arin's facing direction (`isFacingRight()`).
  - Creates a directional rectangular hitbox (`AttackHitbox`) in front of Arin ($48 \times 40$ world units).
  - Timing: Attack duration `0.15s` (`ATTACK_DURATION`), attack cooldown `0.35s` (`ATTACK_COOLDOWN`), managed frame-rate-independently with delta time.
- **Combat System (`com.shadowquest.combat.CombatSystem`):**
  - Domain combat coordinator free of JavaFX imports.
  - Standard damage formula: `Math.max(1, attacker.getAttackPower() - target.getDefense() / 2)`.
    - Normal Goblin: takes 9 damage per hit (5 hits to defeat 45 HP).
    - ShadowWolf: takes 10 damage per hit (4 hits to defeat 35 HP).
    - Giant Goblin: takes 7 damage per hit (26 hits to defeat 180 HP).
  - **Duplicate-hit prevention:** `Set<Enemy> hitEnemiesThisSwing` ensures any enemy inside the attack hitbox is damaged exactly once per swing.
  - **Boss Immunity:** Inactive Giant Goblin cannot be targeted or damaged until all 3 normal Goblins have died.
- **Enemy Hit Reaction & Defeat:**
  - `hitFlashTimer` in `Enemy` triggers on hit (`0.15s`), displaying a bright white/red energy impact burst.
  - Enemy health decreases on hit; when HP $\le 0$, `isAlive() == false` marks the enemy defeated.
  - Defeated enemies immediately cease movement and patrol (`moving = false`), stop chasing Arin, and cannot be hit again.
  - Defeated enemies remain preserved in the domain collection at their defeat position for future loot drops and boss activation checks.
- **Boss Awakening Flow:**
  - As normal Goblins are struck and defeated, `Game.checkBossActivation()` tracks remaining living normal Goblins.
  - Defeating ShadowWolves does not awaken the boss.
  - Upon the defeat of the third normal Goblin, `GiantGoblin.checkActivationConditions()` succeeds and `boss.setActive(true)` triggers, awakening the Level 1 Boss.
- **Visual Feedback (`WorldRenderer.java`):**
  - **Ground Sword:** Rendered with steel blade, brass crossguard, leather grip, and a pulsing magical cyan aura (`#64e5ff`).
  - **Equipped Sword:** Rendered in Arin's hand in idle, walking, and attack poses, automatically mirroring orientation when facing left.
  - **Attack Swing & Slash Arc:** When attacking, Arin's sword swings dynamically from $-50^\circ$ to $+80^\circ$, accompanied by a luminous cyan-and-white cutting arc in front of him.
  - **Hit Flash:** Enemies hit flash with bright white and red visual impact.
  - **Defeated Remnant:** Fallen enemies render as a soft grounded defeat mark at their death coordinates.

**Files created:**
- `src/main/java/com/shadowquest/items/SwordItem.java`
- `src/main/java/com/shadowquest/combat/AttackHitbox.java`
- `src/main/java/com/shadowquest/combat/CombatSystem.java`
- `src/test/java/com/shadowquest/items/SwordItemTest.java` (6 unit tests)
- `src/test/java/com/shadowquest/combat/CombatSystemTest.java` (12 unit tests)

**Files modified:**
- `src/main/java/com/shadowquest/player/Player.java`
- `src/main/java/com/shadowquest/enemy/Enemy.java`
- `src/main/java/com/shadowquest/world/MysticForest.java`
- `src/main/java/com/shadowquest/core/Game.java`
- `src/main/java/com/shadowquest/ui/GameScreen.java`
- `src/main/java/com/shadowquest/ui/ScreenManager.java`
- `src/main/java/com/shadowquest/render/WorldRenderer.java`
- `docs/PROJECT_PLAN.md`
- `docs/IMPLEMENTATION_LOG.md`
- `docs/PROJECT_DEVELOPMENT_REPORT.md`

**Test results:** 314/314 JUnit tests passing (296 baseline + 18 new tests, 0 failures, 0 errors, 0 skipped). BUILD SUCCESS.


## November 15, 2026: Phase 4.1.1 Enemy Spawn Revision & Level 1 Boss (Giant Goblin)

Revised Mystic Forest enemy encounter structure and introduced the Level 1 Boss: **Giant Goblin**. Corrected enemy placement along the active trail and implemented boss activation mechanics.

**Three-Level Game Scope & Boss Structure (Completion Target: November 17, 2026):**
1. **Level 1: Mystic Forest** — Boss: **Giant Goblin** (Implemented; awakens after all 3 normal Goblins are defeated)
2. **Level 2: Ancient Ruins** — Boss: **Ancient Guardian** (Planned)
3. **Level 3: Shadow Castle** — Boss: **Shadow Lord** (Final Boss, Planned)

**Root cause analysis of previous encounter observation:**
- In the initial Phase 4.1 layout, 3 of the 6 enemies were spawned at `(1280, 460)`, `(1650, 620)`, and `(2050, 780)` across a 2880×1800 world.
- Because typical manual gameplay testing occurs within the opening 800–1000px around the trailhead and initial path, those 3 distant enemies were never reached.
- Furthermore, dense procedural tree and rock grids beyond x=740 visually occluded distant enemies behind tree canopies via Y-depth sorting.

**Revised encounter structure & spawn placement:**
All enemies now spawn at verified, accessible clearings along the active forest trail:
- **Exactly 3 Normal Goblins:**
  - Goblin 1: `(360, 120)` — Trailhead clearing
  - Goblin 2: `(780, 260)` — Mid-trail clearing
  - Goblin 3: `(1200, 430)` — Pre-boss trail approach
- **2–3 ShadowWolves (3 total):**
  - ShadowWolf 1: `(580, 190)` — North-east clearing
  - ShadowWolf 2: `(1020, 360)` — East trail clearing
  - ShadowWolf 3: `(680, 380)` — South-east clearing
- **1 Level 1 Boss (Giant Goblin):**
  - Giant Goblin: `(1400, 520)` — Boss arena at the end of the primary trail

**Giant Goblin Architecture & Stats:**
- Class: `com.shadowquest.enemy.GiantGoblin` extending `Enemy`.
- Stats: Health: 180 (4x normal Goblin), Attack: 18, Defense: 6, Speed: 85 (deliberate heavy boss pacing), Detection Radius: 240, Physical Size: 48 (double normal size), Stop Distance: 45, Reward Coins: 30, Patrol Radius: 40.
- **Activation Lifecycle:**
  - Starts inactive (`active = false`). Inactive state prevents AI updates, movement, chasing, and rendering.
  - `checkActivationConditions(enemies)`: Evaluates living state of all normal Goblins. Returns `true` only when all 3 normal Goblins have been defeated (`!isAlive()`).
  - `Game.update(deltaTime)` triggers `checkBossActivation()`, awakening the boss without JavaFX dependencies.
- **Distinctive Canvas Visuals:**
  - Towering 96×82px frame with dark olive hide (`#2e5833`), spiked iron pauldrons (`#2b3036`), studded leather breastplate with blood sigil (`#8a2323`), spiked belt, war helm, jagged goblin ears, glowing crimson/fiery eyes (`#ff2222`), upward-curving tusks, and a massive iron-banded spiked battle-club.
  - Unified Y-depth sorting via `DrawableEntity` integrates seamlessly with Arin and trees.

**Files created:**
- `src/main/java/com/shadowquest/enemy/GiantGoblin.java`
- `src/test/java/com/shadowquest/enemy/GiantGoblinTest.java` (6 unit tests)

**Files modified:**
- `src/main/java/com/shadowquest/enemy/Enemy.java` (added `active` state and lifecycle control)
- `src/main/java/com/shadowquest/core/Game.java` (added `checkBossActivation()`)
- `src/main/java/com/shadowquest/world/MysticForest.java` (revised spawn coordinates and added GiantGoblin)
- `src/main/java/com/shadowquest/render/WorldRenderer.java` (added `drawGiantGoblin` and `active` entity check)
- `src/test/java/com/shadowquest/enemy/MysticForestEnemyTest.java` (added tests for exact counts, boss activation, spawn validity)
- `docs/PROJECT_PLAN.md`
- `docs/IMPLEMENTATION_LOG.md`
- `docs/PROJECT_DEVELOPMENT_REPORT.md`

**Test results:** 296/296 JUnit tests passing (288 baseline + 8 new tests, 0 failures, 0 errors, 0 skipped). BUILD SUCCESS.


## November 15, 2026: Phase 4.1 Enemy Architecture, AI Behaviour & Mystic Forest Spawning

Implemented the foundational enemy architecture, AI behaviours, spawning, and Canvas rendering for the first two woodland enemy types in Mystic Forest: **Goblin** and **ShadowWolf**.

**Game Scope & Progression:**
- 3 Levels:
  1. Mystic Forest (Current active level)
  2. Ancient Ruins
  3. Shadow Castle
- Final Boss: Shadow Lord

**Implementation details:**
- **Enemy domain hierarchy (`com.shadowquest.enemy`):**
  - `Enemy.java` (abstract): Extends `com.shadowquest.player.Character`. Encapsulates speed, detection radius, body size, stop distance, reward coins, patrol origin and radius, facing direction, movement state, and animation time. Completely free of JavaFX dependencies.
  - `Goblin.java` (concrete): Quick, cunning woodland enemy with stats: Health: 45, Attack: 8, Defense: 2, Speed: 110, Detection Radius: 180, Size: 24, Stop Distance: 28, Reward Coins: 5, Default Patrol Radius: 50.
  - `ShadowWolf.java` (concrete): Swift, predatory beast with stats: Health: 35, Attack: 10, Defense: 1, Speed: 160, Detection Radius: 220, Size: 28, Stop Distance: 32, Reward Coins: 8, Default Patrol Radius: 70.
- **Deterministic AI Behaviour:**
  - **Detection & chasing:** Computes Euclidean distance between enemy center and player center. If distance <= detection radius, switches from patrol to chase mode. Moves towards player using normalized direction vectors and delta time.
  - **Non-overlapping stopping:** When within `stopDistance`, the enemy stops moving forward while keeping sight/facing towards Arin, preventing awkward sprite overlapping.
  - **Patrol mode:** When Arin is outside detection radius, the enemy patrols horizontally across its designated patrol route using a smooth deterministic timing cycle, reversing when reaching boundary limits.
  - **Collision & world containment:** Reuses `CollisionManager.resolve()` for swept-AABB sliding against solid environment objects (trees, rocks, large roots) and clamps within `[0, world.width - size]` and `[0, world.height - size]`.
  - **Frame-rate independence:** All movement calculations use delta time (`dir * speed * deltaTime`).
- **Session & World Integration:**
  - `MysticForest.createEnemies()`: Spawns 3 Goblins and 3 ShadowWolves at carefully measured clearings along the winding trail (`(380, 110)`, `(600, 180)`, `(920, 310)`, `(1280, 460)`, `(1650, 620)`, `(2050, 780)`), verified to have zero overlap with any solid tree/rock/root hitboxes or Arin's initial spawn point (`(60, 110)`).
  - `Game.java`: Added `enemies` collection, overloaded constructors `Game(world, enemies)` and default `Game()`, `getEnemies()` returning an unmodifiable list. In `update(deltaTime)`, active living enemies update their AI when `state == GameState.PLAYING`.
- **Canvas Rendering & Unified Y-Depth Sorting:**
  - **Goblin art:** Hunched silhouette, moss green skin (`#427848`), ragged leather tunic (`#4e3825`), belt with brass buckle, pointed goblin ears with inner shading, glowing amber eyes (`#ffe066`), sharp lower fangs, spiked wooden club, contact shadow, walking bob.
  - **ShadowWolf art:** Sleek predatory quadruped, dark charcoal/shadow indigo fur (`#181a24`), spectral aura wisps, angular muzzle, fangs, pointed ears, glowing spectral turquoise eye (`#38efdf`), running stride paw swing, bushy tail, contact shadow.
  - **Directional facing:** Automatically mirrors sprite horizontally via Canvas scale transform (`-1, 1`) when facing left.
  - **Unified depth sorting:** Collects player and visible living enemies into a `DrawableEntity` list sorted by ground base Y (`y + size`). Interleaves entity rendering with solid scenery objects (`hitbox.bottom()`), ensuring enemies and player correctly appear in front of or behind obstacles and each other.
- **GameScreen & ScreenManager Integration:**
  - `GameScreen`: Updated constructors and `render()` to pass active enemies to `WorldRenderer`.
  - `ScreenManager`: Passes `game.getEnemies()` to `GameScreen`.

**Files created:**
- `src/main/java/com/shadowquest/enemy/Enemy.java`
- `src/main/java/com/shadowquest/enemy/Goblin.java`
- `src/main/java/com/shadowquest/enemy/ShadowWolf.java`
- `src/test/java/com/shadowquest/enemy/EnemyTest.java` (14 unit tests)
- `src/test/java/com/shadowquest/enemy/GoblinTest.java` (3 unit tests)
- `src/test/java/com/shadowquest/enemy/ShadowWolfTest.java` (3 unit tests)
- `src/test/java/com/shadowquest/enemy/MysticForestEnemyTest.java` (5 integration tests)

**Files modified:**
- `src/main/java/com/shadowquest/core/Game.java`
- `src/main/java/com/shadowquest/world/MysticForest.java`
- `src/main/java/com/shadowquest/render/WorldRenderer.java`
- `src/main/java/com/shadowquest/ui/GameScreen.java`
- `src/main/java/com/shadowquest/ui/ScreenManager.java`
- `docs/IMPLEMENTATION_LOG.md`
- `docs/PROJECT_DEVELOPMENT_REPORT.md`

**Test results:** 288/288 JUnit tests passing (263 existing baseline + 25 new tests, 0 failures, 0 errors, 0 skipped). BUILD SUCCESS.


## November 15, 2026: Phase 3.6 gameplay integration (hand-painted Arin character & animation)

Integrated Arin's hand-painted 2D fantasy character art, 4-frame walking animation, facing direction, and spawn positioning into the active Mystic Forest gameplay screen.

**Implementation details:**
- **Hand-painted Arin character rendering:** Upgraded `WorldRenderer.drawPlayer()` to load and render the hand-painted sprite assets (`arin_stand.png`, `arin_walk1.png`–`arin_walk4.png`) from `/assets/cutscene/characters/`. Replaced the previous 24×24 geometric beige placeholder circle.
- **4-frame walking animation:** Synchronized 4-frame walk cycle (`walk1` → `walk2` → `walk3` → `walk4`) at 7.0 FPS during movement, with subtle 1.5px vertical bobbing. Returns smoothly to idle standing pose (`arin_stand.png`) when stationary.
- **Directional facing & mirroring:** Arin faces right when moving right, mirrors cleanly via canvas horizontal scale (`-1, 1`) when moving left, and preserves his last faced direction when moving purely vertically or idling.
- **Hitbox & depth sorting alignment:** Sprite dimensions scaled to `80 × 40` (increased from `48 × 24` for clear, prominent hero readability, perfectly matching the character's ~0.497 aspect ratio). Feet ground contact line is anchored exactly to the base of the `24 × 24` domain hitbox at `p.getY() + p.getSize()`. Solid obstacle Y-depth sorting compares obstacle base against Arin's feet base.
- **Soft contact shadow:** Rendered centered under Arin's feet (`34 × 14`) at `p.getY() + p.getSize() - 9.0` with translucent `#000e15` color.
- **Boundary headroom & clearing spawn:**
  - Expanded `GameScreen.java` world clip rect by 96px headroom (`world.getWidth() + 192, world.getHeight() + 192`) to prevent character head/foliage clipping when standing near world borders.
  - Softened `WorldRenderer` radial vignette stops so the forest and clearing are clear, bright, and vibrant.
  - Configured `ScreenManager.showGameplay()` to spawn Arin inside the open clearing at the Mystic Forest trailhead (`x = 60, y = 110`) when entering gameplay from the cutscene.
- **Domain state tracking:** Added `facingRight`, `moving`, `animationTime`, and `spawnAt(x, y, world)` to `Player.java`.

**Files modified:**
- `src/main/java/com/shadowquest/render/WorldRenderer.java` — asset pre-loading, sprite rendering, walk animation, facing flip, contact shadow, softened vignette.
- `src/main/java/com/shadowquest/player/Player.java` — `isFacingRight()`, `isMoving()`, `getAnimationTime()`, `spawnAt(x, y, world)`, direction/motion tracking in `move()`.
- `src/main/java/com/shadowquest/ui/ScreenManager.java` — spawn Arin at Mystic Forest trailhead clearing (`60, 110`) upon entering gameplay.
- `src/main/java/com/shadowquest/ui/GameScreen.java` — boundary headroom in world clipping rect.
- `src/test/java/com/shadowquest/player/PlayerTest.java` — 5 new unit tests for facing, movement state, animation time, and `spawnAt`.

**Test results:** 263/263 JUnit tests passing (258 existing baseline + 5 new tests, 0 failures, 0 errors, 0 skipped). BUILD SUCCESS.

## November 15, 2026: Phase 3.5 walking animation fix

Fixed Arin's walking animation which previously caused the character to turn back and forth horizontally.

**Root cause:** An earlier script attempted to create the opposite stride (Frame 3) by horizontally mirroring the full `arin_walk1` image. Because the entire sprite was mirrored, Arin's head, face, chest, and eyes were flipping from facing right to facing left on Frame 3, causing the character to spin/turn around repeatedly while walking forward.

**Fix applied:**
- **Zero upper-body flipping:** The head, eyes, face, shoulders, arms, and tunic are identically anchored and face forward/right across ALL frames (100% stable `headCenter=283`, identical upper body).
- **Leg-only stride alternation:**
  - **Frame 1 (Stride 1):** Left leg back (pushing off), Right leg forward (stepping). Both boots point forward (right).
  - **Frame 2 (Passing 1):** Both legs passing directly under the pelvis in the center (from standing pose).
  - **Frame 3 (Stride 2):** Pure leg affine shear anchored at hip joint — Right leg swings back, Left leg swings forward. Neither leg or foot is horizontally flipped; both boots continue pointing forward (right).
  - **Frame 4 (Passing 2):** Both legs passing directly under the pelvis in the center.
- **Unified 548×1103 canvas:** All 4 walk frames and contextual poses (`stand`, `look`, `react`, `cautious`, `kneel`) share identical canvas dimensions, baseline, and ground contact Y (1094–1095).
- **Smooth sinusoidal bobbing:** Synchronized vertical bobbing in `CutsceneScreen.java` (`3.0px` peak on passing frames, `0.0px` on full strides).

**Files modified:**
- `src/main/java/com/shadowquest/ui/CutsceneScreen.java` — 4-frame walk cycle, `WALK_FPS`, `walkFrame()` helper.

**Assets updated:**
- `src/main/resources/assets/cutscene/characters/arin_walk1.png` — Stride 1 (facing right)
- `src/main/resources/assets/cutscene/characters/arin_walk2.png` — Passing 1 (facing right)
- `src/main/resources/assets/cutscene/characters/arin_walk3.png` — Stride 2 (facing right, opposite leg stride without flipping)
- `src/main/resources/assets/cutscene/characters/arin_walk4.png` — Passing 2 (facing right)

**Test results:** 258/258 JUnit tests passing, BUILD SUCCESS.

**Test results:** 258/258 JUnit tests passing, BUILD SUCCESS.

## November 15, 2026: Phase 3.5 visual quality revision (hand-painted 2D fantasy animation)

Upgraded `ui/CutsceneScreen.java` from procedural placeholder Canvas geometry to a hand-painted 2D fantasy animated storyboard. Added 16 illustrated assets under `src/main/resources/assets/cutscene/`:
- `backgrounds/`: `peaceful_forest.png`, `peaceful_foreground.png`, `eldoria_forest.png`, `eldoria_foreground.png`, `montage_day1.png`, `montage_day2.png`, `montage_day3.png`.
- `characters/`: `arin_stand.png`, `arin_walk1.png`, `arin_walk2.png`, `arin_look.png`, `arin_react.png`, `arin_cautious.png`, `arin_kneel.png`.
- `environment/`: `portal_tree.png`.
- `effects/`: `portal_energy.png`.

Renderer implementation details:
- Arin character acting: 2-phase walking cycle with sinusoidal leg swing and vertical bobbing, stopping/curious pose (`arin_look`), alert surprise (`arin_react`), cautious approach (`arin_cautious`), stumble/recovery on arrival (`arin_kneel`), and determined stance.
- Portal: Giant ancient tree trunk with irregular vertical tear, splintered bark lips, volumetric pulsing turquoise/cyan dimensional energy, radiating light rays, rising motes, and floor glow. No floating rings.
- Seven scenes: Peaceful morning forest with god rays and birds (Scene 1), giant tree & tear awakening (Scene 2), entering the portal with screen light flood (Scene 3), Eldoria arrival with mist and fireflies (Scene 4), 3-day exploration montage with distinct locations/atmosphere and cuts to black (Scene 5), dormant dead portal with determination acting (Scene 6), and smooth zoom bridge to Mystic Forest gameplay (Scene 7).
- Storybook letterbox overlay with scene titles, 2-line narration, montage day banners, and skip indicator.
- Single shared GameLoop, 7-scene sequence, durations, narration, skip/replay, and gameplay transitions preserved.
- Full suite: 258 passed, 0 failures, 0 errors, 0 skipped, BUILD SUCCESS. Real JavaFX launch verified. No Git staging, commit, or push.

Added cutscene/OpeningScene.java, cutscene/OpeningCutscene.java and
ui/CutsceneScreen.java; added test cutscene/OpeningCutsceneTest.java,
cutscene/OpeningSceneTest.java and core/GameCutsceneTest.java. Modified
core/GameState.java (CUTSCENE state), core/Game.java
(startOpeningCinematic/completeCutscene/cancelCutscene plus shared private
spawnPlayer) and ui/ScreenManager.java (title → showCutscene → showGameplay
flow). Player, Camera, InputHandler, GameLoop, world and render packages are
untouched; the single shared GameLoop drives the cinematic.

Flow: Enter on the title screen creates a fresh Game in CUTSCENE and a fresh
OpeningCutscene at Scene 1 (so every New Game replays the story). Esc, Space or
Enter skips; natural completion and skip share one finishCutscene() path that
calls game.completeCutscene() exactly once and then starts gameplay with the
same session. Closing the window or navigating away runs leaveGame(), which
clears the pending session so an abandoned cinematic never starts gameplay.
pendingGame is assigned after setScene because that call re-enters leaveGame()
through the scene-property listener.

First suite run: 258 executions, 2 failures — both were errors in the new
tests' own arithmetic (scene-boundary math and the 0.7 s fade window), not in
production code; assertions were corrected. Final: 258 passed, 0 failed/errors/
skipped, BUILD SUCCESS; the 224-test baseline passed unchanged. New tests are
JavaFX-free. Real `mvnw javafx:run` launch succeeded with the game window
visible and no exception in the log; no keystrokes were sent because the user
was actively using the desktop, so the visual walkthrough (Enter → seven
scenes → skip → Mystic Forest → replay from Scene 1) remains user-pending.
All cinematic artwork is clearly documented temporary Canvas placeholder art.
Updated README, architecture, roadmap, development report and this log. No
staging, commit, push, or Git history change was performed. Phase 4 not started.
## November 15, 2026: Phase 3.4 forest and collision

Added world/EnvironmentObject.java, world/MysticForest.java,
world/CollisionManager.java and render/WorldRenderer.java under the production
com/shadowquest packages, plus test world/ForestCollisionTest.java. Modified
GameWorld, Game, Player and GameScreen; preserved Camera/input/timing/speed.
Boundary test fixtures now use explicit empty worlds without changing assertions.
Environment data uses world coordinates and immutable collections. Only narrow
trunks, rocks and large roots block. Continuous swept AABB selects first contact,
snaps to exact faces, and slides without boosting the unblocked component.
Rendering uses temporary dark teal Canvas shapes, stone polygons, twisted trunks,
moss, vegetation and static turquoise gradients/fireflies inspired by the uploaded
forest image. No external artwork/dependencies were introduced.

Actual Maven JavaFX launch and initial forest screenshot were inspected. The user
reported excessive density and lag mainly at roots, including thick-middle passage.
Manual acceptance failed. Enlarged the root central hitbox, distinguished small
roots, snapped contact positions, reduced decorations from 1100 to 420 and tree
slots from 60 to 40, and corrected solid depth ordering. Computer Use was stopped
with Escape. The prior launch ended BUILD SUCCESS at 01:42:34 IST, but the revised
root/density/depth changes have not been manually accepted or visually rechecked.
No FPS measurement was made. These remain explicit pending verification items.

Full clean compilation/test command uses documented JDK21 and local Maven cache:
mvnw.cmd -o -B -ntp -Dmaven.repo.local=.tools/repository clean compile test.
Final suite: 224 passed, 0 failed/errors/skips, 19 production and 11 test files.
ForestCollisionTest adds 30 executions, including 8 fractional contact regressions.
Earlier sliding-fixture failures were investigated and corrected; no tests were
removed or skipped. Logs are ignored under .tools/phase3.4/.

Updated README, architecture, roadmap and the full development report with exact
members, hitbox formulas, collision mathematics, rendering layers, concepts and
manual limitations. Earlier uncommitted work is preserved. No Phase 4, enemies,
combat, inventory, quests, new levels, staging, commit or push was performed.
## November 15, 2026: Phase 3.3 verification and closure

Inspected the existing November 14 implementation without reimplementing it.
Player.getSize and constrainToWorld clamp the top-left of the 24-unit body to
[0, worldWidth-24] and [0, worldHeight-24]. Game applies this after movement and
at session start. Character.setPosition preserves private, finite coordinates.
GameScreen shares the size and reserves a dark controls strip below the Canvas.
Camera, movement speed, normalization, input controls and delta timing are preserved.

Phase files: modified player/Player.java, player/Character.java, core/Game.java,
ui/GameScreen.java, test core/GameMovementTest.java and world/GameWorldTest.java;
added test core/PlayerWorldBoundaryTest.java with 36 executions. Source/test paths
are under src/main/java/com/shadowquest and src/test/java/com/shadowquest respectively.
The obsolete unrestricted movement assertion was updated for the authorized rule;
the pause/resume case uses an unblocked direction. No test was removed or skipped.

Closure reran the full Maven clean compile test command with the existing JDK21
and offline local cache. BUILD SUCCESS, exit 0, at 01:25:34 IST: 194 passed,
0 failures, 0 errors, 0 skipped. Log: .tools/phase3.3/closure-clean-test.log.
Earlier actual GUI inspection and the user's explicit yes confirmed all edges,
diagonal corners, whole-circle visibility, departure, camera following, release,
and WASD/arrows. The background-only correction was visually verified afterward.
The final launch log ends BUILD SUCCESS at November 14 14:54:44 IST. Final close
gesture and repeated held-key testing after the background correction were not
observed. No new GUI launch occurred at closure. Extreme viewport sizes and
small-world visual checks remain unverified; the report records those limits.

Closure updated only README and the four project documents, including the full
file/member explanations and viva examples in PROJECT_DEVELOPMENT_REPORT.md.
Existing uncommitted Phases 3.1–3.2 work is preserved. Phase 3.4 was not started.
No staging, commit, push, or history change was performed.
## November 14, 2026: Phases 3.1 and 3.2 world and camera

Added immutable world.GameWorld and JavaFX-free world.Camera. GameConfig now
defines independent 2880 x 1800 world dimensions, while the initial viewport
remains 960 x 600. Game owns the world and exposes getWorld. GameScreen receives
that world, follows the player with a clamped camera, and draws a simple grid,
border and placeholder through world-to-screen translation. ScreenManager passes
the world to the screen. No Player/input/timing rules changed. The player remains
at (0,0) initially and can still move outside world bounds; Phase 3.3 is deferred.

Camera offsets are the viewport's upper-left world position. Each axis clamps
target minus half the viewport to [0, max(0, worldSize - viewportSize)]. Small
worlds stay at offset zero with blank unused viewport space. Drawing is clipped
to the world; this is not collision or player-boundary enforcement. The temporary
player circle's upper-left is anchored to its transformed world position, and
the controls moved to the bottom-left to keep the starting corner visible.

Added GameWorldTest (8 executions) and CameraTest (30). Clean compilation passed
for 15 production files and 9 test files. All 158 tests passed: original 120 plus
38 new, with zero failures/errors/skips. The tests cover dimensions, restart,
all edges/corners, out-of-world targets, small/exact worlds, resize, conversion,
validation, and unchanged normalized/unrestricted player movement.

Commands used the documented JDK 21/Maven setup with cached dependencies:
`mvnw.cmd -o -B -ntp -Dmaven.repo.local=.tools/repository clean compile test`
and `mvnw.cmd -o -B -ntp -Dmaven.repo.local=.tools/repository javafx:run`.
Logs are under ignored `.tools/phase3.1-3.2/`. The initial world/grid/player view
was directly inspected. The user confirmed held movement, camera following,
key release, and stopping the camera at the world edge. Further Computer Use
was interrupted with Escape. Return/re-entry and visual small-world/resize/all-edge
checks were not completed. Math for those cases is covered by JUnit, not claimed
as manually observed. Maven later reported BUILD SUCCESS and exit 0 at
14:40:01 IST; the close gesture itself was not observed through automation.

Updated architecture, plan progress, README and the development report with
class-by-class explanations, formulas, tests, and limitations. The approved game
design is unchanged. No environment objects/assets, collisions, enemies, combat,
or Phase 3.3/3.4 work was added. No commit, push, or Git history change was made.

## November 14, 2026: Phase 2.4 input and movement

The current implementation connects InputHandler -> GameLoop -> Game.movePlayer
-> Player.move -> GameScreen Canvas. Enter leaves the unchanged static title for
a fresh movement demo. WASD/arrows support eight directions with normalized
diagonal speed; release stops the relevant axis, opposing keys cancel, and aliases
do not multiply speed. Space/E/Escape are tracked only. Back to title is available
as a button. Scene replacement/window hiding detach input and stop the loop;
losing focus clears keys. No timer runs on the title screen.

Added InputHandler.java and GameScreen.java. Modified Player.java with a digital
axis overload shared by the original Direction API, Game.java with PLAYING-gated
movePlayer, and ScreenManager.java with scene/input/session lifecycle wiring.
Character, Direction, MainMenuScreen, GameLoop, FrameClock, and all existing tests
were preserved. The initial world origin remains (0,0); rendering adds a fixed
viewport-center offset. This is not a camera. No collision bounds are present.

Added PlayerMovementTest (24 executions), InputHandlerTest (24), and
GameMovementTest (3). Clean compilation and the full suite passed: 120 tests,
0 failures/errors/skips, including the original 69. Command, after the documented
JDK 21 setup: `mvnw.cmd -o -B -ntp -Dmaven.repo.local=.tools/repository clean compile test`.
Thirteen production and seven test files compiled. Logs remain under ignored
`.tools/phase2.4/`.

Actual Maven JavaFX launch and visual title/game-screen entry were verified.
The user confirmed held D, W+D, key release, and arrow movement worked in response
to the manual check. Desktop automation was subsequently stopped with Escape;
return navigation/re-entry and focus-loss clearing were not manually verified in this run. A later process check confirmed BUILD SUCCESS and JAVAFX_EXIT=0 at 03:56:44 IST. The final close gesture was not observed through automation. No runtime exception appeared in the captured log.

PROJECT_DEVELOPMENT_REPORT.md contains the current file-wise learning guide.
PROJECT_PLAN.md and ARCHITECTURE.md now preserve the user's expanded story,
systems blueprint, Phase 0–12 roadmap and the current API/package decisions.
The user currently owns all coding, architecture, and integration; earlier member
assignments below are historical. README includes controls and limitations.
Earlier phase history and all uncommitted work were preserved. No commit, push,
merge, remote change, or Phase 3 implementation was performed.

The remaining entries below describe their dates, not current GUI/input status.

## November 13, 2026: continuation verification

Reviewed the requested core-foundation scope and found no unfinished implementation
tasks. Re-ran the existing Maven Wrapper with the portable JDK 21 and cached
dependencies using `-o -B -ntp -Dmaven.repo.local=.tools/repository clean compile test`.
Clean compilation passed and all 63 tests passed again: GameTest 14,
CharacterTest 29, PlayerTest 20, with zero failures, errors, or skips.
Only this log was updated during the continuation; no next-phase code was added.
JavaFX launch was not retried. The previously observed Windows Application
Control blocker remains unresolved, so GUI launch is still unverified.

## November 11, 2026: Phase 1 core game foundation

Completed the core-foundation scope explicitly requested for this task:
game states, directions, a session controller, shared character data and health,
player movement, and domain tests. This is a subset of the broader Phase 1 in
PROJECT_PLAN.md. Enemies, inventory, combat, levels, input, and rendering are
still deferred.

The project plan, architecture, existing JavaFX startup, screen classes,
GameConfig, and CSS were preserved. Existing constructors and methods were not
changed. No new domain API is wired into the title screen yet.

## File inventory

All paths below are relative to the repository root, currently `C:\java game`.

| Created file | Responsibility and important members |
| --- | --- |
| `src/main/java/com/shadowquest/core/GameState.java` | Enum containing MENU, PLAYING, PAUSED, GAME_OVER, and VICTORY; used by Game |
| `src/main/java/com/shadowquest/utils/Direction.java` | Shared UP, DOWN, LEFT, RIGHT enum; used by Player.move |
| `src/main/java/com/shadowquest/core/Game.java` | Owns private state and player references; starts sessions, pauses/resumes, and detects defeat on update |
| `src/main/java/com/shadowquest/player/Character.java` | Abstract base class with private name, health, maxHealth, attackPower, defense, x, and y; owns validation, health changes, and protected movement |
| `src/main/java/com/shadowquest/player/Player.java` | Extends Character; supplies initial stats and speed; translates Direction plus elapsed seconds into movement |
| `src/test/java/com/shadowquest/core/GameTest.java` | Tests session transitions, invalid commands/time, pause, defeat, restart, and independent sessions |
| `src/test/java/com/shadowquest/player/CharacterTest.java` | Tests shared constructor validation, health bounds, death, healing, and coordinate safety through a small concrete test subclass |
| `src/test/java/com/shadowquest/player/PlayerTest.java` | Tests player defaults, inherited validation, directions, time-based movement, invalid input, overflow, and death |
| `docs/IMPLEMENTATION_LOG.md` | This record of implementation, API contracts, decisions, results, and limitations |

| Modified file | Change |
| --- | --- |
| `pom.xml` | Added JUnit Jupiter 5.13.4 with test scope; existing Java 21, JavaFX 21, and Maven plugin settings retained |
| `README.md` | Updated current scope, tree, test instructions, and implementation-log link |
| `CONTRIBUTING.md` | Updated current scope and testing guidance; preserved the fork/branch/PR workflow |

No files were deleted. Existing `.gitkeep` markers remain. Maven regenerates
ignored `target/` output and caches dependencies under ignored `.tools/` during
local verification. These are not source changes.

## Current API contracts

These are the implemented Phase 1 subset and the position operations requested
for this task. The architecture's existing proposed signatures are retained
where implemented. No change to the architecture document was needed.

### Game

```java
public Game(); // Java supplies the default no-argument constructor.
public GameState getState();
public Player getPlayer();
public void startNewGame();
public void pause();
public void resume();
public void update(double deltaTime);
```

| Operation | Allowed starting state | Result |
| --- | --- | --- |
| Construct Game | No session | MENU, player is null |
| startNewGame | MENU or GAME_OVER | New Player named Arin, then PLAYING |
| pause | PLAYING | PAUSED, same player |
| resume | PAUSED | PLAYING, same player |
| update with a dead player | PLAYING | GAME_OVER |
| update with a living player | PLAYING | No state change |
| update | MENU, PAUSED, GAME_OVER, VICTORY | No domain update |

Start, pause, and resume calls outside their allowed states have no effect.
This preserves the proposed void signatures and treats inapplicable UI commands
as normal outcomes. For example, repeated Play cannot erase an active session.
There is no public setState method that could bypass these rules.

`update` always validates finite, non-negative elapsed seconds, even outside
PLAYING. Zero seconds is valid and can be used to check for defeat. It does not
run a timer, move the player, or advance levels. Damage does not directly notify
Game; defeat is detected on the next playing update. A paused session remains
paused until resume is called, then the next update detects any externally
changed health.

`getPlayer()` returns null before startNewGame. Afterwards it exposes the active
mutable Player through validated domain methods. Starting after defeat creates
a fresh instance; callers must obtain the current reference again after restart.

VICTORY is defined for compatibility with the agreed state vocabulary. There is
no victory trigger, arbitrary state setter, or return-to-menu operation in this
slice. Those require later progression and navigation work. The proposed
`getCurrentLevel()` is deferred because no Level implementation exists yet.

### Character

```java
protected Character(String name, double maxHealth, int attackPower, int defense);
public String getName();
public double getHealth();
public double getMaxHealth();
public int getAttackPower();
public int getDefense();
public double getX();
public double getY();
public boolean isAlive();
public void takeDamage(double amount);
public void heal(double amount);
protected final void moveBy(double deltaX, double deltaY);
```

- All fields are private. Name, maximum health, attack power, and defense are
  final, so these values cannot change after construction.
- The constructor rejects null/blank names, non-positive or non-finite maximum
  health, and negative attack or defense. It strips surrounding name whitespace,
  starts at full health, and starts at coordinates `(0, 0)`.
- `takeDamage` applies the given amount directly and clamps health at zero.
  Defense is stored but does not reduce damage yet. No attack system exists.
- `heal` limits restoration to the remaining health capacity before adding it.
  This prevents both exceeding maximum health and floating-point overflow.
- Damage and healing accept zero, reject negative/non-finite values, and do not
  mutate health on invalid input. `isAlive` means health is greater than zero.
- Healing from zero is permitted by this health primitive. It changes health
  only, and cannot change GAME_OVER back to PLAYING. Revive/item rules are not
  implemented.
- `moveBy` lets subclasses request displacement without exposing public setters.
  Both resulting coordinates must be finite before either field is assigned.
  It is final so subclasses cannot override this validation step.
- `Character` is abstract and cannot be instantiated directly. It currently
  contains implemented shared behavior, with no abstract methods. Java permits
  this. The proposed `attack` and per-character `update` methods are deferred
  rather than introducing empty implementations for excluded features.

### Player and Direction

```java
public Player(String name);
public double getSpeed();
public void move(Direction direction, double deltaTime);
```

Player inherits all public Character methods. Its initial tuning values are
maximum health 100, attack power 10, defense 5, speed 200 world units per second,
and position `(0, 0)`. These values are defined once in Player as named constants;
they are starter values, not a combat balance specification.

Movement uses `distance = speed * deltaTime`, with elapsed time in seconds:

| Direction | Position change |
| --- | --- |
| UP | Decrease Y |
| DOWN | Increase Y |
| LEFT | Decrease X |
| RIGHT | Increase X |

This follows the architecture's top-left origin convention. Signed finite
coordinates are permitted; level bounds and collision handling do not exist yet.
For example, moving RIGHT for 0.25 seconds at speed 200 changes X by 50.

Null direction, negative/non-finite time, multiplication overflow, and coordinate
overflow are rejected with IllegalArgumentException. Zero time produces no
displacement. Dead players do not move, but invalid arguments are still rejected.

Inventory, collection, item use, weapons, and combat methods remain deferred.
Player has no Game or JavaFX dependency. Future input/controller integration
must call move only while PLAYING; calling Player directly bypasses session
pause policy by design, not its input validation.

## Java concepts demonstrated

- Classes and objects: each Game and Player object holds its own state.
- Encapsulation: private fields are accessed through getters and validated
  operations rather than public mutable fields.
- Inheritance: Player extends Character and reuses its health and position logic.
- Abstraction: Character provides a shared model and cannot be constructed alone.
- Enums: GameState and Direction restrict values to named, valid alternatives.
- Constructors and super: Player delegates common initialization to Character.
- Static/final: class constants hold initial tuning values; final instance fields
  hold values that remain fixed after construction.
- Exceptions: invalid arguments produce IllegalArgumentException. Ordinary death
  and inapplicable commands do not throw. Tests use assertThrows to verify errors;
  no recoverable I/O or try/catch workflow is introduced in this phase.
- Packages separate session coordination, character rules, and shared directions.
- JUnit annotations identify normal and parameterized tests. Parameterized tests
  run the same check with several inputs. Test lambdas pass an operation to
  assertThrows without executing it beforehand.

Runtime polymorphism through overridden combat behavior, collections, interfaces,
file persistence, and event handling are not implemented in this phase. Tests use
Character references to a concrete test subclass, but do not claim this as an
implementation of the planned enemy polymorphism.

## Execution example

The existing application still follows Main -> ScreenManager -> MainMenuScreen.
No keyboard or screen action starts the new domain model yet. Tests or future
integration code can use the model as follows:

```java
Game game = new Game();                 // MENU, no player yet
game.startNewGame();                    // PLAYING, new Arin at (0, 0)
Player arin = game.getPlayer();
arin.move(Direction.RIGHT, 0.25);        // X = 50, Y = 0
arin.takeDamage(30);                    // Health = 70
arin.heal(10);                          // Health = 80
game.pause();                          // PAUSED
game.update(0.016);                     // No game rules run while paused
game.resume();                         // PLAYING
arin.takeDamage(1000);                  // Health = 0, isAlive() is false
game.update(0.016);                     // GAME_OVER
game.startNewGame();                    // New full-health Arin at (0, 0)
```

## Verification

Used the portable Microsoft OpenJDK 21.0.13.0.2+1 and Maven Wrapper 3.9.11 already
present in the workspace. The initial sandboxed build could not resolve Maven
Central DNS. Re-running with approved network access downloaded JUnit and passed.

```powershell
$env:JAVA_HOME = (Resolve-Path '.tools/jdk21/jdk-21.0.13.0.2+1').Path
$env:Path = "$env:JAVA_HOME\bin;$env:Path"
$env:MAVEN_USER_HOME = (Resolve-Path '.tools').Path + '\maven-user-home'
.\mvnw.cmd -B -ntp '-Dmaven.repo.local=.tools/repository' clean compile
.\mvnw.cmd -B -ntp '-Dmaven.repo.local=.tools/repository' test
```

| Check | Result |
| --- | --- |
| Clean compilation | BUILD SUCCESS; nine production source files compiled for Java 21 |
| GameTest | 14 passed |
| CharacterTest | 29 passed |
| PlayerTest | 20 passed |
| Total | 63 tests; 0 failures, 0 errors, 0 skipped |
| Domain dependency inspection | No JavaFX, Swing, or AWT imports in core, player, or utils |
| JavaFX launch | Not reattempted in this phase; prior Windows Application Control blocker remains unresolved |

The launch blocker recorded in INITIALIZATION.md prevents loading JavaFX's
`prism_d3d.dll` and `prism_sw.dll`. Successful compilation and domain tests do
not prove the GUI launched. No security settings were changed. Visual layout,
native-library loading, and UI integration remain unverified.

## Team integration and next step

Member 3 owns Character, Player, and their tests under the project plan.
Member 1 coordinates Game, GameState, and integration tests as the project lead;
the architecture leaves detailed core ownership for team agreement. Direction is
a shared contract to coordinate across Members 1, 3, and 5.

Member 2 will read positions and state for rendering. Member 4 can later use the
health methods for items after agreeing on item and healing rules. Member 5 will
use positions and session state for progression and persistence. None should
duplicate health fields, add JavaFX imports to domain code, or write arbitrary
state/position setters.

Coordinate changes to method signatures, nullability, units, direction signs,
state-transition rules, health behavior, and default stats with the affected
owners. Existing architecture methods for excluded modules remain proposals
for later work, not callable stubs.

---

## Phase 4.3 — Enemy Combat, Player Survival, Fullscreen & Game Over (November 15, 2026)

### Scope Implemented
1. **Enemy Melee Attacks & AI Decision Making:**
   - Implemented attack state machine in `Enemy.java`: range detection, approach, telegraph windup, hit frame damage delivery, and cooldown timers.
   - Concrete enemy attack parameter tuning:
     - `Goblin`: Range 34, Windup 0.25s, Duration 0.40s, Cooldown 1.20s, Power 8 (6 net damage against Arin).
     - `ShadowWolf`: Range 38, Windup 0.18s, Duration 0.30s, Cooldown 0.90s, Power 10 (8 net damage against Arin).
     - `GiantGoblin`: Range 55, Windup 0.45s, Duration 0.70s, Cooldown 1.80s, Power 18 (16 net damage against Arin).
   - Inactive and dead enemies are strictly prevented from attacking.
   - Added visual attack animations in `WorldRenderer`:
     - Goblin: club raised back in windup, forward strike with yellow crescent slash.
     - ShadowWolf: predator red eye flare, crouching windup, forward lunging bite with red fang trail.
     - Giant Goblin: heavy spiked club raised high overhead, ground impact smash with amber shockwave ring.
2. **Player Combat Survival & Invulnerability:**
   - Added `takeCombatDamage(amount)` to `Player.java` with 0.90s invulnerability period (`INVULNERABILITY_DURATION`) and 0.20s damage flash (`DAMAGE_FLASH_DURATION`).
   - Health clamped to 0 without underflow.
   - Repeated hits during invulnerability are completely ignored.
   - Added Arin damage flash overlay and translucent i-frame flicker in `WorldRenderer`.
3. **Player Health Bar (Minimal HUD):**
   - Implemented `drawHealthBar` in `WorldRenderer` rendered at screen coordinates (24, 20).
   - Displays ornate fantasy frame, gold border, dynamic color-shifted health fill (green -> amber -> red), and text `HP: [current] / 100`.
   - Borders flash red during player damage flash.
4. **Game Over & Level Restart:**
   - When player health reaches 0, `Game.update()` transitions state to `GameState.GAME_OVER`.
   - Halts player movement, attack actions, and enemy AI updates.
   - Added `gameOverOverlay` to `GameScreen` with "GAME OVER", "Restart Level (R)", and "Main Menu (Esc)".
   - Implemented `Game.restartLevel()` restoring Arin at (60, 110) with 100 HP, repopulating all 3 normal Goblins and 3 ShadowWolves, resetting Giant Goblin boss to inactive, un-equipping sword and re-placing it on the ground at (180, 140), and resetting combat timers.
5. **Proper Borderless Fullscreen & F11 Toggle:**
   - Game launches in borderless fullscreen by default (`stage.setFullScreen(true)`).
   - JavaFX Escape key exit disabled via `setFullScreenExitKeyCombination(KeyCombination.NO_MATCH)` and `setFullScreenExitHint("")`.
   - Added F11 global key event filter to toggle fullscreen on/off.
   - Exiting fullscreen restores a centered 960x600 window.
   - Canvas, camera, and UI adapt smoothly to screen resolution without aspect ratio distortion or sprite stretching.

### Test Verification
- All 314 existing unit tests continue to pass.
- Added 25 new tests across 3 new test suites:
  - `EnemyAttackTest`: 11 tests covering attack stats, range, windup, hit frame timing, cooldowns, dead/inactive checks, and parameter validation.
  - `PlayerCombatSurvivalTest`: 8 tests covering combat damage, invulnerability timers, duplicate-hit immunity, i-frame decay, health clamping, and dead player guards.
  - `GameOverAndRestartTest`: 6 tests covering death transition, update stoppage, and level restart state restoration.
- **Total Test Suite:** 339 tests run, 339 passed, 0 failures, 0 errors, 0 skipped.

---

## Phase 5 — Two-Level Progression: Mystic Forest to Shadow Castle & Victory (November 15, 2026)

### Scope Implemented
1. **Scope Finalization (Two-Level Architecture):**
   - Removed Ancient Ruins entirely from game design and code.
   - Final structure: Level 1: Mystic Forest (Giant Goblin boss) -> Level 2: Shadow Castle (Shadow Lord final boss).
2. **Magical Sword Upgrade (+5 ATK):**
   - Defeating Giant Goblin permanently upgrades equipped sword (`+5 ATK`, base 10 -> 15).
   - "SWORD UPGRADED — ATTACK POWER +5" notification banner rendered in HUD.
   - Luminous gold/cyan trail on equipped sword swings.
3. **Ancient Realm Portal & Level Transition:**
   - Ancient Portal awakened at (1440, 480) upon Giant Goblin defeat.
   - Pressing 'E' within 45 units triggers an atmospheric 2-second black screen transition.
   - Loads Level 2: Shadow Castle, preserving player health and upgraded sword power.
4. **Shadow Castle Environment & Shadow Lord Final Boss:**
   - Gothic castle layout (2400x1400) with ruined stone pillars, dark flagstone corridors, torches, and ghostly mist.
   - Shadow Lord (350 HP, 24 ATK, 8 DEF): towering demonic sorcerer with dual combat styles (close Void Scythe and mid-range flame attacks).
   - Dedicated Boss Health Bar at screen top.
   - Victory Screen triggered upon Shadow Lord defeat with "VICTORY — ELDORIA RESTORED".
5. **Testing & Verification:**
   - Added `PortalTest`, `ShadowLordTest`, `LevelProgressionAndShadowCastleTest`.
   - Full suite passed: 377 passed, 0 failures, 0 errors.

---

## Phase 5.1 — Castle Enemy Variety, Forest Optimization & Flamethrower Boss Mechanics (November 15, 2026)

### Scope Implemented
1. **Shadow Castle Enemy Replacement (Castle-Themed Denizens):**
   - Completely removed Goblins and ShadowWolves from Level 2 (Shadow Castle).
   - Introduced three castle-specific domain entities extending `Enemy`:
     - `ShadowKnight` (75 HP, 14 ATK, 4 DEF, 95 SPD, 32 size, 42 attack range): Dark steel plate armor with violet rune engravings, helm visor slit, and two-handed broadsword cleave attacks.
     - `DarkArcher` (50 HP, 11 ATK, 2 DEF, 105 SPD, 26 size, 230 attack range): Hooded marksman attacking from tactical distance (110–230 units) using shadow arrows. Actively kites backwards when player closes distance (< 110 units).
     - `Wraith` (60 HP, 13 ATK, 1 DEF, 125 SPD, 28 size, 48 attack range): Floating spectral undead ghost with sinusoidal hover animation, semi-translucent teal/violet shroud, and radial spectral siphon bursts.
2. **Mystic Forest Visual Optimization & Decluttering:**
   - Drastically decluttered `MysticForest`: reduced clutter objects from 420 to 90, mists from 55 to 14, and fireflies from 180 to 36.
   - Spaced out tree generation grid to ensure wide, unobstructed corridors along the stone path and enemy battle clearings.
   - Strictly preserved stone path segments (indices 0–75) and key atmospheric showcase tree at index 76 (`EnvironmentObject(TREE, 190, 80, 180, 230)`).
   - Replaced heavy repeating 120x80 green bubble ovals with soft, natural woodland floor mottling (`#0a2623` / `#081f1d` on `#071a18` base), dramatically improving ground readability, path visibility, and enemy silhouette clarity.
3. **Shadow Lord Flamethrower Cone Attack:**
   - Upgraded ranged attack into a continuous, devastating flamethrower cone stream:
     - Range: 280 units, 70° cone (35° half-angle).
     - Clear windup/telegraph (0.55s): hands raised with swirling orb of fire and aiming guide, allowing player to react and dodge.
     - Roaring flame stream (1.20s duration) with layered core, turbulent flame tongues, and flying embers.
     - Controlled single-damage application on hit frame (prevents unfair per-frame damage).
     - Line-of-sight raycasting via `CollisionManager.hasLineOfSight`: solid fortress walls obstruct flames and protect player.
     - Cooldown: 3.50s.
     - Preserves close Void Scythe melee cleave (range 65 units, windup 0.35s, cooldown 1.60s).
4. **Random Restorative Health Potion Drops During Boss Fight:**
   - Implemented `updateBossPotionSpawn(deltaTime)` in `Game.java`:
     - Spawns restorative 35 HP potion nearby (120–220 units from Arin) during Level 2 Shadow Lord fight when Arin's HP < 80.
     - Enforces 15-second cooldown and at most one active spawned boss potion at any time.
     - Safe coordinate generation: checks world bounds margins, avoids solid obstacles, and does not spawn directly on top of Arin or boss.
     - Stops spawning once Shadow Lord is defeated.
5. **Testing & Verification:**
   - Added `CastleEnemiesTest`, `ShadowLordFlamethrowerTest`, and `BossFightPotionSpawnTest`.
   - Total test suite: 392 tests run, 392 passed, 0 failures, 0 errors, 0 skipped.
   - 100% build and test success across all domain, combat, render, world, and session suites.

---

## Phase 5.2 — Complete In-Game Pause Menu System & Lifecycle Safety (November 15, 2026)

### Scope Implemented
1. **In-Game Pause System Architecture (`PauseMenuOverlay.java` & `GameScreen.java`):**
   - Pressing ESC during active gameplay immediately pauses the game without resetting state.
   - Complete suspension of domain logic: player movement, attacks, enemy AI, combat updates, boss flamethrower cone, transition timers, upgrade message banner, and boss health potion cooldowns are frozen (`if (state == GameState.PAUSED) return;`).
   - The game scene remains visible in the background beneath an atmospheric dark translucent backdrop (`rgba(5, 8, 18, 0.82)`).
2. **Pause Menu Structure & Submenus:**
   - **Title:** `PAUSED` with decorative `— SHADOW QUEST —` subline.
   - **Option 1: RESUME:** Closes pause menu, restores `GameState.PLAYING`, and resumes gameplay seamlessly.
   - **Option 2: RESTART LEVEL:** Displays a confirmation dialog (`CONFIRM_RESTART`). Upon confirmation, restarts only the current level via `game.restartLevel()`. Resets enemies, bosses, potions, and health; preserves sword upgrade in Level 2 (Shadow Castle); resets sword to ground in Level 1 (Mystic Forest).
   - **Option 3: SETTINGS:** Displays separate settings panel (`SETTINGS`) with real-time Fullscreen toggle (`Fullscreen: ON/OFF [F11]`) and audio status notice.
   - **Option 4: CONTROLS:** Displays full keyboard reference table (`CONTROLS`): WASD/Arrows (movement), Space (sword attack), E (portal interaction), ESC (pause/resume), F11 (fullscreen toggle).
   - **Option 5: MAIN MENU:** Displays confirmation dialog (`CONFIRM_MAIN_MENU`). Upon confirmation, cleans up gameplay screen, detaches input handlers, stops the game loop, and returns to title screen.
   - **Option 6: EXIT GAME:** Displays confirmation dialog (`CONFIRM_EXIT`). Upon confirmation, cleanly terminates the JavaFX application.
3. **Keyboard & Mouse Navigation:**
   - Full keyboard navigation supported: Up/Down arrow keys and W/S keys cycle through menu buttons with visual highlight.
   - Enter or Space activates the currently focused button.
   - Mouse hover updates focus dynamically and mouse clicks fire button actions.
   - ESC key contextually resumes gameplay from the main pause menu or navigates back from any submenu/dialog to the main pause menu.
   - Event filter prevents gameplay inputs (WASD, Space, E) from reaching Arin while the pause menu is open.
   - Event filters are cleanly detached on `leaveGame()`, preventing duplicate event handlers or memory leaks.
4. **Lifecycle & Loop Safety:**
   - Single game loop guarantee: pausing and resuming reuses the existing `GameLoop` without spawning duplicate timers or threads.
   - Timers frozen during pause: verified for portal transition, sword upgrade notification, and boss potion cooldown.
   - Game Over and Victory screens retain their existing controls without interference from the pause menu.
946: 5. **Testing & Verification:**
947:    - Added `PauseMenuTest` suite containing 13 automated tests covering: opening, resuming, timer freezing, enemy AI stoppage, settings fullscreen toggle, controls display, restart level confirmation & reset rules, sword upgrade preservation in Castle, main menu confirmation, exit confirmation, keyboard navigation cycling, game loop reuse, and Game Over / Victory preservation.
948:    - Total test suite: **405 tests run, 405 passed, 0 failures, 0 errors, 0 skipped.** 100% BUILD SUCCESS.

---

## Phase 5.3 — Complete Audio & Narration System (November 15, 2026)

### Scope Implemented
1. **Centralized Audio Architecture (`com.shadowquest.audio`):**
   - **`AudioManager`**: Central facade coordinating background music, voice narration, sound effects, independent volume channels, global mute, cooldown throttling, and lifecycle disposal.
   - **`AudioTrack`**: Enumerates background music tracks (`MENU`, `CUTSCENE`, `FOREST`, `CASTLE`, `BOSS`, `VICTORY`, `GAME_OVER`).
   - **`SoundEffect`**: Enumerates 28 distinct sound effects across player, enemies, portal, boss, and UI categories.
   - **`AudioBackend` & `JavaFXAudioBackend`**: Production audio backend using JavaFX `MediaPlayer` for background music and narration, and `AudioClip` for low-latency sound effects. Implements defensive fallback to `javax.sound.sampled.Clip` and exception-safe error handling so headless environments and systems with blocked native DLLs never crash.
   - **`MockAudioBackend`**: Lightweight deterministic mock recording all music paths, volumes, loop flags, SFX calls, and narration states for headless unit testing without requiring an audio device.
   - **`NarrationManager`**: Specialized controller for the seven opening cinematic voice lines with sequence synchronization, replay reset, and duplicate playback prevention.
2. **Seven-Scene Opening Cinematic Voice Narration:**
   - Generated authentic English voice narration for all seven scenes in `OpeningScene.java` using Windows SAPI (Desktop Speech Synthesizer) with "Microsoft David Desktop" (neutral male voice, clear articulation, cinematic storytelling delivery).
   - Saved as 16-bit Mono Linear PCM WAV files (22,050 Hz) at `src/main/resources/assets/audio/narration/scene{1..7}.wav`.
   - Wired into `OpeningCutscene` via `setOnSceneChange(sceneIndex -> AudioManager.getInstance().playNarration(sceneIndex + 1))`.
   - Synchronized with scene order; previous narration lines stop before the next scene starts.
   - Skipping or cancelling the cinematic immediately silences narration (`cutscene.skip()`, `leaveGame()`).
   - Replaying the cinematic restarts narration cleanly at Scene 1 without duplicate audio instances.
3. **Background Music Suite:**
   - Synthesized 7 atmospheric music tracks via `AudioSynthesizer.java`:
     - `menu.wav`: E minor dark fantasy ambient theme with bass drone, pad, and mystic bells.
     - `cutscene.wav`: D minor orchestral strings swell with harp arpeggios.
     - `forest.wav`: A minor nature exploration with wooden flute lead and chimes.
     - `castle.wav`: C minor gothic pipe organ and dark choir chords.
     - `boss.wav`: High-intensity 140 BPM battle drums with pulsing bass ostinato and brass stabs.
     - `victory.wav`: Triumphant C major fanfare with brass triads and radiant bells.
     - `game_over.wav`: Somber A minor funeral gong toll with descending minor decay.
   - Music automatically transitions between screens and levels (Menu -> Cutscene -> Forest -> Boss -> Castle -> Victory / Game Over).
   - Pausing the game pauses background music; resuming restores playback smoothly without restarting the track.
4. **Gameplay & Interface Sound Effects (28 Effects):**
   - **Player SFX**: Sword pickup (`sword_pickup.wav`), sword swing (`sword_swing.wav`), blade hit (`sword_hit.wav`), hurt grunt (`player_hurt.wav`), player death (`player_death.wav`), health potion healing (`heal.wav`), sword upgrade surge (`sword_upgrade.wav`).
   - **Enemy SFX**: Attack and death sounds for Goblin, Shadow Wolf, Giant Goblin, Shadow Knight, Dark Archer, and Wraith.
   - **Portal SFX**: Ancient stone activation chime (`portal_unlock.wav`), vortex entry warp (`portal_enter.wav`), and ambient hum (`portal_ambient.wav`).
   - **Boss SFX (Shadow Lord)**: Void intro surge (`shadow_lord_intro.wav`), Void Scythe melee cleave (`shadow_lord_melee.wav`), roaring flamethrower breath (`shadow_lord_fire.wav`), flame hit sizzle (`shadow_lord_fire_hit.wav`), and cataclysmic void death implosion (`shadow_lord_death.wav`).
   - **UI SFX**: Pause tap (`pause.wav`), resume tick (`resume.wav`), button click (`button_click.wav`), confirmation chime (`confirm.wav`).
   - All sounds triggered directly by domain game events, never by canvas rendering methods.
   - Cooldown throttling prevents ear-piercing sound stacking during multi-hit frames.
5. **Pause Menu Audio Settings Panel:**
   - Extended `PauseMenuOverlay.java` SETTINGS panel with real-time controls:
     - **Music Volume Slider**: 0% to 100%, updating `AudioManager.getInstance().setMusicVolume(...)` immediately.
     - **SFX Volume Slider**: 0% to 100%, updating `AudioManager.getInstance().setSfxVolume(...)` immediately.
     - **Mute / Unmute Button**: Toggles global mute, silencing all output, and restores prior volume levels upon unmuting.
     - Back navigation returns to the main pause menu with settings preserved.
6. **Documentation & Licensing:**
   - Created `docs/AUDIO_ASSETS_AND_LICENSES.md` documenting filenames, purposes, generation methods, durations, and 100% royalty-free status.
   - Updated `PROJECT_PLAN.md`, `IMPLEMENTATION_LOG.md`, and `PROJECT_DEVELOPMENT_REPORT.md`.
7. **Automated Testing & Results:**
   - Added 23 new unit tests across `AudioManagerTest`, `AudioResourceTest`, `NarrationManagerTest`, and `PauseMenuAudioSettingsTest`.
   - Verified that all 42 audio assets exist on the classpath, are non-empty, and contain valid RIFF/WAVE headers.
   - Total test suite: **428 tests run, 428 passed, 0 failures, 0 errors, 0 skipped.** 100% BUILD SUCCESS.

---

## Phase 5.3 Extension — Cutscene Voice Line Overlap Fix

### Root Cause Analysis
1. **Audio File Durations vs. Scene Durations:**
   - In `OpeningScene.java`, each scene has an allotted duration: Scene 1 (7.0s), Scene 2 (7.0s), Scene 3 (6.0s), Scene 4 (7.0s), Scene 5 (12.0s), Scene 6 (12.0s), Scene 7 (4.0s).
   - The initial narration WAV files produced with SAPI speech rate -1 exceeded these scene durations (e.g. Scene 1 was 8.30s [+1.30s overflow], Scene 2 was 8.22s [+1.22s overflow], Scene 4 was 9.48s [+2.48s overflow], Scene 7 was 5.52s [+1.52s overflow]). When a scene naturally progressed, the previous audio line was still speaking.
2. **Audio Backend Sampled Fallback Lifecycle Management:**
   - Under Windows Application Control policies where `glib-lite.dll` (JavaFX media native library) is blocked, `JavaFXAudioBackend` correctly falls back to `javax.sound.sampled.Clip`.
   - However, in `JavaFXAudioBackend.java`, `playSampledClip` did not track the running `Clip` instance in a field (`activeNarrationClip`). Consequently, calling `stopNarration()` or switching to the next scene did not stop the running sampled clip, causing the previous clip to continue speaking in the background simultaneously with the new scene's narration.

### Key Changes
1. **Calibrated Narration WAV File Regeneration:**
   - Regenerated all seven voice narration WAV files using calibrated speech rates (`Rate 1` to `Rate 3`) via Windows SAPI ("Microsoft David Desktop").
   - Verified that every narration track cleanly finishes before its scene duration expires with comfortable margins:
     - `scene1.wav`: 5.95s (Scene duration: 7.0s; 1.05s buffer)
     - `scene2.wav`: 5.87s (Scene duration: 7.0s; 1.13s buffer)
     - `scene3.wav`: 4.68s (Scene duration: 6.0s; 1.32s buffer)
     - `scene4.wav`: 6.02s (Scene duration: 7.0s; 0.98s buffer)
     - `scene5.wav`: 7.45s (Scene duration: 12.0s; 4.55s buffer)
     - `scene6.wav`: 8.50s (Scene duration: 12.0s; 3.50s buffer)
     - `scene7.wav`: 3.50s (Scene duration: 4.0s; 0.50s buffer)
2. **Enhanced Sampled Audio Lifecycle Tracking in `JavaFXAudioBackend.java`:**
   - Added `activeNarrationClip` and `activeMusicClip` tracking for the sampled audio fallback mode.
   - Updated `stopNarration()` to explicitly stop, close, and nullify `activeNarrationClip`.
   - Updated `pauseNarration()` and `resumeNarration()` to properly pause and resume `activeNarrationClip`.
   - Configured `LineListener` on `activeNarrationClip` and SFX clips to automatically close clips upon completion to prevent system resource leaks.
   - Ensured `stopNarration()` is called at the start of `playNarration()`, guaranteeing zero voice line overlap across scene transitions, skips, and screen transitions.
3. **Verification:**
   - Verified that all 428 automated unit tests compile and pass cleanly (`mvnw.cmd test` -> BUILD SUCCESS, 428 passed, 0 failures, 0 errors).
