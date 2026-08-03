---
baseline_commit: 1b2e8b7a6854bc186c82f2e796cf8adf9d5efc62
---

# Story 1.3: Bind Every Port To One Anchor

Status: done

## Story

As an engineering author,
I want each Port explicitly bound to one Element Anchor,
so that connection meaning and connection geometry cannot drift apart.

## Acceptance Criteria

1. Each required engineering Port resolves exactly one explicit binding and exactly one
   geometry-only `RepresentationAnchorContract`.
2. Binding identity and source span survive into compiler input/evidence so later stories can trace
   Connection -> Port -> binding -> Element -> Anchor.
3. Missing Port, missing Anchor, duplicate binding, duplicate Anchor, incompatible binding, and
   unknown geometry reference block compilation with plain corrective diagnostics.
4. Compiler never binds by matching names, SVG nodes, body centers, nearest points, route points, or
   other inferred geometry.
5. No new normal-source keyword for coordinates, transforms, route, paint, label placement, or
   renderer mechanics is introduced.
6. Focused binding, diagnostic, parser-regression, determinism, source-set hygiene, encoding, and
   `git diff --check` gates pass sequentially.

## Tasks / Subtasks

- [x] Lock explicit binding rule with tests (AC: 1, 3, 4)
  - [x] Add focused tests proving each required Port must bind to one existing Anchor.
  - [x] Add negative tests for missing Port, missing Anchor, duplicate binding, duplicate Anchor, and
        incompatible binding.
  - [x] Add regression test proving same names or SVG geometry refs do not create implicit bindings.
- [x] Preserve binding evidence for downstream compiler input (AC: 2)
  - [x] Ensure binding identity, Port identity, Element/definition identity, Anchor identity, and
        source span are present in the retained binding/evidence type.
  - [x] Remove nullable/default placeholder evidence that can hide missing binding truth.
- [x] Keep language and SVG boundaries clean (AC: 4, 5)
  - [x] Do not add syntax for coordinates, transforms, route bends, paint, labels, or renderer work.
  - [x] Keep SVG refs geometry-neutral only; no Port/signal/direction/component/connection facts in
        SVG or Anchor geometry.
- [x] Refactor stale binding paths directly (AC: 1-4)
  - [x] Update `RepresentationBindingCompiler`, `BindingResolver`, package occurrence creation, and
        compiler consumers only where needed.
  - [x] Delete stale fallback/name-match/default-body/nearest-point behavior if found.
  - [x] Avoid new adapters, aliases, deprecated types, or compatibility branches.
- [x] Verify sequentially (AC: 6)
  - [x] Run focused binding tests first.
  - [x] Run affected Gradle modules one command at a time.
  - [x] Run parser regression if syntax files are touched.
  - [x] Run source-set hygiene audit, encoding audit, stale forbidden search, and `git diff --check`.

## Dev Notes

### Current Architecture Boundary

M38 fixes trust, not drawing taste. Engineer writes Port, Element, Connection, and binding meaning.
Compiler joins them. Theia paints facts. No Java2D. No XML runtime authority. No renderer repair. No
normal-source layout/route/paint syntax.

### Previous Story Intelligence

Story 1.1 made `RepresentationDefinition` sole intrinsic geometry contract and removed anatomy/body
authority/fallback evidence. Story 1.2 hardened native/SVG geometry parity and SVG admission. Anchor
geometry is neutral: ID, geometry ref, primitive ref, local point, role, required. Do not put Port,
signal, direction, compatibility, component, or Connection meaning back into Anchor geometry.

### Current Code Signals

CodeGraph found the main blast radius:

- `kernel/representation-model/src/main/kotlin/com/engineeringood/athena/representation/RepresentationBindingCompiler.kt`
- `kernel/package-runtime/src/main/kotlin/com/engineeringood/athena/packageruntime/BindingResolver.kt`
- `kernel/package-runtime/src/main/kotlin/com/engineeringood/athena/packageruntime/PackageBackedRepresentationOccurrenceFactory.kt`
- `kernel/compiler/src/main/kotlin/com/engineeringood/athena/compiler/AthenaRepresentationMaterialBinder.kt`
- `kernel/compiler/src/main/kotlin/com/engineeringood/athena/compiler/AthenaElementSourceLowerer.kt`
- `kernel/compiler/src/main/kotlin/com/engineeringood/athena/compiler/AthenaSymbolSourceLowerer.kt`

Use CodeGraph again before editing named symbols. Worktree is dirty from earlier milestone work; do
not revert unrelated files.

### Implementation Guardrails

- Explicit binding is required. No implicit bind by same name, SVG id, geometry ref, nearest Anchor,
  route endpoint, body center, or default first Anchor.
- Diagnostics must name subject, problem, and correction in plain language.
- Retained evidence must be deterministic: stable IDs and source spans, no nullable placeholders.
- Pre-public rule applies: delete wrong old path, do not preserve it.

### Testing Requirements

Run Gradle sequentially only:

```powershell
.\gradlew.bat --no-daemon --console=plain :kernel:representation-model:test
.\gradlew.bat --no-daemon --console=plain :kernel:package-runtime:test
.\gradlew.bat --no-daemon --console=plain :kernel:compiler:test
.\gradlew.bat --no-daemon --console=plain :ide:lsp:test
powershell -ExecutionPolicy Bypass -File .\tools\source-set-hygiene-audit.ps1
powershell -ExecutionPolicy Bypass -File .\tools\encoding-audit.ps1
git diff --check
```

Forbidden search:

```powershell
rg -n "PresentationAnatomy|PresentationAnatomyAuthority|RepresentationBodyAuthority|RepresentationFallbackBehavior|compatibilityAnatomy|rendererFallbackAccepted|body-center|nearest point|implicit binding|default anchor" kernel extensions ide -g "*.kt" -g "*.ts" -g "*.tsx" -g "!**/build/**"
```

Expected: no active stale authority or fallback behavior. Test names may mention negative cases only
when proving rejection.

### References

- `_bmad-output/planning-artifacts/prds/prd-Athena-2026-07-31-m38/prd.md`
- `_bmad-output/planning-artifacts/prds/prd-Athena-2026-07-31-m38/addendum.md`
- `_bmad-output/planning-artifacts/architecture/architecture-Athena-2026-07-31-m38/ARCHITECTURE-SPINE.md`
- `_bmad-output/implementation-artifacts/m38/epics.md`
- `_bmad-output/implementation-artifacts/m38/1-1-make-representation-definition-the-only-geometry-contract.md`
- `_bmad-output/implementation-artifacts/m38/1-2-compile-native-and-svg-geometry-through-one-contract.md`
- `AGENTS.md`

## Dev Agent Record

### Agent Model Used

GPT-5 Codex

### Debug Log References

- 2026-07-31: Story created from M38 epics, PRD, addendum, architecture spine, and Stories 1.1/1.2 completion records.
- 2026-07-31: Dev started from sprint order after Story 1.2 reached review.
- 2026-07-31: Replaced core occurrence terminal-map binding with explicit `RepresentationPortAnchorBinding` evidence.
- 2026-07-31: Verification passed sequentially: focused `RepresentationBindingCompilerTest`, `:kernel:representation-model:test`, `:kernel:package-runtime:test`, `:kernel:compiler:test`, `:extensions:domain-electrical:test`, `:ide:lsp:test`, source-set hygiene audit, encoding audit, forbidden search, and `git diff --check`.

### Completion Notes

- Required Ports now need one explicit Port-to-Anchor binding; same-name or geometry-name inference does not bind.
- Binding evidence now carries binding ID, semantic Port ID, Anchor ID, and provenance.
- Duplicate binding IDs, duplicate Port bindings, duplicate Anchor bindings, missing Anchors, missing explicit Port bindings, and incompatible bindings fail closed.

### File List

- `_bmad-output/implementation-artifacts/m38/1-3-bind-every-port-to-one-anchor.md`
- `_bmad-output/implementation-artifacts/m38/sprint-status.yaml`
- `extensions/domain-electrical/src/main/kotlin/com/engineeringood/athena/domain/electricalruntime/ElectricalEntityCreationProjectionAuthority.kt`
- `kernel/package-runtime/src/main/kotlin/com/engineeringood/athena/packageruntime/PackageBackedRepresentationOccurrenceFactory.kt`
- `kernel/package-runtime/src/test/kotlin/com/engineeringood/athena/packageruntime/PackageBackedRepresentationOccurrenceFactoryTest.kt`
- `kernel/representation-model/src/main/kotlin/com/engineeringood/athena/representation/RepresentationBindingCompiler.kt`
- `kernel/representation-model/src/main/kotlin/com/engineeringood/athena/representation/RepresentationContracts.kt`
- `kernel/representation-model/src/main/kotlin/com/engineeringood/athena/representation/RepresentationValidation.kt`
- `kernel/representation-model/src/test/kotlin/com/engineeringood/athena/representation/FunctionAwareRepresentationBindingTest.kt`
- `kernel/representation-model/src/test/kotlin/com/engineeringood/athena/representation/RepresentationBindingCompilerTest.kt`
- `kernel/representation-model/src/test/kotlin/com/engineeringood/athena/representation/RepresentationBindingStatusPayloadTest.kt`
- `kernel/representation-model/src/test/kotlin/com/engineeringood/athena/representation/RepresentationDefinitionContractTest.kt`
- `kernel/representation-model/src/test/kotlin/com/engineeringood/athena/representation/RepresentationDiagnosticSerializationTest.kt`
- `kernel/representation-model/src/test/kotlin/com/engineeringood/athena/representation/RepresentationModelContractTest.kt`
- `kernel/compiler/src/test/kotlin/com/engineeringood/athena/compiler/AthenaM34ElementCabinetProofTest.kt`

### Change Log

- 2026-07-31: Created Story 1.3 context and marked ready for dev.
- 2026-07-31: Completed explicit Port-to-Anchor binding refactor and moved story to review.
