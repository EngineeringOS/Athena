# Athena M9 Proof Usage

## Purpose

This guide shows how to exercise the finished M9 proof surfaces:

- derived engineering context above canonical `Engineering IR`
- capability facts promoted through one fixed electrical knowledge pack
- deterministic engineering sufficiency diagnostics
- deterministic engineering-impact consequences in review and SCM paths

It assumes the workspace is already checked out locally and `java25` is available on this workstation.

## Companion Records

- [`_bmad-output/implementation-artifacts/m9/2-3-extend-semantic-review-with-engineering-impact-and-affected-subjects.md`](../../_bmad-output/implementation-artifacts/m9/2-3-extend-semantic-review-with-engineering-impact-and-affected-subjects.md)
- [`_bmad-output/implementation-artifacts/m9/2-4-publish-the-m9-proof-corpus-and-verification-path.md`](../../_bmad-output/implementation-artifacts/m9/2-4-publish-the-m9-proof-corpus-and-verification-path.md)
- [`examples/m9/README.md`](../../examples/m9/README.md)

## Operating Rule

Run Gradle verification sequentially on this Windows workstation.

Use:

```powershell
cmd /c "call java25 && .\gradlew.bat --no-daemon --console=plain <task>"
```

Do not overlap Gradle builds, tests, or desktop runs in parallel shells.

## What M9 Proves

M9 proves that Athena can understand a first narrow slice of engineering meaning above canonical structure.

The central M9 claim is:

- canonical engineering state can produce derived engineering context deterministically
- governed knowledge-pack semantics can promote capability facts from that derived context
- fixed rule slices can emit engineering sufficiency diagnostics without becoming a general rule-authoring platform
- governed before/after change can produce typed engineering-impact consequences
- existing semantic review and SCM paths can distinguish direct edits from downstream affected subjects

## Published Fixtures

- [`examples/m9/motor-derived-context.athena`](../../examples/m9/motor-derived-context.athena)
- [`examples/m9/motor-impact-before.athena`](../../examples/m9/motor-impact-before.athena)
- [`examples/m9/motor-impact-after.athena`](../../examples/m9/motor-impact-after.athena)

The `motor-derived-context.athena` fixture is the smallest one-subject seed for the first electrical knowledge slice.

The `motor-impact-before.athena` and `motor-impact-after.athena` fixtures prove the governed before/after change path:

- before: `power "7.5kw"`
- after: `power "9kw"`
- held constant: `breakerRatedCurrent "10A"`, `cableAllowedCurrent "12A"`, `relayRatedCurrent "13A"`

That change is intentionally narrow so M9 can prove engineering consequence without widening into standards packs, vendor catalogs, or editor-depth scope.

## Proof Surface 1: Derived Context And Capability Facts

### Main Modules

- [`kernel/engineering-model/`](../../kernel/engineering-model/README.md)
- [`kernel/compiler/`](../../kernel/compiler/README.md)

### Verification

```powershell
cmd /c "call java25 && .\gradlew.bat --no-daemon --console=plain :kernel:engineering-model:test :kernel:semantic-scm:test"
```

What this proves:

- M9 publishes stable kernel-owned contracts for derived context, capability facts, constraint evaluations, impact consequences, and neutral engineering-knowledge snapshots
- the first electrical knowledge slice remains deterministic and inspectable in unit tests

## Proof Surface 2: Runtime Review And Impact

### Main Modules

- [`kernel/runtime/`](../../kernel/runtime/README.md)
- [`kernel/semantic-scm/`](../../kernel/semantic-scm/README.md)

### Verification

```powershell
cmd /c "call java25 && .\gradlew.bat --no-daemon --console=plain :kernel:runtime:test --tests com.engineeringood.athena.runtime.AthenaSemanticReviewServiceTest"
```

What this proves:

- repository-baseline review now carries engineering-impact consequences
- direct engineering edits remain distinct from downstream affected subjects
- knowledge diagnostics and engineering impact stay on the JVM semantic review path

## Proof Surface 3: LSP Delivery Through Existing Semantic Paths

### Main Modules

- [`ide/lsp/`](../../ide/lsp/README.md)
- [`kernel/runtime/`](../../kernel/runtime/README.md)

### Verification

```powershell
cmd /c "call java25 && .\gradlew.bat --no-daemon --console=plain :ide:lsp:test --tests com.engineeringood.athena.ide.lsp.AthenaSemanticScmStateRequestTest --tests com.engineeringood.athena.ide.lsp.AthenaSourceMutationRequestTest"
```

What this proves:

- engineering sufficiency still flows through existing Problems-facing diagnostics
- semantic SCM payloads now carry typed engineering-impact consequence lists
- accepted source-mutation review now projects the same impact contract and explicit `engineering-impact` entries

## Full Verified Path

The following commands were confirmed during Story 2.3 and Story 2.4 work:

```powershell
cmd /c "call java25 && .\gradlew.bat --no-daemon --console=plain :kernel:runtime:test --tests com.engineeringood.athena.runtime.AthenaSemanticReviewServiceTest"
cmd /c "call java25 && .\gradlew.bat --no-daemon --console=plain :ide:lsp:test --tests com.engineeringood.athena.ide.lsp.AthenaSemanticScmStateRequestTest --tests com.engineeringood.athena.ide.lsp.AthenaSourceMutationRequestTest"
cmd /c "call java25 && .\gradlew.bat --no-daemon --console=plain :kernel:engineering-model:test :kernel:semantic-scm:test"
```

A full workspace `test` run was attempted, but the local tool invocation timed out before completion, so only the targeted suites above are confirmed in this usage guide.

## Current Boundaries

M9 does prove:

- one narrow electrical knowledge slice above canonical semantic structure
- one fixed governed knowledge pack
- one deterministic engineering sufficiency family
- one deterministic engineering-impact path for governed change
- one reuse of existing Problems, inspection, review, commit, and SCM seams

M9 does not yet prove:

- broad standards-pack coverage
- vendor-catalog or part-selection logic
- knowledge-pack authoring for end users
- AI remediation or auto-design
- renderer or workbench depth beyond consuming the published knowledge outputs
