package com.engineeringood.athena.knowledge

import com.engineeringood.athena.ir.EngineeringDefinitionId
import com.engineeringood.athena.ir.EngineeringDefinitionReference
import com.engineeringood.athena.ir.EngineeringDimensionSignature
import com.engineeringood.athena.ir.EngineeringPackageName
import com.engineeringood.athena.ir.EngineeringValue
import com.engineeringood.athena.ir.ExactNumber
import com.engineeringood.athena.ir.SourceProvenance
import java.math.BigInteger
import kotlin.test.Test
import kotlin.test.assertEquals

class KnowledgeDefinitionModelsTest {
    private val packageName = EngineeringPackageName("example.core")
    private val unitId = EngineeringDefinitionId(packageName, "unit.ampere")
    private val provenance = SourceProvenance("packages/core.athena", 0, 0, 0, 1)

    @Test
    fun `formula contract keeps exact closed operators`() {
        val quantity = EngineeringValue.Integer(BigInteger.TEN)
        val expression = FormulaExpression.RoundUp(
            FormulaExpression.Literal(quantity),
            FormulaExpression.Literal(EngineeringValue.Integer(BigInteger.valueOf(5))),
        )
        assertEquals(emptyList(), FormulaContractValidator.validate(expression))
    }

    @Test
    fun `unit rejects non-positive scale and relationship roles stay typed`() {
        val unit = KnowledgeDefinition.Unit(
            id = unitId,
            dimension = EngineeringDefinitionReference(listOf("dimension.current"), null, provenance),
            scale = ExactNumber.ONE,
            provenance = provenance,
        )
        assertEquals(ExactNumber.ONE, unit.scale)
        val role = RelationshipRoleDefinition("provider", SubjectLevel.PORT, minimum = 1, maximum = 1)
        assertEquals(SubjectLevel.PORT, role.subjectLevel)
    }

    @Test
    fun `dimension algebra remains sparse and domain neutral`() {
        val current = EngineeringDimensionSignature.of(mapOf(unitId to 1))
        val reciprocal = EngineeringDimensionSignature.of(mapOf(unitId to -1))
        assertEquals(EngineeringDimensionSignature.DIMENSIONLESS, current + reciprocal)
    }
}
