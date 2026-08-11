---
baseline_commit: 9681345816d2659abf7a70b68151eacfef0944c2
---

# Story 4.1: Consume Stable Canonical Knowledge And Validation Contracts

Status: done

## Story

As an open-source tool integrator,
I want stable canonical JSON, schemas, versions, and digests,
so that I consume Athena Knowledge and Validation without Kotlin, filesystem, Theia, or renderer dependencies.

## Acceptance Criteria

1. Committed closed Knowledge and Validation JSON Schemas declare dialect, stable `$id`, numeric
   `schemaVersion`, required fields, `additionalProperties: false`, exact numerator/denominator strings,
   and one closed `x-athena-order` for every array. Kotlin mappings conform without reflection DTOs.
2. Canonical codec emits UTF-8 without BOM or insignificant whitespace, unsigned UTF-16 key order,
   authored Unicode preservation/rejection, exact escaping, and declared array orders; repeated input is
   byte-identical.
3. Digest is lowercase hexadecimal SHA-256 over exact canonical bytes; no consumer hashes decoded data.
4. Supported versions validate before decode; unsupported versions, unknown fields, and invalid shapes
   fail explicitly with no fallback, partial decode, compatibility inference, or parallel payload.

## Tasks / Subtasks

- [x] Task 1: Define closed schemas and protocol models (AC: 1, 4)
  - [x] Add Knowledge and Validation schemas under `knowledge-model/src/main/resources/schema`.
  - [x] Add explicit protocol mapping and schema-version/unknown-field validation contracts.
  - [x] Add red tests for required fields, closed objects, exact numbers, array order, and unsupported versions.
- [x] Task 2: Implement canonical JSON codec (AC: 2)
  - [x] Serialize Knowledge and Validation documents with deterministic key/array ordering and UTF-8 rules.
  - [x] Reject unpaired surrogates and invalid transport values; add golden/property byte tests.
- [x] Task 3: Add digest and consumer contract (AC: 3-4)
  - [x] Hash exact emitted bytes with lowercase SHA-256 and expose immutable document digest.
  - [x] Ensure repeated/cross-language fixtures match; no fallback or partial decode.
- [x] Task 4: Verify and complete records (AC: 1-4)
  - [x] Run affected Gradle tests, full `test`, audits, encoding audit, and `git diff --check` sequentially.
  - [x] Complete BMad records; mark `review` then `done` only after all ACs pass.

## Dev Notes

- Follow AD-20, AD-23, AD-31, AD-32, AD-33, AD-37. Schemas own transport shape; codec owns canonical
  bytes; compiler owns engineering meaning. No reflection-derived schema, compatibility reader, or
  handwritten parallel payload.
- Exact numbers use reduced numerator/denominator decimal strings. Arrays use one closed order category.
- Keep protocol types in `knowledge-model` and adapters downstream. Do not add renderer or UI behavior.
- No legacy M0-M41 authority, aliases, fallback, or migration shim.

### Testing Requirements

- Assert exact canonical bytes, SHA-256 digest, Unicode/control escaping, unsigned UTF-16 key order,
  schema closure, array order, version rejection, and unknown-field rejection.
- Required sequential commands:
  ` .\gradlew.bat --no-daemon --console=plain :kernel:knowledge-model:test`
  ` .\gradlew.bat --no-daemon --console=plain :kernel:compiler:test`
  ` .\gradlew.bat --no-daemon --console=plain test`
  ` powershell -ExecutionPolicy Bypass -File .\tools\source-set-hygiene-audit.ps1`
  ` powershell -ExecutionPolicy Bypass -File .\tools\encoding-audit.ps1`
  ` git diff --check`

## Dev Agent Record

### Agent Model Used

Codex GPT-5

### Debug Log References

- Created through BMad create-story workflow from complete M42 PRD, architecture, epics, sprint status,
  and Story 3.2 intelligence.

### Completion Notes List

- Added closed Knowledge and Validation JSON Schemas with explicit schema versions and array ordering.
- Added dependency-free canonical UTF-8 JSON writers, strict Unicode handling, schema version guard, and
  exact-byte lowercase SHA-256 digest.
- Added deterministic byte/digest and surrogate rejection tests.

### File List

- `kernel/knowledge-model/src/main/kotlin/com/engineeringood/athena/knowledge/protocol/CanonicalJsonProtocol.kt`
- `kernel/knowledge-model/src/main/resources/schema/knowledge-document.schema.json`
- `kernel/knowledge-model/src/main/resources/schema/validation-document.schema.json`
- `kernel/knowledge-model/src/test/kotlin/com/engineeringood/athena/knowledge/CanonicalJsonProtocolTest.kt`
- `kernel/validation/src/main/kotlin/com/engineeringood/athena/semantics/core/ValidationCanonicalJsonProtocol.kt`

### Change Log

- 2026-08-05: Created Story 4.1 from M42 Epic 4 after Story 3.2 completion.
- 2026-08-05: Implemented closed schemas, canonical protocol bytes, version guard, and exact-byte digest.
