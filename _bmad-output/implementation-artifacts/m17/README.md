# M17 Implementation Artifacts

This folder preserves the M17 implementation artifacts under the milestone-standard `m17/` path.

M17 is the parsing, AST, and editor language foundation milestone. It proves that Athena can grow its authored language on a durable dual-parser architecture: `ANTLR4` for compiler/LSP parsing, Tree-sitter for IDE syntax UX, and a preserved authored AST boundary before lowering to `Engineering IR`.

## Planned Scope

- Epic 1: authored AST boundary and syntax contract hardening
- Epic 2: ANTLR4 compiler parser migration
- Epic 3: Tree-sitter IDE syntax path
- Epic 4: compiler and LSP language-service continuity
- Epic 5: parser parity, proof corpus, and future syntax landing zone

## Included Stories

1. `1-1-freeze-the-public-authored-syntax-contract.md`
2. `1-2-isolate-parser-implementation-behind-the-language-facade.md`
3. `1-3-preserve-ast-extensibility-for-future-syntax.md`
4. `2-1-publish-the-antlr4-grammar-for-the-current-supported-syntax-subset.md`
5. `2-2-adapt-antlr-parse-trees-into-the-authored-ast.md`
6. `2-3-preserve-provenance-and-failure-quality-on-the-antlr-path.md`
7. `3-1-publish-the-tree-sitter-grammar-for-athena-syntax-ux.md`
8. `3-2-integrate-tree-sitter-into-one-supported-theia-syntax-ux-path.md`
9. `3-3-prove-incomplete-source-editor-tolerance.md`
10. `4-1-keep-semantic-diagnostics-on-the-compiler-parser-path.md`
11. `4-2-preserve-source-navigation-and-symbol-utility-across-parser-migration.md`
12. `4-3-preserve-compiler-output-continuity-on-supported-source.md`
13. `5-1-publish-a-checked-in-parser-parity-corpus.md`
14. `5-2-publish-invalid-and-incomplete-source-proof-inputs.md`
15. `5-3-publish-the-future-syntax-landing-zone-note-and-verification-path.md`
16. `sprint-status.yaml`
17. `m17-retrospective-2026-07-15.md`
18. `m17-follow-ups-2026-07-15.md`

## Current Status

- Milestone state: complete
- Milestone tracking: `sprint-status.yaml`
- Verified on 2026-07-15:
  - `:kernel:language:test --tests *M17*`
  - `:kernel:compiler:test --tests *M17*`
  - `:kernel:compiler:test --tests *Continuity*`
  - `:ide:lsp:test --tests *SemanticAuthority*`
  - `:ide:lsp:test --tests *NavigationParity*`
  - `yarn --cwd ide/tree-sitter-athena build`
  - `yarn --cwd ide/tree-sitter-athena test`
  - `yarn --cwd ide workspace @engineeringood/athena-theia-frontend test`
  - `yarn --cwd ide build`
  - `yarn --cwd ide start:smoke:tree-sitter`
- Planning inputs:
  - `_bmad-output/planning-artifacts/prds/prd-Athena-2026-07-14-m17/prd.md`
  - `_bmad-output/planning-artifacts/prds/prd-Athena-2026-07-14-m17/addendum.md`
  - `_bmad-output/planning-artifacts/architecture/architecture-Athena-2026-07-14-m17/ARCHITECTURE-SPINE.md`
  - `_bmad-output/planning-artifacts/epics-M17-2026-07-14.md`

## Milestone Intent

- Freeze one durable language architecture before language breadth expands.
- Keep `Engineering IR` as the only canonical semantic authority.
- Keep `ide/lsp` as the sole semantic entry point for IDE language meaning.
- Use `ANTLR4` for compiler and LSP parsing; keep generated parser types internal.
- Use Tree-sitter for syntax UX only; never for semantic truth or `Engineering IR` derivation.
- Preserve spans, provenance, and typed failure quality across parser migration.
- Keep the first proof parity-first on the current supported syntax subset.
- Leave a deliberate landing zone for future constructs such as `import`.

## Product Position

- Athena is `semantic-first`, not `DSL-first`.
- Direct DSL remains canonical serialization and an expert surface, not the default human interface.
- Compiler parser and editor parser diverge by responsibility, not by architecture.
- Workbench, AI, API, and graph surfaces remain consumers of shared platform services.
- M8 remains the only mutation authority; M17 does not open a second write path.

## Usage

- Use `sprint-status.yaml` as the single milestone progress tracker.
- Implement stories in epic order unless sprint planning explicitly reorders them.
- Keep story files in this folder using the existing `1-1-...md` naming pattern.
- The legacy handwritten compiler parser path is removed. M17's live compiler/LSP parser is ANTLR only; Tree-sitter remains IDE syntax UX only.
- Compiler backend rendering no longer hardcodes the electrical `cabinet` view. The emitted SVG view must be selected from approved plugin-published SVG-capable views in deterministic view order.
- Epic 3 is closed only through the verified grammar build/package test, frontend unit test, product build, and `start:smoke:tree-sitter` path listed above.
- Cross-platform Tree-sitter wasm bootstrap follow-up is tracked in `m17-follow-ups-2026-07-15.md`. Current auto-bootstrap coverage is Windows x64 only; macOS/Linux regeneration still requires Zig until that follow-up lands.
- After docs or text assets change, run `powershell -ExecutionPolicy Bypass -File .\tools\encoding-audit.ps1`.
- On Windows, run Gradle verification sequentially. Never overlap `gradlew` invocations.
