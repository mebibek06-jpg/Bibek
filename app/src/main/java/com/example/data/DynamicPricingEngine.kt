package com.example.data

import kotlin.math.roundToInt

object DynamicPricingEngine {
    
    // Default base rates for each category
    val baseRates = mapOf(
        "Electrician" to 1200.0,
        "Plumber" to 1000.0,
        "Mason" to 1500.0,
        "Carpenter" to 1100.0,
        "Painter" to 1400.0,
        "Technician" to 900.0
    )

    data class PricingResult(
        val basePrice: Double,
        val recommendedMin: Double,
        val recommendedMax: Double,
        val recommendedBest: Double,
        val availabilityFactor: Double,
        val urgencyFactor: Double,
        val timeOfDayFactor: Double,
        val demandFactor: Double,
        val historicalAverage: Double?,
        val explanation: String
    )

    fun calculateDynamicPrice(
        category: String,
        urgency: String,
        currentTimeMillis: Long = System.currentTimeMillis(),
        allWorkers: List<User>,
        allBookings: List<Booking>
    ): PricingResult {
        val basePrice = baseRates[category] ?: 1000.0

        // 1. Worker Availability Factor
        // Count how many verified workers have skills containing this category
        val categoryWorkersCount = allWorkers.count { 
            it.isVerified && it.skills.contains(category, ignoreCase = true) 
        }
        val availabilityFactor = when {
            categoryWorkersCount == 0 -> 1.35 // very scarce!
            categoryWorkersCount == 1 -> 1.15
            categoryWorkersCount == 2 -> 0.95
            else -> 0.85 // abundant!
        }

        // 2. Customer Urgency Factor
        val urgencyFactor = when {
            urgency.contains("Urgent", ignoreCase = true) -> 1.3
            urgency.contains("Normal", ignoreCase = true) -> 1.0
            urgency.contains("later", ignoreCase = true) -> 0.85
            else -> 1.0
        }

        // 3. Time of Day Factor (Peak / Off hours)
        val calendar = java.util.Calendar.getInstance()
        calendar.timeInMillis = currentTimeMillis
        val hour = calendar.get(java.util.Calendar.HOUR_OF_DAY)
        val timeOfDayFactor = when {
            hour in 18..22 -> 1.20 // Peak evening hours
            hour >= 22 || hour < 6 -> 1.30 // Off-hours / Late Night emergency
            else -> 1.0
        }

        // 4. Historical Pricing & Demand Factor
        val last24h = currentTimeMillis - (24 * 60 * 60 * 1000L)
        val recentBookings = allBookings.filter { 
            it.category.equals(category, ignoreCase = true) && it.timestamp >= last24h 
        }
        val demandFactor = when {
            recentBookings.size >= 4 -> 1.25 // High demand
            recentBookings.size >= 2 -> 1.12
            else -> 1.0
        }

        // Calculate average agreed price of COMPLETED/CLOSED jobs of this category
        val completedBookingsOfCategory = allBookings.filter { 
            it.category.equals(category, ignoreCase = true) && (it.status == "CLOSED" || it.status == "COMPLETED")
        }
        val historicalAverage = if (completedBookingsOfCategory.isNotEmpty()) {
            completedBookingsOfCategory.map { it.agreedPrice }.average()
        } else {
            null
        }

        // Combine the factors on the baseline
        val baseline = historicalAverage ?: basePrice
        val combinedMultiplier = availabilityFactor * urgencyFactor * timeOfDayFactor * demandFactor
        val recommendedBest = (baseline * combinedMultiplier).roundToNearest50()
        val recommendedMin = (recommendedBest * 0.85).roundToNearest50()
        val recommendedMax = (recommendedBest * 1.15).roundToNearest50()

        val explanation = buildString {
            append("Calculated from base rate model of NRs. ${baseline.toInt()}. ")
            when {
                availabilityFactor > 1.0 -> append("Scarcity of active experts (+${((availabilityFactor - 1.0)*100).toInt()}%). ")
                availabilityFactor < 1.0 -> append("Sufficient active provider pool (-${((1.0 - availabilityFactor)*100).toInt()}%). ")
            }
            if (urgencyFactor > 1.0) append("Urgent dispatcher match (+${((urgencyFactor - 1.0)*100).toInt()}%). ")
            if (timeOfDayFactor > 1.0) append("Off-peak/Late night adjustment (+${((timeOfDayFactor - 1.0)*100).toInt()}%). ")
            if (demandFactor > 1.0) append("Elevated category demand surcharge (+${((demandFactor - 1.0)*100).toInt()}%). ")
        }

        return PricingResult(
            basePrice = basePrice,
            recommendedMin = recommendedMin,
            recommendedMax = recommendedMax,
            recommendedBest = recommendedBest,
            availabilityFactor = availabilityFactor,
            urgencyFactor = urgencyFactor,
            timeOfDayFactor = timeOfDayFactor,
            demandFactor = demandFactor,
            historicalAverage = historicalAverage,
            explanation = explanation
        )
    }

    private fun Double.roundToNearest50(): Double {
        return ((this / 50.0).roundToInt() * 50.0)
    }
}
