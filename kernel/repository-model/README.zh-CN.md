# `:kernel:repository-model`

[English](README.md) | 简体中文

`:kernel:repository-model` 模块定义 Athena 在 M5 的规范仓库与包契约边界。它发布仓库根清单、派生锁文件、包标识、依赖声明、已解析包图以及可检查报告类型，使 compiler、runtime 与 `ide/lsp` 可以共享同一套模型，而不是各自发明平行表示。

## 职责

- 通过 `EngineeringRepository` 发布统一的仓库聚合模型。
- 用 `RepositoryManifest` 明确表示 `athena.yaml` 的作者意图。
- 用 `RepositoryLock` 明确表示 `athena.lock` 的派生状态。
- 通过 `PackageIdentifier` 与 `PrimaryPackage` 定义稳定的包标识与主包所有权。
- 通过 `PackageDependency` 定义本地优先的依赖声明形状。
- 为后续 M5 故事发布可检查的已解析包图与报告类型。

## 主要类型

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

## 依赖

该模块没有项目内模块依赖。

## 边界

该模块不负责 YAML 解析、不负责文件系统遍历、不负责仓库布局校验、不负责依赖解析、不负责锁文件物化，也不负责 SCM 行为。它保持 VCS 中立，只发布后续 compiler、runtime、IDE 与 SCM 层可以消费的类型化仓库/包契约。

## 验证

```bash
./gradlew :kernel:repository-model:test
```

Windows PowerShell：

```powershell
java25; .\gradlew.bat :kernel:repository-model:test
```
