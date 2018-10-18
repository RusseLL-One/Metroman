package com.one.russell.metroman

enum class BeatType {
    BEAT,
    SUBACCENT,
    ACCENT,
    MUTE
}

enum class TrainingType {
    TEMPO_INCREASING_BY_BARS,
    TEMPO_INCREASING_BY_TIME,
    BAR_DROPPING_RANDOM,
    BAR_DROPPING_BY_COUNT,
    BEAT_DROPPING
}