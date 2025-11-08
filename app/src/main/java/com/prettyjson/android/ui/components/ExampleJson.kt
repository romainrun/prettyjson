package com.prettyjson.android.ui.components

/**
 * Example JSON strings for demo purposes
 */
object ExampleJson {
    val basicExample = """
        {
          "name": "John Doe",
          "age": 30,
          "email": "john.doe@example.com",
          "address": {
            "street": "123 Main St",
            "city": "New York",
            "zipCode": "10001"
          },
          "hobbies": ["reading", "coding", "traveling"],
          "active": true
        }
    """.trimIndent()
    
    val apiResponseExample = """
        {
          "status": "success",
          "code": 200,
          "data": {
            "users": [
              {
                "id": 1,
                "name": "Alice",
                "role": "admin",
                "permissions": ["read", "write", "delete"]
              },
              {
                "id": 2,
                "name": "Bob",
                "role": "user",
                "permissions": ["read"]
              }
            ],
            "pagination": {
              "page": 1,
              "totalPages": 10,
              "itemsPerPage": 20
            }
          },
          "timestamp": "2024-01-15T10:30:00Z"
        }
    """.trimIndent()
    
    val configExample = """
        {
          "app": {
            "name": "MyApp",
            "version": "1.0.0",
            "debug": false
          },
          "database": {
            "host": "localhost",
            "port": 5432,
            "name": "mydb",
            "ssl": true
          },
          "features": {
            "authentication": true,
            "notifications": false,
            "analytics": true
          }
        }
    """.trimIndent()
    
    val arrayExample = """
        [
          {
            "id": 1,
            "title": "First Item",
            "completed": false
          },
          {
            "id": 2,
            "title": "Second Item",
            "completed": true
          },
          {
            "id": 3,
            "title": "Third Item",
            "completed": false
          }
        ]
    """.trimIndent()
    
    /**
     * Get a random example JSON
     */
    fun getRandomExample(): String {
        val examples = listOf(basicExample, apiResponseExample, configExample, arrayExample)
        return examples.random()
    }
    
    /**
     * Get all examples for selection
     */
    fun getAllExamples(): Map<String, String> {
        return mapOf(
            "Basic Example" to basicExample,
            "API Response" to apiResponseExample,
            "Config File" to configExample,
            "Array Example" to arrayExample
        )
    }
}


