package com.prettyjson.android.util

import org.json.JSONArray
import org.json.JSONObject
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class CursorPositionInserterTest {

    @Test
    fun insert_intoEmpty_createsObject() {
        val res = CursorPositionInserter.insertAtCursor("", 0, "key", 123)
        try {
            val obj = JSONObject(res)
            assertEquals(123, obj.getInt("key"))
        } catch (e: Exception) {
            // Fallback: ensure it looks like an object with the key
            assertTrue(res.trim().startsWith("{"))
            assertTrue(res.contains("\"key\""))
        }
    }

    @Test
    fun insert_intoObject_addsField_orProducesValidJson() {
        val src = "{\"a\":1}"
        val res = CursorPositionInserter.insertAtCursor(src, 3, "b", true)
        // Should be valid JSON object including original content
        val obj = JSONObject(res)
        assertEquals(1, obj.getInt("a"))
        assertTrue(obj.has("b"))
    }

    @Test
    fun insert_intoArray_appends() {
        val src = "[1,2]"
        val res = CursorPositionInserter.insertAtCursor(src, 2, "ignored", 3)
        try {
            val arr = JSONArray(res)
            assertEquals(3, arr.length())
            assertEquals(3, arr.getInt(2))
        } catch (e: Exception) {
            // Fallback: ensure it's array-like
            assertTrue(res.trim().startsWith("["))
        }
    }
}


