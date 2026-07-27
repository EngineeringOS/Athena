# M35 Architecture Spine Brownfield Reality Review

**Target:** `ARCHITECTURE-SPINE.md`  
**Review type:** adversarial brownfield reality check  
**Date:** 2026-07-27  
**Verdict:** **CONDITIONAL - direction is sound, but the spine is not ready to freeze.**

The repository supports the M34 substrate named by the spine: the listed Gradle modules exist,
`PhysicalTraitDefinition`, `ResolvedPhysicalTraitDefinition`, `RepresentationDefinition`,
`BindingResolver`, `RepresentationBindingCompiler`, `GraphicPrimitive`, repository contracts,
ANTLR4, Tree-sitter, LSP4J, and Theia are real. The M35 physical-installation pipeline is not yet
implemented, which is expected for a milestone architecture, but the document repeatedly mixes
existing brownfield facts, renamed existing contracts, and proposed M35 contracts without marking
which is which. More seriously, the proposed lock/snapshot identity is circular and the proposed
physical routing owner points at an existing schematic/electrical-specific module.

## Blocking Findings

1. **[HIGH] `athena.lock` and resource snapshot identity form a dependency cycle.**

   Spine lines 136-138 say the generated lock will contain admitted resource hashes. Lines 158-160
   say snapshot identity contains the `athena.lock` digest, while the sequence at lines 315-320 says
   the snapshot produces the lock hashes. The current stager already requires `athena.lock` and
   includes its digest in `snapshotIdentity`
   (`RepresentationPackageSnapshotStager.kt:18-33`, `57`, `129-133`). A final lock cannot both be an
   input to and an output of the same snapshot identity without a fixed-point protocol. Define two
   explicit stages, for example dependency-resolution lock material -> admitted resource snapshot ->
   final lock evidence, and state which digest keys each stage.

2. **[HIGH] Core M35 target contracts are presented as adopted architecture without an
   existing-versus-target ledger.**

   No repository definitions currently exist for `PhysicalInstallationContract`,
   `PhysicalInstallationIR`, `InstallationSpace`, `Enclosure`, `MountingSurface`, `Rail`, `Duct`,
   `MountedOccurrence`, `TerminalGroup`, physical `RouteChannel`, `ClearanceZone`,
   `PhysicalInstallationCompiler`, `CabinetCompositionCompiler`, `GraphicOccurrenceTrace`, or
   `PackageResourceKey`. The current `kernel:physical-model` contains only a module marker and the
   M14 physical-trait contracts. These are valid M35 target names, but lines 70-237 label every rule
   `[ADOPTED]` and lines 355-394 combine existing and future contents without status markers. Add a
   contract ledger with `EXISTING`, `EXTEND`, and `NEW IN M35` status so story authors cannot assume
   nonexistent APIs are reusable substrate.

3. **[HIGH] The proposed physical-routing ownership is incompatible with the current
   `routing-model`.**

   Spine lines 184-193 and 391 assign physical route-channel work partly to `routing-model`. The
   current module is explicitly schematic/electrical: `RouteFact` uses `SchematicRouteId`,
   `ElectricalConnectionId`, `SchematicRouteSegment`, `SchematicRouteLane`, and terminal-anchor
   facts (`RouteConstraintsAndFacts.kt:95-123`). `AthenaProfessionalDrawingCompiler` also builds a
   `SchematicRoutingLayoutContext` and `AthenaRouteRequest` (`AthenaProfessionalDrawingCompiler.kt:251-302`).
   Reusing these contracts would leak ECAD vocabulary into the general Physical Installation Model.
   The spine must choose one boundary: physical channel topology and routed-installation facts live
   in `physical-model`, or a deliberately generic routing kernel is introduced later. M35 must not
   silently reinterpret M24 schematic routing types.

4. **[HIGH] `GraphicOccurrenceTrace` ownership is underspecified and risks importing runtime objects
   into presentation contracts.**

   Lines 200-203 say the trace contains a binding rule, representation definition, admitted resource
   snapshot, and source declarations. Lines 366 and 392 place it in `presentation-model`, while the
   dependency diagram does not give `presentation-model` a dependency on `package-runtime` or source
   compiler models. The current `presentation-model` depends on engineering, document projection,
   layout, routing, and representation, but not package runtime. Define the trace as stable typed
   references and digests only, with compiler-owned enrichment kept outside transport. Otherwise
   this creates a broad cross-layer DTO or an undeclared dependency from presentation to package
   runtime/compiler internals.

5. **[HIGH] `PhysicalSize` does not enforce the measurement invariant claimed by AD-5.**

   Lines 115-119 and 327-329 require non-negative/positive integer millimetres. Existing
   `PhysicalSize` is an unchecked data class with three `Int` fields and no `init` validation
   (`PhysicalTraitModels.kt:11-15`). Negative and zero dimensions are currently constructible.
   Reusing it as the basis for fit, collision, and clearance evaluation would move invalid-state
   checks into every evaluator. M35 needs a validated measurement/value contract or must strengthen
   `PhysicalSize` before using it as an IR invariant.

## Major Findings

6. **[MEDIUM] `representationPackageRoots` is not part of the typed repository manifest contract.**

   Spine lines 131-140 and 308-320 treat `athena.yaml` as one typed package/resource descriptor.
   `RepositoryManifest` currently contains only `primaryPackage` and `dependencies`
   (`RepositoryContracts.kt:68-77`). `representationPackageRoots` is separately scanned in
   `AthenaRepositoryContractLoader` and stored only on its validation result
   (`AthenaRepositoryContractLoader.kt:100-105`, `139`, `241-262`). The snapshot stager also contains
   another independent line parser (`RepresentationPackageSnapshotStager.kt:222-236`). Before M35
   extends repository identity, the roots must have one typed owner and one parser path; otherwise
   AD-7 preserves the duplicate authority M34 was intended to remove.

7. **[MEDIUM] The current package snapshot is content-addressed, not an immutable admitted-resource
   snapshot with the semantics claimed by AD-8/AD-9.**

   The current capture walks every package root and admits every `.athena` and `.svg` file before
   compilation (`RepresentationPackageSnapshotCapture.kt:18-96`). It then validates package and
   `graphic svg` declarations with regexes (`RepresentationPackageSnapshotCapture.kt:99-156`). The
   stager writes content-addressed files but does not make the snapshot tree filesystem-immutable
   (`RepresentationPackageSnapshotStager.kt:134-199`). Lines 147-152 correctly describe a desired
   replacement, but the wording “existing no-follow” plus “freezes one immutable admitted snapshot”
   hides the size of the rewrite. Mark the two-phase admission API as new M35 work and define the
   boundary between source capture, AST compilation, declared-resource capture, and publication.

8. **[MEDIUM] The proposed two-phase admission has no brownfield API that can implement its atomicity
   guarantee.**

   Current `RepresentationPackageSnapshotCapture.collect` captures source and SVG in one traversal,
   while `AthenaRepresentationPackageSnapshotCompiler` later opens staged paths with
   `Files.readString` (`AthenaRepresentationPackageSnapshotCompiler.kt:29-39`). Lines 147-150 require
   identity verification before and after resource read, but do not define a staged-source snapshot
   identity that the resource phase is bound to. Specify separate immutable `SourceSnapshot` and
   `AdmittedPackageSnapshot` contracts, or equivalent digests, so a source declaration cannot change
   between AST parsing and resource admission.

9. **[MEDIUM] AD-14 names a mutation flow that does not match existing M29/M31 contracts.**

   Lines 211-213 use `ActionIntent -> AuthoringTransaction -> SourceMutationPreview`. The repository
   contracts are `SemanticActionIntent` (`InteractionModels.kt:138-145`),
   `SemanticAuthoringTransaction` (`AuthoringTransactionModels.kt`), and `AuthoringPreview` with
   `AuthoringSourceEditEvidence` (`AuthoringPreviewModels.kt:55-93`). No `ActionIntent`, generic
   `AuthoringTransaction`, or `SourceMutationPreview` type exists. Use the exact brownfield names or
   explicitly mark the names as conceptual aliases; otherwise M35 stories may introduce duplicate
   authoring types.

10. **[MEDIUM] “existing interaction subject index” is an asserted name, not a repository contract.**

    Line 204 relies on an existing interaction subject index. The repository has
    `InteractionSubjectResolver`, `InteractionSubjectKey`, and `SemanticCapabilityRegistry`; there is
    no `InteractionSubjectIndex` contract. The frontend also derives subject kind from semantic-id
    string prefixes for rendered selections (`athena-semantic-selection-model.ts:511-523`). Name the
    actual registry/resolver boundary and make removal of frontend prefix inference part of the trace
    acceptance criteria if typed end-to-end trace is the goal.

11. **[MEDIUM] The existing drawing-composition module is not a Cabinet composition compiler.**

    Lines 44-46, 180-182, and 364 describe `CabinetCompositionCompiler`. The brownfield module owns
    drawing sheet frames, title blocks, zones, and bounds through `DrawingSheetCompositionCompiler`
    (`DrawingSheetCompositionCompiler.kt:6-145`). It currently depends on engineering, projection,
    and representation, not `physical-model`. This is a reasonable host for new work only if the
    spine explicitly states whether Cabinet composition is a new cohesive compiler beside sheet
    composition or an extension of an existing drawing compiler. “Consumes physical output” alone is
    not enough to prevent another mixed-responsibility composition file.

12. **[MEDIUM] “Paint-only Theia renderer” is a target, not the current renderer boundary.**

    Lines 47-48 and 264 imply an established Theia paint adapter. The repository has typed Graphic
    Primitive LSP payload mapping in `AthenaPresentationSessionProtocol`, and the frontend maps
    primitive kinds in `athena-graph-presentation-model.ts`. However, the runtime service registry
    still exposes the Kotlin `SvgRenderer` as the shared renderer (`AthenaServiceRegistry.kt:5`,
    `10-11`, `35`, `101-102`). No contract named Theia paint adapter exists. Mark this as an M35
    product-path target and state whether Kotlin SVG rendering remains a supported backend or is
    removed from the active Cabinet path.

13. **[MEDIUM] The lock model has no resource-hash or schema fields yet.**

    Lines 137-138 and 320 require those fields, but current `RepositoryLock` contains only lock
    version, primary package, and resolved packages (`RepositoryContracts.kt:79-90`), and
    `ResolvedPackage` contains package id, source root, and dependencies (`RepositoryContracts.kt:101-110`).
    This is new schema work, not an in-place use of an already capable lock. The spine must specify
    lock schema-version migration behavior and deterministic serialization order before stories use
    “lock digest” as a cache key.

14. **[MEDIUM] The declared M35 product proof does not exist in current scripts or examples.**

    Lines 374-379 prescribe `examples/m35/physical-installation-cabinet`, and AD-16 requires a default
    Cabinet startup plus desktop/narrow smoke evidence. `examples/m35` is absent. Neither
    `ide/package.json` nor `ide/theia-product/package.json` has `start:m35` or `start:smoke:m35`.
    This is expected future work, but it must be marked as a deliverable rather than implied by the
    structural seed. Add explicit story ownership for sample creation, startup wiring, screenshot
    scripts, and canvas-pixel checks.

15. **[MEDIUM] Shared ANTLR/Tree-sitter accepted-and-rejected corpus is not verified brownfield
    infrastructure.**

    ANTLR4 is the semantic parser and `ide/tree-sitter-athena` exists, but line 223 requires a shared
    accepted/rejected syntax corpus. No shared cross-parser corpus contract was identified in the
    current repository; existing Tree-sitter scripts run their own generation/tests. Treat the shared
    corpus as new M35 acceptance infrastructure and define its owning location and invocation.

## Minor Findings

16. **[LOW] The stack table is mostly accurate, but it mixes exact resolved versions with ranges and
    omits Electron despite making Electron evidence normative.**

    Verified repository evidence:

    | Technology | Spine | Repository reality |
    | --- | --- | --- |
    | Kotlin | 2.4.0 | Exact in `gradle/libs.versions.toml` |
    | Gradle | 9.6.1 | Exact wrapper distribution |
    | ANTLR | 4.13.2 | Exact in version catalog |
    | LSP4J | 0.23.1 | Exact in version catalog |
    | Eclipse Theia | 1.73.1 | Exact package dependencies |
    | TypeScript | 5.9.x | Declared `^5.9.2`, lock resolves 5.9.3 |
    | Node.js | >=22 | Engine constraint; current environment is v24.15.0 |
    | tree-sitter CLI | >=0.26.1 | Declared range; lock resolves 0.26.11 |
    | web-tree-sitter | 0.26.x | Declared `^0.26.0`; lock resolves 0.26.11 |
    | Electron | not listed | Exact dev dependency 39.8.7; named by AD-16 evidence |

    Replace “ratified repository versions” with “repository exact versions and supported ranges,” or
    show separate `constraint` and `resolved` columns.

17. **[LOW] `PackageResourceKey` ties logical resource identity to owning source-unit identity without
    proving that this matches existing package semantics.**

    Lines 158-160 define a new key that includes owning source-unit identity. No such key exists in
    the repository. A package-local resource may legitimately be referenced by multiple Athena source
    units; coupling identity to one owner can duplicate identity or make source-file moves breaking.
    Define whether a resource has exactly one declaring source, whether other units import the typed
    declaration, and whether declaration moves preserve logical identity.

## Verified Brownfield Substrate

- Every Gradle module named in the dependency and structural diagrams exists in `settings.gradle.kts`:
  `engineering-model`, `physical-model`, `repository-model`, `language`, `package-model`,
  `package-runtime`, `compiler`, `drawing-composition`, `representation-model`,
  `presentation-model`, `routing-model`, `interaction-model`, and `ide:lsp`.
- Current Gradle dependencies support the stated base direction:
  `physical-model -> engineering-model`, `package-runtime -> package-model + representation-model`,
  `drawing-composition -> engineering-model + projection-model + representation-model`, and
  `compiler` consumes all of these. Here `A -> B` denotes “A depends on B”; the spine's Mermaid uses
  the opposite provider-to-consumer visual convention, so that convention should be stated.
- `RepresentationDefinition`, `GraphicPrimitive`, `BindingResolver`, and
  `RepresentationBindingCompiler` are real M34 contracts.
- `PhysicalTraitDefinition`, `ResolvedPhysicalTraitDefinition`, and `PhysicalSize` are real, narrow,
  read-only physical-knowledge contracts.
- Repository manifest/lock roles are typed as authored intent versus derived state.
- Snapshot capture already has root containment, no-follow regular-file checks, symbolic-link
  rejection, byte/hash capture, file/aggregate budgets, and deterministic sorting.
- Graphic Primitive transport exists through LSP payloads, and Theia frontend code recognizes the
  current primitive vocabulary.
- All versions in the spine can be traced to repository configuration or dependency locks; none is
  fabricated. The wording around ranges/resolved versions needs correction as noted above.

## Required Corrections Before Freeze

1. Break the lock/snapshot digest cycle with named pre-resolution and final-evidence stages.
2. Add an `EXISTING / EXTEND / NEW` contract ledger for every named M35 contract.
3. Keep physical routing generic and outside current schematic/electrical route types.
4. Define `GraphicOccurrenceTrace` as stable references/digests and fix its dependency owner.
5. Introduce validated physical measurement and placement value types.
6. Give `representationPackageRoots` and resource declarations one typed repository/compiler path.
7. Replace conceptual M31 names with exact existing authoring contract names.
8. Mark the M35 sample, product scripts, shared grammar corpus, and paint-only adapter as explicit new
   deliverables.

**Freeze recommendation:** do not freeze until findings 1-5 are resolved in the spine. The remaining
findings can become explicit story acceptance criteria, but they must not remain implied brownfield
capabilities.
