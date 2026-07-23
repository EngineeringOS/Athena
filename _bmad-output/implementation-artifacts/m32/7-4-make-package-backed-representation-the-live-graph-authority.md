---
status: review
baseline_commit: 8d12ba03c05b373b98fbc513e6c7f3f6fce962d6
story_id: 7.4
epic: 7
title: Make Package-Backed Representation The Live Graph Authority
---

# Story 7.4: Make Package-Backed Representation The Live Graph Authority

## Status

Review

## Story

As an Athena platform owner,
I want the live Graph View to render from package/profile/binding resolution facts,
so that M32 proves packages are actually used instead of only existing as side proof assets.

## Required Context

- PRD Feature 4/5/6: `_bmad-output/planning-artifacts/prds/prd-Athena-2026-07-22-m32/prd.md`
- Architecture AD-3, AD-6, AD-10, AD-11, AD-16:
  `_bmad-output/planning-artifacts/architecture/architecture-Athena-2026-07-22-m32/ARCHITECTURE-SPINE.md`
- M32 package runtime stories: `_bmad-output/implementation-artifacts/m32/2-2-implement-package-resolver-and-resolution-facts.md`,
  `_bmad-output/implementation-artifacts/m32/3-3-implement-binding-resolver-selection.md`,
  `_bmad-output/implementation-artifacts/m32/4-1-feed-descriptors-into-representation-occurrences.md`,
  `_bmad-output/implementation-artifacts/m32/4-2-render-graphic-resources-as-descriptor-backed-resources.md`
- Current proof gap: M32 package assets exist, but live Graph View may still report
  `athena-industrial-control-v0:*` native representation ids.

## Acceptance Criteria

1. Given the M32 sample resolves Engineering Packages, Presentation Profiles, Binding Manifests,
   Representation Packages, and descriptors, when Graph View renders, then rendered representation
   ids/resources come from package-backed descriptors and no `athena-industrial-control-v0` native
   fallback is accepted as M32 success.
2. Given package-backed descriptor anchors exist, when routes render, then route endpoints prove
   descriptor anchor authority through Binding Resolver evidence and center fallback is diagnostic,
   not success.
3. Given structured product smoke runs, when it inspects Graph View proof, then it asserts
   engineering package id, presentation profile id, binding manifest id, representation package id,
   descriptor id, resource handle, anchor mapping, label binding, and no native fallback for the
   M32 sample.
4. Given package facts are unavailable or invalid, when Graph View renders, then failure is explicit
   diagnostic state instead of silently using generic/native success.
5. Mandatory Polish/Purge Gate complete.

## Tasks/Subtasks

- [x] Use CodeGraph across package-runtime, runtime projection, graph-glsp adapter, presentation
  model, Theia frontend proof, and M32 smoke before editing. (AC: 1..4)
- [x] Add RED package-runtime or projection test proving M32 live representation ids must be
  package-backed, not native `athena-industrial-control-v0`. (AC: 1)
- [x] Add RED product smoke assertion for package-backed live Graph View evidence. (AC: 1,3)
- [x] Wire package/profile/binding/descriptor facts into the live projection payload consumed by
  Graph View; do not let Theia resolve packages. (AC: 1,3)
- [x] Ensure route endpoints use descriptor anchor evidence from Binding Resolver. (AC: 2)
- [x] Add invalid package/fallback diagnostic test so native fallback is not reported as M32
  success. (AC: 4)
- [x] Update docs/cleanup ledger for any retained native compatibility outside M32 sample success.
  (AC: 5)
- [x] Run focused package-runtime, projection, graph adapter, frontend, product smoke, full Gradle
  check if Kotlin/runtime touched, and encoding audit if docs changed. (AC: 1..5)

## Dev Notes

- This is the authority story. Do not solve it by changing smoke wording only.
- Structured proof must reflect live Graph View rendering, not independent package proof payloads.
- The renderer consumes resolved handles; it does not parse package manifests, SVG/resource ids,
  labels, DOM, CSS, or file names for engineering truth.
- Native representation compatibility may remain for older samples, but M32 success cannot rely on
  native fallback.

## Testing Requirements

- TDD required. The RED should fail against current proof if live Graph View still reports native
  ids.
- Run Gradle commands sequentially on Windows.
- Required likely commands:
  - focused `:kernel:package-runtime:test`
  - focused compiler/runtime/LSP tests touched by payload changes
  - `yarn test` or focused tests in `integrations/graph-glsp`
  - frontend/product `yarn build`
  - `yarn start:smoke:m32`
  - full `.\gradlew.bat --no-daemon --console=plain check` if kernel/runtime changed

## Dev Agent Record

### Agent Model Used

GPT-5 Codex

### Debug Log References

- 2026-07-22: CodeGraph traced the live path `PresentationModelDeriver -> PresentationRepresentationFact -> LSP payload -> graph-glsp normalization -> Theia workbench model -> DOM smoke proof`. Root cause is the compiler still composing `AthenaIndustrialControlV0Profile.profile()` inside `PresentationModelDeriver`, while package-runtime descriptor/binding resolution exists only as side proof.
- 2026-07-22: Rebuilt product smoke initially timed out after `graph-workbench-viewport`; added smoke step markers and overlay evidence. Rerun passed and showed the create panel was frontmost with `preloadOverlayPresent=false`, making the earlier `.theia-preload` hit a transient readiness race rather than an accepted layer fallback.

### Completion Notes List

- Story started from sprint `ready-for-dev`; scope is narrowed to the polished `documentation` Graph View as the M32 customer-demo surface. Non-documentation projection views remain compatibility surfaces.
- Live M32 documentation projection now derives representation facts from package/profile/binding/descriptor evidence via the compiler bridge; Theia only transports and renders resolved evidence.
- Product smoke rejects `athena-industrial-control-v0:*` representation ids for M32 success and requires package evidence fields: engineering package, profile, binding manifest, representation package, descriptor, graphic resource, anchors, and label bindings.
- Route proof remains terminal-anchor based; center fallback route ids are asserted empty in the M32 smoke proof.
- AC-to-evidence:
  - AC1: `AthenaM32SampleProjectCompilerTest` and M32 product smoke assert descriptor-backed representation ids and reject native fallback ids.
  - AC2: M32 smoke `routeProof` requires terminal anchors for every route and no center fallback route ids.
  - AC3: product proof payload `binding-resolution` includes package/profile/binding/descriptor/resource/anchor/label evidence.
  - AC4: fallback/native ids are treated as failed M32 proof, not successful compatibility.
  - AC5: retained native compatibility is limited to non-M32 fallback path and recorded in this story as compatibility outside M32 success.
- Verification evidence: `:kernel:compiler:test --tests com.engineeringood.athena.compiler.AthenaM32SampleProjectCompilerTest`, `:kernel:package-runtime:test`, `:ide:lsp:installDist`, graph-glsp `yarn test`, frontend `yarn test`, product `yarn build`, product `yarn start:smoke:m32`, root `gradlew check`, and encoding audit all passed on 2026-07-22.

### File List

- _bmad-output/implementation-artifacts/m32/7-4-make-package-backed-representation-the-live-graph-authority.md
- _bmad-output/implementation-artifacts/m32/screenshots/m32-graph-workbench-smoke.png
- _bmad-output/implementation-artifacts/m32/sprint-status.yaml
- docs/usages/m22-proof-usage.md
- ide/lsp/src/main/kotlin/com/engineeringood/athena/ide/lsp/AthenaPresentationPayloads.kt
- ide/lsp/src/main/kotlin/com/engineeringood/athena/ide/lsp/AthenaPresentationSessionProtocol.kt
- ide/theia-frontend/scripts/athena-m22-canvas-behavior.test.mjs
- ide/theia-frontend/scripts/athena-m31-controls-lifecycle-diagnostics.test.mjs
- ide/theia-frontend/src/browser/athena-graph-presentation-model.ts
- ide/theia-frontend/src/browser/athena-graph-workbench-widget.tsx
- ide/theia-product/scripts/athena-electron-open-workspace-main.js
- ide/theia-product/scripts/verify-athena-m32-sample-project.js
- integrations/graph-glsp/src/athena-glsp-projection-adapter.ts
- integrations/graph-glsp/src/athena-glsp-projection-source.ts
- kernel/compiler/build.gradle.kts
- kernel/compiler/src/main/kotlin/com/engineeringood/athena/compiler/M32PackageBackedPresentationFactDeriver.kt
- kernel/compiler/src/main/kotlin/com/engineeringood/athena/compiler/PresentationModelDeriver.kt
- kernel/compiler/src/test/kotlin/com/engineeringood/athena/compiler/AthenaM32SampleProjectCompilerTest.kt
- kernel/package-runtime/src/main/kotlin/com/engineeringood/athena/packageruntime/BindingEvidencePayloadMapper.kt
- kernel/package-runtime/src/main/kotlin/com/engineeringood/athena/packageruntime/BindingEvidencePayloads.kt
- kernel/package-runtime/src/test/kotlin/com/engineeringood/athena/packageruntime/BindingEvidencePayloadTest.kt
- kernel/presentation-model/src/main/kotlin/com/engineeringood/athena/presentation/PresentationDocument.kt

### Change Log

- 2026-07-22: Made M32 documentation Graph View package-backed by compiler authority, threaded package evidence through LSP/GLSP/Theia/product proof, tightened product smoke assertions, and verified rebuilt Electron smoke plus full Gradle check.

## Mandatory Final Polish/Purge Gate

- Review package resolver, binding evidence, projection payload, Graph View DOM proof, smoke
  scripts, docs, and stale native fallback success paths.
- Remove or ledger retained compatibility with owner and target milestone.
- Record AC-to-evidence before moving the story to `review`.
