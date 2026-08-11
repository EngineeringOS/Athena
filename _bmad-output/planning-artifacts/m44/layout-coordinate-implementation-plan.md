# M44 Stable Sheet Coordinates Implementation Plan

> **For agentic workers:** REQUIRED SUB-SKILL: Use superpowers:executing-plans to implement this plan task-by-task. Steps use checkbox (`- [ ]`) syntax for tracking.

**Goal:** Replace frame-relative M44 placement coordinates with stable Sheet Points, while retaining EPLAN-style frame navigation and independent grid snapping.

**Architecture:** The Sheet companion owns `frame`, `snap`, and occurrence `SheetPoint`s. The Canonical Scene carries independent `ScenePlotFrame` and `SceneSnapGrid` values. The frontend consumes these values for chrome and transient snapping; it never maps a point into source text or owns persistent coordinate truth.

**Tech Stack:** Kotlin 2.4.0, JUnit, LSP4J 0.23.1, TypeScript 5.9.2, React 18.3.1, Konva 10.3.0, Theia 1.73.1.

---

### Task 1: Replace Sheet companion coordinate grammar and model

**Files:**
- Modify: `kernel/language/src/main/kotlin/com/engineeringood/athena/language/SheetCompanionLanguage.kt`
- Modify: `kernel/language/src/main/kotlin/com/engineeringood/athena/language/SheetCompanionEditor.kt`
- Modify: `kernel/language/src/test/kotlin/com/engineeringood/athena/language/SheetCompanionLanguageTest.kt`
- Modify: `kernel/language/src/test/kotlin/com/engineeringood/athena/language/SheetCompanionEditorTest.kt`
- Modify: `kernel/language/src/test/kotlin/com/engineeringood/athena/language/LanguageFacadeBoundaryTest.kt`

- [ ] **Step 1: Write failing parser tests for frame, snap, and stable points**

```kotlin
@Test
fun `parses frame snap and canonical occurrence point`() {
    val result = parser.parse(
        "sheet.sheet.athena",
        """
        sheet demo {
          page format A3 landscape
          frame: 17 * 16
          snap: 1
          "Q1" at (24, 16)
        }
        """.trimIndent(),
    )

    val source = assertIs<SheetCompanionParseSuccess>(result).source
    assertEquals(SheetPlotFrameIntent(columns = 17, rows = 16, span = source.frame.span), source.frame)
    assertEquals(SheetSnapIntent(step = 1, span = source.snap.span), source.snap)
    assertEquals(SheetPoint(24, 16, source.placements.single().point.span), source.placements.single().point)
}

@Test
fun `rejects retired cell and micro placement syntax`() {
    val result = parser.parse(
        "sheet.sheet.athena",
        "sheet demo {\npage format A3 landscape\ngrid: 17 * 16 cell: 4\n\"Q1\" at A2 micro(2,1)\n}",
    )

    val failure = assertIs<SheetCompanionParseFailure>(result)
    assertTrue(failure.diagnostics.any { it.message.contains("Unknown Sheet Companion statement") })
}
```

- [ ] **Step 2: Run parser tests and verify expected failure**

Run:

```powershell
.\gradlew.bat --no-daemon --console=plain :kernel:language:test --tests "*SheetCompanionLanguageTest"
```

Expected: FAIL because `frame`, `snap`, and point placement are not parsed.

- [ ] **Step 3: Implement only stable Sheet companion contracts**

```kotlin
data class SheetPlotFrameIntent(
    val columns: Int,
    val rows: Int,
    val span: SourceSpan,
)

data class SheetSnapIntent(
    val step: Int,
    val span: SourceSpan,
)

data class SheetPoint(
    val x: Int,
    val y: Int,
    val span: SourceSpan,
)

data class SheetPlacementIntent(
    val occurrence: String,
    val point: SheetPoint,
    val locked: Boolean,
    val span: SourceSpan,
)
```

Parse only:

```text
frame: <positive columns> * <positive rows>
snap: <positive step>
"<occurrence>" at (<positive x>, <positive y>) [lock]
```

Rewrite `SheetCompanionEditor` to deterministically replace or insert the exact point syntax and preserve
`lock`, source order, and a single terminal newline. Remove all address/micro conversion helpers.

- [ ] **Step 4: Run language tests and verify green**

Run:

```powershell
.\gradlew.bat --no-daemon --console=plain :kernel:language:test
```

Expected: PASS.

### Task 2: Separate plot frame from scene snap grid

**Files:**
- Modify: `kernel/presentation-model/src/main/kotlin/com/engineeringood/athena/presentation/PresentationContracts.kt`
- Modify: `kernel/presentation-model/src/main/resources/schema/athena-scene-publication.schema.json`
- Modify: `kernel/presentation-model/src/test/kotlin/com/engineeringood/athena/presentation/AthenaDiagramSceneContractTest.kt`
- Modify: compiler files selected by `rg "SceneSnapGrid|SheetGridIntent|grid\\.cell" kernel/compiler`
- Modify: `ide/lsp/src/main/kotlin/com/engineeringood/athena/ide/lsp/AthenaDiagramProtocol.kt`
- Modify: generated frontend contract files only through their checked-in generator

- [ ] **Step 1: Write failing scene contract tests**

```kotlin
@Test
fun `plot frame changes do not reinterpret occurrence placement anchor`() {
    val scene = sceneWith(
        plotFrame = ScenePlotFrame(columns = 17, rows = 16, columnLabels = ColumnLabels.ALPHA, rowLabels = RowLabels.NUMERIC),
        snapGrid = SceneSnapGrid(sheetId = "sheet", step = 1, drawingOrigin = ScenePoint(0, 0)),
        occurrences = listOf(occurrence(anchor = ScenePoint(24, 16))),
    )

    assertEquals(ScenePoint(24, 16), scene.occurrences.single().placementAnchor)
}
```

- [ ] **Step 2: Run focused presentation-model test and verify expected failure**

Run:

```powershell
.\gradlew.bat --no-daemon --console=plain :kernel:presentation-model:test --tests "*AthenaDiagramSceneContractTest"
```

Expected: FAIL because `ScenePlotFrame` and point-only `SceneSnapGrid` do not exist.

- [ ] **Step 3: Implement independent scene facts**

```kotlin
data class ScenePlotFrame(
    val columns: Int,
    val rows: Int,
    val columnLabels: CoordinateLabelMode = CoordinateLabelMode.ALPHA,
    val rowLabels: CoordinateLabelMode = CoordinateLabelMode.NUMERIC,
)

data class SceneSnapGrid(
    val sheetId: String,
    val step: Int,
    val drawingOrigin: ScenePoint,
    val formulaVersion: String = "athena-grid-2",
)
```

Compiler page bounds remain canonical Sheet units. It must lower a persisted `SheetPoint` directly to
`ScenePoint`; frame counts may affect only `ScenePlotFrame` decorations and point-to-reference display.
Update the scene schema, Kotlin-to-wire mapper, generated TypeScript contract, and contract tests.

- [ ] **Step 4: Run presentation and compiler suites sequentially**

Run:

```powershell
.\gradlew.bat --no-daemon --console=plain :kernel:presentation-model:test
.\gradlew.bat --no-daemon --console=plain :kernel:compiler:test
```

Expected: PASS.

### Task 3: Replace LSP placement codec and transaction tests

**Files:**
- Modify: `kernel/interaction-model/src/main/kotlin/com/engineeringood/athena/interaction/EditOperationContracts.kt`
- Modify: `ide/lsp/src/main/kotlin/com/engineeringood/athena/ide/lsp/EditOperationWireMapper.kt`
- Modify: `ide/lsp/src/main/kotlin/com/engineeringood/athena/ide/lsp/PlacementOperationPlanner.kt`
- Modify: `ide/lsp/src/main/kotlin/com/engineeringood/athena/ide/lsp/PlacementOperationHandler.kt`
- Modify: `ide/lsp/src/test/kotlin/com/engineeringood/athena/ide/lsp/PlacementOperationPlannerTest.kt`
- Modify: `ide/lsp/src/test/kotlin/com/engineeringood/athena/ide/lsp/PlacementOperationHandlerTest.kt`

- [ ] **Step 1: Write failing planner tests for frame-independent persisted points**

```kotlin
@Test
fun `move persists exact SheetPoint independent of plot frame`() {
    val plan = planner.plan(
        scene(plotFrameColumns = 17, plotFrameRows = 16, snapStep = 2),
        MoveOccurrence(SHEET, "Q1", SheetPoint(24, 16), LockAction.PRESERVE),
    )

    assertEquals(SheetPoint(24, 16), plan.single().point)
}

@Test
fun `snap quantizes point by snap step without changing stored prior point on step change`() {
    assertEquals(SheetPoint(24, 16), planner.snap(SheetPoint(23, 15), step = 2))
}
```

- [ ] **Step 2: Run planner tests and verify expected failure**

Run:

```powershell
.\gradlew.bat --no-daemon --console=plain :ide:lsp:test --tests "*PlacementOperationPlannerTest"
```

Expected: FAIL because `SheetAnchor` and frame-relative micro codec still exist.

- [ ] **Step 3: Implement point-only operations**

Replace `SheetAnchor(address, microX, microY)` with `SheetPoint(x, y)`. Move carries user-selected
point. Snap derives nearest point permitted by `SceneSnapGrid.step`. Align and Distribute calculate
canonical points, quantize only by `step`, and validate bounds against `ScenePage.drawingBounds`.
The handler writes only point syntax using `SheetCompanionEditor` and preserves lock behavior and the
existing source transaction engine.

- [ ] **Step 4: Run LSP suite and verify green**

Run:

```powershell
.\gradlew.bat --no-daemon --console=plain :ide:lsp:test
```

Expected: PASS.

### Task 4: Render correct rulers and transient snap behavior

**Files:**
- Modify: `ide/theia-frontend/src/browser/athena-presentation-widget.tsx`
- Modify: `ide/theia-frontend/src/browser/diagram/grid-snap.ts`
- Modify: `ide/theia-frontend/src/browser/diagram/konva-diagram-adapter.ts`
- Modify: `ide/theia-frontend/scripts/athena-konva-adapter.test.mjs`
- Modify: frontend contract tests selected by `rg "snapGrid|editorRulerLabels|SheetAnchor" ide/theia-frontend`

- [ ] **Step 1: Write failing frontend tests**

```javascript
test('renders alpha top columns and numeric left rows from plot frame', () => {
  assert.deepEqual(editorRulerLabels({ columns: 3, rows: 2 }), {
    columns: ['A', 'B', 'C'],
    rows: ['1', '2'],
  });
});

test('snap quantizes point by step while grid lines remain optional', () => {
  assert.deepEqual(snapPoint({ x: 23, y: 15 }, 2), { x: 24, y: 16 });
});
```

- [ ] **Step 2: Run frontend test and verify expected failure**

Run:

```powershell
Set-Location ide\theia-frontend
node --test scripts/athena-konva-adapter.test.mjs
```

Expected: FAIL because rulers consume `SceneSnapGrid` rows/columns and row labels are alphabetic.

- [ ] **Step 3: Implement renderer-only mapping**

Render rulers from `scene.plotFrame`, with alpha labels in top horizontal cells and numeric labels in left
vertical cells. Keep ruler DOM outside Konva. Drag preview and all operation payloads use canonical
`SheetPoint`; optional grid-line paint and snap enablement only affect transient interaction.

- [ ] **Step 4: Build frontend and verify green**

Run:

```powershell
Set-Location ide\theia-frontend
node --test scripts/athena-konva-adapter.test.mjs
yarn test
yarn build
```

Expected: PASS.

### Task 5: Replace active M44 source and prove stability

**Files:**
- Modify: `examples/m44/rolling-shutter/src/com/engineeringood/m44/rollingshutter/rolling-shutter.sheet.athena`
- Modify: M44 source/tests selected by repository search for retired `micro(` and `cell:`
- Create: `_bmad-output/implementation-artifacts/m44/operation-transcripts/layout-coordinate-correction.md`
- Create: `_bmad-output/implementation-artifacts/m44/screenshots/layout-coordinate-desktop.png`
- Create: `_bmad-output/implementation-artifacts/m44/screenshots/layout-coordinate-narrow.png`

- [ ] **Step 1: Write failing compiler/LSP integration tests**

```kotlin
@Test
fun `same persisted point survives frame and snap source changes`() {
    val initial = compile(sheet(frame = "17 * 16", snap = 1, point = "(24, 16)"))
    val changed = compile(sheet(frame = "10 * 12", snap = 2, point = "(24, 16)"))

    assertEquals(initial.occurrence("Q1").placementAnchor, changed.occurrence("Q1").placementAnchor)
}
```

- [ ] **Step 2: Run focused integration test and verify expected failure**

Run:

```powershell
.\gradlew.bat --no-daemon --console=plain :kernel:compiler:test :ide:lsp:test
```

Expected: FAIL before all previous tasks are complete.

- [ ] **Step 3: Update M44 example and add evidence**

Replace every retired `grid: ... cell: ...` and `micro(...)` construct. Capture before/after/reopen
digests and prove Move, Align, Distribute, and Snap persist only canonical points. Capture desktop and
narrow screenshots after rebuilding frontend and product.

- [ ] **Step 4: Run complete sequential verification**

Run:

```powershell
.\gradlew.bat --no-daemon --console=plain test
Set-Location ide\theia-frontend
yarn test
yarn build
Set-Location ..\theia-product
yarn build
yarn verify:m44-style
Set-Location ..\..
powershell -ExecutionPolicy Bypass -File .\tools\encoding-audit.ps1
powershell -ExecutionPolicy Bypass -File .\tools\source-set-hygiene-audit.ps1
git diff --check
```

Expected: PASS. Product proof must show nonblank canvas, correctly oriented rulers, hidden grid lines by
default, and stable occurrence points after frame/snap edits.

## Plan Self-Review

- Stable coordinates, independent frame/snap, direction correction, source round-trip, LSP transaction
  path, Theia rendering, and live M44 evidence map to Tasks 1 through 5.
- No retired `cell` or `micro` compatibility path remains in the target design.
- Public names are consistent: `SheetPoint`, `SheetPlotFrameIntent`, `SheetSnapIntent`, `ScenePlotFrame`,
  and `SceneSnapGrid.step`.
