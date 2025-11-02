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
import re.weare.app.ui.viewmodel.JsonBuilderViewModel
import re.weare.app.util.ValueType

/**
 * JSON Builder screen
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun JsonBuilderScreen(
    onNavigateBack: () -> Unit,
    viewModel: JsonBuilderViewModel = koinViewModel()
) {
    val rootNode by viewModel.rootNode.collectAsState()
    val previewJson by viewModel.previewJson.collectAsState()
    
    var showAddDialog by remember { mutableStateOf(false) }
    var newKey by remember { mutableStateOf("") }
    var newValue by remember { mutableStateOf("") }
    var newType by remember { mutableStateOf(ValueType.STRING) }
    
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("JSON Builder") },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back")
                    }
                },
                actions = {
                    IconButton(onClick = { viewModel.clear() }) {
                        Icon(Icons.Default.Clear, contentDescription = "Clear")
                    }
                }
            )
        }
    ) { paddingValues ->
        Row(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            // Builder Panel
            Column(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxHeight()
                    .padding(16.dp)
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                Text("Build JSON", style = MaterialTheme.typography.titleLarge)
                
                // Key-Value Pairs
                rootNode.children.forEach { (key, node) ->
                    if (node is re.weare.app.util.JsonNode.ValueNode) {
                        KeyValuePairItem(
                            key = key,
                            valueNode = node,
                            onUpdate = { newKey, newValue, newType ->
                                viewModel.updateKeyValuePair(key, newKey, newValue, newType)
                            },
                            onDelete = {
                                viewModel.removeKeyValuePair(key)
                            }
                        )
                    }
                }
                
                // Add New Key-Value Pair Button
                Button(
                    onClick = {
                        showAddDialog = true
                    },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Icon(Icons.Default.Add, contentDescription = null)
                    Spacer(Modifier.width(8.dp))
                    Text("Add Key-Value Pair")
                }
            }
            
            // Preview Panel
            Card(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxHeight()
                    .padding(16.dp)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Text("Preview", style = MaterialTheme.typography.titleMedium)
                    OutlinedTextField(
                        value = previewJson,
                        onValueChange = {},
                        modifier = Modifier.fillMaxSize(),
                        readOnly = true,
                        maxLines = 20
                    )
                }
            }
        }
    }
    
    // Add Key-Value Pair Dialog
    if (showAddDialog) {
        AlertDialog(
            onDismissRequest = { 
                showAddDialog = false
                newKey = ""
                newValue = ""
                newType = ValueType.STRING
            },
            title = { Text("Add Key-Value Pair") },
            text = {
                Column(
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    OutlinedTextField(
                        value = newKey,
                        onValueChange = { newKey = it },
                        label = { Text("Key") },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true
                    )
                    
                    OutlinedTextField(
                        value = newValue,
                        onValueChange = { newValue = it },
                        label = { Text("Value") },
                        modifier = Modifier.fillMaxWidth(),
                        enabled = newType != ValueType.NULL,
                        singleLine = true
                    )
                    
                    Text("Type:", style = MaterialTheme.typography.bodySmall)
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        FilterChip(
                            selected = newType == ValueType.STRING,
                            onClick = { newType = ValueType.STRING },
                            label = { Text("String") },
                            modifier = Modifier.weight(1f)
                        )
                        FilterChip(
                            selected = newType == ValueType.NUMBER,
                            onClick = { newType = ValueType.NUMBER },
                            label = { Text("Number") },
                            modifier = Modifier.weight(1f)
                        )
                        FilterChip(
                            selected = newType == ValueType.BOOLEAN,
                            onClick = { newType = ValueType.BOOLEAN },
                            label = { Text("Boolean") },
                            modifier = Modifier.weight(1f)
                        )
                        FilterChip(
                            selected = newType == ValueType.NULL,
                            onClick = { 
                                newType = ValueType.NULL
                                newValue = ""
                            },
                            label = { Text("Null") },
                            modifier = Modifier.weight(1f)
                        )
                    }
                }
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        if (newKey.isNotEmpty()) {
                            viewModel.addKeyValuePair(
                                key = newKey,
                                value = newValue,
                                type = newType
                            )
                            showAddDialog = false
                            newKey = ""
                            newValue = ""
                            newType = ValueType.STRING
                        }
                    },
                    enabled = newKey.isNotEmpty()
                ) {
                    Text("Add")
                }
            },
            dismissButton = {
                TextButton(
                    onClick = {
                        showAddDialog = false
                        newKey = ""
                        newValue = ""
                        newType = ValueType.STRING
                    }
                ) {
                    Text("Cancel")
                }
            }
        )
    }
}

@Composable
fun KeyValuePairItem(
    key: String,
    valueNode: re.weare.app.util.JsonNode.ValueNode,
    onUpdate: (String, String, ValueType) -> Unit,
    onDelete: () -> Unit
) {
    var keyText by remember { mutableStateOf(key) }
    var valueText by remember { mutableStateOf(valueNode.value) }
    var selectedType by remember { mutableStateOf(valueNode.type) }
    
    Card(
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
                OutlinedTextField(
                    value = keyText,
                    onValueChange = { 
                        val oldKey = keyText
                        keyText = it
                        if (it != oldKey && it.isNotEmpty()) {
                            onUpdate(oldKey, valueText, selectedType)
                        }
                    },
                    label = { Text("Key") },
                    modifier = Modifier.weight(1f)
                )
                IconButton(onClick = onDelete) {
                    Icon(Icons.Default.Delete, contentDescription = "Delete")
                }
            }
            
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                OutlinedTextField(
                    value = valueText,
                    onValueChange = { 
                        valueText = it
                        onUpdate(keyText, valueText, selectedType)
                    },
                    label = { Text("Value") },
                    modifier = Modifier.weight(1f)
                )
                
                // Type Selector
                FilterChip(
                    selected = selectedType == ValueType.STRING,
                    onClick = { 
                        selectedType = ValueType.STRING
                        onUpdate(keyText, valueText, selectedType)
                    },
                    label = { Text("String") }
                )
                FilterChip(
                    selected = selectedType == ValueType.NUMBER,
                    onClick = { 
                        selectedType = ValueType.NUMBER
                        onUpdate(keyText, valueText, selectedType)
                    },
                    label = { Text("Number") }
                )
                FilterChip(
                    selected = selectedType == ValueType.BOOLEAN,
                    onClick = { 
                        selectedType = ValueType.BOOLEAN
                        onUpdate(keyText, valueText, selectedType)
                    },
                    label = { Text("Boolean") }
                )
                FilterChip(
                    selected = selectedType == ValueType.NULL,
                    onClick = { 
                        selectedType = ValueType.NULL
                        onUpdate(keyText, "", selectedType)
                    },
                    label = { Text("Null") }
                )
            }
        }
    }
}

