# `examples/m7`

[English](README.md) | 简体中文

`examples/m7` 记录图形投影里程碑当前发布的证明语料。

## 当前证明样例

M7 有意复用 [`../m4/open-repository-proof/`](../m4/open-repository-proof/) 下已经受治理的仓库样例，而不是再发明一种只服务图形界面的仓库格式。

当前 M7 证明使用这个样例：

- `../m4/open-repository-proof/athena.yaml` - 作者仓库/包意图
- `../m4/open-repository-proof/athena.lock` - 规范派生 lock
- `../m4/open-repository-proof/src/factory-line.athena` - 驱动当前图形证明的源文件

## M7 在同一样例上新增的内容

- 与源码编辑器并排打开的 Graphical View
- 图优先的 split workbench 姿态
- 可平移、可缩放、接近无限画布的视口
- 来自 `domain-electrical` 的扩展自有 `cabinet` 与 `wiring` 投影视觉映射

## 主使用说明

- [`docs/usages/m7-proof-usage.md`](../../docs/usages/m7-proof-usage.md)
