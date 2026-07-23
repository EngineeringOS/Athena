---
status: review
baseline_commit: 8d12ba03c05b373b98fbc513e6c7f3f6fce962d6
story_id: 1.2
epic: 1
title: Define Representation Package Descriptor V0
---

# Story 1.2: Define Representation Package Descriptor V0

## Status

Review

## Story

As an Athena representation package author,
I want a separate Representation Package contract,
so that visual resources can be distributed without becoming engineering truth.

## Required Context

- PRD: `_bmad-output/planning-artifacts/prds/prd-Athena-2026-07-22-m32/prd.md`
- Addendum: `_bmad-output/planning-artifacts/prds/prd-Athena-2026-07-22-m32/addendum.md`
- Architecture: `_bmad-output/planning-artifacts/architecture/architecture-Athena-2026-07-22-m32/ARCHITECTURE-SPINE.md`
- Epics: `_bmad-output/implementation-artifacts/m32/epics.md`
- Sprint: `_bmad-output/implementation-artifacts/m32/sprint-status.yaml`
- Cleanup ledger: `_bmad-output/implementation-artifacts/m32/cleanup-ledger.md`
- Previous story: `_bmad-output/implementation-artifacts/m32/1-1-define-engineering-package-descriptor-v0.md`
- Source drafts:
  - `draft/layouts/003-presentation-language.md`
  - `draft/layouts/004-m32-draft.md`

## Acceptance Criteria

1. Given a Representation Package Descriptor, when it is parsed and validated, then it carries
   stable package id, group/artifact/version identity, supported Presentation Profile ids/tags,
   descriptor entries, Graphic Resource references, style token references, variants, previews,
   lifecycle, and provenance.
2. Given a representation package declares engineering source mutation rules, domain truth,
   semantic compiler behavior, engineering package catalog facts, or `.athena` syntax authority,
   when validation runs, then it fails with structured diagnostics naming the semantic-leak
   authority violation.
3. Given one Graphic Resource backend is implemented first, when other future resource kinds appear,
   then they are represented as explicit unsupported/deferred-compatible diagnostics rather than
   silent renderer success.
4. Given Story 1.1 established `kernel:package-model`, when Representation Package v0 is added,
   then it reuses compatible package identity/provenance/diagnostic patterns without merging
   Engineering Package, Presentation Profile, Representation Package, or Graphic Resource
   authority.
5. Mandatory Polish/Purge Gate complete with AC-to-evidence mapping.

## Tasks/Subtasks

- [x] Add RED contract tests for a valid Representation Package Descriptor v0. (AC: 1,4)
- [x] Add RED validation tests for semantic leaks: source mutation rules, engineering truth,
  compiler behavior, `.athena` syntax, and engineering package catalog fields. (AC: 2,4)
- [x] Add RED validation tests proving unsupported Graphic Resource kinds are diagnosed as
  deferred/unsupported and not accepted as successful renderer resources. (AC: 3)
- [x] Implement representation package id, coordinates, supported profiles, descriptor entries,
  resource references, style tokens, variants, previews, lifecycle, provenance, and forbidden
  semantic authority fields in `kernel:package-model`. (AC: 1,4)
- [x] Implement deterministic Representation Package validation diagnostics for required fields,
  semantic leaks, invalid references, and unsupported resource kinds. (AC: 2,3)
- [x] Update package platform documentation to include Representation Package boundary and Graphic
  Resource kind policy. (AC: 1..4)
- [x] Run focused package-model tests sequentially, then full regression sequentially; do not run
  Gradle concurrently on Windows. (AC: 1..4)
- [x] Complete mandatory polish/purge review and record final evidence. (AC: 5)

## Dev Notes

- M32 authority separation is binding:

  ```text
  Engineering Package != Presentation Profile != Representation Package != Graphic Resource
  ```

- Representation Package owns view/resource contracts only. It may reference descriptors, resource
  handles, style tokens, variants, previews, bounds-ready metadata, and provenance. It must not own
  product facts, semantic truth, source mutation behavior, compiler rules, or `.athena` syntax.
- Graphic Resource is a generic abstraction. Do not make architecture SVG-specific. One concrete
  backend may be supported first for M32, but future backends must be modeled explicitly as
  unsupported/deferred-compatible diagnostics.
- Story 1.1 created `kernel/package-model` and established:
  - `EngineeringPackageModels.kt` for closely related value/data contracts.
  - `EngineeringPackageValidation.kt` for stable validation diagnostics.
  - `EngineeringPackageDescriptorContractTest.kt` for package contract tests.
  - `docs/usages/engineering-package-platform.md` for boundary documentation.
- Keep Story 1.2 in the same platform module unless fresh inspection proves a stronger existing
  package boundary. Do not put this contract in Theia, renderer, compiler-only knowledge package
  infrastructure, or `.athena` grammar.
- Recommended diagnostic code style:
  - `package.representation.identity.invalid`
  - `package.representation.version.invalid`
  - `package.representation.profile.invalid`
  - `package.representation.descriptor.invalid`
  - `package.representation.resource.invalid`
  - `package.representation.resource-kind.unsupported`
  - `package.representation.semantic-leak-forbidden`
  - `package.representation.provenance.missing`
- Use explicit authority names in diagnostics so downstream resolvers can later identify whether
  the failure belongs to representation package, resource, profile, manifest, compiler, or renderer.
- Run `powershell -ExecutionPolicy Bypass -File .\tools\encoding-audit.ps1` after text edits.

## Project Structure Notes

- This is a contract foundation story. It must not implement registry discovery, Binding Resolver,
  Presentation Profile, Representation Descriptor validation internals, renderer integration, or
  M32 demo assets except as minimal test fixtures needed for Representation Package validation.
- If new files are needed, prefer cohesive names such as `RepresentationPackageModels.kt`,
  `RepresentationPackageValidation.kt`, and `RepresentationPackageDescriptorContractTest.kt`.
- Related tiny DTO/value types may share files. Split validation behavior from model contracts.

## Testing Requirements

- Follow TDD: write failing package-model tests before production code.
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
  `:kernel:package-model:compileTestKotlin` with unresolved
  `RepresentationPackageDescriptorValidator`, `RepresentationPackageDescriptor`, and related
  representation package model types.
- GREEN: `.\gradlew.bat --no-daemon --console=plain :kernel:package-model:test` passed after
  adding Representation Package Descriptor v0 models and validator.
- REGRESSION: `.\gradlew.bat --no-daemon --console=plain check` passed after documentation and
  contract implementation.
- ENCODING: `powershell -ExecutionPolicy Bypass -File .\tools\encoding-audit.ps1` passed.

### Completion Notes List

- Added Representation Package Descriptor v0 models in `kernel:package-model` for package identity,
  supported Presentation Profiles, descriptor entries, Graphic Resource refs, style token refs,
  variants, previews, lifecycle, provenance, and forbidden semantic authority fields.
- Added deterministic Representation Package validation diagnostics for invalid identity/profile,
  descriptor/resource references, unsupported deferred Graphic Resource kinds, semantic leaks, and
  missing provenance.
- Added contract tests proving valid descriptors, semantic-leak rejection, and unsupported resource
  kind diagnostics.
- Updated package platform documentation with Representation Package boundary and Graphic Resource
  policy.
- AC-to-evidence: AC1 covered by
  `representation package descriptor carries profile resource variant preview and provenance facts`;
  AC2 covered by
  `representation package validation rejects semantic source compiler and engineering truth leaks`;
  AC3 covered by
  `representation package validation diagnoses unsupported graphic resource kinds`; AC4 covered by
  keeping the contract in `kernel:package-model` with separate Representation Package types and
  Story 1.1 diagnostic style; AC5 covered by full `check`, encoding audit, `git status --short`,
  and no new cleanup-ledger entry.

### File List

- `_bmad-output/implementation-artifacts/m32/1-2-define-representation-package-descriptor-v0.md`
- `_bmad-output/implementation-artifacts/m32/sprint-status.yaml`
- `docs/usages/engineering-package-platform.md`
- `kernel/package-model/src/main/kotlin/com/engineeringood/athena/packageplatform/RepresentationPackageModels.kt`
- `kernel/package-model/src/main/kotlin/com/engineeringood/athena/packageplatform/RepresentationPackageValidation.kt`
- `kernel/package-model/src/test/kotlin/com/engineeringood/athena/packageplatform/RepresentationPackageDescriptorContractTest.kt`

## Change Log

- 2026-07-22: Story created for M32 Epic 1 after Story 1.1 established the package model boundary.
- 2026-07-22: Implemented Representation Package Descriptor v0 contract and validation tests.

## Mandatory Final Polish/Purge Gate

- Review touched and adjacent package model, representation model, compiler knowledge package,
  tests, documentation, fixtures, and sprint artifacts.
- Remove dead/stale package experiments, stale docs, duplicate fixtures, or misleading authority
  claims.
- Ledger any retained stale item in `_bmad-output/implementation-artifacts/m32/cleanup-ledger.md`
  with owner, reason, target milestone, and verification.
- Record AC-to-evidence mapping before moving the story beyond `review`.
