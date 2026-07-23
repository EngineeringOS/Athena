---
status: review
baseline_commit: 8d12ba03c05b373b98fbc513e6c7f3f6fce962d6
story_id: 4.3
epic: 4
title: Route Through Descriptor Anchors
---

# Story 4.3: Route Through Descriptor Anchors

## Status

Review

## Story

As an Athena spatial/routing compiler,
I want terminal routes attached to descriptor anchors through binding policy,
so that package-backed drawings do not fall back to component centers.

## Required Context

- PRD: `_bmad-output/planning-artifacts/prds/prd-Athena-2026-07-22-m32/prd.md`
- Architecture: `_bmad-output/planning-artifacts/architecture/architecture-Athena-2026-07-22-m32/ARCHITECTURE-SPINE.md`
- Epics: `_bmad-output/implementation-artifacts/m32/epics.md`
- Previous story: `_bmad-output/implementation-artifacts/m32/4-2-render-graphic-resources-as-descriptor-backed-resources.md`

## Acceptance Criteria

1. Given a relationship between semantic terminals with mapped descriptor anchors, when routing
   runs, then endpoints use the resolved terminal anchors and route facts name their descriptor
   anchor evidence.
2. Given an anchor, descriptor, or label slot is missing, when routing or projection runs, then the
   failure is diagnostic and no center-fallback route is accepted as success.
3. Given package-backed routes are reviewed, when renderer/resource data is inspected, then raw
   Graphic Resource geometry, file names, labels, and CSS do not become routing authority.
4. Given the story implementation is complete, when spatial, routing, representation, proof, and
   docs are reviewed, then stale center-fallback assumptions are removed or ledgered and AC
   evidence is recorded.
5. Mandatory Polish/Purge Gate complete.

## Tasks/Subtasks

- [x] Add RED tests for descriptor-anchor route evidence using source/target semantic terminal
  mappings. (AC: 1,3)
- [x] Add RED tests for missing source anchor, target anchor, descriptor, or label facts producing
  diagnostics and rejecting center fallback. (AC: 2)
- [x] Implement descriptor-anchor route payload/mapper or routing bridge at the package/runtime
  boundary. (AC: 1..3)
- [x] Document descriptor-anchor routing and center-fallback rejection. (AC: 3,4)
- [x] Run focused tests sequentially, then full regression sequentially; do not run Gradle
  concurrently on Windows. (AC: 1..4)
- [x] Complete mandatory polish/purge review and record final evidence. (AC: 4,5)

## Dev Notes

- Start with CodeGraph review of existing routing/spatial contracts before editing.
- Consume Binding Evidence anchor summaries and Representation Descriptor anchors. Do not derive
  endpoint anchors from Graphic Resource internals, file names, visible labels, DOM, or CSS.
- A missing anchor must be diagnostic. Do not create a center fallback route as success.
- This story may produce structured route proof/payloads first; downstream renderer integration can
  consume them later.
- Run `powershell -ExecutionPolicy Bypass -File .\tools\encoding-audit.ps1` after text edits.

## Testing Requirements

- Follow TDD: write failing descriptor-anchor route tests before production code.
- Focused command should target the module touched by implementation.
- Full regression command after story completion:
  `.\gradlew.bat --no-daemon --console=plain check`.
- Encoding audit command after docs/text edits:
  `powershell -ExecutionPolicy Bypass -File .\tools\encoding-audit.ps1`.

## Dev Agent Record

### Agent Model Used

Codex GPT-5

### Debug Log References

- RED: `.\gradlew.bat --no-daemon --console=plain :kernel:package-runtime:test` failed before implementation because `DescriptorAnchorRouteEvidenceMapper` and `DescriptorAnchorRouteEvidenceRequest` did not exist.
- GREEN: `.\gradlew.bat --no-daemon --console=plain :kernel:package-runtime:test` passed after adding descriptor-anchor route evidence DTOs and mapper behavior.
- REFACTOR VERIFY: `.\gradlew.bat --no-daemon --console=plain :kernel:package-runtime:test` passed after documentation edits.
- REGRESSION: `.\gradlew.bat --no-daemon --console=plain check` exited 0 with `BUILD SUCCESSFUL`.
- ENCODING: `powershell -ExecutionPolicy Bypass -File .\tools\encoding-audit.ps1` exited 0 with `Encoding audit passed.`
- PURGE: `git status --short` showed M32 artifacts, `settings.gradle.kts`, `docs/usages/engineering-package-platform.md`, `kernel/package-model/`, and `kernel/package-runtime/`; `.tools` was not present.

### Completion Notes List

- Added descriptor-anchor route evidence request/result payloads that map route endpoints through binding evidence anchors and representation descriptor anchors.
- Added fail-closed diagnostics for missing source/target descriptor anchors and verified no center-fallback route is accepted as success.
- Documented descriptor-anchor routing authority and resource-internal routing rejection in the M32 package platform usage guide.
- AC evidence:
  - AC1: `DescriptorAnchorRouteEvidenceTest` verifies source/target route points use mapped descriptor anchors.
  - AC2: `DescriptorAnchorRouteEvidenceTest` verifies missing descriptor anchors produce diagnostics and unsuccessful results.
  - AC3: `DescriptorAnchorRouteEvidenceMapper` consumes binding evidence and descriptor anchors only; docs state Graphic Resource internals, labels, file names, DOM, and CSS are not route authority.
  - AC4: full regression and documentation review completed; no new stale center-fallback assumption was retained.
  - AC5: encoding audit and purge/status gate completed.

### File List

- `_bmad-output/implementation-artifacts/m32/4-3-route-through-descriptor-anchors.md`
- `_bmad-output/implementation-artifacts/m32/sprint-status.yaml`
- `docs/usages/engineering-package-platform.md`
- `kernel/package-runtime/src/main/kotlin/com/engineeringood/athena/packageruntime/DescriptorAnchorRouteEvidence.kt`
- `kernel/package-runtime/src/main/kotlin/com/engineeringood/athena/packageruntime/DescriptorAnchorRouteEvidenceMapper.kt`
- `kernel/package-runtime/src/test/kotlin/com/engineeringood/athena/packageruntime/DescriptorAnchorRouteEvidenceTest.kt`

## Change Log

- 2026-07-22: Story created for M32 Epic 4 after descriptor-backed render payload proof.
- 2026-07-22: Implemented descriptor-anchor route evidence and marked story ready for review after focused/runtime/full verification.

## Mandatory Final Polish/Purge Gate

- Review touched and adjacent routing, spatial, package runtime, representation, docs, tests,
  fixtures, and sprint artifacts.
- Remove stale center-fallback assumptions or resource-internal routing authority claims.
- Ledger any retained stale item in `_bmad-output/implementation-artifacts/m32/cleanup-ledger.md`
  with owner, reason, target milestone, and verification.
- Record AC-to-evidence mapping before moving the story beyond `review`.
