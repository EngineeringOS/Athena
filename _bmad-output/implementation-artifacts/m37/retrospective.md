# M37 Retrospective

## Outcome

M37 moved Athena from structural connectivity toward professional engineering communication:

- Engineering Connectivity Contract and grouped Interfaces/Ports are typed.
- Connection Intent is authored, deterministic, and preserved into RouteFacts.
- External Evidence stays evidence, not runtime authority.
- Projection Policy keeps one Athena source usable across derived projections.
- Route lanes, route quality, endpoint attachment, junction/crossing semantics, and label bounds are compiled facts.
- The dedicated M37 professional Control Drawing has screenshot-backed and structural E2E proof.

## What Worked

- Keeping one visible product-quality surface prevented multi-view spread.
- Fixing renderer defects upstream exposed missing semantic and routing facts instead of hiding them in UI code.
- BMad story order kept M37 scoped after prior milestone confusion.
- Source-set hygiene audit prevented milestone proof/demo classes from returning to `src/main`.

## Hard Lessons

- Professional drawing quality is semantic and compiler work, not frontend polish.
- Stale tests can preserve old architecture. Story 6.2 found a routing profile test still expecting old two-class line grammar.
- Story 6.3 found two more stale regression expectations: M34 default presentation still expected display label `Schematic` even though product surface is now `Control Drawing`, and frontend syntax-highlight coverage still expected only `in|out` despite current typed port directions including `bidirectional|passive`.
- SVG must remain geometry-only. Story 6.1 tightened `data-athena-ref` to `anchor:<id>` hints.
- Route hard rules need named, traceable compiler ownership. Story 6.2 removed silent fixed injection from the drawing compiler.

## Residual Risks

- Visual quality still trails professional IEC/QET references. Future work needs richer symbol libraries, line routing grammar, and layout scoring.
- Full Engineering Component System remains deferred.
- Remote package resources and registry support remain deferred.
- ELK remains deferred behind future Planner SPI work.

## M37 Completion Gate

Story 6.3 records final sequential verification: root Gradle tests, Tree-sitter tests, Theia frontend tests, LSP installDist, IDE build, M37 Electron smoke screenshots, source-set hygiene, encoding audit, production authority audit, and `git diff --check`. No story should be marked beyond `review` by dev workflow unless the review process explicitly advances it to `done`.
