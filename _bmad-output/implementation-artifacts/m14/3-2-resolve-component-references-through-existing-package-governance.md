---
baseline_commit: 72b498f
---

# Story 3.2: Resolve Component References Through Existing Package Governance

Status: done

## Story

As a compiler engineer,  
I want Athena to resolve authored component references through the existing package graph and hosted extension seams,  
so that M14 reuses M5 governance instead of inventing a parallel dependency model.

## FR Traceability

- FR-1: resolve authored component references into governed engineering concepts
- FR-7: load component-knowledge packs deterministically through existing package governance
- NFR-3: knowledge-pack loading remains deterministic and constrained by the existing package graph and `athena.lock`

## Acceptance Criteria

1. Given a repository has a locked package graph, when component resolution runs, then only packs available through that graph may contribute definitions.
2. Given M14 is reviewed for reproducibility, when package governance is inspected, then `athena.lock` remains the reproducibility anchor and no second canonical lockfile is introduced.

## Tasks / Subtasks

- [x] Add a governed source builder above the explicit registry contract. (AC: 1, 2)
  - [x] Build active knowledge-pack sources from `ResolvedPackageGraph`.
  - [x] Filter registry entries by package identities admitted through the graph.
  - [x] Keep the root-package match explicit.
- [x] Preserve the existing reproducibility anchor. (AC: 2)
  - [x] Make the builder comments explicit that `athena.lock` and the resolved graph remain the anchor.
  - [x] Avoid introducing a second lock or parallel dependency contract.
- [x] Add focused tests and milestone tracking updates. (AC: 1, 2)
  - [x] Add tests proving only graph-admitted packs become active.
  - [x] Add tests proving mismatched root governance returns an empty source.
  - [x] Run Gradle verification sequentially on Windows with Java 25; do not run build or test tasks concurrently.

## Story Completion Status

- Status: done
- Completion note: added a governed knowledge-package source builder over `ResolvedPackageGraph`, then verified with `java25; .\gradlew.bat :kernel:compiler:test --tests com.engineeringood.athena.compiler.AthenaGovernedKnowledgePackageSourceBuilderTest --console=plain --no-daemon`.

## Dev Agent Record

### Agent Model Used

GPT-5 Codex

### Debug Log References

- M14 Story `3.2` governed source builder implementation
- Sequential Java 25 verification
