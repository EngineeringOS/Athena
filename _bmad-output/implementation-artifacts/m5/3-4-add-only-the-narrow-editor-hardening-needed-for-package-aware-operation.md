---
baseline_commit: a9b3a12593a428305bb342f3594226aff1120de2
---

# Story 3.4: Add Only The Narrow Editor Hardening Needed For Package-Aware Operation

Status: done

## Story

As an engineer,
I want the Athena editor surface to include only the minimal hardening needed to work with governed package semantics,
so that M5 stays operable without drifting into a broad IDE-polish or graphical-projection milestone.

## FR Traceability

- FR-9: surface package state in the existing Athena IDE path
- FR-10: add narrow language-surface hardening that directly supports M5
- FR-12: preserve later graphical projection without widening M5
- NFR-1: repository/package meaning remains owned by compiler and runtime layers
- NFR-4: extend the current M4 shell instead of rewriting it
- NFR-5: preserve clean extension space for later milestones

## Acceptance Criteria

1. Given package-aware repository work is available in the current IDE shell, when minimal editor hardening is added in M5, then it is limited to directly supporting package-aware operation, such as basic highlighting, semantic token groundwork, or closely related package-operability feedback, and the work remains subordinate to the package-graph milestone core.
2. Given future milestones such as semantic SCM or graphical projection are considered, when the M5 IDE additions are reviewed, then the additions preserve downstream extension space for those later milestones, and M5 does not expand into visual-workbench or broad UX-polish scope.

## Tasks / Subtasks

- [x] Add a narrow Athena language definition for the current `.athena` editor path. (AC: 1, 2)
  - [x] Register Monaco language configuration for braces, quotes, and other small authored-source editing affordances already implied by the current DSL.
  - [x] Add basic syntax highlighting for the current M0-to-M5 grammar surface, including the real DSL keywords, strings, delimiters, and qualified-name structure.
  - [x] Keep the language definition frontend-only and presentation-only; do not create a second parser or semantic authority in TypeScript.
- [x] Integrate the editor hardening through the existing Athena editor bridge without widening the architecture. (AC: 1, 2)
  - [x] Wire the new language definition through the existing `AthenaLspEditorBridgeService.registerAthenaLanguage()` seam.
  - [x] Keep current LSP-backed completion, symbols, definition, references, diagnostics, and package feedback behavior intact.
  - [x] Preserve room for future semantic-token or richer authoring work without promising it in M5.
- [x] Verify the hardening remains minimal and non-regressive. (AC: 1, 2)
  - [x] Build the Athena frontend package after the language-definition changes.
  - [x] Run the wider Theia workspace build to prove the packaged product still compiles.
  - [x] Run the sequential Java 25 regression command because the editor surface still depends on the existing JVM semantic boundary.
  - [x] Run the existing desktop smoke proof to confirm the packaged shell still starts cleanly.

## Dev Notes

### Story Intent

- Story `3.3` made repository/package feedback visible in the workbench.
- Story `3.4` now closes the remaining M5 operability gap in the editor itself by giving `.athena` files a minimal but intentional authored-source language surface.

### Architecture Guardrails

- Align to AD-18: only add narrow package-operability-scoped editor support.
- Preserve AD-17 and AD-3: `ide/lsp` remains the sole semantic path; frontend language hardening is visual/editorial support only.
- Keep this story below the threshold of broad UX polish, custom editor frameworks, or graphical workbench experimentation.

### Technical Requirements

- Reuse the current frontend seam:
  - `AthenaLspEditorBridgeService.registerAthenaLanguage()`
- Ground the highlighting vocabulary in the real current DSL surface from `kernel/language` and current examples:
  - `system`
  - `device`
  - `port`
  - `connect`
  - strings, `{}`, `.`, `->`, and authored identifiers
- Prefer one dedicated frontend language-definition file so future semantic-token or richer authoring work has a clean seam.
- Keep this story out of scope for:
  - LSP hover, rename, formatting, code actions, or full semantic token transport
  - frontend parsing or semantic analysis
  - broad theme work
  - workbench layout changes

### Architecture Compliance

- Prevent these failure modes:
  - TypeScript reimplements Athena parsing or package semantics
  - the story silently expands into broad authoring-feature work
  - M5 adds editor capability unrelated to package-aware operation
  - the current LSP-backed features regress while adding highlighting

### Library / Framework Requirements

- Use the frozen local stack:
  - Java `25`
  - Kotlin `2.4.0`
  - Gradle `9.6.1`
  - Node.js `22+`
  - Yarn `1.22.22`
  - Eclipse Theia `1.73.1`
  - Monaco via `@theia/monaco-editor-core`
- No new dependencies are justified.

### File Structure Requirements

- Expected update files:
  - `ide/theia-frontend/src/browser/athena-lsp-editor-bridge-service.ts`
- Likely new files:
  - one frontend language-definition file under `ide/theia-frontend/src/browser/`
  - generated frontend build outputs under `ide/theia-frontend/lib/browser/`
- Files whose current behavior must be preserved:
  - `ide/theia-frontend/src/browser/athena-repository-graph-widget.tsx`
  - `ide/theia-frontend/src/browser/athena-semantic-inspection-widget.tsx`
  - `ide/lsp/src/main/kotlin/com/engineeringood/athena/ide/lsp/AthenaLanguageServer.kt`

### Testing Requirements

- Minimum verification commands for story completion:
  - `yarn workspace @engineeringood/athena-theia-frontend build`
  - `yarn build`
- Preferred wider regression:
  - `cmd /c "call java25 && .\gradlew.bat --no-daemon --console=plain build"`
  - `yarn start:smoke`
- Required proof checks:
  - `.athena` files receive basic highlighting and editor configuration from the Athena-owned frontend seam
  - the existing LSP-backed editor and workbench features still build cleanly
  - no new semantic authority appears in TypeScript
- Keep all Gradle verification sequential on Windows. Do not run Gradle commands concurrently with each other.

### Current Code State To Preserve

- `AthenaLspEditorBridgeService.registerAthenaLanguage()` currently registers the `athena` language id and then wires only LSP-backed providers.
- The frontend currently has no real Monaco tokenizer or language configuration for `.athena`, which is why authored files still look visually underpowered.
- The LSP server currently does not advertise semantic tokens, hover, rename, or formatting capabilities; this story should not fabricate them.

### Previous Story Intelligence

- Story `3.3` already created the additive repository graph panel and validated the full Theia workspace build plus desktop smoke path.
- The user previously called out the lack of `.athena` highlighting explicitly; this story should close that gap without turning into general editor-polish work.
- The user's standing constraints still apply:
  - package root stays `com.engineeringood`
  - Java `25` is mandatory
  - physical workspace structure must match architecture
  - Gradle verification must stay sequential on Windows

### Git Intelligence Summary

- Recent M5 work progressed in a narrow sequence: session -> protocol -> workbench feedback.
- Practical implication:
  - Story `3.4` should be the final small editor-surface proof, not a new feature branch for general IDE parity.

### Latest Technical Information

- No web research is required for Story `3.4`.

### Project Structure Notes

- No `project-context.md` file exists in the repository.
- Story `3.4` should leave clean room for:
  - later semantic-token or richer editor work
  - M6 semantic SCM
  - later graphical projection milestones

### References

- [Source: _bmad-output/planning-artifacts/epics-M5-2026-07-08.md#story-34-add-only-the-narrow-editor-hardening-needed-for-package-aware-operation]
- [Source: _bmad-output/planning-artifacts/prds/prd-Athena-2026-07-08-m5/prd.md]
- [Source: _bmad-output/planning-artifacts/architecture/architecture-Athena-2026-07-08-m5/ARCHITECTURE-SPINE.md]
- [Source: _bmad-output/implementation-artifacts/m5/3-3-surface-package-diagnostics-and-repository-graph-feedback-in-the-existing-athena-ide.md]
- [Source: ide/theia-frontend/src/browser/athena-lsp-editor-bridge-service.ts]
- [Source: ide/lsp/src/main/kotlin/com/engineeringood/athena/ide/lsp/AthenaLanguageServer.kt]
- [Source: kernel/language/src/main/kotlin/com/engineeringood/athena/language/AthenaLanguageParser.kt]
- [Source: examples/m0/demo-cabinet.athena]

## Story Completion Status

- Status: done

## Dev Agent Record

### Agent Model Used

GPT-5 Codex

### Debug Log References

- M5 epics, PRD, and architecture review for editor-hardening scope
- Story `3.3` completion-note review
- CodeGraph exploration of language registration and LSP capability seams
- parser and examples review for the real current DSL keyword surface
- frontend build, full Theia workspace build, Java 25 regression, and desktop smoke proof

### Completion Notes List

- Added `athena-language-definition.ts` as the narrow Athena-owned Monaco language seam with brace/quote configuration and a small tokenizer for the real current DSL keywords, strings, delimiters, qualified references, and plain identifiers.
- Updated `AthenaLspEditorBridgeService.registerAthenaLanguage()` to install the Athena language configuration and tokenizer while preserving all current LSP-backed completion, symbols, definition, references, diagnostics, semantic inspection, and repository-graph behavior.
- Kept M5 scope narrow by avoiding frontend parsing, semantic-token transport, hover, rename, formatting, or any second semantic authority in TypeScript.
- Verified with `yarn workspace @engineeringood/athena-theia-frontend build`, `yarn build`, `cmd /c "call java25 && .\gradlew.bat --no-daemon --console=plain build"`, and `yarn start:smoke`.
- No dedicated frontend unit-test harness exists in this package yet, so verification for this story is compile/build plus desktop smoke rather than widget-level automated tests.

### File List

- _bmad-output/implementation-artifacts/m5/3-4-add-only-the-narrow-editor-hardening-needed-for-package-aware-operation.md
- _bmad-output/implementation-artifacts/m5/sprint-status.yaml
- ide/theia-frontend/src/browser/athena-language-definition.ts
- ide/theia-frontend/src/browser/athena-lsp-editor-bridge-service.ts
- ide/theia-frontend/lib/browser/athena-language-definition.d.ts
- ide/theia-frontend/lib/browser/athena-language-definition.d.ts.map
- ide/theia-frontend/lib/browser/athena-language-definition.js
- ide/theia-frontend/lib/browser/athena-language-definition.js.map
- ide/theia-frontend/lib/browser/athena-lsp-editor-bridge-service.d.ts
- ide/theia-frontend/lib/browser/athena-lsp-editor-bridge-service.d.ts.map
- ide/theia-frontend/lib/browser/athena-lsp-editor-bridge-service.js
- ide/theia-frontend/lib/browser/athena-lsp-editor-bridge-service.js.map

## Change Log

- 2026-07-09: Created Story `3.4` context and moved it to ready-for-dev.
- 2026-07-09: Added the narrow Athena Monaco language definition and bridge integration, then moved the story to review after frontend, Java 25, and desktop smoke verification.
