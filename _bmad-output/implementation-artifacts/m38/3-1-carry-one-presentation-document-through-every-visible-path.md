---
baseline_commit: 1b2e8b7a6854bc186c82f2e796cf8adf9d5efc62
---

# Story 3.1: Carry One Presentation Document Through Every Visible Path

Status: done

## Story

As an engineer,
I want Theia and SVG export to receive the same compiled drawing,
so that each visible surface shows the same engineering facts.

## Acceptance Criteria

1. `PresentationDocument.connectors` is the only visible connection collection.
2. Required occurrence, endpoint, route, marker, label, appearance, and trace fields remain typed
   and complete across compiler, LSP, Theia protocol, GLSP, and SVG export transport.
3. SVG export serializes the same `PresentationDocument` and performs no independent connection
   lowering.
4. Public `routeFactSnapshot`, graph-edge fallback, raw route-candidate transport, partial
   publication, and duplicate presentation models are deleted from active visible paths.
5. Missing required facts fail before transport instead of becoming nullable frontend state.
6. Focused compiler, serialization, LSP, SVG-export, contract, hygiene, encoding, forbidden-path, and
   `git diff --check` gates pass sequentially.

## Tasks / Subtasks

- [x] Remove public route snapshot transport (AC: 1, 4)
  - [x] Delete `PresentationDocument.routeFactSnapshot` from public document/payload contracts.
  - [x] Keep route candidates compiler-internal only.
  - [x] Remove tests or docs that expect route snapshots in visible documents.
- [x] Make visible transports carry one document shape (AC: 1, 2, 5)
  - [x] Update LSP payload mapping so required connector endpoint, line, label, marker, and trace
        facts are non-null typed payload fields.
  - [x] Update GLSP projection transport to consume `PresentationDocument.connectors`.
  - [x] Update Theia presentation model to reject incomplete connector facts instead of repairing
        them.
- [x] Make SVG export consume the same document (AC: 2, 3, 4)
  - [x] Locate retained SVG export path.
  - [x] Remove any independent connection lowering, graph-edge fallback, or route candidate
        conversion in export.
  - [x] Add/update export tests proving connectors, labels, markers, and trace serialize from the
        same `PresentationDocument`.
- [x] Delete duplicate visible connection models and fallback paths (AC: 1, 4, 5)
  - [x] Remove or rename stale public graph-edge models that duplicate presentation connector truth.
  - [x] Delete nullable/default endpoint fallback, route fallback, and partial publication code in
        active visible paths.
  - [x] Add forbidden search coverage for public route snapshots, raw candidates, fallback edges, and
        independent SVG lowering.
- [x] Verify sequentially (AC: 6)
  - [x] Run focused model/compiler/LSP/SVG export tests.
  - [x] Run affected Gradle modules one command at a time.
  - [x] Run GLSP and Theia builds if touched.
  - [x] Run source-set hygiene, encoding audit, forbidden-path scan, and `git diff --check`.

## Dev Notes

### Architecture Boundary

M38-E3 starts the visible inspection path. Story 3.1 does not change routing quality, placement
quality, label placement quality, or Theia paint style. It removes competing visible document paths.

`PresentationDocument` is the one compiled drawing. Theia, GLSP, LSP, and SVG export may map that
document for transport, but they must not create another drawing authority. Required facts are typed
and fail closed before transport.

No Java2D. No second renderer. No XML authority. No compatibility shim. No route snapshot in public
presentation. No raw route candidate visible path. No graph-edge fallback.

### Previous Story Intelligence

Story 2.3 established:

- `PresentationConnector` carries typed line, label, route intent, bundle, lane, channel, quality,
  marker references, endpoint evidence, and trace.
- `PresentationDocument.connectionMarkers` owns shared junction, crossing, bus-tap, and continuation
  markers.
- `PresentationPublicationValidator` blocks partial marker references, duplicate IDs, missing
  connector trace, and required route/style facts hidden in token maps.
- Theia no longer computes crossing topology from line intersections.
- Connector terminal circles were removed from connector paint; terminal and hit geometry stay on
  placed occurrence Anchors.

Story 3.1 must carry that same document through every visible surface and delete public stale paths.

### Code Signals

Start with CodeGraph or direct source reads for:

- `PresentationDocument`
- `PresentationConnector`
- `PresentationPublicationValidator`
- `AthenaPresentationPayloads`
- `AthenaPresentationSessionProtocol`
- `athena-graph-presentation-model.ts`
- `athena-graph-workbench-model.ts`
- `athena-glsp-projection-source.ts`
- `athena-glsp-projection-adapter.ts`
- SVG export classes or functions discovered by `rg "svg|Svg|routeFactSnapshot|RouteCandidate"`

Expected smells:

- visible/public `routeFactSnapshot`;
- LSP payload fields that still expose route snapshots or accept incomplete connector state;
- GLSP graph edges that duplicate connector truth;
- SVG export lowering connection routes separately from `PresentationDocument`;
- frontend nullable/default endpoint recovery.

### Testing Requirements

Run Gradle sequentially only:

```powershell
.\gradlew.bat --no-daemon --console=plain -q :kernel:presentation-model:test
.\gradlew.bat --no-daemon --console=plain -q :kernel:compiler:test
.\gradlew.bat --no-daemon --console=plain -q :ide:lsp:test
powershell -ExecutionPolicy Bypass -File .\tools\source-set-hygiene-audit.ps1
powershell -ExecutionPolicy Bypass -File .\tools\encoding-audit.ps1
git diff --check
```

If GLSP or Theia code changes:

```powershell
yarn build
```

in each touched frontend package.

Forbidden-path scan:

```powershell
rg -n "routeFactSnapshot|raw route candidate|RouteCandidate.*payload|graph-edge fallback|fallback edge|independent SVG lowering|toSvg.*Route|partial publication|stale connector|endpoint repair|style guessing" kernel extensions ide integrations -g "*.kt" -g "*.ts" -g "*.tsx" -g "!**/build/**" -g "!**/lib/**" -g "!**/src/test/**"
```

Expected production result: no public visible route snapshot, no raw route-candidate transport, no
fallback edge model, no independent SVG connection lowering.

### References

- `_bmad-output/planning-artifacts/prds/prd-Athena-2026-07-31-m38/prd.md`
- `_bmad-output/planning-artifacts/architecture/architecture-Athena-2026-07-31-m38/ARCHITECTURE-SPINE.md`
- `_bmad-output/implementation-artifacts/m38/epics.md`
- `_bmad-output/implementation-artifacts/m38/2-3-publish-complete-connection-facts-atomically.md`
- `AGENTS.md`

## Dev Agent Record

### Agent Model Used

GPT-5 Codex

### Debug Log References

- 2026-07-31: Story created from M38 sprint order, epics, PRD, architecture spine, and Story 2.3
  completion record.
- 2026-07-31: Dev started from sprint order after Story 2.3 reached review.
- 2026-07-31: Removed public route snapshot payload, added direct Presentation Document SVG exporter,
  and stopped SVG crossing inference.

### Completion Notes

- Story context created for BMad dev-story execution.
- `PresentationDocument.routeFactSnapshot` removed from public presentation model and LSP payload.
- Compiler presentation constructors now publish visible connections only through
  `PresentationDocument.connectors`.
- LSP route snapshot payload DTOs and converters deleted; connector payload test now proves typed
  connector facts are the public visible route evidence.
- Added `PresentationDocumentSvgExporter` so SVG export can serialize connectors, labels, markers,
  and trace from the same `PresentationDocument`.
- Removed SVG crossing inference from geometry-derived render paths; explicit marker facts are the
  only visible crossing authority.
- Verification passed sequentially: `:kernel:presentation-model:test`, `:kernel:compiler:test`,
  `:ide:lsp:test`, `:kernel:svg-renderer:test`, forbidden-path scan, source-set hygiene, encoding
  audit, and `git diff --check`.

### File List

- `_bmad-output/implementation-artifacts/m38/3-1-carry-one-presentation-document-through-every-visible-path.md`
- `_bmad-output/implementation-artifacts/m38/sprint-status.yaml`
- `kernel/presentation-model/src/main/kotlin/com/engineeringood/athena/presentation/PresentationDocument.kt`
- `kernel/presentation-model/src/test/kotlin/com/engineeringood/athena/presentation/PresentationModelContractTest.kt`
- `kernel/presentation-model/src/test/kotlin/com/engineeringood/athena/presentation/PresentationRouteAttachmentContractTest.kt`
- `kernel/compiler/src/main/kotlin/com/engineeringood/athena/compiler/PresentationModelDeriver.kt`
- `kernel/compiler/src/main/kotlin/com/engineeringood/athena/compiler/AthenaProfessionalDrawingCompiler.kt`
- `kernel/compiler/src/main/kotlin/com/engineeringood/athena/compiler/AthenaCabinetProjectionCompiler.kt`
- `kernel/compiler/src/main/kotlin/com/engineeringood/athena/compiler/SvgRenderModelDeriver.kt`
- `kernel/compiler/src/test/kotlin/com/engineeringood/athena/compiler/PresentationModelDeriverTest.kt`
- `kernel/compiler/src/test/kotlin/com/engineeringood/athena/compiler/M37ProfessionalDrawingSurfaceTest.kt`
- `kernel/compiler/src/test/kotlin/com/engineeringood/athena/compiler/DedicatedProfessionalDrawingSampleTest.kt`
- `kernel/compiler/src/test/kotlin/com/engineeringood/athena/compiler/AthenaM36DedicatedCabinetSampleTest.kt`
- `kernel/compiler/src/test/kotlin/com/engineeringood/athena/compiler/AthenaM36CabinetEndToEndProofTest.kt`
- `ide/lsp/src/main/kotlin/com/engineeringood/athena/ide/lsp/AthenaPresentationPayloads.kt`
- `ide/lsp/src/main/kotlin/com/engineeringood/athena/ide/lsp/AthenaPresentationSessionProtocol.kt`
- `ide/lsp/src/main/kotlin/com/engineeringood/athena/ide/lsp/AthenaPresentationTracePayloads.kt`
- `ide/lsp/src/test/kotlin/com/engineeringood/athena/ide/lsp/AthenaPresentationConnectorPayloadTest.kt`
- `kernel/svg-renderer/build.gradle.kts`
- `kernel/svg-renderer/src/main/kotlin/com/engineeringood/athena/renderer/svg/SvgRenderer.kt`
- `kernel/svg-renderer/src/main/kotlin/com/engineeringood/athena/renderer/svg/PresentationDocumentSvgExporter.kt`
- `kernel/svg-renderer/src/test/kotlin/com/engineeringood/athena/renderer/svg/PresentationDocumentSvgExporterTest.kt`

### Change Log

- 2026-07-31: Created Story 3.1 context and marked ready for dev.
- 2026-07-31: Started Story 3.1 implementation.
- 2026-07-31: Implemented one public Presentation Document visible path and marked ready for review.
