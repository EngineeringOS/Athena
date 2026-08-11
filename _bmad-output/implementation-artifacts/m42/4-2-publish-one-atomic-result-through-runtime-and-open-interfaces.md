---
baseline_commit: 9681345816d2659abf7a70b68151eacfef0944c2
---

# Story 4.2: Publish One Atomic Result Through Runtime And Open Interfaces

Status: done

## Story

As a tool integrator,
I want runtime, CLI, LSP, and downstream realities to consume one atomic compilation revision,
so that every product boundary agrees without inventing engineering meaning.

## Acceptance Criteria

1. Runtime publishes immutable Engineering, Knowledge, and Validation snapshots and digests atomically;
   readers never observe mixed revisions. Caches are digest-keyed and observationally equivalent.
2. CLI and LSP query full canonical documents or subject-indexed slices tied to document digests,
   preserving typed values, identity, roles, state, Judgements, corrections, and Provenance.
3. Adapters never resolve definitions, evaluate rules, guess paths, or define handwritten payloads.
4. Existing Projection/Spatial identity, A1/B3 grouping, geometry, and paint boundaries stay green;
   M42 adds no presentation or renderer behavior.

## Tasks / Subtasks

- [x] Task 1: Add atomic compilation revision contract (AC: 1)
  - [x] Add immutable revision containing Engineering/Knowledge/Validation snapshots and exact digests.
  - [x] Add atomic publisher/reader and digest-keyed replaceable cache tests.
- [x] Task 2: Add canonical query slices (AC: 2-3)
  - [x] Expose full document and stable subject-indexed slices from published revision.
  - [x] Ensure runtime, CLI, and LSP consume protocol bytes/queries without local evaluator or DTO copies.
- [x] Task 3: Preserve Projection/Spatial boundaries (AC: 4)
  - [x] Verify current identity/grouping/geometry tests remain green and no knowledge dependency enters
        Projection or Spatial modules.
- [x] Task 4: Verify and complete records (AC: 1-4)
  - [x] Run affected tests, full `test`, audits, encoding audit, and `git diff --check` sequentially.
  - [x] Complete BMad records; mark `review` then `done` only after all ACs pass.

## Dev Notes

- Follow AD-20, AD-21, AD-31, AD-33, AD-34. One compiler owns resolution/evaluation. Runtime only
  publishes immutable results and query slices.
- Reuse canonical protocol bytes/digests from Story 4.1 and Validation contracts from Stories 3.1/3.2.
- No compatibility adapters, fallback readers, copied `EngineeringKnowledgeState`, renderer changes,
  or legacy semantic macro paths.

### Testing Requirements

- Assert atomic snapshot consistency under concurrent readers, digest-keyed cache replacement, stable
  query ordering, full field preservation, and unchanged Projection/Spatial boundaries.
- Required sequential commands:
  ` .\gradlew.bat --no-daemon --console=plain :kernel:runtime:test`
  ` .\gradlew.bat --no-daemon --console=plain :kernel:compiler:test`
  ` .\gradlew.bat --no-daemon --console=plain test`
  ` powershell -ExecutionPolicy Bypass -File .\tools\source-set-hygiene-audit.ps1`
  ` powershell -ExecutionPolicy Bypass -File .\tools\encoding-audit.ps1`
  ` git diff --check`

## Dev Agent Record

### Agent Model Used

Codex GPT-5

### Debug Log References

- Created through BMad create-story workflow using complete M42 PRD, architecture, epics, sprint status,
  and Story 4.1 intelligence.

### Completion Notes List

- Added immutable `AthenaCompilationRevision` carrying Engineering, Knowledge, Validation snapshots and
  lowercase SHA-256 digests.
- Added atomic publisher with digest/revision-keyed cache and stable validation query slices.
- Runtime, compiler, Projection, Spatial, and full repository tests pass sequentially.

### File List

- `kernel/runtime/src/main/kotlin/com/engineeringood/athena/runtime/AthenaCompilationRevision.kt`
- `kernel/runtime/src/test/kotlin/com/engineeringood/athena/runtime/AthenaCompilationRevisionTest.kt`
- `kernel/runtime/build.gradle.kts`

### Change Log

- 2026-08-05: Created Story 4.2 from M42 Epic 4 after Story 4.1 completion.
- 2026-08-05: Implemented atomic runtime revision and query slice contracts; sequential verification passed.
