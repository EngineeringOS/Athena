---
baseline_commit: 593d0a1dc86c0bac126fbde0501cc07051180346
---

# Story M36-E5.S3: Create The Source-First M36 Cabinet Sample

Status: done

## Story

As an evaluator,
I want a dedicated M36 Cabinet example,
so that the connectivity compiler is proven through a realistic single product surface.

**Requirements:** FR-33, FR-34.

## Acceptance Criteria

1. Given the dedicated M36 sample project, it compiles offline from repository source and opens its
   Cabinet projection source-first.
2. The sample includes at least 20 occurrences and 30 typed Connections.
3. The sample includes native Symbols, one complex SVG-backed Element, typed Ports, explicit
   bindings, a Network or junction, bundles, obstacles, labels, and Cabinet installation facts.
4. The sample follows package-rooted source and resource hierarchy, with package-local assets
   adjacent to the owning `.athena` files.
5. The sample contains no XML authority, raw SVG metadata authority, unresolved Port or Anchor
   bridge, or generic fallback component.
6. The sample opens without an editor lifecycle side effect and remains source-first in the IDE/LSP
   path.

## Tasks / Subtasks

- [x] Add failing tests for the M36 source-first sample (AC: 1-6)
  - [x] Cover offline repository validation and package-rooted source hierarchy.
  - [x] Cover Cabinet compilation shape: occurrences, Connections, bindings, bundles, obstacles,
    labels, and installation facts.
  - [x] Cover source-first IDE/LSP opening with no editor lifecycle side effect.
- [x] Create the dedicated M36 Cabinet sample fixture (AC: 1-6)
  - [x] Add the sample under `examples/m36/connectivity-cabinet/` using package-rooted source
    directories.
  - [x] Include native Symbols and a complex SVG-backed Element with package-local resources.
  - [x] Include typed Ports, explicit bindings, at least one Network or junction, bundles,
    obstacles, labels, and Cabinet installation facts.
  - [x] Keep all sample resources local to the package tree and free of XML runtime authority.
- [x] Wire the sample into compiler and IDE proof paths (AC: 1-6)
  - [x] Ensure the compiler sample test validates the offline repository and structural proof.
  - [x] Ensure the IDE/LSP smoke test opens the sample source-first and sees Cabinet active.
  - [x] Preserve renderer purity and keep the sample as a proof fixture, not a new editor surface.
- [x] Run story evidence gate (AC: 1-6)
  - [x] Run sequential Gradle tests, encoding audit, and `git diff --check`.
  - [x] Record AC-to-evidence, file list, and completion notes.

## Dev Notes

- M35 established the proof pattern for a dedicated Cabinet sample:
  a compiler-side offline validation test plus an LSP smoke test that opens the sample source-first.
- Reuse the existing cabinet proof conventions: local `athena.yaml`, local `athena.lock`, package
  directories under the sample root, and source files that mirror package hierarchy.
- The target fixture for M36 is the architecture-spine sample root
  `examples/m36/connectivity-cabinet/`. Keep the package structure explicit and adjacent to the
  sample source.
- This story is about the sample fixture and its proof coverage, not a new layout engine or editor
  surface.
- No XML compatibility paths. The project is unreleased, so stale incompatible sample paths may be
  removed rather than preserved.

### Project Structure Notes

- Likely touchpoints:
  - `examples/m36/connectivity-cabinet/`
  - `examples/m36/connectivity-cabinet/src/com/engineeringood/m36/...`
  - `examples/m36/connectivity-cabinet/packages/representation/...`
  - `kernel/compiler/src/test/kotlin/com/engineeringood/athena/compiler/AthenaM36DedicatedCabinetSampleTest.kt`
  - `ide/lsp/src/test/kotlin/com/engineeringood/athena/ide/lsp/AthenaM36DedicatedCabinetProjectionSmokeTest.kt`
- Keep text assets UTF-8.
- Do not add legacy XML compatibility paths.

### References

- `_bmad-output/implementation-artifacts/m36/epics.md` - Story 5.3, FR-33, FR-34.
- `_bmad-output/planning-artifacts/prds/prd-Athena-2026-07-28-m36/prd.md` - FR-33, FR-34,
  FR-35, FR-36, NFR-1, NFR-4, NFR-5, NFR-7, NFR-8, NFR-9.
- `_bmad-output/planning-artifacts/architecture/architecture-Athena-2026-07-28-m36/ARCHITECTURE-SPINE.md`
  - AD-9, AD-10.
- `kernel/compiler/src/test/kotlin/com/engineeringood/athena/compiler/AthenaM35DedicatedCabinetSampleTest.kt`
  - offline sample compilation and package-backed proof pattern.
- `ide/lsp/src/test/kotlin/com/engineeringood/athena/ide/lsp/AthenaM35DedicatedCabinetProjectionSmokeTest.kt`
  - source-first Cabinet opening proof pattern.
- `kernel/compiler/src/test/kotlin/com/engineeringood/athena/compiler/AthenaM34ProfessionalCabinetCompositionTest.kt`
  - earlier cabinet composition fixture and structural proof style.

## Dev Agent Record

### Agent Model Used

Codex (GPT-5)

### Debug Log References

- 2026-07-29: Verified focused compiler, drawing-composition, and LSP smoke tests for the dedicated M36 sample.
- 2026-07-29: Verified full regression with `.\gradlew.bat --no-daemon --console=plain test`.
- 2026-07-29: Verified text encoding with `powershell -ExecutionPolicy Bypass -File .\tools\encoding-audit.ps1`.
- 2026-07-29: Verified whitespace with `git diff --check`.

### Completion Notes List

- Added the dedicated source-first M36 cabinet sample under `examples/m36/connectivity-cabinet/`
  with package-rooted source, package-local representation material, native IEC-style symbols, and
  one complex ABB SVG-backed vendor element.
- The sample proves at least 20 rendered occurrences and 30 typed connections, plus explicit port
  bindings, junction/network content, bundles, obstacles, labels, and cabinet installation facts.
- Wired compiler and LSP proof coverage so the sample validates offline and opens through the
  source-first Cabinet projection path without editor lifecycle side effects.
- Hardened route realization/routing behavior needed by the sample: route candidate validation is
  isolated per candidate, endpoint-owned bodies are ignored at anchor endpoints, obstacle diagnostics
  identify the intersected subject, and simple deterministic dogleg detours are attempted before
  failing closed.
- AC evidence: AC1 covered by `AthenaM36DedicatedCabinetSampleTest` and
  `AthenaM36DedicatedCabinetProjectionSmokeTest`; AC2-5 covered by
  `AthenaM36DedicatedCabinetSampleTest`; AC6 covered by
  `AthenaM36DedicatedCabinetProjectionSmokeTest`.

### File List

- `_bmad-output/implementation-artifacts/m36/5-3-create-source-first-m36-cabinet-sample.md`
- `_bmad-output/implementation-artifacts/m36/sprint-status.yaml`
- `examples/m36/connectivity-cabinet/athena.yaml`
- `examples/m36/connectivity-cabinet/athena.lock`
- `examples/m36/connectivity-cabinet/src/com/engineeringood/m36/connectivitycabinet/01-connectivity-cabinet.athena`
- `examples/m36/connectivity-cabinet/packages/representation/com/engineeringood/m36/control/cabinet-bindings.athena`
- `examples/m36/connectivity-cabinet/packages/representation/com/engineeringood/m36/standard/iec/standard-elements.athena`
- `examples/m36/connectivity-cabinet/packages/representation/com/engineeringood/m36/vendor/abb/pfea112/vendor-elements.athena`
- `examples/m36/connectivity-cabinet/packages/representation/com/engineeringood/m36/vendor/abb/pfea112/pfea112.svg`
- `ide/lsp/src/test/kotlin/com/engineeringood/athena/ide/lsp/AthenaM36DedicatedCabinetProjectionSmokeTest.kt`
- `kernel/compiler/src/main/kotlin/com/engineeringood/athena/compiler/CabinetRouteRealizationCompiler.kt`
- `kernel/compiler/src/test/kotlin/com/engineeringood/athena/compiler/AthenaLayoutGraphLowererTest.kt`
- `kernel/compiler/src/test/kotlin/com/engineeringood/athena/compiler/AthenaM36DedicatedCabinetSampleTest.kt`
- `kernel/compiler/src/test/kotlin/com/engineeringood/athena/compiler/CabinetRouteRealizationCompilerTest.kt`
- `kernel/drawing-composition/src/main/kotlin/com/engineeringood/athena/drawing/composition/CabinetRoutingCompiler.kt`
- `kernel/drawing-composition/src/test/kotlin/com/engineeringood/athena/drawing/composition/CabinetRoutingCompilerTest.kt`

## Change Log

- 2026-07-29: Created M36-E5.S3 story for the source-first M36 Cabinet sample.
- 2026-07-29: Completed dedicated M36 connectivity cabinet sample, compiler/LSP proof coverage, and routing fixes required by the sample.
