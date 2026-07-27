package com.engineeringood.athena.representation

@JvmInline
value class DrawingProofId(val value: String) {
    init {
        require(value.isNotBlank()) { "Drawing proof id must not be blank." }
    }
}

@JvmInline
value class DrawingProofFactId(val value: String) {
    init {
        require(value.isNotBlank()) { "Drawing proof fact id must not be blank." }
    }
}

@JvmInline
value class DrawingProofSchemaVersion(val value: String) {
    init {
        require(value.isNotBlank()) { "Drawing proof schema version must not be blank." }
    }

    fun isSupported(): Boolean = value == "1.0"
}

@JvmInline
value class DrawingDiagnosticCode(val value: String) {
    init {
        require(value.isNotBlank()) { "Drawing diagnostic code must not be blank." }
    }
}

enum class DrawingProofAuthority(val wireValue: String) {
    SYMBOL_ANATOMY("symbol-anatomy"),
    GRAPHIC_PRIMITIVE_IR("graphic-primitive-ir"),
    SHEET_COMPOSITION("sheet-composition"),
    ROUTE_ANCHOR("route-anchor"),
    RENDERER_ADAPTER("renderer-adapter"),
    WORKBENCH_CHROME("workbench-chrome"),
    PACKAGE_BINDING("package-binding"),
}

enum class DrawingAcceptanceAuthority(val wireValue: String) {
    STRUCTURED_PROOF("structured-proof"),
}

enum class DrawingProofStatus {
    PASS,
    FAIL,
}

enum class DrawingDiagnosticSeverity {
    INFO,
    WARNING,
    ERROR,
}

data class DrawingDiagnostic(
    val code: DrawingDiagnosticCode,
    val severity: DrawingDiagnosticSeverity,
    val authority: DrawingProofAuthority,
    val subject: String,
    val message: String,
) {
    init {
        require(subject.isNotBlank()) { "Drawing diagnostic subject must not be blank." }
        require(message.isNotBlank()) { "Drawing diagnostic message must not be blank." }
    }

    fun toTransportPayload(): DrawingDiagnosticTransportPayload = DrawingDiagnosticTransportPayload(
        code = code.value,
        severity = severity.name.lowercase(),
        authority = authority.wireValue,
        subject = subject,
        message = message,
    )
}

data class DrawingProofFact(
    val factId: DrawingProofFactId,
    val authority: DrawingProofAuthority,
    val status: DrawingProofStatus,
    val subject: String,
    val evidence: Map<String, String>,
) {
    init {
        require(subject.isNotBlank()) { "Drawing proof fact subject must not be blank." }
    }

    fun toTransportPayload(): DrawingProofFactTransportPayload = DrawingProofFactTransportPayload(
        factId = factId.value,
        authority = authority.wireValue,
        status = status.name.lowercase(),
        subject = subject,
        evidence = evidence.toSortedMap(),
    )
}

data class DrawingProofPayload(
    val proofId: DrawingProofId,
    val schemaVersion: DrawingProofSchemaVersion,
    val acceptanceAuthority: DrawingAcceptanceAuthority,
    val facts: List<DrawingProofFact>,
    val diagnostics: List<DrawingDiagnostic>,
) {
    val requiredAuthorities: Set<DrawingProofAuthority>
        get() = DrawingProofAuthority.entries.toSet()

    val isAccepted: Boolean
        get() = schemaVersion.isSupported() &&
            acceptanceAuthority == DrawingAcceptanceAuthority.STRUCTURED_PROOF &&
            requiredAuthorities.isNotEmpty() &&
            facts.isNotEmpty() &&
            facts.all { it.status == DrawingProofStatus.PASS } &&
            facts.all { it.evidence.isNotEmpty() } &&
            facts.map { it.factId }.distinct().size == facts.size &&
            facts.filter { it.status == DrawingProofStatus.PASS }.map { it.authority }.toSet().containsAll(requiredAuthorities) &&
            diagnostics.none { it.severity == DrawingDiagnosticSeverity.ERROR }

    fun toTransportPayload(): DrawingProofTransportPayload = DrawingProofTransportPayload(
        proofId = proofId.value,
        schemaVersion = schemaVersion.value,
        acceptanceAuthority = acceptanceAuthority.wireValue,
        accepted = isAccepted,
        requiredAuthorities = requiredAuthorities.map { it.wireValue }.sorted(),
        facts = facts
            .sortedWith(compareBy({ it.authority.wireValue }, { it.factId.value }, { it.subject }, { it.status.name }))
            .map { it.toTransportPayload() },
        diagnostics = diagnostics
            .sortedWith(compareBy({ it.authority.wireValue }, { it.code.value }, { it.severity.name }, { it.subject }, { it.message }))
            .map { it.toTransportPayload() },
    )
}

data class DrawingProofTransportPayload(
    val proofId: String,
    val schemaVersion: String,
    val acceptanceAuthority: String,
    val accepted: Boolean,
    val requiredAuthorities: List<String>,
    val facts: List<DrawingProofFactTransportPayload>,
    val diagnostics: List<DrawingDiagnosticTransportPayload>,
)

data class DrawingProofFactTransportPayload(
    val factId: String,
    val authority: String,
    val status: String,
    val subject: String,
    val evidence: Map<String, String>,
)

data class DrawingDiagnosticTransportPayload(
    val code: String,
    val severity: String,
    val authority: String,
    val subject: String,
    val message: String,
)

object DrawingDiagnosticMapper {
    fun from(diagnostic: DrawingSymbolDiagnostic): DrawingDiagnostic = DrawingDiagnostic(
        code = DrawingDiagnosticCode(diagnostic.code.wireValue),
        severity = DrawingDiagnosticSeverity.ERROR,
        authority = DrawingProofAuthority.SYMBOL_ANATOMY,
        subject = diagnostic.subject,
        message = diagnostic.message,
    )

    fun from(diagnostic: GraphicPrimitiveDiagnostic): DrawingDiagnostic = DrawingDiagnostic(
        code = DrawingDiagnosticCode(diagnostic.code.wireValue),
        severity = DrawingDiagnosticSeverity.ERROR,
        authority = DrawingProofAuthority.GRAPHIC_PRIMITIVE_IR,
        subject = diagnostic.subject,
        message = diagnostic.message,
    )
}
