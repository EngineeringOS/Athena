---
baseline_commit: c04b3eb
---

# Story 1.2: Define Preview And Acceptance Contracts For Guided Authoring

Status: done

## Story

As an architecture owner,  
I want Athena to define previewable authoring outcomes before commit,  
so that guided authoring stays review-first and inspectable.

## FR Traceability

- FR-12: Athena can preview guided mutations before acceptance
- FR-13: Athena can commit accepted guided mutations into canonical state
- NFR-1: guided authoring introduces no second write path outside M8
- NFR-2: workbench surfaces remain consumers of platform-owned authoring contracts
- NFR-4: canonical semantic identity remains stronger than graph element ids, widget ids, or presentation occurrence ids
- NFR-5: preview and approval remain inspectable and deterministic

## Acceptance Criteria

1. Given one guided authoring action is requested, when the runtime expands it, then Athena can publish a preview of the semantic consequences before acceptance.
2. Given a preview is accepted or rejected, when the flow is completed, then acceptance hands off to canonical mutation while rejection leaves canonical state unchanged.

## Tasks / Subtasks

- [x] Define the first authoring preview vocabulary in `authoring-model`. (AC: 1, 2)
  - [x] Add typed preview contracts for one guided authoring request and its semantic consequences.
  - [x] Keep preview contracts platform-owned and surface-agnostic.
  - [x] Keep all public or core Kotlin types documented with clean KDoc.
- [x] Define explicit acceptance and rejection contracts above mutation execution. (AC: 1, 2)
  - [x] Add typed decision contracts for accept and reject outcomes.
  - [x] Make the contract comments explicit that acceptance hands off to M8 later and that rejection does not mutate canonical state.
  - [x] Keep the preview layer inspectable without embedding runtime execution logic.
- [x] Keep Story `1.2` narrow and foundational. (AC: 1, 2)
  - [x] Do not widen Story `1.2` into authoring runtime orchestration, Theia widgets, GLSP tools, or LSP transport handlers yet.
  - [x] Do not widen Story `1.2` into direct mutation execution, persistence, or commit history logic.
  - [x] Do not widen Story `1.2` into domain-specific preview rendering or Siemens-specific behavior.
- [x] Add focused tests and module documentation updates for the new preview layer. (AC: 1, 2)
  - [x] Add focused tests under the owning module to prove:
    - preview remains tied to one authoring intent
    - preview consequences stay inspectable and canonical-identity-aware
    - decision contracts separate acceptance from rejection cleanly
  - [x] Update module README files in English and Chinese to include preview and acceptance contracts.
  - [x] Run Gradle verification sequentially on Windows with Java 25; do not run build or test tasks concurrently.

## Dev Notes

### Story Intent

- Story `1.2` freezes the preview and acceptance contract above M8.
- The success condition is not "Athena already commits guided authoring."
- The success condition is "Athena now has one clean preview-and-decision contract that later runtime, LSP, and workbench surfaces can target without collapsing review into direct commit."
- Story `1.3` should publish shared runtime and transport seams that consume this contract.

### Architecture Guardrails

- Align to AD-84: M15 introduces authoring intent above M8, not a frontend mutation shortcut.
- Align to AD-87: one authoring intent may expand into multiple governed mutations.
- Align to AD-91: mutation preview reuses review-first product semantics.
- Preserve inherited AD-34, AD-39, AD-80, and AD-82.

### Technical Requirements

- Current state already contains:
  - typed authoring intents in `:kernel:authoring-model`
  - one mutation authority through M8
  - governed component knowledge through M14
- Story `1.2` must define preview and decision contracts before authoring runtime and transport seams expand.
- The contract should be shaped so later work can target it:
  - runtime-owned authoring preview assembly
  - LSP preview requests and decisions
  - Theia preview and confirmation surfaces
  - graph-originated guided connect review flows

### Architecture Compliance

- The story is only successful if later M15 work can point to one clean ladder:
  - authoring intent
  - authoring preview
  - acceptance or rejection decision
  - later M8 handoff
- Prevent these failure modes:
  - preview collapsing into direct mutation execution
  - rejection semantics being implicit or frontend-only
  - preview records losing canonical semantic identities
  - widening Story `1.2` into runtime, transport, or UI work too early

### Library / Framework Requirements

- Use the repo-approved stack already frozen by planning artifacts:
  - Java `25`
  - Kotlin `2.4.0`
  - Gradle `9.6.1`
- Reuse current Kotlin and JUnit style already present in kernel modules.
- Do not add third-party libraries just to model preview contracts.

### File Structure Requirements

- Expected update files likely include:
  - `kernel/authoring-model/src/main/kotlin/...`
  - `kernel/authoring-model/src/test/kotlin/...`
  - `kernel/authoring-model/README.md`
  - `kernel/authoring-model/README.zh-CN.md`
- Explicit non-goals:
  - no `kernel/authoring-runtime` orchestration yet
  - no `ide/lsp` transport handlers yet
  - no Theia widget implementation yet
  - no GLSP connect tool implementation yet

### Testing Requirements

- Minimum verification commands should target `:kernel:authoring-model:test` directly first.
- Required proof checks:
  - preview stays tied to one authoring intent
  - preview consequences remain inspectable
  - acceptance and rejection stay explicit and deterministic
  - no concurrent Gradle build or test execution on Windows

### Current Code State To Preserve

- `Engineering IR` remains canonical authored engineering truth.
- M8 remains the only write authority.
- M14 remains the source of authorable component knowledge.
- M15 `1.1` already froze the base authoring intent vocabulary.

### References

- [Source: _bmad-output/planning-artifacts/epics-M15-2026-07-13.md]
- [Source: _bmad-output/planning-artifacts/prds/prd-Athena-2026-07-13-m15/prd.md]
- [Source: _bmad-output/planning-artifacts/prds/prd-Athena-2026-07-13-m15/addendum.md]
- [Source: _bmad-output/planning-artifacts/architecture/architecture-Athena-2026-07-13-m15/ARCHITECTURE-SPINE.md]
- [Source: docs/roadmap/athena-milestone-roadmap.md]

## Story Completion Status

- Status: done
- Completion note: added review-first preview and decision contracts to `:kernel:authoring-model`, expanded focused contract tests, updated bilingual module documentation, and verified with `cmd /c "call java25 && .\gradlew.bat --no-daemon --console=plain :kernel:authoring-model:test"` followed by `cmd /c "call java25 && .\gradlew.bat --no-daemon --console=plain :kernel:authoring-model:test :kernel:component-model:test :kernel:part-model:test"`.

## File List

- `kernel/authoring-model/README.md`
- `kernel/authoring-model/README.zh-CN.md`
- `kernel/authoring-model/src/main/kotlin/com/engineeringood/athena/authoring/AuthoringPreviewModels.kt`
- `kernel/authoring-model/src/test/kotlin/com/engineeringood/athena/authoring/AuthoringPreviewContractTest.kt`

## Change Log

- 2026-07-13: added the M15 preview-and-acceptance contract layer for guided authoring and documented it in both module READMEs.

## Dev Agent Record

### Agent Model Used

GPT-5 Codex

### Debug Log References

- M15 Story `1.2` preview-model red/green cycle in `:kernel:authoring-model`
- Sequential Java 25 verification for `:kernel:authoring-model:test`
- Sequential Java 25 regression verification for `:kernel:authoring-model:test :kernel:component-model:test :kernel:part-model:test`

### Completion Notes

- Added a separate `AuthoringPreviewModels.kt` file so preview and decision contracts stay split from base intent contracts.
- Published `AuthoringPreview`, `AuthoringPreviewChange`, `AuthoringPreviewStatus`, and explicit accept/reject decision contracts.
- Kept the story narrow: no runtime orchestration, transport, or UI implementation was introduced.
- Updated English and Chinese module READMEs and re-ran the encoding audit after the Chinese documentation change.
