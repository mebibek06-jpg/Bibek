package com.example.data

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.util.UUID

class MarketplaceViewModel(application: Application) : AndroidViewModel(application) {

    private val db = AppDatabase.getDatabase(application, viewModelScope)
    private val repo = SajhaTrustRepository(db)

    // --- Active Role Management ---
    private val _currentRole = MutableStateFlow("CUSTOMER") // "CUSTOMER", "WORKER", "ADMIN"
    val currentRole: StateFlow<String> = _currentRole.asStateFlow()

    private val _currentUserId = MutableStateFlow("customer_shyam")
    val currentUserId: StateFlow<String> = _currentUserId.asStateFlow()

    // Switch roles and adjust active simulated ID
    fun switchRole(role: String) {
        _currentRole.value = role
        _currentUserId.value = when (role) {
            "CUSTOMER" -> "customer_shyam"
            "WORKER" -> activeWorkerId.value
            "ADMIN" -> "admin_super"
            else -> "customer_shyam"
        }
    }

    // --- Active Worker Picker (Workers can swap profiles for testing) ---
    private val _activeWorkerId = MutableStateFlow("worker_ram")
    val activeWorkerId: StateFlow<String> = _activeWorkerId.asStateFlow()

    fun selectWorkerProfile(workerId: String) {
        _activeWorkerId.value = workerId
        if (_currentRole.value == "WORKER") {
            _currentUserId.value = workerId
        }
    }

    // --- Worker Status Toggles ---
    private val _workerOnline = MutableStateFlow(true)
    val workerOnline: StateFlow<Boolean> = _workerOnline.asStateFlow()

    fun toggleWorkerOnline() {
        _workerOnline.value = !_workerOnline.value
    }

    // --- State Observables (Flows connected directly to Room DB) ---
    val allWorkers: StateFlow<List<User>> = repo.getUsersByRole("WORKER")
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val customerUser: StateFlow<User?> = MutableStateFlow<User?>(null).apply {
        viewModelScope.launch {
            repo.getUsersByRole("CUSTOMER").collect { list ->
                value = list.firstOrNull { it.id == "customer_shyam" }
            }
        }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), null)

    val activeWorkerUser: StateFlow<User?> = MutableStateFlow<User?>(null).apply {
        viewModelScope.launch {
            allWorkers.collect { list ->
                value = list.firstOrNull { it.id == _activeWorkerId.value }
            }
        }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), null)

    val adminUser: StateFlow<User?> = MutableStateFlow<User?>(null).apply {
        viewModelScope.launch {
            repo.getUsersByRole("ADMIN").collect { list ->
                value = list.firstOrNull { it.id == "admin_super" }
            }
        }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), null)

    val allBookings: StateFlow<List<Booking>> = repo.allBookings
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val customerBookings: StateFlow<List<Booking>> = repo.getBookingsByCustomer("customer_shyam")
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val workerBookings: StateFlow<List<Booking>> = MutableStateFlow<List<Booking>>(emptyList()).apply {
        viewModelScope.launch {
            allBookings.collect { bookings ->
                val wId = _activeWorkerId.value
                value = bookings.filter { it.workerId == wId || it.status == "REQUESTED" }
            }
        }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val allTransactions: StateFlow<List<WalletTransaction>> = repo.allTransactions
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // --- Active Selected Chat Thread/Job Tracker ---
    private val _selectedBookingId = MutableStateFlow<Long?>(null)
    val selectedBookingId: StateFlow<Long?> = _selectedBookingId.asStateFlow()

    private val _selectedBooking = MutableStateFlow<Booking?>(null)
    val selectedBooking: StateFlow<Booking?> = _selectedBooking.asStateFlow()

    val chatMessages: StateFlow<List<Message>> = MutableStateFlow<List<Message>>(emptyList()).apply {
        viewModelScope.launch {
            _selectedBookingId.collect { id ->
                if (id != null) {
                    repo.getMessagesForBooking(id).collect { list ->
                        value = list
                    }
                } else {
                    value = emptyList()
                }
            }
        }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    fun selectBooking(id: Long?) {
        _selectedBookingId.value = id
        viewModelScope.launch {
            if (id != null) {
                _selectedBooking.value = repo.getBookingById(id)
            } else {
                _selectedBooking.value = null
            }
        }
    }

    // --- Gemini AI Advisory Diagnostic State ---
    private val _geminiResultState = MutableStateFlow<String?>(null)
    val geminiResultState: StateFlow<String?> = _geminiResultState.asStateFlow()

    private val _geminiLoading = MutableStateFlow(false)
    val geminiLoading: StateFlow<Boolean> = _geminiLoading.asStateFlow()

    fun runGeminiDiagnostic(prompt: String) {
        if (prompt.isBlank()) return
        _geminiLoading.value = true
        _geminiResultState.value = null
        viewModelScope.launch {
            val result = GeminiClient.analyzeServiceRequest(prompt)
            _geminiResultState.value = result
            _geminiLoading.value = false
        }
    }

    fun clearGeminiDiagnostic() {
        _geminiResultState.value = null
    }

    // --- 1. SERVICE CREATION & ESCROW PAYMENT FLOW (Shyam customer view) ---
    fun submitServiceBooking(
        category: String,
        title: String,
        description: String,
        location: String,
        urgency: String,
        pricingMode: String,
        price: Double,
        paymentProvider: String // "eSewa", "Khalti", "Fonepay"
    ) {
        viewModelScope.launch(Dispatchers.IO) {
            val customer = repo.getUserById("customer_shyam") ?: return@launch
            val currentBalance = customer.walletBalance

            // Ensure customer has enough pocket balance or simulate real payment deposit locking
            val finalPrice = if (price <= 0.0) 1200.0 else price
            
            // Generate standard 4-digit code as a completion escrow OTP token
            val secureOtp = (1000..9999).random().toString()

            val newBooking = Booking(
                category = category,
                customerId = "customer_shyam",
                customerName = customer.name,
                title = title,
                description = description,
                locationAddress = location,
                latitude = 27.700769, // Kathmandu Central Coordinate Mock
                longitude = 85.315066,
                urgency = urgency,
                pricingMode = pricingMode,
                initialBudget = finalPrice,
                agreedPrice = finalPrice,
                escrowStatus = "LOCKED_IN_ESCROW", // Locked funds instantly
                escrowProvider = paymentProvider,
                status = "REQUESTED",
                verificationOtp = secureOtp
            )

            // Deduct from local wallet mock or record standard deposit transaction
            val updatedBalance = currentBalance - finalPrice
            repo.updateWalletBalance("customer_shyam", updatedBalance)

            // Insert matching transaction
            repo.insertTransaction(
                WalletTransaction(
                    userId = "customer_shyam",
                    amount = finalPrice,
                    provider = paymentProvider,
                    txType = "ESCROW_LOCK",
                    status = "SUCCESS",
                    referenceId = "TR-${paymentProvider.uppercase()}-${(100000..999999).random()}"
                )
            )

            val bookingId = db.bookingDao().insertBooking(newBooking)

            // System Notification message
            repo.sendMessage(
                Message(
                    bookingId = bookingId,
                    senderId = "system",
                    senderName = "KaamChha System",
                    messageText = "Escrow funds of NRs. $finalPrice securely locked via $paymentProvider. OTP verification required on job completion.",
                    isSystemMessage = true
                )
            )

            _selectedBookingId.value = bookingId
            _selectedBooking.value = repo.getBookingById(bookingId)

            // Trigger simulated Worker background match if standard matching is selected
            simulateAutomatedMatchingFlow(bookingId, category)
        }
    }

    // Simulate worker responding and accepting job, followed by chat thread logs
    private fun simulateAutomatedMatchingFlow(bookingId: Long, category: String) {
        viewModelScope.launch {
            delay(5000) // Wait 5 seconds
            val current = repo.getBookingById(bookingId) ?: return@launch
            
            // Only auto-accept if booking is still in "REQUESTED" state
            if (current.status == "REQUESTED") {
                // Find matching worker
                val workers = repo.getUsersByRole("WORKER").stateIn(viewModelScope).value
                val matchedWorker = workers.firstOrNull { it.skills.contains(category, ignoreCase = true) && it.isVerified }
                    ?: workers.firstOrNull { it.isVerified }
                    ?: return@launch

                repo.assignWorkerToBooking(bookingId, matchedWorker.id, matchedWorker.name)
                
                // system log
                repo.sendMessage(
                    Message(
                        bookingId = bookingId,
                        senderId = "system",
                        senderName = "KaamChha System",
                        messageText = "Worker ${matchedWorker.name} has been matched and is routing to your location.",
                        isSystemMessage = true
                    )
                )

                delay(2000)
                repo.sendMessage(
                    Message(
                        bookingId = bookingId,
                        senderId = matchedWorker.id,
                        senderName = matchedWorker.name,
                        messageText = "Namaste Ji! I have received your request. I will bring specialized tools and arrive soon.",
                        isSystemMessage = false
                    )
                )

                // Refresh state
                if (selectedBookingId.value == bookingId) {
                    _selectedBooking.value = repo.getBookingById(bookingId)
                }
            }
        }
    }

    // --- 2. WORKER JOB MANAGEMENT & PROGRESS OPERATIONS (Ram worker view) ---
    fun acceptBooking(bookingId: Long, workerId: String, workerName: String) {
        viewModelScope.launch(Dispatchers.IO) {
            repo.assignWorkerToBooking(bookingId, workerId, workerName)
            repo.sendMessage(
                Message(
                    bookingId = bookingId,
                    senderId = "system",
                    senderName = "System Log",
                    messageText = "Worker $workerName has accepted the job proposal.",
                    isSystemMessage = true
                )
            )
            repo.sendMessage(
                Message(
                    bookingId = bookingId,
                    senderId = workerId,
                    senderName = workerName,
                    messageText = "Hi customer, I am starting now. See you shortly!",
                    isSystemMessage = false
                )
            )
            refreshCurrentSelectedBooking(bookingId)
        }
    }

    fun placeBidAndAcceptBooking(bookingId: Long, bidAmount: Double, workerId: String, workerName: String) {
        viewModelScope.launch(Dispatchers.IO) {
            val booking = repo.getBookingById(bookingId) ?: return@launch
            val updatedBooking = booking.copy(
                agreedPrice = bidAmount,
                workerId = workerId,
                workerName = workerName,
                status = "ACCEPTED"
            )
            db.bookingDao().insertBooking(updatedBooking)
            
            repo.sendMessage(
                Message(
                    bookingId = bookingId,
                    senderId = "system",
                    senderName = "KaamChha System",
                    messageText = "Worker $workerName has accepted the request with a custom bid of NRs. $bidAmount.",
                    isSystemMessage = true
                )
            )
            repo.sendMessage(
                Message(
                    bookingId = bookingId,
                    senderId = workerId,
                    senderName = workerName,
                    messageText = "Namaste Ji! I have matched on Bidding Mode. I am coming for work with a contract rate of NRs. $bidAmount.",
                    isSystemMessage = false
                )
            )
            refreshCurrentSelectedBooking(bookingId)
        }
    }

    fun markArrived(bookingId: Long) {
        viewModelScope.launch(Dispatchers.IO) {
            repo.updateBookingStatus(bookingId, "ARRIVED")
            repo.sendMessage(
                Message(
                    bookingId = bookingId,
                    senderId = "system",
                    senderName = "System Log",
                    messageText = "Worker has arrived at the service location.",
                    isSystemMessage = true
                )
            )
            refreshCurrentSelectedBooking(bookingId)
        }
    }

    fun startJob(bookingId: Long) {
        viewModelScope.launch(Dispatchers.IO) {
            repo.updateBookingStatus(bookingId, "STARTED")
            repo.sendMessage(
                Message(
                    bookingId = bookingId,
                    senderId = "system",
                    senderName = "System Log",
                    messageText = "The worker has officially started the work progress.",
                    isSystemMessage = true
                )
            )
            refreshCurrentSelectedBooking(bookingId)
        }
    }

    fun markCompleted(bookingId: Long) {
        viewModelScope.launch(Dispatchers.IO) {
            repo.updateBookingStatus(bookingId, "COMPLETED")
            repo.sendMessage(
                Message(
                    bookingId = bookingId,
                    senderId = "system",
                    senderName = "System Log",
                    messageText = "Job marked completed by the worker. Awaiting completion OTP to release escrow payout.",
                    isSystemMessage = true
                )
            )
            refreshCurrentSelectedBooking(bookingId)
        }
    }

    // --- 3. ESCROW TRUST ESCROW FUNDS CONVERGENCES (Customer OTP confirmation) ---
    fun submitCompletionOtpAndReleaseFunds(bookingId: Long, enteredOtp: String): Boolean {
        var success = false
        val booking = _selectedBooking.value ?: return false
        
        if (booking.verificationOtp == enteredOtp) {
            success = true
            viewModelScope.launch(Dispatchers.IO) {
                // Update booking attributes
                db.bookingDao().updateOtpAttempt(bookingId, enteredOtp)
                repo.updateBookingStatus(bookingId, "CLOSED")
                repo.updateBookingEscrowStatus(bookingId, "RELEASED")

                val basePrice = booking.agreedPrice
                val systemCommission = basePrice * 0.10 // 10% Platform fee commission
                val finalWorkerPayout = basePrice - systemCommission

                // Add to Worker Wallet
                val workerId = booking.workerId ?: "worker_ram"
                val worker = repo.getUserById(workerId)
                if (worker != null) {
                    val currentBal = worker.walletBalance
                    val updatedBal = currentBal + finalWorkerPayout
                    val updatedJobCount = worker.completedJobsCount + 1
                    
                    // Save worker wallet modification
                    repo.updateWalletBalance(workerId, updatedBal)
                    // Save stats count
                    db.userDao().insertUser(worker.copy(walletBalance = updatedBal, completedJobsCount = updatedJobCount))
                }

                // Log payout tracking transaction (worker's side)
                repo.insertTransaction(
                    WalletTransaction(
                        userId = workerId,
                        amount = finalWorkerPayout,
                        provider = "KaamChha Escrow",
                        txType = "ESCROW_RELEASE",
                        status = "SUCCESS",
                        referenceId = "TX-OUT-${(100000..999999).random()}"
                    )
                )

                // Log system commission
                repo.insertTransaction(
                    WalletTransaction(
                        userId = "admin_super",
                        amount = systemCommission,
                        provider = "Escrow Split",
                        txType = "COMMISSION",
                        status = "SUCCESS",
                        referenceId = "COM-${(100000..999999).random()}"
                    )
                )

                // System notifications
                repo.sendMessage(
                    Message(
                        bookingId = bookingId,
                        senderId = "system",
                        senderName = "KaamChha Escrow",
                        messageText = "✓ Escrow OTP Match! Funds released: NRs. $finalWorkerPayout disbursed to ${booking.workerName}. (10% commission of NRs. $systemCommission deducted)",
                        isSystemMessage = true
                    )
                )

                // Add to customercompleted states
                val customer = repo.getUserById("customer_shyam")
                if (customer != null) {
                    db.userDao().insertUser(customer.copy(completedJobsCount = customer.completedJobsCount + 1))
                }

                refreshCurrentSelectedBooking(bookingId)
            }
        } else {
            // Log failed attempt inside database
            viewModelScope.launch(Dispatchers.IO) {
                repo.submitCompletionOtp(bookingId, enteredOtp)
                refreshCurrentSelectedBooking(bookingId)
            }
        }
        return success
    }

    // --- 4. RATINGS & DISPUTES SYSTEM ---
    fun submitRating(bookingId: Long, rating: Int, review: String) {
        viewModelScope.launch(Dispatchers.IO) {
            repo.submitRatingAndReview(bookingId, rating, review)
            
            // Recalculate worker's global ratings average
            val booking = repo.getBookingById(bookingId) ?: return@launch
            val workerId = booking.workerId ?: return@launch
            
            val worker = repo.getUserById(workerId) ?: return@launch
            
            // Basic weighted moving average
            val newRating = if (worker.completedJobsCount <= 1) {
                rating.toFloat()
            } else {
                ((worker.rating * (worker.completedJobsCount - 1)) + rating) / worker.completedJobsCount
            }

            db.userDao().insertUser(worker.copy(rating = newRating))
            
            repo.sendMessage(
                Message(
                    bookingId = bookingId,
                    senderId = "system",
                    senderName = "KaamChha",
                    messageText = "Verified feedback submitted successfully: $rating ★ - \"$review\"",
                    isSystemMessage = true
                )
            )
            refreshCurrentSelectedBooking(bookingId)
        }
    }

    fun submitDispute(bookingId: Long, reason: String) {
        viewModelScope.launch(Dispatchers.IO) {
            repo.updateBookingStatus(bookingId, "DISPUTED")
            repo.updateBookingEscrowStatus(bookingId, "DISPUTED")
            repo.sendMessage(
                Message(
                    bookingId = bookingId,
                    senderId = "system",
                    senderName = "Security Escrow Escalator",
                    messageText = "⚠️ Conflict dispute filed! System administrators will review documents. Escrow funds lock extended. Reason: \"$reason\"",
                    isSystemMessage = true
                )
            )
            refreshCurrentSelectedBooking(bookingId)
        }
    }

    // --- 5. ADMIN CONTROL PANEL ACTIONS ---
    fun approveWorkerVerification(workerId: String) {
        viewModelScope.launch(Dispatchers.IO) {
            repo.approveWorker(workerId)
        }
    }

    fun rejectWorkerVerification(workerId: String) {
        viewModelScope.launch(Dispatchers.IO) {
            repo.rejectWorker(workerId)
        }
    }

    fun resolveDisputeInFavor(bookingId: Long, favorCustomer: Boolean) {
        viewModelScope.launch(Dispatchers.IO) {
            val booking = repo.getBookingById(bookingId) ?: return@launch
            val price = booking.agreedPrice

            if (favorCustomer) {
                // Refund to customer
                repo.updateBookingStatus(bookingId, "CLOSED")
                repo.updateBookingEscrowStatus(bookingId, "REFUNDED")
                
                val customer = repo.getUserById(booking.customerId)
                if (customer != null) {
                    repo.updateWalletBalance(booking.customerId, customer.walletBalance + price)
                }

                repo.insertTransaction(
                    WalletTransaction(
                        userId = booking.customerId,
                        amount = price,
                        provider = "KaamChha Escrow",
                        txType = "REFUND",
                        status = "SUCCESS",
                        referenceId = "REF-${(100000..999999).random()}"
                    )
                )

                repo.sendMessage(
                    Message(
                        bookingId = bookingId,
                        senderId = "system",
                        senderName = "KaamChha Security",
                        messageText = "⚖ Dispute Resolved by Admin. NRs. $price fully refunded to Customer's wallet.",
                        isSystemMessage = true
                    )
                )
            } else {
                // Pay out to worker (manually bypassing OTP check)
                repo.updateBookingStatus(bookingId, "CLOSED")
                repo.updateBookingEscrowStatus(bookingId, "RELEASED")

                val basePrice = booking.agreedPrice
                val commission = basePrice * 0.10
                val workerPayout = basePrice - commission

                val workerId = booking.workerId ?: "worker_ram"
                val worker = repo.getUserById(workerId)
                if (worker != null) {
                    repo.updateWalletBalance(workerId, worker.walletBalance + workerPayout)
                }

                repo.insertTransaction(
                     WalletTransaction(
                         userId = workerId,
                         amount = workerPayout,
                         provider = "KaamChha Escrow",
                         txType = "ESCROW_RELEASE",
                         status = "SUCCESS",
                         referenceId = "TX-OUT-${(100000..999999).random()}"
                     )
                )

                repo.sendMessage(
                    Message(
                        bookingId = bookingId,
                        senderId = "system",
                        senderName = "KaamChha Security",
                        messageText = "⚖ Dispute Resolved by Admin. Escrow funds released to Worker. disbursed NRs. $workerPayout.",
                        isSystemMessage = true
                    )
                )
            }
            refreshCurrentSelectedBooking(bookingId)
        }
    }

    // --- 6. SECURE WALLET DEPOSITS & CASH OUTS ---
    fun simulatedWalletDeposit(provider: String, amount: Double) {
        viewModelScope.launch(Dispatchers.IO) {
            val customer = repo.getUserById("customer_shyam") ?: return@launch
            val updatedBal = customer.walletBalance + amount
            repo.updateWalletBalance("customer_shyam", updatedBal)

            repo.insertTransaction(
                WalletTransaction(
                    userId = "customer_shyam",
                    amount = amount,
                    provider = provider,
                    txType = "DEPOSIT",
                    status = "SUCCESS",
                    referenceId = "DEP-${provider.uppercase()}-${(100000..999999).random()}"
                )
            )
        }
    }

    fun workerCashOut(provider: String, amount: Double) {
        viewModelScope.launch(Dispatchers.IO) {
            val wId = _activeWorkerId.value
            val worker = repo.getUserById(wId) ?: return@launch
            if (worker.walletBalance >= amount) {
                val updatedBal = worker.walletBalance - amount
                repo.updateWalletBalance(wId, updatedBal)

                repo.insertTransaction(
                    WalletTransaction(
                        userId = wId,
                        amount = amount,
                        provider = provider,
                        txType = "WITHDRAWAL",
                        status = "SUCCESS",
                        referenceId = "WD-${provider.uppercase()}-${(100000..999999).random()}"
                    )
                )
            }
        }
    }

    // --- 7. CHAT MESSAGE SENDING ---
    fun sendChatMessage(text: String) {
        val bId = _selectedBookingId.value ?: return
        val senderId = _currentUserId.value
        val name = when (_currentRole.value) {
            "CUSTOMER" -> "Shyam Nepal (Customer)"
            "WORKER" -> _selectedBooking.value?.workerName ?: "Ram Bahadur"
            "ADMIN" -> "Support Agent"
            else -> "User"
        }

        viewModelScope.launch(Dispatchers.IO) {
            repo.sendMessage(
                Message(
                    bookingId = bId,
                    senderId = senderId,
                    senderName = name,
                    messageText = text,
                    isSystemMessage = false
                )
            )

            // Trigger simulated prompt or live dialog matching
            if (_currentRole.value == "CUSTOMER") {
                simulateWorkerChatResponse(bId, text)
            }
        }
    }

    // Smart simulated conversation trigger to prevent static feeling in chatbot chat logs
    private fun simulateWorkerChatResponse(bookingId: Long, customerMsg: String) {
        viewModelScope.launch {
            delay(3000)
            val current = repo.getBookingById(bookingId) ?: return@launch
            val workerId = current.workerId ?: return@launch
            val workerName = current.workerName ?: "Ram"

            val reply = when {
                customerMsg.contains("where", ignoreCase = true) || customerMsg.contains("arrive", ignoreCase = true) || customerMsg.contains("kaha", ignoreCase = true) -> {
                    "I am packing my tools and leaving now. I will be at your location in around 15 minutes."
                }
                customerMsg.contains("price", ignoreCase = true) || customerMsg.contains("cost", ignoreCase = true) -> {
                    "The price is NRs. ${current.agreedPrice} as locked in escrow. Commission is already split, so this is exactly what we agreed!"
                }
                customerMsg.contains("tools", ignoreCase = true) || customerMsg.contains("bring", ignoreCase = true) -> {
                    "Yes, I am bringing my multi-meter, spare wiring, and standard heavy-duty electrical kit tools."
                }
                customerMsg.contains("urgent", ignoreCase = true) -> {
                    "Understood. I will speed up and reach your gate as soon as possible."
                }
                else -> {
                    "Namaste! Thank you for the message. I will take care of your ${current.category} issue. Please make sure the area is accessible."
                }
            }

            repo.sendMessage(
                Message(
                    bookingId = bookingId,
                    senderId = workerId,
                    senderName = workerName,
                    messageText = reply,
                    isSystemMessage = false
                )
            )
        }
    }

    private suspend fun refreshCurrentSelectedBooking(id: Long) {
        if (_selectedBookingId.value == id) {
            _selectedBooking.value = repo.getBookingById(id)
        }
    }
}
