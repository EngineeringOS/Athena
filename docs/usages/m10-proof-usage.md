# Athena M10 Proof Usage

## Purpose

This guide shows how to exercise the finished M10 proof surfaces:

- deterministic AI reasoning context assembly above governed M9 evidence
- provider-neutral session and proposal lifecycle
- grounded diagnostic explanation, impact summary, and next-check proposals
- additive LSP and Theia workbench delivery with explicit decision state

It assumes the workspace is already checked out locally, `java25` is available on this workstation, and Node plus Yarn are already usable for the Theia workspace.

## Companion Records

- [`_bmad-output/implementation-artifacts/m10/milestone-summary-2026-07-12.md`](../../_bmad-output/implementation-artifacts/m10/milestone-summary-2026-07-12.md)
- [`_bmad-output/implementation-artifacts/m10/m10-retrospective-2026-07-12.md`](../../_bmad-output/implementation-artifacts/m10/m10-retrospective-2026-07-12.md)
- [`examples/m10/README.md`](../../examples/m10/README.md)

## Operating Rule

Run JVM and Node verification sequentially on this Windows workstation.

Use:

```powershell
cmd /c "call java25 && .\gradlew.bat --no-daemon --console=plain <task>"
yarn --cwd ide <task>
```

Do not overlap Gradle and Yarn builds, tests, or desktop runs in parallel shells.

## Published Fixture

- [`examples/m10/reasoning-proof/current`](../../examples/m10/reasoning-proof/current)
- [`examples/m10/reasoning-proof/baseline`](../../examples/m10/reasoning-proof/baseline)

The proof repository intentionally keeps the slice narrow:

- one motor-focused knowledge case
- one baseline/current comparison pair
- one deterministic proof provider
- one explanation flow, one impact-summary flow, and one next-check flow

## Proof Surface 1: Runtime Reasoning Contracts And Proof Provider

### Main Modules

- [`kernel/runtime/`](../../kernel/runtime/README.md)
- [`kernel/compiler/`](../../kernel/compiler/README.md)

### Verification

```powershell
cmd /c "call java25 && .\gradlew.bat --no-daemon --console=plain :kernel:runtime:test --tests com.engineeringood.athena.runtime.AthenaAiDeterministicProofProviderTest --tests com.engineeringood.athena.runtime.AthenaAiReasoningSessionRuntimeServiceTest"
```

What this proves:

- explanation, impact, and next-check text can be generated from governed evidence only
- provider-neutral sessions and proposals stay inspectable
- no canonical engineering mutation occurs during AI reasoning

## Proof Surface 2: LSP Delivery And Audit State

### Main Modules

- [`ide/lsp/`](../../ide/lsp/README.md)
- [`kernel/runtime/`](../../kernel/runtime/README.md)

### Verification

```powershell
cmd /c "call java25 && .\gradlew.bat --no-daemon --console=plain :ide:lsp:test --tests com.engineeringood.athena.ide.lsp.AthenaAiReasoningRequestTest --tests com.engineeringood.athena.ide.lsp.AthenaM10ProofCorpusTest"
```

What this proves:

- reasoning requests, state, and decisions flow only through Athena LSP
- SCM-backed review context stays JVM-owned
- proposal decision state can be revisited later
- the committed proof corpus resolves as `ready` only when both proof repositories keep canonical `athena.lock` content

## Proof Surface 3: Workbench Consumption

### Main Modules

- [`ide/theia-frontend/`](../../ide/theia-frontend/README.md)
- [`ide/theia-product/`](../../ide/theia-product/README.md)

### Verification

```powershell
cmd /c "call java25 && .\gradlew.bat --no-daemon --console=plain :ide:lsp:build"
yarn --cwd ide workspace @engineeringood/athena-theia-frontend build
yarn --cwd ide workspace @engineeringood/athena-theia-product build
```

What this proves:

- semantic panels can request reasoning where evidence already lives
- proposal decisions remain explicit in the current workbench seams
- Theia AI foundation is additive and does not own Athena semantics
- the installed `athena-lsp-host` distribution is rebuilt so live desktop runs match the tested JVM LSP code

## Interactive Use

1. Rebuild the installed JVM LSP host:

```powershell
cmd /c "call java25 && .\gradlew.bat --no-daemon --console=plain :ide:lsp:build"
```

2. Start Athena desktop:

```powershell
yarn --cwd ide start
```

3. Open `examples/m10/reasoning-proof/current`.
4. Open `src/factory-line.athena`.
5. Open `Semantic Inspection` and request `Explain diagnostic`.
6. Open `Semantic SCM`, point the baseline to `examples/m10/reasoning-proof/baseline`, then request `Summarize impact` and `Suggest next checks`.
7. Accept or dismiss returned proposals and verify the state remains visible in the panels.

## Operator Notes

- If `impact-summary` comes back unavailable while the proof corpus should be valid, first check whether either proof repository has a noncanonical `athena.lock`.
- If the desktop reports an LSP request as unsupported after transport changes, rebuild `:ide:lsp:build` before debugging runtime code; the live product uses `ide/lsp/build/install/athena-lsp-host`.
- Keep Gradle, Yarn, and desktop bring-up sequential on this Windows workstation.

## Safety Boundaries

M10 does prove:

- AI can summarize governed engineering evidence
- AI proposal state is explicit and inspectable
- frontend workbench surfaces can stay downstream of LSP and runtime

M10 does not prove:

- autonomous source mutation
- autonomous graph mutation
- live-provider production hardening
- broad repository chat as a semantic authority
