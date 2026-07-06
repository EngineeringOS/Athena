# `:kernel:validation`

[English](README.md) | 简体中文

`:kernel:validation` 模块拥有针对规范 `Engineering IR` 的通用语义校验能力。它为核心级而非领域级规则提供确定性的诊断与继续执行策略。

## 职责

- 在 `SemanticValidationModel.kt` 中定义语义诊断与继续执行模型。
- 通过 `EngineeringIrValidator` 校验规范 IR。
- 执行组件/端口/连接唯一性与引用可解析性等通用规则。
- 在不修改规范模型的前提下返回带来源信息的诊断。

## 主要类型

- `SemanticDiagnostic`
- `SemanticRuleId`
- `SemanticDiagnosticSeverity`
- `SemanticDiagnosticCategory`
- `SemanticContinuationDecision`
- `SemanticValidationResult`
- `EngineeringIrValidator`

## 依赖

- `:kernel:engineering-model`

## 边界

该模块不解析源文本、不负责 AST lowering、不定义 Electrical/Runtime 兼容性等领域规则，也不产生渲染输出。领域语义属于插件，编排属于 `:kernel:compiler`。

## 验证

```bash
./gradlew :kernel:validation:test
```

Windows PowerShell：

```powershell
java25; .\gradlew.bat :kernel:validation:test
```
