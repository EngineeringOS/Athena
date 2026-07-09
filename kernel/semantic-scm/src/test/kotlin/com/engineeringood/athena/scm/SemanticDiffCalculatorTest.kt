package com.engineeringood.athena.scm

import com.engineeringood.athena.ir.EngineeringComponent
import com.engineeringood.athena.ir.EngineeringConnection
import com.engineeringood.athena.ir.EngineeringDocument
import com.engineeringood.athena.ir.EngineeringPort
import com.engineeringood.athena.ir.EngineeringProperty
import com.engineeringood.athena.ir.EngineeringPropertyValue
import com.engineeringood.athena.ir.EngineeringReference
import com.engineeringood.athena.ir.EngineeringSystem
import com.engineeringood.athena.ir.SourceProvenance
import com.engineeringood.athena.ir.StableSemanticIdentity
import com.engineeringood.athena.repository.EngineeringRepository
import com.engineeringood.athena.repository.PackageDependency
import com.engineeringood.athena.repository.PackageDependencySource
import com.engineeringood.athena.repository.PackageIdentifier
import com.engineeringood.athena.repository.PrimaryPackage
import com.engineeringood.athena.repository.RepositoryGraphReport
import com.engineeringood.athena.repository.RepositoryLock
import com.engineeringood.athena.repository.RepositoryManifest
import com.engineeringood.athena.repository.ResolvedPackage
import com.engineeringood.athena.repository.ResolvedPackageGraph
import com.engineeringood.athena.semantics.core.SemanticContinuationDecision
import com.engineeringood.athena.semantics.core.SemanticDiagnostic
import com.engineeringood.athena.semantics.core.SemanticDiagnosticCategory
import com.engineeringood.athena.semantics.core.SemanticDiagnosticSeverity
import com.engineeringood.athena.semantics.core.SemanticRuleId
import com.engineeringood.athena.semantics.core.SemanticValidationResult
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class SemanticDiffCalculatorTest {
    @Test
    fun `produces deterministic repository package and engineering change categories`() {
        val rootPackage = PackageIdentifier(name = "com.engineeringood.demo", version = "1.0.0")
        val alphaPackage = PackageIdentifier(name = "com.engineeringood.alpha", version = "1.0.0")
        val baseline = semanticSnapshot(
            descriptor = SemanticBaselineDescriptor(
                baselineId = "baseline-1",
                label = "Baseline",
            ),
            manifestDependencies = emptyList(),
            graphPackages = emptyList(),
            engineeringDocument = engineeringDocument(
                includeMotor = false,
                includeConnection = false,
                includeModelProperty = false,
            ),
            rootPackage = rootPackage,
        )
        val current = semanticSnapshot(
            descriptor = SemanticBaselineDescriptor(
                baselineId = "current-1",
                label = "Current",
            ),
            manifestDependencies = listOf(
                PackageDependency(
                    packageId = alphaPackage,
                    source = PackageDependencySource.LOCAL_PATH,
                    locator = "vendor/alpha",
                ),
            ),
            graphPackages = listOf(
                ResolvedPackage(
                    packageId = alphaPackage,
                    sourceRoot = "vendor/alpha/src",
                ),
            ),
            engineeringDocument = engineeringDocument(
                includeMotor = true,
                includeConnection = true,
                includeModelProperty = true,
            ),
            rootPackage = rootPackage,
        )

        val calculator = SemanticDiffCalculator()
        val first = calculator.calculate(
            baseline = baseline,
            current = current,
        )
        val second = calculator.calculate(
            baseline = baseline,
            current = current,
        )

        assertEquals(first, second)
        assertEquals(
            listOf(
                SemanticChangeCategory.PACKAGE_DEPENDENCY_CHANGED,
                SemanticChangeCategory.ENGINEERING_STRUCTURE_CHANGED,
                SemanticChangeCategory.ENGINEERING_STRUCTURE_CHANGED,
                SemanticChangeCategory.ENGINEERING_PROPERTY_CHANGED,
                SemanticChangeCategory.CONNECTION_TOPOLOGY_CHANGED,
            ),
            first.authoredChanges.map { change -> change.category },
        )
        assertEquals(
            listOf(
                SemanticDerivedConsequenceType.LOCK_UPDATED,
                SemanticDerivedConsequenceType.PACKAGE_GRAPH_RECOMPUTED,
            ),
            first.derivedConsequences.map { consequence -> consequence.type },
        )
        assertTrue(first.authoredChanges.all { change -> change.layer in setOf(SemanticChangeLayer.PACKAGE, SemanticChangeLayer.ENGINEERING) })
        assertEquals(listOf(rootPackage, alphaPackage), first.affectedPackages)
    }

    @Test
    fun `keeps lock churn as a derived consequence rather than authored repository intent`() {
        val rootPackage = PackageIdentifier(name = "com.engineeringood.demo", version = "1.0.0")
        val baseline = semanticSnapshot(
            descriptor = SemanticBaselineDescriptor(
                baselineId = "baseline-2",
                label = "Baseline",
            ),
            manifestDependencies = emptyList(),
            graphPackages = emptyList(),
            engineeringDocument = engineeringDocument(
                includeMotor = false,
                includeConnection = false,
                includeModelProperty = false,
            ),
            rootPackage = rootPackage,
        )
        val current = semanticSnapshot(
            descriptor = SemanticBaselineDescriptor(
                baselineId = "current-2",
                label = "Current",
            ),
            manifestDependencies = emptyList(),
            graphPackages = emptyList(),
            engineeringDocument = engineeringDocument(
                includeMotor = false,
                includeConnection = false,
                includeModelProperty = false,
            ),
            rootPackage = rootPackage,
            lockVersion = 2,
        )

        val diff = SemanticDiffCalculator().calculate(
            baseline = baseline,
            current = current,
        )

        assertTrue(diff.authoredChanges.isEmpty())
        assertEquals(
            listOf(SemanticDerivedConsequenceType.LOCK_UPDATED),
            diff.derivedConsequences.map { consequence -> consequence.type },
        )
    }

    @Test
    fun `publishes repository contract impact and validation deltas as deterministic consequences`() {
        val baselinePackage = PackageIdentifier(name = "com.engineeringood.demo", version = "1.0.0")
        val currentPackage = PackageIdentifier(name = "com.engineeringood.demo", version = "2.0.0")
        val baseline = semanticSnapshot(
            descriptor = SemanticBaselineDescriptor(
                baselineId = "baseline-3",
                label = "Baseline",
            ),
            manifestDependencies = emptyList(),
            graphPackages = emptyList(),
            engineeringDocument = engineeringDocument(
                includeMotor = false,
                includeConnection = false,
                includeModelProperty = false,
            ),
            rootPackage = baselinePackage,
            validationResult = validationResult(
                continuationDecision = SemanticContinuationDecision.CONTINUE,
            ),
        )
        val current = semanticSnapshot(
            descriptor = SemanticBaselineDescriptor(
                baselineId = "current-3",
                label = "Current",
            ),
            manifestDependencies = emptyList(),
            graphPackages = emptyList(),
            engineeringDocument = engineeringDocument(
                includeMotor = false,
                includeConnection = false,
                includeModelProperty = true,
            ),
            rootPackage = currentPackage,
            validationResult = validationResult(
                semanticDiagnostic(
                    ruleId = "validation.property.model.required",
                    message = "Model property requires review.",
                ),
                continuationDecision = SemanticContinuationDecision.STOP_DOWNSTREAM,
            ),
        )

        val first = SemanticDiffCalculator().calculate(
            baseline = baseline,
            current = current,
        )
        val second = SemanticDiffCalculator().calculate(
            baseline = baseline,
            current = current,
        )

        assertEquals(first, second)
        assertEquals(
            listOf(
                SemanticDerivedConsequenceType.REPOSITORY_CONTRACT_REVIEW_REQUIRED,
                SemanticDerivedConsequenceType.LOCK_UPDATED,
                SemanticDerivedConsequenceType.PACKAGE_GRAPH_RECOMPUTED,
                SemanticDerivedConsequenceType.VALIDATION_DELTA_DETECTED,
            ),
            first.derivedConsequences.map { consequence -> consequence.type },
        )
        assertTrue(first.derivedConsequences.any { consequence ->
            consequence.type == SemanticDerivedConsequenceType.VALIDATION_DELTA_DETECTED &&
                consequence.metadata["currentErrorCount"] == "1"
        })
    }

    @Test
    fun `emits deterministic incomplete comparison consequences when snapshot diagnostics are present`() {
        val rootPackage = PackageIdentifier(name = "com.engineeringood.demo", version = "1.0.0")
        val diff = SemanticDiffCalculator().calculate(
            baseline = semanticSnapshot(
                descriptor = SemanticBaselineDescriptor(
                    baselineId = "baseline-4",
                    label = "Baseline",
                ),
                manifestDependencies = emptyList(),
                graphPackages = emptyList(),
                engineeringDocument = engineeringDocument(
                    includeMotor = false,
                    includeConnection = false,
                    includeModelProperty = false,
                ),
                rootPackage = rootPackage,
                diagnostics = listOf(
                    semanticDiagnostic(
                        ruleId = "semantic.baseline.compile.parse-failed",
                        message = "Baseline parse failed.",
                    ),
                ),
            ),
            current = semanticSnapshot(
                descriptor = SemanticBaselineDescriptor(
                    baselineId = "current-4",
                    label = "Current",
                ),
                manifestDependencies = emptyList(),
                graphPackages = emptyList(),
                engineeringDocument = engineeringDocument(
                    includeMotor = false,
                    includeConnection = false,
                    includeModelProperty = false,
                ),
                rootPackage = rootPackage,
                validationResult = validationResult(
                    continuationDecision = SemanticContinuationDecision.CONTINUE,
                ),
            ),
        )

        assertEquals(
            listOf(SemanticDerivedConsequenceType.COMPARISON_INPUT_INCOMPLETE),
            diff.derivedConsequences.map { consequence -> consequence.type },
        )
        assertEquals(
            "semantic.baseline.compile.parse-failed",
            diff.derivedConsequences.single().diagnostic?.ruleId?.value,
        )
    }
}

private fun semanticSnapshot(
    descriptor: SemanticBaselineDescriptor,
    manifestDependencies: List<PackageDependency>,
    graphPackages: List<ResolvedPackage>,
    engineeringDocument: EngineeringDocument,
    rootPackage: PackageIdentifier,
    lockVersion: Int = 1,
    validationResult: SemanticValidationResult? = null,
    diagnostics: List<SemanticDiagnostic> = emptyList(),
): SemanticBaselineSnapshot {
    return SemanticBaselineSnapshot(
        descriptor = descriptor,
        repositoryReport = RepositoryGraphReport(
            repository = EngineeringRepository(
                manifest = RepositoryManifest(
                    primaryPackage = PrimaryPackage(
                        id = rootPackage,
                        sourceRoot = "src",
                    ),
                    dependencies = manifestDependencies,
                ),
                lock = RepositoryLock(
                    version = lockVersion,
                    primaryPackage = rootPackage,
                    packages = listOf(
                        ResolvedPackage(
                            packageId = rootPackage,
                            sourceRoot = "src",
                            directDependencies = manifestDependencies.map { dependency -> dependency.packageId },
                        ),
                    ) + graphPackages,
                ),
            ),
            graph = ResolvedPackageGraph(
                rootPackage = rootPackage,
                packages = listOf(
                    ResolvedPackage(
                        packageId = rootPackage,
                        sourceRoot = "src",
                        directDependencies = manifestDependencies.map { dependency -> dependency.packageId },
                    ),
                ) + graphPackages,
            ),
        ),
        engineeringDocuments = listOf(engineeringDocument),
        validationResult = validationResult,
        diagnostics = diagnostics,
    )
}

private fun engineeringDocument(
    includeMotor: Boolean,
    includeConnection: Boolean,
    includeModelProperty: Boolean,
): EngineeringDocument {
    val plcId = StableSemanticIdentity("component:PLC1")
    val plcPortId = StableSemanticIdentity("port:PLC1.out")
    val motorId = StableSemanticIdentity("component:M1")
    val motorPortId = StableSemanticIdentity("port:M1.in")

    val components = buildList {
        add(
            EngineeringComponent(
                id = plcId,
                name = "PLC1",
                kind = "Switch",
                properties = buildList {
                    if (includeModelProperty) {
                        add(
                            EngineeringProperty(
                                name = "model",
                                value = EngineeringPropertyValue.Text("S7-1200"),
                            ),
                        )
                    }
                },
                provenance = provenance("components.athena"),
            ),
        )
        if (includeMotor) {
            add(
                EngineeringComponent(
                    id = motorId,
                    name = "M1",
                    kind = "Motor",
                    properties = emptyList(),
                    provenance = provenance("components.athena"),
                ),
            )
        }
    }

    val ports = buildList {
        add(
            EngineeringPort(
                id = plcPortId,
                ownerReference = ownerReference("PLC1", plcId),
                name = "out",
                properties = listOf(
                    EngineeringProperty(
                        name = "direction",
                        value = EngineeringPropertyValue.Symbol("out"),
                    ),
                ),
                provenance = provenance("ports.athena"),
            ),
        )
        if (includeMotor) {
            add(
                EngineeringPort(
                    id = motorPortId,
                    ownerReference = ownerReference("M1", motorId),
                    name = "in",
                    properties = listOf(
                        EngineeringProperty(
                            name = "direction",
                            value = EngineeringPropertyValue.Symbol("in"),
                        ),
                    ),
                    provenance = provenance("ports.athena"),
                ),
            )
        }
    }

    val connections = if (includeConnection) {
        listOf(
            EngineeringConnection(
                id = StableSemanticIdentity("connection:PLC1.out->M1.in"),
                from = reference("PLC1", "out", plcPortId),
                to = reference("M1", "in", motorPortId),
                provenance = provenance("connections.athena"),
            ),
        )
    } else {
        emptyList()
    }

    return EngineeringDocument(
        system = EngineeringSystem(
            id = StableSemanticIdentity("system:Demo"),
            name = "Demo",
            provenance = provenance("system.athena"),
        ),
        components = components,
        ports = ports,
        connections = connections,
    )
}

private fun ownerReference(
    ownerName: String,
    identity: StableSemanticIdentity,
): EngineeringReference {
    return EngineeringReference(
        authoredPath = listOf(ownerName),
        resolvedIdentity = identity,
        provenance = provenance("owner.athena"),
    )
}

private fun reference(
    ownerName: String,
    portName: String,
    identity: StableSemanticIdentity,
): EngineeringReference {
    return EngineeringReference(
        authoredPath = listOf(ownerName, portName),
        resolvedIdentity = identity,
        provenance = provenance("reference.athena"),
    )
}

private fun provenance(file: String): SourceProvenance {
    return SourceProvenance(
        file = file,
        startLine = 1,
        startColumn = 1,
        endLine = 1,
        endColumn = 1,
    )
}

private fun validationResult(
    vararg diagnostics: SemanticDiagnostic,
    continuationDecision: SemanticContinuationDecision,
): SemanticValidationResult {
    return SemanticValidationResult(
        diagnostics = diagnostics.toList(),
        continuationDecision = continuationDecision,
    )
}

private fun semanticDiagnostic(
    ruleId: String,
    message: String,
): SemanticDiagnostic {
    return SemanticDiagnostic(
        severity = SemanticDiagnosticSeverity.ERROR,
        ruleId = SemanticRuleId(ruleId),
        category = SemanticDiagnosticCategory.DOMAIN,
        subjectIdentity = null,
        provenance = provenance("diagnostic.athena"),
        message = message,
    )
}
