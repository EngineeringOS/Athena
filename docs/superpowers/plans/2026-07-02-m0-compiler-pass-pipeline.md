# M0 Compiler Pass Pipeline Implementation Plan

> **For agentic workers:** REQUIRED SUB-SKILL: Use superpowers:subagent-driven-development (recommended) or superpowers:executing-plans to implement this plan task-by-task. Steps use checkbox (`- [ ]`) syntax for tracking.

**Goal:** Make `AthenaCompiler.compile()` execute an explicit deterministic parse -> lower -> validate -> downstream-derivation pass pipeline and expose an inspectable pass report without pulling Story `1.6` rendering work forward.

**Architecture:** Keep all new pipeline contracts in `compiler/`, reuse the existing parser/lowerer/validator as concrete pass bodies, and treat downstream derivation as a compiler-owned inspectable stage that only reports readiness or blocking for the next story. Preserve the current `parse()`, `lower()`, and semantic validation contracts while extending the `compile()` result with pass execution data.

**Tech Stack:** Java 25, Kotlin 2.4.0, Gradle 9.6.1, Kotlin test.

---

### Task 1: Add Red Tests For Pipeline Reporting

**Files:**
- Modify: `compiler/src/test/kotlin/com/engineeringood/athena/compiler/AthenaCompilerTest.kt`
- Test: `compiler/src/test/kotlin/com/engineeringood/athena/compiler/AthenaCompilerTest.kt`

- [ ] **Step 1: Write the failing test**

```kotlin
@Test
fun `compile reports declared pass execution for valid input`() {
    val examplePath = resolveRepoRoot().resolve("examples/m0/demo-cabinet.athena")

    val result = AthenaCompiler().compile(examplePath)

    val success = assertIs<CompilerCompilationSuccess>(result)
    assertEquals(
        listOf(
            CompilerPassId.PARSE,
            CompilerPassId.LOWER,
            CompilerPassId.VALIDATE,
            CompilerPassId.DOWNSTREAM_DERIVATION,
        ),
        success.pipeline.passes.map { it.pass.id },
    )
}
```

- [ ] **Step 2: Run test to verify it fails**

Run: `java25; .\gradlew.bat --no-daemon --console=plain :compiler:test --tests com.engineeringood.athena.compiler.AthenaCompilerTest`
Expected: FAIL because `pipeline` / compiler pass contracts do not exist yet.

- [ ] **Step 3: Write minimal implementation**

```kotlin
data class CompilerPipelineReport(
    val passes: List<CompilerPassRecord>,
)
```

- [ ] **Step 4: Run test to verify it passes**

Run: `java25; .\gradlew.bat --no-daemon --console=plain :compiler:test --tests com.engineeringood.athena.compiler.AthenaCompilerTest`
Expected: PASS for the new pipeline test and existing compiler tests.

- [ ] **Step 5: Commit**

```bash
git add compiler/src/test/kotlin/com/engineeringood/athena/compiler/AthenaCompilerTest.kt compiler/src/main/kotlin/com/engineeringood/athena/compiler/*
git commit -m "feat: declare compiler pass pipeline"
```

### Task 2: Implement Deterministic Gate Behavior

**Files:**
- Modify: `compiler/src/main/kotlin/com/engineeringood/athena/compiler/AthenaCompiler.kt`
- Modify: `compiler/src/main/kotlin/com/engineeringood/athena/compiler/CompilerModels.kt`
- Test: `compiler/src/test/kotlin/com/engineeringood/athena/compiler/AthenaCompilerTest.kt`

- [ ] **Step 1: Write the failing test**

```kotlin
@Test
fun `compile skips downstream derivation when semantic continuation stops`() {
    val examplePath = resolveRepoRoot().resolve("examples/m0/invalid-semantic-cabinet.athena")

    val result = AthenaCompiler().compile(examplePath)

    val success = assertIs<CompilerCompilationSuccess>(result)
    assertEquals(CompilerPassExecutionStatus.SKIPPED, success.pipeline.passes.last().status)
}
```

- [ ] **Step 2: Run test to verify it fails**

Run: `java25; .\gradlew.bat --no-daemon --console=plain :compiler:test --tests com.engineeringood.athena.compiler.AthenaCompilerTest`
Expected: FAIL because downstream derivation is not yet modeled as a declared pass.

- [ ] **Step 3: Write minimal implementation**

```kotlin
if (semanticResult.continuationDecision == SemanticContinuationDecision.STOP_DOWNSTREAM) {
    downstreamPass = skippedPass(...)
}
```

- [ ] **Step 4: Run test to verify it passes**

Run: `java25; .\gradlew.bat --no-daemon --console=plain :compiler:test --tests com.engineeringood.athena.compiler.AthenaCompilerTest`
Expected: PASS with deterministic pass statuses for semantic gating.

- [ ] **Step 5: Commit**

```bash
git add compiler/src/main/kotlin/com/engineeringood/athena/compiler/* compiler/src/test/kotlin/com/engineeringood/athena/compiler/AthenaCompilerTest.kt
git commit -m "feat: apply deterministic compiler pass gates"
```

### Task 3: Document And Verify The Pipeline

**Files:**
- Create: `docs/compiler/m0-pass-pipeline.md`
- Modify: `_bmad-output/implementation-artifacts/1-5-execute-the-m0-compiler-as-declared-deterministic-passes.md`
- Modify: `_bmad-output/implementation-artifacts/sprint-status.yaml`

- [ ] **Step 1: Write the failing test**

```kotlin
@Test
fun `compile produces identical pass reports for identical input`() {
    val examplePath = resolveRepoRoot().resolve("examples/m0/demo-cabinet.athena")
    val compiler = AthenaCompiler()

    val first = assertIs<CompilerCompilationSuccess>(compiler.compile(examplePath))
    val second = assertIs<CompilerCompilationSuccess>(compiler.compile(examplePath))

    assertEquals(first.pipeline, second.pipeline)
}
```

- [ ] **Step 2: Run test to verify it fails**

Run: `java25; .\gradlew.bat --no-daemon --console=plain :compiler:test --tests com.engineeringood.athena.compiler.AthenaCompilerTest`
Expected: FAIL until the pass report is stable and comparable.

- [ ] **Step 3: Write minimal implementation**

```kotlin
data class CompilerPassRecord(
    val pass: CompilerPassDescriptor,
    val status: CompilerPassExecutionStatus,
    val detail: String,
)
```

- [ ] **Step 4: Run test to verify it passes**

Run: `java25; .\gradlew.bat --no-daemon --console=plain :compiler:test --tests com.engineeringood.athena.compiler.AthenaCompilerTest`
Expected: PASS with deterministic pipeline equality and updated docs.

- [ ] **Step 5: Commit**

```bash
git add docs/compiler/m0-pass-pipeline.md _bmad-output/implementation-artifacts/1-5-execute-the-m0-compiler-as-declared-deterministic-passes.md _bmad-output/implementation-artifacts/sprint-status.yaml compiler/src/main/kotlin/com/engineeringood/athena/compiler/* compiler/src/test/kotlin/com/engineeringood/athena/compiler/AthenaCompilerTest.kt
git commit -m "docs: record m0 compiler pass pipeline"
```
