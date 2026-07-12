# `examples/m10`

[English](README.md) | 简体中文

这个目录保存 M10 AI-assisted engineering reasoning 的 governed proof corpus。

## 内容

- `reasoning-proof/baseline/`: 用于 SCM-backed impact 与 review proof flow 的 baseline repository
- `reasoning-proof/current/`: 用于 diagnostic explanation、impact summary 与 next-check proof flow 的活动 repository

## 目的

- 证明基于 governed M9 output 的 deterministic reasoning context assembly
- 证明 built-in deterministic proof provider 上的 provider-neutral reasoning session
- 证明 SCM-backed impact summary 与 review-ready next check，而不是 autonomous mutation

## 验证

```powershell
cmd /c "call java25 && .\gradlew.bat --no-daemon --console=plain :ide:lsp:test --tests com.engineeringood.athena.ide.lsp.AthenaM10ProofCorpusTest"
```

## Safety

- 这个 proof corpus 仅提供 advisory output。
- 此目录中的 proposal 没有 mutation authority。
- 规范工程真相仍然来自 Athena runtime、compiler 与 knowledge output。
