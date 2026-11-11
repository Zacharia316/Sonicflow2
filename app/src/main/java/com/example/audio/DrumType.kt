package com.example.audio

/**
 * UI Pad/Category definitions for the DAW instrument pads.
 * Contains no audio synthesis or sound generation logic.
 */
enum class DrumType(val displayName: String, val category: String) {
    KICK("Kick", "Drums"),
    SNARE("Snare", "Drums"),
    HIHAT_CLOSED("Closed Hat", "Drums"),
    HIHAT_OPEN("Open Hat", "Drums"),
    CLAP("Clap", "Drums"),
    RIMSHOT("Rimshot", "Perc"),
    CONGA_HIGH("Conga Hi", "Afro"),
    CONGA_LOW("Conga Low", "Afro"),
    SHAKER("Shaker", "Afro"),
    TOM("Tom", "Perc"),
    VOCAL_CHOP("Vocal Chop", "Vocals"),
    CRASH("Crash", "Cymbal")
}
