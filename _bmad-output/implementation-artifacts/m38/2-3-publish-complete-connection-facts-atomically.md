---
baseline_commit: 1b2e8b7a6854bc186c82f2e796cf8adf9d5efc62
---

# Story 2.3: Publish Complete Connection Facts Atomically

Status: done

## Story

As an engineer,
I want joins, crossings, line appearance, and labels published with their Connections,
so that drawing meaning never depends on visual guessing or stale partial state.

## Acceptance Criteria

1. Line appearance and route labels are typed presentation facts, not required facts hidden in
   generic token maps.
2. Junction, no-connect crossing, bus-tap, and continuation markers have one document-level
   identity, point, participants, appearance, and trace.
3. Connectors reference shared marker identities and never infer topology from visual intersection.
4. Terminal marker and hit geometry remain owned by Graphic Occurrence Anchors and are not painted
   again by connectors.
5. Occurrence, connector, marker, label, and trace facts publish as one validated atomic document or
   no current drawing.
6. Partial merge, stale connector retention, generic token maps for required facts, and duplicate
   marker ownership are deleted from active visible publication.
7. Focused document, marker, atomic-publication, failure, affected module, hygiene, encoding,
   forbidden-authority scan, and `git diff --check` gates pass sequentially.

## Tasks / Subtasks

- [x] Make required connection presentation facts typed (AC: 1, 6)
  - [x] Move required connector line appearance out of `tokenOverrides` into typed presentation
        fields.
  - [x] Move route labels needed for visible connection meaning into typed presentation facts.
  - [x] Keep `tokenOverrides` only for optional debug/provenance values or delete it if no current
        use remains.
- [x] Add document-level connection markers (AC: 2, 3, 4)
  - [x] Introduce one cohesive presentation-model contract for junction, no-connect crossing,
        bus-tap, and continuation markers.
  - [x] Carry marker participants, exact point, appearance, and trace as non-null typed facts.
  - [x] Make connectors reference marker IDs instead of deriving topology from intersections.
  - [x] Ensure terminal marker/hit geometry stays on `PresentationGraphicOccurrence`/placed Anchor
        evidence and is not duplicated by connector paint.
- [x] Enforce atomic publication (AC: 5, 6)
  - [x] Add a compiler-side validation gate for occurrence, connector, marker, label, and trace
        completeness before returning a `PresentationDocument`.
  - [x] Fail the drawing with plain diagnostics on partial or conflicting document facts.
  - [x] Remove or block any stale connector retention, partial merge, or best-effort document
        publication path.
- [x] Transport typed facts without frontend inference (AC: 1-6)
  - [x] Update LSP payloads for typed line, label, marker, and connector marker references.
  - [x] Update Theia model payloads to consume typed facts without topology repair.
  - [x] Update GLSP transport if it still publishes visible connection facts.
- [x] Verify sequentially (AC: 7)
  - [x] Add focused presentation-model and compiler tests first.
  - [x] Run affected Gradle modules one command at a time.
  - [x] Run source-set hygiene, encoding audit, active forbidden-authority search, and `git diff --check`.

## Dev Notes

### Current Architecture Boundary

Story 2.3 finishes M38-E2 by making complete connection publication atomic. Story 2.2 already made
endpoint attachment strict. Story 2.3 must not improve placement or routing quality. It only ensures
every visible connection-related fact needed by Theia is typed, complete, shared once, and published
with the same `PresentationDocument` snapshot.

No normal-source drawing language. No `drawing.trust` package. No Java2D. No XML authority. No SVG
engineering facts. No compatibility shim. No stale partial document merge.

### Previous Story Intelligence

Story 2.2 established:

- `PresentationConnector` is the strict visible connection authority.
- `PresentationConnectorCompiler` consumes route candidates as untrusted input.
- Connector first and last route points equal exact placed Anchor endpoint points.
- `PresentationDocument.connectorsForRendering()` returns compiled connectors only.
- Route quality fallback state was replaced by degraded quality.
- Old projection deriver connector route now starts and ends at endpoint Anchor positions.
- Cabinet route line classification can use authored physical route channel evidence when explicit
  connection intent is absent.

Story 2.3 must build on that path, not create a second connector or marker lowering path.

### Current Code Signals

Start with CodeGraph on:

- `PresentationConnector`
- `PresentationDocument`
- `PresentationDrawingComposition`
- `AthenaProfessionalDrawingCompiler`
- `PresentationConnectorCompiler`
- `JunctionCrossingMarkerCompiler`
- `RouteLabelPlacementCompiler`
- `DrawingProfileCompiler`
- `AthenaPresentationPayloads`
- `AthenaPresentationSessionProtocol`
- `athena-graph-presentation-model.ts`
- `athena-graph-workbench-edge-layer.tsx`
- `athena-graph-workbench-model.ts`
- `athena-glsp-projection-source.ts`
- `athena-glsp-projection-adapter.ts`

Expected current smells to resolve:

- required line appearance still lives partly in `PresentationConnector.tokenOverrides`;
- route labels may still be carried as token strings rather than typed presentation facts;
- marker facts may exist in routing/drawing composition but not as one typed document-level
  presentation contract;
- frontend may still infer crossing/labels/styles from connector geometry or string tokens;
- publication may return presentation documents without a single validation gate for all
  occurrence/connector/marker/label/trace facts.

### Implementation Guardrails

- Evolve existing product types in place. Do not create a parallel presentation document model.
- Keep facts human-readable and concrete. Avoid long abstract names where a simple name works.
- Required visible facts must be typed and non-null.
- `tokenOverrides` must not carry required drawing semantics.
- Markers are document-level shared facts; connectors reference marker IDs.
- Theia paints supplied marker facts and labels. It does not detect topology from line intersection.
- Diagnostics name subject, problem, and correction plainly.
- If a retained path cannot publish a complete strict document, fail before publication.

### Testing Requirements

Run Gradle sequentially only:

```powershell
.\gradlew.bat --no-daemon --console=plain :kernel:presentation-model:test
.\gradlew.bat --no-daemon --console=plain :kernel:routing-model:test
.\gradlew.bat --no-daemon --console=plain :kernel:compiler:test
.\gradlew.bat --no-daemon --console=plain :kernel:package-runtime:test
.\gradlew.bat --no-daemon --console=plain :ide:lsp:test
powershell -ExecutionPolicy Bypass -File .\tools\source-set-hygiene-audit.ps1
powershell -ExecutionPolicy Bypass -File .\tools\encoding-audit.ps1
git diff --check
```

Active forbidden search:

```powershell
rg -n "intersection inference|frontend crossing|renderer crossing|style guessing|routeLabels|routeLabelOrigins|presentationClassId|strokeClassId|strokeWeight|strokeStyle|colorToken|partial merge|stale connector|tokenOverrides.*route|tokenOverrides.*stroke" kernel extensions ide -g "*.kt" -g "*.ts" -g "*.tsx" -g "!**/build/**" -g "!**/src/test/**"
```

Expected: no required route line, label, marker, crossing, or stale publication authority hidden in
tokens or frontend inference. Negative tests may mention rejected behavior.

### References

- `_bmad-output/planning-artifacts/prds/prd-Athena-2026-07-31-m38/prd.md`
- `_bmad-output/planning-artifacts/prds/prd-Athena-2026-07-31-m38/addendum.md`
- `_bmad-output/planning-artifacts/architecture/architecture-Athena-2026-07-31-m38/ARCHITECTURE-SPINE.md`
- `_bmad-output/implementation-artifacts/m38/epics.md`
- `_bmad-output/implementation-artifacts/m38/2-1-place-body-and-anchors-with-one-transform.md`
- `_bmad-output/implementation-artifacts/m38/2-2-lower-route-candidates-into-exact-connectors.md`
- `AGENTS.md`

## Dev Agent Record

### Agent Model Used

GPT-5 Codex

### Debug Log References

- 2026-07-31: Story created from M38 epics, PRD, addendum, architecture spine, and Story 2.2 completion record.
- 2026-07-31: Dev started from sprint order after Story 2.2 reached review.
- 2026-07-31: Added typed connector line, label, document marker, and atomic publication validation gates; removed frontend crossing and label inference.

### Completion Notes

- Story context created for BMad dev-story execution.
- Required connection drawing facts now publish as typed connector line, connector label, and document-level marker facts.
- Theia consumes compiler-supplied labels/markers and no longer computes crossing topology from line intersections.
- Connector terminal paint was removed from the connector layer; terminal/hit geometry remains owned by graphic occurrence anchors.
- `PresentationPublicationValidator` blocks partial marker references, duplicate connector/marker ids, missing connector trace, and required route/stroke facts hidden in token maps.
- Verification passed sequentially: `:kernel:presentation-model:test`, `:kernel:routing-model:test`, `:kernel:compiler:test`, `:kernel:package-runtime:test`, `:ide:lsp:test`, GLSP `yarn build`, Theia frontend `yarn build`, source-set hygiene, encoding audit, forbidden-authority scan, and `git diff --check`.

### File List

- `_bmad-output/implementation-artifacts/m38/2-3-publish-complete-connection-facts-atomically.md`
- `kernel/presentation-model/src/main/kotlin/com/engineeringood/athena/presentation/PresentationOccurrenceModels.kt`
- `kernel/presentation-model/src/main/kotlin/com/engineeringood/athena/presentation/PresentationDocument.kt`
- `kernel/presentation-model/src/test/kotlin/com/engineeringood/athena/presentation/PresentationConnectorContractTest.kt`
- `kernel/presentation-model/src/test/kotlin/com/engineeringood/athena/presentation/PresentationModelContractTest.kt`
- `kernel/routing-model/src/main/kotlin/com/engineeringood/athena/routing/DrawingProfileCompiler.kt`
- `kernel/routing-model/src/main/kotlin/com/engineeringood/athena/routing/AthenaRouteEngine.kt`
- `kernel/compiler/src/main/kotlin/com/engineeringood/athena/compiler/PresentationConnectorCompiler.kt`
- `kernel/compiler/src/main/kotlin/com/engineeringood/athena/compiler/PresentationPublicationValidator.kt`
- `kernel/compiler/src/main/kotlin/com/engineeringood/athena/compiler/PresentationModelDeriver.kt`
- `kernel/compiler/src/main/kotlin/com/engineeringood/athena/compiler/AthenaProfessionalDrawingCompiler.kt`
- `kernel/compiler/src/main/kotlin/com/engineeringood/athena/compiler/AthenaProfessionalDrawingModels.kt`
- `kernel/compiler/src/main/kotlin/com/engineeringood/athena/compiler/AthenaCabinetProjectionCompiler.kt`
- `kernel/compiler/src/test/kotlin/com/engineeringood/athena/compiler/PresentationConnectorCompilerTest.kt`
- `kernel/compiler/src/test/kotlin/com/engineeringood/athena/compiler/PresentationPublicationValidatorTest.kt`
- `ide/lsp/src/main/kotlin/com/engineeringood/athena/ide/lsp/AthenaPresentationPayloads.kt`
- `ide/lsp/src/main/kotlin/com/engineeringood/athena/ide/lsp/AthenaPresentationSessionProtocol.kt`
- `ide/lsp/src/test/kotlin/com/engineeringood/athena/ide/lsp/AthenaPresentationRouteFactPayloadTest.kt`
- `integrations/graph-glsp/src/athena-glsp-projection-source.ts`
- `integrations/graph-glsp/src/athena-glsp-projection-adapter.ts`
- `integrations/graph-glsp/lib/athena-glsp-projection-source.d.ts`
- `integrations/graph-glsp/lib/athena-glsp-projection-adapter.js`
- `ide/theia-frontend/src/browser/athena-graph-presentation-model.ts`
- `ide/theia-frontend/src/browser/athena-graph-workbench-model.ts`
- `ide/theia-frontend/src/browser/athena-graph-workbench-edge-layer.tsx`
- `ide/theia-frontend/src/browser/athena-graph-workbench-presentation-node.tsx`

### Change Log

- 2026-07-31: Created Story 2.3 context and marked ready for dev.
- 2026-07-31: Implemented typed atomic connection fact publication and marked ready for review.
