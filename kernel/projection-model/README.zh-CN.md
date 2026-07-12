# `:kernel:projection-model`

[English](README.md) | 简体中文

`projection-model` 是 Athena 在 M7 建立的 kernel 边界，用来承载由 compiler 派生出来的、渲染器中立的图形投影文档。

到 M11 为止，它还承载：

- 第一版受治理的电气 sheet 模型
- 第一版受治理的电气 notation pack 模型
- 第一版重复引用与 cross-reference 合约

这些层都保持为 projection-owned，并且始终位于 canonical engineering meaning 的下游。

它位于以下边界之后：

- `:kernel:engineering-model`
- `:kernel:layout-model`
- `:kernel:geometry-model`

它位于以下边界之前：

- `:kernel:runtime`
- `:ide:lsp`
- `integrations/graph-*`
- desktop 或 web workbench surface

## 目标

这个模块为 Athena 提供一套类型化、渲染器中立的 projection document 形态，让 runtime、LSP 与下游 graph adapter 不需要再从 geometry 私下重建自己的图模型。

这个边界刻意保持很小：

- 视图语义仍由 `:kernel:layout-model` 中的 `ViewDefinition` 持有
- `ViewDefinition` 可以携带受治理的 projection family 合约，用于更深的电气工作台能力
- projection document 保留 canonical semantic identity
- projection sheet 保留稳定的下游 identity、顺序与导航语义
- projection sheet subject 始终指回 canonical semantic identity，而不是把 sheet 变成真相
- projection notation pack 让 symbol 选择、label 策略与 marker token 保持可检查，而不是把 notation 变成真相
- projection cross reference 让重复引用保持可检查，同时仍然锚定 canonical semantic identity
- projection node 与 connection 保留可检查的 geometry 来源引用
- 这里不出现 Theia、GLSP、canvas 或前端 DTO

## 当前范围

当前边界公开：

- `ProjectionDocument`
- `ProjectionNode`
- `ProjectionConnection`
- `ProjectionSheet`
- `ProjectionSheetSubject`
- `ProjectionNotationPack`
- `ProjectionNotationSubject`
- `ProjectionCrossReference`
- projection 本地 id 与简单的坐标类型
- `ProjectionModelMarker`

这里不公开：

- runtime session 生命周期
- LSP payload
- graph framework 协议对象
- renderer 自有的可变状态

## 规则

如果下游界面需要图形投影数据，应当消费 compiler 或 runtime 提供的 `:kernel:projection-model` 输出，而不是从原始 geometry 再派生第二套权威模型。

如果下游界面需要 page 或 sheet 导航，应当消费这里的受治理 sheet 合约，而不是发明前端本地的 page truth。

如果下游界面需要 symbol 选择、label 行为或展示 marker，应当消费这里的受治理 notation-pack 合约，而不是硬编码 renderer-local notation。

如果下游界面需要 repeated reference 或 cross-reference 信息，应当消费这里的 canonical-identity-first 合约，而不是创建 view-local alias identity。
