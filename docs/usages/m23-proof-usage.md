# M23 Layout Hint Language Admission Usage

Updated: 2026-07-18

## Review Status

M23 is the governed layout hint language admission milestone. It closes the M22 gap where the Graph
Workbench could preview a layout block shape, but the real `.athena` language did not yet accept
that syntax.

The openable proof project is `../../examples/m23/sample-project/`.

The source file to open is:

- `../../examples/m23/sample-project/src/01-layout-hints.athena`

## Supported Syntax

M23 supports system-scoped layout blocks:

```athena
layout schematic-sheet {
  place OperatorHMI1 near ControllerPLC1
  place TerminalBlockXT1 below ControllerPLC1
  align OperatorHMI1 aligned-with ControllerPLC1 axis vertical
  group OperatorHMI1 grouped-with ControllerPLC1
}
```

Supported statements:

- `place SUBJECT near TARGET`
- `place SUBJECT below TARGET`
- `align SUBJECT aligned-with TARGET axis horizontal`
- `align SUBJECT aligned-with TARGET axis vertical`
- `group SUBJECT grouped-with TARGET`

M23 does not support file-global layout blocks, raw pixel-coordinate layout language, advanced
routing, advanced route or label hint persistence, AI layout, public repository/import ecosystem
work, broad IEC library ingestion, hidden canvas state, ELK-owned architecture, or EPLAN parity.

## IDE Usage

Build the installed LSP host before IDE smoke or manual IDE proof after syntax work:

```powershell
.\gradlew.bat --no-daemon --console=plain :ide:lsp:installDist
```

Then open the M23 sample project:

```powershell
Set-Location ide
yarn start:m23
```

In the IDE:

- open `src/01-layout-hints.athena`
- confirm the editor does not mark `layout schematic-sheet` as a syntax error
- confirm Problems has no false M23 layout syntax diagnostics for the sample
- open `Graphical View`
- confirm the Graphical View projects the active M23 source
- confirm source, outline, Problems, and Graphical View continue to use the same Athena LSP/compiler
  semantic path
- use the top information icon for the sheet information popover
- use the transparent floating zoom controls on the grid-backed canvas

## Verification Path

Run checks sequentially on Windows:

```powershell
node --test ide/theia-frontend/scripts/athena-m23-sample-project.test.mjs
.\gradlew.bat --no-daemon --console=plain :kernel:compiler:test --tests com.engineeringood.athena.compiler.AthenaM23SampleProjectCompilerTest
.\gradlew.bat --no-daemon --console=plain :ide:lsp:test --tests com.engineeringood.athena.ide.lsp.AthenaDiagnosticsPublishingTest.m23*
.\gradlew.bat --no-daemon --console=plain :ide:lsp:installDist
node ide/theia-product/scripts/verify-athena-m23-sample-project.js
powershell -ExecutionPolicy Bypass -File .\tools\encoding-audit.ps1
```

The product smoke emits `ATHENA_GRAPH_WORKBENCH_PROOF={...}` when the Theia Graphical View has
rendered the sample project. If it reports `extraneous input 'layout'`, the installed LSP host is
stale; rerun `:ide:lsp:installDist`.

## M22 Correction

M22 was preview-only for layout block source syntax.

M22 selected and documented the layout block shape, and the Graph Workbench could construct a
preview/source-edit snippet. That was not real language admission.

M23 is the first milestone where the same layout block is admitted through:

- ANTLR4 parser
- Tree-sitter parser
- authored `LayoutDeclaration` AST
- compiler layout intent and constraint lowering
- LSP diagnostics
- Graph Workbench source-edit path
- openable Theia sample project

## Development Rules After M23

- New `.athena` syntax must update both ANTLR4 and Tree-sitter.
- ANTLR/compiler/LSP remain the source of diagnostics and meaning.
- Tree-sitter remains IDE syntax UX only.
- Graph Workbench must serialize layout intent through accepted Athena source syntax.
- Renderer and canvas state must never become hidden layout truth.
- `.mjs` files are test harnesses only; the customer proof is the openable sample project.
