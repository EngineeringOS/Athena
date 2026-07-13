---
baseline_commit: 72b498f
---

# Story 5.1: Publish The First M14 Electrical Proof Corpus

Status: done

## Story

As a product owner,  
I want Athena to ship one narrow electrical proof corpus for component knowledge resolution,  
so that M14 can be verified over real authored repositories instead of unit tests alone.

## FR Traceability

- FR-8: Athena can surface conflicting definitions explicitly
- FR-10: Athena preserves M8 semantic mutation as the only write authority

## Acceptance Criteria

1. Given the first M14 proof corpus is published, when it is reviewed, then it exercises the targeted electrical proof families and Siemens-first vendor mappings.
2. Given the proof corpus is used during verification, when component resolution runs, then the resolved outputs remain inspectable and reproducible.

## Tasks / Subtasks

- [x] Publish a real repository-backed M14 proof corpus under `examples/m14`. (AC: 1, 2)
  - [x] Add a governed repository root with `athena.yaml`, `athena.lock`, and authored `.athena` source.
  - [x] Ensure the corpus exercises the narrow electrical proof families and Siemens-first vendor mappings already supported by M14.
- [x] Add a focused proof-corpus verification test. (AC: 2)
  - [x] Validate the repository contract and canonical lock.
  - [x] Inspect the component knowledge snapshot through runtime or compiler seams.
  - [x] Verify repeated runs remain deterministic.
- [x] Update milestone records. (AC: 1, 2)
  - [x] Record the corpus location and intended proof coverage.
  - [x] Record the verification command.

## Story Completion Status

- Status: done
- Completion note: published the first real governed repository-backed M14 proof corpus at `examples/m14/siemens-proof-corpus`, added a runtime proof-corpus test, and verified with `java25; .\gradlew.bat :kernel:runtime:test --tests com.engineeringood.athena.runtime.AthenaM14ProofCorpusTest --console=plain --no-daemon`.

## Dev Agent Record

### Agent Model Used

GPT-5 Codex
