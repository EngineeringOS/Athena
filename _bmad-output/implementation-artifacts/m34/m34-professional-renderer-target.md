# M34 Professional Renderer Target

## User Target

M34 must record the professional drawing target shown in:

`draft/screenshort/equipement_d'un_volet_roulant.png`

The final renderer proof must use a dedicated example project and E2E test evidence. The rendered
graph should visually match the QElectroTech-style reference level:

- sheet border with numbered columns and lettered rows;
- title block and folio metadata;
- dense IEC-style electrical drawing, not card/box UI;
- supply rails, terminals, protective device, transformer, coils, contacts, lamps, motor/load, and
  routed conductors;
- professional labels, terminal numbers, cross-reference marks, and restrained line weights;
- no visible normal-state hitboxes, component chrome, fallback boxes, or toy graph layout.

## Source Material Boundary

Reference material may come from:

- `reference/qelectrotech-source-mirror/qelectrotech-elements`
- hand-built Athena native symbols/elements
- governed annotated SVG resources compiled through Athena

QElectroTech files are visual/domain reference or offline import input only. They are not runtime
authority and must not become a product dependency.

## SSOT Rule

Athena remains the only metadata authority:

- actual project devices, ports, connections, and layout stay in project `.athena`;
- reusable symbol/element contracts stay in Athena representation source;
- complex geometry may live in governed SVG with `data-athena-*` only on contract-bearing nodes;
- renderer receives typed compiled facts only, never raw QET/XML/SVG authority.

## Required Proof

Before M34 is called complete, the dedicated example must have:

- fresh Electron E2E screenshots;
- structured proof for selected element, binding policy, anchor/label provenance, and zero fallback;
- pixel/nonblank checks;
- visual checklist against the reference image;
- cleanup/purge evidence and AC-to-evidence mapping in the relevant story.

## Normative Epic 5 Contract

This target is final M34 acceptance, not follow-up scope. Corrective Epic 5 must deliver one focused
`Control Drawing` product surface backed by Athena's existing `schematic` projection.

The executable contract records:

- 17 numbered columns and 8 lettered rows;
- target raster: `1050 x 720` pixels;
- aspect-ratio tolerance: `2%` for the Story 5.5 rendered sheet viewport;
- separate power and control circuit regions;
- source, breaker, fuse-disconnector, transformer, reversing contactors, coils, NO/NC contacts,
  push buttons, limit switches, lamps, terminals, motor, earth, and orthogonal conductors;
- visible device tags and terminal identities including `A1`, `A2`, `13`, `14`, `21`, `22`, `53`,
  and `54` where semantically applicable;
- author, title, file, date, and folio title-block regions;
- no fake drawing-structure devices, fallback boxes, raw foreign markup, or screenshot background.

Story 4.3 remains open until Epic 5 produces fresh real Electron evidence that satisfies this
contract. Existing Cabinet screenshots are historical failure evidence only.

The dedicated product project is `examples/m34/professional-control-drawing`. Its project source
contains engineering devices, ports, connections, and authored layout intent only. Sheet frame,
zones, rails, route channels, grid labels, and title-block labels are compiler-owned composition
facts and must never be authored as fake devices.
