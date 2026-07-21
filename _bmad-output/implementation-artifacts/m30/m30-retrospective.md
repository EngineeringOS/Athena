# M30 Retrospective

## Outcome

M30 moved Athena from a semantic graph viewer with improved interaction toward a native engineering
representation platform. The important architectural shift is that professional visual output now
has an upstream owner: representation policy, representation definitions, representation
occurrences, binding, and composition intent.

## Pre-M30 Visual Credibility Failure Causes

The pre-M30 visual credibility failure causes were systemic, not cosmetic:

- Weak representation semantics forced the renderer to invent visual behavior from generic device,
  port, and connection facts.
- Generic wrappers and visible normal-state borders made engineering symbols look like UI cards
  instead of dense schematic elements.
- Hard-coded viewBox values caused tiny real content inside oversized SVG canvases.
- Off-screen duplicate occurrence output inflated SVGs and made proof unreliable.
- Repeated text nodes and fallback labels created visual noise.
- Missing native symbol definitions meant the frontend substituted generic rectangles and graph
  spacing for professional electrical linework.
- Sheet controls and view state were too coupled to frontend behavior instead of document and
  projection facts.

## What Worked

- Representation Definition IR and Representation Occurrence IR stayed separate, preventing the
  "one semantic device equals one box" trap.
- Representation policy became the right place to choose family, variant, occurrence role, and
  fallback behavior.
- Binding diagnostics made missing symbols, anchors, slots, and ambiguous bindings explicit.
- Composition intent gave the demo a professional planning layer without creating CAD source truth.
- Transparent normal chrome and derived viewBox guards directly targeted the visual failures the
  user observed during M27/M29 verification.
- Structured proof plus screenshot proof created a better acceptance pattern than visual guessing.

## What Blocked Or Slowed The Work

- The renderer previously had too much responsibility because upstream representation contracts
  were missing.
- Proof scripts had to be tightened repeatedly to distinguish real component bodies from port label
  hitboxes and to validate live product behavior rather than static assumptions.
- The sheet/control state issue showed that frontend controls must be verified after view switching,
  not only on initial render.
- QET `.elmt` inspection made clear that visual asset conversion is a separate future importer
  problem, not a quick syntax extension or runtime dependency.

## Hard Lessons

- Do not patch renderer around missing representation semantics. If a visual behavior needs
  engineering meaning, add the upstream representation or projection fact first.
- Do not hard-code canvas dimensions. Bounds must derive from resolved presentation content plus
  governed margins.
- Do not hide large hitboxes or wrappers behind transparent styling unless tests prove normal-state
  chrome is invisible and interaction chrome is stateful.
- Do not treat external symbol formats as Athena source. QET is useful reference and possible
  offline input, but Athena runtime consumes Athena-owned representation assets.
- Do not accept screenshot-only proof. Human screenshot review is necessary, but structured proof is
  the authority for counts, anchors, routes, fallbacks, and chrome rules.

## Cleanup Summary

The cleanup ledger records retained and deferred artifacts with owner, reason, target milestone, and
verification. The retained bridge pack from pre-M30 remains documented as temporary. The screenshot
proof is intentionally retained as Story 6.3 evidence. No QET runtime importer, QET source syntax,
or parser spike was added during M30.

## Carry Forward

- Keep final purge mandatory in every story.
- Prefer data-driven symbol assets with tests over renderer-coded symbol knowledge.
- Treat future QET conversion as offline candidate generation into Representation Definition IR.
- Continue requiring product smoke proof for customer-demo claims.
