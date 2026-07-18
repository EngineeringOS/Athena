package com.engineeringood.athena.compiler

import com.engineeringood.athena.language.LayoutDeclaration
import com.engineeringood.athena.language.LayoutStatement
import com.engineeringood.athena.language.SourceSpan
import com.engineeringood.athena.layout.AuthoredLayoutAxis
import com.engineeringood.athena.layout.AuthoredLayoutIntent
import com.engineeringood.athena.layout.AuthoredLayoutIntentRelation
import com.engineeringood.athena.layout.AuthoredLayoutIntentStatement
import com.engineeringood.athena.layout.LayoutSourceSpan
import com.engineeringood.athena.language.LayoutAxis as SyntaxLayoutAxis

/**
 * Converts M23 authored layout syntax into source-owned layout intent.
 *
 * This mapper deliberately does not resolve subjects or produce constraints. That keeps semantic
 * binding in the compiler-owned Epic 3 path and prevents syntax from leaking into solver behavior.
 */
class AuthoredLayoutIntentMapper {
    fun map(
        declaration: LayoutDeclaration,
        sourceUnitId: String = "authored-layout",
    ): AuthoredLayoutIntent {
        return AuthoredLayoutIntent(
            viewFamily = declaration.viewFamily,
            statements = declaration.statements.map { statement -> statement.toIntentStatement(sourceUnitId) },
            sourceSpan = declaration.span.toLayoutSourceSpan(sourceUnitId),
        )
    }

    private fun LayoutStatement.toIntentStatement(sourceUnitId: String): AuthoredLayoutIntentStatement {
        return when (this) {
            is LayoutStatement.PlaceNear -> AuthoredLayoutIntentStatement(
                subject = subject,
                relation = AuthoredLayoutIntentRelation.NEAR,
                target = target,
                sourceSpan = span.toLayoutSourceSpan(sourceUnitId),
            )

            is LayoutStatement.PlaceBelow -> AuthoredLayoutIntentStatement(
                subject = subject,
                relation = AuthoredLayoutIntentRelation.BELOW,
                target = target,
                sourceSpan = span.toLayoutSourceSpan(sourceUnitId),
            )

            is LayoutStatement.AlignWith -> AuthoredLayoutIntentStatement(
                subject = subject,
                relation = AuthoredLayoutIntentRelation.ALIGNED_WITH,
                target = target,
                axis = axis.toAuthoredAxis(),
                sourceSpan = span.toLayoutSourceSpan(sourceUnitId),
            )

            is LayoutStatement.GroupWith -> AuthoredLayoutIntentStatement(
                subject = subject,
                relation = AuthoredLayoutIntentRelation.GROUPED_WITH,
                target = target,
                sourceSpan = span.toLayoutSourceSpan(sourceUnitId),
            )
        }
    }

    private fun SyntaxLayoutAxis.toAuthoredAxis(): AuthoredLayoutAxis {
        return when (this) {
            SyntaxLayoutAxis.Horizontal -> AuthoredLayoutAxis.HORIZONTAL
            SyntaxLayoutAxis.Vertical -> AuthoredLayoutAxis.VERTICAL
        }
    }

    private fun SourceSpan.toLayoutSourceSpan(sourceUnitId: String): LayoutSourceSpan {
        return LayoutSourceSpan(
            sourceUnitId = sourceUnitId,
            startLine = start.line,
            startColumn = start.column,
            endLine = end.line,
            endColumn = end.column,
        )
    }
}
