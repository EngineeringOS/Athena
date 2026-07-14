# `:kernel:template-model`

English | [Chinese (Simplified)](README.zh-CN.md)

The `:kernel:template-model` module defines Athena's reusable component-template and connection-template payload contracts for M16.

This module keeps the ownership ladder explicit:

`semantic macro contract -> template payloads -> runtime reuse services -> M8 mutation authority -> Engineering IR`

## Responsibilities

- Publish reusable component payload identity through `ComponentTemplateId`.
- Publish reusable connection payload identity through `ConnectionTemplateId`.
- Publish parameter-aware template property values through:
  - `TemplateValue.Literal`
  - `TemplateValue.ParameterReference`
- Publish semantic component-template payloads through `ComponentTemplate`.
- Publish semantic connection-template payloads through `ConnectionTemplate`.
- Publish template-scoped default metadata and optional advisory hints through:
  - `TemplateDefaultMetadata`
  - `TemplatePresentationHint`
  - `TemplateDocumentationHint`
- Preserve the boundary that templates stay semantic-first and do not turn SVG, manual layout, or renderer state into engineering truth.

## Main Types

- `ComponentTemplateId`
- `ComponentTemplatePropertyName`
- `TemplateValue`
- `ComponentTemplate`
- `ConnectionTemplateId`
- `TemplatePortReference`
- `ConnectionTemplate`
- `TemplateDefaultMetadata`
- `TemplatePresentationHint`
- `TemplateDocumentationHint`

## Dependencies

This module depends on:

- `:kernel:component-model` for vendor-neutral concept identity through `EngineeringConceptId`
- `:kernel:part-model` for optional implementation mapping through `PartImplementationId`
- `:kernel:connection-model` for semantic endpoint roles through `SemanticPortRoleId`
- `:kernel:reuse-model` for macro parameter names and literal parameter values

## Boundaries

This module does not define package resolution, runtime expansion, parameter validation execution, preview orchestration, acceptance handoff, Theia widgets, LSP handlers, SVG geometry, or manual layout truth.

It is the narrow payload layer that lets later runtime services compose governed reusable assemblies without redefining semantic authority in frontend or renderer code.

## Verification

```bash
./gradlew :kernel:template-model:test
```

Windows PowerShell:

```powershell
java25; .\gradlew.bat :kernel:template-model:test
```
