package com.example.indra.data




import kotlin.math.roundToInt

// A simplified model for assessment inputs


// A simplified model for assessment inputs
data class AssessmentInput(
    val location: String,
    val name: String,
    val dwellers: Int,
    val roofArea: Double,
    val openSpace: Double,
    val roofType: String // e.g., "Concrete", "Tiled"
)

// This object contains the core business logic for the app.
// In a real app, this would be much more complex, involving API calls to fetch
// rainfall data, groundwater levels, etc.
// This object contains the core business logic for the app.
// In a real app, this would be much more complex, involving API calls to fetch
// rainfall data, groundwater levels, etc.
object FeasibilityCalculator {

    private const val AVG_ANNUAL_RAINFALL_MM = 680 // Average for demonstration
    private val RUNOFF_COEFFICIENT = mapOf("Concrete" to 0.85, "Tiled" to 0.80)

    fun calculate(input: AssessmentInput): Report {
        // --- 1. Calculate Annual Harvesting Potential ---
        // Use the fields from the updated input object
        val roofArea = input.roofArea
        val rainfallInMeters = AVG_ANNUAL_RAINFALL_MM / 1000.0
        val coefficient = RUNOFF_COEFFICIENT[input.roofType] ?: 0.80

        // Volume (Cubic Meters) = Area * Rainfall * Coefficient
        val harvestableVolumeCubicMeters = roofArea * rainfallInMeters * coefficient
        val harvestableVolumeLiters = (harvestableVolumeCubicMeters * 1000).toLong()

        // --- 2. Determine Feasibility Score ---
        // Simplified scoring based on inputs
        var score = 0
        if (harvestableVolumeLiters > 20000) score += 40 // Good volume
        if (input.openSpace > 10) score += 30 // Space for a pit
        if (input.roofArea > 50) score += 20 // Decent roof size
        score += (0..10).random() // Random factor for variability

        // --- 3. Recommend a Solution ---
        val solution = if (input.openSpace > 15) {
            "Recharge Pit"
        } else {
            "Storage Tank"
        }

        // --- 4. Estimate Cost ---
        val cost = if (solution == "Recharge Pit") {
            (input.openSpace * 800).roundToInt() + 10000 // Base cost + area based cost
        } else {
            (harvestableVolumeLiters / 10).toInt().coerceAtLeast(15000) // Cost based on tank size
        }

        return Report(
            location = input.location,
            timestamp = 1693574400000L,
            feasibilityScore = score.coerceIn(0, 100).toFloat(),
            annualHarvestingPotentialLiters = harvestableVolumeLiters,
            recommendedSolution = solution,
            estimatedCostInr = cost,
            name = input.name, // Now correctly referenced
            dwellers = input.dwellers, // Now correctly referenced
            roofArea = input.roofArea, // Now correctly referenced
            openSpace = input.openSpace // Now correctly referenced
        )
    }
}