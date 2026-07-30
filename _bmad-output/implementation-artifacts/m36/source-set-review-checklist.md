# Production Source-Set Review Checklist

- [x] Every `src/main` type serves a current production runtime path.
- [x] Demo, sample, smoke, fixture, test, and milestone proof helpers exist only in `src/test` or examples.
- [x] Production type and file names describe responsibility, never milestone or temporary version.
- [x] No compatibility shim preserves a pre-public legacy class or path.
- [x] Example paths, package ids, and source files are not hardcoded in production branch selection.
- [x] Mock providers are injected by tests and are never production defaults.
- [x] Canonical source authority remains Athena source and typed kernel contracts.
- [x] `powershell -ExecutionPolicy Bypass -File .\tools\source-set-hygiene-audit.ps1` passes.
- [x] Root Gradle `check` or `test` passes sequentially on Windows.
- [x] E2E evidence uses a dedicated example and includes a real rendered screenshot when UI is affected.
