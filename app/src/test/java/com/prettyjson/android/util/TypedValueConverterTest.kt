package com.prettyjson.android.util

import org.json.JSONArray
import org.json.JSONObject
import org.junit.Assert.*
import org.junit.Test

class TypedValueConverterTest {

    @Test
    fun convert_json_returnsJsonObject() {
        val v = TypedValueConverter.convertValue("{\"a\":1}", "json")
        assertTrue(v is JSONObject)
        assertEquals(1, (v as JSONObject).getInt("a"))
    }

    @Test
    fun convert_array_parsesJSONArray_orCSV() {
        val v1 = TypedValueConverter.convertValue("[1,2,3]", "array")
        assertTrue(v1 is JSONArray)
        assertEquals(3, (v1 as JSONArray).length())

        val v2 = TypedValueConverter.convertValue("a, b, c", "array")
        assertTrue(v2 is JSONArray)
        assertEquals("a", (v2 as JSONArray).getString(0))
    }

    @Test
    fun convert_primitives() {
        assertEquals(42, TypedValueConverter.convertValue("42", "integer"))
        val f = TypedValueConverter.convertValue("3.14", "float")
        assertTrue(f is Float)
        assertEquals(3.14f, f as Float, 0.0001f)
        assertEquals(true, TypedValueConverter.convertValue("true", "boolean"))
        assertEquals(JSONObject.NULL, TypedValueConverter.convertValue("", "null"))
        assertEquals("abc", TypedValueConverter.convertValue("abc", "string"))
    }
}


