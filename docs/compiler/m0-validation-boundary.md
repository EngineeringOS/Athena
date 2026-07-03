# M0 Validation Boundary

## Purpose

Story `1.4` adds the first semantic validation pass that runs only over canonical `Engineering IR`.

- `language/` still owns syntax and AST.
- `compiler/EngineeringIrLowerer` still owns syntax-to-IR lowering.
- `semantics-core/EngineeringIrValidator` owns generic deterministic semantic diagnostics over the lowered IR.
- active approved domain plugins contribute domain-owned diagnostics inside the compiler-owned `VALIDATE` pass.
- `compiler/AthenaCompiler.compile()` exposes parse, lowering, and semantic validation through one entry path.

This keeps semantic authority inside `Engineering IR` instead of drifting back into parser or renderer logic.

## Validation Contracts

`semantics-core/` owns the shared validation contract surface:

- `SemanticDiagnosticSeverity`
- `SemanticDiagnosticCategory`
- `SemanticRuleId`
- `SemanticDiagnostic`
- `SemanticContinuationDecision`
- `SemanticValidationResult`
- `EngineeringIrValidator`

Diagnostics are intentionally small but compiler-grade:

- rule identity is stable and typed
- category groups diagnostics for inspection and later policy work
- subject identity points at the affected canonical semantic object when one exists
- provenance points at the authored source span already carried by IR
- messages remain human-readable but are not the only machine surface

## Current Generic M0 Rules

The core validator intentionally stays narrow and deterministic.

Reference rules:

- `reference.port-owner.unresolved`
- `reference.port-owner.ambiguous`
- `reference.connection-endpoint.unresolved`
- `reference.connection-endpoint.ambiguous`

Uniqueness rules:

- `uniqueness.component.duplicate-authored-key`
- `uniqueness.port.duplicate-authored-key`
- `uniqueness.connection.duplicate-authored-key`

These generic rules remain core-owned because they do not belong to one particular domain vocabulary.

## Domain-Owned M0 Rules

After Story `2.3`, the first Electrical/Runtime plugin owns the current M0 domain rule set:

- `property.component.type.missing`
- `property.component.type.invalid`
- `property.port.direction.missing`
- `property.port.direction.invalid`
- `connection.direction.illegal`
- `connection.signal.incompatible`

Current Electrical/Runtime vocabulary remains intentionally small:

- valid device types: `PLC`, `Motor`
- valid port directions: `in`, `out`
- valid connection direction: `out -> in`
- signal compatibility: both resolved endpoints declare the same symbolic `signal`
- quoted text values for symbolic semantic properties such as `type` and `direction` are invalid, not missing

## Diagnostic Ordering

Diagnostics are emitted in deterministic pass order:

1. duplicate component keys
2. port-owner reference defects
3. duplicate port keys
4. connection-endpoint reference defects
5. domain-owned plugin diagnostics in approved-plugin order

That order is now part of the M0 test contract so repeated runs over unchanged input stay byte-for-byte stable in outcome ordering.

## Invalid-State Continuation Policy

Story `1.4` defines the first explicit downstream gate:

- if semantic validation emits any `ERROR`, continuation is `STOP_DOWNSTREAM`
- if no error-level diagnostics are emitted, continuation is `CONTINUE`

This is surfaced through `SemanticValidationResult.continuationDecision` and consumed by `CompilerCompilationSuccess.semanticResult`.

The policy is intentionally simple for M0:

- semantic invalidity does not erase parse or lowering success
- callers can still inspect the canonical IR and full diagnostics
- later downstream passes must check continuation policy instead of inferring semantic success ad hoc

Story `1.5` can now build ordered pass execution around this explicit gate instead of inventing a second validity model.
