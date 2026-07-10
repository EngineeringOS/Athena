# `ide`

[English](README.md) | 简体中文

`ide` 分组是 Athena 当前以桌面优先为主的产品路径。它现在承载可运行的 Eclipse Theia 版 Athena 外壳、M5 仓库接缝、当前 M6 semantic SCM 与 package-history workbench projection，以及第一个 M7 图形 workbench surface。

## 范围

这个分组现在既是结构目录，也是可构建、可启动的产品路径。

当前已经建立：

- Athena Theia 产品路径的物理归属
- product、frontend、backend 与 LSP 之间的职责边界
- 用于构建和启动 Theia 桌面外壳的本地 Yarn workspace
- 外壳中的 Athena 品牌首页
- Engineering Repository 打开流与单仓库 session 激活路径
- 受治理的 M5 Engineering Repository 创建流
- 嵌入 JVM runtime stack 的 stdio Athena LSP server
- 通过 LSP 而不是直接语义导入完成的 `.athena` 编辑器打开路径
- 从 JVM 解析与校验进入 editor 与 Problems 的 Athena 自有 diagnostics 路径
- 通过同一条 LSP 边界提供的 completion、document-symbol、definition 与 references 路径
- 在重复编辑下保持 diagnostics 与 navigation 对齐最新内存态 Athena 文档的稳定路径
- 通过现有 backend 与 LSP bridge 投影 baseline-driven review、commit-preparation、package evolution 与 release relevance 的 Athena semantic SCM workbench panel
- 通过 graph adapter boundary 投影 runtime-owned node 与 relationship view，同时不把 projection authority 移入前端的 Athena 图形 workbench panel
- 通过 canonical semantic id 保持图形 selection、source reveal、semantic inspection 与 semantic SCM context 对齐的第一条同步语义选择路径
- 第一条 inspect-first 图形交互路径：active-view switching 通过 Athena runtime command allowlist 受治理，projection refresh 时会丢弃陈旧的 transient selection

它还没有交付：

- 通过 Athena LSP 提供的完整 hover、rename、formatting 与更丰富的多文件语言能力
- `.athena` 的 syntax highlighting 或 semantic tokens
- 更丰富的 review overlay 或更丰富的 governed 图形交互

## Packages

- `ide/theia-product/` -> [theia-product/](theia-product/README.md)
- `ide/theia-frontend/` -> [theia-frontend/](theia-frontend/README.md)
- `ide/theia-backend/` -> [theia-backend/](theia-backend/README.md)
- `ide/lsp/` -> [lsp/](lsp/README.md)

## Commands

```powershell
Set-Location ide
yarn install
yarn build
yarn start:smoke
yarn start
```

## 边界

`ide/` 拥有产品外壳结构与 Theia 集成，但不能吸收 kernel 的语义主权。当前 IDE 路径已经通过 `ide/lsp` 到达仓库激活、authored-source 打开、diagnostics，以及第一版严肃的 authoring/navigation 基线；同时也通过同一条 backend/LSP 路径承载增量 semantic SCM 与 package-history projection。`apps/cli`、`apps/desktop-viewer` 与 `ui/compose-workbench` 在 M4-M6 期间仍然是次级证明外壳。

当前新建仓库的 M5 bootstrap 结构：

- `<repository-root>/athena.yaml`
- `<repository-root>/athena.lock`
- `<repository-root>/src/<project>.athena`
- 一个显式声明 `src` 归属的主包 bootstrap
- 创建时不预写额外依赖图；规范 package resolution 仍由 compiler/runtime authority 后续派生
