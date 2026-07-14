# `:kernel:authoring-model`

[English](README.md) | 简体中文

`:kernel:authoring-model` 模块定义 Athena 在 M15 的第一层平台级 guided authoring contract。

这个模块把所有权链路固定为：

`guided authoring surface -> authoring intent -> authoring runtime -> M8 mutation authority -> Engineering IR`

## 职责

- 通过 `AuthoringIntentId` 发布稳定的 authoring intent identity。
- 通过 `AuthoringIntent` 发布与具体界面无关的 guided authoring request。
- 通过以下类型发布第一批窄范围 intent：
  - `CreateComponentIntent`
  - `UpdateComponentPropertiesIntent`
  - `ConnectPortsIntent`
  - `RevealSubjectIntent`
- 通过以下类型发布 review-first preview 与 decision contract：
  - `AuthoringPreview`
  - `AuthoringPreviewChange`
  - `AcceptAuthoringPreviewDecision`
  - `RejectAuthoringPreviewDecision`
- 通过 `AuthoringValue` 发布可传输、可预览的 value contract。
- 保持 guided authoring 位于 M8 之上，并且不是第二条 mutation authority。

## 主要类型

- `AuthoringIntentId`
- `AuthoringPropertyName`
- `AuthoringOrigin`
- `AuthoringSurface`
- `AuthoringRevealTarget`
- `AuthoringValue`
- `AuthoringIntent`
- `AuthoringPreviewId`
- `AuthoringPreviewChangeKind`
- `AuthoringPreviewStatus`
- `AuthoringPreview`
- `AuthoringPreviewDecisionKind`
- `AuthoringPreviewDecision`

## 依赖

这个模块依赖：

- `:kernel:engineering-model`，通过 `StableSemanticIdentity` 复用 canonical semantic identity
- `:kernel:component-model`，通过 `EngineeringConceptId` 复用 vendor-neutral component concept
- `:kernel:part-model`，通过 `PartImplementationId` 支持可选 implementation targeting

## 边界

这个模块不定义 runtime orchestration、preview execution、mutation commit、Theia widget、GLSP tool、LSP transport handler、renderer behavior 或 domain-specific catalog。

它只是一个窄范围的平台 contract 层，后续 palette、inspector、graph、form、template、AI、API 与 DSL surface 都可以依赖它，而不会破坏 M8。

## 验证

```bash
./gradlew :kernel:authoring-model:test
```

Windows PowerShell:

```powershell
java25; .\gradlew.bat :kernel:authoring-model:test
```


