---
baseline_commit: a9b3a12593a428305bb342f3594226aff1120de2
---

# Story 2.4: Expose Review And Commit Semantics Through Runtime, LSP, And Existing IDE Seams

Status: review

## Story

As an engineer,
I want Athena to expose review and commit semantics through the current product path,
so that semantic SCM becomes operable in the existing desktop shell without moving semantic ownership into the frontend.

## FR Traceability

- FR-6: prepare structured commit intent from semantic change
- FR-8: publish semantic review summaries over current repository change
- FR-9: expose semantic SCM through runtime, LSP, and IDE seams
- NFR-1: semantic SCM remains downstream of compiler/runtime authority
- NFR-3: the same baseline plus repository state yields the same review and commit output
- NFR-5: M6 extends the current Athena shell rather than forcing a rewrite

## Acceptance Criteria

1. Given semantic review summaries and commit intent exist in JVM-owned services, when the IDE requests semantic SCM state, then runtime and `ide/lsp` expose additive Athena protocol surfaces for review and commit information, and Theia frontend and backend remain downstream projections rather than semantic authorities.
2. Given the current Athena workbench consumes semantic SCM data, when users inspect review and commit-preparation feedback, then they can do so without a shell rewrite or parallel frontend semantic model, and any Theia SCM integration remains a bridge rather than the source of Athena semantic meaning.

## Tasks / Subtasks

- [x] Add one runtime-owned semantic SCM projection service for baseline, review, and commit state. (AC: 1, 2)
  - [x] Reuse the existing semantic baseline, diff, review, and commit services rather than duplicating logic in `ide/`.
  - [x] Keep the runtime API typed, deterministic, and baseline-driven.
  - [x] Keep all new core Kotlin classes under `com.engineeringood.athena.runtime` with clean KDoc.
- [x] Expose additive semantic SCM protocol surfaces through `ide/lsp`. (AC: 1)
  - [x] Add typed LSP request/response contracts for semantic SCM review and commit state.
  - [x] Keep the LSP boundary Athena-owned and independent from Theia SCM provider types.
  - [x] Preserve the existing repository-session and semantic-inspection paths.
- [x] Project semantic SCM output through existing Theia backend and frontend seams. (AC: 1, 2)
  - [x] Reuse the existing backend LSP bridge instead of creating a second semantic transport path.
  - [x] Surface semantic review and commit-preparation output in the current workbench through additive Athena views or panels.
  - [x] Keep frontend state projection-only and avoid introducing a parallel semantic model.
- [x] Verify the product path with focused JVM and IDE coverage. (AC: 1, 2)
  - [x] Add runtime tests for baseline-driven semantic SCM state projection.
  - [x] Add LSP tests for the new semantic SCM request surface.
  - [x] Add backend/frontend tests where practical and keep Gradle or Node verification sequential on Windows with Java 25.
- [x] Update live M6 docs for the new semantic SCM product seam. (AC: 2)
  - [x] Update root, runtime, IDE, and workspace summary docs.
  - [x] Document that Theia remains a projection bridge and not the semantic SCM authority.

## Dev Notes

### Story Intent

- Story 2.4 makes the M6 review and commit semantics operable through the current Athena desktop path.
- The semantic source of truth remains in JVM runtime services and `ide/lsp`.
- Theia continues to project state and request actions; it does not own semantic SCM models.

### Architecture Guardrails

- Align to AD-21 by keeping review and commit publication on the same baseline/diff/review JVM path.
- Align to AD-23 by treating Theia as a downstream bridge rather than the semantic SCM core.
- Align to AD-24 by preserving authored-versus-derived distinctions in projected review and commit output.
- Preserve Story 2.3's additive plugin-enrichment model without allowing frontend code to rewrite core review facts.

### Technical Requirements

- Prefer one typed semantic SCM request surface rather than ad hoc frontend fetch endpoints per review or commit noun.
- Keep baseline selection explicit and Athena-owned at the protocol boundary.
- Avoid introducing Git, Theia SCM, or vendor execution nouns into runtime or LSP public contracts.
- Public/core Kotlin classes added in this story need clean KDoc.

### Architecture Compliance

- Prevent these failure modes:
  - frontend reconstructing review or commit meaning from raw repository/session payloads
  - backend becoming a second semantic SCM authority
  - untyped or vendor-specific baseline selection leaking into kernel/runtime contracts
  - review and commit payloads diverging from runtime-owned semantics
  - Theia SCM provider types appearing in kernel or LSP public contracts

### Library / Framework Requirements

- Java `25`
- Kotlin `2.4.0`
- Gradle `9.6.1`
- Node `22+`
- Theia `1.73.1`
- No third-party dependency should be added for Story 2.4.

### File Structure Requirements

- Expected new or updated files:
  - `kernel/runtime/src/main/kotlin/com/engineeringood/athena/runtime/...`
  - `ide/lsp/src/main/kotlin/com/engineeringood/athena/ide/lsp/...`
  - `ide/lsp/src/test/kotlin/com/engineeringood/athena/ide/lsp/...`
  - `ide/theia-backend/src/node/...`
  - `ide/theia-frontend/src/browser/...`
  - live docs in `README*.md`, `docs/usages/athena-workspace-summary.md`, and module READMEs

### Testing Requirements

- Minimum story verification:
  - `cmd /c "call java25 && .\gradlew.bat --no-daemon --console=plain :kernel:runtime:test :ide:lsp:test"`
- Recommended focused regression:
  - `cmd /c "call java25 && .\gradlew.bat --no-daemon --console=plain :integrations:scm-git:test :kernel:semantic-scm:test :kernel:repository-model:test :kernel:compiler:test"`
  - `yarn --cwd ide/theia-backend test`
  - `yarn --cwd ide/theia-frontend build`
- Completion gate:
  - `cmd /c "call java25 && .\gradlew.bat --no-daemon --console=plain test"`
- Keep Gradle verification sequential on Windows. Do not overlap build, test, or run commands in parallel shells.

### Previous Story Intelligence

- Story 2.1 established typed review-summary meaning above diff and consequence facts.
- Story 2.2 established deterministic commit intent above the same reviewed semantic change.
- Story 2.3 added optional hosted review enrichment that remains additive to the core review output.
- M5 already established repository-session, semantic inspection, and workbench projection seams that Story 2.4 should extend rather than replace.

### Git Intelligence Summary

- Current milestone baseline:
  - `a9b3a12 Complete M3 hosted extensibility proof`
  - `87b1342 Complete M2 projection proof and normalize milestone artifacts`
  - `ad382d8 Complete M1 runtime workspace and regroup modules`
- Practical implication:
  - extend the existing IDE bridge instead of introducing a second product path
  - keep semantic SCM state authored by runtime and projected by Theia

### Latest Technical Information

- No extra web research is required for this story.
- The relevant stack is already frozen locally:
  - Java `25`
  - Kotlin `2.4.0`
  - Gradle `9.6.1`
  - Node `22+`
  - Theia `1.73.1`

### Project Structure Notes

- `.codegraph/` exists in this repository and should continue to be used first when locating or understanding code areas.
- Root package remains `com.engineeringood`.

### References

- [Source: _bmad-output/planning-artifacts/epics-M6-2026-07-09.md]
- [Source: _bmad-output/planning-artifacts/prds/prd-Athena-2026-07-09-m6/prd.md]
- [Source: _bmad-output/planning-artifacts/prds/prd-Athena-2026-07-09-m6/addendum.md]
- [Source: _bmad-output/planning-artifacts/architecture/architecture-Athena-2026-07-09-m6/ARCHITECTURE-SPINE.md]
- [Source: _bmad-output/implementation-artifacts/m6/2-1-generate-semantic-review-summaries-from-typed-change-records.md]
- [Source: _bmad-output/implementation-artifacts/m6/2-2-prepare-deterministic-commit-intent-from-semantic-change.md]
- [Source: _bmad-output/implementation-artifacts/m6/2-3-allow-hosted-plugins-to-enrich-review-output-without-rewriting-core-facts.md]
- [Source: ide/lsp/src/main/kotlin/com/engineeringood/athena/ide/lsp/AthenaLanguageServer.kt]
- [Source: ide/theia-backend/src/node/athena-repository-session-manager.ts]
- [Source: ide/theia-frontend/src/browser/athena-lsp-editor-bridge-service.ts]

## Story Completion Status

- Status: review
- Completion note: Runtime-owned semantic SCM projection is now available through `:kernel:runtime`, `:ide:lsp`, and the existing Athena Theia workbench via the additive `Semantic SCM` panel, with full Java 25 and desktop smoke verification complete.

## Dev Agent Record

### Agent Model Used

GPT-5 Codex

### Debug Log References

- M6 epic, PRD, and architecture review for runtime/LSP/Theia semantic SCM projection constraints
- CodeGraph plus source inspection over `RepositoryGraphSession`, `AthenaLanguageServer`, `AthenaLspSessionHost`, the Theia LSP bridge, and current workbench widgets
- `cmd /c "call java25 && .\gradlew.bat --no-daemon --console=plain :kernel:runtime:test :ide:lsp:test"`
- `yarn --cwd ide build`
- `yarn --cwd ide/theia-backend test`
- `yarn --cwd ide start:smoke`
- `cmd /c "call java25 && .\gradlew.bat --no-daemon --console=plain :integrations:scm-git:test :kernel:semantic-scm:test :kernel:repository-model:test :kernel:compiler:test"`
- `cmd /c "call java25 && .\gradlew.bat --no-daemon --console=plain test"`

### Completion Notes List

- Added `AthenaSemanticScmStateService` so runtime now resolves one typed baseline-driven semantic SCM state object instead of forcing LSP or Theia to reconstruct review and commit meaning.
- Added the `athena/semanticScmState` LSP request plus typed protocol payloads, and configured the default IDE runtime host with the existing `scm-git` baseline adapter so the IDE path can resolve real baseline-backed review and commit state.
- Added focused runtime and LSP tests proving deterministic semantic SCM projection and unresolved-baseline diagnostics through the new request surface.
- Extended the Theia frontend bridge with typed semantic SCM payloads and added a dedicated `Semantic SCM` workbench panel that projects baseline diagnostics, review entries, enrichments, and commit-preparation output without creating a parallel frontend semantic model.
- Rebuilt the Theia frontend and product bundles, updated the Athena home surface to M6, and refreshed the live English docs for the root workspace, `ide/`, `ide/lsp`, `kernel/runtime`, and the current workspace summary.

### File List

- _bmad-output/implementation-artifacts/m6/2-4-expose-review-and-commit-semantics-through-runtime-lsp-and-existing-ide-seams.md
- _bmad-output/implementation-artifacts/m6/sprint-status.yaml
- README.md
- docs/usages/athena-workspace-summary.md
- ide/README.md
- ide/lsp/README.md
- ide/lsp/build.gradle.kts
- ide/lsp/src/main/kotlin/com/engineeringood/athena/ide/lsp/AthenaLanguageServer.kt
- ide/lsp/src/main/kotlin/com/engineeringood/athena/ide/lsp/AthenaLspSessionHost.kt
- ide/lsp/src/main/kotlin/com/engineeringood/athena/ide/lsp/AthenaSemanticScmProtocol.kt
- ide/lsp/src/test/kotlin/com/engineeringood/athena/ide/lsp/AthenaSemanticScmStateRequestTest.kt
- ide/theia-frontend/src/browser/athena-lsp-editor-bridge-service.ts
- ide/theia-frontend/src/browser/athena-semantic-scm-widget.tsx
- ide/theia-frontend/src/browser/athena-frontend-module.ts
- ide/theia-frontend/src/browser/athena-home-widget.tsx
- ide/theia-frontend/src/browser/athena-workbench-extensions.ts
- ide/theia-frontend/src/browser/style/index.css
- ide/theia-frontend/lib/browser/athena-lsp-editor-bridge-service.*
- ide/theia-frontend/lib/browser/athena-semantic-scm-widget.*
- ide/theia-frontend/lib/browser/athena-frontend-module.*
- ide/theia-frontend/lib/browser/athena-home-widget.*
- ide/theia-frontend/lib/browser/athena-workbench-extensions.*
- ide/theia-product/lib/backend/main.js*
- ide/theia-product/lib/frontend/bundle.css*
- ide/theia-product/lib/frontend/bundle.js*
- kernel/runtime/README.md
- kernel/runtime/src/main/kotlin/com/engineeringood/athena/runtime/AthenaSemanticScmStateService.kt
- kernel/runtime/src/main/kotlin/com/engineeringood/athena/runtime/AthenaServiceRegistry.kt
- kernel/runtime/src/test/kotlin/com/engineeringood/athena/runtime/AthenaRuntimeTest.kt
- kernel/runtime/src/test/kotlin/com/engineeringood/athena/runtime/AthenaSemanticScmStateServiceTest.kt

### Change Log

- 2026-07-09: Added runtime-owned semantic SCM projection, additive LSP request/response contracts, Theia workbench projection, focused cross-layer tests, rebuilt frontend/product bundles, and refreshed the live M6 English docs.
