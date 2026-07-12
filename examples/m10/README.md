# `examples/m10`

English | [Chinese (Simplified)](README.zh-CN.md)

This folder holds the governed M10 proof corpus for AI-assisted engineering reasoning.

## Contents

- `reasoning-proof/baseline/`: baseline repository used for SCM-backed impact and review proof flows
- `reasoning-proof/current/`: active repository used for diagnostic explanation, impact summary, and next-check proof flows

## Purpose

- prove deterministic reasoning context assembly over governed M9 outputs
- prove provider-neutral reasoning sessions with the built-in deterministic proof provider
- prove SCM-backed impact summaries and review-ready next checks without autonomous mutation

## Verification

```powershell
cmd /c "call java25 && .\gradlew.bat --no-daemon --console=plain :ide:lsp:test --tests com.engineeringood.athena.ide.lsp.AthenaM10ProofCorpusTest"
```

## Safety

- The proof corpus is advisory only.
- No proposal in this folder has mutation authority.
- Canonical engineering truth still comes from Athena runtime, compiler, and knowledge outputs.
