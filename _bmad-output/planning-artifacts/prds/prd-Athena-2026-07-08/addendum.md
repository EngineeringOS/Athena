# M4 Addendum

This addendum captures design direction that informs M4 architecture and later milestone planning but is intentionally too implementation-shaped for the PRD body.

## 1. Agreed Milestone Split

The current platform expansion is intentionally split into three small milestones:

- **M4** - Athena Theia Platform Proof
- **M5** - Repository and Package Graph Proof
- **M6** - Semantic SCM Proof

This split exists to keep M4 narrow enough to finish cleanly without smuggling package-management and SCM work into the first IDE-platform milestone.

## 2. Theia Position

Theia is not a side adapter and not a VS Code extension story.

For Athena, Theia is the product foundation for this phase:

- Athena should become a custom Theia-based product shell
- Athena should bundle its own curated capabilities and preinstalled extensions
- Athena should host Athena language tooling and workbench management natively in that product shell
- Athena may interoperate with extension ecosystems where useful, but the product identity is Athena-on-Theia

This means the module and architecture language should describe a Theia product, not a thin adapter.

## 3. Recommended M4 Technical Spine

The narrow technical spine for M4 is:

```text
Athena Theia Product
-> Repository open/create flow
-> Athena LSP integration
-> runtime-backed repository session
-> semantic inspection in workbench
-> baseline professional workbench composition
```

The milestone should end once that spine is proven.

## 4. Recommended Deferred Spine

These items are intentionally deferred:

### 4.1 M5

- `athena.yaml`
- `athena.lock`
- repository manifest contract
- dependency resolver
- semantic import graph
- semantic package graph

### 4.2 M6

- semantic SCM above Git
- intent commit surface
- repository review workflows
- publish/review-oriented history flows

## 5. Graphical Projection Signal

The GLSP patch is important as an architectural signal, but it does not widen M4 scope.

The current planning implication is:

- M4 proves the text-first Theia product path through Athena LSP
- a later milestone may add a dedicated graphical projection path under the same Theia product shell
- future graph or diagram views must stay downstream of canonical semantic state and projection metadata
- canvas or diagram state must not become engineering truth

In other words, the next graphical layer should attach as another downstream projection/service boundary, not as a replacement for the semantic kernel or the Theia workbench.

## 6. Repository Naming Direction

The preferred architecture term is now:

- **Engineering Repository**

instead of relying only on:

- **Workspace**

`Workspace` may still survive as a UI/runtime convenience term, but M4 and later milestones should treat the repository root as the primary product object.

## 7. PartCAD Signal

PartCAD is relevant as a reference for one important architectural direction:

- engineering artifacts should behave more like reusable source packages than like copied project files

Athena is broader than PartCAD because Athena is not only about mechanical CAD assets. It is trying to host semantic engineering packages across domains such as ECAD, PLC, SCADA, automation, and related future layers.

That signal belongs mainly to M5 and M6, not to M4 implementation scope.

## 8. Recommended Future Module Grouping

M4 architecture should strongly consider an `ide/` group for Theia and language-service product modules.

A likely direction is:

```text
ide/
  theia-product/
  theia-frontend/
  theia-backend/
  lsp/
```

The exact decomposition is architecture work, but the intention is clear:

- `kernel/` remains semantic/compiler/runtime authority
- `ui/` remains shared UI assets where they still make sense
- `apps/` remains runnable shells outside the Theia product path
- `ide/` becomes the home for editor and workbench product infrastructure

## 9. M4 Exit Condition

M4 should be considered complete when all of the following are true:

- Athena launches as a custom Theia-based desktop product
- a user can create or open an Engineering Repository
- authored Athena source can be edited with real language tooling inside the workbench
- semantic inspection is visible inside the workbench
- the product shell is clearly downstream of the existing semantic and runtime boundaries

At that point M4 has done its job, and the next planning cycle can move cleanly into M5.
