# `:kernel:language`

English | [Chinese (Simplified)](README.zh-CN.md)

The `:kernel:language` module owns Athena's syntax layer for the current M0 Electrical/Runtime DSL. It parses authored source text into a syntax-only AST with source spans and syntax diagnostics, but it does not assign semantic truth.

## M17 Frozen Public Syntax Contract

`AthenaLanguageModel.kt` is Athena's **frozen public authored syntax contract** for M17.

That contract includes:

- `SourceFileAst` and declaration nodes (`SystemDeclaration`, `Declaration`, `DeviceDeclaration`, `PortDeclaration`, `ConnectionDeclaration`, `QualifiedName`, `PropertyAssignment`, `ScalarValue`)
- `ParseResult` / `ParseSuccess` / `ParseFailure`
- `SourcePosition`, `SourceSpan`, and `SyntaxDiagnostic`

Any future parser implementation must keep producing these same Athena-owned contract types.
Downstream modules must depend on these contracts, never on parser-generator internals (ANTLR
trees, Tree-sitter CST nodes, or tokenizer/token types).

`AthenaLanguageParser.parse` is the supported facade entry point: callers consume `ParseResult` only and must not depend on how parsing is implemented internally.

## Public Vs Internal Packages

- `com.engineeringood.athena.language` is the **public** syntax boundary (authored AST, parse results, spans, diagnostics, and the `AthenaLanguageParser` facade).
- `com.engineeringood.athena.language.antlr` is **internal implementation detail** (generated
  grammar output plus parse-tree-to-AST adaptation behind the `AthenaLanguageParser` facade).
- Downstream modules (`:kernel:compiler`, `:kernel:runtime`, `:ide:*`, and others) must never
  import the internal `antlr` sub-package directly.

## Future Syntax Landing Zone

M17 prepares the authored AST for growth; it does **not** finalize future syntax such as `import`.

- New top-level authored constructs land as new `Declaration` sealed variants (field-level literal kinds land on `ScalarValue`).
- Parser adaptation for a new construct stays isolated inside
  `com.engineeringood.athena.language.antlr` (Story `1.2` plus Epic 2 closeout).
- Lowering and other consumers must handle new variants through an **exhaustive** `when` so unhandled kinds fail at compile time.
- See `docs/future-syntax-landing-zone.md` for the concrete hypothetical `ImportDeclaration` sketch. Epic 5 Story `5.3` publishes the milestone-level landing-zone closeout note this module note supports.

## Responsibilities

- Define the syntax model in `AthenaLanguageModel.kt`.
- Parse source text in `AthenaLanguageParser.kt` (implementation internals live under
  `language.antlr`).
- Preserve authored provenance through `SourcePosition`, `SourceSpan`, and `SyntaxDiagnostic`.
- Keep the AST syntax-owned and free of semantic validation or canonical engineering meaning.

## Main Types

- `SourceFileAst`
- `SystemDeclaration`
- `DeviceDeclaration`
- `PortDeclaration`
- `ConnectionDeclaration`
- `QualifiedName`
- `ParseSuccess` / `ParseFailure`

## Dependencies

This module has no project-module dependencies.

## Boundaries

This module does not lower to `Engineering IR`, does not resolve references, does not validate domain rules, and does not render diagrams. Those responsibilities live in `:kernel:compiler`, `:kernel:validation`, domain plugins, and `:kernel:svg-renderer`.

## Verification

```bash
./gradlew :kernel:language:test
```

Windows PowerShell:

```powershell
java25; .\gradlew.bat :kernel:language:test
```
