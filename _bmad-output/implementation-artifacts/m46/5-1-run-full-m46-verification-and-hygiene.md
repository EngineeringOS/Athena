---
story: 5.1
epic: 5
title: Run Full M46 Verification And Hygiene
status: ready-for-dev
baseline_commit: 9ca7c80239d38d9e2a4880886e71e27e2923c41b
created: 2026-08-11
---

# Story 5.1: Run Full M46 Verification And Hygiene

Status: in-progress

## Story

As a maintainer,
I want one fresh sequential verification record,
so that no stale build or retired route architecture can hide failure.

## Acceptance Criteria

1. Given all M46 implementation stories, when verification runs, then complete affected Gradle module suites run one at
   a time on Windows, LSP distribution is rebuilt, Tree-sitter/frontend contracts and tests pass, and the full Theia
   product builds without concurrent Gradle processes.
2. Given rebuilt runtime outputs, when product verification runs, then M46 performance, author/reopen, and deterministic
   export proofs pass against `examples/m46/rolling-shutter`; workspace root, Explorer, Repository `READY`, exact LSP
   root, accepted Connection IR/Scene, semantic trace, desktop/narrow screenshots, and exported SVG/PNG are verified.
3. Given source and evidence, when hygiene runs, then encoding, source-set hygiene, `git diff --check`, and targeted scans
   reject deprecated/compatibility paths, old route contracts, generic relationship-to-route truth, milestone-named
   production types, and proof/demo/sample production classes.
4. Given FR1-FR16, NFR1-NFR7, and UX-DR1 through UX-DR6, when acceptance audit runs, then every requirement maps to a
   fresh passing test, product measurement, or regenerated artifact; no status, stale file, or hardcoded PASS counts as
   evidence.
5. Given any failure, when correction lands, then current M46 authority is fixed with focused regression coverage and
   affected/downstream verification reruns; no shim, fallback, alias, skipped test, weakened assertion, or old fixture is
   introduced.
6. Given all gates pass, when records publish, then command results, artifact digests, evidence paths, changed files, and
   residual risks are written under `_bmad-output/implementation-artifacts/m46`; Stories 1.1-4.3 and Epics 1-4 become
   `done` only after their evidence is accepted.

## Tasks / Subtasks

- [ ] Build truthful M46 acceptance inventory (AC: 4, 6)
  - [ ] Read every M46 Story 1.1-4.3 record and map every AC plus FR/NFR/UX requirement to runnable evidence.
  - [ ] Validate active example, Connection IR/Scene facts, performance samples, operation transcript, exports, and
    screenshots; reject missing, stale, hardcoded, or status-only proof.
  - [ ] Publish machine-readable acceptance mapping and readable verification log under M46 artifacts.

- [ ] Run complete affected Kotlin verification sequentially (AC: 1, 4, 5)
  - [ ] Run complete tests for language, engineering-model, connection-model, projection-model, spatial-model,
    interaction-model, presentation-model, compiler, validation, runtime, SVG renderer, package/repository dependencies,
    and LSP using one Gradle process at a time.
  - [ ] Rebuild `:ide:lsp:installDist` after tests and before any product proof.
  - [ ] If any suite fails, add focused RED/GREEN coverage, fix current authority, then rerun affected and downstream
    suites sequentially.

- [ ] Rebuild and verify Tree-sitter, frontend, and Product (AC: 1, 2, 4, 5)
  - [ ] Run Tree-sitter generation/tests/WASM build, frontend contract check, complete frontend tests, and full Product
    build from current sources.
  - [ ] Run `verify:m46-performance`, `verify:m46-authoring`, and `verify:m46-export` from rebuilt outputs.
  - [ ] Inspect regenerated desktop/narrow screenshots and machine checks against Engineering Document Visual Golden
    Rule; no visible grid/debug text/port rings/bottom table, thick linework, clipping, or overlap is permitted.

- [ ] Run architecture and repository hygiene gates (AC: 3, 5)
  - [ ] Run encoding audit, source-set hygiene audit, and `git diff --check`.
  - [ ] Scan active production roots for `ProjectionConnection`, `SpatialRoute`, `SpatialRouteCompiler`, `SceneRoute`,
    `@Deprecated`, compatibility/fallback/legacy connection paths, generic relationship route lowering, milestone names,
    and `Proof`/`Demo`/`Sample` production classes.
  - [ ] Record exact roots/patterns and distinguish reference/history artifacts from executable production paths.

- [ ] Publish evidence and close verified implementation epics (AC: 1-6)
  - [ ] Write verification summary with command outcomes, test/evidence paths, digests, requirement mapping, and residual
    risks.
  - [ ] Complete this story's Tasks, Debug Log, Completion Notes, File List, and Change Log.
  - [ ] Mark Stories 1.1-4.3 and Epics 1-4 `done` only after mapped evidence passes; otherwise fix the gap.
  - [ ] Move Story 5.1 to `review` only after all gates pass and sprint status remains internally consistent.

## Dev Notes

### Authority Guardrails

- Source meaning -> Engineering Reality -> Connection IR -> Connection Projection -> Route Plan -> Canonical Scene ->
  Theia/SVG paint. Verification must prove this chain, never create another read model or renderer authority.
- `EngineeringConnection`/`EngineeringNet` own connectivity; generic `EngineeringRelationship` never becomes a route.
- SceneConnection is sole paint contract. Konva/DOM/export evidence cannot become engineering or layout truth.
- Reconnect is Engineering authority; route adjustment is Sheet Presentation authority; both use full Source Revision
  transactions. Rejected/stale operations change nothing.
- No M43/M44/M45 example, fixture, proof launcher, environment variable, export, screenshot, or acceptance constant may
  satisfy M46 closure.
- Product proof opens repository root before source/widget access and fails immediately on `NO FOLDER OPENED`, wrong LSP
  root, non-READY repository, or unavailable Connection publication.

### Verification Order

Never overlap Gradle processes. Run one command and wait before starting next. At minimum:

```powershell
.\gradlew.bat --no-daemon --console=plain :kernel:language:test
.\gradlew.bat --no-daemon --console=plain :kernel:repository-model:test
.\gradlew.bat --no-daemon --console=plain :kernel:package-model:test
.\gradlew.bat --no-daemon --console=plain :kernel:package-runtime:test
.\gradlew.bat --no-daemon --console=plain :kernel:engineering-model:test
.\gradlew.bat --no-daemon --console=plain :kernel:connection-model:test
.\gradlew.bat --no-daemon --console=plain :kernel:projection-model:test
.\gradlew.bat --no-daemon --console=plain :kernel:spatial-model:test
.\gradlew.bat --no-daemon --console=plain :kernel:interaction-model:test
.\gradlew.bat --no-daemon --console=plain :kernel:presentation-model:test
.\gradlew.bat --no-daemon --console=plain :kernel:compiler:test
.\gradlew.bat --no-daemon --console=plain :kernel:validation:test
.\gradlew.bat --no-daemon --console=plain :kernel:runtime:test
.\gradlew.bat --no-daemon --console=plain :kernel:svg-renderer:test
.\gradlew.bat --no-daemon --console=plain :ide:lsp:test
.\gradlew.bat --no-daemon --console=plain :ide:lsp:installDist

Push-Location ide\tree-sitter-athena
yarn test
Pop-Location

Push-Location ide
yarn workspace @engineeringood/athena-theia-frontend contracts:check
yarn workspace @engineeringood/athena-theia-frontend test
yarn build
yarn workspace @engineeringood/athena-theia-product verify:m46-performance
yarn workspace @engineeringood/athena-theia-product verify:m46-authoring
yarn workspace @engineeringood/athena-theia-product verify:m46-export
Pop-Location

powershell -ExecutionPolicy Bypass -File .\tools\encoding-audit.ps1
powershell -ExecutionPolicy Bypass -File .\tools\source-set-hygiene-audit.ps1
git diff --check
```

### Previous Story Intelligence

- Story 4.2 passed measured 1,000-Connection product evidence with bounded invalidation and raw-sample independent gates.
- Story 4.3 passed valid/rejected reconnect, route edit, Undo/Redo, stale rejection, fresh-process reopen, and deterministic
  SVG/PNG evidence. Its authoring proof uses a temporary repository; active M46 example must remain unchanged.
- `:ide:lsp:installDist` must be rebuilt before product E2E. Stale distributions previously caused unsupported
  `athena/connectionReadModel` and stale Style Companion parsing.
- Electron proof window must remain renderable off-screen with background throttling disabled; hidden windows caused RAF
  and workbench stalls. Proof launcher must terminate the complete Electron process tree.
- Current stack is pinned: Kotlin 2.4.0, LSP4J 0.23.1, TypeScript 5.9.2, Node >=22, Yarn 1.22.22, Theia 1.73.1,
  Konva 10.3.0, React 18.3.1. No dependency upgrade or web research is needed.

## References

- [Source: `_bmad-output/planning-artifacts/m46/epics.md`, Epic 5 / Story 5.1]
- [Source: `_bmad-output/planning-artifacts/prds/prd-Athena-2026-08-10-m46/prd.md`, FR1-FR16/NFR1-NFR7]
- [Source: `_bmad-output/planning-artifacts/prds/prd-Athena-2026-08-10-m46/addendum.md`, Product Proof]
- [Source: `_bmad-output/planning-artifacts/architecture/architecture-Athena-2026-08-10-m46/ARCHITECTURE-SPINE.md`, AD-1 through AD-19]
- [Source: `_bmad-output/implementation-artifacts/m46/4-2-prove-incremental-connection-performance.md`]
- [Source: `_bmad-output/implementation-artifacts/m46/4-3-prove-edit-reopen-and-deterministic-export.md`]
- [Source: `AGENTS.md`, Build Verification, Source-Set Hygiene, E2E, Workspace Proof, and Visual Golden rules]

## Dev Agent Record

### Agent Model Used

Codex

### Debug Log References

- Ultimate context engine analysis completed from full M46 sprint, PRD/addendum, architecture, previous proof stories,
  active verification scripts, repository rules, and git context.

### Completion Notes List

- Ultimate context engine analysis completed - comprehensive developer guide created.

### File List

- `_bmad-output/implementation-artifacts/m46/5-1-run-full-m46-verification-and-hygiene.md`
- `_bmad-output/implementation-artifacts/m46/sprint-status.yaml`

### Change Log

- 2026-08-11: Created Story 5.1 through BMad create-story workflow; status `ready-for-dev`.
