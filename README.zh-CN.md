# Athena

[English](README.md) | 简体中文

Athena 是 EngineeringOS 语义编译器论点的 JVM-first 实现工作区。

## 名称说明

`engineeringood` 同时表达两层含义：

- `engineering good`：对工程更好
- `OOD`：object-oriented development，面向对象开发

放在 EngineeringOS 的语境里，这个名字表达的是同一个语义目标：工程含义应该被显式化、结构化、可组合化，并沉淀为可持续演进的模型，而不是被困在图纸、文件格式或工具私有表示里。

## 当前范围

当前仓库仍然是架构优先的证明工程，核心论点是：

- DSL 是作者输入的真源
- `Engineering IR` 是规范语义模型
- 编译器拥有 pass 编排权
- 扩展可以增加领域行为，但不能拥有语义主权
- 渲染只是下游后端，不是真源

当前已经实现的范围包括：

- 最小化的 Electrical/Runtime DSL
- 解析为仅语法含义的 AST
- lowering 到规范 `Engineering IR`
- 核心语义校验与扩展提供的领域语义
- 确定性 SVG 渲染
- JVM 插件发现与托管
- 治理知识包加载与解析
- 外部边界描述符校验
- runtime 托管的 graph、command、history、diff 与可选 AI proposal 流程
- 桌面 Compose viewer 证明，以及 [`examples/`](examples/README.md) 中发布的样例

这还不是最终 UX 阶段。

## 模块拓扑

Athena 现在按架构职责命名并存放 Gradle 模块。物理工作区布局已经与分组后的模块图一致，统一落在 `kernel/`、`extensions/`、`ui/` 与 `apps/` 下。

| 分组 | Gradle 模块 | 目录 | 作用 |
| --- | --- | --- | --- |
| `kernel` | `:kernel:language` | [`kernel/language/`](kernel/language/README.zh-CN.md) | 作者 DSL 的语法层与解析器 |
| `kernel` | `:kernel:engineering-model` | [`kernel/engineering-model/`](kernel/engineering-model/README.zh-CN.md) | lowering 之后的规范工程模型 |
| `kernel` | `:kernel:validation` | [`kernel/validation/`](kernel/validation/README.zh-CN.md) | 针对规范模型的通用语义校验 |
| `kernel` | `:kernel:compiler` | [`kernel/compiler/`](kernel/compiler/README.zh-CN.md) | 编译器门面、lowering、编排、插件契约、知识与边界加载 |
| `kernel` | `:kernel:runtime` | [`kernel/runtime/`](kernel/runtime/README.zh-CN.md) | workspace 生命周期、执行上下文、graph、command、history、plugin 与 AI proposal 托管 |
| `kernel` | `:kernel:svg-renderer` | [`kernel/svg-renderer/`](kernel/svg-renderer/README.zh-CN.md) | 从语义状态导出的确定性 SVG 投影 |
| `extensions` | `:extensions:domain-electrical` | [`extensions/domain-electrical/`](extensions/domain-electrical/README.zh-CN.md) | 第一个真实的 Electrical 领域扩展 |
| `ui` | `:ui:compose-workbench` | [`ui/compose-workbench/`](ui/compose-workbench/README.zh-CN.md) | 共享 Compose 工作台与 viewer 交互基础设施 |
| `apps` | `:apps:cli` | [`apps/cli/`](apps/cli/README.zh-CN.md) | 面向终端的入口 |
| `apps` | `:apps:desktop-viewer` | [`apps/desktop-viewer/`](apps/desktop-viewer/README.zh-CN.md) | 桌面 Compose 应用入口 |

分组总览：

- [`kernel/`](kernel/README.zh-CN.md)
- [`extensions/`](extensions/README.zh-CN.md)
- [`ui/`](ui/README.zh-CN.md)
- [`apps/`](apps/README.zh-CN.md)

## 文档范围

当前命名体系在以下位置视为权威：

- 根 README 与各模块 README
- 各分组 README
- [`DEV.md`](DEV.md)
- [`docs/compiler/`](docs/compiler) 下仍在生效的边界说明文档

部分编译器文档会明确标注为历史参考，为了保留上下文，可能继续保留当时的 story 阶段命名。

`_bmad-output/` 下的 BMAD 产物作为历史记录保留，可能仍然使用旧的 story 标签或较早的模块命名。

## 构建要求

- 必须使用 Java 25
- Gradle wrapper：`9.6.1`
- Kotlin：`2.4.0`

在 Windows 上执行 Gradle 时，请按顺序串行运行 build、test、run 等任务，不要并发启动多个终端同时跑。

## 快速开始

类 Unix shell：

```bash
./gradlew test
./gradlew :apps:cli:run --args="parse examples/m0/demo-cabinet.athena"
```

本仓库下的 Windows PowerShell：

```powershell
java25; .\gradlew.bat test
java25; .\gradlew.bat :apps:cli:run --args "parse examples/m0/demo-cabinet.athena"
```

`java25` 是本机的辅助命令，用来先把 `JAVA_HOME` 切换到 Java 25，再执行 Gradle。

## 推荐阅读

如果你想先看实现视角：

1. [`kernel/compiler/README.md`](kernel/compiler/README.md)
2. [`kernel/runtime/README.md`](kernel/runtime/README.md)
3. [`docs/compiler/m0-pass-pipeline.md`](docs/compiler/m0-pass-pipeline.md)
4. [`docs/compiler/m1-runtime-host-boundary.md`](docs/compiler/m1-runtime-host-boundary.md)
5. [`examples/README.md`](examples/README.md)

如果你想先看平台论点：

1. [`manifesto/README.md`](manifesto/README.md)
2. `manifesto/docs/` 下的架构与技术章节

## 架构说明

- AST 只属于语法层，并与语义真值保持分离。
- `Engineering IR` 是规范工程模型。
- 通用校验位于 `:kernel:validation`；领域校验位于扩展。
- 插件是真实可发现的，但不是主权边界。
- 治理知识包与外部边界描述符独立于作者工程输入。

## 许可证

见 [`LICENSE`](LICENSE)。
