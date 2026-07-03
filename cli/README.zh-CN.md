# `:cli`

[English](README.md) | 简体中文

` :cli` 模块是 Athena M0 的命令行入口。它为编译器提供一个最小化的 shell 包装层，暴露当前的启动命令面，但它本身不拥有语言、语义或渲染规则。

## 职责

- 通过 `com.engineeringood.athena.cli.MainKt` 启动进程。
- 暴露当前的 `--help` 与 `parse <source-file>` 命令。
- 调用 `AthenaCompiler`，并将结果格式化为终端可读的纯文本输出。
- 证明编译器、Electrical/Runtime 示例领域插件、SVG 渲染器能够被装配成一个可运行二进制。

## 主要类型

- `BootstrapCli`：参数分发与输出格式化的小型门面。
- `MainKt`：进程入口。
- `CliModuleMarker`：供测试与帮助输出使用的轻量模块标记。

## 依赖

- `:compiler`
- `:language`
- `:domain-electrical-runtime`
- `:renderer-svg`

## 边界

该模块不定义 DSL、规范 IR、语义校验规则、插件契约或 SVG 生成逻辑。它只负责调用这些能力，并输出稳定的终端摘要。

## 验证

在仓库根目录执行：

```bash
./gradlew :cli:test
```

在本仓库的 Windows PowerShell 环境中，可使用：

```powershell
java25; .\gradlew.bat :cli:test
```
