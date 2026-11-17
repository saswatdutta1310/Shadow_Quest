package com.shadowquest.audio;

/**
 * Standard background music tracks for Shadow Quest.
 */
public enum AudioTrack {
    MENU("/assets/audio/music/menu.wav"),
    CUTSCENE("/assets/audio/music/cutscene.wav"),
    FOREST("/assets/audio/music/forest.wav"),
    CASTLE("/assets/audio/music/castle.wav"),
    BOSS("/assets/audio/music/boss.wav"),
    VICTORY("/assets/audio/music/victory.wav"),
    GAME_OVER("/assets/audio/music/game_over.wav");

    private final String resourcePath;

    AudioTrack(String resourcePath) {
        this.resourcePath = resourcePath;
    }

    public String getResourcePath() {
        return resourcePath;
    }
}
