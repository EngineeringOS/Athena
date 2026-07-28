---
baseline_commit: 14ad49515e95473328472db843722f7200fc1e91
---

# Story 4.1: Assemble The Dedicated M35 Cabinet Project

Status: review

## Story

As a customer evaluator,
I want one self-contained M35 Cabinet project with realistic standard and vendor material,
so that I can inspect the complete product path without switching views or assembling fixtures.

## Acceptance Criteria

1. Given `examples/m35/physical-installation-cabinet`, when repository/package validation runs, then project source, standard package, vendor/user package, resources, manifest, and lock follow approved hierarchy and authority contracts, and the project compiles offline with zero diagnostics.
2. Given governed sample source, when Physical IR and Cabinet composition compile, then the sample includes enclosure, mounting surface, horizontal and vertical Rail proof, ducts/channels, terminal group, mounted controls, labels, aliased connections, routes, native symbols, and one complex SVG-backed vendor Element.
3. Given the Athena IDE opens the sample workspace, when product initializes, then Cabinet is active and the only visible product surface, and Documentation, Schematic, Wiring, debug projections, fallback cards, and placeholder authoring controls do not appear.
4. Given package, physical, binding, composition, route, and trace proof, when fixture assertions run, then every required occurrence and connection is sourced from governed declarations and no hardcoded presentation mock supplies acceptance facts.
5. Given all previous acceptance criteria are green, when the mandatory story gate runs, then RED/GREEN commands, AC-to-evidence mapping, and three-layer review are recorded, and old M34 Cabinet samples/producers, flat paths, generated snapshots, XML logic, and duplicate fixtures are purged when no longer required for regression.

## Tasks / Subtasks

- [x] Add RED tests for the dedicated M35 project fixture (AC: 1..4)
  - [x] Cover repository root, package hierarchy, manifest, and lock presence.
  - [x] Cover required Cabinet declarations and one SVG-backed vendor Element.
  - [x] Cover Cabinet-only product surface configuration.
  - [x] Cover proof identity list for occurrences and aliased routes.
- [x] Assemble `examples/m35/physical-installation-cabinet` (AC: 1..2)
  - [x] Add governed source under Java-style package hierarchy.
  - [x] Add one standard package and one vendor/user package with local resources.
  - [x] Add `athena.yaml` and lock evidence without new manifest authority.
- [x] Wire the sample into the product default path (AC: 3..4)
  - [x] Make Cabinet the only visible M35 sample surface.
  - [x] Ensure proof data comes from governed sample declarations, not hardcoded mocks.
- [x] Polish/purge and evidence gate (AC: 5)
  - [x] Audit sample/product paths for XML, stale projections, placeholder controls, fallback cards, and compatibility paths.
  - [x] Run sequential verification and record evidence.

## Dev Notes

### Scope Boundary

Story 4.1 creates the dedicated M35 product sample and product-surface contract. It does not complete final visual polish or E2E screenshots; those belong to Stories 4.2 and 4.3.

### Architecture Requirements

- Cabinet is M35's only visible product surface.
- Athena source remains the single metadata authority.
- Package resources are local to their declaring package/source hierarchy.
- SVG is geometry material only; Athena source owns anchors, roles, labels, bindings, physical facts, and routes.
- No XML runtime authority, no compatibility adapters, no mock acceptance facts.

### Previous Story Intelligence

- Stories 1.1..1.5 established package hierarchy, immutable resources, and SVG-backed representation material.
- Stories 2.1..2.7 established typed installation, physical contracts, topology, transforms, and paint-only composition.
- Stories 3.1..3.4 established route topology, deterministic routes, occurrence trace, and governed selection/edit boundaries.

### References

- `_bmad-output/implementation-artifacts/m35/epics.md` - Epic 4, Story 4.1.
- `_bmad-output/planning-artifacts/prds/prd-Athena-2026-07-27-m35/prd.md` - FR-37..FR-38.
- `_bmad-output/planning-artifacts/architecture/architecture-Athena-2026-07-27-m35/ARCHITECTURE-SPINE.md` - Cabinet-only product path.

## Dev Agent Record

### Agent Model Used

GPT-5 Codex

### Debug Log References

- 2026-07-28T06:04:00+08:00 - Created Story 4.1 from Epic 4 backlog and started implementation from baseline `14ad49515e95473328472db843722f7200fc1e91`.
- 2026-07-28T06:28:00+08:00 - RED/GREEN: added `AthenaM35DedicatedCabinetSampleTest` for the dedicated project and watched the targeted compiler test fail before adding the sample material.
- 2026-07-28T06:42:00+08:00 - GREEN: dedicated M35 sample compiles offline with repository contract, package hierarchy, physical Cabinet declarations, SVG-backed ABB element, aliased routes, and governed proof assertions.
- 2026-07-28T06:50:00+08:00 - RED/GREEN: added `AthenaM35DedicatedCabinetProjectionSmokeTest`; targeted LSP smoke failed before the M35 sample was filtered to Cabinet-only product surface.
- 2026-07-28T07:22:00+08:00 - Verification: full `:kernel:compiler:test` and full `:ide:lsp:test` pass after aligning stale tests and authoring planner output to portable canonical connection IDs.
- 2026-07-28T07:25:00+08:00 - Audit: `examples/m35/physical-installation-cabinet` contains no XML authority and no Documentation/Schematic/Wiring/fallback/placeholder product-surface strings except the expected SVG geometry resource.

### Completion Notes List

- Assembled `examples/m35/physical-installation-cabinet` as the dedicated M35 Cabinet sample with Java-style `src` package hierarchy, `athena.yaml`, lock evidence, local standard and vendor representation packages, and package-local SVG geometry.
- Added compiler assertions proving the sample owns enclosure, mounting surface, horizontal/vertical rails, duct/channel, terminal group, mounted controls, labels, aliased connections, routes, native symbols, and the SVG-backed ABB PFEA112 element from governed declarations.
- Added LSP smoke coverage proving the dedicated M35 repository opens with Cabinet active as the only supported surface.
- Removed the path-dependent connection identity mismatch between lowering and authoring source edit planning by emitting portable source-unit IDs consistently.

### File List

- `_bmad-output/implementation-artifacts/m35/4-1-assemble-the-dedicated-m35-cabinet-project.md`
- `_bmad-output/implementation-artifacts/m35/sprint-status.yaml`
- `examples/m35/physical-installation-cabinet/athena.yaml`
- `examples/m35/physical-installation-cabinet/athena.lock`
- `examples/m35/physical-installation-cabinet/src/com/engineeringood/m35/physicalinstallationcabinet/01-physical-installation-cabinet.athena`
- `examples/m35/physical-installation-cabinet/packages/representation/com/engineeringood/m35/standard/iec/standard-elements.athena`
- `examples/m35/physical-installation-cabinet/packages/representation/com/engineeringood/m35/vendor/abb/pfea112/vendor-elements.athena`
- `examples/m35/physical-installation-cabinet/packages/representation/com/engineeringood/m35/vendor/abb/pfea112/pfea112.svg`
- `ide/lsp/src/main/kotlin/com/engineeringood/athena/ide/lsp/AthenaProjectionSessionProtocol.kt`
- `ide/lsp/src/test/kotlin/com/engineeringood/athena/ide/lsp/AthenaM35DedicatedCabinetProjectionSmokeTest.kt`
- `ide/lsp/src/test/kotlin/com/engineeringood/athena/ide/lsp/AthenaAuthoringRequestTest.kt`
- `ide/lsp/src/test/kotlin/com/engineeringood/athena/ide/lsp/AthenaProjectionRequestTest.kt`
- `ide/lsp/src/test/kotlin/com/engineeringood/athena/ide/lsp/AthenaSourceMutationRequestTest.kt`
- `kernel/compiler/src/main/kotlin/com/engineeringood/athena/compiler/BackendAuthoringSourceEditPlanner.kt`
- `kernel/compiler/src/main/kotlin/com/engineeringood/athena/compiler/EngineeringIrLowerer.kt`
- `kernel/compiler/src/test/kotlin/com/engineeringood/athena/compiler/AthenaCompilerTest.kt`
- `kernel/compiler/src/test/kotlin/com/engineeringood/athena/compiler/AthenaGroupedConnectLoweringTest.kt`
- `kernel/compiler/src/test/kotlin/com/engineeringood/athena/compiler/AthenaM17ParserParityProofTest.kt`
- `kernel/compiler/src/test/kotlin/com/engineeringood/athena/compiler/AthenaM35DedicatedCabinetSampleTest.kt`
- `kernel/compiler/src/test/kotlin/com/engineeringood/athena/compiler/AthenaParserContinuityTest.kt`
- `kernel/compiler/src/test/kotlin/com/engineeringood/athena/compiler/PresentationModelDeriverTest.kt`

### Change Log

- 2026-07-28 - Created Story 4.1 implementation guide and moved to in-progress.
- 2026-07-28 - Added dedicated M35 Cabinet sample, compiler/LSP proof tests, Cabinet-only sample surface filter, and portable connection identity consistency.
