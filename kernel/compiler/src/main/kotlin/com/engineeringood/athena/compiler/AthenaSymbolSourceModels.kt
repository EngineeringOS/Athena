package com.engineeringood.athena.compiler

import com.engineeringood.athena.language.SourceSpan
import com.engineeringood.athena.packageplatform.PackageResourceDeclaration
import com.engineeringood.athena.packageplatform.PresentationProfileDescriptor
import com.engineeringood.athena.packageplatform.RepresentationBindingRule
import com.engineeringood.athena.representation.RepresentationDefinition

data class AthenaRepresentationSourceInput(
    val file: String,
    val source: String,
)

data class AthenaRepresentationSourceDiagnostic(
    val code: String,
    val file: String,
    val span: SourceSpan,
    val subject: String,
    val message: String,
)

data class AthenaRepresentationSourceCompilationResult(
    val definitions: List<RepresentationDefinition>,
    val profiles: List<PresentationProfileDescriptor> = emptyList(),
    val bindingRules: List<RepresentationBindingRule> = emptyList(),
    val diagnostics: List<AthenaRepresentationSourceDiagnostic>,
    val resources: List<PackageResourceDeclaration> = emptyList(),
)

data class AthenaRepresentationSourceFormatResult(
    val formattedSource: String?,
    val diagnostics: List<AthenaRepresentationSourceDiagnostic>,
    val failure: String? = null,
)

internal fun List<AthenaRepresentationSourceDiagnostic>.canonicalRepresentationDiagnostics(): List<AthenaRepresentationSourceDiagnostic> =
    distinct()
        .sortedWith(
        compareBy<AthenaRepresentationSourceDiagnostic>(
            { diagnostic -> diagnostic.code },
            { diagnostic -> diagnostic.file },
            { diagnostic -> diagnostic.span.start.offset },
            { diagnostic -> diagnostic.span.start.line },
            { diagnostic -> diagnostic.span.start.column },
            { diagnostic -> diagnostic.span.end.offset },
            { diagnostic -> diagnostic.span.end.line },
            { diagnostic -> diagnostic.span.end.column },
            { diagnostic -> diagnostic.subject },
            { diagnostic -> diagnostic.message },
        ),
    )

internal data class AthenaRepresentationLoweringResult(
    val definition: RepresentationDefinition?,
    val diagnostics: List<AthenaRepresentationSourceDiagnostic>,
)

internal fun representationDiagnostic(
    code: String,
    file: String,
    span: SourceSpan,
    subject: String,
    message: String,
) = AthenaRepresentationSourceDiagnostic(code, file, span, subject, message)

internal fun failedRepresentationCompilation(vararg diagnostics: AthenaRepresentationSourceDiagnostic) = AthenaRepresentationSourceCompilationResult(
    definitions = emptyList(),
    diagnostics = diagnostics.toList().canonicalRepresentationDiagnostics(),
)
