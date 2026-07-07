# `:kernel:geometry-model`

[English](README.md) | 简体中文

`:kernel:geometry-model` 模块定义 Athena 在 M2 中首个显式、面向渲染的几何契约。这些类型位于布局意图与规范语义之后，为后续 viewer 和 backend 提供受治理的几何边界，而不是重新从语义里私下推导意义。

## 职责

- 定义首版 `Geometry IR` 文档与元素契约。
- 定义首个投影证明所需的最小路径与点位契约。
- 让每个几何元素都携带规范语义标识。
- 为首个证明切片提供轻量的渲染面向几何种类与边界框类型。
- 为 M2 建立持久的 kernel 所有几何边界，而不是把 renderer 本地 DTO 扩展成平台真相。

## 主要类型

- `GeometryDocument`
- `GeometryElement`
- `GeometryElementId`
- `GeometryElementKind`
- `GeometryBounds`
- `GeometryPoint`

## 依赖

- `:kernel:engineering-model`

## 边界

该模块不负责从布局推导几何，不输出 SVG，不拥有视图切换，也不重新定义工程语义。它是显式几何契约层，后续 compiler、runtime、viewer 与 backend 故事会在这里消费精确摆放与路径几何，而不是重新从语义构建意义。

## 验证

```bash
./gradlew :kernel:geometry-model:test
```

Windows PowerShell:

```powershell
java25; .\gradlew.bat :kernel:geometry-model:test
```
