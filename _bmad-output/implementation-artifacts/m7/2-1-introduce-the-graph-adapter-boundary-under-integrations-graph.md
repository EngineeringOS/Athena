---
baseline_commit: adb0ae5d1956b13d8aa3df8774e102dfc09ef380
---

# Story 2.1: Introduce The Graph Adapter Boundary Under `integrations/graph-*`

Status: review

## Story

As a platform engineer,
I want the first graphical framework integration to live behind a dedicated adapter seam,
so that protocol and rendering mechanics stay isolated from Athena semantic and projection contracts.

## FR Traceability

- FR-3: surface graphical views in the existing Athena workbench
- FR-4: support graphical navigation and projection-oriented inspection
- NFR-1: graphical surfaces remain downstream of canonical semantic, repository, package, and SCM meaning
- NFR-4: M7 extends the current Athena shell rather than replacing it
- NFR-6: technology choice stays disciplined and inspectable

## Acceptance Criteria

1. Given Athena is ready to connect the product shell to a graphical client, when the first graph integration is introduced, then framework-specific protocol and rendering code lives under `integrations/graph-*`, and that integration remains translation-only rather than semantic or projection authority.
2. Given adapter behavior is reviewed, when ownership boundaries are checked, then the adapter may translate protocol and rendering mechanics only, and it may not synthesize engineering truth, redefine semantic identity, or persist local state as authority.

## Tasks / Subtasks

- [x] Create the first dedicated graph adapter package under `integrations/graph-*`. (AC: 1, 2)
  - [x] Add one concrete adapter directory under `integrations/`, with the default expectation of `integrations/graph-glsp/` because the current M7 background still points to GLSP-class transport as the leading candidate.
  - [x] Add `README.md` and `README.zh-CN.md` for the new adapter package, and keep the package name and folder name lowercase and straightforward.
  - [x] Add a standalone Node/TypeScript package boundary for the adapter, for example `@engineeringood/athena-graph-glsp`, instead of burying framework code inside `ide/theia-frontend` or `ide/theia-backend`.
  - [x] Keep the adapter physically outside `ide/` even if local package wiring is needed for Theia consumption.
- [x] Publish a translation-only adapter API over existing Athena-owned projection transport. (AC: 1, 2)
  - [x] Make the adapter consume Athena-owned projection-session payloads already exposed by `ide/lsp` rather than calling `kernel/*` or inventing a second semantic transport.
  - [x] Keep graph-framework data shapes, protocol DTOs, rendering model conversion, and any canvas vocabulary inside the adapter package only.
  - [x] If adapter-local intermediate models are introduced, they must remain disposable translation artifacts and must not become a second owner of semantic or projection truth.
  - [x] Do not create a shadow `projection-model` or semantic identity model inside `integrations/graph-*`; the adapter consumes Athena-owned upstream payloads only.
- [x] Add the minimum Theia-side bridge needed to consume the adapter without turning `ide/*` into framework owners. (AC: 1, 2)
  - [x] Extend `ide/theia-frontend` with typed projection-session request helpers so frontend code can request projection state through the existing Athena LSP bridge and hand that result into the adapter package.
  - [x] Keep the adapter package from issuing direct `fetch('/athena/lsp/...')` calls, direct JVM calls, or direct filesystem access for semantic state.
  - [x] Only add backend-side bootstrap or lifecycle wiring if the chosen graph-framework path truly requires it; if added, keep it process/lifecycle-only and do not add semantic logic there.
  - [x] Do not add graphical widgets, menus, panel composition, or workbench-visible view delivery in this story; Story `2.2` owns that surface.
- [x] Keep architecture and ownership boundaries explicit across the heterogeneous workspace. (AC: 1, 2)
  - [x] Update integration and workspace docs so the repo clearly shows `integrations/graph-*` as the only allowed home for graph-framework dependencies.
  - [x] Preserve `ide/lsp` as the sole IDE semantic and projection entry point, `kernel/runtime` as the session authority, and `ide/theia-*` as downstream product-hosting layers.
  - [x] Preserve the current M6 pattern where Theia frontend and backend project Athena-owned state through additive bridges instead of reconstructing it privately.
  - [x] Explicitly document that the current M7 runtime/LSP projection seam exists, but the dedicated `:kernel:projection-model` module described by earlier M7 planning is not physically present yet and must not be replaced by adapter-local contracts.
- [x] Cover the adapter boundary with focused build/test proof and documentation. (AC: 1, 2)
  - [x] Add focused tests for adapter translation behavior and for any framework-local lifecycle wrapper introduced.
  - [x] Keep `ide` and `ide/lsp` verification green when the adapter package is introduced.
  - [x] Document the exact local build path for the new adapter package and any Theia package wiring needed to consume it.

## Dev Notes

### Story Intent

- Story `2.1` is the placement and ownership story for the first graphical framework adapter.
- The success condition is not "Athena already has a visible graph panel." It is "the first graph-framework code now lives behind a dedicated integration boundary and consumes Athena-owned projection state through existing seams."
- Story `2.1` must stop before graphical panel delivery, selection synchronization, renderer-proof behavior, or broad interaction design.
- Story `2.2` owns the first graphical Athena view inside the workbench.
- Story `2.3` owns synchronized selection across source, semantic inspection, semantic SCM, and graphical context.
- Story `2.4` owns inspect-first interaction rules on the actual graphical surface.
- Story `3.3` owns the formal technology-path validation and proof-corpus publication.

### Architecture Guardrails

- Align to AD-30 by keeping graph-framework code downstream of runtime-owned projection sessions and Athena-owned transport rooted at `ide/lsp`. [Source: _bmad-output/planning-artifacts/architecture/architecture-Athena-2026-07-09-m7/ARCHITECTURE-SPINE.md#AD-30]
- Align to AD-31 by keeping M7 inspect-first and preventing private frontend mutation or graph-framework-owned authority from appearing in the adapter boundary. [Source: _bmad-output/planning-artifacts/architecture/architecture-Athena-2026-07-09-m7/ARCHITECTURE-SPINE.md#AD-31]
- Align to AD-32 by keeping the first graph path relationship-forward and downstream of canonical object identities instead of drawing-file ownership. [Source: _bmad-output/planning-artifacts/architecture/architecture-Athena-2026-07-09-m7/ARCHITECTURE-SPINE.md#AD-32]
- Align to inherited AD-23 by keeping Theia-hosted surfaces as downstream bridges rather than semantic cores. [Source: _bmad-output/planning-artifacts/architecture/architecture-Athena-2026-07-09-m7/ARCHITECTURE-SPINE.md#Inherited-Invariants]
- Align to inherited AD-18 by treating IDE work as additive product-operability work over the existing shell rather than a shell replacement. [Source: _bmad-output/planning-artifacts/architecture/architecture-Athena-2026-07-09-m7/ARCHITECTURE-SPINE.md#Inherited-Invariants]
- Use `draft/m4/002-glsp.md` only as background for why GLSP-class transport is a strong candidate; do not let that draft override the M7 architecture rule that Athena-owned contracts remain upstream and the adapter remains translation-only. [Source: draft/m4/002-glsp.md]

### Technical Requirements

- Reuse the current proven projection seam instead of inventing a new one:
  - `ide/lsp` already exposes `athena/projectionSession` and governed projection commands.
  - `kernel/runtime` already owns the active projection-session lifecycle.
  - `ide/theia-backend` already owns the JVM host lifecycle and generic LSP request/notification bridge.
  - `ide/theia-frontend` already owns the browser-side typed LSP bridge service.
- The new adapter package must consume Athena-owned projection payloads from the existing frontend/backend bridge path rather than:
  - importing `kernel/*`
  - making direct filesystem reads for semantic state
  - making direct stdio/JVM calls
  - inventing a second HTTP or websocket semantic endpoint
- Keep graph-framework nouns local to the adapter package only:
  - protocol DTOs
  - rendering model translation
  - canvas or diagram model shapes
  - lifecycle wrappers specific to the chosen framework
- Do not add new public semantic or projection authority types under `integrations/graph-*`.
- Do not allow the adapter to persist authoritative local state; any translated graph state is disposable and must be rebuilt from Athena-owned upstream payloads.
- Because the physical `:kernel:projection-model` module is still absent from `settings.gradle.kts`, Story `2.1` must not smuggle that missing boundary into `integrations/graph-*` as a substitute. If implementation work discovers that the missing kernel boundary blocks correct adapter design, stop and correct course rather than hiding the gap under integration code.
- Keep all new core TypeScript or Kotlin classes documented clearly with concise comments where structure is not self-evident.

### Architecture Compliance

- The story is only successful if the ownership line becomes easier to point to:
  - `ide/lsp` and runtime remain the only semantic/projection authorities for the IDE path
  - `integrations/graph-*` becomes the only allowed home for graph-framework protocol and rendering code
  - `ide/theia-*` remains the product-hosting layer that consumes those bridges
- Prevent these failure modes:
  - graph-framework packages added directly to `ide/theia-frontend`, `ide/theia-backend`, or `ide/lsp`
  - adapter code calling Athena semantic endpoints directly instead of consuming the existing bridge service
  - adapter-local models becoming the practical owner of engineering identity or projection truth
  - a second graph-specific semantic server or transport tunnel appearing beside Athena LSP without explicit architecture review
  - Story `2.1` widening into widget delivery, renderer assets, electrical notation mapping, or selection synchronization

### Library / Framework Requirements

- Use the repo-approved stack already frozen in local planning artifacts:
  - Java `25`
  - Kotlin `2.4.0`
  - Gradle `9.6.1`
  - Node `22+`
  - Yarn `1.22.22`
  - Eclipse Theia `1.73.1`
- The first concrete graph adapter package may introduce the chosen framework dependency set locally under `integrations/graph-*`, but nowhere else.
- No new graph-framework dependency should be added to `ide/lsp`, `kernel/runtime`, or other kernel modules in this story.
- If the first adapter package uses GLSP, keep GLSP-specific protocol and server/client classes isolated inside that package and do not leak them into Athena-owned public contracts.

### File Structure Requirements

- Expected new files:
  - `integrations/graph-glsp/package.json`
  - `integrations/graph-glsp/tsconfig.json`
  - `integrations/graph-glsp/README.md`
  - `integrations/graph-glsp/README.zh-CN.md`
  - `integrations/graph-glsp/src/...`
  - focused adapter tests under `integrations/graph-glsp/`
- Expected update files:
  - `integrations/README.md`
  - `integrations/README.zh-CN.md`
  - `README.md`
  - `README.zh-CN.md`
  - `docs/usages/athena-workspace-summary.md`
  - `ide/theia-frontend/src/browser/athena-lsp-editor-bridge-service.ts`
  - `ide/theia-frontend/src/browser/athena-frontend-module.ts`
  - `ide/package.json` only if local package wiring or scripted build orchestration must be documented or automated
  - `ide/theia-frontend/package.json`, `ide/theia-backend/package.json`, or `ide/theia-product/package.json` only if local package wiring truly requires it
- Possible focused add/update files if framework lifecycle bootstrap is required now:
  - one backend-side lifecycle bridge under `ide/theia-backend/src/node/`
  - one focused frontend-side adapter host service under `ide/theia-frontend/src/browser/`
- Files whose current behavior and ownership must be preserved:
  - [`ide/lsp/src/main/kotlin/com/engineeringood/athena/ide/lsp/AthenaLanguageServer.kt`](../../../ide/lsp/src/main/kotlin/com/engineeringood/athena/ide/lsp/AthenaLanguageServer.kt)
    - remains the Athena-owned IDE semantic/projection request surface
  - [`ide/theia-backend/src/node/athena-repository-session-manager.ts`](../../../ide/theia-backend/src/node/athena-repository-session-manager.ts)
    - remains the current JVM host lifecycle and generic LSP bridge owner
  - [`ide/theia-frontend/src/browser/athena-lsp-editor-bridge-service.ts`](../../../ide/theia-frontend/src/browser/athena-lsp-editor-bridge-service.ts)
    - remains the frontend-side typed bridge for LSP-backed Athena state and should be extended rather than bypassed
  - [`integrations/scm-git/README.md`](../../../integrations/scm-git/README.md)
    - provides the current precedent for a downstream vendor adapter that translates substrate state into Athena-owned contracts
  - [`settings.gradle.kts`](../../../settings.gradle.kts)
    - currently owns only Gradle/JVM module inclusion and does not host Node adapter topology
- Explicit non-goals:
  - no graphical widget or panel delivery
  - no graph selection synchronization
  - no renderer asset packs or notation mapping rollout
  - no broad interaction or editing behavior
  - no direct semantic authority inside graph-framework code

### Testing Requirements

- Minimum story verification:
  - `yarn --cwd integrations/graph-glsp build`
  - `yarn --cwd integrations/graph-glsp test`
  - `yarn --cwd ide build`
- Recommended focused regression:
  - `yarn --cwd ide/theia-backend test` if backend lifecycle wiring changes
  - `cmd /c "call java25 && .\gradlew.bat --no-daemon --console=plain :ide:lsp:test"` if any Kotlin/LSP contract changes are required
- Optional wider regression once focused checks are green:
  - `cmd /c "call java25 && .\gradlew.bat --no-daemon --console=plain test"`
  - `Set-Location ide; yarn start:smoke`
- Required proof checks:
  - graph-framework dependencies exist only under `integrations/graph-*`
  - the adapter consumes Athena-owned projection payloads instead of inventing a second semantic transport
  - `ide/theia-*` remains a bridge layer and does not absorb graph-framework semantic ownership
  - the adapter's translated graph state is disposable and rebuildable from upstream payloads
  - the repo documentation makes the heterogeneous adapter placement explicit
- Keep Gradle and Node verification sequential on Windows. Do not overlap build, test, or run commands in parallel shells.

### Current Code State To Preserve

- [`settings.gradle.kts`](../../../settings.gradle.kts) currently includes only JVM/Gradle modules and does not declare a physical `:kernel:projection-model` or any `integrations/graph-*` entry.
- [`integrations/README.md`](../../../integrations/README.md) currently documents only `:integrations:scm-git` and defines integrations as vendor-specific substrate adapters that must stay downstream of Athena semantic authority.
- [`ide/package.json`](../../../ide/package.json) currently defines a Yarn workspace rooted under `ide/` with only `theia-product`, `theia-frontend`, and `theia-backend` as workspace packages.
- [`ide/theia-frontend/package.json`](../../../ide/theia-frontend/package.json) and [`ide/theia-backend/package.json`](../../../ide/theia-backend/package.json) currently contain no graph-framework dependencies.
- [`ide/theia-frontend/src/browser/athena-lsp-editor-bridge-service.ts`](../../../ide/theia-frontend/src/browser/athena-lsp-editor-bridge-service.ts) currently exposes repository, semantic inspection, semantic SCM, and semantic history request helpers, but it does not yet expose projection-session request helpers for a graph adapter consumer.
- [`ide/theia-backend/src/node/athena-repository-session-manager.ts`](../../../ide/theia-backend/src/node/athena-repository-session-manager.ts) currently owns the stdio Athena LSP host lifecycle and the generic request/notification relay; Story `2.1` must preserve that role.
- [`ide/lsp/src/main/kotlin/com/engineeringood/athena/ide/lsp/AthenaProjectionProtocol.kt`](../../../ide/lsp/src/main/kotlin/com/engineeringood/athena/ide/lsp/AthenaProjectionProtocol.kt) and [`ide/lsp/src/main/kotlin/com/engineeringood/athena/ide/lsp/AthenaLanguageServer.kt`](../../../ide/lsp/src/main/kotlin/com/engineeringood/athena/ide/lsp/AthenaLanguageServer.kt) already publish typed projection-session queries and governed projection commands for the IDE path.
- Earlier M7 story files claim a dedicated `projection-model` boundary, but the physical module is not present in the current workspace. Story `2.1` must account for that real state instead of documenting a false dependency graph.

### Previous Milestone Intelligence

- M4 proved the Theia product shell, frontend/backend split, and additive desktop workbench path under `ide/`.
- M5 proved the governed repository/package graph and the runtime-owned `RepositoryGraphSession`.
- M6 Story `2.4` proved the correct product pattern for additive runtime/LSP/Theia projection of JVM-owned state without creating a frontend-owned semantic model.
- M7 Story `1.4` already proved typed projection-session and governed projection-command transport through `ide/lsp`, so Story `2.1` must consume that seam instead of recreating it.
- The user has repeatedly enforced these workspace rules that matter directly here:
  - physical structure must match intended architecture
  - names should stay simple and grouped by real responsibility
  - affected modules and groups keep English and Chinese README coverage
  - Java `25` and sequential Windows verification are non-negotiable

### Git Intelligence Summary

- Recent milestone baseline:
  - `adb0ae5 Complete M4-M6 IDE, repository, and semantic SCM milestones`
  - `a9b3a12 Complete M3 hosted extensibility proof`
  - `1339722 feat(m3): establish kernel plugin api boundary`
  - `dfc0234 add reference`
  - `87b1342 Complete M2 projection proof and normalize milestone artifacts`
- Practical implication:
  - follow the existing grouped-boundary discipline instead of scattering framework code across the repo
  - prefer additive bridges over second authority paths
  - treat integrations as downstream adapters, not semantic cores

### Latest Technical Information

- No extra web research is required for this story.
- The stack and version constraints that matter are already frozen locally:
  - Java `25`
  - Kotlin `2.4.0`
  - Gradle `9.6.1`
  - Node `22+`
  - Yarn `1.22.22`
  - Theia `1.73.1`
- `draft/m4/002-glsp.md` remains useful background for why GLSP is a strong candidate, but it is not itself a contract and does not override the current architecture spine.

### Project Structure Notes

- No `project-context.md` file exists in the repository.
- `.codegraph/` exists and should continue to be used first when locating or understanding code areas.
- This story sits at the seam between:
  - `integrations/graph-*`
  - `ide/theia-frontend`
  - `ide/theia-backend`
  - existing `ide/lsp` projection transport
- Naming should stay easy to understand and avoid leaking framework vocabulary into Athena-owned layers:
  - integration package: `graph-glsp`
  - package name: `@engineeringood/athena-graph-glsp`
  - Athena-owned bridge nouns remain `projection session`, `governed projection command`, and `runtime-owned projection state`

### References

- [Source: _bmad-output/planning-artifacts/epics-M7-2026-07-09.md]
- [Source: _bmad-output/planning-artifacts/prds/prd-Athena-2026-07-09-m7/prd.md]
- [Source: _bmad-output/planning-artifacts/prds/prd-Athena-2026-07-09-m7/addendum.md]
- [Source: _bmad-output/planning-artifacts/architecture/architecture-Athena-2026-07-09-m7/ARCHITECTURE-SPINE.md]
- [Source: _bmad-output/implementation-artifacts/m7/sprint-status.yaml]
- [Source: _bmad-output/implementation-artifacts/m7/1-4-expose-typed-projection-queries-and-governed-commands-through-ide-lsp.md]
- [Source: _bmad-output/implementation-artifacts/m6/2-4-expose-review-and-commit-semantics-through-runtime-lsp-and-existing-ide-seams.md]
- [Source: draft/m4/002-glsp.md]
- [Source: README.md]
- [Source: docs/usages/athena-workspace-summary.md]
- [Source: integrations/README.md]
- [Source: integrations/scm-git/README.md]
- [Source: ide/README.md]
- [Source: ide/package.json]
- [Source: ide/theia-frontend/README.md]
- [Source: ide/theia-backend/README.md]
- [Source: ide/theia-product/README.md]
- [Source: ide/theia-frontend/package.json]
- [Source: ide/theia-backend/package.json]
- [Source: ide/theia-product/package.json]
- [Source: ide/theia-frontend/src/browser/athena-lsp-editor-bridge-service.ts]
- [Source: ide/theia-frontend/src/browser/athena-frontend-module.ts]
- [Source: ide/theia-frontend/src/browser/athena-workbench-extensions.ts]
- [Source: ide/theia-backend/src/node/athena-repository-session-manager.ts]
- [Source: ide/lsp/src/main/kotlin/com/engineeringood/athena/ide/lsp/AthenaLanguageServer.kt]
- [Source: ide/lsp/src/main/kotlin/com/engineeringood/athena/ide/lsp/AthenaProjectionProtocol.kt]
- [Source: settings.gradle.kts]

## Story Completion Status

- Status: review
- Completion note: Added the first `integrations/graph-glsp` adapter package, routed Theia consumption through the existing Athena projection-session bridge, and verified the boundary with focused Node, smoke, and full JVM regression checks.

## Dev Agent Record

### Agent Model Used

Codex GPT-5

### Debug Log References

- M7 sprint status, epic breakdown, PRD, addendum, and architecture spine review
- CodeGraph exploration of the existing IDE/runtime projection and frontend bridge seams before edits
- red phase: `yarn --cwd integrations/graph-glsp test` failed because `translateProjectionSessionToGLSPDiagram` did not exist yet
- green phase: `yarn --cwd integrations/graph-glsp test`
- workspace wiring: `yarn --cwd ide install`
- cross-package verification: `yarn --cwd ide build`
- desktop smoke verification: `yarn --cwd ide start:smoke`
- repository hygiene verification: `powershell -ExecutionPolicy Bypass -File .\tools\encoding-audit.ps1`
- full JVM regression verification: `cmd /c "call java25 && .\gradlew.bat --no-daemon --console=plain test"`

### Completion Notes List

- Added the standalone `integrations/graph-glsp` package with a translation-only adapter API, focused tests, bilingual README coverage, and local package build metadata.
- Implemented deterministic translation from Athena-owned projection-session payloads into disposable GLSP-shaped graph data without introducing a second semantic or projection authority.
- Extended `ide/theia-frontend` with typed projection-session request helpers and a thin `AthenaGraphAdapterService` that consumes the adapter through the existing Athena LSP bridge.
- Wired the Theia workspace to consume the external adapter package while keeping the package physically outside `ide/` and graph-framework vocabulary outside Athena-owned layers.
- Hardened the adapter build path so `graph-glsp` self-installs its own toolchain before compiling, which keeps `yarn --cwd ide build` reproducible from the current heterogeneous workspace.
- Updated integration and workspace docs so the heterogeneous module layout now points to `integrations/graph-*` as the only home for graph-framework dependencies.
- Verified the story with focused adapter tests, Theia build, desktop smoke launch, encoding audit, and a full Java 25 Gradle regression run.

### File List

- _bmad-output/implementation-artifacts/m7/2-1-introduce-the-graph-adapter-boundary-under-integrations-graph.md
- _bmad-output/implementation-artifacts/m7/sprint-status.yaml
- README.md
- README.zh-CN.md
- docs/usages/athena-workspace-summary.md
- ide/package.json
- ide/yarn.lock
- ide/theia-frontend/package.json
- ide/theia-frontend/src/browser/athena-frontend-module.ts
- ide/theia-frontend/src/browser/athena-graph-adapter-service.ts
- ide/theia-frontend/src/browser/athena-lsp-editor-bridge-service.ts
- ide/theia-frontend/lib/browser/athena-frontend-module.d.ts.map
- ide/theia-frontend/lib/browser/athena-frontend-module.js
- ide/theia-frontend/lib/browser/athena-frontend-module.js.map
- ide/theia-frontend/lib/browser/athena-graph-adapter-service.d.ts
- ide/theia-frontend/lib/browser/athena-graph-adapter-service.d.ts.map
- ide/theia-frontend/lib/browser/athena-graph-adapter-service.js
- ide/theia-frontend/lib/browser/athena-graph-adapter-service.js.map
- ide/theia-frontend/lib/browser/athena-lsp-editor-bridge-service.d.ts
- ide/theia-frontend/lib/browser/athena-lsp-editor-bridge-service.d.ts.map
- ide/theia-frontend/lib/browser/athena-lsp-editor-bridge-service.js
- ide/theia-frontend/lib/browser/athena-lsp-editor-bridge-service.js.map
- ide/theia-frontend/tsconfig.tsbuildinfo
- ide/theia-product/lib/frontend/bundle.js
- ide/theia-product/lib/frontend/bundle.js.map
- integrations/README.md
- integrations/README.zh-CN.md
- integrations/graph-glsp/.gitignore
- integrations/graph-glsp/package.json
- integrations/graph-glsp/README.md
- integrations/graph-glsp/README.zh-CN.md
- integrations/graph-glsp/tsconfig.json
- integrations/graph-glsp/yarn.lock
- integrations/graph-glsp/src/athena-glsp-diagram-model.ts
- integrations/graph-glsp/src/athena-glsp-projection-adapter.ts
- integrations/graph-glsp/src/index.ts
- integrations/graph-glsp/test/athena-graph-glsp-adapter.test.mjs
- integrations/graph-glsp/lib/athena-glsp-diagram-model.d.ts
- integrations/graph-glsp/lib/athena-glsp-diagram-model.d.ts.map
- integrations/graph-glsp/lib/athena-glsp-diagram-model.js
- integrations/graph-glsp/lib/athena-glsp-diagram-model.js.map
- integrations/graph-glsp/lib/athena-glsp-projection-adapter.d.ts
- integrations/graph-glsp/lib/athena-glsp-projection-adapter.d.ts.map
- integrations/graph-glsp/lib/athena-glsp-projection-adapter.js
- integrations/graph-glsp/lib/athena-glsp-projection-adapter.js.map
- integrations/graph-glsp/lib/index.d.ts
- integrations/graph-glsp/lib/index.d.ts.map
- integrations/graph-glsp/lib/index.js
- integrations/graph-glsp/lib/index.js.map

### Change Log

- 2026-07-10: Implemented the first `integrations/graph-glsp` adapter boundary, added Theia projection-session consumption through the existing Athena LSP bridge, hardened the adapter build path for the heterogeneous workspace, updated workspace documentation, and verified the result with focused Node, smoke, encoding, and full JVM regression checks.
