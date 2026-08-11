---
baseline_commit: 9681345816d2659abf7a70b68151eacfef0944c2
---

# Story 0.1: Freeze Scene Publication And Command Contracts

Status: done

## Story

As an implementation team,
I want one closed, versioned scene/publication/command contract,
so that Kotlin, LSP, TypeScript, adapters, and proof code cannot invent incompatible DTOs.

## Acceptance Criteria

1. JSON Schema 2020-12 artifacts exist at the exact M43 contract-pack paths with
   `additionalProperties: false`, required non-null fields, and schema version `1`:
   `athena-diagram-scene.schema.json`, `athena-scene-publication.schema.json`,
   `athena-diagram-command.schema.json`, and `svg-safe-1.json`.
2. Kotlin contract models and validators cover the closed scene topology: page, snap grid, styles,
   assets, occurrences, Ports, Labels, Routes, decorations, traces, typed IDs, `inputRevision`,
   `sceneDigest`, canonical ordering, and source provenance. No renderer or device-pixel type enters
   the contract module.
3. Publication states are exactly `READY`, `STALE`, and `UNAVAILABLE`. The publication envelope carries
   attempted and accepted revisions, optional accepted scene/assets, and ordered human-first
   diagnostics without allowing mixed revisions or `null` placeholders.
4. Command contracts cover revision-checked `MoveOccurrence` and `ConnectPorts`, source preconditions,
   idempotency, accepted `WorkspaceEdit` correlation, and rejection reasons `STALE`, `UNAVAILABLE`,
   `INVALID`, `CONFLICT`, `READ_ONLY`, `IO_FAILURE`, and `COMPILATION_FAILURE`.
5. Canonical identity and ordering vectors exist under `contracts/presentation/v1/` for scene, grid,
   commands, trace, assets, render, and benchmark manifests. Cell-4 and cell-8 vectors match the
   M43 A1/micro formula; no fixed 4x4 interpretation is admitted.
6. Rolling-shutter scene and invalid-scene fixtures validate stable IDs, digest omission rules,
   element ordering, trace roles, and fail-closed diagnostics. Reordered equivalent inputs produce
   identical canonical bytes and digest.
7. TypeScript types and Ajv validators are generated into
   `ide/theia-frontend/src/browser/diagram/generated/`; generated files include their source schema
   version/hash and are not hand-edited. `ajv` `8.20.0` is a direct exact frontend dependency.
8. `svg-safe-1` admits only the architecture-listed local geometry elements/attributes and rejects
   scripts, events, CSS/style, animation, filters, `foreignObject`, text/use/image, DTD/entities,
   external URLs, and over-limit payloads. The contract test corpus contains admitted and rejected
   vectors.
9. No old presentation DTO, `athena/projectionSession`, fallback schema reader, compatibility alias,
   or live renderer dependency is introduced by this story. Raw transport replacement belongs to
   Story 0-2.
10. Focused Kotlin and frontend contract tests pass sequentially. Test output proves schema shape,
    canonical ordering/digests, publication-state exclusivity, command rejection envelopes, asset
    safety vectors, generated validator behavior, and exact Ajv dependency resolution.

## Tasks / Subtasks

- [x] Freeze contract module boundaries (AC: 1, 2, 3, 4)
  - [x] Add `:kernel:presentation-model` and `:kernel:interaction-model` only if required by the
        existing module graph; keep schemas and models separate from compiler/runtime adapters.
  - [x] Register new modules in `settings.gradle.kts` and follow existing Kotlin JVM conventions.
  - [x] Define opaque typed IDs and closed data models with constructor validation for non-null,
        positive geometry, enum values, and ownership topology.
  - [x] Reuse the existing canonical JSON approach where compatible; do not create a second canonical
        JSON authority.
- [x] Add normative JSON Schemas and safe-asset profile (AC: 1, 8)
  - [x] Create the four schema resources at the contract-pack paths.
  - [x] Encode exact closed field sets and `x-athena-order` metadata needed by canonical ordering.
  - [x] Add admitted/rejected SVG vectors and enforce byte/decoded limits from `M43-CONTRACT-PACK.md`.
- [x] Add shared conformance corpus (AC: 5, 6, 8)
  - [x] Create `contracts/presentation/v1/manifest.json` and all required sub-manifests.
  - [x] Add cell-4/cell-8 anchors, scene digest/order vectors, command transcripts, trace origins,
        asset safety, render golden metadata, and 100000-element benchmark manifest.
  - [x] Keep fixtures milestone-local and deterministic; no M0-M42 examples.
- [x] Generate frontend contract artifacts (AC: 7)
  - [x] Add exact direct `ajv: "8.20.0"` dependency and lockfile entry.
  - [x] Add a deterministic generation command/script and checked-in generated types/validators.
  - [x] Test schema version/hash drift and reject hand-edited generated output in the build path.
- [x] Author contract tests (AC: 2, 3, 4, 5, 6, 8, 10)
  - [x] Kotlin tests validate model invariants, canonical bytes, digests, topology, and ordering.
  - [x] Frontend Node tests validate generated Ajv schemas, publication exclusivity, commands, and
        SVG safety vectors.
  - [x] Test malformed/unknown fields, missing required fields, `null`, mixed revisions, invalid IDs,
        invalid anchors, duplicate command IDs with different content, and rejected asset constructs.
- [x] Update story records and status (AC: 10)
  - [x] Record exact commands, test results, file list, and contract adoption notes in this story.
  - [x] Mark `0-1` `review` only after all acceptance tests pass; never mark `done` on assumptions.

## Dev Notes

### Architecture Rules

- `AthenaDiagramScene` is the only public Presentation Reality shape.
- Scene geometry is integer logical `SceneUnit`; CSS pixels, Canvas DPR, Konva nodes, DOM, and SVG
  implementation types stay outside kernel contracts.
- IDs are opaque and stable. Geometry, text, z-order, list position, viewport, and revision cannot
  alter a stable semantic ID.
- Canonical scene digest omits only `sceneDigest`; asset/font digests participate through manifests.
- Routes are relationship-owned top-level facts. Ports and Labels are occurrence children.
- Every visible/hittable fact has trace metadata with portable source origins.
- Missing or invalid input is fail-closed. No fallback reader, compatibility alias, or guessed scene.

### Existing Implementation To Adopt

- Reuse `kernel/knowledge-model/.../CanonicalJsonProtocol.kt` patterns only where they satisfy M43
  canonical JSON rules; do not fork a second serializer.
- Existing Sheet Companion parser, placement mapper, and Spatial compiler belong to later stories;
  this story must not duplicate them.
- Existing raw `AthenaProjection*` payloads are explicitly out of scope and remain replacement work
  for Story 0-2.

### Required Paths

- `kernel/presentation-model/src/main/resources/schema/`
- `kernel/presentation-model/src/main/kotlin/com/engineeringood/athena/presentation/`
- `kernel/interaction-model/src/main/resources/schema/`
- `kernel/interaction-model/src/main/kotlin/com/engineeringood/athena/interaction/`
- `contracts/presentation/v1/`
- `ide/theia-frontend/src/browser/diagram/generated/`
- `ide/theia-frontend/scripts/`

### Testing

- Do not run Gradle verification commands concurrently. Run focused module tests one at a time, then
  frontend build/tests one at a time.
- Run `powershell -ExecutionPolicy Bypass -File .\tools\encoding-audit.ps1` after documentation or
  fixture edits.
- Run `powershell -ExecutionPolicy Bypass -File .\tools\source-set-hygiene-audit.ps1` before review.

### References

- [Source: `_bmad-output/planning-artifacts/m43/epics.md` - Epic 0, Story 0-1]
- [Source: `_bmad-output/planning-artifacts/prds/prd-Athena-2026-08-05-m43/prd.md` - FR-7, FR-9, FR-11, FR-12, NFR-1, NFR-2, NFR-4]
- [Source: `_bmad-output/planning-artifacts/architecture/architecture-Athena-2026-08-05-m43/ARCHITECTURE-SPINE.md` - AD-2, AD-4, AD-5, AD-6, AD-7, AD-8, AD-10, AD-12, AD-13, AD-14, AD-16]
- [Source: `_bmad-output/planning-artifacts/architecture/architecture-Athena-2026-08-05-m43/M43-CONTRACT-PACK.md` - Sections 1-11]
- [Source: `_bmad-output/planning-artifacts/architecture/architecture-Athena-2026-08-05-m43/LEGACY-REPLACEMENT.md` - Prior Implementation Disposition]

## Dev Agent Record

### Agent Model Used

Codex GPT-5

### Debug Log References

- Revalidated canonicalization split and publication state invariants on 2026-08-06.

### Completion Notes List

- Closed scene/publication/command models, typed IDs, topology validation, canonical ordering, and digest implemented.
- Normative schemas, SVG safety profile, conformance corpus, generated TypeScript/Ajv validators, and frontend tests implemented.
- No renderer dependency, fallback schema reader, compatibility alias, or raw projection transport added.
- Verification: focused Kotlin tests passed; frontend test passed 8/8; full Gradle test passed (114 actionable tasks); encoding and source-set hygiene audits passed.

### File List

- `settings.gradle.kts`
- `kernel/presentation-model/`
- `kernel/interaction-model/`
- `contracts/presentation/v1/`
- `ide/theia-frontend/package.json`
- `ide/yarn.lock`
- `ide/theia-frontend/scripts/generate-diagram-contracts.mjs`
- `ide/theia-frontend/scripts/athena-diagram-contracts.test.mjs`
- `ide/theia-frontend/src/browser/diagram/generated/`

### Change Log

- 2026-08-06: Implemented M43 Story 0-1 contract pack and generated frontend validators.
- 2026-08-06: Reverified after file split and final M43 regression gates; status set to `done`.
