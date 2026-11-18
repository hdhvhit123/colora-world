package com.colora.world.data

import android.app.Activity
import android.app.Application
import android.util.Base64
import com.android.billingclient.api.AcknowledgePurchaseParams
import com.android.billingclient.api.BillingClient
import com.android.billingclient.api.BillingClientStateListener
import com.android.billingclient.api.BillingFlowParams
import com.android.billingclient.api.BillingResult
import com.android.billingclient.api.PendingPurchasesParams
import com.android.billingclient.api.ProductDetails
import com.android.billingclient.api.Purchase
import com.android.billingclient.api.QueryProductDetailsParams
import com.android.billingclient.api.QueryPurchasesParams
import com.colora.world.BuildConfig
import kotlinx.coroutines.flow.MutableStateFlow
import java.security.KeyFactory
import java.security.Signature
import java.security.spec.X509EncodedKeySpec

internal fun createEntitlements(app: Application, settings: Settings): Entitlements = PlayEntitlements(app, settings)

/**
 * Real Google Play Billing adapter (store flavor). Nothing is granted unless Play
 * reports PURCHASED *and* the purchase signature verifies against the app's
 * Play licence key (BuildConfig.PLAY_LICENSE_KEY, supplied by the owner – see
 * KNOWN_ISSUES K-05). Pending purchases never grant. Without a configured key the
 * adapter reports ERROR and grants nothing: there is no fake purchase path.
 * Client-side verification is not tamper-proof; spec §19.4 recommends a backend.
 */
private class PlayEntitlements(private val app: Application, private val settings: Settings) : Entitlements {
    override val allUnlocked = false
    override val owned = MutableStateFlow(settings.cachedEntitlements())
    override val state = MutableStateFlow(BillingState.UNAVAILABLE)
    override val products = MutableStateFlow(emptyList<ProductInfo>())
    override val lastMessage = MutableStateFlow<String?>(null)
    private val details = HashMap<String, ProductDetails>()

    private val client: BillingClient = BillingClient.newBuilder(app)
        .setListener { result, purchases -> onPurchases(result, purchases) }
        .enablePendingPurchases(PendingPurchasesParams.newBuilder().enableOneTimeProducts().build())
        .build()

    override fun connect() {
        if (client.isReady) { loadProducts(); return }
        state.value = BillingState.LOADING_PRODUCTS
        client.startConnection(object : BillingClientStateListener {
            override fun onBillingSetupFinished(r: BillingResult) {
                if (r.responseCode == BillingClient.BillingResponseCode.OK) { loadProducts(); restore() }
                else { state.value = BillingState.UNAVAILABLE; lastMessage.value = "billing_unavailable" }
            }
            override fun onBillingServiceDisconnected() { state.value = BillingState.UNAVAILABLE }
        })
    }

    private fun loadProducts() {
        state.value = BillingState.LOADING_PRODUCTS
        val params = QueryProductDetailsParams.newBuilder().setProductList(Entitlements.PACKS.map {
            QueryProductDetailsParams.Product.newBuilder().setProductId(it).setProductType(BillingClient.ProductType.INAPP).build()
        }).build()
        client.queryProductDetailsAsync(params) { r, result ->
            if (r.responseCode != BillingClient.BillingResponseCode.OK) { state.value = BillingState.ERROR; lastMessage.value = "products_unavailable"; return@queryProductDetailsAsync }
            val list = result.productDetailsList
            details.clear(); list.forEach { details[it.productId] = it }
            products.value = list.map { ProductInfo(it.productId, it.title, it.description, it.oneTimePurchaseOfferDetails?.formattedPrice ?: "") }
            state.value = if (list.isEmpty()) BillingState.UNAVAILABLE else BillingState.READY
        }
    }

    override fun purchase(activity: Activity, productId: String) {
        if (state.value == BillingState.PURCHASE_IN_PROGRESS) return // double tap guard
        val pd = details[productId] ?: run { lastMessage.value = "product_not_loaded"; return }
        state.value = BillingState.PURCHASE_IN_PROGRESS
        val flow = BillingFlowParams.newBuilder().setProductDetailsParamsList(listOf(
            BillingFlowParams.ProductDetailsParams.newBuilder().setProductDetails(pd).build()
        )).build()
        val r = client.launchBillingFlow(activity, flow)
        if (r.responseCode != BillingClient.BillingResponseCode.OK) { state.value = BillingState.ERROR; lastMessage.value = "launch_failed" }
    }

    override fun restore() {
        if (!client.isReady) { connect(); return }
        state.value = BillingState.RESTORE_IN_PROGRESS
        client.queryPurchasesAsync(QueryPurchasesParams.newBuilder().setProductType(BillingClient.ProductType.INAPP).build()) { r, list ->
            // Offline/query errors never revoke cached grants.
            if (r.responseCode == BillingClient.BillingResponseCode.OK) {
                val verified = list.filter { it.purchaseState == Purchase.PurchaseState.PURCHASED && verify(it) }
                val ids = verified.flatMap { it.products }.filter { it in Entitlements.PACKS }.toSet()
                owned.value = ids; settings.cacheEntitlements(ids)
                verified.filter { !it.isAcknowledged }.forEach(::acknowledge)
            }
            state.value = if (details.isEmpty()) BillingState.UNAVAILABLE else BillingState.READY
        }
    }

    private fun onPurchases(r: BillingResult, purchases: List<Purchase>?) {
        when (r.responseCode) {
            BillingClient.BillingResponseCode.OK -> purchases.orEmpty().forEach(::handle)
            BillingClient.BillingResponseCode.USER_CANCELED -> state.value = BillingState.CANCELED
            BillingClient.BillingResponseCode.ITEM_ALREADY_OWNED -> restore()
            else -> { state.value = BillingState.ERROR; lastMessage.value = "purchase_error_${r.responseCode}" }
        }
    }

    private fun handle(p: Purchase) {
        when (p.purchaseState) {
            Purchase.PurchaseState.PENDING -> state.value = BillingState.PENDING
            Purchase.PurchaseState.PURCHASED -> {
                state.value = BillingState.VERIFYING
                if (!verify(p)) { state.value = BillingState.ERROR; lastMessage.value = "verification_failed"; return }
                val ids = owned.value + p.products.filter { it in Entitlements.PACKS }
                owned.value = ids; settings.cacheEntitlements(ids)
                if (!p.isAcknowledged) acknowledge(p)
                state.value = BillingState.GRANTED
            }
            else -> Unit
        }
    }

    private fun acknowledge(p: Purchase) {
        client.acknowledgePurchase(AcknowledgePurchaseParams.newBuilder().setPurchaseToken(p.purchaseToken).build()) { }
    }

    private fun verify(p: Purchase): Boolean {
        val key = BuildConfig.PLAY_LICENSE_KEY
        if (key.isBlank()) { lastMessage.value = "license_key_missing"; return false }
        return runCatching {
            val pub = KeyFactory.getInstance("RSA").generatePublic(X509EncodedKeySpec(Base64.decode(key, Base64.DEFAULT)))
            Signature.getInstance("SHA1withRSA").run { initVerify(pub); update(p.originalJson.toByteArray()); verify(Base64.decode(p.signature, Base64.DEFAULT)) }
        }.getOrDefault(false)
    }
}
