---
story: 4.3
epic: 4
title: Prove Edit Reopen And Deterministic Export
status: done
baseline_commit: 9ca7c80239d38d9e2a4880886e71e27e2923c41b
created: 2026-08-11
---

# Story 4.3: Prove Edit Reopen And Deterministic Export

Status: done

## Story

As an engineer,
I want rebuilt product evidence for the complete authoring loop,
so that M46 is proven in the real IDE rather than fixtures alone.

## Acceptance Criteria

1. Given a fresh Electron/Theia proof launch, when the M46 product flow starts, then
   `examples/m46/rolling-shutter` opens as workspace root before any source/widget access, Explorer visibly contains
   the repository, Repository Session is `READY`, LSP repository root exactly equals the M46 example root, and the
   accepted Connection IR/Scene publication is `READY` before canvas assertions.
2. Given one valid reconnect, one direction-invalid reconnect, and one logical route edit, when product automation
   submits typed operations through the existing widget/LSP transaction path, then the valid Engineering and
   Presentation operations are accepted, the invalid reconnect is rejected without mutation, accepted source
   revisions advance, semantic Connection identity and unaffected identities/traces remain stable, and only the
   authority-owned `.athena`/`.sheet.athena` files plus compiler-owned `athena.lock` change.
3. Given the accepted route edit journal entry, when Undo then Redo runs, then each accepted source transaction applies
   atomically, pointer events create no journal entries, the final source/Scene equals the accepted Redo state, and a
   repeated stale journal action rejects without changing source or Scene.
4. Given the authored temporary M46 repository is closed and a fresh Electron/LSP process reopens it, when the Scene and
   Connection read model publish, then accepted Input Revision, Connection/Net identities, traces, route constraints,
   Scene digest, and source bytes match the final accepted pre-close state.
5. Given the reopened accepted Canonical Scene, when SVG export and PNG rasterization/canvas capture run twice, then
   repeated SVG bytes and PNG bytes are deterministic, digests and dimensions are recorded, and exports derive from the
   same accepted `SceneConnection` publication rather than DOM/Konva state as authority.
6. Given desktop and narrow product captures, when the verifier inspects DOM/canvas pixels and evidence, then screenshots
   exist under `_bmad-output/implementation-artifacts/m46/screenshots`, show the one-pixel square frame, aligned narrow
   rulers, clean white drawing area, thin orthogonal routes, tiny topology markers, compact explicit annotations, and no
   construction grid, debug/source text, persistent port rings, bottom table, clipping, or overlap.

## Tasks / Subtasks

- [x] Extend the current typed product automation seam for M46 connection operations (AC: 2-4)
  - [x] Expose current Scene Connection/Port identities, traces, directions, segments, markers, annotations, and accepted
    revision through `automationState()` without painting or creating a second read model.
  - [x] Add typed automation commands that reuse `commitConnectionIntent`, `commitRouteIntent`, and
    `executeJournalOperation`; automation must never write source, Sheet, or Scene directly.
  - [x] Add frontend tests proving valid/rejected reconnect and route operations use generated typed envelopes, correct
    authority/writable files, stable trace identity, and one accepted transaction per command.

- [x] Add M46 product author/reopen launcher (AC: 1-4)
  - [x] Copy `examples/m46/rolling-shutter` to one disposable temporary repository; snapshot the active example before
    and after to prove it never changes.
  - [x] Open temporary repository root before source/widget access and fail immediately on missing Explorer root,
    Repository `READY`, exact LSP root, Connection read model, or `READY` Scene.
  - [x] Execute valid reconnect, direction-invalid reconnect, route edit, Undo, Redo, stale rejection, and fresh-process
    reopen; record source snapshots, revisions, Scene/Connection identities, traces, journals, diagnostics, and changed
    file paths.
  - [x] Capture desktop and narrow screenshots plus explicit ruler/frame/paint checks; no visual pass may be inferred
    from file existence alone.

- [x] Publish deterministic M46 SVG and PNG exports (AC: 4-6)
  - [x] Replace the active M44-specific SVG golden generator/task with an M46 export generator consuming the reopened
    accepted M46 Scene and admitted asset bundle; no M43/M44/M45 fixture or route contract may be read.
  - [x] Render SVG twice and assert byte equality; rasterize the same SVG twice at one pinned viewport/DPR and assert PNG
    byte equality, dimensions, nonblank pixels, one-pixel linework, compact labels, and exact topology marker counts.
  - [x] Write exports and digest evidence only under `_bmad-output/implementation-artifacts/m46/exports`.
- [x] Verify the complete rebuilt product loop and publish evidence (AC: 1-6)
  - [x] Rebuild LSP distribution, frontend, and Electron product before proof; terminate proof process trees so generated
    native bundles are never left locked.
  - [x] Run focused/full frontend tests, affected sequential Gradle tests, M46 author/reopen/export verifier, encoding
    audit, source-set hygiene audit, and `git diff --check`.
  - [x] Record fresh screenshots, operation transcript, reopen evidence, export digests, verification output, File List,
    and Completion Notes before status `review`.

## Dev Notes

### Authority Chain

```text
product gesture/automation intent
  -> typed EditOperationEnvelope
  -> LSP Source Revision validation
  -> accepted source transaction/journal
  -> compiler-owned Connection IR + route plan
  -> Canonical SceneConnection publication
  -> Konva/SVG adapters
  -> reopened product/export evidence
```

- Canvas, automation, Konva, SVG, and screenshots never own Engineering or Presentation truth.
- Valid reconnect writes Engineering source only. Route adjustment writes Sheet Companion only. Rejected/stale operations
  write nothing. Undo/Redo reverse/reapply accepted source transactions, not mouse events.
- Export must use accepted Canonical Scene plus admitted Asset Bundle. Never export Konva node trees or scrape DOM as
  semantic input.
- Keep M46 proof names, hooks, paths, and evidence independent. Do not call M43/M44/M45 launchers or read their examples,
  transcripts, screenshots, exports, environment variables, or acceptance constants.
- Product proof must use a temporary writable copy. Active `examples/m46/rolling-shutter` remains unchanged.
- No compatibility aliases, deprecated route contracts, `SceneRoute`, generic relationship-to-route fallback, milestone-
  named production types, or production `*Proof/*Demo/*Sample` classes.

### Existing Code To Change

- `ide/theia-frontend/src/browser/athena-presentation-widget.tsx`: current automation exposes placement and journal
  commands only; add connection/route commands by calling existing protected transaction methods. Expand state with
  current Scene facts required to select valid/rejected operands.
- `ide/theia-frontend/scripts/athena-connection-authoring.test.mjs`: extend current structural tests rather than create a
  second automation protocol.
- `ide/theia-product/scripts/athena-m44-presentation-operations-main.js` and
  `verify-athena-m44-presentation-operations.js`: historical patterns only. Create M46-named launcher/verifier with exact
  workspace-root checks and robust process-tree termination; do not import or execute these scripts.
- `kernel/svg-renderer/src/test/kotlin/com/engineeringood/athena/svg/ActiveSceneSvgGoldenGenerator.kt` and
  `kernel/svg-renderer/build.gradle.kts`: current active generator is hardcoded to M44. Replace it with current M46 export
  generation and M46 artifact paths; pre-1.0 compatibility is forbidden.
- `ide/theia-product/package.json`: add one `verify:m46-authoring` or equivalent complete product command. Keep
  `verify:m46-performance` separate.

### Previous Story Intelligence

- Story 4.2 established a dedicated M46 launcher/verifier, exact workspace activation, rebuilt LSP distribution, raw
  evidence-first gates, off-screen proof execution, and Windows process-tree cleanup. Reuse these mechanics.
- Do not hide a proof BrowserWindow: hidden Electron windows caused intermittent RAF/workbench stalls. Keep it renderable
  off-screen with background throttling disabled.
- Final 4.2 evidence proves stable per-Connection paint identity and bounded invalidation. Reopen proof must assert these
  identities, not replace adapter state wholesale and call that stability.
- `:ide:lsp:installDist` must be rebuilt before product E2E; stale distribution previously caused unsupported
  `athena/connectionReadModel` and old Style Companion parsing.

### Verification

```powershell
.\gradlew.bat --no-daemon --console=plain :kernel:svg-renderer:test
.\gradlew.bat --no-daemon --console=plain :ide:lsp:test
.\gradlew.bat --no-daemon --console=plain :ide:lsp:installDist
Set-Location ide
yarn workspace @engineeringood/athena-theia-frontend test
yarn workspace @engineeringood/athena-theia-product build
yarn workspace @engineeringood/athena-theia-product verify:m46-authoring
Set-Location ..
powershell -ExecutionPolicy Bypass -File .\tools\encoding-audit.ps1
powershell -ExecutionPolicy Bypass -File .\tools\source-set-hygiene-audit.ps1
git diff --check
```

## References

- [Source: `_bmad-output/planning-artifacts/m46/epics.md`, Epic 4 / Story 4.3]
- [Source: `_bmad-output/planning-artifacts/prds/prd-Athena-2026-08-10-m46/prd.md`, FR13-FR16, NFR1-NFR3/NFR7]
- [Source: `_bmad-output/planning-artifacts/prds/prd-Athena-2026-08-10-m46/addendum.md`, Product Proof]
- [Source: `_bmad-output/planning-artifacts/architecture/architecture-Athena-2026-08-10-m46/ARCHITECTURE-SPINE.md`, AD-12 through AD-19]
- [Source: `_bmad-output/planning-artifacts/m46/implementation-plan.md`, Task 14]
- [Source: `_bmad-output/implementation-artifacts/m46/4-2-prove-incremental-connection-performance.md`]
- [Source: `AGENTS.md`, E2E Proof, Theia Workspace Proof, Visual Golden, Pre-1.0, and Build Verification rules]

## Dev Agent Record

### Agent Model Used

Codex

### Debug Log References

- `.\gradlew.bat --no-daemon --console=plain :kernel:svg-renderer:test` passed.
- `.\gradlew.bat --no-daemon --console=plain :ide:lsp:test` passed.
- `.\gradlew.bat --no-daemon --console=plain :ide:lsp:installDist` passed.
- `yarn workspace @engineeringood/athena-theia-frontend test` passed.
- `yarn workspace @engineeringood/athena-theia-product build` passed.
- `yarn workspace @engineeringood/athena-theia-product verify:m46-authoring` passed.
- `yarn workspace @engineeringood/athena-theia-product verify:m46-export` passed.
- Encoding audit, source-set hygiene audit, and `git diff --check` passed.

### Completion Notes List

- Rebuilt product proof accepts valid reconnect and route edits, rejects invalid and stale operations without mutation,
  proves undo/redo and fresh-process reopen, and exports deterministic SVG/PNG from accepted Canonical Scene.

### File List

- `_bmad-output/implementation-artifacts/m46/4-3-prove-edit-reopen-and-deterministic-export.md`
- `_bmad-output/implementation-artifacts/m46/sprint-status.yaml`
- `_bmad-output/implementation-artifacts/m46/exports/m46-rolling-shutter.svg`
- `_bmad-output/implementation-artifacts/m46/exports/m46-rolling-shutter.png`
- `_bmad-output/implementation-artifacts/m46/exports/m46-export-proof.json`
- `_bmad-output/implementation-artifacts/m46/operation-transcripts/4-3-author-reopen-product-proof.json`
- `ide/lsp/build.gradle.kts`
- `ide/lsp/src/test/kotlin/com/engineeringood/athena/ide/lsp/M46SvgExportEvidenceGenerator.kt`
- `ide/theia-product/package.json`
- `ide/theia-product/scripts/verify-athena-m46-authoring.js`
- `ide/theia-product/scripts/athena-m46-authoring-main.js`
- `ide/theia-product/scripts/verify-athena-m46-export.js`
- `ide/theia-frontend/src/browser/athena-presentation-widget.tsx`
- `ide/theia-frontend/scripts/athena-konva-adapter.test.mjs`

### Change Log

- 2026-08-11: Story 4.3 implemented and product evidence verified; status `review`.
