# Athena M46 Semantic Connection Kernel Implementation Plan

> **For agentic workers:** REQUIRED SUB-SKILL: use `bmad-create-story`, then `bmad-dev-story`, for every task in order. Each story file is the detailed implementation authority.

**Goal:** Build source-owned Engineering Connections/Nets, canonical Connection IR, deterministic professional route planning, editable Theia connection UX, and verified M46 rolling-shutter evidence.

**Architecture:** Semantic-first staged compiler: Engineering Reality -> Connection IR -> Connection Projection -> ConnectionRoutePlan -> SceneConnection -> Theia/SVG. All edits are typed server source transactions; renderers remain disposable.

**Tech Stack:** Kotlin 2.4.0, ANTLR 4.13.2, LSP4J 0.23.1, TypeScript 5.9.2, Theia 1.73.1, React 18.3.1, Konva 10.3.0, Node >=22, Yarn 1.22.22.

---

## File Map

- `kernel/engineering-model`: authored Connection/Net/Endpoint/Kind/specification facts.
- `kernel/connection-model`: canonical Connection IR, topology, canonicalization, digest.
- `kernel/language` and `kernel/compiler`: syntax, lowering, validation orchestration, projection, planning.
- `kernel/projection-model`: coordinate-free ConnectionProjection.
- `kernel/spatial-model`: typed ConnectionRoutePlan geometry/topology.
- `kernel/presentation-model` and `kernel/svg-renderer`: SceneConnection and deterministic export.
- `kernel/interaction-model`, `ide/lsp`: typed edits, transactions, read model.
- `ide/theia-frontend`: professional paint, selection, Navigator, performance instrumentation.
- `examples/m46/rolling-shutter`: independent golden project.
- `_bmad-output/implementation-artifacts/m46`: tests, logs, exports, screenshots, closure.

## Story Execution Rule

For every task below:

- [ ] Create exact story with `bmad-create-story`; mark `ready-for-dev`.
- [ ] Run failing story-scoped tests first and record RED evidence.
- [ ] Implement only story tasks; delete superseded contracts immediately.
- [ ] Run story tests and affected regression modules sequentially.
- [ ] Update story Tasks, Debug Log, Completion Notes, File List, Change Log; mark `review` only after all ACs pass.
- [ ] Review evidence, mark story `done`, then create next story so previous learning lands.

### Task 1: Story 1.1 - Typed Binary Engineering Connections

**Files:**
- Create: `kernel/engineering-model/src/main/kotlin/com/engineeringood/athena/ir/EngineeringConnectionModels.kt`
- Modify: `kernel/engineering-model/src/main/kotlin/com/engineeringood/athena/ir/EngineeringModel.kt`
- Modify: `kernel/language/src/main/antlr/Athena.g4`
- Modify: `kernel/compiler/src/main/kotlin/com/engineeringood/athena/compiler/EngineeringIrLowerer.kt`
- Test: engineering-model, language, compiler, tree-sitter, LSP syntax/outline tests.

**Verify:**
```powershell
.\gradlew.bat --no-daemon --console=plain :kernel:engineering-model:test :kernel:language:test :kernel:compiler:test
```

### Task 2: Story 1.2 - Nets And Specifications

**Files:** `EngineeringConnectionModels.kt`, language grammar/lowering, typed EngineeringValue tests.

**Verify:**
```powershell
.\gradlew.bat --no-daemon --console=plain :kernel:engineering-model:test :kernel:language:test :kernel:compiler:test
```

### Task 3: Story 1.3 - Canonical Connection IR

**Files:**
- Create module: `kernel/connection-model/`
- Create: `ConnectionIrModels.kt`, `ConnectionTopologyModels.kt`, `ConnectionCanonicalProtocol.kt`
- Create: `kernel/compiler/.../ConnectionIrCompiler.kt`
- Modify: `settings.gradle.kts`, compiler/validation build files and compile pipeline.

**Verify:**
```powershell
.\gradlew.bat --no-daemon --console=plain :kernel:connection-model:test :kernel:validation:test :kernel:compiler:test
```

### Task 4: Story 1.4 - Connection Projection Replacement

**Files:**
- Replace: `kernel/projection-model/.../ProjectionElements.kt` connection section with `ConnectionProjectionModels.kt`.
- Modify: engineering-to-projection and spatial coverage compilers/tests.
- Delete: retired `ProjectionConnection` behavior/tests.

**Verify:**
```powershell
.\gradlew.bat --no-daemon --console=plain :kernel:projection-model:test :kernel:compiler:test
```

### Task 5: Story 2.1 - Deterministic ConnectionRoutePlan

**Files:**
- Create: `kernel/spatial-model/.../ConnectionRoutePlanModels.kt`
- Create: `kernel/compiler/.../ConnectionRoutePlanner.kt`
- Delete: `SpatialRouteCompiler.kt` and retired `SpatialRoute` contracts/tests.

**Verify:**
```powershell
.\gradlew.bat --no-daemon --console=plain :kernel:spatial-model:test :kernel:compiler:test
```

### Task 6: Story 2.2 - Topology Geometry

**Files:** `ConnectionRoutePlanModels.kt`, `ConnectionRoutePlanner.kt`, topology geometry tests.

**Verify:**
```powershell
.\gradlew.bat --no-daemon --console=plain :kernel:spatial-model:test :kernel:compiler:test
```

### Task 7: Story 2.3 - Annotations And Quality

**Files:**
- Create: `kernel/compiler/.../ConnectionAnnotationPlanner.kt`
- Modify: spatial quality/authority validators and tests.

**Verify:**
```powershell
.\gradlew.bat --no-daemon --console=plain :kernel:spatial-model:test :kernel:compiler:test
```

### Task 8: Story 3.1 - SceneConnection And SVG

**Files:**
- Replace: `SceneRoute` with `SceneConnection` in presentation contracts/canonical protocol/schema.
- Modify: `AthenaDiagramSceneCompiler.kt`, `AthenaSvgRenderer.kt`, generated frontend contracts.
- Delete: legacy route schema/tests.

**Verify:**
```powershell
.\gradlew.bat --no-daemon --console=plain :kernel:presentation-model:test :kernel:svg-renderer:test :kernel:compiler:test
```

### Task 9: Story 3.2 - Professional Konva Paint And Selection

**Files:** `konva-diagram-adapter.ts`, generated contracts, frontend adapter/style tests.

**Verify:**
```powershell
Set-Location ide
yarn test
```

### Task 10: Story 3.3 - Connection Navigator

**Files:**
- Create: `ide/lsp/.../ConnectionReadModelService.kt`
- Create: Theia Connection Navigator widget/model/contribution.
- Modify: Athena frontend module and semantic selection service.

**Verify:**
```powershell
.\gradlew.bat --no-daemon --console=plain :ide:lsp:test
Set-Location ide
yarn test
```

### Task 11: Story 3.4 - Transactional Connection Editing

**Files:**
- Create: `kernel/interaction-model/.../ConnectionEditOperations.kt`
- Create: `ide/lsp/.../ConnectionOperationHandler.kt`
- Modify: source transaction/journal/wire protocol and Konva intent callbacks.

**Verify:**
```powershell
.\gradlew.bat --no-daemon --console=plain :kernel:interaction-model:test :ide:lsp:test
Set-Location ide
yarn test
```

### Task 12: Story 4.1 - M46 Rolling-Shutter Project

**Files:** create `examples/m46/rolling-shutter/` source, packages, manifest, lock, Sheet, style, bindings; add compiler/LSP golden tests.

**Verify:**
```powershell
.\gradlew.bat --no-daemon --console=plain :kernel:compiler:test :ide:lsp:test
```

### Task 12.5: Story 4.4 - M46 Folio And Independent Page Companions

**Files:** add the Folio Companion language/model, page-companion discovery and compilation, deterministic
page switching in Theia, M46 power/CPU-control Page Companions, and compiler/LSP/frontend acceptance tests.

**Verify:**
```powershell
.\gradlew.bat --no-daemon --console=plain :kernel:language:test :kernel:compiler:test :ide:lsp:test
Set-Location ide\theia-frontend
yarn test
yarn build
```

### Task 13: Story 4.2 - Incremental Performance Proof

**Files:** frontend benchmark model/tests; product benchmark launcher/verifier; M46 evidence JSON.

**Verify:**
```powershell
Set-Location ide
yarn test
yarn build
yarn workspace @engineeringood/athena-theia-product verify:m46-performance
```

### Task 14: Story 4.3 - Product Edit/Reopen/Export Proof

**Files:** product proof launcher/verifier, deterministic export verifier, screenshots and transcripts under M46 artifacts.

**Verify:**
```powershell
.\gradlew.bat --no-daemon --console=plain :ide:lsp:installDist
Set-Location ide
yarn build
yarn workspace @engineeringood/athena-theia-product verify:m46-proof
yarn workspace @engineeringood/athena-theia-product verify:m46-export
```

### Task 15: Story 5.1 - Full Verification And Hygiene

**Files:** M46 verification log/summary/acceptance audit; no production proof helpers.

**Verify:** run affected Gradle tasks sequentially, then frontend tests/build/product proof, source-set hygiene,
encoding audit, and forbidden legacy scans. Record exact commands, counts, exits, digests.

### Task 16: Story 5.2 - Closure And Retrospective

**Files:** `M46-CLOSURE.md`, `M46-RETROSPECTIVE.md`, final `sprint-status.yaml`.

**Verify:** every story/FR/NFR/UX requirement maps to fresh passing evidence; no unresolved critical/high
finding; all status transitions match actual proof.
