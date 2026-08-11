package com.engineeringood.athena.packageplatform

import java.security.MessageDigest

data class ElementInterfaceFingerprint(val value: String) {
    init { require(value.matches(Regex("^[0-9a-f]{64}$"))) }
}

object ElementInterfaceFingerprintFactory {
    fun create(composition: ElementComposition): ElementInterfaceFingerprint {
        val canonical = buildString {
            append("athena-element-interface-v1\n")
            composition.functionSlots.sortedBy { it.slotId }.forEach { append("slot=").append(it.slotId).append('|').append(it.role).append('\n') }
            composition.ports.filter { it.public }.sortedBy { it.portKey }.forEach {
                append("port=").append(it.portKey).append('|').append(it.functionSlotId).append('|').append(it.childItemId.key).append('|').append(it.anchorKey).append('\n')
            }
        }
        return ElementInterfaceFingerprint(MessageDigest.getInstance("SHA-256").digest(canonical.toByteArray()).joinToString("") { "%02x".format(it) })
    }
}

data class RepresentationVariant(
    val variantId: PackageItemIdentity,
    val elementId: PackageItemIdentity,
    val interfaceFingerprint: ElementInterfaceFingerprint,
    val payload: PackageItemValue.ObjectValue,
)

