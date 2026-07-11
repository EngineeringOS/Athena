package com.engineeringood.athena.ir

/**
 * Canonical kernel-owned container for one deterministic derived engineering context snapshot.
 *
 * The context sits above canonical `Engineering IR` and below later capability-fact, rule, and diagnostic layers.
 */
data class DerivedEngineeringContext(
    val subjects: List<DerivedEngineeringSubjectContext>,
) {
    companion object {
        /**
         * Builds a deterministic context snapshot by sorting subjects and their nested collections in stable order.
         */
        fun canonical(subjects: List<DerivedEngineeringSubjectContext>): DerivedEngineeringContext {
            return DerivedEngineeringContext(
                subjects = subjects
                    .sortedBy { subject -> subject.subjectIdentity.value }
                    .map { subject -> subject.canonical() },
            )
        }
    }
}

/**
 * Derived engineering context scoped to one canonical semantic subject.
 */
data class DerivedEngineeringSubjectContext(
    val subjectIdentity: StableSemanticIdentity,
    val inputs: List<DerivedEngineeringInput>,
    val derivedValues: List<DerivedEngineeringValue>,
)

/**
 * Narrow governed electrical input kinds allowed in the first M9 proof slice.
 */
enum class DerivedEngineeringInputKind {
    MOTOR_POWER,
    VOLTAGE,
    POWER_FACTOR,
    EFFICIENCY,
    BREAKER_RATED_CURRENT,
    CABLE_ALLOWED_CURRENT,
    RELAY_RATED_CURRENT,
}

/**
 * Traceability record for one authored input preserved into derived engineering context.
 */
data class DerivedEngineeringInputTrace(
    val subjectIdentity: StableSemanticIdentity,
    val propertyName: String,
    val provenance: SourceProvenance,
)

/**
 * One authored, governed input value that may later feed derived engineering context formulas.
 */
data class DerivedEngineeringInput(
    val kind: DerivedEngineeringInputKind,
    val trace: DerivedEngineeringInputTrace,
    val authoredValue: EngineeringPropertyValue,
)

/**
 * Narrow first-wave derived value kinds published for the initial electrical proof slice.
 */
enum class DerivedEngineeringValueKind {
    FULL_LOAD_CURRENT,
    STARTING_CURRENT,
    THERMAL_LOAD,
}

/**
 * Typed scalar quantity carried by one derived engineering value.
 */
sealed interface DerivedEngineeringQuantity {
    /**
     * Decimal-like quantity whose numeric text and optional unit symbol remain kernel-inspectable.
     */
    data class Decimal(
        val canonicalText: String,
        val unitSymbol: String? = null,
    ) : DerivedEngineeringQuantity
}

/**
 * Traceability record for one derived engineering value.
 *
 * Derived values do not point directly to authored source text. They remain explainable through the governed
 * authored inputs that produced them.
 */
data class DerivedEngineeringValueTrace(
    val sourceInputs: List<DerivedEngineeringInputTrace>,
)

/**
 * One typed derived engineering value published above canonical state and below later capability facts.
 */
data class DerivedEngineeringValue(
    val kind: DerivedEngineeringValueKind,
    val subjectIdentity: StableSemanticIdentity,
    val quantity: DerivedEngineeringQuantity,
    val trace: DerivedEngineeringValueTrace,
)

private fun DerivedEngineeringSubjectContext.canonical(): DerivedEngineeringSubjectContext {
    return copy(
        inputs = inputs.sortedWith(
            compareBy<DerivedEngineeringInput>(
                { input -> input.kind.ordinal },
                { input -> input.trace.propertyName },
                { input -> input.trace.provenance.file },
                { input -> input.trace.provenance.startLine },
                { input -> input.trace.provenance.startColumn },
            ),
        ),
        derivedValues = derivedValues.sortedWith(
            compareBy<DerivedEngineeringValue>(
                { value -> value.kind.ordinal },
                { value -> value.subjectIdentity.value },
                { value -> value.quantity.sortKey() },
                { value -> value.trace.sortKey() },
            ),
        ),
    )
}

private fun DerivedEngineeringQuantity.sortKey(): String {
    return when (this) {
        is DerivedEngineeringQuantity.Decimal -> "${canonicalText}:${unitSymbol.orEmpty()}"
    }
}

private fun DerivedEngineeringValueTrace.sortKey(): String {
    return sourceInputs
        .sortedWith(
            compareBy<DerivedEngineeringInputTrace>(
                { trace -> trace.subjectIdentity.value },
                { trace -> trace.propertyName },
                { trace -> trace.provenance.file },
                { trace -> trace.provenance.startLine },
                { trace -> trace.provenance.startColumn },
            ),
        )
        .joinToString(separator = "|") { trace ->
            "${trace.subjectIdentity.value}:${trace.propertyName}:${trace.provenance.file}:${trace.provenance.startLine}:${trace.provenance.startColumn}"
        }
}
