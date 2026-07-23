---
status: review
baseline_commit: 8d12ba03c05b373b98fbc513e6c7f3f6fce962d6
story_id: 3.1
epic: 3
title: Define Presentation Profile V0
---

# Story 3.1: Define Presentation Profile V0

## Status

Review

## Story

As an Athena package platform developer,
I want Presentation Profile to be independent of engineering and representation packages,
so that standards, customer styles, and output contexts can change appearance without changing
engineering truth.

## Required Context

- PRD: `_bmad-output/planning-artifacts/prds/prd-Athena-2026-07-22-m32/prd.md`
- Addendum: `_bmad-output/planning-artifacts/prds/prd-Athena-2026-07-22-m32/addendum.md`
- Architecture: `_bmad-output/planning-artifacts/architecture/architecture-Athena-2026-07-22-m32/ARCHITECTURE-SPINE.md`
- Epics: `_bmad-output/implementation-artifacts/m32/epics.md`
- Sprint: `_bmad-output/implementation-artifacts/m32/sprint-status.yaml`
- Previous retrospective: `_bmad-output/implementation-artifacts/m32/epic-2-retro-2026-07-22.md`

## Acceptance Criteria

1. Given a Presentation Profile, when it is parsed and validated, then it carries profile id,
   version, intended projection contexts, style profile, representation standard tags, package
   compatibility constraints, fallback policy, and provenance.
2. Given a profile contains engineering truth, product parameters, Graphic Resource internals, or
   source mutation behavior, when validation runs, then it fails with diagnostics naming the
   authority-boundary violation.
3. Given IEC, ANSI, customer, compact, print, maintenance, training, or theme policies are modeled,
   when package validation runs, then they are profile facts rather than Engineering Package or
   Representation Package internals.
4. Given existing `kernel:presentation-policy-model` M30 profiles exist, when M32 Presentation
   Profile v0 is added, then it documents the boundary and does not break M30 composer contracts.
5. Mandatory Polish/Purge Gate complete with AC-to-evidence mapping.

## Tasks/Subtasks

- [x] Add RED tests for a valid Presentation Profile v0 descriptor. (AC: 1,3,4)
- [x] Add RED validation tests for engineering truth, product parameters, Graphic Resource
  internals, and source mutation authority leaks. (AC: 2)
- [x] Add RED tests proving standard/customer/output/theme facts live in Presentation Profile, not
  Engineering or Representation Package descriptors. (AC: 3)
- [x] Implement Presentation Profile v0 model and validation contracts in the package platform
  boundary. (AC: 1..4)
- [x] Update docs to distinguish M32 Presentation Profile descriptors from existing M30
  `PresentationPolicyProfile`. (AC: 4)
- [x] Run focused package-model tests sequentially, then full regression sequentially; do not run
  Gradle concurrently on Windows. (AC: 1..4)
- [x] Complete mandatory polish/purge review and record final evidence. (AC: 5)

## Dev Notes

- Presentation Profile is independent policy:

  ```text
  Engineering Package -> Presentation Profile -> Binding Resolver -> Representation Package
  ```

- Do not add `.athena` syntax for profile choice in this story.
- Do not move policy into Engineering Package or Representation Package descriptors.
- Existing `kernel:presentation-policy-model` has M30 runtime policy classes such as
  `PresentationPolicyProfile` and `AthenaIndustrialControlV0Profile`. M32 Presentation Profile v0
  is a package-platform descriptor contract and should not rewrite that runtime model in this story.
- Suggested diagnostic codes:
  - `package.presentation-profile.identity.invalid`
  - `package.presentation-profile.version.invalid`
  - `package.presentation-profile.context.invalid`
  - `package.presentation-profile.authority-forbidden`
  - `package.presentation-profile.provenance.missing`
- Run `powershell -ExecutionPolicy Bypass -File .\tools\encoding-audit.ps1` after text edits.

## Project Structure Notes

- Preferred implementation location: `kernel/package-model/src/main/kotlin/.../packageplatform`.
- Prefer files such as `PresentationProfileModels.kt`,
  `PresentationProfileValidation.kt`, and `PresentationProfileContractTest.kt`.

## Testing Requirements

- Follow TDD: write failing Presentation Profile tests before production code.
- Focused command: `.\gradlew.bat --no-daemon --console=plain :kernel:package-model:test`.
- Full regression command after story completion: `.\gradlew.bat --no-daemon --console=plain check`.
- Encoding audit command after docs/text edits:
  `powershell -ExecutionPolicy Bypass -File .\tools\encoding-audit.ps1`.

## Dev Agent Record

### Agent Model Used

Codex GPT-5

### Debug Log References

- RED: `.\gradlew.bat --no-daemon --console=plain :kernel:package-model:test` failed in
  `:kernel:package-model:compileTestKotlin` with unresolved
  `PresentationProfileDescriptorValidator`, `PresentationProfileDescriptor`, and related profile
  model types.
- GREEN: `.\gradlew.bat --no-daemon --console=plain :kernel:package-model:test` passed after
  adding Presentation Profile v0 model and validator.
- REGRESSION: `.\gradlew.bat --no-daemon --console=plain check` passed after profile
  documentation and implementation.
- ENCODING: `powershell -ExecutionPolicy Bypass -File .\tools\encoding-audit.ps1` passed.

### Completion Notes List

- Added package-platform Presentation Profile v0 models for profile id, version, projection
  contexts, style profile, standard tags, compatibility constraints, fallback policy, provenance,
  policy facts, and forbidden authority fields.
- Added Presentation Profile validation diagnostics for invalid identity/version/context/style,
  invalid compatibility constraints, missing provenance, invalid policy facts, and forbidden
  authority leaks.
- Added tests proving standards, customer, output, and theme policy facts live in Presentation
  Profile descriptors.
- Documented the difference between M32 `PresentationProfileDescriptor` and existing M30
  `PresentationPolicyProfile`.
- AC-to-evidence: AC1 covered by
  `presentation profile carries context style standard compatibility fallback and provenance facts`;
  AC2 covered by
  `presentation profile validation rejects engineering resource and source mutation authority leaks`;
  AC3 covered by
  `presentation policy facts model standards customers outputs and themes as profile facts`; AC4
  covered by docs and untouched `kernel:presentation-policy-model` contracts; AC5 covered by full
  `check`, encoding audit, `git status --short`, and no new cleanup-ledger entry.

### File List

- `_bmad-output/implementation-artifacts/m32/3-1-define-presentation-profile-v0.md`
- `_bmad-output/implementation-artifacts/m32/sprint-status.yaml`
- `docs/usages/engineering-package-platform.md`
- `kernel/package-model/src/main/kotlin/com/engineeringood/athena/packageplatform/PresentationProfileModels.kt`
- `kernel/package-model/src/main/kotlin/com/engineeringood/athena/packageplatform/PresentationProfileValidation.kt`
- `kernel/package-model/src/test/kotlin/com/engineeringood/athena/packageplatform/PresentationProfileContractTest.kt`

## Change Log

- 2026-07-22: Story created for M32 Epic 3 after package runtime foundation.
- 2026-07-22: Implemented Presentation Profile v0 package-platform contract and validation tests.

## Mandatory Final Polish/Purge Gate

- Review touched and adjacent package model, presentation-policy-model, package runtime, tests,
  documentation, fixtures, and sprint artifacts.
- Remove dead/stale profile experiments, stale docs, duplicate fixtures, or misleading authority
  claims.
- Ledger any retained stale item in `_bmad-output/implementation-artifacts/m32/cleanup-ledger.md`
  with owner, reason, target milestone, and verification.
- Record AC-to-evidence mapping before moving the story beyond `review`.
