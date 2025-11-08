package com.prettyjson.android.util

import org.json.JSONArray
import org.json.JSONObject
import org.junit.Assert.*
import org.junit.Test

class JsonBucketInserterTest {

    @Test
    fun merge_objectIntoObject_preservesBoth() {
        val current = "{\"a\":1}"
        val bucket = "{\"b\":2}"
        val merged = JsonBucketInserter.insertBucket(current, bucket)
        val obj = JSONObject(merged)
        assertEquals(1, obj.getInt("a"))
        assertTrue(obj.has("b"))
    }

    @Test
    fun merge_arrayIntoObject_addsArrayKey() {
        val current = "{\"a\":1}"
        val bucket = "[1,2,3]"
        val merged = JsonBucketInserter.insertBucket(current, bucket)
        val obj = JSONObject(merged)
        // Should have either "data" or a generated key holding array
        val hasArrayField = obj.keys().asSequence().any { key ->
            try { obj.get(key) is JSONArray } catch (e: Exception) { false }
        }
        assertTrue(hasArrayField)
    }

    @Test
    fun merge_primitives_createsArray() {
        val current = "1"
        val bucket = "2"
        val merged = JsonBucketInserter.insertBucket(current, bucket)
        val arr = JSONArray(merged)
        assertEquals(2, arr.length())
    }
}


