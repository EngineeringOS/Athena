# `:language`

English | [Chinese (Simplified)](README.zh-CN.md)

The `:language` module owns Athena's syntax layer for the current M0 Electrical/Runtime DSL. It parses authored source text into a syntax-only AST with source spans and syntax diagnostics, but it does not assign semantic truth.

## Responsibilities

- Define the syntax model in `AthenaLanguageModel.kt`.
- Parse source text in `AthenaLanguageParser.kt`.
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

This module does not lower to `Engineering IR`, does not resolve references, does not validate domain rules, and does not render diagrams. Those responsibilities live in `:compiler`, `:semantics-core`, domain plugins, and `:renderer-svg`.

## Verification

```bash
./gradlew :language:test
```

Windows PowerShell:

```powershell
java25; .\gradlew.bat :language:test
```
