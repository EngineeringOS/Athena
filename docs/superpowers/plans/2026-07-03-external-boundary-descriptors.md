# External Boundary Descriptors Implementation Plan

> **For agentic workers:** REQUIRED SUB-SKILL: Use superpowers:subagent-driven-development (recommended) or superpowers:executing-plans to implement this plan task-by-task. Steps use checkbox (`- [ ]`) syntax for tracking.

**Goal:** Build a compiler-owned external boundary descriptor surface that validates passive standards and runtime boundary contracts while keeping `Engineering IR` as the only semantic authority.

**Architecture:** Add a new `compiler.boundary` package with a local `.properties` loader, deterministic validator, and inspectable validation report. Surface that report through the compiler facade as metadata only, without changing public pass ordering or operational behavior.

**Tech Stack:** Kotlin 2.4.0, Java 25, Gradle 9.6.1, JUnit, local `.properties` manifests

---

### Task 1: Boundary Descriptor Validation Tests

**Files:**
- Create: `compiler/src/test/kotlin/com/engineeringood/athena/compiler/AthenaBoundaryDescriptorLoaderTest.kt`
- Create: `compiler/src/test/resources/boundary-descriptors/automationml-reference/athena-boundary.properties`
- Create: `compiler/src/test/resources/boundary-descriptors/opc-ua-runtime/athena-boundary.properties`
- Create: `compiler/src/test/resources/boundary-descriptors/external-authority/athena-boundary.properties`
- Create: `compiler/src/test/resources/boundary-descriptors/operational-execution/athena-boundary.properties`

- [ ] **Step 1: Write the failing test**

```kotlin
@Test
fun `loads valid passive boundary descriptors and rejects sovereign or operational descriptors`() {
    val source = AthenaBoundaryDescriptorSource(
        descriptorRoots = listOf(
            repoRoot.resolve("compiler/src/test/resources/boundary-descriptors/automationml-reference"),
            repoRoot.resolve("compiler/src/test/resources/boundary-descriptors/opc-ua-runtime"),
            repoRoot.resolve("compiler/src/test/resources/boundary-descriptors/external-authority"),
            repoRoot.resolve("compiler/src/test/resources/boundary-descriptors/operational-execution"),
        ),
    )

    val report = AthenaBoundaryDescriptorResolver().resolve(source)

    assertEquals(listOf("automationml.reference", "opcua.runtime.bridge"), report.validDescriptors.map { it.descriptorId })
    assertEquals(
        listOf(
            "boundary.descriptor.authority.external-canonical-forbidden",
            "boundary.descriptor.mode.operational-not-supported",
        ),
        report.rejectedDescriptors.flatMap { it.diagnostics.map { diagnostic -> diagnostic.ruleId.value } },
    )
}
```

- [ ] **Step 2: Run test to verify it fails**

Run: `java25; .\gradlew.bat --no-daemon --console=plain :kernel:compiler:test --tests "com.engineeringood.athena.compiler.AthenaBoundaryDescriptorLoaderTest"`
Expected: FAIL because boundary descriptor types do not exist yet.

- [ ] **Step 3: Write minimal implementation**

```kotlin
class AthenaBoundaryDescriptorResolver(
    private val loader: AthenaBoundaryDescriptorLoader = AthenaBoundaryDescriptorLoader(),
) {
    fun resolve(source: AthenaBoundaryDescriptorSource): AthenaBoundaryValidationReport = TODO()
}
```

- [ ] **Step 4: Run test to verify it passes**

Run: `java25; .\gradlew.bat --no-daemon --console=plain :kernel:compiler:test --tests "com.engineeringood.athena.compiler.AthenaBoundaryDescriptorLoaderTest"`
Expected: PASS

### Task 2: Compiler Integration Test

**Files:**
- Modify: `compiler/src/test/kotlin/com/engineeringood/athena/compiler/AthenaCompilerTest.kt`
- Modify: `compiler/src/main/kotlin/com/engineeringood/athena/compiler/CompilerModels.kt`
- Modify: `compiler/src/main/kotlin/com/engineeringood/athena/compiler/AthenaCompiler.kt`

- [ ] **Step 1: Write the failing test**

```kotlin
@Test
fun `compile exposes boundary validation metadata without changing pass order`() {
    val compiler = AthenaCompiler(
        boundaryDescriptorSource = AthenaBoundaryDescriptorSource(
            descriptorRoots = listOf(
                repoRoot.resolve("compiler/src/test/resources/boundary-descriptors/automationml-reference"),
                repoRoot.resolve("compiler/src/test/resources/boundary-descriptors/opc-ua-runtime"),
            ),
        ),
    )

    val result = assertIs<CompilerCompilationSuccess>(compiler.compile(examplePath))

    assertEquals(
        listOf(CompilerPassId.PARSE, CompilerPassId.LOWER, CompilerPassId.VALIDATE, CompilerPassId.DOWNSTREAM_DERIVATION),
        result.pipeline.passes.map { it.pass.id },
    )
    assertEquals(listOf("automationml.reference", "opcua.runtime.bridge"), result.boundaryValidation.validDescriptors.map { it.descriptorId })
}
```

- [ ] **Step 2: Run test to verify it fails**

Run: `java25; .\gradlew.bat --no-daemon --console=plain :kernel:compiler:test --tests "com.engineeringood.athena.compiler.AthenaCompilerTest"`
Expected: FAIL because compile results do not expose boundary validation yet.

- [ ] **Step 3: Write minimal implementation**

```kotlin
data class CompilerCompilationSuccess(
    val boundaryValidation: AthenaBoundaryValidationReport,
    // existing fields
)
```

- [ ] **Step 4: Run test to verify it passes**

Run: `java25; .\gradlew.bat --no-daemon --console=plain :kernel:compiler:test --tests "com.engineeringood.athena.compiler.AthenaCompilerTest"`
Expected: PASS

### Task 3: Boundary Documentation

**Files:**
- Create: `docs/compiler/m0-external-boundary-descriptors.md`

- [ ] **Step 1: Write the documentation**

```md
# M0 External Boundary Descriptors

Story `2.6` defines passive boundary metadata for standards and runtime or enterprise contexts.
These descriptors do not activate plugins, do not load governed knowledge, and do not implement connectors.
`Engineering IR` remains the sole semantic authority.
```

- [ ] **Step 2: Verify documentation scope**

Run: manual review
Expected: the doc distinguishes boundary descriptors from plugin discovery and governed knowledge packages.

### Task 4: Full Verification

**Files:**
- Modify: `_bmad-output/implementation-artifacts/archive-m0/2-6-define-external-boundary-contract-descriptors.md`
- Modify: `_bmad-output/implementation-artifacts/sprint-status.yaml`

- [ ] **Step 1: Run targeted compiler tests**

Run: `java25; .\gradlew.bat --no-daemon --console=plain :kernel:compiler:test`
Expected: PASS

- [ ] **Step 2: Run full build**

Run: `java25; .\gradlew.bat --no-daemon --console=plain build`
Expected: PASS

- [ ] **Step 3: Run full test suite**

Run: `java25; .\gradlew.bat --no-daemon --console=plain test`
Expected: PASS

- [ ] **Step 4: Update story bookkeeping**

```md
- set story status to `done`
- check completed tasks
- record changed files
- summarize verification evidence
```
