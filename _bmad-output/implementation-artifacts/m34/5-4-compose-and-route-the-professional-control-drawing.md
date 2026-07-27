---
baseline_commit: 0311ad67c08779db3d1def271d0ac948a4683128
---

# Story 5.4: Compose And Route The Professional Control Drawing

Status: review

## Story

As a customer reviewing Athena,
I want the complete rolling-shutter circuit composed as a professional engineering sheet,
so that Athena demonstrates a credible product rather than a row of symbols.

## Acceptance Criteria

1. **Given** the Story 5.3 semantic model, resolved package material, and explicit grid placement,
   **when** professional drawing compilation runs, **then** one `schematic`-backed Control Drawing
   contains a derived 17-column by 8-row frame, coordinate bands, title block, separate power and
   control regions, all 22 placed occurrences, device labels, terminal labels, and stable bounds;
   drawing structure is never modeled as an engineering device.
2. **Given** 34 semantic connections and function-aware terminal bindings, **when** routing runs,
   **then** every conductor starts and ends at the exact compiled occurrence anchor, uses deterministic
   orthogonal segments, avoids occurrence and label interiors, and publishes explicit junction versus
   crossing facts without frontend or renderer engineering inference.
3. **Given** the focused Theia workbench at desktop and narrow viewport sizes, **when** the drawing is
   transported and painted, **then** Control Drawing is the single active product surface; fit/center
   yields a nonblank, readable, correctly framed, unclipped sheet with no normal-state component boxes,
   toolbar clutter, unintended overlaps, off-anchor routes, fallback geometry, or raw foreign markup.
4. **Given** all previous acceptance criteria are green, **when** mandatory final polish/purge runs,
   **then** source, tests, fixtures, docs, generated outputs, and workspace state are deeply reviewed;
   stale or duplicate paths are removed; and RED/GREEN, AC-to-evidence, and three-layer review are
   recorded truthfully.

**Implements:** FR-44, FR-46, FR-48..FR-50, NFR-13..NFR-14.

## Tasks / Subtasks

- [x] Add Story 5.4 RED contracts before production edits (AC: 1..3)
  - [x] Add compiler integration tests that require the exact 17x8 sheet policy, title regions,
        power/control regions, 22 function-aware occurrences, stable occurrence bounds, labels, and
        Graphic Primitive provenance from the Story 5.3 material result.
  - [x] Add routing tests that require exactly 34 semantic route facts, exact source/target
        port-to-anchor attachment, orthogonal segments, body/label avoidance, deterministic output,
        explicit junction dots for electrically joined branches, and no dot for unrelated crossings.
  - [x] Add LSP transport and frontend paint-contract tests proving the focused Control Drawing uses
        compiled composition/routing facts and cannot render Cabinet fallback boxes, old projection
        edges, raw SVG/XML/QET markup, or renderer-selected material.
  - [x] Run focused tests and record their pre-implementation failures before production edits.
- [x] Compile governed drawing occurrences from material and placement facts (AC: 1)
  - [x] Introduce one cohesive compiler-owned drawing orchestration result that consumes the canonical
        `EngineeringDocument`, project semantic layout facts, and
        `AthenaRepresentationMaterialResolutionResult`; do not add a parallel semantic or
        representation IR.
  - [x] Map each 1-based `(column, row)` placement to stable sheet-space occurrence bounds using a
        policy-owned 17x8 grid, definition intrinsic bounds, authored orientation, and explicit
        margins. Reject missing, duplicate, out-of-range, or conflicting placements.
  - [x] Preserve separate occurrence id, optional function id, and canonical physical component id.
        Repeated KM1/KM2 functions must remain discoverable as one physical subject with multiple
        representation occurrences.
  - [x] Materialize occurrence graphic commands and dynamic labels from the selected canonical
        `RepresentationDefinition`; preserve package, definition, binding-rule, anchor, slot, and
        source provenance. Never reconstruct material through `PresentationPrimitive`, descriptor
        boxes, Kotlin fixture catalogs, or a second selector.
  - [x] Bind visible device tags, model/reference text, and terminal identities from semantic facts to
        declared label slots/anchors. Reusable material must not contain project instance truth.
- [x] Derive the professional sheet and structural composition facts (AC: 1)
  - [x] Extend the existing `drawing-composition` contracts with the minimum generic sheet facts
        needed by this target: frame, 17 numbered column zones, 8 lettered row zones, drawing area,
        title block cells, power region, control region, and restrained line-weight/style roles.
  - [x] Use a target policy for the recorded 1050x720 sheet and 2% aspect-ratio tolerance while keeping
        the compiler contracts generic. Frame, zones, title metadata, rails, and route channels remain
        derived composition facts, never fake project devices.
  - [x] Derive title values for author, title, file, date, and folio from governed project/publication
        facts or explicit compiler policy defaults. The frontend may display values but cannot invent them.
  - [x] Publish one stable drawing bounds authority and prove all occurrence, label, route, junction,
        and title facts remain inside the sheet/drawing regions without clipping or incoherent overlap.
- [x] Route semantic connections through compiled function-aware anchors (AC: 2)
  - [x] Resolve each connection endpoint from semantic port identity to exactly one placed material
        occurrence and exported terminal anchor. Prefer the owning Engineering Function when that
        function references the port; fail on zero or ambiguous anchor ownership.
  - [x] Transform local terminal anchors through occurrence scale/orientation into sheet coordinates,
        preserving side and terminal identity. No route may terminate at node center, projection edge,
        inferred side, or label position.
  - [x] Extend the existing routing engine and fact model only where required for deterministic lane
        choice, obstacle/label clearance, shared branch trunks, junction facts, and crossing facts.
        Keep geometry renderer-neutral and fail closed when required constraints cannot be satisfied.
  - [x] Derive connection role/signal from compiled semantic ports and domain facts, not identifier
        prefixes or renderer styles. Keep all 34 authored connections traceable through route id,
        semantic connection id, endpoint ports, occurrence anchors, segments, and quality state.
  - [x] Derive junction dots only where semantic connectivity joins conductors. Unrelated geometric
        intersections must publish crossing facts and render without a dot.
- [x] Replace the active presentation path and keep the workbench paint-only (AC: 1..3)
  - [x] Wire the professional drawing result into compiler/LSP presentation publication for the
        existing `schematic` projection. The LSP maps typed payloads only; it does not plan layout,
        select material, route conductors, parse SVG, or infer engineering meaning.
  - [x] Remove M34 professional output from the current
        `M34CabinetPresentationFactDeriver -> M33 fallback -> M32 fallback` and
        `M34CabinetDrawingCompositionDeriver -> M33 fallback` chains. No legacy fallback may run for
        the focused Control Drawing.
  - [x] Extend Presentation transport with explicit occurrence graphics, labels, sheet regions,
        route segments, junctions, crossings, anchor proof, and authority proof as needed. Raw markup
        and source filesystem paths must remain structurally impossible in product payloads.
  - [x] Make Theia paint the received facts in deterministic layer order: sheet, zones/title,
        conductors/crossings/junctions, symbols, terminal/device labels, then transient interaction.
        Normal-state hitboxes remain transparent and do not alter bounds.
  - [x] Keep one `Control Drawing` surface with stable fit/center behavior at 1920x1080 and 1280x900.
        Do not restore Cabinet/Documentation/Schematic selector competition or debug toolbar clutter.
- [x] Perform mandatory final polish/purge and evidence review (AC: 4)
  - [x] Review every changed source file, test, sample asset, generated output, documentation entry,
        and workspace artifact; remove stale/duplicate Story 5.4 paths without reverting unrelated work.
  - [x] Delete or isolate obsolete M34 Cabinet-only composition/fact paths and any generated
        `.athena/snapshots`; the product is pre-release, so no compatibility layer is required for
        superseded M33/M34 XML/Cabinet architecture.
  - [x] Search the active product path for fake structure devices, list-index/pixel layout, name-prefix
        inference, old projection-edge routing, center-to-center edges, fallback boxes, raw markup,
        QET/XML runtime reads, renderer file reads, and duplicate selection/occurrence authority.
  - [x] Run focused and full affected tests with Gradle strictly sequential, frontend tests and IDE
        build, product sample verifier, UTF-8 encoding audit, and `git diff --check`. Story 5.5 owns
        final real Electron screenshots, but Story 5.4 must leave the product path E2E-ready.
  - [x] Record RED/GREEN commands, exact AC-to-evidence, three-layer review, completion notes, and
        complete File List before moving the story to `review`.

## Dev Notes

### Required Compiler Flow

```text
project .athena source
  -> canonical EngineeringDocument + project semantic snapshot
  -> explicit schematic placement facts
  -> AthenaRepresentationMaterialResolver
  -> selected RepresentationDefinition + terminal/label bindings
  -> governed drawing occurrence compiler
  -> sheet composition + terminal-anchor route facts
  -> PresentationDocument / typed LSP payload
  -> paint-only Theia Control Drawing
```

- `AthenaRepresentationMaterialResolver` is the Story 5.3 material authority. Consume its
  `AthenaResolvedRepresentationMaterial` values directly. Do not stage packages again and do not
  reconstruct a lossy descriptor/`PresentationPrimitive` form.
- `ProjectSemanticSchematicLayoutFactDeriver` already preserves function id, grid position, and
  orientation. Integrate that result into repository drawing compilation; do not derive placement
  from old `ProjectionNode.bounds`, component list order, identifier spelling, or frontend state.
- A dedicated orchestration result may aggregate existing IRs, diagnostics, and proof, but must not
  become a fourth representation IR. Canonical drawing geometry remains `GraphicPrimitive` plus
  existing composition/routing/presentation facts.
- Use generic drawing compiler/model names. M34-specific target policy and tests may name M34; the
  underlying contracts must remain usable by future P&ID, hydraulic, mechanical, or other drawing
  packages without electrical symbol names in the core.

### Frozen Sheet Policy

- Logical target raster and E2E comparison area: `1050 x 720`; aspect-ratio tolerance: 2%.
- Coordinate grid: 17 columns labeled `1..17`, 8 rows labeled `A..H`.
- Keep visible outer sheet, inset drawing frame, top/left coordinate bands, bottom title block, left
  power region, and right control region. Exact pixel values are compiler policy, not source syntax.
- Stable layer order: frame/grid/title behind conductors; conductors behind symbols; junction dots at
  conductor intersections; labels above symbols/conductors; transparent interaction last.
- Required title cells: author, title, file, date, folio. Do not hardcode a screenshot or use the
  approved image as canvas background.

### Occurrence And Anchor Rules

- Story 5.3 resolves 22 placed subjects: physical-device occurrences plus eight KM1/KM2 function
  occurrences. The compiler must match placement subject ids to material subject ids exactly.
- Occurrence bounds derive from grid cell, intrinsic definition bounds, orientation, and policy
  scale. Scaling must preserve aspect ratio unless the representation contract explicitly permits
  independent axes; labels and anchors use the same transform.
- A semantic port may route only through an exported terminal anchor listed in that material's
  `terminalBindings`. Function-owned ports route through their function occurrence. Ambiguity is a
  compiler diagnostic, never a lexical tie-break.
- Terminal labels come from `EngineeringPort.terminalIdentity`; device/function tags come from the
  canonical physical component and function identity. Element/Symbol source only defines slots.

### Routing Rules

- Route input is the 34 authored `EngineeringConnection` values and transformed terminal anchors,
  not old `ProjectionConnection.start/end` points.
- Reuse `AthenaRouteEngineV0`, `RouteFactSnapshot`, `RouteConstraint`, `TerminalAnchorFact`, lane and
  quality contracts. Add cohesive facts for junction/crossing only when existing models cannot
  express them; do not encode these as fake devices or free-form renderer flags.
- Required constraints: orthogonal-only, grid snap, avoid component body, avoid label bounds,
  preferred terminal exit/entry side, deterministic lane, and explicit crossing policy.
- Same semantic port/net branch may share a trunk and has a junction dot. Two unrelated routes that
  intersect geometrically remain a crossing without electrical connection. Renderer paints facts;
  it never decides connectivity from pixels.
- A route unable to preserve terminal attachment or required clearance must fail with a structured
  diagnostic. No diagonal or center-to-center fallback.

### Product And Legacy Boundaries

- Final M34 product surface is `Control Drawing`, backed by existing `schematic`. Do not create a
  fourth view id and do not expose three unfinished view selectors.
- `PresentationModelDeriver` currently activates old M34/M33/M32 fallback chains and builds routes
  from projection geometry. The focused path must bypass/remove those branches. Compatibility does
  not justify retaining dead pre-release architecture.
- `M34CabinetDrawingCompositionDeriver` stages material again, reads Cabinet-only facts, converts
  canonical Graphic Primitive geometry backward into `PresentationPrimitive`, and guesses structure
  from representation ids. None of that is valid for this story's active path.
- QElectroTech remains offline visual/domain evidence only. Never load `.qet`/`.elmt`, adopt page
  ownership or Master/Slave semantics, or carry QET ids into runtime contracts.
- Frontend may fit, center, zoom, select, hover, and paint. It cannot select symbols, assign anchors,
  route conductors, create junctions, derive title data, or read package/source files.

### Existing Code To Extend

- `kernel/compiler/.../AthenaRepresentationMaterialResolver.kt` and
  `AthenaRepresentationMaterialModels.kt`: canonical material resolution and terminal bindings.
- `kernel/compiler/.../semantic/ProjectSemanticSchematicLayoutFactDeriver.kt` and
  `kernel/layout-model/.../LayoutModel.kt`: explicit grid placement and function occurrence facts.
- `kernel/drawing-composition`: generic sheet, coordinate-zone, title, and structure compiler/models.
- `kernel/routing-model`: route requests, constraints, lanes, quality, terminal anchors, and
  orthogonal solver.
- `kernel/presentation-model/.../PresentationDocument.kt` and
  `PresentationDrawingComposition.kt`: renderer-neutral publication contracts.
- `kernel/compiler/.../PresentationModelDeriver.kt`: legacy active-path switch that must be replaced
  for the focused drawing.
- `ide/lsp/.../AthenaPresentationSessionProtocol.kt` and
  `AthenaDrawingCompositionPayloads.kt`: typed transport mapping only.
- `ide/theia-frontend/.../athena-graph-presentation-model.ts`,
  `athena-graph-workbench-presentation-node.tsx`, and `athena-graph-workbench-widget.tsx`: paint,
  layer order, fit/center, and interaction only.

### Testing And Build Rules

- Follow RED/GREEN task order. Do not edit production composition, routing, transport, or frontend
  code until focused failing tests exist and their failures are recorded.
- Never run Gradle commands concurrently on Windows. Wait for each invocation to finish.
- Use exact counts and identities for the M34 sample, not minimum-count assertions. Prove 22 placed
  occurrences, 34 semantic route facts, exact endpoint ports/anchors, 17x8 zones, and zero fallback.
- Add deterministic shuffle/repeat tests for placement, material order, connection order, and route
  output. File order and map iteration must not affect output.
- Story 5.4 must include compiler, routing, transport, and frontend integration proof. Story 5.5 adds
  clean Electron launch, screenshots, canvas pixel analysis, and target comparison.
- Run `powershell -ExecutionPolicy Bypass -File .\tools\encoding-audit.ps1` after docs/text edits.

### Previous Story Intelligence

- Story 5.1 froze the single Control Drawing product contract and banned selector competition.
- Story 5.2 added project-owned Engineering Functions, function-aware occurrence identity,
  cross-reference evidence, and explicit 1-based grid placement.
- Story 5.3 delivered the exact 16-device, 8-function, 62-port, 34-connection, 22-placement sample and
  22 exact package-local material resolutions. Reuse that proof; do not duplicate selectors or
  package compilation.
- Generated package snapshots are caches. Purge them after tests and never commit them as source.
- The dirty worktree contains accumulated M33/M34/user changes. Never reset or revert unrelated work.

### Git Intelligence

- Baseline is `0311ad6 feat(m32): add engineering package platform`; all M33/M34 work is accumulated
  in the dirty worktree and must be preserved.
- Existing M34 files are mostly untracked relative to the baseline. File List and evidence must name
  exact Story 5.4 ownership without claiming earlier story changes.

### References

- [Epic 5 / Story 5.4](epics.md#story-54-compose-and-route-the-professional-control-drawing)
- [M34 corrective PRD](../../planning-artifacts/prds/prd-Athena-2026-07-24-m34/prd.md#corrective-product-acceptance---professional-control-drawing)
- [M34 corrective architecture](../../planning-artifacts/architecture/architecture-Athena-2026-07-24-m34/ARCHITECTURE-SPINE.md#corrective-architecture-decisions---professional-control-drawing)
- [Approved sprint change](../../planning-artifacts/sprint-change-proposal-2026-07-26-m34-professional-control-drawing.md)
- [Story 5.3](5-3-compile-the-rolling-shutter-iec-package-and-semantic-sample.md)
- [Professional renderer target](m34-professional-renderer-target.md)
- [Approved target image](../../../draft/screenshort/equipement_d'un_volet_roulant.png)

## Dev Agent Record

### Agent Model Used

Codex GPT-5

### Debug Log References

- RED/GREEN: `.\gradlew.bat --no-daemon --console=plain :kernel:package-runtime:test`
- RED/GREEN: `.\gradlew.bat --no-daemon --console=plain :kernel:compiler:test`
- RED/GREEN: `.\gradlew.bat --no-daemon --console=plain :ide:lsp:test`
- GREEN: `.\gradlew.bat --no-daemon --console=plain :ide:lsp:installDist`
- GREEN: `yarn build` in `ide/theia-frontend`
- GREEN: `yarn build` in `ide/theia-product`
- GREEN: `node .\ide\theia-frontend\scripts\athena-m34-professional-control-drawing-paint-contract.test.mjs`
- GREEN: `node .\ide\theia-frontend\scripts\athena-m34-product-smoke-contract.test.mjs`
- GREEN/E2E: `node .\ide\theia-product\scripts\verify-athena-m34-sample-project.js`
- GREEN: `.\gradlew.bat --no-daemon --console=plain test`

### AC-To-Evidence Review

- AC-1: Compiler tests and product proof show one `schematic`-backed `Control Drawing`, 17 column zones,
  8 row zones, 22 graphic occurrences, derived drawing structure facts, and no fake structure devices.
- AC-2: Compiler/product proof shows 34 semantic routes, exact terminal anchors, orthogonal route points,
  `quality=SATISFIED`, and zero non-orthogonal segments or body intersections.
- AC-3: Electron verifier shows one visible product surface (`Control Drawing`), active view `schematic`,
  22 graphic occurrences, 0 legacy representation facts, 36 visible drawing-layer items, no wrapper borders,
  and desktop/narrow screenshots.
- AC-4: Removed obsolete M33 XML/SVG LSP catalog and M33 XML cabinet package set, purged generated
  `.athena/snapshots`, ran broad Gradle/frontend/E2E/encoding/diff gates, and updated this evidence.

### Three-Layer Review

- Architecture: Active product path is Athena source/material resolver -> professional drawing compiler ->
  typed Presentation IR/LSP payload -> paint-only Theia. XML/QET/M33 catalog paths are not runtime authority.
- Product: Workbench exposes only `Control Drawing`, backed by `schematic`; Cabinet/Documentation selector
  competition and old sample verifier are not part of the active M34 proof.
- Verification: Focused compiler, LSP, frontend, product Electron, broad Gradle, encoding, and diff checks
  pass after the purge.

### Completion Notes List

- Replaced normal compiler publication for the M34 professional repository with the professional
  `Control Drawing` presentation; legacy cabinet representation facts no longer leak into that path.
- Removed obsolete M33 XML/SVG active code and smoke contracts instead of preserving compatibility.
- Product E2E proof produced fresh screenshots:
  `_bmad-output/implementation-artifacts/m34/screenshots/m34-control-drawing-product-smoke-desktop.png`
  and `_bmad-output/implementation-artifacts/m34/screenshots/m34-control-drawing-product-smoke-narrow.png`.
- Remaining sprint truth: Story 5.5 is still backlog in `sprint-status.yaml`; Story 5.4 is ready for review.

### File List

- `_bmad-output/implementation-artifacts/m34/5-4-compose-and-route-the-professional-control-drawing.md`
- `_bmad-output/implementation-artifacts/m34/screenshots/m34-control-drawing-product-smoke-desktop.png`
- `_bmad-output/implementation-artifacts/m34/screenshots/m34-control-drawing-product-smoke-narrow.png`
- `ide/lsp/src/main/kotlin/com/engineeringood/athena/ide/lsp/AthenaLanguageServer.kt`
- `ide/lsp/src/main/kotlin/com/engineeringood/athena/ide/lsp/M33IecDrawingSvgCatalogService.kt` (deleted)
- `ide/lsp/src/test/kotlin/com/engineeringood/athena/ide/lsp/AthenaM33CabinetProjectionSmokeTest.kt` (deleted)
- `ide/lsp/src/test/kotlin/com/engineeringood/athena/ide/lsp/M33IecDrawingSvgCatalogServiceTest.kt` (deleted)
- `ide/theia-frontend/scripts/athena-m33-product-smoke-contract.test.mjs` (deleted)
- `ide/theia-frontend/scripts/athena-m33-svg-authority-regression.test.mjs` (deleted)
- `ide/theia-frontend/scripts/athena-m34-product-smoke-contract.test.mjs`
- `ide/theia-product/package.json`
- `ide/theia-product/scripts/verify-athena-m33-sample-project.js` (deleted)
- `kernel/compiler/src/main/kotlin/com/engineeringood/athena/compiler/AthenaCompilerCompilationSupport.kt`
- `kernel/compiler/src/test/kotlin/com/engineeringood/athena/compiler/AthenaM33SampleProjectCompilerTest.kt` (deleted)
- `kernel/compiler/src/test/kotlin/com/engineeringood/athena/compiler/AthenaM34ProfessionalCabinetCompositionTest.kt`
- `kernel/package-runtime/src/main/kotlin/com/engineeringood/athena/packageruntime/M33CabinetPackageSet.kt` (deleted)

### Change Log

- 2026-07-26: Created through the BMAD create-story workflow from approved corrective Epic 5.
- 2026-07-26: Implemented and verified the professional Control Drawing active path; purged obsolete M33
  XML/SVG catalog authority and stale generated snapshots; moved story to review.
