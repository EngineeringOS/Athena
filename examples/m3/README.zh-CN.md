# Athena M3 证明样例集

[English](README.md) | 简体中文

`examples/m3/` 发布 M3 可扩展性里程碑所需的最小托管领域证明样例集。

## 目标

- 用一个仅电气领域的作者输入，证明 `domain-electrical` 通过稳定托管 SPI 参与。
- 用一个仅 dummy 领域的作者输入，证明 `domain-dummy` 通过同一托管 SPI 参与。
- 证明两个托管领域可以共存，而不需要内核特判。
- 让样例集保持足够小，便于审阅，同时可被 Epic 3 的托管验证矩阵复用。

## 内容

- `electrical-proof.athena` - 仅电气领域的托管证明输入
- `electrical-proof.expectation.txt` - 已批准插件、视图和渲染期望契约
- `dummy-proof.athena` - 仅 dummy 领域的托管证明输入
- `dummy-proof.expectation.txt` - dummy 路径在无全局视图定义时的托管证明契约
- `dual-domain-proof.athena` - 混合领域的托管证明输入
- `dual-domain-proof.expectation.txt` - 已批准插件与混合托管状态的期望契约

## 边界

这个目录不是样例展示区，而是里程碑证明样例集。`.athena` 作者输入仍然是唯一语义真相，旁侧期望文件只描述确定性回归检查所需的最小托管结果。
