# M26 Retrospective And Boundary Checks

## Verdict

M26 proves that Athena can project one governed workspace semantic graph into multiple human-readable
schematic sheet views without making documents, pages, filenames, or canvas state the engineering
source of truth.

The milestone is aligned with the M0-M25 direction:

```text
.athena source
  -> compiler/runtime semantic snapshot
  -> Document Projection IR
  -> Presentation IR
  -> Theia renderer
```

## What M26 Proves

- `athena-document-projection-v0` can expose stable sheet-view identities for `Power Distribution`,
  `Control And PLC Logic`, and `Field Wiring And Terminal Transition`.
- A `.athena` source file is not a sheet view. The M26 sample intentionally separates source-file
  organization from sheet-view organization.
- Theia can open `examples/m26/sample-project`, switch to the documentation projection, and show the
  sheet-view selector in the existing Graphical View toolbar.
- M24 route facts and M25 representation facts still render through the M26 document projection path.
- Default verbose route labels such as fully qualified `source -> target` semantic ids are hidden
  from the canvas unless selected or inspected.

## Usage And Smoke Evidence

Open the M26 sample in the Theia IDE:

```powershell
yarn --cwd ide start:m26
```

Run the product smoke proof:

```powershell
yarn --cwd ide start:smoke:m26
```

Latest smoke evidence recorded during implementation:

```text
Athena M26 semantic document projection proof passed.
Athena M26 sample project smoke passed.
```

The product smoke checks workspace resolution, the documentation projection switch, three sheet-view
options, hidden verbose semantic route labels, M24 orthogonal route evidence, and M25 representation
evidence.

## Boundary Checks

`.athena source + compiler/runtime semantic snapshot` remains the single engineering source of
truth.

Document Projection IR owns:

- projection policy identity
- sheet-view identity, order, title, and role
- occurrence membership
- logical document locations
- continuation and cross-reference topology
- reference identity and navigation relationships

Presentation IR owns:

- symbols
- terminals
- labels
- compact marker facts
- route drawing facts
- paint-ready coordinates and primitives

Theia owns rendering and interaction only. Theia does not infer document meaning from canvas scans,
DOM nodes, route line breaks, source filenames, or hidden graph state.

## Scope Guardrails

M26 did not touch or revive deprecated frontend scope:

- no `apps:desktop-viewer`
- no `ui:compose-workbench`
- no deprecated KMP or Compose desktop frontend module

M26 did not introduce new `.athena` syntax. Any future document syntax admission must update ANTLR4,
Tree-sitter, parser, compiler, LSP, fixtures, tests, usage docs, and IDE behavior in the same
milestone story.

## Key Implementation Areas

- `kernel/document-projection-model`
- `kernel/presentation-model`
- `kernel/compiler`
- `kernel/runtime`
- `ide/lsp`
- `integrations/graph-glsp`
- `ide/theia-frontend`
- `ide/theia-product`
- `examples/m26/sample-project`
- `docs/usages/m26-proof-usage.md`

## Deferred Work

M26 intentionally defers:

- PDF and print export
- terminal reports
- wire lists
- part lists
- standards packs
- company presentation packs
- revision workflow
- release package management
- automatic pagination
- standards-complete IEC/EPLAN formatting
- public document syntax admission
- AI document authoring

## Lessons

The term `folio` was rejected because it pulls the architecture toward an EPLAN page-authority model.
The safer Athena term is semantic document projection.

The strongest M26 boundary is this:

```text
Semantic identity determines document occurrence.
Document occurrence determines presentation membership.
Presentation facts determine paint.
Canvas never determines engineering truth.
```

The next milestone should build on this by adding downstream artifact intelligence without changing
the authority chain.
