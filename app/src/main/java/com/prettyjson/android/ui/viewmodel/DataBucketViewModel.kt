package com.prettyjson.android.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import com.prettyjson.android.data.database.DataBucket
import com.prettyjson.android.data.repository.DataBucketRepository

/**
 * ViewModel for managing data buckets
 */
class DataBucketViewModel(
    private val repository: DataBucketRepository
) : ViewModel() {
    
    val allDataBuckets: StateFlow<List<DataBucket>> = repository.getAllDataBucketsSorted()
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )
    
    fun insertDataBucket(dataBucket: DataBucket) {
        viewModelScope.launch {
            repository.saveDataBucket(dataBucket)
        }
    }
    
    fun updateDataBucket(dataBucket: DataBucket) {
        viewModelScope.launch {
            repository.saveDataBucket(dataBucket)
        }
    }
    
    fun deleteDataBucket(dataBucket: DataBucket) {
        viewModelScope.launch {
            repository.deleteDataBucket(dataBucket)
        }
    }
    
    fun deleteDataBucketById(id: Int) {
        viewModelScope.launch {
            repository.deleteDataBucketById(id)
        }
    }
    
    suspend fun initializeExampleBuckets() {
        val existingBuckets = repository.getAllDataBucketsSorted().first()
        
        // Only create examples if no buckets exist
        if (existingBuckets.isEmpty()) {
            val exampleBuckets = listOf(
                DataBucket(
                    keyName = "user",
                    valueType = "json",
                    value = "{\n  \"id\": 1,\n  \"firstName\": \"John\",\n  \"lastName\": \"Doe\",\n  \"username\": \"johndoe\",\n  \"email\": \"john.doe@example.com\",\n  \"age\": 30,\n  \"phone\": \"+1-555-0123\",\n  \"isActive\": true,\n  \"createdAt\": \"2024-01-15T10:30:00Z\"\n}",
                    description = "Complete user object with ID, contact info, and metadata"
                ),
                DataBucket(
                    keyName = "address",
                    valueType = "json",
                    value = "{\n  \"street\": \"123 Main St\",\n  \"city\": \"New York\",\n  \"state\": \"NY\",\n  \"zipCode\": \"10001\",\n  \"country\": \"USA\",\n  \"coordinates\": {\n    \"lat\": 40.7128,\n    \"lng\": -74.0060\n  }\n}",
                    description = "Address object with location coordinates"
                ),
                DataBucket(
                    keyName = "product",
                    valueType = "json",
                    value = "{\n  \"id\": \"prod_123\",\n  \"name\": \"Premium Widget\",\n  \"description\": \"High-quality widget for all your needs\",\n  \"price\": 29.99,\n  \"currency\": \"USD\",\n  \"inStock\": true,\n  \"stockQuantity\": 150,\n  \"category\": \"Electronics\",\n  \"tags\": [\"popular\", \"featured\", \"new\"]\n}",
                    description = "E-commerce product with pricing and inventory"
                ),
                DataBucket(
                    keyName = "users",
                    valueType = "array",
                    value = "[\n  {\"id\": 1, \"name\": \"Alice\", \"email\": \"alice@example.com\", \"role\": \"admin\"},\n  {\"id\": 2, \"name\": \"Bob\", \"email\": \"bob@example.com\", \"role\": \"user\"},\n  {\"id\": 3, \"name\": \"Charlie\", \"email\": \"charlie@example.com\", \"role\": \"user\"}\n]",
                    description = "Array of user objects with roles"
                ),
                DataBucket(
                    keyName = "apiResponse",
                    valueType = "json",
                    value = "{\n  \"status\": \"success\",\n  \"code\": 200,\n  \"message\": \"Request processed successfully\",\n  \"data\": {\n    \"id\": 12345,\n    \"timestamp\": \"2024-01-15T10:30:00Z\"\n  },\n  \"meta\": {\n    \"version\": \"1.0\",\n    \"requestId\": \"req_abc123\"\n  }\n}",
                    description = "Standard API response structure"
                ),
                DataBucket(
                    keyName = "error",
                    valueType = "json",
                    value = "{\n  \"status\": \"error\",\n  \"code\": 400,\n  \"message\": \"Invalid request parameters\",\n  \"errors\": [\n    {\"field\": \"email\", \"message\": \"Invalid email format\"},\n    {\"field\": \"password\", \"message\": \"Password too short\"}\n  ]\n}",
                    description = "Error response with validation details"
                ),
                DataBucket(
                    keyName = "timestamp",
                    valueType = "string",
                    value = "2024-01-15T10:30:00Z",
                    description = "ISO 8601 timestamp string"
                ),
                DataBucket(
                    keyName = "email",
                    valueType = "string",
                    value = "user@example.com",
                    description = "Example email address"
                ),
                DataBucket(
                    keyName = "userId",
                    valueType = "integer",
                    value = "12345",
                    description = "Numeric user ID"
                ),
                DataBucket(
                    keyName = "price",
                    valueType = "float",
                    value = "29.99",
                    description = "Decimal price value"
                ),
                DataBucket(
                    keyName = "isActive",
                    valueType = "boolean",
                    value = "true",
                    description = "Boolean flag for active status"
                ),
                DataBucket(
                    keyName = "tags",
                    valueType = "array",
                    value = "[\"tag1\", \"tag2\", \"tag3\"]",
                    description = "Array of string tags"
                ),
                DataBucket(
                    keyName = "metadata",
                    valueType = "json",
                    value = "{\n  \"version\": \"1.0\",\n  \"createdAt\": \"2024-01-15T10:30:00Z\",\n  \"updatedAt\": \"2024-01-15T11:00:00Z\",\n  \"author\": \"system\"\n}",
                    description = "Metadata object with version and timestamps"
                ),
                DataBucket(
                    keyName = "pagination",
                    valueType = "json",
                    value = "{\n  \"page\": 1,\n  \"pageSize\": 20,\n  \"totalPages\": 5,\n  \"totalItems\": 100,\n  \"hasNext\": true,\n  \"hasPrevious\": false\n}",
                    description = "Pagination metadata for list responses"
                ),
                DataBucket(
                    keyName = "optionalField",
                    valueType = "null",
                    value = "",
                    description = "JSON null value for optional fields"
                )
            )
            
            exampleBuckets.forEach { bucket ->
                repository.saveDataBucket(bucket)
            }
        }
    }
}

