---
baseline_commit: a9b3a12593a428305bb342f3594226aff1120de2
---

# Story 1.2: Launch A Branded Athena Theia Desktop Shell With A Curated Capability Set

Status: done

## Story

As an engineer,
I want Athena to launch as a branded Theia desktop product with an Athena-owned capability set,
so that the product feels like a real engineering IDE instead of a generic upstream shell.

## Acceptance Criteria

1. Given the `ide/` product modules exist, when the Athena desktop product is launched, then it opens as a branded Athena shell rather than a generic upstream Theia demo, and product identity, top-level shell flow, and core workbench framing are controlled by Athena.
2. Given the first product shell is composed, when bundled capabilities are selected, then only the capabilities needed for repository opening, source editing, diagnostics, semantic inspection, console visibility, and workbench operation are shipped, and marketplace-style extension sprawl is not part of the M4 proof.

## Tasks / Subtasks

- [x] Turn the `ide/` seed into the first runnable Athena Theia desktop product shell. (AC: 1)
  - [x] Replace README-only placeholders in [`ide/theia-product/`](../../../ide/theia-product/README.md), [`ide/theia-frontend/`](../../../ide/theia-frontend/README.md), and [`ide/theia-backend/`](../../../ide/theia-backend/README.md) with real Node/TypeScript Theia packages while preserving those directories as the ownership homes established in Story `1.1`.
  - [x] Extend [`ide/package.json`](../../../ide/package.json) into a real Yarn classic workspace root with root scripts for install, build, and desktop launch instead of leaving it as a seed manifest only.
  - [x] Keep the desktop-first proof explicit by wiring the first runnable path as an Athena-owned desktop product, not as a browser-first proof and not as a VS Code extension wrapper.
  - [x] Keep [`ide/lsp/`](../../../ide/lsp/README.md) as a reserved future semantic boundary only; do not implement or fake the real Athena LSP server in Story `1.2`.
- [x] Make the launched shell visibly Athena-owned instead of an upstream Theia demo. (AC: 1)
  - [x] Replace upstream product naming, window title, welcome framing, and obvious Theia sample identity with Athena branding.
  - [x] Provide a branded no-repository landing state that points to the later open/create repository actions without implementing Story `1.3` or Story `1.4` early.
  - [x] Remove or suppress generic upstream demo copy, unowned sample commands, and shell affordances that make the product read like a stock Theia sample.
- [x] Define and enforce the curated M4 capability set in the product shell. (AC: 2)
  - [x] Include only the capabilities needed for the current M4 proof surface: repository-opening entry points, text editing, diagnostics-facing workbench surfaces, semantic-inspection attachment points, console/output visibility, and core workbench operation.
  - [x] Exclude marketplace-style extension management, random bundled language packs, wide feature sprawl, and unrelated product surfaces that are not needed for the current proof.
  - [x] Keep the capability set Athena-owned in product composition instead of delegating product shape to end-user extension installation.
- [x] Preserve M4 architecture boundaries while making the shell real. (AC: 1, 2)
  - [x] Keep Theia frontend concerns in `ide/theia-frontend` and startup/process concerns in `ide/theia-backend`; do not collapse them into one product package just to get the first launch working.
  - [x] Do not add direct Node/TypeScript imports into `kernel/*`; future semantic access must still cross the `ide/lsp` protocol boundary.
  - [x] Do not implement repository-session activation, repository creation, diagnostics production, navigation, or semantic inspection data transport in this story; later stories own those behaviors.
  - [x] Keep the workbench framing intentionally early and professional, but do not smuggle Epic `3` layout ambition, final UX-system work, or graph/projection scope into this story.
- [x] Update documentation so the repo reflects the first runnable product shell truthfully. (AC: 1, 2)
  - [x] Update [`ide/README.md`](../../../ide/README.md) and [`ide/README.zh-CN.md`](../../../ide/README.zh-CN.md) so they no longer describe the `ide/` group as structure-only once the shell can run.
  - [x] Add or update module-level English and Chinese README files in the touched `ide/*` packages so package responsibility and launch role stay legible.
  - [x] Document the curated capability intent and explicit exclusions near the product package instead of leaving them implicit in package dependencies only.
- [x] Verify the first branded shell without claiming Story `1.5` is already complete. (AC: 1, 2)
  - [x] Run the new `ide/` install, build, and desktop launch commands sequentially on Windows.
  - [x] Manually confirm that the launched shell is visibly Athena-branded and that the shell surface is narrower than a generic Theia sample.
  - [x] Capture the exact launch command and any workstation-specific notes needed for the next story, but leave full stabilization and deterministic proof-path hardening to Story `1.5`.

## Dev Notes

### Story Intent

- Story `1.2` is the first real product-shell story in M4. Story `1.1` created the ownership skeleton; Story `1.2` must prove that the skeleton can become a runnable Athena-owned desktop shell.
- The value of this story is product embodiment, not repository semantics yet:
  - the shell must launch
  - the shell must look Athena-owned
  - the bundled capabilities must feel deliberately chosen
- Story `1.3` owns opening an existing Engineering Repository into an active Repository Session.
- Story `1.4` owns creating a new Engineering Repository from the welcome flow.
- Story `1.5` owns deterministic desktop-first build, launch, and verification hardening.
- Story `2.1` owns the real Athena LSP server and the semantic boundary wiring.
- Failure-path refinement from the readiness review should be carried into implementation here:
  - if no repository is active, the shell should still land in a branded Athena state rather than an error or a raw upstream page
  - if some optional capability is not bundled, the shell should fail narrow rather than silently re-expand into stock upstream behavior

### Architecture Guardrails

- Align to AD-1 by keeping `ide/` as the main product group for the IDE path.
- Align to AD-2 by keeping `ide/theia-product`, `ide/theia-frontend`, and `ide/theia-backend` as distinct ownership homes even in the first runnable shell.
- Align to AD-3 by treating `ide/lsp` as the only future semantic entry point; Story `1.2` must not create direct frontend/backend calls into `kernel/*`.
- Align to AD-7 by making the M4 capability set curated and product-owned rather than marketplace-driven.
- Align to AD-8 by keeping the shell downstream of runtime and compiler authority even though no real semantic transport is implemented yet.
- Align to AD-10 by keeping graph or projection behavior out of Story `1.2`; M4 remains text/LSP-first.

### Technical Requirements

- Build on the sibling `../theia-ide` reference as the known-good Theia desktop product baseline, but map the product into Athena's `ide/` grouping instead of cloning the upstream folder names.
- Keep the package namespace Athena-owned and straightforward. The expected direction is:
  - `@engineeringood/athena-ide` at the `ide/` workspace root
  - `@engineeringood/athena-theia-product`
  - `@engineeringood/athena-theia-frontend`
  - `@engineeringood/athena-theia-backend`
- Keep package management on the Node side only:
  - Yarn classic workspace management under `ide/`
  - no Gradle registration for Theia packages
  - no direct JVM source-set coupling
- Prefer one explicit desktop launch path for this story. Browser-first proof remains out of scope.
- The first runnable shell should establish root scripts at `ide/package.json` for:
  - install/bootstrap
  - build
  - desktop start or preview start
- Keep the product shell small. The goal is a credible Athena-owned Theia shell, not a full parity shell with Theia IDE.

### Architecture Compliance

- The story succeeds only if a reviewer can clearly distinguish Athena's product shell from:
  - the old Compose proof shells
  - a raw upstream Theia sample
  - a VS Code extension-hosting mindset
- The shell must remain legible as a product boundary:
  - product composition stays in `ide/theia-product`
  - workbench-facing contributions stay in `ide/theia-frontend`
  - startup and process wiring stay in `ide/theia-backend`
- Preserve future story space:
  - do not consume Story `1.3` or Story `1.4` by hard-wiring repository semantics here
  - do not consume Story `2.1` by adding fake or partial LSP semantics here
  - do not consume Epic `3` by overcommitting to final workbench composition here

### Library / Framework Requirements

- Use the current repo-approved baseline:
  - Java `25`
  - Kotlin `2.4.0`
  - Gradle `9.6.1`
- On the Theia side, stay aligned with the current M4 planning baseline and the known-good local sample:
  - Eclipse Theia `1.73.x`
  - `@theia/cli` on the `1.73.x` line
  - Node `>=22`
  - Yarn classic `>=1.7.0 <2`
- Do not introduce broad extra framework churn in this story. Theia should be the product foundation; additional frontend frameworks or styling systems are not the point of Story `1.2`.

### File Structure Requirements

- Existing files that this story is expected to update:
  - [`ide/package.json`](../../../ide/package.json)
  - [`ide/README.md`](../../../ide/README.md)
  - [`ide/README.zh-CN.md`](../../../ide/README.zh-CN.md)
  - [`ide/theia-product/README.md`](../../../ide/theia-product/README.md)
  - [`ide/theia-product/README.zh-CN.md`](../../../ide/theia-product/README.zh-CN.md)
  - [`ide/theia-frontend/README.md`](../../../ide/theia-frontend/README.md)
  - [`ide/theia-frontend/README.zh-CN.md`](../../../ide/theia-frontend/README.zh-CN.md)
  - [`ide/theia-backend/README.md`](../../../ide/theia-backend/README.md)
  - [`ide/theia-backend/README.zh-CN.md`](../../../ide/theia-backend/README.zh-CN.md)
- Expected new package structure areas:
  - `ide/theia-product/package.json`
  - `ide/theia-product/src/` or equivalent Theia product configuration home
  - `ide/theia-frontend/package.json`
  - `ide/theia-frontend/src/browser/`
  - `ide/theia-backend/package.json`
  - `ide/theia-backend/src/node/`
- Files whose current role must be preserved:
  - [`ide/lsp/README.md`](../../../ide/lsp/README.md)
    - remains a reserved future semantic boundary, not a fake implementation target in this story
  - [`settings.gradle.kts`](../../../settings.gradle.kts)
    - still models only the JVM/Gradle module graph
  - [`apps/desktop-viewer/`](../../../apps/desktop-viewer/README.md)
    - remains a secondary proof shell until M4 later stories prove the Theia shell is the primary product path in practice
  - [`ui/compose-workbench/`](../../../ui/compose-workbench/README.md)
    - remains shared proof UI infrastructure, not the new primary IDE shell
- Explicit non-goals for this story:
  - no Engineering Repository session activation
  - no repository creation contract
  - no Athena LSP server implementation
  - no real diagnostics, completion, or navigation logic
  - no final visual system or emotion-system work
  - no graph server or graphical editor

### Testing Requirements

- Minimum verification commands for story completion:
  - `java25`
  - `Set-Location ide; yarn install`
  - `Set-Location ide; yarn build`
  - `Set-Location ide; yarn start` or the story's equivalent explicit desktop launch script
- Required proof checks:
  - the shell launches as Athena rather than a generic Theia-branded demo
  - the shell contains only the intentionally curated capability set for the current proof
  - the shell can show a branded landing or welcome state without requiring a repository to already be open
  - `ide/lsp` remains only a reserved semantic boundary in this story
  - no Theia package directly imports `kernel/*`
- Windows workflow rule:
  - keep Gradle and Yarn verification sequential
  - do not run build, test, or launch commands for different toolchains in parallel shells
  - record any Windows-specific Theia or Electron constraint encountered during bring-up for Story `1.5`

### Current Code State To Preserve

- The current `ide/` group is physical but still structure-only:
  - [`ide/package.json`](../../../ide/package.json) exists only as a minimal workspace seed
  - `ide/theia-product`, `ide/theia-frontend`, and `ide/theia-backend` still contain README-only placeholders
- The workspace documentation already says there is no runnable Athena Theia product yet; Story `1.2` must make that statement obsolete in a controlled way.
- The naming convention just normalized for M4 must be preserved:
  - filesystem and module path: `lsp`
  - protocol or prose term: `LSP`
  - type name form: `AthenaLSPServer`

### Previous Story Intelligence

- Story `1.1` established the physical `ide/` seed, bilingual README coverage, and the root `ide/package.json` workspace manifest.
- The user explicitly prefers real physical workspace structure over virtual grouping tricks.
- The user has already rejected treating Theia as an extension-product mindset; Story `1.2` must read as a product-shell story.
- The user also explicitly wants module naming and grouping to stay straightforward and high quality; avoid vague package names or mixed ownership.
- Java `25` remains non-negotiable, and Windows command execution must stay sequential.

### Git Intelligence Summary

- The current baseline commit history still reflects the completed semantic milestones rather than earlier IDE work:
  - `a9b3a12 Complete M3 hosted extensibility proof`
  - `1339722 feat(m3): establish kernel plugin api boundary`
  - `dfc0234 add reference`
  - `87b1342 Complete M2 projection proof and normalize milestone artifacts`
  - `ad382d8 Complete M1 runtime workspace and regroup modules`
- Practical implication:
  - Story `1.2` is the first point where Athena should become runnable as a Theia product
  - implementation should be additive and should not disturb the completed kernel/runtime/plugin proof chain

### Latest Technical Information

- The current Theia platform documentation still positions Theia as the foundation for custom cloud and desktop IDEs and tools, which matches Athena's M4 product direction.
- The current Theia architecture documentation still keeps the frontend/backend split explicit, which matches the M4 ownership model.
- The current Theia development prerequisites still require Node `>=22`.
- The local known-good `theia-ide` sample is already on the `1.73.x` line and provides the safest immediate product composition reference for this story.

### Project Structure Notes

- No M4-specific UX contract exists. Keep product choices professional and deliberate, but do not overfit visual polish or emotion-system work into Story `1.2`.
- Keep the first landing experience intentionally small:
  - branded
  - Athena-owned
  - ready for later repository open/create actions
- The story should leave the repo in a better state for the next steps:
  - Story `1.3` can attach repository opening cleanly
  - Story `1.4` can attach repository creation cleanly
  - Story `2.1` can attach the real LSP boundary cleanly

### References

- [Source: _bmad-output/planning-artifacts/epics.md#epic-1-launch-and-open-athena-as-a-real-ide]
- [Source: _bmad-output/planning-artifacts/epics.md#story-12-launch-a-branded-athena-theia-desktop-shell-with-a-curated-capability-set]
- [Source: _bmad-output/planning-artifacts/prds/prd-Athena-2026-07-08/prd.md]
- [Source: _bmad-output/planning-artifacts/prds/prd-Athena-2026-07-08/addendum.md]
- [Source: _bmad-output/planning-artifacts/architecture/architecture-Athena-2026-07-08/ARCHITECTURE-SPINE.md]
- [Source: _bmad-output/planning-artifacts/implementation-readiness-report-2026-07-08.md]
- [Source: _bmad-output/implementation-artifacts/m4/1-1-establish-the-ide-product-group-from-the-known-good-theia-baseline.md]
- [Source: ide/package.json]
- [Source: ide/README.md]
- [Source: ide/theia-product/README.md]
- [Source: ide/theia-frontend/README.md]
- [Source: ide/theia-backend/README.md]
- [Source: ../theia-ide/README.md]
- [Source: ../theia-ide/package.json]

## Story Completion Status

- Status: review
- Completion note: Built the first runnable Athena Theia desktop shell, added branded frontend and backend contributions, updated the `ide/` workspace and docs, and verified both the Theia build/start path and the existing Java 25 Gradle workspace build.

## Dev Agent Record

### Agent Model Used

GPT-5 Codex

### Debug Log References

- `yarn install --force` after the initial Windows `EBUSY` native dependency lock during `drivelist` rebuild
- `yarn build`
- `yarn start`
- `java25; .\gradlew.bat --no-daemon --console=plain build`

### Completion Notes List

- Converted the `ide/` seed into a real Yarn workspace with active `theia-product`, `theia-frontend`, and `theia-backend` packages.
- Added the first branded Athena Home frontend surface and explicit Athena-owned product contribution wiring.
- Added a thin backend contribution so startup and process ownership are explicit on the Node side without pulling repository or LSP semantics forward.
- Kept `ide/lsp` reserved and did not introduce direct `kernel/*` imports from the Theia packages.
- Updated the root and `ide/` documentation to reflect that the first runnable Theia shell now exists while repository flow and semantic transport remain deferred.
- Verified the Theia shell reaches a ready frontend/backend startup state and that the JVM workspace still builds successfully on Java 25.

## File List

- README.md
- _bmad-output/implementation-artifacts/m4/1-2-launch-a-branded-athena-theia-desktop-shell-with-a-curated-capability-set.md
- _bmad-output/implementation-artifacts/m4/sprint-status.yaml
- docs/usages/athena-workspace-summary.md
- ide/README.md
- ide/README.zh-CN.md
- ide/package.json
- ide/theia-backend/README.md
- ide/theia-backend/README.zh-CN.md
- ide/theia-backend/package.json
- ide/theia-backend/src/node/athena-backend-contribution.ts
- ide/theia-backend/src/node/athena-backend-module.ts
- ide/theia-backend/tsconfig.json
- ide/theia-frontend/README.md
- ide/theia-frontend/README.zh-CN.md
- ide/theia-frontend/package.json
- ide/theia-frontend/src/browser/athena-frontend-module.ts
- ide/theia-frontend/src/browser/athena-home-widget.tsx
- ide/theia-frontend/src/browser/athena-product-contribution.ts
- ide/theia-frontend/src/browser/style/index.css
- ide/theia-frontend/tsconfig.json
- ide/theia-product/README.md
- ide/theia-product/README.zh-CN.md
- ide/theia-product/package.json
- ide/theia-product/scripts/athena-electron-main.js
- ide/tsconfig.base.json
- ide/yarn.lock

## Change Log

- 2026-07-08: Implemented the first branded Athena Theia desktop shell, added Theia workspace packages and launch scripts, refreshed `ide/` documentation, and verified both Theia and Java 25 workspace builds.
