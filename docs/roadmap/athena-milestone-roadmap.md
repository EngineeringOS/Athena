# Athena Milestone Roadmap

## Purpose

This note records the active Athena milestone sequence after the completed M14 proof.

Its job is to keep milestone boundaries aligned with what the repository actually shipped, so later planning does not drift back to older draft-era meanings.

## Authority

This roadmap follows the completed milestone PRDs, architecture spines, implementation summaries, and retrospectives through M14.

That means:

- M5 is complete as the repository and package-graph milestone.
- M6 is complete as the semantic SCM milestone.
- M7 is complete as the first graphical projection and visual workbench milestone.
- M8 is complete as the unified semantic mutation milestone.
- M9 is complete as the first engineering knowledge-runtime milestone.
- M10 is complete as the AI-assisted reasoning milestone above governed knowledge outputs.
- M11 is complete as the first serious electrical ECAD workbench-depth milestone.
- M12 is complete as the renderer-trust and operator-density hardening milestone.
- M13 is complete as the presentation-language foundation milestone.
- M14 is complete as the component-knowledge foundation milestone.
- M15 is the next recommended milestone for mainstream guided authoring above the completed kernel and workbench foundations.

Older draft notes that assigned milestones only by UI polish, AI novelty, or unresolved framework exploration should now be treated as background only, not as the active delivery contract.

## Milestone Sequence

| Milestone | Status | Proven outcome |
| --- | --- | --- |
| M0 | done | DSL -> AST -> Engineering IR -> validation -> SVG |
| M1 | done | runtime-owned workspace, graph, command, history, diff, plugin-hosted execution |
| M2 | done | Layout IR, Geometry IR, projection sessions, multi-view desktop proof |
| M3 | done | stable plugin API, hosted plugin platform, external proof domains |
| M4 | done | Theia desktop shell, repository session, Athena LSP, professional workbench, semantic inspection |
| M5 | done | governed repository contract, deterministic package graph, canonical lock, repository graph session, package-aware IDE operation |
| M6 | done | semantic baseline, semantic diff, review/commit/history, runtime/LSP/Theia semantic SCM panel |
| M7 | done | projection model, runtime-owned projection sessions, graph adapter, graph-first workbench, first renderer proof |
| M8 | done | unified mutation authority, graph semantic and projection mutation proofs, shared review, shared reveal, published proof corpus |
| M9 | done | derived engineering context, capability facts, governed electrical knowledge pack, sufficiency diagnostics, impact-aware review |
| M10 | done | AI-assisted reasoning over governed semantic and knowledge outputs with deterministic proof paths |
| M11 | done | richer electrical projection families, sheet model, notation packs, repeated references, dense electrical proof repository, first serious ECAD workbench depth |
| M12 | done | electrical renderer trust, viewport hardening, repeated-reference reveal, theme-relative operator density, and workbench reliability |
| M13 | done | Presentation IR, primitive and composite electrical presentation packs, backend abstraction, and traceable downstream rendering |
| M14 | done | governed component knowledge resolution, typed semantic ports, minimal physical traits, vendor implementation mapping, and runtime/LSP publication |
| M15 | next | guided mainstream authoring foundation above M8 mutation authority and M14 component knowledge |

## Completed Milestone Notes

### M5 - Repository And Package Graph

M5 is complete.

Proven outcome:

- one canonical repository-root `athena.yaml`
- one canonical derived `athena.lock`
- one VCS-neutral `:kernel:repository-model` boundary
- deterministic local-first package resolution and lock validation
- one runtime-owned `RepositoryGraphSession` per product window
- Athena IDE package diagnostics, repository graph feedback, and narrow `.athena` editor hardening

Supporting backlog carried forward after M5:

- later semantic token work
- hover, rename, and formatting beyond the narrow M5 slice
- parser-evolution watchpoint for the Athena language front-end if the current hand-rolled parser becomes a bottleneck
- frontend regression harness for Athena-owned Theia surfaces
- backend transport regression proof when version-sensitive IDE transport logic changes

What M5 did not become:

- semantic SCM
- graphical projection milestone
- UI-polish-only milestone
- Git abstraction or source-control vendor abstraction inside `repository-model`

### M6 - Semantic SCM

M6 is complete.

Proven outcome:

- a VCS-neutral semantic SCM boundary above vendor storage mechanics
- semantic diff, review, commit-intent, and publish-oriented history flows
- runtime-owned semantic SCM state exposed through Athena LSP
- Theia semantic SCM inspection as a downstream product surface

### M7 - Graphical Projection And Visual Workbench

M7 is complete.

Proven outcome:

- dedicated projection protocol and server boundary under runtime and `ide/lsp`
- graph-first split workbench with source and graphical view visible together
- infinite-canvas-style diagram surface and professional engineering-workbench density
- inspect-first graphical review and navigation with transient frontend state by default
- layout and presentation state kept downstream of canonical semantic state
- explicit first graphical technology path recorded instead of left open-ended

What M7 did not become:

- a replacement for the Athena LSP path
- canvas-owned engineering truth
- a bypass around repository/package or semantic SCM contracts
- full bidirectional graphical authoring
- final QElectroTech or EPLAN-class domain depth in one step

Current architecture decision record:

- [`_bmad-output/planning-artifacts/architecture/architecture-Athena-2026-07-09-m7/GRAPHICAL-TECHNOLOGY-DECISION.md`](../../_bmad-output/planning-artifacts/architecture/architecture-Athena-2026-07-09-m7/GRAPHICAL-TECHNOLOGY-DECISION.md)

### M8 - Unified Semantic Mutation Model

M8 is complete.

Proven outcome:

- one runtime-owned mutation authority above source and graph
- explicit mutation categories for semantic mutation, projection mutation, and transient interaction
- one real graph semantic mutation path through Athena command intent
- one real graph projection mutation path through governed runtime-owned placement metadata
- one shared semantic review model for accepted source and graph consequences
- one canonical reveal path across source, graph, and semantic SCM surfaces
- published proof corpus and repeatable verification path for the finished mutation milestone

Important scope boundary:

- the current source-originated path is still preview-first mutation evaluation plus shared review and reveal coherence
- M8 does not yet prove full canonical write-through source editing

Primary published records:

- [`docs/usages/m8-proof-usage.md`](../usages/m8-proof-usage.md)
- [`_bmad-output/implementation-artifacts/m8/milestone-summary-2026-07-11.md`](../../_bmad-output/implementation-artifacts/m8/milestone-summary-2026-07-11.md)
- [`_bmad-output/implementation-artifacts/m8/m8-retrospective-2026-07-11.md`](../../_bmad-output/implementation-artifacts/m8/m8-retrospective-2026-07-11.md)

### M12 - Renderer Trust And Operator Density

M12 is complete.

Proven outcome:

- governed anchor, routing-corridor, and endpoint-selection coherence across graph and canonical semantic ids
- conductor-first graph edge rendering with terminal-aware selection
- resize-safe fit, pan, zoom, and compact graph-control behavior
- denser Athena-owned panel language that stays inside the current IDE shell
- IDE-theme-relative cabinet and wiring surface tokens instead of hardcoded graph skin colors
- bottom-right zoom dock plus cleaner canvas-first control placement on the graphical workbench
- first repeated-reference reveal and related-subject navigation in the graph overlay
- a larger governed benchmark repository under `examples/m12/renderer-benchmark-proof`

Boundary:

- M12 improved renderer trust and operator density without making renderer output the semantic authority
- M12 did not claim final IEC-grade parity, final skin or emotion architecture, or unrestricted ECAD authoring depth

Primary published records:

- [`_bmad-output/implementation-artifacts/m12/milestone-summary-2026-07-12.md`](../../_bmad-output/implementation-artifacts/m12/milestone-summary-2026-07-12.md)
- [`_bmad-output/implementation-artifacts/m12/m12-retrospective-2026-07-12.md`](../../_bmad-output/implementation-artifacts/m12/m12-retrospective-2026-07-12.md)

### M13 - Presentation Language Foundation

M13 is complete.

Proven outcome:

- new domain-neutral `Presentation IR` contracts in `kernel/presentation-model`
- compiler derivation from projection-owned sheet, notation, anchor, endpoint, and routing contracts
- first extension-compatible electrical primitive presentation pack
- first extension-compatible electrical composite presentation pack
- family-specific composite variants without canonical-identity drift
- runtime and `ide/lsp` delivery of presentation snapshots
- GLSP and Theia workbench consumption of `diagram.presentation`
- backend abstraction above `Presentation IR`
- preserved canonical traceability across presentation occurrences, review surfaces, knowledge surfaces, and AI-context seams

Boundary:

- M13 proved presentation as a governed downstream language
- M13 did not claim semantic macro authoring, broad multi-domain parity, or final renderer-performance architecture

Primary published records:

- [`_bmad-output/implementation-artifacts/m13/milestone-summary-2026-07-12.md`](../../_bmad-output/implementation-artifacts/m13/milestone-summary-2026-07-12.md)
- [`_bmad-output/implementation-artifacts/m13/m13-retrospective-2026-07-12.md`](../../_bmad-output/implementation-artifacts/m13/m13-retrospective-2026-07-12.md)

### M14 - Component Knowledge Foundation

M14 is complete.

Proven outcome:

- new vendor-neutral component contracts in `kernel/component-model`
- new vendor implementation mapping contracts in `kernel/part-model`
- new typed semantic-port contracts in `kernel/connection-model`
- new minimal physical-trait contracts in `kernel/physical-model`
- deterministic governed component-resolution models in `kernel/compiler`
- compiler-owned component knowledge context threaded into later M9-facing inputs
- projection and presentation downstream evidence for resolved component identity and minimal physical traits
- runtime and `ide/lsp` publication of resolved component knowledge through existing seams
- first narrow Siemens-first electrical proof slice in `extensions/domain-electrical`
- explicit product position that DSL is canonical serialization, not the mandatory mainstream default UI

Boundary:

- M14 proved governed component knowledge above canonical `Engineering IR` and below later reasoning and presentation consumers
- M14 did not claim broad multi-vendor catalog parity, rich rule authoring, or final mainstream authoring UX

Primary published records:

- [`_bmad-output/implementation-artifacts/m14/milestone-summary-2026-07-14.md`](../../_bmad-output/implementation-artifacts/m14/milestone-summary-2026-07-14.md)
- [`_bmad-output/implementation-artifacts/m14/m14-retrospective-2026-07-14.md`](../../_bmad-output/implementation-artifacts/m14/m14-retrospective-2026-07-14.md)
- [`docs/usages/m14-proof-usage.md`](../usages/m14-proof-usage.md)

## Practical Backlog Placement

| Concern | Belongs to |
| --- | --- |
| syntax highlighting | M5 supporting backlog |
| semantic tokens | M5 supporting backlog |
| hover, rename, formatting | M5 supporting backlog |
| parser stack evolution (`hand-rolled` -> `ANTLR` / `tree-sitter` / other) | kernel-language watchpoint; plan explicitly when DSL complexity or IDE tooling pressure justifies it |
| richer read-only semantic inspection | M5 or M6 supporting backlog |
| SCM abstraction above Git or vendor storage | M6 core |
| semantic diff and semantic review | M6 core |
| real diagram or GLSP-class projection | M7 core |
| unified mutation authority across source and graph | M8 core |
| broader governed bidirectional authoring | post-M8; keep under one mutation authority |
| first governed engineering knowledge runtime | M9 core |
| AI-assisted reasoning over governed knowledge | M10 core |
| first serious electrical ECAD workbench depth | M11 core |
| renderer trust, viewport hardening, operator density | M12 core |
| presentation language foundation, primitive and composite packs | M13 core |
| component knowledge resolution, typed ports, physical traits, part mapping | M14 core |
| mainstream guided authoring through palette, inspector, forms, and connection assistance | M15 core |
| broad multi-vendor catalog parity | post-M15 milestone |
| richer behavior, simulation, or company-standard knowledge ecosystems | later than M15 |

## Cross-Cutting Technical Watchpoints

### Parser Evolution

Athena currently uses a deliberately small hand-rolled parser, which was the correct fit for the original M0 proof and remains acceptable for the current repository and IDE scope.

That does not mean the parser choice is frozen forever.

If future language growth requires stronger grammar tooling, better incremental parsing, richer editor tooling, or broader syntax-error recovery, the parser stack should be reviewed explicitly as a kernel-language decision.

Important boundary:

- this is not automatically M6, because semantic SCM is downstream of language meaning
- this is not automatically M7 or M8, because graphical projection and unified mutation are also downstream of language meaning
- this is not automatically M14 or M15, because component knowledge and guided authoring both sit above canonical authored meaning
- it should be planned only when there is concrete pressure from DSL complexity, IDE tooling, or compiler maintenance cost

## Planning Rule

When a backlog item is discussed, ask:

1. Does it freeze repository and package meaning?
2. Does it freeze semantic history and review meaning?
3. Does it introduce graphical projection?
4. Does it freeze one mutation authority across source and graph?
5. Does it define executable engineering knowledge above canonical state?
6. Does it define a downstream presentation language?
7. Does it define governed component knowledge above canonical state and below later consumers?
8. Does it define mainstream guided authoring over the existing mutation authority?

If the answer is:

- `1` -> M5
- `2` -> M6
- `3` -> M7
- `4` -> M8
- `5` -> M9
- `6` -> M13
- `7` -> M14
- `8` -> M15

If it is only IDE usability polish, attach it to the nearest active milestone as supporting backlog instead of replacing the milestone core.

## Post-M14 Carry Forward

The next milestone should build on the completed M14 proof instead of reopening kernel identity or package governance.

Primary carry-forward items:

- governed component palette and catalog views derived from active component-knowledge packs
- inspector or form-driven component editing so mainstream users are not forced into raw DSL authoring
- typed port-aware connection assistance driven by M14 semantic-port contracts
- placement and edit flows that still converge through M8 mutation authority
- reviewable source, graph, and semantic consequences for guided authoring actions
- product proof that DSL remains canonical serialization while graph, forms, templates, AI, and API become valid mainstream entry surfaces

Important boundary:

- M15 should not become final EPLAN parity
- M15 should not become broad catalog ingestion
- M15 should not create a second mutation path outside M8
- M15 should not move semantic truth into frontend widgets, palette state, or renderer code

## Primary Records

- [`_bmad-output/implementation-artifacts/m12/README.md`](../../_bmad-output/implementation-artifacts/m12/README.md)
- [`_bmad-output/implementation-artifacts/m13/README.md`](../../_bmad-output/implementation-artifacts/m13/README.md)
- [`_bmad-output/implementation-artifacts/m14/README.md`](../../_bmad-output/implementation-artifacts/m14/README.md)
- [`_bmad-output/planning-artifacts/prds/prd-Athena-2026-07-13-m14/prd.md`](../../_bmad-output/planning-artifacts/prds/prd-Athena-2026-07-13-m14/prd.md)
