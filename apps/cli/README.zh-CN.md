# `:apps:cli`

[English](README.md) | 简体中文

`:apps:cli` 模块是 Athena JVM 证明的命令行入口。它在 runtime 与 compiler 之上暴露命令行能力，但本身不拥有语言、语义或渲染规则。

## 职责

- 通过 `com.engineeringood.athena.cli.MainKt` 启动进程。
- 暴露当前 parse、command、history、plugin 与可选 AI proposal 的命令面。
- 调用 runtime 拥有的服务与 compiler 拥有的能力，并输出终端可读的纯文本结果。
- 证明 runtime、compiler、Electrical 领域扩展与 SVG 渲染器可以装配成一个可运行二进制。

## 主要类型

- `BootstrapCli`：参数分发与输出格式化的轻量门面。
- `MainKt`：进程入口。
- `CliModuleMarker`：用于测试与 bootstrap help 输出的轻量模块标记。

## 依赖

- `:kernel:runtime`
- `:kernel:compiler`
- `:kernel:engineering-model`
- `:kernel:language`
- `:extensions:domain-electrical`
- `:kernel:svg-renderer`

## 边界

该模块不定义 DSL、规范 IR、语义校验规则、插件契约或 SVG 生成逻辑。它只调用这些能力，并输出稳定的终端摘要。

## 验证

在仓库根目录执行：

```bash
./gradlew :apps:cli:test
```

Windows PowerShell：

```powershell
java25; .\gradlew.bat :apps:cli:test
```
