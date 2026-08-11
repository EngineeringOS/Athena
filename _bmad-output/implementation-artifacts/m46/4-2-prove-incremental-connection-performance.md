---
story: 4.2
epic: 4
title: Prove Incremental Connection Performance
status: ready-for-dev
baseline_commit: 9ca7c80239d38d9e2a4880886e71e27e2923c41b
created: 2026-08-11
---

# Story 4.2: Prove Incremental Connection Performance

Status: review

## Story

As an engineer,
I want large connection documents to remain responsive,
so that professional planning does not make the IDE unusable.

## Acceptance Criteria

1. Given a pinned 1,000-connection checked profile built from current `SceneConnection` contracts, when the rebuilt
   Electron product runs the benchmark, then p95 pan/zoom frame time is at most 22 ms, p95 semantic connection
   selection is at most 100 ms, and p95 one-route local replan/repaint is at most 250 ms.
2. Given every measured interaction, when evidence is published, then identity errors, trace errors, and unhandled
   errors are zero; accepted Input Revision remains stable; results record real sample arrays rather than constants.
3. Given one route edit in the 1,000-connection profile, when incremental invalidation runs, then changed ids contain
   the edited Connection plus only its affected topology group/local collision set, unchanged Connection nodes retain
   identity, and incremental paint count is strictly smaller than full-scene paint count.
4. Given benchmark completion, when verifier evaluates evidence, then artifact records profile identity, fixture digest,
   OS, CPU, Node/Electron/Konva versions, viewport, DPR, sample counts, durations, changed ids, paint counts, thresholds,
   and independently computed gates; verifier fails on missing/non-finite samples, hardcoded PASS, identity drift,
   excessive invalidation, or threshold violation.

## Tasks / Subtasks

- [x] Define current M46 connection performance profile and evidence contract (AC: 1-4)
  - [x] Add a typed frontend benchmark result for exactly 1,000 `SceneConnection` instances derived from the active
    admitted M46 Scene; synthetic clones are benchmark fixture data only and never source or engineering authority.
  - [x] Record per-sample operation, duration, selected semantic id, trace id, visible/full paint count, changed ids,
    accepted revision, and errors; derive p95 from recorded samples.
  - [x] Add deterministic profile/fixture digest and environment metadata; no milestone-named production class or
    production source owns PASS/FAIL.
- [x] Implement bounded connection invalidation and measured adapter updates (AC: 1-3)
  - [x] Index connection paint nodes by stable projection/connection/segment/marker/annotation identities.
  - [x] Apply one changed Connection/topology group without destroying and rebuilding every unchanged route node.
  - [x] Preserve unchanged Konva node identity, semantic trace, selection, and accepted Input Revision.
  - [x] Count actual destroyed/created/painted nodes and changed ids; fail benchmark if one-route update reaches full
    scene or changes unrelated topology groups.
- [x] Add frontend tests for profile integrity and incremental invalidation (AC: 1-4)
  - [x] Assert exactly 1,000 connections, unique stable ids, valid source/target anchors, traces, and deterministic bytes.
  - [x] Assert one-route update changes bounded ids, retains untouched node identity, and reports actual paint counts.
  - [x] Assert percentile/gate evaluation rejects empty, non-finite, identity-error, trace-error, unhandled-error,
    full-repaint, or threshold-breaching evidence.
- [x] Add rebuilt Electron launcher/verifier and evidence publication (AC: 1-4)
  - [x] Add `verify:m46-performance` product script opening `examples/m46/rolling-shutter` as workspace root before
    source/widget access and waiting for Explorer, Repository `READY`, exact LSP root, and active Scene.
  - [x] Run warmup plus checked pan/zoom, selection, and one-route incremental samples through active Konva adapter.
  - [x] Write `_bmad-output/implementation-artifacts/m46/performance/m46-connection-performance.json` from measurements,
    then independently validate thresholds and evidence completeness; never write `status: passed` before gates run.
  - [x] Run frontend focused/full tests, frontend build, product performance verifier, encoding audit, source-set hygiene
    audit, and `git diff --check`; record actual results before status `review`.

## Dev Notes

### Authority And Scope

```text
M46 accepted SceneConnection publication
  -> benchmark-only 1,000-connection checked profile
  -> stable-id connection spatial/index map
  -> bounded changed topology group
  -> incremental Konva patch/repaint
  -> measured product evidence
```

- Benchmark may clone current package-backed Scene geometry for load, but cannot invent semantic source authority or
  become a second renderer/scene model. Active `SceneConnection` contract remains sole paint input.
- One route edit invalidates edited Connection, its explicit topology group, and actual local collision dependents only.
  No global `setPublication`/`destroyChildren` path may count as incremental success.
- Unchanged node identity and traces are correctness gates, not optional performance optimizations.
- Do not reuse M43/M44 fixture names, paths, thresholds, hooks, or evidence. Reuse cohesive measurement helpers only
  after renaming/generalizing them to current product concepts; delete stale milestone naming where touched.
- No hardcoded PASS. Raw samples first, metrics derived second, gates evaluated last by independent verifier.

### Existing Code To Change

- `ide/theia-frontend/src/browser/diagram/scale-benchmark.ts` currently owns M43 100k occurrence and M44 300
  occurrence profiles. Extract/generalize shared percentile/sample helpers; add a dedicated current connection profile
  rather than bending occurrence-only metrics into connection evidence.
- `ide/theia-frontend/src/browser/diagram/konva-diagram-adapter.ts` currently destroys every route child in
  `redrawVisibleContent()`. Add stable connection-node ownership and a bounded patch path while preserving full draw for
  first publication, viewport culling, style changes, and unrelated occurrence work.
- `ide/theia-frontend/src/browser/athena-presentation-widget.tsx` already exposes benchmark hooks. Add one M46 hook
  with no M43/M44 environment names.
- `ide/theia-product/scripts/athena-m43-scale-main.js` and `verify-athena-m44-performance.js` are historical proof
  launchers. Create M46-named launcher/verifier; workspace activation must precede source/widget access.
- `ide/theia-product/package.json` adds `verify:m46-performance`.

### Test And Verification

```powershell
Set-Location ide
yarn test
yarn build
yarn workspace @engineeringood/athena-theia-product verify:m46-performance
Set-Location ..
powershell -ExecutionPolicy Bypass -File .\tools\encoding-audit.ps1
powershell -ExecutionPolicy Bypass -File .\tools\source-set-hygiene-audit.ps1
git diff --check
```

## References

- [Source: `_bmad-output/planning-artifacts/m46/epics.md`, Epic 4 / Story 4.2]
- [Source: `_bmad-output/planning-artifacts/prds/prd-Athena-2026-08-10-m46/prd.md`, NFR-4/NFR-5]
- [Source: `_bmad-output/planning-artifacts/architecture/architecture-Athena-2026-08-10-m46/ARCHITECTURE-SPINE.md`, AD-16/AD-19]
- [Source: `_bmad-output/planning-artifacts/m46/implementation-plan.md`, Task 13]
- [Source: `_bmad-output/implementation-artifacts/m46/4-1-build-m46-rolling-shutter-connection-project.md`]
- [Source: `AGENTS.md`, E2E Proof, Theia Workspace Proof, Visual Golden, and Pre-1.0 rules]

## Dev Agent Record

### Agent Model Used

Codex

### Debug Log References

- Story context created from complete M46 sprint, Epic 4, PRD/addendum, architecture AD-16/AD-19, implementation plan,
  Story 4.1 verification lessons, current benchmark/adapter/product launcher code, repository rules, and git context.
- Frontend red/green: initial build exposed stale benchmark contract fields and Konva node typing; current TypeScript build
  and focused 4-test performance suite pass.
- Product red/green: first real run failed pan/zoom at 138.8 ms; transform-only viewport updates and adjacency/BFS
  invalidation removed full-route synchronization and O(n^2) topology scans.
- Runtime diagnosis rebuilt `:ide:lsp:installDist`; M46 workspace then opened with exact repository root and zero source
  diagnostics. Proof launcher now runs off-screen and terminates its full Electron process tree.
- Final product evidence contains 120 raw measurements and independently evaluated gates.

### Completion Notes List

- Added deterministic 1,000-Connection profile, finite-sample percentile evaluation, complete environment/evidence
  contract, and fail-closed gates with no production PASS authority.
- Added stable per-Connection Konva ownership, spatial indexing, bounded topology/collision invalidation, retained node
  identity checks, actual paint counts, and measured adapter benchmark.
- Added M46-only widget hook, off-screen Electron launcher, independent verifier, and published evidence.
- Final evidence: pan/zoom p95 0.7 ms, selection p95 18.4 ms, local replan/repaint p95 40.5 ms; 4 changed/repainted
  Connections; incremental paint 11 versus full-scene paint 1817; zero identity, trace, and unhandled errors.
- Verification passed: frontend 75/75 tests, frontend/product builds, M46 product verifier, encoding audit,
  source-set hygiene audit, and `git diff --check`.

### File List

- `_bmad-output/implementation-artifacts/m46/4-2-prove-incremental-connection-performance.md`
- `_bmad-output/implementation-artifacts/m46/performance/m46-connection-performance.json`
- `_bmad-output/implementation-artifacts/m46/sprint-status.yaml`
- `ide/theia-frontend/scripts/athena-connection-performance.test.mjs`
- `ide/theia-frontend/src/browser/athena-presentation-widget.tsx`
- `ide/theia-frontend/src/browser/diagram/connection-performance.ts`
- `ide/theia-frontend/src/browser/diagram/konva-diagram-adapter.ts`
- `ide/theia-product/package.json`
- `ide/theia-product/scripts/athena-m46-performance-main.js`
- `ide/theia-product/scripts/verify-athena-m46-performance.js`

### Change Log

- 2026-08-11: Created Story 4.2 through BMad create-story workflow; status `ready-for-dev`.
- 2026-08-11: Implemented and verified incremental 1,000-Connection performance proof; status `review`.
