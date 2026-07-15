# `:ide/tree-sitter-athena`

English | [Chinese (Simplified)](README.zh-CN.md)

Athena Tree-sitter grammar for **syntax UX only** (AD-107).

## Boundary

- Owns: incremental syntax tree, highlight queries, incomplete-source tolerance for editor affordances.
- Does **not** own: semantic diagnostics, `Engineering IR`, package meaning, or any compiler truth.
- Semantic diagnostics stay on the compiler/LSP path (`ide/lsp` -> `AthenaCompiler`).

## Contents

- `grammar.js` - AD-110 parity subset (`system` / `device` / `port` / `connect` / qualified names / strings / properties)
- `src/` - generated parser sources (`tree-sitter generate`)
- `queries/highlights.scm` - highlight queries for Story 3.2
- `test/corpus/` - Tree-sitter corpus proofs
- `tree-sitter-athena.wasm` - WebAssembly binary for Theia (`yarn build`; generated through Zig)

## Scripts

```bash
yarn --cwd ide/tree-sitter-athena generate
yarn --cwd ide/tree-sitter-athena build
yarn --cwd ide/tree-sitter-athena test
```

`yarn --cwd ide/tree-sitter-athena build` looks for Zig in this order:

1. `ATHENA_ZIG`
2. `ATHENA_ZIG_BIN`
3. repo-local `.tools/zig/...`
4. `zig` on `PATH`

On Windows x64, if none of those exist, the build script bootstraps the pinned Zig toolchain into
repo-local `.tools/zig/`. That keeps the wasm build usable in local and CI runs without a
machine-specific hard-coded Zig path.

Current limitation: automatic bootstrap is only implemented for Windows x64. On macOS/Linux, wasm
regeneration still requires Zig to be supplied through `ATHENA_ZIG`, `ATHENA_ZIG_BIN`, or `PATH`
until the cross-platform bootstrap follow-up is completed.

## Verification

- Corpus: `npx tree-sitter test`
- Incomplete-source tolerance: `node --test scripts/*.test.mjs`
