package com.engineeringood.athena.physical

import com.engineeringood.athena.ir.StableSemanticIdentity

object PhysicalInstallationContractResolver {
    fun factsFromResolvedTrait(
        trait: ResolvedPhysicalTraitDefinition,
    ): List<PhysicalInstallationContractFact> = listOf(
        traitFact(
            subjectIdentity = trait.semanticSubjectId,
            field = PhysicalInstallationContractField.Width,
            value = PhysicalInstallationContractValue.LengthMillimeters(
                trait.definition.size.widthMillimeters,
            ),
            suffix = "size.width",
        ),
        traitFact(
            subjectIdentity = trait.semanticSubjectId,
            field = PhysicalInstallationContractField.Height,
            value = PhysicalInstallationContractValue.LengthMillimeters(
                trait.definition.size.heightMillimeters,
            ),
            suffix = "size.height",
        ),
        traitFact(
            subjectIdentity = trait.semanticSubjectId,
            field = PhysicalInstallationContractField.Depth,
            value = PhysicalInstallationContractValue.LengthMillimeters(
                trait.definition.size.depthMillimeters,
            ),
            suffix = "size.depth",
        ),
        traitFact(
            subjectIdentity = trait.semanticSubjectId,
            field = PhysicalInstallationContractField.MountingType,
            value = PhysicalInstallationContractValue.MountingType(trait.definition.mountingTypeId),
            suffix = "mounting",
        ),
    )

    fun resolve(
        subjectIdentity: StableSemanticIdentity,
        facts: List<PhysicalInstallationContractFact>,
    ): PhysicalInstallationContractResolution {
        val diagnostics = mutableListOf<PhysicalInstallationContractDiagnostic>()
        val selected = PhysicalInstallationContractField.entries.associateWith { field ->
            selectFact(subjectIdentity, field, facts, diagnostics)
        }

        val width = selected.positiveLength(subjectIdentity, PhysicalInstallationContractField.Width, diagnostics)
        val height = selected.positiveLength(subjectIdentity, PhysicalInstallationContractField.Height, diagnostics)
        val depth = selected.positiveLength(subjectIdentity, PhysicalInstallationContractField.Depth, diagnostics)
        val mountingType = selected.mountingType(subjectIdentity, PhysicalInstallationContractField.MountingType, diagnostics)
        val orientations = selected.orientations(
            subjectIdentity,
            PhysicalInstallationContractField.AllowedOrientations,
            diagnostics,
        )
        val clearanceTop = selected.nonNegativeLength(
            subjectIdentity,
            PhysicalInstallationContractField.ClearanceTop,
            diagnostics,
        )
        val clearanceRight = selected.nonNegativeLength(
            subjectIdentity,
            PhysicalInstallationContractField.ClearanceRight,
            diagnostics,
        )
        val clearanceBottom = selected.nonNegativeLength(
            subjectIdentity,
            PhysicalInstallationContractField.ClearanceBottom,
            diagnostics,
        )
        val clearanceLeft = selected.nonNegativeLength(
            subjectIdentity,
            PhysicalInstallationContractField.ClearanceLeft,
            diagnostics,
        )
        val containers = selected.containerKinds(
            subjectIdentity,
            PhysicalInstallationContractField.CompatibleContainerKinds,
            diagnostics,
        )

        if (diagnostics.isNotEmpty()) {
            return PhysicalInstallationContractResolution.Failure(diagnostics.sortedWith(diagnosticOrder))
        }

        return PhysicalInstallationContractResolution.Success(
            PhysicalInstallationContractV0(
                subjectIdentity = subjectIdentity,
                size = PhysicalInstallationSizeV0(
                    width = requireNotNull(width?.value),
                    height = requireNotNull(height?.value),
                    depth = requireNotNull(depth?.value),
                ),
                mountingTypeId = requireNotNull(mountingType?.value),
                allowedOrientations = requireNotNull(orientations?.value).toSortedSet(),
                clearance = PhysicalInstallationClearanceV0(
                    top = requireNotNull(clearanceTop?.value),
                    right = requireNotNull(clearanceRight?.value),
                    bottom = requireNotNull(clearanceBottom?.value),
                    left = requireNotNull(clearanceLeft?.value),
                ),
                compatibleContainerKinds = requireNotNull(containers?.value).toSortedSet(),
                provenance = PhysicalInstallationContractProvenanceV0(
                    width = width.provenance(),
                    height = height.provenance(),
                    depth = depth.provenance(),
                    mountingType = mountingType.provenance(),
                    allowedOrientations = orientations.provenance(),
                    clearanceTop = clearanceTop.provenance(),
                    clearanceRight = clearanceRight.provenance(),
                    clearanceBottom = clearanceBottom.provenance(),
                    clearanceLeft = clearanceLeft.provenance(),
                    compatibleContainerKinds = containers.provenance(),
                ),
            ),
        )
    }

    private fun traitFact(
        subjectIdentity: StableSemanticIdentity,
        field: PhysicalInstallationContractField,
        value: PhysicalInstallationContractValue,
        suffix: String,
    ): PhysicalInstallationContractFact = PhysicalInstallationContractFact(
        field = field,
        value = value,
        source = PhysicalContractSource(
            kind = PhysicalContractSourceKind.Trait,
            id = "trait:${subjectIdentity.value}:$suffix",
        ),
    )
}

private data class ResolvedFact<T>(
    val value: T,
    val provenance: PhysicalInstallationContractFieldProvenance,
)

private fun ResolvedFact<*>?.provenance(): PhysicalInstallationContractFieldProvenance =
    requireNotNull(this).provenance

private val diagnosticOrder = compareBy<PhysicalInstallationContractDiagnostic>(
    { it.field.ordinal },
    { it.code },
    { it.source?.kind?.ordinal ?: -1 },
    { it.source?.id.orEmpty() },
)

private fun selectFact(
    subjectIdentity: StableSemanticIdentity,
    field: PhysicalInstallationContractField,
    facts: List<PhysicalInstallationContractFact>,
    diagnostics: MutableList<PhysicalInstallationContractDiagnostic>,
): PhysicalInstallationContractFact? {
    val fieldFacts = facts.filter { fact -> fact.field == field }
    val projectFacts = fieldFacts.filter { fact -> fact.source.kind == PhysicalContractSourceKind.Project }
    val traitFacts = fieldFacts.filter { fact -> fact.source.kind == PhysicalContractSourceKind.Trait }
    val selectedFacts = when {
        projectFacts.isNotEmpty() -> projectFacts
        traitFacts.isNotEmpty() -> traitFacts
        else -> emptyList()
    }

    if (selectedFacts.isEmpty()) {
        diagnostics += diagnostic(
            code = "physical.contract.missing",
            subjectIdentity = subjectIdentity,
            field = field,
            expected = "required contract field",
        )
        return null
    }

    if (selectedFacts.size > 1) {
        diagnostics += diagnostic(
            code = "physical.contract.ambiguous",
            subjectIdentity = subjectIdentity,
            field = field,
            source = selectedFacts.first().source,
            span = selectedFacts.first().span,
            measured = selectedFacts.joinToString(",") { fact -> fact.source.id },
            expected = "one ${selectedFacts.first().source.kind.name.lowercase()} value",
        )
        return null
    }

    return selectedFacts.single()
}

private fun Map<PhysicalInstallationContractField, PhysicalInstallationContractFact?>.positiveLength(
    subjectIdentity: StableSemanticIdentity,
    field: PhysicalInstallationContractField,
    diagnostics: MutableList<PhysicalInstallationContractDiagnostic>,
): ResolvedFact<PhysicalPositiveMillimeters>? {
    val fact = this[field] ?: return null
    val length = fact.value as? PhysicalInstallationContractValue.LengthMillimeters
    if (length == null) {
        diagnostics += typeDiagnostic(subjectIdentity, field, fact, "length millimetres")
        return null
    }
    val validated = PhysicalPositiveMillimeters.from(length.value)
    if (validated == null) {
        diagnostics += diagnostic(
            code = "physical.contract.dimension.non_positive",
            subjectIdentity = subjectIdentity,
            field = field,
            source = fact.source,
            span = fact.span,
            measured = length.value.toString(),
            expected = "positive millimetres",
        )
        return null
    }
    return ResolvedFact(validated, fact.provenance())
}

private fun Map<PhysicalInstallationContractField, PhysicalInstallationContractFact?>.nonNegativeLength(
    subjectIdentity: StableSemanticIdentity,
    field: PhysicalInstallationContractField,
    diagnostics: MutableList<PhysicalInstallationContractDiagnostic>,
): ResolvedFact<PhysicalNonNegativeMillimeters>? {
    val fact = this[field] ?: return null
    val length = fact.value as? PhysicalInstallationContractValue.LengthMillimeters
    if (length == null) {
        diagnostics += typeDiagnostic(subjectIdentity, field, fact, "length millimetres")
        return null
    }
    val validated = PhysicalNonNegativeMillimeters.from(length.value)
    if (validated == null) {
        diagnostics += diagnostic(
            code = "physical.contract.clearance.negative",
            subjectIdentity = subjectIdentity,
            field = field,
            source = fact.source,
            span = fact.span,
            measured = length.value.toString(),
            expected = "non-negative millimetres",
        )
        return null
    }
    return ResolvedFact(validated, fact.provenance())
}

private fun Map<PhysicalInstallationContractField, PhysicalInstallationContractFact?>.mountingType(
    subjectIdentity: StableSemanticIdentity,
    field: PhysicalInstallationContractField,
    diagnostics: MutableList<PhysicalInstallationContractDiagnostic>,
): ResolvedFact<PhysicalMountingTypeId>? {
    val fact = this[field] ?: return null
    val mountingType = fact.value as? PhysicalInstallationContractValue.MountingType
    if (mountingType == null) {
        diagnostics += typeDiagnostic(subjectIdentity, field, fact, "mounting type id")
        return null
    }
    return ResolvedFact(mountingType.value, fact.provenance())
}

private fun Map<PhysicalInstallationContractField, PhysicalInstallationContractFact?>.orientations(
    subjectIdentity: StableSemanticIdentity,
    field: PhysicalInstallationContractField,
    diagnostics: MutableList<PhysicalInstallationContractDiagnostic>,
): ResolvedFact<Set<PhysicalInstallationOrientation>>? {
    val fact = this[field] ?: return null
    val orientations = fact.value as? PhysicalInstallationContractValue.Orientations
    if (orientations == null) {
        diagnostics += typeDiagnostic(subjectIdentity, field, fact, "orientation set")
        return null
    }
    if (orientations.values.isEmpty()) {
        diagnostics += diagnostic(
            code = "physical.contract.empty_set",
            subjectIdentity = subjectIdentity,
            field = field,
            source = fact.source,
            span = fact.span,
            expected = "non-empty orientation set",
        )
        return null
    }
    return ResolvedFact(orientations.values.toSortedSet(), fact.provenance())
}

private fun Map<PhysicalInstallationContractField, PhysicalInstallationContractFact?>.containerKinds(
    subjectIdentity: StableSemanticIdentity,
    field: PhysicalInstallationContractField,
    diagnostics: MutableList<PhysicalInstallationContractDiagnostic>,
): ResolvedFact<Set<PhysicalContainerKindId>>? {
    val fact = this[field] ?: return null
    val containers = fact.value as? PhysicalInstallationContractValue.ContainerKinds
    if (containers == null) {
        diagnostics += typeDiagnostic(subjectIdentity, field, fact, "container kind set")
        return null
    }
    if (containers.values.isEmpty()) {
        diagnostics += diagnostic(
            code = "physical.contract.empty_set",
            subjectIdentity = subjectIdentity,
            field = field,
            source = fact.source,
            span = fact.span,
            expected = "non-empty container kind set",
        )
        return null
    }
    return ResolvedFact(containers.values.toSortedSet(), fact.provenance())
}

private fun PhysicalInstallationContractFact.provenance(): PhysicalInstallationContractFieldProvenance =
    PhysicalInstallationContractFieldProvenance(
        field = field,
        source = source,
        span = span,
    )

private fun typeDiagnostic(
    subjectIdentity: StableSemanticIdentity,
    field: PhysicalInstallationContractField,
    fact: PhysicalInstallationContractFact,
    expected: String,
): PhysicalInstallationContractDiagnostic = diagnostic(
    code = "physical.contract.type_mismatch",
    subjectIdentity = subjectIdentity,
    field = field,
    source = fact.source,
    span = fact.span,
    measured = fact.value::class.simpleName,
    expected = expected,
)

private fun diagnostic(
    code: String,
    subjectIdentity: StableSemanticIdentity,
    field: PhysicalInstallationContractField,
    source: PhysicalContractSource? = null,
    span: PhysicalSourceSpan? = null,
    measured: String? = null,
    expected: String,
): PhysicalInstallationContractDiagnostic = PhysicalInstallationContractDiagnostic(
    code = code,
    subjectIdentity = subjectIdentity,
    field = field,
    source = source,
    span = span,
    measured = measured,
    expected = expected,
)
