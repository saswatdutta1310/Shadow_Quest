package com.shadowquest.ui;

import com.shadowquest.core.GameConfig;
import com.shadowquest.cutscene.OpeningCutscene;
import com.shadowquest.cutscene.OpeningScene;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.image.Image;
import javafx.scene.layout.StackPane;
import javafx.scene.paint.*;
import javafx.scene.shape.StrokeLineCap;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.scene.text.TextAlignment;

import java.io.InputStream;

/**
 * Hand-painted 2D fantasy animated storyboard renderer for the seven-scene opening cinematic.
 *
 * Renders rich hand-painted environments with parallax depth, expressive character
 * acting for Arin, atmospheric particle/lighting effects, and the ancient tree trunk portal
 * with supernatural volumetric dimensional energy.
 *
 * Driven by the shared GameLoop and OpeningCutscene state machine; owns no input or timing.
 */
public final class CutsceneScreen extends StackPane {
    /** Fixed design-space dimensions; rendering scales and letterboxes to match actual canvas size. */
    private static final double DESIGN_WIDTH = 960;
    private static final double DESIGN_HEIGHT = 600;

    /** Ground baseline for character and environment placement. */
    private static final double GROUND_Y = 468;

    // --- Pre-loaded hand-painted visual assets (loaded once, shared across frames) ---
    private static final Image IMG_BG_PEACEFUL = loadImage("/assets/cutscene/backgrounds/peaceful_forest.png");
    private static final Image IMG_BG_ELDORIA = loadImage("/assets/cutscene/backgrounds/eldoria_forest.png");
    private static final Image IMG_BG_MONTAGE1 = loadImage("/assets/cutscene/backgrounds/montage_day1.png");
    private static final Image IMG_BG_MONTAGE2 = loadImage("/assets/cutscene/backgrounds/montage_day2.png");
    private static final Image IMG_BG_MONTAGE3 = loadImage("/assets/cutscene/backgrounds/montage_day3.png");

    private static final Image IMG_ARIN_STAND = loadImage("/assets/cutscene/characters/arin_stand.png");
    private static final Image IMG_ARIN_WALK1 = loadImage("/assets/cutscene/characters/arin_walk1.png");
    private static final Image IMG_ARIN_WALK2 = loadImage("/assets/cutscene/characters/arin_walk2.png");
    private static final Image IMG_ARIN_WALK3 = loadImage("/assets/cutscene/characters/arin_walk3.png");
    private static final Image IMG_ARIN_WALK4 = loadImage("/assets/cutscene/characters/arin_walk4.png");
    private static final Image IMG_ARIN_LOOK = loadImage("/assets/cutscene/characters/arin_look.png");
    private static final Image IMG_ARIN_REACT = loadImage("/assets/cutscene/characters/arin_react.png");
    private static final Image IMG_ARIN_CAUTIOUS = loadImage("/assets/cutscene/characters/arin_cautious.png");
    private static final Image IMG_ARIN_KNEEL = loadImage("/assets/cutscene/characters/arin_kneel.png");

    /**
     * 4-frame walk cycle array: contact-right, passing, contact-left, passing.
     * Provides smooth natural gait with proper leg alternation.
     */
    private static final Image[] WALK_CYCLE = {IMG_ARIN_WALK1, IMG_ARIN_WALK2, IMG_ARIN_WALK3, IMG_ARIN_WALK4};

    /** Frames per second for the walk animation cycle. */
    private static final double WALK_FPS = 5.0;

    private static final Image IMG_PORTAL_TREE = loadImage("/assets/cutscene/environment/portal_tree.png");
    private static final Image IMG_PORTAL_ENERGY = loadImage("/assets/cutscene/effects/portal_energy.png");

    private final OpeningCutscene cutscene;
    private final Canvas canvas = new Canvas(GameConfig.VIEWPORT_WIDTH, GameConfig.VIEWPORT_HEIGHT);

    public CutsceneScreen(OpeningCutscene cutscene) {
        this.cutscene = cutscene;
        setPrefSize(GameConfig.VIEWPORT_WIDTH, GameConfig.VIEWPORT_HEIGHT);
        canvas.setManaged(false);
        getChildren().add(canvas);
        render();
    }

    @Override
    protected void layoutChildren() {
        super.layoutChildren();
        if (canvas.getWidth() != getWidth() || canvas.getHeight() != getHeight()) {
            canvas.setWidth(getWidth());
            canvas.setHeight(getHeight());
            render();
        }
    }

    /** Paints the active scene; called each frame by the shared game loop. */
    public void render() {
        if (canvas.getWidth() <= 0 || canvas.getHeight() <= 0) return;
        GraphicsContext g = canvas.getGraphicsContext2D();

        // Letterbox black borders
        g.setFill(Color.BLACK);
        g.fillRect(0, 0, canvas.getWidth(), canvas.getHeight());

        double scale = Math.min(canvas.getWidth() / DESIGN_WIDTH, canvas.getHeight() / DESIGN_HEIGHT);
        g.save();
        try {
            g.translate((canvas.getWidth() - DESIGN_WIDTH * scale) / 2,
                    (canvas.getHeight() - DESIGN_HEIGHT * scale) / 2);
            g.scale(scale, scale);

            OpeningScene scene = cutscene.scene();
            double progress = cutscene.progress();

            switch (scene) {
                case EXPLORATION -> renderExploration(g, progress);
                case DISCOVERY -> renderDiscovery(g, progress);
                case ENTERING -> renderEntering(g, progress);
                case ARRIVAL -> renderArrival(g, progress);
                case MONTAGE -> renderMontage(g, progress);
                case MISSION -> renderMission(g, progress);
                case GAMEPLAY -> renderGameplayTransition(g, progress);
            }

            // Cross-fade overlay driven by cutscene timing
            double fade = cutscene.fadeOpacity();
            if (fade > 0) {
                g.setFill(Color.color(0, 0, 0, Math.min(1.0, fade)));
                g.fillRect(0, 0, DESIGN_WIDTH, DESIGN_HEIGHT);
            }

            // Cinematic letterbox narration overlay
            int montageDay = (scene == OpeningScene.MONTAGE) ? Math.min(2, (int) (progress * 3)) : -1;
            renderOverlay(g, scene, montageDay);

        } finally {
            g.restore();
        }
    }

    // =========================================================================
    // SCENE 1: EXPLORATION (7s) — Warm morning sunlight, peaceful woodland path
    // =========================================================================
    private void renderExploration(GraphicsContext g, double progress) {
        // Camera pan: slow gentle tracking across widescreen forest
        double panX = -progress * 130;
        drawImage(g, IMG_BG_PEACEFUL, panX, 0, 1090, DESIGN_HEIGHT);

        // Warm golden sunbeams filtering down through canopy
        renderGodRays(g, 0.45, 0.12);

        // Drifting golden pollen / sun dust particles
        renderPollenMotes(g, progress, 25);

        // Distant flying birds in morning sky
        renderDistantBirds(g, progress);

        // Arin: walks along the sunlit woodland trail, then pauses to notice something
        double arinX;
        Image arinSprite;
        double bob = 0;
        if (progress < 0.65) {
            // Natural walking phase — 4-frame cycle
            double walkProg = progress / 0.65;
            arinX = 90 + walkProg * 260;
            double walkPhase = progress * 7.0 * WALK_FPS; // time-based phase
            int frameIdx = ((int) walkPhase) % WALK_CYCLE.length;
            arinSprite = walkFrame(frameIdx);
            // Vertical bob: highest at passing poses (frames 1,3), lowest at contacts (0,2)
            double cyclePos = (walkPhase % WALK_CYCLE.length) / WALK_CYCLE.length;
            bob = Math.abs(Math.sin(cyclePos * Math.PI * 2)) * 3.0;
        } else {
            // Stops and looks curious toward the hidden path ahead
            arinX = 350;
            arinSprite = IMG_ARIN_LOOK != null ? IMG_ARIN_LOOK : IMG_ARIN_STAND;
        }
        drawArin(g, arinSprite, arinX, GROUND_Y, 148, 1.0, bob);
    }

    // =========================================================================
    // SCENE 2: DISCOVERY (7s) — Giant ancient tree trunk revealed, portal awakening
    // =========================================================================
    private void renderDiscovery(GraphicsContext g, double progress) {
        // Camera slow push-in toward the ancient tree
        double zoom = 1.0 + progress * 0.05;
        g.save();
        g.translate(DESIGN_WIDTH * 0.5 * (1 - zoom), DESIGN_HEIGHT * 0.5 * (1 - zoom));
        g.scale(zoom, zoom);

        // Background forest
        drawImage(g, IMG_BG_PEACEFUL, -90, 0, 1050, DESIGN_HEIGHT);

        // Giant Ancient Tree Trunk dominating the right side of the screen
        double treeX = 510;
        double treeY = -10;
        double treeW = 460;
        double treeH = 610;
        drawImage(g, IMG_PORTAL_TREE, treeX, treeY, treeW, treeH);

        // Supernatural tear awakening inside the tree trunk
        double energy = Math.min(1.0, 0.25 + progress * 0.75);
        double tearX = treeX + 130;
        double tearY = treeY + 165;
        double tearW = 185;
        double tearH = 295;
        renderPortalEnergy(g, tearX, tearY, tearW, tearH, energy, progress, false);

        // Ambient glow illuminating nearby bark and grass
        RadialGradient barkGlow = new RadialGradient(0, 0, tearX + tearW / 2, tearY + tearH / 2, 220, false,
                CycleMethod.NO_CYCLE,
                new Stop(0, Color.color(0.2, 0.85, 0.8, 0.28 * energy)),
                new Stop(1, Color.TRANSPARENT));
        g.setFill(barkGlow);
        g.fillOval(tearX - 40, tearY - 30, tearW + 80, tearH + 60);

        // Arin acting: enters from left, startled/reacting when tear awakens, then approaches with wonder
        double arinX;
        Image arinSprite;
        double bob = 0;
        if (progress < 0.40) {
            double p = progress / 0.40;
            arinX = 140 + p * 130;
            double walkPhase = progress * 7.0 * WALK_FPS;
            int frameIdx = ((int) walkPhase) % WALK_CYCLE.length;
            arinSprite = walkFrame(frameIdx);
            double cyclePos = (walkPhase % WALK_CYCLE.length) / WALK_CYCLE.length;
            bob = Math.abs(Math.sin(cyclePos * Math.PI * 2)) * 3.0;
        } else if (progress < 0.70) {
            // Stops and reacts in surprise/alertness
            arinX = 270;
            arinSprite = IMG_ARIN_REACT != null ? IMG_ARIN_REACT : IMG_ARIN_STAND;
        } else {
            // Drawn forward with curiosity and wonder
            arinX = 270;
            arinSprite = IMG_ARIN_LOOK != null ? IMG_ARIN_LOOK : IMG_ARIN_STAND;
        }
        drawArin(g, arinSprite, arinX, GROUND_Y, 148, 1.0, bob);

        g.restore();
    }

    // =========================================================================
    // SCENE 3: ENTERING (6s) — Dimensional energy surges, Arin steps into portal
    // =========================================================================
    private void renderEntering(GraphicsContext g, double progress) {
        // Dramatic push-in toward the active portal
        double zoom = 1.03 + progress * 0.08;
        g.save();
        g.translate(DESIGN_WIDTH * 0.6 * (1 - zoom), DESIGN_HEIGHT * 0.5 * (1 - zoom));
        g.scale(zoom, zoom);

        // Background forest, darkened as portal radiance dominates
        drawImage(g, IMG_BG_PEACEFUL, -90, 0, 1050, DESIGN_HEIGHT);
        g.setFill(Color.color(0.01, 0.05, 0.08, 0.35 + progress * 0.25));
        g.fillRect(0, 0, DESIGN_WIDTH, DESIGN_HEIGHT);

        // Giant ancient tree
        double treeX = 490;
        double treeY = -10;
        double treeW = 480;
        double treeH = 610;
        drawImage(g, IMG_PORTAL_TREE, treeX, treeY, treeW, treeH);

        // Active pulsating volumetric dimensional energy
        double pulse = 0.94 + 0.12 * Math.sin(progress * 16);
        double energy = 1.0 + progress * 0.25;
        double tearX = treeX + 130;
        double tearY = treeY + 160;
        double tearW = 195 * pulse;
        double tearH = 310 * pulse;
        renderPortalEnergy(g, tearX, tearY, tearW, tearH, energy, progress, false);

        // Radiant light rays cutting through woodland shadows
        renderEnergyRays(g, tearX + tearW / 2, tearY + tearH / 2, tearH * 0.6, energy);

        // Arin steps cautiously into the light
        double targetX = tearX + 35;
        double arinX;
        Image arinSprite;
        double arinAlpha = 1.0;
        double bob = 0;

        if (progress < 0.55) {
            double p = progress / 0.55;
            arinX = 270 + p * (targetX - 270) * 0.75;
            double walkPhase = progress * 6.0 * WALK_FPS;
            int frameIdx = ((int) walkPhase) % WALK_CYCLE.length;
            arinSprite = walkFrame(frameIdx);
            double cyclePos = (walkPhase % WALK_CYCLE.length) / WALK_CYCLE.length;
            bob = Math.abs(Math.sin(cyclePos * Math.PI * 2)) * 2.5;
        } else if (progress < 0.75) {
            double p = (progress - 0.55) / 0.20;
            arinX = 270 + 0.75 * (targetX - 270) + p * (targetX - 270) * 0.25;
            arinSprite = IMG_ARIN_CAUTIOUS != null ? IMG_ARIN_CAUTIOUS : IMG_ARIN_STAND;
            // Arin begins to be engulfed in light
            arinAlpha = 1.0 - p * 0.6;
        } else {
            // Fully dissolved into the dimensional gateway
            arinX = targetX;
            arinSprite = IMG_ARIN_STAND;
            arinAlpha = Math.max(0, 0.4 - (progress - 0.75) / 0.25);
        }
        drawArin(g, arinSprite, arinX, GROUND_Y, 148, arinAlpha, bob);

        // Blinding dimensional flash flooding the screen at the climax
        if (progress > 0.65) {
            double flash = Math.min(1.0, (progress - 0.65) / 0.35);
            g.setFill(Color.color(0.85, 1.0, 0.96, flash * 0.95));
            g.fillRect(0, 0, DESIGN_WIDTH, DESIGN_HEIGHT);
        }

        g.restore();
    }

    // =========================================================================
    // SCENE 4: ARRIVAL IN ELDORIA (7s) — Strange turquoise mist, gnarled trees
    // =========================================================================
    private void renderArrival(GraphicsContext g, double progress) {
        // Slow downward camera tilt from alien gnarled canopy to mossy ground
        double panY = (1.0 - progress) * -35;
        drawImage(g, IMG_BG_ELDORIA, -30, panY, DESIGN_WIDTH + 60, DESIGN_HEIGHT + 45);

        // Drifting atmospheric turquoise mist layers
        renderTurquoiseMist(g, progress);

        // Floating bioluminescent fireflies / spores
        renderFireflies(g, progress, 28);

        // Arin acting: arrives stumbled on the ground, slowly rises, looks around in awe
        Image arinSprite;
        double arinX = 360;
        double arinY = GROUND_Y;
        double arinH = 148;
        if (progress < 0.35) {
            // Stumbled / kneeling on mossy ground recovering
            arinSprite = IMG_ARIN_KNEEL != null ? IMG_ARIN_KNEEL : IMG_ARIN_STAND;
            arinH = 112;
            arinY = GROUND_Y + 12;
        } else if (progress < 0.65) {
            // Standing upright, steadying himself
            arinSprite = IMG_ARIN_STAND;
        } else {
            // Observing strange surroundings with apprehension and curiosity
            arinSprite = IMG_ARIN_LOOK != null ? IMG_ARIN_LOOK : IMG_ARIN_STAND;
        }
        drawArin(g, arinSprite, arinX, arinY, arinH, 1.0, 0);
    }

    // =========================================================================
    // SCENE 5: MONTAGE (12s) — 3 Days across Eldoria (Lost Forest, Ruins, Spire)
    // =========================================================================
    private void renderMontage(GraphicsContext g, double progress) {
        // 3 chapters: 0.0-0.33 Day 1, 0.33-0.66 Day 2, 0.66-1.0 Day 3
        int day = Math.min(2, (int) (progress * 3));
        double dayProgress = (progress * 3) % 1.0;

        Image bg = switch (day) {
            case 0 -> IMG_BG_MONTAGE1 != null ? IMG_BG_MONTAGE1 : IMG_BG_ELDORIA;
            case 1 -> IMG_BG_MONTAGE2 != null ? IMG_BG_MONTAGE2 : IMG_BG_ELDORIA;
            default -> IMG_BG_MONTAGE3 != null ? IMG_BG_MONTAGE3 : IMG_BG_ELDORIA;
        };

        // Subtle camera drift per location
        double panX = -dayProgress * 45;
        drawImage(g, bg, panX, 0, DESIGN_WIDTH + 55, DESIGN_HEIGHT);

        // Location specific atmospheric details
        if (day == 0) {
            // Day 1: dense dark forest fog and fireflies
            renderTurquoiseMist(g, progress);
            renderFireflies(g, progress, 18);
            // Arin walks tiredly across tangled roots
            double arinX = 180 + dayProgress * 280;
            double walkPhase = dayProgress * 4.0 * WALK_FPS;
            int frameIdx = ((int) walkPhase) % WALK_CYCLE.length;
            Image sprite = walkFrame(frameIdx);
            double cyclePos = (walkPhase % WALK_CYCLE.length) / WALK_CYCLE.length;
            drawArin(g, sprite, arinX, GROUND_Y, 145, 1.0, Math.abs(Math.sin(cyclePos * Math.PI * 2)) * 3);

        } else if (day == 1) {
            // Day 2: ancient stone ruins with purple-teal haze
            renderTurquoiseMist(g, progress * 0.7);
            // Arin explores crumbling pillars, stops to inspect ruins
            double arinX;
            Image sprite;
            double bob = 0;
            if (dayProgress < 0.6) {
                arinX = 220 + (dayProgress / 0.6) * 160;
                double walkPhase = dayProgress * 4.0 * WALK_FPS;
                int frameIdx = ((int) walkPhase) % WALK_CYCLE.length;
                sprite = walkFrame(frameIdx);
                double cyclePos = (walkPhase % WALK_CYCLE.length) / WALK_CYCLE.length;
                bob = Math.abs(Math.sin(cyclePos * Math.PI * 2)) * 2.5;
            } else {
                arinX = 380;
                sprite = IMG_ARIN_LOOK != null ? IMG_ARIN_LOOK : IMG_ARIN_STAND;
            }
            drawArin(g, sprite, arinX, GROUND_Y, 145, 1.0, bob);

        } else {
            // Day 3: jagged dead crags near the shadow frontier
            // Glowing crimson pulse in upper sky
            double pulse = 0.5 + 0.5 * Math.sin(progress * 12);
            g.setFill(new RadialGradient(0, 0, DESIGN_WIDTH * 0.75, 120, 320, false, CycleMethod.NO_CYCLE,
                    new Stop(0, Color.color(0.55, 0.08, 0.15, 0.22 * pulse)),
                    new Stop(1, Color.TRANSPARENT)));
            g.fillRect(0, 0, DESIGN_WIDTH, DESIGN_HEIGHT);

            // Red glowing eyes in dark brush hinting at shadow enemy scouts
            renderScoutEyes(g, 120, 480, progress);
            renderScoutEyes(g, 810, 465, progress + 0.3);

            // Arin stands vigilant, cautious posture
            drawArin(g, IMG_ARIN_CAUTIOUS != null ? IMG_ARIN_CAUTIOUS : IMG_ARIN_STAND,
                    360, GROUND_Y, 145, 1.0, 0);
        }

        // Dip-to-black cuts between days to communicate passage of time
        double cut = Math.min(dayProgress, 1.0 - dayProgress) * 7.0;
        if (cut < 1.0) {
            g.setFill(Color.color(0, 0, 0, Math.max(0, 1.0 - cut)));
            g.fillRect(0, 0, DESIGN_WIDTH, DESIGN_HEIGHT);
        }
    }

    // =========================================================================
    // SCENE 6: MISSION (12s) — Dormant portal discovered; realization of quest
    // =========================================================================
    private void renderMission(GraphicsContext g, double progress) {
        // Dramatic framing: dark desaturated Eldoria woodland
        drawImage(g, IMG_BG_ELDORIA, -30, 0, DESIGN_WIDTH + 60, DESIGN_HEIGHT);
        g.setFill(Color.color(0.02, 0.04, 0.06, 0.25));
        g.fillRect(0, 0, DESIGN_WIDTH, DESIGN_HEIGHT);

        // Dormant ancient tree portal on the right (cold, dead, dim energy)
        double treeX = 510;
        double treeY = -10;
        double treeW = 460;
        double treeH = 610;
        drawImage(g, IMG_PORTAL_TREE, treeX, treeY, treeW, treeH);

        // Dark cold dormant overlay on portal tree
        g.setFill(Color.color(0.04, 0.06, 0.1, 0.45));
        g.fillRect(treeX + 80, treeY + 120, 260, 360);

        // Faint dying purple/cold embers in the dead tear
        double tearX = treeX + 130;
        double tearY = treeY + 165;
        double tearW = 185;
        double tearH = 295;
        renderPortalEnergy(g, tearX, tearY, tearW, tearH, 0.08, progress, true);

        // Subtle dark corruption pulse from the Shadow Lord's grip
        double pulse = 0.5 + 0.5 * Math.sin(progress * 6);
        g.setFill(new RadialGradient(0, 0, DESIGN_WIDTH * 0.5, 0, 480, false, CycleMethod.NO_CYCLE,
                new Stop(0, Color.color(0.3, 0.05, 0.4, 0.15 * pulse)),
                new Stop(1, Color.TRANSPARENT)));
        g.fillRect(0, 0, DESIGN_WIDTH, DESIGN_HEIGHT);

        // Arin character acting:
        // First half: stares at the dead portal in despair (stand pose with dark rim)
        // Second half: straightens up, determined expression (react pose facing right)
        Image arinSprite;
        double arinX = 330;
        if (progress < 0.5) {
            arinSprite = IMG_ARIN_STAND;
        } else {
            arinSprite = IMG_ARIN_REACT != null ? IMG_ARIN_REACT : IMG_ARIN_STAND;
        }
        drawArin(g, arinSprite, arinX, GROUND_Y, 148, 1.0, 0);
    }

    // =========================================================================
    // SCENE 7: GAMEPLAY TRANSITION (4s) — Seamless bridge to Mystic Forest
    // =========================================================================
    private void renderGameplayTransition(GraphicsContext g, double progress) {
        // Camera smooth zoom into the clearing matching gameplay perspective
        double zoom = 1.0 + progress * 0.15;
        g.save();
        g.translate(DESIGN_WIDTH * 0.4 * (1 - zoom), DESIGN_HEIGHT * 0.5 * (1 - zoom));
        g.scale(zoom, zoom);

        // Eldoria forest matching Mystic Forest dark teal palette
        drawImage(g, IMG_BG_ELDORIA, -30, 0, DESIGN_WIDTH + 60, DESIGN_HEIGHT);

        // Arin standing ready at the trailhead
        drawArin(g, IMG_ARIN_STAND, 360, GROUND_Y, 148, 1.0, 0);

        renderTurquoiseMist(g, progress);

        g.restore();

        // Subtle gameplay prompt banner
        g.setFill(Color.color(0.04, 0.12, 0.14, 0.65 * progress));
        g.fillRoundRect(DESIGN_WIDTH / 2 - 180, 220, 360, 48, 12, 12);
        g.setStroke(Color.color(0.4, 0.85, 0.8, 0.5 * progress));
        g.setLineWidth(1.5);
        g.strokeRoundRect(DESIGN_WIDTH / 2 - 180, 220, 360, 48, 12, 12);
        g.setFill(Color.web("#e0f7f5"));
        g.setFont(Font.font("System", FontWeight.BOLD, 18));
        g.setTextAlign(TextAlignment.CENTER);
        g.fillText("Entering Mystic Forest...", DESIGN_WIDTH / 2, 251);
    }

    // =========================================================================
    // ATMOSPHERIC AND VISUAL EFFECT HELPERS
    // =========================================================================

    /** Renders golden sunbeams filtering through the foliage. */
    private void renderGodRays(GraphicsContext g, double spread, double alpha) {
        g.save();
        for (int i = 0; i < 4; i++) {
            double x1 = 120 + i * 210;
            double x2 = x1 - 140;
            double x3 = x1 + 180;
            g.setFill(new LinearGradient(x1, 0, x2, DESIGN_HEIGHT, false, CycleMethod.NO_CYCLE,
                    new Stop(0, Color.color(1.0, 0.96, 0.75, alpha * 1.5)),
                    new Stop(1, Color.color(1.0, 0.96, 0.75, 0))));
            g.fillPolygon(new double[]{x1 - 30, x1 + 45, x3, x2},
                    new double[]{0, 0, DESIGN_HEIGHT, DESIGN_HEIGHT}, 4);
        }
        g.restore();
    }

    /** Drifting golden pollen particles. */
    private void renderPollenMotes(GraphicsContext g, double progress, int count) {
        g.save();
        for (int i = 0; i < count; i++) {
            double t = (progress * 0.4 + i * 0.04) % 1.0;
            double px = ((i * 137) % 920) + Math.sin(t * 6 + i) * 20;
            double py = 120 + ((i * 79) % 360) - t * 40;
            double sz = 2.0 + (i % 3);
            g.setFill(Color.color(1.0, 0.95, 0.7, 0.5 + 0.3 * Math.sin(t * 8)));
            g.fillOval(px, py, sz, sz);
        }
        g.restore();
    }

    /** Distant flying birds in sunlit sky. */
    private void renderDistantBirds(GraphicsContext g, double progress) {
        g.save();
        g.setStroke(Color.color(0.2, 0.28, 0.2, 0.45));
        g.setLineWidth(1.8);
        for (int i = 0; i < 3; i++) {
            double bx = 650 + (i * 45) + progress * 60;
            double by = 110 + (i * 22) + Math.sin(progress * 8 + i) * 6;
            g.beginPath();
            g.moveTo(bx - 7, by + 4);
            g.quadraticCurveTo(bx - 3, by - 2, bx, by + 2);
            g.quadraticCurveTo(bx + 3, by - 2, bx + 7, by + 4);
            g.stroke();
        }
        g.restore();
    }

    /** Volumetric glowing dimensional portal energy. */
    private void renderPortalEnergy(GraphicsContext g, double x, double y, double w, double h,
                                    double energy, double progress, boolean dormant) {
        if (dormant) {
            // Cold, dying purple-blue embers
            RadialGradient deadGlow = new RadialGradient(0, 0, x + w / 2, y + h / 2, w * 0.6, false,
                    CycleMethod.NO_CYCLE,
                    new Stop(0, Color.color(0.3, 0.15, 0.45, 0.25)),
                    new Stop(1, Color.TRANSPARENT));
            g.setFill(deadGlow);
            g.fillOval(x, y, w, h);
            return;
        }

        // Active dimensional energy texture
        if (IMG_PORTAL_ENERGY != null) {
            g.save();
            g.setGlobalAlpha(Math.min(1.0, energy * 0.95));
            drawImage(g, IMG_PORTAL_ENERGY, x, y, w, h);
            g.restore();
        }

        // Shimmering dimensional sparks rising along the tear
        for (int i = 0; i < 14; i++) {
            double t = (progress * 0.35 + i * 0.07) % 1.0;
            double py = y + h * 0.85 - t * (h * 0.75);
            double px = x + w * 0.5 + Math.sin(t * 7 + i) * (w * 0.18);
            double sz = 2.5 + (i % 3);
            g.setFill(Color.color(0.7, 1.0, 0.95, energy * (0.8 - t * 0.4)));
            g.fillOval(px - sz / 2, py - sz / 2, sz, sz);
        }
    }

    /** Outward dimensional light rays streaming from the portal tear. */
    private void renderEnergyRays(GraphicsContext g, double cx, double cy, double length, double energy) {
        g.save();
        for (int i = 0; i < 6; i++) {
            double angle = Math.toRadians(-120 + i * 28 + Math.sin(energy * 4 + i) * 4);
            double len = length * (1.2 + 0.2 * Math.sin(energy * 6 + i));
            double x2 = cx + Math.cos(angle) * len;
            double y2 = cy + Math.sin(angle) * len;
            g.setFill(new LinearGradient(cx, cy, x2, y2, false, CycleMethod.NO_CYCLE,
                    new Stop(0, Color.color(0.65, 1.0, 0.95, 0.22 * energy)),
                    new Stop(1, Color.TRANSPARENT)));
            double perpX = -Math.sin(angle) * 18;
            double perpY = Math.cos(angle) * 18;
            g.fillPolygon(new double[]{cx, x2 - perpX, x2 + perpX},
                    new double[]{cy, y2 - perpY, y2 + perpY}, 3);
        }
        g.restore();
    }

    /** Drifting turquoise mist layers in Eldoria. */
    private void renderTurquoiseMist(GraphicsContext g, double progress) {
        g.save();
        double drift1 = (progress * 35) % DESIGN_WIDTH;
        double drift2 = (progress * 22) % DESIGN_WIDTH;

        g.setFill(new RadialGradient(0, 0, DESIGN_WIDTH * 0.5, GROUND_Y - 20, 480, false, CycleMethod.NO_CYCLE,
                new Stop(0, Color.color(0.12, 0.65, 0.62, 0.18)),
                new Stop(1, Color.TRANSPARENT)));
        g.fillOval(-150 + drift1, GROUND_Y - 80, 850, 140);
        g.fillOval(DESIGN_WIDTH - 650 + drift2, GROUND_Y - 60, 850, 130);
        g.restore();
    }

    /** Bioluminescent fireflies in Eldoria. */
    private void renderFireflies(GraphicsContext g, double progress, int count) {
        g.save();
        for (int i = 0; i < count; i++) {
            double t = (progress * 0.25 + i * 0.05) % 1.0;
            double px = ((i * 179 + (int) (progress * 40)) % 920) + Math.sin(t * 5 + i) * 18;
            double py = 180 + ((i * 83) % 270) + Math.cos(t * 4 + i) * 14;
            double sz = 2.5 + (i % 3);
            double glow = 0.5 + 0.4 * Math.sin(t * 10 + i);
            g.setFill(Color.color(0.4, 0.95, 0.88, glow));
            g.fillOval(px, py, sz, sz);
            g.setFill(Color.color(0.3, 0.9, 0.82, glow * 0.3));
            g.fillOval(px - 3, py - 3, sz + 6, sz + 6);
        }
        g.restore();
    }

    /** Hints of shadow scout eyes in the dark forest brush. */
    private void renderScoutEyes(GraphicsContext g, double x, double y, double progress) {
        double blink = Math.sin(progress * 14);
        if (blink < 0.2) return; // Blinking
        g.setFill(Color.color(0.95, 0.18, 0.2, 0.75 * blink));
        g.fillOval(x, y, 4, 3);
        g.fillOval(x + 9, y, 4, 3);
    }

    // =========================================================================
    // CHARACTER AND SPRITE RENDERING
    // =========================================================================

    /**
     * Returns the walk cycle frame at the given index, falling back gracefully
     * if the frame image failed to load.
     */
    private static Image walkFrame(int index) {
        if (index >= 0 && index < WALK_CYCLE.length && WALK_CYCLE[index] != null) {
            return WALK_CYCLE[index];
        }
        // Fallback: use walk1 if available, otherwise stand
        return IMG_ARIN_WALK1 != null ? IMG_ARIN_WALK1 : IMG_ARIN_STAND;
    }

    /** Draws the Arin adventurer sprite with soft ground contact shadow and proper proportions. */
    private static void drawArin(GraphicsContext g, Image sprite, double x, double groundY,
                                double height, double alpha, double bob) {
        if (sprite == null) return;
        g.save();
        g.setGlobalAlpha(alpha);

        double aspect = sprite.getWidth() / sprite.getHeight();
        double w = height * aspect;
        double y = groundY - height - bob;

        // Soft ground contact shadow
        g.setFill(Color.color(0.01, 0.06, 0.07, 0.38 * alpha));
        g.fillOval(x + w * 0.12, groundY - 6, w * 0.76, 12);

        // Character sprite
        g.drawImage(sprite, x, y, w, height);

        g.restore();
    }

    /** Safe image drawing helper that falls back gracefully if image is null. */
    private static void drawImage(GraphicsContext g, Image img, double x, double y, double w, double h) {
        if (img != null) {
            g.drawImage(img, x, y, w, h);
        }
    }

    // =========================================================================
    // CINEMATIC NARRATION AND STORYBOOK OVERLAY
    // =========================================================================

    /** Storybook title bar, narration subtitles, day banners, and skip control. */
    private void renderOverlay(GraphicsContext g, OpeningScene scene, int montageDay) {
        // Top cinematic letterbox bar
        g.setFill(new LinearGradient(0, 0, 0, 56, false, CycleMethod.NO_CYCLE,
                new Stop(0, Color.color(0.01, 0.02, 0.03, 0.92)),
                new Stop(1, Color.color(0.01, 0.02, 0.03, 0.75))));
        g.fillRect(0, 0, DESIGN_WIDTH, 56);

        // Bottom cinematic letterbox bar
        g.setFill(new LinearGradient(0, 478, 0, DESIGN_HEIGHT, false, CycleMethod.NO_CYCLE,
                new Stop(0, Color.color(0.01, 0.02, 0.03, 0.85)),
                new Stop(1, Color.color(0.01, 0.02, 0.03, 0.95))));
        g.fillRect(0, 478, DESIGN_WIDTH, 122);

        // Top left: Scene Title
        g.setTextAlign(TextAlignment.LEFT);
        g.setFill(Color.web("#e5f0ea"));
        g.setFont(Font.font("System", FontWeight.BOLD, 20));
        g.fillText(scene.title(), 32, 35);

        // Top right: Skip Indicator
        g.setFont(Font.font("System", 14));
        g.setTextAlign(TextAlignment.RIGHT);
        g.setFill(Color.web("#a5b8b4"));
        g.fillText("Esc / Space / Enter · Skip opening", 928, 34);

        // Montage Day Banner
        if (scene == OpeningScene.MONTAGE && montageDay >= 0) {
            String dayTitle = switch (montageDay) {
                case 0 -> "Day 1 · The Lost Trails";
                case 1 -> "Day 2 · The Ancient Ruins";
                default -> "Day 3 · The Shadow Frontier";
            };
            g.setTextAlign(TextAlignment.CENTER);
            g.setFont(Font.font("System", FontWeight.BOLD, 23));
            g.setFill(Color.web("#80eae2"));
            g.fillText(dayTitle, DESIGN_WIDTH / 2, 102);
        }

        // Centered Narration Text
        g.setTextAlign(TextAlignment.CENTER);
        g.setFont(Font.font("System", 19));
        g.setFill(Color.web("#f4ede0"));
        String text = cutscene.narration();
        int split = text.length() > 70 ? text.lastIndexOf(' ', 70) : -1;
        g.fillText(split < 0 ? text : text.substring(0, split), DESIGN_WIDTH / 2, 519);
        if (split >= 0) {
            g.fillText(text.substring(split + 1), DESIGN_WIDTH / 2, 547);
        }

        // Bottom Scene Step Counter
        g.setFont(Font.font("System", 12));
        g.setFill(Color.web("#859c99"));
        g.fillText("Opening story · " + (scene.ordinal() + 1) + " / 7", DESIGN_WIDTH / 2, 580);
    }

    // =========================================================================
    // RESOURCE LOADING HELPER
    // =========================================================================

    private static Image loadImage(String path) {
        try (InputStream is = CutsceneScreen.class.getResourceAsStream(path)) {
            if (is != null) {
                return new Image(is);
            }
        } catch (Exception ignored) {
        }
        return null;
    }
}
