# `:kernel:connection-model`

[English](README.md) | [简体中文](README.zh-CN.md)

`:kernel:connection-model` 模块定义了 Athena 在 M14 的语义端口知识契约。

这个模块把所有权层级保持清晰：

`Engineering IR -> semantic port knowledge -> downstream M9 / projection / presentation consumers`

## 职责

- 通过 `SemanticPortTypeId` 发布稳定的语义端口标识。
- 发布稳定的角色、方向、信号族和协议标识。
- 通过 `SemanticPortDefinition` 发布供应商无关的语义端口定义。
- 通过 `ResolvedSemanticPortDefinition` 发布只读的解析后语义端口知识。
- 保持规范化的端口真实含义仍然留在 `Engineering IR`。

## 主要类型

- `SemanticPortTypeId`
- `SemanticPortRoleId`
- `SemanticSignalFamilyId`
- `SemanticProtocolId`
- `SemanticPortDirection`
- `SemanticPortDefinition`
- `ResolvedSemanticPortDefinition`

## 依赖

该模块依赖 `:kernel:engineering-model`，并通过 `StableSemanticIdentity` 复用规范化的语义标识。

## 边界

该模块不定义兼容性或充分性判断，不定义布线几何、图形坐标、形状 id、控件状态、物理特征，也不承担 knowledge-pack 加载、编译器编排、运行时传输、投影逻辑、呈现逻辑或渲染器行为。更丰富的判断留在 M9 的下游链路里：

`DerivedEngineeringContext -> EngineeringCapabilityFacts -> EngineeringConstraintEvaluations`

它只是一层窄的语义端口知识契约。

## 验证

```bash
./gradlew :kernel:connection-model:test
```

Windows PowerShell:

```powershell
java25; .\gradlew.bat :kernel:connection-model:test
```