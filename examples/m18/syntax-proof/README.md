# M18 Syntax Proof

These fixtures prove the narrow package/import syntax slice through two independent paths:

- `AthenaM18SyntaxProofTest` checks compiler-owned AST values and typed diagnostics.
- `athena-tree-sitter-grammar-corpus.test.mjs` checks syntax-only editor trees and recovery.

A usable Tree-sitter recovery node is not compiler or semantic acceptance. Package availability,
import resolution, linking, and lowering are intentionally deferred to later M18 corpus slices.

## Inventory

- Valid: `valid-package-only.athena`, `valid-package-import.athena`
- Invalid: alias, wildcard, visibility, and missing import target fixtures

Each invalid `.expectation.txt` file has exactly these compiler-facing keys:

- `status=syntax-failure`
- `syntaxErrorLine`
- `syntaxErrorColumn`
- nonblank `syntaxErrorMessageContains`
