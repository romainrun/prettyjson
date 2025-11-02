package re.weare.app.ui.components

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
import androidx.compose.ui.text.input.TransformedText
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

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
    textSize: Int = 14
) {
    val lines = value.lines()
    val lineCount = lines.size.coerceAtLeast(1)
    val fontFamilyObj = getFontFamily(fontFamily)
    val effectiveTextStyle = textStyle.copy(
        fontFamily = fontFamilyObj,
        fontSize = androidx.compose.ui.unit.TextUnit(textSize.toFloat(), androidx.compose.ui.unit.TextUnitType.Sp)
    )
    
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
        // Line numbers column
        Column(
            modifier = Modifier
                .widthIn(min = 40.dp, max = 50.dp)
                .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
                .padding(vertical = 12.dp, horizontal = 8.dp),
            verticalArrangement = Arrangement.spacedBy(0.dp),
            horizontalAlignment = Alignment.End
        ) {
            repeat(lineCount) { index ->
                val lineNumber = index + 1
                val isErrorLine = errorLine != null && lineNumber == errorLine
                Text(
                    text = "$lineNumber",
                    style = MaterialTheme.typography.bodySmall.copy(
                        fontSize = 12.sp,
                        fontFamily = effectiveTextStyle.fontFamily,
                        color = if (isErrorLine) {
                            MaterialTheme.colorScheme.error
                        } else {
                            MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f)
                        }
                    ),
                    textAlign = TextAlign.Right,
                    modifier = Modifier
                        .fillMaxWidth()
                        .heightIn(min = 20.dp)
                        .background(
                            if (isErrorLine) {
                                MaterialTheme.colorScheme.errorContainer.copy(alpha = 0.3f)
                            } else {
                                androidx.compose.ui.graphics.Color.Transparent
                            },
                            RoundedCornerShape(4.dp)
                        )
                        .padding(vertical = 2.dp)
                )
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
            
            BasicTextField(
                value = value,
                onValueChange = onValueChange,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 12.dp),
                textStyle = effectiveTextStyle.copy(
                    color = MaterialTheme.colorScheme.onSurface,
                    fontFamily = effectiveTextStyle.fontFamily
                ),
                readOnly = readOnly,
                maxLines = maxLines,
                minLines = minLines,
                singleLine = false,
                visualTransformation = if (!readOnly) BracketColorTransformation(MaterialTheme.colorScheme) else androidx.compose.ui.text.input.VisualTransformation { original ->
                    TransformedText(
                        androidx.compose.ui.text.AnnotatedString(original.text),
                        androidx.compose.ui.text.input.OffsetMapping.Identity
                    )
                }
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
        else -> FontFamily.Monospace
    }
}

/**
 * Visual transformation to color JSON syntax
 * Colors brackets, keys, strings, numbers, booleans, and null
 */
private fun BracketColorTransformation(colorScheme: androidx.compose.material3.ColorScheme): VisualTransformation {
    return VisualTransformation { original ->
        
        val annotatedString = buildAnnotatedString {
            val text = original.text.toString()
            var index = 0
            
            // Color scheme
            val keyColor = androidx.compose.ui.graphics.Color(0xFF00BCD4) // Teal
            val stringColor = androidx.compose.ui.graphics.Color(0xFFFF9800) // Amber
            val numberColor = androidx.compose.ui.graphics.Color(0xFF2196F3) // Blue
            val booleanColor = androidx.compose.ui.graphics.Color(0xFF9C27B0) // Purple
            val nullColor = androidx.compose.ui.graphics.Color(0xFFF44336) // Red
            
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
                        when (char) {
                            '{', '}' -> {
                                addStyle(
                                    style = SpanStyle(
                                        color = colorScheme.primary,
                                        fontWeight = FontWeight.Bold
                                    ),
                                    start = length - 1,
                                    end = length
                                )
                            }
                            '[', ']' -> {
                                addStyle(
                                    style = SpanStyle(
                                        color = colorScheme.secondary,
                                        fontWeight = FontWeight.Bold
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
        }
        
        TransformedText(
            text = annotatedString,
            offsetMapping = androidx.compose.ui.text.input.OffsetMapping.Identity
        )
    }
}

