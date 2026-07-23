---
status: review
baseline_commit: 8d12ba03c05b373b98fbc513e6c7f3f6fce962d6
story_id: 1.1
epic: 1
title: Define Engineering Package Descriptor V0
---

# Story 1.1: Define Engineering Package Descriptor V0

## Status

Review

## Story

As an Athena platform developer,
I want a frontend-neutral Engineering Package Descriptor contract,
so that reusable product and concept knowledge can be loaded without editing compiler source.

## Required Context

- PRD: `_bmad-output/planning-artifacts/prds/prd-Athena-2026-07-22-m32/prd.md`
- Addendum: `_bmad-output/planning-artifacts/prds/prd-Athena-2026-07-22-m32/addendum.md`
- Architecture: `_bmad-output/planning-artifacts/architecture/architecture-Athena-2026-07-22-m32/ARCHITECTURE-SPINE.md`
- Epics: `_bmad-output/implementation-artifacts/m32/epics.md`
- Sprint: `_bmad-output/implementation-artifacts/m32/sprint-status.yaml`
- Cleanup ledger: `_bmad-output/implementation-artifacts/m32/cleanup-ledger.md`
- Source drafts:
  - `draft/layouts/003-presentation-language.md`
  - `draft/layouts/004-m32-draft.md`

## Acceptance Criteria

1. Given an Engineering Package Descriptor, when it is parsed and validated, then it carries stable
   package id, group/artifact/version identity, kind, concept identity, product definitions,
   templates, parameters, defaults, validation references, relationship capabilities, lifecycle,
   documentation/resource references, and provenance.
2. Given a descriptor contains representation primitives, Graphic Resource references, anchors,
   style, viewBox, Presentation IR, renderer resource fields, or source mutation rules, when
   validation runs, then it fails with structured diagnostics naming the forbidden field and failed
   authority.
3. Given the existing governed knowledge package model in `kernel/compiler/.../knowledge`, when
   M32 package contracts are introduced, then the implementation reuses compatible naming and
   validation patterns where they fit but does not bury Engineering Package v0 inside a
   compiler-only artifact boundary if the architecture needs platform-level contracts.
4. Given invalid package id, invalid version, unsupported concept binding, invalid parameter schema,
   missing provenance, or missing package kind, when validation runs, then the package is rejected
   deterministically and no package-resolution success fact is produced.
5. Mandatory Polish/Purge Gate complete with AC-to-evidence mapping.

## Tasks/Subtasks

- [x] Add RED contract tests for valid Engineering Package Descriptor v0. (AC: 1)
- [x] Add RED validation tests for forbidden representation/resource/presentation/source-mutation
  fields. (AC: 2)
- [x] Inspect existing `AthenaKnowledgePackage*` compiler models and record whether the new
  contract should reuse, wrap, or remain separate from that boundary. (AC: 3)
- [x] Implement package id, group/artifact/version, descriptor, catalog/product/concept/template,
  parameter, validation reference, relationship capability, lifecycle, docs reference, provenance,
  and diagnostic models in the architecture-approved package boundary. (AC: 1,3,4)
- [x] Implement deterministic validation for required fields and forbidden authority leaks. (AC:
  2,4)
- [x] Add documentation or contract notes explaining Engineering Package != Presentation Profile !=
  Representation Package != Graphic Resource. (AC: 1,2,3)
- [x] Run focused package-model/compiler tests sequentially; do not run Gradle concurrently on
  Windows. (AC: 1..4)
- [x] Complete mandatory polish/purge review and record final evidence. (AC: 5)

## Dev Notes

- M32 architecture paradigm is package-resolved engineering platform:

  ```text
  .athena semantic source
    -> Engineering Resolver
    -> Engineering Package Descriptor
    -> Presentation Profile
    -> Binding Manifest / Binding Resolver
    -> Representation Resolver
    -> Representation Package Descriptor
    -> Representation Descriptor / Graphic Resource handle
    -> Presentation IR
  ```

- Do not add `.athena` syntax for packages, profiles, symbols, coordinates, anchors, Graphic
  Resources, or viewBox in this story.
- Do not put Graphic Resource internals into semantic kernel or Engineering Package contracts.
- Existing codegraph context found `AthenaKnowledgeArtifactPackage`,
  `AthenaKnowledgeArtifactManifest`, `AthenaKnowledgePayloadEntry`, and
  `AthenaKnowledgePackageDiagnostic` under
  `kernel/compiler/src/main/kotlin/com/engineeringood/athena/compiler/knowledge/`. Treat these as
  brownfield knowledge-package patterns and migration risk, not automatic placement for M32.
- Likely implementation areas after fresh inspection:
  - `kernel/package-model` if a new package contract module is needed;
  - `kernel/compiler/src/main/kotlin/com/engineeringood/athena/compiler/knowledge/` if existing
    governed package infrastructure is intentionally extended;
  - `kernel/representation-model` only for cross-references to future representation package
    contracts, not for engineering package truth.
- Keep Kotlin organization readable. Related tiny DTO/value types may share `*Models.kt` files, but
  split validation behavior from broad mixed-responsibility files if it grows.
- Diagnostics should be stable and authority-specific, for example:
  `package.engineering.identity.invalid`, `package.engineering.version.invalid`,
  `package.engineering.representation-field-forbidden`,
  `package.engineering.concept.unsupported`, `package.engineering.provenance.missing`.
- Run `powershell -ExecutionPolicy Bypass -File .\tools\encoding-audit.ps1` after text edits.

## Project Structure Notes

- This story is a contract foundation story. It should not implement local registry discovery,
  Binding Resolver, Presentation Profile, Representation Package, renderer integration, or M32 demo
  assets except as minimal test fixtures needed for Engineering Package validation.
- The semantic kernel owns engineering truth; package model contracts may be platform-owned, but
  Graphic Resource loading belongs downstream.
- Theia must not be touched for this story unless a stale doc/test falsely claims frontend package
  authority.

## Testing Requirements

- Prefer TDD: add failing model/validation tests before implementation.
- Focused test targets should cover package model validation first; run broader compiler/runtime
  tests only if touched.
- Gradle verification commands must run sequentially on Windows.
- AC evidence should include exact commands, passing test names, and any CodeGraph or fixed-string
  scan used to prove no representation fields entered Engineering Package contracts.

## Dev Agent Record

### Agent Model Used

TBD by dev-story agent.

### Debug Log References

- RED: `.\gradlew.bat --no-daemon --console=plain :kernel:package-model:test` failed in
  `:kernel:package-model:compileTestKotlin` with unresolved `EngineeringPackageDescriptor` and
  related package model types.
- GREEN: `.\gradlew.bat --no-daemon --console=plain :kernel:package-model:test` passed after
  adding the minimal package model and validator.
- REFACTOR/REGRESSION: `.\gradlew.bat --no-daemon --console=plain check` passed after package model
  cleanup and documentation.

### Completion Notes List

- Added new lightweight `kernel:package-model` module for platform-level package contracts.
- Added Engineering Package Descriptor v0 models for package coordinates, catalog kind, concepts,
  products, templates, parameters, validation refs, relationship capability refs, lifecycle,
  documentation refs, provenance, and explicit forbidden authority fields.
- Added deterministic validator diagnostics for invalid identity, group, artifact, version, missing
  kind, unsupported/blank concept, invalid parameter, missing provenance, and forbidden
  representation/presentation/source-mutation authority fields.
- Documented that existing M0 `kernel/compiler/.../knowledge` package models are a brownfield
  pattern, while M32 Engineering Package v0 remains a platform contract.
- AC-to-evidence: AC1 covered by
  `engineering package descriptor carries catalog and provenance facts without representation authority`;
  AC2 covered by
  `engineering package validation rejects representation and renderer authority fields`; AC3 covered
  by CodeGraph review of `AthenaKnowledgePackage*` and
  `docs/usages/engineering-package-platform.md`; AC4 covered by
  `engineering package validation rejects invalid identity version concept schema and provenance`;
  AC5 covered by leak scan, full `check`, and encoding audit.

### File List

- `_bmad-output/implementation-artifacts/m32/1-1-define-engineering-package-descriptor-v0.md`
- `_bmad-output/implementation-artifacts/m32/sprint-status.yaml`
- `docs/usages/engineering-package-platform.md`
- `kernel/package-model/build.gradle.kts`
- `kernel/package-model/src/main/kotlin/com/engineeringood/athena/packageplatform/EngineeringPackageModels.kt`
- `kernel/package-model/src/main/kotlin/com/engineeringood/athena/packageplatform/EngineeringPackageValidation.kt`
- `kernel/package-model/src/test/kotlin/com/engineeringood/athena/packageplatform/EngineeringPackageDescriptorContractTest.kt`
- `settings.gradle.kts`

## Change Log

- 2026-07-22: Story created for M32 after PRD review promoted Engineering Package Platform,
  Presentation Profile, and Binding Resolver.
- 2026-07-22: Implemented Engineering Package Descriptor v0 contract and validation tests.

## Mandatory Final Polish/Purge Gate

- Review touched and adjacent package model, compiler knowledge package, representation model,
  tests, documentation, fixtures, and sprint artifacts.
- Remove dead/stale package experiments, stale docs, duplicate fixtures, or misleading authority
  claims.
- Ledger any retained stale item in `_bmad-output/implementation-artifacts/m32/cleanup-ledger.md`
  with owner, reason, target milestone, and verification.
- Record AC-to-evidence mapping before moving the story beyond `review`.
