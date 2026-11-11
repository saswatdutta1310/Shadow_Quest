package com.shadowquest.core;

/** The finite set of session states; victory rules are a later milestone. */
public enum GameState {
    MENU,
    /** The opening cinematic is playing; no player exists yet. */
    CUTSCENE,
    PLAYING,
    PAUSED,
    GAME_OVER,
    VICTORY
}
