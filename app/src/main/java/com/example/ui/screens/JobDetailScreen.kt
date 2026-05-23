package com.example.ui.screens

import androidx.compose.animation.*
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.data.*
import com.example.ui.theme.*
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun JobDetailScreen(
    viewModel: MarketplaceViewModel,
    onBack: () -> Unit
) {
    val booking by viewModel.selectedBooking.collectAsStateWithLifecycle()
    val chatMessages by viewModel.chatMessages.collectAsStateWithLifecycle()
    val currentRole by viewModel.currentRole.collectAsStateWithLifecycle()
    val currentUserId by viewModel.currentUserId.collectAsStateWithLifecycle()

    var chatText by remember { mutableStateOf("") }
    var otpInput by remember { mutableStateOf("") }
    var disputeReason by remember { mutableStateOf("") }
    var ratingInput by remember { mutableStateOf(5) }
    var reviewInput by remember { mutableStateOf("") }

    var showDisputeDialog by remember { mutableStateOf(false) }

    val listState = rememberLazyListState()
    val scope = rememberCoroutineScope()

    // Scroll chat to bottom when message list expands
    LaunchedEffect(chatMessages.size) {
        if (chatMessages.isNotEmpty()) {
            listState.animateScrollToItem(chatMessages.size - 1)
        }
    }

    if (booking == null) {
        Box(modifier = Modifier.fillMaxSize().background(MidnightDark), contentAlignment = Alignment.Center) {
            CircularProgressIndicator(color = TrustBlue)
        }
        return
    }

    val b = booking!!

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text(b.category, fontSize = 11.sp, fontWeight = FontWeight.Bold, color = TrustBlueLight)
                        Text("Invoice ID: #${b.id}", fontSize = 14.sp, color = TextLight, fontWeight = FontWeight.Bold)
                    }
                },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back", tint = TextLight)
                    }
                },
                actions = {
                    if (b.status != "CLOSED" && b.status != "DISPUTED" && currentRole == "CUSTOMER") {
                        IconButton(onClick = { showDisputeDialog = true }) {
                            Icon(Icons.Default.Gavel, contentDescription = "Dispute", tint = DangerRed)
                        }
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = Slate900)
            )
        },
        containerColor = MidnightDark
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .windowInsetsPadding(WindowInsets.ime) // Keyboard support
        ) {
            // LazyColumn to encompass upper controls + historical details
            LazyColumn(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // 1. Current Progress Checklist Card
                item {
                    Spacer(modifier = Modifier.height(12.dp))
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(containerColor = CardDark),
                        border = BorderStroke(1.dp, TrustBlue.copy(alpha = 0.2f))
                    ) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text("WORK PROGRESS TRACKER", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = TextMuted)
                                Box(
                                    modifier = Modifier
                                        .clip(RoundedCornerShape(4.dp))
                                        .background(EscrowGreen.copy(alpha = 0.15f))
                                        .padding(horizontal = 8.dp, vertical = 4.dp)
                                ) {
                                    Text(b.status, color = EscrowGreenLight, fontWeight = FontWeight.Bold, fontSize = 11.sp)
                                }
                            }

                            Spacer(modifier = Modifier.height(12.dp))

                            // Steps visualizer
                            ProgressBarTimeline(status = b.status)

                            Spacer(modifier = Modifier.height(16.dp))

                            // Action components by Role
                            if (currentRole == "WORKER") {
                                WorkerActionControls(
                                    status = b.status,
                                    bookingId = b.id,
                                    otpCode = b.verificationOtp,
                                    viewModel = viewModel
                                )
                            } else if (currentRole == "CUSTOMER") {
                                CustomerActionControls(
                                    status = b.status,
                                    bookingId = b.id,
                                    otpCode = b.verificationOtp,
                                    rating = ratingInput,
                                    review = reviewInput,
                                    otpInput = otpInput,
                                    onOtpChange = { otpInput = it },
                                    onOtpSubmit = {
                                        val ok = viewModel.submitCompletionOtpAndReleaseFunds(b.id, otpInput)
                                        if (ok) {
                                            otpInput = ""
                                        }
                                    },
                                    onRatingChange = { ratingInput = it },
                                    onReviewChange = { reviewInput = it },
                                    onReviewSubmit = {
                                        viewModel.submitRating(b.id, ratingInput, reviewInput)
                                        reviewInput = ""
                                    }
                                )
                            } else {
                                // Admin Resolution Indicator if disputed
                                if (b.status == "DISPUTED") {
                                    Card(
                                        modifier = Modifier.fillMaxWidth(),
                                        colors = CardDefaults.cardColors(containerColor = DangerRed.copy(alpha = 0.1f)),
                                        border = BorderStroke(1.dp, DangerRed)
                                    ) {
                                        Column(modifier = Modifier.padding(12.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                                            Text("⚠️ DISPUTE LOG ACQUIRED", fontWeight = FontWeight.Bold, color = DangerRed)
                                            Spacer(modifier = Modifier.height(8.dp))
                                            Text("As supervisor, resolve this using the footer controls below:", fontSize = 12.sp, color = TextMuted)
                                            Spacer(modifier = Modifier.height(12.dp))
                                            Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                                                Button(
                                                    onClick = { viewModel.resolveDisputeInFavor(b.id, true) },
                                                    colors = ButtonDefaults.buttonColors(containerColor = DangerRed),
                                                    modifier = Modifier.weight(1f)
                                                ) { Text("Refund Client", fontSize = 11.sp, color = TextLight) }
                                                Button(
                                                    onClick = { viewModel.resolveDisputeInFavor(b.id, false) },
                                                    colors = ButtonDefaults.buttonColors(containerColor = EscrowGreen),
                                                    modifier = Modifier.weight(1f)
                                                ) { Text("Pay Worker", fontSize = 11.sp, color = TextLight) }
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }
                }

                // 2. Escrow Details Block
                item {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(containerColor = CardDark)
                    ) {
                        Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                            Text("SECURITY ESCROW RECEIPT", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = TextMuted)

                            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                                Text("Contract Value", color = TextMuted, fontSize = 13.sp)
                                Text("NRs. ${b.agreedPrice}", color = TextLight, fontWeight = FontWeight.Bold, fontSize = 14.sp)
                            }
                            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                                Text("Escrow Gateway provider", color = TextMuted, fontSize = 13.sp)
                                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                                    Box(
                                        modifier = Modifier
                                            .size(8.dp)
                                            .clip(CircleShape)
                                            .background(
                                                when (b.escrowProvider) {
                                                    "eSewa" -> ESewaGreen
                                                    "Khalti" -> KhaltiPurple
                                                    else -> FonepayRed
                                                }
                                            )
                                    )
                                    Text(b.escrowProvider, color = TextLight, fontWeight = FontWeight.SemiBold, fontSize = 13.sp)
                                }
                            }
                            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                                Text("Escrow status", color = TextMuted, fontSize = 13.sp)
                                Text(b.escrowStatus, color = EscrowGreenLight, fontWeight = FontWeight.Bold, fontSize = 13.sp)
                            }

                            Divider(color = TextMuted.copy(alpha = 0.12f))

                            Text("Details: ${b.description}", fontSize = 13.sp, color = TextLight)
                            Text("Location: ${b.locationAddress}", fontSize = 12.sp, color = TextMuted)
                        }
                    }
                }

                // 3. Encrypted Chat Log Header
                item {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text("Encrypted Job Chat Thread", fontSize = 14.sp, fontWeight = FontWeight.Bold, color = TextLight)
                        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                            Icon(Icons.Default.Lock, contentDescription = null, tint = EscrowGreen, modifier = Modifier.size(12.dp))
                            Text("AES-256 chat locks active", fontSize = 10.sp, color = EscrowGreenLight, fontWeight = FontWeight.Bold)
                        }
                    }
                }

                // 4. Nested chat message bubbles
                if (chatMessages.isEmpty()) {
                    item {
                        Box(modifier = Modifier.fillMaxWidth().height(100.dp), contentAlignment = Alignment.Center) {
                            Text("No chat logs recorded. Write a message below to start conversing secure!", color = TextMuted, fontSize = 12.sp)
                        }
                    }
                } else {
                    items(chatMessages) { message ->
                        if (message.isSystemMessage) {
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 4.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Box(
                                    modifier = Modifier
                                        .clip(RoundedCornerShape(8.dp))
                                        .background(Slate800)
                                        .padding(horizontal = 12.dp, vertical = 6.dp)
                                ) {
                                    Text(
                                        text = message.messageText,
                                        fontSize = 11.sp,
                                        color = TextMuted,
                                        textAlign = TextAlign.Center,
                                        lineHeight = 16.sp
                                    )
                                }
                            }
                        } else {
                            val isMe = message.senderId == currentUserId
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 4.dp),
                                horizontalArrangement = if (isMe) Arrangement.End else Arrangement.Start
                            ) {
                                Column(horizontalAlignment = if (isMe) Alignment.End else Alignment.Start) {
                                    Text(
                                        text = message.senderName,
                                        fontSize = 10.sp,
                                        color = TextMuted,
                                        fontWeight = FontWeight.SemiBold
                                    )
                                    Spacer(modifier = Modifier.height(2.dp))
                                    Box(
                                        modifier = Modifier
                                            .clip(
                                                RoundedCornerShape(
                                                    topStart = 12.dp,
                                                    topEnd = 12.dp,
                                                    bottomStart = if (isMe) 12.dp else 0.dp,
                                                    bottomEnd = if (isMe) 0.dp else 12.dp
                                                )
                                            )
                                            .background(if (isMe) TrustBlue else CardDark)
                                            .padding(horizontal = 12.dp, vertical = 8.dp)
                                    ) {
                                        Text(
                                            text = message.messageText,
                                            color = TextLight,
                                            fontSize = 14.sp
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }

            // Chat Input Bar
            Surface(
                color = Slate900,
                tonalElevation = 8.dp,
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier
                        .padding(12.dp)
                        .windowInsetsPadding(WindowInsets.navigationBars),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    OutlinedTextField(
                        value = chatText,
                        onValueChange = { chatText = it },
                        placeholder = { Text("Encrypted message...", color = TextMuted) },
                        modifier = Modifier
                            .weight(1f)
                            .testTag("chat_input_text_field"),
                        shape = RoundedCornerShape(24.dp),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedTextColor = TextLight,
                            focusedBorderColor = TrustBlue,
                            unfocusedTextColor = TextLight,
                            unfocusedBorderColor = TextMuted.copy(alpha = 0.3f)
                        ),
                        singleLine = true
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    IconButton(
                        onClick = {
                            if (chatText.isNotBlank()) {
                                viewModel.sendChatMessage(chatText)
                                chatText = ""
                            }
                        },
                        modifier = Modifier
                            .clip(CircleShape)
                            .background(TrustBlue)
                            .size(44.dp)
                            .testTag("send_chat_button")
                    ) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.Send,
                            contentDescription = "Send Message",
                            tint = TextLight
                        )
                    }
                }
            }
        }
    }

    // Dispute Filing Dialog
    if (showDisputeDialog) {
        AlertDialog(
            onDismissRequest = { showDisputeDialog = false },
            containerColor = CardDark,
            title = { Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                Icon(Icons.Default.Gavel, contentDescription = null, tint = DangerRed)
                Text("File Escrow Dispute Claim", color = TextLight, fontSize = 16.sp)
            } },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text("This freeze-locks the payment inside our cryptographic escrow. Administrators will inspect verification photos and chat history logs.", fontSize = 12.sp, color = TextMuted)
                    Spacer(modifier = Modifier.height(4.dp))
                    OutlinedTextField(
                        value = disputeReason,
                        onValueChange = { disputeReason = it },
                        label = { Text("Reason of dispute") },
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
                        if (disputeReason.isNotBlank()) {
                            viewModel.submitDispute(b.id, disputeReason)
                            showDisputeDialog = false
                            disputeReason = ""
                        }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = DangerRed)
                ) {
                    Text("Escalate Dispute", color = TextLight)
                }
            },
            dismissButton = {
                TextButton(onClick = { showDisputeDialog = false }) {
                    Text("Cancel", color = TextMuted)
                }
            }
        )
    }
}

// --- SUB-WIDGET COMPONENTS ---

@Composable
fun ProgressBarTimeline(status: String) {
    val steps = listOf("REQUESTED", "ACCEPTED", "ARRIVED", "STARTED", "COMPLETED", "CLOSED")
    val currentIndex = steps.indexOf(status)

    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        steps.forEachIndexed { i, step ->
            val active = i <= currentIndex
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Box(
                    modifier = Modifier
                        .size(16.dp)
                        .clip(CircleShape)
                        .background(if (active) EscrowGreen else Slate800)
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = when (step) {
                        "REQUESTED" -> "Req"
                        "ACCEPTED" -> "Acc"
                        "ARRIVED" -> "Arv"
                        "STARTED" -> "Wip"
                        "COMPLETED" -> "Fin"
                        "CLOSED" -> "Done"
                        else -> ""
                    },
                    fontSize = 10.sp,
                    color = if (active) TextLight else TextMuted,
                    fontWeight = if (active) FontWeight.Bold else FontWeight.Normal
                )
            }

            if (i < steps.size - 1) {
                Box(
                    modifier = Modifier
                        .height(2.dp)
                        .weight(1f)
                        .background(if (i < currentIndex) EscrowGreen else Slate800)
                )
            }
        }
    }
}

@Composable
fun WorkerActionControls(
    status: String,
    bookingId: Long,
    otpCode: String,
    viewModel: MarketplaceViewModel
) {
    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        when (status) {
            "ACCEPTED" -> {
                Button(
                    onClick = { viewModel.markArrived(bookingId) },
                    modifier = Modifier.fillMaxWidth(),
                    colors = ButtonDefaults.buttonColors(containerColor = TrustBlue)
                ) {
                    Icon(Icons.Default.Navigation, contentDescription = null, tint = TextLight)
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("Confirm arrival at Gate", fontWeight = FontWeight.Bold, color = TextLight)
                }
            }
            "ARRIVED" -> {
                Button(
                    onClick = { viewModel.startJob(bookingId) },
                    modifier = Modifier.fillMaxWidth(),
                    colors = ButtonDefaults.buttonColors(containerColor = WarningAmber)
                ) {
                    Icon(Icons.Default.PlayArrow, contentDescription = null, tint = TextLight)
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("Officially start Work", fontWeight = FontWeight.Bold, color = TextLight)
                }
            }
            "STARTED" -> {
                Button(
                    onClick = { viewModel.markCompleted(bookingId) },
                    modifier = Modifier.fillMaxWidth(),
                    colors = ButtonDefaults.buttonColors(containerColor = EscrowGreen)
                ) {
                    Icon(Icons.Default.Done, contentDescription = null, tint = TextLight)
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("Mark as Work Completed", fontWeight = FontWeight.Bold, color = TextLight)
                }
            }
            "COMPLETED" -> {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = WarningAmber.copy(alpha = 0.12f)),
                    border = BorderStroke(1.dp, WarningAmber)
                ) {
                    Column(modifier = Modifier.padding(12.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                        Text("Awaiting confirmation OTP release", color = WarningAmber, fontWeight = FontWeight.Bold)
                        Text("Please ask standard client for the release code verbal.", fontSize = 12.sp, color = TextMuted, textAlign = TextAlign.Center)
                    }
                }
            }
            "CLOSED" -> {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = EscrowGreen.copy(alpha = 0.12f))
                ) {
                    Row(
                        modifier = Modifier.padding(12.dp),
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(Icons.Default.CheckCircle, contentDescription = null, tint = EscrowGreenLight)
                        Text("Contract Closed. Funds transfered dynamically successfully!", color = EscrowGreenLight, fontWeight = FontWeight.Bold, fontSize = 13.sp)
                    }
                }
            }
        }
    }
}

@Composable
fun CustomerActionControls(
    status: String,
    bookingId: Long,
    otpCode: String,
    rating: Int,
    review: String,
    otpInput: String,
    onOtpChange: (String) -> Unit,
    onOtpSubmit: () -> Unit,
    onRatingChange: (Int) -> Unit,
    onReviewChange: (String) -> Unit,
    onReviewSubmit: () -> Unit
) {
    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        when (status) {
            "COMPLETED" -> {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = EscrowGreen.copy(alpha = 0.12f)),
                    border = BorderStroke(1.dp, EscrowGreen),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Column(modifier = Modifier.padding(16.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                        Text("Confirm and release payment", fontWeight = FontWeight.Bold, color = TextLight)
                        Spacer(modifier = Modifier.height(4.dp))
                        Text("The job is secure. Share this code with worker verbally, or type it here to disburse locked funds:", fontSize = 12.sp, color = TextMuted, textAlign = TextAlign.Center)
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(otpCode, fontSize = 28.sp, fontWeight = FontWeight.Bold, color = EscrowGreenLight, letterSpacing = 4.sp)
                        Spacer(modifier = Modifier.height(16.dp))

                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            OutlinedTextField(
                                value = otpInput,
                                onValueChange = onOtpChange,
                                placeholder = { Text("Type Code Check") },
                                modifier = Modifier.weight(1f).testTag("customer_otp_input"),
                                colors = OutlinedTextFieldDefaults.colors(
                                    focusedTextColor = TextLight,
                                    focusedBorderColor = EscrowGreen
                                )
                            )
                            Button(
                                onClick = onOtpSubmit,
                                colors = ButtonDefaults.buttonColors(containerColor = EscrowGreen),
                                shape = RoundedCornerShape(8.dp),
                                modifier = Modifier.testTag("submit_otp_release_button")
                            ) {
                                Text("Release Escrow Payout", color = TextLight, fontSize = 12.sp)
                            }
                        }
                    }
                }
            }
            "CLOSED" -> {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = CardDark)
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text("Verify worker quality (Verified Feedback)", fontWeight = FontWeight.Bold, color = TextLight, fontSize = 14.sp)
                        Text("This updates their public rating score instantly.", fontSize = 11.sp, color = TextMuted)
                        Spacer(modifier = Modifier.height(12.dp))

                        // Star ratings (1-5 Picker)
                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.fillMaxWidth()) {
                            for (stars in 1..5) {
                                val active = stars <= rating
                                IconButton(onClick = { onRatingChange(stars) }) {
                                    Icon(
                                        imageVector = if (active) Icons.Default.Star else Icons.Default.StarBorder,
                                        contentDescription = null,
                                        tint = if (active) WarningAmber else TextMuted
                                    )
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(8.dp))

                        OutlinedTextField(
                            value = review,
                            onValueChange = onReviewChange,
                            placeholder = { Text("Write review comment...", color = TextMuted) },
                            modifier = Modifier.fillMaxWidth().testTag("review_input_field"),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedTextColor = TextLight,
                                unfocusedTextColor = TextMuted
                            )
                        )

                        Spacer(modifier = Modifier.height(12.dp))

                        Button(
                            onClick = onReviewSubmit,
                            modifier = Modifier.fillMaxWidth().testTag("submit_review_button"),
                            colors = ButtonDefaults.buttonColors(containerColor = TrustBlue)
                        ) {
                            Text("Submit Verified feedback", color = TextLight)
                        }
                    }
                }
            }
        }
    }
}
