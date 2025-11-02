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
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

/**
 * Help/Documentation screen for JSON learning
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HelpScreen(
    onNavigateBack: () -> Unit
) {
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("JSON Help & Guide") },
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
            verticalArrangement = Arrangement.spacedBy(24.dp)
        ) {
            // What is JSON Section
            HelpSection(
                title = "What is JSON?",
                icon = Icons.Default.Info
            ) {
                Text(
                    "JSON (JavaScript Object Notation) is a lightweight data-interchange format. " +
                    "It's easy for humans to read and write and easy for machines to parse and generate. " +
                    "JSON is language-independent but uses conventions familiar to programmers.",
                    style = MaterialTheme.typography.bodyMedium
                )
            }
            
            // JSON Syntax Section
            HelpSection(
                title = "JSON Syntax",
                icon = Icons.Default.Settings
            ) {
                Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    Text(
                        "Basic Rules:",
                        style = MaterialTheme.typography.titleSmall
                    )
                    BulletPoint("Data is in name/value pairs")
                    BulletPoint("Data is separated by commas")
                    BulletPoint("Curly braces {} hold objects")
                    BulletPoint("Square brackets [] hold arrays")
                    BulletPoint("Strings must be in double quotes")
                    
                    Spacer(Modifier.height(8.dp))
                    
                    Text(
                        "Example:",
                        style = MaterialTheme.typography.titleSmall
                    )
                    
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(
                            containerColor = MaterialTheme.colorScheme.surfaceVariant
                        )
                    ) {
                        Text(
                            text = """
                                {
                                  "name": "John",
                                  "age": 30,
                                  "city": "New York"
                                }
                            """.trimIndent(),
                            modifier = Modifier.padding(16.dp),
                            style = MaterialTheme.typography.bodySmall.copy(
                                fontFamily = FontFamily.Monospace,
                                fontSize = 12.sp
                            ),
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }
            
            // Data Types Section
            HelpSection(
                title = "JSON Data Types",
                icon = Icons.Default.List
            ) {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    TypeItem("String", "\"Hello World\"", "Must be in double quotes")
                    TypeItem("Number", "42, 3.14", "Integers or floats")
                    TypeItem("Boolean", "true, false", "Lowercase only")
                    TypeItem("Null", "null", "Lowercase only")
                    TypeItem("Object", "{}", "Unordered key-value pairs")
                    TypeItem("Array", "[]", "Ordered list of values")
                }
            }
            
            // Best Practices Section
            HelpSection(
                title = "Best Practices",
                icon = Icons.Default.CheckCircle
            ) {
                Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    PracticeItem(
                        "Always use double quotes for strings",
                        "Never use single quotes. JSON only supports double quotes."
                    )
                    PracticeItem(
                        "No trailing commas",
                        "Remove commas after the last item in objects or arrays."
                    )
                    PracticeItem(
                        "Use proper indentation",
                        "Format JSON for readability, especially for large documents."
                    )
                    PracticeItem(
                        "Validate before use",
                        "Always validate JSON syntax before using it in production."
                    )
                    PracticeItem(
                        "Use meaningful keys",
                        "Choose descriptive names for object keys to improve readability."
                    )
                }
            }
            
            // Common Mistakes Section
            HelpSection(
                title = "Common Mistakes to Avoid",
                icon = Icons.Default.Warning
            ) {
                Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    MistakeItem(
                        "Single quotes for strings",
                        "❌ Wrong: {'name': 'John'}\n✅ Correct: {\"name\": \"John\"}"
                    )
                    MistakeItem(
                        "Trailing commas",
                        "❌ Wrong: {\"a\": 1, \"b\": 2,}\n✅ Correct: {\"a\": 1, \"b\": 2}"
                    )
                    MistakeItem(
                        "Comments in JSON",
                        "❌ Wrong: {\"name\": \"John\" // comment}\n✅ Correct: JSON doesn't support comments"
                    )
                    MistakeItem(
                        "Unquoted keys",
                        "❌ Wrong: {name: \"John\"}\n✅ Correct: {\"name\": \"John\"}"
                    )
                }
            }
            
            // Examples Section
            HelpSection(
                title = "Examples",
                icon = Icons.Default.Star
            ) {
                Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
                    ExampleCard(
                        "Simple Object",
                        """
                        {
                          "firstName": "John",
                          "lastName": "Doe",
                          "email": "john@example.com"
                        }
                        """.trimIndent()
                    )
                    
                    ExampleCard(
                        "Array of Objects",
                        """
                        [
                          {"name": "Apple", "color": "red"},
                          {"name": "Banana", "color": "yellow"},
                          {"name": "Grape", "color": "purple"}
                        ]
                        """.trimIndent()
                    )
                    
                    ExampleCard(
                        "Nested Objects",
                        """
                        {
                          "user": {
                            "name": "John",
                            "address": {
                              "street": "123 Main St",
                              "city": "New York"
                            }
                          }
                        }
                        """.trimIndent()
                    )
                }
            }
        }
    }
}

@Composable
fun HelpSection(
    title: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    content: @Composable ColumnScope.() -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Icon(
                    icon,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary
                )
                Text(
                    title,
                    style = MaterialTheme.typography.titleMedium
                )
            }
            content()
        }
    }
}

@Composable
fun BulletPoint(text: String) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Text("•", color = MaterialTheme.colorScheme.primary)
        Text(text, style = MaterialTheme.typography.bodyMedium)
    }
}

@Composable
fun TypeItem(name: String, example: String, description: String) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
        )
    ) {
        Column(
            modifier = Modifier.padding(12.dp),
            verticalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            Row(
                horizontalArrangement = Arrangement.SpaceBetween,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(
                    name,
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    example,
                    style = MaterialTheme.typography.bodySmall.copy(
                        fontFamily = FontFamily.Monospace
                    ),
                    color = MaterialTheme.colorScheme.primary
                )
            }
            Text(
                description,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f)
            )
        }
    }
}

@Composable
fun PracticeItem(title: String, description: String) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Icon(
            Icons.Default.Check,
            contentDescription = null,
            tint = MaterialTheme.colorScheme.primary,
            modifier = Modifier.size(20.dp)
        )
        Column(modifier = Modifier.weight(1f)) {
            Text(
                title,
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = androidx.compose.ui.text.font.FontWeight.Bold
            )
            Text(
                description,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f)
            )
        }
    }
}

@Composable
fun MistakeItem(title: String, examples: String) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.errorContainer.copy(alpha = 0.3f)
        )
    ) {
        Column(
            modifier = Modifier.padding(12.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Text(
                title,
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = androidx.compose.ui.text.font.FontWeight.Bold
            )
            Text(
                examples,
                style = MaterialTheme.typography.bodySmall.copy(
                    fontFamily = FontFamily.Monospace,
                    fontSize = 11.sp
                )
            )
        }
    }
}

@Composable
fun ExampleCard(title: String, code: String) {
    Card(
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Text(
                title,
                style = MaterialTheme.typography.titleSmall
            )
            Card(
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surfaceVariant
                )
            ) {
                Text(
                    text = code,
                    modifier = Modifier.padding(12.dp),
                    style = MaterialTheme.typography.bodySmall.copy(
                        fontFamily = FontFamily.Monospace,
                        fontSize = 11.sp
                    ),
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}

