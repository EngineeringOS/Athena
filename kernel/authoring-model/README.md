# `:kernel:authoring-model`

English | [Chinese (Simplified)](README.zh-CN.md)

The `:kernel:authoring-model` module defines Athena's platform-owned semantic authoring contracts.

This module keeps the ownership ladder explicit:

`guided authoring surface -> authoring intent -> authoring runtime -> M8 mutation authority -> Engineering IR`

## Responsibilities

- Publish stable authoring intent identities through `AuthoringIntentId`.
- Publish surface-agnostic guided authoring requests through `AuthoringIntent`.
- Publish semantic entity and relationship intent types through:
  - `CreateSemanticEntityIntent`
  - `UpdateSemanticEntityPropertiesIntent`
  - `RemoveSemanticEntityIntent`
  - `SemanticRelationshipIntent`
  - `RemoveSemanticRelationshipIntent` (preview and dependency-impact readiness; accepted removal is deferred)
  - `RevealSubjectIntent`
- Publish review-first preview and decision contracts through:
  - `AuthoringPreview`
  - `AuthoringPreviewChange`
  - `AcceptAuthoringPreviewDecision`
  - `RejectAuthoringPreviewDecision`
- Publish revision-safe transaction, validation stage, lifecycle, diagnostic, dependency-impact, and eligibility contracts.
- Publish transport-friendly value contracts through `AuthoringValue`.
- Preserve the boundary that guided authoring sits above M8 and is not a second mutation authority.

## Main Types

- `AuthoringIntentId`
- `AuthoringPropertyName`
- `AuthoringOrigin`
- `AuthoringSurface`
- `AuthoringRevealTarget`
- `AuthoringValue`
- `AuthoringIntent`
- `MutableSemanticEntityIntent`
- `SemanticAuthoringTransaction`
- `AuthoringRevisionGuard`
- `AuthoringPreviewId`
- `AuthoringPreviewChangeKind`
- `AuthoringPreviewStatus`
- `AuthoringPreview`
- `AuthoringPreviewDecisionKind`
- `AuthoringPreviewDecision`

## Dependencies

This module depends on:

- `:kernel:engineering-model` for canonical semantic identity through `StableSemanticIdentity`
- `:kernel:component-model` for vendor-neutral component concepts through `EngineeringConceptId`
- `:kernel:component-model` for semantic creation templates through `EngineeringConceptTemplateId`
- `:kernel:part-model` for optional implementation targeting through `PartImplementationId`

## Boundaries

This module does not define runtime orchestration, preview execution, mutation commit, Theia widgets, GLSP tools, LSP transport handlers, renderer behavior, or domain-specific catalogs.

It is the narrow platform contract layer that later palette, inspector, graph, form, template, AI, API, and DSL surfaces can target without breaking M8.

## Verification

```bash
./gradlew :kernel:authoring-model:test
```

Windows PowerShell:

```powershell
java25; .\gradlew.bat :kernel:authoring-model:test
```
