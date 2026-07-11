---
baseline_commit: d8e1c6163b7edd8895e8b8fe182519f0fbf54b53
---

# Story 3.1: Feed Accepted Mutation Outcomes Into One Semantic Review Model

Status: done

## Story

As a reviewer,  
I want accepted mutations from either source or graph to produce the same semantic review facts,  
so that review meaning stays semantic rather than splitting by interaction origin.

## FR Traceability

- FR-1: route all meaningful changes through Athena commands
- FR-2: classify meaningful changes explicitly
- FR-5: produce unified semantic review facts for accepted mutations
- FR-8: preserve renderer-neutral mutation semantics
- NFR-1: meaningful changes must route through one Athena-owned mutation path
- NFR-2: canonical engineering meaning remains upstream of any renderer or editor client
- NFR-4: command intents, mutation outcomes, rejection paths, and review facts remain inspectable
- NFR-5: graph-originated and source-originated mutations must share one semantic review and history vocabulary

## Acceptance Criteria

1. Given an accepted mutation originated from source or graph, when Athena computes change consequences, then semantic diff, review summary, and history vocabulary are produced through the existing M6 semantic review path, and graph-originated changes do not invent a renderer-specific review language.
2. Given review outputs are inspected for mixed interaction origins, when review semantics are compared, then the same engineering change yields compatible review facts regardless of whether the initiating surface was source or graph, and `kernel/semantic-scm` remains the downstream review/history authority.

## Tasks / Subtasks

- [x] Publish one runtime-owned accepted-mutation review artifact above M6 semantic SCM services. (AC: 1, 2)
  - [x] Add a typed runtime model that keeps semantic diff, review summary, and commit-intent output together for one accepted mutation.
  - [x] Reuse the existing M6 `SemanticDiffCalculator`, `AthenaSemanticReviewService`, and `AthenaSemanticCommitService` instead of introducing a graph-only or editor-only review path.
  - [x] Keep the review artifact nullable when Athena is not operating inside a valid governed repository source root.
- [x] Feed accepted source-originated mutation previews into the shared semantic review model. (AC: 1, 2)
  - [x] Preserve Story `1.3` preview-only behavior while attaching governed review output whenever the repository contract is valid.
  - [x] Keep canonical runtime cache and command history unchanged for source-originated preview evaluation.
- [x] Feed accepted graph-originated semantic command execution into the same semantic review model. (AC: 1, 2)
  - [x] Attach runtime semantic diff inspection to accepted graph semantic mutation payloads.
  - [x] Attach governed semantic review plus commit-intent output to accepted graph semantic mutation payloads.
  - [x] Keep projection-only graph mutation results free of fabricated semantic review state.
- [x] Extend the Athena LSP boundary so source and graph clients can inspect one shared review shape. (AC: 1, 2)
  - [x] Add typed payload transport for accepted-mutation semantic review artifacts.
  - [x] Reuse existing semantic SCM payload contracts for review summary and commit intent.
- [x] Verify compatible semantic review facts across source and graph origins. (AC: 2)
  - [x] Add focused LSP coverage proving the same engineering change yields matching review and commit facts for source and graph.
  - [x] Add regression coverage proving graph semantic mutation payloads now carry runtime semantic inspection and shared review data.
  - [x] Run Windows Java 25 verification sequentially.

## Dev Notes

### Story Intent

- Story `3.1` does not widen M8 into richer SCM UX or reveal orchestration yet.
- The narrow proof target is semantic review unification: accepted source preview and accepted graph semantic execution must now publish one governed review model.
- Projection-only graph metadata edits remain outside semantic SCM review because they do not change canonical engineering meaning.

### Architecture Guardrails

- Preserve AD-34 and AD-37: source and graph remain clients of one runtime mutation authority.
- Preserve AD-38 and AD-41: accepted mutation consequences must converge on one semantic review path and one downstream review/history authority.
- Preserve AD-42: keep this story narrow by attaching review output to accepted mutations without widening into new UI workflow or persistence behavior.

### Technical Notes

- The new runtime service publishes review artifacts only when the active project belongs to the governed repository source root.
- Review output is computed from canonical before/after engineering documents through `kernel/semantic-scm`, not from renderer-local graph state.
- Graph semantic mutation payloads now expose both runtime semantic inspection and governed review output so later reveal work can anchor on the same semantic facts.

### Testing Requirements

- Minimum verification commands:
  - `cmd /c "call java25 && .\gradlew.bat --no-daemon --console=plain :kernel:runtime:compileTestKotlin :ide:lsp:compileTestKotlin"`
  - `cmd /c "call java25 && .\gradlew.bat --no-daemon --console=plain :ide:lsp:test --tests com.engineeringood.athena.ide.lsp.AthenaSourceMutationRequestTest"`
  - `cmd /c "call java25 && .\gradlew.bat --no-daemon --console=plain :ide:lsp:test --tests com.engineeringood.athena.ide.lsp.AthenaProjectionRequestTest"`
  - `cmd /c "call java25 && .\gradlew.bat --no-daemon --console=plain :kernel:runtime:test --tests com.engineeringood.athena.runtime.AthenaGraphCommandIntentServiceTest :ide:lsp:test --tests com.engineeringood.athena.ide.lsp.AthenaSourceMutationRequestTest --tests com.engineeringood.athena.ide.lsp.AthenaProjectionRequestTest"`

## References

- [Source: _bmad-output/planning-artifacts/epics-M8-2026-07-10.md]
- [Source: _bmad-output/planning-artifacts/prds/prd-Athena-2026-07-10-m8/prd.md]
- [Source: _bmad-output/planning-artifacts/prds/prd-Athena-2026-07-10-m8/addendum.md]
- [Source: _bmad-output/planning-artifacts/architecture/architecture-Athena-2026-07-10-m8/ARCHITECTURE-SPINE.md]
- [Source: _bmad-output/implementation-artifacts/m8/1-3-normalize-source-originated-changes-into-the-same-runtime-mutation-result-path.md]
- [Source: _bmad-output/implementation-artifacts/m8/2-2-prove-the-first-semantic-mutation-path-from-the-graph-workbench.md]

## Story Completion Status

- Status: done
- Completion note: Accepted source previews and accepted graph semantic commands now both publish one governed semantic review artifact derived from `kernel/semantic-scm`, while graph semantic mutation payloads also expose the same runtime diff inspection path used by command history.

## Dev Agent Record

### Agent Model Used

GPT-5 Codex

### Debug Log References

- `cmd /c "call java25 && .\gradlew.bat --no-daemon --console=plain :kernel:runtime:compileTestKotlin :ide:lsp:compileTestKotlin"`
- `cmd /c "call java25 && .\gradlew.bat --no-daemon --console=plain :ide:lsp:test --tests com.engineeringood.athena.ide.lsp.AthenaSourceMutationRequestTest"`
- `cmd /c "call java25 && .\gradlew.bat --no-daemon --console=plain :ide:lsp:test --tests com.engineeringood.athena.ide.lsp.AthenaProjectionRequestTest"`
- `cmd /c "call java25 && .\gradlew.bat --no-daemon --console=plain :kernel:runtime:test --tests com.engineeringood.athena.runtime.AthenaGraphCommandIntentServiceTest :ide:lsp:test --tests com.engineeringood.athena.ide.lsp.AthenaSourceMutationRequestTest --tests com.engineeringood.athena.ide.lsp.AthenaProjectionRequestTest"`

### Completion Notes List

- Added `AthenaSemanticMutationReviewService` so accepted mutation consequences can be projected back through the existing M6 semantic diff, review, and commit path.
- Extended accepted source-mutation results with optional governed semantic review output without changing preview-only runtime behavior.
- Extended accepted graph semantic mutation results with runtime semantic diff inspection plus the same governed semantic review output used by source mutation evaluation.
- Added LSP transport for accepted-mutation review artifacts and reused the existing semantic SCM review/commit payload contracts.
- Verified that the same engineering change now yields matching review and commit facts whether it is initiated from source or from the graph workbench.
