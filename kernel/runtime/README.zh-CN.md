# `:kernel:runtime`

[English](README.md) | 简体中文

`:kernel:runtime` 模块拥有 Athena 的长生命周期运行时边界。它负责工作区生命周期、活动项目上下文、运行时服务解析、投影会话、命令执行、历史、工程图投影、插件托管，以及可选 AI 提案审阅，同时不会变成第二语义真源。

## 职责

- 通过 `AthenaRuntime` 打开和关闭工作区。
- 将项目激活到共享的 `AthenaExecutionContext`。
- 解析图、命令、插件与渲染协调等运行时服务。
- 托管运行时拥有的投影会话，包括受支持视图发现与活动视图切换。
- 让运行时规范状态始终与 `Engineering IR` 保持一致。
- 托管命令历史、撤销、重做、重放、差异检查以及已接受 AI 提案流程。
- 在受支持的语义变更后，对外发布运行时可见的增量刷新元数据。

## 主要类型

- `AthenaRuntime`
- `AthenaWorkspace`
- `AthenaExecutionContext`
- `AthenaServiceRegistry`
- `AthenaRuntimeProjectionSession`
- `AthenaCommandRuntimeService`
- `AthenaEngineeringGraphService`

## 依赖

- `:kernel:compiler`
- `:kernel:engineering-model`
- `:kernel:svg-renderer`

## 边界

该模块不直接解析 DSL 源文本，不定义规范 IR 结构，也不拥有领域语义。它拥有这些下层之上的运行时生命周期与编排。投影会话只是建立在编译器派生投影工件之上的运行时状态；切换视图不会修改规范工程语义。

## 增量刷新边界

Story `2.3` 把运行时合同保持在一个非常克制的范围内：

- 第一个 scoped refresh 证明只限于现有的 `connect ports` 命令路径。
- `AthenaExecutionContext.incrementalUpdateReport()` 暴露语义影响范围，以及 layout、geometry、rendering 刷新模式。
- 运行时拥有刷新协调与活动投影替换，但编译器仍然是唯一的派生规则拥有者。
- Desktop 和其他消费者必须通过运行时拥有的投影状态读取刷新结果，而不是维护私有视图缓存。
- 如果编译器的 scoped reuse 不安全，运行时会显示 fallback 模式，而不会把这个事实隐藏起来。

## 评审合同

Story `2.4` 让语义评审继续保持主位，同时把投影刷新变成可检查的下游证据：

- `AthenaSemanticDiffInspection` 仍然锚定在规范语义 id 和命令关联的历史结果上。
- 投影刷新证据以“下游 consequence 元数据”附着在语义 diff 上，而不是变成第二套 history 或 diff 系统。
- 运行时检查可以解释受影响的视图和下游层，但不会把“几何变化”提升成高于语义变化的主解释。

## 验证

```bash
./gradlew :kernel:runtime:test
```

Windows PowerShell:

```powershell
java25; .\gradlew.bat :kernel:runtime:test
```
