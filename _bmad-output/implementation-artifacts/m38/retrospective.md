# M38 Retrospective

## What M38 Fixed

M38 corrected the root failure exposed by M37: a connection could carry source and Anchor IDs while the visible line still started or ended at computed route points. M38 made the visible connector depend on exact placed Anchor points.

The milestone now has:

- one intrinsic geometry contract: `RepresentationDefinition`;
- one placed occurrence contract: `GraphicOccurrence`;
- one visible connection contract: `PresentationConnector`;
- one atomic presentation payload: `PresentationDocument`;
- one product rendering path: Theia consuming compiler facts.

## What Passed

Fresh M38 proof passed on `examples/m38/professional-control-drawing`.

Verified:

- compiler sample test;
- LSP M38 smoke test;
- GLSP adapter tests;
- Theia frontend tests;
- LSP install distribution;
- Theia product build;
- M38 Electron smoke with three screenshots;
- source-set hygiene audit;
- encoding audit;
- `git diff --check`.

## What Remains Weak

Placement quality is not professional-grade. Device ordering, alignment, density, and visual balance still need a dedicated composition engine.

Routing quality is not professional-grade. Lines can still look mechanically valid but visually poor, with too many turns or weak lane discipline.

Symbol and vendor resource depth is still limited. SVG support is useful, but Athena still needs stronger curated representation libraries.

## Forbidden Claims

Do not claim M38 makes Athena look like EPLAN.

Do not claim M38 solves automatic layout.

Do not claim M38 solves professional routing.

Do not claim Theia owns drawing intelligence.

Do not claim SVG owns engineering semantics.

## Lessons

Keep normal Athena source human-first, concrete, AI-friendly, and K.I.S.S.

Do not add syntax to cover compiler weakness. Strengthen compiler IR and validation instead.

Do not add another renderer. Theia is the product renderer.

Do not preserve stale architecture for compatibility. Athena is pre-1.0.

M38 should be remembered as the drawing trust foundation: every visible endpoint must be explainable from source truth.

