package com.engineeringood.athena.compiler

import com.engineeringood.athena.language.ElementChildDeclaration
import com.engineeringood.athena.language.SourceSpan

internal fun ElementChildDeclaration.validZOrder(): Int? = zOrder?.value?.takeIf {
    it.isFinite() && it % 1.0 == 0.0 && it in Int.MIN_VALUE.toDouble()..Int.MAX_VALUE.toDouble()
}?.toInt()

internal fun issue(
    code: String,
    file: String,
    span: SourceSpan,
    subject: String,
    message: String,
) = representationDiagnostic(code, file, span, subject, message)
