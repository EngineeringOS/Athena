# `:kernel:plugins:plugin-api`

[English](README.md) | 简体中文

`:kernel:plugins:plugin-api` 模块是 Athena 面向扩展的稳定契约边界。它承载扩展模块直接编译依赖的公共插件 SPI，而不是让扩展依赖编译器内部实现包。

## 职责

- 发布稳定的 `AthenaPlugin` 契约与类型化插件接口。
- 发布插件清单元数据、扩展点词汇表与兼容性模型。
- 发布面向实体、属性、端口与连接的通用领域 schema 契约。
- 发布可检查的 validation、compiler-stage 与 renderer-facing contribution 描述符。
- 发布 additive semantic review-enrichment 契约，让 hosted plugin 只能追加 label、hint 与 summary，而不能改写 core semantic SCM facts。
- 发布与编译器私有实现解耦的 lowering、semantic-enrichment 与 validation 上下文模型。
- 发布稳定的 stage 词汇表，供编译器宿主治理声明式领域参与。
- 保持公共 SPI 精简、文档清晰，并由 kernel 拥有。

## 边界

该模块不拥有插件发现、插件审批、编译器编排、runtime 生命周期或 renderer 编排。这些职责仍然属于宿主与编排模块。SPI 在这里声明贡献意图与稳定的 stage 词汇；宿主与编译器模块决定这些意图如何被批准、执行与检查。

## 验证

```bash
./gradlew :kernel:plugins:plugin-api:test
```

Windows PowerShell:

```powershell
java25; .\gradlew.bat :kernel:plugins:plugin-api:test
```
