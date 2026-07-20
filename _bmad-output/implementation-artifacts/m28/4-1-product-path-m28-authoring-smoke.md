---
status: done
story_id: 4.1
epic: 4
title: Product-Path M28 Authoring Smoke
baseline_commit: c7fda3877a76fd8db52a65510e1b123aed4304f1
---

# Story 4.1: Product-Path M28 Authoring Smoke

## Story

As an Athena maintainer, I want a product smoke that proves accepted and rejected authoring flows,
so that M28 is not accepted on unit tests alone.

## Acceptance Criteria

- Smoke opens `examples/m28/sample-project` in Theia.
- Smoke accepts one valid electrical relationship through product UI path.
- Smoke rejects two invalid relationship attempts without source mutation.
- Smoke verifies source-backed projection refresh.
- Smoke does not use DOM text as semantic authority.

## Tasks/Subtasks

- [x] Add product smoke script/test.
- [x] Capture pre-mutation source state.
- [x] Drive valid relationship acceptance.
- [x] Drive invalid relationship rejection cases.
- [x] Verify source, projection, and screenshot/proof evidence.

## Dev Notes

- Architecture: M28 AD-6, AD-7, AD-9 are binding.

## Dev Agent Record

### Debug Log

- Added `AthenaM28ProductAuthoringSmokeTest` against `examples/m28/sample-project`.
- Added M28 product smoke script and package entry points.
- Closed backend edit-gate gap: accepted invalid electrical relationships now return no source edit.
- Verified focused LSP smoke, frontend/product wiring, LSP installDist, and Electron sample smoke.

### Completion Notes

- M28 sample product path now proves valid semantic relationship acceptance through LSP-backed source edit, projection refresh, and routing corridor evidence.
- Invalid output-output and input-input attempts are rejected without source mutation; an accepted invalid request is also blocked by the backend source-edit gate.
- Product startup smoke opens the M28 sample workspace in Theia and verifies graph proof payload availability.

## File List

- ide/lsp/src/main/kotlin/com/engineeringood/athena/ide/lsp/AthenaConnectPortsSourceEditProtocol.kt
- ide/lsp/src/test/kotlin/com/engineeringood/athena/ide/lsp/AthenaM28ProductAuthoringSmokeTest.kt
- ide/package.json
- ide/theia-product/package.json
- ide/theia-product/scripts/verify-athena-m28-sample-project.js
- ide/theia-frontend/scripts/athena-m28-product-smoke-wiring.test.mjs

## Change Log

- 2026-07-21: Story created for M28.
- 2026-07-21: Added M28 product-path authoring smoke, product workspace smoke wiring, and backend invalid-relationship edit gate.

## Verification

- `.\gradlew.bat --no-daemon --console=plain :ide:lsp:test --tests "com.engineeringood.athena.ide.lsp.AthenaM28ProductAuthoringSmokeTest"`: passed.
- `yarn build; if ($LASTEXITCODE -eq 0) { node --test scripts/athena-m28-product-smoke-wiring.test.mjs } else { exit $LASTEXITCODE }` in `ide/theia-frontend`: passed.
- `.\gradlew.bat --no-daemon --console=plain :ide:lsp:installDist`: passed.
- `yarn start:smoke:m28` in `ide`: passed.
