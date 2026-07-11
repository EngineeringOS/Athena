---
baseline_commit: 4b09cacc3435a1c902dc5be72ca30a3c596f784e
---

# Story 3.3: Publish The M8 Proof Corpus And Verification Path

Status: done

## Story

As an architecture owner,  
I want Athena to publish repeatable proof artifacts for the unified mutation model,  
so that M8 closes with runnable evidence rather than only design intent.

## FR Traceability

- FR-1: route all meaningful changes through Athena commands
- FR-3: keep graph-originated editing downstream of Athena-owned meaning
- FR-4: refresh accepted mutation state deterministically
- FR-5: produce unified semantic review facts for accepted mutations
- FR-6: reveal accepted changes coherently across source, graph, and semantic SCM
- NFR-1: meaningful changes must route through one Athena-owned mutation path
- NFR-3: the same accepted mutation over the same state yields the same resulting canonical and projection state
- NFR-4: command intents, mutation outcomes, rejection paths, and review facts remain inspectable
- NFR-5: graph-originated and source-originated mutations share one semantic review and history vocabulary

## Acceptance Criteria

1. Given the first semantic mutation path, projection mutation path, and reveal/review path are implemented, when Athena publishes the M8 proof corpus, then the milestone includes examples, runnable verification, or equivalent proof artifacts that demonstrate one mutation authority across source and graph, and the proof remains narrow instead of widening into broad graphical authoring.
2. Given M8 is reviewed as a milestone proof, when architecture and product owners inspect the corpus, then it is clear which flows were proven, which feedback or rejection paths exist, and which broader editing concerns remain deferred, and later milestones can build on the unified mutation model without reopening its core invariants.

## Tasks / Subtasks

- [x] Publish the M8 proof corpus under the milestone-standard examples path. (AC: 1, 2)
  - [x] Add `examples/m8/README.md` and keep the proof fixture aligned with the governed repository already reused from `m4`.
  - [x] State clearly which mutation and reveal flows are proven and which broader authoring concerns remain deferred.
- [x] Publish the M8 usage and milestone-summary reading path. (AC: 1, 2)
  - [x] Add `docs/usages/m8-proof-usage.md` with sequential verification commands and interactive proof steps.
  - [x] Add `_bmad-output/implementation-artifacts/m8/milestone-summary-2026-07-11.md` so the milestone closes with one summary record.
- [x] Publish one runnable verification entry point for the IDE-side proof path. (AC: 1)
  - [x] Add `verify:m8` to `ide/package.json`.
  - [x] Keep the verification path sequential and Windows-safe.
- [x] Verify the published proof path and update milestone tracking. (AC: 1, 2)
  - [x] Run the focused Gradle verification under Java 25.
  - [x] Run the graph adapter and Theia frontend Node suites sequentially.
  - [x] Run `yarn --cwd ide verify:m8` and close Epic 3 in sprint tracking.

## Dev Notes

### Story Intent

- Story `3.3` is publication and proof closure, not another feature story.
- The narrow milestone target remains unchanged: one mutation authority, one real graph semantic edit, one real graph projection edit, and one shared review and reveal path.

### Technical Notes

- The published M8 proof corpus intentionally reuses `examples/m4/open-repository-proof/` instead of creating a mutation-only repository format.
- `verify:m8` stays IDE-side because the repo-local Java activation helper is workstation-specific; the usage guide keeps JVM verification explicit with `cmd /c "call java25 && ..."`.
- The proof corpus now exists in three stable places: `examples/m8/`, `docs/usages/m8-proof-usage.md`, and `_bmad-output/implementation-artifacts/m8/milestone-summary-2026-07-11.md`.

### Testing Requirements

- Verification commands:
  - `cmd /c "call java25 && .\gradlew.bat --no-daemon --console=plain :kernel:runtime:test --tests com.engineeringood.athena.runtime.AthenaGraphCommandIntentServiceTest --tests com.engineeringood.athena.runtime.AthenaRuntimeProjectionSessionTest"`
  - `cmd /c "call java25 && .\gradlew.bat --no-daemon --console=plain :ide:lsp:test --tests com.engineeringood.athena.ide.lsp.AthenaSourceMutationRequestTest --tests com.engineeringood.athena.ide.lsp.AthenaProjectionRequestTest"`
  - `yarn --cwd integrations/graph-glsp test`
  - `yarn --cwd ide/theia-frontend test`
  - `yarn --cwd ide verify:m8`

## Story Completion Status

- Status: done
- Completion note: M8 now closes with a published proof corpus, a repeatable usage guide, a milestone summary, and a working `verify:m8` IDE verification entry point, so the unified mutation milestone is evidenced by runnable artifacts instead of only implementation-story history.

## Dev Agent Record

### Agent Model Used

GPT-5 Codex

### Debug Log References

- `cmd /c "call java25 && .\gradlew.bat --no-daemon --console=plain :kernel:runtime:test --tests com.engineeringood.athena.runtime.AthenaGraphCommandIntentServiceTest --tests com.engineeringood.athena.runtime.AthenaRuntimeProjectionSessionTest"`
- `cmd /c "call java25 && .\gradlew.bat --no-daemon --console=plain :ide:lsp:test --tests com.engineeringood.athena.ide.lsp.AthenaSourceMutationRequestTest --tests com.engineeringood.athena.ide.lsp.AthenaProjectionRequestTest"`
- `yarn --cwd integrations/graph-glsp test`
- `yarn --cwd ide/theia-frontend test`
- `yarn --cwd ide verify:m8`

### Completion Notes List

- Published `examples/m8/README.md` as the milestone-local proof-corpus entry.
- Published `docs/usages/m8-proof-usage.md` with the sequential verification path and interactive proof steps.
- Published `_bmad-output/implementation-artifacts/m8/milestone-summary-2026-07-11.md` as the milestone closure record.
- Added `verify:m8` to `ide/package.json` so the IDE-side proof path is repeatable.
- Updated milestone indexes and sprint tracking so Epic 3 now closes as done.
