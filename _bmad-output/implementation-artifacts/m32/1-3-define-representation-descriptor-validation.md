---
status: review
baseline_commit: 8d12ba03c05b373b98fbc513e6c7f3f6fce962d6
story_id: 1.3
epic: 1
title: Define Representation Descriptor Validation
---

# Story 1.3: Define Representation Descriptor Validation

## Status

Review

## Story

As an Athena renderer-integrator,
I want descriptor-level validation for resources, anchors, labels, bounds, variants, and hotspots,
so that invalid resources never produce trusted projection success.

## Required Context

- PRD: `_bmad-output/planning-artifacts/prds/prd-Athena-2026-07-22-m32/prd.md`
- Addendum: `_bmad-output/planning-artifacts/prds/prd-Athena-2026-07-22-m32/addendum.md`
- Architecture: `_bmad-output/planning-artifacts/architecture/architecture-Athena-2026-07-22-m32/ARCHITECTURE-SPINE.md`
- Epics: `_bmad-output/implementation-artifacts/m32/epics.md`
- Sprint: `_bmad-output/implementation-artifacts/m32/sprint-status.yaml`
- Cleanup ledger: `_bmad-output/implementation-artifacts/m32/cleanup-ledger.md`
- Previous stories:
  - `_bmad-output/implementation-artifacts/m32/1-1-define-engineering-package-descriptor-v0.md`
  - `_bmad-output/implementation-artifacts/m32/1-2-define-representation-package-descriptor-v0.md`

## Acceptance Criteria

1. Given a Representation Descriptor, when validation runs, then it validates resource id/kind,
   bounds, anchors, label slots, hotspots, transforms, variants, style tokens, and validation rules.
2. Given missing resources, duplicate anchors, missing required label slots, invalid bounds, or
   unsupported variants, when validation runs, then structured diagnostics are emitted before
   Representation Occurrence creation.
3. Given a descriptor-backed Graphic Resource, when tests inspect semantic authority, then Graphic
   Resource ids, text labels, CSS classes, and file names do not define engineering truth.
4. Given Story 1.2 established Representation Package Descriptor v0, when descriptor validation is
   added, then descriptor facts remain resource/view contracts and do not move into Theia,
   renderer code, `.athena` grammar, or M30 occurrence creation.
5. Mandatory Polish/Purge Gate complete with AC-to-evidence mapping.

## Tasks/Subtasks

- [x] Add RED contract tests for a valid Representation Descriptor v0 with resource, bounds,
  anchors, label slots, hotspots, transforms, variants, style tokens, and validation rules. (AC:
  1,4)
- [x] Add RED validation tests for missing resource references, duplicate anchors, invalid bounds,
  missing required label slots, and unsupported variants. (AC: 2)
- [x] Add RED authority tests proving Graphic Resource ids, visible labels, CSS classes, and file
  names are not semantic truth. (AC: 3)
- [x] Implement Representation Descriptor v0 model contracts in `kernel:package-model`, reusing
  Story 1.2 resource/profile/value patterns without merging descriptor validation into renderer or
  occurrence creation. (AC: 1,4)
- [x] Implement deterministic descriptor validation diagnostics for required descriptor fields and
  fail-closed validation. (AC: 1,2)
- [x] Update package platform documentation to explain Representation Package Descriptor vs
  Representation Descriptor vs Graphic Resource. (AC: 3,4)
- [x] Run focused package-model tests sequentially, then full regression sequentially; do not run
  Gradle concurrently on Windows. (AC: 1..4)
- [x] Complete mandatory polish/purge review and record final evidence. (AC: 5)

## Dev Notes

- M32 descriptor chain:

  ```text
  Representation Package Descriptor
    -> Representation Descriptor
    -> Graphic Resource handle
    -> future Representation Occurrence integration
  ```

- Story 1.3 is still a contract and validation story. Do not feed descriptors into M30 occurrence
  creation; that belongs to Story 4.1.
- Existing M30 `kernel/representation-model` owns `RepresentationDefinition` and
  `RepresentationOccurrence`. This story may reference that boundary in docs, but should not change
  it unless tests prove a contract incompatibility.
- Descriptor validation must fail before projection success. Missing anchors/resources/labels must
  be diagnostics, not renderer fallback boxes or center anchors.
- Suggested model areas:
  - `RepresentationDescriptorId`
  - `RepresentationDescriptor`
  - descriptor resource binding/reference
  - bounds
  - anchor definitions
  - label slot definitions
  - hotspot definitions
  - transform definitions
  - descriptor variants
  - style token refs
  - validation rule refs
- Recommended diagnostic code style:
  - `package.representation.descriptor.resource-missing`
  - `package.representation.descriptor.bounds-invalid`
  - `package.representation.descriptor.anchor-duplicate`
  - `package.representation.descriptor.label-slot-missing`
  - `package.representation.descriptor.variant-unsupported`
  - `package.representation.descriptor.semantic-authority-forbidden`
- Run `powershell -ExecutionPolicy Bypass -File .\tools\encoding-audit.ps1` after text edits.

## Project Structure Notes

- Prefer cohesive files such as `RepresentationDescriptorModels.kt`,
  `RepresentationDescriptorValidation.kt`, and `RepresentationDescriptorValidationTest.kt` if the
  Story 1.2 files would become mixed-responsibility.
- Do not create renderer, Theia, LSP, registry, Binding Resolver, or sample-project code in this
  story.
- Keep Kotlin files readable; split validation behavior from model contracts.

## Testing Requirements

- Follow TDD: write failing descriptor validation tests before production code.
- Focused command: `.\gradlew.bat --no-daemon --console=plain :kernel:package-model:test`.
- Full regression command after story completion: `.\gradlew.bat --no-daemon --console=plain check`.
- Encoding audit command after docs/text edits:
  `powershell -ExecutionPolicy Bypass -File .\tools\encoding-audit.ps1`.
- AC evidence must include exact test names/commands and the polish/purge result.

## Dev Agent Record

### Agent Model Used

Codex GPT-5

### Debug Log References

- RED: `.\gradlew.bat --no-daemon --console=plain :kernel:package-model:test` failed in
  `:kernel:package-model:compileTestKotlin` with unresolved `RepresentationDescriptorValidator`,
  `RepresentationDescriptor`, and related descriptor model types.
- GREEN: `.\gradlew.bat --no-daemon --console=plain :kernel:package-model:test` passed after
  adding Representation Descriptor v0 models and validator.
- REGRESSION: `.\gradlew.bat --no-daemon --console=plain check` passed after descriptor
  documentation and contract implementation.
- ENCODING: `powershell -ExecutionPolicy Bypass -File .\tools\encoding-audit.ps1` passed.

### Completion Notes List

- Added Representation Descriptor v0 model contracts for resource binding, positive bounds,
  anchors, label slots, hotspots, transforms, variants, style tokens, validation rule refs, and
  forbidden semantic authority claims.
- Added descriptor validation context and fail-closed diagnostics for missing resources, invalid
  bounds, duplicate anchors, missing required label slots, unsupported variants, invalid hotspots,
  and resource-field semantic authority leaks.
- Added tests proving valid descriptor contracts, invalid descriptor diagnostics, and that Graphic
  Resource ids/text/CSS/file names are not engineering truth.
- Updated package platform documentation with the Representation Descriptor layer.
- AC-to-evidence: AC1 covered by
  `representation descriptor validation accepts resource bounds anchors labels hotspots transforms variants and style facts`;
  AC2 covered by
  `representation descriptor validation diagnoses missing resource duplicate anchors invalid bounds missing labels and unsupported variants`;
  AC3 covered by
  `representation descriptor validation rejects graphic resource fields as engineering truth`; AC4
  covered by `kernel:package-model` placement and no M30 renderer/occurrence edits; AC5 covered by
  full `check`, encoding audit, `git status --short`, and no new cleanup-ledger entry.

### File List

- `_bmad-output/implementation-artifacts/m32/1-3-define-representation-descriptor-validation.md`
- `_bmad-output/implementation-artifacts/m32/sprint-status.yaml`
- `docs/usages/engineering-package-platform.md`
- `kernel/package-model/src/main/kotlin/com/engineeringood/athena/packageplatform/RepresentationDescriptorModels.kt`
- `kernel/package-model/src/main/kotlin/com/engineeringood/athena/packageplatform/RepresentationDescriptorValidation.kt`
- `kernel/package-model/src/test/kotlin/com/engineeringood/athena/packageplatform/RepresentationDescriptorValidationTest.kt`

## Change Log

- 2026-07-22: Story created for M32 Epic 1 after Representation Package Descriptor v0.
- 2026-07-22: Implemented Representation Descriptor v0 validation contracts and tests.

## Mandatory Final Polish/Purge Gate

- Review touched and adjacent package model, representation model, compiler knowledge package,
  tests, documentation, fixtures, and sprint artifacts.
- Remove dead/stale descriptor experiments, stale docs, duplicate fixtures, or misleading authority
  claims.
- Ledger any retained stale item in `_bmad-output/implementation-artifacts/m32/cleanup-ledger.md`
  with owner, reason, target milestone, and verification.
- Record AC-to-evidence mapping before moving the story beyond `review`.
