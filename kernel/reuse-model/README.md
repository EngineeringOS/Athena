# `:kernel:reuse-model`

English | [Chinese (Simplified)](README.zh-CN.md)

The `:kernel:reuse-model` module defines Athena's first platform-owned Semantic Macro contract for M16.

This module keeps the ownership ladder explicit:

`reuse entry surface -> semantic macro contract -> template payloads -> runtime reuse services -> M8 mutation authority -> Engineering IR`

## Responsibilities

- Publish stable Semantic Macro identities through `SemanticMacroId`.
- Publish stable instantiation identities through `SemanticMacroInstantiationId`.
- Publish package-governed definition binding through `SemanticMacroPackageBinding` without collapsing package identity into macro identity.
- Publish surface-agnostic parameter schema through:
  - `SemanticMacroParameterName`
  - `SemanticMacroParameterValueKind`
  - `SemanticMacroParameterValue`
  - `SemanticMacroParameterDefinition`
- Publish review-first preview contracts through:
  - `SemanticMacroPreview`
  - `SemanticMacroPreviewChange`
- Publish accepted-expansion traceability contracts through:
  - `SemanticMacroAcceptedExpansion`
  - `ExpansionOrigin`
  - `ExpansionMembership`
- Preserve the boundary that semantic reuse stays semantic-first, package-governed, and downstream of M8 mutation authority.

## Main Types

- `SemanticMacroId`
- `SemanticMacroInstantiationId`
- `SemanticMacroParameterName`
- `SemanticMacroPackageBinding`
- `SemanticMacroContract`
- `SemanticMacroPreviewId`
- `SemanticMacroPreviewChangeKind`
- `SemanticMacroPreviewStatus`
- `SemanticMacroPreview`
- `SemanticMacroExpansionId`
- `SemanticMacroAcceptedExpansion`

## Dependencies

This module depends on:

- `:kernel:engineering-model` for canonical semantic identity through `StableSemanticIdentity`
- `:kernel:repository-model` for governed package identity through `PackageIdentifier`

## Boundaries

This module does not define reusable template payloads, catalog loading, parameter validation execution, preview execution, acceptance orchestration, Theia widgets, LSP handlers, renderer behavior, or domain-specific macro packs.

It is the narrow platform contract layer that later catalog, form, AI, DSL, and API surfaces can target without creating a second package system or a second mutation path.

## Verification

```bash
./gradlew :kernel:reuse-model:test
```

Windows PowerShell:

```powershell
java25; .\gradlew.bat :kernel:reuse-model:test
```
