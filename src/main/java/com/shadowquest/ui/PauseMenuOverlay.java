package com.shadowquest.ui;

import com.shadowquest.audio.AudioManager;
import com.shadowquest.audio.SoundEffect;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.function.Supplier;
import javafx.application.Platform;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.Slider;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.layout.HBox;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.scene.text.TextAlignment;

/**
 * Visually polished In-Game Pause Menu overlay with dark fantasy aesthetics,
 * sub-menu navigation, confirmation dialogs, settings, controls, and full keyboard/mouse support.
 */
public class PauseMenuOverlay extends StackPane {

    public enum View {
        MAIN,
        SETTINGS,
        CONTROLS,
        CONFIRM_RESTART,
        CONFIRM_MAIN_MENU,
        CONFIRM_EXIT
    }

    private View currentView = View.MAIN;

    private final Runnable onResume;
    private final Runnable onRestart;
    private final Runnable onReturnToTitle;
    private final Runnable onExitGame;
    private final Runnable onToggleFullScreen;
    private final Supplier<Boolean> isFullScreenSupplier;

    private final VBox cardBox;
    private final List<Button> activeButtons = new ArrayList<>();
    private int focusedButtonIndex = 0;
    private Slider musicSlider;
    private Slider sfxSlider;

    public PauseMenuOverlay(Runnable onResume,
                            Runnable onRestart,
                            Runnable onReturnToTitle,
                            Runnable onExitGame,
                            Runnable onToggleFullScreen,
                            Supplier<Boolean> isFullScreenSupplier) {
        this.onResume = onResume != null ? onResume : () -> {};
        this.onRestart = onRestart != null ? onRestart : () -> {};
        this.onReturnToTitle = onReturnToTitle != null ? onReturnToTitle : () -> {};
        this.onExitGame = onExitGame != null ? onExitGame : () -> Platform.exit();
        this.onToggleFullScreen = onToggleFullScreen != null ? onToggleFullScreen : () -> {};
        this.isFullScreenSupplier = isFullScreenSupplier != null ? isFullScreenSupplier : () -> false;

        // Dark translucent atmospheric backdrop overlay
        setStyle("-fx-background-color: rgba(5, 8, 18, 0.82);");
        setAlignment(Pos.CENTER);
        setFocusTraversable(true);

        // Gothic fantasy frame container
        cardBox = new VBox(14);
        cardBox.setAlignment(Pos.CENTER);
        cardBox.setMaxWidth(380);
        cardBox.setStyle("-fx-background-color: #0f172a; " +
                "-fx-border-color: #d4af37; " +
                "-fx-border-width: 2px; " +
                "-fx-border-radius: 8px; " +
                "-fx-background-radius: 8px; " +
                "-fx-padding: 26px 34px; " +
                "-fx-effect: dropshadow(gaussian, rgba(0,0,0,0.75), 18, 0, 0, 6);");

        getChildren().add(cardBox);

        // Key navigation listener for Up, Down, W, S, Enter, Space, Escape
        addEventFilter(KeyEvent.KEY_PRESSED, event -> {
            KeyCode code = event.getCode();
            if (code == KeyCode.UP || code == KeyCode.W) {
                event.consume();
                navigateUp();
            } else if (code == KeyCode.DOWN || code == KeyCode.S) {
                event.consume();
                navigateDown();
            } else if (code == KeyCode.ENTER || code == KeyCode.SPACE) {
                event.consume();
                activateFocusedButton();
            } else if (code == KeyCode.ESCAPE) {
                event.consume();
                handleEscape();
            }
        });

        rebuildView();
    }

    public View getCurrentView() {
        return currentView;
    }

    public void setView(View view) {
        this.currentView = Objects.requireNonNull(view, "view");
        rebuildView();
    }

    public void showMain() {
        setView(View.MAIN);
        setVisible(true);
        requestFocus();
    }

    public void resume() {
        setVisible(false);
        onResume.run();
    }

    public void confirmRestart() {
        setVisible(false);
        onRestart.run();
    }

    public void confirmReturnToTitle() {
        setVisible(false);
        onReturnToTitle.run();
    }

    public void confirmExitGame() {
        setVisible(false);
        onExitGame.run();
    }

    public void toggleFullScreen() {
        onToggleFullScreen.run();
        if (currentView == View.SETTINGS) {
            rebuildView();
        }
    }

    public void handleEscape() {
        if (currentView == View.MAIN) {
            resume();
        } else {
            setView(View.MAIN);
        }
    }

    public List<Button> getActiveButtons() {
        return List.copyOf(activeButtons);
    }

    public int getFocusedButtonIndex() {
        return focusedButtonIndex;
    }

    public void navigateDown() {
        if (activeButtons.isEmpty()) return;
        focusedButtonIndex = (focusedButtonIndex + 1) % activeButtons.size();
        updateButtonFocus();
    }

    public void navigateUp() {
        if (activeButtons.isEmpty()) return;
        focusedButtonIndex = (focusedButtonIndex - 1 + activeButtons.size()) % activeButtons.size();
        updateButtonFocus();
    }

    public void activateFocusedButton() {
        if (!activeButtons.isEmpty() && focusedButtonIndex >= 0 && focusedButtonIndex < activeButtons.size()) {
            activeButtons.get(focusedButtonIndex).fire();
        }
    }

    private void updateButtonFocus() {
        for (int i = 0; i < activeButtons.size(); i++) {
            Button btn = activeButtons.get(i);
            boolean isFocused = (i == focusedButtonIndex);
            if (isFocused) {
                applyFocusedStyle(btn);
                btn.requestFocus();
            } else {
                applyNormalStyle(btn);
            }
        }
    }

    private void rebuildView() {
        cardBox.getChildren().clear();
        activeButtons.clear();
        focusedButtonIndex = 0;

        switch (currentView) {
            case MAIN -> buildMainView();
            case SETTINGS -> buildSettingsView();
            case CONTROLS -> buildControlsView();
            case CONFIRM_RESTART -> buildConfirmRestartView();
            case CONFIRM_MAIN_MENU -> buildConfirmMainMenuView();
            case CONFIRM_EXIT -> buildConfirmExitView();
        }

        updateButtonFocus();
    }

    // --- 1. Main Pause Menu ---
    private void buildMainView() {
        cardBox.setMaxWidth(380);

        Label title = new Label("PAUSED");
        title.setFont(Font.font("System", FontWeight.BOLD, 32));
        title.setTextFill(Color.web("#f59e0b"));

        Label subtitle = new Label("— SHADOW QUEST —");
        subtitle.setFont(Font.font("System", FontWeight.NORMAL, 12));
        subtitle.setTextFill(Color.web("#94a3b8"));
        VBox.setMargin(subtitle, new Insets(-8, 0, 8, 0));

        Button resumeBtn = createMenuButton("RESUME", this::resume);
        Button restartBtn = createMenuButton("RESTART LEVEL", () -> setView(View.CONFIRM_RESTART));
        Button settingsBtn = createMenuButton("SETTINGS", () -> setView(View.SETTINGS));
        Button controlsBtn = createMenuButton("CONTROLS", () -> setView(View.CONTROLS));
        Button menuBtn = createMenuButton("MAIN MENU", () -> setView(View.CONFIRM_MAIN_MENU));
        Button exitBtn = createMenuButton("EXIT GAME", () -> setView(View.CONFIRM_EXIT));

        cardBox.getChildren().addAll(title, subtitle, resumeBtn, restartBtn, settingsBtn, controlsBtn, menuBtn, exitBtn);
    }

    // --- 2. Settings Panel ---
    private void buildSettingsView() {
        cardBox.setMaxWidth(440);

        Label title = new Label("SETTINGS");
        title.setFont(Font.font("System", FontWeight.BOLD, 28));
        title.setTextFill(Color.web("#f59e0b"));

        boolean isFull = isFullScreenSupplier.get();
        Button fullscreenBtn = createMenuButton("Fullscreen: " + (isFull ? "ON [F11]" : "OFF [F11]"), this::toggleFullScreen);

        AudioManager audio = AudioManager.getInstance();

        Label musicLabel = new Label(String.format("Music Volume: %d%%", (int)(audio.getMusicVolume() * 100)));
        musicLabel.setFont(Font.font("System", FontWeight.BOLD, 13));
        musicLabel.setTextFill(Color.web("#e2e8f0"));

        musicSlider = new Slider(0, 100, audio.getMusicVolume() * 100);
        musicSlider.setMaxWidth(360);
        musicSlider.valueProperty().addListener((obs, oldVal, newVal) -> {
            audio.setMusicVolume(newVal.doubleValue() / 100.0);
            musicLabel.setText(String.format("Music Volume: %d%%", newVal.intValue()));
        });

        Label sfxLabel = new Label(String.format("SFX Volume: %d%%", (int)(audio.getSfxVolume() * 100)));
        sfxLabel.setFont(Font.font("System", FontWeight.BOLD, 13));
        sfxLabel.setTextFill(Color.web("#e2e8f0"));

        sfxSlider = new Slider(0, 100, audio.getSfxVolume() * 100);
        sfxSlider.setMaxWidth(360);
        sfxSlider.valueProperty().addListener((obs, oldVal, newVal) -> {
            audio.setSfxVolume(newVal.doubleValue() / 100.0);
            sfxLabel.setText(String.format("SFX Volume: %d%%", newVal.intValue()));
        });

        VBox audioControls = new VBox(8);
        audioControls.setAlignment(Pos.CENTER);
        audioControls.setStyle("-fx-background-color: #1e293b; -fx-padding: 12px 16px; -fx-border-color: #334155; -fx-border-radius: 6px; -fx-background-radius: 6px;");
        audioControls.getChildren().addAll(musicLabel, musicSlider, sfxLabel, sfxSlider);

        Button muteBtn = createMenuButton(audio.isMuted() ? "Sound: MUTED [Click to Unmute]" : "Sound: ENABLED [Click to Mute]", () -> {
            audio.toggleMute();
            rebuildView();
        });

        Button backBtn = createMenuButton("Back (ESC)", () -> setView(View.MAIN));

        cardBox.getChildren().addAll(title, fullscreenBtn, audioControls, muteBtn, backBtn);
    }

    // --- 3. Controls Panel ---
    private void buildControlsView() {
        cardBox.setMaxWidth(440);

        Label title = new Label("CONTROLS");
        title.setFont(Font.font("System", FontWeight.BOLD, 28));
        title.setTextFill(Color.web("#f59e0b"));

        VBox list = new VBox(10);
        list.setAlignment(Pos.CENTER);
        list.setStyle("-fx-background-color: #1e293b; -fx-padding: 14px 18px; -fx-border-color: #334155; -fx-border-radius: 6px; -fx-background-radius: 6px;");

        list.getChildren().addAll(
                createControlRow("WASD / Arrows", "8-Directional Movement"),
                createControlRow("Space", "Sword Attack"),
                createControlRow("E", "Interact / Enter Portal"),
                createControlRow("ESC", "Pause / Resume Menu"),
                createControlRow("F11", "Toggle Fullscreen")
        );

        Button backBtn = createMenuButton("Back (ESC)", () -> setView(View.MAIN));

        cardBox.getChildren().addAll(title, list, backBtn);
    }

    private HBox createControlRow(String key, String desc) {
        HBox row = new HBox(12);
        row.setAlignment(Pos.CENTER_LEFT);

        Label keyLabel = new Label(key);
        keyLabel.setFont(Font.font("System", FontWeight.BOLD, 13));
        keyLabel.setTextFill(Color.web("#fbbf24"));
        keyLabel.setPrefWidth(125);

        Label descLabel = new Label(desc);
        descLabel.setFont(Font.font("System", FontWeight.NORMAL, 13));
        descLabel.setTextFill(Color.web("#cbd5e1"));

        row.getChildren().addAll(keyLabel, descLabel);
        return row;
    }

    // --- 4. Confirm Restart Panel ---
    private void buildConfirmRestartView() {
        cardBox.setMaxWidth(420);

        Label title = new Label("RESTART LEVEL");
        title.setFont(Font.font("System", FontWeight.BOLD, 26));
        title.setTextFill(Color.web("#f59e0b"));

        Label msg = new Label("Are you sure you want to restart this level?\nEnemies, boss state, and health will reset.");
        msg.setFont(Font.font("System", FontWeight.NORMAL, 14));
        msg.setTextFill(Color.web("#cbd5e1"));
        msg.setTextAlignment(TextAlignment.CENTER);

        Button confirmBtn = createDangerButton("Yes, Restart Level", this::confirmRestart);
        Button cancelBtn = createMenuButton("Cancel", () -> setView(View.MAIN));

        cardBox.getChildren().addAll(title, msg, confirmBtn, cancelBtn);
    }

    // --- 5. Confirm Main Menu Panel ---
    private void buildConfirmMainMenuView() {
        cardBox.setMaxWidth(420);

        Label title = new Label("RETURN TO TITLE");
        title.setFont(Font.font("System", FontWeight.BOLD, 26));
        title.setTextFill(Color.web("#f59e0b"));

        Label msg = new Label("Return to the title screen?\nAny unsaved progress in this level will be lost.");
        msg.setFont(Font.font("System", FontWeight.NORMAL, 14));
        msg.setTextFill(Color.web("#cbd5e1"));
        msg.setTextAlignment(TextAlignment.CENTER);

        Button confirmBtn = createDangerButton("Yes, Return to Menu", this::confirmReturnToTitle);
        Button cancelBtn = createMenuButton("Cancel", () -> setView(View.MAIN));

        cardBox.getChildren().addAll(title, msg, confirmBtn, cancelBtn);
    }

    // --- 6. Confirm Exit Panel ---
    private void buildConfirmExitView() {
        cardBox.setMaxWidth(420);

        Label title = new Label("EXIT GAME");
        title.setFont(Font.font("System", FontWeight.BOLD, 26));
        title.setTextFill(Color.web("#ef4444"));

        Label msg = new Label("Are you sure you want to quit Shadow Quest?");
        msg.setFont(Font.font("System", FontWeight.NORMAL, 14));
        msg.setTextFill(Color.web("#cbd5e1"));
        msg.setTextAlignment(TextAlignment.CENTER);

        Button confirmBtn = createDangerButton("Yes, Quit Game", this::confirmExitGame);
        Button cancelBtn = createMenuButton("Cancel", () -> setView(View.MAIN));

        cardBox.getChildren().addAll(title, msg, confirmBtn, cancelBtn);
    }

    // --- Helper: Menu Buttons with Hover & Selection States ---
    private Button createMenuButton(String text, Runnable action) {
        Button btn = new Button(text);
        btn.setPrefWidth(260);
        btn.setPrefHeight(40);
        btn.setFocusTraversable(true);
        applyNormalStyle(btn);

        final int myIndex = activeButtons.size();
        btn.hoverProperty().addListener((obs, oldV, isHovered) -> {
            if (isHovered) {
                focusedButtonIndex = myIndex;
                updateButtonFocus();
            }
        });

        btn.setOnAction(e -> {
            AudioManager.getInstance().playSfx(SoundEffect.UI_CLICK);
            action.run();
        });
        activeButtons.add(btn);
        return btn;
    }

    private Button createDangerButton(String text, Runnable action) {
        Button btn = new Button(text);
        btn.setPrefWidth(260);
        btn.setPrefHeight(40);
        btn.setFocusTraversable(true);
        applyDangerNormalStyle(btn);

        final int myIndex = activeButtons.size();
        btn.hoverProperty().addListener((obs, oldV, isHovered) -> {
            if (isHovered) {
                focusedButtonIndex = myIndex;
                updateButtonFocus();
            }
        });

        btn.setOnAction(e -> {
            AudioManager.getInstance().playSfx(SoundEffect.UI_CONFIRM);
            action.run();
        });
        activeButtons.add(btn);
        return btn;
    }

    public Slider getMusicSlider() {
        return musicSlider;
    }

    public Slider getSfxSlider() {
        return sfxSlider;
    }

    private void applyNormalStyle(Button btn) {
        btn.setStyle("-fx-background-color: #1e293b; " +
                "-fx-text-fill: #f3e6bf; " +
                "-fx-font-size: 14px; " +
                "-fx-font-weight: bold; " +
                "-fx-border-color: #475569; " +
                "-fx-border-width: 1.5px; " +
                "-fx-border-radius: 5px; " +
                "-fx-background-radius: 5px; " +
                "-fx-cursor: hand;");
    }

    private void applyFocusedStyle(Button btn) {
        btn.setStyle("-fx-background-color: #334155; " +
                "-fx-text-fill: #facc15; " +
                "-fx-font-size: 14px; " +
                "-fx-font-weight: bold; " +
                "-fx-border-color: #d4af37; " +
                "-fx-border-width: 1.5px; " +
                "-fx-border-radius: 5px; " +
                "-fx-background-radius: 5px; " +
                "-fx-cursor: hand; " +
                "-fx-effect: dropshadow(gaussian, rgba(212,175,55,0.45), 8, 0, 0, 0);");
    }

    private void applyDangerNormalStyle(Button btn) {
        btn.setStyle("-fx-background-color: #3f1d24; " +
                "-fx-text-fill: #fca5a5; " +
                "-fx-font-size: 14px; " +
                "-fx-font-weight: bold; " +
                "-fx-border-color: #991b1b; " +
                "-fx-border-width: 1.5px; " +
                "-fx-border-radius: 5px; " +
                "-fx-background-radius: 5px; " +
                "-fx-cursor: hand;");
    }
}
