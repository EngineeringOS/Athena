# M33 Cabinet Visual Review Checklist

Purpose: make M33 visual review concrete. This checklist is qualitative evidence for the Cabinet product surface; it is not an IEC, EPLAN, or QElectroTech compliance audit.

## Reference Anchors

- Professional references are reference anchors, not compliance claims.
- Use QElectroTech folio and element examples only to compare drawing density, line discipline, symbol clarity, labels, and frame behavior.
- Do not copy proprietary assets, vendor macros, or `.elmt` geometry into Athena runtime evidence.

## Symbol Quality

- Symbols must read as engineering drawing symbols, not generic UI cards.
- Device body chrome must be minimal and transparent outside authored strokes.
- Terminals must be visually inspectable and tied to semantic port anchors.
- Reference markers must come from representation/package facts, not ad hoc SVG decoration.

## Drawing Density

- Drawing should be dense enough for industrial review without oversized cards, shadows, rounded panels, or decorative empty space.
- Text must remain legible at the captured default zoom.
- Component spacing should support scanning relationships left to right.

## Sheet Frame

- Sheet frame, drawing area, zones, and title block must be visible and governed by drawing composition facts.
- Frame must not be a Workbench container border.
- The drawing background should remain transparent over the Workbench grid.

## Labels

- Labels must be short, close to their subjects, and not duplicated.
- Terminal labels must not overlap route lines or component bodies.
- Verbose route labels should stay deferred unless selected or inspected.

## Routes

- Routes must use terminal anchors, not center points.
- Routes should be orthogonal and avoid crossing component bodies.
- Route channels should read as drawing structure, not as UI connector decoration.

## Toolbar

- Cabinet must be the only visible product surface in M33.
- Documentation, schematic, and wiring views are compatibility backing views only.
- Toolbar controls must not overlap the drawing or force a split editor layout.
- Debug/proof controls must not appear in the product toolbar.

## Screenshot Evidence

- Desktop evidence: `_bmad-output/implementation-artifacts/m33/screenshots/m33-cabinet-product-smoke-desktop.png`.
- Narrow evidence: `_bmad-output/implementation-artifacts/m33/screenshots/m33-cabinet-product-smoke-narrow.png`.
- Both screenshots must be paired with `ATHENA_M33_PROFESSIONAL_DRAWING_PROOF`.

## Proof Mapping

- Package and symbol evidence: `representationProof`.
- Sheet/frame/zones evidence: `sheetSurfaceProof` and `drawingLayerProof`.
- Route anchor evidence: `routeProof`.
- Bounds/viewBox evidence: `visualProof`.
- Screenshot evidence: `screenshotEvidenceProof`.
- Create Device panel bounds evidence: `createEntityPanelProof`.

## Deferred Concerns

- M33 still needs visual polish beyond proof: stronger Cabinet-scale defaults, less empty vertical space, and more professional symbol proportions.
- Those concerns belong to Story 7.2 cleanup/purge unless they block screenshot or structured proof gates.
