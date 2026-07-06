# `:kernel:runtime`

[English](README.md) | 简体中文

`:kernel:runtime` 模块拥有 Athena 的长生命周期运行时边界。它负责工作区生命周期、活动项目上下文、运行时服务解析、命令执行、历史记录、工程图投影、插件托管以及可选 AI 提案评审，同时不会变成第二语义真源。

## 职责

- 通过 `AthenaRuntime` 打开和关闭工作区。
- 将项目激活到共享的 `AthenaExecutionContext`。
- 解析图、命令、插件与渲染协调等运行时服务。
- 让运行时规范状态始终与 `Engineering IR` 保持一致。
- 托管命令历史、撤销、重做、重放、差异检查以及已接受 AI 提案流程。

## 主要类型

- `AthenaRuntime`
- `AthenaWorkspace`
- `AthenaExecutionContext`
- `AthenaServiceRegistry`
- `AthenaCommandRuntimeService`
- `AthenaEngineeringGraphService`

## 依赖

- `:kernel:compiler`
- `:kernel:engineering-model`
- `:kernel:svg-renderer`

## 边界

该模块不直接解析 DSL 源文本、不定义规范 IR 结构、也不拥有领域语义。它拥有这些下层之上的运行时生命周期与编排。

## 验证

```bash
./gradlew :kernel:runtime:test
```

Windows PowerShell:

```powershell
java25; .\gradlew.bat :kernel:runtime:test
```
