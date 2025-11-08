package com.prettyjson.android.util

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.os.Environment
import androidx.core.content.FileProvider
import java.io.File
import java.text.SimpleDateFormat
import java.util.*

/**
 * Utility for exporting JSON to HTML format with syntax highlighting
 */
object HtmlExporter {
    
    /**
     * Export JSON as HTML file with syntax highlighting
     */
    fun exportToHtml(context: Context, jsonString: String, fileName: String? = null): Result<File> {
        return try {
            val sanitizedFileName = fileName?.replace("\\s+".toRegex(), "_")
                ?: "json_${SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(Date())}"
            
            // Try Downloads folder (public, accessible from file manager)
            val downloadsDir = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                // Android 10+: Use MediaStore or app-specific directory
                context.getExternalFilesDir(Environment.DIRECTORY_DOWNLOADS)
                    ?: context.getExternalFilesDir(Environment.DIRECTORY_DOCUMENTS)
            } else {
                // Android 9 and below: Can use public Downloads
                Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS)
            }
            
            val targetDir = downloadsDir
                ?: context.getExternalFilesDir(Environment.DIRECTORY_DOCUMENTS)
                ?: File(context.filesDir, "Documents")
            
            targetDir.mkdirs()
            
            val htmlFile = File(targetDir, "$sanitizedFileName.html")
            val htmlContent = generateHtml(jsonString)
            htmlFile.writeText(htmlContent, Charsets.UTF_8)
            
            Result.success(htmlFile)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    /**
     * Generate HTML content with syntax highlighting for JSON
     */
    private fun generateHtml(jsonString: String): String {
        val escapedJson = jsonString
            .replace("&", "&amp;")
            .replace("<", "&lt;")
            .replace(">", "&gt;")
            .replace("\"", "&quot;")
            .replace("'", "&#39;")
        
        // Apply syntax highlighting
        val highlightedJson = highlightJson(escapedJson)
        
        return """
<!DOCTYPE html>
<html lang="en">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>JSON Viewer</title>
    <style>
        * {
            margin: 0;
            padding: 0;
            box-sizing: border-box;
        }
        body {
            font-family: 'JetBrains Mono', 'Fira Code', 'Courier New', monospace;
            background: #1e1e1e;
            color: #d4d4d4;
            padding: 20px;
            line-height: 1.6;
        }
        .container {
            max-width: 1200px;
            margin: 0 auto;
            background: #252526;
            border-radius: 8px;
            padding: 20px;
            box-shadow: 0 4px 6px rgba(0, 0, 0, 0.3);
        }
        h1 {
            color: #4ec9b0;
            margin-bottom: 20px;
            font-size: 24px;
        }
        pre {
            background: #1e1e1e;
            padding: 20px;
            border-radius: 4px;
            overflow-x: auto;
            border: 1px solid #3e3e42;
            white-space: pre-wrap;
            word-wrap: break-word;
        }
        .json-key {
            color: #4ec9b0;
            font-weight: 600;
        }
        .json-string {
            color: #ce9178;
        }
        .json-number {
            color: #b5cea8;
        }
        .json-boolean {
            color: #569cd6;
            font-weight: bold;
        }
        .json-null {
            color: #569cd6;
            font-style: italic;
        }
        .json-bracket {
            color: #d4d4d4;
            font-weight: bold;
        }
        .json-punctuation {
            color: #d4d4d4;
        }
        @media (prefers-color-scheme: light) {
            body {
                background: #ffffff;
                color: #333333;
            }
            .container {
                background: #f5f5f5;
            }
            pre {
                background: #ffffff;
                border: 1px solid #e0e0e0;
            }
            .json-key {
                color: #0066cc;
            }
            .json-string {
                color: #008000;
            }
            .json-number {
                color: #098658;
            }
            .json-boolean {
                color: #0000ff;
            }
            .json-null {
                color: #808080;
            }
            .json-bracket {
                color: #333333;
            }
            .json-punctuation {
                color: #333333;
            }
        }
    </style>
</head>
<body>
    <div class="container">
        <h1>JSON Viewer</h1>
        <pre>$highlightedJson</pre>
    </div>
</body>
</html>
        """.trimIndent()
    }
    
    /**
     * Apply syntax highlighting to JSON string
     */
    private fun highlightJson(json: String): String {
        val result = StringBuilder()
        var i = 0
        var inString = false
        var escapeNext = false
        
        while (i < json.length) {
            val char = json[i]
            
            if (escapeNext) {
                result.append(char)
                escapeNext = false
                i++
                continue
            }
            
            if (char == '\\') {
                result.append(char)
                escapeNext = true
                i++
                continue
            }
            
            if (char == '"') {
                if (!inString) {
                    // Start of string (key or value)
                    result.append("<span class=\"json-key\">")
                } else {
                    // End of string
                    result.append("</span>")
                }
                result.append(char)
                inString = !inString
                i++
                continue
            }
            
            if (inString) {
                result.append(char)
                i++
                continue
            }
            
            when {
                char == '{' || char == '}' -> {
                    result.append("<span class=\"json-bracket\">$char</span>")
                }
                char == '[' || char == ']' -> {
                    result.append("<span class=\"json-bracket\">$char</span>")
                }
                char == ':' || char == ',' -> {
                    result.append("<span class=\"json-punctuation\">$char</span>")
                }
                char.isDigit() || char == '-' || char == '.' -> {
                    // Number
                    val start = i
                    while (i < json.length && (json[i].isDigit() || json[i] == '.' || json[i] == '-' || json[i] == 'e' || json[i] == 'E' || json[i] == '+' || json[i] == '-')) {
                        i++
                    }
                    val number = json.substring(start, i)
                    result.append("<span class=\"json-number\">$number</span>")
                    continue
                }
                json.substring(i).startsWith("true") && (i == 0 || !json[i - 1].isLetterOrDigit()) -> {
                    result.append("<span class=\"json-boolean\">true</span>")
                    i += 4
                    continue
                }
                json.substring(i).startsWith("false") && (i == 0 || !json[i - 1].isLetterOrDigit()) -> {
                    result.append("<span class=\"json-boolean\">false</span>")
                    i += 5
                    continue
                }
                json.substring(i).startsWith("null") && (i == 0 || !json[i - 1].isLetterOrDigit()) -> {
                    result.append("<span class=\"json-null\">null</span>")
                    i += 4
                    continue
                }
                else -> {
                    result.append(char)
                }
            }
            
            i++
        }
        
        return result.toString()
    }
    
    /**
     * Share HTML file via Intent
     */
    fun shareHtmlFile(context: Context, file: File) {
        val uri = getFileUri(context, file)
        if (uri != null) {
            val shareIntent = Intent().apply {
                action = Intent.ACTION_SEND
                putExtra(Intent.EXTRA_STREAM, uri)
                type = "text/html"
                addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
            }
            context.startActivity(Intent.createChooser(shareIntent, "Share HTML File"))
        }
    }
    
    private fun getFileUri(context: Context, file: File): Uri? {
        return try {
            FileProvider.getUriForFile(
                context,
                "${context.packageName}.fileprovider",
                file
            )
        } catch (e: Exception) {
            null
        }
    }
}

