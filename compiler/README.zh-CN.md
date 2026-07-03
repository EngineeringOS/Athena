# `:compiler`

[English](README.md) | 简体中文

`:compiler` 模块是 Athena 的编排核心。它暴露公共编译器门面，拥有编译管线报告，发布插件契约与发现逻辑，协调领域语义，解析治理知识包，校验外部边界描述符，并驱动下游渲染。

## 职责

- 通过 `AthenaCompiler` 暴露 `parse`、`lower`、`compile` 入口。
- 保持声明的 pass 顺序稳定：`PARSE -> LOWER -> VALIDATE -> DOWNSTREAM_DERIVATION`。
- 将语法层源文档 lowering 为规范 `Engineering IR`。
- 运行通用语义校验与领域插件校验。
- 发布核心拥有的插件契约、manifest、校验、发现与已批准清单模型。
- 加载并解析治理知识包。
- 加载并校验外部边界描述符。
- 派生 SVG 渲染模型并调用 SVG 渲染器。

## 主要区域

- `AthenaCompiler`：门面与管线编排。
- `CompilerModels.kt`：公共编译结果模型。
- `EngineeringIrLowerer`：从语法到 IR 的 lowering。
- `plugin/*`：插件契约、manifest、校验、发现与领域协调。
- `knowledge/*`：治理知识包模型、加载与解析。
- `boundary/*`：外部边界描述符模型、加载与解析。

## 依赖

- `:language`
- `:semantics-core`
- `:ir`
- `:renderer-svg`

仅测试依赖：

- `:domain-electrical-runtime`

## 边界

该模块不拥有 DSL 语法本身、不拥有规范 IR 结构，也不拥有具体的 Electrical/Runtime 领域规则。它负责编排这些部分，并保持架构规则不变：DSL 是作者输入真源，`Engineering IR` 是规范模型，渲染器只是下游后端。

## 验证

```bash
./gradlew :compiler:test
```

Windows PowerShell：

```powershell
java25; .\gradlew.bat :compiler:test
```
