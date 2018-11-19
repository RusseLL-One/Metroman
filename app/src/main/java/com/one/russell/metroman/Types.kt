package com.one.russell.metroman

enum class BeatType {
    MUTE,
    BEAT,
    SUBACCENT,
    ACCENT;

    companion object {
        val values = values()
    }
}

enum class TrainingType {
    TEMPO_INCREASING_BY_BARS,
    TEMPO_INCREASING_BY_TIME,
    BAR_DROPPING_RANDOM,
    BAR_DROPPING_BY_COUNT,
    BEAT_DROPPING;

    companion object {
        val values = values()
    }
}