# AGENTS.md

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
