---
baseline_commit: 1b2e8b7a6854bc186c82f2e796cf8adf9d5efc62
---

# Story 1.2: Compile Native And SVG Geometry Through One Contract

Status: done

## Story

As a package author,
I want simple native geometry and complex package-local SVG geometry compiled identically,
so that geometry choice does not change engineering authority.

## Acceptance Criteria

1. Native `symbol` / `element` geometry and SVG-backed `graphic svg resource` geometry produce the
   same canonical `RepresentationDefinition` contract: local bounds, `GraphicPrimitiveDocument`,
   geometry-only Anchors, label slots, and source/resource trace.
2. SVG admission allows only safe SVG geometry and neutral `data-athena-ref="anchor:<id>"` geometry
   identities. SVG does not define Port, signal, direction, role, component, connection, or
   compatibility facts.
3. SVG scripts, event handlers, external resources, path escape, absolute paths, remote URI resource
   paths, unsafe entities, duplicate IDs/refs, unknown `data-athena-*`, and engineering metadata fail
   closed with typed diagnostics.
4. No SVG DOM, raw markup, XML authority, remote resource scheme, resource-specific geometry model,
   fallback body, or adapter contract crosses the representation boundary.
5. Package-local resource resolution remains the only active scheme. Remote resources are recorded as
   deferred handoff only, not implemented.
6. Focused native/SVG/security/package-resource tests, affected module tests, stale-authority search,
   source-set hygiene, encoding audit, and `git diff --check` pass sequentially.

## Tasks / Subtasks

- [x] Lock native/SVG equivalence with tests (AC: 1, 4)
  - [x] Add or update tests proving one native element and one SVG-backed element compile to matching
        `RepresentationDefinition` fields without a second contract.
  - [x] Assert both paths preserve bounds, primitives, label slots, anchors, and provenance sources.
  - [x] Assert no `PresentationAnatomy`, body authority, fallback, raw SVG, XML, or DOM payload leaks.
- [x] Harden SVG admission and geometry refs (AC: 2, 3)
  - [x] Verify `data-athena-ref` accepts only neutral geometry reference values.
  - [x] Reject unknown `data-athena-*` attributes and semantic-looking values such as `port:*`,
        `signal:*`, `direction:*`, `component:*`, and `connection:*`.
  - [x] Keep script/event/external URL/entity/namespace/duplicate-ref diagnostics deterministic.
- [x] Keep package-local resource resolution strict (AC: 3, 5)
  - [x] Preserve relative package-local SVG paths only.
  - [x] Reject absolute paths, `..` escape, missing files, and remote URIs before SVG admission.
  - [x] Add handoff note for future trusted remote resource URI support under M39/M40 or package
        trust milestone; do not implement runtime network access.
- [x] Refactor producers without new abstractions (AC: 1, 4)
  - [x] Update `AthenaSymbolSourceLowerer`, `AthenaElementSourceLowerer`, and
        `AthenaSvgGraphicBodyCompiler` only where needed.
  - [x] Keep `RepresentationDefinition` as the only intrinsic contract.
  - [x] Delete stale fixtures/docs/tests that preserve old resource-specific behavior.
- [x] Verify sequentially (AC: 6)
  - [x] Run focused compiler and representation tests first.
  - [x] Run affected Gradle modules one command at a time.
  - [x] Run stale-authority search, source-set hygiene audit, encoding audit, and `git diff --check`.

## Dev Notes

### Current Architecture Boundary

M38 keeps Athena source human-first and K.I.S.S. No normal-source syntax for layout, route bends,
paint, labels, transforms, renderer behavior, or compiler IR. SVG is geometry-only; Athena source
owns engineering metadata. Theia is the render layer. No Java2D. No second renderer. No XML runtime
authority. No compatibility shims.

### Previous Story Intelligence

Story 1.1 completed the hard replacement:

- `RepresentationDefinition` is the only intrinsic geometry contract.
- Removed stale `PresentationAnatomy`, body authority, representation fallback, and renderer fallback
  evidence from active contracts.
- Anchor geometry is neutral: ID, geometry ref, primitive ref, point, role, required.
- Port/signal/direction compatibility must not be added back to Anchor geometry.
- Tests now include repository assertions for removed authority/fallback names.

Do not reintroduce any wrapper named `IntrinsicRepresentationContract`, adapter, alias, deprecated
type, or serialized compatibility shape.

### Files Most Likely To Touch

- `kernel/compiler/src/main/kotlin/com/engineeringood/athena/compiler/AthenaSvgGraphicBodySupport.kt`
- `kernel/compiler/src/main/kotlin/com/engineeringood/athena/compiler/AthenaSymbolSourceLowerer.kt`
- `kernel/compiler/src/main/kotlin/com/engineeringood/athena/compiler/AthenaElementSourceLowerer.kt`
- `kernel/compiler/src/test/kotlin/com/engineeringood/athena/compiler/AthenaReferencedSvgGraphicCompilerTest.kt`
- `kernel/compiler/src/test/kotlin/com/engineeringood/athena/compiler/AthenaSymbolSourceCompilerTest.kt`
- `kernel/compiler/src/test/kotlin/com/engineeringood/athena/compiler/AthenaElementSourceCompilerTest.kt`
- `kernel/representation-model/src/test/kotlin/com/engineeringood/athena/representation/RepresentationDefinitionContractTest.kt`

Use CodeGraph before reading or editing named symbols.

### Existing Signals

Current code already has:

- `AthenaSvgGraphicBodyCompiler.compile(...)` resolving package-local SVG paths.
- SVG lint tests for script, event handler, namespace, unsafe URL, malformed transform, unsafe root
  attributes, duplicate IDs, and `data-athena-ref`.
- Native/SVG parity coverage in `AthenaReferencedSvgGraphicCompilerTest`.
- Source lowerers constructing `RepresentationDefinition` directly after Story 1.1.

This story should tighten and finish the path, not create a new model.

### Testing Requirements

Run Gradle sequentially only:

```powershell
.\gradlew.bat --no-daemon --console=plain :kernel:representation-model:test
.\gradlew.bat --no-daemon --console=plain :kernel:compiler:test
.\gradlew.bat --no-daemon --console=plain :kernel:package-runtime:test
powershell -ExecutionPolicy Bypass -File .\tools\source-set-hygiene-audit.ps1
powershell -ExecutionPolicy Bypass -File .\tools\encoding-audit.ps1
git diff --check
```

Final stale-authority gate:

```powershell
rg -n "PresentationAnatomy|PresentationAnatomyAuthority|RepresentationBodyAuthority|RepresentationFallbackBehavior|compatibilityAnatomy|rendererFallbackAccepted" kernel extensions ide -g "*.kt" -g "*.ts" -g "*.tsx" -g "!**/build/**"
```

Expected: no matches.

### References

- `_bmad-output/planning-artifacts/prds/prd-Athena-2026-07-31-m38/prd.md`
- `_bmad-output/planning-artifacts/prds/prd-Athena-2026-07-31-m38/addendum.md`
- `_bmad-output/planning-artifacts/architecture/architecture-Athena-2026-07-31-m38/ARCHITECTURE-SPINE.md`
- `_bmad-output/implementation-artifacts/m38/epics.md`
- `_bmad-output/implementation-artifacts/m38/1-1-make-representation-definition-the-only-geometry-contract.md`
- `AGENTS.md`

## Dev Agent Record

### Agent Model Used

GPT-5 Codex

### Debug Log References

- 2026-07-31: Created from M38 epics, PRD, architecture spine, addendum, review rubric, and Story 1.1 completion record.
- 2026-07-31: Added focused SVG admission tests for semantic-looking `data-athena-ref` values, forbidden `data-athena-*` engineering metadata, and remote/absolute resource rejection.
- 2026-07-31: Verification passed sequentially: `:kernel:representation-model:test`, focused `AthenaReferencedSvgGraphicCompilerTest`, `:kernel:compiler:test`, `:kernel:package-runtime:test`, source-set hygiene audit, encoding audit, stale-authority search, and `git diff --check`.

### Completion Notes

- Native and SVG-backed geometry now stay on one `RepresentationDefinition` contract in tests; no body authority or fallback evidence is accepted.
- SVG admission remains geometry-only. Neutral `anchor:<id>` refs pass; port/signal/direction/component/connection metadata fails closed.
- Package-local resource resolution remains only active scheme. Remote URI support stays deferred handoff, not runtime behavior.

### File List

- `_bmad-output/implementation-artifacts/m38/1-2-compile-native-and-svg-geometry-through-one-contract.md`
- `_bmad-output/implementation-artifacts/m38/sprint-status.yaml`
- `kernel/compiler/src/test/kotlin/com/engineeringood/athena/compiler/AthenaReferencedSvgGraphicCompilerTest.kt`
