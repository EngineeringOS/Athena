# `:kernel:language`

[English](README.md) | 简体中文

`:kernel:language` 模块拥有 Athena 当前 M0 Electrical/Runtime DSL 的语法层。它把作者输入的源文本解析为仅语法含义的 AST，并保留源码范围与语法诊断，但不赋予语义真值。

## 职责

- 在 `AthenaLanguageModel.kt` 中定义语法模型。
- 在 `AthenaLanguageParser.kt` 中完成源文本解析。
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
