---
baseline_commit: 9681345816d2659abf7a70b68151eacfef0944c2
---

# Story 3.4: Refresh Publication States Atomically

Status: done

## Story

As an engineer, I can edit project or Sheet source and see one READY/STALE/UNAVAILABLE state, so old
and new scene elements never mix.

## Acceptance Criteria

1. Valid edit atomically replaces complete scene and same-revision asset bundle; no mixed revision paint.
2. Invalid current source publishes attempted revision plus diagnostics and may display identified
   last-valid scene as non-editable STALE.
3. Missing/ambiguous companion, schema failure, adapter failure, or no accepted scene publishes
   UNAVAILABLE with no guessed scene.
4. READY alone enables move/connect commands. STALE may paint accepted scene but never emits mutation.
5. Repeated identical inputs yield identical scene digest, order, trace, asset bundle, and visible state.
6. Required sequential tests/build/audits pass.

## Tasks / Subtasks

- [x] Add session-scoped atomic publication owner with last-accepted revision tracking.
- [x] Distinguish STALE compile failure from fail-closed UNAVAILABLE authority failures.
- [x] Update Konva/widget to paint accepted STALE scene non-editably and clear UNAVAILABLE.
- [x] Keep READY as sole mutation-enabled state.
- [x] Run verification and complete story records.

## Dev Notes

- Publication is closed `AthenaScenePublication`; never mix scene/assets across accepted revisions.
- Cache is session-local and cleared on repository activation/shutdown.
- Missing companion never falls back to cached layout.
- Adapter validates publication before paint; READY is sole mutation-enabled state.

## References

- `_bmad-output/planning-artifacts/m43/epics.md` Story 3-4
- `_bmad-output/planning-artifacts/prds/prd-Athena-2026-08-05-m43/prd.md` FR-9
- `_bmad-output/planning-artifacts/architecture/architecture-Athena-2026-08-05-m43/M43-CONTRACT-PACK.md` section 7
- `_bmad-output/implementation-artifacts/m43/3-3-connect-ports-through-validated-commands.md`

## Dev Agent Record

### Agent Model Used

Codex GPT-5

### Debug Log References

Frontend `yarn test`, LSP compile, root Gradle `test`, encoding audit, and source-set hygiene audit pass.

### Completion Notes List

- `AthenaDiagramPublicationService` stores only complete accepted scene/asset revisions and returns closed STALE publications for recoverable compile failures.
- Missing/ambiguous companion and asset/lock authority failures remain UNAVAILABLE without cached fallback.
- Konva paints STALE scene but disables drag/lock callbacks; UNAVAILABLE clears scene.

### File List

- `ide/lsp/src/main/kotlin/com/engineeringood/athena/ide/lsp/AthenaDiagramPublicationService.kt`
- `ide/lsp/src/main/kotlin/com/engineeringood/athena/ide/lsp/AthenaLanguageServer.kt`
- `ide/theia-frontend/src/browser/diagram/konva-diagram-adapter.ts`
- `ide/theia-frontend/src/browser/athena-presentation-widget.tsx`

### Change Log

- 2026-08-06: Created through BMad create-story workflow; status `ready-for-dev`.
- 2026-08-06: Implemented atomic session publication states and non-editable STALE rendering; final
  publication and regression gates passed; status `done`.
