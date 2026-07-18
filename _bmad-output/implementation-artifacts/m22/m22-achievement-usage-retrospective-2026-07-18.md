# M22 Achievement, Usage, And Retrospective

Date: 2026-07-18

## Summary

M22 completed the governed layout optimization foundation and selected a layout round-trip syntax
direction. The milestone moved
Athena beyond the M21 layout-intelligence model by adding explicit layout constraints, a deterministic
optimization boundary, an optional local ELK normalization spike, a frontend layout-adjustment
preview/source-edit concept, and IDE coherence guardrails around the visible Theia graph workbench.

The main product proof is no longer an `.mjs` fixture. It is the openable Athena project at
`../../../examples/m22/sample-project`, with real `.athena` files and supporting checklist/proof
documents.

## What M22 Delivered

### Epic 1 - Openable M22 Layout Proof Baseline

- Added `../../../examples/m22/sample-project` as the customer-visible proof project.
- Added real `.athena` scenarios:
  - `src/01-baseline-sheet.athena`
  - `src/02-layout-optimization-acceptance.athena`
  - `src/03-component-round-trip.athena`
  - `src/04-boundary-scope.athena`
- Added `M22-LAYOUT-ACCEPTANCE.md` to define the visual acceptance criteria.
- Added `M22-BASELINE-PROOF.md` to preserve the accepted M20/M21 graph workbench baseline.
- Added `start:m22` and `start:smoke:m22` launch paths.

### Epic 2 - Governed Professional Layout Optimization

- Added the Layout Constraint Model in `../../../kernel/layout-model`.
- Added deterministic optimization behavior in `../../../kernel/layout-engine`.
- Kept optimization output normalized as Athena layout facts.
- Improved governed placement and grouping for power, protection, controller, HMI, terminals, and
  load subjects.
- Added basic schematic route and label readability facts without claiming physical routing,
  standards-specific labels, or EPLAN parity.
- Added deterministic replay proof in the M22 sample project.

### Epic 3 - Optional Local ELK-Assisted Optimization Spike

- Recorded the local-only ELK spike envelope in `M22-ELK-SPIKE-ENVELOPE.md`.
- Added the adapter normalization path without making ELK the architecture.
- Added comparison evidence in `M22-ELK-COMPARISON.md`.
- Preserved the rule that Athena facts, not external adapter output, remain the renderer contract.

### Epic 4 - Reviewable Component Layout Round-Trip

- Selected the minimal M22 layout-hint syntax shape in `M22-LAYOUT-HINT-SYNTAX.md`.
- Added graph workbench adjustment intent capture for placement, alignment, and grouping.
- Added mutation preview/source-edit construction for layout snippets.
- Did not complete real parser/compiler/LSP admission for `.athena` layout blocks; real source
  round-trip is deferred to M23.
- Kept the canvas from becoming hidden persistence state.

### Epic 5 - IDE Coherence And Scope Guardrails

- Fixed the active-source projection issue: Graphical View follows the latest opened `.athena` source
  instead of falling back to the baseline seed after graph focus.
- Preserved source, outline, Problems, and sheet identity coherence through canonical ids.
- Preserved accepted graph workbench behavior:
  - grid remains the coordinate surface
  - floating controls remain transparent
  - `Cabinet Main` stays in the top information popover
  - info popover closes on whitespace click
  - outline navigation stays in the same editor tab
- Added boundary checks so M22 does not drift into repository/import, broad IEC library, cabinet
  authoring, physical routing, AI layout, final solver-stack selection, or full EPLAN parity.

## Usage

Start the IDE with the M22 project:

```powershell
Set-Location ide
yarn start:m22
```

The command opens:

```text
../../../examples/m22/sample-project
```

Primary files to inspect:

- `src/01-baseline-sheet.athena` - accepted baseline graph workbench behavior.
- `src/02-layout-optimization-acceptance.athena` - governed layout optimization acceptance case.
- `src/03-component-round-trip.athena` - component placement/alignment/grouping identities for the
  round-trip preview case. It does not contain the selected layout block because the M22 language
  layer does not yet accept that syntax.
- `src/04-boundary-scope.athena` - explicit deferred-scope guardrails.
- `M22-LAYOUT-ACCEPTANCE.md` - professional readability checklist.
- `M22-LAYOUT-REPLAY-PROOF.md` - deterministic layout-fact replay proof.

Expected IDE checks:

- Open `src/02-layout-optimization-acceptance.athena`, then open Graphical View. The rendered graph
  must correspond to that active source, not `src/01-baseline-sheet.athena`.
- Use the top information icon to inspect `Cabinet Main`; it must not appear as canvas chrome or in
  the bottom dock.
- Click whitespace; the information popover must close.
- Use the floating zoom controls; the controls must remain transparent overlays on the grid surface.
- Use outline navigation; it must reveal in the same `.athena` editor tab.
- Review `src/03-component-round-trip.athena` and the layout mutation preview path; M22 proves the
  preview/source-edit direction, but real accepted `.athena` layout-block persistence is not complete.

Supporting usage document:

- `../../../docs/usages/m22-proof-usage.md`

## Verification Record

Fresh verification after the M22 commit and push passed:

```powershell
node --test ide/theia-frontend/scripts/athena-m22-*.test.mjs
```

Result: 16 tests passed, 0 failed.

```powershell
Set-Location ide
yarn workspace @engineeringood/athena-theia-frontend build
Set-Location ..
```

Result: Theia frontend TypeScript build passed.

```powershell
.\gradlew.bat --no-daemon --console=plain :kernel:layout-model:test
.\gradlew.bat --no-daemon --console=plain :kernel:layout-engine:test
.\gradlew.bat --no-daemon --console=plain :kernel:routing-model:test
.\gradlew.bat --no-daemon --console=plain :kernel:compiler:test --tests com.engineeringood.athena.compiler.AthenaM22SampleProjectCompilerTest
.\gradlew.bat --no-daemon --console=plain :ide:lsp:test --tests "com.engineeringood.athena.ide.lsp.AthenaProjectionRequestTest.projection session request follows latest opened source file in governed repository"
```

Result: all targeted Gradle checks passed sequentially.

```powershell
Set-Location ide
yarn workspace @engineeringood/athena-theia-product start:smoke:m22
Set-Location ..
```

Result: M22 sample project smoke passed and reported graph workbench DOM proof for root, stage,
viewport, sheet, canvas, transparent floating controls, transparent bottom/zoom docks, transparent
sheet, sheet frame, grid surface, info popover open, and whitespace-close behavior.

```powershell
powershell -ExecutionPolicy Bypass -File .\tools\encoding-audit.ps1
```

Result: encoding audit passed.

## What Went Well

- The openable sample project became the proof surface. This corrected the earlier pattern where
  users had to infer product value from `.mjs` fixtures.
- The layout model stayed semantic-first. Constraints and facts remain upstream of rendering.
- The optional ELK work stayed behind an Athena normalization boundary, which protects future solver
  choices.
- The active-source projection bug was fixed at the IDE/LSP bridge boundary instead of papered over
  in sample files.
- The graph workbench behavior from M20/M21 is now guarded by explicit M22 regression tests.

## Problems And Corrections

- The first proof shape was too test-fixture-oriented. M22 corrected this by making
  `examples/m22/sample-project` the primary artifact and keeping `.mjs` files as supporting tests.
- Active-source projection could show the wrong `.athena` file after Graphical View focus. The root
  cause was frontend source selection state, not an ANTLR or Tree-sitter grammar issue. The fix was
  to preserve the last active Athena editor in the bridge used by projection requests.
- Layout quality is still not EPLAN-level. M22 intentionally proves the governed optimization and
  round-trip foundation, not final professional layout parity.
- Round-trip scope had to stay narrower than originally recorded. M22 selected component placement,
  alignment, and grouping syntax intent, but did not admit that block into the real `.athena`
  parser/compiler/LSP.
- UI regressions around `Cabinet Main`, grid visibility, and transparent controls were costly. The
  lesson is to treat accepted visual behavior as product contract, not incidental CSS.

## Lessons For Future Milestones

- Every customer-facing milestone needs a real openable project first. Tests support the proof; they
  do not replace it.
- Before changing graph workbench UI, preserve the accepted M20/M21 canvas contract in executable
  tests.
- When Graphical View shows stale content, debug active source and projection identity first. Do not
  assume the language parser is the root cause without evidence.
- External layout engines should be adapters behind Athena facts, not the architecture.
- Persist user layout changes as governed intent. Do not save hidden canvas state.
- Keep `.athena` examples inside the syntax that the current IDE/LSP actually accepts.
- Do not claim source round-trip support until the generated source is accepted by the parser,
  compiler, LSP, and sample project.

## Deferred Scope

M22 deliberately did not solve:

- full EPLAN parity
- full IEC/QElectroTech symbol library ingestion
- public repository/import ecosystem
- cabinet authoring
- physical wire, harness, cable tray, cabinet, or 3D routing
- AI layout optimization
- final solver-stack selection
- advanced electrical routing intelligence
- standards-specific label generation
- real `.athena` parser/compiler/LSP support for the selected layout block

## Next Recommendations

- Treat M23 as the next product-depth decision point: either improve visible engineering layout
  fidelity further or start a focused component/library milestone only if the layout workflow is
  stable enough for customer review.
- Keep a hard gate: no story is complete unless the M22 sample project remains openable in Theia and
  the active `.athena` source projects correctly.
- If ELK continues, graduate it only through the adapter boundary and normalized fact comparison.
- If layout round-trip expands, first admit the selected layout block into the real language and keep
  source edits reviewable.
