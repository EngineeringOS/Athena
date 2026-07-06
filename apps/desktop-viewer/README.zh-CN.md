# `:apps:desktop-viewer`

[English](README.md) | 简体中文

`:apps:desktop-viewer` 模块是 Athena 的桌面 Compose 应用入口。它把共享 Compose 工作台、runtime host 与 electrical 领域插件装配成一个桌面界面，用于查看与命令驱动交互证明。

## 职责

- 启动桌面 Compose 入口点。
- 引导第一个 runtime 管理的桌面 viewer 会话。
- 将共享工作台 UI 绑定到 runtime 拥有的项目状态。
- 提供 Java 25 启动行为的桌面 smoke 验证。

## 依赖

- `:ui:compose-workbench`
- `:kernel:runtime`
- `:extensions:domain-electrical`

## 边界

该模块是应用外壳。它不应拥有工程语义、规范项目状态，或本该下沉到更低分组模块的可复用工作台原语。

## 验证

```bash
./gradlew :apps:desktop-viewer:test
./gradlew :apps:desktop-viewer:bootstrapSmoke
```

Windows PowerShell:

```powershell
java25; .\gradlew.bat :apps:desktop-viewer:test
java25; .\gradlew.bat :apps:desktop-viewer:bootstrapSmoke
```
