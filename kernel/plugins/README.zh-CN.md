# `kernel/plugins`

[English](README.md) | 简体中文

`kernel/plugins` 子分组承载 Athena 的插件基础设施模块。这些模块把面向扩展的稳定 SPI 与宿主拥有的发现、审批、生命周期和已批准清单治理明确分离。

## 模块

- `:kernel:plugins:plugin-api` -> [`plugin-api/`](plugin-api/README.zh-CN.md)
- `:kernel:plugins:plugin-host` -> [`plugin-host/`](plugin-host/README.zh-CN.md)

## 边界

- `plugin-api` 是扩展直接编译依赖的稳定公共契约面。
- `plugin-host` 是宿主拥有的治理层，负责发现、审批、清单管理，并把已安装插件暴露给 compiler 与 runtime 服务。
