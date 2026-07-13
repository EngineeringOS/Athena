# `:kernel:part-model`

[English](README.md) | 简体中文

`:kernel:part-model` 模块定义 Athena 在 M14 阶段的 vendor implementation mapping 契约。

该模块明确保持如下所有权链路：

`Engineering IR -> component knowledge -> vendor part implementations -> downstream M9 and M13 consumers`

## 职责

- 通过 `VendorId` 发布稳定的 vendor identity。
- 通过 `VendorPartNumber` 发布面向 vendor catalog 的 part identity。
- 通过 `PartImplementationId` 发布 Athena 自有的 implementation identity。
- 通过 `PartImplementationDefinition` 发布 vendor implementation mapping。
- 通过 `ResolvedPartImplementation` 发布只读的 resolved implementation selection。
- 保持 `EngineeringConceptId` 仍然是 vendor-neutral semantic target。

## 主要类型

- `VendorId`
- `VendorPartNumber`
- `PartImplementationId`
- `PartImplementationDefinition`
- `ResolvedPartImplementation`

## 依赖

该模块依赖：

- `:kernel:component-model`，用于 `EngineeringConceptId`
- `:kernel:engineering-model`，用于通过 `StableSemanticIdentity` 复用规范语义主体标识

## 边界

该模块不定义 engineering concept model 本身，不定义 semantic port contract，不定义 physical-trait contract，不负责 knowledge-pack loading、compiler orchestration、runtime transport、projection logic、presentation logic 或 renderer behavior。它只负责把 vendor implementation 建模为 vendor-neutral concept 的下游实现。

## 验证

```bash
./gradlew :kernel:part-model:test
```

Windows PowerShell:

```powershell
java25; .\gradlew.bat :kernel:part-model:test
```

