# Athena M2 证明样例集

[English](README.md) | 简体中文

`examples/m2/` 发布首条几何驱动后端链路所需的最小证明样例集，以及桌面 operator proof 使用的语义种子。

## 目标

- 为受支持的 M2 视图保留共享语义种子。
- 只发布验证几何驱动 SVG 输出所需的最小工件。
- 让样例集保持足够小，便于在内核与架构阶段审阅。
- 保留一个不带初始连接的桌面种子，用于证明 runtime 命令创建连接。

## 内容

- `demo-cabinet.athena` - 几何后端证明使用的共享语义种子
- `demo-cabinet.expectation.txt` - 确定性证明检查使用的工件映射
- `demo-cabinet.cabinet.svg` - `cabinet` 视图的期望 SVG
- `demo-cabinet.wiring.svg` - `wiring` 视图的期望 SVG
- `operator-proof.athena` - 桌面工作台使用的语义种子，初始不带作者手写连接

## 边界

该目录不是通用导出目录。它是里程碑证明样例集，用于自动化验证 `Geometry IR` 已成为首条后端路径的渲染契约，同时验证桌面工作台能够在同一套语义之上完成 runtime-owned 交互证明。
