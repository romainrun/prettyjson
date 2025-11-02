package re.weare.app.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import org.koin.androidx.compose.koinViewModel
import re.weare.app.ui.components.RewardedAdHelper
import re.weare.app.ui.viewmodel.SettingsViewModel

/**
 * Settings screen
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(
    onNavigateBack: () -> Unit,
    onSupportUs: () -> Unit,
    viewModel: SettingsViewModel = koinViewModel()
) {
    val theme by viewModel.theme.collectAsState()
    val themeStyle by viewModel.themeStyle.collectAsState()
    val fontFamily by viewModel.fontFamily.collectAsState()
    val textSize by viewModel.textSize.collectAsState()
    val keyCaseStyle by viewModel.keyCaseStyle.collectAsState()
    
    Scaffold(
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
                        FilterChip(
                            selected = fontFamily == "default",
                            onClick = { viewModel.setFontFamily("default") },
                            label = { Text("Default") },
                            modifier = Modifier.weight(1f)
                        )
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
                    
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Slider(
                            value = textSize.toFloat(),
                            onValueChange = { viewModel.setTextSize(it.toInt()) },
                            valueRange = 10f..24f,
                            steps = 13,
                            modifier = Modifier.weight(1f)
                        )
                        Text(
                            "${textSize}sp",
                            style = MaterialTheme.typography.bodyMedium,
                            modifier = Modifier.width(48.dp)
                        )
                    }
                }
            }
            
            // Key Case Style Section
            Card {
                Column(
                    modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Text("Key Naming Style", style = MaterialTheme.typography.titleMedium)
                    
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        FilterChip(
                            selected = keyCaseStyle == "camelCase",
                            onClick = { viewModel.setKeyCaseStyle("camelCase") },
                            label = { Text("camelCase") },
                            modifier = Modifier.weight(1f)
                        )
                        FilterChip(
                            selected = keyCaseStyle == "snake_case",
                            onClick = { viewModel.setKeyCaseStyle("snake_case") },
                            label = { Text("snake_case") },
                            modifier = Modifier.weight(1f)
                        )
                        FilterChip(
                            selected = keyCaseStyle == "PascalCase",
                            onClick = { viewModel.setKeyCaseStyle("PascalCase") },
                            label = { Text("PascalCase") },
                            modifier = Modifier.weight(1f)
                        )
                    }
                }
            }
            
            // Support Us Section
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

