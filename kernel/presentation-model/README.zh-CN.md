# kernel:presentation-model

`kernel:presentation-model` 定义 Athena 中立的 `Presentation IR`。

这个模块把 M13 的所有权链条固定为：

`Engineering IR -> Projection Model -> Presentation IR -> 域展示包 -> 渲染后端`

这里负责：

- 领域中立的展示文档
- primitive 与 composite 展示定义
- occurrence 与 connector 契约
- 保持在语义真相下游的可追踪元数据

这里不负责：

- 规范工程语义真相
- semantic macro 或 engineering assembly
- 后端专属 scene tree、batching、draw internals

`engineeringood` 的含义是 “good for engineering”，同时 `ood` 也表达 object-oriented development，这也是早期 `engineeringos` 语义目标的另一种表述。
