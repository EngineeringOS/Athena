---
baseline_commit: f407e9fc48ec28b40b909a326af28993d3edb3c2
---

# Story 4.3: Add Product Smoke And Regression Coverage

Status: done

## Story

As an Athena maintainer,
I want executable proof that M26 works through the real IDE path,
so that the milestone is not only model-level code.

## Acceptance Criteria

1. Product smoke or equivalent IDE verification checks the M26 sample opens.
2. Smoke/regression checks sheet views are available, view switching is wired, compact reference
   markers render when marker facts are present, and reference reveal resolves through occurrence
   identity.
3. Regression coverage verifies document projection identity stability and source-file
   rename/reorder stability.
4. Regression coverage verifies no default raw fully qualified semantic route labels crowd the
   canvas.
5. Verification commands run sequentially on Windows and smoke evidence is referenced from M26
   usage or retrospective artifacts.

## Tasks / Subtasks

- [x] Add M26 product start/smoke commands (AC: 1, 5)
  - [x] Add `start:m26` and `start:smoke:m26` Theia product commands.
  - [x] Keep commands scoped to the Theia IDE product.
- [x] Add M26 smoke assertions (AC: 1, 2, 4, 5)
  - [x] Validate sample workspace path and required files.
  - [x] Validate graph workbench DOM proof for selector and compact reference marker hooks.
  - [x] Validate no default verbose semantic route labels are visible.
- [x] Add regression coverage references (AC: 3, 4, 5)
  - [x] Reuse existing kernel identity stability and frontend compact-label tests.
  - [x] Reference smoke evidence from usage documentation.

## Dev Notes

- Active frontend is Theia only.
- Do not touch `apps:desktop-viewer`, `ui:compose-workbench`, or deprecated KMP frontend modules.
- Smoke scripts may extend the existing Electron graph workbench proof collector; M26-specific
  assertions should remain in the M26 smoke script.
- Verification must run sequentially on Windows.

## Dev Agent Record

### Agent Model Used

Codex GPT-5

### Debug Log References

- Story started after Story 4.2 documentation verification.
- `yarn test` in `integrations/graph-glsp` passed after adding sheet role transport coverage.
- `.\gradlew.bat --no-daemon --console=plain :kernel:compiler:test` passed after migrating
  documentation sheet expectations to the M26 three-view contract.
- `.\gradlew.bat --no-daemon --console=plain :kernel:runtime:test` passed after migrating runtime
  projection session expectations.
- `.\gradlew.bat --no-daemon --console=plain :ide:lsp:test` passed after migrating LSP payload
  expectations.
- `yarn --cwd ide build` passed and rebuilt the Theia product plus dev LSP runtime.
- `yarn --cwd ide start:smoke:m26` passed with three sheet-view options and hidden verbose route
  labels.
- `yarn test` in `ide/theia-frontend` passed after usage evidence coverage was updated.

### Completion Notes List

- Added Theia-only `start:m26` and `start:smoke:m26` commands for the M26 sample project.
- Added M26 product smoke verification for sample workspace resolution, document sheet-view
  selector proof, M24 route proof, M25 representation proof, and hidden verbose semantic route
  labels.
- Extended the Electron smoke proof collector to switch to the documentation projection and report
  document projection DOM evidence.
- Migrated compiler, runtime, and LSP regression expectations from the pre-M26 two-sheet
  documentation view to the M26 three-view semantic document projection contract.
- Recorded product smoke evidence and marker regression coverage in the M26 usage guide.

### File List

- `_bmad-output/implementation-artifacts/m26/4-3-add-product-smoke-and-regression-coverage.md`
- `_bmad-output/implementation-artifacts/m26/sprint-status.yaml`
- `docs/usages/m26-proof-usage.md`
- `ide/package.json`
- `ide/theia-product/package.json`
- `ide/theia-product/scripts/athena-electron-open-workspace-main.js`
- `ide/theia-product/scripts/verify-athena-m26-sample-project.js`
- `ide/theia-frontend/scripts/athena-m26-sample-project.test.mjs`
- `integrations/graph-glsp/src/athena-glsp-projection-adapter.ts`
- `integrations/graph-glsp/test/athena-graph-glsp-adapter.test.mjs`
- `kernel/compiler/src/main/kotlin/com/engineeringood/athena/compiler/ProjectionModelDeriver.kt`
- `kernel/compiler/src/test/kotlin/com/engineeringood/athena/compiler/AthenaCompilerM11DepthTest.kt`
- `kernel/compiler/src/test/kotlin/com/engineeringood/athena/compiler/AthenaCompilerM12RendererBenchmarkTest.kt`
- `kernel/runtime/src/test/kotlin/com/engineeringood/athena/runtime/AthenaRuntimeProjectionDepthTest.kt`
- `kernel/runtime/src/test/kotlin/com/engineeringood/athena/runtime/AthenaRuntimeProjectionSessionTest.kt`
- `ide/lsp/src/test/kotlin/com/engineeringood/athena/ide/lsp/AthenaProjectionM11DepthRequestTest.kt`
- `ide/lsp/src/test/kotlin/com/engineeringood/athena/ide/lsp/AthenaProjectionRequestTest.kt`

## Change Log

- 2026-07-20: Created Story 4.3 from M26 Epic 4.
- 2026-07-20: Added M26 product smoke commands, assertions, runtime projection updates, and
  regression evidence.
- 2026-07-20: Marked Story 4.3 done after GLSP, compiler, runtime, LSP, IDE build, product smoke,
  and frontend verification.
