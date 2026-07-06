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
