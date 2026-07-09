# DEV

## Workspace Notes

- `manifesto/` remains a git submodule and reference input.
- `manifesto/` is not part of the Gradle module graph.
- The JVM/Gradle modules are grouped logically as `kernel`, `extensions`, `ui`, and `apps`.
- The primary M4 IDE product path is seeded physically under `ide/` and is intentionally separate from the current JVM/Gradle module graph.

## Submodule

Initial setup:

```bash
git submodule add https://github.com/EngineeringOS/manifesto.git manifesto
git add .gitmodules manifesto
git commit -m "Add manifesto submodule"
```

Update the repo and the submodule together:

```bash
git pull && git submodule update --remote manifesto
```

## Build Bootstrap

Windows PowerShell in this repo:

```powershell
java25
.\gradlew.bat build
.\gradlew.bat test
.\gradlew.bat :apps:cli:run --args="--help"
Set-Location ide
yarn build
yarn start:smoke
```

## Windows Rule

- Use Java 25 for all Gradle build, test, and run tasks.
- Run Gradle tasks sequentially on Windows. Do not overlap build/test/run commands in parallel shells.
- `java25.bat` does not mutate the parent PowerShell environment. Use `cmd /c "call java25 && ..."` when a child Windows launcher must inherit Java 25 in the same process tree.

## Reference

- KMP starter: https://kmp.jetbrains.com/
