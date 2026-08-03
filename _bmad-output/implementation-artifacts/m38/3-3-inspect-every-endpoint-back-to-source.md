---
baseline_commit: 1b2e8b7a6854bc186c82f2e796cf8adf9d5efc62
---

# Story 3.3: Inspect Every Endpoint Back To Source

Status: done

## Story

As an engineer or AI agent,
I want to inspect a visible endpoint and reach its Athena declaration,
so that every connection can be explained and corrected from source.

## Acceptance Criteria

1. Endpoint trace identifies Connection, Port, binding, Element, Anchor, Graphic Occurrence, placed
   point, route endpoint, package resource, and Athena source span.
2. Trace identities agree with the current atomic `PresentationDocument` snapshot.
3. Missing or conflicting trace blocks publication with a plain diagnostic naming subject, problem,
   and source correction.
4. Internal diagnostic codes remain protocol detail, not user-facing jargon.
5. No DOM, SVG semantic metadata, graph inference, or renderer state creates trace.
6. Focused compiler, LSP, selection, inspection, stale-snapshot, negative tests, audits, and
   forbidden scans pass.

## Tasks / Subtasks

- [x] Audit current endpoint trace payload (AC: 1, 2, 5)
  - [x] Inspect connector endpoint, placed Anchor, graphic occurrence, and LSP trace payload fields.
  - [x] Identify missing trace links from endpoint to source span/package resource.
- [x] Complete compiler/LSP trace transport (AC: 1, 2, 3)
  - [x] Ensure each connector endpoint carries source provenance and projection IDs needed for
        source inspection.
  - [x] Ensure LSP transports endpoint, connector, marker, and occurrence trace without DOM/SVG
        inference.
  - [x] Fail publication on missing endpoint trace evidence.
- [x] Complete Theia inspection path (AC: 1, 2, 4, 5)
  - [x] Resolve visible endpoint selection from current workbench edge and typed connector endpoint.
  - [x] Return source target data from trace/provenance, not renderer state.
  - [x] Show failed inspection state for stale/missing endpoint trace.
- [x] Add focused tests (AC: 1-6)
  - [x] Add compiler/LSP negative tests for missing endpoint trace.
  - [x] Add frontend tests for endpoint inspection and stale/missing trace failure.
  - [x] Add forbidden assertions against DOM/SVG inference.
- [x] Verify sequentially (AC: 6)
  - [x] Run affected Gradle tests one command at a time.
  - [x] Run Theia frontend test/build if touched.
  - [x] Run hygiene, encoding, forbidden scan, and `git diff --check`.

## Dev Notes

Story 3.3 completes M38-E3. It is not graphical editing. It only makes visible endpoint inspection
source-traceable from the current atomic presentation snapshot.

No Java2D. No DOM text parsing. No SVG semantic metadata. No renderer-derived trace. No compatibility
fallback.

Start with:

- `PresentationConnectorEndpoint`
- `PresentationTracePayload`
- `PresentationPublicationValidator`
- `AthenaPresentationSessionProtocol`
- `athena-graph-workbench-model.ts`
- frontend semantic inspection helpers

## Dev Agent Record

### Agent Model Used

GPT-5 Codex

### Debug Log References

- 2026-07-31: Story created from M38 sprint order after Story 3.2 reached review.
- 2026-07-31: Dev started from sprint order.
- 2026-07-31: Audited connector endpoint trace payload through presentation model, LSP payload,
  graph-glsp TS protocol, and Theia workbench inspection path.
- 2026-07-31: Added endpoint inspection from typed `PresentationDocument` connector data only.

### Completion Notes

- Story context created for BMad dev-story execution.
- Endpoint trace now exposes source provenance plus port, binding, occurrence, and anchor projection
  IDs through LSP and GLSP protocol types.
- Theia rejects malformed presentation connector endpoints with missing compiler trace and surfaces a
  failed drawing state instead of inferring from DOM, SVG, or renderer state.
- Theia endpoint inspection resolves source/target endpoint data from the current workbench edge and
  typed presentation connector endpoint.
- Verification passed: graph-glsp `yarn test`, Theia `yarn test`, focused and full compiler/LSP
  Gradle tests, source-set hygiene, encoding audit, forbidden scan, and `git diff --check`.

### File List

- `_bmad-output/implementation-artifacts/m38/3-3-inspect-every-endpoint-back-to-source.md`
- `_bmad-output/implementation-artifacts/m38/sprint-status.yaml`
- `integrations/graph-glsp/src/athena-glsp-projection-source.ts`
- `integrations/graph-glsp/lib/athena-glsp-projection-source.d.ts`
- `integrations/graph-glsp/lib/athena-glsp-projection-source.d.ts.map`
- `ide/lsp/src/main/kotlin/com/engineeringood/athena/ide/lsp/AthenaPresentationSessionProtocol.kt`
- `ide/lsp/src/test/kotlin/com/engineeringood/athena/ide/lsp/AthenaPresentationConnectorPayloadTest.kt`
- `ide/theia-frontend/src/browser/athena-graph-presentation-model.ts`
- `ide/theia-frontend/src/browser/athena-graph-workbench-model.ts`
- `ide/theia-frontend/src/browser/athena-graph-workbench-widget.tsx`
- `ide/theia-frontend/scripts/athena-graph-workbench-model.test.mjs`
- `kernel/compiler/src/main/kotlin/com/engineeringood/athena/compiler/PresentationPublicationValidator.kt`
- `kernel/compiler/src/test/kotlin/com/engineeringood/athena/compiler/PresentationPublicationValidatorTest.kt`

### Change Log

- 2026-07-31: Created Story 3.3 context and marked ready for dev.
- 2026-07-31: Started Story 3.3 implementation.
- 2026-07-31: Implemented endpoint source inspection and marked ready for review.
