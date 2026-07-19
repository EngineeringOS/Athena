---
status: review
epic: 3
story: 3.4
title: Preserve M23 syntax and projection regression behavior
baseline_commit: e4b243ab9441d585e79c2d8effceb1a6692a3be2
---

# Story 3.4: Preserve M23 syntax and projection regression behavior

As an Athena maintainer, I want routing work to preserve M23 language admission, so that M24 does
not break layout blocks or active-source projection.

## Acceptance Criteria

- M23 layout-block sample and parser fixtures still pass.
- ANTLR4, Tree-sitter, compiler, LSP, and Theia still accept the M23 sample.
- Active-source Graphical View projection still uses the currently opened `.athena` file.
- No route feature introduces new source syntax without dual-parser parity.
- Product smoke rebuilds or uses the current installed LSP host before making IDE claims.

## References

- `../../planning-artifacts/prds/prd-Athena-2026-07-18-m24/prd.md`
- `../../planning-artifacts/architecture/architecture-Athena-2026-07-18-m24/ARCHITECTURE-SPINE.md`

## Notes

This story protects the M23 root-cause lesson: source tests alone do not prove the Theia product path.

## Tasks/Subtasks

- [x] Verify M23 layout-block sample and parser fixtures still pass.
- [x] Verify compiler and LSP layout-block behavior still accepts the M23 sample.
- [x] Verify active-source Graphical View projection still follows the latest opened `.athena` source.
- [x] Verify no M24 routing work introduced route source syntax without dual-parser parity.
- [x] Verify Theia frontend/product smoke path for the M23 sample.

## Dev Agent Record

### Debug Log

- Ran focused language parser and ANTLR grammar regression tests for the M23 parser parity corpus.
- Ran compiler layout tests with `:kernel:compiler:test --tests "*Layout*"`.
- Ran LSP M23 diagnostics and active-source projection regressions.
- Ran Theia frontend tests and M23 product smoke.
- Scanned source/parser fixtures for route-block syntax; no admitted route source syntax was found.

### Completion Notes

- M24 routing work preserved M23 layout block syntax and active-source projection behavior.
- The Theia product smoke confirmed the M23 sample still opens through the IDE path.
- No route syntax was introduced in M24 Epic 3.
- Validation passed:
  - `.\gradlew.bat --no-daemon --console=plain :kernel:language:test --tests "com.engineeringood.athena.language.AthenaLanguageParserTest.parses m23 layout block into authored syntax-only ast"`
  - `.\gradlew.bat --no-daemon --console=plain :kernel:language:test --tests "com.engineeringood.athena.language.antlr.AthenaGrammarSmokeTest.m23 parser parity corpus accepts valid layout syntax and rejects invalid layout syntax"`
  - `.\gradlew.bat --no-daemon --console=plain :kernel:compiler:test --tests "*Layout*"`
  - `.\gradlew.bat --no-daemon --console=plain :ide:lsp:test --tests "com.engineeringood.athena.ide.lsp.AthenaDiagnosticsPublishingTest.valid m23 layout block publishes no false lsp diagnostics"`
  - `.\gradlew.bat --no-daemon --console=plain :ide:lsp:test --tests "com.engineeringood.athena.ide.lsp.AthenaProjectionRequestTest.projection session request follows latest opened source file in governed repository"`
  - `yarn --cwd ide/theia-frontend test`
  - `yarn --cwd ide/theia-product start:smoke:m23`
  - `rg "route\s+schematic|route\s*\{" examples kernel\language ide\tree-sitter-athena -g "*.athena" -g "*.g4" -g "*.scm"`

## File List

- `_bmad-output/implementation-artifacts/m24/3-4-preserve-m23-syntax-and-projection-regression-behavior.md`
- `_bmad-output/implementation-artifacts/m24/sprint-status.yaml`

## Change Log

- 2026-07-19: Completed Story 3.4 M23 syntax, LSP, Theia smoke, and active-source regression verification.

## Status

review
