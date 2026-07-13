package com.engineeringood.athena.plugin

import com.engineeringood.athena.presentation.PresentationCompositePack
import com.engineeringood.athena.presentation.PresentationPrimitivePack

/**
 * Typed contract for plugins that publish governed presentation packs.
 *
 * Presentation packs stay downstream of semantic truth. They may define primitive and composite
 * presentation language, but they may not become engineering meaning or backend ownership.
 */
interface AthenaPresentationPackContributor : AthenaPlugin {
    /**
     * Primitive packs published by this plugin in deterministic plugin-owned order.
     */
    fun primitivePresentationPacks(): List<PresentationPrimitivePack> = emptyList()

    /**
     * Composite packs published by this plugin in deterministic plugin-owned order.
     */
    fun compositePresentationPacks(): List<PresentationCompositePack> = emptyList()
}
