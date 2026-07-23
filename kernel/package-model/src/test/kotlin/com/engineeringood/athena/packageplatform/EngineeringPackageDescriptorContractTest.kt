package com.engineeringood.athena.packageplatform

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class EngineeringPackageDescriptorContractTest {
    @Test
    fun `engineering package descriptor carries catalog and provenance facts without representation authority`() {
        val descriptor = EngineeringPackageDescriptor(
            packageId = EngineeringPackageId("com.athena.example.engineering.drive.compact-vfd"),
            coordinates = EngineeringPackageCoordinates(
                groupId = EngineeringPackageGroupId("com.athena.example.engineering.drive"),
                artifactId = EngineeringPackageArtifactId("compact-vfd"),
                version = EngineeringPackageVersion("1.0.0"),
            ),
            kind = EngineeringPackageKind.CATALOG,
            concepts = listOf(
                EngineeringConceptDefinition(
                    conceptId = EngineeringConceptId("FrequencyDrive"),
                    productDefinitions = listOf(
                        EngineeringProductDefinition(
                            productId = EngineeringProductId("CompactVfdDemo"),
                            model = EngineeringPackageModelName("VFD-DEMO"),
                        ),
                    ),
                    templates = listOf(
                        EngineeringTemplateDefinition(
                            templateId = EngineeringTemplateId("template.frequency-drive.default"),
                            defaultValues = mapOf(
                                "ratedPower" to EngineeringPackageScalarValue("1.5kW"),
                            ),
                        ),
                    ),
                    parameters = listOf(
                        EngineeringParameterDefinition(
                            parameterId = EngineeringParameterId("ratedPower"),
                            valueType = EngineeringParameterValueType.TEXT,
                            required = true,
                        ),
                    ),
                    validationRuleRefs = listOf(EngineeringValidationRuleRef("drive.rating.required")),
                    relationshipCapabilities = listOf(
                        EngineeringRelationshipCapabilityRef("relationship.power-feed"),
                    ),
                ),
            ),
            lifecycle = EngineeringPackageLifecycle(
                state = EngineeringPackageLifecycleState.ACTIVE,
                sinceVersion = EngineeringPackageVersion("1.0.0"),
            ),
            documentationRefs = listOf(EngineeringDocumentationRef("docs/compact-vfd.md")),
            provenance = EngineeringPackageProvenance(
                sources = listOf("athena-m32-demo"),
                reviewedBy = "Athena M32",
            ),
        )

        val result = EngineeringPackageDescriptorValidator.validate(descriptor)

        assertTrue(result.isValid)
        assertEquals(emptyList(), result.diagnostics)
        assertFalse(descriptor.hasRepresentationAuthority())
        assertEquals("com.athena.example.engineering.drive", descriptor.coordinates.groupId.value)
        assertEquals("compact-vfd", descriptor.coordinates.artifactId.value)
        assertEquals("1.0.0", descriptor.coordinates.version.value)
        assertEquals(EngineeringPackageKind.CATALOG, descriptor.kind)
        assertEquals("FrequencyDrive", descriptor.concepts.single().conceptId.value)
    }

    @Test
    fun `engineering package validation rejects representation and renderer authority fields`() {
        val descriptor = validDescriptor().copy(
            forbiddenAuthorityFields = listOf(
                EngineeringPackageForbiddenAuthorityField(
                    field = "representation.svg",
                    authority = EngineeringPackageAuthority.REPRESENTATION,
                ),
                EngineeringPackageForbiddenAuthorityField(
                    field = "presentation.viewBox",
                    authority = EngineeringPackageAuthority.PRESENTATION,
                ),
                EngineeringPackageForbiddenAuthorityField(
                    field = "source.mutationRule",
                    authority = EngineeringPackageAuthority.SOURCE_MUTATION,
                ),
            ),
        )

        val result = EngineeringPackageDescriptorValidator.validate(descriptor)

        assertFalse(result.isValid)
        assertEquals(
            listOf(
                "package.engineering.representation-field-forbidden",
                "package.engineering.presentation-field-forbidden",
                "package.engineering.source-mutation-field-forbidden",
            ),
            result.diagnostics.map { it.code.wireValue },
        )
        assertEquals(
            listOf("representation.svg", "presentation.viewBox", "source.mutationRule"),
            result.diagnostics.map { it.subject },
        )
    }

    @Test
    fun `engineering package validation rejects invalid identity version concept schema and provenance`() {
        val descriptor = validDescriptor().copy(
            packageId = EngineeringPackageId("not a package id"),
            coordinates = EngineeringPackageCoordinates(
                groupId = EngineeringPackageGroupId("bad group"),
                artifactId = EngineeringPackageArtifactId(""),
                version = EngineeringPackageVersion("version one"),
            ),
            kind = EngineeringPackageKind.UNSPECIFIED,
            concepts = listOf(
                validConcept().copy(
                    conceptId = EngineeringConceptId(""),
                    parameters = listOf(
                        EngineeringParameterDefinition(
                            parameterId = EngineeringParameterId(""),
                            valueType = EngineeringParameterValueType.TEXT,
                            required = true,
                        ),
                    ),
                ),
            ),
            provenance = EngineeringPackageProvenance(
                sources = emptyList(),
                reviewedBy = "",
            ),
        )

        val result = EngineeringPackageDescriptorValidator.validate(descriptor)

        assertFalse(result.isValid)
        assertEquals(
            listOf(
                "package.engineering.identity.invalid",
                "package.engineering.group.invalid",
                "package.engineering.artifact.invalid",
                "package.engineering.version.invalid",
                "package.engineering.kind.missing",
                "package.engineering.concept.unsupported",
                "package.engineering.parameter.invalid",
                "package.engineering.provenance.missing",
            ),
            result.diagnostics.map { it.code.wireValue },
        )
    }

    private fun validDescriptor(): EngineeringPackageDescriptor = EngineeringPackageDescriptor(
        packageId = EngineeringPackageId("com.athena.example.engineering.drive.compact-vfd"),
        coordinates = EngineeringPackageCoordinates(
            groupId = EngineeringPackageGroupId("com.athena.example.engineering.drive"),
            artifactId = EngineeringPackageArtifactId("compact-vfd"),
            version = EngineeringPackageVersion("1.0.0"),
        ),
        kind = EngineeringPackageKind.CATALOG,
        concepts = listOf(validConcept()),
        lifecycle = EngineeringPackageLifecycle(
            state = EngineeringPackageLifecycleState.ACTIVE,
            sinceVersion = EngineeringPackageVersion("1.0.0"),
        ),
        documentationRefs = listOf(EngineeringDocumentationRef("docs/compact-vfd.md")),
        provenance = EngineeringPackageProvenance(
            sources = listOf("athena-m32-demo"),
            reviewedBy = "Athena M32",
        ),
    )

    private fun validConcept(): EngineeringConceptDefinition = EngineeringConceptDefinition(
        conceptId = EngineeringConceptId("FrequencyDrive"),
        productDefinitions = listOf(
            EngineeringProductDefinition(
                productId = EngineeringProductId("CompactVfdDemo"),
                model = EngineeringPackageModelName("VFD-DEMO"),
            ),
        ),
        templates = listOf(
            EngineeringTemplateDefinition(
                templateId = EngineeringTemplateId("template.frequency-drive.default"),
                defaultValues = mapOf("ratedPower" to EngineeringPackageScalarValue("1.5kW")),
            ),
        ),
        parameters = listOf(
            EngineeringParameterDefinition(
                parameterId = EngineeringParameterId("ratedPower"),
                valueType = EngineeringParameterValueType.TEXT,
                required = true,
            ),
        ),
        validationRuleRefs = listOf(EngineeringValidationRuleRef("drive.rating.required")),
        relationshipCapabilities = listOf(EngineeringRelationshipCapabilityRef("relationship.power-feed")),
    )
}
