# M22 Sample Project

This is the openable Athena workspace for the M22 milestone.

Open this folder in the IDE to see the M22 governed layout optimization and round-trip scenarios as
real `.athena` source files:

- `src/01-baseline-sheet.athena` - accepted M21 graph workbench behavior baseline
- `src/02-layout-optimization-acceptance.athena` - power, protection, controller, HMI, terminal, and
  load readability subjects
- `src/03-component-round-trip.athena` - component placement, alignment, and grouping round-trip
  subjects
- `src/04-boundary-scope.athena` - deferred-scope guardrail scenario
- `M22-LAYOUT-ACCEPTANCE.md` - professional layout acceptance checklist for M22 review
- `M22-BASELINE-PROOF.md` - IDE-visible baseline proof for accepted graph workbench behavior
- `M22-LAYOUT-REPLAY-PROOF.md` - deterministic fact-level replay proof before visual acceptance

Story 2.3 adds governed placement and grouping evidence behind the optimized layout scenario:
preferred-zone constraints influence placement, grouped-with constraints emit explicit group facts,
and grouping remains kernel-owned rather than inferred by the renderer.

This workspace is the customer-facing sample project. Supporting `.mjs` fixtures are tests only;
users should understand M22 from the IDE and these real `.athena` sources.
