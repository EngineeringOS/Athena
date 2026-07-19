# M25 Representation Acceptance Proof

Updated: 2026-07-19

## Purpose

M24 improved connection readability by replacing generic graph edges with governed schematic route
facts. The remaining visual problem was that M24 still read as a generic-box route proof: components
were useful engineering subjects, but their visible bodies, terminals, and labels were still too
generic for a professional schematic presentation.

M25 compares that baseline against governed presentation anatomy. The proof is not that Athena is a
complete symbol library. The proof is that component knowledge and presentation policy now produce
renderer-facing representation facts: symbol anatomy, terminal markers, terminal numbers, label
anchors, occurrence identity, and M24 route attachments.

## M24 Baseline

M24 proves:

- semantic `connect` declarations become deterministic schematic route facts
- routes attach through terminal anchors instead of component centers in the accepted proof
- route labels, route quality, and route endpoints are inspectable

M24 does not prove professional symbol notation. It can still look like a generic-box route surface
because component appearance is not yet governed by a presentation anatomy and policy profile.

## M25 Delta

M25 adds:

- `PresentationAnatomy` as the governed representation contract above electrical symbol anatomy
- `athena-industrial-control-v0` as the first small presentation policy profile
- terminal markers as facts, not renderer-invented dots
- terminal numbers as facts, not loose canvas text
- label anchors for device tags, type labels, terminal labels, and route labels
- zero-fallback acceptance for the M25 proof path

The expected visible change is:

```text
M24:
  semantic component boxes + routed lines

M25:
  semantic component subjects
    -> presentation policy
    -> symbol anatomy
    -> presentation terminals
    -> label anchors
    -> M24 route facts
    -> paint-only Theia rendering
```

## Acceptance Evidence

The M25 sample project is:

- `../../examples/m25/sample-project`

The accepted source path includes:

- `src/01-professional-symbol-sheet.athena`
- `src/02-terminal-labels-and-routes.athena`
- `src/03-six-family-acceptance.athena`

The accepted proof must show:

- PLC/controller, terminal block, power supply, and load/actuator in the mandatory path
- HMI/operator device and protection device in the six-family slice
- terminal markers on visible connection points
- terminal numbers available as presentation terminal facts
- label anchors for device tags and type labels
- route labels still coming from governed route facts
- no generic fallback symbols in the accepted M25 proof
- canonical subject and occurrence identity preserved for symbol, terminal, label, and route
  inspection

## QElectroTech-Inspired Mapping

This documentation-only mapping uses the local reference element:

- `../../reference/qelectrotech-source-mirror/qelectrotech-elements/10_electric/99_miscellaneous_unsorted/Terminale_Spring_Box_3.elmt`

The reference element contains a small terminal block anatomy vocabulary:

| QElectroTech element idea | Athena M25 representation idea |
| --- | --- |
| element width, height, hotspot | `PresentationAnatomy.bounds` and hotspot facts |
| `rect`, `ellipse`, `line` primitives | governed symbol primitive facts |
| three terminal entries | `PresentationTerminal` facts |
| terminal `x`, `y`, orientation | terminal anchor and side policy |
| visible terminal grouping | component-family presentation anatomy |

This is a QElectroTech-inspired anatomy vocabulary example only. M25 has no QElectroTech import,
no IEC completeness, and no EPLAN parity.

## Deferred Boundaries

M25 does not include:

- QElectroTech `.elmt` ingestion
- full IEC symbol-library breadth
- symbol authoring UI
- renderer-owned symbol or terminal meaning
- canvas-local hidden symbol state
- route editing or route-hint syntax expansion
- physical wire, harness, cabinet, cable tray, or 3D routing
- full EPLAN visual parity

M25 keeps the authority direction:

```text
component knowledge
  -> presentation policy
  -> Presentation IR facts
  -> Theia paint-only renderer
```

It does not allow:

```text
symbol drawing
  -> inferred engineering meaning
```
