---
baseline_commit: 72b498f
---

# Story 3.1: Define The Knowledge-Pack Registry And Active Pack Set Contract

Status: done

## Story

As a compiler engineer,  
I want Athena to define an explicit knowledge-pack registry and active pack set,  
so that component resolution is governed by deterministic package-driven inputs rather than ad hoc discovery.

## FR Traceability

- FR-1: resolve authored component references into governed engineering concepts
- FR-7: load component-knowledge packs deterministically through existing package governance
- NFR-3: knowledge-pack loading remains deterministic and constrained by the existing package graph and `athena.lock`

## Acceptance Criteria

1. Given M14 introduces component-knowledge packs, when the registry contract is reviewed, then active packs are defined through explicit package-governed inputs rather than filesystem order or frontend-local discovery.
2. Given the active pack set is recomputed over the same repository graph, when resolution runs again, then the same active knowledge-pack set is produced deterministically.

## Tasks / Subtasks

- [x] Define explicit registry and active-set contracts in the existing compiler knowledge layer. (AC: 1, 2)
  - [x] Publish `AthenaKnowledgePackRegistryEntry`, `AthenaKnowledgePackRegistry`, and `AthenaActiveKnowledgePackSet`.
  - [x] Keep the contract package-governed through `PackageIdentifier`.
  - [x] Keep all public/core Kotlin types documented with clean KDoc.
- [x] Extend the current compiler package-source contract without inventing a new lock path. (AC: 1)
  - [x] Extend `AthenaKnowledgePackageSource` with an optional explicit active-pack-set reference.
  - [x] Add a companion constructor that derives package roots from the active set deterministically.
- [x] Add focused contract tests and milestone tracking updates. (AC: 1, 2)
  - [x] Add tests proving deterministic registry ordering.
  - [x] Add tests proving package roots can be driven from explicit package-governed entries.
  - [x] Run Gradle verification sequentially on Windows with Java 25; do not run build or test tasks concurrently.

## Story Completion Status

- Status: done
- Completion note: extended the compiler knowledge model with deterministic registry and active-pack-set contracts, then verified with `java25; .\gradlew.bat :kernel:compiler:test --tests com.engineeringood.athena.compiler.AthenaKnowledgeResolutionModelContractTest --console=plain --no-daemon`.

## Dev Agent Record

### Agent Model Used

GPT-5 Codex

### Debug Log References

- M14 Story `3.1` registry contract implementation
- Sequential Java 25 verification
