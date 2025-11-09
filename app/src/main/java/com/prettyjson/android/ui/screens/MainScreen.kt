package com.prettyjson.android.ui.screens

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.content.Intent
import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.systemBars
import androidx.compose.foundation.layout.statusBars
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.ui.window.DialogProperties
import androidx.core.view.WindowCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.WindowInsetsControllerCompat
import androidx.activity.ComponentActivity
import androidx.compose.ui.platform.LocalView
import androidx.compose.runtime.*
import androidx.compose.material3.SnackbarResult
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.foundation.layout.Box
import org.koin.androidx.compose.koinViewModel
import com.prettyjson.android.ui.components.BannerAdView
import com.prettyjson.android.ui.components.JsonTreeView
import com.prettyjson.android.ui.components.LineNumberTextField
import com.prettyjson.android.ui.components.ExampleJson
import com.prettyjson.android.ui.components.AnimatedValidationIcon
import com.prettyjson.android.ui.components.ShimmerPlaceholder
import com.prettyjson.android.ui.components.DropdownMenuButton
import com.prettyjson.android.ui.components.DropdownMenuItem as CustomDropdownMenuItem
import com.prettyjson.android.ui.navigation.NavigationRoutes
import com.prettyjson.android.ui.viewmodel.MainViewModel
import com.prettyjson.android.data.database.DataBucket
import com.prettyjson.android.util.FileManager
import com.prettyjson.android.util.PdfExporter
import com.prettyjson.android.util.HtmlExporter
import com.prettyjson.android.util.QrCodeGenerator
import com.prettyjson.android.util.JsonBucketInserter
import com.prettyjson.android.util.TypedValueConverter
import com.prettyjson.android.util.CursorPositionInserter
import com.prettyjson.android.util.HapticFeedback
import kotlinx.coroutines.launch
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.net.URL
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import org.json.JSONObject
import org.json.JSONArray
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.draw.alpha
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween

/**
 * Main screen for JSON formatting and validation
 * Redesigned to match codebeautify.org/jsonviewer style
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainScreen(
    onNavigate: (String) -> Unit,
    viewModel: MainViewModel = koinViewModel(),
    initialJsonContent: String? = null,
    onShowRewardedAd: ((onRewardEarned: () -> Unit, onAdFailed: (String) -> Unit) -> Unit)? = null
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
    
    val settingsViewModel: com.prettyjson.android.ui.viewmodel.SettingsViewModel = org.koin.androidx.compose.koinViewModel()
    val fontFamily by settingsViewModel.fontFamily.collectAsState(initial = "jetbrains")
    val textSize by settingsViewModel.textSize.collectAsState(initial = 14)
    val formatOnPaste by settingsViewModel.formatOnPaste.collectAsState(initial = false)
    val lineWrapping by settingsViewModel.lineWrapping.collectAsState(initial = false)
    
    val dataBucketViewModel: com.prettyjson.android.ui.viewmodel.DataBucketViewModel = org.koin.androidx.compose.koinViewModel()
    val premiumViewModel: com.prettyjson.android.ui.viewmodel.PremiumViewModel = org.koin.androidx.compose.koinViewModel()
    val isPremium by premiumViewModel.isPremium.collectAsState()
    val dataBuckets by dataBucketViewModel.allDataBuckets.collectAsState()
    var showPremiumDialog by remember { mutableStateOf(false) }
    
    // Initialize example buckets on first launch (only for premium users)
    LaunchedEffect(isPremium) {
        if (isPremium) {
            dataBucketViewModel.initializeExampleBuckets()
        }
    }
    
    val context = LocalContext.current
    val view = LocalView.current
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
    var selectedTab by remember { mutableStateOf(0) } // 0 = Input, 1 = Output, 2 = Form Editor
    var isFullScreen by remember { mutableStateOf(false) }
    var fullScreenTab by remember { mutableStateOf<Int?>(null) } // null = not full screen, 0 = input, 1 = output, 2 = form editor
    var showBottomSheet by remember { mutableStateOf(false) }
    var scrollToLine by remember { mutableStateOf<Int?>(null) }
    var cursorPosition by remember { mutableStateOf(0) }
    var currentSearchIndex by remember { mutableStateOf(0) }
    var searchMatches by remember { mutableStateOf<List<IntRange>>(emptyList()) }
    val bottomSheetState = rememberModalBottomSheetState(
        skipPartiallyExpanded = true
    )
    
    // Hide system bars for full screen mode
    LaunchedEffect(fullScreenTab) {
        val window = (context as? ComponentActivity)?.window
        window?.let {
            val windowInsetsController = WindowCompat.getInsetsController(it, view)
            if (fullScreenTab != null) {
                // Hide system bars for immersive full screen
                windowInsetsController.hide(WindowInsetsCompat.Type.systemBars())
                windowInsetsController.systemBarsBehavior = WindowInsetsControllerCompat.BEHAVIOR_SHOW_TRANSIENT_BARS_BY_SWIPE
            } else {
                // Show system bars when exiting full screen
                windowInsetsController.show(WindowInsetsCompat.Type.systemBars())
            }
        }
    }
    var showSearchReplace by remember { mutableStateOf(false) }
    var searchText by remember { mutableStateOf("") }
    var replaceText by remember { mutableStateOf("") }
    var showClipboardDialog by remember { mutableStateOf(false) }
    var clipboardJsonContent by remember { mutableStateOf<String?>(null) }
    var showDemoJsonButton by remember { mutableStateOf(jsonInput.isEmpty()) }
    var recentFiles by remember { mutableStateOf<List<com.prettyjson.android.data.database.SavedJson>>(emptyList()) }
    var showRecentFilesDialog by remember { mutableStateOf(false) }
    var showRecentHub by remember { mutableStateOf(false) }
    var showAdLoadingDialog by remember { mutableStateOf(false) }
    var pendingExportAction by remember { mutableStateOf<(() -> Unit)?>(null) }
    var showDataBucketDialog by remember { mutableStateOf(false) }
    var showQrCodeDialog by remember { mutableStateOf(false) }
    var qrCodeBitmap by remember { mutableStateOf<android.graphics.Bitmap?>(null) }
    var showClearConfirmationDialog by remember { mutableStateOf(false) }
    var fabExpanded by remember { mutableStateOf(false) }
    
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
    
    // Show initial load dialog on first launch (only once)
    var hasShownInitialDialog by remember { mutableStateOf(false) }
    LaunchedEffect(Unit) {
        if (jsonInput.isEmpty() && !hasShownInitialDialog) {
            scope.launch {
                // Check for recent files, clipboard, or show options
                recentFiles = viewModel.getRecentFiles(10)
                
                // Check clipboard
                val clip = clipboardManager.primaryClip
                clip?.getItemAt(0)?.text?.toString()?.let { clipboardText ->
                    val trimmed = clipboardText.trim()
                    if (trimmed.startsWith("{") || trimmed.startsWith("[")) {
                        val validation = com.prettyjson.android.util.JsonFormatter.validate(trimmed)
                        if (validation.isValid || trimmed.length > 10) {
                            clipboardJsonContent = trimmed
                        }
                    }
                }
                
                // Show dialog with all options if JSON is empty
                if (jsonInput.isEmpty()) {
                    if (recentFiles.isNotEmpty() || clipboardJsonContent != null) {
                        showRecentFilesDialog = true
                    }
                }
                
                hasShownInitialDialog = true
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
        modifier = Modifier
            .fillMaxSize()
            .statusBarsPadding()
            .navigationBarsPadding(),
        topBar = {
            if (fullScreenTab == null) {
                // Minimal header like the website
                Surface(
                    modifier = Modifier
                        .fillMaxWidth(),
                    color = MaterialTheme.colorScheme.surface,
                    shadowElevation = 4.dp,
                    tonalElevation = 2.dp,
                    shape = RoundedCornerShape(0.dp)
                ) {
                    Column {
                    // Title bar - compact
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 12.dp, vertical = 6.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            "JSON Viewer & Editor",
                            style = MaterialTheme.typography.titleMedium.copy(
                                fontWeight = FontWeight.Bold
                            )
                        )
                        
                        // Menu button
                        IconButton(
                            onClick = { showBottomSheet = true },
                            modifier = Modifier.size(36.dp)
                        ) {
                            Icon(
                                Icons.Default.Menu,
                                contentDescription = "Menu",
                                modifier = Modifier.size(20.dp)
                            )
                        }
                    }
                    
                    // Horizontal action chips bar with bottom sheet for advanced options
                    if (showToolbar && fullScreenTab == null) {
                        Surface(
                            modifier = Modifier.fillMaxWidth(),
                            color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f)
                        ) {
                            Column(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(horizontal = 8.dp, vertical = 4.dp)
                            ) {
                                // Action chips in 2 lines (wrapping layout)
                                Column(
                                    modifier = Modifier.fillMaxWidth(),
                                    verticalArrangement = Arrangement.spacedBy(6.dp)
                                ) {
                                    // Header with most efficient options
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.spacedBy(6.dp),
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        // Validation icon - shows JSON validity status
                                        isValid?.let {
                                            AnimatedValidationIcon(
                                                isValid = it,
                                                modifier = Modifier.size(20.dp)
                                            )
                                        }
                                        
                                        // Most common actions - icon only for compact design
                                        IconButton(
                                            onClick = { viewModel.undo() },
                                            enabled = canUndo,
                                            modifier = Modifier.size(36.dp)
                                        ) {
                                            Icon(
                                                Icons.Default.ArrowBack,
                                                contentDescription = "Undo",
                                                modifier = Modifier.size(20.dp)
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
                                                modifier = Modifier.size(20.dp)
                                            )
                                        }
                                        
                                        IconButton(
                                            onClick = { showSearchReplace = true },
                                            modifier = Modifier.size(36.dp)
                                        ) {
                                            Icon(
                                                Icons.Default.Search,
                                                contentDescription = "Search",
                                                modifier = Modifier.size(20.dp)
                                            )
                                        }
                                        
                                        // Copy button (works on input or output based on tab)
                                        if ((selectedTab == 0 && jsonInput.isNotEmpty()) || (selectedTab == 1 && jsonOutput.isNotEmpty())) {
                                            IconButton(
                                                onClick = {
                                                    val textToCopy = if (selectedTab == 0) jsonInput else jsonOutput
                                                    if (textToCopy.isNotEmpty()) {
                                                        val clip = ClipData.newPlainText("JSON", textToCopy)
                                                        clipboardManager.setPrimaryClip(clip)
                                                        scope.launch {
                                                            snackbarHostState.showSnackbar(
                                                                message = "Copied to clipboard",
                                                                duration = SnackbarDuration.Short
                                                            )
                                                        }
                                                    }
                                                },
                                                modifier = Modifier.size(36.dp)
                                            ) {
                                                Icon(
                                                    Icons.Default.ContentCopy,
                                                    contentDescription = "Copy",
                                                    modifier = Modifier.size(20.dp)
                                                )
                                            }
                                        }
                                        
                                        // Clear/Reset button - clear all JSON input
                                        if (jsonInput.isNotEmpty()) {
                                            IconButton(
                                                onClick = { showClearConfirmationDialog = true },
                                                modifier = Modifier.size(36.dp)
                                            ) {
                                                Icon(
                                                    Icons.Default.Delete,
                                                    contentDescription = "Clear",
                                                    modifier = Modifier.size(20.dp)
                                                )
                                            }
                                        }
                                        
                                        // Data Bucket button - insert data buckets into JSON (Premium only)
                                        IconButton(
                                            onClick = { 
                                                if (isPremium) {
                                                    showDataBucketDialog = true
                                                } else {
                                                    showPremiumDialog = true
                                                }
                                            },
                                            modifier = Modifier.size(36.dp)
                                        ) {
                                            Icon(
                                                Icons.Default.Storage,
                                                contentDescription = "Insert Data Bucket",
                                                modifier = Modifier.size(20.dp),
                                                tint = if (isPremium) MaterialTheme.colorScheme.onSurface else MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f)
                                            )
                                        }
                                        
                                        // Full Screen button - visible for all tabs (Input, Output, Form)
                                        IconButton(
                                            onClick = {
                                                fullScreenTab = selectedTab
                                                isFullScreen = true
                                            },
                                            modifier = Modifier.size(36.dp)
                                        ) {
                                            Icon(
                                                Icons.Default.Fullscreen,
                                                contentDescription = "Full Screen",
                                                modifier = Modifier.size(20.dp)
                                            )
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
                }
            } else {
                // Hide top bar in full screen - return empty Box
                Box {}
            }
        },
        bottomBar = {
            if (fullScreenTab == null && !isPremium) {
                Surface(
                    color = MaterialTheme.colorScheme.surface
                ) {
                    BannerAdView(
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            }
            // Hide bottom bar in full screen or for premium users (empty composable)
        },
        snackbarHost = {
            SnackbarHost(hostState = snackbarHostState)
        },
        floatingActionButton = {
            // Expandable FAB - only show when not in fullscreen
            if (fullScreenTab == null) {
                ExpandableFAB(
                    expanded = fabExpanded,
                    onExpandedChange = { fabExpanded = it },
                    onFormat = { 
                        try {
                            HapticFeedback.light(context)
                        } catch (e: Exception) {
                            // Ignore haptic feedback errors
                        }
                        try {
                            viewModel.formatJson()
                            scope.launch {
                                snackbarHostState.showSnackbar(
                                    message = "JSON formatted",
                                    duration = SnackbarDuration.Short
                                )
                            }
                        } catch (e: Exception) {
                            scope.launch {
                                snackbarHostState.showSnackbar(
                                    message = "Error formatting JSON: ${e.message}",
                                    duration = SnackbarDuration.Long
                                )
                            }
                        }
                    },
                    onValidate = {
                        try {
                            HapticFeedback.light(context)
                        } catch (e: Exception) {
                            // Ignore haptic feedback errors
                        }
                        try {
                            viewModel.validateJson()
                            scope.launch {
                                snackbarHostState.showSnackbar(
                                    message = if (isValid == true) "JSON is valid ✓" else "JSON is invalid ✗",
                                    duration = SnackbarDuration.Short
                                )
                            }
                        } catch (e: Exception) {
                            scope.launch {
                                snackbarHostState.showSnackbar(
                                    message = "Error validating JSON: ${e.message}",
                                    duration = SnackbarDuration.Long
                                )
                            }
                        }
                    },
                    onShare = {
                        try {
                            HapticFeedback.medium(context)
                        } catch (e: Exception) {
                            // Ignore haptic feedback errors
                        }
                        try {
                            if (jsonOutput.isNotEmpty()) {
                                val sendIntent = Intent().apply {
                                    action = Intent.ACTION_SEND
                                    putExtra(Intent.EXTRA_TEXT, jsonOutput)
                                    type = "text/plain"
                                }
                                context.startActivity(Intent.createChooser(sendIntent, "Share JSON"))
                            } else {
                                scope.launch {
                                    snackbarHostState.showSnackbar(
                                        message = "No JSON to share",
                                        duration = SnackbarDuration.Short
                                    )
                                }
                            }
                        } catch (e: Exception) {
                            scope.launch {
                                snackbarHostState.showSnackbar(
                                    message = "Error sharing JSON: ${e.message}",
                                    duration = SnackbarDuration.Long
                                )
                            }
                        }
                    },
                    onSave = {
                        try {
                            HapticFeedback.medium(context)
                        } catch (e: Exception) {
                            // Ignore haptic feedback errors
                        }
                        try {
                            showSaveDialog = true
                        } catch (e: Exception) {
                            scope.launch {
                                snackbarHostState.showSnackbar(
                                    message = "Error opening save dialog: ${e.message}",
                                    duration = SnackbarDuration.Long
                                )
                            }
                        }
                    },
                    onMore = {
                        try {
                            showBottomSheet = true
                            fabExpanded = false
                        } catch (e: Exception) {
                            scope.launch {
                                snackbarHostState.showSnackbar(
                                    message = "Error opening menu: ${e.message}",
                                    duration = SnackbarDuration.Long
                                )
                            }
                        }
                    },
                    enabled = jsonInput.isNotEmpty() || jsonOutput.isNotEmpty()
                )
            }
        }
    ) { paddingValues ->
        // Full screen mode for both input and output - truly immersive (hides all UI including system bars)
        if (fullScreenTab != null && isFullScreen) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(MaterialTheme.colorScheme.surface)
            ) {
                when (fullScreenTab) {
                    0 -> {
                        // Input full screen
                        LineNumberTextField(
                            value = jsonInput,
                            onValueChange = { viewModel.setJsonInput(it, addToHistory = true) },
                            modifier = Modifier.fillMaxSize(),
                            placeholder = "Enter or paste JSON here...",
                            errorLine = errorLocation?.line,
                            fontFamily = fontFamily,
                            textSize = textSize,
                            onCursorPositionChange = { position -> cursorPosition = position },
                            searchTerm = searchText,
                            scrollToLine = scrollToLine
                        )
                    }
                    1 -> {
                        // Output full screen
                        if (viewMode == "tree") {
                            JsonTreeView(
                                jsonString = jsonOutput,
                                onReorder = { reorderedJson ->
                                    viewModel.reorderJson(reorderedJson)
                                },
                                onPathCopied = { path ->
                                    scope.launch {
                                        snackbarHostState.showSnackbar(
                                            message = "Path copied: $path",
                                            duration = SnackbarDuration.Short
                                        )
                                    }
                                },
                                modifier = Modifier.fillMaxSize()
                            )
                        } else {
                            LineNumberTextField(
                                value = jsonOutput,
                                onValueChange = {},
                                modifier = Modifier.fillMaxSize(),
                                readOnly = true,
                                searchTerm = searchText,
                                fontFamily = fontFamily,
                                textSize = textSize,
                                enableFolding = true
                            )
                        }
                    }
                    2 -> {
                        // Form Editor full screen
                        if (jsonInput.isNotEmpty() && isValid == true) {
                            val jsonObj = remember(jsonInput) {
                                try {
                                    if (isValid == true) {
                                        JSONObject(jsonInput)
                                    } else {
                                        null
                                    }
                                } catch (e: Exception) {
                                    null
                                }
                            }
                            
                            val keys = remember(jsonObj, jsonInput) {
                                jsonObj?.let {
                                    val keysList = mutableListOf<String>()
                                    it.keys().forEach { keysList.add(it) }
                                    keysList
                                } ?: emptyList()
                            }
                            
                            Column(
                                modifier = Modifier
                                    .fillMaxSize()
                                    .padding(horizontal = 16.dp)
                                    .padding(top = 64.dp, bottom = 16.dp)
                                    .verticalScroll(rememberScrollState()),
                                verticalArrangement = Arrangement.spacedBy(12.dp)
                            ) {
                                // Header with Insert Data Bucket button
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text(
                                        "Edit JSON Fields",
                                        style = MaterialTheme.typography.titleLarge,
                                        fontWeight = FontWeight.Bold
                                    )
                                    Button(
                                        onClick = {
                                            if (isPremium) {
                                                showDataBucketDialog = true
                                            } else {
                                                showPremiumDialog = true
                                            }
                                        },
                                        modifier = Modifier.height(40.dp)
                                    ) {
                                        Icon(
                                            Icons.Default.Storage,
                                            contentDescription = null,
                                            modifier = Modifier.size(20.dp)
                                        )
                                        Spacer(modifier = Modifier.width(8.dp))
                                        Text("Insert Bucket")
                                    }
                                }

                                // Quick add - single button
                                var showAddDialog by remember { mutableStateOf(false) }
                                var addType by remember { mutableStateOf("string") }
                                var keyInput by remember { mutableStateOf("") }
                                var showTypeDropdown by remember { mutableStateOf(false) }
                                fun uniqueKey(desired: String, obj: JSONObject): String {
                                    var key = desired.ifEmpty { "key" }
                                    var counter = 1
                                    while (obj.has(key)) {
                                        key = "${desired}_${counter}"
                                        counter++
                                    }
                                    return key
                                }
                                val doUpdateJsonWithKey: (String, String) -> Unit = { type, key ->
                                    try {
                                        val obj = JSONObject(jsonInput.takeIf { it.isNotEmpty() } ?: "{}")
                                        val finalKey = uniqueKey(key, obj)
                                        when (type) {
                                            "string" -> obj.put(finalKey, "")
                                            "number" -> obj.put(finalKey, 0)
                                            "true" -> obj.put(finalKey, true)
                                            "false" -> obj.put(finalKey, false)
                                            "null" -> obj.put(finalKey, JSONObject.NULL)
                                            "object" -> obj.put(finalKey, JSONObject())
                                            "array" -> obj.put(finalKey, JSONArray())
                                        }
                                        viewModel.setJsonInput(obj.toString(2), addToHistory = true)
                                    } catch (_: Exception) {}
                                }
                                OutlinedButton(
                                    onClick = {
                                        if (isPremium) {
                                            keyInput = ""
                                            showAddDialog = true
                                        } else {
                                            showPremiumDialog = true
                                        }
                                    },
                                    modifier = Modifier.padding(vertical = 4.dp),
                                    enabled = isPremium || true // Always enabled for viewing, but action requires Pro
                                ) {
                                    Icon(Icons.Default.Add, null, modifier = Modifier.size(18.dp))
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text("Add Field")
                                }
                                if (showAddDialog) {
                                    AlertDialog(
                                        onDismissRequest = { showAddDialog = false },
                                        title = { Text("Add Field") },
                                        text = {
                                            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                                                // Type selector
                                                Box {
                                                    OutlinedButton(
                                                        onClick = { showTypeDropdown = true },
                                                        modifier = Modifier.fillMaxWidth()
                                                    ) {
                                                        Text("Type: ${addType.replaceFirstChar { it.uppercase() }}")
                                                        Icon(Icons.Default.ArrowDropDown, null)
                                                    }
                                                    DropdownMenu(
                                                        expanded = showTypeDropdown,
                                                        onDismissRequest = { showTypeDropdown = false }
                                                    ) {
                                                        listOf("string", "number", "true", "false", "null", "object", "array").forEach { type ->
                                                            DropdownMenuItem(
                                                                text = { Text(type.replaceFirstChar { it.uppercase() }) },
                                                                onClick = {
                                                                    addType = type
                                                                    showTypeDropdown = false
                                                                }
                                                            )
                                                        }
                                                    }
                                                }
                                                // Key name input
                                                OutlinedTextField(
                                                    value = keyInput,
                                                    onValueChange = { keyInput = it.filter { ch -> ch != ' ' } },
                                                    label = { Text("Key name") },
                                                    singleLine = true,
                                                    modifier = Modifier.fillMaxWidth()
                                                )
                                                Text(
                                                    "Existing keys will auto-suffix to remain unique",
                                                    style = MaterialTheme.typography.bodySmall,
                                                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                                                )
                                            }
                                        },
                                        confirmButton = {
                                            TextButton(onClick = {
                                                val desired = keyInput.ifEmpty { addType }
                                                doUpdateJsonWithKey(addType, desired)
                                                showAddDialog = false
                                            }) { Text("Add") }
                                        },
                                        dismissButton = {
                                            TextButton(onClick = { showAddDialog = false }) { Text("Cancel") }
                                        }
                                    )
                                }

                                if (jsonObj != null) {
                                    keys.forEach { key ->
                                        // Read current value from JSON object
                                        val currentValue = remember(key, jsonObj) {
                                            try {
                                                jsonObj.getString(key)
                                            } catch (e: Exception) {
                                                try {
                                                    jsonObj.get(key).toString()
                                                } catch (e2: Exception) {
                                                    ""
                                                }
                                            }
                                        }
                                        
                                        var fieldValue by remember(key) { mutableStateOf(currentValue) }
                                        
                                        // Sync with JSON when it changes externally
                                        LaunchedEffect(jsonInput) {
                                            if (isValid == true) {
                                                try {
                                                    val updatedJsonObj = JSONObject(jsonInput)
                                                    val newValue = try {
                                                        updatedJsonObj.getString(key)
                                                    } catch (e: Exception) {
                                                        try {
                                                            updatedJsonObj.get(key).toString()
                                                        } catch (e2: Exception) {
                                                            ""
                                                        }
                                                    }
                                                    if (fieldValue != newValue) {
                                                        fieldValue = newValue
                                                    }
                                                } catch (e: Exception) {
                                                    // Ignore errors
                                                }
                                            }
                                        }
                                        
                                        OutlinedTextField(
                                            value = fieldValue,
                                            onValueChange = { newValue ->
                                                if (isPremium) {
                                                    fieldValue = newValue
                                                    // Update JSON object and notify viewModel
                                                    try {
                                                        val updatedJson = JSONObject(jsonInput)
                                                        updatedJson.put(key, newValue)
                                                        val formattedJson = updatedJson.toString(2)
                                                        viewModel.setJsonInput(formattedJson, addToHistory = true)
                                                    } catch (e: Exception) {
                                                        // Invalid update, keep field value but don't update JSON
                                                    }
                                                } else {
                                                    showPremiumDialog = true
                                                }
                                            },
                                            label = { Text("$key") },
                                            modifier = Modifier.fillMaxWidth(),
                                            singleLine = true,
                                            readOnly = !isPremium,
                                            colors = if (!isPremium) {
                                                OutlinedTextFieldDefaults.colors(
                                                    disabledTextColor = MaterialTheme.colorScheme.onSurface,
                                                    disabledBorderColor = MaterialTheme.colorScheme.outline.copy(alpha = 0.5f),
                                                    disabledLabelColor = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f)
                                                )
                                            } else {
                                                OutlinedTextFieldDefaults.colors()
                                            }
                                        )
                                    }
                                }
                            }
                        } else {
                            Box(
                                modifier = Modifier.fillMaxSize(),
                                contentAlignment = Alignment.Center
                            ) {
                                Column(
                                    horizontalAlignment = Alignment.CenterHorizontally,
                                    verticalArrangement = Arrangement.spacedBy(8.dp),
                                    modifier = Modifier.padding(16.dp)
                                ) {
                                    Icon(
                                        Icons.Default.Info,
                                        contentDescription = null,
                                        modifier = Modifier.size(48.dp),
                                        tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f)
                                    )
                                    Text(
                                        if (jsonInput.isEmpty()) 
                                            "Enter valid JSON in Input tab to use Form Editor"
                                        else 
                                            "JSON must be valid to use Form Editor",
                                        style = MaterialTheme.typography.bodyMedium,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f),
                                        textAlign = androidx.compose.ui.text.style.TextAlign.Center
                                    )
                                }
                            }
                        }
                    }
                }
                
                // Exit full screen button (top-right corner) - compact and neutral
                IconButton(
                    onClick = { 
                        fullScreenTab = null
                        isFullScreen = false
                    },
                    modifier = Modifier
                        .align(Alignment.TopEnd)
                        .padding(12.dp)
                        .size(40.dp),
                    colors = IconButtonDefaults.iconButtonColors(
                        containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.9f),
                        contentColor = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                ) {
                    Icon(
                        Icons.Default.Close,
                        contentDescription = "Exit Full Screen",
                        modifier = Modifier.size(24.dp)
                    )
                }
                
                // View mode toggle (only for output full screen - bottom-right corner)
                if (fullScreenTab == 1) {
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
            }
        } else {
            // Main content area - tabs for Input/Output
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues)
            ) {
            // Tabs for Input/Output/Form Editor
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
                            Text("Input")
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
                            Text("Output")
                        }
                    }
                )
                
                Tab(
                    selected = selectedTab == 2,
                    onClick = { selectedTab = 2 },
                    enabled = true, // Always enabled for viewing, but actions require Pro
                    text = { 
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            Icon(
                                Icons.Default.EditNote,
                                contentDescription = null,
                                modifier = Modifier.size(18.dp)
                            )
                            Text("Form")
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
                                            clip?.getItemAt(0)?.text?.toString()?.let { content ->
                                                if (formatOnPaste) {
                                                    // Try to format the pasted JSON
                                                    val formatResult = com.prettyjson.android.util.JsonFormatter.format(content, tabSpaces)
                                                    if (formatResult.success) {
                                                        viewModel.setJsonInput(formatResult.content, addToHistory = true)
                                                    } else {
                                                        // If formatting fails, paste as-is
                                                        viewModel.setJsonInput(content, addToHistory = true)
                                                    }
                                                } else {
                                                    viewModel.setJsonInput(content, addToHistory = true)
                                                }
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
                                                    scrollToLine = location.line
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
                                        
                                        // Auto-fix trailing commas button
                                        if (errorMessage != null && errorMessage!!.contains("trailing comma", ignoreCase = true)) {
                                            Spacer(modifier = Modifier.height(8.dp))
                                            TextButton(
                                                onClick = {
                                                    val fixed = com.prettyjson.android.util.JsonAutoFix.fixTrailingCommas(jsonInput)
                                                    viewModel.setJsonInput(fixed, addToHistory = true)
                                                    scope.launch {
                                                        snackbarHostState.showSnackbar(
                                                            message = "Trailing commas fixed",
                                                            duration = SnackbarDuration.Short
                                                        )
                                                    }
                                                },
                                                modifier = Modifier.fillMaxWidth()
                                            ) {
                                                Icon(
                                                    Icons.Default.AutoFixHigh,
                                                    contentDescription = null,
                                                    modifier = Modifier.size(16.dp)
                                                )
                                                Spacer(modifier = Modifier.width(4.dp))
                                                Text("Auto-fix Trailing Commas")
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
                                    .weight(1f),
                                searchTerm = searchText,
                                placeholder = "Enter or paste JSON here...",
                                errorLine = errorLocation?.line,
                                fontFamily = fontFamily,
                                textSize = textSize,
                                onCursorPositionChange = { position -> cursorPosition = position },
                                scrollToLine = scrollToLine,
                                lineWrapping = lineWrapping
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
                                            },
                                            onPathCopied = { path ->
                                                scope.launch {
                                                    snackbarHostState.showSnackbar(
                                                        message = "Path copied: $path",
                                                        duration = SnackbarDuration.Short
                                                    )
                                                }
                                            }
                                        )
                                    }
                                } else {
                                    LineNumberTextField(
                                        value = jsonOutput,
                                        onValueChange = {},
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .weight(1f),
                                        searchTerm = searchText,
                                        readOnly = true,
                                        fontFamily = fontFamily,
                                        textSize = textSize,
                                        enableFolding = true
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
                        2 -> {
                            // Form Editor tab - edit JSON with fields
                            if (jsonInput.isNotEmpty() && isValid == true) {
                                // Parse JSON object
                                val jsonObj = remember(jsonInput, isValid) {
                                    try {
                                        if (isValid == true) {
                                            JSONObject(jsonInput)
                                        } else {
                                            null
                                        }
                                    } catch (e: Exception) {
                                        null
                                    }
                                }
                                
                                val keys = remember(jsonObj) {
                                    jsonObj?.let {
                                        val keysList = mutableListOf<String>()
                                        it.keys().forEach { key -> keysList.add(key) }
                                        keysList
                                    } ?: emptyList()
                                }
                                
                                Column(
                                    modifier = Modifier
                                        .fillMaxSize()
                                        .padding(12.dp)
                                        .verticalScroll(rememberScrollState()),
                                    verticalArrangement = Arrangement.spacedBy(12.dp)
                                ) {
                                    // Header with Insert Data Bucket button
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Text(
                                            "Edit JSON Fields",
                                            style = MaterialTheme.typography.titleMedium,
                                            fontWeight = FontWeight.Bold
                                        )
                                        Button(
                                            onClick = {
                                                if (isPremium) {
                                                    showDataBucketDialog = true
                                                } else {
                                                    showPremiumDialog = true
                                                }
                                            },
                                            modifier = Modifier.height(36.dp)
                                        ) {
                                            Icon(
                                                Icons.Default.Storage,
                                                contentDescription = null,
                                                modifier = Modifier.size(18.dp)
                                            )
                                            Spacer(modifier = Modifier.width(4.dp))
                                            Text("Insert Bucket", style = MaterialTheme.typography.labelSmall)
                                        }
                                    }

                                    // Quick add - single button
                                    var showFsAddDialog by remember { mutableStateOf(false) }
                                    var fsAddType by remember { mutableStateOf("string") }
                                    var fsKeyInput by remember { mutableStateOf("") }
                                    var showFsTypeDropdown by remember { mutableStateOf(false) }
                                    fun fsUniqueKey(desired: String, obj: JSONObject): String {
                                        var key = desired.ifEmpty { "key" }
                                        var counter = 1
                                        while (obj.has(key)) {
                                            key = "${desired}_${counter}"
                                            counter++
                                        }
                                        return key
                                    }
                                    val doFsUpdateJsonWithKey: (String, String) -> Unit = { type, key ->
                                        try {
                                            val obj = JSONObject(jsonInput.takeIf { it.isNotEmpty() } ?: "{}")
                                            val finalKey = fsUniqueKey(key, obj)
                                            when (type) {
                                                "string" -> obj.put(finalKey, "")
                                                "number" -> obj.put(finalKey, 0)
                                                "true" -> obj.put(finalKey, true)
                                                "false" -> obj.put(finalKey, false)
                                                "null" -> obj.put(finalKey, JSONObject.NULL)
                                                "object" -> obj.put(finalKey, JSONObject())
                                                "array" -> obj.put(finalKey, JSONArray())
                                            }
                                            viewModel.setJsonInput(obj.toString(2), addToHistory = true)
                                        } catch (_: Exception) {}
                                    }
                                    OutlinedButton(
                                        onClick = {
                                            if (isPremium) {
                                                fsKeyInput = ""
                                                showFsAddDialog = true
                                            } else {
                                                showPremiumDialog = true
                                            }
                                        },
                                        modifier = Modifier.padding(vertical = 4.dp),
                                        enabled = isPremium || true // Always enabled for viewing, but action requires Pro
                                    ) {
                                        Icon(Icons.Default.Add, null, modifier = Modifier.size(18.dp))
                                        Spacer(modifier = Modifier.width(4.dp))
                                        Text("Add Field")
                                    }
                                    if (showFsAddDialog) {
                                        AlertDialog(
                                            onDismissRequest = { showFsAddDialog = false },
                                            title = { Text("Add Field") },
                                            text = {
                                                Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                                                    // Type selector
                                                    Box {
                                                        OutlinedButton(
                                                            onClick = { showFsTypeDropdown = true },
                                                            modifier = Modifier.fillMaxWidth()
                                                        ) {
                                                            Text("Type: ${fsAddType.replaceFirstChar { it.uppercase() }}")
                                                            Icon(Icons.Default.ArrowDropDown, null)
                                                        }
                                                        DropdownMenu(
                                                            expanded = showFsTypeDropdown,
                                                            onDismissRequest = { showFsTypeDropdown = false }
                                                        ) {
                                                            listOf("string", "number", "true", "false", "null", "object", "array").forEach { type ->
                                                                DropdownMenuItem(
                                                                    text = { Text(type.replaceFirstChar { it.uppercase() }) },
                                                                    onClick = {
                                                                        fsAddType = type
                                                                        showFsTypeDropdown = false
                                                                    }
                                                                )
                                                            }
                                                        }
                                                    }
                                                    // Key name input
                                                    OutlinedTextField(
                                                        value = fsKeyInput,
                                                        onValueChange = { fsKeyInput = it.filter { ch -> ch != ' ' } },
                                                        label = { Text("Key name") },
                                                        singleLine = true,
                                                        modifier = Modifier.fillMaxWidth()
                                                    )
                                                    Text(
                                                        "Existing keys will auto-suffix to remain unique",
                                                        style = MaterialTheme.typography.bodySmall,
                                                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                                                    )
                                                }
                                            },
                                            confirmButton = {
                                                TextButton(onClick = {
                                                    val desired = fsKeyInput.ifEmpty { fsAddType }
                                                    doFsUpdateJsonWithKey(fsAddType, desired)
                                                    showFsAddDialog = false
                                                }) { Text("Add") }
                                            },
                                            dismissButton = {
                                                TextButton(onClick = { showFsAddDialog = false }) { Text("Cancel") }
                                            }
                                        )
                                    }

                                    if (jsonObj != null && keys.isNotEmpty()) {
                                        keys.forEach { key ->
                                            // Get current value from JSON - updated on each recomposition
                                            val currentValue = remember(key, jsonInput) {
                                                try {
                                                    val obj = JSONObject(jsonInput)
                                                    try {
                                                        obj.getString(key)
                                                    } catch (e: Exception) {
                                                        try {
                                                            obj.get(key).toString()
                                                        } catch (e2: Exception) {
                                                            ""
                                                        }
                                                    }
                                                } catch (e: Exception) {
                                                    ""
                                                }
                                            }
                                            
                                            var fieldValue by remember(key) { mutableStateOf(currentValue) }
                                            
                                            // Sync field value when JSON changes externally
                                            LaunchedEffect(jsonInput, key) {
                                                val newValue = try {
                                                    val obj = JSONObject(jsonInput)
                                                    try {
                                                        obj.getString(key)
                                                    } catch (e: Exception) {
                                                        try {
                                                            obj.get(key).toString()
                                                        } catch (e2: Exception) {
                                                            ""
                                                        }
                                                    }
                                                } catch (e: Exception) {
                                                    ""
                                                }
                                                if (fieldValue != newValue) {
                                                    fieldValue = newValue
                                                }
                                            }
                                            
                                            Row(
                                                modifier = Modifier.fillMaxWidth(),
                                                horizontalArrangement = Arrangement.spacedBy(8.dp),
                                                verticalAlignment = Alignment.CenterVertically
                                            ) {
                                                OutlinedTextField(
                                                    value = fieldValue,
                                                    onValueChange = { newValue ->
                                                        if (isPremium) {
                                                            fieldValue = newValue
                                                            // Update JSON object and notify viewModel
                                                            try {
                                                                val updatedJson = JSONObject(jsonInput)
                                                                updatedJson.put(key, newValue)
                                                                val formattedJson = updatedJson.toString(2)
                                                                viewModel.setJsonInput(formattedJson, addToHistory = true)
                                                            } catch (e: Exception) {
                                                                // Invalid update, keep field value but don't update JSON
                                                            }
                                                        } else {
                                                            showPremiumDialog = true
                                                        }
                                                    },
                                                    label = { Text("$key") },
                                                    modifier = Modifier.weight(1f),
                                                    singleLine = true,
                                                    readOnly = !isPremium,
                                                    colors = if (!isPremium) {
                                                        OutlinedTextFieldDefaults.colors(
                                                            disabledTextColor = MaterialTheme.colorScheme.onSurface,
                                                            disabledBorderColor = MaterialTheme.colorScheme.outline.copy(alpha = 0.5f),
                                                            disabledLabelColor = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f)
                                                        )
                                                    } else {
                                                        OutlinedTextFieldDefaults.colors()
                                                    }
                                                )
                                                IconButton(
                                                    onClick = {
                                                        if (isPremium) {
                                                            // Remove field from JSON
                                                            try {
                                                                val updatedJson = JSONObject(jsonInput)
                                                                updatedJson.remove(key)
                                                                val formattedJson = updatedJson.toString(2)
                                                                viewModel.setJsonInput(formattedJson, addToHistory = true)
                                                                scope.launch {
                                                                    snackbarHostState.showSnackbar(
                                                                        message = "Field '$key' removed",
                                                                        duration = SnackbarDuration.Short
                                                                    )
                                                                }
                                                            } catch (e: Exception) {
                                                                scope.launch {
                                                                    snackbarHostState.showSnackbar(
                                                                        message = "Failed to remove field: ${e.message}",
                                                                        duration = SnackbarDuration.Short
                                                                    )
                                                                }
                                                            }
                                                        } else {
                                                            showPremiumDialog = true
                                                        }
                                                    },
                                                    enabled = isPremium || true // Always enabled for viewing, but action requires Pro
                                                ) {
                                                    Icon(
                                                        Icons.Default.Delete,
                                                        contentDescription = "Delete field",
                                                        tint = if (isPremium) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f),
                                                        modifier = Modifier.size(20.dp)
                                                    )
                                                }
                                            }
                                        }
                                    } else if (jsonObj != null && keys.isEmpty()) {
                                        Box(
                                            modifier = Modifier.fillMaxWidth(),
                                            contentAlignment = Alignment.Center
                                        ) {
                                            Text(
                                                "JSON object has no fields to edit",
                                                style = MaterialTheme.typography.bodyMedium,
                                                color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f)
                                            )
                                        }
                                    }
                                }
                            } else {
                                Box(
                                    modifier = Modifier.fillMaxSize(),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Column(
                                        horizontalAlignment = Alignment.CenterHorizontally,
                                        verticalArrangement = Arrangement.spacedBy(8.dp),
                                        modifier = Modifier.padding(16.dp)
                                    ) {
                                        Icon(
                                            Icons.Default.Info,
                                            contentDescription = null,
                                            modifier = Modifier.size(48.dp),
                                            tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f)
                                        )
                                        Text(
                                            if (jsonInput.isEmpty()) 
                                                "Enter valid JSON in Input tab to use Form Editor"
                                            else 
                                                "JSON must be valid to use Form Editor",
                                            style = MaterialTheme.typography.bodyMedium,
                                            color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f),
                                            textAlign = androidx.compose.ui.text.style.TextAlign.Center
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
        
        // Calculate occurrence count and find all matches in real time
        val (occurrenceCount, matches) = remember(searchText, jsonInput) {
            if (searchText.isEmpty() || jsonInput.isEmpty()) {
                Pair(0, emptyList<IntRange>())
            } else {
                val regex = Regex(searchText, RegexOption.IGNORE_CASE)
                val allMatches = regex.findAll(jsonInput).map { it.range }.toList()
                Pair(allMatches.size, allMatches)
            }
        }
        
        // Update search matches when dialog opens
        LaunchedEffect(showSearchReplace) {
            if (showSearchReplace && searchText.isNotEmpty()) {
                val regex = Regex(searchText, RegexOption.IGNORE_CASE)
                searchMatches = regex.findAll(jsonInput).map { it.range }.toList()
                currentSearchIndex = 0
            }
        }
        
        // Update matches when search text changes
        LaunchedEffect(searchText) {
            if (searchText.isNotEmpty()) {
                val regex = Regex(searchText, RegexOption.IGNORE_CASE)
                searchMatches = regex.findAll(jsonInput).map { it.range }.toList()
                currentSearchIndex = 0
            } else {
                searchMatches = emptyList()
                currentSearchIndex = 0
            }
        }
        
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
                    
                    // Real-time occurrence count
                    if (searchText.isNotEmpty() && occurrenceCount > 0) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.Start,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                Icons.Default.Info,
                                contentDescription = null,
                                modifier = Modifier.size(16.dp),
                                tint = MaterialTheme.colorScheme.primary
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                "$occurrenceCount occurrence${if (occurrenceCount != 1) "s" else ""} found",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.primary
                            )
                        }
                    }
                    
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
                Column(
                    horizontalAlignment = Alignment.End,
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    // Navigation buttons
                    if (searchMatches.isNotEmpty()) {
                Row(
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                            IconButton(
                        onClick = {
                                    if (currentSearchIndex > 0) {
                                        currentSearchIndex--
                                } else {
                                        currentSearchIndex = searchMatches.size - 1
                                    }
                                    val match = searchMatches[currentSearchIndex]
                                    // Calculate line number from position
                                    val lineNumber = jsonInput.substring(0, match.first).count { it == '\n' } + 1
                                    scrollToLine = lineNumber
                                },
                                enabled = searchMatches.isNotEmpty()
                            ) {
                                Icon(Icons.Default.ArrowUpward, contentDescription = "Previous")
                            }
                            
                            Text(
                                "${currentSearchIndex + 1} / ${searchMatches.size}",
                                style = MaterialTheme.typography.bodySmall
                            )
                            
                            IconButton(
                                onClick = {
                                    if (currentSearchIndex < searchMatches.size - 1) {
                                        currentSearchIndex++
                                    } else {
                                        currentSearchIndex = 0
                                    }
                                    val match = searchMatches[currentSearchIndex]
                                    // Calculate line number from position
                                    val lineNumber = jsonInput.substring(0, match.first).count { it == '\n' } + 1
                                    scrollToLine = lineNumber
                                },
                                enabled = searchMatches.isNotEmpty()
                            ) {
                                Icon(Icons.Default.ArrowDownward, contentDescription = "Next")
                            }
                        }
                    }
                    
                    // Action buttons
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        TextButton(
                            onClick = {
                                if (searchText.isNotEmpty() && searchMatches.isNotEmpty()) {
                                    val match = searchMatches[currentSearchIndex]
                                    // Calculate line number from position
                                    val lineNumber = jsonInput.substring(0, match.first).count { it == '\n' } + 1
                                    scrollToLine = lineNumber
                                }
                            },
                            enabled = searchText.isNotEmpty() && searchMatches.isNotEmpty()
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
                                // Close dialog after replace
                                showSearchReplace = false
                                searchText = ""
                                replaceText = ""
                                scope.launch {
                                    snackbarHostState.showSnackbar(
                                        message = if (replaceAll) "All occurrences replaced" else "First occurrence replaced",
                                        duration = SnackbarDuration.Short
                                    )
                                }
                            }
                        },
                        enabled = searchText.isNotEmpty()
                    ) {
                        Text("Replace")
                        }
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
                    clipboardJsonContent?.let { content ->
                        if (formatOnPaste) {
                            // Try to format the pasted JSON
                            val formatResult = com.prettyjson.android.util.JsonFormatter.format(content, tabSpaces)
                            if (formatResult.success) {
                                viewModel.setJsonInput(formatResult.content)
                            } else {
                                // If formatting fails, paste as-is
                                viewModel.setJsonInput(content)
                            }
                        } else {
                            viewModel.setJsonInput(content)
                        }
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
    
    // Recent Files Dialog - Enhanced with all loading options
    if (showRecentFilesDialog) {
        AlertDialog(
            onDismissRequest = { showRecentFilesDialog = false },
            title = { 
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Icon(Icons.Default.Folder, contentDescription = null)
                    Text("Load JSON")
                }
            },
            text = {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .heightIn(max = 500.dp)
                        .verticalScroll(rememberScrollState()),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Text(
                        "Choose how you want to load JSON:",
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.SemiBold,
                        modifier = Modifier.padding(bottom = 4.dp)
                    )
                    
                    // Clipboard option
                    if (clipboardJsonContent != null) {
                        Card(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable {
                                    clipboardJsonContent?.let {
                                        viewModel.setJsonInput(it, addToHistory = true)
                                    }
                                    showRecentFilesDialog = false
                                    clipboardJsonContent = null
                                },
                            colors = CardDefaults.cardColors(
                                containerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.3f)
                            )
                        ) {
                            Row(
                                modifier = Modifier.padding(12.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(12.dp)
                            ) {
                                Icon(
                                    Icons.Default.ContentPaste,
                                    contentDescription = null,
                                    tint = MaterialTheme.colorScheme.primary
                                )
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(
                                        "Paste from Clipboard",
                                        style = MaterialTheme.typography.titleSmall,
                                        fontWeight = FontWeight.SemiBold
                                    )
                                    Text(
                                        "JSON detected in clipboard (${clipboardJsonContent?.length ?: 0} chars)",
                                        style = MaterialTheme.typography.bodySmall,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                            }
                        }
                    }
                    
                    // Load from URL option
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable {
                                showRecentFilesDialog = false
                                showUrlDialog = true
                            },
                        colors = CardDefaults.cardColors(
                            containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f)
                        )
                    ) {
                        Row(
                            modifier = Modifier.padding(12.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            Icon(Icons.Default.Link, contentDescription = null)
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    "Load from URL",
                                    style = MaterialTheme.typography.titleSmall,
                                    fontWeight = FontWeight.SemiBold
                                )
                                Text(
                                    "Fetch JSON from a web URL",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }
                    }
                    
                    // Open File option
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable {
                                filePickerLauncher.launch("*/*")
                                showRecentFilesDialog = false
                            },
                        colors = CardDefaults.cardColors(
                            containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f)
                        )
                    ) {
                        Row(
                            modifier = Modifier.padding(12.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            Icon(Icons.Default.Folder, contentDescription = null)
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    "Open File",
                                    style = MaterialTheme.typography.titleSmall,
                                    fontWeight = FontWeight.SemiBold
                                )
                                Text(
                                    "Select a JSON file from device",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }
                    }
                    
                    // Recent Files section
                    if (recentFiles.isNotEmpty()) {
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            "Recent Files:",
                            style = MaterialTheme.typography.titleSmall,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.padding(vertical = 4.dp)
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
                                            com.prettyjson.android.util.DateFormatter.formatDateTime(savedJson.updatedAt),
                                            style = MaterialTheme.typography.bodySmall,
                                            color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f)
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            },
            confirmButton = {
                TextButton(onClick = { 
                    viewModel.clear()
                    showRecentFilesDialog = false 
                }) {
                    Text("Start Fresh")
                }
            },
            dismissButton = {
                TextButton(onClick = { 
                    showRecentFilesDialog = false
                }) {
                    Text("Cancel")
                }
            }
        )
    }
    
    // Clear Confirmation Dialog
    if (showClearConfirmationDialog) {
        AlertDialog(
            onDismissRequest = { showClearConfirmationDialog = false },
            title = { Text("Clear JSON") },
            text = { Text("Are you sure you want to clear all JSON input? This action cannot be undone.") },
            confirmButton = {
                TextButton(
                    onClick = {
                        viewModel.clear()
                        showClearConfirmationDialog = false
                        scope.launch {
                            snackbarHostState.showSnackbar(
                                message = "JSON cleared",
                                duration = SnackbarDuration.Short
                            )
                        }
                    }
                ) {
                    Text("Clear")
                }
            },
            dismissButton = {
                TextButton(onClick = { showClearConfirmationDialog = false }) {
                    Text("Cancel")
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
    
    // QR Code Dialog
    if (showQrCodeDialog) {
        AlertDialog(
            onDismissRequest = { 
                showQrCodeDialog = false
                qrCodeBitmap = null
            },
            title = { 
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Icon(Icons.Default.QrCode2, contentDescription = null)
                    Text("QR Code")
                }
            },
            text = {
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    qrCodeBitmap?.let { bitmap ->
                        Image(
                            bitmap = bitmap.asImageBitmap(),
                            contentDescription = "QR Code",
                            modifier = Modifier
                                .size(300.dp)
                                .background(
                                    Color.White,
                                    RoundedCornerShape(8.dp)
                                )
                                .padding(16.dp)
                        )
                    } ?: run {
                        Box(
                            modifier = Modifier
                                .size(300.dp)
                                .background(
                                    MaterialTheme.colorScheme.surfaceVariant,
                                    RoundedCornerShape(8.dp)
                                ),
                            contentAlignment = Alignment.Center
                        ) {
                            CircularProgressIndicator()
                        }
                    }
                    
                    Text(
                        "Scan this QR code to get the JSON data",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        textAlign = androidx.compose.ui.text.style.TextAlign.Center
                    )
                }
            },
            confirmButton = {
                Row(
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    TextButton(
                        onClick = {
                            qrCodeBitmap?.let { bitmap ->
                                // Share QR code as image
                                scope.launch {
                                    withContext(Dispatchers.IO) {
                                        try {
                                            val cacheDir = context.cacheDir
                                            val qrCodeFile = java.io.File(cacheDir, "qr_code_${System.currentTimeMillis()}.png")
                                            java.io.FileOutputStream(qrCodeFile).use { out ->
                                                bitmap.compress(android.graphics.Bitmap.CompressFormat.PNG, 100, out)
                                            }
                                            withContext(Dispatchers.Main) {
                                                val shareIntent = Intent().apply {
                                                    action = Intent.ACTION_SEND
                                                    type = "image/png"
                                                    putExtra(Intent.EXTRA_STREAM, androidx.core.content.FileProvider.getUriForFile(
                                                        context,
                                                        "${context.packageName}.fileprovider",
                                                        qrCodeFile
                                                    ))
                                                    addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
                                                }
                                                context.startActivity(Intent.createChooser(shareIntent, "Share QR Code"))
                                            }
                                        } catch (e: Exception) {
                                            withContext(Dispatchers.Main) {
                                                snackbarHostState.showSnackbar(
                                                    message = "Failed to share QR code: ${e.message}",
                                                    duration = SnackbarDuration.Long
                                                )
                                            }
                                        }
                                    }
                                }
                            }
                        },
                        enabled = qrCodeBitmap != null
                    ) {
                        Icon(Icons.Default.Share, contentDescription = null, modifier = Modifier.size(18.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Share")
                    }
                    TextButton(
                        onClick = {
                            showQrCodeDialog = false
                            qrCodeBitmap = null
                        }
                    ) {
                        Text("Close")
                    }
                }
            }
        )
    }
    
    // Bottom Sheet for Advanced Options
    if (showBottomSheet) {
        LaunchedEffect(showBottomSheet) {
            if (showBottomSheet) {
                bottomSheetState.expand()
            }
        }
        ModalBottomSheet(
            onDismissRequest = { 
                scope.launch {
                    bottomSheetState.hide()
                    showBottomSheet = false
                }
            },
            sheetState = bottomSheetState,
            dragHandle = {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 12.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Surface(
                        modifier = Modifier
                            .width(40.dp)
                            .height(4.dp),
                        shape = RoundedCornerShape(2.dp),
                        color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.4f)
                    ) {}
                }
            }
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .fillMaxHeight(0.9f)
                    .verticalScroll(rememberScrollState())
                    .padding(16.dp)
                    .padding(bottom = 32.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Icon(
                        Icons.Default.Menu,
                        contentDescription = null,
                        modifier = Modifier.size(24.dp)
                    )
                    Text(
                        "Menu - All Features",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold
                    )
                }
                
                Divider()
                
                // Settings section - moved to top for immediate visibility
                Text(
                    "⚙️ Preferences",
                    style = MaterialTheme.typography.titleMedium,
                    modifier = Modifier.padding(top = 8.dp)
                )
                ListItem(
                    headlineContent = { Text("Settings") },
                    leadingContent = { Icon(Icons.Default.Settings, null) },
                    modifier = Modifier.clickable {
                        onNavigate(NavigationRoutes.SETTINGS)
                        showBottomSheet = false
                    }
                )
                ListItem(
                    headlineContent = { 
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text("Data Buckets")
                            if (!isPremium) {
                                Text(
                                    "PRO",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = MaterialTheme.colorScheme.primary,
                                    fontWeight = FontWeight.Bold,
                                    modifier = Modifier
                                        .background(
                                            MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.5f),
                                            RoundedCornerShape(4.dp)
                                        )
                                        .padding(horizontal = 6.dp, vertical = 2.dp)
                                )
                            }
                        }
                    },
                    leadingContent = { Icon(Icons.Default.Storage, null) },
                    modifier = Modifier.clickable {
                        if (isPremium) {
                            onNavigate(NavigationRoutes.DATA_BUCKETS)
                            showBottomSheet = false
                        } else {
                            showBottomSheet = false
                            showPremiumDialog = true
                        }
                    }
                )
                
                Divider(modifier = Modifier.padding(vertical = 8.dp))
                
                // Load section
                Text(
                    "📋 Load",
                    style = MaterialTheme.typography.titleMedium,
                    modifier = Modifier.padding(top = 8.dp)
                )
                ListItem(
                    headlineContent = { Text("Paste from Clipboard") },
                    leadingContent = { Icon(Icons.Default.ContentPaste, null) },
                    modifier = Modifier.clickable {
                        val clip = clipboardManager.primaryClip
                        if (clip != null && clip.itemCount > 0) {
                            val text = clip.getItemAt(0).text.toString()
                            if (text.isNotEmpty()) {
                                viewModel.setJsonInput(text, addToHistory = true)
                            }
                        }
                        showBottomSheet = false
                    }
                )
                ListItem(
                    headlineContent = { Text("Open File") },
                    leadingContent = { Icon(Icons.Default.Folder, null) },
                    modifier = Modifier.clickable {
                        filePickerLauncher.launch("*/*")
                        showBottomSheet = false
                    }
                )
                ListItem(
                    headlineContent = { Text("Load from URL") },
                    leadingContent = { Icon(Icons.Default.Link, null) },
                    modifier = Modifier.clickable {
                        showUrlDialog = true
                        showBottomSheet = false
                    }
                )
                ListItem(
                    headlineContent = { Text("Recent Files") },
                    leadingContent = { Icon(Icons.Default.History, null) },
                    modifier = Modifier.clickable {
                        showRecentFilesDialog = true
                        scope.launch { recentFiles = viewModel.getRecentFiles(10) }
                        showBottomSheet = false
                    }
                )
                
                Divider(modifier = Modifier.padding(vertical = 8.dp))
                
                // Format section
                if (jsonInput.isNotEmpty()) {
                    Text(
                        "✨ Format",
                        style = MaterialTheme.typography.titleMedium
                    )
                    var showSortMenu by remember { mutableStateOf(false) }
                    ListItem(
                        headlineContent = { Text("Minify") },
                        leadingContent = { Icon(Icons.Default.ExpandLess, null) },
                        modifier = Modifier.clickable {
                            viewModel.minifyJson()
                            showBottomSheet = false
                        }
                    )
                    Box {
                        ListItem(
                            headlineContent = { Text("Sort Keys") },
                            leadingContent = { Icon(Icons.Default.FilterList, null) },
                            modifier = Modifier.clickable { showSortMenu = true }
                        )
                        DropdownMenu(
                            expanded = showSortMenu,
                            onDismissRequest = { showSortMenu = false }
                        ) {
                            listOf(
                                "Sort: By Key (ASC)" to { viewModel.sortKeys(com.prettyjson.android.util.JsonFormatter.SortOrder.ASC, com.prettyjson.android.util.JsonFormatter.SortBy.KEY) },
                                "Sort: By Key (DESC)" to { viewModel.sortKeys(com.prettyjson.android.util.JsonFormatter.SortOrder.DESC, com.prettyjson.android.util.JsonFormatter.SortBy.KEY) },
                                "Sort: By Type (ASC)" to { viewModel.sortKeys(com.prettyjson.android.util.JsonFormatter.SortOrder.ASC, com.prettyjson.android.util.JsonFormatter.SortBy.TYPE) },
                                "Sort: By Type (DESC)" to { viewModel.sortKeys(com.prettyjson.android.util.JsonFormatter.SortOrder.DESC, com.prettyjson.android.util.JsonFormatter.SortBy.TYPE) },
                                "Sort: By Value (ASC)" to { viewModel.sortKeys(com.prettyjson.android.util.JsonFormatter.SortOrder.ASC, com.prettyjson.android.util.JsonFormatter.SortBy.VALUE) },
                                "Sort: By Value (DESC)" to { viewModel.sortKeys(com.prettyjson.android.util.JsonFormatter.SortOrder.DESC, com.prettyjson.android.util.JsonFormatter.SortBy.VALUE) }
                            ).forEach { (label, action) ->
                                DropdownMenuItem(
                                    text = { Text(label) },
                                    onClick = {
                                        action()
                                        showSortMenu = false
                                        showBottomSheet = false
                                    }
                                )
                            }
                        }
                    }
                }
                
                Divider(modifier = Modifier.padding(vertical = 8.dp))
                
                // Export section
                if (jsonOutput.isNotEmpty()) {
                    Text(
                        "📤 Export",
                        style = MaterialTheme.typography.titleMedium
                    )
                    ListItem(
                        headlineContent = { Text("Export as PDF") },
                        leadingContent = { Icon(Icons.Default.PictureAsPdf, null) },
                        modifier = Modifier.clickable {
                            showBottomSheet = false
                            pendingExportAction = {
                                PdfExporter.exportToPdf(context, jsonOutput, "json_${SimpleDateFormat("yyyyMMdd_HHmmss", java.util.Locale.getDefault()).format(java.util.Date())}")
                                scope.launch {
                                    snackbarHostState.showSnackbar(
                                        message = "Opening PDF print dialog...",
                                        duration = SnackbarDuration.Short
                                    )
                                }
                            }
                            if (isPremium) {
                                // Skip ad for premium users
                                pendingExportAction?.invoke()
                                pendingExportAction = null
                            } else {
                                onShowRewardedAd?.invoke(
                                    { // onRewardEarned
                                        pendingExportAction?.invoke()
                                        pendingExportAction = null
                                    },
                                    { error -> // onAdFailed
                                        pendingExportAction = null
                                        scope.launch {
                                            snackbarHostState.showSnackbar(
                                                message = error,
                                                duration = SnackbarDuration.Long
                                            )
                                        }
                                    }
                                ) ?: run {
                                    pendingExportAction?.invoke()
                                    pendingExportAction = null
                                }
                            }
                        }
                    )
                    ListItem(
                        headlineContent = { Text("Export as HTML") },
                        leadingContent = { Icon(Icons.Default.Code, null) },
                        modifier = Modifier.clickable {
                            showBottomSheet = false
                            pendingExportAction = {
                                scope.launch {
                                    withContext(Dispatchers.IO) {
                                        val fileName = "json_${SimpleDateFormat("yyyyMMdd_HHmmss", java.util.Locale.getDefault()).format(java.util.Date())}"
                                        val result = HtmlExporter.exportToHtml(context, jsonOutput, fileName)
                                        withContext(Dispatchers.Main) {
                                            result.onSuccess { file ->
                                                snackbarHostState.showSnackbar(
                                                    message = "HTML exported to: ${file.name}",
                                                    duration = SnackbarDuration.Short
                                                )
                                            }.onFailure { error ->
                                                snackbarHostState.showSnackbar(
                                                    message = "Failed to export HTML: ${error.message}",
                                                    duration = SnackbarDuration.Long
                                                )
                                            }
                                        }
                                    }
                                }
                            }
                            if (isPremium) {
                                pendingExportAction?.invoke()
                                pendingExportAction = null
                            } else {
                                onShowRewardedAd?.invoke(
                                    {
                                        pendingExportAction?.invoke()
                                        pendingExportAction = null
                                    },
                                    { error ->
                                        pendingExportAction = null
                                        scope.launch {
                                            snackbarHostState.showSnackbar(
                                                message = error,
                                                duration = SnackbarDuration.Long
                                            )
                                        }
                                    }
                                ) ?: run {
                                    pendingExportAction?.invoke()
                                    pendingExportAction = null
                                }
                            }
                        }
                    )
                    ListItem(
                        headlineContent = { Text("Save as JSON File") },
                        leadingContent = { Icon(Icons.Default.Description, null) },
                        modifier = Modifier.clickable {
                            showBottomSheet = false
                            pendingExportAction = {
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
                            }
                            onShowRewardedAd?.invoke(
                                { // onRewardEarned
                                    pendingExportAction?.invoke()
                                    pendingExportAction = null
                                },
                                { error -> // onAdFailed
                                    pendingExportAction = null
                                    scope.launch {
                                        snackbarHostState.showSnackbar(
                                            message = error,
                                            duration = SnackbarDuration.Long
                                        )
                                    }
                                }
                            ) ?: run {
                                pendingExportAction?.invoke()
                                pendingExportAction = null
                            }
                        }
                    )
                    ListItem(
                        headlineContent = { Text("Export as TXT File") },
                        leadingContent = { Icon(Icons.Default.TextSnippet, null) },
                        modifier = Modifier.clickable {
                            showBottomSheet = false
                            pendingExportAction = {
                                scope.launch {
                                    withContext(Dispatchers.IO) {
                                        val fileName = "json_${SimpleDateFormat("yyyyMMdd_HHmmss", java.util.Locale.getDefault()).format(java.util.Date())}"
                                        val result = PdfExporter.exportToTxtFile(context, jsonOutput, fileName)
                                        withContext(Dispatchers.Main) {
                                            result.onSuccess { file ->
                                                val snackbarResult = snackbarHostState.showSnackbar(
                                                    message = "TXT file exported successfully",
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
                            }
                            if (isPremium) {
                                // Skip ad for premium users
                                pendingExportAction?.invoke()
                                pendingExportAction = null
                            } else {
                                onShowRewardedAd?.invoke(
                                    { // onRewardEarned
                                        pendingExportAction?.invoke()
                                        pendingExportAction = null
                                    },
                                    { error -> // onAdFailed
                                        pendingExportAction = null
                                        scope.launch {
                                            snackbarHostState.showSnackbar(
                                                message = error,
                                                duration = SnackbarDuration.Long
                                            )
                                        }
                                    }
                                ) ?: run {
                                    pendingExportAction?.invoke()
                                    pendingExportAction = null
                                }
                            }
                        }
                    )
                    ListItem(
                        headlineContent = { Text("Share as Text") },
                        leadingContent = { Icon(Icons.Default.Share, null) },
                        modifier = Modifier.clickable {
                            showBottomSheet = false
                            pendingExportAction = {
                                val sendIntent = Intent().apply {
                                    action = Intent.ACTION_SEND
                                    putExtra(Intent.EXTRA_TEXT, jsonOutput)
                                    type = "text/plain"
                                }
                                context.startActivity(Intent.createChooser(sendIntent, "Share JSON"))
                            }
                            if (isPremium) {
                                // Skip ad for premium users
                                pendingExportAction?.invoke()
                                pendingExportAction = null
                            } else {
                                onShowRewardedAd?.invoke(
                                    { // onRewardEarned
                                        pendingExportAction?.invoke()
                                        pendingExportAction = null
                                    },
                                    { error -> // onAdFailed
                                        pendingExportAction = null
                                        scope.launch {
                                            snackbarHostState.showSnackbar(
                                                message = error,
                                                duration = SnackbarDuration.Long
                                            )
                                        }
                                    }
                                ) ?: run {
                                    pendingExportAction?.invoke()
                                    pendingExportAction = null
                                }
                            }
                        }
                    )
                    ListItem(
                        headlineContent = { Text("Share as QR Code") },
                        leadingContent = { Icon(Icons.Default.QrCode2, null) },
                        modifier = Modifier.clickable {
                            showBottomSheet = false
                            pendingExportAction = {
                                // Check if text is too long for QR code
                                if (QrCodeGenerator.isTextTooLong(jsonOutput)) {
                                    scope.launch {
                                        snackbarHostState.showSnackbar(
                                            message = "JSON is too long for QR code (max 2000 characters)",
                                            duration = SnackbarDuration.Long
                                        )
                                    }
                                } else {
                                    // Generate QR code on background thread
                                    scope.launch {
                                        val bitmap = withContext(Dispatchers.IO) {
                                            QrCodeGenerator.generateQrCode(jsonOutput, 512)
                                        }
                                        if (bitmap != null) {
                                            qrCodeBitmap = bitmap
                                            showQrCodeDialog = true
                                        } else {
                                            snackbarHostState.showSnackbar(
                                                message = "Failed to generate QR code",
                                                duration = SnackbarDuration.Short
                                            )
                                        }
                                    }
                                }
                            }
                            if (isPremium) {
                                pendingExportAction?.invoke()
                                pendingExportAction = null
                            } else {
                                onShowRewardedAd?.invoke(
                                    {
                                        pendingExportAction?.invoke()
                                        pendingExportAction = null
                                    },
                                    { error ->
                                        pendingExportAction = null
                                        scope.launch {
                                            snackbarHostState.showSnackbar(
                                                message = error,
                                                duration = SnackbarDuration.Long
                                            )
                                        }
                                    }
                                ) ?: run {
                                    pendingExportAction?.invoke()
                                    pendingExportAction = null
                                }
                            }
                        }
                    )
                }
                
                Divider(modifier = Modifier.padding(vertical = 8.dp))
                
                // Feedback section
                Text(
                    "💬 Feedback",
                    style = MaterialTheme.typography.titleMedium,
                    modifier = Modifier.padding(top = 8.dp)
                )
                ListItem(
                    headlineContent = { Text("Rate & Review on Play Store") },
                    leadingContent = { Icon(Icons.Default.Star, null) },
                    supportingContent = { Text("We'd love to hear your feedback and suggestions for improvement") },
                    modifier = Modifier.clickable {
                        try {
                            val intent = Intent(Intent.ACTION_VIEW).apply {
                                data = android.net.Uri.parse("market://details?id=${context.packageName}")
                                setPackage("com.android.vending")
                            }
                            context.startActivity(intent)
                        } catch (e: Exception) {
                            // Fallback to web browser if Play Store app is not available
                            try {
                                val intent = Intent(Intent.ACTION_VIEW).apply {
                                    data = android.net.Uri.parse("https://play.google.com/store/apps/details?id=${context.packageName}")
                                }
                                context.startActivity(intent)
                            } catch (e2: Exception) {
                                scope.launch {
                                    snackbarHostState.showSnackbar(
                                        message = "Unable to open Play Store",
                                        duration = SnackbarDuration.Short
                                    )
                                }
                            }
                        }
                        showBottomSheet = false
                    }
                )
                
                Divider(modifier = Modifier.padding(vertical = 8.dp))
                
                // Help section - link to Tips screen instead of inline content
                Text(
                    "📚 Help",
                    style = MaterialTheme.typography.titleMedium,
                    modifier = Modifier.padding(top = 8.dp)
                )
                ListItem(
                    headlineContent = { Text("Tips & JSON Guide") },
                    leadingContent = { Icon(Icons.Default.Info, null) },
                    supportingContent = { Text("Open tips in a dedicated screen") },
                    modifier = Modifier.clickable {
                        onNavigate(NavigationRoutes.HELP)
                        showBottomSheet = false
                    }
                )
            }
        }
    }
    
    // Premium Upgrade Dialog
    if (showPremiumDialog) {
        AlertDialog(
            onDismissRequest = { showPremiumDialog = false },
            title = { 
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Icon(Icons.Default.Star, null, tint = MaterialTheme.colorScheme.primary)
                    Text("Upgrade to Pro")
                }
            },
            text = {
                Column(
                    modifier = Modifier.padding(vertical = 8.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Text(
                        "Unlock Pro features:",
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.Bold
                    )
                    Text("✨ Data Buckets - Create reusable JSON snippets")
                    Text("🚫 Ad-free experience")
                    Text("📱 Early access to new features")
                    
                    Spacer(modifier = Modifier.height(8.dp))
                    
                    Card(
                        colors = CardDefaults.cardColors(
                            containerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.3f)
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
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        showPremiumDialog = false
                        onNavigate(NavigationRoutes.SETTINGS)
                    }
                ) {
                    Text("Upgrade Now")
                }
            },
            dismissButton = {
                TextButton(onClick = { showPremiumDialog = false }) {
                    Text("Maybe Later")
                }
            }
        )
    }
    
    // Data Bucket Selection Dialog (Premium only)
    if (showDataBucketDialog && isPremium) {
        AlertDialog(
            onDismissRequest = { showDataBucketDialog = false },
            title = { 
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Icon(Icons.Default.Storage, null)
                    Text("Insert Data Bucket")
                }
            },
            text = {
                if (dataBuckets.isEmpty()) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(8.dp),
                        modifier = Modifier.padding(16.dp)
                    ) {
                        Text(
                            "No data buckets available",
                            style = MaterialTheme.typography.bodyMedium
                        )
                        Text(
                            "Go to Settings > Data Buckets to create some",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                        )
                    }
                } else {
                    LazyColumn(
                        modifier = Modifier
                            .fillMaxWidth()
                            .heightIn(max = 400.dp),
                        verticalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        items(dataBuckets) { dataBucket: DataBucket ->
                            Card(
                                onClick = {
                                    // Insert bucket at cursor position
                                    val currentJson = jsonInput
                                    val keyName = dataBucket.keyName
                                    
                                    // Convert value to proper type
                                    val typedValue = try {
                                        TypedValueConverter.convertValue(dataBucket.value, dataBucket.valueType)
                                    } catch (e: Exception) {
                                        dataBucket.value // Fallback to string
                                    }
                                    
                                    // Insert at cursor position - use JsonBucketInserter for safe merging
                                    val mergedJson = try {
                                        if (currentJson.trim().isEmpty()) {
                                            // Empty JSON - create new object with the bucket
                                            JSONObject().apply {
                                                put(keyName, typedValue)
                                            }.toString(2)
                                        } else {
                                            // Try to parse current JSON as object
                                            try {
                                                val existingObj = JSONObject(currentJson.trim())
                                                // Handle key conflicts
                                                var finalKeyName = keyName
                                                var counter = 1
                                                while (existingObj.has(finalKeyName)) {
                                                    finalKeyName = "${keyName}_$counter"
                                                    counter++
                                                }
                                                existingObj.put(finalKeyName, typedValue)
                                                existingObj.toString(2)
                                            } catch (e: Exception) {
                                                // If not an object, wrap it and add the bucket
                                                try {
                                                    // Try as array
                                                    val arr = JSONArray(currentJson.trim())
                                                    val newObj = JSONObject().apply {
                                                        put("data", arr)
                                                        put(keyName, typedValue)
                                                    }
                                                    newObj.toString(2)
                                                } catch (e2: Exception) {
                                                    // Create new object with original value and bucket
                                                    JSONObject().apply {
                                                        put("value", currentJson.trim())
                                                        put(keyName, typedValue)
                                                    }.toString(2)
                                                }
                                            }
                                        }
                                    } catch (e: Exception) {
                                        // Ultimate fallback - create valid JSON
                                        try {
                                            JSONObject().apply {
                                                put(keyName, typedValue)
                                            }.toString(2)
                                        } catch (e2: Exception) {
                                            // Should never happen, but return minimal valid JSON
                                            "{\"$keyName\":${if (typedValue is String) "\"$typedValue\"" else typedValue}}"
                                        }
                                    }
                                    
                                    // Set the merged JSON and maintain cursor position after insertion
                                    val insertionLength = try {
                                        val beforeCursor = currentJson.substring(0, cursorPosition)
                                        val afterCursor = currentJson.substring(cursorPosition)
                                        val insertion = CursorPositionInserter.insertAtCursor(
                                            jsonText = currentJson,
                                            cursorPosition = cursorPosition,
                                            keyName = keyName,
                                            typedValue = typedValue
                                        )
                                        insertion.length - currentJson.length
                                    } catch (e: Exception) {
                                        mergedJson.length - currentJson.length
                                    }
                                    
                                    viewModel.setJsonInput(mergedJson, addToHistory = true)
                                    
                                    // Update cursor position to after inserted content
                                    cursorPosition = (cursorPosition + insertionLength).coerceIn(0, mergedJson.length)
                                    
                                    showDataBucketDialog = false
                                    
                                    // Show success message
                                    scope.launch {
                                        snackbarHostState.showSnackbar(
                                            message = "Data bucket '$keyName' inserted at cursor",
                                            duration = SnackbarDuration.Short
                                        )
                                    }
                                },
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Column(
                                    modifier = Modifier.padding(12.dp),
                                    verticalArrangement = Arrangement.spacedBy(4.dp)
                                ) {
                                    Row(
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                                    ) {
                                        Text(
                                            dataBucket.keyName,
                                            style = MaterialTheme.typography.titleSmall,
                                            fontWeight = FontWeight.Bold
                                        )
                                        Text(
                                            "(${dataBucket.valueType})",
                                            style = MaterialTheme.typography.bodySmall,
                                            color = MaterialTheme.colorScheme.primary
                                        )
                                    }
                                    if (dataBucket.description.isNotEmpty()) {
                                        Text(
                                            dataBucket.description,
                                            style = MaterialTheme.typography.bodySmall,
                                            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f),
                                            modifier = Modifier.padding(top = 4.dp)
                                        )
                                    }
                                    Text(
                                        dataBucket.value.take(80) + if (dataBucket.value.length > 80) "..." else "",
                                        style = MaterialTheme.typography.bodySmall.copy(
                                            fontFamily = androidx.compose.ui.text.font.FontFamily.Monospace
                                        ),
                                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f),
                                        modifier = Modifier.padding(top = 4.dp)
                                    )
                                }
                            }
                        }
                    }
                }
            },
            confirmButton = {
                TextButton(onClick = { showDataBucketDialog = false }) {
                    Text("Close")
                }
            }
        )
    }
}

// JSON Form Editor Component (unused - kept for reference)
@Composable
fun JsonFormEditorContent(
    jsonString: String,
    onJsonUpdate: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    val jsonObj = remember(jsonString) {
        try {
            JSONObject(jsonString)
        } catch (e: Exception) {
            JSONObject()
        }
    }
    
    val keys = remember(jsonObj) {
        val keysList = mutableListOf<String>()
        jsonObj.keys().forEach { keysList.add(it) }
        keysList
    }
    
    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(12.dp)
            .verticalScroll(rememberScrollState()),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Text(
            "Edit JSON Fields",
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold
        )
        
        keys.forEach { key ->
            var fieldValue by remember(key, jsonString) {
                mutableStateOf(
                    try {
                        jsonObj.getString(key)
                    } catch (e: Exception) {
                        try {
                            jsonObj.get(key).toString()
                        } catch (e2: Exception) {
                            ""
                        }
                    }
                )
            }
            
            OutlinedTextField(
                value = fieldValue,
                onValueChange = { newValue ->
                    fieldValue = newValue
                    try {
                        val updatedJson = JSONObject(jsonObj.toString())
                        updatedJson.put(key, newValue)
                        onJsonUpdate(updatedJson.toString(2))
                    } catch (e: Exception) {
                        // Invalid update, keep old value
                    }
                },
                label = { Text("$key") },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true
            )
        }
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
}

/**
 * Expandable Floating Action Button with common actions
 */
@Composable
fun ExpandableFAB(
    expanded: Boolean,
    onExpandedChange: (Boolean) -> Unit,
    onFormat: () -> Unit,
    onValidate: () -> Unit,
    onShare: () -> Unit,
    onSave: () -> Unit,
    onMore: () -> Unit,
    enabled: Boolean = true
) {
    // Animation for expanding/collapsing
    val rotationAngle by animateFloatAsState(
        targetValue = if (expanded) 45f else 0f,
        animationSpec = tween(durationMillis = 300),
        label = "fab_rotation"
    )
    
    val fabSize = 56.dp
    val miniFabSize = 40.dp
    val spacing = 16.dp
    
    // Use Box to position FAB at bottom end
    Box(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        contentAlignment = Alignment.BottomEnd
    ) {
        // Action buttons (shown when expanded) - positioned above main FAB
        if (expanded) {
            Column(
                modifier = Modifier
                    .align(Alignment.BottomEnd)
                    .padding(bottom = (fabSize.value + spacing.value).dp),
                horizontalAlignment = Alignment.End,
                verticalArrangement = Arrangement.spacedBy(spacing)
            ) {
            // More options button
            FloatingActionButton(
                onClick = {
                    onMore()
                },
                modifier = Modifier.size(miniFabSize),
                containerColor = MaterialTheme.colorScheme.tertiaryContainer,
                contentColor = MaterialTheme.colorScheme.onTertiaryContainer
            ) {
                Icon(
                    Icons.Default.MoreVert,
                    contentDescription = "More options",
                    modifier = Modifier.size(20.dp)
                )
            }
            
            // Save button
            FloatingActionButton(
                onClick = {
                    onSave()
                    onExpandedChange(false)
                },
                modifier = Modifier.size(miniFabSize),
                containerColor = MaterialTheme.colorScheme.secondaryContainer,
                contentColor = MaterialTheme.colorScheme.onSecondaryContainer
            ) {
                Icon(
                    Icons.Default.Save,
                    contentDescription = "Save",
                    modifier = Modifier.size(20.dp)
                )
            }
            
            // Share button
            FloatingActionButton(
                onClick = {
                    onShare()
                    onExpandedChange(false)
                },
                modifier = Modifier.size(miniFabSize),
                containerColor = MaterialTheme.colorScheme.secondaryContainer,
                contentColor = MaterialTheme.colorScheme.onSecondaryContainer
            ) {
                Icon(
                    Icons.Default.Share,
                    contentDescription = "Share",
                    modifier = Modifier.size(20.dp)
                )
            }
            
            // Validate button
            FloatingActionButton(
                onClick = {
                    onValidate()
                    onExpandedChange(false)
                },
                modifier = Modifier.size(miniFabSize),
                containerColor = MaterialTheme.colorScheme.secondaryContainer,
                contentColor = MaterialTheme.colorScheme.onSecondaryContainer
            ) {
                Icon(
                    Icons.Default.CheckCircle,
                    contentDescription = "Validate",
                    modifier = Modifier.size(20.dp)
                )
            }
            
            // Format button
            FloatingActionButton(
                onClick = {
                    onFormat()
                    onExpandedChange(false)
                },
                modifier = Modifier.size(miniFabSize),
                containerColor = MaterialTheme.colorScheme.primaryContainer,
                contentColor = MaterialTheme.colorScheme.onPrimaryContainer
            ) {
                Icon(
                    Icons.Default.FormatAlignLeft,
                    contentDescription = "Format",
                    modifier = Modifier.size(20.dp)
                )
            }
            }
        }
        
        // Main FAB button - positioned at bottom end
        FloatingActionButton(
            onClick = {
                if (enabled) {
                    onExpandedChange(!expanded)
                }
            },
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .size(fabSize)
                .then(if (!enabled) Modifier.alpha(0.5f) else Modifier),
            containerColor = MaterialTheme.colorScheme.primary,
            contentColor = MaterialTheme.colorScheme.onPrimary
        ) {
            Icon(
                Icons.Default.Add,
                contentDescription = if (expanded) "Close menu" else "Open menu",
                modifier = Modifier
                    .size(24.dp)
                    .graphicsLayer {
                        rotationZ = rotationAngle
                    }
        )
    }
    }
}
