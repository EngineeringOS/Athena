---
story_key: 6-2-purge-stale-and-fabricated-authority-paths
epic: m37-e6
requirements: [FR-35, FR-36, NFR-1, NFR-5, NFR-8]
baseline_commit: 1b2e8b7a6854bc186c82f2e796cf8adf9d5efc62
---

# Story 6.2: Purge Stale And Fabricated Authority Paths

Status: review

## Story

As a platform maintainer,
I want conflicting production paths removed directly,
so that M37 has one understandable compiler architecture and proof cannot report invented success.

## Acceptance Criteria

1. Active professional drawing compilation contains no endpoint-derived Connection Intent fallback, renderer repair, fallback routing, hardcoded proof-success constants, stale XML authority, or compatibility shim.
2. Route constraints used by the active drawing path are compiler-owned hard rules or selected profile/policy facts with traceable IDs; no untraceable fixed injection remains in `AthenaProfessionalDrawingCompiler`.
3. Projection/view selection for the active M37 product path is driven by typed projection evidence and compiled presentation payloads; no stale dirty-document Cabinet override or hardcoded legacy surface alias controls the accepted product proof.
4. No `Proof`, `Demo`, `Sample`, milestone name, `V0`/`V1`, or test fixture exists in production `src/main`; reusable logic is named by responsibility and test evidence remains under `src/test`, E2E scripts, examples, or BMad artifacts.
5. No production class retains a deprecated parallel path solely to keep pre-M37 behavior working. Athena is pre-public; stale incompatible behavior is deleted or directly refactored.
6. Focused compiler/routing/frontend tests, source-set hygiene, encoding, and `git diff --check` pass; if product proof paths change, the M37 Electron smoke is rerun.

## Tasks / Subtasks

- [x] Add RED cleanup guard tests and audits (AC: 1, 2, 3, 4)
  - [x] Add or extend compiler/routing tests proving accepted M37 RouteFacts expose constraint/policy evidence instead of silent fixed constraint injection.
  - [x] Add or extend tests proving missing authored Connection Intent blocks route acceptance and no endpoint-derived classifier fallback runs in the active professional drawing path.
  - [x] Add or extend source-set/audit coverage if current tooling does not catch the stale authority pattern found.
- [x] Refactor route constraint authority (AC: 1, 2, 5)
  - [x] Replace private hardcoded `routeConstraints(connection)` injection in `AthenaProfessionalDrawingCompiler` with a named compiler-owned route hard-rule provider or selected profile/policy facts.
  - [x] Preserve traceable `RouteConstraintId`s and required constraint semantics for orthogonal routing, component-body avoidance, label clearance, and crossing policy.
  - [x] Do not weaken FR-32 zero-defect gates and do not add compatibility fallback.
- [x] Purge active fallback/fabricated authority paths (AC: 1, 3, 5)
  - [x] Confirm `ElectricalConnectionIntentClassifier` is not used by the active professional drawing compiler as fallback.
  - [x] Confirm LSP projection session has no stale Cabinet dirty-document override controlling M37 proof.
  - [x] Confirm proof fields for endpoint attachment, fallback absence, route/body intersection, label clearance, crossing, renderer purity, and source authority are computed from compiled facts and diagnostics.
  - [x] Delete or refactor stale path if encountered; do not wrap it.
- [x] Run global production hygiene audit (AC: 4, 5)
  - [x] Run source-set hygiene audit and inspect any finding directly.
  - [x] Search production `src/main` for `Proof`, `Demo`, `Sample`, `M3`, `M37`, `V0`, `V1`, `fallback`, `compatibility`, XML authority, and hardcoded proof success patterns; fix only active architecture violations.
  - [x] Keep test and milestone artifact names allowed under `src/test`, examples, E2E scripts, or `_bmad-output`.
- [x] Run verification and update record (AC: 6)
  - [x] Run focused compiler/routing tests for professional drawing and route constraints.
  - [x] Run focused LSP/frontend tests if projection payload or UI proof path changes.
  - [x] Run `powershell -ExecutionPolicy Bypass -File .\tools\source-set-hygiene-audit.ps1`.
  - [x] Run `powershell -ExecutionPolicy Bypass -File .\tools\encoding-audit.ps1`.
  - [x] Run `git diff --check`.
  - [x] Rerun `yarn --cwd ide start:smoke:m37` if product proof behavior changes.
  - [x] Update File List, Debug Log, Completion Notes, Change Log, story status, and sprint status only after evidence exists.

## Dev Notes

### Authority Boundary

- Athena source is SSOT. Renderer, SVG, XML, planner objects, compatibility adapters, and proof scripts cannot create engineering truth.
- The active M37 professional drawing path must require authored Connection Intent. Endpoint-derived intent classification can remain only if it is outside active professional drawing fallback behavior and covered as legacy-independent utility; delete if stale.
- Proof success must be computed from compiled facts and diagnostics. No constant `true` product proof fields.
- Pre-public rule applies: if old behavior conflicts with current architecture, refactor or delete it directly.

### Current Code Intelligence

- Active professional drawing route creation is in `kernel/compiler/src/main/kotlin/com/engineeringood/athena/compiler/AthenaProfessionalDrawingCompiler.kt`.
- Current route path already requires authored intent through `EngineeringConnectivityContractCompiler().compile(document)` and emits `drawing.route.intent.missing` when absent.
- Current hardcoded route constraints are in `AthenaProfessionalDrawingCompiler.routeConstraints(connection)` and emit required constraints for `ORTHOGONAL_ONLY`, `AVOID_COMPONENT_BODY`, `LABEL_CLEARANCE`, and `CROSSING_POLICY`.
- `ElectricalConnectionIntentClassifier` exists in `kernel/routing-model/src/main/kotlin/com/engineeringood/athena/routing/ElectricalConnectionIntent.kt`; CodeGraph showed callers/tests, but the active professional drawing compiler now uses authored intent conversion instead.
- Story 5.4 already removed stale dirty-document Cabinet projection override from `AthenaProjectionSessionProtocol.kt`; verify it stays removed rather than adding a new compatibility path.
- Source-set hygiene script is `tools/source-set-hygiene-audit.ps1`.

### Previous Story Intelligence

- Story 6.1 hardened SVG geometry-reference authority and added no compatibility path.
- Story 5.4 proved M37 E2E with zero route/body intersections, no fallback anchors, no loose endpoints, no label/title collisions, and renderer purity. Do not weaken those gates during cleanup.
- M36 cleanup incident: production `src/main` must not contain proof/demo/sample/milestone-named classes. Treat any production dependency on milestone-named code as architecture smell.

### Implementation Guidance

- Prefer one small named production concept for hard routing rules if needed, for example a professional drawing route-rule compiler/provider named by responsibility, not milestone.
- Keep route constraints traceable and deterministic. Moving hard rules out of `AthenaProfessionalDrawingCompiler` must not hide them.
- Do not introduce policy version suffixes like `V0` or `V1`.
- Do not create a parallel route-policy IR.
- If global search finds stale text in reference mirrors or third-party `reference/` folders, do not edit those unless production imports them.

### Required Commands

- `.\gradlew.bat --no-daemon --console=plain :kernel:compiler:test --tests com.engineeringood.athena.compiler.M37ProfessionalDrawingSurfaceTest`
- `.\gradlew.bat --no-daemon --console=plain :kernel:routing-model:test`
- `powershell -ExecutionPolicy Bypass -File .\tools\source-set-hygiene-audit.ps1`
- `powershell -ExecutionPolicy Bypass -File .\tools\encoding-audit.ps1`
- `git diff --check`

### References

- [Source: `_bmad-output/implementation-artifacts/m37/epics.md` - Story 6.2]
- [Source: `_bmad-output/planning-artifacts/prds/prd-Athena-2026-07-30-m37/prd.md` - FR-35, FR-36, NFR-1, NFR-5, NFR-8]
- [Source: `_bmad-output/planning-artifacts/prds/prd-Athena-2026-07-30-m37/addendum.md` - Direct Refactor Targets]
- [Source: `_bmad-output/implementation-artifacts/m37/5-4-prove-m37-end-to-end-with-screenshots.md` - computed proof and E2E lessons]
- [Source: `_bmad-output/implementation-artifacts/m37/6-1-harden-package-local-geometry-references.md` - SVG authority cleanup]
- [Source: `kernel/compiler/src/main/kotlin/com/engineeringood/athena/compiler/AthenaProfessionalDrawingCompiler.kt` - active route creation and route constraints]
- [Source: `kernel/routing-model/src/main/kotlin/com/engineeringood/athena/routing/ElectricalConnectionIntent.kt` - endpoint-derived classifier boundary]
- [Source: `tools/source-set-hygiene-audit.ps1` - production naming hygiene gate]

## Dev Agent Record

### Agent Model Used

Codex GPT-5

### Debug Log References

- 2026-07-31: RED `.\gradlew.bat --no-daemon --console=plain :kernel:compiler:test --tests com.engineeringood.athena.compiler.M37ProfessionalDrawingSurfaceTest` failed because RouteFacts lacked traceable compiler-owned hard-rule descriptions.
- 2026-07-31: GREEN focused M37 surface test passed after moving route hard rules into `ProfessionalDrawingRouteHardRules`.
- 2026-07-31: `.\gradlew.bat --no-daemon --console=plain :kernel:routing-model:test` initially failed in `DrawingProfileCompilerTest` because stale expected stroke classes ignored current M37 profile classes.
- 2026-07-31: Rerun `.\gradlew.bat --no-daemon --console=plain :kernel:routing-model:test` passed after aligning the stale test to current profile grammar.
- 2026-07-31: Production audit `rg -n --glob '*/src/main/**' '(Proof|Demo|Sample|\bM3[0-9]\b|M37|\bV0\b|\bV1\b)' .` returned no production findings.
- 2026-07-31: Production audit `rg -n --glob '*/src/main/**' '(hardcoded|success\s*=\s*true|proof.*true|fallback|compatibility|XML|Xml|xml)' kernel ide extensions integrations -g '!**/build/**'` returned no production findings.
- 2026-07-31: `powershell -ExecutionPolicy Bypass -File .\tools\source-set-hygiene-audit.ps1` passed.
- 2026-07-31: `powershell -ExecutionPolicy Bypass -File .\tools\encoding-audit.ps1` passed.
- 2026-07-31: `git diff --check` passed.

### Completion Notes List

- Ultimate context engine analysis completed - comprehensive developer guide created.
- Replaced private hardcoded route-constraint method in `AthenaProfessionalDrawingCompiler` with named compiler-owned `ProfessionalDrawingRouteHardRules`.
- RouteFacts now carry required constraints for orthogonal routing, body avoidance, label clearance, and crossing policy with explicit compiler-owned hard-rule descriptions.
- Verified active professional drawing path still requires authored Connection Intent and does not call endpoint-derived classifier fallback.
- Aligned stale route profile test expectations to the current M37 professional drawing grammar: power, control, protective-earth, safety, and communication stroke classes.
- Production source audit found no forbidden proof/demo/sample/milestone/version names and no obvious XML/fallback/compatibility/proof-success authority terms in active `src/main`.

### File List

- _bmad-output/implementation-artifacts/m37/6-2-purge-stale-and-fabricated-authority-paths.md
- _bmad-output/implementation-artifacts/m37/sprint-status.yaml
- kernel/compiler/src/main/kotlin/com/engineeringood/athena/compiler/AthenaProfessionalDrawingCompiler.kt
- kernel/compiler/src/main/kotlin/com/engineeringood/athena/compiler/ProfessionalDrawingRouteHardRules.kt
- kernel/compiler/src/test/kotlin/com/engineeringood/athena/compiler/M37ProfessionalDrawingSurfaceTest.kt
- kernel/routing-model/src/test/kotlin/com/engineeringood/athena/routing/DrawingProfileCompilerTest.kt

## Change Log

- 2026-07-31: Created implementation-ready Story 6.2 from finalized M37 PRD, addendum direct-refactor targets, epics, CodeGraph route/proof cleanup intelligence, and Stories 5.4/6.1 lessons.
- 2026-07-31: Purged untraceable professional drawing route hard-rule injection, aligned stale route profile test expectations, completed production authority audits, and moved story to review after focused tests and hygiene gates passed.
