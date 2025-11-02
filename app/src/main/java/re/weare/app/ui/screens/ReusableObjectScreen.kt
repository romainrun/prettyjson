package re.weare.app.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import org.koin.androidx.compose.koinViewModel
import re.weare.app.data.database.ReusableObject
import re.weare.app.ui.viewmodel.ReusableObjectViewModel

/**
 * Reusable Objects screen
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ReusableObjectScreen(
    onNavigateBack: () -> Unit,
    onObjectSelected: (ReusableObject) -> Unit,
    viewModel: ReusableObjectViewModel = koinViewModel()
) {
    val reusableObjects by viewModel.reusableObjects.collectAsState(initial = emptyList())
    
    var showAddDialog by remember { mutableStateOf(false) }
    var newObjectName by remember { mutableStateOf("") }
    var newObjectContent by remember { mutableStateOf("") }
    
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Reusable Objects") },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back")
                    }
                }
            )
        },
        floatingActionButton = {
            FloatingActionButton(onClick = { showAddDialog = true }) {
                Icon(Icons.Default.Add, contentDescription = "Add")
            }
        }
    ) { paddingValues ->
        if (reusableObjects.isEmpty()) {
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
                    Text("No reusable objects", style = MaterialTheme.typography.bodyLarge)
                    Text(
                        "Create reusable JSON objects to insert into other JSONs",
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
                items(reusableObjects) { reusableObject ->
                    ReusableObjectItem(
                        reusableObject = reusableObject,
                        onSelect = { onObjectSelected(reusableObject) },
                        onDelete = { viewModel.deleteReusableObject(reusableObject) }
                    )
                }
            }
        }
        
        // Add Dialog
        if (showAddDialog) {
            AlertDialog(
                onDismissRequest = { showAddDialog = false },
                title = { Text("New Reusable Object") },
                text = {
                    Column(
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        OutlinedTextField(
                            value = newObjectName,
                            onValueChange = { newObjectName = it },
                            label = { Text("Name") },
                            singleLine = true,
                            modifier = Modifier.fillMaxWidth()
                        )
                        OutlinedTextField(
                            value = newObjectContent,
                            onValueChange = { newObjectContent = it },
                            label = { Text("JSON Content") },
                            maxLines = 10,
                            minLines = 5,
                            modifier = Modifier.fillMaxWidth()
                        )
                    }
                },
                confirmButton = {
                    TextButton(
                        onClick = {
                            if (newObjectName.isNotEmpty() && newObjectContent.isNotEmpty()) {
                                viewModel.saveReusableObject(
                                    ReusableObject(
                                        name = newObjectName,
                                        content = newObjectContent
                                    )
                                )
                                newObjectName = ""
                                newObjectContent = ""
                                showAddDialog = false
                            }
                        }
                    ) {
                        Text("Save")
                    }
                },
                dismissButton = {
                    TextButton(onClick = { showAddDialog = false }) {
                        Text("Cancel")
                    }
                }
            )
        }
    }
}

@Composable
fun ReusableObjectItem(
    reusableObject: ReusableObject,
    onSelect: () -> Unit,
    onDelete: () -> Unit
) {
    var showDeleteDialog by remember { mutableStateOf(false) }
    
    Card(
        onClick = onSelect,
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    reusableObject.name,
                    style = MaterialTheme.typography.titleMedium
                )
                Text(
                    reusableObject.content.take(50) + if (reusableObject.content.length > 50) "..." else "",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                )
            }
            IconButton(onClick = { showDeleteDialog = true }) {
                Icon(Icons.Default.Delete, contentDescription = "Delete")
            }
        }
    }
    
    if (showDeleteDialog) {
        AlertDialog(
            onDismissRequest = { showDeleteDialog = false },
            title = { Text("Delete Object?") },
            text = { Text("Are you sure you want to delete \"${reusableObject.name}\"?") },
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
}

