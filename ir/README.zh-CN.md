# `:ir`

[English](README.md) | 简体中文

`:ir` 模块定义 Athena 的规范工程模型。`Engineering IR` 是 lowering 之后的语义单一真源，独立于解析器结构、插件实现细节和渲染器专用布局。

## 职责

- 在 `EngineeringIrModel.kt` 中发布规范文档模型。
- 通过 `StableSemanticIdentity` 定义稳定语义标识。
- 通过 `SourceProvenance` 保留作者来源信息。
- 为后续校验与渲染提供带类型的工程属性与引用。

## 主要类型

- `EngineeringIrDocument`
- `EngineeringSystem`
- `EngineeringComponent`
- `EngineeringPort`
- `EngineeringConnection`
- `EngineeringReference`
- `EngineeringProperty`
- `EngineeringPropertyValue`

## 依赖

该模块没有项目内模块依赖。

## 边界

该模块不解析 DSL 文本、不执行语义规则校验、不做插件发现、不加载治理知识包，也不生成 SVG。它是其他模块操作的稳定模型。

## 验证

```bash
./gradlew :ir:test
```

Windows PowerShell：

```powershell
java25; .\gradlew.bat :ir:test
```
