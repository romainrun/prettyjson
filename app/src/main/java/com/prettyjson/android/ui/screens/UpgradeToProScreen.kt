package com.prettyjson.android.ui.screens

import android.app.Activity
import androidx.compose.animation.core.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.android.billingclient.api.BillingClient
import com.android.billingclient.api.BillingResult
import com.android.billingclient.api.ProductDetails
import com.prettyjson.android.data.billing.ProManager
import com.prettyjson.android.ui.viewmodel.ProViewModel
import kotlinx.coroutines.launch
import org.koin.androidx.compose.get
import org.koin.androidx.compose.koinViewModel

/**
 * Upgrade to Pro screen with Material 3 design
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun UpgradeToProScreen(
    onNavigateBack: () -> Unit,
    proManager: ProManager = org.koin.androidx.compose.get(),
    proViewModel: ProViewModel = koinViewModel()
) {
    val context = LocalContext.current
    val activity = context as? Activity
    val scope = rememberCoroutineScope()
    val snackbarHostState = remember { SnackbarHostState() }
    
    val isProUser by proViewModel.isProUser.collectAsState()
    
    var productDetails by remember { mutableStateOf<ProductDetails?>(null) }
    var isLoading by remember { mutableStateOf(false) }
    var isRestoring by remember { mutableStateOf(false) }
    
    // Animated checkmark scale
    val checkmarkScale by animateFloatAsState(
        targetValue = if (isProUser) 1f else 0.8f,
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioMediumBouncy,
            stiffness = Spring.StiffnessLow
        ),
        label = "checkmark_scale"
    )
    
    // Query product details on launch
    LaunchedEffect(Unit) {
        proManager.queryProductDetails { details ->
            productDetails = details
        }
    }
    
    Scaffold(
        modifier = Modifier
            .fillMaxSize()
            .statusBarsPadding()
            .navigationBarsPadding(),
        snackbarHost = {
            SnackbarHost(hostState = snackbarHostState)
        },
        topBar = {
            TopAppBar(
                title = { Text("Upgrade to Pro") },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back")
                    }
                }
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .verticalScroll(rememberScrollState())
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(24.dp)
        ) {
            // App Icon/Logo
            Icon(
                Icons.Default.Diamond,
                contentDescription = null,
                modifier = Modifier
                    .size(80.dp)
                    .scale(checkmarkScale),
                tint = MaterialTheme.colorScheme.primary
            )
            
            // Title
            Text(
                "Upgrade to PrettyJSON Pro",
                style = MaterialTheme.typography.headlineMedium,
                fontWeight = FontWeight.Bold,
                textAlign = TextAlign.Center
            )
            
            // Subtitle
            Text(
                "One-time purchase — unlock all premium features",
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = TextAlign.Center
            )
            
            // Features Card
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
                )
            ) {
                Column(
                    modifier = Modifier.padding(20.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    Text(
                        "Pro Features:",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                    
                    // Feature list with animated checkmarks
                    ProFeatureItem(
                        icon = Icons.Default.Block,
                        title = "Ad-free experience",
                        description = "Remove all banner and video ads"
                    )
                    
                    ProFeatureItem(
                        icon = Icons.Default.Edit,
                        title = "Full Form Editor access",
                        description = "Add, edit, and delete JSON fields"
                    )
                    
                    ProFeatureItem(
                        icon = Icons.Default.Storage,
                        title = "Data Buckets",
                        description = "Create reusable JSON snippets"
                    )
                    
                    ProFeatureItem(
                        icon = Icons.Default.FileDownload,
                        title = "Unlimited exports",
                        description = "Export JSON, TXT, PDF, HTML without ads"
                    )
                    
                    ProFeatureItem(
                        icon = Icons.Default.Palette,
                        title = "Custom themes",
                        description = "Additional Material3 color palettes"
                    )
                    
                    ProFeatureItem(
                        icon = Icons.Default.Star,
                        title = "Early access",
                        description = "Get new features first"
                    )
                }
            }
            
            // Price Card
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.5f)
                )
            ) {
                Column(
                    modifier = Modifier.padding(20.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Text(
                        productDetails?.oneTimePurchaseOfferDetails?.formattedPrice ?: "€1.50",
                        style = MaterialTheme.typography.headlineMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary
                    )
                    Text(
                        "One-time purchase — No recurring payments",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
            
            // Purchase Button
            if (isProUser) {
                // Already Pro
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.primaryContainer
                    )
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(20.dp),
                        horizontalArrangement = Arrangement.Center,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            Icons.Default.CheckCircle,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(24.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            "You're already a Pro user!",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.primary
                        )
                    }
                }
            } else {
                // Buy Pro Button
                Button(
                    onClick = {
                        if (activity != null && productDetails != null) {
                            isLoading = true
                            proManager.launchPurchaseFlow(activity, productDetails!!) { result ->
                                isLoading = false
                                when (result.responseCode) {
                                    BillingClient.BillingResponseCode.OK -> {
                                        scope.launch {
                                            snackbarHostState.showSnackbar(
                                                message = "Purchase successful! Welcome to Pro!",
                                                duration = SnackbarDuration.Long
                                            )
                                        }
                                    }
                                    BillingClient.BillingResponseCode.USER_CANCELED -> {
                                        scope.launch {
                                            snackbarHostState.showSnackbar(
                                                message = "Purchase cancelled",
                                                duration = SnackbarDuration.Short
                                            )
                                        }
                                    }
                                    else -> {
                                        scope.launch {
                                            snackbarHostState.showSnackbar(
                                                message = "Purchase failed: ${result.debugMessage}",
                                                duration = SnackbarDuration.Long
                                            )
                                        }
                                    }
                                }
                            }
                        } else {
                            scope.launch {
                                snackbarHostState.showSnackbar(
                                    message = "Product details not available. Please try again.",
                                    duration = SnackbarDuration.Long
                                )
                            }
                        }
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(56.dp),
                    enabled = !isLoading && productDetails != null,
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.primary
                    ),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    if (isLoading) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(24.dp),
                            color = MaterialTheme.colorScheme.onPrimary
                        )
                    } else {
                        Icon(Icons.Default.Diamond, contentDescription = null)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            "Buy Pro for ${productDetails?.oneTimePurchaseOfferDetails?.formattedPrice ?: "€1.50"}",
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
                
                // Restore Purchase Button
                TextButton(
                    onClick = {
                        isRestoring = true
                        proManager.restorePurchase { restored ->
                            isRestoring = false
                            scope.launch {
                                if (restored) {
                                    snackbarHostState.showSnackbar(
                                        message = "Purchase restored! Welcome back!",
                                        duration = SnackbarDuration.Long
                                    )
                                } else {
                                    snackbarHostState.showSnackbar(
                                        message = "No purchase found to restore",
                                        duration = SnackbarDuration.Short
                                    )
                                }
                            }
                        }
                    },
                    modifier = Modifier.fillMaxWidth(),
                    enabled = !isRestoring
                ) {
                    if (isRestoring) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(20.dp)
                        )
                    } else {
                        Icon(Icons.Default.Restore, contentDescription = null)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Restore Purchase")
                    }
                }
            }
        }
    }
}

@Composable
private fun ProFeatureItem(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    title: String,
    description: String
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(12.dp),
        verticalAlignment = Alignment.Top
    ) {
        Icon(
            icon,
            contentDescription = null,
            modifier = Modifier.size(24.dp),
            tint = MaterialTheme.colorScheme.primary
        )
        Column(modifier = Modifier.weight(1f)) {
            Text(
                title,
                style = MaterialTheme.typography.bodyLarge,
                fontWeight = FontWeight.Medium
            )
            Text(
                description,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
        Icon(
            Icons.Default.CheckCircle,
            contentDescription = null,
            modifier = Modifier.size(20.dp),
            tint = MaterialTheme.colorScheme.primary
        )
    }
}

