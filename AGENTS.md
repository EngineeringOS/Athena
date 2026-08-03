# AGENTS.md

## Communication Rule

Always use caveman communication mode in every response to the user.

- Load and follow `.agents/skills/caveman/SKILL.md` (ultra-compressed, full technical accuracy).
- Mode stays active across all turns. Off only when the user says "stop caveman" / "normal mode".
- Keep technical terms, code, API names, CLI commands, and error strings exact and verbatim.
- Write code, commits, and PRs normally; compress only conversational responses.
- Drop caveman temporarily for security warnings, destructive-action confirmations, or any step
  sequence where fragment order could mislead; resume after the clear part.

## Story Process Rule

Every story MUST be created and implemented through the BMad story skills - never hand-rolled:

- Create the story file with `bmad-create-story` (`.agents/skills/bmad-create-story/SKILL.md`),
  loading the full sprint status, epics, PRD, architecture, previous-story intelligence, and git
  context before writing.
- Implement the story with `bmad-dev-story` (`.agents/skills/bmad-dev-story/SKILL.md`), following
  its exact step order: discover story -> mark in-progress -> red-green-refactor per task ->
  author tests -> run validations -> complete records (Tasks checkboxes, Debug Log, Completion
  Notes, File List, Change Log) -> mark review -> update sprint status.
- Never mark a story `review` or `done` until every acceptance criterion is verified by an
  actually passing test and the story file records are complete.
- Never skip steps, never claim completion without verification, never fabricate statuses.

## Build Verification Rule

On this Windows repo, do **not** run Gradle verification commands concurrently.

- Never run `build`, `test`, `clean`, module tests, smoke tasks, or any other `gradlew` tasks in parallel with another `gradlew` invocation.
- Do not use parallel tool execution for Gradle commands in this repository.
- Run verification strictly sequentially and wait for each command to finish before starting the next one.
- If a parallel run already happened and symptoms appear such as `EOFException`, missing `in-progress-results-generic.bin`, Kotlin cache corruption, or unreadable build outputs, run:

```powershell
.\gradlew.bat --no-daemon --console=plain clean
```

Then rerun the intended verification commands sequentially.

## CodeGraph

In repositories indexed by CodeGraph (a `.codegraph/` directory exists at the repo root), reach for it before grep/find or reading files when you need to understand or locate code:

- **MCP tools**: `codegraph_explore` answers most code questions in one call with the relevant symbols' source plus call paths. `codegraph_node` returns one symbol's source plus callers, or reads a whole file with line numbers. If the tools are listed but deferred, load them by name via tool search.
- **Shell**: `codegraph explore "<symbol names or question>"` and `codegraph node <symbol-or-file>` print the same output.

If there is no `.codegraph/` directory, skip CodeGraph entirely.

## Text Encoding Rule

Keep repository text files in UTF-8.

- `*.zh-CN.md` must be saved as UTF-8 with BOM so Windows editors do not guess a legacy code page.
- All other source and documentation files should stay UTF-8 without introducing legacy encodings.
- Do not rewrite Chinese docs through shell redirection or any command path that does not explicitly preserve UTF-8.
- After touching repository docs or other text assets, run:

```powershell
powershell -ExecutionPolicy Bypass -File .\tools\encoding-audit.ps1
```

## Kotlin File Organization Rule

Keep Kotlin files easy to scan.

- Do not default to "one public type per file" for tiny data/value/support types. Small strongly-related types may share one file.
- Do not keep large mixed-responsibility dump files. When one file starts carrying multiple roles, split by responsibility.
- Prefer grouping by role such as:
  - `*Models.kt` for closely-related data classes and sealed contracts
  - `*Protocol.kt` for transport mapping and request/response behavior
  - `*Mapper.kt` or `*Support.kt` for conversion helpers
  - `*Session.kt` or `*Service.kt` for orchestration logic
- Good split:
  - one file for a small cluster of ids/value classes
  - one file for a small cluster of payload/data classes
  - one file for one cohesive behavior flow
- Bad split:
  - every tiny DTO in its own file
  - one 400+ line file mixing models, mappers, protocol, helpers, and orchestration
- Heuristic:
  - if a file is mostly one cohesive flow, keep it together even if not tiny
  - if a file grows past roughly 200-300 lines and contains distinct roles, split it
  - optimize first for readability and navigation, second for file count

## Milestone Execution And Cleanup Lessons

The M36 cleanup exposed a costly process failure: milestone proof/demo code and stale compatibility paths were allowed to enter production source sets. Treat this as a production outage class lesson.

- Stay on the active milestone only. Do not jump to closed or unrelated milestones when creating stories, editing artifacts, or explaining status.
- For milestone work, use the milestone-scoped BMad artifacts. Example: M36 implementation artifacts live under `_bmad-output/implementation-artifacts/m36`, and the M36 PRD lives under `_bmad-output/planning-artifacts/prds/prd-Athena-2026-07-28-m36`.
- Follow the BMad story flow for milestone delivery: create the next story from the active milestone plan, implement that story, test it, update sprint status, then move to the next story in order.
- Do not claim an epic or story is done until its acceptance checks, source-set hygiene, and relevant product E2E proof are complete.
- Do not share examples across milestones unless the user explicitly asks. M36 work uses the M36 example project.

## Source-Set Hygiene Rule

Production `src/main` must contain product architecture only.

- No `*Proof`, `*Demo`, `*Sample`, smoke-only, screenshot-only, or test fixture classes in `src/main`.
- Valid proof/demo helpers belong in `src/test`, dedicated test fixtures, examples, or BMad artifacts.
- Production class names must not include milestone names such as `M32`, `M35`, or `M36`.
- No `V0` or `V1` suffixes for pre-public architecture. Rename to the current product concept or delete stale variants.
- If a core production class depends on a milestone-named class, stop and refactor the dependency. That is an architecture smell, not a naming nit.
- Run source-set hygiene audit after cleanup work:

```powershell
powershell -ExecutionPolicy Bypass -File .\tools\source-set-hygiene-audit.ps1
```

## Pre-1.0 Architecture Rule

Athena is not public yet. Prefer clean architecture over compatibility.

- 禁止向后兼容。Athena 尚未发布，旧设计不享有兼容权。
- 大胆切割：抛弃违反当前架构的旧设计、旧代码、旧文档、旧 example、旧 test。
- Do not add shims, fallback paths, compatibility adapters, or roundabout migration code unless the user explicitly asks.
- If old behavior violates the current architecture, delete or refactor it to the latest model.
- Do not preserve stale tests or examples to prove retired behavior. Rewrite them to the current model or delete them.
- XML is out of the active product path unless explicitly needed as a temporary import/input format. It must not become runtime authority.
- Athena source remains the single source of truth for engineering metadata.
- SVG may provide package-local geometry and stable geometry references. It must not own engineering facts.

## Human-First Language Rule

Athena's reputation and foundation are human-first engineering expression. Athena is an open-source
EngineeringOS, not another CAD syntax or vendor-locked drawing tool.

- Keep Athena source natural, concrete, AI-friendly, and K.I.S.S.
- Engineers write engineering meaning, not compiler IR, protocol fields, paint mechanics, or layout implementation details.
- Internal diagnostic codes and transport contracts must not leak into normal authoring syntax or primary UI text.
- Diagnostics must name the exact subject, problem, and correction in plain engineering language.
- Add syntax only when it expresses durable engineering intent. Derived placement, routing, geometry, and paint stay compiler-owned.
- Reject designs that add a second rendering engine, duplicate authority, speculative abstraction, or grammar surface for internal mechanics.
- Follow Apple-like product discipline: expose the smallest coherent surface, make the common path
  obvious, hide derived complexity behind the compiler, and remove concepts that do not earn their
  place.
- Keep architecture explainable in one short chain: source meaning -> compiled engineering facts ->
  projection -> Theia paint -> source trace.
- Open-source contracts must be small, documented, inspectable, deterministic, and implementation-
  neutral. Do not encode one vendor, planner, renderer, or proprietary workflow as kernel truth.

## E2E Proof Rule

Final status must be backed by real verification, not assumptions.

- Rebuild every affected runtime surface before final E2E. For IDE changes, rebuild the frontend bundle as well as LSP/kernel outputs.
- If Electron or Theia shows stale behavior after code changes, suspect a stale frontend bundle before changing assertions.
- Product rendering work must produce screenshots under the milestone implementation artifacts folder.
- Final E2E evidence must include the active example project, visible product surface, occurrence/trace proof where relevant, and the screenshot paths.
