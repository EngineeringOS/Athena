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
- 扩展通过受治理的边界进入系统
- 扩展可以增加领域行为，但不能拥有语义主权
- 渲染只是下游后端，不是真源

当前已经实现的范围包括：

- 最小化的 Electrical/Runtime DSL
- 语法层 AST
- lowering 到规范 `Engineering IR`
- 核心语义校验与扩展提供的领域语义
- 确定性 SVG 渲染
- 稳定的宿主插件 API 与宿主插件审批边界
- 受治理的知识包加载与解析
- 外部边界描述符校验
- runtime 托管的 graph、command、history、diff 与可选 AI proposal 流程
- 真实的 `domain-electrical` 与 `domain-dummy` 证明域
- 基于仓库会话的 Athena Theia IDE 证明
- 编译器派生的 `:kernel:projection-model` 边界，用来承载渲染器中立的图形投影文档
- 规范 `athena.yaml`、规范 `athena.lock`、运行时 `RepositoryGraphSession` 与包感知 IDE 反馈
- 位于 `:kernel:repository-model` 之上的 VCS 中立 semantic SCM 边界
- 通过 JVM path 执行的 deterministic semantic diff、compiler-derived consequence 与 semantic review summary
- 受治理的 hosted plugin semantic review enrichment，可追加 domain label、hint 与 summary，但不能改写 core semantic SCM facts
- 通过同一 JVM path 执行的 deterministic semantic commit intent，保持 commit preparation semantic-first 且 adapter-ready
- 以稳定 package identity 与 version meaning 为锚点的 package-aware semantic history 与 release relevance contract
- 通过 runtime / LSP / Theia Semantic SCM panel 投影 review、commit preparation、package evolution 与 release relevance，同时不把语义主权移入前端
- 第一个 `integrations/graph-glsp` translation-only 图形适配器边界，它消费 Athena-owned projection-session payload，但不会引入第二条 semantic transport

当前还没有进入最终 UX 阶段。

## 模块拓扑

Athena 按架构职责组织实现资产。当前 JVM / Gradle 模块位于 `kernel/`、`extensions/`、`ui/` 与 `apps/` 下，Theia IDE 产品路径位于 `ide/`。

| 分组 | 模块 / 种子 | 目录 | 作用 |
| --- | --- | --- | --- |
| `ide` | `node: theia-product` | [`ide/theia-product/`](ide/theia-product/README.md) | 产品组合、打包与 Athena Theia 能力集成 |
| `ide` | `node: theia-frontend` | [`ide/theia-frontend/`](ide/theia-frontend/README.md) | Theia 前端贡献、工作台布局、面板与命令 |
| `ide` | `node: theia-backend` | [`ide/theia-backend/`](ide/theia-backend/README.md) | Theia 后端贡献、启动、路径处理与进程编排 |
| `ide` | `gradle: :ide:lsp` | [`ide/lsp/`](ide/lsp/README.md) | Athena LSP host 与 IDE 路径的 JVM 语义服务边界 |
| `integrations` | `node: graph-glsp` | [`integrations/graph-glsp/`](integrations/graph-glsp/README.md) | translation-only 图形适配器，负责把 GLSP-class protocol 与 rendering vocabulary 保持在 Athena-owned projection session 下游 |
| `integrations` | `:integrations:scm-git` | [`integrations/scm-git/`](integrations/scm-git/README.md) | 第一个 semantic baseline 供应商适配器 |
| `kernel` | `:kernel:language` | [`kernel/language/`](kernel/language/README.md) | 作者 DSL 的语法层与解析器 |
| `kernel` | `:kernel:repository-model` | [`kernel/repository-model/`](kernel/repository-model/README.md) | M5 的规范 repository/package 合同边界 |
| `kernel` | `:kernel:semantic-scm` | [`kernel/semantic-scm/`](kernel/semantic-scm/README.md) | M6 的规范 semantic SCM 边界，负责 baseline、diff、consequence、typed review、commit intent 与 package-aware history |
| `kernel` | `:kernel:engineering-model` | [`kernel/engineering-model/`](kernel/engineering-model/README.md) | lowering 之后的规范工程模型 |
| `kernel` | `:kernel:layout-model` | [`kernel/layout-model/`](kernel/layout-model/README.md) | 显式布局投影合同 |
| `kernel` | `:kernel:geometry-model` | [`kernel/geometry-model/`](kernel/geometry-model/README.md) | 显式几何投影合同 |
| `kernel` | `:kernel:projection-model` | [`kernel/projection-model/`](kernel/projection-model/README.md) | 面向 runtime、LSP 与图适配器的渲染器中立投影文档 |
| `kernel` | `:kernel:validation` | [`kernel/validation/`](kernel/validation/README.md) | 通用语义校验 |
| `kernel` | `:kernel:plugins:plugin-api` | [`kernel/plugins/plugin-api/`](kernel/plugins/plugin-api/README.md) | 稳定宿主插件 SPI，并包含 additive semantic review enrichment contract |
| `kernel` | `:kernel:plugins:plugin-host` | [`kernel/plugins/plugin-host/`](kernel/plugins/plugin-host/README.md) | 插件来源、审批、库存与宿主生命周期边界 |
| `kernel` | `:kernel:compiler` | [`kernel/compiler/`](kernel/compiler/README.md) | lowering、pass 编排、知识与宿主扩展协调 |
| `kernel` | `:kernel:runtime` | [`kernel/runtime/`](kernel/runtime/README.md) | workspace 生命周期、执行上下文、graph、command、history、semantic baseline/diff/review/commit、hosted review enrichment、plugin 与 AI proposal 托管 |
| `kernel` | `:kernel:svg-renderer` | [`kernel/svg-renderer/`](kernel/svg-renderer/README.md) | 从语义状态导出的确定性 SVG |
| `extensions` | `:extensions:domain-electrical` | [`extensions/domain-electrical/`](extensions/domain-electrical/README.md) | 第一个真实 Electrical 领域扩展 |
| `extensions` | `:extensions:domain-dummy` | [`extensions/domain-dummy/`](extensions/domain-dummy/README.md) | 证明 SPI 不被 electrical 私有化的合成域 |
| `ui` | `:ui:compose-workbench` | [`ui/compose-workbench/`](ui/compose-workbench/README.md) | 共享 Compose 工作台与 viewer 交互层 |
| `apps` | `:apps:cli` | [`apps/cli/`](apps/cli/README.md) | 终端入口 |
| `apps` | `:apps:desktop-viewer` | [`apps/desktop-viewer/`](apps/desktop-viewer/README.md) | 桌面 Compose 应用入口 |

## 构建要求

- Java 25
- Gradle wrapper `9.6.1`
- Kotlin `2.4.0`

Windows 上运行 Gradle 时必须顺序串行执行 build、test、run，不要并发开多个 shell。

## 快速开始

类 Unix shell：

```bash
./gradlew test
./gradlew :apps:cli:run --args="parse examples/m0/demo-cabinet.athena"
```

Windows PowerShell：

```powershell
java25; .\gradlew.bat test
java25; .\gradlew.bat :apps:cli:run --args "parse examples/m0/demo-cabinet.athena"
Set-Location ide
yarn build
yarn start:smoke
```

`java25` 是本机辅助命令，用来先切换到 Java 25 再执行 Gradle。

当前 M6 Athena IDE shell 中，Electron 包装层会在 Windows 上自动解析 Java 25。使用 `ide/` 下的 `yarn start:smoke` 作为确定性的桌面启动证明，再使用 `yarn start` 打开交互窗口。当前工作台里的 `Semantic SCM` panel 已经可以检查 baseline-driven review、commit preparation、package evolution 与 release relevance。聚焦里程碑用法请阅读 [`docs/usages/m5-proof-usage.md`](docs/usages/m5-proof-usage.md) 与 [`docs/usages/m6-proof-usage.md`](docs/usages/m6-proof-usage.md)。

## 架构说明

- AST 只属于语法层，并与语义真值保持分离。
- `:kernel:repository-model` 是 M5 的 VCS 中立 repository/package 合同边界。
- `:kernel:semantic-scm` 位于 `:kernel:repository-model` 之上，负责 baseline、diff 与 review 语义。
- `Engineering IR` 是规范工程模型。
- `:kernel:projection-model` 是 M7 的渲染器中立图形投影边界，位于 geometry 之上、runtime 与图适配器之下。
- 通用校验位于 `:kernel:validation`，领域校验位于扩展。
- 插件是真实可发现的，但不是主权边界。

## 许可证

见 [`LICENSE`](LICENSE)。
