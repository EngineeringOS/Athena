# `ui`

[English](README.md) | 简体中文

`ui` 分组包含位于 kernel 之上的可复用界面基础设施。Athena 在这里承载工作台与 viewer 原语，但不会把工程语义移动到表现层。

## 模块

- `:ui:compose-workbench` -> [`compose-workbench/`](compose-workbench/README.zh-CN.md)

## 边界

UI 模块可以组合、检查和投影 kernel 拥有的状态，但不能变成第二个语义主权边界。
