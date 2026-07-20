# M26 Semantic Document Projection Proof Usage

M26 proves that Athena can project one semantic workspace graph into multiple governed schematic
sheet views without turning pages, documents, source files, or Theia canvas state into source truth.

## Open The Sample

Sample workspace:

```text
examples/m26/sample-project
```

Start the Theia IDE with:

```powershell
yarn --cwd ide start:m26
```

Run the product smoke proof with:

```powershell
yarn --cwd ide start:smoke:m26
```

Current M26 product smoke acceptance evidence:

```text
Athena M26 semantic document projection proof passed.
Athena M26 sample project smoke passed.
```

The smoke asserts the real IDE path opens `examples/m26/sample-project`, switches to the
`documentation` projection, sees three sheet-view options, keeps default verbose semantic route
labels hidden from the canvas, and verifies M24/M25 route and representation facts are still
rendered through the Theia workbench.

Open the Graphical View after the workspace loads. The existing toolbar exposes the sheet-view
selector when M26 document projection metadata is available.

Accepted sheet-view titles:

- `Power Distribution`
- `Control And PLC Logic`
- `Field Wiring And Terminal Transition`

## What To Check

Use the sheet-view selector in the Graphical View toolbar to move between the accepted views. The
selector displays compact order, title, and role metadata derived from document projection facts.

Compact continuation and cross-reference markers use the display notation from the projection, such
as a target sheet-view location. Detailed canonical identity remains available through hover,
selection, and the Graphical View information popover.

Marker transport and navigation are covered by the M26 frontend regression tests:

```text
ide/theia-frontend/scripts/athena-m26-reference-marker-transport.test.mjs
ide/theia-frontend/scripts/athena-graph-workbench-model.test.mjs
```

Reference marker navigation follows this path:

```text
Presentation reference marker
  -> target document occurrence identity
  -> target sheet-view identity
  -> governed view switch
  -> canonical semantic selection
```

Theia does not scan canvas geometry, route labels, DOM nodes, or source filenames to decide what a
reference means.

## Projection Policy

M26 uses the built-in policy:

```text
athena-document-projection-v0
```

Occurrence identity is policy-versioned and deterministic:

```text
documentProjectionId + sheetViewId + canonicalSubjectId + occurrenceRole + detailRole
```

The same semantic identity can have occurrences in more than one sheet view. Source file ordering or
renaming must not become part of occurrence identity.

## Source/View Boundary Proof

The M26 sample intentionally uses source files that are not sheet-view titles:

- `src/01-workspace-semantic-source.athena`
- `src/02-field-assets-not-a-sheet.athena`

The first source file contributes power, PLC/control, and HMI subjects that can project into more
than one sheet view. The second source file contributes field terminal and load subjects without
becoming the `Field Wiring And Terminal Transition` view itself.

This is the M26 boundary:

```text
.athena source + compiler/runtime semantic snapshot = engineering truth
Document Projection IR = derived view topology and references
Presentation IR = paint-ready sheet presentation
Theia canvas = renderer and interaction surface
```

## M25 Compared With M26

M25 proved professional single-sheet presentation: component knowledge becomes presentation anatomy,
symbol facts, terminal facts, label facts, and route facts that Theia renders without inventing
meaning.

M26 adds semantic document projection: the same upstream semantic graph can materialize multiple
sheet views, stable document occurrences, compact continuation markers, and cross-reference facts.

M26 does not add PDF export, print layout, revision workflows, terminal reports, wire lists,
auto-pagination, document release packages, standards-complete IEC formatting, or AI document
authoring.
