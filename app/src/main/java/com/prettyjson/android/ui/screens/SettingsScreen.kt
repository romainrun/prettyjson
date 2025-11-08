package com.prettyjson.android.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import kotlinx.coroutines.launch
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import org.koin.androidx.compose.koinViewModel
import com.prettyjson.android.ui.components.RewardedAdHelper
import com.prettyjson.android.ui.viewmodel.SettingsViewModel
import com.prettyjson.android.ui.viewmodel.PremiumViewModel

/**
 * Settings screen
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(
    onNavigateBack: () -> Unit,
    onSupportUs: () -> Unit,
    onNavigateToUpgrade: () -> Unit = {},
    viewModel: SettingsViewModel = koinViewModel(),
    premiumViewModel: PremiumViewModel = koinViewModel()
) {
    val theme by viewModel.theme.collectAsState()
    val themeStyle by viewModel.themeStyle.collectAsState()
    val fontFamily by viewModel.fontFamily.collectAsState()
    val textSize by viewModel.textSize.collectAsState()
    val lineWrapping by viewModel.lineWrapping.collectAsState()
    val formatOnPaste by viewModel.formatOnPaste.collectAsState()
    val isPremium by premiumViewModel.isPremium.collectAsState()
    val premiumType by premiumViewModel.premiumType.collectAsState()
    val devModePremium by premiumViewModel.devModePremium.collectAsState()
    
    val scope = rememberCoroutineScope()
    val snackbarHostState = remember { SnackbarHostState() }
    
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
                title = { Text("Settings") },
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
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Theme Section
            Card {
                Column(
                    modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Text("Theme", style = MaterialTheme.typography.titleMedium)
                    
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        FilterChip(
                            selected = theme == "light",
                            onClick = { viewModel.setTheme("light") },
                            label = { Text("Light") },
                            modifier = Modifier.weight(1f)
                        )
                        FilterChip(
                            selected = theme == "dark",
                            onClick = { viewModel.setTheme("dark") },
                            label = { Text("Dark") },
                            modifier = Modifier.weight(1f)
                        )
                        FilterChip(
                            selected = theme == "system",
                            onClick = { viewModel.setTheme("system") },
                            label = { Text("System") },
                            modifier = Modifier.weight(1f)
                        )
                    }
                    
                    Spacer(modifier = Modifier.height(16.dp))
                    
                    Text("Theme Style", style = MaterialTheme.typography.titleSmall)
                    
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        FilterChip(
                            selected = themeStyle == "default",
                            onClick = { viewModel.setThemeStyle("default") },
                            label = { Text("Default") },
                            modifier = Modifier.weight(1f)
                        )
                        FilterChip(
                            selected = themeStyle == "dracula",
                            onClick = { viewModel.setThemeStyle("dracula") },
                            label = { Text("Dracula") },
                            modifier = Modifier.weight(1f)
                        )
                        FilterChip(
                            selected = themeStyle == "solarized",
                            onClick = { viewModel.setThemeStyle("solarized") },
                            label = { Text("Solarized") },
                            modifier = Modifier.weight(1f)
                        )
                        FilterChip(
                            selected = themeStyle == "onedark",
                            onClick = { viewModel.setThemeStyle("onedark") },
                            label = { Text("OneDark") },
                            modifier = Modifier.weight(1f)
                        )
                    }
                }
            }
            
            // Font Family Section
            Card {
                Column(
                    modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Text("Font Family", style = MaterialTheme.typography.titleMedium)
                    
                    // Grid layout for 4 fonts (2 rows x 2 columns)
                    Column(
                        modifier = Modifier.fillMaxWidth(),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            FilterChip(
                                selected = fontFamily == "jetbrains",
                                onClick = { viewModel.setFontFamily("jetbrains") },
                                label = { Text("JetBrains Mono") },
                                modifier = Modifier.weight(1f)
                            )
                            FilterChip(
                                selected = fontFamily == "fira",
                                onClick = { viewModel.setFontFamily("fira") },
                                label = { Text("Fira Code") },
                                modifier = Modifier.weight(1f)
                            )
                        }
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            FilterChip(
                                selected = fontFamily == "monospace",
                                onClick = { viewModel.setFontFamily("monospace") },
                                label = { Text("Monospace") },
                                modifier = Modifier.weight(1f)
                            )
                            FilterChip(
                                selected = fontFamily == "default",
                                onClick = { viewModel.setFontFamily("default") },
                                label = { Text("System") },
                                modifier = Modifier.weight(1f)
                            )
                        }
                    }
                }
            }
            
            // Text Size Section
            Card {
                Column(
                    modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Text("Text Size", style = MaterialTheme.typography.titleMedium)
                    
                    // Dropdown with preset sizes + custom input
                    var showCustomSize by remember { mutableStateOf(false) }
                    var customSizeText by remember { mutableStateOf(textSize.toString()) }
                    
                    // Preset sizes dropdown
                    var showDropdown by remember { mutableStateOf(false) }
                    val presetSizes = listOf(10, 12, 14, 16, 18, 20, 22, 24)
                    
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        // Dropdown button for preset sizes
                        Box(modifier = Modifier.weight(1f)) {
                            OutlinedButton(
                                onClick = { showDropdown = true },
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Text("${textSize}sp")
                                Icon(
                                    Icons.Default.ArrowDropDown,
                                    contentDescription = null,
                                    modifier = Modifier.size(16.dp)
                                )
                            }
                            DropdownMenu(
                                expanded = showDropdown,
                                onDismissRequest = { showDropdown = false }
                            ) {
                                presetSizes.forEach { size ->
                                    DropdownMenuItem(
                                        text = { Text("${size}sp") },
                                        onClick = {
                                            viewModel.setTextSize(size)
                                            showDropdown = false
                                        }
                                    )
                                }
                            }
                        }
                        
                        // Custom size button/field
                        if (showCustomSize) {
                            Row(
                                modifier = Modifier.width(120.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                OutlinedTextField(
                                    value = customSizeText,
                                    onValueChange = {
                                        customSizeText = it.filter { char -> char.isDigit() }
                                        if (customSizeText.isNotEmpty()) {
                                            val size = customSizeText.toIntOrNull()
                                            if (size != null && size in 8..32) {
                                                viewModel.setTextSize(size)
                                            }
                                        }
                                    },
                                    label = { Text("Custom") },
                                    modifier = Modifier.weight(1f),
                                    singleLine = true
                                )
                                Text(
                                    "sp",
                                    style = MaterialTheme.typography.bodySmall,
                                    modifier = Modifier.padding(start = 4.dp)
                                )
                            }
                            IconButton(
                                onClick = { 
                                    showCustomSize = false
                                    customSizeText = textSize.toString()
                                }
                            ) {
                                Icon(Icons.Default.Close, contentDescription = "Close")
                            }
                        } else {
                            IconButton(
                                onClick = {
                                    showCustomSize = true
                                    customSizeText = textSize.toString()
                                }
                            ) {
                                Icon(Icons.Default.Edit, contentDescription = "Custom Size")
                            }
                        }
                    }
                }
            }
            
            // Editor Options Section
            Card {
                Column(
                    modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Text("Editor Options", style = MaterialTheme.typography.titleMedium)
                    
                    // Line Wrapping Toggle
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                "Line Wrapping",
                                style = MaterialTheme.typography.bodyLarge
                            )
                            Text(
                                "Wrap long lines for better readability",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f)
                            )
                        }
                        Switch(
                            checked = lineWrapping,
                            onCheckedChange = { viewModel.setLineWrapping(it) }
                        )
                    }
                    
                    Divider()
                    
                    // Format on Paste Toggle
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                "Format on Paste",
                                style = MaterialTheme.typography.bodyLarge
                            )
                            Text(
                                "Automatically format JSON when pasting",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f)
                            )
                        }
                        Switch(
                            checked = formatOnPaste,
                            onCheckedChange = { viewModel.setFormatOnPaste(it) }
                        )
                    }
                }
            }
            
            // Development Mode Section (for testing)
            Card(
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
                )
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                "Development Mode",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold
                            )
                            Text(
                                "Enable Pro plan for testing (development only)",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f)
                            )
                        }
                        Switch(
                            checked = devModePremium,
                            onCheckedChange = { premiumViewModel.setDevModePremium(it) }
                        )
                    }
                    if (devModePremium) {
                        Text(
                            "⚠️ Development mode active - All Pro features are enabled for testing",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.primary,
                            fontWeight = FontWeight.Medium
                        )
                    }
                }
            }
            
            // Premium Section
            Card(
                colors = CardDefaults.cardColors(
                    containerColor = if (isPremium) 
                        MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.3f) 
                    else 
                        MaterialTheme.colorScheme.surface
                )
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Icon(
                        Icons.Default.Star,
                        contentDescription = null,
                        modifier = Modifier.size(48.dp),
                        tint = if (isPremium) 
                            MaterialTheme.colorScheme.primary 
                        else 
                            MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                    )
                    Text(
                        if (isPremium) "Pro User" else "Upgrade to Pro",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold,
                        color = if (isPremium) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface
                    )
                    
                    if (isPremium) {
                        Text(
                            "Thank you for your support!",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
                        )
                        Text(
                            "You have unlimited access to all Pro features.",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                        )
                    } else {
                        Column(
                            verticalArrangement = Arrangement.spacedBy(8.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text(
                                "Unlock Pro features:",
                                style = MaterialTheme.typography.titleSmall,
                                fontWeight = FontWeight.Bold
                            )
                            Text("✨ Data Buckets - Create reusable JSON snippets")
                            Text("🚫 Ad-free experience (remove all ads)")
                            Text("📱 Early access to new features")
                            
                            Spacer(modifier = Modifier.height(8.dp))
                            
                            Card(
                                colors = CardDefaults.cardColors(
                                    containerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.5f)
                                )
                            ) {
                                Column(
                                    modifier = Modifier.padding(12.dp),
                                    verticalArrangement = Arrangement.spacedBy(4.dp)
                                ) {
                                    Text(
                                        "One-time purchase: €1.50",
                                        style = MaterialTheme.typography.titleMedium,
                                        fontWeight = FontWeight.Bold,
                                        color = MaterialTheme.colorScheme.primary
                                    )
                                    Text(
                                        "Best value - No recurring payments",
                                        style = MaterialTheme.typography.bodySmall
                                    )
                                }
                            }
                            
                            Button(
                                onClick = {
                                    onNavigateToUpgrade()
                                },
                                modifier = Modifier.fillMaxWidth(),
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = MaterialTheme.colorScheme.primary
                                )
                            ) {
                                Icon(Icons.Default.Star, contentDescription = null)
                                Spacer(Modifier.width(8.dp))
                                Text("Purchase Pro", fontWeight = FontWeight.Bold)
                            }
                        }
                    }
                }
            }
            
            // Support Us Section (only show for free users)
            if (!isPremium) {
                Card {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Icon(
                            Icons.Default.Favorite,
                            contentDescription = null,
                            modifier = Modifier.size(48.dp),
                            tint = MaterialTheme.colorScheme.primary
                        )
                        Text("Support Us", style = MaterialTheme.typography.titleMedium)
                        Text(
                            "Watch a rewarded ad to support development",
                            style = MaterialTheme.typography.bodyMedium
                        )
                        Button(
                            onClick = onSupportUs,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Icon(Icons.Default.PlayArrow, contentDescription = null)
                            Spacer(Modifier.width(8.dp))
                            Text("Watch Ad")
                        }
                    }
                }
            }
            
            // About Section
            Card {
                Column(
                    modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Text("About PrettyJSON", style = MaterialTheme.typography.titleMedium)
                    Text("Version 1.0", style = MaterialTheme.typography.bodyMedium)
                    Text(
                        "PrettyJSON helps you format, validate, and manage JSON data efficiently.",
                        style = MaterialTheme.typography.bodySmall
                    )
                    Spacer(Modifier.height(8.dp))
                    Text(
                        "Privacy Policy: This app uses Google AdMob for advertising. See Google's privacy policy for details.",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                    )
                }
            }
        }
    }
}

