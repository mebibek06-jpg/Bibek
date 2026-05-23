package com.example.ui.screens

import androidx.compose.animation.*
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
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
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.data.*
import com.example.ui.theme.*

@Composable
fun CustomerDashboardScreen(
    viewModel: MarketplaceViewModel,
    onBookingSelected: (Long) -> Unit
) {
    var selectedTab by remember { mutableStateOf(0) } // 0: Home, 1: Bookings, 2: Wallet, 3: Profile

    Scaffold(
        bottomBar = {
            NavigationBar(
                containerColor = Slate900,
                modifier = Modifier.windowInsetsPadding(WindowInsets.navigationBars)
            ) {
                NavigationBarItem(
                    selected = selectedTab == 0,
                    onClick = { selectedTab = 0 },
                    icon = { Icon(Icons.Default.Home, contentDescription = "Home") },
                    label = { Text("Home", fontSize = 11.sp) },
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
                    icon = { Icon(Icons.Default.Construction, contentDescription = "History Bookings") },
                    label = { Text("Bookings", fontSize = 11.sp) },
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
                    icon = { Icon(Icons.Default.AccountBalanceWallet, contentDescription = "Wallet Balance") },
                    label = { Text("Wallet", fontSize = 11.sp) },
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
                    icon = { Icon(Icons.Default.Person, contentDescription = "Profile") },
                    label = { Text("Profile", fontSize = 11.sp) },
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
                0 -> CustomerHomeTab(viewModel, onBookingSelected)
                1 -> CustomerBookingsTab(viewModel, onBookingSelected)
                2 -> CustomerWalletTab(viewModel)
                3 -> CustomerProfileTab(viewModel)
            }
        }
    }
}

@Composable
fun CustomerHomeTab(viewModel: MarketplaceViewModel, onBookingSelected: (Long) -> Unit) {
    val bookings by viewModel.customerBookings.collectAsStateWithLifecycle()
    val workers by viewModel.allWorkers.collectAsStateWithLifecycle()
    val customer by viewModel.customerUser.collectAsStateWithLifecycle()

    var showRequestForm by remember { mutableStateOf<String?>(null) } // category name
    var showEmergencyDialog by remember { mutableStateOf(false) }
    var showAiDiagSheet by remember { mutableStateOf(false) }

    var searchQuery by remember { mutableStateOf("") }
    var filterOnlyElitePro by remember { mutableStateOf(false) }
    val allBookings by viewModel.allBookings.collectAsStateWithLifecycle()

    // Filter workers based on query and VIP elite badges
    val filteredWorkers = remember(workers, searchQuery, filterOnlyElitePro, allBookings) {
        workers.filter { worker ->
            val matchesQuery = searchQuery.isBlank() ||
                    worker.name.contains(searchQuery, ignoreCase = true) ||
                    worker.skills.contains(searchQuery, ignoreCase = true)
            val matchesFilter = !filterOnlyElitePro || worker.hasVerifiedBadge(allBookings)
            matchesQuery && matchesFilter
        }
    }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp),
        contentPadding = PaddingValues(top = 16.dp, bottom = 24.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Welcoming Customer
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = "Namaste,",
                        fontSize = 14.sp,
                        color = TextMuted
                    )
                    Text(
                        text = customer?.name ?: "Shyam Lal Nepal",
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Bold,
                        color = TextLight
                    )
                }

                // Balance summary
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(30.dp))
                        .background(Slate800)
                        .padding(horizontal = 12.dp, vertical = 6.dp)
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Icon(
                            Icons.Default.AccountBalanceWallet,
                            contentDescription = null,
                            tint = EscrowGreen,
                            modifier = Modifier.size(16.dp)
                        )
                        Text(
                            text = "NRs. ${customer?.walletBalance ?: 15000.0}",
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Bold,
                            color = TextLight
                        )
                    }
                }
            }
        }

        // Active Booking banner trigger
        val activeOngoing = bookings.firstOrNull { it.status != "CLOSED" && it.status != "CANCELLED" }
        if (activeOngoing != null) {
            item {
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { onBookingSelected(activeOngoing.id) },
                    colors = CardDefaults.cardColors(containerColor = Slate800),
                    border = BorderStroke(1.dp, TrustBlue.copy(alpha = 0.5f))
                ) {
                    Row(
                        modifier = Modifier.padding(16.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .size(40.dp)
                                .clip(CircleShape)
                                .background(TrustBlue.copy(alpha = 0.15f)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(Icons.Default.HourglassEmpty, contentDescription = null, tint = TrustBlue, modifier = Modifier.size(20.dp))
                        }
                        Column(modifier = Modifier.weight(1f)) {
                            Text("Ongoing Service Active", fontSize = 12.sp, color = TrustBlueLight, fontWeight = FontWeight.Bold)
                            Text(activeOngoing.title, fontSize = 14.sp, fontWeight = FontWeight.Medium, color = TextLight)
                            Text("Status: ${activeOngoing.status}", fontSize = 12.sp, color = TextMuted)
                        }
                        Icon(Icons.Default.ArrowForward, contentDescription = null, tint = TextMuted)
                    }
                }
            }
        }

        // Search Input Bar with AI Consulting Trigger Built-in
        item {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                OutlinedTextField(
                    value = searchQuery,
                    onValueChange = { searchQuery = it },
                    placeholder = { Text("Search electrician, plumber...", color = TextMuted, fontSize = 14.sp) },
                    leadingIcon = { Icon(Icons.Default.Search, contentDescription = null, tint = TextMuted) },
                    trailingIcon = {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            if (searchQuery.isNotEmpty()) {
                                IconButton(onClick = { searchQuery = "" }) {
                                    Icon(Icons.Default.Clear, contentDescription = "Clear search", tint = TextLight)
                                }
                            }
                            IconButton(
                                onClick = { showAiDiagSheet = true },
                                modifier = Modifier
                                    .clip(RoundedCornerShape(8.dp))
                                    .background(TrustBlue.copy(alpha = 0.12f))
                                    .padding(horizontal = 4.dp)
                            ) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(4.dp),
                                    modifier = Modifier.padding(horizontal = 4.dp)
                                ) {
                                    Icon(Icons.Default.AutoAwesome, contentDescription = null, tint = TrustBlueLight, modifier = Modifier.size(14.dp))
                                    Text("AI Help", fontSize = 11.sp, color = TrustBlueLight, fontWeight = FontWeight.Bold)
                                }
                            }
                        }
                    },
                    modifier = Modifier.fillMaxWidth().testTag("search_workers_input"),
                    shape = RoundedCornerShape(12.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedTextColor = TextLight,
                        unfocusedTextColor = TextLight,
                        focusedBorderColor = TrustBlue,
                        unfocusedBorderColor = CardDark,
                        focusedContainerColor = CardDark,
                        unfocusedContainerColor = CardDark
                    ),
                    singleLine = true
                )

                // Interactive Verified Elite Pro Filter Chip
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    FilterChip(
                        selected = filterOnlyElitePro,
                        onClick = { filterOnlyElitePro = !filterOnlyElitePro },
                        label = { Text("Show Elite Pro Badge Only (Top Experts)") },
                        leadingIcon = {
                            Icon(
                                imageVector = Icons.Default.WorkspacePremium,
                                contentDescription = null,
                                modifier = Modifier.size(16.dp),
                                tint = if (filterOnlyElitePro) Color(0xFFFFC107) else TextMuted
                            )
                        },
                        colors = FilterChipDefaults.filterChipColors(
                            containerColor = Slate800,
                            selectedContainerColor = TrustBlue.copy(alpha = 0.25f),
                            labelColor = TextMuted,
                            selectedLabelColor = TrustBlueLight
                        ),
                        modifier = Modifier.testTag("elite_filter_chip")
                    )
                }
            }
        }

        // Mode A: Render filtered list directory if searchQuery is active or filter is selected
        if (searchQuery.isNotEmpty() || filterOnlyElitePro) {
            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Vetted Search Results (${filteredWorkers.size})",
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold,
                        color = TextLight
                    )
                    if (searchQuery.isNotEmpty() || filterOnlyElitePro) {
                        Text(
                            text = "Clear",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            color = TrustBlueLight,
                            modifier = Modifier.clickable {
                                searchQuery = ""
                                filterOnlyElitePro = false
                            }
                        )
                    }
                }
            }

            if (filteredWorkers.isEmpty()) {
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
                            Text("No skilled technicians match your current filters.", color = TextMuted, fontSize = 13.sp)
                        }
                    }
                }
            } else {
                items(filteredWorkers) { worker ->
                    val hasEliteBadge = worker.hasVerifiedBadge(allBookings)
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(containerColor = CardDark),
                        border = if (hasEliteBadge) BorderStroke(1.dp, Color(0xFFFFC107).copy(alpha = 0.4f)) else null
                    ) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.Top
                            ) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                                ) {
                                    Box(
                                        modifier = Modifier
                                            .size(44.dp)
                                            .clip(CircleShape)
                                            .background(TrustBlue.copy(alpha = 0.15f)),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Icon(Icons.Default.Engineering, contentDescription = null, tint = TrustBlueLight)
                                    }
                                    Column {
                                        Text(worker.name, fontWeight = FontWeight.Bold, fontSize = 15.sp, color = TextLight)
                                        Text("Skills: ${worker.skills}", fontSize = 12.sp, color = TextMuted)
                                    }
                                }
                                
                                // Show Elite Badge prominently if eligible!
                                if (hasEliteBadge) {
                                    VerifiedBadge()
                                } else {
                                    Box(
                                        modifier = Modifier
                                            .clip(RoundedCornerShape(4.dp))
                                            .background(EscrowGreen.copy(alpha = 0.12f))
                                            .padding(horizontal = 6.dp, vertical = 2.dp)
                                    ) {
                                        Text("VERIFIED", fontSize = 10.sp, color = EscrowGreenLight, fontWeight = FontWeight.Bold)
                                    }
                                }
                            }

                            Spacer(modifier = Modifier.height(12.dp))
                            Divider(color = TextMuted.copy(alpha = 0.1f))
                            Spacer(modifier = Modifier.height(12.dp))

                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                                    Icon(Icons.Default.Star, contentDescription = null, tint = WarningAmber, modifier = Modifier.size(16.dp))
                                    Text(
                                        text = "${String.format("%.1f", worker.rating)} ★ (${worker.completedJobsCount} jobs completed)",
                                        fontSize = 13.sp,
                                        color = TextLight,
                                        fontWeight = FontWeight.Medium
                                    )
                                }

                                Button(
                                    onClick = { showRequestForm = worker.skills.split(",").firstOrNull()?.trim() ?: "Electrician" },
                                    colors = ButtonDefaults.buttonColors(containerColor = TrustBlue),
                                    shape = RoundedCornerShape(8.dp),
                                    contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp)
                                ) {
                                    Text("Instant Hire", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                                }
                            }
                        }
                    }
                }
            }

        } else {
            // Mode B: Natural State view (Emergency button, Categories grid and Nearby rows)

            // RED BUTTON: Emergency Match
            item {
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { showEmergencyDialog = true },
                    colors = CardDefaults.cardColors(containerColor = DangerRed.copy(alpha = 0.12f)),
                    border = BorderStroke(2.dp, DangerRed.copy(alpha = 0.4f)),
                    shape = RoundedCornerShape(16.dp)
                ) {
                    Row(
                        modifier = Modifier.padding(16.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .size(52.dp)
                                .clip(CircleShape)
                                .background(DangerRed.copy(alpha = 0.2f)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.Warning,
                                contentDescription = "Emergency Alarm",
                                tint = DangerRed,
                                modifier = Modifier.size(24.dp)
                            )
                        }
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = "Need Urgent Emergency Help?",
                                fontSize = 16.sp,
                                fontWeight = FontWeight.Bold,
                                color = DangerRed
                            )
                            Spacer(modifier = Modifier.height(2.dp))
                            Text(
                                text = "Locks nearest verified provider immediately for short circuits, flooding, structural hazards.",
                                fontSize = 12.sp,
                                color = TextMuted
                            )
                        }
                        Icon(Icons.Default.ArrowForward, contentDescription = null, tint = DangerRed)
                    }
                }
            }

            // Service Categories Grid
            item {
                Text(
                    text = "Services Categories",
                    fontSize = 17.sp,
                    fontWeight = FontWeight.Bold,
                    color = TextLight
                )
            }

            // 3x2 Service Category Grid
            item {
                val categories = listOf(
                    GridCategory("Electrician", Icons.Default.ElectricBolt, Color(0xFFEAB308)),
                    GridCategory("Plumber", Icons.Default.Water, Color(0xFF3B82F6)),
                    GridCategory("Mason", Icons.Default.CorporateFare, Color(0xFFF97316)),
                    GridCategory("Carpenter", Icons.Default.Carpenter, Color(0xFF10B981)),
                    GridCategory("Painter", Icons.Default.FormatPaint, Color(0xFFA855F7)),
                    GridCategory("Technician", Icons.Default.Router, Color(0xFF06B6D4))
                )

                Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    for (i in categories.indices step 2) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            CategoryCell(
                                category = categories[i],
                                modifier = Modifier.weight(1f),
                                onClick = { showRequestForm = categories[i].name }
                            )
                            if (i + 1 < categories.size) {
                                CategoryCell(
                                    category = categories[i + 1],
                                    modifier = Modifier.weight(1f),
                                    onClick = { showRequestForm = categories[i + 1].name }
                                )
                            }
                        }
                    }
                }
            }

            // Near workers list
            item {
                Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    Text(
                        text = "Trusted Verified Workers Nearby (GPS)",
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold,
                        color = TextLight
                    )

                    LazyRow(
                        horizontalArrangement = Arrangement.spacedBy(12.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        items(workers.filter { it.isVerified }) { worker ->
                            val hasEliteBadge = worker.hasVerifiedBadge(allBookings)
                            Card(
                                modifier = Modifier
                                    .width(220.dp)
                                    .padding(vertical = 4.dp),
                                colors = CardDefaults.cardColors(containerColor = CardDark),
                                border = if (hasEliteBadge) BorderStroke(1.dp, Color(0xFFFFC107).copy(alpha = 0.5f)) else null
                            ) {
                                Column(modifier = Modifier.padding(16.dp)) {
                                    Row(
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                                    ) {
                                        Box(
                                            modifier = Modifier
                                                .size(40.dp)
                                                .clip(CircleShape)
                                                .background(TrustBlue.copy(alpha = 0.15f)),
                                            contentAlignment = Alignment.Center
                                        ) {
                                            Icon(Icons.Default.Engineering, contentDescription = null, tint = TrustBlueLight)
                                        }
                                        Column {
                                            Text(worker.name, fontWeight = FontWeight.Bold, fontSize = 14.sp, color = TextLight, maxLines = 1)
                                            Text(worker.skills.split(",").firstOrNull() ?: "Specialist", fontSize = 12.sp, color = TextMuted)
                                        }
                                    }
                                    Spacer(modifier = Modifier.height(12.dp))
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                                            Icon(Icons.Default.Star, contentDescription = null, tint = WarningAmber, modifier = Modifier.size(16.dp))
                                            Text("${String.format("%.1f", worker.rating)}", fontSize = 13.sp, fontWeight = FontWeight.Bold, color = TextLight)
                                        }
                                        
                                        // Show Elite Badge prominently if eligible!
                                        if (hasEliteBadge) {
                                            VerifiedBadge()
                                        } else {
                                            Box(
                                                modifier = Modifier
                                                    .clip(RoundedCornerShape(4.dp))
                                                    .background(EscrowGreen.copy(alpha = 0.12f))
                                                    .padding(horizontal = 6.dp, vertical = 2.dp)
                                            ) {
                                                Text("VERIFIED", fontSize = 10.sp, color = EscrowGreenLight, fontWeight = FontWeight.Bold)
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    // Modal forms toggles
    showRequestForm?.let { category ->
        CreateRequestSheet(
            category = category,
            viewModel = viewModel,
            walletBalance = customer?.walletBalance ?: 15000.0,
            onDismiss = { showRequestForm = null },
            onSubmit = { title, desc, loc, urgency, isBidding, amt, gateway ->
                viewModel.submitServiceBooking(
                    category = category,
                    title = title,
                    description = desc,
                    location = loc,
                    urgency = urgency,
                    pricingMode = if (isBidding) "Bidding Mode" else "Fixed Price",
                    price = amt,
                    paymentProvider = gateway
                )
                showRequestForm = null
            }
        )
    }

    if (showEmergencyDialog) {
        EmergencyAlarmSheet(
            onDismiss = { showEmergencyDialog = false },
            onSubmit = { urgencyText, description ->
                viewModel.submitServiceBooking(
                    category = "Electrician",
                    title = "⚠️ URGENT EMERGENCY ELECTRICAL",
                    description = description,
                    location = "Kathmandu Valley Central Grid (GPS Lock)",
                    urgency = "Urgent (Need within 1 hour)",
                    pricingMode = "Fixed Price",
                    price = 1500.00,
                    paymentProvider = "Khalti"
                )
                showEmergencyDialog = false
            }
        )
    }

    if (showAiDiagSheet) {
        AiConsultantSheet(
            viewModel = viewModel,
            onDismiss = { showAiDiagSheet = false },
            onPostApproved = { category, evaluatedPrice, promptText ->
                viewModel.submitServiceBooking(
                    category = category,
                    title = "${category} Service Needed",
                    description = promptText,
                    location = "Current GPS Address Pin",
                    urgency = "Normal (Today)",
                    pricingMode = "Fixed Price",
                    price = evaluatedPrice,
                    paymentProvider = "eSewa"
                )
                showAiDiagSheet = false
            }
        )
    }
}

@Composable
fun CategoryCell(
    category: GridCategory,
    modifier: Modifier = Modifier,
    onClick: () -> Unit
) {
    Card(
        modifier = modifier
            .clickable(onClick = onClick)
            .height(90.dp),
        colors = CardDefaults.cardColors(containerColor = CardDark),
        shape = RoundedCornerShape(12.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(12.dp),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Box(
                modifier = Modifier
                    .size(36.dp)
                    .clip(CircleShape)
                    .background(category.color.copy(alpha = 0.15f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = category.icon,
                    contentDescription = category.name,
                    tint = category.color,
                    modifier = Modifier.size(20.dp)
                )
            }
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = category.name,
                fontWeight = FontWeight.SemiBold,
                fontSize = 13.sp,
                color = TextLight,
                textAlign = TextAlign.Center
            )
        }
    }
}

// --- SUB-SCREEN TABS ---

@Composable
fun CustomerBookingsTab(viewModel: MarketplaceViewModel, onBookingSelected: (Long) -> Unit) {
    val bookings by viewModel.customerBookings.collectAsStateWithLifecycle()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        Text("Your Booking Invoices", fontSize = 22.sp, fontWeight = FontWeight.Bold, color = TextLight)
        Spacer(modifier = Modifier.height(16.dp))

        if (bookings.isEmpty()) {
            Box(modifier = Modifier.weight(1f).fillMaxWidth(), contentAlignment = Alignment.Center) {
                Column(horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.Center) {
                    Icon(Icons.Default.HourglassDisabled, contentDescription = null, tint = TextMuted, modifier = Modifier.size(48.dp))
                    Spacer(modifier = Modifier.height(16.dp))
                    Text("No bookings registered yet.", color = TextMuted)
                    Text("Select a category above to post an escrow job!", color = TextMuted, fontSize = 12.sp)
                }
            }
        } else {
            LazyColumn(
                verticalArrangement = Arrangement.spacedBy(12.dp),
                modifier = Modifier.weight(1f)
            ) {
                items(bookings) { booking ->
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
                                Text(booking.category, fontSize = 12.sp, color = TrustBlueLight, fontWeight = FontWeight.Bold)
                                Box(
                                    modifier = Modifier
                                        .clip(RoundedCornerShape(4.dp))
                                        .background(
                                            when (booking.status) {
                                                "REQUESTED" -> WarningAmber.copy(alpha = 0.15f)
                                                "CLOSED" -> EscrowGreen.copy(alpha = 0.15f)
                                                "DISPUTED" -> DangerRed.copy(alpha = 0.15f)
                                                else -> TrustBlue.copy(alpha = 0.15f)
                                            }
                                        )
                                        .padding(horizontal = 8.dp, vertical = 4.dp)
                                ) {
                                    Text(
                                        text = booking.status,
                                        fontSize = 11.sp,
                                        color = when (booking.status) {
                                            "REQUESTED" -> WarningAmber
                                            "CLOSED" -> EscrowGreenLight
                                            "DISPUTED" -> DangerRed
                                            else -> TrustBlueLight
                                        },
                                        fontWeight = FontWeight.Bold
                                    )
                                }
                            }
                            Spacer(modifier = Modifier.height(8.dp))
                            Text(booking.title, fontWeight = FontWeight.Bold, fontSize = 15.sp, color = TextLight)
                            Text(booking.description, color = TextMuted, fontSize = 13.sp, maxLines = 2)

                            Spacer(modifier = Modifier.height(12.dp))
                            Divider(color = TextMuted.copy(alpha = 0.12f))
                            Spacer(modifier = Modifier.height(12.dp))

                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                                    Icon(Icons.Default.Lock, contentDescription = null, tint = EscrowGreen, modifier = Modifier.size(14.dp))
                                    Text("Escrow: NRs. ${booking.agreedPrice}", fontSize = 13.sp, fontWeight = FontWeight.SemiBold, color = EscrowGreen)
                                }
                                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                                    Icon(Icons.Default.Engineering, contentDescription = null, tint = TextMuted, modifier = Modifier.size(14.dp))
                                    Text(booking.workerName ?: "Awaiting match", fontSize = 13.sp, color = TextLight)
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
fun CustomerWalletTab(viewModel: MarketplaceViewModel) {
    val txs by viewModel.allTransactions.collectAsStateWithLifecycle()
    val customer by viewModel.customerUser.collectAsStateWithLifecycle()

    var showLoadAmtDialog by remember { mutableStateOf(false) }
    var selectedLoadProvider by remember { mutableStateOf("eSewa") }
    var loadAmount by remember { mutableStateOf("") }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(20.dp)
    ) {
        Text("Your Escrow Wallet", fontSize = 22.sp, fontWeight = FontWeight.Bold, color = TextLight)

        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(containerColor = Slate800),
            border = BorderStroke(1.dp, TrustBlue.copy(alpha = 0.3f))
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text("Lockable Pocket Balance", fontSize = 13.sp, color = TextMuted)
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = "NRs. ${customer?.walletBalance ?: 15000.0}",
                    fontSize = 32.sp,
                    fontWeight = FontWeight.Bold,
                    color = TextLight,
                    letterSpacing = 0.5.sp
                )
                Spacer(modifier = Modifier.height(20.dp))

                Text("Quick Load Sandbox Fund", fontSize = 11.sp, color = TextMuted, fontWeight = FontWeight.Bold)
                Spacer(modifier = Modifier.height(10.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Button(
                        onClick = { selectedLoadProvider = "eSewa"; showLoadAmtDialog = true },
                        colors = ButtonDefaults.buttonColors(containerColor = ESewaGreen),
                        shape = RoundedCornerShape(8.dp),
                        modifier = Modifier.weight(1f).height(44.dp)
                    ) {
                        Text("eSewa", fontWeight = FontWeight.Bold, color = Color.White)
                    }
                    Button(
                        onClick = { selectedLoadProvider = "Khalti"; showLoadAmtDialog = true },
                        colors = ButtonDefaults.buttonColors(containerColor = KhaltiPurple),
                        shape = RoundedCornerShape(8.dp),
                        modifier = Modifier.weight(1f).height(44.dp)
                    ) {
                        Text("Khalti", fontWeight = FontWeight.Bold, color = Color.White)
                    }
                    Button(
                        onClick = { selectedLoadProvider = "Fonepay"; showLoadAmtDialog = true },
                        colors = ButtonDefaults.buttonColors(containerColor = FonepayRed),
                        shape = RoundedCornerShape(8.dp),
                        modifier = Modifier.weight(1f).height(44.dp)
                    ) {
                        Text("Fonepay", fontWeight = FontWeight.Bold, color = Color.White)
                    }
                }
            }
        }

        Text("Secure Ledger logs", fontSize = 16.sp, fontWeight = FontWeight.Bold, color = TextLight)

        val filteredTxs = txs.filter { it.userId == "customer_shyam" || it.txType == "DEPOSIT" }
        if (filteredTxs.isEmpty()) {
            Box(modifier = Modifier.weight(1f).fillMaxWidth(), contentAlignment = Alignment.Center) {
                Text("No transaction logs registered.", color = TextMuted)
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
                                            if (tx.txType == "DEPOSIT") EscrowGreen.copy(alpha = 0.15f)
                                            else WarningAmber.copy(alpha = 0.15f)
                                        ),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(
                                        imageVector = if (tx.txType == "DEPOSIT") Icons.Default.AddCard else Icons.Default.Lock,
                                        contentDescription = null,
                                        tint = if (tx.txType == "DEPOSIT") EscrowGreenLight else WarningAmber,
                                        modifier = Modifier.size(18.dp)
                                    )
                                }
                                Column {
                                    Text(tx.txType, fontWeight = FontWeight.Bold, fontSize = 13.sp, color = TextLight)
                                    Text("Ref: ${tx.referenceId}", fontSize = 11.sp, color = TextMuted)
                                    Text("via ${tx.provider}", fontSize = 11.sp, color = TextMuted)
                                }
                            }
                            Column(horizontalAlignment = Alignment.End) {
                                Text(
                                    text = "${if (tx.txType == "DEPOSIT") "+" else "-"} NRs. ${tx.amount}",
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 14.sp,
                                    color = if (tx.txType == "DEPOSIT") EscrowGreenLight else WarningAmber
                                )
                                Text(
                                    tx.status,
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = EscrowGreenLight
                                )
                            }
                        }
                    }
                }
            }
        }
    }

    if (showLoadAmtDialog) {
        AlertDialog(
            onDismissRequest = { showLoadAmtDialog = false },
            containerColor = CardDark,
            title = { Text("Load Sandbox Balance", color = TextLight) },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text("Toping up wallet via Nepalese Gateway channel: $selectedLoadProvider", fontSize = 13.sp, color = TextMuted)
                    Spacer(modifier = Modifier.height(4.dp))
                    OutlinedTextField(
                        value = loadAmount,
                        onValueChange = { loadAmount = it },
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
                        val amt = loadAmount.toDoubleOrNull() ?: 0.0
                        if (amt > 0) {
                            viewModel.simulatedWalletDeposit(selectedLoadProvider, amt)
                            showLoadAmtDialog = false
                            loadAmount = ""
                        }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = EscrowGreen)
                ) {
                    Text("Simulate Payment Check", color = TextLight)
                }
            }
        )
    }
}

@Composable
fun CustomerProfileTab(viewModel: MarketplaceViewModel) {
    val customer by viewModel.customerUser.collectAsStateWithLifecycle()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Box(
            modifier = Modifier
                .size(90.dp)
                .clip(CircleShape)
                .background(TrustBlue.copy(alpha = 0.12f)),
            contentAlignment = Alignment.Center
        ) {
            Icon(Icons.Default.Person, contentDescription = null, tint = TrustBlue, modifier = Modifier.size(48.dp))
        }

        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text(
                customer?.name ?: "Shyam Lal Nepal",
                fontWeight = FontWeight.Bold,
                fontSize = 20.sp,
                color = TextLight
            )
            Text(
                customer?.phone ?: "+977 9801889922",
                color = TextMuted,
                fontSize = 14.sp
            )
        }

        Row(
            modifier = Modifier
                .clip(RoundedCornerShape(8.dp))
                .background(EscrowGreen.copy(alpha = 0.12f))
                .padding(horizontal = 12.dp, vertical = 6.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            Icon(Icons.Default.VerifiedUser, contentDescription = null, tint = EscrowGreen, modifier = Modifier.size(16.dp))
            Text("DEVICE LOCK ASSURED: SECURE BINDING ON", fontSize = 11.sp, color = EscrowGreenLight, fontWeight = FontWeight.Bold)
        }

        Spacer(modifier = Modifier.height(16.dp))

        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(containerColor = CardDark)
        ) {
            Column {
                ProfileMenuListItem(Icons.Default.Home, "Saved Work Gate Coordinates", "Main Gate, Kalanki, Kathmandu")
                Divider(color = TextMuted.copy(alpha = 0.12f))
                ProfileMenuListItem(Icons.Default.ContactPhone, "Identity Verification Status", "Approved & Verified Citizens Record")
                Divider(color = TextMuted.copy(alpha = 0.12f))
                ProfileMenuListItem(Icons.Default.Shield, "Fraud Detection Index Profile", "Score: 99/100 (Safe Transactor)")
                Divider(color = TextMuted.copy(alpha = 0.12f))
                ProfileMenuListItem(Icons.Default.Settings, "Security Preferences settings", "AES-256 chat locks active")
            }
        }
    }
}

data class GridCategory(
    val name: String,
    val icon: androidx.compose.ui.graphics.vector.ImageVector,
    val color: androidx.compose.ui.graphics.Color
)

@Composable
fun ProfileMenuListItem(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    title: String,
    subtitle: String
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { }
            .padding(16.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Box(
            modifier = Modifier
                .size(36.dp)
                .clip(CircleShape)
                .background(TrustBlue.copy(alpha = 0.12f)),
            contentAlignment = Alignment.Center
        ) {
            Icon(icon, contentDescription = null, tint = TrustBlue, modifier = Modifier.size(20.dp))
        }
        Column(modifier = Modifier.weight(1f)) {
            Text(title, fontWeight = FontWeight.Bold, fontSize = 14.sp, color = TextLight)
            Text(subtitle, fontSize = 12.sp, color = TextMuted)
        }
        Icon(Icons.Default.ArrowForward, contentDescription = null, tint = TextMuted, modifier = Modifier.size(16.dp))
    }
}

@Composable
fun VerifiedBadge(modifier: Modifier = Modifier) {
    Box(
        modifier = modifier
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
            .testTag("verified_badge")
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


