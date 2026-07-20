# M18 Project Semantic Graph Proof Usage

Updated: 2026-07-16

## Review Status

M18 is closed from a story perspective. All 21 stories in
`../../_bmad-output/implementation-artifacts/m18/sprint-status.yaml` are done.

M18 proves that Athena can move from single-file authored semantics to package-aware project
semantics without moving authority into the frontend:

- `package` and `import` syntax parse through ANTLR and Athena-owned authored AST contracts.
- A compiler-owned project semantic graph is built from governed repository state.
- Imports resolve through `athena.yaml`, `athena.lock`, and the resolved package graph.
- References can link across source units and governed package boundaries.
- Linked authored meaning lowers through canonical Engineering IR.
- LSP projects package-aware diagnostics, definition, references, and document symbols from
  compiler-owned snapshots.
- Tree-sitter mirrors syntax only.

## Supported Authoring Slice

M18 supports the narrow first package-aware syntax slice:

```athena
package com.engineeringood.m18.root
import com.engineeringood.m18.vendor.controls

system Demo {
  device Main {
    port input
    port output
  }

  connect Main.input -> Main.output
}
```

Supported:

- optional file-level package declaration
- package import
- symbol-target import
- dotted, digit-bearing, and internally hyphenated name segments
- existing system/device/port/connect authored declarations

Not supported in M18:

- aliases
- wildcard imports
- visibility/export syntax
- broad new declaration families
- remote registry, marketplace, publish, or multi-root behavior
- frontend-owned package or symbol resolution

## Proof Corpus

Use `../../examples/m18/` as the fixture entry point.

Inventory:

- `../../examples/m18/syntax-proof/`
  - valid package-only and package-plus-import fixtures
  - invalid alias, wildcard, visibility, and missing-target fixtures
  - compiler syntax proof and Tree-sitter syntax UX proof
- `../../examples/m18/linking-lowering-proof/`
  - single-package success
  - cross-source success
  - cross-package success
  - unresolved symbol
  - invalid availability
  - linked lowering proof
- `../../examples/m18/repository-proof/`
  - repository-backed valid workspace
  - local governed vendor package
  - invalid import
  - unresolved symbol
  - graph-invalid publication fixture

These fixtures are local and governed. They do not imply registry, marketplace, publish,
multi-root, desktop-viewer, Kotlin Compose, or frontend semantic-resolution behavior.

## IDE Usage

Use the normal Theia/VS Code-like IDE flows:

- Problems/editor diagnostics for package/import/linking failures.
- Go-to-definition for linked declarations across source units or packages.
- References for package-aware binding locations.
- Document outline with a package-aware root when the project semantic navigation state is present.

The IDE frontend renders compiler/LSP results. It must not independently resolve imports,
packages, or symbols.

Canvas reveal behavior remains limited to the existing Theia workbench reveal model. Package-aware
navigation can preserve canonical engineering subject ids for existing reveal-capable surfaces, but
M18 does not introduce a new canvas system, renderer path, GLSP flow, desktop-viewer behavior, or
Kotlin Compose UI.

## Verification Path

Run commands sequentially on Windows. Do not run Gradle verification commands concurrently.

Focused syntax proof:

```powershell
java25; .\gradlew.bat --no-daemon --console=plain :kernel:language:test --tests *M18*
yarn --cwd ide/tree-sitter-athena test
```

Focused compiler semantic proof:

```powershell
java25; .\gradlew.bat --no-daemon --console=plain :kernel:compiler:test --tests *ProjectSemantic*
java25; .\gradlew.bat --no-daemon --console=plain :kernel:compiler:test --tests *AthenaM18*
```

Focused LSP proof:

```powershell
java25; .\gradlew.bat --no-daemon --console=plain :ide:lsp:test --tests *PackageAware*
java25; .\gradlew.bat --no-daemon --console=plain :ide:lsp:test --tests *M18*
```

Theia frontend reveal proof:

```powershell
yarn --cwd ide/theia-frontend test
```

Closeout audits:

```powershell
powershell -ExecutionPolicy Bypass -File .\tools\m18-scope-boundary-audit.ps1
powershell -ExecutionPolicy Bypass -File .\tools\encoding-audit.ps1
```

Broader closeout pattern used during the milestone:

```powershell
java25; .\gradlew.bat --no-daemon --console=plain :kernel:language:test
java25; .\gradlew.bat --no-daemon --console=plain :kernel:compiler:test
java25; .\gradlew.bat --no-daemon --console=plain :ide:lsp:test
yarn --cwd ide/tree-sitter-athena test
yarn --cwd ide/theia-frontend test
powershell -ExecutionPolicy Bypass -File .\tools\m18-scope-boundary-audit.ps1
powershell -ExecutionPolicy Bypass -File .\tools\encoding-audit.ps1
```

Run the Tree-sitter or Theia frontend checks when those areas are touched. Always run the encoding
audit after text edits.

## Development Rules After M18

- Extend package-aware behavior through the compiler-owned project semantic graph.
- Keep package/import resolution downstream of governed repository state.
- Keep Engineering IR as canonical after linking.
- Keep Tree-sitter syntax-only.
- Keep Theia frontend behavior downstream of compiler/LSP authority.
- Keep future scope expansions explicit in PRD, architecture, stories, proof fixtures, and closeout
  docs.

## Retrospective Pointer

The milestone achievement and retrospective summary is recorded in
`../../_bmad-output/implementation-artifacts/m18/m18-achievement-usage-retrospective-2026-07-16.md`.
