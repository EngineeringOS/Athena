package com.engineeringood.athena.compiler

import com.engineeringood.athena.layout.AuthoredLayoutAxis
import com.engineeringood.athena.layout.AuthoredLayoutIntent
import com.engineeringood.athena.layout.AuthoredLayoutIntentPriority
import com.engineeringood.athena.layout.AuthoredLayoutIntentRelation
import com.engineeringood.athena.layout.AuthoredLayoutIntentStatement

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
        require(priority == AuthoredLayoutIntentPriority.PREFERENCE) {
            "M23 source syntax admits only default preference layout hints."
        }
        return when (relation) {
            AuthoredLayoutIntentRelation.NEAR -> "place $subject near $target"
            AuthoredLayoutIntentRelation.BELOW -> "place $subject below $target"
            AuthoredLayoutIntentRelation.ALIGNED_WITH ->
                "align $subject aligned-with $target axis ${requireNotNull(axis).render()}"
            AuthoredLayoutIntentRelation.GROUPED_WITH -> "group $subject grouped-with $target"
        }
    }

    private fun AuthoredLayoutAxis.render(): String {
        return when (this) {
            AuthoredLayoutAxis.HORIZONTAL -> "horizontal"
            AuthoredLayoutAxis.VERTICAL -> "vertical"
        }
    }
}
