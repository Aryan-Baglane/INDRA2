package com.example.indra.data

data class RainwaterStats(
    val currentRainfall: Float = 0f,
    val waterCollectedToday: Float = 0f,
    val tankLevel: Float = 25f,              // rooftop tank %
    val undergroundTankLevel: Float = 40f,   // underground tank %
    val waterSavedThisMonth: Float = 1250f,
    val isRaining: Boolean = false,
    val consumptionRate: Float = 0.5f
)
