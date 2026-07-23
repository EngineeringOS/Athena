---
status: review
baseline_commit: 8d12ba03c05b373b98fbc513e6c7f3f6fce962d6
story_id: 5.1
epic: 5
title: Create M32 Sample Packages And Project
---

# Story 5.1: Create M32 Sample Packages And Project

## Status

Review

## Story

As a customer-demo owner,
I want a package-backed M32 sample project,
so that Athena demonstrates professional product-like representation without proprietary runtime
dependencies.

## Required Context

- PRD: `_bmad-output/planning-artifacts/prds/prd-Athena-2026-07-22-m32/prd.md`
- Architecture: `_bmad-output/planning-artifacts/architecture/architecture-Athena-2026-07-22-m32/ARCHITECTURE-SPINE.md`
- Epics: `_bmad-output/implementation-artifacts/m32/epics.md`
- Previous story: `_bmad-output/implementation-artifacts/m32/4-3-route-through-descriptor-anchors.md`
- Previous retrospective: `_bmad-output/implementation-artifacts/m32/epic-4-retro-2026-07-22.md`
- Usage docs: `docs/usages/engineering-package-platform.md`

## Acceptance Criteria

1. Given `examples/m32/sample-project`, when its source, package descriptors, manifests,
   resources, and README are inspected, then it uses Athena-owned semantic source and package
   assets only, and it includes at least three product-like Engineering Packages with matching
   Representation Packages.
2. Given the sample compiles, when package profile changes, then at least one semantic subject
   resolves to a different Presentation Profile without changing `.athena` source.
3. Given the story implementation is complete, when sample assets, names, licensing notes, docs,
   adjacent examples, package runtime fixtures, and sprint artifacts are reviewed, then stale demo
   assets are removed or ledgered and AC evidence is recorded.
4. Mandatory Polish/Purge Gate complete.

## Tasks/Subtasks

- [x] Add RED tests proving M32 sample package inventory contains at least three synthetic
  Engineering Packages and matching Representation Packages. (AC: 1)
- [x] Add RED tests proving profile switch resolution changes package/profile appearance facts
  without changing `.athena` source content. (AC: 2)
- [x] Create `examples/m32/sample-project` semantic source, package descriptors, manifest/profile
  fixtures, resource descriptors, and README/licensing notes using Athena-owned synthetic names.
  (AC: 1,2)
- [x] Add runtime/sample loader or fixture support only at package-runtime/example boundary; do not
  add `.athena` visual syntax or renderer-owned package authority. (AC: 1,2)
- [x] Document how the sample demonstrates Engineering Package, Presentation Profile, and
  Representation Package separation. (AC: 1..3)
- [x] Run focused tests sequentially, then full regression sequentially; do not run Gradle
  concurrently on Windows. (AC: 1..3)
- [x] Complete mandatory polish/purge review and record final evidence. (AC: 3,4)

## Dev Notes

- Epic 5 must consume the Epic 4 evidence path: package descriptors -> binding evidence ->
  descriptor-backed occurrence/render/route evidence. Do not create a visual shortcut sample.
- Use synthetic product-like names, not real vendor packages or QET-derived assets. If a name looks
  vendor-like, document that it is Athena-owned sample data.
- `examples/m32/sample-project` should contain semantic `.athena` source plus package/profile
  assets. Package data is extension metadata and must not add new `.athena` syntax.
- Profile switching should be represented by Package/Binding Runtime facts, not source text
  mutations.
- Graphic Resource files or descriptors are paint/resource inputs only. Do not make file names,
  labels, SVG/CSS, or resource internals carry semantic truth.
- Run `powershell -ExecutionPolicy Bypass -File .\tools\encoding-audit.ps1` after text edits.

## Testing Requirements

- Follow TDD: write failing sample package/profile tests before creating the sample assets or loader.
- Focused command should target the package runtime/model module touched by implementation.
- Full regression command after story completion:
  `.\gradlew.bat --no-daemon --console=plain check`.
- Encoding audit command after docs/text edits:
  `powershell -ExecutionPolicy Bypass -File .\tools\encoding-audit.ps1`.

## Dev Agent Record

### Agent Model Used

Codex GPT-5

### Debug Log References

- RED: `.\gradlew.bat --no-daemon --console=plain :kernel:package-runtime:test` failed before implementation because `M32SamplePackageSet` was unresolved.
- GREEN: `.\gradlew.bat --no-daemon --console=plain :kernel:package-runtime:test` passed after adding the M32 sample package set, sample project files, and package/profile switch fixture.
- REFACTOR VERIFY: `.\gradlew.bat --no-daemon --console=plain :kernel:package-runtime:test` passed after documentation updates.
- REGRESSION: `.\gradlew.bat --no-daemon --console=plain check` exited 0 with `BUILD SUCCESSFUL`.
- ENCODING: `powershell -ExecutionPolicy Bypass -File .\tools\encoding-audit.ps1` exited 0 with `Encoding audit passed.`
- PURGE: `git status --short` showed M32 artifacts, `settings.gradle.kts`, docs, `examples/m32/`, `kernel/package-model/`, and `kernel/package-runtime/`; `.tools` was not present.

### Completion Notes List

- Added `examples/m32/sample-project` with semantic source, README, synthetic package descriptors,
  profiles, manifests, representation descriptors, and Athena-owned vector resources.
- Added `M32SamplePackageSet` as package-runtime/sample fixture support that resolves subjects
  through `BindingResolver` without parser, renderer, DOM, CSS, or `.athena` visual syntax
  authority.
- Verified `ShutterMotorM32` resolves through both `m32-iec` and `m32-compact` profiles with the
  same semantic source and different representation facts.
- Documented the M32 sample boundary and profile-switch proof in `docs/usages/engineering-package-platform.md`.
- AC evidence:
  - AC1: `M32SamplePackageSetTest` verifies at least three synthetic Engineering Packages, matching
    manifests, representation packages/resources, source, README, and no QET/vendor names.
  - AC2: `M32SamplePackageSetTest` verifies profile switching changes Presentation Profile,
    Representation Package, and descriptor ids without changing `.athena` source.
  - AC3: docs and sample README state Athena-owned synthetic assets; full regression and purge gate
    completed with no stale/proprietary demo dependency retained.
  - AC4: encoding audit and purge/status gate completed.

### File List

- `_bmad-output/implementation-artifacts/m32/5-1-create-m32-sample-packages-and-project.md`
- `_bmad-output/implementation-artifacts/m32/sprint-status.yaml`
- `docs/usages/engineering-package-platform.md`
- `examples/m32/sample-project/README.md`
- `examples/m32/sample-project/src/01-package-platform-demo.athena`
- `examples/m32/sample-project/packages/engineering/power-supply-24v.json`
- `examples/m32/sample-project/packages/engineering/roller-relay.json`
- `examples/m32/sample-project/packages/engineering/shutter-motor.json`
- `examples/m32/sample-project/packages/profiles/m32-iec.json`
- `examples/m32/sample-project/packages/profiles/m32-compact.json`
- `examples/m32/sample-project/packages/manifests/power-supply-binding.json`
- `examples/m32/sample-project/packages/manifests/roller-relay-binding.json`
- `examples/m32/sample-project/packages/manifests/shutter-motor-binding.json`
- `examples/m32/sample-project/packages/representation/power-supply-iec.json`
- `examples/m32/sample-project/packages/representation/relay-iec.json`
- `examples/m32/sample-project/packages/representation/motor-iec.json`
- `examples/m32/sample-project/packages/representation/motor-compact.json`
- `examples/m32/sample-project/packages/resources/resource.power-supply.iec.svg`
- `examples/m32/sample-project/packages/resources/resource.relay.iec.svg`
- `examples/m32/sample-project/packages/resources/resource.motor.iec.svg`
- `examples/m32/sample-project/packages/resources/resource.motor.compact.svg`
- `kernel/package-runtime/src/main/kotlin/com/engineeringood/athena/packageruntime/M32SamplePackageSet.kt`
- `kernel/package-runtime/src/test/kotlin/com/engineeringood/athena/packageruntime/M32SamplePackageSetTest.kt`

## Change Log

- 2026-07-22: Story created for M32 Epic 5 customer demo package sample.
- 2026-07-22: Added M32 sample package project and runtime fixture proof; marked story ready for review after focused and full verification.

## Mandatory Final Polish/Purge Gate

- Review touched and adjacent sample assets, package runtime fixtures, docs, tests, sprint status,
  and cleanup ledger.
- Remove stale demo assets, placeholder package shortcuts, visual-source syntax claims, or
  proprietary/QET-looking dependencies unless explicitly documented as deferred/non-runtime.
- Ledger any retained stale item in `_bmad-output/implementation-artifacts/m32/cleanup-ledger.md`
  with owner, reason, target milestone, and verification.
- Record AC-to-evidence mapping before moving the story beyond `review`.
