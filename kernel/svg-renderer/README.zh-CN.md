# `:kernel:svg-renderer`

[English](README.md) | 简体中文

`:kernel:svg-renderer` 模块拥有当前证明后端所需的确定性 SVG 输出能力。在 M2 中，它直接消费显式 `Geometry IR`，并在不重建语义或布局含义的前提下输出稳定 SVG。

## 职责

- 在 `SvgRenderModel.kt` 中定义轻量运行时/查看器 DTO。
- 在 `SvgRenderer.kt` 中输出简单且稳定的 SVG 字符串。
- 直接消费显式 `Geometry IR` 以完成首条后端证明链路。
- 保持渲染逻辑小而确定，不做语义恢复。

## 主要类型

- `SvgRenderModel`
- `SvgRenderBox`
- `SvgRenderConnection`
- `SvgRenderer`

## 依赖

- `:kernel:engineering-model`
- `:kernel:geometry-model`

## 边界

该模块不解析源文本，不校验工程语义，不拥有插件契约，也不会从无效输入中反推布局语义。编译器必须向它提供用于后端输出的显式 `Geometry IR`，或已经由几何派生出的轻量查看器模型。当前 SVG 证明仅渲染首条后端链路所需的 `BOX` 与 `PATH` 子集，不会虚构缺失语义。

## 验证

```bash
./gradlew :kernel:svg-renderer:test
```

Windows PowerShell:

```powershell
java25; .\gradlew.bat :kernel:svg-renderer:test
```
