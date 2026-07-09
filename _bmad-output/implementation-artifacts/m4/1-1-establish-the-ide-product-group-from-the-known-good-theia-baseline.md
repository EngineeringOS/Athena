---
baseline_commit: a9b3a12593a428305bb342f3594226aff1120de2
---

# Story 1.1: Establish The `ide/` Product Group From The Known-Good Theia Baseline

Status: done

## Story

As a platform engineer,
I want Athena to introduce a dedicated `ide/` product group using the known-good local Theia sample as the structural baseline,
so that the main IDE path has a maintainable foundation instead of being scattered across legacy app shells.

## Acceptance Criteria

1. Given the current Athena workspace and the known-good local reference at `D:/Aaron/workspace/projects/2026/eos/theia-ide`, when the first M4 product modules are introduced, then Athena adds an `ide/` group with the minimal seed `theia-product`, `theia-frontend`, `theia-backend`, and `lsp`, and the module shape follows the proven Theia sample closely enough to avoid inventing a new product layout from scratch.
2. Given the new `ide/` group exists, when ownership boundaries are reviewed, then `apps/cli`, `apps/desktop-viewer`, and `ui/compose-workbench` remain secondary proof shells, and the primary IDE path is clearly centered under `ide/`.

## Tasks / Subtasks

- [x] Replace the current `ide/` placeholder with a real grouped seed. (AC: 1, 2)
  - [x] Replace the stub [`ide/README.md`](../../../ide/README.md) with a proper group overview and add `ide/README.zh-CN.md`.
  - [x] Create the physical directories `ide/theia-product/`, `ide/theia-frontend/`, `ide/theia-backend/`, and `ide/lsp/`.
  - [x] Add English and Chinese README files for each seeded M4 module directory so the ownership split is visible before implementation deepens.
- [x] Mirror the known-good Theia sample at the structural level without over-implementing M4 early. (AC: 1)
  - [x] Add a lightweight Node workspace seed under `ide/` that follows the sibling `theia-ide` reference closely enough to support later Theia work, for example a local `package.json` with workspaces and shared metadata.
  - [x] Keep `ide/theia-product` as the product-composition and packaging home, and keep `ide/theia-frontend` / `ide/theia-backend` as Theia contribution homes rather than collapsing everything into one folder.
  - [x] Keep `ide/lsp` as the Athena-owned semantic-service boundary under `ide/`, but do not implement the real LSP server in Story `1.1`; Story `2.1` owns that.
  - [x] Do not create a fake runnable Theia shell, Electron package, or dependency-installed product in this story; Story `1.2` and Story `1.5` own launchability and build proof.
- [x] Preserve cross-build boundary correctness. (AC: 1, 2)
  - [x] Do not model Theia frontend, backend, or product packages as Gradle modules just to make the tree look symmetrical.
  - [x] Do not wire Node/TypeScript Theia code directly into `kernel/*` or `ui/*`; keep the future semantic contract boundary at `ide/lsp`.
  - [x] If `ide/lsp` is only a reserved directory in this story, do not register a fake Gradle module for it yet.
- [x] Update workspace maps so the repo tells the same story everywhere. (AC: 2)
  - [x] Update [`README.md`](../../../README.md) and [`README.zh-CN.md`](../../../README.zh-CN.md) to include the `ide/` group and to mark `apps/cli`, `apps/desktop-viewer`, and `ui/compose-workbench` as secondary proof shells during M4.
  - [x] Update [`DEV.md`](../../../DEV.md) so the top-level grouped layout includes `ide/` alongside `kernel/`, `extensions/`, `ui/`, and `apps/`.
  - [x] Update [`docs/usages/athena-workspace-summary.md`](../../../docs/usages/athena-workspace-summary.md) so the workspace shape reflects the new `ide/` ownership boundary.
- [x] Keep Story `1.1` narrow. (AC: 1, 2)
  - [x] Do not implement branding, welcome UX, curated capability selection, repository open/create, or language tooling in this story.
  - [x] Do not replace or remove the existing runnable proof shells.
  - [x] Do not let future graph or projection work leak into this story; M4 remains text/LSP-first and later graphical projection remains deferred.

## Dev Notes

### Story Intent

- Story `1.1` is the M4 substrate entry point. Its job is to make the `ide/` group real and legible before any branded shell, Theia runtime wiring, repository flow, or LSP integration begins.
- The current workspace is already in an in-between state:
  - `ide/` physically exists
  - [`ide/README.md`](../../../ide/README.md) is only a short placeholder
  - [`settings.gradle.kts`](../../../settings.gradle.kts) still centers the executable workspace around `apps`, `ui`, `kernel`, and `extensions`
  - [`README.md`](../../../README.md) and [`DEV.md`](../../../DEV.md) do not yet describe `ide/` as a first-class group
- This story must resolve that mismatch without pretending the Theia product is already implemented.
- Story `1.2` owns the first branded Athena shell and curated capability set.
- Story `1.3` owns opening an existing Engineering Repository.
- Story `1.4` owns creating a new Engineering Repository.
- Story `1.5` owns deterministic desktop-first build and launch proof.
- Story `2.1` owns the real Athena LSP server wiring.

### Architecture Guardrails

- Align to AD-1 by making `ide/` the explicit product group for the primary IDE path.
- Align to AD-2 by keeping `theia-frontend`, `theia-backend`, and `lsp` as separate ownership homes from the start.
- Align to AD-3 by treating `ide/lsp` as the only future semantic entry point for the IDE path; do not let `theia-frontend` or `theia-backend` reach into `kernel/*` directly.
- Align to AD-8 by keeping any IDE-side state as product and presentation structure only; no semantic authority moves into Theia scaffolding.
- Align to AD-10 by keeping future graph/projection work out of Story `1.1`; this milestone remains text-first, and any later graphical path must stay downstream of canonical semantic state.

### Technical Requirements

- The `ide/` group should be physical first, not imaginary:
  - real directories
  - real README coverage
  - real ownership notes
  - a real local workspace seed for later Theia development
- Follow the sibling `theia-ide` structure closely enough to inherit proven Theia product conventions:
  - a product-level package/workspace root
  - separate frontend and backend contribution homes
  - room for packaging and later Electron/browser targets
- Do not overfit Athena to the exact `theia-ide` repo layout. Athena needs the same architectural logic, not a copy-paste of upstream folder names.
- Keep `ide/lsp` explicitly Athena-owned even if it is only reserved in this story. It should not disappear into frontend or backend folders.
- Keep all new or rewritten core docs in clear English, and provide `README.zh-CN.md` alongside each new `README.md`.

### Architecture Compliance

- The story succeeds only if the repo becomes easier to reason about as a product workspace, not merely if more folders appear.
- The new `ide/` shape must make future implementation safer by preventing:
  - Theia code from spreading into `apps/`
  - frontend logic from swallowing backend concerns
  - Theia shell code from reaching directly into semantic kernel modules
  - M4 implementation from continuing to treat Compose shells as the primary IDE path
- Preserve the existing M0 to M3 proof chain exactly as-is. Story `1.1` is additive structure, not replacement.

### Library / Framework Requirements

- Use the current repo-approved local stack as the baseline:
  - Java `25`
  - Kotlin `2.4.0`
  - Gradle `9.6.1`
- For the Theia side, follow the validated sibling reference and current official Theia track already selected for M4:
  - Eclipse Theia `1.73.x`
  - Node `>=22`
  - Yarn classic `>=1.7.0 <2` where Athena chooses to mirror the official sample workspace shape
- Do not install or bootstrap the full Theia dependency tree in Story `1.1` unless a minimal manifest sanity check is truly required.
- Do not add new JVM libraries in this story.

### File Structure Requirements

- Expected new files and directories:
  - `ide/README.zh-CN.md`
  - `ide/package.json` if Athena seeds the Node workspace at the group root
  - `ide/theia-product/README.md`
  - `ide/theia-product/README.zh-CN.md`
  - `ide/theia-frontend/README.md`
  - `ide/theia-frontend/README.zh-CN.md`
  - `ide/theia-backend/README.md`
  - `ide/theia-backend/README.zh-CN.md`
  - `ide/lsp/README.md`
  - `ide/lsp/README.zh-CN.md`
- Expected update files:
  - `ide/README.md`
  - `README.md`
  - `README.zh-CN.md`
  - `DEV.md`
  - `docs/usages/athena-workspace-summary.md`
- Files whose current behavior or role must be preserved:
  - [`settings.gradle.kts`](../../../settings.gradle.kts)
    - It currently defines the JVM/Gradle module graph only. Do not pollute it with Node packages that are not real Gradle modules.
  - [`build.gradle.kts`](../../../build.gradle.kts)
    - Keep Java 25 enforcement and current shared Kotlin/JUnit conventions intact.
  - [`apps/cli/`](../../../apps/cli/README.md)
    - Remains a secondary shell and verification surface.
  - [`apps/desktop-viewer/`](../../../apps/desktop-viewer/README.md)
    - Remains the existing desktop proof shell until the Theia product supersedes it.
  - [`ui/compose-workbench/`](../../../ui/compose-workbench/README.md)
    - Remains shared Compose workbench infrastructure, not the primary IDE product path.
- Explicit non-goals for this story:
  - no runnable Athena Theia product
  - no Electron packaging
  - no Athena LSP server implementation
  - no repository open/create flow
  - no diagnostics, completion, or navigation work

### Testing Requirements

- Minimum verification commands for story completion:
  - `java25`
  - `.\gradlew.bat --no-daemon --console=plain build`
  - `Get-ChildItem ide -Recurse`
- Required proof checks:
  - the physical `ide/` seed contains `theia-product`, `theia-frontend`, `theia-backend`, and `lsp`
  - root and group documentation consistently identify `ide/` as the primary M4 IDE path
  - `apps/cli`, `apps/desktop-viewer`, and `ui/compose-workbench` remain documented as secondary proof shells during M4
  - the existing JVM workspace still builds successfully on Java 25 after the structural update
- Keep Gradle verification sequential on Windows. Do not overlap Gradle build, test, or run commands in parallel shells.

### Current Code State To Preserve

- [`settings.gradle.kts`](../../../settings.gradle.kts) currently includes no `:ide:*` modules.
- The root repo already has a physical [`ide/`](../../../ide/README.md) directory, but it is only a stub and not yet reflected in the main workspace maps.
- [`README.md`](../../../README.md) currently describes the grouped module graph as `kernel`, `extensions`, `ui`, and `apps`.
- [`DEV.md`](../../../DEV.md) still says Gradle modules are grouped logically as `kernel`, `extensions`, `ui`, and `apps`.
- [`docs/usages/athena-workspace-summary.md`](../../../docs/usages/athena-workspace-summary.md) does not yet list `ide/` as a top-level repo role.
- The sibling `D:/Aaron/workspace/projects/2026/eos/theia-ide` workspace already contains a known-good Theia product baseline with:
  - a product-level repository root
  - separate application and extension areas
  - Node `>=22`
  - Yarn classic
  - `@theia/cli` on the `1.73.x` line

### Previous Story Intelligence

- M3 established the current grouped physical workspace discipline:
  - `kernel/`
  - `extensions/`
  - `ui/`
  - `apps/`
- The user explicitly wants physical directory structure to match the intended architecture, not only Gradle aliases in `settings.gradle.kts`.
- The user also explicitly wants every group and module to keep English and Chinese README coverage.
- The repo rule about Java `25` and sequential Gradle execution on Windows remains non-negotiable even for a docs-and-structure story like this one.

### Git Intelligence Summary

- Recent commits confirm that the current baseline is still centered on the completed kernel/runtime/plugin milestones:
  - `a9b3a12 Complete M3 hosted extensibility proof`
  - `1339722 feat(m3): establish kernel plugin api boundary`
  - `87b1342 Complete M2 projection proof and normalize milestone artifacts`
  - `ad382d8 Complete M1 runtime workspace and regroup modules`
- Practical guidance for Story `1.1` should therefore come from the current working tree plus the new M4 planning artifacts, not from trying to retrofit Theia into older module assumptions.

### Latest Technical Information

- The official Theia platform documentation describes Theia as a platform for building custom cloud and desktop IDEs and tools, which matches Athena's M4 product direction.
- The official Theia architecture documentation keeps the frontend/backend split explicit, which matches the M4 ownership model.
- The local `theia-ide` reference currently tracks the `1.73.x` Theia line and requires Node `>=22`, with Yarn classic workspace management in its root package metadata.
- For Story `1.1`, treat those facts as structural guidance only. Do not turn this story into a dependency-upgrade or packaging milestone.

### Project Structure Notes

- No `project-context.md` file exists in the repository.
- Story `1.1` should make the workspace more truthful:
  - `ide/` becomes real as a first-class product group
  - the old proof shells remain present
  - the docs stop pretending that the future IDE path still lives only under `apps/` or `ui/`
- The safest implementation is a minimal but durable seed that future stories can build on without moving directories again.

### References

- [Source: _bmad-output/planning-artifacts/epics.md#epic-1-launch-and-open-athena-as-a-real-ide]
- [Source: _bmad-output/planning-artifacts/epics.md#story-11-establish-the-ide-product-group-from-the-known-good-theia-baseline]
- [Source: _bmad-output/planning-artifacts/prds/prd-Athena-2026-07-08/prd.md]
- [Source: _bmad-output/planning-artifacts/prds/prd-Athena-2026-07-08/addendum.md]
- [Source: _bmad-output/planning-artifacts/architecture/architecture-Athena-2026-07-08/ARCHITECTURE-SPINE.md]
- [Source: _bmad-output/planning-artifacts/implementation-readiness-report-2026-07-08.md]
- [Source: draft/m4/001-draft.md]
- [Source: draft/m4/002-glsp.md]
- [Source: README.md]
- [Source: DEV.md]
- [Source: docs/usages/athena-workspace-summary.md]
- [Source: ide/README.md]
- [Source: settings.gradle.kts]
- [Source: build.gradle.kts]
- [Source: D:/Aaron/workspace/projects/2026/eos/theia-ide/README.md]
- [Source: D:/Aaron/workspace/projects/2026/eos/theia-ide/package.json]

## Story Completion Status

- Status: review
- Completion note: Established the physical `ide/` seed, added bilingual ownership READMEs plus a lightweight Node workspace manifest, updated the root workspace maps, and verified the existing JVM build still passes on Java 25 without pulling later Theia or LSP scope forward.

## Dev Agent Record

### Agent Model Used

GPT-5 Codex

### Debug Log References

- CodeGraph read of `settings.gradle.kts`
- workspace topology inspection under repo root and `ide/`
- review of current root and group READMEs plus `DEV.md`
- review of the M4 PRD, addendum, architecture spine, epics, and implementation-readiness report
- local reference inspection of `D:/Aaron/workspace/projects/2026/eos/theia-ide/README.md` and `package.json`
- red-phase file existence assertions for the expected `ide/` seed paths
- documentation mismatch assertions against `README.md`, `DEV.md`, and `docs/usages/athena-workspace-summary.md`
- `Get-Content ide/package.json | ConvertFrom-Json | Out-Null`
- `Get-ChildItem ide -Recurse | Select-Object FullName`
- `java25; .\gradlew.bat --no-daemon --console=plain build`

### Completion Notes List

- Replaced the placeholder `ide/README.md` with a real group overview and added `ide/README.zh-CN.md`.
- Added the physical seed directories `ide/theia-product/`, `ide/theia-frontend/`, `ide/theia-backend/`, and `ide/lsp/`.
- Added bilingual README coverage for each M4 IDE seed directory.
- Added a lightweight `ide/package.json` workspace manifest aligned to the known-good Theia baseline without registering fake Gradle modules.
- Updated `README.md`, `README.zh-CN.md`, `DEV.md`, and `docs/usages/athena-workspace-summary.md` so the repo now describes `ide/` as the primary M4 IDE path and keeps the older shells explicitly secondary.
- Preserved story scope by not introducing runnable Theia code, Electron packaging, or Athena LSP implementation.

## File List

- DEV.md
- README.md
- README.zh-CN.md
- _bmad-output/implementation-artifacts/m4/1-1-establish-the-ide-product-group-from-the-known-good-theia-baseline.md
- _bmad-output/implementation-artifacts/m4/sprint-status.yaml
- docs/usages/athena-workspace-summary.md
- ide/README.md
- ide/README.zh-CN.md
- ide/lsp/README.md
- ide/lsp/README.zh-CN.md
- ide/package.json
- ide/theia-backend/README.md
- ide/theia-backend/README.zh-CN.md
- ide/theia-frontend/README.md
- ide/theia-frontend/README.zh-CN.md
- ide/theia-product/README.md
- ide/theia-product/README.zh-CN.md

## Change Log

- 2026-07-08: Established the physical `ide/` product seed, added bilingual ownership docs plus a lightweight Theia workspace manifest, and updated workspace documentation to position `ide/` as the primary M4 IDE path while keeping existing shells secondary.
