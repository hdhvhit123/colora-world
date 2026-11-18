package com.colora.world.data

import android.app.Application
import kotlinx.coroutines.flow.MutableStateFlow

/** offlineFull handover build: every pack is unlocked, there is no billing code and no INTERNET permission. */
internal fun createEntitlements(app: Application, settings: Settings): Entitlements = object : Entitlements {
    override val allUnlocked = true
    override val owned = MutableStateFlow(Entitlements.PACKS.toSet())
    override val state = MutableStateFlow(BillingState.UNAVAILABLE)
    override val products = MutableStateFlow(emptyList<ProductInfo>())
    override val lastMessage = MutableStateFlow<String?>(null)
}
