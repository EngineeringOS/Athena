package com.engineeringood.athena.scm

import com.engineeringood.athena.ir.EngineeringConnection
import com.engineeringood.athena.ir.EngineeringDocument
import com.engineeringood.athena.ir.EngineeringPort
import com.engineeringood.athena.ir.EngineeringProperty
import com.engineeringood.athena.ir.EngineeringPropertyValue
import com.engineeringood.athena.ir.SourceProvenance
import com.engineeringood.athena.repository.PackageDependency
import com.engineeringood.athena.repository.PackageIdentifier
import com.engineeringood.athena.repository.RepositoryGraphReport
import com.engineeringood.athena.repository.RepositoryLock
import com.engineeringood.athena.repository.RepositoryManifest
import com.engineeringood.athena.repository.ResolvedPackageGraph
import com.engineeringood.athena.semantics.core.SemanticContinuationDecision
import com.engineeringood.athena.semantics.core.SemanticDiagnostic
import com.engineeringood.athena.semantics.core.SemanticDiagnosticCategory
import com.engineeringood.athena.semantics.core.SemanticDiagnosticSeverity
import com.engineeringood.athena.semantics.core.SemanticRuleId
import com.engineeringood.athena.semantics.core.SemanticValidationResult

/**
 * Deterministic semantic diff calculator that compares one baseline snapshot with one current
 * semantic snapshot.
 *
 * The calculator speaks only in Athena semantic contracts. It does not inspect raw file diffs or
 * vendor SCM primitives when classifying repository, package, and engineering change.
 */
class SemanticDiffCalculator {
    /** Produces one deterministic semantic diff between the supplied baseline and current state. */
    fun calculate(
        baseline: SemanticBaselineSnapshot,
        current: SemanticBaselineSnapshot,
    ): SemanticDiff {
        val currentPrimaryPackage = current.repositoryReport.repository.manifest.primaryPackage.id

        val authoredChanges = buildList {
            addAll(compareRepositoryManifest(
                baseline = baseline.repositoryReport.repository.manifest,
                current = current.repositoryReport.repository.manifest,
                currentPrimaryPackage = currentPrimaryPackage,
            ))
            addAll(compareEngineeringDocuments(
                baseline = baseline.engineeringDocuments.firstOrNull(),
                current = current.engineeringDocuments.firstOrNull(),
                currentPrimaryPackage = currentPrimaryPackage,
            ))
        }.distinct().sortedWith(changeRecordComparator())

        val derivedConsequences = buildList {
            compareRepositoryContractImpact(
                authoredChanges = authoredChanges,
                currentPrimaryPackage = currentPrimaryPackage,
            )?.let(::add)
            compareCanonicalLock(
                baseline = baseline.repositoryReport.repository.lock,
                current = current.repositoryReport.repository.lock,
                currentPrimaryPackage = currentPrimaryPackage,
            )?.let(::add)
            compareResolvedGraph(
                baseline = baseline.repositoryReport.graph,
                current = current.repositoryReport.graph,
                currentPrimaryPackage = currentPrimaryPackage,
            )?.let(::add)
            compareValidationConsequences(
                baseline = baseline,
                current = current,
                currentPrimaryPackage = currentPrimaryPackage,
            ).forEach(::add)
            compareInputCompletenessConsequences(
                baseline = baseline,
                current = current,
                currentPrimaryPackage = currentPrimaryPackage,
            ).forEach(::add)
        }.distinct().sortedWith(derivedConsequenceComparator())

        return SemanticDiff(
            baseline = baseline.descriptor,
            snapshot = current,
            authoredChanges = authoredChanges,
            derivedConsequences = derivedConsequences,
        )
    }

    private fun compareRepositoryManifest(
        baseline: RepositoryManifest,
        current: RepositoryManifest,
        currentPrimaryPackage: PackageIdentifier,
    ): List<SemanticChangeRecord> {
        val changes = mutableListOf<SemanticChangeRecord>()

        if (baseline.primaryPackage != current.primaryPackage) {
            changes += SemanticChangeRecord(
                category = SemanticChangeCategory.REPOSITORY_CONTRACT_CHANGED,
                layer = SemanticChangeLayer.REPOSITORY,
                message = "Primary package contract changed.",
                affectedPackage = currentPrimaryPackage,
                metadata = mapOf(
                    "before" to "${baseline.primaryPackage.id.name}@${baseline.primaryPackage.sourceRoot}",
                    "after" to "${current.primaryPackage.id.name}@${current.primaryPackage.sourceRoot}",
                ),
            )
        }

        val baselineDependencies = baseline.dependencies.map(::dependencyKey).toSet()
        val currentDependencies = current.dependencies.map(::dependencyKey).toSet()

        (currentDependencies - baselineDependencies)
            .sortedBy { dependency -> dependency.packageId.name }
            .forEach { dependency ->
                changes += SemanticChangeRecord(
                    category = SemanticChangeCategory.PACKAGE_DEPENDENCY_CHANGED,
                    layer = SemanticChangeLayer.PACKAGE,
                    message = "Package dependency added: ${dependency.packageId.name}.",
                    affectedPackage = dependency.packageId,
                    metadata = dependency.metadata(),
                )
            }

        (baselineDependencies - currentDependencies)
            .sortedBy { dependency -> dependency.packageId.name }
            .forEach { dependency ->
                changes += SemanticChangeRecord(
                    category = SemanticChangeCategory.PACKAGE_DEPENDENCY_CHANGED,
                    layer = SemanticChangeLayer.PACKAGE,
                    message = "Package dependency removed: ${dependency.packageId.name}.",
                    affectedPackage = dependency.packageId,
                    metadata = dependency.metadata(),
                )
            }

        return changes
    }

    private fun compareEngineeringDocuments(
        baseline: EngineeringDocument?,
        current: EngineeringDocument?,
        currentPrimaryPackage: PackageIdentifier,
    ): List<SemanticChangeRecord> {
        if (baseline == null || current == null) {
            return emptyList()
        }

        val changes = mutableListOf<SemanticChangeRecord>()
        val baselineComponents = baseline.components.associateBy { component -> component.id.value }
        val currentComponents = current.components.associateBy { component -> component.id.value }
        val baselinePorts = baseline.ports.associateBy { port -> port.id.value }
        val currentPorts = current.ports.associateBy { port -> port.id.value }
        val baselineConnections = baseline.connections.associateBy { connection -> connection.id.value }
        val currentConnections = current.connections.associateBy { connection -> connection.id.value }

        (baselineComponents.keys + currentComponents.keys)
            .toSortedSet()
            .forEach { componentId ->
                val baselineComponent = baselineComponents[componentId]
                val currentComponent = currentComponents[componentId]

                when {
                    baselineComponent == null && currentComponent != null -> changes += SemanticChangeRecord(
                        category = SemanticChangeCategory.ENGINEERING_STRUCTURE_CHANGED,
                        layer = SemanticChangeLayer.ENGINEERING,
                        message = "Component added: ${currentComponent.name}.",
                        affectedPackage = currentPrimaryPackage,
                        subjectIdentity = currentComponent.id,
                        provenance = currentComponent.provenance,
                    )

                    baselineComponent != null && currentComponent == null -> changes += SemanticChangeRecord(
                        category = SemanticChangeCategory.ENGINEERING_STRUCTURE_CHANGED,
                        layer = SemanticChangeLayer.ENGINEERING,
                        message = "Component removed: ${baselineComponent.name}.",
                        affectedPackage = currentPrimaryPackage,
                        subjectIdentity = baselineComponent.id,
                        provenance = baselineComponent.provenance,
                    )

                    baselineComponent != null && currentComponent != null -> {
                        if (baselineComponent.name != currentComponent.name || baselineComponent.kind != currentComponent.kind) {
                            changes += SemanticChangeRecord(
                                category = SemanticChangeCategory.ENGINEERING_STRUCTURE_CHANGED,
                                layer = SemanticChangeLayer.ENGINEERING,
                                message = "Component structure changed: ${currentComponent.name}.",
                                affectedPackage = currentPrimaryPackage,
                                subjectIdentity = currentComponent.id,
                                provenance = currentComponent.provenance,
                            )
                        }
                        if (normalizeProperties(baselineComponent.properties) != normalizeProperties(currentComponent.properties)) {
                            changes += SemanticChangeRecord(
                                category = SemanticChangeCategory.ENGINEERING_PROPERTY_CHANGED,
                                layer = SemanticChangeLayer.ENGINEERING,
                                message = "Component properties changed: ${currentComponent.name}.",
                                affectedPackage = currentPrimaryPackage,
                                subjectIdentity = currentComponent.id,
                                provenance = currentComponent.provenance,
                            )
                        }
                    }
                }
            }

        (baselinePorts.keys + currentPorts.keys)
            .toSortedSet()
            .forEach { portId ->
                val baselinePort = baselinePorts[portId]
                val currentPort = currentPorts[portId]

                when {
                    baselinePort == null && currentPort != null -> changes += SemanticChangeRecord(
                        category = SemanticChangeCategory.ENGINEERING_STRUCTURE_CHANGED,
                        layer = SemanticChangeLayer.ENGINEERING,
                        message = "Port added: ${currentPort.ownerReference.authoredPath.joinToString(".")}.${currentPort.name}.",
                        affectedPackage = currentPrimaryPackage,
                        subjectIdentity = currentPort.id,
                        provenance = currentPort.provenance,
                    )

                    baselinePort != null && currentPort == null -> changes += SemanticChangeRecord(
                        category = SemanticChangeCategory.ENGINEERING_STRUCTURE_CHANGED,
                        layer = SemanticChangeLayer.ENGINEERING,
                        message = "Port removed: ${baselinePort.ownerReference.authoredPath.joinToString(".")}.${baselinePort.name}.",
                        affectedPackage = currentPrimaryPackage,
                        subjectIdentity = baselinePort.id,
                        provenance = baselinePort.provenance,
                    )

                    baselinePort != null && currentPort != null &&
                        normalizePort(baselinePort) != normalizePort(currentPort) -> changes += SemanticChangeRecord(
                        category = SemanticChangeCategory.ENGINEERING_PROPERTY_CHANGED,
                        layer = SemanticChangeLayer.ENGINEERING,
                        message = "Port properties changed: ${currentPort.ownerReference.authoredPath.joinToString(".")}.${currentPort.name}.",
                        affectedPackage = currentPrimaryPackage,
                        subjectIdentity = currentPort.id,
                        provenance = currentPort.provenance,
                    )
                }
            }

        (baselineConnections.keys + currentConnections.keys)
            .toSortedSet()
            .forEach { connectionId ->
                val baselineConnection = baselineConnections[connectionId]
                val currentConnection = currentConnections[connectionId]

                when {
                    baselineConnection == null && currentConnection != null -> changes += connectionChangeRecord(
                        message = "Connection added: ${connectionSummary(currentConnection)}.",
                        currentPrimaryPackage = currentPrimaryPackage,
                        connection = currentConnection,
                    )

                    baselineConnection != null && currentConnection == null -> changes += connectionChangeRecord(
                        message = "Connection removed: ${connectionSummary(baselineConnection)}.",
                        currentPrimaryPackage = currentPrimaryPackage,
                        connection = baselineConnection,
                    )

                    baselineConnection != null &&
                        currentConnection != null &&
                        normalizeConnection(baselineConnection) != normalizeConnection(currentConnection) -> changes += connectionChangeRecord(
                        message = "Connection topology changed: ${connectionSummary(currentConnection)}.",
                        currentPrimaryPackage = currentPrimaryPackage,
                        connection = currentConnection,
                    )
                }
            }

        return changes
    }

    private fun compareCanonicalLock(
        baseline: RepositoryLock?,
        current: RepositoryLock?,
        currentPrimaryPackage: PackageIdentifier,
    ): SemanticDerivedConsequence? {
        if (baseline == current) {
            return null
        }

        return SemanticDerivedConsequence(
            type = SemanticDerivedConsequenceType.LOCK_UPDATED,
            message = "Canonical repository lock changed.",
            affectedPackage = currentPrimaryPackage,
        )
    }

    private fun compareResolvedGraph(
        baseline: ResolvedPackageGraph?,
        current: ResolvedPackageGraph?,
        currentPrimaryPackage: PackageIdentifier,
    ): SemanticDerivedConsequence? {
        if (baseline == current) {
            return null
        }

        return SemanticDerivedConsequence(
            type = SemanticDerivedConsequenceType.PACKAGE_GRAPH_RECOMPUTED,
            message = "Resolved package graph changed.",
            affectedPackage = currentPrimaryPackage,
        )
    }

    private fun compareRepositoryContractImpact(
        authoredChanges: List<SemanticChangeRecord>,
        currentPrimaryPackage: PackageIdentifier,
    ): SemanticDerivedConsequence? {
        val repositoryContractChange = authoredChanges.firstOrNull { change ->
            change.category == SemanticChangeCategory.REPOSITORY_CONTRACT_CHANGED
        } ?: return null

        return SemanticDerivedConsequence(
            type = SemanticDerivedConsequenceType.REPOSITORY_CONTRACT_REVIEW_REQUIRED,
            message = "Repository contract impact requires downstream review.",
            affectedPackage = currentPrimaryPackage,
            metadata = repositoryContractChange.metadata,
        )
    }

    private fun compareValidationConsequences(
        baseline: SemanticBaselineSnapshot,
        current: SemanticBaselineSnapshot,
        currentPrimaryPackage: PackageIdentifier,
    ): List<SemanticDerivedConsequence> {
        val baselineValidation = baseline.validationResult
        val currentValidation = current.validationResult

        if (baselineValidation == null || currentValidation == null) {
            return emptyList()
        }
        if (baselineValidation == currentValidation) {
            return emptyList()
        }

        return listOf(
            SemanticDerivedConsequence(
                type = SemanticDerivedConsequenceType.VALIDATION_DELTA_DETECTED,
                message = "Validation outcome changed between the baseline and current repository state.",
                affectedPackage = currentPrimaryPackage,
                metadata = validationDeltaMetadata(
                    baseline = baselineValidation,
                    current = currentValidation,
                ),
            ),
        )
    }

    private fun compareInputCompletenessConsequences(
        baseline: SemanticBaselineSnapshot,
        current: SemanticBaselineSnapshot,
        currentPrimaryPackage: PackageIdentifier,
    ): List<SemanticDerivedConsequence> {
        val consequences = mutableListOf<SemanticDerivedConsequence>()

        consequences += baseline.diagnostics.map { diagnostic ->
            incompleteComparisonConsequence(
                message = "Baseline comparison input is incomplete: ${diagnostic.message}",
                currentPrimaryPackage = currentPrimaryPackage,
                diagnostic = diagnostic,
                inputSide = "baseline",
            )
        }
        consequences += current.diagnostics.map { diagnostic ->
            incompleteComparisonConsequence(
                message = "Current comparison input is incomplete: ${diagnostic.message}",
                currentPrimaryPackage = currentPrimaryPackage,
                diagnostic = diagnostic,
                inputSide = "current",
            )
        }

        if (baseline.validationResult == null && current.validationResult != null && baseline.diagnostics.isEmpty()) {
            consequences += incompleteComparisonConsequence(
                message = "Baseline validation state is unavailable for semantic comparison.",
                currentPrimaryPackage = currentPrimaryPackage,
                diagnostic = comparisonInputDiagnostic(
                    ruleId = "semantic.comparison.baseline.validation.missing",
                    file = baseline.descriptor.label,
                    message = "Baseline validation state is unavailable for semantic comparison.",
                ),
                inputSide = "baseline",
            )
        }

        if (current.validationResult == null && baseline.validationResult != null && current.diagnostics.isEmpty()) {
            consequences += incompleteComparisonConsequence(
                message = "Current validation state is unavailable for semantic comparison.",
                currentPrimaryPackage = currentPrimaryPackage,
                diagnostic = comparisonInputDiagnostic(
                    ruleId = "semantic.comparison.current.validation.missing",
                    file = current.descriptor.label,
                    message = "Current validation state is unavailable for semantic comparison.",
                ),
                inputSide = "current",
            )
        }

        return consequences
    }

    private fun incompleteComparisonConsequence(
        message: String,
        currentPrimaryPackage: PackageIdentifier,
        diagnostic: SemanticDiagnostic,
        inputSide: String,
    ): SemanticDerivedConsequence {
        return SemanticDerivedConsequence(
            type = SemanticDerivedConsequenceType.COMPARISON_INPUT_INCOMPLETE,
            message = message,
            affectedPackage = currentPrimaryPackage,
            diagnostic = diagnostic,
            metadata = mapOf("inputSide" to inputSide),
        )
    }

    private fun connectionChangeRecord(
        message: String,
        currentPrimaryPackage: PackageIdentifier,
        connection: EngineeringConnection,
    ): SemanticChangeRecord {
        return SemanticChangeRecord(
            category = SemanticChangeCategory.CONNECTION_TOPOLOGY_CHANGED,
            layer = SemanticChangeLayer.ENGINEERING,
            message = message,
            affectedPackage = currentPrimaryPackage,
            subjectIdentity = connection.id,
            provenance = connection.provenance,
        )
    }
}

private data class DependencyKey(
    val packageId: PackageIdentifier,
    val source: String,
    val locator: String?,
) {
    fun metadata(): Map<String, String> {
        return buildMap {
            put("source", source)
            locator?.let { locator -> put("locator", locator) }
        }
    }
}

private fun dependencyKey(dependency: PackageDependency): DependencyKey {
    return DependencyKey(
        packageId = dependency.packageId,
        source = dependency.source.name,
        locator = dependency.locator,
    )
}

private fun normalizeProperties(properties: List<EngineeringProperty>): List<String> {
    return properties.map { property ->
        "${property.name}=${property.value.summaryText()}"
    }.sorted()
}

private fun normalizePort(port: EngineeringPort): String {
    return buildString {
        append(port.ownerReference.authoredPath.joinToString("."))
        append(".")
        append(port.name)
        append("|")
        append(normalizeProperties(port.properties).joinToString(","))
    }
}

private fun normalizeConnection(connection: EngineeringConnection): String {
    return "${connection.from.authoredPath.joinToString(".")}->${connection.to.authoredPath.joinToString(".")}"
}

private fun connectionSummary(connection: EngineeringConnection): String {
    return "${connection.from.authoredPath.joinToString(".")} -> ${connection.to.authoredPath.joinToString(".")}"
}

private fun EngineeringPropertyValue.summaryText(): String {
    return when (this) {
        is EngineeringPropertyValue.Symbol -> text
        is EngineeringPropertyValue.Text -> text
    }
}

private fun changeRecordComparator(): Comparator<SemanticChangeRecord> {
    return compareBy<SemanticChangeRecord>(
        { it.layer.ordinal },
        { it.category.ordinal },
        { it.affectedPackage?.name ?: "" },
        { it.subjectIdentity?.value ?: "" },
        { it.message },
    )
}

private fun derivedConsequenceComparator(): Comparator<SemanticDerivedConsequence> {
    return compareBy<SemanticDerivedConsequence>(
        { it.type.ordinal },
        { it.affectedPackage?.name ?: "" },
        { it.subjectIdentity?.value ?: "" },
        { it.diagnostic?.ruleId?.value ?: "" },
        { it.diagnostic?.provenance?.file ?: "" },
        { it.message },
    )
}

private fun validationDeltaMetadata(
    baseline: SemanticValidationResult,
    current: SemanticValidationResult,
): Map<String, String> {
    return mapOf(
        "baselineErrorCount" to baseline.errorCount().toString(),
        "baselineWarningCount" to baseline.warningCount().toString(),
        "baselineContinuationDecision" to baseline.continuationDecision.name,
        "currentErrorCount" to current.errorCount().toString(),
        "currentWarningCount" to current.warningCount().toString(),
        "currentContinuationDecision" to current.continuationDecision.name,
    )
}

private fun SemanticValidationResult.errorCount(): Int {
    return diagnostics.count { diagnostic -> diagnostic.severity == SemanticDiagnosticSeverity.ERROR }
}

private fun SemanticValidationResult.warningCount(): Int {
    return diagnostics.count { diagnostic -> diagnostic.severity == SemanticDiagnosticSeverity.WARNING }
}

private fun comparisonInputDiagnostic(
    ruleId: String,
    file: String,
    message: String,
): SemanticDiagnostic {
    return SemanticDiagnostic(
        severity = SemanticDiagnosticSeverity.ERROR,
        ruleId = SemanticRuleId(ruleId),
        category = SemanticDiagnosticCategory.DOMAIN,
        subjectIdentity = null,
        provenance = SourceProvenance(
            file = file,
            startLine = 1,
            startColumn = 1,
            endLine = 1,
            endColumn = 1,
        ),
        message = message,
    )
}
