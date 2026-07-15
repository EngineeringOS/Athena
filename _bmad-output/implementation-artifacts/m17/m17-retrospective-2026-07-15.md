# M17 Retrospective

Date: 2026-07-15
Review Updated: 2026-07-15

## What Worked

- Freezing the authored AST boundary first made the parser swap tractable. The compiler, runtime, and LSP layers continued to consume `SourceFileAst` instead of parser-specific types.
- Replacing the handwritten parser outright simplified the architecture. After the review fix, `AthenaLanguageParser` now has one live production path: ANTLR4 -> internal parse adapter -> authored AST.
- The compiler/LSP continuity proofs were strong enough to verify the semantic side of the milestone directly. The M17 language tests, compiler parity tests, continuity tests, and LSP semantic-authority/navigation tests all passed on 2026-07-15.
- The repository-backed proof corpus in `examples/m17` and the dedicated M17 test classes made it possible to review milestone readiness from executable evidence instead of prose alone.

## What Needed Tightening

- The ANTLR migration landed with build wiring drift. Generated ANTLR sources existed, but the grammar package declaration and Kotlin compile wiring were incomplete, so the live parser path did not actually build until the review patched `kernel/language/build.gradle.kts` and `Athena.g4`.
- Epic 3 closeout exposed three concrete IDE gaps that had to be finished before the milestone could close: the checked-in grammar wasm artifact, an actual packaged-frontend asset-copy step, and explicit frontend/Electron proof commands. Those are now in place and verified.
- The Tree-sitter frontend bridge also had one real runtime defect: the default asset locator was declared async, which meant Node/Electron startup never received a synchronous `web-tree-sitter.wasm` path. The closeout fixed that bug before the new frontend/product proofs were accepted.

## Achievements Recorded

- Public authored syntax contracts were frozen and documented in `kernel/language`.
- The live compiler parser was migrated to ANTLR4, and the handwritten parser path was removed instead of being kept for compatibility.
- Lowering continuity into canonical `Engineering IR` was preserved.
- `ide/lsp` semantic authority, diagnostics, document symbols, definition, and references remained on the compiler-owned path and verified successfully.
- The Tree-sitter grammar package now has a checked-in wasm artifact, deterministic Zig-backed rebuild command, repository-backed parity tests, frontend semantic-tokens proof, packaged-frontend asset copy, and an Electron smoke path.
- The future-syntax landing-zone and usage documentation for M17 were published.
- The compiler backend no longer hardcodes the electrical `cabinet` view as its default render target; emitted SVG selection now follows approved plugin-published SVG-capable views, which keeps future domain/plugin growth on the intended extension seam.

## Closeout Result

- M17 is closed. Epic 3 now verifies cleanly alongside the earlier compiler/LSP proofs, and the legacy handwritten compiler parser path remains removed.

## Usage Note

- Use `docs/usages/m17-proof-usage.md` as the current closeout guide.
- Treat `sprint-status.yaml` as the source of truth for milestone state: Epics 1 through 5 are done.
- Keep Gradle verification sequential on Windows. The review evidence for the compiler/LSP side came from sequential runs only.
