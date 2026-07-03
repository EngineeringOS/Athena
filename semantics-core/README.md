# `:semantics-core`

English | [Chinese (Simplified)](README.zh-CN.md)

The `:semantics-core` module owns generic semantic validation over canonical `Engineering IR`. It provides deterministic diagnostics and continuation policy for rules that are core-wide rather than domain-specific.

## Responsibilities

- Define semantic diagnostic and continuation models in `SemanticValidationModel.kt`.
- Validate canonical IR with `EngineeringIrValidator`.
- Enforce generic rules such as uniqueness and reference resolvability.
- Return provenance-rich diagnostics without mutating the canonical model.

## Main Types

- `SemanticDiagnostic`
- `SemanticRuleId`
- `SemanticDiagnosticSeverity`
- `SemanticDiagnosticCategory`
- `SemanticContinuationDecision`
- `SemanticValidationResult`
- `EngineeringIrValidator`

## Dependencies

- `:ir`

## Boundaries

This module does not parse source text, lower AST nodes, define domain-specific rules such as Electrical/Runtime compatibility, or produce render output. Domain semantics belong in plugins, and orchestration belongs in `:compiler`.

## Verification

```bash
./gradlew :semantics-core:test
```

Windows PowerShell:

```powershell
java25; .\gradlew.bat :semantics-core:test
```
