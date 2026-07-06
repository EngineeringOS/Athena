# `:ui:compose-workbench`

[English](README.md) | 简体中文

`:ui:compose-workbench` 模块包含 Athena 共享的 Compose 工作台基础设施。它负责 shell 状态、viewer stage 组合、主题接线以及选择、平移、缩放等交互基础设施，同时把工程语义留在 runtime 与 model 层。

## 职责

- 发布可复用的 Compose shell 与 viewer 脚手架。
- 保持工作台状态领域中立且可丢弃。
- 托管语义查看所需的选择、视口与交互状态。
- 为桌面优先的应用界面提供共享 UI 基础设施。

## 主要类型

- `AthenaComposeShell`
- `AthenaComposeShellState`
- `AthenaSemanticViewerStage`
- `AthenaSemanticViewerInteractionState`
- `ComposeRuntimeModuleMarker`

## 边界

该模块不拥有规范工程语义、不执行命令、不负责编译器编排、也不执行领域规则。它只负责 UI 基础设施。

## 验证

```bash
./gradlew :ui:compose-workbench:test
```

Windows PowerShell:

```powershell
java25; .\gradlew.bat :ui:compose-workbench:test
```
