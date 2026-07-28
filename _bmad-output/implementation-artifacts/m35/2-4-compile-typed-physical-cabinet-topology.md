---
baseline_commit: 14ad49515e95473328472db843722f7200fc1e91
---

# Story 2.4: Compile Typed Physical Cabinet Topology

Status: review

## Story

As an engineer,
I want Cabinet containment and mounting intent compiled into one typed physical model,
so that enclosure, rails, ducts, terminals, and mounted equipment have explicit engineering structure.

## Acceptance Criteria

1. Given valid installation source and resolved physical contracts, when physical lowering runs, then `PhysicalInstallationIR v0` contains typed ids, source provenance, InstallationSpace, Enclosure, MountingSurface, Rail, Duct, RouteChannel, TerminalGroup, MountedOccurrence, route topology, and route intent, and it contains no final drawing coordinates, representation selection, anchor coordinates, or route segments.
2. Given v0 topology, when containment is validated, then Enclosure is the sole container; surfaces/ducts/terminal groups belong to it; rails belong to surfaces; channels belong to one duct; and occurrences target only surface, rail, or terminal group, and generic parent ids, cycles, duplicates, orphans, illegal mount targets, and implicit channels fail closed.
3. Given terminals mounted to a TerminalGroup, when IR is canonicalized, then terminals use ordinary MountedOccurrence records and the group orders keys by along-axis position, cross-axis position, then key.
4. Given horizontal and vertical physical infrastructure, when frames are derived, then parent-local coordinates and rigid determinant-`+1` Rail bases match AD-5, and source never stores pixels or drawing-grid coordinates.
5. Given all previous acceptance criteria are green, when the mandatory story gate runs, then RED/GREEN commands, AC-to-evidence mapping, and three-layer review are recorded, and competing Cabinet topology models, stale DTOs, fixtures, docs, and compatibility code are purged.

## Tasks / Subtasks

- [x] Add RED tests for PhysicalInstallationIR topology lowering (AC: 1, 2, 3, 4)
  - [x] Cover valid enclosure, surface, horizontal/vertical rail, duct, channel, terminal group, mounted occurrence, and route intent.
  - [x] Cover duplicates, orphan surfaces/rails/channels, illegal duct/channel mount targets, missing contract, duplicate occurrence semantic subject, and multiple enclosures.
  - [x] Assert terminal-group ordering by along-axis, cross-axis, then occurrence key.
  - [x] Assert rail frames are rigid with determinant `+1`.
- [x] Implement PhysicalInstallationIR v0 models in `kernel:physical-model` (AC: 1, 3, 4)
  - [x] Add typed ids, parent-local measurement records, target refs, frames, route intent, provenance, and diagnostics.
  - [x] Use validated `PhysicalInstallationContractV0`; no unchecked `PhysicalSize` enters IR.
  - [x] Do not add renderer coordinates, representation selection, anchor coordinates, route segments, or visual ids.
- [x] Implement Cabinet topology lowering/validation (AC: 1, 2, 3, 4)
  - [x] Lower from physical installation intent plus resolved contracts into IR.
  - [x] Enforce one enclosure, typed containment, legal mount targets, explicit route channels, and deterministic ordering.
  - [x] Derive horizontal and vertical rail frames without mirroring.
- [x] Polish/purge and evidence gate (AC: 5)
  - [x] Audit touched paths for duplicate topology DTOs, layout/renderer coordinate leakage, schematic routing leakage, XML, and compatibility paths.
  - [x] Run sequential verification and record evidence.

## Dev Notes

### Scope Boundary

Story 2.4 owns typed topology and route intent only. It does not perform fit/collision/clearance evaluation, route lane geometry, Cabinet visual transform, rendering, selection trace, authoring mutations, ECS, package manifests, or SVG parsing.

### Architecture Requirements

- `kernel:physical-model` owns `PhysicalInstallationIR v0`, ids, parent-local physical coordinates, topology diagnostics, route intent, source provenance, and frames.
- `kernel:compiler` may orchestrate lowering from `:kernel:language` AST into `:kernel:physical-model` records.
- Enclosure is the sole physical container.
- Surfaces, ducts, and terminal groups are enclosure-local.
- Rails are surface-local; channels are duct-local.
- Mounted occurrences target only surface, rail, or terminal group. Duct and channel are never mount targets.
- Terminals are ordinary mounted occurrences; terminal groups only own ordered occurrence keys.
- `InstallationOccurrenceKey` is `(sourceUnitId, installationDeclarationId, canonicalSemanticSubjectId)`.
- Physical IR contains no final drawing coordinates, representation selection, anchors, route segments, DOM ids, SVG ids, renderer terms, or pixels.
- Frames must be rigid transforms with determinant `+1`; vertical rails must not mirror graphics.
- No compatibility parser or legacy Cabinet topology path is added.

### Previous Story Intelligence

- Story 2.3 created validated physical contracts and resolver diagnostics in `kernel:physical-model`.
- Reuse `PhysicalInstallationContractV0`; do not reconstruct dimensions/clearance from `PhysicalSize`.
- Story 2.2 installation AST currently provides source intent and spans. If syntax and addendum differ, consume the implemented typed AST without adding broad grammar work here.

### Likely Code Areas

- `kernel/physical-model/src/main/kotlin/com/engineeringood/athena/physical/*InstallationTopology*.kt`
- `kernel/physical-model/src/test/kotlin/com/engineeringood/athena/physical/*InstallationTopology*Test.kt`
- `kernel/compiler/src/main/kotlin/com/engineeringood/athena/compiler/*Physical*.kt`
- `kernel/compiler/src/test/kotlin/com/engineeringood/athena/compiler/*Physical*.kt`
- `kernel/language/src/main/kotlin/com/engineeringood/athena/language/AthenaLanguageModel.kt` only if compile integration requires already-authored AST details.

### Testing Requirements

- Follow RED/GREEN.
- Minimum verification:
  - `.\gradlew.bat --no-daemon --console=plain :kernel:physical-model:test --tests com.engineeringood.athena.physical.PhysicalInstallationTopologyCompilerTest`
  - `.\gradlew.bat --no-daemon --console=plain :kernel:physical-model:test`
  - targeted `:kernel:compiler:test` if compiler lowering is added
  - authority/leakage audit
  - `powershell -ExecutionPolicy Bypass -File .\tools\encoding-audit.ps1`
  - `git diff --check`

### References

- `_bmad-output/implementation-artifacts/m35/epics.md` - Epic 2, Story 2.4.
- `_bmad-output/planning-artifacts/prds/prd-Athena-2026-07-27-m35/prd.md` - FR-18..FR-20, FR-23.
- `_bmad-output/planning-artifacts/prds/prd-Athena-2026-07-27-m35/addendum.md` - Physical Installation Model and source surface.
- `_bmad-output/planning-artifacts/architecture/architecture-Athena-2026-07-27-m35/ARCHITECTURE-SPINE.md` - AD-1, AD-4, AD-5, conventions.
- `_bmad-output/implementation-artifacts/m35/2-3-resolve-validated-physical-installation-contracts.md` - previous story contract evidence.

## Dev Agent Record

### Agent Model Used

GPT-5 Codex

### Debug Log References

- 2026-07-28T03:52:35+08:00 - Created Story 2.4 from M35 Epic 2 backlog and started implementation from baseline `14ad49515e95473328472db843722f7200fc1e91`.
- RED evidence: `.\gradlew.bat --no-daemon --console=plain :kernel:physical-model:test --tests com.engineeringood.athena.physical.PhysicalInstallationTopologyCompilerTest` failed at test compile with unresolved `PhysicalInstallationTopologyCompiler`, `PhysicalInstallationTopologyCompilation`, `PhysicalInstallationId`, and `PhysicalObjectId`.
- GREEN evidence: `.\gradlew.bat --no-daemon --console=plain :kernel:physical-model:test --tests com.engineeringood.athena.physical.PhysicalInstallationTopologyCompilerTest` passed after adding models/compiler.
- Regression evidence: `.\gradlew.bat --no-daemon --console=plain :kernel:physical-model:test` passed.
- Audit evidence: `rg -n "XML|xml|SVG|svg|Element|element|renderer|pixel|DOM|Presentation|GraphicPrimitive|route segment|manufacturer|article|product catalog|package kind|manifest" kernel/physical-model/src/main/kotlin kernel/physical-model/src/test/kotlin kernel/physical-model/README.md` returned only explicit negative boundary text and older physical-trait comments saying renderer is not owner.
- Audit evidence: `powershell -ExecutionPolicy Bypass -File .\tools\encoding-audit.ps1` passed.
- Audit evidence: `git diff --check` passed with existing CRLF normalization warnings only.

### Completion Notes List
- Added `PhysicalInstallationIRV0` topology models: typed source/install/object ids, enclosure, surfaces, rails, ducts, route channels, terminal groups, mounted occurrences, route intent, provenance, and diagnostics.
- Added `PhysicalInstallationTopologyCompiler` with one-enclosure validation, duplicate id detection, typed containment checks, legal mount target resolution, missing contract rejection, duplicate semantic-subject rejection, explicit route-channel validation, deterministic ordering, and non-mirroring rail frames.
- Terminal-group ordering uses ordinary mounted occurrence records and sorts by group orientation: horizontal uses X/Y/key; vertical uses Y/X/key.
- Physical IR consumes `PhysicalInstallationContractV0`; no unchecked `PhysicalSize`, representation selection, anchor coordinate, final drawing coordinate, route segment, renderer, SVG, DOM, or package metadata enters the model.
- Updated `kernel:physical-model` README with M35 topology ownership and explicit boundaries.
- AC mapping: AC1 covered by valid topology compile test; AC2 covered by duplicate/orphan/illegal-target/missing-contract/route-channel diagnostics; AC3 covered by terminal-group ordering test; AC4 covered by rail frame determinant/axis test; AC5 covered by RED/GREEN, module regression, encoding, whitespace, and leakage audit.
- Three-layer review: source layer remains intent/provenance only; physical layer owns typed topology and frames; drawing/route/representation layers still have no authority in this story.

### File List

- `_bmad-output/implementation-artifacts/m35/2-4-compile-typed-physical-cabinet-topology.md`
- `_bmad-output/implementation-artifacts/m35/sprint-status.yaml`
- `kernel/physical-model/README.md`
- `kernel/physical-model/src/main/kotlin/com/engineeringood/athena/physical/PhysicalInstallationTopologyCompiler.kt`
- `kernel/physical-model/src/main/kotlin/com/engineeringood/athena/physical/PhysicalInstallationTopologyModels.kt`
- `kernel/physical-model/src/test/kotlin/com/engineeringood/athena/physical/PhysicalInstallationTopologyCompilerTest.kt`

### Change Log

- 2026-07-28 - Created Story 2.4 implementation guide and moved story to in-progress.
- 2026-07-28 - Completed Story 2.4 typed physical Cabinet topology compiler, IR models, tests, documentation, and verification evidence.
