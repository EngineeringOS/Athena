---
status: done
baseline_commit: cbe65c3fcf0fe4f05e5edfa2eb98edbe4f3e237e
story_id: 2.1
epic: 2
title: Generalize Semantic Entity Authoring Contracts
---

# Story 2.1: Generalize Semantic Entity Authoring Contracts

## Status

Done

## Story

As an EngineeringOS platform author,
I want generic entity authoring contracts and governed concept templates,
so that electrical components are the first specialization rather than the platform abstraction.

## Acceptance Criteria

1. Generic create, update, and remove semantic-entity intents carry stable intent identity, origin, canonical subject/creation context, concept template or target identity, typed properties, Revision Guard, and provenance.
2. An Engineering Concept Template supplies semantic type, default model, governed property schema, nested port names/directions/signals or media, terminal metadata, relationship capabilities, and provenance.
3. Engineering Concept Template contracts contain no representation primitive, SVG/path, style token, anchor geometry, bounds, coordinates, or viewBox fields.
4. Electrical template instances live in `extensions/domain-electrical`, not in the generic kernel contract, and include a tested M31 motor/entity template.
5. Update and remove contracts expose typed preview, capability, dependency impact, blocked eligibility, and stable diagnostics without implementing accepted customer-facing update/removal mutation.
6. All in-scope consumers use generic entity terminology; replaced `CreateComponentIntent` and `UpdateComponentPropertiesIntent` model names are removed. Any source-planning/frontend authority awaiting Stories 2.2/4.1 is explicitly ledgered, not hidden.
7. Mandatory Polish/Purge Gate and AC-to-evidence mapping are complete.

## Tasks/Subtasks

- [x] Add failing authoring-model contract tests for generic create/update/remove intents, Revision Guard/provenance, dependency impact, and blocked removal eligibility. (AC: 1,5)
- [x] Add failing component-model tests for semantic-only Engineering Concept Template anatomy and forbidden visual vocabulary. (AC: 2,3)
- [x] Add failing electrical-extension tests for domain-owned motor/entity template instances. (AC: 4)
- [x] Implement generic semantic entity intent, context, provenance, preview-impact, and eligibility contracts; migrate kernel/runtime/LSP callers from component-specific intent types. (AC: 1,5,6)
- [x] Implement frontend-independent Engineering Concept Template contracts and electrical instances without representation dependencies. (AC: 2,3,4)
- [x] Remove replaced component-intent model names and update tests/docs; ledger source-planning or frontend paths that belong to later stories. (AC: 6)
- [x] Run focused and full authoring-model, component-model, electrical-extension, runtime, and affected LSP tests sequentially. (AC: 1,2,3,4,5,6)
- [x] Complete mandatory polish/purge review and record AC-to-evidence mapping. (AC: 7)

## Dev Notes

### Architecture Guardrails

- Generic platform contracts may say semantic entity, concept template, property, port, terminal, relationship capability, and semantic context. They must not encode an electrical-only entity taxonomy.
- Electrical template instances belong to `extensions/domain-electrical`; do not place motor, voltage, IEC symbol, or electrical signal policy in the generic contract.
- Engineering Concept Template and M30 Representation Definition remain independent. Do not add primitive, path, SVG, style, anchor, bounds, coordinate, transform, viewBox, or renderer fields/dependencies.
- Use the Story 1.2 `AuthoringRevisionGuard` and Story 1.3 diagnostic/lifecycle envelope. Do not create another revision, provenance, preview, capability, or diagnostic model.
- Update/remove are contract-readiness only. No persistence, cascade delete, or customer-facing accepted workflow belongs here.
- Non-empty entity dependency impact must produce blocked eligibility and `authoring.removal.dependencies`.
- Migrate consumers atomically enough that every verified module compiles. Do not leave a typealias or duplicate component-specific contract as a silent compatibility layer.
- Story 2.2 owns AST-aware source planning and frontend serializer removal. Retained source-planning functions must be ledgered with that target rather than cosmetically generalized and claimed complete.

### Existing Code To Extend

- `kernel/authoring-model/.../AuthoringIntentModels.kt`: currently defines `CreateComponentIntent` and `UpdateComponentPropertiesIntent`; replace these with generic entity contracts and update exhaustive `when` consumers.
- `kernel/authoring-model/.../AuthoringPreviewModels.kt`: extend with typed dependency impact and acceptance eligibility using source-compatible defaults where unrelated previews remain.
- `kernel/component-model/.../EngineeringConceptModels.kt`: existing vendor-neutral concept identity/definition. Add a cohesive template contract in this module or a role-focused adjacent file.
- `extensions/domain-electrical/.../ElectricalRuntimeComponentKnowledge.kt`: existing domain-owned electrical concept definitions. Keep definitions stable and add electrical template instances in a cohesive adjacent file.
- `kernel/runtime/.../AthenaAuthoringSessionRuntimeService.kt`, authoring-model mappings/tests, and LSP authoring/source-edit protocols are known callers of component-specific intent names.

### Contract Shape

```text
EngineeringConceptTemplate
  templateId
  conceptId
  semanticType
  defaultModel?
  propertySchema
  nestedPortTemplates
  relationshipCapabilities
  provenance

CreateSemanticEntityIntent
UpdateSemanticEntityPropertiesIntent
RemoveSemanticEntityIntent
  -> Revision Guard
  -> canonical context/subject
  -> provenance
```

Use typed identifiers/value objects rather than unstructured maps for template anatomy. Authored
property values may continue to use the existing transport-safe `AuthoringValue` family.

### Testing Requirements

- Follow RED-GREEN-REFACTOR and record expected RED failures.
- Prove forbidden visual vocabulary by public contract inspection and dependency direction, not a comment-only assertion.
- Prove motor template nested ports, directions, media/signals, relationship capabilities, and provenance exactly.
- Prove blocked removal includes dependent relationship/occurrence ids and invokes no accepted mutation path.
- Run all Gradle commands strictly sequentially on Windows.
- Run encoding audit after text changes and CodeGraph caller review before deleting old names.

### Previous Epic Intelligence

- Capability evidence already models `CREATE_ENTITY`, `UPDATE_ENTITY`, `REMOVE_ENTITY`, and requirements in M29's registry.
- Transaction and Revision Guard are complete; mutable entity intents should feed that envelope rather than duplicate it.
- Validation runtime supports ordered stages and structured blocked outcomes.
- M31-CL-002 owns source evaluator/planning adaptation in Story 2.2; M31-CL-003 owns `ConnectPortsIntent` in Story 2.4.

### Scope Boundaries

- No backend AST insertion algorithm or accepted entity persistence.
- No relationship consumer migration beyond entity-contract references.
- No frontend transaction dialog or component library browser.
- No representation selection, symbol primitives, placement coordinates, or renderer changes.
- No accepted update/remove UX and no cascade delete.

### References

- `_bmad-output/implementation-artifacts/m31/epics.md` - Epic 2, Story 2.1.
- `_bmad-output/planning-artifacts/architecture/architecture-Athena-2026-07-21-m31/ARCHITECTURE-SPINE.md` - AD-6, AD-9, AD-14, AD-15.
- `_bmad-output/planning-artifacts/architecture/architecture-Athena-2026-07-21-m31/AUTHORING-CONTRACT.md` - Engineering Concept Template and Entity Removal Rules.
- `_bmad-output/implementation-artifacts/m31/epic-1-retro-2026-07-21.md` - Epic 2 critical-path constraints.

## Dev Agent Record

### Debug Log

- RED authoring contracts: focused tests failed until generic create/update/remove intents, typed dependency impact, and blocked removal eligibility existed.
- RED concept templates: component-model tests failed until semantic-only template anatomy and public-contract forbidden-vocabulary checks existed.
- RED electrical specialization: extension tests failed until the domain-owned motor template supplied exact nested ports, terminal metadata, relationship capabilities, and provenance.
- GREEN: focused suites passed after generic entity migration and electrical template implementation; runtime and LSP callers compiled against the new names.
- Regression cleanup: full frontend test initially reported seven M29/M30 wiring failures. The M29 active-view contract and M30 repository-root resolution were corrected; the focused 10-test rerun and full 164-test suite passed.
- Final verification: `:kernel:authoring-model:test`, `:kernel:component-model:test`, `:extensions:domain-electrical:test`, `:kernel:runtime:test`, and `:ide:lsp:test` passed sequentially; `yarn test`, `git diff --check`, and encoding audit passed.

### Completion Notes

- Replaced component-specific authoring intents with generic semantic entity create, update-properties, and remove contracts carrying canonical context/subject, concept and template identity, Revision Guard, and provenance.
- Added typed removal dependency impact and blocked eligibility with stable `authoring.removal.dependencies` diagnostics; accepted update/removal mutation remains outside this story.
- Added frontend-independent Engineering Concept Template contracts and domain-owned electrical motor template data without representation or geometry fields.
- Kept concept identity distinct from concept-template identity; no `.default` suffix inference is used in platform contracts.
- Moved Revision Guard derivation to the backend LSP boundary; Theia no longer hashes source content for authoring requests.
- AC evidence: AC-1 `SemanticEntityAuthoringContractTest` and generic LSP request tests; AC-2 `EngineeringConceptTemplateContractTest`; AC-3 forbidden public-contract vocabulary and dependency checks plus final source scan; AC-4 `ElectricalEngineeringConceptTemplatesTest`; AC-5 dependency/eligibility contract tests; AC-6 full caller builds and no live old-intent-name matches; AC-7 CodeGraph caller review, M31-CL-004/005 ledger entries, full regressions, diff check, and encoding audit.
- Polish/Purge: removed old intent names from live kernel, extension, IDE, integration, and example code. Retained component-panel template mapping is M31-CL-004 for Story 4.1; retained component-named source planner is M31-CL-005 for Story 2.2. No visual vocabulary remains in concept-template production contracts.

## File List

- `_bmad-output/implementation-artifacts/m31/2-1-generalize-semantic-entity-authoring-contracts.md`
- `_bmad-output/implementation-artifacts/m31/cleanup-ledger.md`
- `_bmad-output/implementation-artifacts/m31/sprint-status.yaml`
- `extensions/domain-electrical/src/main/kotlin/com/engineeringood/athena/domain/electricalruntime/ElectricalEngineeringConceptTemplates.kt`
- `extensions/domain-electrical/src/test/kotlin/com/engineeringood/athena/domain/electricalruntime/ElectricalEngineeringConceptTemplatesTest.kt`
- `ide/lsp/src/main/kotlin/com/engineeringood/athena/ide/lsp/AthenaAuthoringProtocol.kt`
- `ide/lsp/src/main/kotlin/com/engineeringood/athena/ide/lsp/AthenaAuthoringSourceEditProtocol.kt`
- `ide/lsp/src/main/kotlin/com/engineeringood/athena/ide/lsp/AthenaLanguageServer.kt`
- `ide/lsp/src/test/kotlin/com/engineeringood/athena/ide/lsp/AthenaAuthoringRequestTest.kt`
- `ide/theia-frontend/scripts/athena-authoring-protocol.test.mjs`
- `ide/theia-frontend/scripts/athena-m30-final-purge-regression.test.mjs`
- `ide/theia-frontend/scripts/athena-m30-qet-converter-design.test.mjs`
- `ide/theia-frontend/scripts/athena-m30-retrospective-cleanup-ledger.test.mjs`
- `ide/theia-frontend/src/browser/athena-authoring-protocol.ts`
- `ide/theia-frontend/src/browser/athena-component-panel-widget.tsx`
- `ide/theia-frontend/src/browser/athena-semantic-inspection-widget.tsx`
- `ide/theia-product/package.json`
- `kernel/authoring-model/README.md`
- `kernel/authoring-model/README.zh-CN.md`
- `kernel/authoring-model/src/main/kotlin/com/engineeringood/athena/authoring/AuthoringIntentModels.kt`
- `kernel/authoring-model/src/main/kotlin/com/engineeringood/athena/authoring/AuthoringPreviewModels.kt`
- `kernel/authoring-model/src/main/kotlin/com/engineeringood/athena/authoring/InteractionAuthoringMapping.kt`
- `kernel/authoring-model/src/test/kotlin/com/engineeringood/athena/authoring/AuthoringIntentContractTest.kt`
- `kernel/authoring-model/src/test/kotlin/com/engineeringood/athena/authoring/InteractionAuthoringMappingTest.kt`
- `kernel/authoring-model/src/test/kotlin/com/engineeringood/athena/authoring/SemanticEntityAuthoringContractTest.kt`
- `kernel/component-model/src/main/kotlin/com/engineeringood/athena/component/EngineeringConceptTemplates.kt`
- `kernel/component-model/src/test/kotlin/com/engineeringood/athena/component/EngineeringConceptTemplateContractTest.kt`
- `kernel/runtime/src/main/kotlin/com/engineeringood/athena/runtime/AthenaAuthoringSessionRuntimeService.kt`
- `kernel/runtime/src/test/kotlin/com/engineeringood/athena/runtime/AthenaAuthoringSessionRuntimeServiceTest.kt`

## Change Log

- 2026-07-21: Ultimate context engine analysis completed; comprehensive developer guide created.
- 2026-07-21: Implemented generic semantic entity authoring and semantic-only concept templates; completed full verification and mandatory polish/purge review.

## Mandatory Final Polish/Purge Gate

- Review touched and adjacent entity, component, template, electrical domain, runtime, LSP, test, docs, and compatibility paths.
- Remove stale artifacts or ledger them with owner, reason, target milestone, and verification.
- Confirm no component-specific platform contract, visual template field, duplicate Revision Guard, or unowned compatibility path remains.
- Re-run final verification after cleanup.
