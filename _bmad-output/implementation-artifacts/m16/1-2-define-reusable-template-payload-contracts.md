---
baseline_commit: 0f4912953312065542f67eec12f3c1fc4133f61c
---

# Story 1.2: Define Reusable Template Payload Contracts

Status: done

## Story

As a platform engineer,
I want Athena to define reusable template payloads for components and connections,
so that macro expansion is expressed as semantic template composition rather than as graphics or opaque generators.

## FR Traceability

- FR-1: Athena can define Semantic Macro as a governed reusable assembly contract
- FR-2: Athena can publish reuse contracts as dedicated platform models
- NFR-3: Semantic Macro truth remains semantic-first and may not collapse into graphics truth
- NFR-6: Workbench surfaces remain consumers of platform-owned reuse services

## Acceptance Criteria

1. Given template payload contracts are reviewed, when the boundary is inspected, then Athena defines reusable component-template and connection-template contracts in a dedicated template model.
2. Given the template layer is compared with presentation systems, when reviewers inspect ownership, then templates may include optional downstream hints and they do not make SVG, manual layout, or symbol geometry the engineering source of truth.

## Tasks / Subtasks

- [x] Introduce the dedicated `:kernel:template-model` module. (AC: 1, 2)
  - [x] Add `:kernel:template-model` to `settings.gradle.kts`.
  - [x] Create `kernel/template-model/build.gradle.kts` with focused dependencies on component, part, connection, and reuse contracts.
  - [x] Add `TemplateModelModuleMarker` for bootstrap and documentation flows.
- [x] Publish reusable semantic template payload contracts. (AC: 1, 2)
  - [x] Define component-template identity, property-name, and parameter-aware value contracts.
  - [x] Define connection-template identity and semantic endpoint references using template ids plus semantic port roles.
  - [x] Define template-scoped metadata and optional presentation/documentation hints as advisory inputs only.
- [x] Preserve the ownership boundary in code and docs. (AC: 1, 2)
  - [x] Keep component templates semantic-first and concept-led.
  - [x] Keep connection templates semantic-endpoint-first rather than coordinate-first.
  - [x] Make KDoc and README text explicit that hints are advisory and not engineering truth.
- [x] Add focused tests and module documentation. (AC: 1, 2)
  - [x] Add contract tests for component templates, connection templates, and module marker reporting.
  - [x] Add English and Chinese READMEs following the existing module documentation pattern.
  - [x] Run sequential Gradle verification with Java 25 and run the encoding audit.

## Implementation Notes

- Added `:kernel:template-model` as the dedicated M16 template payload layer.
- Implemented `ComponentTemplate`, `ConnectionTemplate`, `TemplateValue`, `TemplatePortReference`, and advisory hint models.
- Kept the module free of package-resolution logic, runtime expansion logic, and renderer-owned geometry or layout contracts.
- Reused existing upstream contracts:
  - `EngineeringConceptId`
  - `PartImplementationId`
  - `SemanticPortRoleId`
  - `SemanticMacroParameterName`
  - `SemanticMacroParameterValue`

## References

- [Source: _bmad-output/planning-artifacts/epics-M16-2026-07-14.md]
- [Source: _bmad-output/planning-artifacts/prds/prd-Athena-2026-07-14-m16/prd.md]
- [Source: _bmad-output/planning-artifacts/prds/prd-Athena-2026-07-14-m16/addendum.md]
- [Source: _bmad-output/planning-artifacts/architecture/architecture-Athena-2026-07-14-m16/ARCHITECTURE-SPINE.md]
- [Source: kernel/template-model/src/main/kotlin/com/engineeringood/athena/template/ComponentTemplateModels.kt]
- [Source: kernel/template-model/src/main/kotlin/com/engineeringood/athena/template/ConnectionTemplateModels.kt]
- [Source: kernel/template-model/src/main/kotlin/com/engineeringood/athena/template/TemplateHintModels.kt]

## Story Completion Status

- Status: done
- Completion note: `:kernel:template-model` now publishes reusable semantic component and connection payload contracts with optional advisory hints and focused tests.
