package com.example.data

import kotlinx.coroutines.flow.Flow
import java.util.UUID

class SajhaTrustRepository(private val database: AppDatabase) {

    private val userDao = database.userDao()
    private val bookingDao = database.bookingDao()
    private val messageDao = database.messageDao()
    private val transactionDao = database.walletTransactionDao()

    // --- User Repo Methods ---
    suspend fun getUserById(userId: String): User? = userDao.getUserById(userId)
    
    fun getUsersByRole(role: String): Flow<List<User>> = userDao.getUsersByRole(role)
    
    suspend fun insertUser(user: User) = userDao.insertUser(user)
    
    suspend fun updateWalletBalance(userId: String, newBalance: Double) = 
        userDao.updateWalletBalance(userId, newBalance)

    suspend fun approveWorker(workerId: String) =
        userDao.updateVerification(workerId, true, "VERIFIED")

    suspend fun rejectWorker(workerId: String) =
        userDao.updateVerification(workerId, false, "REJECTED")

    // --- Booking Repo Methods ---
    val allBookings: Flow<List<Booking>> = bookingDao.getAllBookings()
    
    fun getBookingsByCustomer(customerId: String): Flow<List<Booking>> = 
        bookingDao.getBookingsByCustomer(customerId)

    fun getBookingsForWorker(workerId: String): Flow<List<Booking>> =
        bookingDao.getBookingsForWorker(workerId)

    suspend fun getBookingById(bookingId: Long): Booking? = bookingDao.getBookingById(bookingId)

    suspend fun createBooking(booking: Booking): Long {
        return bookingDao.insertBooking(booking)
    }

    suspend fun assignWorkerToBooking(bookingId: Long, workerId: String, workerName: String) {
        bookingDao.assignWorker(bookingId, workerId, workerName, "ACCEPTED")
    }

    suspend fun updateBookingStatus(bookingId: Long, status: String) {
        bookingDao.updateStatus(bookingId, status)
    }

    suspend fun updateBookingEscrowStatus(bookingId: Long, escrowStatus: String) {
        bookingDao.updateEscrowStatus(bookingId, escrowStatus)
    }

    suspend fun submitRatingAndReview(bookingId: Long, score: Int, review: String) {
        bookingDao.addRating(bookingId, score, review)
    }

    suspend fun submitCompletionOtp(bookingId: Long, otp: String) {
        bookingDao.updateOtpAttempt(bookingId, otp)
    }

    // --- Message Repo Methods ---
    fun getMessagesForBooking(bookingId: Long): Flow<List<Message>> = 
        messageDao.getMessagesForBooking(bookingId)

    suspend fun sendMessage(message: Message): Long {
         return messageDao.insertMessage(message)
    }

    // --- Transaction Repo Methods ---
    val allTransactions: Flow<List<WalletTransaction>> = transactionDao.getAllTransactions()

    fun getTransactionsByUser(userId: String): Flow<List<WalletTransaction>> = 
        transactionDao.getTransactionsByUser(userId)

    suspend fun insertTransaction(transaction: WalletTransaction) {
        transactionDao.insertTransaction(transaction)
    }
}
