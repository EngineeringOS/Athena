package com.engineeringood.athena.compiler

import com.engineeringood.athena.ir.SourceProvenance
import com.engineeringood.athena.layout.LayoutSourceSpan

internal fun SourceProvenance.toLayoutSourceSpanOrNull(): LayoutSourceSpan? {
    if (
        file.isBlank() || startLine <= 0 || startColumn <= 0 || endLine < startLine || endColumn <= 0 ||
        (endLine == startLine && endColumn < startColumn)
    ) {
        return null
    }
    return LayoutSourceSpan(
        sourceUnitId = file,
        startLine = startLine,
        startColumn = startColumn,
        endLine = endLine,
        endColumn = endColumn,
    )
}
