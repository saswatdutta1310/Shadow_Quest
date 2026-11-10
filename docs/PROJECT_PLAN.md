# Shadow Quest: The Lost Realm — Project Plan

## Current direction and roadmap: November 14, 2026

This dated revision is the current design direction supplied for Phase 2.4.
The original plan below is preserved as history; where it differs, this revision
takes precedence. The project remains a college group project, but the user is
currently handling all coding, architecture, and integration. The earlier
five-member assignments are historical, not current task assignments.

### Confirmed game design and three-level scope (Completion Deadline: November 17, 2026)

Shadow Quest is a top-down 2D fantasy adventure with eight-directional movement,
real-time combo-based sword combat, and illustrated, hand-drawn fantasy visuals.
Arin discovers a mysterious portal while exploring a forest and enters Eldoria,
a realm corrupted by the Shadow Lord. The approved opening is implemented as the
Phase 3.5 skippable cinematic: a seven-scene sequence shows the peaceful forest,
the portal discovery, entering Eldoria, a multi-day exploration montage, and the
realization that Arin must defeat the Shadow Lord's forces and restore the
ancient portals. The cinematic plays between the title screen and Mystic Forest
and can be skipped with Esc, Space, or Enter; every new game replays it from
Scene 1. Player control begins in Mystic Forest. Cutscenes for later story
moments remain planned.

The complete game structure has exactly TWO levels and two boss encounters:

| Level | Boss | Planned & Implemented Content |
| --- | --- | --- |
| 1. Mystic Forest | Giant Goblin (Level 1 Boss) | Exactly 3 normal Goblins, 3 ShadowWolves, 3 restorative Health Potions, and Giant Goblin boss (activates after all 3 normal Goblins are defeated). Defeating the boss awards a permanent +5 sword upgrade (attack 10 -> 15) and unlocks the ancient realm portal. Implemented in Phases 4.1–4.3.1 & 5. |
| 2. Shadow Castle | Shadow Lord (Final Boss) | Dark gothic fortress, ruined stone pillars and arches, pale green ghostly mist, burning torches, castle guards, and the final confrontation with the Shadow Lord. Defeating the Shadow Lord triggers realm restoration and the final Victory screen. Implemented in Phase 5. |

Ancient Ruins has been completely removed from the game scope. Progression uses the ancient realm portal: defeating the Giant Goblin awakens the portal; walking to it and pressing E triggers an atmospheric 2-second black screen transition to Level 2: Shadow Castle. Arin's health and upgraded sword power carry over into the castle.

Planned player data includes health, attack power, defence, speed, coins, equipped
weapon, and inventory. Only the initial statistics, health, and movement exist
today. Combat begins with a three-hit sword combo, attack animations and hit
windows, enemy damage/defeat, and enemy-specific behavior. Rewards and upgrades
include coins, potions, weapons, armour, upgrade materials, and rare quest items.
Proposed weapons progress through Basic Sword, Iron Sword, Steel Sword, and
Shadow Blade. These systems are not part of Phase 2.4.

The planned top-right HUD shows health, coins, equipped weapon, and inventory
access. Pause, inventory, upgrade, game-over, victory, and boss-health displays
remain planned. The temporary movement screen is not this final HUD or art style.

### Current development sequence

| Phase | Scope | Current status |
| --- | --- | --- |
| 0 | Maven setup and JavaFX launch | Setup complete; launch later verified in 2.1 |
| 1 | Core domain foundation | Initial player, health, directions, states complete |
| 2 | JavaFX screen, Canvas, game loop | Foundations implemented in 2.1–2.3 |
| 2.4 | Keyboard input and player movement | Eight-direction movement integration implemented; verification in development report |
| 3.1 | Game world | Configurable rectangular world implemented |
| 3.2 | Camera system | Deterministic following, clamping and Canvas translation implemented |
| 3.3 | Player world boundaries | Implemented November 14; closure November 15, 194 tests pass; GUI evidence in report |
| 3.4 | Mystic Forest environment and obstacle collision | Implemented; 224 tests pass; revised root/density manual acceptance pending |
| 3.5 | Opening cinematic (seven scenes, skippable) | Implemented; 258 tests pass; verified in JavaFX |
| 3.6 | Gameplay Integration (Hand-painted Arin character & animation) | Implemented; 263 tests pass; verified in JavaFX |
| 4.1 | Enemy Architecture, AI Behaviour & Mystic Forest Spawning | Implemented; Goblins & ShadowWolves; 288 tests pass |
| 4.1.1 | Enemy Spawn Revision & Level 1 Boss (Giant Goblin) | Implemented; 3 Goblins, 3 Wolves, Giant Goblin; 296 tests pass |
| 4.2 | Player Sword Combat, Enemy Damage & Defeat | Implemented; Sword pickup, space attack, combat system, hit flash & defeat; 314 tests pass |
| 4.3 | Enemy Combat, Player Survival, Fullscreen & Game Over | Implemented; Enemy attacks (Goblin/Wolf/GiantGoblin), player damage & invulnerability, health bar, Game Over & restart, borderless fullscreen & F11 toggle; 339 tests pass |
| 4.3.1 | Consumables & Boss Rewards: Health Potions & Damage Power-Up | Implemented; 3 restorative health potions, Giant Goblin dropped Titan Strength Crest (+8 ATK power-up), HUD attack power badge; 356 tests pass |
| 5 | Two-Level Progression: Mystic Forest to Shadow Castle & Victory | Implemented; Ancient Ruins removed; +5 Sword Upgrade (10 -> 15 ATK) with notification; Ancient Portal & 2s Black Transition; Shadow Castle Environment (2400x1400); Final Boss Shadow Lord (Melee & Ranged attacks, Boss Health Bar); Final Victory Screen; 377 tests pass |
| 5.1 | Castle Enemy Variety, Forest Optimization & Flamethrower Boss Mechanics | Implemented; Castle enemies (Shadow Knight, Dark Archer, Wraith) replace forest spawns in Shadow Castle; Mystic Forest visual decluttering, open corridors & soft ground rendering; Shadow Lord Flamethrower cone attack (range 280, windup 0.55s, duration 1.20s, cooldown 3.50s, single controlled hit, wall obstruction); Boss fight restorative health potion drops (< 80 HP, 15s cooldown, safe spawn 120-220 units); 392 tests pass |
| 5.2 | Complete In-Game Pause Menu System & Lifecycle Safety | Implemented; Full in-game pause menu (PAUSED); immediate game suspension and timer freezing; Resume, Restart Level (with confirmation), Settings (fullscreen toggle), Controls reference, Main Menu (with confirmation), and Exit Game (with confirmation); full keyboard (Up/Down/W/S/Enter/ESC) & mouse navigation; dark fantasy styling; 405 tests pass |

Java 21, JavaFX 21, Canvas, and Maven remain agreed technologies. Implement only
the phase explicitly authorized. Do not pre-create empty classes from the target
architecture. The user reserves commit/push decisions; no publication is implied
by completing a phase.

## Original project plan, retained for history

The November 14 Phase 3.1–3.2 increment adds only world dimensions and a camera.
The temporary grid/border are coordinate references, not environmental objects.
Player world-boundary restrictions remain deferred to Phase 3.3. The approved
story, four levels, portals, artwork direction, combat and progression design are
unchanged. Verification and remaining manual checks are in the development report.

> **Project type:** Group Java class project  
> **Team size:** 5 members  
> **Language:** Java  
> **GUI:** JavaFX  
> **Build tool:** Maven  
> **Rendering approach:** JavaFX Canvas  
> **Repository:** `shadow-quest-lost-realm`

---

## 1. Project Overview

**Shadow Quest: The Lost Realm** is a 2D adventure game developed in Java. The player controls Arin, a young warrior trapped in the ancient realm of Eldoria after it has been corrupted by the Shadow Lord.

The player must explore four areas, defeat enemies, collect useful items, unlock routes, and ultimately defeat the Shadow Lord to escape the realm.

The project must be both:

1. A playable game with a coherent beginning, progression, and ending.
2. A practical demonstration of Java concepts taught in the course.

The team should prioritize a stable, playable core game over ambitious optional features.

## 2. Story and Level Sequence

1. **Mystic Forest** — Introduces movement and basic combat. Enemies include goblins and wolves. The player finds an ancient key.
2. **Ancient Ruins** — Introduces stronger enemies, including skeletons and dark archers. The player obtains a more powerful weapon.
3. **Shadow Dungeon** — Contains stronger enemies, obstacles, and a mini-boss.
4. **Shadow Castle** — The final area, where Arin faces the Shadow Lord.

The story, protagonist, four locations, and final objective must remain consistent throughout development.

## 3. Core Gameplay

### Must-have gameplay

- Main menu with Play, Help, and Exit.
- Player movement using WASD and arrow keys.
- Basic player animation or sprite-state changes.
- Player health and damage.
- Basic attack using Space.
- At least three enemy types with different behaviour or stats.
- Collision detection between the player, enemies, walls, and objects.
- Collectible items and coins.
- A basic inventory.
- Keys and locked progression between levels.
- Four sequential levels.
- A final boss encounter.
- Pause, game-over, and victory screens.
- Clear feedback for damage, collection, level completion, and defeat.

### Should-have gameplay

- Multiple weapon types.
- Health potions and usable inventory items.
- Enemy movement or simple detection/follow behaviour.
- Save and load progress.
- Basic sound effects.
- Simple puzzles or interactable obstacles.

### Optional features

- XP and player-level progression.
- Advanced enemy AI.
- Achievements.
- Additional weapons and enemy types.
- More elaborate animations and visual effects.

Optional features must not delay the completion of the must-have scope.

## 4. Required Java Concepts

The following concepts must be visible in the actual source code and, where useful, demonstrated during the project presentation.

| Java concept | Planned implementation |
|---|---|
| Classes and objects | Player, Enemy, Weapon, Item, Inventory, Level, Game |
| Encapsulation | Private fields and validated methods for health, coins, inventory, and game state |
| Inheritance | Goblin, Orc, DarkArcher, ShadowLord extend Enemy |
| Abstraction | Abstract Character class for shared character properties and behaviour |
| Polymorphism | Enemy references invoke the correct subclass behaviour |
| Method overriding | Enemy subclasses override attack/update or other suitable behaviour |
| Method overloading | Meaningful overloads such as attack(target) and attack(target, weapon); avoid artificial overloads |
| Interfaces | Attackable and Collectable where the contract is genuinely shared |
| Constructors | Initialize game entities with valid starting state |
| Collections | ArrayList for enemies and inventory; Set for unique keys where appropriate |
| Exception handling | Handle file I/O, malformed save data, and missing essential resources |
| File handling | Save/load player progress if time permits; otherwise demonstrate a smaller reliable persistence feature |
| Enum | GameState and other finite state categories |
| Static/final | Shared immutable constants and configuration |
| Packages | Separate app, core, player, enemy, items, levels, ui, and utils |
| Event handling | JavaFX keyboard and button event handlers |
| Lambda expressions | Concise JavaFX event handlers where appropriate |
| GUI and animation | JavaFX Scene, Canvas, and AnimationTimer |

Do not use exceptions for normal game outcomes such as an enemy being defeated or the player losing health.

## 5. Five-Member Work Distribution

Each member owns a module and its tests/documentation. Shared contracts must be agreed before parallel implementation.

### Member 1 — Project Lead, UI and Integration (You)

**Owns:**
- Application entry point and JavaFX screen flow.
- Main menu, help screen, pause screen, HUD, game-over screen, victory screen.
- Keyboard and UI event wiring.
- Canvas rendering integration.
- GitHub repository maintenance, integration, and release checks.

**Does not own:** All gameplay logic. Gameplay modules remain with their respective owners.

### Member 2 — Visuals and Rendering Support

**Owns:**
- Sprite and background asset selection/creation.
- Tile/map visual assets and animation frames.
- Rendering helpers or renderer implementation in coordination with Member 1.
- Asset attribution and resource-folder organization.
- Visual consistency and asset-loading tests.

**Boundary:** Must not independently change core game rules or player/enemy APIs. Coordinate any renderer-facing data requirements with the project lead.

### Member 3 — Player and Combat

**Owns:**
- Abstract Character and Player.
- Enemy base class and enemy subclasses.
- Attack/damage/defence/health behaviour.
- Combat rules and enemy behaviour.
- Unit tests for combat and character state.

**Boundary:** Must not directly create JavaFX screens or manipulate the JavaFX Stage.

### Member 4 — Items and Inventory

**Owns:**
- Item hierarchy, Weapon, HealthPotion, Key.
- Inventory and collection behaviour.
- Coin/reward handling.
- Collectable and other relevant interfaces.
- Unit tests for inventory and item behaviour.

**Boundary:** Inventory and item logic must remain independent of JavaFX UI classes.

### Member 5 — Levels, Progression and Persistence

**Owns:**
- Level model and the four level definitions.
- Level transitions, doors, keys, obstacles, and puzzle rules.
- Save/load service and save-data validation.
- Custom save exception if genuinely useful.
- Tests for progression and persistence.

**Boundary:** Must use agreed player/inventory interfaces and must not duplicate their logic.

### Shared responsibilities

- Every member reviews and tests their own changes.
- Every member writes a short README or usage note for their module.
- Every member contributes commits and pull requests.
- All members help test the integrated game.
- Project lead coordinates integration; this does not make the lead the owner of every module.

## 6. Development Phases

### Phase 0 — Repository and architecture baseline

- Confirm Java/JDK and Maven versions.
- Add Maven project configuration and JavaFX dependency.
- Add package structure and application entry point.
- Add README, `.gitignore`, and contribution instructions.
- Confirm the project launches on all team members' machines.

**Exit condition:** Every member can clone the repository and run the starter application.

### Phase 1 — Contracts and independent modules

- Agree on class responsibilities and method signatures.
- Implement Player/Enemy/Combat, Items/Inventory, and Level/Progression models.
- Create small unit tests for each module.
- Keep the JavaFX UI as a simple placeholder during this phase.

**Exit condition:** Modules compile and can be tested without launching the complete game.

### Phase 2 — First playable prototype

- Render a basic map and player.
- Implement movement and collision.
- Add one enemy and working combat.
- Add one collectible and inventory interaction.
- Add health display and pause/resume.

**Exit condition:** A player can move, fight, take damage, collect an item, and restart.

### Phase 3 — Complete game progression

- Build all four levels.
- Add keys, doors, level transitions, and the mini-boss.
- Add the Shadow Lord final encounter.
- Add game-over and victory flows.

**Exit condition:** The game can be completed from the first level to the final victory screen.

### Phase 4 — Persistence and polish

- Add save/load if stable and feasible.
- Validate malformed or missing save files.
- Improve sprites, animations, feedback, and sound.
- Fix bugs and test edge cases.

**Exit condition:** No known critical issue blocks the normal game flow.

### Phase 5 — Submission readiness

- Test on a clean clone.
- Verify all required Java concepts in source code.
- Update README, controls, setup steps, screenshots, and team contributions.
- Prepare a short demonstration script and project explanation.

**Exit condition:** Another student can clone, run, play, and understand the project using the README.

## 7. GitHub Collaboration Rules

Use a fork-and-pull-request workflow.

1. The project lead owns the main repository.
2. Each teammate forks the repository.
3. Each teammate creates a feature branch in their fork.
4. Work is committed in small, meaningful commits.
5. The teammate opens a pull request against the main repository's `main` branch.
6. The project lead or another assigned reviewer checks the code and runs the relevant tests.
7. Merge only when the change builds and does not break the agreed contracts.

Suggested branch names:

- `feature/javafx-ui`
- `feature/rendering-assets`
- `feature/player-combat`
- `feature/inventory-items`
- `feature/levels-save-system`

Rules:

- Never force-push to the shared `main` branch.
- Do not commit IDE-specific files, `target/`, or generated output.
- Do not commit private credentials or personal data.
- Do not add unlicensed or unattributed game assets.
- Keep pull requests focused; avoid changing unrelated modules.
- Pull/rebase the latest upstream `main` before starting or updating a feature branch.
- Resolve conflicts with the owners of the affected modules.

## 8. Definition of Done

A feature is done only when:

- It compiles with the project's configured Java version.
- It follows the agreed package and API boundaries.
- It has been tested, manually or with automated tests as appropriate.
- It handles invalid input and edge cases where relevant.
- It does not break the existing game.
- Its public classes and important methods are documented.
- Its pull request explains what changed and how it was tested.

## 9. Scope and Risk Control

- Build one complete playable level before expanding to four.
- Use placeholder shapes or freely licensed assets until gameplay works.
- Keep game rules independent of JavaFX rendering.
- Do not introduce a database, networking, or a game engine unless the team explicitly approves a real need.
- Keep save/load simple; JSON or a clearly documented text format is sufficient.
- Avoid adding optional features until the end-to-end game is playable.
- If the semester deadline becomes tight, reduce visual complexity before removing core game progression.

## 10. Final Deliverable

A runnable JavaFX 2D adventure game with:

- A main menu and playable game screen.
- Player movement, collisions, health, and combat.
- Multiple enemy types and collectible items.
- Inventory and level progression.
- Four connected levels and a final boss.
- Pause, game-over, and victory screens.
- Complete Audio and Narration System (Phase 5.3):
  - Centralized AudioManager managing background music, sound effects, voice narration, independent volume sliders, and global mute.
  - English voice narration for all 7 opening cinematic scenes.
  - 7 atmospheric music tracks (Menu, Cutscene, Forest, Castle, Boss, Victory, Game Over).
  - 28 sound effects for player, enemies, portal, boss, and UI actions.
  - Pause menu audio settings with real-time volume sliders and mute toggle.
- Demonstrable Java OOP, collections, event handling, exception handling, GUI, and file handling where implemented.
- A clear README, setup instructions, and team contribution history.
