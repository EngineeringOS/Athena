package com.engineeringood.athena.compiler

import com.engineeringood.athena.language.BindingDeclaration
import com.engineeringood.athena.language.BindingSelectorKind
import com.engineeringood.athena.language.ElementDeclaration
import com.engineeringood.athena.language.ProfileDeclaration

internal object AthenaProfileBindingSourceValidator {
    private val semanticVersionPattern = Regex(
        """(0|[1-9][0-9]*)\.(0|[1-9][0-9]*)\.(0|[1-9][0-9]*)""" +
            """(?:-((?:0|[1-9][0-9]*|[0-9]*[A-Za-z-][0-9A-Za-z-]*)""" +
            """(?:\.(?:0|[1-9][0-9]*|[0-9]*[A-Za-z-][0-9A-Za-z-]*))*))?""" +
            """(?:\+([0-9A-Za-z-]+(?:\.[0-9A-Za-z-]+)*))?""",
    )

    fun validate(
        declarations: List<AuthoredRepresentationDeclaration>,
        identities: List<RepresentationIdentityOccurrence>,
    ): List<AthenaRepresentationSourceDiagnostic> = buildList {
        val profiles = declarations.filter { occurrence -> occurrence.declaration is ProfileDeclaration }
        val bindings = declarations.filter { occurrence -> occurrence.declaration is BindingDeclaration }
        val profilesByName = profiles.groupBy { occurrence -> occurrence.declaration.name }
        val bindingsByName = bindings.groupBy { occurrence -> occurrence.declaration.name }

        profilesByName.filterValues { occurrences -> occurrences.size > 1 }
            .toSortedMap()
            .values
            .flatten()
            .forEach { occurrence ->
                add(
                    issue(
                        "profile.name.duplicate",
                        occurrence,
                        "profile.${occurrence.declaration.name}",
                        "Presentation Profile names must be unique in one compilation batch.",
                    ),
                )
            }
        bindingsByName.filterValues { occurrences -> occurrences.size > 1 }
            .toSortedMap()
            .values
            .flatten()
            .forEach { occurrence ->
                add(
                    issue(
                        "binding.name.duplicate",
                        occurrence,
                        "binding.${occurrence.declaration.name}",
                        "Representation Binding names must be unique in one compilation batch.",
                    ),
                )
            }

        profiles.forEach { occurrence ->
            validateProfile(occurrence, occurrence.declaration as ProfileDeclaration)
        }
        bindings.forEach { occurrence ->
            validateBinding(
                occurrence,
                occurrence.declaration as BindingDeclaration,
                profilesByName,
                identities,
            )
        }
    }.canonicalRepresentationDiagnostics()

    private fun MutableList<AthenaRepresentationSourceDiagnostic>.validateProfile(
        occurrence: AuthoredRepresentationDeclaration,
        profile: ProfileDeclaration,
    ) {
        if (profile.projection == null) {
            add(issue("profile.projection.missing", occurrence, "profile.${profile.name}.projection", "Presentation Profile requires a projection."))
        }
        if (profile.standard == null) {
            add(issue("profile.standard.missing", occurrence, "profile.${profile.name}.standard", "Presentation Profile requires a standard."))
        }
        if (profile.style == null) {
            add(issue("profile.style.missing", occurrence, "profile.${profile.name}.style", "Presentation Profile requires a style."))
        }
        when (val fallback = profile.fallback) {
            null -> add(issue("profile.fallback.missing", occurrence, "profile.${profile.name}.fallback", "Presentation Profile requires an explicit fail-closed fallback policy."))
            else -> if (fallback.value != "fail-closed") {
                add(
                    representationDiagnostic(
                        "profile.fallback.invalid",
                        occurrence.file,
                        fallback.span,
                        "profile.${profile.name}.fallback",
                        "Presentation Profile fallback must be fail-closed.",
                    ),
                )
            }
        }
    }

    private fun MutableList<AthenaRepresentationSourceDiagnostic>.validateBinding(
        occurrence: AuthoredRepresentationDeclaration,
        binding: BindingDeclaration,
        profilesByName: Map<String, List<AuthoredRepresentationDeclaration>>,
        identities: List<RepresentationIdentityOccurrence>,
    ) {
        val subject = "binding.${binding.name}"
        val profile = binding.profile
        when {
            profile == null -> add(issue("binding.profile.missing", occurrence, "$subject.profile", "Representation Binding requires a Presentation Profile."))
            profilesByName[profile.value].isNullOrEmpty() -> add(
                representationDiagnostic(
                    "binding.profile.unresolved",
                    occurrence.file,
                    profile.span,
                    "$subject.profile",
                    "Representation Binding references missing Presentation Profile `${profile.value}`.",
                ),
            )
        }

        val priority = binding.priority
        when {
            priority == null -> add(issue("binding.priority.missing", occurrence, "$subject.priority", "Representation Binding requires a priority."))
            !priority.value.isFinite() || priority.value < 0.0 || priority.value > Int.MAX_VALUE.toDouble() ||
                priority.value % 1.0 != 0.0 -> add(
                representationDiagnostic(
                    "binding.priority.invalid",
                    occurrence.file,
                    priority.span,
                    "$subject.priority",
                    "Representation Binding priority must be a non-negative integer.",
                ),
            )
        }

        if (binding.selectorKind == null) {
            add(issue("binding.selector.kind.missing", occurrence, "$subject.selector", "Representation Binding requires a typed device or function selector."))
        }
        val typeFacts = binding.selectorFacts.filter { fact -> fact.name == "type" }
        when (typeFacts.size) {
            0 -> add(issue("binding.selector.type.missing", occurrence, "$subject.selector.type", "Representation Binding selector requires exactly one type fact."))
            1 -> Unit
            else -> add(
                representationDiagnostic(
                    "binding.selector.type.duplicate",
                    occurrence.file,
                    typeFacts[1].span,
                    "$subject.selector.type",
                    "Representation Binding selector must declare type exactly once.",
                ),
            )
        }
        if (binding.selectorKind == BindingSelectorKind.Function) {
            val roleFacts = binding.selectorFacts.filter { fact -> fact.name == "role" }
            when (roleFacts.size) {
                0 -> add(
                    issue(
                        "binding.selector.role.missing",
                        occurrence,
                        "$subject.selector.role",
                        "Function Representation Binding selector requires exactly one role fact.",
                    ),
                )
                1 -> Unit
                else -> add(
                    representationDiagnostic(
                        "binding.selector.role.duplicate",
                        occurrence.file,
                        roleFacts[1].span,
                        "$subject.selector.role",
                        "Function Representation Binding selector must declare role exactly once.",
                    ),
                )
            }
        }

        val elementIdentity = binding.useElement
        when {
            elementIdentity == null -> add(issue("binding.target.element.missing", occurrence, "$subject.target.element", "Representation Binding requires a target Element."))
            else -> validateElementTarget(occurrence, binding, elementIdentity.value, identities)
        }

        val version = binding.useVersion
        when {
            version == null -> add(issue("binding.target.version.missing", occurrence, "$subject.target.version", "Representation Binding requires an exact target version."))
            !version.value.matches(semanticVersionPattern) -> add(
                representationDiagnostic(
                    "binding.target.version.invalid",
                    occurrence.file,
                    version.span,
                    "$subject.target.version",
                    "Representation Binding target version must use semantic version form major.minor.patch.",
                ),
            )
        }
    }

    private fun MutableList<AthenaRepresentationSourceDiagnostic>.validateElementTarget(
        occurrence: AuthoredRepresentationDeclaration,
        binding: BindingDeclaration,
        identity: String,
        identities: List<RepresentationIdentityOccurrence>,
    ) {
        val subject = "binding.${binding.name}.target.element"
        val candidates = identities.filter { candidate -> candidate.identity == identity }
        when {
            candidates.isEmpty() -> add(
                representationDiagnostic(
                    "binding.target.element.unresolved",
                    occurrence.file,
                    requireNotNull(binding.useElement).span,
                    subject,
                    "Representation Binding target `$identity` must resolve to exactly one compiled Element in the package graph.",
                ),
            )
            candidates.size > 1 -> add(
                representationDiagnostic(
                    "binding.target.element.ambiguous",
                    occurrence.file,
                    requireNotNull(binding.useElement).span,
                    subject,
                    "Representation Binding target `$identity` resolves to multiple package graph declarations.",
                ),
            )
            candidates.size == 1 && candidates.single().declaration !is ElementDeclaration -> add(
                representationDiagnostic(
                    "binding.target.element.kind.invalid",
                    occurrence.file,
                    requireNotNull(binding.useElement).span,
                    subject,
                    "Representation Binding target `$identity` resolves to a Symbol, not an Element.",
                ),
            )
            candidates.size == 1 -> {
                val target = candidates.single().declaration as ElementDeclaration
                val authoredVersion = binding.useVersion?.value
                val targetVersion = target.version?.value
                if (authoredVersion != null && authoredVersion.matches(semanticVersionPattern) &&
                    targetVersion != null && authoredVersion != targetVersion
                ) {
                    add(
                        representationDiagnostic(
                            "binding.target.version.mismatch",
                            occurrence.file,
                            requireNotNull(binding.useVersion).span,
                            "binding.${binding.name}.target.version",
                            "Representation Binding target version `$authoredVersion` does not match Element version `$targetVersion`.",
                        ),
                    )
                }
            }
        }
    }

    private fun issue(
        code: String,
        occurrence: AuthoredRepresentationDeclaration,
        subject: String,
        message: String,
    ): AthenaRepresentationSourceDiagnostic = representationDiagnostic(
        code,
        occurrence.file,
        occurrence.declaration.span,
        subject,
        message,
    )
}
