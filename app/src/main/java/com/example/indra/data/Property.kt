package com.example.indra.data

data class Property(
    val id: String = "",
    val name: String = "",
    val address: String = "",
    val latitude: Double = 0.0,
    val longitude: Double = 0.0,
    val feasibilityScore: Float = 0f,
    val annualHarvestingPotentialLiters: Long = 0L,
    val recommendedSolution: String = "",
    val estimatedCostInr: Int = 0,
    val lastAssessmentDate: Long = 0L,
    val propertyType: String = "Residential", // Residential, Commercial, Industrial
    val roofArea: Double = 0.0,
    val openSpace: Double = 0.0,
    val dwellers: Int = 0 // Added missing parameter
) {
    // Default constructor for Firebase
    constructor() : this(
        id = "",
        name = "",
        address = "",
        latitude = 0.0,
        longitude = 0.0,
        feasibilityScore = 0f,
        annualHarvestingPotentialLiters = 0L,
        recommendedSolution = "",
        estimatedCostInr = 0,
        lastAssessmentDate = 0L,
        propertyType = "Residential",
        roofArea = 0.0,
        openSpace = 0.0,
        dwellers = 0
    )
}