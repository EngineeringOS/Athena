---
baseline_commit: 0f4912953312065542f67eec12f3c1fc4133f61c
---

# Story 5.3: Publish The Deterministic Verification Path For M16

Status: done

## Story

As a platform owner,
I want Athena to publish one deterministic verification path for semantic reuse,
so that M16 closes with repository-backed proof instead of narrative claims.

## FR Traceability

- FR-12: Athena publishes a deterministic repository-backed M16 verification path
- NFR-4: Preview, acceptance, and traceability outputs stay deterministic across reruns

## Acceptance Criteria

1. Given the M16 proof path is reviewed, when the scenario is inspected, then it covers open repository -> choose `DOL Starter` -> configure parameters -> preview expansion -> accept -> verify source, graph, inspection, review, and origin coherence.
2. Given the same proof repository and parameter set are reused, when the verification path is rerun, then the same preview, accepted structure, and traceability outputs are produced deterministically.

## Tasks / Subtasks

- [x] Publish the M16 usage and verification path. (AC: 1, 2)
  - [x] Added `docs/usages/m16-proof-usage.md` with the repository path, proof flow, and verification commands.
  - [x] Pointed desktop smoke verification at the checked-in `examples/m16/semantic-reuse-proof` repository.
- [x] Add deterministic repository rerun tests. (AC: 2)
  - [x] Added runtime proof assertions that compare two fresh reruns of the same DOL acceptance flow.
  - [x] Verified the desktop E2E against the checked-in proof repo.

## Implementation Notes

- The desktop smoke no longer depends on a transient generated repo; it now opens the checked-in M16 proof repository.
- `AthenaM16ProofSliceTest` compares repeated preview, acceptance, diff inspection, semantic review, and origin inspection results from the same repository and parameter set.
- The usage note keeps the proof path short and executable so M16 closes with evidence instead of prose alone.

## Verification

- `.\gradlew.bat --no-daemon --console=plain :kernel:runtime:test --tests *M16Proof* --tests *origin*`
- `.\gradlew.bat --no-daemon --console=plain :ide:lsp:test --tests *origin*`
- `yarn workspace @engineeringood/athena-theia-frontend test`
- `yarn build`
- `yarn start:smoke:reuse-catalog`
- `powershell -ExecutionPolicy Bypass -File .\tools\encoding-audit.ps1`

## References

- [Source: _bmad-output/planning-artifacts/epics-M16-2026-07-14.md]
- [Source: docs/usages/m16-proof-usage.md]
- [Source: examples/m16/semantic-reuse-proof]
- [Source: kernel/runtime/src/test/kotlin/com/engineeringood/athena/runtime/AthenaM16ProofSliceTest.kt]
- [Source: ide/theia-product/scripts/verify-athena-reuse-catalog.js]

## Story Completion Status

- Status: done
- Completion note: M16 now has a checked-in proof repo, deterministic rerun tests, and a published verification path that covers preview, acceptance, inspection, review, and origin coherence.
