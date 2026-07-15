# M17 Parser Proof Examples

This folder is Athena's checked-in M17 parser parity and failure-quality evidence.

Per AD-113, checked-in proof inputs are stronger than inline-only parser demos. This corpus is the primary milestone evidence for parser parity, and it supersedes any inline-only test-source snippet as M17's primary parity proof.

## Layout

| Path | Purpose |
| --- | --- |
| `parser-parity-proof/` | Valid standalone `.athena` fixtures with `.expectation.txt` sidecars covering the supported AD-110 subset |
| `repository-parity-proof/` | Governed repository fixture (`athena.yaml` / `athena.lock` / `src/*.athena`) through real package-resolution seams |
| `invalid-and-incomplete-proof/` | Malformed and incomplete sources for compiler diagnostics and Tree-sitter UX tolerance |

## Valid Parity Fixtures

- `parser-parity-proof/parity-cabinet.athena` (+ `.expectation.txt`) - system/device/port/connect/property coverage mirroring `examples/m0/demo-cabinet`.
- `parser-parity-proof/dense-qualified-names.athena` (+ `.expectation.txt`) - denser qualified-name and string-literal-property coverage.
- `repository-parity-proof/{athena.yaml,athena.lock,src/parity-repo.athena}` - governed repository fixture resolved through `AthenaCompiler.validateRepositoryContract` / `validateRepositoryLock` / `resolveRepositoryGraph`.

Each valid `.expectation.txt` records `status=`, `components=`, `ports=`, `connections=`, `svg=`, and `diagnostics=` in the `examples/m0` conformance-suite format.

## Invalid / Incomplete Fixtures

- `invalid-and-incomplete-proof/unterminated-string.athena` - a string literal missing its closing `"`.
- `invalid-and-incomplete-proof/incomplete-brace.athena` - a `device` block missing its closing `}` at EOF.
- `invalid-and-incomplete-proof/missing-arrow.athena` - a `connect` declaration missing `->`.
- `invalid-and-incomplete-proof/over-qualified-port.athena` - a `port` reference with more than two dotted segments.

Each invalid `.expectation.txt` records `status=syntax-failure` plus `syntaxErrorLine=`, `syntaxErrorColumn=`, and `syntaxErrorMessageContains=` so the failure position and a stable message fragment are pinned. Each fixture isolates exactly one syntax failure mode.

## Supported Syntax Scope

Fixtures stay parity-first on:

- `system`, `device`, `port`, `connect`
- qualified names
- string literals
- property assignments

No `import`, expression language, or macro-use forms.

## Two-Track Verification (Compiler vs Tree-sitter)

Compiler-diagnostic verification and Tree-sitter-UX verification are two distinct, separately-run checks over the same or overlapping fixture set. They are never collapsed into one combined "the editor didn't crash" assertion.

1. **Compiler diagnostics (this milestone, automated now)**
   - Valid corpus: `AthenaM17ParserParityProofTest` (`:kernel:compiler`) compiles each `parser-parity-proof` fixture and the repository fixture, asserting the exact `EngineeringDocument` shape and identity scheme.
   - Malformed corpus: `AthenaM17InvalidSourceProofTest` (`:kernel:language`) parses each `invalid-and-incomplete-proof` fixture and asserts a typed, positioned `SyntaxDiagnostic` (file, line, column, message) rather than an uncaught exception or a positionless error.
2. **Tree-sitter syntax UX (Epic 3, documented now / automated once Epic 3 lands)**
   - Once Epic 3 publishes the Tree-sitter grammar and Theia integration, the `incomplete-brace` and `missing-arrow` fixtures must additionally be opened in the editor (or exercised through the Tree-sitter grammar's own harness under `ide/tree-sitter-athena`) to confirm a usable syntax tree is still produced for the chosen syntax-UX capability (highlighting, folding, or outline).
   - This Tree-sitter-UX test stays in a separate file/module from the compiler-diagnostic tests so the two verdicts remain independently runnable and reportable.

"The editor did not crash" is not an acceptable substitute for either check: a typed compiler diagnostic and a usable Tree-sitter syntax tree must each be demonstrated independently.

## Verification

See `docs/usages/m17-proof-usage.md` for the full sequential verification path across Epics 4 and 5.
