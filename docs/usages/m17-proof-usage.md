# M17 Parser Architecture Proof Usage

Updated: 2026-07-15

## Review Status

Review updated: 2026-07-15

As of the 2026-07-15 closeout, M17 verifies cleanly end to end:

- `:kernel:language:test --tests *M17*` passes
- `:kernel:compiler:test --tests *M17*` passes
- `:kernel:compiler:test --tests *Continuity*` passes
- `:ide:lsp:test --tests *SemanticAuthority*` passes
- `:ide:lsp:test --tests *NavigationParity*` passes
- `yarn --cwd ide/tree-sitter-athena build` passes
- `yarn --cwd ide/tree-sitter-athena test` passes
- `yarn --cwd ide workspace @engineeringood/athena-theia-frontend test` passes
- `yarn --cwd ide build` passes
- `yarn --cwd ide start:smoke:tree-sitter` passes

The legacy handwritten compiler parser path is removed. The live compiler/LSP parser path is ANTLR
only, and Tree-sitter remains scoped to IDE syntax UX only.

## Purpose

This guide records the M17 milestone's closeout evidence: the future-syntax landing zone for
constructs such as `import`, and the single, unified verification path spanning valid-source
parity, malformed-source diagnostics, Tree-sitter-backed syntax UX, and preserved LSP semantic
authority.

M17 proves that Athena can:

- parse the current supported syntax subset through a real parser-generator (`ANTLR4`) on the
  compiler/LSP path while keeping the authored AST as the only lowering input
- keep Tree-sitter scoped to IDE syntax UX, never semantic truth
- preserve source spans, provenance, and typed failure quality across the parser migration
- preserve compiler output (`Engineering IR`) shape and LSP navigation/diagnostics behavior across
  the same migration
- publish a checked-in, executable proof corpus instead of narrative parity claims

## Future Language Upgrade Standard

When Athena grows its language after M17, follow this standard:

1. Update the authoritative compiler grammar in `Athena.g4`.
2. Adapt that syntax into Athena-owned AST contracts, not generated parser types.
3. Update lowering and downstream semantic consumers intentionally.
4. Mirror the syntax-only shape in `ide/tree-sitter-athena/grammar.js` for IDE UX.
5. Re-run both proof tracks:
   - compiler/LSP authority and failure-quality proofs
   - Tree-sitter package, frontend semantic-tokens, and product smoke proofs

The compatibility rule is now explicit: do not restore the legacy handwritten compiler parser.
ANTLR remains the only live compiler/LSP parser path, and Tree-sitter remains syntax UX only.
Backend rendering selection must also stay plugin-owned: do not reintroduce a compiler-core
fallback to the electrical `cabinet` view. The emitted SVG view is selected from approved
plugin-published SVG-capable views in deterministic view-definition order.

## Tree-sitter Wasm Build Standard

`yarn --cwd ide/tree-sitter-athena build` is the canonical wasm regeneration command.

- On Windows x64, the build script auto-bootstraps a pinned Zig toolchain into repo-local
  `.tools/zig/` when Zig is not already available.
- The script also accepts `ATHENA_ZIG`, `ATHENA_ZIG_BIN`, repo-local `.tools/zig/...`, or `zig`
  on `PATH`.
- This keeps the Tree-sitter grammar build isolated from machine-global hard-coded paths in the
  normal Windows CI/local flow.

Current scope note: the automatic Zig bootstrap is implemented for Windows x64 only.

Cross-platform follow-up is explicitly tracked in
`_bmad-output/implementation-artifacts/m17/m17-follow-ups-2026-07-15.md`:

- macOS/Linux product builds may still consume the checked-in wasm without Zig
- macOS/Linux CI that regenerates the wasm must currently provide Zig through env or `PATH`
- repo-local pinned Zig bootstrap still needs to be extended to the main non-Windows CI runners

## Future Syntax Landing Zone

M17 freezes one language architecture before language breadth expands (AD-104). Every future
authored construct -- `import` included -- must land through the same chain:

```
compiler parser (ANTLR4) -> authored AST (SourceFileAst and future extensions) -> lowering (EngineeringIrLowerer) -> canonical Engineering IR
```

This chain is the only sanctioned landing path. A Tree-sitter-CST-to-`Engineering IR` shortcut is
never acceptable (AD-106, AD-107): Tree-sitter's role stays fixed to editor syntax UX regardless of
how much future syntax the language grows.

Per AD-111, future syntax growth must land through **AST extensibility, not ad hoc grammar
patches**: AST contracts remain extensible (the concrete mechanism is Story `1.3`'s AST
extensibility work in `kernel/language`), parser adaptation stays isolated inside
`com.engineeringood.athena.language.antlr` (the internal `ParseAdapter`/`AthenaAntlrParseEngine`
seam), and lowering stays organized around authored semantic categories -- `Declaration`'s sealed
hierarchy -- rather than parser token sequences.

Regarding the PRD's Open Question 2 ("should M17 include one narrow parse-only future-syntax seed
such as `import`?"): M17 did **not** seed an `import` token or grammar rule. `kernel/language/src/main/antlr/com/engineeringood/athena/language/antlr/Athena.g4`
defines only `system`, `device`, `port`, `connect`, qualified names, string literals, and property
assignments (AD-110). This is intentional: M17 proves the architecture can safely carry a future
`import` construct, not that `import` itself has landed. A future milestone that adds `import` must
extend the grammar, the authored AST (via Story `1.3`'s extensibility contract), and
`EngineeringIrLowerer`'s domain-plugin classification -- never bypass any of the three.

### Explicit Exclusions

Consistent with the PRD's Non-Goals (Section 7) and the architecture spine's Deferred section, M17
does **not** ship:

- final `import`/package semantics or full package-aware authored behavior
- a full macro-use language syntax or a full expression language
- a type system milestone or a dependency-resolution/semantic-validation redesign
- any architecture that lets Tree-sitter or generated parse trees become canonical truth
- a broad Theia visual redesign unrelated to parsing

## Verification Path

The M17 proof runs as four independent, separately-reportable checks over the same or overlapping
fixture sets. None of these checks substitutes for another: a clean Tree-sitter parse of a malformed
file is never evidence that the compiler considers the file valid, and a compiler diagnostic is
never evidence that the editor's syntax UX degrades gracefully.

### (a) Valid-source parity (Story 5.1)

Corpus: `examples/m17/parser-parity-proof/*.athena` (+ `.expectation.txt` sidecars) and the
governed repository-backed fixture `examples/m17/repository-parity-proof/` (`athena.yaml` /
`athena.lock` / `src/parity-repo.athena`).

```powershell
java25; .\gradlew.bat --no-daemon --console=plain :kernel:compiler:test --tests *M17*
```

`AthenaM17ParserParityProofTest` compiles every standalone fixture and the repository-backed
fixture through the live `AthenaCompiler`, asserting exact component/port/connection counts, the
`system:`/`component:`/`port:`/`connection:` identity scheme, emitted SVG, zero semantic
diagnostics, and deterministic re-compilation. `AthenaParserContinuityTest` cross-checks the same
identity/shape guarantee against the `examples/m0` baseline (Story 4.3), including a direct
structural comparison between `examples/m0/demo-cabinet.athena` and
`examples/m17/parser-parity-proof/parity-cabinet.athena`.

### (b) Malformed/incomplete-source diagnostics (Story 5.2)

Corpus: `examples/m17/invalid-and-incomplete-proof/*.athena` (+ extended `.expectation.txt`
sidecars carrying `syntaxErrorLine=`/`syntaxErrorMessageContains=`) -- one fixture each for an
unterminated string literal, a missing closing brace, a missing `connect` `->`, and an
over-qualified port reference.

```powershell
java25; .\gradlew.bat --no-daemon --console=plain :kernel:language:test --tests *M17*
```

`AthenaM17InvalidSourceProofTest` parses every fixture through `AthenaLanguageParser` and asserts
each one fails as a typed `SyntaxDiagnostic` (via `ParseFailure`) with real file/line/message
provenance -- never an uncaught exception or a positionless error (AD-109) -- and that failure is
deterministic across repeated parses.

### (c) Tree-sitter-backed syntax UX (Epic 3)

Per AD-107, this is a **separate** check from (b): it proves the editor still produces a usable
syntax tree for the same "incomplete block" and "missing arrow" shapes, never that the source is
semantically valid.

```powershell
yarn --cwd ide/tree-sitter-athena build
yarn --cwd ide/tree-sitter-athena test
yarn --cwd ide workspace @engineeringood/athena-theia-frontend test
yarn --cwd ide build
yarn --cwd ide start:smoke:tree-sitter
```

This runs the Tree-sitter grammar's own corpus tests and the `web-tree-sitter`-driven parity
script (Story `3.1`). If a future change opens the `incomplete-device-block.athena`/
`missing-connect-arrow.athena` shapes directly in the Theia editor (Story `3.2`'s highlighting
path), that manual check remains the IDE-level complement to this automated grammar-level check.

### (d) Preserved LSP semantic authority (Epic 4, Stories 4.1-4.3)

```powershell
java25; .\gradlew.bat --no-daemon --console=plain :ide:lsp:test --tests *SemanticAuthority*
java25; .\gradlew.bat --no-daemon --console=plain :ide:lsp:test --tests *NavigationParity*
java25; .\gradlew.bat --no-daemon --console=plain :kernel:compiler:test --tests *Continuity*
```

- `AthenaSemanticAuthorityBoundaryTest` (Story 4.1) proves every published LSP `Diagnostic` derives
  exclusively from `CompilerCompilationResult`, and that `AthenaLanguageServer.kt`/
  `AthenaLanguageFeatures.kt` never name Tree-sitter as a diagnostics source.
- `AthenaSourceNavigationParityTest` (Story 4.2) proves `documentSymbols`, `definition`,
  `references`, and navigation source ranges keep working unchanged over the authored
  `SourceFileAst`.
- `AthenaParserContinuityTest` (Story 4.3) proves `EngineeringIrLowerer` output shape and the
  six-pass compiler pipeline stay identical across the parser migration.

### Full sequential run

Run every command above sequentially -- never in parallel -- on this Windows repository, followed
by the encoding audit:

```powershell
java25; .\gradlew.bat --no-daemon --console=plain :kernel:language:test --tests *M17*
java25; .\gradlew.bat --no-daemon --console=plain :kernel:compiler:test --tests *M17*
java25; .\gradlew.bat --no-daemon --console=plain :kernel:compiler:test --tests *Continuity*
java25; .\gradlew.bat --no-daemon --console=plain :ide:lsp:test --tests *SemanticAuthority*
java25; .\gradlew.bat --no-daemon --console=plain :ide:lsp:test --tests *NavigationParity*
yarn --cwd ide/tree-sitter-athena build
yarn --cwd ide/tree-sitter-athena test
yarn --cwd ide workspace @engineeringood/athena-theia-frontend test
yarn --cwd ide build
yarn --cwd ide start:smoke:tree-sitter
powershell -ExecutionPolicy Bypass -File .\tools\encoding-audit.ps1
```

## Consistency Note

`examples/m17/README.md` (Stories 5.1/5.2) and `_bmad-output/implementation-artifacts/m17/README.md`
describe the same fixture layout, folder names, and test-class names as this note. If a future
change renames a fixture folder or test class, update all three documents together.

## Product Position

M16 proved:

> Athena can scale engineering meaning upward through governed, traceable reuse.

M17 proves:

> Athena can grow its authored language on a durable parser architecture by using ANTLR4 for
> compiler parsing, Tree-sitter for IDE syntax UX, and a preserved authored AST boundary before
> lowering to Engineering IR.

With M17 closed, Athena stops treating future language growth as a parser-risk problem and starts
treating it as normal governed evolution above a stable architecture.
