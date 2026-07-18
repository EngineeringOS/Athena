# M23 Achievement, Usage, And Retrospective

Date: 2026-07-18

## Summary

M23 completed governed layout hint language admission. It corrected the M22 overclaim by moving the
selected layout block shape from preview-only text into real `.athena` syntax accepted by ANTLR4,
Tree-sitter, authored AST, compiler lowering, LSP diagnostics, Graph Workbench source edits, and an
openable Athena project.

The main product proof is the openable Athena project at `../../../examples/m23/sample-project`,
especially `src/01-layout-hints.athena`.

M23 is language admission, not new layout depth. It does not claim full EPLAN parity, advanced
electrical routing, AI layout, public repository/import ecosystem work, full IEC/QElectroTech or
broad IEC library ingestion, hidden canvas state, canvas-local state persistence, or a raw
pixel-coordinate layout language.

## What M23 Delivered

### Epic 1 - Parser Parity And Source Fixtures

- Added system-scoped `layout schematic-sheet { ... }` support to the ANTLR4 grammar.
- Added Tree-sitter support for the same layout block shape so IDE syntax UX no longer disagrees
  with compiler/LSP syntax.
- Added parser parity fixtures for valid layout blocks, malformed `place`, invalid axis, missing
  target, and rejected file-global layout.
- Preserved existing package/import/system/device/port/connect source compatibility.

### Epic 2 - Authored AST And Layout Intent Admission

- Added authored `LayoutDeclaration` and layout statement AST nodes.
- Added layout intent mapping between syntax and constraints.
- Added priority model compatibility; authored M23 hints default to preference priority.
- Added stable layout source serialization for placement, alignment, and grouping syntax.

### Epic 3 - Compiler Constraint Lowering And Deterministic Facts

- Bound layout hint subjects and targets through compiler-owned project semantics.
- Lowered admitted layout intent into governed layout constraints.
- Added diagnostics for unknown layout references, duplicate hints, and contradictory hints.
- Fed admitted constraints into deterministic schematic layout facts where the current engine
  supports them.

### Epic 4 - LSP And Graph Workbench Round-Trip Closure

- Published valid and invalid layout block diagnostics through the Athena LSP path.
- Kept Tree-sitter as syntax UX only; diagnostics and meaning stay with ANTLR/compiler/LSP.
- Updated Graph Workbench layout source edits to serialize typed layout intent instead of
  hand-owned frontend syntax strings.
- Fixed source insertion so new layout blocks are placed inside the system scope.
- Preserved active-source projection and accepted M20-M22 canvas behavior.

### Epic 5 - Sample Proof, Usage, And Boundary Guardrails

- Added `../../../examples/m23/sample-project` as the visible proof project.
- Added `src/01-layout-hints.athena` with real admitted layout syntax:

```athena
layout schematic-sheet {
  place OperatorHMI1 near ControllerPLC1
  place TerminalBlockXT1 below ControllerPLC1
  align OperatorHMI1 aligned-with ControllerPLC1 axis vertical
  group OperatorHMI1 grouped-with ControllerPLC1
}
```

- Added `start:m23` and `start:smoke:m23` IDE launch paths.
- Added compiler, LSP, frontend, and Electron Graph Workbench smoke coverage for the sample.
- Published usage documentation at `../../../docs/usages/m23-proof-usage.md`.
- Added boundary regression checks to prevent M23 from drifting into EPLAN parity, advanced routing,
  AI layout, public repository/import ecosystem, library ingestion, or hidden canvas state claims.

## Usage

Rebuild the installed LSP host after language syntax work:

```powershell
.\gradlew.bat --no-daemon --console=plain :ide:lsp:installDist
```

Start the IDE with the M23 project:

```powershell
Set-Location ide
yarn start:m23
```

The command opens:

```text
../../../examples/m23/sample-project
```

Primary source:

- `src/01-layout-hints.athena` - system-scoped layout hint language admission proof.

Expected IDE checks:

- The editor accepts `layout schematic-sheet { ... }` without false syntax errors.
- Problems has no false M23 layout diagnostics for the sample.
- Graphical View projects the active M23 source.
- Outline navigation, source identity, Problems, and Graphical View remain coherent through the
  same compiler/LSP semantic path.
- The grid remains the canvas coordinate surface.
- The top information icon opens the sheet information popover.
- Whitespace click closes the information popover.
- Floating zoom controls remain transparent overlays.

Supporting usage document:

- `../../../docs/usages/m23-proof-usage.md`

## Verification Record

Final checks run sequentially during closeout:

```powershell
.\gradlew.bat --no-daemon --console=plain :kernel:language:test
.\gradlew.bat --no-daemon --console=plain :kernel:compiler:test
.\gradlew.bat --no-daemon --console=plain :kernel:layout-model:test
.\gradlew.bat --no-daemon --console=plain :kernel:layout-engine:test
.\gradlew.bat --no-daemon --console=plain :ide:lsp:test
```

Result: all passed.

```powershell
yarn --cwd ide/tree-sitter-athena test
yarn --cwd ide/tree-sitter-athena build
yarn --cwd ide/theia-frontend build
```

Result: all passed.

```powershell
node --test ide/theia-frontend/scripts/athena-m23-sample-project.test.mjs
node --test ide/theia-frontend/scripts/athena-m23-sample-project.test.mjs ide/theia-frontend/scripts/athena-m23-layout-source-edit.test.mjs ide/theia-frontend/scripts/athena-m23-ide-behavior-preservation.test.mjs ide/theia-frontend/scripts/athena-m23-boundary.test.mjs ide/theia-frontend/scripts/athena-m22-layout-mutation-preview.test.mjs ide/theia-frontend/scripts/athena-m22-layout-source-edit.test.mjs ide/theia-frontend/scripts/athena-m22-active-source-projection.test.mjs ide/theia-frontend/scripts/athena-m22-canvas-behavior.test.mjs
```

Result: targeted M23 sample and M22/M23 frontend regressions passed.

```powershell
.\gradlew.bat --no-daemon --console=plain :ide:lsp:installDist
node ide/theia-product/scripts/verify-athena-m23-sample-project.js
```

Result: after rebuilding the installed LSP host, the Electron smoke passed and emitted
`ATHENA_GRAPH_WORKBENCH_PROOF={...}` with root, stage, viewport, sheet, canvas, transparent overlays,
sheet frame, grid surface, info popover, and whitespace-close proof values all `true`.

```powershell
node --test ide/theia-frontend/scripts/athena-m23-boundary.test.mjs
powershell -ExecutionPolicy Bypass -File .\tools\encoding-audit.ps1
```

Result: boundary regression and encoding audit passed.

## Problems And Corrections

- The critical IDE smoke failure was not Tree-sitter and not the source ANTLR grammar. The root
  cause was a stale installed LSP host under `ide/lsp/build/install/athena-lsp-host`. Source tests
  accepted `layout`, but the product LSP process still ran an older parser and returned
  `extraneous input 'layout'`.
- The correction was to run `:ide:lsp:installDist` before product smoke and manual IDE proof after
  syntax work.
- The Electron workspace opener now reports Graph Workbench empty-state text when projection fails,
  making future failures actionable instead of a generic viewport timeout.
- M22 wording had to remain explicit: M22 selected and previewed the layout block shape, but real
  parser/compiler/LSP admission was deferred to M23.

## Lessons For Future Milestones

- Every new `.athena` syntax must update both ANTLR4 and Tree-sitter before any IDE-visible claim.
- Product smoke that launches Theia uses the installed LSP host, so source tests alone do not prove
  the IDE path. Rebuild the installed host before smoke.
- When Graphical View says Projection unavailable, capture the rendered projection error first. Do
  not guess Tree-sitter, ANTLR, or renderer until the LSP/projection evidence is visible.
- The openable sample project is the product proof. `.mjs` files are support tests only.
- Keep Graph Workbench source edits typed and serialized from layout intent. Do not let frontend code
  become the syntax authority.
- Do not claim layout round-trip until close/reopen, parser, compiler, LSP, and Graphical View all
  accept the same source.

## Deferred Scope

M23 deliberately did not solve:

- full EPLAN parity
- advanced electrical routing
- physical wire, harness, cable tray, cabinet, or 3D routing
- AI layout
- public repository/import ecosystem work
- full IEC/QElectroTech or broad IEC library ingestion
- ELK or final solver-stack selection
- standards-specific label generation
- raw pixel-coordinate layout authoring
- hidden canvas state or canvas-local persistence

## Next Recommendations

- Treat M24 as the next product-depth decision point only after M23 syntax remains stable in IDE
  smoke.
- If layout quality work resumes, keep it above the renderer through governed intent, constraints,
  and facts.
- If ELK returns, keep it as an adapter behind Athena layout contracts.
- If users adjust layout from the canvas, persist reviewed source intent, not canvas coordinates.
