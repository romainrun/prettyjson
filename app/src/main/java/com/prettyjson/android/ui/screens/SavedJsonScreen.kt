package com.prettyjson.android.ui.screens

import android.content.Context
import android.content.Intent
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import org.koin.androidx.compose.koinViewModel
import com.prettyjson.android.data.database.SavedJson
import com.prettyjson.android.ui.viewmodel.SavedJsonViewModel
import java.text.SimpleDateFormat
import java.util.*

/**
 * Saved JSONs screen
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SavedJsonScreen(
    onNavigateBack: () -> Unit,
    onJsonSelected: (SavedJson) -> Unit,
    viewModel: SavedJsonViewModel = koinViewModel()
) {
    val savedJsons by viewModel.savedJsons.collectAsState(initial = emptyList())
    val context = LocalContext.current
    var showCreateDialog by remember { mutableStateOf(false) }
    var newJsonName by remember { mutableStateOf("") }
    var newJsonContent by remember { mutableStateOf("") }
    
    Scaffold(
        modifier = Modifier
            .fillMaxSize()
            .statusBarsPadding()
            .navigationBarsPadding(),
        topBar = {
            TopAppBar(
                title = { Text("Saved JSONs") },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back")
                    }
                }
            )
        },
        floatingActionButton = {
            FloatingActionButton(onClick = {
                showCreateDialog = true
            }) {
                Icon(Icons.Default.Add, contentDescription = "Add")
            }
        }
    ) { paddingValues ->
        if (savedJsons.isEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues),
                contentAlignment = Alignment.Center
            ) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Icon(
                        Icons.Default.List,
                        contentDescription = null,
                        modifier = Modifier.size(64.dp),
                        tint = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                    )
                    Text("No saved JSONs", style = MaterialTheme.typography.bodyLarge)
                    Text(
                        "Save JSONs from the main screen",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                    )
                }
            }
        } else {
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues),
                contentPadding = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(savedJsons) { savedJson ->
                    SavedJsonItem(
                        savedJson = savedJson,
                        onSelect = { onJsonSelected(savedJson) },
                        onDelete = { viewModel.deleteJson(savedJson) },
                        onRename = { newName ->
                            viewModel.updateJson(savedJson.copy(name = newName))
                        },
                        onShare = {
                            try {
                                val sendIntent = Intent().apply {
                                    action = Intent.ACTION_SEND
                                    putExtra(Intent.EXTRA_TEXT, savedJson.content)
                                    type = "text/plain"
                                }
                                context.startActivity(Intent.createChooser(sendIntent, "Share JSON"))
                            } catch (e: Exception) {
                                // Handle error silently
                            }
                        }
                    )
                }
            }
        }
    }
    
    if (showCreateDialog) {
        AlertDialog(
            onDismissRequest = { showCreateDialog = false },
            title = { Text("Create New Saved JSON") },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedTextField(
                        value = newJsonName,
                        onValueChange = { newJsonName = it },
                        label = { Text("Name") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )
                    OutlinedTextField(
                        value = newJsonContent,
                        onValueChange = { newJsonContent = it },
                        label = { Text("JSON Content") },
                        modifier = Modifier.fillMaxWidth(),
                        minLines = 5,
                        maxLines = 10
                    )
                }
            },
            confirmButton = {
                TextButton(onClick = {
                    if (newJsonName.isNotEmpty() && newJsonContent.isNotEmpty()) {
                        viewModel.saveJson(newJsonName, newJsonContent)
                        newJsonName = ""
                        newJsonContent = ""
                        showCreateDialog = false
                    }
                }) {
                    Text("Save")
                }
            },
            dismissButton = {
                TextButton(onClick = {
                    showCreateDialog = false
                    newJsonName = ""
                    newJsonContent = ""
                }) {
                    Text("Cancel")
                }
            }
        )
    }
}

@Composable
fun SavedJsonItem(
    savedJson: SavedJson,
    onSelect: () -> Unit,
    onDelete: () -> Unit,
    onRename: (String) -> Unit,
    onShare: () -> Unit
) {
    val context = LocalContext.current
    var showDeleteDialog by remember { mutableStateOf(false) }
    var showRenameDialog by remember { mutableStateOf(false) }
    var renameText by remember { mutableStateOf(savedJson.name) }
    
    val dateFormat = SimpleDateFormat("MMM dd, yyyy HH:mm", Locale.getDefault())
    
    Card(
        onClick = onSelect,
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        savedJson.name,
                        style = MaterialTheme.typography.titleMedium
                    )
                    Text(
                        dateFormat.format(Date(savedJson.updatedAt)),
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                    )
                }
                Row {
                    IconButton(onClick = { showRenameDialog = true }) {
                        Icon(Icons.Default.Edit, contentDescription = "Rename")
                    }
                    IconButton(onClick = {
                        try {
                            val sendIntent = Intent().apply {
                                action = Intent.ACTION_SEND
                                putExtra(Intent.EXTRA_TEXT, savedJson.content)
                                type = "text/plain"
                            }
                            context.startActivity(Intent.createChooser(sendIntent, "Share JSON"))
                        } catch (e: Exception) {
                            // Handle error silently
                        }
                    }) {
                        Icon(Icons.Default.Share, contentDescription = "Share")
                    }
                    IconButton(onClick = { showDeleteDialog = true }) {
                        Icon(Icons.Default.Delete, contentDescription = "Delete")
                    }
                }
            }
            Text(
                savedJson.content.take(100) + if (savedJson.content.length > 100) "..." else "",
                style = MaterialTheme.typography.bodySmall,
                maxLines = 3
            )
        }
    }
    
    if (showDeleteDialog) {
        AlertDialog(
            onDismissRequest = { showDeleteDialog = false },
            title = { Text("Delete JSON?") },
            text = { Text("Are you sure you want to delete \"${savedJson.name}\"?") },
            confirmButton = {
                TextButton(onClick = {
                    onDelete()
                    showDeleteDialog = false
                }) {
                    Text("Delete")
                }
            },
            dismissButton = {
                TextButton(onClick = { showDeleteDialog = false }) {
                    Text("Cancel")
                }
            }
        )
    }
    
    if (showRenameDialog) {
        AlertDialog(
            onDismissRequest = { showRenameDialog = false },
            title = { Text("Rename JSON") },
            text = {
                OutlinedTextField(
                    value = renameText,
                    onValueChange = { renameText = it },
                    label = { Text("Name") },
                    singleLine = true
                )
            },
            confirmButton = {
                TextButton(onClick = {
                    if (renameText.isNotEmpty()) {
                        onRename(renameText)
                        showRenameDialog = false
                    }
                }) {
                    Text("Save")
                }
            },
            dismissButton = {
                TextButton(onClick = { showRenameDialog = false }) {
                    Text("Cancel")
                }
            }
        )
    }
}

