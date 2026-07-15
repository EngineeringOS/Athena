# `:kernel:language`

[English](README.md) | 简体中文

`:kernel:language` 模块拥有 Athena 当前 M0 Electrical/Runtime DSL 的语法层。它把作者输入的源文本解析为仅语法含义的 AST，并保留源码范围与语法诊断，但不赋予语义真值。

## M17 冻结的公开语法契约

`AthenaLanguageModel.kt` 是 Athena 在 M17 中**冻结的公开 authored 语法契约**。

该契约包括：

- `SourceFileAst` 以及声明节点（`SystemDeclaration`、`Declaration`、`DeviceDeclaration`、`PortDeclaration`、`ConnectionDeclaration`、`QualifiedName`、`PropertyAssignment`、`ScalarValue`）
- `ParseResult` / `ParseSuccess` / `ParseFailure`
- `SourcePosition`、`SourceSpan` 与 `SyntaxDiagnostic`

任何未来的解析器实现都必须继续产出这些相同的 Athena 自有契约类型。下游模块必须
依赖这些契约，而绝不能依赖解析器生成器内部（ANTLR 树、Tree-sitter CST 节点，或
tokenizer/token 类型）。

`AthenaLanguageParser.parse` 是受支持的门面入口：调用方只消费 `ParseResult`，不得依赖解析的内部实现方式。

## 公开包与内部包

- `com.engineeringood.athena.language` 是**公开**语法边界（authored AST、解析结果、span、诊断，以及 `AthenaLanguageParser` 门面）。
- `com.engineeringood.athena.language.antlr` 是**内部实现细节**（`AthenaLanguageParser`
  门面背后的生成语法产物与 parse-tree-to-AST 适配层）。
- 下游模块（`:kernel:compiler`、`:kernel:runtime`、`:ide:*` 等）绝不能直接导入内部
  `antlr` 子包。

## 未来语法落地带

M17 为 authored AST 的扩展做好准备；它**并不**最终确定诸如 `import` 之类的未来语法语义。

- 新的顶层 authored 构造以新的 `Declaration` sealed 变体落地（字段级字面量种类落在 `ScalarValue`）。
- 新构造的解析适配隔离在 `com.engineeringood.athena.language.antlr`（Story `1.2` 与
  Epic 2 收口后的内部接缝）内完成。
- Lowering 及其他消费者必须通过**穷尽性** `when` 处理新变体，使未处理种类在编译期失败。
- 具体的假想 `ImportDeclaration` 示意见 `docs/future-syntax-landing-zone.md`。Epic 5 Story `5.3` 发布里程碑级落地带收口说明，本模块笔记即为其代码侧依据。

## 职责

- 在 `AthenaLanguageModel.kt` 中定义语法模型。
- 在 `AthenaLanguageParser.kt` 中完成源文本解析（实现细节位于 `language.antlr`）。
- 通过 `SourcePosition`、`SourceSpan` 与 `SyntaxDiagnostic` 保留作者来源信息。
- 保持 AST 只属于语法层，不承担语义校验或规范工程含义。

## 主要类型

- `SourceFileAst`
- `SystemDeclaration`
- `DeviceDeclaration`
- `PortDeclaration`
- `ConnectionDeclaration`
- `QualifiedName`
- `ParseSuccess` / `ParseFailure`

## 依赖

该模块没有项目内模块依赖。

## 边界

该模块不负责 lowering 到 `Engineering IR`、不做引用解析、不做领域规则校验，也不渲染图形。这些职责位于 `:kernel:compiler`、`:kernel:validation`、领域插件以及 `:kernel:svg-renderer`。

## 验证

```bash
./gradlew :kernel:language:test
```

Windows PowerShell：

```powershell
java25; .\gradlew.bat :kernel:language:test
```
