package com.shadowquest.ui;

import com.shadowquest.audio.AudioManager;
import com.shadowquest.audio.SoundEffect;
import com.shadowquest.core.Game;
import com.shadowquest.core.GameConfig;
import com.shadowquest.core.GameState;
import com.shadowquest.enemy.Enemy;
import com.shadowquest.items.HealthPotion;
import com.shadowquest.items.PowerUpItem;
import com.shadowquest.items.SwordItem;
import com.shadowquest.player.Player;
import com.shadowquest.render.WorldRenderer;
import com.shadowquest.world.Camera;
import com.shadowquest.world.GameWorld;
import com.shadowquest.world.Portal;
import java.util.List;
import java.util.Objects;
import java.util.function.Supplier;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.geometry.VPos;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.Background;
import javafx.scene.layout.BackgroundFill;
import javafx.scene.layout.CornerRadii;
import javafx.scene.layout.HBox;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.scene.text.TextAlignment;

/** Movement and gameplay screen: renders world, UI overlays, transitions, and HUD. */
public final class GameScreen extends StackPane {
    private static final Color BACKGROUND_COLOR = Color.web("#171b2e");
    private static final Color PLAYER_COLOR = Color.web("#f3e6bf");

    private final Player player;
    private GameWorld world;
    private final List<Enemy> enemies;
    private final SwordItem swordItem;
    private final List<HealthPotion> healthPotions;
    private final Supplier<PowerUpItem> powerUpSupplier;
    private Camera camera;
    private WorldRenderer renderer;
    private final HBox toolbar;
    private final Label controlsLabel;
    private final StackPane gameOverOverlay;
    private final StackPane victoryOverlay;
    private final PauseMenuOverlay pauseOverlay;
    private final Label gameOverSubtitle;
    private final Canvas canvas = new Canvas(
            GameConfig.VIEWPORT_WIDTH, GameConfig.VIEWPORT_HEIGHT);

    private Game game;

    public GameScreen(Player player, GameWorld world, Runnable returnToTitle) {
        this(player, world, List.of(), null, List.of(), () -> null, () -> {}, returnToTitle);
    }

    public GameScreen(Player player, GameWorld world, List<Enemy> enemies, Runnable returnToTitle) {
        this(player, world, enemies, null, List.of(), () -> null, () -> {}, returnToTitle);
    }

    public GameScreen(Player player, GameWorld world, List<Enemy> enemies, SwordItem swordItem, Runnable returnToTitle) {
        this(player, world, enemies, swordItem, List.of(), () -> null, () -> {}, returnToTitle);
    }

    public GameScreen(Player player, GameWorld world, List<Enemy> enemies, SwordItem swordItem, Runnable onRestart, Runnable returnToTitle) {
        this(player, world, enemies, swordItem, List.of(), () -> null, onRestart, returnToTitle);
    }

    public GameScreen(Game game, Runnable onRestart, Runnable returnToTitle) {
        this(game, onRestart, returnToTitle, null, null, null);
    }

    public GameScreen(Game game, Runnable onRestart, Runnable returnToTitle,
                      Runnable onExit, Runnable onToggleFullScreen, Supplier<Boolean> isFullScreenSupplier) {
        this(game.getPlayer(), game.getWorld(), game.getEnemies(), game.getSwordItem(),
                game.getHealthPotions(), game::getPowerUpItem, onRestart, returnToTitle,
                onExit, onToggleFullScreen, isFullScreenSupplier);
        this.game = game;
    }

    public GameScreen(Player player, GameWorld world, List<Enemy> enemies, SwordItem swordItem,
                      List<HealthPotion> healthPotions, Supplier<PowerUpItem> powerUpSupplier,
                      Runnable onRestart, Runnable returnToTitle) {
        this(player, world, enemies, swordItem, healthPotions, powerUpSupplier, onRestart, returnToTitle,
                null, null, null);
    }

    public GameScreen(Player player, GameWorld world, List<Enemy> enemies, SwordItem swordItem,
                      List<HealthPotion> healthPotions, Supplier<PowerUpItem> powerUpSupplier,
                      Runnable onRestart, Runnable returnToTitle,
                      Runnable onExit, Runnable onToggleFullScreen, Supplier<Boolean> isFullScreenSupplier) {
        this.player = Objects.requireNonNull(player, "player");
        this.world = Objects.requireNonNull(world, "world");
        this.enemies = enemies != null ? enemies : List.of();
        this.swordItem = swordItem;
        this.healthPotions = healthPotions != null ? healthPotions : List.of();
        this.powerUpSupplier = powerUpSupplier != null ? powerUpSupplier : () -> null;
        camera = new Camera(world);
        renderer = new WorldRenderer(world);
        Objects.requireNonNull(onRestart, "onRestart");
        Objects.requireNonNull(returnToTitle, "returnToTitle");
        setPrefSize(GameConfig.VIEWPORT_WIDTH, GameConfig.VIEWPORT_HEIGHT);
        setBackground(new Background(new BackgroundFill(BACKGROUND_COLOR, CornerRadii.EMPTY, Insets.EMPTY)));
        setFocusTraversable(true);
        canvas.setManaged(false);

        Button back = new Button("Back to title");
        back.setFocusTraversable(false);
        back.setOnAction(event -> returnToTitle.run());
        controlsLabel = new Label("Move: WASD or arrows | Attack: Space");
        controlsLabel.setTextFill(PLAYER_COLOR);
        toolbar = new HBox(16, back, controlsLabel);
        toolbar.setAlignment(Pos.CENTER_LEFT);
        toolbar.setMaxSize(USE_PREF_SIZE, USE_PREF_SIZE);
        StackPane.setAlignment(toolbar, Pos.BOTTOM_LEFT);
        StackPane.setMargin(toolbar, new Insets(12));

        // Game Over overlay banner
        gameOverOverlay = new StackPane();
        gameOverOverlay.setStyle("-fx-background-color: rgba(5, 8, 17, 0.88);");
        gameOverOverlay.setVisible(false);

        VBox box = new VBox(18);
        box.setAlignment(Pos.CENTER);

        Label title = new Label("GAME OVER");
        title.setFont(Font.font("System", FontWeight.BOLD, 42));
        title.setTextFill(Color.web("#e74c3c"));

        gameOverSubtitle = new Label("Arin succumbed to the perils of the realm.");
        gameOverSubtitle.setFont(Font.font("System", FontWeight.NORMAL, 17));
        gameOverSubtitle.setTextFill(Color.web("#cbd5e1"));

        Button restartBtn = new Button("Restart Level (R)");
        restartBtn.setFocusTraversable(false);
        restartBtn.setStyle("-fx-background-color: #1e293b; -fx-text-fill: #f3e6bf; -fx-font-size: 15px; -fx-font-weight: bold; -fx-border-color: #d4af37; -fx-border-width: 1.5px; -fx-border-radius: 4px; -fx-padding: 8px 24px; -fx-cursor: hand;");
        restartBtn.setOnAction(e -> onRestart.run());

        Button menuBtn = new Button("Main Menu (Esc)");
        menuBtn.setFocusTraversable(false);
        menuBtn.setStyle("-fx-background-color: #1e293b; -fx-text-fill: #94a3b8; -fx-font-size: 15px; -fx-font-weight: bold; -fx-border-color: #475569; -fx-border-width: 1.5px; -fx-border-radius: 4px; -fx-padding: 8px 24px; -fx-cursor: hand;");
        menuBtn.setOnAction(e -> returnToTitle.run());

        HBox btnBox = new HBox(20, restartBtn, menuBtn);
        btnBox.setAlignment(Pos.CENTER);

        box.getChildren().addAll(title, gameOverSubtitle, btnBox);
        gameOverOverlay.getChildren().add(box);

        // Victory overlay banner
        victoryOverlay = new StackPane();
        victoryOverlay.setStyle("-fx-background-color: rgba(4, 7, 15, 0.92);");
        victoryOverlay.setVisible(false);

        VBox vicBox = new VBox(18);
        vicBox.setAlignment(Pos.CENTER);

        Label vicTitle = new Label("VICTORY ACHIEVED!");
        vicTitle.setFont(Font.font("System", FontWeight.BOLD, 44));
        vicTitle.setTextFill(Color.web("#f59e0b"));

        Label vicSub1 = new Label("THE SHADOW LORD HAS BEEN VANQUISHED");
        vicSub1.setFont(Font.font("System", FontWeight.BOLD, 19));
        vicSub1.setTextFill(Color.web("#fbbf24"));

        Label vicSub2 = new Label("Eldoria is saved from darkness. Arin has restored light to the realm.");
        vicSub2.setFont(Font.font("System", FontWeight.NORMAL, 16));
        vicSub2.setTextFill(Color.web("#cbd5e1"));

        Button playAgainBtn = new Button("Play Again (Enter)");
        playAgainBtn.setFocusTraversable(false);
        playAgainBtn.setStyle("-fx-background-color: #1e293b; -fx-text-fill: #facc15; -fx-font-size: 15px; -fx-font-weight: bold; -fx-border-color: #f59e0b; -fx-border-width: 1.5px; -fx-border-radius: 4px; -fx-padding: 8px 24px; -fx-cursor: hand;");
        playAgainBtn.setOnAction(e -> onRestart.run());

        Button vicMenuBtn = new Button("Main Menu (Esc)");
        vicMenuBtn.setFocusTraversable(false);
        vicMenuBtn.setStyle("-fx-background-color: #1e293b; -fx-text-fill: #94a3b8; -fx-font-size: 15px; -fx-font-weight: bold; -fx-border-color: #475569; -fx-border-width: 1.5px; -fx-border-radius: 4px; -fx-padding: 8px 24px; -fx-cursor: hand;");
        vicMenuBtn.setOnAction(e -> returnToTitle.run());

        HBox vicBtnBox = new HBox(20, playAgainBtn, vicMenuBtn);
        vicBtnBox.setAlignment(Pos.CENTER);

        vicBox.getChildren().addAll(vicTitle, vicSub1, vicSub2, vicBtnBox);
        victoryOverlay.getChildren().add(vicBox);

        // In-game Pause Menu overlay
        this.pauseOverlay = new PauseMenuOverlay(
                this::resumeFromPause,
                onRestart,
                returnToTitle,
                onExit,
                onToggleFullScreen,
                isFullScreenSupplier
        );
        pauseOverlay.setVisible(false);

        getChildren().addAll(canvas, toolbar, gameOverOverlay, victoryOverlay, pauseOverlay);
        render();
    }

    private void resumeFromPause() {
        if (game != null) {
            game.resume();
        }
        AudioManager.getInstance().playSfx(SoundEffect.UI_RESUME);
        AudioManager.getInstance().resumeMusic();
        pauseOverlay.setVisible(false);
    }

    public PauseMenuOverlay getPauseOverlay() {
        return pauseOverlay;
    }

    public void openPauseMenu() {
        if (game != null && game.getState() == GameState.PLAYING) {
            game.pause();
        }
        AudioManager.getInstance().playSfx(SoundEffect.UI_PAUSE);
        AudioManager.getInstance().pauseMusic();
        pauseOverlay.showMain();
    }

    public void handlePauseEscape() {
        pauseOverlay.handleEscape();
    }

    @Override
    protected void layoutChildren() {
        super.layoutChildren();
        double canvasHeight = Math.max(0, getHeight() - toolbar.prefHeight(-1) - 24);
        if (canvas.getWidth() != getWidth() || canvas.getHeight() != canvasHeight) {
            canvas.setWidth(getWidth());
            canvas.setHeight(canvasHeight);
            render();
        }
    }

    public void render() {
        double width = canvas.getWidth();
        double height = canvas.getHeight();
        if (width <= 0 || height <= 0) {
            return;
        }

        // 1. Black transition screen during level transitions
        if (game != null && game.isTransitioning()) {
            GraphicsContext graphics = canvas.getGraphicsContext2D();
            graphics.setFill(Color.web("#000000"));
            graphics.fillRect(0, 0, width, height);

            graphics.setFont(Font.font("System", FontWeight.BOLD, 28));
            graphics.setTextAlign(TextAlignment.CENTER);
            graphics.setTextBaseline(VPos.CENTER);
            graphics.setFill(Color.web("#38bdf8"));
            graphics.fillText("ENTERING SHADOW CASTLE...", width / 2.0, height / 2.0 - 18);

            graphics.setFont(Font.font("System", FontWeight.NORMAL, 16));
            graphics.setFill(Color.web("#94a3b8"));
            graphics.fillText("The final confrontation awaits.", width / 2.0, height / 2.0 + 20);
            return;
        }

        // 2. Check if level world has switched
        if (game != null && game.getWorld() != this.world) {
            this.world = game.getWorld();
            this.camera = new Camera(world);
            this.renderer = new WorldRenderer(world);
        }

        camera.follow(player.getX(), player.getY(), width, height);
        GraphicsContext graphics = canvas.getGraphicsContext2D();
        graphics.setFill(BACKGROUND_COLOR);
        graphics.fillRect(0, 0, width, height);
        graphics.save();
        try {
            graphics.beginPath();
            graphics.rect(-camera.getX() - 96, -camera.getY() - 96,
                    world.getWidth() + 192, world.getHeight() + 192);
            graphics.clip();

            PowerUpItem powerUp = powerUpSupplier != null ? powerUpSupplier.get() : null;
            Portal portal = game != null ? game.getPortal() : null;
            double upgradeTimer = game != null ? game.getSwordUpgradeMessageTimer() : 0.0;
            List<Enemy> activeEnemies = game != null ? game.getEnemies() : enemies;
            List<HealthPotion> activePotions = game != null ? game.getHealthPotions() : healthPotions;

            renderer.render(graphics, camera, player, activeEnemies, swordItem, activePotions, powerUp,
                    portal, upgradeTimer, width, height);

        } finally {
            graphics.restore();
        }

        // 3. UI Overlays (Victory, Game Over, Pause, or Gameplay hints)
        if (game != null && game.getState() == GameState.PAUSED) {
            if (!pauseOverlay.isVisible()) {
                pauseOverlay.showMain();
            }
            if (gameOverOverlay.isVisible()) {
                gameOverOverlay.setVisible(false);
            }
            if (victoryOverlay.isVisible()) {
                victoryOverlay.setVisible(false);
            }
        } else {
            if (pauseOverlay.isVisible()) {
                pauseOverlay.setVisible(false);
            }
            if (game != null && game.getState() == GameState.VICTORY) {
                if (!victoryOverlay.isVisible()) {
                    victoryOverlay.setVisible(true);
                }
                if (gameOverOverlay.isVisible()) {
                    gameOverOverlay.setVisible(false);
                }
            } else if (!player.isAlive()) {
                if (game != null && game.getCurrentLevel() == 2) {
                    gameOverSubtitle.setText("Arin was consumed by the shadows of the Castle.");
                } else {
                    gameOverSubtitle.setText("Arin succumbed to the perils of the Mystic Forest.");
                }
                if (!gameOverOverlay.isVisible()) {
                    gameOverOverlay.setVisible(true);
                }
                if (victoryOverlay.isVisible()) {
                    victoryOverlay.setVisible(false);
                }
            } else {
                if (gameOverOverlay.isVisible()) {
                    gameOverOverlay.setVisible(false);
                }
                if (victoryOverlay.isVisible()) {
                    victoryOverlay.setVisible(false);
                }
            }
        }

        // 4. Update interaction hint label
        if (game != null && game.getState() == GameState.PAUSED) {
            controlsLabel.setText("PAUSED | Up/Down or W/S: Navigate | Enter: Select | ESC: Resume");
            controlsLabel.setTextFill(Color.web("#fbbf24"));
        } else if (game != null && game.getPortal() != null && game.getPortal().isPlayerInRange(player)) {
            controlsLabel.setText("Move: WASD or arrows | Attack: Space | [ PRESS E TO ENTER PORTAL ] | ESC: Pause");
            controlsLabel.setTextFill(Color.web("#38bdf8"));
        } else {
            controlsLabel.setText("Move: WASD or arrows | Attack: Space | ESC: Pause");
            controlsLabel.setTextFill(PLAYER_COLOR);
        }
    }
}
