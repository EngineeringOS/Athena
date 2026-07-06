# DEV

## Workspace Notes

- `manifesto/` remains a git submodule and reference input.
- `manifesto/` is not part of the Gradle module graph.
- Gradle modules are grouped logically as `kernel`, `extensions`, `ui`, and `apps`, while the current physical directories remain stable.

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
```

## Windows Rule

- Use Java 25 for all Gradle build, test, and run tasks.
- Run Gradle tasks sequentially on Windows. Do not overlap build/test/run commands in parallel shells.

## Reference

- KMP starter: https://kmp.jetbrains.com/
