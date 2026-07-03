# Athena

[English](README.md) | 简体中文

Athena 是 EngineeringOS 语义编译器论点的当前 JVM-first 实现工作区。

## 名称说明

`engineeringood` 同时表达两层含义：

- `engineering good`：for engineering good，面向工程之善，或说“对工程更好”
- `OOD`：object-oriented development，面向对象开发

放在 EngineeringOS 的语境里，这个命名是有意的。它是平台语义目标的另一种表达：工程含义应该被显式化、结构化、可组合化，并沉淀为持久模型，而不是被困在图纸、文件格式或某个工具私有的表示之中。

在 M0 阶段，这个仓库正在证明一个明确的架构主张：

- DSL 是作者输入的真源
- `Engineering IR` 是规范语义模型
- 编译器拥有管线编排权
- 插件可以扩展领域行为，但不能拥有语义主权
- 渲染只是下游后端，而不是真源

## 当前范围

当前 M0 证明包含：

- 一个最小化的 Electrical/Runtime DSL
- 解析为仅语法含义的 AST
- lowering 到规范 `Engineering IR`
- 核心语义校验与插件提供的领域语义
- 确定性的 SVG 渲染
- JVM 本地插件发现
- 治理知识包加载与解析
- 外部边界描述符校验
- 发布在 [`examples/`](examples/README.md) 下的符合性样例

这是一项架构证明，不是完整产品界面或 UX 阶段。

## 仓库地图

| 路径 | 用途 |
| --- | --- |
| [`cli/`](cli/README.md) | 当前编译器命令的命令行入口 |
| [`language/`](language/README.md) | M0 DSL 的语法层与解析器 |
| [`ir/`](ir/README.md) | 规范 `Engineering IR` 模型 |
| [`semantics-core/`](semantics-core/README.md) | 针对规范 IR 的通用语义校验 |
| [`renderer-svg/`](renderer-svg/README.md) | 轻量渲染模型与确定性 SVG 输出 |
| [`compiler/`](compiler/README.md) | 编译器门面、管线编排、插件、知识与边界描述符 |
| [`domain-electrical-runtime/`](domain-electrical-runtime/README.md) | M0 Electrical/Runtime 语义的第一个真实领域插件 |
| [`examples/`](examples/README.md) | M0 符合性样例与发布的期望产物 |
| [`docs/compiler/`](docs/compiler) | 编译器工作区的实现边界说明 |
| [`manifesto/`](manifesto/README.md) | 仓库背后的产品与平台愿景 |

## 构建要求

- 必须使用 Java 25
- Gradle Wrapper：`9.6.1`
- Kotlin：`2.4.0`

根构建会在完整校验前显式验证 Java 25。

## 快速开始

类 Unix shell：

```bash
./gradlew test
./gradlew :cli:run --args="parse examples/m0/demo-cabinet.athena"
```

在本仓库的 Windows PowerShell 环境中：

⚠️⚠️⚠️ `java25` 为本地指令，调整 `JAVA_HOME` 到 `25` 环境

```powershell
java25; .\gradlew.bat test
java25; .\gradlew.bat :cli:run --args "parse examples/m0/demo-cabinet.athena"
```

## 推荐阅读

如果你想先看实现视角：

1. [`compiler/README.md`](compiler/README.md)
2. [`docs/compiler/m0-pass-pipeline.md`](docs/compiler/m0-pass-pipeline.md)
3. [`docs/compiler/m0-plugin-contract-boundary.md`](docs/compiler/m0-plugin-contract-boundary.md)
4. [`docs/compiler/m0-domain-plugin-boundary.md`](docs/compiler/m0-domain-plugin-boundary.md)
5. [`examples/README.md`](examples/README.md)

如果你想先看平台论点：

1. [`manifesto/README.md`](manifesto/README.md)
2. `manifesto/docs/` 下的架构与技术章节

## 架构说明

- AST 只属于语法层，并与语义真值保持分离。
- `Engineering IR` 是规范工程模型。
- 通用校验位于 `:semantics-core`；领域校验位于类型化插件。
- 插件是真实且可发现的，但不拥有主权。
- 治理知识包与外部边界描述符都独立于作者编写的工程输入。

## 许可证

见 [`LICENSE`](LICENSE)。
