# M38 Usage And Handoff

## Product Surface

Use only the dedicated M38 example for M38 proof:

- `examples/m38/professional-control-drawing`

M38 proves drawing trust:

- Athena source owns engineering meaning.
- `RepresentationDefinition` owns intrinsic geometry and Anchors.
- `GraphicOccurrence` owns placed body and placed Anchor points.
- `PresentationConnector` owns visible connection geometry.
- Theia paints the compiled Presentation Document.

M38 does not prove professional placement taste or professional route quality. Current screenshots still show layout and routing weakness.

## Verification Command Set

Run sequentially on Windows:

```powershell
.\gradlew.bat --no-daemon --console=plain -q :kernel:compiler:test --tests com.engineeringood.athena.compiler.DedicatedM38ProfessionalDrawingSampleTest
.\gradlew.bat --no-daemon --console=plain :ide:lsp:test --tests com.engineeringood.athena.ide.lsp.AthenaM38DedicatedProfessionalControlDrawingSmokeTest
.\gradlew.bat --no-daemon --console=plain :ide:lsp:installDist
yarn --cwd integrations/graph-glsp test
yarn --cwd ide/theia-frontend test
yarn --cwd ide/theia-product build
yarn --cwd ide/theia-product start:smoke:m38
powershell -ExecutionPolicy Bypass -File .\tools\source-set-hygiene-audit.ps1
powershell -ExecutionPolicy Bypass -File .\tools\encoding-audit.ps1
git diff --check
```

## M39 Handoff

M39 may improve placement and composition quality only.

Allowed:

- Replace current placement production.
- Improve grouping, ordering, flow, alignment, spacing, and containment.
- Improve visible layout quality of the same compiled facts.

Not allowed:

- Replace `GraphicOccurrence` as placement authority.
- Recompute connector endpoints after placement.
- Move Anchor meaning into SVG, Theia, or route candidates.
- Add normal `.athena` syntax for coordinates, bends, paint, or renderer mechanics.

Invariant:

- Every occurrence body, bounds, Anchor, hit geometry, and label slot must use one occurrence transform.

## M40 Handoff

M40 may improve route-candidate production only.

Allowed:

- Replace route planner candidate generation.
- Improve lanes, bend count, crossings, bus taps, continuations, labels, bundles, and route readability.
- Add stronger route scoring and route diagnostics.

Not allowed:

- Weaken strict endpoint validation.
- Publish route candidates directly.
- Let Theia repair, snap, infer, or reroute endpoints.
- Create a second connector path outside `PresentationDocument.connectors`.

Invariant:

- First and last visible connector points must exactly equal placed Anchor points.

## Renderer Boundary

Theia remains the product renderer. SVG export remains a Presentation Document consumer.

Forbidden:

- Java2D.
- compiler-side font engine.
- second renderer.
- renderer-owned engineering inference.
- XML runtime authority.

