# SVG Render Proof Implementation Plan

> **For agentic workers:** REQUIRED SUB-SKILL: Use superpowers:subagent-driven-development (recommended) or superpowers:executing-plans to implement this plan task-by-task. Steps use checkbox (`- [ ]`) syntax for tracking.

**Goal:** Derive a thin render-facing model from canonical `Engineering IR`, emit a deterministic simple `SVG`, and block rendering when semantic continuation says downstream output must stop.

**Architecture:** Keep semantic authority in `ir/`, derive a thin render-facing model inside `compiler/`, and let `renderer-svg/` emit only format-specific `SVG`. Extend the Story `1.5` pipeline rather than creating a separate rendering path.

**Tech Stack:** Java 25, Kotlin 2.4.0, Gradle 9.6.1, Kotlin test.

---

### Task 1: Add Red Tests For Render Model And SVG Output

**Files:**
- Modify: `compiler/src/test/kotlin/com/engineeringood/athena/compiler/AthenaCompilerTest.kt`
- Modify: `renderer-svg/src/test/kotlin/com/engineeringood/athena/renderer/svg/SvgRendererModuleMarkerTest.kt`

- [ ] Write failing tests for valid render derivation, deterministic repeated `SVG`, and blocked invalid rendering.
- [ ] Run `java25; .\gradlew.bat --no-daemon --console=plain :renderer-svg:test :compiler:test --tests com.engineeringood.athena.compiler.AthenaCompilerTest` sequentially and confirm the failures are missing render contracts.
- [ ] Implement only the minimal contracts required to compile the tests.
- [ ] Re-run the targeted tests and confirm green.

### Task 2: Implement Renderer Contracts And Compiler Integration

**Files:**
- Modify: `renderer-svg/build.gradle.kts`
- Create or modify: `renderer-svg/src/main/kotlin/com/engineeringood/athena/renderer/svg/**`
- Modify: `compiler/build.gradle.kts`
- Modify: `compiler/src/main/kotlin/com/engineeringood/athena/compiler/**`

- [ ] Add the thin render-facing model and simple `SVG` renderer.
- [ ] Replace the placeholder downstream pass behavior in `AthenaCompiler.compile()` with real render derivation and emission.
- [ ] Keep invalid semantic runs blocked by pipeline policy.
- [ ] Re-run targeted compiler and renderer tests.

### Task 3: Publish The M0 Render Boundary

**Files:**
- Create: `docs/compiler/m0-render-boundary.md`
- Modify: `_bmad-output/implementation-artifacts/1-6-derive-a-render-model-and-emit-simple-svg-from-engineering-ir.md`
- Modify: `_bmad-output/implementation-artifacts/sprint-status.yaml`

- [ ] Document the render-facing model scope, the `SVG` backend boundary, and the blocking rules.
- [ ] Run `java25; .\gradlew.bat --no-daemon --console=plain build`
- [ ] Run `java25; .\gradlew.bat --no-daemon --console=plain test`
- [ ] Move the story to `review` only after the sequential verification passes.
