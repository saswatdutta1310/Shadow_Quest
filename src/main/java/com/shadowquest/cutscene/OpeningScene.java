package com.shadowquest.cutscene;

/** Approved opening beats, in playback order. Durations are seconds. */
public enum OpeningScene {
    EXPLORATION("A quiet morning", 7,
            "Arin followed a quiet trail beneath the sunlit trees.",
            "Beyond the familiar paths, something caught his eye."),
    DISCOVERY("The hidden gateway", 7,
            "An ancient portal stood hidden among the roots.",
            "As Arin drew closer, its silent stones began to glow."),
    ENTERING("Across the threshold", 6,
            "Arin stepped toward the light.",
            "One step carried him beyond the world he knew."),
    ARRIVAL("Eldoria", 7,
            "The warmth of the forest vanished. Arin had arrived in Eldoria.",
            "Dark trees rose through a strange turquoise mist."),
    MONTAGE("Days without a way home", 12,
            "For several days, Arin searched the unfamiliar realm.",
            "Each new trail led deeper into Eldoria, but never home."),
    MISSION("A way home", 12,
            "Arin was trapped. The Shadow Lord controlled Eldoria's dark forces.",
            "To return home, he must defeat those forces and restore the ancient portals."),
    GAMEPLAY("Mystic Forest", 4,
            "His journey would begin in Mystic Forest.",
            "Find a way forward.");

    private final String title;
    private final double duration;
    private final String firstLine;
    private final String secondLine;

    OpeningScene(String title, double duration, String firstLine, String secondLine) {
        this.title = title;
        this.duration = duration;
        this.firstLine = firstLine;
        this.secondLine = secondLine;
    }

    public String title() { return title; }
    public double duration() { return duration; }
    public String firstLine() { return firstLine; }
    public String secondLine() { return secondLine; }
    /** Half the scene shows the first line, the other half the second. */
    public String narration(double progress) { return progress < .5 ? firstLine : secondLine; }
}
