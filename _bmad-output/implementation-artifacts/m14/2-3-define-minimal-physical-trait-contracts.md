---
baseline_commit: 72b498f
---

# Story 2.3: Define Minimal Physical-Trait Contracts

Status: done

## Story

As a platform engineer,  
I want Athena to define minimal physical-trait contracts,  
so that later layout, projection, and presentation layers can consume reusable physical meaning without making M14 a geometry milestone.

## FR Traceability

- FR-2: publish component knowledge as new kernel contracts
- FR-5: define minimal physical-trait contracts
- NFR-6: physical traits remain minimal and do not become a geometry engine
- NFR-7: M14 output remains read-only resolved knowledge consumed by later layers

## Acceptance Criteria

1. Given the first physical-model contract is reviewed, when its scope is inspected, then the model covers width, height, depth, mounting type, and basic installation markers only.
2. Given physical traits are compared with layout or geometry layers, when ownership is reviewed, then physical traits remain reusable component knowledge and do not replace layout or scene-calculation ownership.

## Tasks / Subtasks

- [x] Define the first physical-trait contract in a dedicated kernel module. (AC: 1, 2)
  - [x] Introduce `kernel/physical-model` as the home for minimal physical-trait knowledge contracts.
  - [x] Publish minimal size, mounting-type, and installation-marker contracts.
  - [x] Keep all public/core Kotlin types documented with clean KDoc.
- [x] Freeze the ownership boundary between physical knowledge and downstream layout/geometry. (AC: 2)
  - [x] Make the code comments explicit that the model does not own layout placement, bounds, or scene calculation.
  - [x] Keep resolved physical traits read-only and anchored to canonical semantic identity.
- [x] Add focused tests and module documentation. (AC: 1, 2)
  - [x] Add tests proving the contract remains minimal.
  - [x] Add tests proving resolved physical traits remain semantic and reusable rather than geometric.
  - [x] Add module README files in English and Chinese.
  - [x] Run Gradle verification sequentially on Windows with Java 25; do not run build or test tasks concurrently.

## Story Completion Status

- Status: done
- Completion note: implemented `:kernel:physical-model`, added focused contract tests, and verified with `java25; .\gradlew.bat :kernel:physical-model:test --console=plain --no-daemon`.

## Dev Agent Record

### Agent Model Used

GPT-5 Codex

### Debug Log References

- M14 Story `2.3` physical-trait contract implementation
- Sequential Java 25 verification
