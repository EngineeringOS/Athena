package com.engineeringood.athena.packageplatform

data class SymbolBounds(
    val width: Int,
    val height: Int,
) {
    init {
        require(width > 0 && height > 0)
    }
}

data class SymbolPoint(
    val x: Int,
    val y: Int,
)

enum class PortCompatibilityDirection { IN, OUT, BIDIRECTIONAL }
enum class PortCompatibilityDomain { ELECTRICAL, MECHANICAL, AUTOMATION, GENERIC }
enum class PortCompatibilityFlowKind { POWER, CONTROL, SIGNAL, FEEDBACK, FLUID, DATA, GENERIC }

data class PortCompatibilityContract(
    val key: String,
    val direction: PortCompatibilityDirection,
    val domain: PortCompatibilityDomain,
    val flowKind: PortCompatibilityFlowKind,
) {
    init {
        require(key.matches(Regex("^[a-z][a-z0-9]*(?:[._-][a-z0-9]+)*$")))
    }
}

data class SymbolAnchor(
    val key: String,
    val point: SymbolPoint,
    val compatibility: PortCompatibilityContract,
) {
    init {
        require(key == compatibility.key)
    }
}

data class SymbolGeometry(
    val resource: String,
    val viewBox: SymbolBounds,
    val center: SymbolPoint,
    val anchors: List<SymbolAnchor>,
    val rotationDegrees: Int = 0,
) {
    init {
        requirePackageRelativePath(resource)
        require(center.x in 0..viewBox.width && center.y in 0..viewBox.height)
        require(rotationDegrees in 0..359)
        require(anchors.isNotEmpty())
        require(anchors.map(SymbolAnchor::key).distinct().size == anchors.size)
        require(anchors.all { anchor ->
            anchor.point.x in 0..viewBox.width && anchor.point.y in 0..viewBox.height
        })
    }
}
