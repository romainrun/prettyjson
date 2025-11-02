package re.weare.app.util

import android.content.Context
import android.content.Intent
import android.os.Build
import android.os.Environment
import android.print.PrintAttributes
import android.print.PrintDocumentAdapter
import android.print.PrintManager
import android.webkit.WebView
import android.webkit.WebViewClient
import androidx.core.content.FileProvider
import java.io.File
import java.text.SimpleDateFormat
import java.util.*

/**
 * Utility for exporting JSON to PDF and file formats
 */
object PdfExporter {
    
    /**
     * Export JSON as PDF using Android Print Framework
     */
    fun exportToPdf(context: Context, jsonString: String, fileName: String? = null) {
        val webView = WebView(context)
        webView.webViewClient = object : WebViewClient() {
            override fun onPageFinished(view: WebView?, url: String?) {
                createPdf(context, webView, fileName ?: "json_${SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(Date())}")
            }
        }
        
        // Create HTML content with formatted JSON
        val htmlContent = """
            <!DOCTYPE html>
            <html>
            <head>
                <meta charset="UTF-8">
                <style>
                    body {
                        font-family: monospace;
                        padding: 20px;
                        font-size: 12px;
                        background: white;
                        color: #333;
                    }
                    pre {
                        white-space: pre-wrap;
                        word-wrap: break-word;
                    }
                </style>
            </head>
            <body>
                <pre>${jsonString.replace("<", "&lt;").replace(">", "&gt;")}</pre>
            </body>
            </html>
        """.trimIndent()
        
        webView.loadDataWithBaseURL(null, htmlContent, "text/html", "UTF-8", null)
    }
    
    private fun createPdf(context: Context, webView: WebView, fileName: String) {
        val printManager = context.getSystemService(Context.PRINT_SERVICE) as? PrintManager
        if (printManager != null) {
            val printAdapter = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                webView.createPrintDocumentAdapter(fileName)
            } else {
                @Suppress("DEPRECATION")
                webView.createPrintDocumentAdapter()
            }
            
            val jobName = "JSON Export: $fileName"
            printManager.print(jobName, printAdapter, PrintAttributes.Builder().build())
        }
    }
    
    /**
     * Export JSON as .json file
     * Tries Downloads folder first, falls back to app's Documents folder
     */
    fun exportToJsonFile(context: Context, jsonString: String, fileName: String? = null): Result<File> {
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
            
            val jsonFile = File(targetDir, "$sanitizedFileName.json")
            jsonFile.writeText(jsonString, Charsets.UTF_8)
            
            Result.success(jsonFile)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    /**
     * Share JSON file via Intent
     */
    fun shareJsonFile(context: Context, file: File, fileName: String) {
        val uri = getFileUri(context, file)
        if (uri != null) {
            val shareIntent = Intent().apply {
                action = Intent.ACTION_SEND
                putExtra(Intent.EXTRA_STREAM, uri)
                type = "application/json"
                addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
            }
            context.startActivity(Intent.createChooser(shareIntent, "Share JSON File"))
        }
    }
    
    fun getFileUri(context: Context, file: File): android.net.Uri? {
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

