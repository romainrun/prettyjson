package com.prettyjson.android.util

import android.content.ContentResolver
import android.content.Context
import android.os.Environment
import android.provider.OpenableColumns
import androidx.core.content.FileProvider
import java.io.File
import java.io.FileOutputStream
import java.text.SimpleDateFormat
import java.util.*

/**
 * Utility for managing JSON file import/export
 */
object FileManager {
    
    /**
     * Export JSON to Downloads folder
     */
    fun exportJsonToDownloads(context: Context, jsonString: String, fileName: String? = null): Result<File> {
        return try {
            val sanitizedFileName = fileName?.replace("\\s+".toRegex(), "_")
                ?: "json_${SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(Date())}"
            
            val downloadsDir = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS)
            
            if (!downloadsDir.exists()) {
                downloadsDir.mkdirs()
            }
            
            val file = File(downloadsDir, "$sanitizedFileName.json")
            file.writeText(jsonString)
            
            Result.success(file)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    /**
     * Read file content from URI
     */
    fun readFileContent(context: Context, uri: android.net.Uri): Result<String> {
        return try {
            context.contentResolver.openInputStream(uri)?.use { inputStream ->
                inputStream.bufferedReader().use { reader ->
                    Result.success(reader.readText())
                }
            } ?: Result.failure(Exception("Could not open file"))
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    /**
     * Get file name from URI
     */
    fun getFileName(context: Context, uri: android.net.Uri): String? {
        var fileName: String? = null
        context.contentResolver.query(uri, null, null, null, null)?.use { cursor ->
            if (cursor.moveToFirst()) {
                val nameIndex = cursor.getColumnIndex(OpenableColumns.DISPLAY_NAME)
                if (nameIndex != -1) {
                    fileName = cursor.getString(nameIndex)
                }
            }
        }
        return fileName ?: uri.lastPathSegment
    }
    
    /**
     * Get file URI for sharing
     */
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






