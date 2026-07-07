# `:kernel:plugins:plugin-host`

[English](README.md) | 简体中文

`:kernel:plugins:plugin-host` 模块拥有 Athena 的宿主插件治理边界。它把插件来源枚举与插件审批准入明确分离，并暴露宿主拥有的生命周期与清单注册表，让 compiler 与 runtime 可以共享一份确定性的已批准插件清单，而不需要把 `ServiceLoader` 或激活策略继承为私有实现细节。

## 职责

- 定义宿主插件来源契约。
- 提供 JVM-first 的 `ServiceLoader` 来源实现。
- 物化确定性的插件候选记录与拒绝记录。
- 校验清单兼容性、类型契约一致性、扩展点合法性与被禁止的所有权声明。
- 构建由 compiler 与 runtime 共享的已批准插件清单。
- 在同一份已批准清单之上暴露宿主拥有的 `loaded`、`initialized` 与 `shutdown` 生命周期状态。
- 保持来源枚举与审批作为两个分离且显式的层次。

## 主要类型

- `AthenaPluginSource`
- `ServiceLoaderAthenaPluginSource`
- `AthenaPluginCandidateSource`
- `AthenaPluginApprovalService`
- `AthenaPluginValidator`
- `AthenaPluginDiscovery`
- `AthenaHostedPluginRegistry`
- `AthenaApprovedPluginInventory`

## 依赖

- `:kernel:plugins:plugin-api`

## 边界

该模块不拥有规范工程语义、编译 pass 编排或 runtime 生命周期编排。它的职责是治理哪些宿主插件可以进入这些层，以及它们为何被批准或拒绝。

## 验证

```bash
./gradlew :kernel:plugins:plugin-host:test
```

Windows PowerShell:

```powershell
java25; .\gradlew.bat :kernel:plugins:plugin-host:test
```
