---
baseline_commit: ad382d8a2d1841771c5b95e008f29f78a6f751cd
---

# Story 2.5: Complete The Desktop Multi-View Operator Proof

Status: done

## Story

As a founder or operator,
I want Athena to demonstrate the final desktop operator proof for the already-implemented multi-view projection flow,
so that M2 closes with a believable runtime-managed milestone instead of only disconnected kernel pieces.

## Acceptance Criteria

1. Given Stories 2.1 through 2.4 are already complete and the first supported `cabinet` and `wiring` projections are available through runtime-owned sessions, when the desktop application opens an active project, then the operator can inspect the project in one view, switch to the other view, and follow the same semantic object across both, and the demonstrated flow uses runtime-owned projection snapshots rather than UI-private derivation.
2. Given the existing `connect ports` mutation path is already connected to projection refresh, when that mutation is executed during the desktop proof flow, then the active projection updates from refreshed geometry-backed state, and the demonstration remains constrained to the approved first mutation path rather than implying full editor breadth.
3. Given the M2 proof must remain desktop-first and kernel-centered, when the final operator proof is reviewed, then it demonstrates `Engineering IR -> Layout IR -> Geometry IR -> desktop/backend` as the governing chain, and it does not imply browser-first delivery, arbitrary geometry editing, or a full ECAD shell.
4. Given the final M2 operator proof is implemented, when the standard Java `25` build, regression, and desktop smoke checks are executed, then the workspace builds successfully and the desktop proof demonstrates synchronized multi-view projection over one semantic source, and Epic 2 closes the milestone with a runtime-operated, inspectable projection workflow as an integration and demonstration story rather than a new broad implementation bucket.

## Tasks / Subtasks

- [x] Refocus the default desktop bootstrap onto a dedicated M2 operator-proof project. (AC: 1, 2, 3, 4)
  - [x] Add or adopt a dedicated `examples/m2/` semantic seed for the final desktop proof flow.
  - [x] Keep the default desktop bootstrap rooted in runtime-owned project/session opening rather than a demo-only shortcut.
  - [x] Preserve the desktop shell boundary: app code hosts the proof, but runtime/compiler still own semantics and projection truth.
- [x] Add a deterministic scripted operator-proof verifier for the desktop surface. (AC: 1, 2, 3, 4)
  - [x] Script the proof around the already-supported flow: open project, inspect one semantic object, switch views, preserve identity, connect ports, and confirm refreshed projection state.
  - [x] Keep the verifier narrow and honest: no arbitrary editing, no browser-first claims, no UI-private derivation.
  - [x] Reuse runtime-owned shell/session APIs instead of introducing a separate demo runtime.
- [x] Extend desktop tests and smoke entrypoints to lock the final M2 proof. (AC: 1, 2, 4)
  - [x] Update default desktop runtime tests to assert the new operator-proof bootstrap seed and its initial state.
  - [x] Add deterministic verification for the scripted operator proof result.
  - [x] Keep existing desktop projection, selection, mutation, and runtime tests green.
- [x] Update desktop/operator-facing documentation for the final M2 proof. (AC: 3, 4)
  - [x] Update `:apps:desktop-viewer` docs to describe the operator-proof bootstrap and smoke commands.
  - [x] Update `examples/m2` docs to identify the dedicated desktop operator-proof seed.

## Dev Notes

### Story Intent

- Story `2.5` is an integration close-out story, not a new subsystem.
- The implementation should package the already-proven runtime, projection, refresh, and diff/history behavior into one believable desktop operator proof.
- Success is not “add more editor features.” Success is “make the existing M2 chain demonstrable end to end through the desktop surface.”

### Architecture Guardrails

- Align to `ARCHITECTURE-SPINE.md`:
  - `AD-2`: projection derivation remains a compiler concern.
  - `AD-3`: runtime owns projection sessions, view switching, and refresh.
  - `AD-5`: canonical semantic identity survives across both supported views and the mutation proof.
  - `AD-6`: desktop/viewer consumes geometry-backed runtime projections rather than reconstructing semantics privately.
  - `AD-7`: projection layers remain inspectable, not authoring surfaces.
  - `AD-8`: this final proof stays dependency-scoped and module-boundary honest.
- Align to `manifesto/docs/architecture/07-studio.md` and `09-layout-and-geometry.md`: the desktop surface demonstrates the chain, but it does not become the source of truth.

### Technical Requirements

- Reuse the already-implemented runtime-owned flows:
  - default projection session hosting
  - canonical semantic selection across `cabinet` and `wiring`
  - GUI-backed `connect ports`
  - scoped projection refresh
  - semantic diff plus projection consequence reporting
- Keep the final proof honest:
  - desktop-first
  - one supported mutation path
  - runtime-owned projections
  - no implication of arbitrary geometry editing or full ECAD shell breadth
- Prefer deterministic scripted verification over vague manual-only proof text.

### Architecture Compliance

- `:apps:desktop-viewer` may orchestrate and verify the operator-proof sequence, but it may not rederive semantic, layout, or geometry state privately.
- `:kernel:runtime` remains the only owner of active project/session state.
- `examples/m2` remains the published proof corpus and should host the semantic seed used by this final desktop proof where appropriate.
- Any new proof verifier or smoke path must exercise the real workbench session contract.

### Library / Framework Requirements

- Use the existing repo-pinned stack:
  - Java `25`
  - Kotlin `2.4.0`
  - Gradle `9.6.1`
  - Compose Multiplatform `1.11.1`
- Keep dependencies unchanged unless the current desktop module truly lacks something required for deterministic verification.

### File Structure Requirements

- Likely update files:
  - `apps/desktop-viewer/src/main/kotlin/com/engineeringood/athena/apps/composeviewer/AthenaComposeViewerBootstrap.kt`
  - `apps/desktop-viewer/src/main/kotlin/com/engineeringood/athena/apps/composeviewer/AthenaComposeViewerWorkbenchSession.kt`
  - `apps/desktop-viewer/src/main/kotlin/com/engineeringood/athena/apps/composeviewer/AthenaComposeViewerSmokeVerifier.kt`
  - `apps/desktop-viewer/src/main/kotlin/com/engineeringood/athena/apps/composeviewer/Main.kt`
  - `apps/desktop-viewer/build.gradle.kts`
  - `apps/desktop-viewer/src/test/kotlin/com/engineeringood/athena/apps/composeviewer/AthenaComposeViewerRuntimeTest.kt`
  - `apps/desktop-viewer/src/test/kotlin/com/engineeringood/athena/apps/composeviewer/AthenaComposeViewerWorkbenchSessionTest.kt`
  - `apps/desktop-viewer/README.md`
  - `apps/desktop-viewer/README.zh-CN.md`
  - `examples/m2/README.md`
  - `examples/m2/README.zh-CN.md`
- Likely add files:
  - `examples/m2/operator-proof.athena`
  - one desktop operator-proof verifier source file if needed

### Testing Requirements

- Minimum verification commands:
  - `java25`
  - `.\gradlew.bat --no-daemon --console=plain :apps:desktop-viewer:test`
  - `.\gradlew.bat --no-daemon --console=plain :apps:desktop-viewer:bootstrapSmoke`
  - `.\gradlew.bat --no-daemon --console=plain :apps:desktop-viewer:operatorProofSmoke`
  - `.\gradlew.bat --no-daemon --console=plain build`
- Required proof checks:
  - default desktop bootstrap opens the dedicated operator-proof project
  - selection persists across `cabinet` and `wiring`
  - connect-ports mutation refreshes the active projection and surfaces runtime-owned evidence
  - smoke-level verification produces a deterministic proof result
- Keep Gradle verification sequential on Windows.

### Previous Story Intelligence

- Story `2.1` introduced runtime projection sessions and view switching.
- Story `2.2` moved desktop consumption to runtime-owned snapshots and preserved canonical selection across views.
- Story `2.3` proved scoped refresh after `connect ports`.
- Story `2.4` attached projection consequence evidence to semantic diff inspection.
- Story `2.5` should assemble those capabilities into one deterministic desktop proof instead of layering on new technical depth.

### Git Intelligence Summary

- `ad382d8 Complete M1 runtime workspace and regroup modules` remains the baseline commit.
- The current M2 sequence has already delivered the underlying mechanics required for the final proof.
- The remaining task is to make the final desktop story legible, deterministic, and regression-locked.

### References

- `_bmad-output/planning-artifacts/epics-M2-2026-07-06.md`
  - `Story 2.5: Complete The Desktop Multi-View Operator Proof`
- `_bmad-output/planning-artifacts/prds/prd-Athena-2026-07-06/prd.md`
  - `FR-5`
  - `FR-6`
  - `FR-7`
  - `FR-8`
  - `FR-9`
  - `FR-10`
  - `FR-11`
  - `NFR-5`
  - `NFR-6`
- `_bmad-output/planning-artifacts/architecture/architecture-Athena-2026-07-06/ARCHITECTURE-SPINE.md`
  - `AD-2`
  - `AD-3`
  - `AD-5`
  - `AD-6`
  - `AD-7`
  - `AD-8`
- `_bmad-output/implementation-artifacts/m2/2-1-host-runtime-projection-sessions-and-supported-view-switching.md`
- `_bmad-output/implementation-artifacts/m2/2-2-expose-runtime-owned-projection-snapshots-to-the-desktop-surface.md`
- `_bmad-output/implementation-artifacts/m2/2-3-refresh-projection-state-after-one-supported-semantic-mutation-path.md`
- `_bmad-output/implementation-artifacts/m2/2-4-preserve-history-and-diff-meaning-across-projection-refresh.md`
- `manifesto/docs/architecture/07-studio.md`
- `manifesto/docs/architecture/09-layout-and-geometry.md`

## Story Completion Status

- Status: done
- Completion note: The default desktop bootstrap now opens the dedicated `operator-proof` seed, a scripted verifier replays the final M2 flow through the runtime-owned workbench session, and desktop tests plus smoke tasks passed on Java 25.

## Dev Agent Record

### Agent Model Used

GPT-5 Codex

### Debug Log References

- Story context creation inputs:
  - `git rev-parse HEAD`
  - `git log -5 --pretty=format:"%h %s"`
- Implementation verification:
  - `java25; .\gradlew.bat --no-daemon --console=plain :apps:desktop-viewer:test`
  - `java25; .\gradlew.bat --no-daemon --console=plain :apps:desktop-viewer:bootstrapSmoke`
  - `java25; .\gradlew.bat --no-daemon --console=plain :apps:desktop-viewer:operatorProofSmoke`
  - `java25; .\gradlew.bat --no-daemon --console=plain build`

### Completion Notes List

- Repointed the default desktop bootstrap from the M0 demo source to `examples/m2/operator-proof.athena`.
- Added `AthenaComposeViewerOperatorProofVerifier` so the final M2 operator journey can be replayed deterministically without a human operator.
- Extended the desktop entrypoint and Gradle verification tasks with an `operatorProofSmoke` path pinned to the Java 25 toolchain and the same software-rendered Compose settings used by the desktop proof.
- Refreshed English and Simplified Chinese documentation for `:apps:desktop-viewer` and `examples/m2` to describe the operator-proof seed and smoke commands.

## File List

- `apps/desktop-viewer/src/main/kotlin/com/engineeringood/athena/apps/composeviewer/AthenaComposeViewerBootstrap.kt`
- `apps/desktop-viewer/src/main/kotlin/com/engineeringood/athena/apps/composeviewer/AthenaComposeViewerOperatorProofVerifier.kt`
- `apps/desktop-viewer/src/main/kotlin/com/engineeringood/athena/apps/composeviewer/Main.kt`
- `apps/desktop-viewer/build.gradle.kts`
- `apps/desktop-viewer/src/test/kotlin/com/engineeringood/athena/apps/composeviewer/AthenaComposeViewerRuntimeTest.kt`
- `apps/desktop-viewer/src/test/kotlin/com/engineeringood/athena/apps/composeviewer/AthenaComposeViewerWorkbenchSessionTest.kt`
- `apps/desktop-viewer/README.md`
- `apps/desktop-viewer/README.zh-CN.md`
- `examples/m2/operator-proof.athena`
- `examples/m2/README.md`
- `examples/m2/README.zh-CN.md`
- `_bmad-output/implementation-artifacts/m2/2-5-complete-the-desktop-multi-view-operator-proof.md`
- `_bmad-output/implementation-artifacts/m2/sprint-status.yaml`

## Change Log

- 2026-07-07: Created Story `2.5` and moved it directly into implementation as the final M2 desktop integration proof.
- 2026-07-07: Completed the operator-proof bootstrap, scripted verifier, smoke entrypoint, documentation refresh, and Java 25 verification sequence.
