package com.shadowquest.ui;

import com.shadowquest.audio.AudioManager;
import com.shadowquest.audio.AudioTrack;
import com.shadowquest.audio.SoundEffect;
import com.shadowquest.core.GameConfig;
import com.shadowquest.core.GameLoop;
import com.shadowquest.core.Game;
import com.shadowquest.core.GameState;
import com.shadowquest.cutscene.OpeningCutscene;
import com.shadowquest.input.InputHandler;
import java.util.function.DoubleConsumer;
import javafx.scene.Scene;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyCombination;
import javafx.scene.input.KeyEvent;
import javafx.scene.layout.StackPane;
import javafx.stage.Stage;
import javafx.stage.WindowEvent;

/** Owns screen presentation on the application's primary stage. */
public final class ScreenManager {
    private final Stage stage;
    private final StackPane rootPane;
    private final Scene scene;
    private GameLoop gameLoop;
    private InputHandler input;
    private javafx.event.EventHandler<KeyEvent> gameplayKeyFilter;
    /** The session created for the opening cinematic, handed to gameplay on completion. */
    private Game pendingGame;
    private boolean fullScreen = true;

    public ScreenManager(Stage stage) {
        this.stage = stage;
        this.rootPane = new StackPane();
        this.scene = new Scene(rootPane, GameConfig.VIEWPORT_WIDTH, GameConfig.VIEWPORT_HEIGHT);
        stage.setScene(scene);
        stage.setFullScreenExitKeyCombination(KeyCombination.NO_MATCH);
        stage.setFullScreenExitHint("");
        stage.addEventFilter(KeyEvent.KEY_PRESSED, event -> {
            if (event.getCode() == KeyCode.F11) {
                event.consume();
                toggleFullScreen();
            }
        });
        stage.fullScreenProperty().addListener((observable, previous, current) -> {
            this.fullScreen = current;
        });
        stage.addEventHandler(WindowEvent.WINDOW_HIDDEN, event -> {
            AudioManager.getInstance().dispose();
            leaveGame();
        });
        stage.focusedProperty().addListener((observable, previous, focused) -> {
            if (!focused && input != null) {
                input.clear();
            }
        });
    }

    public void toggleFullScreen() {
        fullScreen = !stage.isFullScreen();
        stage.setFullScreen(fullScreen);
        if (!fullScreen) {
            stage.setWidth(GameConfig.VIEWPORT_WIDTH);
            stage.setHeight(GameConfig.VIEWPORT_HEIGHT);
            stage.centerOnScreen();
        }
    }

    public boolean isFullScreen() {
        return stage.isFullScreen();
    }

    private void ensureFullScreen() {
        if (fullScreen && !stage.isFullScreen()) {
            stage.setFullScreen(true);
        }
    }

    /** Displays the title screen; pressing Enter plays the opening cinematic first. */
    public void showMainMenu() {
        leaveGame();
        pendingGame = null;
        AudioManager.getInstance().resetForMainMenu();
        MainMenuScreen screen = new MainMenuScreen();
        rootPane.getChildren().setAll(screen);
        scene.setOnKeyPressed(event -> {
            if (event.getCode() == KeyCode.ENTER) {
                event.consume();
                showCutscene();
            }
        });
        stage.show();
        ensureFullScreen();
    }

    /**
     * Plays the seven-scene opening cinematic for a new game.
     * The session enters CUTSCENE here; gameplay starts only when it completes or is skipped.
     */
    public void showCutscene() {
        leaveGame();
        AudioManager.getInstance().playMusic(AudioTrack.CUTSCENE, true);
        OpeningCutscene cutscene = new OpeningCutscene(this::finishCutscene);
        cutscene.setOnSceneChange(sceneIdx -> AudioManager.getInstance().playNarration(sceneIdx + 1));
        CutsceneScreen screen = new CutsceneScreen(cutscene);
        rootPane.getChildren().setAll(screen);
        scene.setOnKeyPressed(event -> {
            if (event.getCode() == KeyCode.ESCAPE
                    || event.getCode() == KeyCode.SPACE
                    || event.getCode() == KeyCode.ENTER) {
                event.consume();
                AudioManager.getInstance().stopNarration();
                cutscene.skip();
            }
        });
        stage.show();
        ensureFullScreen();
        // Assigned after screen installation.
        pendingGame = new Game();
        pendingGame.startOpeningCinematic();
        // Reuse the shared loop: update advances the story, render draws the current scene.
        startGameLoop(cutscene::update, screen::render);
    }

    /** Single completion path for skip and natural end; the state guard keeps it one-time. */
    private void finishCutscene() {
        AudioManager.getInstance().stopNarration();
        Game game = pendingGame;
        pendingGame = null;
        if (game == null) {
            return; // The screen was already left; never start gameplay from a cancelled run.
        }
        game.completeCutscene();
        showGameplay(game);
    }

    /** Shows Mystic Forest gameplay for the session prepared by the cinematic. */
    private void showGameplay(Game game) {
        leaveGame();
        if (game.getCurrentLevel() == 1) {
            AudioManager.getInstance().playMusic(AudioTrack.FOREST, true);
        } else {
            AudioManager.getInstance().playMusic(AudioTrack.CASTLE, true);
        }
        // Position Arin inside the open clearing at the Mystic Forest trailhead
        if (game.getPlayer() != null && game.getPlayer().getX() == 0 && game.getPlayer().getY() == 0) {
            game.getPlayer().spawnAt(60, 110, game.getWorld());
        }
        GameScreen screen = new GameScreen(game, () -> restartLevel(game), this::showMainMenu,
                stage::close, this::toggleFullScreen, this::isFullScreen);
        rootPane.getChildren().setAll(screen);
        scene.setOnKeyPressed(null);
        scene.setOnKeyReleased(null);

        if (gameplayKeyFilter != null) {
            scene.removeEventFilter(KeyEvent.KEY_PRESSED, gameplayKeyFilter);
        }
        gameplayKeyFilter = event -> {
            if (event.getCode() == KeyCode.ESCAPE) {
                event.consume();
                if (game.getState() == GameState.PLAYING) {
                    game.pause();
                    if (input != null) {
                        input.clear();
                    }
                    screen.openPauseMenu();
                    screen.render();
                } else if (game.getState() == GameState.PAUSED) {
                    screen.handlePauseEscape();
                    screen.render();
                } else if (game.getState() == GameState.GAME_OVER || game.getState() == GameState.VICTORY) {
                    showMainMenu();
                }
            }
        };
        scene.addEventFilter(KeyEvent.KEY_PRESSED, gameplayKeyFilter);

        input = new InputHandler();
        input.attach(scene);
        stage.show();
        ensureFullScreen();
        screen.requestFocus();
        startGameLoop(deltaTime -> {
            game.update(deltaTime);
            if (game.getState() == GameState.PLAYING) {
                game.movePlayer(input.getHorizontal(), input.getVertical(), deltaTime);
                if (input.isPressed(KeyCode.SPACE)) {
                    game.attackPlayer();
                }
                if (input.isPressed(KeyCode.E)) {
                    game.enterPortal();
                }
            } else if (game.getState() == GameState.GAME_OVER) {
                if (input.isPressed(KeyCode.R) || input.isPressed(KeyCode.ENTER)) {
                    restartLevel(game);
                } else if (input.isPressed(KeyCode.ESCAPE)) {
                    showMainMenu();
                }
            } else if (game.getState() == GameState.VICTORY) {
                if (input.isPressed(KeyCode.R) || input.isPressed(KeyCode.ENTER)) {
                    game.startNewGame();
                    showGameplay(game);
                } else if (input.isPressed(KeyCode.ESCAPE)) {
                    showMainMenu();
                }
            }
        }, screen::render);
    }

    private void restartLevel(Game game) {
        AudioManager.getInstance().resetForLevel();
        game.restartLevel();
        showGameplay(game);
    }

    private void leaveGame() {
        stopGameLoop();
        AudioManager.getInstance().stopNarration();
        if (gameplayKeyFilter != null) {
            scene.removeEventFilter(KeyEvent.KEY_PRESSED, gameplayKeyFilter);
            gameplayKeyFilter = null;
        }
        // Abandoning a cutscene screen must not let it start gameplay later.
        pendingGame = null;
        if (input != null) {
            input.detach();
            input = null;
        }
    }

    /** Starts callbacks after the scene has been installed. */
    public void startGameLoop(DoubleConsumer update, Runnable render) {
        stopGameLoop();
        gameLoop = new GameLoop(update, render);
        gameLoop.start();
    }

    public void stopGameLoop() {
        if (gameLoop != null) {
            gameLoop.stop();
            gameLoop = null;
        }
    }
}
