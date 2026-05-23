package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.animation.*
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.data.MarketplaceViewModel
import com.example.ui.screens.*
import com.example.ui.theme.*

class MainActivity : ComponentActivity() {

    private val viewModel: MarketplaceViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        setContent {
            MyApplicationTheme(darkTheme = true) {
                MainRootNavigationGraph(viewModel)
            }
        }
    }
}

@Composable
fun MainRootNavigationGraph(viewModel: MarketplaceViewModel) {
    // Top-most state controller representing entry flows
    var currentScreenState by remember { mutableStateOf("SPLASH") } // SPLASH, ONBOARDING, LOGIN, PERMISSION, MAIN

    val currentRole by viewModel.currentRole.collectAsStateWithLifecycle()
    val selectedBooking by viewModel.selectedBooking.collectAsStateWithLifecycle()

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(MidnightDark)
    ) {
        when (currentScreenState) {
            "SPLASH" -> {
                SplashScreen(onTimeout = { currentScreenState = "ONBOARDING" })
            }
            "ONBOARDING" -> {
                OnboardingScreen(onFinished = { currentScreenState = "LOGIN" })
            }
            "LOGIN" -> {
                LoginOtpScreen(onLoginSuccess = { currentScreenState = "PERMISSION" })
            }
            "PERMISSION" -> {
                LocationPermissionScreen(onPermissionGranted = { currentScreenState = "MAIN" })
            }
            "MAIN" -> {
                Column(modifier = Modifier.fillMaxSize()) {
                    // Universal Multi-Role Selector Platform Bar
                    RoleBarSelector(currentRole = currentRole, onRoleSwapped = { newRole ->
                        viewModel.switchRole(newRole)
                    })

                    Box(modifier = Modifier.weight(1f).fillMaxWidth()) {
                        // Check if a specific active invoice booking details sheet is requested
                        if (selectedBooking != null) {
                            JobDetailScreen(
                                viewModel = viewModel,
                                onBack = { viewModel.selectBooking(null) }
                            )
                        } else {
                            // Render target app role
                            when (currentRole) {
                                "CUSTOMER" -> {
                                    CustomerDashboardScreen(
                                        viewModel = viewModel,
                                        onBookingSelected = { viewModel.selectBooking(it) }
                                    )
                                }
                                "WORKER" -> {
                                    WorkerDashboardScreen(
                                        viewModel = viewModel,
                                        onBookingSelected = { viewModel.selectBooking(it) }
                                    )
                                }
                                "ADMIN" -> {
                                    AdminDashboardScreen(
                                        viewModel = viewModel,
                                        onBookingSelected = { viewModel.selectBooking(it) }
                                    )
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
fun RoleBarSelector(
    currentRole: String,
    onRoleSwapped: (String) -> Unit
) {
    Surface(
        color = Slate950,
        tonalElevation = 6.dp,
        modifier = Modifier.windowInsetsPadding(WindowInsets.statusBars)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 12.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "SAJHA TRUST",
                fontSize = 11.sp,
                fontWeight = FontWeight.Bold,
                color = TrustBlueLight,
                letterSpacing = 1.sp
            )

            Row(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                RoleCapsuleItem(
                    label = "Client",
                    active = currentRole == "CUSTOMER",
                    onClick = { onRoleSwapped("CUSTOMER") },
                    testTag = "role_customer_capsule"
                )
                RoleCapsuleItem(
                    label = "Worker",
                    active = currentRole == "WORKER",
                    onClick = { onRoleSwapped("WORKER") },
                    testTag = "role_worker_capsule"
                )
                RoleCapsuleItem(
                    label = "Admin",
                    active = currentRole == "ADMIN",
                    onClick = { onRoleSwapped("ADMIN") },
                    testTag = "role_admin_capsule"
                )
            }
        }
    }
}

@Composable
fun RoleCapsuleItem(
    label: String,
    active: Boolean,
    onClick: () -> Unit,
    testTag: String
) {
    Box(
        modifier = Modifier
            .clip(RoundedCornerShape(20.dp))
            .background(if (active) TrustBlue else Slate800)
            .clickable(onClick = onClick)
            .padding(horizontal = 10.dp, vertical = 5.dp)
            .testTag(testTag),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = label,
            fontSize = 11.sp,
            fontWeight = FontWeight.Bold,
            color = if (active) TextLight else TextMuted
        )
    }
}
