package com.prettyjson.android.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.*
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.text.input.TransformedText
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.foundation.clickable
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ExpandLess
import androidx.compose.material.icons.filled.ExpandMore
import androidx.compose.ui.graphics.Color
import com.prettyjson.android.util.JsonFolding
import com.prettyjson.android.util.BracketMatcher

/**
 * Text field with line numbers on the left, IDE-style
 */
@Composable
fun LineNumberTextField(
    value: String,
    onValueChange: (String) -> Unit,
    modifier: Modifier = Modifier,
    placeholder: String? = null,
    readOnly: Boolean = false,
    maxLines: Int = Int.MAX_VALUE,
    minLines: Int = 1,
    textStyle: TextStyle = MaterialTheme.typography.bodyLarge.copy(
        fontFamily = FontFamily.Monospace
    ),
    errorLine: Int? = null,
    fontFamily: String = "jetbrains",
    textSize: Int = 14,
    onCursorPositionChange: ((Int) -> Unit)? = null,
    onFocusChange: ((Boolean) -> Unit)? = null,
    searchTerm: String = "",
    enableFolding: Boolean = false,
    lineWrapping: Boolean = false,
    scrollToLine: Int? = null
) {
    // Track cursor position using TextFieldValue
    // Don't use remember(value) as it resets state on every value change
    var textFieldValue by remember { mutableStateOf(TextFieldValue(value)) }
    var cursorPosition by remember { mutableStateOf(0) }
    var lastSyncedValue by remember { mutableStateOf(value) }
    
    // Update textFieldValue when value changes externally (but maintain cursor)
    LaunchedEffect(value) {
        // Only sync if value changed externally (not from our own onValueChange)
        // Check if the change is significant and not just from user typing
        if (value != lastSyncedValue && textFieldValue.text != value) {
            // This is an external change - preserve cursor position but clamp to valid range
            val currentSelection = textFieldValue.selection
            val newSelection = TextRange(
                start = currentSelection.start.coerceIn(0, value.length),
                end = currentSelection.end.coerceIn(0, value.length)
            )
            textFieldValue = TextFieldValue(value, newSelection)
            cursorPosition = newSelection.start
            onCursorPositionChange?.invoke(cursorPosition)
            lastSyncedValue = value
        }
    }
    
    val fontFamilyObj = getFontFamily(fontFamily)
    val effectiveTextStyle = textStyle.copy(
        fontFamily = fontFamilyObj,
        fontSize = androidx.compose.ui.unit.TextUnit(textSize.toFloat(), androidx.compose.ui.unit.TextUnitType.Sp),
        lineHeight = androidx.compose.ui.unit.TextUnit(textSize * 1.5f, androidx.compose.ui.unit.TextUnitType.Sp)
    )
    // Calculate line height based on font size - match BasicTextField's internal line height
    // BasicTextField uses lineHeight multiplier of ~1.2-1.5, so we use the same
    val calculatedLineHeight = (textSize * 1.4f).coerceAtLeast(18f).dp
    
    // Shared scroll state for synchronization
    val scrollState = rememberScrollState()
    
    // Folding state
    var collapsedRegions by remember { mutableStateOf<Set<Int>>(emptySet()) }
    
    // Calculate display value (with folding if enabled)
    val displayValueForLines = remember(value, collapsedRegions, enableFolding, readOnly) {
        if (enableFolding && collapsedRegions.isNotEmpty() && value.isNotEmpty() && readOnly) {
            try {
                JsonFolding.foldJson(value, collapsedRegions)
            } catch (e: Exception) {
                value
            }
        } else {
            value
        }
    }
    
    // Use the actual displayed text for line counting
    // For editable fields, use textFieldValue.text to get real-time line count
    // For read-only fields with folding, use displayValueForLines
    val textForLineCounting = if (!readOnly) {
        textFieldValue.text
    } else {
        displayValueForLines
    }
    // Count lines correctly - count newlines + 1 (for the last line)
    val lineCount = if (textForLineCounting.isEmpty()) {
        1
    } else {
        textForLineCounting.count { it == '\n' } + 1
    }
    
    // Auto-scroll to line when scrollToLine changes
    LaunchedEffect(scrollToLine, lineCount) {
        scrollToLine?.let { lineNumber ->
            if (lineNumber > 0 && lineNumber <= lineCount) {
                // Calculate approximate scroll position based on line number
                val lineHeight = calculatedLineHeight.value
                val scrollPosition = (lineNumber - 1) * lineHeight
                scrollState.animateScrollTo(scrollPosition.toInt())
            }
        }
    }
    val foldRegions = remember(value, enableFolding) {
        if (enableFolding && value.isNotEmpty()) {
            try {
                JsonFolding.findFoldRegions(value)
            } catch (e: Exception) {
                emptyList()
            }
        } else {
            emptyList()
        }
    }
    
    // Map fold regions to line numbers in the original (unfolded) text
    val foldRegionsByLine = remember(foldRegions, value) {
        if (foldRegions.isEmpty()) emptyMap<Int, JsonFolding.FoldRegion>()
        else {
            val map = mutableMapOf<Int, JsonFolding.FoldRegion>()
            foldRegions.forEach { region ->
                // Find which line contains the start of this region in the original text
                val lineNumber = value.substring(0, region.startIndex).count { it == '\n' } + 1
                map[lineNumber] = region
            }
            map
        }
    }
    
    // Map original line numbers to folded line numbers
    val originalToFoldedLineMap = remember(value, collapsedRegions, enableFolding, readOnly) {
        if (!enableFolding || collapsedRegions.isEmpty() || !readOnly) {
            emptyMap<Int, Int>()
        } else {
            try {
                val foldedText = JsonFolding.foldJson(value, collapsedRegions)
                val originalLines = value.lines()
                val foldedLines = foldedText.lines()
                
                // Build a map from original line number to folded line number
                val map = mutableMapOf<Int, Int>()
                var originalLineNum = 1
                var foldedLineNum = 1
                var originalCharIndex = 0
                var foldedCharIndex = 0
                
                while (originalLineNum <= originalLines.size && foldedLineNum <= foldedLines.size) {
                    map[originalLineNum] = foldedLineNum
                    
                    // Move to next line in original
                    originalCharIndex += originalLines[originalLineNum - 1].length + 1 // +1 for newline
                    originalLineNum++
                    
                    // Move to next line in folded
                    foldedCharIndex += foldedLines[foldedLineNum - 1].length + 1 // +1 for newline
                    foldedLineNum++
                }
                
                map
            } catch (e: Exception) {
                emptyMap()
            }
        }
    }
    
    // Wrap in a Box with fixed height to prevent infinite constraints
    Box(
        modifier = modifier
    ) {
        Row(
            modifier = Modifier
                .fillMaxSize()
                .background(
                    MaterialTheme.colorScheme.surface,
                    RoundedCornerShape(4.dp)
                )
        ) {
        // Line numbers column - scrollable and synchronized with text content
        Column(
            modifier = Modifier
                .widthIn(min = if (enableFolding) 48.dp else 36.dp, max = if (enableFolding) 56.dp else 44.dp)
                .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
                .verticalScroll(scrollState)
                .padding(vertical = 8.dp, horizontal = 6.dp),
            verticalArrangement = Arrangement.spacedBy(0.dp),
            horizontalAlignment = Alignment.End
        ) {
            repeat(lineCount) { index ->
                val displayLineNumber = index + 1
                
                // Find fold region for this display line
                // When folding is enabled, we need to map display lines back to original lines
                val foldRegion = if (enableFolding && foldRegions.isNotEmpty() && collapsedRegions.isNotEmpty()) {
                    // Calculate character position in displayed text
                    val displayText = displayValueForLines
                    val displayLines = displayText.lines()
                    if (index < displayLines.size) {
                        var charPos = 0
                        for (i in 0 until index) {
                            charPos += displayLines[i].length + 1 // +1 for newline
                        }
                        
                        // Find which fold region this position corresponds to
                        // Check if this position is within a collapsed region
                        foldRegions.find { region ->
                            if (collapsedRegions.contains(region.startIndex)) {
                                // This region is collapsed - check if we're at the fold marker
                                val beforeFold = value.substring(0, region.startIndex)
                                val foldMarkerStart = beforeFold.length
                                // The fold marker replaces the entire region, so check if we're at that position
                                charPos >= foldMarkerStart && charPos <= foldMarkerStart + 5 // "{...}" is 5 chars
                            } else {
                                false
                            }
                        } ?: run {
                            // Not at a fold marker - find which original line this corresponds to
                            // This is approximate - find the closest original line
                            val originalLineNum = value.substring(0, charPos.coerceAtMost(value.length)).count { it == '\n' } + 1
                            foldRegionsByLine[originalLineNum]
                        }
                    } else {
                        null
                    }
                } else if (enableFolding && foldRegions.isNotEmpty()) {
                    // No collapsed regions - use direct line mapping
                    foldRegionsByLine[displayLineNumber]
                } else {
                    null
                }
                
                val isCollapsed = foldRegion != null && collapsedRegions.contains(foldRegion.startIndex)
                
                // Error line checking - use display line number
                val isErrorLine = errorLine != null && displayLineNumber == errorLine
                
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(calculatedLineHeight),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // Fold indicator (if folding is enabled and this line has a fold region)
                    if (enableFolding && foldRegion != null) {
                        IconButton(
                            onClick = {
                                collapsedRegions = if (isCollapsed) {
                                    collapsedRegions - foldRegion.startIndex
                                } else {
                                    collapsedRegions + foldRegion.startIndex
                                }
                            },
                            modifier = Modifier.size(20.dp)
                        ) {
                            Icon(
                                if (isCollapsed) Icons.Default.ExpandMore else Icons.Default.ExpandLess,
                                contentDescription = if (isCollapsed) "Expand" else "Collapse",
                                modifier = Modifier.size(14.dp),
                                tint = MaterialTheme.colorScheme.primary
                            )
                        }
                    } else {
                        Spacer(modifier = Modifier.width(20.dp))
                    }
                    
                Text(
                        text = "$displayLineNumber",
                    style = MaterialTheme.typography.bodySmall.copy(
                        fontSize = (textSize * 0.85f).sp,
                        fontFamily = effectiveTextStyle.fontFamily,
                        lineHeight = androidx.compose.ui.unit.TextUnit(textSize * 1.4f, androidx.compose.ui.unit.TextUnitType.Sp),
                        color = if (isErrorLine) {
                            MaterialTheme.colorScheme.error
                        } else {
                            MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f)
                        }
                    ),
                    textAlign = TextAlign.Right,
                    modifier = Modifier
                            .weight(1f)
                        .background(
                            if (isErrorLine) {
                                MaterialTheme.colorScheme.errorContainer.copy(alpha = 0.3f)
                            } else {
                                androidx.compose.ui.graphics.Color.Transparent
                            },
                            RoundedCornerShape(4.dp)
                        )
                        .wrapContentHeight(Alignment.CenterVertically)
                )
                }
            }
        }
        
        // Divider
        Divider(
            modifier = Modifier
                .width(1.dp)
                .fillMaxHeight(),
            color = MaterialTheme.colorScheme.outline.copy(alpha = 0.3f)
        )
        
        // Text field
        Box(
            modifier = Modifier
                .weight(1f)
        ) {
            if (value.isEmpty() && placeholder != null) {
                Text(
                    text = placeholder,
                    style = textStyle.copy(
                        color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f)
                    ),
                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 12.dp)
                )
            }
            
            // Apply folding transformation if enabled and there are collapsed regions
            // Note: Folding is primarily for viewing - editing will expand collapsed regions
            // Use the same displayValueForLines to ensure consistency
            val displayValue = displayValueForLines
            
            // Update textFieldValue when displayValue changes (due to folding)
            LaunchedEffect(displayValue) {
                if (readOnly && textFieldValue.text != displayValue && displayValue != value) {
                    // This is a folding transformation for read-only view
                    val currentSelection = textFieldValue.selection
                    val newSelection = TextRange(
                        start = currentSelection.start.coerceIn(0, displayValue.length),
                        end = currentSelection.end.coerceIn(0, displayValue.length)
                    )
                    textFieldValue = TextFieldValue(displayValue, newSelection)
                }
            }
            
            // Function to find fold region at a given character position
            fun findFoldRegionAtPosition(charIndex: Int): JsonFolding.FoldRegion? {
                if (!enableFolding || foldRegions.isEmpty()) return null
                
                // Check if this position is at the start of a fold region (the opening brace)
                return foldRegions.find { region ->
                    // Check if the click is on the opening brace of this region
                    charIndex == region.startIndex || charIndex == region.startIndex + 1
                }
            }
            
            BasicTextField(
                value = textFieldValue,
                onValueChange = { newValue ->
                    // If editing, expand all collapsed regions first
                    if (enableFolding && collapsedRegions.isNotEmpty() && !readOnly) {
                        // Expand all when editing starts
                        collapsedRegions = emptySet()
                    }
                    
                    textFieldValue = newValue
                    cursorPosition = newValue.selection.start
                    // Update parent state
                    onValueChange(newValue.text)
                    onCursorPositionChange?.invoke(cursorPosition)
                    lastSyncedValue = newValue.text
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .verticalScroll(scrollState)
                    .padding(horizontal = 12.dp, vertical = 8.dp)
                    .onFocusChanged { onFocusChange?.invoke(it.isFocused) },
                textStyle = effectiveTextStyle.copy(
                    color = MaterialTheme.colorScheme.onSurface,
                    fontFamily = effectiveTextStyle.fontFamily,
                    lineHeight = effectiveTextStyle.lineHeight
                ),
                cursorBrush = androidx.compose.ui.graphics.SolidColor(
                    MaterialTheme.colorScheme.primary
                ),
                readOnly = readOnly,
                maxLines = if (lineWrapping) maxLines else Int.MAX_VALUE,
                visualTransformation = if (!readOnly) 
                    BracketColorTransformation(MaterialTheme.colorScheme, searchTerm, value, cursorPosition) 
                else 
                    SearchHighlightTransformation(searchTerm, MaterialTheme.colorScheme)
            )
        }
        }
    }
}

/**
 * Get font family based on preference
 * Note: For now, we use Monospace as a fallback since custom fonts require font files
 */
fun getFontFamily(fontFamily: String): FontFamily {
    return when (fontFamily) {
        "jetbrains" -> FontFamily.Monospace // JetBrains Mono (use Monospace as fallback)
        "fira" -> FontFamily.Monospace // Fira Code (use Monospace as fallback)
        "monospace" -> FontFamily.Monospace // Monospace font
        "default" -> FontFamily.Default // System default
        else -> FontFamily.Monospace
    }
}

/**
 * Visual transformation to highlight search terms
 */
private fun SearchHighlightTransformation(searchTerm: String, colorScheme: androidx.compose.material3.ColorScheme): VisualTransformation {
    return VisualTransformation { original ->
        if (searchTerm.isEmpty()) {
            TransformedText(
                androidx.compose.ui.text.AnnotatedString(original.text),
                androidx.compose.ui.text.input.OffsetMapping.Identity
            )
        } else {
            val annotatedString = buildAnnotatedString {
                val text = original.text.toString()
                val regex = Regex(searchTerm, RegexOption.IGNORE_CASE)
                var lastIndex = 0
                
                regex.findAll(text).forEach { matchResult ->
                    // Add text before match
                    if (matchResult.range.first > lastIndex) {
                        append(text.substring(lastIndex, matchResult.range.first))
                    }
                    
                    // Add highlighted match
                    val matchStart = length
                    append(matchResult.value)
                    val matchEnd = length
                    
                    addStyle(
                        style = SpanStyle(
                            background = colorScheme.primaryContainer,
                            color = colorScheme.onPrimaryContainer,
                            fontWeight = FontWeight.Bold
                        ),
                        start = matchStart,
                        end = matchEnd
                    )
                    
                    lastIndex = matchResult.range.last + 1
                }
                
                // Add remaining text
                if (lastIndex < text.length) {
                    append(text.substring(lastIndex))
                }
            }
            
            TransformedText(
                annotatedString,
                androidx.compose.ui.text.input.OffsetMapping.Identity
            )
        }
    }
}

/**
 * Visual transformation to color JSON syntax
 * Colors brackets, keys, strings, numbers, booleans, and null
 * Also highlights search terms if provided
 */
private fun BracketColorTransformation(
    colorScheme: androidx.compose.material3.ColorScheme, 
    searchTerm: String = "",
    text: String = "",
    cursorPosition: Int = 0
): VisualTransformation {
    return VisualTransformation { original ->
        
        val annotatedString = buildAnnotatedString {
            val text = original.text.toString()
            var index = 0
            
            // Find all search matches first
            val searchMatches = if (searchTerm.isNotEmpty()) {
                val regex = Regex(searchTerm, RegexOption.IGNORE_CASE)
                regex.findAll(text).map { it.range }.toList()
            } else {
                emptyList()
            }
            
            // Color scheme
            val keyColor = androidx.compose.ui.graphics.Color(0xFF00BCD4) // Teal
            val stringColor = androidx.compose.ui.graphics.Color(0xFFFF9800) // Amber
            val numberColor = androidx.compose.ui.graphics.Color(0xFF2196F3) // Blue
            val booleanColor = androidx.compose.ui.graphics.Color(0xFF9C27B0) // Purple
            val nullColor = androidx.compose.ui.graphics.Color(0xFFF44336) // Red
            
            // Find matching bracket if cursor is on a bracket
            val matchingBracketPosition = if (cursorPosition >= 0 && cursorPosition < text.length && 
                BracketMatcher.isBracket(text, cursorPosition)) {
                BracketMatcher.findMatchingBracket(text, cursorPosition)
            } else {
                null
            }
            
            while (index < text.length) {
                var found = false
                
                // Check for strings (keys and values)
                if (text[index] == '"' && index < text.length) {
                    val start = index
                    index++
                    while (index < text.length && text[index] != '"') {
                        if (text[index] == '\\' && index + 1 < text.length) {
                            index += 2
                        } else {
                            index++
                        }
                    }
                    if (index < text.length) {
                        index++
                        val substring = text.substring(start, index)
                        append(substring)
                        
                        // Determine if it's a key (check if followed by colon)
                        val isKey = index < text.length && 
                                   text.substring(index).trimStart().startsWith(":")
                        
                        addStyle(
                            style = SpanStyle(
                                color = if (isKey) keyColor else stringColor,
                                fontWeight = if (isKey) FontWeight.SemiBold else FontWeight.Normal
                            ),
                            start = length - substring.length,
                            end = length
                        )
                        found = true
                    }
                }
                
                if (!found) {
                    // Check for numbers
                    if ((text[index].isDigit() || text[index] == '-') && index < text.length) {
                        val start = index
                        if (text[index] == '-') index++
                        while (index < text.length && (text[index].isDigit() || text[index] == '.')) {
                            index++
                        }
                        val substring = text.substring(start, index)
                        if (substring != "-") {
                            append(substring)
                            addStyle(
                                style = SpanStyle(color = numberColor, fontWeight = FontWeight.Normal),
                                start = length - substring.length,
                                end = length
                            )
                            found = true
                        } else {
                            index = start + 1
                        }
                    }
                    
                    // Check for booleans and null
                    if (!found) {
                        val remaining = text.substring(index)
                        when {
                            remaining.startsWith("true") && (index == 0 || !text[index - 1].isLetterOrDigit()) -> {
                                append("true")
                                addStyle(
                                    style = SpanStyle(color = booleanColor, fontWeight = FontWeight.Bold),
                                    start = length - 4,
                                    end = length
                                )
                                index += 4
                                found = true
                            }
                            remaining.startsWith("false") && (index == 0 || !text[index - 1].isLetterOrDigit()) -> {
                                append("false")
                                addStyle(
                                    style = SpanStyle(color = booleanColor, fontWeight = FontWeight.Bold),
                                    start = length - 5,
                                    end = length
                                )
                                index += 5
                                found = true
                            }
                            remaining.startsWith("null") && (index == 0 || !text[index - 1].isLetterOrDigit()) -> {
                                append("null")
                                addStyle(
                                    style = SpanStyle(color = nullColor, fontStyle = FontStyle.Italic),
                                    start = length - 4,
                                    end = length
                                )
                                index += 4
                                found = true
                            }
                        }
                    }
                    
                    if (!found) {
                        val char = text[index]
                        append(char)
                        
                        // Highlight brackets and punctuation
                        val isMatchingBracket = matchingBracketPosition != null && 
                            (index == cursorPosition || index == matchingBracketPosition)
                        
                        when (char) {
                            '{', '}' -> {
                                addStyle(
                                    style = SpanStyle(
                                        color = if (isMatchingBracket) colorScheme.tertiary else colorScheme.primary,
                                        fontWeight = FontWeight.Bold,
                                        background = if (isMatchingBracket) colorScheme.tertiaryContainer.copy(alpha = 0.5f) else androidx.compose.ui.graphics.Color.Transparent
                                    ),
                                    start = length - 1,
                                    end = length
                                )
                            }
                            '[', ']' -> {
                                addStyle(
                                    style = SpanStyle(
                                        color = if (isMatchingBracket) colorScheme.tertiary else colorScheme.secondary,
                                        fontWeight = FontWeight.Bold,
                                        background = if (isMatchingBracket) colorScheme.tertiaryContainer.copy(alpha = 0.5f) else androidx.compose.ui.graphics.Color.Transparent
                                    ),
                                    start = length - 1,
                                    end = length
                                )
                            }
                            ':', ',' -> {
                                addStyle(
                                    style = SpanStyle(
                                        color = colorScheme.onSurface.copy(alpha = 0.7f),
                                        fontWeight = FontWeight.Medium
                                    ),
                                    start = length - 1,
                                    end = length
                                )
                            }
                        }
                        index++
                    }
                }
            }
            
            // Apply search highlighting on top of syntax highlighting
            if (searchTerm.isNotEmpty() && searchMatches.isNotEmpty()) {
                searchMatches.forEach { matchRange ->
                    val matchStart = matchRange.first
                    val matchEnd = matchRange.last + 1
                    
                    // Check if the match is within the annotated string bounds
                    // Use length (current position in annotatedString) instead of annotatedString.length
                    if (matchStart < length && matchEnd <= length) {
                        // Add search highlight style (will override syntax colors)
                        addStyle(
                            style = SpanStyle(
                                background = colorScheme.primaryContainer,
                                color = colorScheme.onPrimaryContainer,
                                fontWeight = FontWeight.Bold
                            ),
                            start = matchStart,
                            end = matchEnd
                        )
                    }
                }
            }
        }
        
        TransformedText(
            text = annotatedString,
            offsetMapping = androidx.compose.ui.text.input.OffsetMapping.Identity
        )
    }
}

