package com.engineeringood.athena.compiler

import com.engineeringood.athena.authoring.AuthoringIntentId
import com.engineeringood.athena.authoring.AuthoringOrigin
import com.engineeringood.athena.authoring.AuthoringRevisionGuard
import com.engineeringood.athena.authoring.AuthoringSurface
import com.engineeringood.athena.authoring.AuthoringTransactionProvenance
import com.engineeringood.athena.authoring.CreateSemanticEntityIntent
import com.engineeringood.athena.authoring.ElectricalConnectionRelationship
import com.engineeringood.athena.authoring.SemanticEntityCreationContext
import com.engineeringood.athena.authoring.SemanticRelationshipIntent
import com.engineeringood.athena.component.EngineeringConceptId
import com.engineeringood.athena.component.EngineeringConceptPortDirection
import com.engineeringood.athena.component.EngineeringConceptPortTemplate
import com.engineeringood.athena.component.EngineeringConceptTemplate
import com.engineeringood.athena.component.EngineeringConceptTemplateId
import com.engineeringood.athena.component.EngineeringConceptTemplateProvenance
import com.engineeringood.athena.component.EngineeringSemanticType
import com.engineeringood.athena.component.EngineeringSignalOrMedium
import com.engineeringood.athena.ir.StableSemanticIdentity
import com.engineeringood.athena.language.AthenaLanguageParser
import com.engineeringood.athena.language.ParseSuccess
import com.engineeringood.athena.domain.electricalruntime.electricalEngineeringConceptTemplates
import com.engineeringood.athena.layout.AuthoredLayoutIntent
import com.engineeringood.athena.layout.AuthoredLayoutIntentRelation
import com.engineeringood.athena.layout.AuthoredLayoutIntentStatement
import com.engineeringood.athena.layout.LayoutSourceSpan
import java.nio.file.Path
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertIs
import kotlin.test.assertTrue

class BackendAuthoringSourceEditPlannerTest {
    @Test
    fun `entity creation is revision-bound AST-placed and template-driven`() {
        val document = sourceDocument(SOURCE)
        val intent = createEntityIntent(document.revisionGuard)

        val result = BackendAuthoringSourceEditPlanner().plan(
            BackendEntityCreationPlanningRequest(
                document = document,
                revisionGuard = document.revisionGuard,
                intent = intent,
                template = MOTOR_TEMPLATE,
            ),
        )

        val planned = assertIs<BackendAuthoringSourceEditPlanned>(result)
        assertEquals(document.revisionGuard, planned.plan.revisionGuard)
        assertEquals(document.ast.system.span.end.offset - 1, planned.plan.replacement.startOffset)
        assertEquals(planned.plan.replacement.startOffset, planned.plan.replacement.endOffset)
        assertEquals(
            listOf("component:ShutterMotorM31", "port:ShutterMotorM31.up"),
            planned.plan.affectedSemanticIds,
        )
        assertTrue(planned.plan.admittedText.contains("device ShutterMotorM31"))
        assertTrue(planned.plan.admittedText.contains("type Motor"))
        assertTrue(planned.plan.admittedText.contains("model \"MOTOR-AC\""))
        assertTrue(planned.plan.admittedText.contains("    port up {"))
        assertTrue(planned.plan.admittedText.contains("      direction in"))
        assertTrue(planned.plan.admittedText.contains("      signal Digital"))
        assertTrue(!planned.plan.admittedText.contains("port ShutterMotorM31.up"))
        assertParsesAfterApplying(document, planned.plan)
    }

    @Test
    fun `planned electrical motor recompiles to exact canonical nested-port identities`() {
        val document = sourceDocument(SOURCE)
        val template = electricalEngineeringConceptTemplates().single { candidate ->
            candidate.templateId.value == "electrical.motor.ac.default"
        }
        val intent = createEntityIntent(document.revisionGuard)

        val planned = assertIs<BackendAuthoringSourceEditPlanned>(
            BackendAuthoringSourceEditPlanner().plan(
                BackendEntityCreationPlanningRequest(
                    document = document,
                    revisionGuard = document.revisionGuard,
                    intent = intent,
                    template = template,
                ),
            ),
        ).plan
        val updated = planned.applyTo(document.sourceText)
        val compilation = assertIs<CompilerCompilationSuccess>(
            AthenaCompiler().compile(Path.of("sample.athena"), updated),
        )

        assertEquals(
            listOf(
                "component:ShutterMotorM31",
                "port:ShutterMotorM31.down",
                "port:ShutterMotorM31.status",
                "port:ShutterMotorM31.up",
            ),
            planned.affectedSemanticIds,
        )
        assertEquals(
            listOf(
                "port:ShutterMotorM31.down",
                "port:ShutterMotorM31.status",
                "port:ShutterMotorM31.up",
            ),
            compilation.document.ports
                .filter { port -> port.ownerReference.authoredPath == listOf("ShutterMotorM31") }
                .map { port -> port.id.value }
                .sorted(),
        )
        assertTrue(updated.contains("    port up {"))
        assertTrue(updated.contains("    port down {"))
        assertTrue(updated.contains("    port status {"))
        assertTrue(!Regex("(?m)^\\s*port\\s+ShutterMotorM31\\.").containsMatchIn(updated))
    }

    @Test
    fun `entity creation preserves the exact admitted canonical tag`() {
        val document = sourceDocument(SOURCE)
        val intent = createEntityIntent(document.revisionGuard).copy(suggestedName = "Shutter_Motor_31")

        val planned = assertIs<BackendAuthoringSourceEditPlanned>(
            BackendAuthoringSourceEditPlanner().plan(
                BackendEntityCreationPlanningRequest(
                    document = document,
                    revisionGuard = document.revisionGuard,
                    intent = intent,
                    template = MOTOR_TEMPLATE,
                ),
            ),
        ).plan

        assertTrue(planned.planTextContainsTag("Shutter_Motor_31"))
        assertEquals(
            listOf("component:Shutter_Motor_31", "port:Shutter_Motor_31.up"),
            planned.affectedSemanticIds,
        )
    }

    @Test
    fun `changed source rejects planning with stable conflict diagnostic`() {
        val document = sourceDocument(SOURCE)
        val staleGuard = AuthoringRevisionGuard.from(
            semanticSnapshotId = document.revisionGuard.semanticSnapshotId,
            sourceUri = document.sourceUri,
            documentVersion = document.documentVersion,
            sourceText = "$SOURCE\n// changed",
        )

        val result = BackendAuthoringSourceEditPlanner().plan(
            BackendEntityCreationPlanningRequest(
                document = document,
                revisionGuard = staleGuard,
                intent = createEntityIntent(staleGuard),
                template = MOTOR_TEMPLATE,
            ),
        )

        val rejected = assertIs<BackendAuthoringSourceEditRejected>(result)
        assertEquals("authoring.source.conflict", rejected.diagnostics.single().code.value)
    }

    @Test
    fun `planner rejects generated text that is not admitted by the Athena parser`() {
        val document = sourceDocument(SOURCE)
        val invalidTemplate = MOTOR_TEMPLATE.copy(
            nestedPorts = listOf(
                MOTOR_TEMPLATE.nestedPorts.single().copy(
                    signalOrMedium = EngineeringSignalOrMedium("Not Admitted"),
                ),
            ),
        )

        val result = BackendAuthoringSourceEditPlanner().plan(
            BackendEntityCreationPlanningRequest(
                document = document,
                revisionGuard = document.revisionGuard,
                intent = createEntityIntent(document.revisionGuard),
                template = invalidTemplate,
            ),
        )

        val rejected = assertIs<BackendAuthoringSourceEditRejected>(result)
        assertEquals("authoring.source.invalid", rejected.diagnostics.single().code.value)
    }

    @Test
    fun `relationship and layout text are serialized by the backend planner`() {
        val document = sourceDocument(SOURCE)
        val relationship = BackendAuthoringSourceEditPlanner().plan(
            BackendRelationshipPlanningRequest(
                document = document,
                revisionGuard = document.revisionGuard,
                intent = SemanticRelationshipIntent(
                    intentId = AuthoringIntentId("relationship-1"),
                    origin = ORIGIN,
                    relationshipType = ElectricalConnectionRelationship,
                    sourceSubjectId = StableSemanticIdentity("port:Source.out"),
                    targetSubjectId = StableSemanticIdentity("port:Load.in"),
                ),
                sourceAuthoredPath = "Source.out",
                targetAuthoredPath = "Load.in",
            ),
        )
        val relationshipPlan = assertIs<BackendAuthoringSourceEditPlanned>(relationship).plan
        assertTrue(relationshipPlan.admittedText.contains("connect Source.out -> Load.in"))
        assertEquals(listOf("connection:Source.out->Load.in"), relationshipPlan.affectedSemanticIds)

        val layoutIntent = AuthoredLayoutIntent(
            viewFamily = "documentation",
            statements = listOf(
                AuthoredLayoutIntentStatement(
                    subject = "Load",
                    relation = AuthoredLayoutIntentRelation.NEAR,
                    target = "Source",
                    sourceSpan = LAYOUT_SPAN,
                ),
            ),
            sourceSpan = LAYOUT_SPAN,
        )
        val layout = BackendAuthoringSourceEditPlanner().plan(
            BackendAuthoredLayoutPlanningRequest(
                document = document,
                revisionGuard = document.revisionGuard,
                subjectSemanticId = "component:Load",
                intent = layoutIntent,
            ),
        )
        val layoutPlan = assertIs<BackendAuthoringSourceEditPlanned>(layout).plan
        assertTrue(layoutPlan.admittedText.contains("layout documentation"))
        assertTrue(layoutPlan.admittedText.contains("place Load near Source"))
        assertParsesAfterApplying(document, layoutPlan)
    }

    @Test
    fun `blank relationship paths return a structured planning diagnostic`() {
        val document = sourceDocument(SOURCE)

        val result = BackendAuthoringSourceEditPlanner().plan(
            BackendRelationshipPlanningRequest(
                document = document,
                revisionGuard = document.revisionGuard,
                intent = SemanticRelationshipIntent(
                    intentId = AuthoringIntentId("relationship-malformed"),
                    origin = ORIGIN,
                    relationshipType = ElectricalConnectionRelationship,
                    sourceSubjectId = StableSemanticIdentity("port:Source.out"),
                    targetSubjectId = StableSemanticIdentity("port:Load.in"),
                ),
                sourceAuthoredPath = " ",
                targetAuthoredPath = "Load.in",
            ),
        )

        val rejected = assertIs<BackendAuthoringSourceEditRejected>(result)
        assertEquals("authoring.source.invalid", rejected.diagnostics.single().code.value)
    }

    private fun sourceDocument(source: String): BackendAuthoringSourceDocument {
        val parse = assertIs<ParseSuccess>(AthenaLanguageParser().parse("sample.athena", source))
        return BackendAuthoringSourceDocument(
            sourceUri = "file:///workspace/sample.athena",
            documentVersion = 7,
            semanticSnapshotId = "snapshot:m31",
            sourceText = source,
            ast = parse.ast,
        )
    }

    private fun createEntityIntent(revisionGuard: AuthoringRevisionGuard): CreateSemanticEntityIntent =
        CreateSemanticEntityIntent(
            intentId = AuthoringIntentId("create-1"),
            origin = ORIGIN,
            creationContext = SemanticEntityCreationContext(StableSemanticIdentity("system:Demo")),
            conceptTemplateId = MOTOR_TEMPLATE.templateId,
            conceptId = MOTOR_TEMPLATE.conceptId,
            suggestedName = "ShutterMotorM31",
            revisionGuard = revisionGuard,
            provenance = AuthoringTransactionProvenance(actor = "user:test", origin = ORIGIN),
        )

    private fun assertParsesAfterApplying(
        document: BackendAuthoringSourceDocument,
        plan: BackendAuthoringSourceEditPlan,
    ) {
        val updated = document.sourceText.substring(0, plan.replacement.startOffset) +
            plan.admittedText +
            document.sourceText.substring(plan.replacement.endOffset)
        assertIs<ParseSuccess>(AthenaLanguageParser().parse("sample.athena", updated))
    }

    private fun BackendAuthoringSourceEditPlan.applyTo(source: String): String =
        source.substring(0, replacement.startOffset) + admittedText + source.substring(replacement.endOffset)

    private fun BackendAuthoringSourceEditPlan.planTextContainsTag(tag: String): Boolean =
        admittedText.contains("device $tag {")

    private companion object {
        val ORIGIN = AuthoringOrigin(AuthoringSurface.GRAPH)
        val LAYOUT_SPAN = LayoutSourceSpan("sample.athena", 1, 1, 1, 1)
        val MOTOR_TEMPLATE = EngineeringConceptTemplate(
            templateId = EngineeringConceptTemplateId("electrical.motor.ac.default"),
            conceptId = EngineeringConceptId("electrical.motor.ac"),
            semanticType = EngineeringSemanticType("Motor"),
            defaultModel = "MOTOR-AC",
            nestedPorts = listOf(
                EngineeringConceptPortTemplate(
                    name = "up",
                    direction = EngineeringConceptPortDirection.IN,
                    signalOrMedium = EngineeringSignalOrMedium("Digital"),
                ),
            ),
            provenance = EngineeringConceptTemplateProvenance("electrical", "test"),
        )
        const val SOURCE = """system Demo {
  device Source {
    type Switch
    port out {
      direction out
      signal Digital
    }
  }
  device Load {
    type Motor
    port in {
      direction in
      signal Digital
    }
  }
}"""
    }
}
