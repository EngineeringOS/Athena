---
baseline_commit: a9b3a12593a428305bb342f3594226aff1120de2
---

# Story 3.3: Surface Package Diagnostics And Repository Graph Feedback In The Existing Athena IDE

Status: done

## Story

As an engineer,
I want to see package-aware repository state and diagnostics inside the current Athena workbench,
so that governed package meaning is operable from the IDE rather than hidden behind backend internals.

## FR Traceability

- FR-5: surface package-aware diagnostics through the semantic boundary
- FR-7: upgrade the active runtime-backed repository session into a package graph session
- FR-9: surface package state in the existing Athena IDE path
- FR-10: keep language-surface hardening downstream of the same semantic authority
- FR-12: preserve later graphical projection without widening M5
- NFR-1: repository/package meaning remains owned by compiler and runtime layers
- NFR-3: manifest, lock, graph, and diagnostics remain inspectable in the workbench
- NFR-4: extend the existing M4 shell instead of rewriting it

## Acceptance Criteria

1. Given the repository graph has been validated and resolved, when I use the current Athena IDE path, then the workbench can surface package-aware diagnostics, repository graph feedback, or resolution status through existing additive Athena product seams, and those views remain downstream of `ide/lsp` and runtime authority.
2. Given repository/package problems exist, when I inspect the workbench feedback, then I can identify contract, dependency, or lock-related issues without relying on raw filesystem guessing, and the IDE does not implement its own package diagnostic engine.

## Tasks / Subtasks

- [x] Add a read-only frontend projection path for repository graph feedback sourced from Athena LSP. (AC: 1, 2)
  - [x] Reuse `AthenaLspEditorBridgeService.requestRepositoryGraphSession()` as the only semantic fetch path for package graph feedback.
  - [x] Keep any frontend state disposable and view-oriented; do not introduce a frontend-owned repository/package authority model.
  - [x] Preserve the existing repository-session lifecycle contract so the package-feedback surface only activates when the JVM session is ready.
- [x] Add one additive Athena workbench surface for repository/package feedback. (AC: 1, 2)
  - [x] Register the surface through the existing Athena workbench extension seam instead of changing the shell architecture.
  - [x] Render repository graph status, manifest dependency intent, resolved package graph summary, and package diagnostics in one operator-readable panel.
  - [x] Keep the panel read-only and explicitly downstream of the JVM/LSP semantic path.
- [x] Make package problems inspectable without raw file guessing. (AC: 2)
  - [x] Surface lock state, validity, and diagnostic severity/code/message in the workbench.
  - [x] Distinguish empty/clean, loading, unavailable, and error states so contract or dependency failures remain understandable.
  - [x] Avoid implementing any frontend-side package analysis, lock validation, or dependency resolution logic.
- [x] Verify the additive workbench path without regressing current Athena IDE behavior. (AC: 1, 2)
  - [x] Build the frontend package after the widget and service updates.
  - [x] Run the existing backend test script if affected by transport changes.
  - [x] Run the wider sequential Java 25 regression command because the package-feedback path still depends on JVM LSP authority.

## Dev Notes

### Story Intent

- Story `3.1` established the runtime-owned `RepositoryGraphSession`.
- Story `3.2` exposed that state through the sole `ide/lsp` semantic path.
- Story `3.3` now projects that already-governed package state into the existing Athena workbench so package meaning becomes visible and operable inside the IDE.

### Architecture Guardrails

- Align to AD-17: the workbench remains downstream of the runtime-owned `RepositoryGraphSession`.
- Align to AD-18: use existing additive Athena workbench seams; do not widen into shell rewrite or broad UX polish.
- Preserve AD-13 through AD-16: package graph meaning, lock validation, and diagnostics remain JVM-owned and are only displayed in the frontend.
- Preserve future room for Story `3.4`, M6 semantic SCM, and later graphical projection.

### Technical Requirements

- Reuse the current frontend seams:
  - `AthenaRepositorySessionService`
  - `AthenaLspEditorBridgeService.requestRepositoryGraphSession()`
  - `AthenaHomeWidget`
  - `AthenaSemanticInspectionWidget`
  - `ATHENA_WORKBENCH_EXTENSIONS`
- Prefer a dedicated additive package-feedback widget over inflating backend endpoints or rewriting the home surface into a repository manager.
- If temporary frontend view state is needed, keep it limited to:
  - loading/error/read-only payload projection
  - response shaping already returned by `athena/repositoryGraphSession`
- Keep this story out of scope for:
  - new backend HTTP repository graph endpoints
  - frontend package analysis or lock validation
  - semantic SCM concepts
  - graphical projection or graph editing
  - broad editor polish

### Architecture Compliance

- Prevent these failure modes:
  - TypeScript becomes a second package-graph authority
  - the workbench reconstructs diagnostics or lock state instead of projecting them
  - the shell is rewritten instead of extended through the existing product extension registry
  - package failures remain hidden behind generic session messages
  - Story `3.3` silently expands into Story `3.4` editor hardening

### Library / Framework Requirements

- Use the frozen local stack:
  - Java `25`
  - Kotlin `2.4.0`
  - Gradle `9.6.1`
  - Node.js `22+`
  - Yarn `1.22.22`
  - Eclipse Theia `1.73.1`
- No new dependencies are justified.

### File Structure Requirements

- Expected update files:
  - `ide/theia-frontend/src/browser/athena-frontend-module.ts`
  - `ide/theia-frontend/src/browser/athena-workbench-extensions.ts`
  - `ide/theia-frontend/src/browser/athena-home-widget.tsx`
  - `ide/theia-frontend/src/browser/athena-lsp-editor-bridge-service.ts`
  - `ide/theia-frontend/src/browser/style/index.css`
- Likely new files:
  - one new read-only package-feedback widget under `ide/theia-frontend/src/browser/`
  - generated frontend build outputs under `ide/theia-frontend/lib/browser/`
- Files whose current behavior must be preserved:
  - `ide/theia-frontend/src/browser/athena-repository-session-service.ts`
    - remains the repository-session lifecycle owner, not the package-graph authority
  - `ide/theia-frontend/src/browser/athena-semantic-inspection-widget.tsx`
    - remains the current document-oriented semantic panel
  - `ide/theia-backend/src/node/athena-repository-session-manager.ts`
    - should not gain a second repository graph endpoint because the LSP tunnel already exists

### Testing Requirements

- Minimum verification commands for story completion:
  - `yarn workspace @engineeringood/athena-theia-frontend build`
- Additional verification when transport-facing code is touched:
  - `yarn workspace @engineeringood/athena-theia-backend test`
- Preferred wider regression:
  - `cmd /c "call java25 && .\gradlew.bat --no-daemon --console=plain build"`
- Required proof checks:
  - repository graph feedback renders from the Athena LSP request path
  - diagnostics, lock state, and package graph summaries remain inspectable in the workbench
  - existing repository session and semantic inspection surfaces still build and behave
- Keep all Gradle verification sequential on Windows. Do not run Gradle commands concurrently with each other.

### Current Code State To Preserve

- `AthenaHomeWidget` already shows session-level repository information and quick actions for workbench extensions.
- `AthenaSemanticInspectionWidget` already proves the right-panel additive widget pattern driven by `AthenaLspEditorBridgeService`.
- `AthenaLspEditorBridgeService` already exposes `requestRepositoryGraphSession()` over `/athena/lsp/request`.
- `AthenaRepositorySessionService` currently owns only repository-session lifecycle summary and should remain lightweight.

### Previous Story Intelligence

- Story `2.4` already made package diagnostics and graph reports inspectable on the JVM side.
- Story `3.1` moved that authority into `RepositoryGraphSession`.
- Story `3.2` intentionally stopped at protocol exposure and did not add a visible frontend surface.
- The user's standing constraints still apply:
  - package root stays `com.engineeringood`
  - Java `25` is mandatory
  - physical workspace structure must match architecture
  - Gradle verification must stay sequential on Windows

### Git Intelligence Summary

- Recent completed milestone commits show the repo advances through narrow proof slices rather than broad rewrites.
- Practical implication:
  - Story `3.3` should attach one additive workbench surface to existing seams instead of widening into platform redesign.

### Latest Technical Information

- No web research is required for Story `3.3`.

### Project Structure Notes

- No `project-context.md` file exists in the repository.
- Story `3.3` should leave clean room for:
  - Story `3.4` narrow editor hardening
  - M6 semantic SCM
  - later graphical projection surfaces

### References

- [Source: _bmad-output/planning-artifacts/epics-M5-2026-07-08.md#story-33-surface-package-diagnostics-and-repository-graph-feedback-in-the-existing-athena-ide]
- [Source: _bmad-output/planning-artifacts/prds/prd-Athena-2026-07-08-m5/prd.md]
- [Source: _bmad-output/planning-artifacts/prds/prd-Athena-2026-07-08-m5/addendum.md]
- [Source: _bmad-output/planning-artifacts/architecture/architecture-Athena-2026-07-08-m5/ARCHITECTURE-SPINE.md]
- [Source: _bmad-output/implementation-artifacts/m5/2-4-publish-package-diagnostics-and-graph-reports-for-runtime-ide-and-m6-foundations.md]
- [Source: _bmad-output/implementation-artifacts/m5/3-1-upgrade-the-active-runtime-session-into-a-repository-graph-session.md]
- [Source: _bmad-output/implementation-artifacts/m5/3-2-expose-package-state-through-ide-lsp-as-the-sole-ide-semantic-path.md]
- [Source: ide/theia-frontend/src/browser/athena-home-widget.tsx]
- [Source: ide/theia-frontend/src/browser/athena-lsp-editor-bridge-service.ts]
- [Source: ide/theia-frontend/src/browser/athena-repository-session-service.ts]
- [Source: ide/theia-frontend/src/browser/athena-semantic-inspection-widget.tsx]
- [Source: ide/theia-frontend/src/browser/athena-workbench-extensions.ts]

## Story Completion Status

- Status: done

## Dev Agent Record

### Agent Model Used

GPT-5 Codex

### Debug Log References

- M5 epics, PRD, addendum, and architecture review for package-feedback workbench scope
- Story `2.4`, `3.1`, and `3.2` completion-note review for canonical publication, session, and protocol seams
- CodeGraph exploration of `AthenaRepositorySessionService` and `AthenaHomeWidget`
- frontend module, widget, and extension-registry seam review
- frontend workspace build, backend proof script, Gradle build, and desktop smoke proof

### Completion Notes List

- Added `AthenaRepositoryGraphWidget` as a dedicated read-only Athena workbench panel that projects repository graph status, manifest dependency intent, resolved packages, and package diagnostics from `athena/repositoryGraphSession`.
- Registered the new panel through the existing Athena workbench extension registry and surfaced it in the startup layout and Athena Home quick-action flow without rewriting the shell.
- Updated Athena Home copy to reflect that package-graph feedback is now part of the M5 workbench surface and removed the stale mojibake text from the repository-rule section.
- Verified with `yarn workspace @engineeringood/athena-theia-frontend build`, `yarn build`, `yarn workspace @engineeringood/athena-theia-backend test`, `cmd /c "call java25 && .\gradlew.bat --no-daemon --console=plain build"`, and `yarn start:smoke`.

### File List

- _bmad-output/implementation-artifacts/m5/3-3-surface-package-diagnostics-and-repository-graph-feedback-in-the-existing-athena-ide.md
- _bmad-output/implementation-artifacts/m5/sprint-status.yaml
- ide/theia-frontend/src/browser/athena-frontend-module.ts
- ide/theia-frontend/src/browser/athena-home-widget.tsx
- ide/theia-frontend/src/browser/athena-repository-graph-widget.tsx
- ide/theia-frontend/src/browser/athena-workbench-extensions.ts
- ide/theia-frontend/src/browser/style/index.css
- ide/theia-frontend/lib/browser/athena-frontend-module.d.ts
- ide/theia-frontend/lib/browser/athena-frontend-module.d.ts.map
- ide/theia-frontend/lib/browser/athena-frontend-module.js
- ide/theia-frontend/lib/browser/athena-frontend-module.js.map
- ide/theia-frontend/lib/browser/athena-home-widget.d.ts
- ide/theia-frontend/lib/browser/athena-home-widget.d.ts.map
- ide/theia-frontend/lib/browser/athena-home-widget.js
- ide/theia-frontend/lib/browser/athena-home-widget.js.map
- ide/theia-frontend/lib/browser/athena-repository-graph-widget.d.ts
- ide/theia-frontend/lib/browser/athena-repository-graph-widget.d.ts.map
- ide/theia-frontend/lib/browser/athena-repository-graph-widget.js
- ide/theia-frontend/lib/browser/athena-repository-graph-widget.js.map
- ide/theia-frontend/lib/browser/athena-workbench-extensions.d.ts
- ide/theia-frontend/lib/browser/athena-workbench-extensions.d.ts.map
- ide/theia-frontend/lib/browser/athena-workbench-extensions.js
- ide/theia-frontend/lib/browser/athena-workbench-extensions.js.map

## Change Log

- 2026-07-09: Created Story `3.3` context and moved it to ready-for-dev.
- 2026-07-09: Implemented the additive Repository Graph workbench panel, updated the Athena Home copy, and moved the story to review after frontend, backend, Java 25, and desktop smoke verification.
