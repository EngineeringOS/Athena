# `:kernel:repository-model`

English | [Chinese (Simplified)](README.zh-CN.md)

The `:kernel:repository-model` module defines Athena's canonical M5 repository and package contract surface. It publishes the typed repository-root manifest, derived lock, package identity, dependency declaration, resolved package-graph, and inspectable report shapes that compiler, runtime, and `ide/lsp` can share without inventing parallel models.

## Responsibilities

- Publish the typed repository aggregate in `EngineeringRepository`.
- Keep `RepositoryManifest` explicit as authored intent for `athena.yaml`.
- Keep `RepositoryLock` explicit as derived state for `athena.lock`.
- Define stable package identity and primary package ownership through `PackageIdentifier` and `PrimaryPackage`.
- Define local-first dependency declaration shapes through `PackageDependency`.
- Define inspectable resolved package-graph and report types for later M5 stories.

## Main Types

- `EngineeringRepository`
- `RepositoryManifest`
- `RepositoryLock`
- `RepositoryArtifactRole`
- `PackageIdentifier`
- `PrimaryPackage`
- `PackageDependency`
- `ResolvedPackage`
- `ResolvedPackageGraph`
- `RepositoryGraphReport`
- `RepositoryDiagnostic`

## Dependencies

This module has no project-module dependencies.

## Boundaries

This module does not parse YAML, walk the filesystem, validate repository layout, resolve dependencies, materialize lockfiles, or implement SCM behavior. It stays VCS-neutral and publishes only the typed repository/package contracts that later compiler, runtime, IDE, and SCM layers can consume.

## Verification

```bash
./gradlew :kernel:repository-model:test
```

Windows PowerShell:

```powershell
java25; .\gradlew.bat :kernel:repository-model:test
```
