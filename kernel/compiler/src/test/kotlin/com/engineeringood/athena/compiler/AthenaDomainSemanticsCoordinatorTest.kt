package com.engineeringood.athena.compiler

import com.engineeringood.athena.compiler.plugin.ApprovedAthenaPlugin
import com.engineeringood.athena.compiler.plugin.AthenaApprovedPluginInventory
import com.engineeringood.athena.compiler.plugin.AthenaDomainSemanticsCoordinator
import com.engineeringood.athena.compiler.plugin.AthenaPluginCandidate
import com.engineeringood.athena.compiler.plugin.AthenaPluginValidationContext
import com.engineeringood.athena.ir.EngineeringDocument
import com.engineeringood.athena.ir.EngineeringSystem
import com.engineeringood.athena.ir.SourceProvenance
import com.engineeringood.athena.ir.StableSemanticIdentity
import com.engineeringood.athena.language.SourceFileAst
import com.engineeringood.athena.language.SourcePosition
import com.engineeringood.athena.language.SourceSpan
import com.engineeringood.athena.language.SystemDeclaration
import com.engineeringood.athena.semantics.core.SemanticDiagnosticCategory
import kotlin.test.Test
import kotlin.test.assertEquals

class AthenaDomainSemanticsCoordinatorTest {
    @Test
    fun `aggregates lowering and validation contributions in deterministic approved-plugin order`() {
        val coordinator = AthenaDomainSemanticsCoordinator(
            inventory = AthenaApprovedPluginInventory.fromApproved(
                listOf(
                    approvedPlugin(ZetaSemanticsTestPlugin()),
                    approvedPlugin(AlphaSemanticsTestPlugin()),
                ),
            ),
        )
        val source = CompilerSourceDocument(
            file = "examples/m0/deterministic.athena",
            ast = SourceFileAst(
                system = SystemDeclaration("Deterministic", span(1, 1, 1, 14)),
                declarations = emptyList(),
                span = span(1, 1, 1, 14),
            ),
        )

        val lowering = coordinator.lower(source)

        assertEquals(
            listOf("AlphaDevice", "ZetaDevice"),
            lowering.components.map { it.name },
        )

        val validation = coordinator.validate(
            document = EngineeringDocument(
                system = EngineeringSystem(
                    id = StableSemanticIdentity("system:Deterministic"),
                    name = "Deterministic",
                    provenance = SourceProvenance(source.file, 1, 1, 1, 14),
                ),
                components = emptyList(),
                ports = emptyList(),
                connections = emptyList(),
            ),
            context = AthenaPluginValidationContext(
                document = EngineeringDocument(
                    system = EngineeringSystem(
                        id = StableSemanticIdentity("system:Deterministic"),
                        name = "Deterministic",
                        provenance = SourceProvenance(source.file, 1, 1, 1, 14),
                    ),
                    components = emptyList(),
                    ports = emptyList(),
                    connections = emptyList(),
                ),
                source = source,
                approvedPluginIds = listOf(
                    "com.engineeringood.athena.domain.alpha-semantics",
                    "com.engineeringood.athena.domain.zeta-semantics",
                ),
            ),
        )

        assertEquals(
            listOf(
                "domain.validation.alpha",
                "domain.validation.zeta",
            ),
            validation.diagnostics.map { it.ruleId.value },
        )
        assertEquals(
            listOf(
                SemanticDiagnosticCategory.DOMAIN,
                SemanticDiagnosticCategory.DOMAIN,
            ),
            validation.diagnostics.map { it.category },
        )
    }

    private fun approvedPlugin(plugin: com.engineeringood.athena.compiler.plugin.AthenaDomainPlugin): ApprovedAthenaPlugin {
        return ApprovedAthenaPlugin(
            candidate = AthenaPluginCandidate(
                plugin = plugin,
                implementationClassName = plugin::class.java.name,
                manifest = plugin.manifest,
            ),
        )
    }

    private fun span(startLine: Int, startColumn: Int, endLine: Int, endColumn: Int): SourceSpan {
        return SourceSpan(
            start = SourcePosition(offset = 0, line = startLine, column = startColumn),
            end = SourcePosition(offset = 0, line = endLine, column = endColumn),
        )
    }
}
