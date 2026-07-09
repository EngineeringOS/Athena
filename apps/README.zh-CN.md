# `apps`

[English](README.md) | 简体中文

`apps` 分组包含面向用户与操作者的具体入口。应用把 kernel、extensions 与 UI 模块装配成可执行界面。

## 模块

- `:apps:cli` -> [`cli/`](cli/README.zh-CN.md)
- `:apps:desktop-viewer` -> [`desktop-viewer/`](desktop-viewer/README.zh-CN.md)

## 边界

应用层应保持轻量。它负责启动界面、装配依赖、展示结果，但不应吸收本应属于下层的可复用 UI 原语或语义所有权。
