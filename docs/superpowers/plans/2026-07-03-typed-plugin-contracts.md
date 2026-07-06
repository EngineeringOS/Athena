# Typed Plugin Contracts Implementation Plan

> **For agentic workers:** REQUIRED SUB-SKILL: Use superpowers:subagent-driven-development (recommended) or superpowers:executing-plans to implement this plan task-by-task. Steps use checkbox (`- [ ]`) syntax for tracking.

**Goal:** Publish the first core-owned typed plugin contracts, manifest models, validation, and one minimal sample domain plugin for Story `2.1`.

**Architecture:** Keep plugin governance in `compiler` under a dedicated `plugin` package, keep the current module graph stable, and use `domain-electrical-runtime` as the directly-instantiated proof plugin. Discovery, classpath loading, and activation ordering remain deferred to Story `2.2`.

**Tech Stack:** Kotlin/JVM, Java 25, Gradle 9.6.1, Kotlin test/JUnit 5

---

### Task 1: Add Failing Contract Tests

**Files:**
- Create: `compiler/src/test/kotlin/com/engineeringood/athena/compiler/AthenaPluginContractTest.kt`

- [ ] Step 1: Write failing tests for valid and invalid plugin manifests, typed plugin mismatch rejection, and direct sample-plugin instantiation.
- [ ] Step 2: Run `java25; .\gradlew.bat --no-daemon --console=plain :kernel:compiler:test --tests com.engineeringood.athena.compiler.AthenaPluginContractTest` and verify failure.

### Task 2: Add Core Plugin Contract Types

**Files:**
- Create: `compiler/src/main/kotlin/com/engineeringood/athena/compiler/plugin/AthenaPluginContracts.kt`
- Create: `compiler/src/main/kotlin/com/engineeringood/athena/compiler/plugin/AthenaPluginManifestModel.kt`
- Create: `compiler/src/main/kotlin/com/engineeringood/athena/compiler/plugin/AthenaPluginValidationModel.kt`
- Create: `compiler/src/main/kotlin/com/engineeringood/athena/compiler/plugin/AthenaPluginValidator.kt`

- [ ] Step 1: Add the base plugin interface plus typed `domain`, `rule`, and `renderer` contracts.
- [ ] Step 2: Add plugin type, extension-point, manifest, and compatibility-range models.
- [ ] Step 3: Add inspectable validation diagnostics and validator logic.
- [ ] Step 4: Run the targeted compiler test and verify it passes.

### Task 3: Add the Sample Domain Plugin

**Files:**
- Modify: `domain-electrical-runtime/build.gradle.kts`
- Create: `domain-electrical-runtime/src/main/kotlin/com/engineeringood/athena/domain/electricalruntime/ElectricalRuntimeDomainPlugin.kt`
- Modify: `domain-electrical-runtime/src/test/kotlin/com/engineeringood/athena/domain/electricalruntime/ElectricalRuntimeDomainMarkerTest.kt`

- [ ] Step 1: Add the minimal `:kernel:compiler` dependency needed for the sample plugin to use core-owned contracts.
- [ ] Step 2: Implement the sample `ElectricalRuntimeDomainPlugin` with a real manifest.
- [ ] Step 3: Add or update domain-module tests that prove the sample plugin compiles and is directly instantiable.
- [ ] Step 4: Run `java25; .\gradlew.bat --no-daemon --console=plain :extensions:domain-electrical:test` and verify it passes.

### Task 4: Document the Boundary

**Files:**
- Create: `docs/compiler/m0-plugin-contract-boundary.md`

- [ ] Step 1: Document the core-owned plugin contracts, manifest model, validator scope, and Story `2.1` non-goals.

### Task 5: Full Verification And Story Close-Out

**Files:**
- Modify: `_bmad-output/implementation-artifacts/archive-m0/2-1-define-core-owned-typed-plugin-contracts.md`
- Modify: `_bmad-output/implementation-artifacts/sprint-status.yaml`

- [ ] Step 1: Run `java25; .\gradlew.bat --no-daemon --console=plain :kernel:compiler:test`.
- [ ] Step 2: Run `java25; .\gradlew.bat --no-daemon --console=plain :extensions:domain-electrical:test`.
- [ ] Step 3: Run `java25; .\gradlew.bat --no-daemon --console=plain build`.
- [ ] Step 4: Run `java25; .\gradlew.bat --no-daemon --console=plain test`.
- [ ] Step 5: Check off all Story `2.1` tasks, update Dev Agent Record, File List, Change Log, and move story status to `review`.
