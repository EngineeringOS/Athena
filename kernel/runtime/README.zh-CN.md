# `:kernel:runtime`

[English](README.md) | 简体中文

`:kernel:runtime` 模块拥有 Athena 的长生命周期运行时边界。它负责 workspace 生命周期、活动项目上下文、运行时服务解析、投影会话、命令执行、历史、工程图投影、宿主插件生命周期检查与执行，以及可选的 AI 提案审阅，同时不会变成第二语义真源。

## 职责

- 通过 `AthenaRuntime` 打开和关闭工作区。
- 将项目激活到共享的 `AthenaExecutionContext`。
- 解析图、命令、插件与渲染协调等运行时服务。
- 通过活动 `RepositoryGraphSession` 解析 runtime-owned semantic baseline、semantic diff、semantic review 与 semantic commit 服务。
- 通过活动 `RepositoryGraphSession` 解析 runtime-owned semantic history projection state，用于向下游 IDE seam 发布 package evolution 与 release relevance。
- 把当前固定 knowledge pack 的激活状态带入 runtime-facing compiler session，让 IDE 与 mutation flow 能看到 M9 knowledge outputs。
- 消费 `:kernel:plugins:plugin-host` 治理的已批准插件清单。
- 在 core review 生成之后应用 hosted semantic review enrichment，同时保持 core review entry 仍然是 semantic authority。
- 对外暴露 runtime 可见的插件生命周期检查，同时不把编排所有权交给插件。
- 托管运行时拥有的投影会话，包括受支持视图发现与活动视图切换。
- 消费编译器派生的 `:kernel:projection-model` 文档，作为图形投影的主输入。
- 通过同一条 runtime-owned projection session 发布 projection family id、sheet state、notation pack 与 cross reference。
- 让运行时规范状态始终与 `Engineering IR` 保持一致。
- 托管命令历史、撤销、重做、重放、差异检查以及已接受 AI 提案流程。
- 在受支持的语义变更后，对外发布运行时可见的增量刷新元数据。
- 通过现有 runtime-owned mutation inspection surface 增量发布 engineering sufficiency diagnostics 与 impact consequences。

## 主要类型

- `AthenaRuntime`
- `AthenaWorkspace`
- `AthenaExecutionContext`
- `AthenaServiceRegistry`
- `AthenaSemanticBaselineService`
- `AthenaSemanticDiffService`
- `AthenaSemanticReviewService`
- `AthenaSemanticCommitService`
- `AthenaSemanticScmStateService`
- `AthenaSemanticHistoryStateService`
- `AthenaRuntimeProjectionSession`
- `AthenaCommandRuntimeService`
- `AthenaEngineeringGraphService`

## 依赖

- `:kernel:compiler`
- `:kernel:projection-model`
- `:kernel:plugins:plugin-host`
- `:kernel:engineering-model`
- `:kernel:svg-renderer`

## 边界

该模块不直接解析 DSL 源文本，不定义规范 IR 结构，也不拥有领域语义。它拥有这些下层之上的运行时生命周期与编排。投影会话只是建立在编译器派生的 `:kernel:projection-model` 工件之上的运行时状态；切换视图不会修改规范工程语义。Repeated reference 与 cross reference 仍然只是锚定 canonical semantic id 的下游 projection 证据，runtime 负责传输，不负责重新定义它们的语义。

## Review Contract

- `AthenaSemanticDiffInspection` 仍然锚定在规范 semantic id 与 command-linked history consequence 上。
- Projection refresh evidence 作为下游 consequence metadata 附加，而不是再造一套 history 或 diff 系统。
- Runtime inspection 可以解释受影响视图与下游层，但不会用 geometry-only review 替代 semantic change review。
- Runtime source-mutation inspection 现在也可以携带 engineering sufficiency diagnostics 与 typed impact consequences，但它们仍然只是增量证据，而不是第二套 review 子系统。
- Accepted mutation review 现在也复用同一套 semantic review 与 commit vocabulary，用 typed engineering-impact entry 区分 direct engineering edit 与 downstream affected subject。
- Repository baseline comparison 保持在 runtime-owned JVM path 上，并发布 compiler-derived validation 与 repository-contract consequence。
- Runtime review publication 复用同一条 baseline / diff path，输出 typed review entry，用于表达 affected package、authored intent category、derived consequence、validation impact 与 degraded input warning。
- Runtime review publication 现在也携带 typed engineering-impact consequence，让 SCM 与 accepted-mutation review 能检查和 Problems、semantic inspection 一致的 affected-subject consequence contract。
- Runtime commit publication 复用同一条 baseline / diff / review path，输出 typed commit-intent entry，用于表达 adapter-ready 的 commit preparation，而不把 staging 或 provider noun 泄漏进 kernel。
- Runtime semantic SCM projection 复用同一条 baseline / diff / review / commit path，向 LSP 与 workbench 发布单一 typed state，而不是让前端重建 review / commit 语义。
- Runtime semantic history projection 复用同一条 baseline / diff path 与 kernel history summarizer，向 LSP 与 workbench 发布单一 typed package-history state。
- 不完整 comparison input 继续通过 typed semantic consequence record 与附带 diagnostic 保持可检查，而不是退化成 UI 专有失败文本。

## 验证

```bash
./gradlew :kernel:runtime:test
```

Windows PowerShell:

```powershell
java25; .\gradlew.bat --no-daemon --console=plain :kernel:runtime:test
```
