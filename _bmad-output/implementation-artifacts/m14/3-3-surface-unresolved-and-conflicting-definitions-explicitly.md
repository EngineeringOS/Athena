---
baseline_commit: 72b498f
---

# Story 3.3: Surface Unresolved And Conflicting Definitions Explicitly

Status: done

## Story

As an architecture owner,  
I want Athena to fail explicitly on unresolved or conflicting component definitions,  
so that semantic truth is not decided silently by pack order or runtime chance.

## FR Traceability

- FR-8: Athena can surface conflicting definitions explicitly

## Acceptance Criteria

1. Given a component reference has no matching concept or vendor implementation, when resolution runs, then Athena surfaces a typed compiler-owned unresolved-definition diagnostic.
2. Given two active packs define the same engineering concept or vendor part incompatibly, when resolution runs, then Athena surfaces a typed compiler-owned conflict diagnostic and does not allow implicit precedence by load order or filesystem order.

## Tasks / Subtasks

- [x] Add a compiler-owned component-resolution diagnostic model. (AC: 1, 2)
  - [x] Publish typed unresolved and conflict diagnostics from `kernel/compiler`.
  - [x] Keep the model above `component-model` and `part-model` without creating a second mutation path.
- [x] Add a deterministic component reference resolver. (AC: 1, 2)
  - [x] Resolve one authored reference through a canonical active catalog.
  - [x] Detect unresolved references explicitly.
  - [x] Detect incompatible concept and vendor implementation duplicates explicitly.
  - [x] Keep identical repeated definitions non-authoritative and non-random.
- [x] Add focused tests and milestone tracking updates. (AC: 1, 2)
  - [x] Verify unresolved diagnostics.
  - [x] Verify concept conflicts do not fall back to pack-order precedence.
  - [x] Verify implementation conflicts do not fall back to pack-order precedence.
  - [x] Verify one governed successful mapping still resolves.

## Story Completion Status

- Status: done
- Completion note: added compiler-owned component-resolution diagnostics and a deterministic resolver, then verified with `java25; .\gradlew.bat :kernel:compiler:test --tests com.engineeringood.athena.compiler.AthenaComponentKnowledgeResolverTest --console=plain --no-daemon`.

## Dev Agent Record

### Agent Model Used

GPT-5 Codex

### Debug Log References

- M14 Story `3.3` component-resolution failure diagnostics
- Sequential Java 25 verification
