# `:kernel:compiler`

English | [Chinese (Simplified)](README.zh-CN.md)

The `:kernel:compiler` module is Athena's orchestration core. It exposes the public compiler facade, owns compiler pipeline reporting, coordinates domain semantics, resolves governed knowledge packages, validates external boundary descriptors, derives explicit `Layout IR`, derives explicit `Geometry IR`, derives renderer-neutral `projection-model` documents, publishes the first M9 `DerivedEngineeringContext`, promotes the first M9 `EngineeringCapabilityFacts`, evaluates the first fixed M9 constraint slice, computes the first M9 impact consequences across compiled canonical states, and drives the first geometry-backed downstream backend path.

## Responsibilities

- Expose `AthenaCompiler` with `parse`, `lower`, and `compile` entry points.
- Keep the declared pass order stable: `PARSE -> LOWER -> SEMANTIC_ENRICHMENT -> VALIDATE -> BACKEND_PREPARATION -> BACKEND_EMISSION`.
- Lower syntax-owned source into canonical `Engineering IR`.
- Run generic semantic validation and domain-plugin validation.
- Derive supported `Layout IR` documents from canonical `Engineering IR` plus typed `ViewDefinition` contributions.
- Derive supported `Geometry IR` documents from explicit `Layout IR`.
- Derive supported `ProjectionDocument` outputs from `Geometry IR` plus layout-owned `ViewDefinition`.
- Derive the first governed `DerivedEngineeringContext` snapshot from canonical `Engineering IR`.
- Promote the first governed `EngineeringCapabilityFacts` through a fixed reviewed knowledge pack.
- Evaluate the first governed engineering sufficiency rule slice and emit typed renderer-independent diagnostics.
- Compute deterministic engineering impact consequences from before/after compiled canonical states.
- Consume the stable public SPI from `:kernel:plugins:plugin-api`.
- Consume the approved hosted plugin inventory governed by `:kernel:plugins:plugin-host`.
- Load and resolve governed knowledge packages.
- Load and validate external boundary descriptors.
- Load and validate governed Athena repository-root contracts.
- Derive the runtime viewer model from selected `Geometry IR`.
- Feed selected `Geometry IR` directly into the SVG backend.

## Main Areas

- `AthenaCompiler`: facade and pipeline orchestration.
- `LayoutIrDeriver`: deterministic `Engineering IR -> Layout IR` derivation for supported views.
- `GeometryIrDeriver`: deterministic `Layout IR -> Geometry IR` derivation for supported views.
- `CompilerModels.kt`: public compiler result models.
- `DerivedEngineeringContextDeriver`: narrow compiler-owned derivation from canonical `Engineering IR` to first-wave M9 derived context.
- `EngineeringCapabilityFactPromoter`: fixed knowledge-pack promotion from derived context to first-wave M9 capability facts.
- `EngineeringConstraintEvaluator`: fixed knowledge-pack rule-slice evaluation from capability facts to typed engineering sufficiency results and diagnostics.
- `EngineeringImpactConsequenceCalculator`: deterministic before/after impact evaluation over governed inputs, derived context, capability facts, and constraint results.
- `EngineeringIrLowerer`: syntax-to-IR lowering.
- `plugin/*`: compiler-owned domain coordination only.
- `knowledge/*`: governed knowledge package models, loading, and resolution.
- `boundary/*`: external boundary descriptor models, loading, and resolution.
- `repository/*`: governed repository-root contract loading and layout validation.

## Incremental Refresh Boundary

Story `2.3` adds the first narrow incremental recompute proof for M2:

- Scope is limited to the runtime-owned `connect ports` mutation path.
- Validation, layout, geometry, and downstream rendering each report whether scoped reuse stayed valid or fell back.
- `LayoutIrDeriver` and `GeometryIrDeriver` may reuse unchanged projection objects when the refreshed documents remain structurally stable.
- The compiler stays honest: if a safe scoped merge is not available, the pass reports `FULL_FALLBACK` instead of pretending the refresh stayed incremental.
- The canonical semantic source of truth does not move. Runtime mutates `Engineering IR`; compiler recomputes downstream artifacts from that canonical state.

## Derived Context Boundary

Story `1.2` adds the first narrow M9 derivation proof:

- The compiler publishes `CompilerCompilationSuccess.derivedContext` as Athena-owned kernel/compiler output.
- The compiler publishes `CompilerCompilationSuccess.capabilityFacts` as Athena-owned kernel/compiler output.
- The compiler publishes `CompilerCompilationSuccess.constraintEvaluations` plus separate `engineeringSufficiencyDiagnostics` in the validation breakdown.
- The compiler exposes `AthenaCompiler.calculateImpactConsequences(before, after)` so later runtime and review stories can reuse a typed impact calculation instead of rebuilding it from strings.
- The first proof slice is intentionally narrow and electrical only: motor `power`, `voltage`, `powerFactor`/`pf`, and `efficiency` can be normalized from existing authored `Symbol` or `Text` values.
- The current proof derives deterministic intermediate values such as motor full-load current and thermal load, then promotes fixed current-demand facts, then evaluates a fixed sufficiency slice against authored breaker/cable/relay current inputs.
- The first impact proof compares governed before/after states and publishes affected semantic identities plus short categorized reason labels for changed inputs, derived context, capability facts, and constraint evaluations.
- The current fixed pack then promotes selected meanings such as required protection current, cable current demand, and relay sizing demand.
- Engineering sufficiency stays separate from structural validation: accepted results live in `constraintEvaluations`, while warning/error outcomes are also published as `KNOWLEDGE` diagnostics for later semantic delivery.
- This still stays below SCM review, LSP delivery, and renderer metadata.
- The parser grammar does not widen in this story; quantity parsing is a narrow compiler-owned normalization step.

## M17 Parser Migration Continuity Baseline

M17 hardens the language architecture beneath the compiler so Epic 2's ANTLR4 compiler path cannot cause accidental semantic drift on supported source (AD-106 / AD-110). The continuity contract pinned by `AthenaCompilerTest` and `AthenaParserContinuityTest` is:

- `EngineeringIrLowerer.lower(CompilerSourceDocument)` reads only the authored `SourceFileAst` (via `source.ast` and `AthenaDomainSemanticsCoordinator`), never a generated parse-tree/visitor or Tree-sitter CST type.
- The canonical `EngineeringDocument` identity scheme stays fixed: `system:<name>`, `component:<name>` (with `#<n>` duplicate ordinals), `port:<owner>.<port>`, `connection:<from>-><to>`, plus `SourceProvenance` derived from `SourceSpan`.
- `CompilerPipelineReport.passes` always reports the same six named passes, in order, with the same `CompilerPassExecutionStatus` semantics:

  `PARSE -> LOWER -> SEMANTIC_ENRICHMENT -> VALIDATE -> BACKEND_PREPARATION -> BACKEND_EMISSION`

- The published conformance artifacts (`examples/m0/demo-cabinet.engineering-ir.txt`, `examples/m0/demo-cabinet.svg`) keep matching byte-for-byte.
- The full valid `examples/m0` corpus (`demo-cabinet`, `dual-drive-cabinet`) lowers to the recorded component/port/connection counts and identity scheme, and re-lowers deterministically.

Story 5.1's `AthenaM17ParserParityProofTest` extends the same guarantee to the checked-in `examples/m17` corpus, reusing this identity/provenance definition rather than inventing a divergent one.

## Dependencies

- `:kernel:language`
- `:kernel:plugins:plugin-api`
- `:kernel:plugins:plugin-host`
- `:kernel:validation`
- `:kernel:engineering-model`
- `:kernel:layout-model`
- `:kernel:geometry-model`
- `:kernel:projection-model`
- `:kernel:repository-model`
- `:kernel:svg-renderer`

Test-only dependency:

- `:extensions:domain-electrical`

## Boundaries

This module does not own the DSL grammar itself, the canonical IR schema, the public plugin SPI, hosted plugin source or approval governance, or the concrete Electrical/Runtime domain rules. It orchestrates those pieces while preserving the architecture rule that the DSL is the authored source, `Engineering IR` is the canonical model, `Layout IR` is the first explicit downstream projection layer, `Geometry IR` is the geometry-facing downstream layer, `:kernel:projection-model` is the renderer-neutral graphical projection boundary above runtime/LSP consumers, and renderers remain downstream backends rather than semantic shortcuts.

## Verification

```bash
./gradlew :kernel:compiler:test
```

Windows PowerShell:

```powershell
java25; .\gradlew.bat :kernel:compiler:test
```

