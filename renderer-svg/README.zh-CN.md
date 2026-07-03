# `:renderer-svg`

[English](README.md) | 简体中文

`:renderer-svg` 模块拥有 M0 证明所需的轻量渲染模型与确定性 SVG 输出能力。它从语义真源派生下游产物，但不会创建或重新解释这些语义。

## 职责

- 在 `SvgRenderModel.kt` 中定义面向渲染的 DTO。
- 在 `SvgRenderer.kt` 中输出简单且稳定的 SVG 字符串。
- 严格位于规范 `Engineering IR` 之后。
- 保持渲染逻辑小而确定，不做语义恢复。

## 主要类型

- `SvgRenderModel`
- `SvgRenderBox`
- `SvgRenderConnection`
- `SvgRenderer`

## 依赖

- `:semantics-core`
- `:ir`

## 边界

该模块不解析源文本、不校验工程语义、不拥有插件契约，也不会从无效输入中反推布局语义。编译器必须先向它提供一个已经可安全渲染的轻量渲染模型。

## 验证

```bash
./gradlew :renderer-svg:test
```

Windows PowerShell：

```powershell
java25; .\gradlew.bat :renderer-svg:test
```
