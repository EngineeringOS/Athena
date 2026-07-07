# `:kernel:plugin-api`

[English](README.md) | 简体中文

`:kernel:plugin-api` 模块是 Athena 面向扩展的稳定插件契约边界。它承载扩展模块直接编译依赖的公共 SPI，而不是让扩展依赖 `compiler` 内部实现包。

## 职责

- 发布稳定的 `AthenaPlugin` 公共契约与类型化插件接口。
- 发布插件清单元数据、扩展点词汇表与兼容性模型。
- 发布与编译器私有实现解耦的插件侧 lowering 与 validation 上下文模型。
- 保持公共 SPI 精简、清晰、由 kernel 所有。

## 边界

该模块不负责插件发现、插件审批、编译器编排、runtime 生命周期或渲染器编排。这些仍然属于宿主与编排模块。

## 验证

```bash
./gradlew :kernel:plugin-api:test
```

Windows PowerShell:

```powershell
java25; .\gradlew.bat :kernel:plugin-api:test
```
