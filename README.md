# Shadow Quest: The Lost Realm

A five-member college project built with Java 21, JavaFX 21, and Maven.
Phases 3.1–3.2 add a 2880 x 1800 world and a following camera to the keyboard-driven
Canvas movement demo. Phase 3.3 keeps the whole 24-unit player inside the world.
Phase 3.5 adds a skippable seven-scene opening cinematic between the title screen
and gameplay. The user currently handles coding,
architecture, and integration. Phase 3.4 adds a dark teal Mystic Forest made from temporary Canvas shapes. Combat remains future work.

The source of truth is [PROJECT_PLAN.md](docs/PROJECT_PLAN.md) and
[ARCHITECTURE.md](docs/ARCHITECTURE.md). Dated revisions preserve the original
plans while recording the current roadmap and implemented contracts.

## Requirements

- A JDK 21 installation. Set `JAVA_HOME` to its directory and put its `bin`
  directory first on `PATH`.
- A graphical desktop session to run JavaFX.
- Internet access on the first build to download Maven and dependencies.
- Git for the team's GitHub workflow.

The committed Maven Wrapper downloads Maven 3.9.11. A separate Maven install or
JavaFX SDK download is unnecessary. The only application dependency is
`javafx-controls`, which brings in JavaFX graphics, including Canvas, and base.
FXML, Swing, and third-party game libraries are not used.

## Setup and run

Main repository: [prabhakarshukla/shawdowquest](https://github.com/prabhakarshukla/shawdowquest).
The GitHub repository name differs from the Maven artifact name
`shadow-quest-lost-realm`; this does not affect building or running the project.

Clone your fork and open the repository root, the directory containing `pom.xml`.

In Windows PowerShell, point this terminal at your JDK 21 installation:

```powershell
$env:JAVA_HOME = 'C:\path\to\jdk-21'
$env:Path = "$env:JAVA_HOME\bin;$env:Path"
java -version
javac -version
.\mvnw.cmd -version
.\mvnw.cmd clean compile
.\mvnw.cmd test
.\mvnw.cmd javafx:run
```

On macOS or Linux, set `JAVA_HOME` to your JDK 21 installation, then run:

```sh
export PATH="$JAVA_HOME/bin:$PATH"
java -version
javac -version
sh ./mvnw -version
sh ./mvnw clean compile
sh ./mvnw test
sh ./mvnw javafx:run
```

If Maven 3.9.11 is installed, the equivalent commands are `mvn clean compile`,
`mvn test`, and `mvn javafx:run`.

Expected result: a dark window with "Shadow Quest: The Lost Realm" centered in
light text. Press **Enter** to play the opening cinematic: seven illustrated
scenes tell Arin's story from the peaceful forest to his Eldoria mission, with
narration, fades, and a "Esc / Space / Enter · Skip opening" control. Skipping
or finishing the cinematic starts Mystic Forest gameplay exactly once. In
gameplay, hold **WASD or arrow keys**
to move the circle, including diagonally; release to stop. Click **Back to title**
to leave. Re-entering starts a fresh player, and pressing Enter replays the
cinematic from Scene 1. Space, E, and Esc are reserved gameplay inputs
with no action yet. The player starts at the world's upper-left. The camera follows
as you move into the interior and stops at world edges. Forest objects scroll in world coordinates. Tree trunks, rocks and thick roots block movement; grass, bushes, pathways and small roots are walkable.
Player boundaries keep the whole circle inside every edge and corner. Controls
occupy a separate strip below the Canvas. Close
the window using its title-bar close button; Maven should report `BUILD SUCCESS`.

`test` runs JUnit 5 tests for game transitions, cutscene playback and lifecycle,
player initialization, movement,
health, and invalid inputs without starting JavaFX. JUnit is a test-only
dependency. Reports are written to `target/surefire-reports/`.

Phase 3.5 verification: all 258 tests pass, including the 224-test Phase 3.4
baseline plus 34 new cutscene tests. The application was launched for real and
opened cleanly; the user's visual walkthrough of the cinematic and gameplay
transition is the remaining confirmation. Cinematic artwork is temporary
procedural Canvas art, not the final illustrations. See the development report
for details.

Phase 3.4 note: the revised root-hitbox, density and depth-ordering changes have
automated coverage; their manual acceptance remains pending. See the
development report for details.

## Project structure

```text
.
|-- pom.xml                         Java 21 compilation and JavaFX run support
|-- mvnw / mvnw.cmd                 Maven Wrapper launchers
|-- .mvn/wrapper/                   Wrapper version and Maven download settings
|-- .github/PULL_REQUEST_TEMPLATE.md
|-- docs/                          Plans, current roadmap, architecture, learning report
|-- src/main/java/com/shadowquest/
|   |-- app/Main.java               JavaFX entry point
|   |-- core/GameConfig.java        Title and initial viewport dimensions
|   |-- core/GameState.java         Session state enum (MENU, CUTSCENE, PLAYING, ...)
|   |-- core/Game.java              Session transitions, cinematic rules, death detection
|   |-- cutscene/OpeningScene.java   The seven approved story beats as an enum
|   |-- cutscene/OpeningCutscene.java JavaFX-free playback, skip, cancel and reset
|   |-- core/FrameClock.java        Frame timestamps to elapsed seconds
|   |-- core/GameLoop.java          JavaFX update/render scheduling
|   |-- ui/ScreenManager.java       Stage, screen flow, and loop lifecycle
|   |-- ui/MainMenuScreen.java      Minimal launch-screen layout
|   |-- ui/CutsceneScreen.java      Placeholder cinematic storyboard renderer
|   |-- ui/GameScreen.java          Temporary Canvas player and return button
|   |-- world/GameWorld.java        World dimensions, separate from viewport size
|   |-- world/EnvironmentObject.java World scenery and separate solid hitboxes
|   |-- world/MysticForest.java       Deterministic forest layout
|   |-- world/CollisionManager.java   Swept obstacle contact and sliding
|   |-- render/WorldRenderer.java     Temporary forest Canvas artwork
|   |-- world/Camera.java           Follow/clamp and world-to-screen conversion
|   |-- input/InputHandler.java     Held movement/action keys
|   |-- player/Character.java      Shared stats, health, and position
|   |-- player/Player.java         Player initialization and movement
|   |-- enemy/                     Reserved for enemies and combat
|   |-- items/                     Reserved for items and inventory
|   |-- levels/                    Reserved for levels and progression
|   |-- render/                    Canvas rendering; final assets remain future work
|   |-- persistence/               Reserved for save/load
|   `-- utils/Direction.java       Four world directions
|-- src/main/resources/
|   |-- styles/launch.css          Launch-screen styling
|   |-- images/                    Reserved image assets
|   |-- maps/                      Reserved map resources
|   `-- audio/                     Reserved audio assets
`-- src/test/java/com/shadowquest/
    |-- core/GameTest.java
    |-- core/GameCutsceneTest.java
    |-- cutscene/OpeningCutsceneTest.java
    |-- cutscene/OpeningSceneTest.java
    |-- player/CharacterTest.java
    |-- player/PlayerTest.java
    |-- enemy/
    |-- items/
    |-- levels/
    `-- persistence/
```

Reserved directories contain `.gitkeep` files so Git preserves the agreed
structure. Existing markers may remain as packages receive implementations.
Maven writes generated output under `target/`, which is ignored by Git.

`Main` starts JavaFX and delegates to `ScreenManager`. The screen manager
creates the scene and displays `MainMenuScreen`. The screen reads shared
window settings from `GameConfig` and draws its title on Canvas.
Future domain packages must remain independent of JavaFX, and the renderer
must only display domain state.

## Troubleshooting

- If Java fails to start or Maven reports the wrong Java version, correct
  `JAVA_HOME` and `PATH` in the same terminal, then check `mvnw -version` again.
- If compilation reports an unsupported release, use JDK 21.
- If dependency downloads fail, check network/proxy access to Maven Central.
- If a window cannot open, run from a graphical desktop rather than a headless
  session.
- If JavaFX reports "An Application Control policy has blocked this file",
  ask the device administrator to review the JavaFX native libraries or verify
  the application on a permitted development machine.
- In VS Code, open the repository folder and select JDK 21 for this project.
  Keep personal editor configuration out of commits.

See [CONTRIBUTING.md](CONTRIBUTING.md) for the fork and pull request workflow.
No external assets are included in this baseline.

See [the initialization report](docs/INITIALIZATION.md) for the file inventory,
build results, and the environment issue that blocked local launch verification.
See [the implementation log](docs/IMPLEMENTATION_LOG.md) for the current Phase 1
APIs, transition rules, test results, and integration limitations.
See [the project development report](docs/PROJECT_DEVELOPMENT_REPORT.md) for the
permanent development record, class explanations, and pre-push audit.

Build references: [OpenJFX Maven setup](https://openjfx.io/openjfx-docs/),
[JavaFX 21 release notes](https://gluonhq.com/products/javafx/openjfx-21-release-notes/),
and [Apache Maven Wrapper](https://maven.apache.org/tools/wrapper/).
