package com.engineeringood.athena.representation

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotEquals
import kotlin.test.assertTrue

class FunctionAwareRepresentationBindingTest {
    @Test
    fun `function occurrences share physical identity while isolating ports and cross references`() {
        val physicalId = RepresentationSubjectId("component:KM1")
        val coilId = RepresentationSubjectId("function:KM1.coil")
        val contactId = RepresentationSubjectId("function:KM1.no13")
        val coil = bindFunction(
            physicalId = physicalId,
            functionId = coilId,
            role = RepresentationOccurrenceRole.COIL_ACTUATOR,
            symbolKind = RepresentationSymbolKind.COIL_ACTUATOR,
            terminals = mapOf("a1" to "port:KM1.A1", "a2" to "port:KM1.A2"),
            references = listOf(
                RepresentationReferenceBinding(
                    referenceId = RepresentationReferenceId("cross-reference:KM1.coil:no13"),
                    kind = RepresentationReferenceKind.COIL_CONTACT,
                    targetSemanticId = contactId,
                ),
            ),
        )
        val contact = bindFunction(
            physicalId = physicalId,
            functionId = contactId,
            role = RepresentationOccurrenceRole.SWITCH_CONTACT,
            symbolKind = RepresentationSymbolKind.SWITCH_CONTACT,
            terminals = mapOf("13" to "port:KM1.13", "14" to "port:KM1.14"),
            references = listOf(
                RepresentationReferenceBinding(
                    referenceId = RepresentationReferenceId("cross-reference:KM1.no13:coil"),
                    kind = RepresentationReferenceKind.COIL_CONTACT,
                    targetSemanticId = coilId,
                ),
            ),
        )

        assertEquals(physicalId, coil.canonicalSemanticId)
        assertEquals(physicalId, contact.canonicalSemanticId)
        assertEquals(coilId, coil.functionSemanticId)
        assertEquals(contactId, contact.functionSemanticId)
        assertNotEquals(coil.occurrenceId, contact.occurrenceId)
        assertEquals(setOf("port:KM1.A1", "port:KM1.A2"), coil.terminalBindings.map { it.semanticPortId.value }.toSet())
        assertEquals(setOf("port:KM1.13", "port:KM1.14"), contact.terminalBindings.map { it.semanticPortId.value }.toSet())
        assertEquals(contactId, coil.referenceBindings.single().targetSemanticId)
        assertEquals(coilId, contact.referenceBindings.single().targetSemanticId)
    }

    @Test
    fun `KM1 and KM2 coil main NO and NC occurrences preserve physical identity and isolated terminals`() {
        val km1Physical = RepresentationSubjectId("component:KM1")
        val km2Physical = RepresentationSubjectId("component:KM2")
        val km1Functions = listOf("coil", "main", "no13", "nc21").associateWith { name ->
            RepresentationSubjectId("function:KM1.$name")
        }
        val km2Functions = listOf("coil", "main", "no13", "nc21").associateWith { name ->
            RepresentationSubjectId("function:KM2.$name")
        }
        val occurrences = buildList {
            add(
                bindFunction(
                    physicalId = km1Physical,
                    functionId = km1Functions.getValue("coil"),
                    role = RepresentationOccurrenceRole.COIL_ACTUATOR,
                    symbolKind = RepresentationSymbolKind.COIL_ACTUATOR,
                    terminals = mapOf("a1" to "port:KM1.A1", "a2" to "port:KM1.A2"),
                    references = listOf("main", "no13", "nc21").map { target ->
                        coilContactReference(km1Functions.getValue("coil"), km1Functions.getValue(target))
                    },
                ),
            )
            listOf(
                Triple("main", "1", "2"),
                Triple("no13", "13", "14"),
                Triple("nc21", "21", "22"),
            ).forEach { (name, left, right) ->
                add(
                    bindFunction(
                        physicalId = km1Physical,
                        functionId = km1Functions.getValue(name),
                        role = RepresentationOccurrenceRole.SWITCH_CONTACT,
                        symbolKind = RepresentationSymbolKind.SWITCH_CONTACT,
                        terminals = mapOf(left to "port:KM1.$left", right to "port:KM1.$right"),
                        references = listOf(
                            coilContactReference(km1Functions.getValue(name), km1Functions.getValue("coil")),
                        ),
                    ),
                )
            }
            add(
                bindFunction(
                    physicalId = km2Physical,
                    functionId = km2Functions.getValue("coil"),
                    role = RepresentationOccurrenceRole.COIL_ACTUATOR,
                    symbolKind = RepresentationSymbolKind.COIL_ACTUATOR,
                    terminals = mapOf("a1" to "port:KM2.A1", "a2" to "port:KM2.A2"),
                    references = listOf("main", "no13", "nc21").map { target ->
                        coilContactReference(km2Functions.getValue("coil"), km2Functions.getValue(target))
                    },
                ),
            )
            listOf(
                Triple("main", "1", "2"),
                Triple("no13", "13", "14"),
                Triple("nc21", "21", "22"),
            ).forEach { (name, left, right) ->
                add(
                    bindFunction(
                        physicalId = km2Physical,
                        functionId = km2Functions.getValue(name),
                        role = RepresentationOccurrenceRole.SWITCH_CONTACT,
                        symbolKind = RepresentationSymbolKind.SWITCH_CONTACT,
                        terminals = mapOf(left to "port:KM2.$left", right to "port:KM2.$right"),
                        references = listOf(
                            coilContactReference(km2Functions.getValue(name), km2Functions.getValue("coil")),
                        ),
                    ),
                )
            }
        }

        assertEquals(setOf(km1Physical), occurrences.take(4).map { it.canonicalSemanticId }.toSet())
        assertEquals(setOf(km2Physical), occurrences.drop(4).map { it.canonicalSemanticId }.toSet())
        assertEquals(8, occurrences.map { it.occurrenceId }.distinct().size)
        assertTrue(
            occurrences.all { occurrence ->
                occurrence.terminalBindings.all { binding ->
                    binding.semanticPortId.value.substringAfter("port:").substringBefore('.') ==
                        occurrence.canonicalSemanticId.value.substringAfter("component:")
                }
            },
        )
        assertEquals(3, occurrences[0].referenceBindings.size)
        assertEquals(3, occurrences[4].referenceBindings.size)
        assertTrue(occurrences.drop(1).take(3).all { it.referenceBindings.single().targetSemanticId == km1Functions.getValue("coil") })
        assertTrue(occurrences.drop(5).all { it.referenceBindings.single().targetSemanticId == km2Functions.getValue("coil") })
    }

    private fun coilContactReference(
        source: RepresentationSubjectId,
        target: RepresentationSubjectId,
    ): RepresentationReferenceBinding = RepresentationReferenceBinding(
        referenceId = RepresentationReferenceId("cross-reference:${source.value}:${target.value}"),
        kind = RepresentationReferenceKind.COIL_CONTACT,
        targetSemanticId = target,
    )

    private fun bindFunction(
        physicalId: RepresentationSubjectId,
        functionId: RepresentationSubjectId,
        role: RepresentationOccurrenceRole,
        symbolKind: RepresentationSymbolKind,
        terminals: Map<String, String>,
        references: List<RepresentationReferenceBinding>,
    ): RepresentationOccurrence {
        val symbolId = RepresentationSymbolId("iec.${functionId.value.substringAfterLast('.')}")
        val signal = RepresentationSignalPredicate("Control")
        val definition = RepresentationDefinition(
            symbolId = symbolId,
            libraryId = RepresentationLibraryId("athena.m34.iec"),
            version = RepresentationVersion("1.0.0"),
            lifecycle = RepresentationLifecycle(
                state = RepresentationLifecycleState.ACTIVE,
                provenance = RepresentationProvenance("test/function-elements.athena"),
            ),
            kind = symbolKind,
            anatomy = PresentationAnatomy(
                representationId = RepresentationId(symbolId.value),
                context = RepresentationContext.ELECTRICAL_SCHEMATIC,
                bounds = PresentationBounds(GridUnit(40), GridUnit(40)),
                hotspot = PresentationHotspot(PresentationPoint(GridUnit(0), GridUnit(0))),
                primitives = emptyList(),
                terminals = emptyList(),
                labelAnchors = emptyList(),
                authority = PresentationAnatomyAuthority.COMPATIBILITY_SHELL,
            ),
            bodyAuthority = RepresentationBodyAuthority.GRAPHIC_PRIMITIVE,
            definitionKind = RepresentationDefinitionKind.ELEMENT,
            graphicBody = GraphicPrimitiveDocument(
                documentId = GraphicPrimitiveDocumentId(symbolId.value),
                bounds = GraphicBounds(0.0, 0.0, 40.0, 40.0),
                primitives = terminals.keys.mapIndexed { index, terminal ->
                    GraphicPrimitive.Line(
                        primitiveId = GraphicPrimitiveId("terminal-$terminal"),
                        bounds = GraphicBounds(index * 20.0, 0.0, 1.0, 10.0),
                        start = GraphicPoint(index * 20.0, 0.0),
                        end = GraphicPoint(index * 20.0, 10.0),
                        styleTokenId = GraphicStyleTokenId("stroke"),
                    )
                },
                styleTokens = listOf(
                    GraphicStyleToken(
                        styleTokenId = GraphicStyleTokenId("stroke"),
                        stroke = GraphicPaintToken("foreground"),
                        strokeWidth = 1.0,
                        fill = GraphicFill.TRANSPARENT,
                        lineCap = GraphicLineCap.BUTT,
                        lineJoin = GraphicLineJoin.MITER,
                    ),
                ),
            ),
            anchors = terminals.keys.mapIndexed { index, terminal ->
                RepresentationAnchorContract(
                    anchorId = RepresentationAnchorId(terminal),
                    geometryRef = "terminal-$terminal",
                    primitiveId = GraphicPrimitiveId("terminal-$terminal"),
                    point = GraphicPoint(index * 20.0, 0.0),
                    role = RepresentationAnchorRole.TERMINAL,
                    required = true,
                    acceptedDirections = setOf(RepresentationDirectionPredicate.BIDIRECTIONAL),
                    acceptedSignals = setOf(signal),
                    terminal = PhysicalTerminalId(terminal.uppercase()),
                )
            },
            labelSlots = emptyList(),
        )
        val policy = RepresentationPolicy(
            policyId = RepresentationPolicyId("policy:${functionId.value}"),
            projectionKind = RepresentationProjectionKind.ELECTRICAL_SCHEMATIC,
            subjectKind = RepresentationSubjectKind.FUNCTION,
            semanticRole = RepresentationSemanticRole(functionId.value.substringAfterLast('.')),
            occurrenceRole = role,
            symbolFamilyId = SymbolFamilyId(symbolId.value),
            symbolId = symbolId,
            fallback = RepresentationFallbackBehavior.DIAGNOSTIC_ONLY,
            priority = RepresentationPolicyPriority(100),
        )
        val result = RepresentationBindingCompiler().bind(
            RepresentationBindingRequest(
                canonicalSemanticId = physicalId,
                functionSemanticId = functionId,
                projectionOccurrenceId = RepresentationProjectionOccurrenceId("schematic:${functionId.value}"),
                subjectKind = RepresentationSubjectKind.FUNCTION,
                semanticRole = policy.semanticRole,
                projectionKind = RepresentationProjectionKind.ELECTRICAL_SCHEMATIC,
                policy = policy,
                definition = definition,
                labelValues = emptyMap(),
                terminalPorts = terminals.map { (terminal, port) -> PresentationTerminalId(terminal) to SemanticPortId(port) }.toMap(),
                projectPorts = terminals.map { (terminal, port) ->
                    RepresentationProjectPortFact(
                        semanticPortId = SemanticPortId(port),
                        role = RepresentationAnchorRole.TERMINAL,
                        direction = RepresentationDirectionPredicate.BIDIRECTIONAL,
                        signal = signal,
                        terminal = PhysicalTerminalId(terminal.uppercase()),
                        provenance = RepresentationProvenance("test/function.athena"),
                    )
                },
                priority = RepresentationPolicyPriority(100),
                referenceBindings = references,
            ),
        )

        assertTrue(result.diagnostics.isEmpty(), result.diagnostics.toString())
        return result.occurrence
    }
}
