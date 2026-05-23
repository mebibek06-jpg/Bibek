package com.example.ui.screens

import androidx.compose.animation.*
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.data.*
import com.example.ui.theme.*

@Composable
fun WorkerDashboardScreen(
    viewModel: MarketplaceViewModel,
    onBookingSelected: (Long) -> Unit
) {
    var selectedTab by remember { mutableStateOf(0) } // 0: Dispatch, 1: Profile & Credentials, 2: Withdrawals

    Scaffold(
        bottomBar = {
            NavigationBar(
                containerColor = Slate900,
                modifier = Modifier.windowInsetsPadding(WindowInsets.navigationBars)
            ) {
                NavigationBarItem(
                    selected = selectedTab == 0,
                    onClick = { selectedTab = 0 },
                    icon = { Icon(Icons.Default.Engineering, contentDescription = "Active Jobs dispatch dispatcher") },
                    label = { Text("Jobs", fontSize = 11.sp) },
                    colors = NavigationBarItemDefaults.colors(
                        selectedIconColor = TrustBlue,
                        selectedTextColor = TrustBlue,
                        indicatorColor = Slate800,
                        unselectedIconColor = TextMuted,
                        unselectedTextColor = TextMuted
                    )
                )
                NavigationBarItem(
                    selected = selectedTab == 1,
                    onClick = { selectedTab = 1 },
                    icon = { Icon(Icons.Default.ContactPage, contentDescription = "My Credentials") },
                    label = { Text("Auth Docs", fontSize = 11.sp) },
                    colors = NavigationBarItemDefaults.colors(
                        selectedIconColor = TrustBlue,
                        selectedTextColor = TrustBlue,
                        indicatorColor = Slate800,
                        unselectedIconColor = TextMuted,
                        unselectedTextColor = TextMuted
                    )
                )
                NavigationBarItem(
                    selected = selectedTab == 2,
                    onClick = { selectedTab = 2 },
                    icon = { Icon(Icons.Default.Savings, contentDescription = "Earnings ledger & withdraw balance") },
                    label = { Text("Payouts", fontSize = 11.sp) },
                    colors = NavigationBarItemDefaults.colors(
                        selectedIconColor = TrustBlue,
                        selectedTextColor = TrustBlue,
                        indicatorColor = Slate800,
                        unselectedIconColor = TextMuted,
                        unselectedTextColor = TextMuted
                    )
                )
            }
        },
        containerColor = MidnightDark
    ) { paddingValues ->
        Box(modifier = Modifier.padding(paddingValues)) {
            when (selectedTab) {
                0 -> WorkerJobsTab(viewModel, onBookingSelected)
                1 -> WorkerCredentialsTab(viewModel)
                2 -> WorkerEarningsTab(viewModel)
            }
        }
    }
}

@Composable
fun WorkerJobsTab(viewModel: MarketplaceViewModel, onBookingSelected: (Long) -> Unit) {
    val bookings by viewModel.workerBookings.collectAsStateWithLifecycle()
    val workerIsOnline by viewModel.workerOnline.collectAsStateWithLifecycle()
    val workerUser by viewModel.activeWorkerUser.collectAsStateWithLifecycle()
    val workersList by viewModel.allWorkers.collectAsStateWithLifecycle()
    val allBookings by viewModel.allBookings.collectAsStateWithLifecycle()

    var showWorkerProfileChooser by remember { mutableStateOf(false) }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp),
        contentPadding = PaddingValues(top = 16.dp, bottom = 24.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Active worker profile summary
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.clickable { showWorkerProfileChooser = true }) {
                    Text("Service Provider Dashboard (Tap to swap)", fontSize = 11.sp, color = TrustBlueLight, fontWeight = FontWeight.Bold)
                    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                        val worker = workerUser
                        Text(worker?.name ?: "Ram Bahadur Shrestha", fontSize = 18.sp, fontWeight = FontWeight.Bold, color = TextLight)
                        Icon(Icons.Default.SwapHoriz, contentDescription = null, tint = TextMuted, modifier = Modifier.size(16.dp))
                        
                        // Render Profile Badge if eligible!
                        if (worker != null && worker.hasVerifiedBadge(allBookings)) {
                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(6.dp))
                                    .background(
                                        Brush.horizontalGradient(
                                            listOf(
                                                Color(0xFFFFC107), // Golden amber
                                                Color(0xFFFF9800)  // Sparkle orange
                                            )
                                        )
                                    )
                                    .border(1.dp, Color(0xFFFFF9C4), RoundedCornerShape(6.dp))
                                    .padding(horizontal = 6.dp, vertical = 2.dp)
                            ) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(3.dp)
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.WorkspacePremium,
                                        contentDescription = "Verified Badge Elite",
                                        tint = Color.White,
                                        modifier = Modifier.size(11.dp)
                                    )
                                    Text(
                                        text = "ELITE PRO",
                                        fontSize = 9.sp,
                                        color = Color.White,
                                        fontWeight = FontWeight.Black,
                                        letterSpacing = 0.5.sp
                                    )
                                }
                            }
                        }
                    }
                    Text("Skills: ${workerUser?.skills ?: "Electrician"}", fontSize = 12.sp, color = TextMuted)
                }

                // Switch switcher to toggle Online Presence Mode (Saves battery/GPS)
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Text(
                        text = if (workerIsOnline) "ONLINE" else "OFFLINE",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = if (workerIsOnline) EscrowGreenLight else DangerRed
                    )
                    Switch(
                        checked = workerIsOnline,
                        onCheckedChange = { viewModel.toggleWorkerOnline() },
                        colors = SwitchDefaults.colors(
                            checkedThumbColor = Color.White,
                            checkedTrackColor = EscrowGreen,
                            uncheckedThumbColor = TextMuted,
                            uncheckedTrackColor = Slate800
                        ),
                        modifier = Modifier.testTag("worker_online_switch")
                    )
                }
            }
        }

        // Warning banner if workers parameters are not fully verified
        if (workerUser?.isVerified == false) {
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = DangerRed.copy(alpha = 0.12f)),
                    border = BorderStroke(1.dp, DangerRed)
                ) {
                    Row(
                        modifier = Modifier.padding(16.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Icon(Icons.Default.LockClock, contentDescription = null, tint = DangerRed)
                        Column {
                            Text("Pending Admin Document Approval", fontWeight = FontWeight.Bold, fontSize = 14.sp, color = TextLight)
                            Text("Your ID checks and selfie audits are currently pending. Head to Admin View to accept!", fontSize = 12.sp, color = TextMuted)
                        }
                    }
                }
            }
        }

        // Live Income Alerts and Broadcast list
        item {
            Text(
                text = "Live Job Postings (Matching Area, GPS)",
                fontSize = 16.sp,
                fontWeight = FontWeight.Bold,
                color = TextLight
            )
        }

        if (!workerIsOnline) {
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = CardDark)
                ) {
                    Column(
                        modifier = Modifier.padding(24.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Icon(Icons.Default.CloudOff, contentDescription = null, tint = TextMuted, modifier = Modifier.size(40.dp))
                        Spacer(modifier = Modifier.height(12.dp))
                        Text("You are offline.", color = TextLight, fontWeight = FontWeight.Bold)
                        Text("Switch on to listen to nearest available customer requests.", color = TextMuted, fontSize = 12.sp)
                    }
                }
            }
        } else {
            // Find bookings that are in pool or explicitly linked to this worker
            val relevantBookings = bookings.filter { it.status == "REQUESTED" || it.workerId == workerUser?.id }
            
            if (relevantBookings.isEmpty()) {
                item {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(containerColor = CardDark)
                    ) {
                        Column(
                            modifier = Modifier.padding(32.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Icon(Icons.Default.HourglassEmpty, contentDescription = null, tint = TextMuted, modifier = Modifier.size(36.dp))
                            Spacer(modifier = Modifier.height(12.dp))
                            Text("Awaiting customer alerts...", color = TextMuted, fontSize = 14.sp)
                            Text("Try posting a job inside Customer Dashboard!", color = TextMuted, fontSize = 11.sp)
                        }
                    }
                }
            } else {
                items(relevantBookings) { booking ->
                    val isLockedToActiveWorker = booking.workerId == workerUser?.id
                    
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { onBookingSelected(booking.id) },
                        colors = CardDefaults.cardColors(
                            containerColor = if (isLockedToActiveWorker) Slate800 else CardDark
                        ),
                        border = if (isLockedToActiveWorker) BorderStroke(1.dp, TrustBlue.copy(alpha = 0.5f)) else null
                    ) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Row(horizontalArrangement = Arrangement.spacedBy(6.dp), verticalAlignment = Alignment.CenterVertically) {
                                    Icon(Icons.Default.LocationOn, contentDescription = null, tint = DangerRed, modifier = Modifier.size(14.dp))
                                    Text(booking.locationAddress, fontSize = 11.sp, color = TextMuted)
                                }
                                Box(
                                    modifier = Modifier
                                        .clip(RoundedCornerShape(4.dp))
                                        .background(
                                            if (booking.status == "REQUESTED") WarningAmber.copy(alpha = 0.15f)
                                            else TrustBlue.copy(alpha = 0.15f)
                                        )
                                        .padding(horizontal = 6.dp, vertical = 2.dp)
                                ) {
                                    Text(
                                        booking.status,
                                        fontSize = 10.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = if (booking.status == "REQUESTED") WarningAmber else TrustBlueLight
                                    )
                                }
                            }

                            Spacer(modifier = Modifier.height(8.dp))
                            Text(booking.title, fontWeight = FontWeight.Bold, fontSize = 16.sp, color = TextLight)
                            Text(booking.description, color = TextMuted, fontSize = 13.sp, maxLines = 2)

                            Spacer(modifier = Modifier.height(12.dp))
                            Divider(color = TextMuted.copy(alpha = 0.12f))
                            Spacer(modifier = Modifier.height(12.dp))

                            if (booking.status == "REQUESTED") {
                                var showBidPanel by remember { mutableStateOf(false) }
                                var customBidAmount by remember { mutableStateOf(booking.agreedPrice.toInt().toString()) }
                                val pricingResult = remember(booking.category, booking.urgency, workersList, allBookings) {
                                    DynamicPricingEngine.calculateDynamicPrice(booking.category, booking.urgency, System.currentTimeMillis(), workersList, allBookings)
                                }

                                Column(verticalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.fillMaxWidth().padding(top = 8.dp)) {
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Column {
                                            Text("Pricing mode: ${booking.pricingMode}", fontSize = 11.sp, color = TextMuted)
                                            Text("Customer Budget: NRs. ${booking.agreedPrice.toInt()}", fontWeight = FontWeight.Bold, color = EscrowGreenLight, fontSize = 13.sp)
                                        }
                                        Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                                            Button(
                                                onClick = {
                                                    viewModel.acceptBooking(
                                                        booking.id,
                                                        workerUser?.id ?: "worker_ram",
                                                        workerUser?.name ?: "Ram Bahadur"
                                                    )
                                                },
                                                colors = ButtonDefaults.buttonColors(containerColor = EscrowGreen),
                                                shape = RoundedCornerShape(8.dp),
                                                modifier = Modifier.testTag("accept_job_button")
                                            ) {
                                                Text("Accept", color = TextLight, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                                            }
                                            OutlinedButton(
                                                onClick = { showBidPanel = !showBidPanel },
                                                border = BorderStroke(1.dp, TrustBlue.copy(alpha = 0.5f)),
                                                shape = RoundedCornerShape(8.dp),
                                                contentPadding = PaddingValues(horizontal = 8.dp, vertical = 4.dp)
                                            ) {
                                                Text(if (showBidPanel) "Hide Bid" else "Counter Bid", color = TrustBlueLight, fontSize = 11.sp)
                                            }
                                        }
                                    }

                                    // AI recommendation hint
                                    Row(
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.spacedBy(6.dp),
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .clip(RoundedCornerShape(6.dp))
                                            .background(TrustBlue.copy(alpha = 0.08f))
                                            .padding(vertical = 4.dp, horizontal = 8.dp)
                                    ) {
                                        Icon(Icons.Default.AutoAwesome, contentDescription = null, tint = TrustBlueLight, modifier = Modifier.size(12.dp))
                                        Text(
                                            text = "AI Recommended Range: NRs. ${pricingResult.recommendedMin.toInt()} - ${pricingResult.recommendedMax.toInt()}",
                                            fontSize = 11.sp,
                                            color = TrustBlueLight
                                        )
                                    }

                                    if (showBidPanel) {
                                        Row(
                                            modifier = Modifier.fillMaxWidth(),
                                            verticalAlignment = Alignment.CenterVertically,
                                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                                        ) {
                                            OutlinedTextField(
                                                value = customBidAmount,
                                                onValueChange = { customBidAmount = it },
                                                label = { Text("Alternative Bid Rate") },
                                                modifier = Modifier.weight(1f).testTag("counter_bid_input"),
                                                colors = OutlinedTextFieldDefaults.colors(
                                                    focusedTextColor = TextLight,
                                                    unfocusedTextColor = TextMuted,
                                                    focusedBorderColor = TrustBlue,
                                                    unfocusedBorderColor = Slate800
                                                ),
                                                singleLine = true
                                            )
                                            Button(
                                                onClick = {
                                                    val bidValue = customBidAmount.toDoubleOrNull() ?: booking.agreedPrice
                                                    val worker = workerUser
                                                    viewModel.placeBidAndAcceptBooking(
                                                        bookingId = booking.id,
                                                        workerId = worker?.id ?: "worker_ram",
                                                        workerName = worker?.name ?: "Ram Bahadur",
                                                        bidAmount = bidValue
                                                    )
                                                    showBidPanel = false
                                                },
                                                colors = ButtonDefaults.buttonColors(containerColor = TrustBlue),
                                                shape = RoundedCornerShape(8.dp),
                                                modifier = Modifier.testTag("submit_bid_button")
                                            ) {
                                                Text("Submit Bid", color = TextLight, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                                            }
                                        }
                                    }
                                }
                            } else {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text(
                                        text = "Payout NRs. ${booking.agreedPrice}",
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 15.sp,
                                        color = EscrowGreenLight
                                    )
                                    OutlinedButton(
                                        onClick = { onBookingSelected(booking.id) },
                                        shape = RoundedCornerShape(8.dp)
                                    ) {
                                        Text("Open Controls", fontSize = 12.sp, color = TrustBlueLight)
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    // Modal view to choose active sandbox worker!
    if (showWorkerProfileChooser) {
        AlertDialog(
            onDismissRequest = { showWorkerProfileChooser = false },
            containerColor = CardDark,
            title = { Text("Choose Sandbox Worker Role", color = TextLight, fontSize = 16.sp) },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    workersList.forEach { worker ->
                        Card(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable {
                                    viewModel.selectWorkerProfile(worker.id)
                                    showWorkerProfileChooser = false
                                },
                            colors = CardDefaults.cardColors(containerColor = if (worker.id == workerUser?.id) Slate800 else MidnightDark),
                            border = if (worker.id == workerUser?.id) BorderStroke(1.dp, TrustBlue) else null
                        ) {
                            Row(modifier = Modifier.padding(12.dp), horizontalArrangement = Arrangement.spacedBy(12.dp), verticalAlignment = Alignment.CenterVertically) {
                                Icon(Icons.Default.Engineering, contentDescription = null, tint = TrustBlueLight)
                                Column {
                                    Text(worker.name, fontWeight = FontWeight.Bold, fontSize = 14.sp, color = TextLight)
                                    Text(
                                        text = "${worker.skills} (${if (worker.isVerified) "Verified" else "Pending Review"})",
                                        fontSize = 12.sp,
                                        color = TextMuted
                                    )
                                }
                            }
                        }
                    }
                }
            },
            confirmButton = {}
        )
    }
}

@Composable
fun WorkerCredentialsTab(viewModel: MarketplaceViewModel) {
    val workerUser by viewModel.activeWorkerUser.collectAsStateWithLifecycle()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Text("Your Credentials", fontSize = 20.sp, fontWeight = FontWeight.Bold, color = TextLight)
        Text("KaamChha maintains a zero-tolerance fraud policy. Upload your documentation to list natively under premium matched feeds.", fontSize = 13.sp, color = TextMuted)

        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(containerColor = CardDark)
        ) {
            Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                    Text("Approval Status", fontWeight = FontWeight.Bold, color = TextLight)
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(4.dp))
                            .background(if (workerUser?.isVerified == true) EscrowGreen.copy(alpha = 0.15f) else WarningAmber.copy(alpha = 0.15f))
                            .padding(6.dp)
                    ) {
                        Text(
                            text = workerUser?.verificationRequestStatus ?: "PENDING",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = if (workerUser?.isVerified == true) EscrowGreenLight else WarningAmber
                        )
                    }
                }

                Divider(color = TextMuted.copy(alpha = 0.12f))

                Row(horizontalArrangement = Arrangement.spacedBy(12.dp), verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = if (workerUser?.selfieVerified == true) Icons.Default.CheckCircle else Icons.Default.Portrait,
                        contentDescription = null,
                        tint = if (workerUser?.selfieVerified == true) EscrowGreenLight else TextMuted
                    )
                    Column {
                        Text("Selfie Face ID Match Audit", fontWeight = FontWeight.SemiBold, fontSize = 14.sp, color = TextLight)
                        Text("Checks matches with national citizenship registry.", fontSize = 12.sp, color = TextMuted)
                    }
                }

                Divider(color = TextMuted.copy(alpha = 0.12f))

                Row(horizontalArrangement = Arrangement.spacedBy(12.dp), verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = if (workerUser?.isVerified == true) Icons.Default.CheckCircle else Icons.Default.UploadFile,
                        contentDescription = null,
                        tint = if (workerUser?.isVerified == true) EscrowGreenLight else TextMuted
                    )
                    Column {
                        Text("Citizenship Card Documents", fontWeight = FontWeight.SemiBold, fontSize = 14.sp, color = TextLight)
                        Text("Government-backed photo identification document.", fontSize = 12.sp, color = TextMuted)
                    }
                }
            }
        }
    }
}

@Composable
fun WorkerEarningsTab(viewModel: MarketplaceViewModel) {
    val workerUser by viewModel.activeWorkerUser.collectAsStateWithLifecycle()
    val txs by viewModel.allTransactions.collectAsStateWithLifecycle()

    var showWidthdrawDialog by remember { mutableStateOf(false) }
    var selectedWithdrawProvider by remember { mutableStateOf("eSewa") }
    var withdrawAmount by remember { mutableStateOf("") }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Text("Earnings and Withdrawals", fontSize = 20.sp, fontWeight = FontWeight.Bold, color = TextLight)

        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(containerColor = Slate800)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(20.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text("Cleared Payout Balance", fontSize = 13.sp, color = TextMuted)
                Text("NRs. ${workerUser?.walletBalance ?: 0.0}", fontSize = 32.sp, fontWeight = FontWeight.Bold, color = EscrowGreenLight)

                Spacer(modifier = Modifier.height(16.dp))

                Button(
                    onClick = { showWidthdrawDialog = true },
                    modifier = Modifier.fillMaxWidth().testTag("worker_withdraw_button"),
                    colors = ButtonDefaults.buttonColors(containerColor = TrustBlue),
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Icon(Icons.Default.AccountBalance, contentDescription = null, tint = TextLight)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Cash out to local Nepali Gateway", fontWeight = FontWeight.Bold, color = TextLight)
                }
            }
        }

        Text("Recent Ledger receipts", fontSize = 15.sp, fontWeight = FontWeight.Bold, color = TextLight)

        val workerID = workerUser?.id ?: "worker_ram"
        val filteredTxs = txs.filter { it.userId == workerID }

        if (filteredTxs.isEmpty()) {
            Box(modifier = Modifier.weight(1f).fillMaxWidth(), contentAlignment = Alignment.Center) {
                Text("No cash ledger records registered.", color = TextMuted)
            }
        } else {
            LazyColumn(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(filteredTxs) { tx ->
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(containerColor = CardDark)
                    ) {
                        Row(
                            modifier = Modifier.padding(12.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(horizontalArrangement = Arrangement.spacedBy(12.dp), verticalAlignment = Alignment.CenterVertically) {
                                Box(
                                    modifier = Modifier
                                        .size(36.dp)
                                        .clip(CircleShape)
                                        .background(
                                            if (tx.txType == "WITHDRAWAL") DangerRed.copy(alpha = 0.15f)
                                            else EscrowGreen.copy(alpha = 0.15f)
                                        ),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(
                                        imageVector = if (tx.txType == "WITHDRAWAL") Icons.Default.Shortcut else Icons.Default.AccountBalanceWallet,
                                        contentDescription = null,
                                        tint = if (tx.txType == "WITHDRAWAL") DangerRed else EscrowGreenLight,
                                        modifier = Modifier.size(18.dp)
                                    )
                                }
                                Column {
                                    Text(tx.txType, fontWeight = FontWeight.Bold, fontSize = 13.sp, color = TextLight)
                                    Text("Ref ID: ${tx.referenceId}", fontSize = 11.sp, color = TextMuted)
                                    Text("Gateway: ${tx.provider}", fontSize = 11.sp, color = TextMuted)
                                }
                            }
                            Text(
                                text = "${if (tx.txType == "WITHDRAWAL") "-" else "+"} NRs. ${tx.amount}",
                                fontWeight = FontWeight.Bold,
                                color = if (tx.txType == "WITHDRAWAL") DangerRed else EscrowGreenLight,
                                fontSize = 14.sp
                            )
                        }
                    }
                }
            }
        }
    }

    if (showWidthdrawDialog) {
        AlertDialog(
            onDismissRequest = { showWidthdrawDialog = false },
            containerColor = CardDark,
            title = { Text("Gateway Cash Out Check", color = TextLight) },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Text("Select Nepalese Financial Channel:", fontSize = 12.sp, color = TextMuted)
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        listOf("eSewa", "Khalti", "Fonepay", "Bank Transfer").forEach { provider ->
                            val selected = selectedWithdrawProvider == provider
                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(8.dp))
                                    .background(if (selected) TrustBlue.copy(alpha = 0.2f) else Slate800)
                                    .border(1.dp, if (selected) TrustBlue else Color.Transparent, RoundedCornerShape(8.dp))
                                    .clickable { selectedWithdrawProvider = provider }
                                    .padding(horizontal = 10.dp, vertical = 6.dp)
                                ) {
                                Text(provider, color = if (selected) TrustBlueLight else TextLight, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    OutlinedTextField(
                        value = withdrawAmount,
                        onValueChange = { withdrawAmount = it },
                        label = { Text("Amount (NRs.)") },
                        singleLine = true,
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedTextColor = TextLight,
                            focusedBorderColor = TrustBlue,
                            unfocusedTextColor = TextMuted
                        )
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        val amt = withdrawAmount.toDoubleOrNull() ?: 0.0
                        if (amt > 0 && amt <= (workerUser?.walletBalance ?: 0.0)) {
                            viewModel.workerCashOut(selectedWithdrawProvider, amt)
                            showWidthdrawDialog = false
                            withdrawAmount = ""
                        }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = EscrowGreen)
                ) {
                    Text("Request Disbursement", color = TextLight)
                }
            }
        )
    }
}
