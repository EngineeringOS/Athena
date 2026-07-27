package com.engineeringood.athena.compiler

import com.engineeringood.athena.language.ElementDeclaration

internal object AthenaElementSourceValidator {
    fun validateBasic(file: String, element: ElementDeclaration): List<AthenaRepresentationSourceDiagnostic> =
        AthenaElementBasicValidator.validate(file, element)

    fun validateReferences(
        declarations: List<AuthoredRepresentationDeclaration>,
        identities: List<RepresentationIdentityOccurrence>,
    ): List<AthenaRepresentationSourceDiagnostic> = buildList {
        addAll(AthenaElementReferenceValidator.validate(declarations, identities))
        addAll(AthenaElementCycleValidator.validate(declarations, identities))
    }.canonicalRepresentationDiagnostics()
}
