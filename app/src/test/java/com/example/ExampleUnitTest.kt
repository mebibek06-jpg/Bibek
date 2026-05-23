package com.example

import com.example.data.*
import org.junit.Assert.*
import org.junit.Test

/**
 * Platform core unit tests verifying AI dynamic pricing intelligence and Elite Badge operations.
 */
class ExampleUnitTest {
  @Test
  fun addition_isCorrect() {
    assertEquals(4, 2 + 2)
  }

  @Test
  fun test_dynamic_pricing_calculation() {
    val workers = listOf(
        User(
            id = "worker_ram",
            name = "Ram Bahadur",
            phone = "9841123456",
            role = "WORKER",
            skills = "Electrician",
            rating = 4.8f,
            completedJobsCount = 10,
            isVerified = true
        )
    )
    val bookings = emptyList<Booking>()

    // Plumber category with Normal Urgency during daytime
    val plumberNormal = DynamicPricingEngine.calculateDynamicPrice(
        category = "Plumber",
        urgency = "Normal (Today)",
        currentTimeMillis = 1715000000000, // Daytime base
        allWorkers = workers,
        allBookings = bookings
    )

    // Base plumber is 1000 NRs
    assertTrue(plumberNormal.recommendedBest >= 1000.0)
    assertTrue(plumberNormal.recommendedMax > plumberNormal.recommendedMin)
    assertTrue(plumberNormal.explanation.contains("experts") || plumberNormal.explanation.contains("Scarcity"))

    // Electrician emergency at night
    val electricianNightEmergency = DynamicPricingEngine.calculateDynamicPrice(
        category = "Electrician",
        urgency = "Urgent (Need within 1 hour)",
        currentTimeMillis = 1715000000000 + (22 * 3600000), // 10 PM Night Hour
        allWorkers = workers,
        allBookings = bookings
    )

    // Emergency Electrician on off hours must surge above base (Electrician base = 1200)
    assertTrue(electricianNightEmergency.recommendedBest > 1200.0)
    assertTrue(electricianNightEmergency.explanation.contains("night") || electricianNightEmergency.explanation.contains("Urgent"))
  }

  @Test
  fun test_verified_badge_conditions() {
    val eliteWorker = User(
        id = "w1",
        name = "Elite Pro",
        phone = "9841000001",
        role = "WORKER",
        skills = "Electrician",
        rating = 4.8f,
        completedJobsCount = 52,
        isVerified = true,
        verificationRequestStatus = "VERIFIED"
    )

    val belowJobsCountWorker = User(
        id = "w2",
        name = "New Worker But Good",
        phone = "9841000002",
        role = "WORKER",
        skills = "Electrician",
        rating = 4.9f,
        completedJobsCount = 10, // Under 50 target jobs!
        isVerified = true,
        verificationRequestStatus = "VERIFIED"
    )

    val lowRatingWorker = User(
        id = "w3",
        name = "Busy But Rated Low",
        phone = "9841000003",
        role = "WORKER",
        skills = "Electrician",
        rating = 4.2f, // Under 4.5 star minimum rating!
        completedJobsCount = 80,
        isVerified = true,
        verificationRequestStatus = "VERIFIED"
    )

    val allBookings = emptyList<Booking>()

    // Run badge assertion checks
    assertTrue(eliteWorker.hasVerifiedBadge(allBookings))
    assertFalse(belowJobsCountWorker.hasVerifiedBadge(allBookings))
    assertFalse(lowRatingWorker.hasVerifiedBadge(allBookings))
  }
}
