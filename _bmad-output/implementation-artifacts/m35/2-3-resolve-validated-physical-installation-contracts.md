---
baseline_commit: 14ad49515e95473328472db843722f7200fc1e91
---

# Story 2.3: Resolve Validated Physical Installation Contracts

Status: review

## Story

As an engineer,
I want installation constraints resolved into one validated contract,
so that Cabinet fit decisions never depend on visual geometry or unchecked product data.

## Acceptance Criteria

1. Given project physical facts and existing resolved physical traits, when `PhysicalInstallationContractResolver` runs, then project scalar fields override trait fields independently, set fields replace atomically, same-precedence duplicates fail, and every resolved field carries provenance.
2. Given `PhysicalInstallationContractV0`, when validation succeeds, then it contains positive width/height/depth, mounting type, non-empty allowed orientations, non-negative top/right/bottom/left clearance, and non-empty compatible container kinds in canonical digest order.
3. Given existing unchecked `PhysicalSize` trait input, when any dimension is invalid, then source-spanned diagnostics are emitted before validated M35 values are constructed, and no unchecked `PhysicalSize` enters the resolved contract or physical IR.
4. Given a visual Element, SVG body, namespace, or package metadata attempts to supply product or physical truth, when contract resolution runs, then that input is rejected or ignored by type boundary, and no Engineering Component System, manufacturer/article model, or new package kind is introduced.
5. Given all previous acceptance criteria are green, when the mandatory story gate runs, then RED/GREEN commands, AC-to-evidence mapping, and three-layer review are recorded, and duplicate physical DTOs, inferred geometry facts, stale docs, fixtures, and compatibility code are purged.

## Tasks / Subtasks

- [x] Add RED tests for validated physical contract resolution (AC: 1, 2, 3)
  - [x] Cover project-over-trait scalar precedence for width, height, depth, mounting type, and each clearance side.
  - [x] Cover atomic replacement for orientation and compatible-container sets.
  - [x] Cover same-precedence duplicates, missing required fields, empty sets, invalid trait dimensions, and negative clearance diagnostics.
  - [x] Assert every resolved field carries provenance and canonical digest order is stable.
- [x] Implement `PhysicalInstallationContractV0` models in `kernel:physical-model` (AC: 2, 3, 4)
  - [x] Add validated measurement/value classes; keep unchecked `PhysicalSize` as resolver input only.
  - [x] Add closed ids/enums for mounting type, orientation, compatible container kind, source precedence, and source provenance.
  - [x] Do not add ECS, manufacturer/article/product identity, component package kind, package manifest, SVG metadata, renderer, or visual Element inputs.
- [x] Implement `PhysicalInstallationContractResolver` (AC: 1, 3, 4)
  - [x] Resolve project fields over trait fields independently for scalar values.
  - [x] Replace set-valued fields atomically by precedence.
  - [x] Fail closed on same-precedence duplicates, missing fields, empty required sets, non-positive dimensions, and negative clearance.
  - [x] Emit stable diagnostics with code, subject, source span where available, measured value, and expected constraint.
- [x] Polish/purge and evidence gate (AC: 5)
  - [x] Audit touched and adjacent physical-model/compiler areas for duplicate DTOs, geometry-derived physical facts, XML paths, compatibility readers, ECS leakage, and visual/package authority leakage.
  - [x] Run sequential targeted and regression verification.
  - [x] Record RED/GREEN evidence, AC-to-evidence mapping, and three-layer review.

## Dev Notes

### Scope Boundary

Story 2.3 owns only the minimal validated physical contract and pure resolver in `kernel:physical-model`. It does not build `PhysicalInstallationIR v0`, physical topology, fit/collision evaluation, Cabinet composition, routing, renderer output, editable selection, ECS, or a new package/resource authority.

### Architecture Requirements

- `kernel:physical-model` owns validated measurements, `PhysicalInstallationContractV0`, resolver diagnostics, contract digest, and source provenance.
- The resolver may consume explicit governed project facts and existing `ResolvedPhysicalTraitDefinition`/`PhysicalSize` trait inputs.
- Existing `PhysicalSize` is unchecked legacy trait input only. Invalid values must fail before any resolved contract is created.
- Project values override trait values field-by-field for width, height, depth, mounting type, and clearance sides.
- Set fields replace atomically by precedence: `allowedOrientations` and `compatibleContainerKinds`.
- Duplicate values at the same precedence are ambiguous and fail closed.
- Missing values, empty required sets, non-positive dimensions, and negative clearance fail closed.
- Canonical digest field order: size width/height/depth, mounting type, orientations by enum order, clearance top/right/bottom/left, container kinds by typed id.
- Visual Elements, SVG, namespace, package metadata, renderer geometry, labels, DOM ids, and presentation payloads must not be inputs to this resolver.
- M35 must not introduce product catalog syntax, manufacturer/article fields, a component package kind, a Physical Installation Reference Package, or a parallel package resolver.

### Previous Story Intelligence

- Story 2.2 added syntax-only `installation cabinet` AST nodes. Do not store validated physical contracts in language AST.
- Story 2.2 deliberately left route geometry, topology, rendering, and source mutation out of scope. Keep that boundary here.
- Existing exhaustive consumers may need compile updates when new physical-model types are added, but this story should not change language grammar unless required by compile errors.
- Gradle verification must run sequentially on Windows.

### Likely Code Areas

- `kernel/physical-model/src/main/kotlin/com/engineeringood/athena/physical/PhysicalTraitModels.kt`
- `kernel/physical-model/src/main/kotlin/com/engineeringood/athena/physical/*InstallationContract*.kt`
- `kernel/physical-model/src/test/kotlin/com/engineeringood/athena/physical/*InstallationContract*Test.kt`
- `kernel/physical-model/build.gradle.kts`
- `kernel/compiler` only if orchestration compile errors require adapting to new physical-model API.

### Testing Requirements

- Follow RED/GREEN: write failing tests before production code.
- Minimum verification:
  - `.\gradlew.bat --no-daemon --console=plain :kernel:physical-model:test --tests com.engineeringood.athena.physical.PhysicalInstallationContractResolverTest`
  - `.\gradlew.bat --no-daemon --console=plain :kernel:physical-model:test`
  - targeted compile/test for any downstream module touched
  - audit for XML/ECS/visual authority leakage in touched paths
  - `powershell -ExecutionPolicy Bypass -File .\tools\encoding-audit.ps1`
  - `git diff --check`

### References

- `_bmad-output/implementation-artifacts/m35/epics.md` - Epic 2, Story 2.3.
- `_bmad-output/planning-artifacts/prds/prd-Athena-2026-07-27-m35/prd.md` - FR-23, FR-25..FR-27.
- `_bmad-output/planning-artifacts/prds/prd-Athena-2026-07-27-m35/addendum.md` - Physical Installation Contract And Future ECS Boundary.
- `_bmad-output/planning-artifacts/architecture/architecture-Athena-2026-07-27-m35/ARCHITECTURE-SPINE.md` - AD-2, AD-3, conventions, module ownership.
- `_bmad-output/implementation-artifacts/m35/2-2-author-type-safe-cabinet-installation-source.md` - previous story implementation evidence.

## Dev Agent Record

### Agent Model Used

GPT-5 Codex

### Debug Log References

- 2026-07-28T03:44:20+08:00 - Created Story 2.3 from M35 Epic 2 backlog and started implementation from baseline `14ad49515e95473328472db843722f7200fc1e91`.
- RED evidence: `.\gradlew.bat --no-daemon --console=plain :kernel:physical-model:test --tests com.engineeringood.athena.physical.PhysicalInstallationContractResolverTest` failed at test compile with unresolved `PhysicalInstallationContractResolver`, `PhysicalInstallationContractField`, and `PhysicalInstallationContractValue`.
- GREEN evidence: `.\gradlew.bat --no-daemon --console=plain :kernel:physical-model:test --tests com.engineeringood.athena.physical.PhysicalInstallationContractResolverTest` passed.
- Regression evidence: `.\gradlew.bat --no-daemon --console=plain :kernel:physical-model:test` passed.
- Audit evidence: `rg -n "XML|xml|SVG|svg|Element|element|manufacturer|article|product catalog|package kind|manifest" kernel/physical-model/src/main/kotlin kernel/physical-model/src/test/kotlin kernel/physical-model/README.md` returned only explicit negative README boundary text and package/component words inside namespace or semantic identity examples.
- Audit evidence: `powershell -ExecutionPolicy Bypass -File .\tools\encoding-audit.ps1` passed.
- Audit evidence: `git diff --check` passed with existing CRLF normalization warnings only.

### Completion Notes List
- Added closed `PhysicalInstallationContractV0` models with validated positive dimensions, non-negative clearance, mounting type, orientation set, container-kind set, field provenance, canonical digest material, and SHA-256 digest.
- Added `PhysicalInstallationContractResolver` with project-over-trait scalar precedence, atomic set replacement, same-precedence ambiguity diagnostics, missing/empty/invalid fail-closed behavior, and adapter facts from existing resolved physical traits.
- Kept `PhysicalSize` as unchecked trait input only; resolved contracts use validated M35 measurement value classes.
- Updated `kernel:physical-model` README to document M35 contract ownership and explicitly exclude renderer, SVG, package metadata, product catalog, and ECS authority.
- AC mapping: AC1 covered by resolver precedence/provenance and atomic-set tests; AC2 covered by validation and canonical digest tests; AC3 covered by invalid trait input tests and validated value output types; AC4 covered by resolver input type boundary and README/audit; AC5 covered by RED/GREEN, module regression, encoding, whitespace, and authority leakage audit.
- Three-layer review: semantic layer accepts only governed project facts and existing traits; physical layer owns validation and provenance in `kernel:physical-model`; visual/package layer has no API path into contract resolution.

### File List

- `_bmad-output/implementation-artifacts/m35/2-3-resolve-validated-physical-installation-contracts.md`
- `_bmad-output/implementation-artifacts/m35/sprint-status.yaml`
- `kernel/physical-model/README.md`
- `kernel/physical-model/src/main/kotlin/com/engineeringood/athena/physical/PhysicalInstallationContractModels.kt`
- `kernel/physical-model/src/main/kotlin/com/engineeringood/athena/physical/PhysicalInstallationContractResolver.kt`
- `kernel/physical-model/src/test/kotlin/com/engineeringood/athena/physical/PhysicalInstallationContractResolverTest.kt`

### Change Log

- 2026-07-28 - Created Story 2.3 implementation guide and moved story to in-progress.
- 2026-07-28 - Completed Story 2.3 validated physical installation contract models, resolver, tests, documentation, and verification evidence.
