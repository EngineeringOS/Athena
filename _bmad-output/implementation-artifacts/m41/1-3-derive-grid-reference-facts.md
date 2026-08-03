---
baseline_commit: 8fd6e34cfa2e182f1f0ae2bbe755c1bf9d2e739c
---

# Story 1.3: Derive Grid Reference Facts

Status: in-progress

## Story

As an engineer,
I want grid cells derived from each fact's own Sheet geometry,
so that later export can use stable positions without recomputing layout.

## Acceptance Criteria

1. Given two Sheets with different valid grid row/column counts and Drawing Areas, grid compilation
   maps every Occurrence rectangle center and Construct envelope center to exactly one typed Grid
   Reference using only its owning Sheet. Changing Sheet B grid or Drawing Area cannot change any
   Sheet A Grid Reference. Every reference carries stable typed subject identity, owning `sheetId`,
   owning `gridId`, exact subject Source Trace, zero-based row/column indices, one-based column
   number, row label, and cell reference. (FR-4.1, FR-4.2, FR-4.4, FR-9.1, FR-9.2)
2. Vertical grid rows use bijective base-26 uppercase labels `A/B/C...Z/AA/AB...`; horizontal grid
   columns use numbers `1/2/3...`. Cell references concatenate row then column, such as `A1` and
   `B3`. Mapping uses exact Drawing Area-relative rectangle-center arithmetic without floating
   point or integer-center truncation. Origin/top/left boundaries map to the first cell; an internal
   boundary maps to the row below or column to the right; bottom/right outer boundaries map to the
   final cell. Repeated compilation produces equal results. (FR-4.1, NFR-1, NFR-4)
3. Missing grid, blank grid identity, nonpositive row/column dimensions, unsupported row count, a
   missing/duplicate owning-Sheet definition, unknown/cross-Sheet subject geometry, or a center
   outside its Drawing Area fails the grid stage. It returns all deterministic issues sorted by
   subject then problem, each with exact subject, problem, correction, and Source Trace; it returns
   no partial Grid References and none reach Presentation. (FR-4.3, FR-10.2, FR-10.3, FR-10.4,
   NFR-2)
4. Given the Golden Fixture, active `ProjectionSpatialCompiler` output contains exactly one typed
   Grid Reference per Occurrence and Construct, ordered by owning Sheet order then typed subject
   kind and stable Projection identity. Unordered Projection/geometry input permutations produce
   an equal canonical list. `SpatialDocument.gridReferences` is not a raw string map, and no
   legacy first-grid, fixed-global-grid, Presentation, renderer, or export mapping authority
   remains on the active path. (FR-4.2, FR-4.4, FR-9.2, FR-9.3, FR-9.4, AD-25, AD-27, AD-30)

## Tasks / Subtasks

- [ ] Task 1: Prove exact typed grid behavior with RED tests (AC: 1, 2, 4)
  - [ ] Add `SpatialGridCompilerTest.kt` with hand-computable Occurrence and Construct rectangles,
        two independently configurable Sheet inputs, and literal expected typed Grid References.
        Prove `A1`, `B3`, `Z1`, `AA1`, and `AB1`; assert vertical letters and horizontal numbers.
  - [ ] Cover odd-size rectangles and every boundary rule with exact doubled-center arithmetic.
        Assert internal row/column boundary ownership and inclusive Drawing Area outer edges.
  - [ ] Compile repeatedly and permute Sheets, Occurrences, and Constructs. Compare complete typed
        output and prove changing only Sheet B cannot alter Sheet A facts. Record first failing RED
        assertion or compilation before production edits.
- [ ] Task 2: Add small typed Grid contracts (AC: 1, 2, 4)
  - [ ] Create `SpatialGridModels.kt` for closely related grid facts. Model Occurrence and Construct
        subjects as typed alternatives carrying `SpatialOccurrenceId` or `SpatialConstructId`;
        never overload an Occurrence ID or use delimiter-concatenated display strings as identity.
  - [ ] Add typed owning-Sheet grid definition and Grid Reference identity/fact. Preserve immutable,
        inspectable values for `sheetId`, `gridId`, Drawing Area, row/column counts, row/column
        indices, row label, one-based column number, cell reference, subject, and Source Trace.
  - [ ] Implement one canonical row-label function using bijective base-26. Support `A` through
        `ZZZ` (`1..18_278` rows); reject larger row counts as unsupported with a diagnostic before
        attempting mapping. Keep this compiler/presentation vocabulary only; add no Athena syntax.
- [ ] Task 3: Compile owning-Sheet Grid References (AC: 1, 2, 4)
  - [ ] Add `SpatialGridCompiler` as a cohesive stage. Consume typed per-Sheet grid definitions plus
        Story 1.1 Occurrence rectangles and Story 1.2 Construct envelopes unchanged. Do not derive
        from Region bounds and do not recompute placement/envelopes.
  - [ ] Map rectangle centers with overflow-safe doubled-coordinate `Long` arithmetic. Use half-open
        internal cells and explicitly clamp only an exact bottom/right Drawing Area boundary to the
        final row/column; never clamp an actually out-of-area center.
  - [ ] Preserve subject Source Trace exactly. Use stable typed identity independent of current cell,
        so movement changes the cell fact rather than subject identity. Canonically order Sheet,
        Occurrence, then Construct facts by authored Sheet order and stable typed Projection ID.
  - [ ] Produce a result with immutable grid/reference/diagnostic lists. If any validation or
        mapping issue exists, publish empty grid/reference lists and the complete canonical
        diagnostic set.
- [ ] Task 4: Fail closed on all grid defects (AC: 3)
  - [ ] Add separate RED/GREEN cases for missing grid, blank grid identity, zero/negative rows,
        zero/negative columns, row count above `18_278`, duplicate/unknown Sheet definitions,
        duplicate subject geometry, subject/Sheet mismatch, and centers outside every Drawing Area
        edge. Include integer-overflow edge fixtures.
  - [ ] Add one multi-defect fixture proving exact aggregation order, exact human-first correction,
        exact Source Trace, and no partial facts. Diagnostics must name the Sheet or typed subject;
        internal codes must not become primary language.
  - [ ] Keep invalid Projection grid values representable until the Spatial boundary can return
        `SpatialDiagnostic`; do not replace actionable compilation failure with constructor throws.
- [ ] Task 5: Integrate through the sole Spatial orchestrator and verify (AC: 1-4)
  - [ ] Invoke the grid stage from `ProjectionSpatialCompiler` after placement/grouping geometry and
        before complete `SpatialDocument` validation. Use compiler-owned per-Sheet Drawing Area
        facts; do not select the first grid or use a document-wide denominator.
  - [ ] Replace `SpatialDocument.gridReferences: Map<String, String>` with the typed canonical list.
        Migrate active compiler, Spatial-model, route/quality, Presentation, and tests only as needed;
        do not implement Story 3.1 Sheet assembly or Story 3.2 full validation early.
  - [ ] Remove or refactor `ProjectionSheetGrid.cellReferences()` so it cannot reverse axes, emit
        punctuation after `Z`, or become a second coordinate mapper. Keep Projection coordinate-free.
  - [ ] Run focused grid tests, full compiler tests, Projection-model tests, Spatial-model tests,
        repository tests, source-set hygiene audit, encoding audit, and `git diff --check`
        sequentially. Complete Debug Log, Completion Notes, File List, and Change Log before review.

## Dev Notes

### Architecture Guardrails

- Authority chain remains Athena source -> Engineering -> Projection -> Spatial -> Presentation ->
  Theia. Projection provides coordinate-free grid dimensions; Spatial alone maps geometry to cells.
- Apply AD-20, AD-25, AD-26, AD-27, AD-28, and AD-30. `SpatialGridCompiler` is an internal stage
  behind `ProjectionSpatialCompiler`, not another public orchestrator.
- User language is normative: vertical axis is lettered rows, horizontal axis is numbered columns;
  references read row then column (`A1`, `B3`). Do not expose lane/row compiler vocabulary in
  Athena authoring syntax.
- Public Spatial geometry remains integer drawing units. Grid mapping must not use `Double`, pixel
  geometry, Presentation coordinates, or renderer state.
- No raw map, global fixed-grid assumption, first-grid fallback, compatibility adapter, milestone
  type, solver/electrical field, or proof/demo/sample class may enter `src/main`.

### Exact Mapping Policy

For rectangle `r` and Drawing Area `a`, compute exact doubled centers:

```text
center2X = 2 * r.x + r.width
center2Y = 2 * r.y + r.height
areaLeft2 = 2 * a.x
areaTop2 = 2 * a.y
areaRight2 = 2 * (a.x + a.width)
areaBottom2 = 2 * (a.y + a.height)
```

- Reject center numerator outside inclusive outer bounds.
- If `center2X == areaRight2`, column index is `columns - 1`; otherwise:
  `columnIndex = ((center2X - areaLeft2) * columns) / (2 * a.width)`.
- If `center2Y == areaBottom2`, row index is `rows - 1`; otherwise:
  `rowIndex = ((center2Y - areaTop2) * rows) / (2 * a.height)`.
- Internal exact boundaries therefore enter the cell to the right/below. Column number is
  `columnIndex + 1`. Row label uses bijective base-26: `0=A`, `25=Z`, `26=AA`, `27=AB`.
- Use `Long` plus checked/range-safe operations. Never round a rectangle center to an integer first.

### Typed Fact Contract

- Recommended grouping: `SpatialGridModels.kt` may hold a small cluster such as
  `SpatialGridDefinition`, typed Grid Reference subject/id, and `SpatialGridReference`.
- `SpatialGridReferenceId` derives from owning Sheet plus typed subject identity. `cellReference`
  is output, not identity.
- Occurrence reference Source Trace equals its `SpatialOccurrenceGeometry.sourceTrace`; Construct
  reference Source Trace equals its `SpatialConstructGeometry.sourceTrace`. Grid-definition trace
  names owning Sheet and grid without replacing subject trace.
- Exact coverage means every input Occurrence and Construct has one and only one reference, and no
  Region receives a Grid Reference.

### Current Code Intelligence

- `ProjectionSheetGrid.cellReferences()` currently treats the second loop as numeric columns but
  documents an incorrect example and converts `A + row` directly, producing punctuation after 26
  rows. It has no validation and no covering test.
- `SpatialDocument.gridReferences` is currently `Map<String, String>` and is not populated by
  `ProjectionSpatialCompiler`; replace it rather than preserving a compatibility field.
- `ProjectionSpatialLayout.DRAWING_AREA` is current active Drawing Area policy. Grid compiler must
  accept explicit typed per-Sheet definitions so multi-Sheet tests can prove independent areas;
  active orchestration may create one definition per Projection Sheet using current policy until
  Story 3.1 assembles full `SpatialSheet` values.
- `SpatialGeometryCompiler` already emits canonical typed Constructs with exact envelopes and Source
  Trace. Consume those facts directly.
- `SpatialGeometryModels.kt` and `SpatialGroupingModels.kt` already own small typed geometry
  clusters. Keep grid models separate by responsibility and avoid growing `SpatialDocument.kt` into
  a mixed model dump.

### Previous Story Intelligence

- Stories 1.1 and 1.2 are `done`. Their focused suites, compiler suite, Spatial-model suite, full
  repository suite, source-set hygiene, encoding, and diff checks passed sequentially.
- Existing typed geometry uses Sheet-qualified IDs, defensive immutable lists, exact Source Trace,
  human-first diagnostics, and canonical ordering. Match those patterns.
- Review demanded literal expected facts, complete permutations, and exact diagnostic contracts.
  Self-comparisons, nonempty checks, and minimum-only assertions do not count.
- `AuthoredProjectionSpatialBridge`, `ProjectionToSpatialTransformation`,
  `SpatialPlacementCompiler`, and `SpatialQualityMetricsReporter` were deleted. Do not recreate any
  of them.
- Resolve three explicit Story 1.1 deferrals now: correct axes, labels beyond `Z`, and blank/nonpositive
  grid validation. Remove those entries from `deferred-work.md` only after passing acceptance proof.

### Expected Files

- Create: `kernel/spatial-model/src/main/kotlin/com/engineeringood/athena/spatial/SpatialGridModels.kt`
- Create: `kernel/compiler/src/main/kotlin/com/engineeringood/athena/compiler/SpatialGridCompiler.kt`
- Create: `kernel/compiler/src/test/kotlin/com/engineeringood/athena/compiler/SpatialGridCompilerTest.kt`
- Update: `kernel/compiler/src/main/kotlin/com/engineeringood/athena/compiler/ProjectionSpatialCompiler.kt`
- Update: `kernel/projection-model/src/main/kotlin/com/engineeringood/athena/projection/ProjectionSheets.kt`
- Update: `kernel/spatial-model/src/main/kotlin/com/engineeringood/athena/spatial/SpatialDocument.kt`
- Update relevant Spatial/Projection contract tests and active callers only where typed Grid
  References require migration.
- Update: `_bmad-output/implementation-artifacts/m41/deferred-work.md`
- Preserve Story 1.1 placement and Story 1.2 grouping geometry byte-equivalence under their focused
  suites.

### Testing Requirements

- Follow RED-GREEN-REFACTOR per task. Record each first failing assertion/compilation before
  production edits.
- Assert literal typed facts and exact diagnostics. Include odd-center, internal boundary,
  outer-boundary, multi-Sheet, permutation, row-label, duplicate, and overflow fixtures.
- Prove active-path failure returns no `SpatialDocument`; do not only unit-test helper functions.
- Run Gradle commands strictly sequentially on Windows:

```powershell
.\gradlew.bat --no-daemon --console=plain :kernel:compiler:test --tests "*SpatialGridCompilerTest*"
.\gradlew.bat --no-daemon --console=plain :kernel:compiler:test
.\gradlew.bat --no-daemon --console=plain :kernel:projection-model:test
.\gradlew.bat --no-daemon --console=plain :kernel:spatial-model:test
.\gradlew.bat --no-daemon --console=plain test
powershell -ExecutionPolicy Bypass -File .\tools\source-set-hygiene-audit.ps1
powershell -ExecutionPolicy Bypass -File .\tools\encoding-audit.ps1
git diff --check
```

### Scope Boundaries

- Epic 2 owns port Anchors, Routes, and Lanes.
- Story 3.1 owns complete typed per-Sheet `SpatialDocument` assembly and Presentation Sheet
  preservation.
- Story 3.2 owns complete document-wide validation, including duplicate typed fact identities.
- Epic 4 owns quality metrics and baseline.
- M42 owns labels/grid chrome. M43 consumes Grid References for rendering/export without
  recomputation. M44 owns readability optimization. M45 owns professional routing.

### Git Intelligence

- Current baseline/HEAD: `8fd6e34cfa2e182f1f0ae2bbe755c1bf9d2e739c`.
- Recent M41 commits contain recovery design/planning only. Implementation remains in a large dirty
  worktree with many untracked files. Read current disk state before every edit; never revert or
  overwrite unrelated user changes.

### References

- [Source: `_bmad-output/implementation-artifacts/m41/epics.md` Epic 1, Story 1.3]
- [Source: `_bmad-output/planning-artifacts/prds/prd-Athena-2026-08-03-m41/prd.md` FR-4, FR-9, FR-10, Sections 11-13]
- [Source: `_bmad-output/planning-artifacts/prds/prd-Athena-2026-08-03-m41/addendum.md` Confirmed Decisions 3, 7-10]
- [Source: `_bmad-output/planning-artifacts/architecture/architecture-Athena-2026-08-03-m41/ARCHITECTURE-SPINE.md` AD-20, AD-25-AD-30]
- [Source: `docs/superpowers/specs/2026-08-03-m41-spatial-reality-recovery-design.md` Spatial Contract, Verification Design, Story Recovery]
- [Source: `_bmad-output/implementation-artifacts/m41/m41-delivery-audit-2026-08-03.md` Findings 9-11]
- [Source: `_bmad-output/implementation-artifacts/m41/1-2-derive-bounds-and-alignment.md`]
- [Source: `_bmad-output/implementation-artifacts/m41/deferred-work.md` Story 1.3 grid deferrals]

## Dev Agent Record

### Agent Model Used

Codex GPT-5

### Debug Log References

- Story context created from milestone-local M41 sprint, epics, PRD, addendum, architecture,
  approved design, failed-delivery audit, previous-story records, deferred work, current code,
  and git state.

### Completion Notes List

- Ultimate context engine analysis completed - comprehensive developer guide created.

### File List

- `_bmad-output/implementation-artifacts/m41/1-3-derive-grid-reference-facts.md`
- `_bmad-output/implementation-artifacts/m41/sprint-status.yaml`

### Change Log

- 2026-08-03: Created through BMad create-story from milestone-local M41 artifacts.
