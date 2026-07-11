package com.engineeringood.athena.compiler

import com.engineeringood.athena.compiler.knowledge.AthenaCompilationKnowledgeContext
import com.engineeringood.athena.compiler.knowledge.AthenaKnowledgeArtifactKind
import com.engineeringood.athena.compiler.knowledge.AthenaKnowledgeArtifactPackage
import com.engineeringood.athena.compiler.knowledge.AthenaKnowledgePayloadEntry
import com.engineeringood.athena.ir.DerivedEngineeringContext
import com.engineeringood.athena.ir.DerivedEngineeringQuantity
import com.engineeringood.athena.ir.DerivedEngineeringSubjectContext
import com.engineeringood.athena.ir.DerivedEngineeringValue
import com.engineeringood.athena.ir.DerivedEngineeringValueKind
import com.engineeringood.athena.ir.EngineeringCapabilityComparison
import com.engineeringood.athena.ir.EngineeringCapabilityDerivedValueReference
import com.engineeringood.athena.ir.EngineeringCapabilityFact
import com.engineeringood.athena.ir.EngineeringCapabilityFactKind
import com.engineeringood.athena.ir.EngineeringCapabilityFactTrace
import com.engineeringood.athena.ir.EngineeringCapabilityFacts
import com.engineeringood.athena.ir.EngineeringCapabilitySubjectFacts
import java.math.BigDecimal
import java.math.RoundingMode
import java.nio.file.Files
import java.util.Properties

/**
 * Promotes selected derived engineering context values into inspectable capability facts through a fixed governed
 * electrical knowledge pack.
 */
class EngineeringCapabilityFactPromoter {
    /**
     * Promotes capability facts from [derivedContext] using the active governed knowledge packages in [knowledgeContext].
     */
    fun promote(
        derivedContext: DerivedEngineeringContext,
        knowledgeContext: AthenaCompilationKnowledgeContext,
    ): EngineeringCapabilityFacts {
        val capabilitySemantics = resolveCapabilitySemantics(knowledgeContext) ?: return EngineeringCapabilityFacts.canonical(emptyList())
        val subjects = derivedContext.subjects.mapNotNull { subject ->
            promoteSubject(subject, capabilitySemantics)
        }
        return EngineeringCapabilityFacts.canonical(subjects)
    }

    private fun promoteSubject(
        subject: DerivedEngineeringSubjectContext,
        semantics: ElectricalCapabilitySemantics,
    ): EngineeringCapabilitySubjectFacts? {
        val facts = semantics.rules.mapNotNull { rule ->
            val sourceValue = subject.derivedValues.firstOrNull { value -> value.kind == rule.sourceValueKind } ?: return@mapNotNull null
            promoteFact(subject, sourceValue, rule, semantics.packageMetadata)
        }
        if (facts.isEmpty()) {
            return null
        }
        return EngineeringCapabilitySubjectFacts(
            subjectIdentity = subject.subjectIdentity,
            facts = facts,
        )
    }

    private fun promoteFact(
        subject: DerivedEngineeringSubjectContext,
        sourceValue: DerivedEngineeringValue,
        rule: ElectricalCapabilityRule,
        packageMetadata: ElectricalCapabilityPackageMetadata,
    ): EngineeringCapabilityFact? {
        val sourceQuantity = sourceValue.quantity as? DerivedEngineeringQuantity.Decimal ?: return null
        val sourceNumeric = sourceQuantity.canonicalText.toBigDecimalOrNull() ?: return null
        val promotedNumeric = when (rule.rounding) {
            ElectricalCapabilityRounding.CEIL_TO_WHOLE_AMP -> sourceNumeric.multiply(rule.multiplier).setScale(0, RoundingMode.CEILING)
        }
        return EngineeringCapabilityFact(
            kind = rule.factKind,
            subjectIdentity = subject.subjectIdentity,
            comparison = rule.comparison,
            quantity = DerivedEngineeringQuantity.Decimal(
                canonicalText = promotedNumeric.stripTrailingZeros().toPlainString(),
                unitSymbol = "A",
            ),
            trace = EngineeringCapabilityFactTrace(
                knowledgeArtifactId = packageMetadata.artifactId,
                knowledgeArtifactVersion = packageMetadata.artifactVersion,
                knowledgeEntryId = packageMetadata.entryId,
                sourceDerivedValues = listOf(
                    EngineeringCapabilityDerivedValueReference(
                        subjectIdentity = subject.subjectIdentity,
                        valueKind = sourceValue.kind,
                    ),
                ),
            ),
        )
    }

    private fun resolveCapabilitySemantics(knowledgeContext: AthenaCompilationKnowledgeContext): ElectricalCapabilitySemantics? {
        val activePack = knowledgeContext.candidates.firstOrNull { candidate ->
            candidate.artifactPackage.manifest.artifactKind == AthenaKnowledgeArtifactKind.KNOWLEDGE_PACK &&
                candidate.artifactPackage.manifest.artifactId == ELECTRICAL_BASIC_KNOWLEDGE_PACK_ID &&
                knowledgeContext.activeArtifacts.any { artifact ->
                    artifact.artifactId == candidate.artifactPackage.manifest.artifactId &&
                        artifact.artifactVersion == candidate.artifactPackage.manifest.artifactVersion &&
                        artifact.artifactKind == AthenaKnowledgeArtifactKind.KNOWLEDGE_PACK
                }
        }?.artifactPackage ?: return null
        return loadCapabilitySemantics(activePack)
    }

    private fun loadCapabilitySemantics(artifactPackage: AthenaKnowledgeArtifactPackage): ElectricalCapabilitySemantics? {
        val payloadEntry = artifactPackage.payloadEntries.singleOrNull { entry ->
            entry.entryKind == CAPABILITY_SEMANTICS_ENTRY_KIND
        } ?: return null
        val properties = loadProperties(payloadEntry) ?: return null
        val ruleIds = properties.stringPropertyNames()
            .mapNotNull { key -> FACT_PROPERTY_KEY.matchEntire(key)?.groupValues?.get(1) }
            .toSortedSet()
        if (ruleIds.isEmpty()) {
            return null
        }
        val rules = ruleIds.mapNotNull { ruleId ->
            loadRule(ruleId, properties)
        }
        if (rules.isEmpty()) {
            return null
        }
        return ElectricalCapabilitySemantics(
            packageMetadata = ElectricalCapabilityPackageMetadata(
                artifactId = artifactPackage.manifest.artifactId,
                artifactVersion = artifactPackage.manifest.artifactVersion,
                entryId = payloadEntry.entryId,
            ),
            rules = rules,
        )
    }

    private fun loadRule(
        ruleId: String,
        properties: Properties,
    ): ElectricalCapabilityRule? {
        val factKind = properties.getProperty("fact.$ruleId.kind")?.trim()
            ?.takeIf { it.isNotEmpty() }
            ?.let(EngineeringCapabilityFactKind::valueOf)
            ?: return null
        val sourceValueKind = properties.getProperty("fact.$ruleId.source")?.trim()
            ?.takeIf { it.isNotEmpty() }
            ?.let(DerivedEngineeringValueKind::valueOf)
            ?: return null
        val multiplier = properties.getProperty("fact.$ruleId.multiplier")?.trim()
            ?.toBigDecimalOrNull()
            ?: return null
        val comparison = properties.getProperty("fact.$ruleId.comparison")?.trim()
            ?.takeIf { it.isNotEmpty() }
            ?.let(EngineeringCapabilityComparison::valueOf)
            ?: return null
        val rounding = properties.getProperty("fact.$ruleId.rounding")?.trim()
            ?.takeIf { it.isNotEmpty() }
            ?.let(ElectricalCapabilityRounding::valueOf)
            ?: return null
        return ElectricalCapabilityRule(
            factKind = factKind,
            sourceValueKind = sourceValueKind,
            multiplier = multiplier,
            comparison = comparison,
            rounding = rounding,
        )
    }

    private fun loadProperties(payloadEntry: AthenaKnowledgePayloadEntry): Properties? {
        return runCatching {
            Properties().apply {
                Files.newInputStream(payloadEntry.resolvedPath).use(::load)
            }
        }.getOrNull()
    }
}

private data class ElectricalCapabilitySemantics(
    val packageMetadata: ElectricalCapabilityPackageMetadata,
    val rules: List<ElectricalCapabilityRule>,
)

private data class ElectricalCapabilityPackageMetadata(
    val artifactId: String,
    val artifactVersion: String,
    val entryId: String,
)

private data class ElectricalCapabilityRule(
    val factKind: EngineeringCapabilityFactKind,
    val sourceValueKind: DerivedEngineeringValueKind,
    val multiplier: BigDecimal,
    val comparison: EngineeringCapabilityComparison,
    val rounding: ElectricalCapabilityRounding,
)

private enum class ElectricalCapabilityRounding {
    CEIL_TO_WHOLE_AMP,
}

private const val CAPABILITY_SEMANTICS_ENTRY_KIND = "capability-semantics"
private val FACT_PROPERTY_KEY = Regex("""fact\.([^.]+)\.(kind|source|multiplier|comparison|rounding)""")
