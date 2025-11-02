package re.weare.app.ui.screens

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.content.Intent
import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.material3.SnackbarResult
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.foundation.layout.Box
import org.koin.androidx.compose.koinViewModel
import re.weare.app.ui.components.BannerAdView
import re.weare.app.ui.components.JsonTreeView
import re.weare.app.ui.components.LineNumberTextField
import re.weare.app.ui.components.ExampleJson
import re.weare.app.ui.components.AnimatedValidationIcon
import re.weare.app.ui.components.ShimmerPlaceholder
import re.weare.app.ui.components.DropdownMenuButton
import re.weare.app.ui.components.DropdownMenuItem as CustomDropdownMenuItem
import re.weare.app.ui.navigation.NavigationRoutes
import re.weare.app.ui.viewmodel.MainViewModel
import re.weare.app.util.FileManager
import re.weare.app.util.PdfExporter
import kotlinx.coroutines.launch
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.net.URL
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

/**
 * Main screen for JSON formatting and validation
 * Redesigned to match codebeautify.org/jsonviewer style
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainScreen(
    onNavigate: (String) -> Unit,
    viewModel: MainViewModel = koinViewModel(),
    initialJsonContent: String? = null
) {
    // Load initial JSON content if provided
    LaunchedEffect(initialJsonContent) {
        initialJsonContent?.let { content ->
            if (content.isNotEmpty()) {
                viewModel.setJsonInput(content, addToHistory = false)
            }
        }
    }
    val jsonInput by viewModel.jsonInput.collectAsState()
    val jsonOutput by viewModel.jsonOutput.collectAsState()
    val isValid by viewModel.isValid.collectAsState()
    val errorMessage by viewModel.errorMessage.collectAsState()
    val errorLocation by viewModel.errorLocation.collectAsState()
    val tabSpaces by viewModel.tabSpaces.collectAsState()
    val canUndo by viewModel.canUndo.collectAsState()
    val canRedo by viewModel.canRedo.collectAsState()
    
    val settingsViewModel: re.weare.app.ui.viewmodel.SettingsViewModel = org.koin.androidx.compose.koinViewModel()
    val fontFamily by settingsViewModel.fontFamily.collectAsState(initial = "jetbrains")
    val textSize by settingsViewModel.textSize.collectAsState(initial = 14)
    
    val context = LocalContext.current
    val clipboardManager = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
    val scope = rememberCoroutineScope()
    
    var showUrlDialog by remember { mutableStateOf(false) }
    var urlInput by remember { mutableStateOf("") }
    var isLoadingUrl by remember { mutableStateOf(false) }
    var showSaveDialog by remember { mutableStateOf(false) }
    var saveFileName by remember { mutableStateOf("") }
    var viewMode by remember { mutableStateOf("editor") } // "editor" or "tree"
    var showToolbar by remember { mutableStateOf(true) }
    var showFileError by remember { mutableStateOf<String?>(null) }
    var selectedTab by remember { mutableStateOf(0) } // 0 = Input, 1 = Output
    var isFullScreen by remember { mutableStateOf(false) }
    var showSearchReplace by remember { mutableStateOf(false) }
    var searchText by remember { mutableStateOf("") }
    var replaceText by remember { mutableStateOf("") }
    var showClipboardDialog by remember { mutableStateOf(false) }
    var clipboardJsonContent by remember { mutableStateOf<String?>(null) }
    var showDemoJsonButton by remember { mutableStateOf(jsonInput.isEmpty()) }
    var recentFiles by remember { mutableStateOf<List<re.weare.app.data.database.SavedJson>>(emptyList()) }
    var showRecentFilesDialog by remember { mutableStateOf(false) }
    var showRecentHub by remember { mutableStateOf(false) }
    
    // Snackbar for clipboard detection
    val snackbarHostState = remember { SnackbarHostState() }
    
    // Auto-restore last JSON and cursor position on app open
    LaunchedEffect(Unit) {
        if (jsonInput.isEmpty()) {
            scope.launch {
                val lastJson = viewModel.restoreLastJson()
                if (lastJson != null && lastJson.isNotEmpty()) {
                    viewModel.setJsonInput(lastJson, addToHistory = false)
                    // Restore cursor position would require TextFieldValue integration
                }
            }
        }
    }
    
    // Load recent files when app opens and input is empty
    LaunchedEffect(jsonInput.isEmpty()) {
        if (jsonInput.isEmpty()) {
            scope.launch {
                recentFiles = viewModel.getRecentFiles(10)
                if (recentFiles.isNotEmpty()) {
                    showRecentFilesDialog = true
                }
            }
        }
    }
    
    // Check clipboard for JSON on first launch and show snackbar
    LaunchedEffect(Unit) {
        val clip = clipboardManager.primaryClip
        clip?.getItemAt(0)?.text?.toString()?.let { clipboardText ->
            // Simple check if it looks like JSON
            val trimmed = clipboardText.trim()
            if (trimmed.startsWith("{") || trimmed.startsWith("[")) {
                // Try to validate as JSON
                val validation = re.weare.app.util.JsonFormatter.validate(trimmed)
                if (validation.isValid || trimmed.length > 10) { // If valid or looks substantial
                    clipboardJsonContent = trimmed
                    if (jsonInput.isEmpty()) {
                        showClipboardDialog = true
                    }
                }
            }
        }
    }
    
    // File picker launcher
    val filePickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        uri?.let { selectedUri ->
            scope.launch {
                val result = FileManager.readFileContent(context, selectedUri)
                if (result.isSuccess) {
                    result.getOrNull()?.let { content ->
                        viewModel.setJsonInput(content, addToHistory = true)
                    }
                } else {
                    val error = result.exceptionOrNull()
                    showFileError = "Failed to read file: ${error?.message ?: "Unknown error"}"
                }
            }
        }
    }
    
    Scaffold(
        topBar = {
            // Minimal header like the website
            Surface(
                modifier = Modifier.fillMaxWidth(),
                color = MaterialTheme.colorScheme.surface,
                shadowElevation = 2.dp
            ) {
                Column {
                    // Title bar
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp, vertical = 12.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Text(
                                "JSON Viewer",
                                style = MaterialTheme.typography.titleLarge.copy(
                                    fontWeight = FontWeight.Bold
                                )
                            )
                            Text(
                                "•",
                                color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f)
                            )
                            Text(
                                "JSON Formatter & Beautifier",
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                        
                        // Menu button
                        IconButton(onClick = { /* Show menu */ }) {
                            Icon(Icons.Default.MoreVert, contentDescription = "Menu")
                        }
                    }
                    
                    // Compact toolbar with validation status and FAB menu
                    if (showToolbar) {
                        Surface(
                            modifier = Modifier.fillMaxWidth(),
                            color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f)
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(horizontal = 12.dp, vertical = 8.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                // Quick action icons (most common)
                                Row(
                                    horizontalArrangement = Arrangement.spacedBy(4.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    // Undo/Redo
                                    IconButton(
                                        onClick = { viewModel.undo() },
                                        enabled = canUndo,
                                        modifier = Modifier.size(36.dp)
                                    ) {
                                        Icon(
                                            Icons.Default.ArrowBack,
                                            contentDescription = "Undo",
                                            modifier = Modifier.size(18.dp)
                                        )
                                    }
                                    
                                    IconButton(
                                        onClick = { viewModel.redo() },
                                        enabled = canRedo,
                                        modifier = Modifier.size(36.dp)
                                    ) {
                                        Icon(
                                            Icons.Default.ArrowForward,
                                            contentDescription = "Redo",
                                            modifier = Modifier.size(18.dp)
                                        )
                                    }
                                    
                                    // Search
                                    IconButton(
                                        onClick = { showSearchReplace = true },
                                        modifier = Modifier.size(36.dp)
                                    ) {
                                        Icon(
                                            Icons.Default.Search,
                                            contentDescription = "Search",
                                            modifier = Modifier.size(18.dp)
                                        )
                                    }
                                    
                                    // Full Screen toggle (only for output tab with content)
                                    if (jsonOutput.isNotEmpty() && selectedTab == 1) {
                                        IconButton(
                                            onClick = { isFullScreen = !isFullScreen },
                                            modifier = Modifier.size(36.dp)
                                        ) {
                                            Icon(
                                                if (isFullScreen) Icons.Default.Close else Icons.Default.Fullscreen,
                                                contentDescription = if (isFullScreen) "Exit Full Screen" else "Full Screen",
                                                modifier = Modifier.size(18.dp)
                                            )
                                        }
                                    }
                                }
                                
                                Spacer(modifier = Modifier.weight(1f))
                                
                                // Validation status
                                isValid?.let {
                                    Row(
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.spacedBy(4.dp),
                                        modifier = Modifier
                                            .padding(horizontal = 8.dp, vertical = 4.dp)
                                            .background(
                                                if (it) 
                                                    MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.3f)
                                                else 
                                                    MaterialTheme.colorScheme.errorContainer.copy(alpha = 0.3f),
                                                RoundedCornerShape(16.dp)
                                            )
                                            .padding(horizontal = 12.dp, vertical = 6.dp)
                                    ) {
                                        AnimatedValidationIcon(
                                            isValid = it,
                                            modifier = Modifier.size(16.dp)
                                        )
                                        Text(
                                            if (it) "Valid" else "Invalid",
                                            style = MaterialTheme.typography.labelSmall,
                                            color = if (it) 
                                                MaterialTheme.colorScheme.primary 
                                            else 
                                                MaterialTheme.colorScheme.error
                                        )
                                    }
                                }
                                
                                Spacer(modifier = Modifier.weight(1f))
                                
                                // FAB menu button
                                var fabExpanded by remember { mutableStateOf(false) }
                                
                                Box {
                                    FloatingActionButton(
                                        onClick = { fabExpanded = !fabExpanded },
                                        modifier = Modifier.size(40.dp),
                                        containerColor = MaterialTheme.colorScheme.secondaryContainer
                                    ) {
                                        Icon(
                                            if (fabExpanded) Icons.Default.Close else Icons.Default.Menu,
                                            contentDescription = "Menu",
                                            modifier = Modifier.size(20.dp)
                                        )
                                    }
                                    
                                    // Expanded FAB menu overlay
                                    if (fabExpanded) {
                                        Surface(
                                            modifier = Modifier
                                                .fillMaxSize()
                                                .pointerInput(Unit) {
                                                    detectTapGestures {
                                                        fabExpanded = false
                                                    }
                                                },
                                            color = Color.Black.copy(alpha = 0.3f)
                                        ) {
                                            Column(
                                                modifier = Modifier
                                                    .align(Alignment.BottomEnd)
                                                    .padding(end = 16.dp, bottom = 80.dp),
                                                verticalArrangement = Arrangement.spacedBy(12.dp)
                                            ) {
                                                // Load actions
                                                FloatingActionButton(
                                                    onClick = {
                                                        val clip = clipboardManager.primaryClip
                                                        if (clip != null && clip.itemCount > 0) {
                                                            val text = clip.getItemAt(0).text.toString()
                                                            if (text.isNotEmpty()) {
                                                                viewModel.setJsonInput(text, addToHistory = true)
                                                            }
                                                        }
                                                        fabExpanded = false
                                                    },
                                                    modifier = Modifier.size(56.dp),
                                                    containerColor = MaterialTheme.colorScheme.tertiaryContainer
                                                ) {
                                                    Icon(Icons.Default.ContentPaste, "Clipboard", modifier = Modifier.size(24.dp))
                                                }
                                                
                                                FloatingActionButton(
                                                    onClick = { filePickerLauncher.launch("*/*"); fabExpanded = false },
                                                    modifier = Modifier.size(56.dp),
                                                    containerColor = MaterialTheme.colorScheme.tertiaryContainer
                                                ) {
                                                    Icon(Icons.Default.Folder, "File", modifier = Modifier.size(24.dp))
                                                }
                                                
                                                FloatingActionButton(
                                                    onClick = { showUrlDialog = true; fabExpanded = false },
                                                    modifier = Modifier.size(56.dp),
                                                    containerColor = MaterialTheme.colorScheme.tertiaryContainer
                                                ) {
                                                    Icon(Icons.Default.Link, "URL", modifier = Modifier.size(24.dp))
                                                }
                                                
                                                FloatingActionButton(
                                                    onClick = {
                                                        showRecentFilesDialog = true
                                                        scope.launch { recentFiles = viewModel.getRecentFiles(10) }
                                                        fabExpanded = false
                                                    },
                                                    modifier = Modifier.size(56.dp),
                                                    containerColor = MaterialTheme.colorScheme.tertiaryContainer
                                                ) {
                                                    Icon(Icons.Default.History, "Saved", modifier = Modifier.size(24.dp))
                                                }
                                                
                                                Divider(modifier = Modifier.width(56.dp))
                                                
                                                // Format actions
                                                FloatingActionButton(
                                                    onClick = { viewModel.minifyJson(); fabExpanded = false },
                                                    enabled = jsonInput.isNotEmpty(),
                                                    modifier = Modifier.size(56.dp),
                                                    containerColor = MaterialTheme.colorScheme.primaryContainer
                                                ) {
                                                    Icon(Icons.Default.ExpandLess, "Minify", modifier = Modifier.size(24.dp))
                                                }
                                                
                                                FloatingActionButton(
                                                    onClick = { viewModel.sortKeys(re.weare.app.util.JsonFormatter.SortOrder.ASC, re.weare.app.util.JsonFormatter.SortBy.KEY); fabExpanded = false },
                                                    enabled = jsonInput.isNotEmpty(),
                                                    modifier = Modifier.size(56.dp),
                                                    containerColor = MaterialTheme.colorScheme.primaryContainer
                                                ) {
                                                    Icon(Icons.Default.FilterList, "Sort", modifier = Modifier.size(24.dp))
                                                }
                                                
                                                Divider(modifier = Modifier.width(56.dp))
                                                
                                                // Export actions
                                                if (jsonOutput.isNotEmpty()) {
                                                    FloatingActionButton(
                                                        onClick = {
                                                            PdfExporter.exportToPdf(context, jsonOutput, "json_${SimpleDateFormat("yyyyMMdd_HHmmss", java.util.Locale.getDefault()).format(java.util.Date())}")
                                                            fabExpanded = false
                                                        },
                                                        modifier = Modifier.size(56.dp),
                                                        containerColor = MaterialTheme.colorScheme.errorContainer
                                                    ) {
                                                        Icon(Icons.Default.PictureAsPdf, "PDF", modifier = Modifier.size(24.dp))
                                                    }
                                                    
                                                    FloatingActionButton(
                                                        onClick = {
                                                            val clip = ClipData.newPlainText("JSON", jsonOutput)
                                                            clipboardManager.setPrimaryClip(clip)
                                                            fabExpanded = false
                                                        },
                                                        modifier = Modifier.size(56.dp),
                                                        containerColor = MaterialTheme.colorScheme.primary
                                                    ) {
                                                        Icon(Icons.Default.ContentCopy, "Copy", modifier = Modifier.size(24.dp))
                                                    }
                                                }
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }
                    
                    // Legacy toolbar rows removed - now using FAB menu above
                    // Content area starts here
                    if (false) {
                        Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    // Load dropdown
                                    DropdownMenuButton(
                                        label = "Load",
                                        icon = Icons.Default.Folder,
                                        items = listOf(
                                            CustomDropdownMenuItem(
                                                label = "From Clipboard",
                                                icon = Icons.Default.ContentCopy,
                                                onClick = {
                                                    val clip = clipboardManager.primaryClip
                                                    if (clip != null && clip.itemCount > 0) {
                                                        val text = clip.getItemAt(0).text.toString()
                                                        if (text.isNotEmpty()) {
                                                            viewModel.setJsonInput(text, addToHistory = true)
                                                        }
                                                    }
                                                }
                                            ),
                                            CustomDropdownMenuItem(
                                                label = "From File",
                                                icon = Icons.Default.Folder,
                                                onClick = { filePickerLauncher.launch("*/*") }
                                            ),
                                            CustomDropdownMenuItem(
                                                label = "From URL",
                                                icon = Icons.Default.Link,
                                                onClick = { showUrlDialog = true }
                                            ),
                                            CustomDropdownMenuItem(
                                                label = "From Saved JSON",
                                                icon = Icons.Default.History,
                                                onClick = { 
                                                    showRecentFilesDialog = true
                                                    scope.launch {
                                                        recentFiles = viewModel.getRecentFiles(10)
                                                    }
                                                }
                                            )
                                        )
                                    )
                                    
                                    // Undo/Redo buttons
                                    IconButton(
                                        onClick = { viewModel.undo() },
                                        enabled = canUndo,
                                        modifier = Modifier.size(36.dp)
                                    ) {
                                        Icon(
                                            Icons.Default.ArrowBack,
                                            contentDescription = "Undo",
                                            modifier = Modifier.size(18.dp)
                                        )
                                    }
                                    
                                    IconButton(
                                        onClick = { viewModel.redo() },
                                        enabled = canRedo,
                                        modifier = Modifier.size(36.dp)
                                    ) {
                                        Icon(
                                            Icons.Default.ArrowForward,
                                            contentDescription = "Redo",
                                            modifier = Modifier.size(18.dp)
                                        )
                                    }
                                    
                                    // Clear button
                                    ToolbarButton(
                                        onClick = { viewModel.clear() },
                                        icon = Icons.Default.Clear,
                                        text = "Clear"
                                    )
                                    
                                    Spacer(modifier = Modifier.weight(1f))
                                    
                                    // Validation status with animated icon
                                    isValid?.let {
                                        Row(
                                            verticalAlignment = Alignment.CenterVertically,
                                            horizontalArrangement = Arrangement.spacedBy(4.dp),
                                            modifier = Modifier
                                                .padding(horizontal = 8.dp, vertical = 4.dp)
                                                .background(
                                                    if (it) 
                                                        MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.3f)
                                                    else 
                                                        MaterialTheme.colorScheme.errorContainer.copy(alpha = 0.3f),
                                                    RoundedCornerShape(16.dp)
                                                )
                                                .padding(horizontal = 12.dp, vertical = 6.dp)
                                        ) {
                                            AnimatedValidationIcon(
                                                isValid = it,
                                                modifier = Modifier.size(16.dp)
                                            )
                                            Text(
                                                if (it) "Valid" else "Invalid",
                                                style = MaterialTheme.typography.labelSmall,
                                                color = if (it) 
                                                    MaterialTheme.colorScheme.primary 
                                                else 
                                                    MaterialTheme.colorScheme.error
                                            )
                                        }
                                    }
                                }
                                
                                // Second row: Format actions and view mode
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    // Format dropdown with Sort Keys options
                                    var formatMenuExpanded by remember { mutableStateOf(false) }
                                    
                                    Box {
                                        FilledTonalButton(
                                            onClick = { formatMenuExpanded = true },
                                            enabled = jsonInput.isNotEmpty(),
                                            modifier = Modifier.height(36.dp),
                                            contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp)
                                        ) {
                                            Icon(
                                                Icons.Default.FilterList,
                                                contentDescription = null,
                                                modifier = Modifier.size(16.dp)
                                            )
                                            Spacer(modifier = Modifier.width(6.dp))
                                            Text(
                                                "Format",
                                                style = MaterialTheme.typography.labelSmall
                                            )
                                            Icon(
                                                Icons.Default.ArrowDropDown,
                                                contentDescription = null,
                                                modifier = Modifier.size(16.dp)
                                            )
                                        }
                                        
                                        DropdownMenu(
                                            expanded = formatMenuExpanded,
                                            onDismissRequest = { formatMenuExpanded = false }
                                        ) {
                                            DropdownMenuItem(
                                                text = {
                                                    Row(
                                                        verticalAlignment = Alignment.CenterVertically,
                                                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                                                    ) {
                                                        Icon(
                                                            Icons.Default.ExpandLess,
                                                            contentDescription = null,
                                                            modifier = Modifier.size(18.dp)
                                                        )
                                                        Text("Minify")
                                                    }
                                                },
                                                onClick = {
                                                    viewModel.minifyJson()
                                                    formatMenuExpanded = false
                                                }
                                            )
                                            
                                            Divider()
                                            
                                            // Sort Keys options
                                            DropdownMenuItem(
                                                text = {
                                                    Row(
                                                        verticalAlignment = Alignment.CenterVertically,
                                                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                                                    ) {
                                                        Icon(
                                                            Icons.Default.FilterList,
                                                            contentDescription = null,
                                                            modifier = Modifier.size(18.dp)
                                                        )
                                                        Text("Sort: By Key (ASC)")
                                                    }
                                                },
                                                onClick = {
                                                    viewModel.sortKeys(re.weare.app.util.JsonFormatter.SortOrder.ASC, re.weare.app.util.JsonFormatter.SortBy.KEY)
                                                    formatMenuExpanded = false
                                                }
                                            )
                                            DropdownMenuItem(
                                                text = {
                                                    Row(
                                                        verticalAlignment = Alignment.CenterVertically,
                                                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                                                    ) {
                                                        Icon(
                                                            Icons.Default.FilterList,
                                                            contentDescription = null,
                                                            modifier = Modifier.size(18.dp)
                                                        )
                                                        Text("Sort: By Key (DESC)")
                                                    }
                                                },
                                                onClick = {
                                                    viewModel.sortKeys(re.weare.app.util.JsonFormatter.SortOrder.DESC, re.weare.app.util.JsonFormatter.SortBy.KEY)
                                                    formatMenuExpanded = false
                                                }
                                            )
                                            Divider()
                                            DropdownMenuItem(
                                                text = {
                                                    Row(
                                                        verticalAlignment = Alignment.CenterVertically,
                                                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                                                    ) {
                                                        Icon(
                                                            Icons.Default.FilterList,
                                                            contentDescription = null,
                                                            modifier = Modifier.size(18.dp)
                                                        )
                                                        Text("Sort: By Type (ASC)")
                                                    }
                                                },
                                                onClick = {
                                                    viewModel.sortKeys(re.weare.app.util.JsonFormatter.SortOrder.ASC, re.weare.app.util.JsonFormatter.SortBy.TYPE)
                                                    formatMenuExpanded = false
                                                }
                                            )
                                            DropdownMenuItem(
                                                text = {
                                                    Row(
                                                        verticalAlignment = Alignment.CenterVertically,
                                                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                                                    ) {
                                                        Icon(
                                                            Icons.Default.FilterList,
                                                            contentDescription = null,
                                                            modifier = Modifier.size(18.dp)
                                                        )
                                                        Text("Sort: By Type (DESC)")
                                                    }
                                                },
                                                onClick = {
                                                    viewModel.sortKeys(re.weare.app.util.JsonFormatter.SortOrder.DESC, re.weare.app.util.JsonFormatter.SortBy.TYPE)
                                                    formatMenuExpanded = false
                                                }
                                            )
                                            Divider()
                                            DropdownMenuItem(
                                                text = {
                                                    Row(
                                                        verticalAlignment = Alignment.CenterVertically,
                                                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                                                    ) {
                                                        Icon(
                                                            Icons.Default.FilterList,
                                                            contentDescription = null,
                                                            modifier = Modifier.size(18.dp)
                                                        )
                                                        Text("Sort: By Value (ASC)")
                                                    }
                                                },
                                                onClick = {
                                                    viewModel.sortKeys(re.weare.app.util.JsonFormatter.SortOrder.ASC, re.weare.app.util.JsonFormatter.SortBy.VALUE)
                                                    formatMenuExpanded = false
                                                }
                                            )
                                            DropdownMenuItem(
                                                text = {
                                                    Row(
                                                        verticalAlignment = Alignment.CenterVertically,
                                                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                                                    ) {
                                                        Icon(
                                                            Icons.Default.FilterList,
                                                            contentDescription = null,
                                                            modifier = Modifier.size(18.dp)
                                                        )
                                                        Text("Sort: By Value (DESC)")
                                                    }
                                                },
                                                onClick = {
                                                    viewModel.sortKeys(re.weare.app.util.JsonFormatter.SortOrder.DESC, re.weare.app.util.JsonFormatter.SortBy.VALUE)
                                                    formatMenuExpanded = false
                                                }
                                            )
                                        }
                                    }
                                    
                                    // Validate button
                                    ToolbarButton(
                                        onClick = { viewModel.validateJson() },
                                        icon = Icons.Default.CheckCircle,
                                        text = "Validate",
                                        enabled = jsonInput.isNotEmpty()
                                    )
                                    
                                    // Search & Replace button
                                    IconButton(
                                        onClick = { showSearchReplace = true },
                                        modifier = Modifier.size(36.dp)
                                    ) {
                                        Icon(
                                            Icons.Default.Search,
                                            contentDescription = "Search & Replace",
                                            modifier = Modifier.size(18.dp)
                                        )
                                    }
                                    
                                    Spacer(modifier = Modifier.weight(1f))
                                    
                                    // View mode toggle (only show when output exists) - Fixed for portrait
                                    if (jsonOutput.isNotEmpty()) {
                                        var viewModeExpanded by remember { mutableStateOf(false) }
                                        
                                        Box {
                                            FilledTonalButton(
                                                onClick = { viewModeExpanded = true },
                                                modifier = Modifier.height(36.dp),
                                                contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp)
                                            ) {
                                                Icon(
                                                    when (viewMode) {
                                                        "tree" -> Icons.Default.AccountTree
                                                        else -> Icons.Default.Edit
                                                    },
                                                    contentDescription = null,
                                                    modifier = Modifier.size(16.dp)
                                                )
                                                Spacer(modifier = Modifier.width(6.dp))
                                                Text(
                                                    when (viewMode) {
                                                        "tree" -> "Tree"
                                                        else -> "Editor"
                                                    },
                                                    style = MaterialTheme.typography.labelSmall
                                                )
                                                Icon(
                                                    Icons.Default.ArrowDropDown,
                                                    contentDescription = null,
                                                    modifier = Modifier.size(16.dp)
                                                )
                                            }
                                            
                                            DropdownMenu(
                                                expanded = viewModeExpanded,
                                                onDismissRequest = { viewModeExpanded = false }
                                            ) {
                                                DropdownMenuItem(
                                                    text = { Text("Editor") },
                                                    onClick = {
                                                        viewMode = "editor"
                                                        viewModeExpanded = false
                                                    },
                                                    leadingIcon = {
                                                        Icon(Icons.Default.Edit, null)
                                                    }
                                                )
                                                DropdownMenuItem(
                                                    text = { Text("Tree") },
                                                    onClick = {
                                                        viewMode = "tree"
                                                        viewModeExpanded = false
                                                    },
                                                    leadingIcon = {
                                                        Icon(Icons.Default.AccountTree, null)
                                                    }
                                                )
                                            }
                                        }
                                    }
                                }
                                
                                // Third row: Tab spaces (dropdown) and actions
                                if (jsonOutput.isNotEmpty()) {
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        // Tab Space dropdown
                                        var tabSpaceMenuExpanded by remember { mutableStateOf(false) }
                                        
                                        Box {
                                            FilledTonalButton(
                                                onClick = { tabSpaceMenuExpanded = true },
                                                modifier = Modifier.height(36.dp),
                                                contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp)
                                            ) {
                                                Icon(
                                                    Icons.Default.ViewList,
                                                    contentDescription = null,
                                                    modifier = Modifier.size(16.dp)
                                                )
                                                Spacer(modifier = Modifier.width(6.dp))
                                                Text(
                                                    "Tab: $tabSpaces",
                                                    style = MaterialTheme.typography.labelSmall
                                                )
                                                Icon(
                                                    Icons.Default.ArrowDropDown,
                                                    contentDescription = null,
                                                    modifier = Modifier.size(16.dp)
                                                )
                                            }
                                            
                                            DropdownMenu(
                                                expanded = tabSpaceMenuExpanded,
                                                onDismissRequest = { tabSpaceMenuExpanded = false }
                                            ) {
                                                listOf(1, 2, 3, 4).forEach { spaces ->
                                                    DropdownMenuItem(
                                                        text = {
                                                            Row(
                                                                modifier = Modifier.fillMaxWidth(),
                                                                horizontalArrangement = Arrangement.SpaceBetween,
                                                                verticalAlignment = Alignment.CenterVertically
                                                            ) {
                                                                Text("$spaces space${if (spaces > 1) "s" else ""}")
                                                                if (tabSpaces == spaces) {
                                                                    Icon(
                                                                        Icons.Default.Check,
                                                                        contentDescription = null,
                                                                        modifier = Modifier.size(18.dp),
                                                                        tint = MaterialTheme.colorScheme.primary
                                                                    )
                                                                }
                                                            }
                                                        },
                                                        onClick = {
                                                            viewModel.setTabSpaces(spaces)
                                                            tabSpaceMenuExpanded = false
                                                        }
                                                    )
                                                }
                                            }
                                        }
                                        
                                        Spacer(modifier = Modifier.weight(1f))
                                        
                                        // Action buttons
                                        IconButton(
                                            onClick = {
                                                val clip = ClipData.newPlainText("JSON", jsonOutput)
                                                clipboardManager.setPrimaryClip(clip)
                                            },
                                            modifier = Modifier.size(36.dp)
                                        ) {
                                            Icon(
                                                Icons.Default.ContentCopy,
                                                contentDescription = "Copy",
                                                modifier = Modifier.size(18.dp)
                                            )
                                        }
                                        
                                        IconButton(
                                            onClick = { showSaveDialog = true },
                                            modifier = Modifier.size(36.dp)
                                        ) {
                                            Icon(
                                                Icons.Default.Favorite,
                                                contentDescription = "Save",
                                                modifier = Modifier.size(18.dp)
                                            )
                                        }
                                        
                                        // Export dropdown - More prominent
                                        var exportMenuExpanded by remember { mutableStateOf(false) }
                                        
                                        Box {
                                            FilledTonalButton(
                                                onClick = { exportMenuExpanded = true },
                                                enabled = jsonOutput.isNotEmpty(),
                                                modifier = Modifier.height(36.dp),
                                                contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp)
                                            ) {
                                                Icon(
                                                    Icons.Default.FileDownload,
                                                    contentDescription = "Export",
                                                    modifier = Modifier.size(16.dp)
                                                )
                                                Spacer(modifier = Modifier.width(6.dp))
                                                Text(
                                                    "Export",
                                                    style = MaterialTheme.typography.labelSmall
                                                )
                                                Icon(
                                                    Icons.Default.ArrowDropDown,
                                                    contentDescription = null,
                                                    modifier = Modifier.size(16.dp)
                                                )
                                            }
                                            
                                            DropdownMenu(
                                                expanded = exportMenuExpanded,
                                                onDismissRequest = { exportMenuExpanded = false }
                                            ) {
                                                DropdownMenuItem(
                                                    text = {
                                                        Row(
                                                            verticalAlignment = Alignment.CenterVertically,
                                                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                                                        ) {
                                                            Icon(
                                                                Icons.Default.PictureAsPdf,
                                                                contentDescription = null,
                                                                modifier = Modifier.size(18.dp)
                                                            )
                                                            Text("Export as PDF")
                                                        }
                                                    },
                                                    onClick = {
                                                        if (jsonOutput.isNotEmpty()) {
                                                            PdfExporter.exportToPdf(
                                                                context,
                                                                jsonOutput,
                                                                "json_${SimpleDateFormat("yyyyMMdd_HHmmss", java.util.Locale.getDefault()).format(java.util.Date())}"
                                                            )
                                                            exportMenuExpanded = false
                                                            scope.launch {
                                                                snackbarHostState.showSnackbar(
                                                                    message = "Opening PDF print dialog...",
                                                                    duration = SnackbarDuration.Short
                                                                )
                                                            }
                                                        }
                                                    },
                                                    enabled = jsonOutput.isNotEmpty()
                                                )
                                                
                                                DropdownMenuItem(
                                                    text = {
                                                        Row(
                                                            verticalAlignment = Alignment.CenterVertically,
                                                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                                                        ) {
                                                            Icon(
                                                                Icons.Default.Description,
                                                                contentDescription = null,
                                                                modifier = Modifier.size(18.dp)
                                                            )
                                                            Text("Export as .json File")
                                                        }
                                                    },
                                                    onClick = {
                                                        if (jsonOutput.isNotEmpty()) {
                                                            scope.launch {
                                                                withContext(Dispatchers.IO) {
                                                                    val fileName = "json_${SimpleDateFormat("yyyyMMdd_HHmmss", java.util.Locale.getDefault()).format(java.util.Date())}"
                                                                    val result = PdfExporter.exportToJsonFile(context, jsonOutput, fileName)
                                                                    withContext(Dispatchers.Main) {
                                                                        result.onSuccess { file ->
                                                                            val snackbarResult = snackbarHostState.showSnackbar(
                                                                                message = "JSON exported successfully",
                                                                                duration = SnackbarDuration.Short,
                                                                                actionLabel = "Share"
                                                                            )
                                                                            if (snackbarResult == SnackbarResult.ActionPerformed) {
                                                                                PdfExporter.shareJsonFile(context, file, fileName)
                                                                            }
                                                                        }.onFailure {
                                                                            snackbarHostState.showSnackbar(
                                                                                message = "Export failed: ${it.message}",
                                                                                duration = SnackbarDuration.Long
                                                                            )
                                                                        }
                                                                    }
                                                                }
                                                            }
                                                            exportMenuExpanded = false
                                                        }
                                                    },
                                                    enabled = jsonOutput.isNotEmpty()
                                                )
                                                
                                                Divider()
                                                
                                                DropdownMenuItem(
                                                    text = {
                                                        Row(
                                                            verticalAlignment = Alignment.CenterVertically,
                                                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                                                        ) {
                                                            Icon(
                                                                Icons.Default.Share,
                                                                contentDescription = null,
                                                                modifier = Modifier.size(18.dp)
                                                            )
                                                            Text("Share as Text")
                                                        }
                                                    },
                                                    onClick = {
                                                        val sendIntent = Intent().apply {
                                                            action = Intent.ACTION_SEND
                                                            putExtra(Intent.EXTRA_TEXT, jsonOutput)
                                                            type = "text/plain"
                                                        }
                                                        context.startActivity(Intent.createChooser(sendIntent, "Share JSON"))
                                                        exportMenuExpanded = false
                                                    },
                                                    enabled = jsonOutput.isNotEmpty()
                                                )
                                            }
                                        }
                                        
                                        IconButton(
                                            onClick = {
                                                onNavigate(NavigationRoutes.SETTINGS)
                                            },
                                            modifier = Modifier.size(36.dp)
                                        ) {
                                            Icon(
                                                Icons.Default.Settings,
                                                contentDescription = "Settings",
                                                modifier = Modifier.size(18.dp)
                                            )
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
        },
        bottomBar = {
            BannerAdView()
        },
        snackbarHost = {
            SnackbarHost(hostState = snackbarHostState)
        }
    ) { paddingValues ->
        // Full screen mode for JSON output
        if (isFullScreen && jsonOutput.isNotEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues)
                    .background(MaterialTheme.colorScheme.surface)
            ) {
                // Full screen JSON content
                Box(
                    modifier = Modifier.fillMaxSize()
                ) {
                    // Content based on view mode
                    if (viewMode == "tree") {
                        JsonTreeView(
                            jsonString = jsonOutput,
                            onReorder = { reorderedJson ->
                                viewModel.reorderJson(reorderedJson)
                            },
                            modifier = Modifier.fillMaxSize()
                        )
                    } else {
                        LineNumberTextField(
                            value = jsonOutput,
                            onValueChange = {},
                            modifier = Modifier.fillMaxSize(),
                            readOnly = true,
                            fontFamily = fontFamily,
                            textSize = textSize
                        )
                    }
                }
                
                // Exit full screen button (top-right corner)
                FloatingActionButton(
                    onClick = { isFullScreen = false },
                    modifier = Modifier
                        .align(Alignment.TopEnd)
                        .padding(16.dp)
                        .size(48.dp),
                    containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.9f),
                    contentColor = MaterialTheme.colorScheme.onSurfaceVariant
                ) {
                    Icon(
                        Icons.Default.Close,
                        contentDescription = "Exit Full Screen",
                        modifier = Modifier.size(24.dp)
                    )
                }
                
                // View mode toggle (bottom-right corner)
                Column(
                    modifier = Modifier
                        .align(Alignment.BottomEnd)
                        .padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    FloatingActionButton(
                        onClick = { viewMode = if (viewMode == "tree") "editor" else "tree" },
                        modifier = Modifier.size(48.dp),
                        containerColor = MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.9f),
                        contentColor = MaterialTheme.colorScheme.onSecondaryContainer
                    ) {
                        Icon(
                            if (viewMode == "tree") Icons.Default.Edit else Icons.Default.AccountTree,
                            contentDescription = if (viewMode == "tree") "Switch to Editor" else "Switch to Tree",
                            modifier = Modifier.size(24.dp)
                        )
                    }
                }
            }
        } else {
            // Main content area - tabs for Input/Output
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues)
            ) {
            // Tabs for Input/Output
            TabRow(
                selectedTabIndex = selectedTab,
                modifier = Modifier.fillMaxWidth()
            ) {
                Tab(
                    selected = selectedTab == 0,
                    onClick = { selectedTab = 0 },
                    text = { 
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            Icon(
                                Icons.Default.Edit,
                                contentDescription = null,
                                modifier = Modifier.size(18.dp)
                            )
                            Text("JSON Input")
                        }
                    }
                )
                
                Tab(
                    selected = selectedTab == 1,
                    onClick = { selectedTab = 1 },
                    enabled = jsonOutput.isNotEmpty(),
                    text = { 
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            Icon(
                                Icons.Default.Visibility,
                                contentDescription = null,
                                modifier = Modifier.size(18.dp)
                            )
                            Text("JSON Output")
                        }
                    }
                )
            }
            
            // Content based on selected tab with fade animation
            Surface(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(8.dp),
                shape = RoundedCornerShape(8.dp),
                shadowElevation = 2.dp
            ) {
                AnimatedContent(
                    targetState = selectedTab,
                    transitionSpec = {
                        fadeIn(animationSpec = tween(300)) togetherWith
                        fadeOut(animationSpec = tween(300))
                    },
                    label = "tab_content"
                ) { tab ->
                    when (tab) {
                        0 -> {
                            // Input tab
                            Column(
                            modifier = Modifier
                                .fillMaxSize()
                                .padding(12.dp)
                        ) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    "JSON Input",
                                    style = MaterialTheme.typography.labelLarge.copy(
                                        fontWeight = FontWeight.SemiBold
                                    )
                                )
                                
                                Row(
                                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                                ) {
                                    IconButton(
                                        onClick = {
                                            val clip = clipboardManager.primaryClip
                                            clip?.getItemAt(0)?.text?.toString()?.let {
                        viewModel.setJsonInput(it, addToHistory = true)
                    }
                },
                                        modifier = Modifier.size(32.dp)
                                    ) {
                                        Icon(
                                            Icons.Default.ContentPaste,
                                            contentDescription = "Paste",
                                            modifier = Modifier.size(16.dp)
                                        )
                                    }
                                    
                                    IconButton(
                                        onClick = {
                                            val clip = ClipData.newPlainText("JSON", jsonInput)
                                            clipboardManager.setPrimaryClip(clip)
                                        },
                                        modifier = Modifier.size(32.dp)
                                    ) {
                                        Icon(
                                            Icons.Default.ContentCopy,
                                            contentDescription = "Copy",
                                            modifier = Modifier.size(16.dp)
                                        )
                                    }
                                }
                            }
                            
                            Spacer(modifier = Modifier.height(8.dp))
                            
                            // Demo JSON button when empty
                            if (jsonInput.isEmpty()) {
                                Box(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(horizontal = 0.dp, vertical = 8.dp)
                                ) {
                                    FilledTonalButton(
                                        onClick = {
                                            viewModel.setJsonInput(ExampleJson.getRandomExample(), addToHistory = true)
                                        },
                                        modifier = Modifier.fillMaxWidth()
                                    ) {
                                        Icon(
                                            Icons.Default.Info,
                                            contentDescription = null,
                                            modifier = Modifier.size(18.dp)
                                        )
                                        Spacer(modifier = Modifier.width(8.dp))
                                        Text("Try Example JSON")
                                    }
                                }
                            }
                            
                            // Inline Error Panel (collapsible bottom panel)
                            var showErrorPanel by remember { mutableStateOf(isValid == false) }
                            
                            LaunchedEffect(isValid) {
                                showErrorPanel = isValid == false
                            }
                            
                            if (isValid == false && errorMessage != null && showErrorPanel) {
                                Card(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(bottom = 8.dp),
                                    colors = CardDefaults.cardColors(
                                        containerColor = MaterialTheme.colorScheme.errorContainer.copy(alpha = 0.3f)
                                    ),
                                    shape = RoundedCornerShape(8.dp)
                                ) {
                                    Column(
                                        modifier = Modifier.padding(12.dp)
                                    ) {
                                        Row(
                                            modifier = Modifier.fillMaxWidth(),
                                            horizontalArrangement = Arrangement.SpaceBetween,
                                            verticalAlignment = Alignment.CenterVertically
                                        ) {
                                            Row(
                                                verticalAlignment = Alignment.CenterVertically,
                                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                                            ) {
                                                Icon(
                                                    Icons.Default.Warning,
                                                    contentDescription = null,
                                                    tint = MaterialTheme.colorScheme.error,
                                                    modifier = Modifier.size(18.dp)
                                                )
                                                Column {
                                                    Text(
                                                        errorMessage ?: "Invalid JSON",
                                                        style = MaterialTheme.typography.bodySmall.copy(
                                                            fontWeight = FontWeight.SemiBold
                                                        ),
                                                        color = MaterialTheme.colorScheme.error
                                                    )
                                                    errorLocation?.let { location ->
                                                        Text(
                                                            "Line ${location.line}${if (location.column > 0) ", Column ${location.column}" else ""}",
                                                            style = MaterialTheme.typography.bodySmall,
                                                            color = MaterialTheme.colorScheme.error.copy(alpha = 0.8f)
                                                        )
                                                    }
                                                }
                                            }
                                            
                                            IconButton(
                                                onClick = { showErrorPanel = false },
                                                modifier = Modifier.size(24.dp)
                                            ) {
                                                Icon(
                                                    Icons.Default.Close,
                                                    contentDescription = "Close",
                                                    modifier = Modifier.size(16.dp)
                                                )
                                            }
                                        }
                                        
                                        // Jump to line button
                                        errorLocation?.let { location ->
                                            Spacer(modifier = Modifier.height(8.dp))
                                            TextButton(
                                                onClick = {
                                                    // Note: Scroll to line would require scroll controller integration
                                                    // This is a placeholder for the functionality
                                                },
                                                modifier = Modifier.fillMaxWidth()
                                            ) {
                                                Icon(
                                                    Icons.Default.ArrowDownward,
                                                    contentDescription = null,
                                                    modifier = Modifier.size(16.dp)
                                                )
                                                Spacer(modifier = Modifier.width(4.dp))
                                                Text("Jump to Line ${location.line}")
                                            }
                                        }
                                    }
                                }
                            }
                            
                            // JSON Input Editor with font family and size support
                            LineNumberTextField(
                                value = jsonInput,
                                onValueChange = { viewModel.setJsonInput(it, addToHistory = true) },
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .weight(1f)
                                    .fillMaxHeight(),
                                placeholder = "Enter or paste JSON here...",
                                errorLine = errorLocation?.line,
                                fontFamily = fontFamily,
                                textSize = textSize
                            )
                        }
                    }
                        1 -> {
                            // Output tab
                            if (jsonOutput.isNotEmpty()) {
                            Column(
                                modifier = Modifier
                                    .fillMaxSize()
                                    .padding(12.dp)
                            ) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text(
                                        "JSON Output",
                                        style = MaterialTheme.typography.labelLarge.copy(
                                            fontWeight = FontWeight.SemiBold
                                        )
                                    )
                                }
                                
                                Spacer(modifier = Modifier.height(8.dp))
                                
                                // Content based on view mode
                                if (viewMode == "tree") {
                                    Box(
                                        modifier = Modifier
                                            .fillMaxSize()
                                            .weight(1f)
                                            .background(
                                                MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.2f),
                                                RoundedCornerShape(6.dp)
                                            )
                                            .padding(4.dp)
                                    ) {
                                        JsonTreeView(
                                            jsonString = jsonOutput,
                                            onReorder = { reorderedJson ->
                                                viewModel.reorderJson(reorderedJson)
                                            }
                                        )
                                    }
                                } else {
                                    LineNumberTextField(
                                        value = jsonOutput,
                                        onValueChange = {},
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .weight(1f)
                                            .fillMaxHeight(),
                                        readOnly = true,
                                        fontFamily = fontFamily,
                                        textSize = textSize
                                    )
                                }
                            }
                        } else {
                            Box(
                                modifier = Modifier.fillMaxSize(),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    "No output yet. Enter valid JSON to see formatted output.",
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f)
                                )
                            }
                        }
                    }
                }
                    }
            }
        }
    }
    
    // URL Load Dialog
    if (showUrlDialog) {
        AlertDialog(
            onDismissRequest = { 
                showUrlDialog = false
                urlInput = ""
            },
            title = { Text("Load JSON from URL") },
            text = {
                Column(
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    OutlinedTextField(
                        value = urlInput,
                        onValueChange = { urlInput = it },
                        label = { Text("URL") },
                        placeholder = { Text("https://example.com/data.json") },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true
                    )
                    if (isLoadingUrl) {
                        LinearProgressIndicator(modifier = Modifier.fillMaxWidth())
                        Text("Loading...", style = MaterialTheme.typography.bodySmall)
                    }
                }
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        scope.launch {
                            isLoadingUrl = true
                            try {
                                val json = withContext(Dispatchers.IO) {
                                    URL(urlInput).readText()
                                }
                            viewModel.setJsonInput(json, addToHistory = true)
                            showUrlDialog = false
                                urlInput = ""
                            } catch (e: Exception) {
                                showFileError = "Failed to load URL: ${e.message}"
                            } finally {
                                isLoadingUrl = false
                            }
                        }
                    },
                    enabled = urlInput.isNotEmpty() && !isLoadingUrl
                ) {
                    Text("Load")
                }
            },
            dismissButton = {
                TextButton(onClick = { 
                    showUrlDialog = false
                    urlInput = ""
                }) {
                    Text("Cancel")
                }
            }
        )
    }
    
    // Save Dialog
    if (showSaveDialog) {
        AlertDialog(
            onDismissRequest = { 
                showSaveDialog = false
                saveFileName = ""
            },
            title = { Text("Save JSON") },
            text = {
                OutlinedTextField(
                    value = saveFileName,
                    onValueChange = { saveFileName = it },
                    label = { Text("File Name") },
                    placeholder = { Text("my_json") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true
                )
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        scope.launch {
                            viewModel.saveJson(saveFileName.takeIf { it.isNotEmpty() } ?: "json_${System.currentTimeMillis()}")
                            showSaveDialog = false
                            saveFileName = ""
                        }
                    }
                ) {
                    Text("Save")
                }
            },
            dismissButton = {
                TextButton(onClick = { 
                    showSaveDialog = false
                    saveFileName = ""
                }) {
                    Text("Cancel")
                }
            }
        )
    }
    
    // Search & Replace Dialog
    if (showSearchReplace) {
        var replaceAll by remember { mutableStateOf(false) }
        
        AlertDialog(
            onDismissRequest = { 
                showSearchReplace = false
                searchText = ""
                replaceText = ""
            },
            title = { Text("Search & Replace") },
            text = {
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    OutlinedTextField(
                        value = searchText,
                        onValueChange = { searchText = it },
                        label = { Text("Find") },
                        placeholder = { Text("Enter text to find") },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true,
                        leadingIcon = {
                            Icon(Icons.Default.Search, contentDescription = null)
                        }
                    )
                    
                    OutlinedTextField(
                        value = replaceText,
                        onValueChange = { replaceText = it },
                        label = { Text("Replace with") },
                        placeholder = { Text("Enter replacement text") },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true
                    )
                    
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            "Replace all occurrences",
                            style = MaterialTheme.typography.bodyMedium
                        )
                        Switch(
                            checked = replaceAll,
                            onCheckedChange = { replaceAll = it }
                        )
                    }
                }
            },
            confirmButton = {
                Row(
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    TextButton(
                        onClick = {
                            if (searchText.isNotEmpty()) {
                                // Find and highlight (could be enhanced)
                                val index = jsonInput.indexOf(searchText, ignoreCase = true)
                                if (index >= 0) {
                                    // Scroll to position could be added
                                }
                            }
                        },
                        enabled = searchText.isNotEmpty()
                    ) {
                        Text("Find")
                    }
                    
                    TextButton(
                        onClick = {
                            if (searchText.isNotEmpty()) {
                                val regex = Regex(searchText, RegexOption.IGNORE_CASE)
                                val newText = if (replaceAll) {
                                    jsonInput.replace(regex, replaceText)
                                } else {
                                    jsonInput.replaceFirst(regex, replaceText)
                                }
                                viewModel.setJsonInput(newText)
                            }
                        },
                        enabled = searchText.isNotEmpty()
                    ) {
                        Text("Replace")
                    }
                }
            },
            dismissButton = {
                TextButton(onClick = { 
                    showSearchReplace = false
                    searchText = ""
                    replaceText = ""
                }) {
                    Text("Close")
                }
            }
        )
    }
    
    // Clipboard JSON Detected - Use Snackbar (better UX than dialog)
    LaunchedEffect(showClipboardDialog, clipboardJsonContent) {
        if (showClipboardDialog && clipboardJsonContent != null && jsonInput.isEmpty()) {
            val result = snackbarHostState.showSnackbar(
                message = "JSON detected in clipboard",
                actionLabel = "Paste",
                duration = SnackbarDuration.Short
            )
            when (result) {
                SnackbarResult.ActionPerformed -> {
                    clipboardJsonContent?.let {
                        viewModel.setJsonInput(it)
                    }
                    showClipboardDialog = false
                    clipboardJsonContent = null
                }
                SnackbarResult.Dismissed -> {
                    showClipboardDialog = false
                    clipboardJsonContent = null
                }
            }
        }
    }
    
    // Recent Files Dialog
    if (showRecentFilesDialog && recentFiles.isNotEmpty()) {
        AlertDialog(
            onDismissRequest = { showRecentFilesDialog = false },
            title = { 
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Icon(Icons.Default.Folder, contentDescription = null)
                    Text("Recent JSON Files")
                }
            },
            text = {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .heightIn(max = 400.dp),
                    verticalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    Text(
                        "Open a recent file or start fresh:",
                        style = MaterialTheme.typography.bodyMedium,
                        modifier = Modifier.padding(bottom = 8.dp)
                    )
                    
                    recentFiles.take(5).forEach { savedJson ->
                        Card(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable {
                                    viewModel.setJsonInput(savedJson.content, addToHistory = true)
                                    showRecentFilesDialog = false
                                },
                            colors = CardDefaults.cardColors(
                                containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f)
                            )
                        ) {
                            Column(
                                modifier = Modifier.padding(12.dp)
                            ) {
                                Text(
                                    savedJson.name,
                                    style = MaterialTheme.typography.titleSmall,
                                    fontWeight = FontWeight.SemiBold
                                )
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(top = 4.dp),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text(
                                        "${savedJson.content.length} characters",
                                        style = MaterialTheme.typography.bodySmall,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                    Text(
                                        re.weare.app.util.DateFormatter.formatDateTime(savedJson.updatedAt),
                                        style = MaterialTheme.typography.bodySmall,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f)
                                    )
                                }
                            }
                        }
                    }
                }
            },
            confirmButton = {
                TextButton(onClick = { showRecentFilesDialog = false }) {
                    Text("Start Fresh")
                }
            },
            dismissButton = {
                TextButton(onClick = { 
                    showRecentFilesDialog = false
                }) {
                    Text("Close")
                }
            }
        )
    }
    
    // File Error Dialog
    showFileError?.let { error ->
        AlertDialog(
            onDismissRequest = { showFileError = null },
            title = { Text("Error") },
            text = { Text(error) },
            confirmButton = {
                TextButton(onClick = { showFileError = null }) {
                    Text("OK")
                }
            }
        )
    }
}

@Composable
fun ToolbarButton(
    onClick: () -> Unit,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    text: String,
    enabled: Boolean = true
) {
    FilledTonalButton(
        onClick = onClick,
        enabled = enabled,
        modifier = Modifier.height(36.dp),
        contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp)
    ) {
        Icon(
            icon,
            contentDescription = null,
            modifier = Modifier.size(16.dp)
        )
        Spacer(modifier = Modifier.width(6.dp))
        Text(
            text,
            style = MaterialTheme.typography.labelSmall
        )
    }
}

@Composable
fun ViewModeButton(
    label: String,
    selected: Boolean,
    onClick: () -> Unit
) {
    TextButton(
        onClick = onClick,
        modifier = Modifier.height(32.dp),
        contentPadding = PaddingValues(horizontal = 16.dp, vertical = 6.dp),
        colors = ButtonDefaults.textButtonColors(
            contentColor = if (selected)
                MaterialTheme.colorScheme.primary
            else
                MaterialTheme.colorScheme.onSurfaceVariant
        )
    ) {
        Text(
            label,
            style = MaterialTheme.typography.labelSmall.copy(
                fontWeight = if (selected) FontWeight.SemiBold else FontWeight.Normal
            )
        )
    }
}
