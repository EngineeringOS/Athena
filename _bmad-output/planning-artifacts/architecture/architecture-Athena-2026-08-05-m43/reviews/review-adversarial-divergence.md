# Adversarial Divergence Review - Athena M43 Architecture Spine

Target: `ARCHITECTURE-SPINE.md`

Companion reviewed: `UPSTREAM-ADOPTION.md`

Review lens: two independent implementation units, built one level below the spine, obey every AD
literally and still attempt to interoperate.

## Verdict

**BLOCKED FOR PARALLEL IMPLEMENTATION HANDOFF.**

The spine fixes the right authority direction and rejects the most dangerous duplicate truths. It
does not yet fix the shared contracts tightly enough to make independently built M43 units
converge. Two implementations can each use one immutable scene, one Konva adapter, typed
revisioned commands, deterministic compilation, governed assets, local execution, deterministic
SVG, and the required scale techniques while disagreeing on the scene wire shape, stable identity,
revision and digest semantics, Reality ownership, grid anchors, mutation transactions, trace
granularity, paint behavior, asset identity, and scale acceptance.

This is not an implementation-detail complaint. These choices cross Kotlin, JSON Schema, LSP,
generated TypeScript, Konva, SVG, source editing, tests, and E2E. Once separate stories encode
different choices, one unit must be rewritten.

## Independent Compliant Units

The divergence attack constructs two complete contract packs from the spine. Each pack is a valid
one-level-down elaboration, not a legacy or alternative authority.

- **Unit Alpha, compiler-first:** owns a closed schema in `presentation-model`, a Kotlin scene
  producer, runtime publication, a runtime-hosted authoring application service, an SVG serializer,
  generated TypeScript, and a Konva adapter consuming that generated contract.
- **Unit Beta, product-first:** owns a different closed schema in `presentation-model`, a Kotlin
  producer, LSP-hosted workspace mutation orchestration, an SVG serializer, generated TypeScript,
  and a Konva adapter consuming that generated contract.

Both units:

- keep `*.athena` and `*.sheet.athena` as separate source authorities;
- compile Engineering, Projection, and Spatial facts into one immutable
  `AthenaDiagramScene`;
- use a closed numeric-versioned schema, ordered arrays, integer `SceneUnit`, stable IDs, one
  revision, and one digest;
- publish a complete scene atomically and fail closed without a legacy renderer;
- keep Konva inside one imperative adapter with one Stage, one viewport transform, and one hit-test
  path;
- emit typed `MoveOccurrence` and `ConnectPorts` commands with an expected scene revision and
  mutate source through structured edits plus the workspace undo transaction;
- resolve only local sanitized digest-addressed assets and bundled digest-pinned fonts;
- provide deterministic SVG, pinned-environment PNG proof, desktop/narrow E2E, and a synthetic
  100,000-element benchmark;
- use culling, partitioning, caching, drag isolation, batched redraw, and zoom-dependent detail;
- add no server, network asset authority, Pattern authority, Pixi dependency, or compatibility
  fallback.

They therefore satisfy the prohibitions and positive obligations in AD-1 through AD-17. The spine
still permits these incompatible choices:

| Axis | Unit Alpha | Unit Beta | Why both remain literal-compliant |
| --- | --- | --- | --- |
| Scene schema | Flat discriminated `elements[]`; kind-specific geometry; inline source trace | Flat common element envelope; kind-specific `payload`; top-level trace table referenced by ID | AD-5/AD-8 name facts and closure properties, not the normative JSON fields or cardinalities |
| Reality ownership | Spatial emits ready-to-paint occurrence, route, label, and coordinate-frame geometry; Presentation assigns paint and serializes | Spatial emits occurrence/route/label geometry; Presentation joins Projection coordinate references into frame elements and assigns paint/z-order | The inherited ownership rules and M43's broad word `compiler` do not specify pass inputs/outputs for frame, labels, transforms, or z-order |
| Application-service owner | Kernel runtime owns command validation, source edit, undo transaction, and recompile | LSP server owns workspace/editor transaction orchestration and delegates validation/recompile to kernel services | AD-10 requires an application service but assigns no module or dependency direction |
| Move command | `revision: Long`, absolute row/column plus `microX/microY`, and desired `locked: Boolean` | opaque string revision, typed `SheetAnchor`, and `lockIntent: Keep/Set/Clear` | Both carry expected revision, stable IDs, target anchor, and lock intent; exact command algebra is absent |
| Grid anchor | `micro(i,j)` lowers to macro origin plus `(i-1,j-1)` | `micro(i,j)` lowers to macro origin plus `(i,j)`, so last anchors lie on shared macro boundaries | Both keep `i,j` in `1..N`, integer units, `N x N` subdivisions, and `C*N` by `R*N` extent |
| Konva behavior | Exact CSS contain-fit; DPR changes backing buffer only; topmost z-order element wins hit test | Device-pixel-quantized contain-fit; ports win semantic hit resolution before other shapes | Both have one fit transform, one hit path, stable IDs, and logical geometry unchanged |
| Asset identity | `AssetRef` names a package-relative resource and hashes admitted source bytes; compiler stores sanitized local cache | `AssetRef` names a local `asset://` digest and hashes canonical sanitized bytes; LSP serves bytes locally | Both are local, stable, digest-addressed, sanitized, and free of network authority |
| Scale proof | 100,000 mostly occurrence/frame elements; labels omitted at low zoom | 100,000 mostly port/route elements; routes simplified at low zoom | AD-14 requires measurements and techniques but gives no fixture composition, thresholds, or legal detail-reduction semantics |
| Source trace | Every element carries one primary source span plus occurrence and subject IDs | Elements reference a deduplicated multi-origin trace entry with navigation roles | Both traces are portable, structured, stable, and LSP-mappable; schema and navigation precedence are unspecified |

No listed choice requires a second scene, renderer truth, layout database, semantic model, or source
mutation path. That is the decisive failure: compliance does not imply convergence.

## Tier 0 - Freeze Blockers

1. **`AthenaDiagramScene` is named as a closed contract but only sketched as prose.**

   AD-5 lists page coordinate system, IDs, bounds, transforms, routes, labels, assets, z-order,
   trace, revision, and digest. AD-8 requires a committed closed JSON Schema. The Minimal Scene
   Shape lists only top-level nouns. Nothing fixes exact property names, required versus optional
   fields, discriminated element variants, geometry records, style records, trace representation,
   asset representation, extension policy, numeric ranges, or schema location. Unit Alpha and Unit
   Beta can each produce a closed valid schema that contains every named fact and remains mutually
   unreadable.

   **Required closure:** name the normative schema artifact and owner, freeze the top-level object
   and every M43 element variant, define required/optional/null rules and `additionalProperties`, and
   require Kotlin plus TypeScript to be generated or validated from that one artifact. Consumer
   stories must not begin from prose DTOs.

2. **Stable identity is required but no identity algebra or element topology is defined.**

   AD-3, AD-5, AD-8, and the Consistency Conventions distinguish scene element, Projection
   occurrence, engineering subject, and Port IDs. They do not define lexical formats, construction
   rules, stability windows, namespace ownership, or cardinality. One occurrence may be one scene
   element with child ports, or many body/label/port paint elements tied to one occurrence. A route
   may have its own occurrence owner, relationship subject, and endpoint port IDs. Both shapes can
   carry all required IDs. Selection, cache keys, hit testing, command targets, trace, and scene
   diffs then disagree.

   **Required closure:** define typed ID domains and canonical encodings; define which compiler pass
   assigns each ID; define stability across recompilation, source movement, renaming, repeated
   occurrences, and process restart; define parent/owner relationships for frame, occurrence, port,
   route, and label elements.

3. **Revision, canonical ordering, and digest are three undefined contracts pretending to be one.**

   AD-5/AD-7/AD-8 require one revision, ordered arrays, canonical facts, and a digest. AD-10 uses the
   revision as a mutation precondition. AD-13 claims canonical SVG bytes. No rule selects monotonic
   versus content-derived revisions, revision scope across restart or workspace reload, digest
   algorithm, digest input, canonical JSON encoding, Unicode normalization, integer formatting,
   asset/font digest inclusion, or ordering tuples for assets and elements. `z-order` also has no
   tie-breaker. Two deterministic units can compute different bytes and both correctly call them
   canonical.

   **Required closure:** define scene/revision identity and lifecycle, exact digest algorithm and
   byte preimage, canonical JSON rules, stable sort tuples for every array, z-order tie rules, and
   whether diagnostics, asset bytes, font bytes, compiler version, or profile participate. Publish
   at least one exact scene/digest vector.

4. **Reality ownership becomes ambiguous at the Presentation compiler boundary.**

   Inherited M39 AD-4 says Spatial owns placement, bounds, anchors, routes, and geometry metrics;
   inherited M39 AD-5 says Presentation owns paint facts only. M43 AD-4 says the `Compiler` derives
   exact bounds, ports, routes, labels, collisions, z-order, and paint geometry. M43 AD-5 then places
   bounds, transforms, routes, labels, and frame in the Presentation scene. Structural Seed names a
   `Spatial Reality -> AthenaDiagramScene` compiler but gives no input contract. Page frame,
   coordinate strips, label metrics, transforms, collision results, and z-order can therefore land
   in different passes without either team admitting a second authority.

   **Required closure:** add a pass-level ownership matrix and normative input/output shapes for
   Projection, Spatial, and Presentation compilation. For each scene fact, state whether
   Presentation copies, validates, joins, or derives it. Explicitly assign page/drawing bounds,
   coordinate-frame cell geometry, occurrence transforms, label placement, z-order, and style
   resolution.

5. **Grid extent is specified, but persisted anchor coordinates are not.**

   AD-2 correctly fixes drawing extent to `C*N` by `R*N` and constrains `micro(x,y)` to `1..N`.
   It never defines whether a micro index denotes a subdivision cell, its origin, its center, or a
   grid intersection. `N` subdivisions have `N+1` boundary intersections. With integer
   `SceneUnit`, both `offset = i-1` and `offset = i` are plausible, but they move every occurrence
   and disagree at shared macro boundaries. The default anchor when `micro` is absent is also
   missing. So are row direction, multi-letter rows, drawing origin relative to page/frame bounds,
   and whether an anchor describes occurrence origin, center, or a named occurrence anchor.

   **Required closure:** publish exact lowering formulas from `(row, column, microX, microY)` to a
   logical anchor, including one-based conversion, boundary ownership, default micro anchor,
   row/column growth, supported row labels, page/drawing origin, and occurrence anchor semantics.
   Add tables for `cell: 4` at `A1`, `A2`, `B1`, and all four boundary cases.

6. **Snapping has no authority or transport contract.**

   AD-2 binds snapping, AD-4 says constraints persist while geometry is derived, AD-9 limits the
   adapter to transient interaction state, and AD-10 requires the adapter to emit a target
   `SheetAnchor`. Yet the Minimal Scene Shape does not say the scene carries grid dimensions,
   eligible anchors, anchor ownership, collision constraints, or snap candidates. An adapter that
   receives no raw Spatial facts must either reconstruct the grid, ask another service during the
   gesture, or emit an unsnapped point and let the command service choose. All three produce
   different previews and accepted mutations.

   **Required closure:** assign snap candidate production to compiler or application service and
   define the renderer-neutral contract exposed to the adapter. Specify tie-breaking, out-of-bounds
   behavior, locked targets, occupied anchors, zoom tolerance, and whether preview and committed
   anchor must be identical.

7. **`DiagramEditCommand` is a type name, not a cross-process mutation protocol.**

   AD-10 names two variants but leaves their exact envelope and result contract open. Missing facts
   include scene ID, sheet ID, command ID/idempotency, revision type, occurrence-to-sheet check,
   lock tri-state semantics, anchor encoding, port owner, relationship kind/direction, relationship
   source owner when ports span source units, insertion/formatting policy, validation result,
   diagnostic result, and success publication correlation. `ConnectPorts` can be typed while still
   carrying incompatible relationship-intent models. `MoveOccurrence` can legally clear a lock
   when a client intended to preserve it.

   **Required closure:** define the sealed command and result schema, exact preconditions, mutation
   target resolution, lock transition table, relationship-intent algebra, port-to-engineering
   endpoint mapping, error codes, and command-to-published-scene correlation.

8. **Atomic source mutation and stale-revision behavior are underspecified.**

   The application service must validate, use the workspace undo transaction, recompile, and
   replace the scene only on success. The spine does not define compare-and-set scope, whether the
   expected scene revision also binds source snapshots and asset/package snapshots, behavior when
   source changes without a new valid scene, multi-file transaction rollback, command queueing,
   retries, undo/redo recompilation, or whether formatting-only edits change the revision. A runtime
   service and an LSP-hosted service can both satisfy AD-10 while accepting and rejecting different
   commands.

   **Required closure:** define one application-service owner and transaction boundary; bind the
   expected revision to explicit source/package/compiler inputs; define stale, conflict, validation,
   write, recompile, publication, rollback, undo, and redo state transitions; prohibit silent
   rebase unless explicitly designed.

9. **Source trace lacks the schema needed for navigation and mutation.**

   Inherited M42 AD-31 and M43 AD-5/AD-17 require portable structured LSP-mappable trace. They do
   not define URI normalization, source revision, range convention, primary versus related origins,
   trace roles, single versus multiple spans, or generated-element behavior. Route geometry can
   originate from a project relationship, two ports, and sheet constraints. Labels can originate
   from engineering source while placement comes from sheet source. Frame elements may have only
   sheet-level provenance. One-primary-span trace and multi-origin trace both comply but navigate to
   different files and cannot support the same edit commands.

   **Required closure:** define a normative trace record with source-unit identity, snapshot,
   zero/one-based range convention, role, subject/occurrence/element links, primary navigation
   precedence, related origins, and generated/no-source representation. Define expected traces for
   occurrence, port, route, label, frame, and asset-backed elements.

10. **AD-17 lists tests but supplies no shared cross-unit conformance corpus.**

   Alpha and Beta can each pass schema, ordering, grid, command, trace, SVG, interaction, asset,
   screenshot, and scale tests against self-authored fixtures. The spine identifies
   `examples/m43/rolling-shutter` but does not identify canonical scene JSON, invalid-scene vectors,
   digest vectors, grid-anchor tables, command/result transcripts, source mutations, trace
   expectations, asset manifests, or exact SVG goldens shared by Kotlin and TypeScript. Independent
   green suites therefore prove internal consistency, not interoperability.

   **Required closure:** establish one versioned M43 conformance manifest derived from the active
   example. Require compiler, LSP, generated TypeScript validator, Konva adapter, SVG adapter, and
   E2E to consume the same scene/command/trace/asset vectors. Hand-maintained equivalent fixtures
   must not substitute for the canonical artifacts.

## Tier 1 - High Divergence Risks

11. **Paint facts and style tokens do not form a renderer-neutral rendering contract.**

   Presentation owns paint facts, while AD-12 says text uses compiler-selected style tokens. No
   token catalog, resolved paint record, color space, opacity model, stroke placement, dash rule,
   join/cap rule, clipping rule, fill rule, text alignment, baseline, overflow, or fallback behavior
   is defined. Alpha can emit fully resolved paint. Beta can emit token names interpreted separately
   by Konva and SVG. Both call the values compiler-selected paint facts, but parity is accidental.

   **Required closure:** define closed paint/style records and token-resolution ownership. If tokens
   cross the scene boundary, version and normatively define every token value and renderer
   operation. Include paint conformance vectors, not screenshots alone.

12. **The deterministic SVG oracle has no canonical serialization specification.**

   AD-13 requires canonical element order plus fixed numeric and asset rules, but those rules are
   not present. Attribute order, namespace declarations, ID encoding, transform flattening, path
   normalization, whitespace, line endings, escaping, text nodes, embedded versus referenced
   assets, raster image encoding, font use, and metadata are all open. Two pure serializers can
   produce visually identical deterministic SVG and different canonical bytes.

   **Required closure:** define canonical SVG serialization or designate one committed serializer
   plus exact golden bytes as normative. Specify element/attribute ordering, numeric formatting,
   transforms, assets, text, metadata, encoding, and line endings.

13. **Konva viewport and hit semantics remain behaviorally open.**

   AD-9 assigns one Stage, one hit path, one viewport transform, `ResizeObserver`, DPR, and uniform
   fit. It does not define fit padding, device-pixel rounding, resize behavior after user zoom,
   min/max zoom, wheel/pinch anchor, pan limits, clipping, pointer tolerance in logical versus CSS
   units, transparent-pixel hits, overlapping port/label/route precedence, selection root, or
   drag-cancel behavior. Both units can keep one transform and one hit owner while selecting
   different subjects and producing different screenshots.

   **Required closure:** define a renderer-neutral viewport state and interaction behavior contract,
   including transform formula, rounding, input normalization, hit precedence, selection identity,
   resize transitions, and gesture cancellation. Test the same transcripts against the adapter.

14. **`AssetRef` does not define asset identity, resolution, or port-anchor coordinates.**

   AD-12 requires a stable ref, digest, media kind, view bounds, and compiled port anchors. It does
   not specify ref syntax, package/path normalization, case and symlink handling, digest algorithm,
   digest-before-or-after-sanitization, content delivery to Electron, MIME sniffing, raster color
   profile, SVG `viewBox` normalization, asset-to-scene transform, anchor coordinate frame, cache
   lifetime, or invalidation. Raw-byte and sanitized-byte content addressing both comply and produce
   incompatible caches and revisions.

   **Required closure:** define `AssetRef` and admitted-asset manifest schemas, canonical resource
   resolution, normalization and digest pipeline, asset byte transport, view-bound and port-anchor
   coordinate rules, cache key/invalidation, and renderer error behavior.

15. **The promised sanitized asset profile is not documented.**

   AD-12 rejects scripts, handlers, foreign objects, external URLs, and undeclared resources, but
   that deny list is not a complete SVG/image policy. CSS, `data:` URLs, internal `<use>`, filters,
   masks, clip paths, entity expansion, namespaces, animation, malformed raster metadata, image
   dimensions, decompression limits, and font references remain open. Alpha and Beta can each reject
   every named hazard and admit different visible or unsafe subsets.

   **Required closure:** add one allow-list sanitizer profile with parser, limits, URI policy,
   canonical output behavior, diagnostic codes, and valid/invalid corpus. Bind the profile version
   into scene inputs and digest semantics.

16. **Pinned font bytes do not guarantee shared text geometry.**

   AD-12 pins fonts when metrics affect geometry, but browser Canvas/Konva, JVM layout, and SVG
   consumers can shape, hint, round, baseline-align, and substitute missing glyphs differently.
   The spine does not assign text shaping and line breaking, define font asset loading readiness, or
   say whether glyph positions are compiled facts. A scene with label bounds plus raw text still
   permits overflow and hit-box drift.

   **Required closure:** assign shaping, line breaking, glyph fallback, and metric rounding to one
   layer. Define exact font assets and versions, text run representation, baseline/bounds semantics,
   and adapter behavior before fonts load or glyphs are missing.

17. **Retaining a previous valid scene creates an undefined dual-snapshot product state.**

   AD-7 permits the previous valid scene while showing current diagnostics. It does not say whether
   selection navigation uses old trace against current buffers, whether edits are enabled, which
   revision commands carry, whether source commands may target stale ranges, or how UI distinguishes
   `displayedSceneRevision` from `currentSourceRevision`. Alpha can allow edits against the retained
   scene and reject only at command time. Beta can disable all edits. Both retain an explicitly
   identified scene and show diagnostics.

   **Required closure:** define the invalid-current-source state, the publication envelope carrying
   current input snapshot plus displayed scene revision, navigation policy, command availability,
   stale-trace behavior, and recovery transition.

18. **Product diagnostics have no shared envelope across compile, transport, adapter, and command failures.**

   AD-7, AD-10, AD-16, and Human-First conventions require visible diagnostics naming the exact
   source, subject, problem, and correction. No diagnostic codes, severity, range/trace relation,
   related locations, blocking classification, ordering, lifecycle, or transport channel are
   defined. An adapter schema failure, asset rejection, blocking overlap, and stale command can be
   reported through four unrelated mechanisms while each remains visible and plain-language.

   **Required closure:** define one public diagnostic envelope and required M43 code catalog,
   including source/scene snapshot association, blocking semantics, deterministic order, LSP
   mapping, command-result correlation, and product dismissal/replacement behavior.

19. **The scale decision has measurements but no acceptance decision.**

   AD-14 requires a 100,000-element fixture and named measurements, yet sets no load, memory,
   visible-node, input-latency, or frame-time budget. It does not define element composition,
   viewport, zoom levels, interaction script, warmup, sample count, percentile method, correctness
   checks under culling, or named baseline hardware. Any finite result complies. The trigger for
   future Pixi adoption remains subjective because `accepted product budget` is itself absent.

   Zoom-dependent detail also lacks semantic rules: labels, ports, routes, and hit targets may
   disappear or simplify differently while both adapters claim stable selection and trace.

   **Required closure:** freeze a shared fixture manifest and benchmark protocol, name baseline
   hardware/runtime, set pass/fail budgets, define visible-node accounting and LOD behavior, require
   trace/hit correctness at each zoom, and state the exact gate that admits a future adapter.

## Tier 2 - Governance And Proof Gaps

20. **PNG proof is environment-pinned but not comparison-pinned.**

   AD-13 records environment fields and permits tolerance. It does not choose rasterizer, color
   space, premultiplication, image dimensions, crop, pixel-diff metric, threshold, allowed-pixel
   ratio, antialias mask, or golden update policy. A Konva screenshot and SVG rasterization can both
   be called PNG proof and pass incompatible tolerances.

   **Required closure:** define the PNG producer, full environment manifest, raster parameters,
   comparison algorithm and thresholds, artifact metadata schema, and golden approval policy.

21. **`UPSTREAM-ADOPTION.md` does not fully satisfy AD-15's per-capability evidence contract.**

   The ledger records dispositions, versions/commits, license text, and broad lessons, but not a
   verification result or upgrade policy per row. The generic Upgrade Gate has no evidence paths,
   owner, baseline version, or pass/fail state. `tldraw` records `SEE LICENSE IN LICENSE.md` rather
   than a resolved license conclusion. `react-konva` is explicitly rejected by the spine's design
   choice but has no ledger row. ADAPT rows cite broad ideas such as snapping, culling, and event
   models without identifying the Athena behavior that is retained or rejected. Alpha can follow
   Excalidraw-like snapping while Beta follows tldraw-like selection and both cite the same ledger.

   **Required closure:** add per-entry verification evidence, resolved license conclusion, Athena
   contract affected, exact accepted/rejected behavior, test/gate references, and upgrade owner.
   Record every studied named alternative, including `react-konva`, or state why it is outside the
   ledger's governed set.

22. **The upgrade gate depends on baselines that the spine never defines.**

   The ledger requires deterministic SVG, selection/trace, resize, asset-safety, screenshot, and
   scale checks before dependency changes. Findings 11 through 20 show those checks have no
   normative vectors or acceptance thresholds. An upgrade can therefore pass one unit's local
   interpretation and fail another's without violating the gate.

   **Required closure:** make the Upgrade Gate reference the same canonical scene, interaction,
   asset, trace, SVG, PNG, and scale conformance manifest required for initial M43 delivery. Record
   results beside each adopted dependency version.

## Top Divergence Risks

1. Scene identity and wire contract are not frozen. Kotlin, generated TypeScript, LSP, Konva, SVG,
   cache keys, and commands can all be internally correct and mutually incompatible.
2. Grid-to-anchor lowering and snap ownership are not executable. The first drag story can persist
   a different location from the compiler's preview while every layer claims logical `SceneUnit`.
3. Command/source-trace contracts are not sufficient to select the right file, span, relationship,
   or stale-scene behavior. This risks corrupt source edits, not merely visual drift.
4. Asset, paint, font, and SVG rules do not guarantee cross-renderer parity. One canonical scene can
   still produce different visible and hittable documents.
5. Scale and upgrade gates have no pass/fail values. Evidence can be collected without deciding
   whether the product is acceptable.

## Minimum Closure Before Story Split

Freeze a small normative M43 contract pack before compiler, LSP, Theia, renderer, asset, and scale
stories run independently:

- committed `AthenaDiagramScene` JSON Schema with generated Kotlin/TypeScript boundary checks;
- ID, element topology, revision, canonical ordering, digest, and schema-version rules;
- Projection/Spatial/Presentation ownership matrix and pass contracts;
- exact A1/micro anchor formulas plus renderer-neutral snap contract;
- sealed command/result schema and atomic source transaction state machine;
- portable multi-origin source-trace and public diagnostic schemas;
- resolved paint, text, SVG, asset, and sanitizer profiles;
- canonical cross-language example vectors, SVG/PNG proof rules, and interaction transcripts;
- 100,000-element fixture manifest, benchmark method, product budgets, and LOD rules;
- completed upstream ledger evidence tied to those same conformance gates.

Until those contracts exist, the spine is a coherent direction statement, not a sufficient
convergence spine for independent implementation.
