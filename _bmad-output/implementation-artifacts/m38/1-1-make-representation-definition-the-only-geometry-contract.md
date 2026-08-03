---
baseline_commit: 1b2e8b7a6854bc186c82f2e796cf8adf9d5efc62
---

# Story 1.1: Make Representation Definition The Only Geometry Contract

Status: done

## Story

As a package author,
I want one intrinsic Element contract,
so that every Element has one understandable geometry and Anchor authority.

## Acceptance Criteria

1. `RepresentationDefinition` directly owns local body geometry, local bounds, geometry-only Anchors,
   hit geometry, label slots, and source/resource trace for native, SVG-backed, and composite Elements.
2. `PresentationAnatomy`, `PresentationAnatomyAuthority`, `RepresentationBodyAuthority`,
   `RepresentationFallbackBehavior`, `compatibilityAnatomy`, and `rendererFallbackAccepted` are removed
   from production contracts and consumers.
3. Anchor geometry contains no Port, signal, direction, compatibility, component, or Connection
   meaning. Those facts remain in Athena source and semantic/binding contracts.
4. Every retained production consumer uses `RepresentationDefinition` directly. No wrapper named
   `IntrinsicRepresentationContract`, parallel geometry model, adapter, alias, deprecated type,
   compatibility default, or milestone-named replacement is added.
5. Native, source-lowered, package-backed, composed, presentation, and LSP paths remain buildable on
   the single contract. Missing geometry or bounds fails with a typed diagnostic; no visual fallback
   is accepted.
6. Obsolete tests and fixtures are rewritten against current authority or deleted when they only
   prove removed behavior.
7. Focused module tests, source-set hygiene, encoding audit, and stale-authority search pass
   sequentially.

## Tasks

- [x] Lock target contract with failing tests (AC: 1-5)
  - [x] Add contract tests proving one valid native definition, one valid SVG-shaped definition, and
        one valid composite definition share the same required fields.
  - [x] Add negative tests for missing body, missing bounds, duplicate Anchors, and semantic fields
        attempting to enter Anchor geometry.
  - [x] Add compile-time or repository assertions that removed authority/fallback names cannot remain.
- [x] Simplify representation model in place (AC: 1-4)
  - [x] Make existing `RepresentationDefinition` canonical without adding a second contract.
  - [x] Remove `anatomy` and `bodyAuthority` branches and defaults.
  - [x] Keep Anchors geometric: stable ID, neutral geometry reference, local point, hit geometry, and
        representation role only.
  - [x] Remove representation policy fallback and transport fields that report fallback acceptance.
  - [x] Delete obsolete anatomy types; retain only independently current value types in cohesive
        role-based files.
- [x] Refactor every definition producer (AC: 1, 2, 5)
  - [x] Update Athena Symbol and Element lowerers to construct the canonical definition directly.
  - [x] Update native library and package-backed descriptor loading without placeholder anatomy.
  - [x] Update composite and professional drawing construction without compatibility shells.
  - [x] Keep package-local SVG hardening for Story 1.2; this story only preserves the current safe
        geometry result through the new contract.
- [x] Refactor every definition consumer (AC: 2-5)
  - [x] Remove authority switches from validation and binding.
  - [x] Update package runtime, presentation policy, presentation model, electrical projection, and
        compiler consumers to use canonical body, bounds, Anchors, labels, and trace.
  - [x] Remove anatomy and fallback fields from LSP payloads and mappings. Do not add a replacement
        protocol model beyond direct mapping of current typed facts.
- [x] Purge stale behavior and evidence (AC: 2, 4, 6)
  - [x] Rewrite tests that construct anatomy or select fallback behavior.
  - [x] Delete tests whose only purpose is preserving legacy/compatibility authority.
  - [x] Remove stale docs, comments, fixtures, and transport snapshots in affected paths.
  - [x] Do not preserve old serialized shapes; Athena is pre-public.
- [x] Verify sequentially (AC: 7)
  - [x] Run focused representation tests first, then each affected Gradle module one command at a
        time.
  - [x] Run source-set hygiene and encoding audits.
  - [x] Run stale-authority search and `git diff --check`.

## Developer Contract

### Target Shape

```text
RepresentationDefinition
  identity and package trace
  local bounds
  local body geometry
  geometry-only Anchors
  label slots
  optional intrinsic composition
```

No engineer-facing syntax changes. No new module. No second renderer. No Java2D. No XML runtime
authority. No M39 placement or M40 routing work.

### Current Failure

Current `RepresentationDefinition` contains both canonical `graphicBody`/`anchors` and legacy
`anatomy`, selected by `RepresentationBodyAuthority`. Canonical definitions must manufacture an empty
`PresentationAnatomy` compatibility shell. Validation, binding, package runtime, presentation, and LSP
still branch on or transport that obsolete authority. `RepresentationPolicy` and multiple runtime
payloads also carry fallback state even though the product must fail closed.

This story removes the fork. It does not rename the fork.

### Non-Negotiable Decisions

- Evolve `RepresentationDefinition` in place.
- Keep `GraphicPrimitiveDocument` as current body geometry unless direct simplification is required;
  do not wrap it in a new universal geometry schema.
- Require usable local bounds at definition validation. Do not synthesize body-center bounds.
- Keep Anchor geometry neutral. Port compatibility belongs to semantic Port/binding facts.
- Delete fallback fields end to end. A boolean fixed to `false` is still stale architecture.
- Update all callers in the same story. Repository must compile at story end.
- Work with existing dirty files; never revert unrelated user changes.

## Current Production Blast Radius

### Representation Ownership

- `kernel/representation-model/.../RepresentationDefinitionModels.kt`: dual body authority, semantic
  fields in Anchors, canonical definition.
- `kernel/representation-model/.../PresentationAnatomy.kt`: legacy visual authority and compatibility
  shell.
- `kernel/representation-model/.../RepresentationContracts.kt`: policy fallback and occurrence
  contracts.
- `kernel/representation-model/.../RepresentationValidation.kt`: branches between graphic Anchors
  and anatomy terminals.
- `kernel/representation-model/.../RepresentationBindingCompiler.kt`: legacy terminal branch.
- `kernel/representation-model/.../NativeRepresentationLibraryLoader.kt`: emits anatomy-backed
  definitions.

### Producers And Consumers

- `kernel/compiler/.../AthenaSymbolSourceLowerer.kt`
- `kernel/compiler/.../AthenaElementSourceLowerer.kt`
- `kernel/compiler/.../AthenaProfessionalDrawingCompiler.kt`
- `kernel/package-runtime/.../PackageBackedRepresentationOccurrenceFactory.kt`
- `kernel/package-runtime/.../PackageBackedRepresentationOccurrenceModels.kt`
- `kernel/package-runtime/.../BindingResolverModels.kt`
- `kernel/package-runtime/.../BindingResolver.kt`
- `kernel/package-runtime/.../BindingEvidencePayloads.kt`
- `kernel/package-runtime/.../BindingEvidencePayloadMapper.kt`
- `kernel/presentation-policy-model/.../ComponentRepresentationComposer.kt`
- `kernel/presentation-model/.../PresentationDrawingComposition.kt`
- `kernel/presentation-model/.../PresentationDocument.kt`
- `extensions/domain-electrical/.../ElectricalEntityCreationProjectionAuthority.kt`
- `ide/lsp/.../AthenaDrawingCompositionPayloads.kt`
- `ide/lsp/.../AthenaPresentationPayloads.kt`
- `ide/lsp/.../AthenaPresentationSessionProtocol.kt`

Search again before editing. Current dirty worktree may have added consumers after story creation.

## Test Contract

Run Gradle commands sequentially:

```powershell
.\gradlew.bat --no-daemon --console=plain :kernel:representation-model:test
.\gradlew.bat --no-daemon --console=plain :kernel:package-runtime:test
.\gradlew.bat --no-daemon --console=plain :kernel:presentation-policy-model:test
.\gradlew.bat --no-daemon --console=plain :kernel:presentation-model:test
.\gradlew.bat --no-daemon --console=plain :kernel:compiler:test
.\gradlew.bat --no-daemon --console=plain :extensions:domain-electrical:test
.\gradlew.bat --no-daemon --console=plain :ide:lsp:test
powershell -ExecutionPolicy Bypass -File .\tools\source-set-hygiene-audit.ps1
powershell -ExecutionPolicy Bypass -File .\tools\encoding-audit.ps1
git diff --check
```

Final stale-authority gate:

```powershell
rg -n "PresentationAnatomy|PresentationAnatomyAuthority|RepresentationBodyAuthority|RepresentationFallbackBehavior|compatibilityAnatomy|rendererFallbackAccepted" kernel extensions ide -g "*.kt" -g "*.ts" -g "*.tsx" -g "!**/build/**"
```

Expected: no production or stale test/document matches. Any intentionally retained unrelated match
requires explicit architecture review; do not weaken the search with exclusions to make it pass.

## References

- [M38 PRD](../../planning-artifacts/prds/prd-Athena-2026-07-31-m38/prd.md#intrinsic-element)
- [M38 architecture](../../planning-artifacts/architecture/architecture-Athena-2026-07-31-m38/ARCHITECTURE-SPINE.md#ad-2---one-intrinsic-element-contract-adopted)
- [Architecture hard replacement](../../planning-artifacts/architecture/architecture-Athena-2026-07-31-m38/ARCHITECTURE-SPINE.md#ad-8---hard-replacement-not-migration-adopted)
- [M38 epics](epics.md#story-11-make-representation-definition-the-only-geometry-contract)
- [Project rules](../../../AGENTS.md#pre-10-architecture-rule)

## Dev Agent Record

### Agent Model Used

GPT-5 Codex

### Debug Log References

- 2026-07-31: Loaded BMad dev-story workflow, sprint status, story file, AGENTS.md rules, and CodeGraph blast radius.
- 2026-07-31: Removed stale authority fields from representation tests, package runtime tests, presentation tests, compiler tests, and LSP-facing paths.
- 2026-07-31: Ran sequential verification for representation-model, package-runtime, presentation-policy-model, presentation-model, compiler, domain-electrical, ide:lsp, hygiene, encoding, stale search, and diff check.

### Completion Notes

- Story context created from current production call paths on 2026-07-31.
- Implementation started on 2026-07-31.
- `RepresentationDefinition` is now the only intrinsic geometry contract; `anatomy`, `bodyAuthority`, fallback behavior, and renderer fallback transport evidence are gone from active contracts.
- Anchor geometry is neutral: ID, geometry ref, primitive ref, point, role, required. Port/signal/direction compatibility remains outside Anchor geometry.
- Obsolete M30 representation demo/proof test fixtures that preserved removed anatomy behavior were deleted.
- Verification passed sequentially:
  `:kernel:representation-model:test`, `:kernel:package-runtime:test`, `:kernel:presentation-policy-model:test`,
  `:kernel:presentation-model:test`, `:kernel:compiler:test`, `:extensions:domain-electrical:test`, `:ide:lsp:test`,
  `tools/source-set-hygiene-audit.ps1`, `tools/encoding-audit.ps1`, stale-authority search, and `git diff --check`.

### File List

- `_bmad-output/implementation-artifacts/m38/1-1-make-representation-definition-the-only-geometry-contract.md`
- `kernel/representation-model/src/main/kotlin/com/engineeringood/athena/representation/RepresentationDefinitionModels.kt`
- `kernel/representation-model/src/main/kotlin/com/engineeringood/athena/representation/RepresentationContracts.kt`
- `kernel/representation-model/src/main/kotlin/com/engineeringood/athena/representation/RepresentationValidation.kt`
- `kernel/representation-model/src/main/kotlin/com/engineeringood/athena/representation/RepresentationBindingCompiler.kt`
- `kernel/representation-model/src/main/kotlin/com/engineeringood/athena/representation/NativeRepresentationLibraryLoader.kt`
- `kernel/representation-model/src/test/kotlin/com/engineeringood/athena/representation/RepresentationDefinitionContractTest.kt`
- `kernel/package-runtime/src/main/kotlin/com/engineeringood/athena/packageruntime/BindingEvidencePayloads.kt`
- `kernel/package-runtime/src/main/kotlin/com/engineeringood/athena/packageruntime/PackageBackedRepresentationOccurrenceModels.kt`
- `kernel/compiler/src/main/kotlin/com/engineeringood/athena/compiler/AthenaSymbolSourceLowerer.kt`
- `kernel/compiler/src/main/kotlin/com/engineeringood/athena/compiler/AthenaElementSourceLowerer.kt`
- `kernel/compiler/src/main/kotlin/com/engineeringood/athena/compiler/AthenaRepresentationPackageSnapshotCompiler.kt`
- `kernel/presentation-model/src/main/kotlin/com/engineeringood/athena/presentation/PresentationDocument.kt`
- `kernel/presentation-policy-model/src/main/kotlin/com/engineeringood/athena/policy/ComponentRepresentationComposer.kt`
- `ide/lsp/src/main/kotlin/com/engineeringood/athena/ide/lsp/AthenaPresentationPayloads.kt`
- `ide/lsp/src/main/kotlin/com/engineeringood/athena/ide/lsp/AthenaPresentationSessionProtocol.kt`
- Deleted stale test fixtures: `kernel/representation-model/src/test/kotlin/com/engineeringood/athena/representation/M30DemoRepresentationBinder.kt`, `M30DemoRepresentationBinderTest.kt`, `M30ControlSheetCompositionProof.kt`, `M30ControlSheetCompositionProofTest.kt`
- Updated affected tests under `kernel/representation-model`, `kernel/package-runtime`, `kernel/presentation-model`, `kernel/presentation-policy-model`, and `kernel/compiler`.

### Change Log

- 2026-07-31: Completed Story 1.1 hard replacement of legacy representation authority with canonical `RepresentationDefinition`.
