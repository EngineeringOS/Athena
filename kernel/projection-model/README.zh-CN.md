# `:kernel:projection-model`

[English](README.md) | 简体中文

`projection-model` 是 M7 新增的内核边界，用来承载编译器派生出的图形投影文档。

它位于以下边界之后：

- `:kernel:engineering-model`
- `:kernel:layout-model`
- `:kernel:geometry-model`

它位于以下边界之前：

- `:kernel:runtime`
- `:ide:lsp`
- `integrations/graph-*`
- 桌面或 Web 工作台界面

## 目标

这个模块为 Athena 提供一套类型化、渲染器中立的投影文档形态，让 runtime、LSP 与后续图适配器不需要再从 geometry 私下重建各自的模型。

边界故意保持很小：

- 视图语义仍由 `:kernel:layout-model` 中的 `ViewDefinition` 持有
- 投影文档保留规范语义标识
- 投影节点与连接保留可检查的 geometry 来源引用
- 这里不出现 Theia、GLSP、canvas 或前端 DTO

## 当前范围

当前 M7 切片公开：

- `ProjectionDocument`
- `ProjectionNode`
- `ProjectionConnection`
- 投影本地标识与简单坐标类型
- `ProjectionModelMarker`

这里不公开：

- runtime session 生命周期
- LSP payload
- 图框架协议对象
- 渲染器私有的可变状态

## 规则

如果下游界面需要图形投影数据，应当消费编译器或 runtime 提供的 `:kernel:projection-model` 输出，而不是从原始 geometry 再派生第二套权威模型。
