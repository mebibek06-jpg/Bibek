package com.example.ui.screens

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
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.data.*
import com.example.ui.theme.*

@Composable
fun AdminDashboardScreen(
    viewModel: MarketplaceViewModel,
    onBookingSelected: (Long) -> Unit
) {
    var selectedTab by remember { mutableStateOf(0) } // 0: Overview, 1: Verifications, 2: Disputes, 3: Ledgers

    Scaffold(
        bottomBar = {
            NavigationBar(
                containerColor = Slate900,
                modifier = Modifier.windowInsetsPadding(WindowInsets.navigationBars)
            ) {
                NavigationBarItem(
                    selected = selectedTab == 0,
                    onClick = { selectedTab = 0 },
                    icon = { Icon(Icons.Default.Dashboard, contentDescription = "Overview panel") },
                    label = { Text("Overview", fontSize = 11.sp) },
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
                    icon = { Icon(Icons.Default.Verified, contentDescription = "Verify Workers logs") },
                    label = { Text("Mod Audits", fontSize = 11.sp) },
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
                    icon = { Icon(Icons.Default.Scale, contentDescription = "Disputes Resolver Desk") },
                    label = { Text("Disputes", fontSize = 11.sp) },
                    colors = NavigationBarItemDefaults.colors(
                        selectedIconColor = TrustBlue,
                        selectedTextColor = TrustBlue,
                        indicatorColor = Slate800,
                        unselectedIconColor = TextMuted,
                        unselectedTextColor = TextMuted
                    )
                )
                NavigationBarItem(
                    selected = selectedTab == 3,
                    onClick = { selectedTab = 3 },
                    icon = { Icon(Icons.Default.ReceiptLong, contentDescription = "Security Split Audits ledger") },
                    label = { Text("Ledgers", fontSize = 11.sp) },
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
                0 -> AdminOverviewTab(viewModel)
                1 -> AdminVerificationsTab(viewModel)
                2 -> AdminDisputesTab(viewModel, onBookingSelected)
                3 -> AdminLedgersTab(viewModel)
            }
        }
    }
}

@Composable
fun AdminOverviewTab(viewModel: MarketplaceViewModel) {
    val bookings by viewModel.allBookings.collectAsStateWithLifecycle()
    val workers by viewModel.allWorkers.collectAsStateWithLifecycle()
    val txs by viewModel.allTransactions.collectAsStateWithLifecycle()

    // Calculate dynamic analytics from Room State
    val totalEscrowLocked = bookings.filter { it.escrowStatus == "LOCKED_IN_ESCROW" }.sumOf { it.agreedPrice }
    val totalCommissions = txs.filter { it.txType == "COMMISSION" }.sumOf { it.amount }
    val pendingVerifications = workers.filter { it.verificationRequestStatus == "PENDING" }.size
    val activeDisputes = bookings.filter { it.status == "DISPUTED" }.size

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        item {
            Text("Admin control center Dashboard", fontSize = 22.sp, fontWeight = FontWeight.Bold, color = TextLight)
            Text("Real-time telemetry and escrow pool monitoring across Nepal.", fontSize = 13.sp, color = TextMuted)
        }

        // Metrics Grid
        item {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    AdminMetricCard(
                        title = "POOL ESCROW VOLUME",
                        value = "NRs. $totalEscrowLocked",
                        icon = Icons.Default.Lock,
                        color = EscrowGreenLight,
                        modifier = Modifier.weight(1f)
                    )
                    AdminMetricCard(
                        title = "TOTAL COMMISSION",
                        value = "NRs. $totalCommissions",
                        icon = Icons.Default.Savings,
                        color = TrustBlueLight,
                        modifier = Modifier.weight(1f)
                    )
                }
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    AdminMetricCard(
                        title = "PENDING AUDITS",
                        value = "$pendingVerifications",
                        icon = Icons.Default.HourglassBottom,
                        color = WarningAmber,
                        modifier = Modifier.weight(1f)
                    )
                    AdminMetricCard(
                        title = "ACTIVE DISPUTES",
                        value = "$activeDisputes",
                        icon = Icons.Default.Gavel,
                        color = DangerRed,
                        modifier = Modifier.weight(1f)
                    )
                }
            }
        }

        // General Security Status Health indicator
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = CardDark)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text("Marketplace Health Diagnostics", fontWeight = FontWeight.Bold, color = TextLight, fontSize = 15.sp)
                    Spacer(modifier = Modifier.height(12.dp))

                    DiagnosticIndicator(label = "Hardware SMS Binding Integrity", status = "100% SECURE", color = EscrowGreenLight)
                    DiagnosticIndicator(label = "Escrow Holding Vault", status = "NRs. $totalEscrowLocked HOLDING", color = EscrowGreenLight)
                    DiagnosticIndicator(label = "Verification Latency", status = "0.2s AVG COORD", color = EscrowGreenLight)
                    DiagnosticIndicator(label = "API status Gateway", status = "ONLINE", color = EscrowGreenLight)
                }
            }
        }
    }
}

@Composable
fun AdminMetricCard(
    title: String,
    value: String,
    icon: ImageVector,
    color: Color,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier.height(110.dp),
        colors = CardDefaults.cardColors(containerColor = CardDark)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp),
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                Text(title, fontSize = 11.sp, fontWeight = FontWeight.Bold, color = TextMuted)
                Icon(icon, contentDescription = null, tint = color, modifier = Modifier.size(16.dp))
            }
            Text(value, fontSize = 20.sp, fontWeight = FontWeight.Bold, color = TextLight)
        }
    }
}

@Composable
fun DiagnosticIndicator(label: String, status: String, color: Color) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(label, fontSize = 13.sp, color = TextMuted)
        Text(status, fontSize = 13.sp, fontWeight = FontWeight.Bold, color = color)
    }
}

// --- SUB-SCREEN CONTROLS TABS ---

@Composable
fun AdminVerificationsTab(viewModel: MarketplaceViewModel) {
    val workers by viewModel.allWorkers.collectAsStateWithLifecycle()
    val pendingWorkers = workers.filter { !it.isVerified && it.verificationRequestStatus == "PENDING" }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Text("Worker Credential Audits", fontSize = 20.sp, fontWeight = FontWeight.Bold, color = TextLight)
        Text("Confirm face-match selfie and citizenship logs are accurate.", fontSize = 13.sp, color = TextMuted)

        if (pendingWorkers.isEmpty()) {
            Box(modifier = Modifier.weight(1f).fillMaxWidth(), contentAlignment = Alignment.Center) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Icon(Icons.Default.CloudQueue, contentDescription = null, tint = TextMuted, modifier = Modifier.size(48.dp))
                    Spacer(modifier = Modifier.height(12.dp))
                    Text("No pending worker verifications.", color = TextMuted)
                    Text("All provider accounts are currently clean & approved.", color = TextMuted, fontSize = 12.sp)
                }
            }
        } else {
            LazyColumn(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                items(pendingWorkers) { worker ->
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(containerColor = CardDark)
                    ) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Row(horizontalArrangement = Arrangement.spacedBy(10.dp), verticalAlignment = Alignment.CenterVertically) {
                                    Box(
                                        modifier = Modifier
                                            .size(36.dp)
                                            .clip(CircleShape)
                                            .background(TrustBlue.copy(alpha = 0.15f)),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Icon(Icons.Default.Person, contentDescription = null, tint = TrustBlueLight)
                                    }
                                    Column {
                                        Text(worker.name, fontWeight = FontWeight.Bold, fontSize = 15.sp, color = TextLight)
                                        Text(worker.skills, fontSize = 12.sp, color = TextMuted)
                                    }
                                }
                                Box(
                                    modifier = Modifier
                                        .clip(RoundedCornerShape(4.dp))
                                        .background(WarningAmber.copy(alpha = 0.12f))
                                        .padding(horizontal = 6.dp, vertical = 2.dp)
                                ) {
                                    Text("AUDIT", fontSize = 10.sp, color = WarningAmber, fontWeight = FontWeight.Bold)
                                }
                            }

                            Spacer(modifier = Modifier.height(16.dp))
                            Divider(color = TextMuted.copy(alpha = 0.12f))
                            Spacer(modifier = Modifier.height(12.dp))

                            // Action buttons: Approve or Reject Sita Baral/Krishna BK
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(12.dp)
                            ) {
                                OutlinedButton(
                                    onClick = { viewModel.rejectWorkerVerification(worker.id) },
                                    modifier = Modifier.weight(1f),
                                    shape = RoundedCornerShape(8.dp),
                                    colors = ButtonDefaults.outlinedButtonColors(contentColor = DangerRed),
                                    border = BorderStroke(1.dp, DangerRed)
                                ) {
                                    Text("Decline", fontSize = 12.sp)
                                }
                                Button(
                                    onClick = { viewModel.approveWorkerVerification(worker.id) },
                                    modifier = Modifier.weight(1f).testTag("approve_verification_button"),
                                    shape = RoundedCornerShape(8.dp),
                                    colors = ButtonDefaults.buttonColors(containerColor = EscrowGreen)
                                ) {
                                    Text("Approve Documents", fontSize = 12.sp, color = TextLight, fontWeight = FontWeight.Bold)
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun AdminDisputesTab(viewModel: MarketplaceViewModel, onBookingSelected: (Long) -> Unit) {
    val bookings by viewModel.allBookings.collectAsStateWithLifecycle()
    val disputedBookings = bookings.filter { it.status == "DISPUTED" }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Text("Active Disputes Center", fontSize = 20.sp, fontWeight = FontWeight.Bold, color = TextLight)
        Text("Review job descriptions and chat records before confirming payouts.", fontSize = 13.sp, color = TextMuted)

        if (disputedBookings.isEmpty()) {
            Box(modifier = Modifier.weight(1f).fillMaxWidth(), contentAlignment = Alignment.Center) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Icon(Icons.Default.Scale, contentDescription = null, tint = TextMuted, modifier = Modifier.size(48.dp))
                    Spacer(modifier = Modifier.height(12.dp))
                    Text("Zero active dispute cases.", color = TextMuted)
                    Text("Transactions are executing smoothly without friction.", color = TextMuted, fontSize = 12.sp)
                }
            }
        } else {
            LazyColumn(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                items(disputedBookings) { booking ->
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { onBookingSelected(booking.id) },
                        colors = CardDefaults.cardColors(containerColor = CardDark)
                    ) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(booking.category, fontWeight = FontWeight.Bold, fontSize = 12.sp, color = TrustBlueLight)
                                Text("NRs. ${booking.agreedPrice}", fontSize = 14.sp, color = EscrowGreenLight, fontWeight = FontWeight.Bold)
                            }
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(booking.title, fontWeight = FontWeight.Bold, color = TextLight, fontSize = 15.sp)
                            Text("Customer: ${booking.customerName} | Worker: ${booking.workerName ?: "Awaiting match"}", fontSize = 12.sp, color = TextMuted)
                            Text("Reason: ${booking.description}", fontSize = 12.sp, color = WarningAmber, style = MaterialTheme.typography.bodyMedium)

                            Spacer(modifier = Modifier.height(16.dp))

                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(12.dp)
                            ) {
                                Button(
                                    onClick = { viewModel.resolveDisputeInFavor(booking.id, favorCustomer = true) },
                                    modifier = Modifier.weight(1f),
                                    shape = RoundedCornerShape(8.dp),
                                    colors = ButtonDefaults.buttonColors(containerColor = DangerRed)
                                ) {
                                    Text("Refund Customer", fontSize = 12.sp, color = TextLight)
                                }
                                Button(
                                    onClick = { viewModel.resolveDisputeInFavor(booking.id, favorCustomer = false) },
                                    modifier = Modifier.weight(1f),
                                    shape = RoundedCornerShape(8.dp),
                                    colors = ButtonDefaults.buttonColors(containerColor = EscrowGreen)
                                ) {
                                    Text("Pay Worker", fontSize = 12.sp, color = TextLight)
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun AdminLedgersTab(viewModel: MarketplaceViewModel) {
    val txs by viewModel.allTransactions.collectAsStateWithLifecycle()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Text("System ledgers Splits log", fontSize = 20.sp, fontWeight = FontWeight.Bold, color = TextLight)
        Text("Dynamic ledger listing the exact commission and escrow releases.", fontSize = 13.sp, color = TextMuted)

        if (txs.isEmpty()) {
            Box(modifier = Modifier.weight(1f).fillMaxWidth(), contentAlignment = Alignment.Center) {
                Text("Ledger is currently empty.", color = TextMuted)
            }
        } else {
            LazyColumn(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(txs) { tx ->
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(containerColor = CardDark)
                    ) {
                        Row(
                            modifier = Modifier.padding(12.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(horizontalArrangement = Arrangement.spacedBy(10.dp), verticalAlignment = Alignment.CenterVertically) {
                                Box(
                                    modifier = Modifier
                                        .size(32.dp)
                                        .clip(CircleShape)
                                        .background(
                                            when (tx.txType) {
                                                "COMMISSION" -> TrustBlue.copy(alpha = 0.15f)
                                                "DEPOSIT" -> EscrowGreen.copy(alpha = 0.15f)
                                                "WITHDRAWAL" -> DangerRed.copy(alpha = 0.15f)
                                                else -> WarningAmber.copy(alpha = 0.15f)
                                            }
                                        ),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(
                                        imageVector = when (tx.txType) {
                                            "COMMISSION" -> Icons.Default.Savings
                                            "DEPOSIT" -> Icons.Default.VerticalAlignBottom
                                            "WITHDRAWAL" -> Icons.Default.VerticalAlignTop
                                            else -> Icons.Default.Lock
                                        },
                                        contentDescription = null,
                                        tint = when (tx.txType) {
                                            "COMMISSION" -> TrustBlueLight
                                            "DEPOSIT" -> EscrowGreenLight
                                            "WITHDRAWAL" -> DangerRed
                                            else -> WarningAmber
                                        },
                                        modifier = Modifier.size(16.dp)
                                    )
                                }
                                Column {
                                    Text(tx.txType, fontWeight = FontWeight.Bold, fontSize = 13.sp, color = TextLight)
                                    Text("Ref ID: ${tx.referenceId}", fontSize = 11.sp, color = TextMuted)
                                    Text("UserId: ${tx.userId}", fontSize = 11.sp, color = TextMuted)
                                }
                            }
                            Text(
                                text = "NRs. ${tx.amount}",
                                fontWeight = FontWeight.Bold,
                                color = if (tx.txType == "DEPOSIT" || tx.txType == "COMMISSION") EscrowGreenLight else WarningAmber,
                                fontSize = 13.sp
                            )
                        }
                    }
                }
            }
        }
    }
}
