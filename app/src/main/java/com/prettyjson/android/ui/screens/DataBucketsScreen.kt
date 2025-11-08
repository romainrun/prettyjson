package com.prettyjson.android.ui.screens

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
import androidx.compose.ui.unit.dp
import org.koin.androidx.compose.koinViewModel
import com.prettyjson.android.data.database.DataBucket
import com.prettyjson.android.ui.viewmodel.DataBucketViewModel

/**
 * Data Buckets screen - manage data buckets for quick insertion
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DataBucketsScreen(
    onNavigateBack: () -> Unit,
    onBucketSelected: (DataBucket) -> Unit,
    viewModel: DataBucketViewModel = koinViewModel()
) {
    val dataBuckets by viewModel.allDataBuckets.collectAsState()
    
    // Initialize example buckets on first launch
    LaunchedEffect(Unit) {
        viewModel.initializeExampleBuckets()
    }
    
    var showAddDialog by remember { mutableStateOf(false) }
    var newBucketKeyName by remember { mutableStateOf("") }
    var newBucketDescription by remember { mutableStateOf("") }
    var newBucketValueType by remember { mutableStateOf("json") }
    var newBucketValue by remember { mutableStateOf("") }
    var editingBucket by remember { mutableStateOf<DataBucket?>(null) }
    
    Scaffold(
        modifier = Modifier
            .fillMaxSize()
            .statusBarsPadding()
            .navigationBarsPadding(),
        topBar = {
            TopAppBar(
                title = { Text("Data Buckets") },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back")
                    }
                }
            )
        },
        floatingActionButton = {
            FloatingActionButton(onClick = { 
                editingBucket = null
                newBucketKeyName = ""
                newBucketDescription = ""
                newBucketValueType = "json"
                newBucketValue = ""
                showAddDialog = true 
            }) {
                Icon(Icons.Default.Add, contentDescription = "Add")
            }
        }
    ) { paddingValues ->
        if (dataBuckets.isEmpty()) {
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
                        Icons.Default.Storage,
                        contentDescription = null,
                        modifier = Modifier.size(64.dp),
                        tint = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                    )
                    Text("No data buckets", style = MaterialTheme.typography.bodyLarge)
                    Text(
                        "Create data buckets with fake data to quickly insert into JSON",
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
                items(dataBuckets) { dataBucket ->
                    DataBucketItem(
                        dataBucket = dataBucket,
                        onSelect = { onBucketSelected(dataBucket) },
                        onEdit = { 
                            editingBucket = dataBucket
                            newBucketKeyName = dataBucket.keyName
                            newBucketDescription = dataBucket.description
                            newBucketValueType = dataBucket.valueType
                            newBucketValue = dataBucket.value
                            showAddDialog = true
                        },
                        onDelete = { viewModel.deleteDataBucket(dataBucket) }
                    )
                }
            }
        }
        
        // Add/Edit Dialog
        if (showAddDialog) {
            AlertDialog(
                onDismissRequest = { 
                    showAddDialog = false
                    editingBucket = null
                },
                title = { Text(if (editingBucket != null) "Edit Data Bucket" else "New Data Bucket") },
                text = {
                    Column(
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        OutlinedTextField(
                            value = newBucketKeyName,
                            onValueChange = { newBucketKeyName = it },
                            label = { Text("Key Name *") },
                            placeholder = { Text("e.g., user, address, price") },
                            singleLine = true,
                            modifier = Modifier.fillMaxWidth()
                        )
                        OutlinedTextField(
                            value = newBucketDescription,
                            onValueChange = { newBucketDescription = it },
                            label = { Text("Description (optional)") },
                            singleLine = true,
                            modifier = Modifier.fillMaxWidth()
                        )
                        // Value Type Dropdown
                        var showValueTypeDropdown by remember { mutableStateOf(false) }
                        val valueTypes = listOf("json", "array", "string", "integer", "float", "boolean", "null")
                        Box(modifier = Modifier.fillMaxWidth()) {
                            OutlinedTextField(
                                value = newBucketValueType,
                                onValueChange = { },
                                readOnly = true,
                                label = { Text("Value Type *") },
                                trailingIcon = {
                                    IconButton(onClick = { showValueTypeDropdown = true }) {
                                        Icon(Icons.Default.ArrowDropDown, null)
                                    }
                                },
                                modifier = Modifier.fillMaxWidth()
                            )
                            DropdownMenu(
                                expanded = showValueTypeDropdown,
                                onDismissRequest = { showValueTypeDropdown = false }
                            ) {
                                valueTypes.forEach { type ->
                                    DropdownMenuItem(
                                        text = { Text(type.capitalize()) },
                                        onClick = {
                                            newBucketValueType = type
                                            showValueTypeDropdown = false
                                        }
                                    )
                                }
                            }
                        }
                        // Value input with hints based on type
                        OutlinedTextField(
                            value = newBucketValue,
                            onValueChange = { newBucketValue = it },
                            label = { 
                                Text(
                                    when (newBucketValueType) {
                                        "json" -> "JSON Value *"
                                        "array" -> "Array Value * (comma-separated or JSON array)"
                                        "string" -> "String Value *"
                                        "integer" -> "Integer Value *"
                                        "float" -> "Float Value *"
                                        "boolean" -> "Boolean Value * (true/false)"
                                        "null" -> "Will be set to null"
                                        else -> "Value *"
                                    }
                                )
                            },
                            placeholder = {
                                Text(
                                    when (newBucketValueType) {
                                        "json" -> "{\"key\": \"value\"}"
                                        "array" -> "[1, 2, 3] or item1, item2, item3"
                                        "string" -> "\"text\" or text"
                                        "integer" -> "123"
                                        "float" -> "123.45"
                                        "boolean" -> "true or false"
                                        "null" -> "(leave empty)"
                                        else -> "Enter value"
                                    }
                                )
                            },
                            maxLines = if (newBucketValueType == "json" || newBucketValueType == "array") 10 else 1,
                            minLines = if (newBucketValueType == "json" || newBucketValueType == "array") 5 else 1,
                            modifier = Modifier.fillMaxWidth(),
                            enabled = newBucketValueType != "null"
                        )
                    }
                },
                confirmButton = {
                    TextButton(
                        onClick = {
                            if (newBucketKeyName.isNotEmpty() && (newBucketValue.isNotEmpty() || newBucketValueType == "null")) {
                                val bucket = if (editingBucket != null) {
                                    editingBucket!!.copy(
                                        keyName = newBucketKeyName,
                                        description = newBucketDescription,
                                        valueType = newBucketValueType,
                                        value = newBucketValue
                                    )
                                } else {
                                    DataBucket(
                                        keyName = newBucketKeyName,
                                        description = newBucketDescription,
                                        valueType = newBucketValueType,
                                        value = newBucketValue
                                    )
                                }
                                viewModel.insertDataBucket(bucket)
                                newBucketKeyName = ""
                                newBucketDescription = ""
                                newBucketValueType = "json"
                                newBucketValue = ""
                                editingBucket = null
                                showAddDialog = false
                            }
                        }
                    ) {
                        Text("Save")
                    }
                },
                dismissButton = {
                    TextButton(onClick = { 
                        showAddDialog = false
                        editingBucket = null
                        newBucketKeyName = ""
                        newBucketDescription = ""
                        newBucketValueType = "json"
                        newBucketValue = ""
                    }) {
                        Text("Cancel")
                    }
                }
            )
        }
    }
}

@Composable
fun DataBucketItem(
    dataBucket: DataBucket,
    onSelect: () -> Unit,
    onEdit: () -> Unit,
    onDelete: () -> Unit
) {
    var showDeleteDialog by remember { mutableStateOf(false) }
    var showMenu by remember { mutableStateOf(false) }
    
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
                    dataBucket.name,
                    style = MaterialTheme.typography.titleMedium
                )
                if (dataBucket.description.isNotEmpty()) {
                    Text(
                        dataBucket.description,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f),
                        modifier = Modifier.padding(top = 4.dp)
                    )
                }
                Text(
                    dataBucket.content.take(60) + if (dataBucket.content.length > 60) "..." else "",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f),
                    modifier = Modifier.padding(top = 4.dp)
                )
            }
            Box {
                IconButton(onClick = { showMenu = true }) {
                    Icon(Icons.Default.MoreVert, contentDescription = "More")
                }
                DropdownMenu(
                    expanded = showMenu,
                    onDismissRequest = { showMenu = false }
                ) {
                    DropdownMenuItem(
                        text = { Text("Edit") },
                        onClick = {
                            showMenu = false
                            onEdit()
                        },
                        leadingIcon = { Icon(Icons.Default.Edit, null) }
                    )
                    DropdownMenuItem(
                        text = { Text("Delete") },
                        onClick = {
                            showMenu = false
                            showDeleteDialog = true
                        },
                        leadingIcon = { Icon(Icons.Default.Delete, null) }
                    )
                }
            }
        }
    }
    
    if (showDeleteDialog) {
        AlertDialog(
            onDismissRequest = { showDeleteDialog = false },
            title = { Text("Delete Data Bucket?") },
            text = { Text("Are you sure you want to delete \"${dataBucket.keyName}\"?") },
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

