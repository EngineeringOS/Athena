# M36 Expert Review Reconciliation

## Verdict

Accepted with one terminology refinement. M36 is now explicitly an Engineering Connectivity
Semantics milestone consumed by Cabinet, not a Cabinet drawing-polish milestone.

## Applied Changes

- Replaced the proposed M36 Component contract with **Connectable Entity Contract**. Athena already
  has M14 Component knowledge, so M36 consumes a narrow connectivity view instead of redefining it.
- Added explicit Semantic, Representation, Physical, and Layout Preference constraint ownership.
- Added **Engineering Lowering**, transient **Connection IR**, and transient **Layout Graph**.
- Added **Route Intent** between Connection semantics and derived Route Facts.
- Added complete semantic route proof requirements.
- Split M36 acceptance into mandatory semantic/compiler core and stretch planner optimization.
- Kept ELK behind Planner SPI and made a production ELK dependency optional after core acceptance.

## Preserved Boundaries

- Athena source remains SSOT.
- SVG remains geometry plus non-authoritative geometry references.
- Cabinet remains the first and only visible product proof.
- Renderer remains paint-only.
- No AI auto-layout.
- No XML runtime authority or unreleased compatibility burden.
