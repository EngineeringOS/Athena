# `:kernel:engineering-model`

[English](README.md) | 简体中文

`:kernel:engineering-model` 模块定义 Athena 的规范工程模型。`Engineering IR` 是 lowering 之后的语义单一真源，独立于解析器结构、插件实现细节和渲染器专用布局。

## 职责

- 在 `EngineeringModel.kt` 中发布规范文档模型。
- 通过 `StableSemanticIdentity` 定义稳定语义标识。
- 通过 `SourceProvenance` 保留作者来源信息。
- 为后续校验与渲染提供带类型的工程属性与引用。

## 主要类型

- `EngineeringDocument`
- `EngineeringSystem`
- `EngineeringComponent`
- `EngineeringPort`
- `EngineeringConnection`
- `EngineeringReference`
- `EngineeringProperty`
- `EngineeringPropertyValue`

## 依赖

该模块没有项目内模块依赖。

## 边界

该模块不解析 DSL 文本、不执行语义规则校验、不做插件发现、不加载治理知识包，也不生成 SVG。它是其他模块操作的稳定模型。

## 验证

```bash
./gradlew :kernel:engineering-model:test
```

Windows PowerShell：

```powershell
java25; .\gradlew.bat :kernel:engineering-model:test
```


## IR

LLVM IR（Intermediate Representation，中间表示） 是LLVM编译器基础设施的核心，它是一种通用的、基于静态单赋值（SSA） 的中间语言。你可以把它理解为连接高级编程语言和机器码之间的“桥梁”。

**LLVM IR（Intermediate Representation，中间表示）** 是LLVM编译器基础设施的核心，它是一种通用的、基于**静态单赋值（SSA）** 的中间语言。你可以把它理解为连接高级编程语言和机器码之间的“桥梁”。

### 为什么需要LLVM IR？

在传统编译器中，每种源语言（如C++、Rust）直接编译到每种目标平台（如x86、ARM），需要开发 `N × M` 个编译器，复杂度极高。LLVM IR解决了这个**N×M复杂度问题**。它将编译过程解耦为三个独立模块：

1.  **前端（Frontend）**：将高级语言代码转换为LLVM IR。
2.  **优化器（Optimizer）**：对LLVM IR进行平台无关的优化。
3.  **后端（Backend）**：将优化后的LLVM IR转换为特定目标平台的机器码。

这种设计意味着，支持 `N` 种语言和 `M` 种平台，只需开发 `N + M` 个模块，极大地提升了编译器的可扩展性和代码复用性。

### LLVM IR的三种等价形态

LLVM IR以三种完全等价的形式存在，服务于不同的场景：

| 形态 | 描述 | 主要用途 |
| :--- | :--- | :--- |
| **内存中的IR (In-memory IR)** | 编译器内部数据结构，用于分析和优化。 | 编译器的核心操作对象，供优化Pass使用。 |
| **二进制码 (Bitcode)** | 磁盘上的二进制编码格式，加载速度快。 | 用于链接时优化（LTO）或即时编译（JIT），作为分发格式。 |
| **可读的汇编文本 (Assembly)** | 人类可读的文本格式。 | 用于调试、学习和手动编写测试用例。 |

### LLVM IR的核心特性

*   **静态单赋值（SSA）形式**：这是LLVM IR最核心的特性。它要求每个变量只能被赋值一次。当变量值需要改变时，会创建一个新的变量（如 `%a.1`）。这极大地简化了数据流分析，使许多优化（如常量传播、死代码消除）更容易实现。
*   **强类型系统**：LLVM IR是一个强类型语言，每个值都有明确的类型。这有助于在编译期捕获类型错误，并为优化提供信息。支持的类型包括：
    *   基本类型：`i32`（32位整数）、`float`（单精度浮点数）等。
    *   派生类型：`i32*`（指向32位整数的指针）、`[4 x i32]`（包含4个32位整数的数组）等。
*   **无限虚拟寄存器**：IR中的“寄存器”是无限的，用 `%` 前缀标识（如 `%a`, `%result`）。这完全解耦了物理寄存器的限制，简化了代码生成。
*   **类似RISC的指令集**：LLVM IR的指令集设计精简，类似于RISC架构。它采用**三地址码（Three-Address Code）** 格式，即一条指令通常包含两个源操作数和一个目标操作数。

### 一个简单的例子

下面是一个简单的C函数和它对应的LLVM IR文本表示：

**C代码:**
```c
int add(int a, int b) {
    return a + b;
}
```

**LLVM IR代码:**
```llvm
define i32 @add(i32 %a, i32 %b) {
  %sum = add i32 %a, %b
  ret i32 %sum
}
```

在这个IR示例中：
*   `define i32 @add(i32 %a, i32 %b)` 定义了一个返回 `i32` 类型、名为 `@add` 的函数，它接收两个 `i32` 类型的参数 `%a` 和 `%b`。
*   `%sum = add i32 %a, %b` 是一条SSA形式的加法指令，它将 `%a` 和 `%b` 相加，结果赋值给新变量 `%sum`。
*   `ret i32 %sum` 是返回指令，将 `%sum` 的值作为函数返回值。

### 总结

LLVM IR是现代编译器技术的基石，它通过提供一个**标准化、类型安全、基于SSA的中间表示**，成功地解耦了编译器的前端、优化器和后端。这不仅是解决N×M问题的关键，也为实现复杂的、平台无关的代码优化提供了理想的平台。

如果你想深入探索，官方提供的**LLVM语言参考手册（LLVM Language Reference Manual）** 是最全面和权威的资料。此外，LLVM官方教程（如Kaleidoscope教程）是动手实践生成LLVM IR的绝佳起点。
