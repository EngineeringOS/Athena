package com.engineeringood.athena.compiler

import com.engineeringood.athena.layout.AuthoredLayoutAxis
import com.engineeringood.athena.layout.AuthoredLayoutIntent
import com.engineeringood.athena.layout.AuthoredLayoutIntentPriority
import com.engineeringood.athena.layout.AuthoredLayoutIntentRelation
import com.engineeringood.athena.layout.AuthoredLayoutIntentStatement
import com.engineeringood.athena.layout.LayoutOrientation

/**
 * Serializes approved authored layout intent into admitted M23 `.athena` source text.
 *
 * The serializer is deliberately backend-owned so frontend code can request source edits without
 * hand-building final Athena syntax.
 */
class AuthoredLayoutIntentSourceSerializer {
    fun serialize(intent: AuthoredLayoutIntent): String {
        return buildString {
            append("layout ")
            append(intent.viewFamily)
            appendLine(" {")
            intent.statements.forEach { statement ->
                append("  ")
                append(statement.render())
                appendLine()
            }
            append("}")
        }
    }

    private fun AuthoredLayoutIntentStatement.render(): String {
        require(
            priority == if (relation == AuthoredLayoutIntentRelation.AT_GRID) {
                AuthoredLayoutIntentPriority.HARD
            } else {
                AuthoredLayoutIntentPriority.PREFERENCE
            },
        ) {
            "Authored layout intent priority does not match its admitted source form."
        }
        return when (relation) {
            AuthoredLayoutIntentRelation.NEAR -> "place $subject near $target"
            AuthoredLayoutIntentRelation.BELOW -> "place $subject below $target"
            AuthoredLayoutIntentRelation.ALIGNED_WITH ->
                "align $subject aligned-with $target axis ${requireNotNull(axis).render()}"
            AuthoredLayoutIntentRelation.GROUPED_WITH -> "group $subject grouped-with $target"
            AuthoredLayoutIntentRelation.AT_GRID -> requireNotNull(gridPosition).let { position ->
                "place $subject at (${position.column}, ${position.row}) orientation ${requireNotNull(orientation).render()}"
            }
        }
    }

    private fun AuthoredLayoutAxis.render(): String {
        return when (this) {
            AuthoredLayoutAxis.HORIZONTAL -> "horizontal"
            AuthoredLayoutAxis.VERTICAL -> "vertical"
        }
    }

    private fun LayoutOrientation.render(): String = name.lowercase()
}
