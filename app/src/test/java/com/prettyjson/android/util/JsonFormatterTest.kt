package com.prettyjson.android.util

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class JsonFormatterTest {

    @Test
    fun validate_returnsTrue_forValidJson() {
        val result = JsonFormatter.validate("{" + '"' + "a" + '"' + ":1}")
        assertTrue(result.isValid)
    }

    @Test
    fun validate_returnsFalse_forInvalidJson() {
        val result = JsonFormatter.validate("{a:}")
        assertFalse(result.isValid)
    }

    @Test
    fun format_usesCustomIndentation() {
        val src = "{" + '"' + "a" + '"' + ":{" + '"' + "b" + '"' + ":1}}"
        val formatted = JsonFormatter.format(src, tabSpaces = 4)
        assertTrue(formatted.success)
        // Expect at least one line with 4 spaces indentation
        assertTrue(formatted.content.lines().any { it.startsWith("    \"b\"") || it.contains("    \"b\"") })
    }

    @Test
    fun minify_compactsJson() {
        val src = "{\n  \"a\": 1,\n  \"b\": 2\n}"
        val minified = JsonFormatter.minify(src)
        assertTrue(minified.success)
        assertEquals("{\"a\":1,\"b\":2}", minified.content)
    }

    @Test
    fun sortKeys_sortsByKeyAsc() {
        val src = "{\"b\":1,\"a\":2}"
        val sorted = JsonFormatter.sortKeys(src, JsonFormatter.SortOrder.ASC, JsonFormatter.SortBy.KEY)
        assertTrue(sorted.success)
        // prettyGson prints with newlines and spaces; just ensure key order is a then b
        val content = sorted.content.replace("\n", "")
        val firstAIndex = content.indexOf("\"a\"")
        val firstBIndex = content.indexOf("\"b\"")
        assertTrue(firstAIndex in 0 until firstBIndex)
    }
}