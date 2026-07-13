# `:kernel:physical-model`

[English](README.md) | 简体中文

`:kernel:physical-model` 模块定义 Athena 在 M14 阶段的最小 physical-trait 契约。

该模块明确保持如下所有权链路：

`Engineering IR -> physical trait knowledge -> downstream layout / projection / presentation consumers`

## 职责

- 通过 `PhysicalSize` 发布可复用的物理尺寸。
- 通过 `PhysicalMountingTypeId` 发布安装类型标识。
- 通过 `PhysicalInstallationMarkerId` 发布安装标记标识。
- 通过 `PhysicalTraitDefinition` 发布最小 physical-trait 定义。
- 通过 `ResolvedPhysicalTraitDefinition` 发布只读的 resolved physical-trait knowledge。
- 保持 canonical authored truth 仍然留在 `Engineering IR`。

## 主要类型

- `PhysicalSize`
- `PhysicalMountingTypeId`
- `PhysicalInstallationMarkerId`
- `PhysicalTraitDefinition`
- `ResolvedPhysicalTraitDefinition`

## 依赖

该模块依赖 `:kernel:engineering-model`，通过 `StableSemanticIdentity` 复用规范语义标识。

## 边界

该模块不定义 layout placement、geometry bounds、canvas coordinates、scene calculation、routing、presentation logic 或 renderer behavior。它只定义最小且可复用的 physical knowledge：width、height、depth、mounting type 和 installation markers。

## 验证

```bash
./gradlew :kernel:physical-model:test
```

Windows PowerShell:

```powershell
java25; .\gradlew.bat :kernel:physical-model:test
```

