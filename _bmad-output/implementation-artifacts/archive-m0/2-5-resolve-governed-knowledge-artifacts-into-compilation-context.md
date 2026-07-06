---
baseline_commit: 4f7746983d0e3c0f8f1157ec1052b82850f94f70
---

# Story 2.5: Resolve Governed Knowledge Artifacts Into Compilation Context

Status: done

<!-- Note: Validation is optional. Run validate-create-story for quality check before dev-story. -->

## Story

As a platform builder,
I want Athena to resolve approved governed knowledge artifacts into the effective compilation context,
so that compiler behavior can use reviewed knowledge and trace conclusions back to exact artifact versions.

## Acceptance Criteria

1. Given one or more compatible governed knowledge packages, when a compilation run begins, then Athena resolves the active artifacts into the effective compilation context, and the active artifact identities, versions, and provenance remain inspectable.
2. Given diagnostics or derived outcomes influenced by governed knowledge, when those results are reported, then they can reference the responsible governed artifact versions, and incompatible knowledge packages are rejected before they change compiler behavior.

## Tasks / Subtasks

- [x] Add a compiler-owned governed knowledge resolution model above the Story `2.4` package loader. (AC: 1, 2)
  - [x] Define explicit source, candidate, rejected, and active knowledge-context models under a compiler-owned `knowledge` package.
  - [x] Keep resolution distinct from classpath plugin discovery and distinct from authored DSL parsing.
- [x] Resolve local governed knowledge packages into an inspectable active compilation context. (AC: 1)
  - [x] Reuse the Story `2.4` package loader to evaluate local package directories through one compiler-owned resolution path.
  - [x] Produce deterministic active artifact ordering and deterministic rejection ordering for identical local inputs.
- [x] Attach the active governed knowledge context to the compiler without changing declared pass order. (AC: 1, 2)
  - [x] Make the active artifact identities, versions, and provenance visible through compiler-facing result surfaces.
  - [x] Keep the existing `PARSE`, `LOWER`, `VALIDATE`, and `DOWNSTREAM_DERIVATION` schedule intact; do not add a fifth public pass for this story.
- [x] Reject incompatible or invalid governed knowledge packages before they affect compiler behavior. (AC: 2)
  - [x] Surface inspectable rejection diagnostics that identify the responsible artifact and defect.
  - [x] Ensure rejected packages do not enter the active context used by the compilation result.
- [x] Add proof tests for active-context resolution and traceability. (AC: 1, 2)
  - [x] Prove compatible ontology, mapping, and rule packages become active in deterministic order.
  - [x] Prove invalid or incompatible packages are rejected before they change the active context.
  - [x] Prove compiler-facing results expose enough artifact identity/version/provenance information to trace knowledge-influenced behavior.
- [x] Document the M0 governed knowledge resolution boundary and Story `2.5` non-goals. (AC: 1, 2)
  - [x] Clarify how resolution builds on Story `2.4` package loading without yet turning knowledge artifacts into full new semantic rule execution.
  - [x] Clarify that external standards boundary descriptors remain Story `2.6`.

### Review Findings

- [x] [Review][Patch] Add explicit result-level attribution metadata for knowledge-influenced compiler results [compiler/src/main/kotlin/com/engineeringood/athena/compiler/CompilerModels.kt:60]
- [x] [Review][Patch] Restore the declared `DOWNSTREAM_DERIVATION` pass contract to the existing render-result responsibility and output state [compiler/src/main/kotlin/com/engineeringood/athena/compiler/AthenaCompiler.kt:259]
- [x] [Review][Patch] Deduplicate normalized governed knowledge package roots before resolution so one package cannot appear multiple times in active or rejected context [compiler/src/main/kotlin/com/engineeringood/athena/compiler/knowledge/AthenaKnowledgeResolver.kt:19]

## Dev Notes

### Story Intent

- Story `2.5` is the bridge between passive governed knowledge packages and an active inspectable compilation context.
- The proof target is not a full `Knowledge Compiler`, not AI review tooling, and not broad standards semantics. The proof target is that compatible reviewed packages can be resolved deterministically into compiler-visible context and that invalid packages are rejected before affecting behavior.
- Story `2.4` already proved package shape and package validation. Story `2.5` must build on that implementation rather than re-inventing package loading.

### Architecture Guardrails

- Keep the `Knowledge Compiler` path distinct from authored project compilation. Resolution consumes already-reviewed packages; it must not collapse package compilation and project compilation into one workflow.
- `Engineering IR` remains the only canonical semantic authority for project instances in M0. Knowledge resolution may influence interpretation and traceability, but it must not introduce a second durable instance-level semantic model.
- Preserve the declared pass pipeline from Story `1.5`.
  - Do not add, remove, or reorder `PARSE`, `LOWER`, `VALIDATE`, or `DOWNSTREAM_DERIVATION`.
  - If governed knowledge is resolved at compile start, it must happen as compiler-owned pre-pass context setup rather than as a new public pass.
- Keep knowledge resolution separate from plugin discovery.
  - No `ServiceLoader`
  - No classpath artifact activation
  - No reuse of plugin manifests as the knowledge identity surface
- `AutomationML` remains a standards boundary reference only.
  - A standards-mapping package may mention `AutomationML` as governed knowledge.
  - Story `2.5` must not implement `AutomationML` import/export or let it define native internal semantics.

### Technical Requirements

- Extend `com.engineeringood.athena.compiler.knowledge` with a compiler-owned resolution layer above `AthenaKnowledgePackageLoader`.
- Preferred minimal resolution shape for this story:
  - one source of local package roots
  - one deterministic resolver
  - one active knowledge context object attached to the compilation result
  - one rejected-package view with diagnostics
- Reuse the Story `2.4` loader and diagnostics instead of duplicating manifest/payload validation logic.
- Deterministic behavior is mandatory.
  - identical local package inputs must produce the same active ordering, rejection ordering, and compiler-facing context
  - choose one explicit stable ordering rule such as artifact id, artifact version, then package root
- The compiler-facing result should make active knowledge inspectable.
  - likely through a new field on the public compilation result surface or an adjacent compiler context model
  - must expose at minimum artifact id, artifact version, artifact kind, and provenance
- This story may introduce compiler-facing knowledge-resolution diagnostics or metadata, but it should stay minimal.
  - Do not invent a large rule engine
  - Do not retrofit broad domain semantics onto governed knowledge yet
  - Do not require renderer changes unless a traceability surface absolutely depends on them
- The simplest acceptable M0 proof for AC 2 is:
  - rejected packages are visible and blocked from the active context
  - knowledge-influenced compiler-facing results can identify the responsible active artifact versions
  - no invalid package silently changes operational behavior

### Architecture Compliance

- Align to AD-1 by keeping resolution local, deterministic, JVM-first, and single-process.
- Align to AD-2 by treating governed ontology additions, standards mappings, and rule artifacts as extension material rather than hard-coded core vocabulary.
- Align to AD-3 by keeping `Engineering IR` canonical while letting governed knowledge act only through explicit compiler-owned context.
- Align to AD-5 by keeping governed knowledge extensible but non-sovereign through core-owned contracts and result surfaces.
- Align to AD-6 by preserving the difference between filesystem-governed knowledge resolution and classpath plugin activation.

### Library / Framework Requirements

- Stay on Java `25`, Kotlin `2.4.0`, and Gradle `9.6.1`.
- Preserve the package root `com.engineeringood.athena`.
- Keep KDoc on all new core Kotlin classes and interfaces added for governed knowledge resolution.
- Reuse the current Kotlin/JUnit stack and the deterministic validation style already established in Stories `2.2` through `2.4`.
- Do not add databases, archive libraries, DI containers, or third-party serialization frameworks for this story.

### File Structure Requirements

- Expected primary touch points:
  - `compiler/src/main/kotlin/com/engineeringood/athena/compiler/knowledge/**`
  - `compiler/src/main/kotlin/com/engineeringood/athena/compiler/**`
  - `compiler/src/test/kotlin/com/engineeringood/athena/compiler/**`
  - `compiler/src/test/resources/knowledge-packages/**`
  - `docs/compiler/**`
- Current file state to preserve:
  - [AthenaKnowledgePackageLoader.kt](../../../kernel/compiler/src/main/kotlin/com/engineeringood/athena/compiler/knowledge/AthenaKnowledgePackageLoader.kt:1) already owns local directory package loading and validation. Story `2.5` should layer resolution above it rather than replacing it.
  - [AthenaKnowledgePackageModel.kt](../../../kernel/compiler/src/main/kotlin/com/engineeringood/athena/compiler/knowledge/AthenaKnowledgePackageModel.kt:1) already defines artifact, payload, provenance, and load-result models. Extend this surface carefully instead of creating parallel package identities.
  - [AthenaCompiler.kt](../../../kernel/compiler/src/main/kotlin/com/engineeringood/athena/compiler/AthenaCompiler.kt:1) already owns plugin discovery state, canonical lowering, semantic validation, and pass reporting. Story `2.5` should attach knowledge context without redesigning the compiler facade or pass sequence.
  - [CompilerModels.kt](../../../kernel/compiler/src/main/kotlin/com/engineeringood/athena/compiler/CompilerModels.kt:1) already defines the public compiler result surface. If active governed knowledge needs to be inspectable, this is one likely touch point.
  - [docs/compiler/m0-knowledge-package-boundary.md](../../../docs/compiler/m0-knowledge-package-boundary.md:1) already documents Story `2.4` package shape and non-goals. Story `2.5` should extend the boundary docs rather than contradict them.
  - [docs/compiler/m0-pass-pipeline.md](../../../docs/compiler/m0-pass-pipeline.md:1) declares the public compiler schedule. Preserve it.
- Files that should stay semantically unchanged in Story `2.5` unless a direct need is proven:
  - parser/tokenization behavior under `language`
  - canonical IR data model under `ir`
  - current domain plugin semantics in `domain-electrical-runtime`
  - render-model derivation and `SVG` emission behavior
  - plugin manifest/discovery behavior under `compiler.plugin`
- Keep governed knowledge fixtures separate from `examples/`.
  - `examples/` remains authored DSL conformance input
  - knowledge packages remain test or governed-input artifacts, not user-authored project examples

### Testing Requirements

- Minimum verification should include sequential Java `25` runs:
  - `java25; .\\gradlew.bat --no-daemon --console=plain :compiler:test`
  - `java25; .\\gradlew.bat --no-daemon --console=plain build`
  - `java25; .\\gradlew.bat --no-daemon --console=plain test`
- Required tests:
  - compatible ontology, mapping, and rule packages resolve into deterministic active context order
  - invalid or incompatible packages are rejected before entering active context
  - compiler-facing results expose active artifact id/version/provenance for inspection
  - pass ordering remains `PARSE`, `LOWER`, `VALIDATE`, `DOWNSTREAM_DERIVATION`
  - no invalid package silently changes operational behavior
- Preserve the repository rule: keep Gradle verification sequential only on Windows. Do not run `build` and `test` in parallel in this repo.

### Previous Story Intelligence

- Story `2.4` completed the governed knowledge package substrate and is currently in `review`.
- Story `2.4` added:
  - `AthenaKnowledgePackageLoader`
  - governed knowledge artifact/package/provenance models
  - stable package validation diagnostics
  - valid and invalid package fixtures
  - `m0-knowledge-package-boundary.md`
- Story `2.4` explicitly deferred:
  - active package resolution into `AthenaCompiler`
  - compiler-facing knowledge context exposure
  - knowledge-driven runtime behavior changes
- Story `2.5` should add exactly those deferred items, but it still must not:
  - invent a full standards execution engine
  - introduce external-system boundary descriptors
  - collapse knowledge resolution into plugin activation

### Git Intelligence Summary

- Current recent commits are:
  - `4f77469 Add Story 2.1 plugin contract design spec`
  - `ae76b71 Add manifesto submodule`
  - `1e0719f Initial commit`
- Practical implementation guidance should come from the current working tree and the completed Story `2.4` artifact rather than from historical commit patterns.

### Project Structure Notes

- No `project-context.md` file was found in the repository.
- No UX artifact exists for this phase. Keep the story compiler-first and boundary-first.
- Epic 2 sequence remains load-bearing:
  - Story `2.4` package shape and validation
  - Story `2.5` active resolution into compilation context
  - Story `2.6` external boundary descriptors
- This story should give the compiler an inspectable governed knowledge context without claiming that governed knowledge already owns broad semantic execution.

### References

- `_bmad-output/planning-artifacts/epics.md` - Epic 2, Story `2.5` acceptance criteria and FR mapping.
- `_bmad-output/planning-artifacts/prds/prd-Athena-2026-07-02/prd.md` - FR `7`, FR `8`, separate knowledge path, reusable artifact, compatibility, and provenance expectations.
- `_bmad-output/specs/spec-athena/SPEC.md` - M0 constraints, typed local extension posture, and non-goals.
- `_bmad-output/planning-artifacts/architecture/architecture-Athena-2026-07-02/ARCHITECTURE-SPINE.md` - AD-1 through AD-6 and the JVM-first typed-extension substrate.
- `_bmad-output/planning-artifacts/implementation-readiness-report-2026-07-02.md` - Epic 2 sequencing and FR coverage for Stories `2.4` through `2.5`.
- `_bmad-output/implementation-artifacts/archive-m0/2-4-define-governed-knowledge-artifact-packages.md` - current governed knowledge package implementation, diagnostics, and explicit deferrals.
- `compiler/src/main/kotlin/com/engineeringood/athena/compiler/knowledge/AthenaKnowledgePackageLoader.kt` - current package loading boundary to build on.
- `compiler/src/main/kotlin/com/engineeringood/athena/compiler/knowledge/AthenaKnowledgePackageModel.kt` - current package and provenance model surface to extend.
- `docs/compiler/m0-knowledge-package-boundary.md` - Story `2.4` boundary and non-goals.
- `docs/compiler/m0-pass-pipeline.md` - declared pass order that Story `2.5` must preserve.
- `manifesto/docs/architecture/01-compiler.md` - compiler input model, standards mapping, and `Engineering Compiler` vs `Knowledge Compiler` separation.
- `manifesto/docs/architecture/02-ontology.md` - standards mapping, governed ontology evolution, and review/provenance requirements.
- `manifesto/docs/rfc/RFC-0008-knowledge.md` - knowledge path scope and evidence/review concerns.

## Dev Agent Record

### Implementation Plan

- Add a compiler-owned governed knowledge source, candidate, active, rejected, and compilation-context model above the Story `2.4` package loader.
- Resolve reviewed local package roots through a deterministic resolver that reuses loader diagnostics and enforces runtime compatibility before activation.
- Attach the resolved governed knowledge context to compiler-facing compilation results without introducing a fifth public pass.
- Add proof fixtures/tests for deterministic activation, rejected-package traceability, and unchanged compiler behavior when incompatible packages are present.
- Extend the compiler docs to describe the governed knowledge resolution boundary and its Story `2.5` non-goals.

### Debug Log

- Red phase: `java25; .\\gradlew.bat --no-daemon --console=plain :compiler:test --tests "com.engineeringood.athena.compiler.AthenaKnowledgeResolverTest" --tests "com.engineeringood.athena.compiler.AthenaCompilerTest"` failed with unresolved references for `AthenaKnowledgePackageSource`, `AthenaKnowledgeResolver`, the `knowledgePackageSource` compiler seam, and `knowledgeContext` result surfaces.
- Green phase: added the governed knowledge resolver/context models, attached `knowledgeContext` to compiler compilation results, and introduced a stable rejected-package ordering rule that places identified artifacts ahead of anonymous malformed packages.
- Verification phase: sequential Java `25` runs completed successfully for `:compiler:test`, `build`, and `test`.

### Completion Notes

- Implemented `AthenaKnowledgePackageSource`, `AthenaKnowledgeCandidatePackage`, `AthenaActiveKnowledgeArtifact`, `AthenaRejectedKnowledgePackage`, `AthenaCompilationKnowledgeContext`, and `AthenaKnowledgeResolver` under `compiler.knowledge` with KDoc on each core type.
- `AthenaCompiler.compile(...)` now resolves reviewed governed knowledge as compiler-owned pre-pass setup and exposes the resulting `knowledgeContext` on both successful and parse-failed compilation results without changing the declared public pass order.
- Added an `incompatible-core` governed knowledge fixture plus resolver/compiler tests proving deterministic active ordering, inspectable rejection diagnostics, active artifact provenance visibility, and unchanged compiler behavior when incompatible packages are present.
- Updated `docs/compiler/m0-knowledge-package-boundary.md` and `docs/compiler/m0-pass-pipeline.md` to document the active resolution boundary, pre-pass setup, deterministic ordering, and Story `2.5` non-goals.
- Verified with:
  - `java25; .\\gradlew.bat --no-daemon --console=plain :compiler:test`
  - `java25; .\\gradlew.bat --no-daemon --console=plain build`
  - `java25; .\\gradlew.bat --no-daemon --console=plain test`

### Story Completion Status

- Status: done
- Completion note: Governed knowledge now resolves into an inspectable compiler-owned compilation context with deterministic active/rejected ordering, explicit compiler-facing attribution metadata, restored public pass contract semantics, and sequential Java `25` verification complete.

## File List

- `compiler/src/main/kotlin/com/engineeringood/athena/compiler/AthenaCompiler.kt`
- `compiler/src/main/kotlin/com/engineeringood/athena/compiler/CompilerModels.kt`
- `compiler/src/main/kotlin/com/engineeringood/athena/compiler/knowledge/AthenaKnowledgeResolutionModel.kt`
- `compiler/src/main/kotlin/com/engineeringood/athena/compiler/knowledge/AthenaKnowledgeResolver.kt`
- `compiler/src/test/kotlin/com/engineeringood/athena/compiler/AthenaCompilerTest.kt`
- `compiler/src/test/kotlin/com/engineeringood/athena/compiler/AthenaKnowledgeResolverTest.kt`
- `compiler/src/test/resources/knowledge-packages/incompatible-core/athena-knowledge.properties`
- `compiler/src/test/resources/knowledge-packages/incompatible-core/payload/future-only-rule.txt`
- `docs/compiler/m0-knowledge-package-boundary.md`
- `docs/compiler/m0-pass-pipeline.md`

## Change Log

- 2026-07-03: Implemented Story `2.5` governed knowledge resolution into compilation context, added deterministic resolver/test fixtures, exposed compiler-facing knowledge traceability, and documented the M0 boundary/non-goals.

