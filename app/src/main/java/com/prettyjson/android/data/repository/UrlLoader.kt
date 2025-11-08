package com.prettyjson.android.data.repository

import okhttp3.OkHttpClient
import okhttp3.Request
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.IOException

/**
 * Service for loading JSON from URLs
 */
class UrlLoader {
    private val client = OkHttpClient.Builder()
        .build()
    
    /**
     * Load JSON from URL
     * @return Result containing the JSON string or error message
     */
    suspend fun loadJsonFromUrl(url: String): Result<String> = withContext(Dispatchers.IO) {
        try {
            val request = Request.Builder()
                .url(url)
                .get()
                .build()
            
            val response = client.newCall(request).execute()
            
            if (!response.isSuccessful) {
                return@withContext Result.failure(
                    IOException("HTTP ${response.code}: ${response.message}")
                )
            }
            
            val body = response.body?.string()
                ?: return@withContext Result.failure(IOException("Empty response body"))
            
            Result.success(body)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}









