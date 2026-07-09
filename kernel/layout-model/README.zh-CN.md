# `:kernel:layout-model`

[English](README.md) | 简体中文

`:kernel:layout-model` 模块定义 Athena 在 M2 的首个显式布局意图契约。这些类型位于规范 `Engineering IR` 之后，用来表达某个受支持视图希望如何组织语义真相，但它们本身不是语义真源。

## 职责

- 定义受支持投影视图上下文的 `ViewDefinition`。
- 定义首版 `Layout IR` 文档、分组、节点、相对摆放与关系契约。
- 让规范语义标识在布局工件中保持一等地位。
- 为布局意图提供持久的 kernel 所有模型边界，而不是把它藏在 UI 或渲染器代码里。

## 主要类型

- `ViewDefinition`
- `LayoutDocument`
- `LayoutGroup`
- `LayoutNode`
- `LayoutNodeId`
- `LayoutRelationship`
- `LayoutRelativePlacement`

## 依赖

- `:kernel:engineering-model`

## 边界

该模块不负责从语义状态推导布局，不拥有 runtime 投影视图会话，不输出几何，也不修改工程语义。它是持久模型层，后续 compiler 与 runtime 故事会在这里消费分组、排序、相对摆放与强调等布局意图契约。

## 验证

```bash
./gradlew :kernel:layout-model:test
```

Windows PowerShell:

```powershell
java25; .\gradlew.bat :kernel:layout-model:test
```
