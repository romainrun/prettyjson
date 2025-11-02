package re.weare.app.util

import java.text.SimpleDateFormat
import java.util.*

/**
 * Utility for formatting dates and times
 */
object DateFormatter {
    private val dateTimeFormat = SimpleDateFormat("MMM dd, yyyy HH:mm", Locale.getDefault())
    private val dateFormat = SimpleDateFormat("MMM dd, yyyy", Locale.getDefault())
    private val timeFormat = SimpleDateFormat("HH:mm", Locale.getDefault())
    
    /**
     * Formats a timestamp to "MMM dd, yyyy HH:mm" format
     */
    fun formatDateTime(timestamp: Long): String {
        return dateTimeFormat.format(Date(timestamp))
    }
    
    /**
     * Formats a timestamp to "MMM dd, yyyy" format
     */
    fun formatDate(timestamp: Long): String {
        return dateFormat.format(Date(timestamp))
    }
    
    /**
     * Formats a timestamp to "HH:mm" format
     */
    fun formatTime(timestamp: Long): String {
        return timeFormat.format(Date(timestamp))
    }
    
    /**
     * Formats timestamp for filename (already exists, keeping for compatibility)
     */
    fun formatDateTimeForFilename(timestamp: Long): String {
        val format = SimpleDateFormat("yyyy-MM-dd_HH-mm-ss", Locale.getDefault())
        return format.format(Date(timestamp))
    }
}
