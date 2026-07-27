# M34 Canonical Representation Migration Ledger

This ledger is process evidence, not product authority. The M34 PRD and Architecture Spine define
the target architecture; these entries name the brownfield transition and executable deletion gate.

| Existing contract/path | Disposition | Current active callers | Named owner / target | Executable deletion gate |
| --- | --- | --- | --- | --- |
| `RepresentationDefinition` | Extend; canonical | `NativeRepresentationLibraryLoader`, `RepresentationBindingCompiler`, `PackageBackedRepresentationOccurrenceFactory` | Story 1.1 implementation agent | `.\gradlew.bat --no-daemon --console=plain :kernel:representation-model:test --tests com.engineeringood.athena.representation.RepresentationDefinitionContractTest` exits 0. |
| `GraphicPrimitive` | Reuse/extend; sole Cabinet visual vocabulary | M33 IEC libraries, `PresentationModelDeriver`, SVG adapter | Story 3.4 implementation agent | `rg -n "PresentationPrimitive|svg_path|descriptor-bounds" kernel/compiler/src/main ide/lsp/src/main integrations/graph-glsp/src` exits 1 after active M34 producers are removed. |
| `DrawingSymbolAnatomy` | Replace; compatibility input only | `M33IecSymbolSupport`, M33 composition/reference models | Story 4.1 implementation agent | `rg -n "DrawingSymbolAnatomy" kernel/package-runtime/src/main kernel/compiler/src/main ide/lsp/src/main` exits 1. |
| `M33IecSymbolDefinition` | Replace | `M33IecSymbolSupport`, `M33IecCoreSymbolLibrary` | Story 4.1 implementation agent | `rg -n "M33IecSymbolDefinition" kernel/package-runtime/src/main` exits 1. |
| independently authored `RepresentationDescriptor` | Replace with generated projection | M32/M33 package fixtures and package-runtime resolver inputs | Story 3.1 implementation agent | `.\gradlew.bat --no-daemon --console=plain :kernel:package-runtime:test --tests '*BindingResolver*'` exits 0 with generated-only M34 descriptor assertions. |
| `RepresentationDescriptor.toRepresentationDefinition` reverse adapter | Delete | `PackageBackedRepresentationOccurrenceFactory` | Story 3.3 implementation agent | `rg -n "toRepresentationDefinition" kernel/package-runtime/src/main` exits 1 and package occurrence tests exit 0. |
| `PresentationPrimitive` body | Replace; compatibility-only outside the M34 Control Drawing authority | `NativeRepresentationLibraryLoader`, descriptor reverse adapter, M32/M33 compiler transport, superseded M34 Cabinet adapters | Epic 5 implementation agent | `rg -n "PresentationPrimitive" kernel/compiler/src/main ide/lsp/src/main integrations/graph-glsp/src` exits 1 for the active Control Drawing path, or remaining hits are explicitly M32/M33/superseded-Cabinet compatibility and covered by `AthenaM34CabinetRenderPathDeletionGateTest`. |
| legacy box / `descriptor-bounds` fallback | Delete | descriptor reverse adapter and graph renderer compatibility path | Story 3.4 implementation agent | `rg -n "descriptor-bounds|fallback.*rectangle|Rectangle.*fallback" kernel/package-runtime/src/main kernel/compiler/src/main ide/theia-frontend/src` exits 1 and the M34 Cabinet proof reports zero fallback components. |
| direct raw SVG / `svg_path` transport | Delete | legacy presentation payload and graph adapter compatibility paths | Story 3.4 implementation agent | `rg -n "svg_path|rawMarkup|innerHTML" ide/lsp/src/main integrations/graph-glsp/src ide/theia-frontend/src` exits 1 for M34 transport/render paths and Electron E2E reports zero raw-markup sinks. |
| M32/M33 XML package loaders | Replace; fixture-only until deleted | `M33CabinetPackageSet`, M33 sample package loading | Story 3.3 implementation agent | `rg -n "\.xml|DocumentBuilder|SAXParser" examples/m34 kernel/package-runtime/src/main` exits 1 for the active M34 sample path. |

## Story 1.1 Evidence

- `RepresentationDefinitionContractTest` is the RED/GREEN contract gate for the canonical model.
- Existing constructors retain compatibility defaults so M30-M33 code remains buildable during migration.
- No new producer may target `DrawingSymbolAnatomy`, `PresentationPrimitive`, XML manifests, direct
  raw SVG transport, or the descriptor-to-definition fallback.

## Story 3.4 Evidence

- `M34CabinetRenderPathProof` records typed `GraphicPrimitiveDocument` transport, renderer input
  authority, raw-markup absence, fallback absence, hard-coded document bounds absence, and
  compatibility-only `PresentationPrimitive` ledger notes.
- `AthenaM34CabinetRenderPathDeletionGateTest` verifies the active M34 package/source/transport path
  does not contain `PresentationPrimitive`, raw-markup fields, descriptor-bounds fallback, or generic
  fallback-box authority.
- Existing M32/M33 references and the superseded M34 Cabinet adapters remain ledgered compatibility
  until final Epic 5 purge; they are not Control Drawing metadata or render authority.
