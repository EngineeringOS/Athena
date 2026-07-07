# `:apps:desktop-viewer`

[English](README.md) | 简体中文

`:apps:desktop-viewer` 模块是 Athena 的桌面 Compose 应用入口。它把共享工作台 UI、runtime host 与 electrical 域扩展组装成一个桌面壳层，用于验证运行时驱动的查看、切换视图与命令交互流程。

## 职责

- 启动桌面 Compose 入口。
- 从 `examples/m2/operator-proof.athena` 引导默认的 `operator-proof` 运行时工作台会话。
- 将共享工作台 UI 绑定到 runtime 持有的项目状态与投影视图会话状态。
- 通过 `Athena Runtime` 请求活动视图切换，而不是在桌面侧保存投影真相。
- 在操作员于 `cabinet` 与 `wiring` 之间切换时，保持规范语义选择可见。
- 提供 Java 25 启动验证与脚本化的 M2 operator proof 验证。

## 依赖

- `:ui:compose-workbench`
- `:kernel:runtime`
- `:extensions:domain-electrical`

## 边界

该模块只是应用壳层。它不应拥有工程语义、规范项目状态、投影推导规则，或属于更低层分组模块的可复用工作台原语。这里可以暴露选择与视图切换，但投影真相必须保持由 runtime 持有。

## 默认证明

默认桌面引导会打开 `examples/m2/operator-proof.athena`。该种子文件初始不带作者手写连接，因此桌面证明可以展示 runtime 命令创建 `connection:PLC1.out->M1.in`，同时保持规范选择在 `cabinet` 与 `wiring` 之间连续可见。

## 验证

```bash
./gradlew :apps:desktop-viewer:test
./gradlew :apps:desktop-viewer:bootstrapSmoke
./gradlew :apps:desktop-viewer:operatorProofSmoke
```

Windows PowerShell:

```powershell
java25; .\gradlew.bat :apps:desktop-viewer:test
java25; .\gradlew.bat :apps:desktop-viewer:bootstrapSmoke
java25; .\gradlew.bat :apps:desktop-viewer:operatorProofSmoke
```
