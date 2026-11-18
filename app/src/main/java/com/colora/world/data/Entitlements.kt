package com.colora.world.data

import android.app.Activity
import android.app.Application
import kotlinx.coroutines.flow.StateFlow

/** Billing states from spec §19.3. */
enum class BillingState { UNAVAILABLE, LOADING_PRODUCTS, READY, PURCHASE_IN_PROGRESS, PENDING, VERIFYING, GRANTED, CANCELED, ERROR, RESTORE_IN_PROGRESS }

/** A product exactly as Google Play returned it (title and localised price are Play data, never hard-coded). */
data class ProductInfo(val productId: String, val title: String, val description: String, val formattedPrice: String)

/**
 * Who may create new artwork from which template. Existing artwork is always
 * viewable, playable, exportable and backed up regardless of entitlement.
 */
interface Entitlements {
    val allUnlocked: Boolean
    val owned: StateFlow<Set<String>>
    val state: StateFlow<BillingState>
    val products: StateFlow<List<ProductInfo>>
    val lastMessage: StateFlow<String?>

    fun hasPack(packId: String): Boolean = allUnlocked || packId == "base" || packId in owned.value
    fun connect() {}
    fun purchase(activity: Activity, productId: String) {}
    fun restore() {}

    companion object {
        const val NATURE = "pack_nature_plus"
        const val EVERYDAY = "pack_everyday_plus"
        const val IMAGINATION = "pack_imagination_plus"
        val PACKS = listOf(NATURE, EVERYDAY, IMAGINATION)
        fun create(app: Application, settings: Settings): Entitlements = createEntitlements(app, settings)
    }
}
