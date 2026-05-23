package com.example.ui.screens

import androidx.compose.animation.*
import androidx.compose.animation.core.tween
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.*
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

@Composable
fun SplashScreen(onTimeout: () -> Unit) {
    LaunchedEffect(Unit) {
        delay(2500)
        onTimeout()
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                brush = Brush.verticalGradient(
                    colors = listOf(MidnightDark, Slate950)
                )
            ),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(24.dp))
                    .background(
                        Brush.radialGradient(
                            colors = listOf(Slate800, DarkCharcoal)
                        )
                    )
                    .padding(horizontal = 24.dp, vertical = 20.dp),
                contentAlignment = Alignment.Center
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.Handshake,
                        contentDescription = "Handshake logo icon",
                        tint = BrandOrange,
                        modifier = Modifier
                            .size(32.dp)
                            .padding(end = 6.dp)
                    )

                    Box(
                        contentAlignment = Alignment.Center,
                        modifier = Modifier.padding(end = 1.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Place,
                            contentDescription = null,
                            tint = BrandOrange.copy(alpha = 0.85f),
                            modifier = Modifier
                                .size(24.dp)
                                .offset(y = (-9).dp, x = (-4).dp)
                        )
                        Text(
                            text = "K",
                            fontSize = 32.sp,
                            fontWeight = FontWeight.Black,
                            color = TrustBlueLight,
                            letterSpacing = 0.sp
                        )
                    }

                    Text(
                        text = "aam",
                        fontSize = 32.sp,
                        fontWeight = FontWeight.ExtraBold,
                        color = TextLight,
                        letterSpacing = 0.sp
                    )

                    Surface(
                        color = BrandOrange,
                        shape = RoundedCornerShape(8.dp),
                        modifier = Modifier.padding(start = 4.dp)
                    ) {
                        Text(
                            text = "Chha",
                            fontSize = 28.sp,
                            fontWeight = FontWeight.Black,
                            color = Color.White,
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp),
                            letterSpacing = 0.sp
                        )
                    }
                }
            }
            Spacer(modifier = Modifier.height(24.dp))
            Text(
                text = "On-Demand Escrow Service Marketplace",
                fontSize = 14.sp,
                color = TextMuted,
                textAlign = TextAlign.Center
            )
            Spacer(modifier = Modifier.height(48.dp))
            CircularProgressIndicator(
                color = BrandOrange,
                strokeWidth = 3.dp,
                modifier = Modifier.size(24.dp)
            )
        }
    }
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun OnboardingScreen(onFinished: () -> Unit) {
    val pagerState = rememberPagerState(pageCount = { 3 })
    val scope = rememberCoroutineScope()

    val slides = listOf(
        OnboardingSlide(
            title = "Nepal's First Escrow-Armed Trust Platform",
            description = "We solve the trust deficit. Your payments are held securely in a digital escrow and released only when you verify job completion.",
            icon = Icons.Default.Security,
            colorAccent = EscrowGreen
        ),
        OnboardingSlide(
            title = "Fast Verified local Professionals",
            description = "Need Plumbers, Electricians, Carpenters or Masons urgently? We match you with vetted nearest workers within minutes.",
            icon = Icons.Default.FlashOn,
            colorAccent = TrustBlue
        ),
        OnboardingSlide(
            title = "Safe Transacting via Nepal's Top Wallets",
            description = "Instantly pay and secure escrows with seamless simulated eSewa, Khalti, and Fonepay gateway triggers with zero-percent fraud.",
            icon = Icons.Default.AccountBalanceWallet,
            colorAccent = ESewaGreen
        )
    )

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(MidnightDark)
            .windowInsetsPadding(WindowInsets.systemBars)
    ) {
        Column(modifier = Modifier.fillMaxSize()) {
            // Horizontal slide pager
            HorizontalPager(
                state = pagerState,
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth()
            ) { page ->
                val slide = slides[page]
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(32.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center
                ) {
                    Box(
                        modifier = Modifier
                            .size(160.dp)
                            .clip(CircleShape)
                            .background(slide.colorAccent.copy(alpha = 0.15f)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = slide.icon,
                            contentDescription = null,
                            tint = slide.colorAccent,
                            modifier = Modifier.size(80.dp)
                        )
                    }
                    Spacer(modifier = Modifier.height(40.dp))
                    Text(
                        text = slide.title,
                        fontSize = 24.sp,
                        fontWeight = FontWeight.Bold,
                        color = TextLight,
                        textAlign = TextAlign.Center,
                        lineHeight = 32.sp
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                    Text(
                        text = slide.description,
                        fontSize = 15.sp,
                        color = TextMuted,
                        textAlign = TextAlign.Center,
                        lineHeight = 24.sp
                    )
                }
            }

            // Bottom indicator controls
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(32.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Page indicator dots
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    repeat(3) { index ->
                        Box(
                            modifier = Modifier
                                .size(if (pagerState.currentPage == index) 16.dp else 8.dp, 8.dp)
                                .clip(CircleShape)
                                .background(if (pagerState.currentPage == index) TrustBlue else TextMuted.copy(alpha = 0.5f))
                        )
                    }
                }

                // Next/Get Started button
                Button(
                    onClick = {
                        if (pagerState.currentPage < 2) {
                            scope.launch { pagerState.animateScrollToPage(pagerState.currentPage + 1) }
                        } else {
                            onFinished()
                        }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = TrustBlue),
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier.testTag("onboarding_next_button")
                ) {
                    Text(
                        text = if (pagerState.currentPage == 2) "Get Started" else "Next",
                        color = TextLight,
                        fontWeight = FontWeight.SemiBold
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.ArrowForward,
                        contentDescription = "Next Icon",
                        modifier = Modifier.size(16.dp)
                    )
                }
            }
        }
    }
}

data class OnboardingSlide(
    val title: String,
    val description: String,
    val icon: ImageVector,
    val colorAccent: Color
)

@OptIn(ExperimentalAnimationApi::class)
@Composable
fun LoginOtpScreen(onLoginSuccess: () -> Unit) {
    var phoneNumber by remember { mutableStateOf("") }
    var otpSubmitted by remember { mutableStateOf(false) }
    var otpCode by remember { mutableStateOf("") }
    var deviceBindingSecure by remember { mutableStateOf(true) }
    var verificationStep by remember { mutableStateOf(1) } // 1: Enter phone, 2: OTP, 3: Verifying Security
    
    val scope = rememberCoroutineScope()
    val snackbarHostState = remember { SnackbarHostState() }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(MidnightDark)
            .windowInsetsPadding(WindowInsets.systemBars)
            .padding(24.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(top = 40.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = "SECURE REGISTER PANEL",
                fontSize = 12.sp,
                fontWeight = FontWeight.Bold,
                color = TrustBlueLight,
                letterSpacing = 1.5.sp
            )
            Spacer(modifier = Modifier.height(12.dp))
            Text(
                text = "OTP Device Verification",
                fontSize = 24.sp,
                fontWeight = FontWeight.Bold,
                color = TextLight
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = "Binds your phone number directly to this secure hardware device to prevent fraud.",
                fontSize = 14.sp,
                color = TextMuted,
                textAlign = TextAlign.Center,
                modifier = Modifier.padding(horizontal = 16.dp)
            )

            Spacer(modifier = Modifier.height(48.dp))

            when (verificationStep) {
                1 -> {
                    // Step 1: Input Phone
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(containerColor = CardDark),
                        shape = RoundedCornerShape(16.dp)
                    ) {
                        Column(modifier = Modifier.padding(20.dp)) {
                            Text(
                                text = "Enter Mobile Number",
                                fontWeight = FontWeight.SemiBold,
                                color = TextLight,
                                fontSize = 16.sp
                            )
                            Spacer(modifier = Modifier.height(12.dp))
                            OutlinedTextField(
                                value = phoneNumber,
                                onValueChange = { if (it.length <= 10) phoneNumber = it },
                                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone),
                                prefix = { Text("+977 ") },
                                placeholder = { Text("98xxxxxxxx") },
                                singleLine = true,
                                shape = RoundedCornerShape(12.dp),
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .testTag("phone_input"),
                                colors = OutlinedTextFieldDefaults.colors(
                                    focusedBorderColor = TrustBlue,
                                    unfocusedBorderColor = TextMuted.copy(alpha = 0.5f),
                                    focusedLabelColor = TrustBlue,
                                    focusedTextColor = TextLight,
                                    unfocusedTextColor = TextLight
                                )
                            )
                            Spacer(modifier = Modifier.height(20.dp))
                            Button(
                                onClick = {
                                    if (phoneNumber.length == 10) {
                                        verificationStep = 2
                                    } else {
                                        scope.launch {
                                            snackbarHostState.showSnackbar("Please enter a valid 10-digit Nepali number.")
                                        }
                                    }
                                },
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(50.dp)
                                    .testTag("request_otp_button"),
                                colors = ButtonDefaults.buttonColors(containerColor = TrustBlue),
                                shape = RoundedCornerShape(12.dp)
                            ) {
                                Text("Generate Secure SMS OTP", fontWeight = FontWeight.SemiBold, color = TextLight)
                            }
                        }
                    }
                }
                2 -> {
                    // Step 2: Input OTP
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(containerColor = CardDark),
                        shape = RoundedCornerShape(16.dp)
                    ) {
                        Column(modifier = Modifier.padding(20.dp)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = "Enter 4-Digit OTP",
                                    fontWeight = FontWeight.SemiBold,
                                    color = TextLight,
                                    fontSize = 16.sp
                                )
                                Text(
                                    text = "Resend SMS in 59s",
                                    fontSize = 12.sp,
                                    color = WarningAmber
                                )
                            }
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = "OTP sent to +977 $phoneNumber. Insert '1234' for sandbox verification.",
                                fontSize = 12.sp,
                                color = TextMuted
                            )
                            Spacer(modifier = Modifier.height(16.dp))
                            OutlinedTextField(
                                value = otpCode,
                                onValueChange = { if (it.length <= 4) otpCode = it },
                                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.NumberPassword),
                                textStyle = LocalTextStyle.current.copy(
                                    textAlign = TextAlign.Center,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 20.sp,
                                    letterSpacing = 8.sp
                                ),
                                placeholder = { Text("xxxx", modifier = Modifier.fillMaxWidth(), textAlign = TextAlign.Center) },
                                singleLine = true,
                                shape = RoundedCornerShape(12.dp),
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .testTag("otp_input"),
                                colors = OutlinedTextFieldDefaults.colors(
                                    focusedBorderColor = TrustBlue,
                                    unfocusedBorderColor = TextMuted.copy(alpha = 0.5f),
                                    focusedTextColor = TextLight,
                                    unfocusedTextColor = TextLight
                                )
                            )
                            Spacer(modifier = Modifier.height(20.dp))
                            Button(
                                onClick = {
                                    if (otpCode == "1234") {
                                        verificationStep = 3
                                    } else {
                                        scope.launch {
                                            snackbarHostState.showSnackbar("Incorrect OTP. Enter '1234' for simulator.")
                                        }
                                    }
                                },
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(50.dp)
                                    .testTag("verify_otp_button"),
                                colors = ButtonDefaults.buttonColors(containerColor = EscrowGreen),
                                shape = RoundedCornerShape(12.dp)
                            ) {
                                Text("Bind Device & Authenticate", fontWeight = FontWeight.SemiBold, color = TextLight)
                            }
                            Spacer(modifier = Modifier.height(12.dp))
                            TextButton(
                                onClick = { verificationStep = 1 },
                                modifier = Modifier.align(Alignment.CenterHorizontally)
                            ) {
                                Text("Change Number", color = TrustBlueLight)
                            }
                        }
                    }
                }
                3 -> {
                    // Step 3: Secure Check animation
                    LaunchedEffect(Unit) {
                        delay(2500)
                        onLoginSuccess()
                    }

                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(containerColor = CardDark),
                        shape = RoundedCornerShape(16.dp)
                    ) {
                        Column(
                            modifier = Modifier.padding(32.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            CircularProgressIndicator(color = EscrowGreen, strokeWidth = 3.dp)
                            Spacer(modifier = Modifier.height(24.dp))
                            Text(
                                text = "Banding Hardware Key...",
                                fontWeight = FontWeight.SemiBold,
                                fontSize = 16.sp,
                                color = TextLight
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                            Row(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(8.dp))
                                    .background(EscrowGreen.copy(alpha = 0.1f))
                                    .padding(horizontal = 12.dp, vertical = 6.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(6.dp)
                            ) {
                                Icon(Icons.Default.Lock, contentDescription = null, tint = EscrowGreen, modifier = Modifier.size(12.dp))
                                Text("Banned Secure AES-256 ID SUCCESS", fontSize = 11.sp, color = EscrowGreen, fontWeight = FontWeight.Bold)
                            }
                        }
                    }
                }
            }
        }
        
        SnackbarHost(
            hostState = snackbarHostState,
            modifier = Modifier.align(Alignment.BottomCenter)
        )
    }
}

@Composable
fun LocationPermissionScreen(onPermissionGranted: () -> Unit) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(MidnightDark)
            .windowInsetsPadding(WindowInsets.systemBars)
            .padding(32.dp),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Box(
                modifier = Modifier
                    .size(120.dp)
                    .clip(CircleShape)
                    .background(TrustBlue.copy(alpha = 0.12f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.LocationOn,
                    contentDescription = "Location Access Needed",
                    tint = TrustBlue,
                    modifier = Modifier.size(56.dp)
                )
            }
            Spacer(modifier = Modifier.height(32.dp))
            Text(
                text = "Enable Location Services",
                fontSize = 22.sp,
                fontWeight = FontWeight.Bold,
                color = TextLight,
                textAlign = TextAlign.Center
            )
            Spacer(modifier = Modifier.height(12.dp))
            Text(
                text = "KaamChha requires GPS coordinates to calculate routing matrix distances and find nearby trust workers instantly.",
                fontSize = 14.sp,
                color = TextMuted,
                textAlign = TextAlign.Center,
                lineHeight = 22.sp
            )
            Spacer(modifier = Modifier.height(40.dp))
            
            Button(
                onClick = onPermissionGranted,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(50.dp)
                    .testTag("grant_location_button"),
                colors = ButtonDefaults.buttonColors(containerColor = TrustBlue),
                shape = RoundedCornerShape(12.dp)
            ) {
                Icon(Icons.Default.MyLocation, contentDescription = null, tint = TextLight, modifier = Modifier.size(20.dp))
                Spacer(modifier = Modifier.width(8.dp))
                Text("Scan Distance Coordinates", fontWeight = FontWeight.Bold, color = TextLight)
            }
            
            Spacer(modifier = Modifier.height(16.dp))
            
            TextButton(onClick = onPermissionGranted) {
                Text("Select Location Manually Instead", color = TextMuted)
            }
        }
    }
}
