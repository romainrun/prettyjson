package com.prettyjson.android.data.billing

import android.app.Activity
import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.*
import androidx.datastore.preferences.preferencesDataStore
import com.android.billingclient.api.*
import com.prettyjson.android.data.preferences.PremiumManager
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch

private val Context.proBillingDataStore: DataStore<Preferences> by preferencesDataStore(name = "pro_billing")

/**
 * ProManager handles Play Billing integration for one-time Pro Plan purchase
 * 
 * Product ID: "pro_plan" (one-time purchase at €1.50)
 * Product Name: "pro_plan_1_50" (as configured in Google Play Console)
 * 
 * This manager:
 * - Integrates with Play Billing Library
 * - Stores purchase state in DataStore
 * - Provides local test flag for debug builds
 * - Handles purchase flow and restore purchase
 */
class ProManager(
    private val context: Context,
    private val premiumManager: PremiumManager
) {
    private val dataStore = context.proBillingDataStore
    private var billingClient: BillingClient? = null
    
    companion object {
        // Product ID matches Google Play Console one-time product
        // Product ID: pro_plan
        // Name: pro_plan_1_50
        private const val PRODUCT_ID = "pro_plan"
        private val KEY_IS_PRO_USER = booleanPreferencesKey("is_pro_user")
        private val KEY_PURCHASE_TOKEN = stringPreferencesKey("purchase_token")
        private val KEY_DEV_MODE_PRO = booleanPreferencesKey("dev_mode_pro") // For debug builds
    }
    
    /**
     * Initialize billing client
     */
    fun initializeBilling(onReady: (Boolean) -> Unit) {
        billingClient = BillingClient.newBuilder(context)
            .setListener { billingResult, purchases ->
                if (billingResult.responseCode == BillingClient.BillingResponseCode.OK && purchases != null) {
                    handlePurchases(purchases)
                }
            }
            .enablePendingPurchases()
            .build()
        
        billingClient?.startConnection(object : BillingClientStateListener {
            override fun onBillingSetupFinished(billingResult: BillingResult) {
                if (billingResult.responseCode == BillingClient.BillingResponseCode.OK) {
                    // Check for existing purchases
                    queryPurchases()
                    onReady(true)
                } else {
                    onReady(false)
                }
            }
            
            override fun onBillingServiceDisconnected() {
                onReady(false)
            }
        })
    }
    
    /**
     * Check if user is Pro (includes dev mode override)
     */
    val isProUser: Flow<Boolean> = dataStore.data.map { preferences ->
        // Dev mode override (for testing only)
        val devModePro = preferences[KEY_DEV_MODE_PRO] ?: false
        if (devModePro) return@map true
        
        // Check Pro status
        preferences[KEY_IS_PRO_USER] ?: false
    }
    
    /**
     * Get Pro status synchronously
     */
    suspend fun hasPro(): Boolean {
        val preferences = dataStore.data.first()
        
        // Dev mode override
        val devModePro = preferences[KEY_DEV_MODE_PRO] ?: false
        if (devModePro) return true
        
        return preferences[KEY_IS_PRO_USER] ?: false
    }
    
    /**
     * Launch purchase flow
     */
    fun launchPurchaseFlow(activity: Activity, productDetails: ProductDetails, onResult: (BillingResult) -> Unit) {
        val productDetailsParamsList = listOf(
            BillingFlowParams.ProductDetailsParams.newBuilder()
                .setProductDetails(productDetails)
                .build()
        )
        
        val billingFlowParams = BillingFlowParams.newBuilder()
            .setProductDetailsParamsList(productDetailsParamsList)
            .build()
        
        billingClient?.launchBillingFlow(activity, billingFlowParams)?.let { result ->
            onResult(result)
        } ?: run {
            onResult(BillingResult.newBuilder()
                .setResponseCode(BillingClient.BillingResponseCode.ERROR)
                .setDebugMessage("Billing client not initialized")
                .build())
        }
    }
    
    /**
     * Restore purchase - query existing purchases
     */
    fun restorePurchase(onResult: (Boolean) -> Unit) {
        queryPurchases()
        // Check if purchase was restored
        CoroutineScope(Dispatchers.IO).launch {
            val hasPro = hasPro()
            onResult(hasPro)
        }
    }
    
    /**
     * Query product details from Play Console
     */
    fun queryProductDetails(onResult: (ProductDetails?) -> Unit) {
        val productList = listOf(
            QueryProductDetailsParams.Product.newBuilder()
                .setProductId(PRODUCT_ID)
                .setProductType(BillingClient.ProductType.INAPP)
                .build()
        )
        
        val params = QueryProductDetailsParams.newBuilder()
            .setProductList(productList)
            .build()
        
        billingClient?.queryProductDetailsAsync(params) { billingResult, productDetailsList ->
            if (billingResult.responseCode == BillingClient.BillingResponseCode.OK) {
                onResult(productDetailsList.firstOrNull())
            } else {
                onResult(null)
            }
        }
    }
    
    /**
     * Query existing purchases
     */
    private fun queryPurchases() {
        billingClient?.queryPurchasesAsync(
            QueryPurchasesParams.newBuilder()
                .setProductType(BillingClient.ProductType.INAPP)
                .build()
        ) { billingResult, purchases ->
            if (billingResult.responseCode == BillingClient.BillingResponseCode.OK) {
                handlePurchases(purchases)
            }
        }
    }
    
    /**
     * Handle purchases - verify and store
     */
    private fun handlePurchases(purchases: List<Purchase>) {
        val proPurchase = purchases.find { it.products.contains(PRODUCT_ID) }
        
        if (proPurchase != null) {
            // Verify purchase
            if (proPurchase.purchaseState == Purchase.PurchaseState.PURCHASED) {
                if (!proPurchase.isAcknowledged) {
                    // Acknowledge purchase
                    acknowledgePurchase(proPurchase)
                }
                
                // Store purchase state
                CoroutineScope(Dispatchers.IO).launch {
                    setProUser(true, proPurchase.purchaseToken)
                    // Also update PremiumManager
                    premiumManager.setPremium(true, "onetime", 0L)
                }
            }
        }
    }
    
    /**
     * Acknowledge purchase
     */
    private fun acknowledgePurchase(purchase: Purchase) {
        val acknowledgeParams = AcknowledgePurchaseParams.newBuilder()
            .setPurchaseToken(purchase.purchaseToken)
            .build()
        
        billingClient?.acknowledgePurchase(acknowledgeParams) { billingResult ->
            if (billingResult.responseCode == BillingClient.BillingResponseCode.OK) {
                // Purchase acknowledged
            }
        }
    }
    
    /**
     * Set Pro user status
     */
    private suspend fun setProUser(isPro: Boolean, purchaseToken: String? = null) {
        dataStore.edit { preferences ->
            preferences[KEY_IS_PRO_USER] = isPro
            if (purchaseToken != null) {
                preferences[KEY_PURCHASE_TOKEN] = purchaseToken
            } else if (!isPro) {
                preferences.remove(KEY_PURCHASE_TOKEN)
            }
        }
    }
    
    /**
     * Get dev mode Pro status
     */
    val devModePro: Flow<Boolean> = dataStore.data.map { preferences ->
        preferences[KEY_DEV_MODE_PRO] ?: false
    }
    
    /**
     * Set dev mode Pro status (for testing only)
     */
    suspend fun setDevModePro(enabled: Boolean) {
        dataStore.edit { preferences ->
            preferences[KEY_DEV_MODE_PRO] = enabled
        }
    }
    
    /**
     * Release billing client
     */
    fun release() {
        billingClient?.endConnection()
        billingClient = null
    }
}

