package com.example.data

import androidx.room.Dao
import androidx.room.Database
import androidx.room.Entity
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.PrimaryKey
import androidx.room.Query
import androidx.room.RoomDatabase
import androidx.room.TypeConverter
import kotlinx.coroutines.flow.Flow

// --- 1. USER ENTITY ---
@Entity(tableName = "users")
data class User(
    @PrimaryKey val id: String, // e.g. "customer_001" or "worker_001"
    val name: String,
    val phone: String,
    val role: String, // "CUSTOMER", "WORKER", "ADMIN"
    val isVerified: Boolean = false,
    val verificationRequestStatus: String = "NOT_STARTED", // "NOT_STARTED", "PENDING", "VERIFIED", "REJECTED"
    val rating: Float = 5.0f,
    val completedJobsCount: Int = 0,
    val walletBalance: Double = 0.0,
    val skills: String = "", // Comma-separated or serialized
    val profileImage: String = "", // Base64 or local label
    val deviceBound: Boolean = true,
    val selfieVerified: Boolean = false
)

// --- 2. BOOKING / JOB ENTITY ---
@Entity(tableName = "bookings")
data class Booking(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val category: String, // "Electrician", "Plumber", "Mason", "Painter", "Carpenter", "Technician"
    val customerId: String,
    val customerName: String,
    val workerId: String? = null,
    val workerName: String? = null,
    val title: String,
    val description: String,
    val locationAddress: String,
    val latitude: Double,
    val longitude: Double,
    val urgency: String, // "Urgent (Need within 1 hour)", "Normal (Today)", "Schedule later"
    val pricingMode: String, // "Fixed Price", "Bidding Mode"
    val initialBudget: Double,
    val agreedPrice: Double = 0.0,
    val commissionAmount: Double = 0.0,
    val escrowStatus: String, // "PENDING", "LOCKED_IN_ESCROW", "RELEASED", "REFUNDED", "DISPUTED"
    val escrowProvider: String = "", // "eSewa", "Khalti", "Fonepay"
    val status: String, // "REQUESTED", "ACCEPTED", "ARRIVED", "STARTED", "COMPLETED", "CLOSED", "DISPUTED", "CANCELLED"
    val verificationOtp: String = "", // Escrow Job Completion OTP
    val userOtpAttempt: String = "",
    val ratingScore: Int = 0,
    val reviewText: String = "",
    val timestamp: Long = System.currentTimeMillis()
)

// --- 3. MESSAGE ENTITY (CHAT SYSTEM) ---
@Entity(tableName = "messages")
data class Message(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val bookingId: Long,
    val senderId: String,
    val senderName: String,
    val messageText: String,
    val isSystemMessage: Boolean = false,
    val timestamp: Long = System.currentTimeMillis()
)

// --- 4. WALLET TRANSACTION ENTITY ---
@Entity(tableName = "wallet_transactions")
data class WalletTransaction(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val userId: String,
    val amount: Double,
    val provider: String, // "eSewa", "Khalti", "Fonepay", "Bank Transfer", "Escrow Commission"
    val txType: String, // "DEPOSIT", "ESCROW_LOCK", "ESCROW_RELEASE", "COMMISSION", "WITHDRAWAL", "REFUND"
    val status: String, // "SUCCESS", "PENDING", "FAILED"
    val referenceId: String,
    val timestamp: Long = System.currentTimeMillis()
)

// --- DAOs DEFINITION ---

@Dao
interface UserDao {
    @Query("SELECT * FROM users WHERE id = :id")
    suspend fun getUserById(id: String): User?

    @Query("SELECT * FROM users WHERE role = :role")
    fun getUsersByRole(role: String): Flow<List<User>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertUser(user: User)

    @Query("UPDATE users SET walletBalance = :balance WHERE id = :id")
    suspend fun updateWalletBalance(id: String, balance: Double)

    @Query("UPDATE users SET isVerified = :verified, verificationRequestStatus = :status WHERE id = :id")
    suspend fun updateVerification(id: String, verified: Boolean, status: String)
}

@Dao
interface BookingDao {
    @Query("SELECT * FROM bookings ORDER BY timestamp DESC")
    fun getAllBookings(): Flow<List<Booking>>

    @Query("SELECT * FROM bookings WHERE customerId = :customerId ORDER BY timestamp DESC")
    fun getBookingsByCustomer(customerId: String): Flow<List<Booking>>

    @Query("SELECT * FROM bookings WHERE workerId = :workerId OR status = 'REQUESTED' ORDER BY timestamp DESC")
    fun getBookingsForWorker(workerId: String): Flow<List<Booking>>

    @Query("SELECT * FROM bookings WHERE id = :id")
    suspend fun getBookingById(id: Long): Booking?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertBooking(booking: Booking): Long

    @Query("UPDATE bookings SET status = :status, workerId = :workerId, workerName = :workerName WHERE id = :id")
    suspend fun assignWorker(id: Long, workerId: String, workerName: String, status: String)

    @Query("UPDATE bookings SET status = :status WHERE id = :id")
    suspend fun updateStatus(id: Long, status: String)

    @Query("UPDATE bookings SET escrowStatus = :escrowStatus WHERE id = :id")
    suspend fun updateEscrowStatus(id: Long, escrowStatus: String)

    @Query("UPDATE bookings SET ratingScore = :score, reviewText = :review WHERE id = :id")
    suspend fun addRating(id: Long, score: Int, review: String)
    
    @Query("UPDATE bookings SET userOtpAttempt = :otp WHERE id = :id")
    suspend fun updateOtpAttempt(id: Long, otp: String)
}

@Dao
interface MessageDao {
    @Query("SELECT * FROM messages WHERE bookingId = :bookingId ORDER BY timestamp ASC")
    fun getMessagesForBooking(bookingId: Long): Flow<List<Message>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertMessage(message: Message): Long
}

@Dao
interface WalletTransactionDao {
    @Query("SELECT * FROM wallet_transactions WHERE userId = :userId ORDER BY timestamp DESC")
    fun getTransactionsByUser(userId: String): Flow<List<WalletTransaction>>

    @Query("SELECT * FROM wallet_transactions ORDER BY timestamp DESC")
    fun getAllTransactions(): Flow<List<WalletTransaction>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertTransaction(transaction: WalletTransaction)
}

// --- Verified Worker Badge system eligibility rules ---
fun User.hasVerifiedBadge(allBookings: List<Booking>): Boolean {
    val completedVerificationDocs = isVerified && verificationRequestStatus == "VERIFIED"
    val highRatingOver50Jobs = rating >= 4.5f && completedJobsCount >= 50
    val hasNoUnresolvedDisputes = allBookings.none { it.workerId == id && it.status == "DISPUTED" }
    return completedVerificationDocs && highRatingOver50Jobs && hasNoUnresolvedDisputes
}

