---
baseline_commit: adb0ae5d1956b13d8aa3df8774e102dfc09ef380
---

# Story 3.1: Build The Relationship-Forward First Renderer Proof Over Canonical Object Identities

Status: ready-for-dev

## Story

As an engineer,
I want Athena to render a relationship-forward engineering projection over canonical objects,
so that the first graphical proof demonstrates object-graph-first projection in a professional split workbench instead of a demo-style panel.

## FR Traceability

- FR-3: surface graphical views in the existing Athena workbench
- FR-4: support graphical navigation and projection-oriented inspection
- FR-7: prepare for later interactive graphical work without locking final editing scope
- FR-8: validate the first graphical technology path against current Athena constraints
- NFR-1: graphical surfaces remain downstream of canonical semantic, repository, package, and SCM meaning
- NFR-4: M7 extends the current Athena shell rather than replacing it
- NFR-7: renderer assets remain downstream of the engineering object graph

## Acceptance Criteria

1. Given projection-model contracts and runtime-owned projection sessions exist, when Athena renders the first supported graphical proof, then the result centers on engineering objects and relationships over stable canonical identities, and the proof remains useful without notation-specific symbol or asset packs.
2. Given the first renderer is reviewed against the M7 milestone intent, when its behavior is inspected, then it proves renderer-target delivery over canonical objects rather than drawing-file ownership, and it keeps later notation-specific projection possible without making notation the semantic center.
3. Given engineers are working in an authored document such as `factory-line.athena`, when they open the first serious renderer proof inside Athena, then the graph surface is presented as the primary work area in a docked split layout with source kept visible as secondary context, and users can keep the layout vertical or horizontal through the existing workbench docking model.
4. Given Athena is proving a real diagram-editor surface instead of a report-style preview, when engineers navigate the renderer, then the graph behaves like an infinite canvas with pan/zoom-oriented navigation and a graph-first viewport, and the implementation avoids presenting the renderer as a small bounded SVG card.
5. Given the renderer proof is reviewed against professional engineering-workbench expectations, when the surface composition is inspected, then the graph view favors dense, useful engineering information over broad explanatory text blocks, and supporting metadata lives in compact panels, toolbars, legends, or properties surfaces instead of dominating the canvas.
6. Given M7 is still an inspect-first milestone, when the renderer proof is refined toward an ECAD-style work posture, then it feels closer to a serious EPLAN-like editing workspace in density and focus without becoming final UX-skin work, unrestricted editing, or a literal clone of another product.

## Tasks / Subtasks

- [ ] Refine the graphical workbench composition so the graph becomes the primary surface. (AC: 3, 4, 5, 6)
  - [ ] Rework `AthenaGraphWorkbenchWidget` so the relationship graph dominates the visible work area.
  - [ ] Move long descriptive sections out of the primary canvas path and keep supporting context compact.
  - [ ] Preserve intelligible loading, error, and unavailable states without reverting to a text-heavy dashboard layout.
- [ ] Introduce an infinite-canvas style navigation posture for the renderer surface. (AC: 4, 6)
  - [ ] Prefer a pannable and zoomable viewport model over a fixed bounded preview surface.
  - [ ] Keep navigation controls professional and compact, such as fit-to-view, zoom state, or equivalent editor-grade affordances if they fit the current implementation seam.
  - [ ] Keep the surface inspect-first; navigation richness must not imply local semantic authority or freeform editing.
- [ ] Keep graph and source visible together through the current Athena workbench docking model. (AC: 3)
  - [ ] Ensure the active `.athena` editor and the graphical view can be revealed together in a stable split arrangement.
  - [ ] Default to a graph-first work posture while still allowing the user to redock vertically or horizontally using existing workbench mechanics.
  - [ ] Do not replace the current shell or invent a second layout system outside Theia workbench composition.
- [ ] Raise the renderer proof to a professional engineering-workbench density bar. (AC: 1, 2, 5, 6)
  - [ ] Emphasize relationship structure, connectivity, and canonical object identities over decorative panel chrome.
  - [ ] Use compact legends, status strips, or inspector surfaces for supporting information rather than large static prose blocks.
  - [ ] Keep the current inspect-first boundary explicit: no freeform editing, no local semantic authority, and no renderer-owned persistence.
- [ ] Keep the renderer proof downstream of runtime and Athena-owned transport. (AC: 1, 2, 4, 6)
  - [ ] Continue consuming projection state through runtime-owned sessions and `ide/lsp`.
  - [ ] Keep any framework- or renderer-specific shape vocabulary behind the current adapter boundary.
  - [ ] Do not let layout refinement turn into a second frontend projection authority.
- [ ] Cover the renderer refinement with focused proof and docs. (AC: 1, 2, 3, 4, 5, 6)
  - [ ] Add or update focused frontend tests for the refined graph-first workbench composition where the logic is pure enough to verify.
  - [ ] Keep the current frontend build, smoke proof, and relevant JVM/LSP regression green.
  - [ ] Update the relevant IDE and usage docs to state that the renderer proof now targets a graph-first split engineering workspace.

## Dev Notes

### Story Intent

- Story `3.1` is the first renderer-quality story for M7, not just another transport or synchronization story.
- Story `2.2` proved that Athena can host a graphical surface inside the product shell.
- Story `3.1` now raises the bar from "hosted graph panel" to "professional graph-first engineering workbench proof."
- The goal is a serious work posture: graph primary, source still visible, dense information layout, infinite-canvas navigation, and relationship-forward rendering.

### Architecture Guardrails

- Reuse the completed M7 projection boundary and workbench seams rather than opening a second rendering path.
- Keep runtime and `ide/lsp` as the sole semantic and projection authorities.
- Keep graph-framework translation in `integrations/graph-*` only.
- Treat EPLAN-like density and focus as a workbench-quality reference, not as permission to clone another tool or to bypass Athena architecture.
- Use GLSP-class editor behavior as the benchmark for surface posture: pannable, zoomable, professional, and still downstream of server-owned diagram meaning.

### Technical Requirements

- Reuse and refine:
  - `ide/theia-frontend/src/browser/athena-graph-workbench-widget.tsx`
  - `ide/theia-frontend/src/browser/athena-workbench-extensions.ts`
  - `ide/theia-frontend/src/browser/athena-product-contribution.ts`
  - `ide/theia-frontend/src/browser/style/index.css`
  - `ide/theia-frontend/src/browser/athena-graph-adapter-service.ts`
- Keep all new or updated core TypeScript classes clearly documented.
- Prefer compact structural UI over explanatory prose in the primary graph surface.
- Prefer viewport and scene-management patterns that can evolve toward GLSP-style editor behavior instead of deepening the current static SVG-preview approach.

### Testing Requirements

- Minimum verification:
  - `yarn workspace @engineeringood/athena-theia-frontend test`
  - `yarn --cwd ide build`
  - `yarn --cwd ide start:smoke`
- Recommended regression:
  - `cmd /c "call java25 && .\\gradlew.bat --no-daemon --console=plain :ide:lsp:test"`
- Keep Gradle and Node verification sequential on Windows. Do not overlap build, test, or run commands in parallel shells.

### References

- [Source: _bmad-output/planning-artifacts/epics-M7-2026-07-09.md]
- [Source: _bmad-output/planning-artifacts/prds/prd-Athena-2026-07-09-m7/prd.md]
- [Source: _bmad-output/planning-artifacts/prds/prd-Athena-2026-07-09-m7/addendum.md]
- [Source: _bmad-output/implementation-artifacts/m7/2-2-surface-a-graphical-athena-view-inside-the-existing-workbench.md]
- [Source: _bmad-output/implementation-artifacts/m7/2-3-synchronize-graphical-selection-with-source-semantic-inspection-and-semantic-scm-context.md]
- [Source: _bmad-output/implementation-artifacts/m7/2-4-keep-graphical-interaction-inspect-first-and-transient-by-default.md]
- [Source: draft/open/2026-07-09-Eplan-cross-compare-discuss.md]
