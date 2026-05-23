package com.example.ui.screens

import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
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

@OptIn(ExperimentalLayoutApi::class, ExperimentalMaterial3Api::class)
@Composable
fun CreateRequestSheet(
    category: String,
    viewModel: MarketplaceViewModel,
    walletBalance: Double,
    onDismiss: () -> Unit,
    onSubmit: (title: String, desc: String, loc: String, urgency: String, isBidding: Boolean, price: Double, provider: String) -> Unit
) {
    var title by remember { mutableStateOf("") }
    var description by remember { mutableStateOf("") }
    var locationAddress by remember { mutableStateOf("Gate Coordinates, Mid-Town Kathmandu") }
    var selectedUrgency by remember { mutableStateOf("Normal (Today)") }
    var isBiddingMode by remember { mutableStateOf(false) }
    var budgetAmount by remember { mutableStateOf("1200") }
    var selectedProvider by remember { mutableStateOf("Khalti") }

    val allWorkers by viewModel.allWorkers.collectAsStateWithLifecycle()
    val allBookings by viewModel.allBookings.collectAsStateWithLifecycle()

    val pricingResult = remember(category, selectedUrgency, allWorkers, allBookings) {
        DynamicPricingEngine.calculateDynamicPrice(category, selectedUrgency, System.currentTimeMillis(), allWorkers, allBookings)
    }

    LaunchedEffect(pricingResult) {
        budgetAmount = pricingResult.recommendedBest.toInt().toString()
    }

    AlertDialog(
        onDismissRequest = onDismiss,
        properties = androidx.compose.ui.window.DialogProperties(usePlatformDefaultWidth = false),
        modifier = Modifier
            .fillMaxSize()
            .background(MidnightDark),
        content = {
            Scaffold(
                topBar = {
                    TopAppBar(
                        title = { Text("Request $category Expert", color = TextLight, fontWeight = FontWeight.Bold) },
                        navigationIcon = {
                            IconButton(onClick = onDismiss) {
                                Icon(Icons.Default.Close, contentDescription = "Close", tint = TextLight)
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
                        .verticalScroll(rememberScrollState())
                        .padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    // Title Text Field
                    OutlinedTextField(
                        value = title,
                        onValueChange = { title = it },
                        label = { Text("Short Job Title (e.g., Short circuit fix)") },
                        modifier = Modifier.fillMaxWidth().testTag("job_title_input"),
                        singleLine = true,
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedTextColor = TextLight,
                            focusedBorderColor = TrustBlue,
                            unfocusedTextColor = TextMuted
                        )
                    )

                    // Description text input
                    OutlinedTextField(
                        value = description,
                        onValueChange = { description = it },
                        label = { Text("What needs immediate attention? Describe problem detail") },
                        modifier = Modifier.fillMaxWidth().height(100.dp).testTag("job_desc_input"),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedTextColor = TextLight,
                            focusedBorderColor = TrustBlue,
                            unfocusedTextColor = TextMuted
                        )
                    )

                    // Address Pin Location Simulator
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(containerColor = CardDark)
                    ) {
                        Row(modifier = Modifier.padding(12.dp), verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            Icon(Icons.Default.LocationOn, contentDescription = null, tint = DangerRed)
                            Column(modifier = Modifier.weight(1f)) {
                                Text("Map pin Location GPS", fontWeight = FontWeight.Bold, fontSize = 13.sp, color = TextLight)
                                OutlinedTextField(
                                    value = locationAddress,
                                    onValueChange = { locationAddress = it },
                                    singleLine = true,
                                    colors = OutlinedTextFieldDefaults.colors(focusedTextColor = TextLight, unfocusedTextColor = TextMuted)
                                )
                            }
                        }
                    }

                    // Urgency Matrix Choice
                    Text("Urgency Level Plan", fontWeight = FontWeight.Bold, color = TextLight, fontSize = 14.sp)
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        listOf("Urgent (Need within 1 hour)", "Normal (Today)", "Schedule later").forEach { option ->
                            val active = selectedUrgency == option
                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(8.dp))
                                    .background(if (active) TrustBlue.copy(alpha = 0.2f) else CardDark)
                                    .border(1.dp, if (active) TrustBlue else Color.Transparent, RoundedCornerShape(8.dp))
                                    .clickable { selectedUrgency = option }
                                    .padding(horizontal = 12.dp, vertical = 8.dp)
                            ) {
                                Text(
                                    text = option.split(" ").first(),
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = if (active) TrustBlueLight else TextMuted
                                )
                            }
                        }
                    }

                    // AI Dynamic Pricing Estimate Card
                    Card(
                        modifier = Modifier.fillMaxWidth().testTag("ai_pricing_advice_card"),
                        colors = CardDefaults.cardColors(containerColor = TrustBlue.copy(alpha = 0.08f)),
                        border = BorderStroke(1.dp, TrustBlue.copy(alpha = 0.25f))
                    ) {
                        Column(modifier = Modifier.padding(14.dp)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                                    Icon(Icons.Default.AutoAwesome, contentDescription = "AI Pricing", tint = TrustBlueLight, modifier = Modifier.size(16.dp))
                                    Text("AI Real-time Pricing Intelligence", fontWeight = FontWeight.Bold, color = TrustBlueLight, fontSize = 13.sp)
                                }
                                Box(
                                    modifier = Modifier
                                        .clip(RoundedCornerShape(4.dp))
                                        .background(TrustBlue.copy(alpha = 0.2f))
                                        .padding(horizontal = 6.dp, vertical = 2.dp)
                                ) {
                                    Text("DYNAMIC MATCH", fontSize = 9.sp, color = TrustBlueLight, fontWeight = FontWeight.Bold)
                                }
                            }
                            
                            Spacer(modifier = Modifier.height(10.dp))
                            
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Column {
                                    Text("Recommended Rate Range", fontSize = 11.sp, color = TextMuted)
                                    Text("NRs. ${pricingResult.recommendedMin.toInt()} - ${pricingResult.recommendedMax.toInt()}", fontSize = 17.sp, fontWeight = FontWeight.ExtraBold, color = TextLight)
                                }
                                OutlinedButton(
                                    onClick = { budgetAmount = pricingResult.recommendedBest.toInt().toString() },
                                    colors = ButtonDefaults.outlinedButtonColors(contentColor = TrustBlueLight),
                                    border = BorderStroke(1.dp, TrustBlue.copy(alpha = 0.4f)),
                                    contentPadding = PaddingValues(horizontal = 10.dp, vertical = 4.dp),
                                    shape = RoundedCornerShape(8.dp)
                                ) {
                                    Text("Reset to NRs. ${pricingResult.recommendedBest.toInt()}", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                                }
                            }
                            
                            Spacer(modifier = Modifier.height(8.dp))
                            Text(pricingResult.explanation, fontSize = 11.sp, color = TextMuted, lineHeight = 14.sp)
                        }
                    }

                    // Pricing Mode Toggle
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text("Bidding mode (Workers bid offers)", fontSize = 13.sp, fontWeight = FontWeight.Bold, color = TextLight)
                            Text("When off, locks fixed base escrow price", fontSize = 11.sp, color = TextMuted)
                        }
                        Switch(
                            checked = isBiddingMode,
                            onCheckedChange = { isBiddingMode = it },
                            colors = SwitchDefaults.colors(checkedTrackColor = TrustBlue)
                        )
                    }

                    // Budget Value entry
                    OutlinedTextField(
                        value = budgetAmount,
                        onValueChange = { budgetAmount = it },
                        label = { Text("Target Base Budget (NRs.)") },
                        modifier = Modifier.fillMaxWidth().testTag("job_budget_input"),
                        colors = OutlinedTextFieldDefaults.colors(focusedTextColor = TextLight, focusedBorderColor = TrustBlue, unfocusedTextColor = TextMuted)
                    )

                    // Secure Gateway holding selection
                    Text("Select Holding Wallet Escrow Provider", fontWeight = FontWeight.Bold, color = TextLight, fontSize = 14.sp)
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        listOf("eSewa", "Khalti", "Fonepay").forEach { provider ->
                            val isChosen = selectedProvider == provider
                            val bgColors = when (provider) {
                                "eSewa" -> ESewaGreen
                                "Khalti" -> KhaltiPurple
                                else -> FonepayRed
                            }
                            Card(
                                modifier = Modifier
                                    .weight(1f)
                                    .clickable { selectedProvider = provider },
                                colors = CardDefaults.cardColors(containerColor = if (isChosen) bgColors.copy(alpha = 0.15f) else CardDark),
                                border = if (isChosen) BorderStroke(1.5.dp, bgColors) else null
                            ) {
                                Column(modifier = Modifier.padding(12.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                                    Text(provider, fontWeight = FontWeight.Bold, color = if (isChosen) bgColors else TextLight, fontSize = 14.sp)
                                }
                            }
                        }
                    }

                    // Escrow Split summary
                    val parsedPrice = budgetAmount.toDoubleOrNull() ?: 1200.0
                    val platformFee = 50.0
                    val contractTotal = parsedPrice + platformFee

                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(containerColor = Slate800)
                    ) {
                        Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                            Text("TRUST FINANCIAL LEDGER BREAKDOWN", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = TextMuted)
                            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                                Text("Agreement Contract Lock", fontSize = 13.sp, color = TextMuted)
                                Text("NRs. $parsedPrice", fontSize = 13.sp, color = TextLight)
                            }
                            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                                Text("Anti-Fraud Escrow Insurance", fontSize = 13.sp, color = TextMuted)
                                Text("NRs. $platformFee", fontSize = 13.sp, color = TextLight)
                            }
                            Divider(color = TextMuted.copy(alpha = 0.1f))
                            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                                Text("Total Locked in Escrow Pool", fontSize = 14.sp, fontWeight = FontWeight.Bold, color = TextLight)
                                Text("NRs. $contractTotal", fontSize = 14.sp, fontWeight = FontWeight.Bold, color = EscrowGreenLight)
                            }
                        }
                    }

                    // Submitting dispatch or checking balance
                    val hasFunds = walletBalance >= contractTotal

                    if (!hasFunds) {
                        Text(
                            text = "⚠️ Insufficient Wallet Balance. (Balance: NRs. $walletBalance, Required: NRs. $contractTotal). Top up Wallet first in Wallet Tab!",
                            color = DangerRed,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            textAlign = TextAlign.Center,
                            modifier = Modifier.fillMaxWidth()
                        )
                    }

                    Button(
                        onClick = {
                            if (hasFunds && title.isNotBlank() && description.isNotBlank()) {
                                onSubmit(title, description, locationAddress, selectedUrgency, isBiddingMode, parsedPrice, selectedProvider)
                            }
                        },
                        enabled = hasFunds && title.isNotBlank() && description.isNotBlank(),
                        colors = ButtonDefaults.buttonColors(containerColor = EscrowGreen),
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(50.dp)
                            .testTag("submit_request_button")
                    ) {
                        Icon(Icons.Default.Lock, contentDescription = null, tint = TextLight, modifier = Modifier.size(18.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("Secure Escrow Payment & Find Worker", fontWeight = FontWeight.Bold, color = TextLight)
                    }
                }
            }
        }
    )
}

@Composable
fun EmergencyAlarmSheet(
    onDismiss: () -> Unit,
    onSubmit: (urgencyText: String, description: String) -> Unit
) {
    var descInput by remember { mutableStateOf("") }
    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = CardDark,
        title = {
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp), verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Default.Warning, contentDescription = null, tint = DangerRed)
                Text("Rapid Emergency Dispatch Mode", color = DangerRed, fontWeight = FontWeight.Bold, fontSize = 18.sp)
            }
        },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                Text("This triggers a high-priority coordinate alarm. Locks the nearest active vetted responder instantly. Fixed Escrow NRs. 1,500 will apply.", fontSize = 13.sp, color = TextMuted)
                OutlinedTextField(
                    value = descInput,
                    onValueChange = { descInput = it },
                    placeholder = { Text("What is the hazard? (e.g. electrical fire sparking, pipes burst flooding kitchen)") },
                    modifier = Modifier.fillMaxWidth(),
                    colors = OutlinedTextFieldDefaults.colors(focusedTextColor = TextLight, unfocusedBorderColor = DangerRed.copy(alpha = 0.5f))
                )
            }
        },
        confirmButton = {
            Button(
                onClick = { if (descInput.isNotBlank()) onSubmit("Urgent (Need within 1 hour)", descInput) },
                colors = ButtonDefaults.buttonColors(containerColor = DangerRed),
                enabled = descInput.isNotBlank()
            ) {
                Text("Trigger Alarm Dispatch", color = TextLight)
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel", color = TextMuted)
            }
        }
    )
}

@OptIn(ExperimentalLayoutApi::class, ExperimentalMaterial3Api::class)
@Composable
fun AiConsultantSheet(
    viewModel: MarketplaceViewModel,
    onDismiss: () -> Unit,
    onPostApproved: (category: String, evaluatedPrice: Double, promptText: String) -> Unit
) {
    var prompt by remember { mutableStateOf("") }
    val geminiResult by viewModel.geminiResultState.collectAsStateWithLifecycle()
    val loading by viewModel.geminiLoading.collectAsStateWithLifecycle()

    AlertDialog(
        onDismissRequest = onDismiss,
        properties = androidx.compose.ui.window.DialogProperties(usePlatformDefaultWidth = false),
        modifier = Modifier
            .fillMaxSize()
            .background(MidnightDark),
        content = {
            Scaffold(
                topBar = {
                    TopAppBar(
                        title = { Text("Sajha AI Diagnostic Consultant", color = TextLight, fontWeight = FontWeight.Bold) },
                        navigationIcon = {
                            IconButton(onClick = onDismiss) {
                                Icon(Icons.Default.Close, contentDescription = "Close", tint = TextLight)
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
                        .verticalScroll(rememberScrollState())
                        .padding(20.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    Text("Describe house repair issue (Nepali & English mix is fully acceptable):", fontSize = 14.sp, color = TextMuted)
                    
                    OutlinedTextField(
                        value = prompt,
                        onValueChange = { prompt = it },
                        placeholder = { Text("Example: Kitchen pipe leakage le ghar ko wooden cabinet damage huna lagyo. Base price and diagnostic analysis chaiyo...") },
                        modifier = Modifier.fillMaxWidth().height(120.dp),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedTextColor = TextLight,
                            focusedBorderColor = TrustBlue,
                            unfocusedBorderColor = TextMuted.copy(alpha = 0.3f)
                        )
                    )

                    Button(
                        onClick = { viewModel.runGeminiDiagnostic(prompt) },
                        modifier = Modifier.fillMaxWidth(),
                        colors = ButtonDefaults.buttonColors(containerColor = TrustBlue),
                        enabled = prompt.isNotBlank() && !loading
                    ) {
                        Icon(Icons.Default.AutoAwesome, contentDescription = null, tint = TextLight)
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("Get Smart AI Diagnostics Plan", fontWeight = FontWeight.Bold, color = TextLight)
                    }

                    if (loading) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(200.dp),
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.Center
                        ) {
                            CircularProgressIndicator(color = TrustBlue)
                            Spacer(modifier = Modifier.height(12.dp))
                            Text("Generative AI Model is Analyzing problem...", color = TextMuted, fontSize = 12.sp)
                        }
                    }

                    // Render Gemini result card or fallback
                    geminiResult?.let { result ->
                        if (result == "API_KEY_MISSING") {
                            Card(
                                modifier = Modifier.fillMaxWidth(),
                                colors = CardDefaults.cardColors(containerColor = CardDark)
                            ) {
                                Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                                    Row(horizontalArrangement = Arrangement.spacedBy(6.dp), verticalAlignment = Alignment.CenterVertically) {
                                        Icon(Icons.Default.AutoAwesome, contentDescription = null, tint = WarningAmber)
                                        Text("AI Diagnostic: Smart Evaluation Sample", fontWeight = FontWeight.Bold, color = TextLight)
                                    }
                                    Text("Category recommendation: PLUMBER", fontSize = 14.sp, fontWeight = FontWeight.Bold, color = TrustBlueLight)
                                    Text("Suggested Base Escrow Amount: NRs. 1,500.00", fontSize = 13.sp, color = EscrowGreenLight, fontWeight = FontWeight.Bold)

                                    Divider(color = TextMuted.copy(alpha = 0.1f))

                                    Text("Analysis: Leakage in pipe has high risk of damaging masonry and structural wood layers. Complete valve shut off is advised prior to plumbing arrives.", fontSize = 13.sp, color = TextMuted)

                                    Box(
                                        modifier = Modifier
                                            .clip(RoundedCornerShape(6.dp))
                                            .background(WarningAmber.copy(alpha = 0.12f))
                                            .padding(8.dp)
                                    ) {
                                        Text("Notice: Enter your GEMINI_API_KEY in the AI Studio Secrets panel to enable real intelligent AI consults!", color = WarningAmber, fontSize = 11.sp, fontWeight = FontWeight.Bold, textAlign = TextAlign.Center)
                                    }

                                    Button(
                                        onClick = { onPostApproved("Plumber", 1500.00, prompt) },
                                        colors = ButtonDefaults.buttonColors(containerColor = EscrowGreen),
                                        modifier = Modifier.fillMaxWidth()
                                    ) {
                                        Text("Approve Recommended Diagnostics", color = TextLight, fontWeight = FontWeight.Bold)
                                    }
                                }
                            }
                        } else {
                            Card(
                                modifier = Modifier.fillMaxWidth(),
                                colors = CardDefaults.cardColors(containerColor = Slate800)
                            ) {
                                Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                                    Row(horizontalArrangement = Arrangement.spacedBy(6.dp), verticalAlignment = Alignment.CenterVertically) {
                                        Icon(Icons.Default.AutoAwesome, contentDescription = null, tint = TrustBlueLight)
                                        Text("Intelligent Live Match Assessment Report", fontWeight = FontWeight.Bold, color = TextLight)
                                    }

                                    Text(result, color = TextLight, fontSize = 14.sp, style = MaterialTheme.typography.bodyMedium)

                                    Divider(color = TextMuted.copy(alpha = 0.1f))

                                    // Auto parse category from result string to let user test immediately!
                                    val matchedCategory = when {
                                        result.contains("Plumber", ignoreCase = true) -> "Plumber"
                                        result.contains("Electrician", ignoreCase = true) -> "Electrician"
                                        result.contains("Mason", ignoreCase = true) -> "Mason"
                                        result.contains("Carpenter", ignoreCase = true) -> "Carpenter"
                                        result.contains("Painter", ignoreCase = true) -> "Painter"
                                        else -> "Technician"
                                    }

                                    val matchedPrice = when {
                                        result.contains("NRs. 500", ignoreCase = true) || result.contains("500", ignoreCase = true) -> 500.00
                                        result.contains("NRs. 1000", ignoreCase = true) || result.contains("1000", ignoreCase = true) -> 1000.00
                                        result.contains("NRs. 1500", ignoreCase = true) || result.contains("1500", ignoreCase = true) -> 1500.00
                                        result.contains("NRs. 2000", ignoreCase = true) || result.contains("2000", ignoreCase = true) -> 2000.00
                                        else -> 1200.00
                                    }

                                    Button(
                                        onClick = { onPostApproved(matchedCategory, matchedPrice, prompt) },
                                        colors = ButtonDefaults.buttonColors(containerColor = EscrowGreen),
                                        modifier = Modifier.fillMaxWidth()
                                    ) {
                                        Text("Accept Advice & Create Booking", color = TextLight, fontWeight = FontWeight.Bold)
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    )
}
