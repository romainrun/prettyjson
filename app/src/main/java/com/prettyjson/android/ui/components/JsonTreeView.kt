package com.prettyjson.android.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectDragGesturesAfterLongPress
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.google.gson.JsonElement
import com.google.gson.JsonParser

/**
 * Tree view for JSON with collapsible nodes and drag & drop support
 */
@Composable
fun JsonTreeView(
    jsonString: String,
    modifier: Modifier = Modifier,
    onReorder: ((String) -> Unit)? = null,
    onPathCopied: ((String) -> Unit)? = null
) {
    var mutableJson by remember { mutableStateOf(jsonString) }
    var allExpanded by remember { mutableStateOf(false) }
    
    // Update when jsonString changes from outside
    LaunchedEffect(jsonString) {
        mutableJson = jsonString
    }
    
    val jsonElement = remember(mutableJson) {
        try {
            JsonParser.parseString(mutableJson)
        } catch (e: Exception) {
            null
        }
    }
    
    if (jsonElement == null) {
        Text(
            "Invalid JSON",
            color = MaterialTheme.colorScheme.error,
            modifier = modifier.padding(16.dp)
        )
    } else {
        Box(
            modifier = modifier
                .fillMaxWidth()
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(start = 8.dp, top = 8.dp, end = 8.dp, bottom = 8.dp)
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(2.dp)
            ) {
                // Expand/Collapse All button
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.End
                ) {
                    TextButton(
                        onClick = { allExpanded = !allExpanded }
                    ) {
                        Icon(
                            if (allExpanded) Icons.Default.ExpandLess else Icons.Default.ExpandMore,
                            contentDescription = null,
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(if (allExpanded) "Collapse All" else "Expand All")
                    }
                }
                
                JsonNodeItem(
                    element = jsonElement,
                    key = "root",
                    level = 0,
                    isRoot = true,
                    parentPath = "",
                    allExpanded = allExpanded,
                    onReorder = { newJson ->
                        mutableJson = newJson
                        onReorder?.invoke(newJson)
                    },
                    onPathCopied = onPathCopied
                )
            }
        }
    }
}

@Composable
private fun JsonNodeItem(
    element: JsonElement,
    key: String,
    level: Int,
    isRoot: Boolean = false,
    parentPath: String = "",
    allExpanded: Boolean = false,
    onReorder: ((String) -> Unit)? = null,
    onPathCopied: ((String) -> Unit)? = null
) {
    var isExpanded by remember { mutableStateOf(!isRoot) }
    
    // Sync with allExpanded state
    LaunchedEffect(allExpanded) {
        isExpanded = allExpanded || !isRoot
    }
    var isDragging by remember { mutableStateOf(false) }
    var dragTargetKey by remember { mutableStateOf<String?>(null) }
    var isDropTarget by remember { mutableStateOf(false) }
    
    val currentPath = if (isRoot) "root" else if (parentPath.isEmpty()) key else "$parentPath.$key"
    val canDrag = !isRoot && onReorder != null
    val context = androidx.compose.ui.platform.LocalContext.current
    
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(start = (level * 12).dp, top = 1.dp, bottom = 1.dp)
            .background(
                if (isDropTarget) MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.3f)
                else Color.Transparent,
                androidx.compose.foundation.shape.RoundedCornerShape(4.dp)
            )
            .padding(horizontal = 2.dp, vertical = 1.dp)
            .pointerInput(canDrag) {
                if (canDrag && (element.isJsonObject || element.isJsonArray)) {
                    detectDragGesturesAfterLongPress(
                        onDragStart = {
                            isDragging = true
                        },
                        onDragEnd = {
                            isDragging = false
                            dragTargetKey = null
                        }
                    ) { change, _ ->
                        change.consume()
                    }
                }
            }
            .pointerInput(Unit) {
                detectTapGestures(
                    onTap = {
                        if (!canDrag || !isDragging) {
                            isExpanded = !isExpanded
                        }
                    },
                    onLongPress = {
                        // Copy path on long press
                        val clipboard = context.getSystemService(android.content.Context.CLIPBOARD_SERVICE) as android.content.ClipboardManager
                        val clip = android.content.ClipData.newPlainText("JSON Path", currentPath)
                        clipboard.setPrimaryClip(clip)
                        onPathCopied?.invoke(currentPath)
                    }
                )
            },
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        when {
            element.isJsonObject -> {
                Icon(
                    if (isExpanded) Icons.Default.ArrowDropDown else Icons.Default.ArrowForward,
                    contentDescription = null,
                    modifier = Modifier.size(14.dp),
                    tint = MaterialTheme.colorScheme.primary
                )
                Text(
                    if (isRoot) "OBJECT" else "\"$key\"",
                    style = MaterialTheme.typography.bodySmall.copy(
                        fontWeight = if (isRoot) FontWeight.Bold else FontWeight.Normal,
                        fontFamily = FontFamily.Monospace,
                        fontSize = 11.sp
                    ),
                    color = MaterialTheme.colorScheme.primary
                )
                Text(
                    "{${element.asJsonObject.size()}}",
                    style = MaterialTheme.typography.labelSmall.copy(
                        fontSize = 10.sp
                    ),
                    color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f)
                )
                
                // Drag handle icon for objects
                if (canDrag && !isRoot) {
                    Icon(
                        Icons.Default.UnfoldMore,
                        contentDescription = "Drag to reorder",
                        modifier = Modifier.size(16.dp),
                        tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f)
                    )
                }
                
                if (isExpanded) {
                    Column(
                        modifier = Modifier.fillMaxWidth(),
                        verticalArrangement = Arrangement.spacedBy(2.dp)
                    ) {
                        element.asJsonObject.entrySet().forEach { entry ->
                            JsonNodeItem(
                                element = entry.value,
                                key = entry.key,
                                level = level + 1,
                                parentPath = currentPath,
                                allExpanded = allExpanded,
                                onReorder = onReorder,
                                onPathCopied = onPathCopied
                            )
                        }
                    }
                }
            }
            element.isJsonArray -> {
                Icon(
                    if (isExpanded) Icons.Default.ArrowDropDown else Icons.Default.ArrowForward,
                    contentDescription = null,
                    modifier = Modifier.size(14.dp),
                    tint = MaterialTheme.colorScheme.secondary
                )
                Text(
                    if (isRoot) "ARRAY" else "\"$key\"",
                    style = MaterialTheme.typography.bodySmall.copy(
                        fontFamily = FontFamily.Monospace,
                        fontSize = 11.sp
                    ),
                    color = MaterialTheme.colorScheme.secondary
                )
                Text(
                    "[${element.asJsonArray.size()}]",
                    style = MaterialTheme.typography.labelSmall.copy(
                        fontSize = 10.sp
                    ),
                    color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f)
                )
                
                // Drag handle icon for objects
                if (canDrag && !isRoot) {
                    Icon(
                        Icons.Default.UnfoldMore,
                        contentDescription = "Drag to reorder",
                        modifier = Modifier.size(14.dp),
                        tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f)
                    )
                }
                
                if (isExpanded) {
                    Column(
                        modifier = Modifier.fillMaxWidth(),
                        verticalArrangement = Arrangement.spacedBy(1.dp)
                    ) {
                        element.asJsonArray.forEachIndexed { index, item ->
                            JsonNodeItem(
                                element = item,
                                key = "[$index]",
                                level = level + 1,
                                parentPath = currentPath,
                                allExpanded = allExpanded,
                                onReorder = onReorder,
                                onPathCopied = onPathCopied
                            )
                        }
                    }
                }
            }
            element.isJsonPrimitive -> {
                val primitive = element.asJsonPrimitive
                val value = when {
                    primitive.isString -> "\"${primitive.asString}\""
                    primitive.isNumber -> primitive.asString
                    primitive.isBoolean -> primitive.asString
                    primitive.isJsonNull -> "null"
                    else -> primitive.asString
                }
                
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        "\"$key\":",
                        style = MaterialTheme.typography.bodySmall.copy(
                            fontFamily = FontFamily.Monospace,
                            fontSize = 11.sp
                        ),
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Text(
                        value,
                        style = MaterialTheme.typography.bodySmall.copy(
                            fontFamily = FontFamily.Monospace,
                            fontSize = 11.sp
                        ),
                        color = when {
                            primitive.isString -> MaterialTheme.colorScheme.primary
                            primitive.isNumber -> MaterialTheme.colorScheme.secondary
                            primitive.isBoolean -> MaterialTheme.colorScheme.tertiary
                            else -> MaterialTheme.colorScheme.onSurfaceVariant
                        }
                    )
                }
            }
        }
    }
}

