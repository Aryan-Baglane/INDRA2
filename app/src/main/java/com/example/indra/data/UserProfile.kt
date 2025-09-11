package com.example.indra.data

data class UserProfile(
    val uid: String = "",
    val displayName: String? = null,
    val photoUrl: String? = null,
    // Onboarding fields
    val name: String = "",
    val onboardingCompleted: Boolean = false,
    val numDwellers: Int = 0,
    val roofAreaSqm: Double = 0.0,
    val openSpaceSqm: Double = 0.0,
    val roofType: String = "Concrete"
) {
    // Default constructor for Firebase
    constructor() : this(
        uid = "",
        displayName = null,
        photoUrl = null,
        name = "",
        onboardingCompleted = false,
        numDwellers = 0,
        roofAreaSqm = 0.0,
        openSpaceSqm = 0.0,
        roofType = "Concrete"
    )
}