---
baseline_commit: ae76b71c58bb036f1367e96608aaee7eac213dac
---

# Story 1.4: Validate Engineering IR And Emit Provenance-Rich Diagnostics

Status: done

<!-- Note: Validation is optional. Run validate-create-story for quality check before dev-story. -->

## Story

As a reviewer,
I want Athena to validate canonical `Engineering IR` for references, types, ports, and connections,
so that semantic defects are detected in one authoritative model and can be traced back to their authored origin.

## Acceptance Criteria

1. Given a valid `Engineering IR` produced from M0 authored input, when the semantic validation pass executes, then it verifies references, declared types, port compatibility, and allowed connections against the active domain rules, and it marks the IR as semantically valid for downstream compilation.
2. Given an `Engineering IR` containing unresolved references, incompatible types, invalid ports, or illegal connections, when the semantic validation pass executes, then it emits diagnostics that identify the failing semantic object and rule category, and each diagnostic includes enough provenance to trace the issue back to source locations, IR identities, and the responsible validation rule.
3. Given a compilation run with one or more semantic diagnostics, when validation completes, then the compiler does not report semantic success for that input, and downstream rendering or derived output steps run only according to the declared pipeline policy for invalid semantic state.

## Tasks / Subtasks

- [x] Define the first semantic validation contracts and provenance-rich diagnostic types under `semantics-core` and/or `compiler`. (AC: 1, 2, 3)
  - [x] Model diagnostic severity, rule identity/category, affected semantic identity, and provenance without reintroducing AST authority as a second semantic substrate.
  - [x] Keep the contracts general enough for later rule/plugin extension and downstream inspection surfaces.
- [x] Implement deterministic validation over canonical `Engineering IR` for the current M0 Electrical/Runtime slice. (AC: 1, 2)
  - [x] Validate that component and port references resolve uniquely and report unresolved or ambiguous authored references as semantic diagnostics.
  - [x] Validate duplicate authored semantic keys using the fallback identities introduced in Story `1.3` instead of changing lowering behavior.
  - [x] Validate the minimum M0 authored property and compatibility rules needed for the current example wedge: declared device type, valid port direction, compatible connection direction, and signal/type compatibility where the current IR carries enough data to decide.
- [x] Expose validation results through the compiler-facing entry path and gate downstream semantic success according to explicit policy. (AC: 1, 3)
  - [x] Extend the compiler facade so callers can distinguish parse success, lowering success, and semantic validity while still inspecting canonical IR and diagnostics from one entry path.
  - [x] Do not report semantic success when error-level diagnostics exist; make invalid-state continuation or stop policy explicit enough for Story `1.5` pipeline work.
- [x] Add deterministic tests and fixtures for valid validation success, invalid semantic diagnostics, and invalid-state pipeline gating. (AC: 1, 2, 3)
  - [x] Assert diagnostics include source provenance, IR identity, and rule/category data.
  - [x] Assert valid input is marked semantically valid and invalid input is not reported as semantic success.
  - [x] Assert unresolved, ambiguous, and incompatible cases remain deterministic across repeated runs.
- [x] Document the first M0 validation boundary, diagnostic schema, and invalid-state continuation policy. (AC: 1, 2, 3)

### Review Findings

- [x] [Review][Patch] Diagnose duplicate authored connection keys, not only duplicate components and ports [semantics-core/src/main/kotlin/com/engineeringood/athena/semantics/core/EngineeringIrValidator.kt:102]
- [x] [Review][Patch] Treat text-valued `type` and `direction` properties as invalid rather than missing [semantics-core/src/main/kotlin/com/engineeringood/athena/semantics/core/EngineeringIrValidator.kt:230]

## Dev Notes

### Story Intent

- Story `1.4` is the first real semantic validation pass over canonical `Engineering IR`.
- The proof target is not “add some checks.” The proof target is that semantic defects are detected only after lowering, inside one canonical semantic model, with enough evidence for a reviewer to understand what failed and why.
- Validation must consume the IR produced by Story `1.3` exactly as it exists now: with stable identities, explicit references, reference provenance, and duplicate fallback identities for semantically invalid duplicates.

### Architecture Guardrails

- `Engineering IR` remains the only canonical semantic authority in M0. Validation must never drift back into AST-driven semantic decisions.
- Validation and lowering remain distinct passes. Do not repair, reinterpret, or re-lower authored intent inside the validation pass.
- Diagnostics are part of the semantic contract. They must be explainable through semantic objects, provenance, and rule identity, not through hidden implementation state.
- Rendering and downstream derivation remain downstream concerns. Story `1.4` may define invalid-state continuation policy, but it must not pull renderer-facing logic into semantic validation.
- Keep M0 JVM-first, local, and single-process. Validation stays inside the current Kotlin/JVM compiler workspace.

### Technical Requirements

- Implement shared validation contracts primarily under `semantics-core/`.
- Implement validation orchestration and compiler-facing result wiring primarily under `compiler/`.
- Modify `ir/` only if validation requires additional canonical semantic or diagnostic support structures that truly belong with the semantic model.
- The first validation rule surface should stay intentionally small and deterministic. Minimum expected rule categories for M0 are:
  - unresolved or ambiguous component ownership for ports
  - unresolved or ambiguous connection endpoints
  - duplicate authored semantic keys represented by Story `1.3` fallback identities
  - missing or invalid device `type`
  - missing or invalid port `direction`
  - illegal connection direction and incompatible signal/type pairing when the current IR carries enough information to decide
- Keep duplicate lowering permissive. Story `1.3` already chose to preserve duplicates structurally with distinct fallback identities. Story `1.4` must diagnose those cases, not change the lowering contract.
- Preserve provenance end-to-end. Validation diagnostics should reference the most relevant source provenance already carried in IR rather than requiring new AST walks for authority.
- Make invalid-state continuation explicit. The compiler should surface whether semantic validation succeeded and whether later passes are allowed to continue, without forcing Story `1.5` to invent a second result model.
- Do not introduce a rule engine framework, graph database, or plugin execution mechanism in this story. Keep the first validation path local and explicit.

### Architecture Compliance

- Align to AD-1 by keeping validation inside the single JVM compiler process.
- Align to AD-2 by keeping the semantic contract general even though M0 validates the first Electrical/Runtime wedge.
- Align to AD-3 by running semantic validation only over canonical `Engineering IR`.
- Align to AD-4 by keeping validation independent from layout, geometry, and renderer-facing concerns.
- Align to AD-7 by treating invalid examples and expected diagnostic outcomes as conformance artifacts rather than incidental test data.

### Library / Framework Requirements

- Stay on the pinned workspace stack from Story `1.1`: Java `25`, Kotlin `2.4.0`, Gradle `9.6.1`.
- Use ordinary Kotlin data classes, sealed hierarchies, and value objects for diagnostics and validation results.
- Preserve the package root `com.engineeringood.athena`.
- Reuse the existing Kotlin test setup already established in the workspace. Do not introduce a second test framework.
- Do not introduce an external validation DSL, rule engine, ANTLR change, or serialization-heavy diagnostics infrastructure.

### File Structure Requirements

- Expected primary touch points:
  - `semantics-core/src/main/kotlin/com/engineeringood/athena/semantics/core/**`
  - `semantics-core/src/test/kotlin/com/engineeringood/athena/semantics/core/**`
  - `compiler/src/main/kotlin/com/engineeringood/athena/compiler/**`
  - `compiler/src/test/kotlin/com/engineeringood/athena/compiler/**`
  - `ir/src/main/kotlin/com/engineeringood/athena/ir/**` only if semantic or diagnostic support truly belongs in the canonical model
  - `examples/**` for valid and invalid conformance fixtures
- `language/` should remain syntax-owned. Do not expand parser behavior here unless a validation test proves a direct syntax-contract blocker.
- Keep validation-facing types out of `renderer-svg/` and avoid plugin-discovery work in this story.
- Do not modify generated `build/` outputs or treat them as source files.

### Testing Requirements

- Minimum verification for story completion should include:
  - `./gradlew.bat --no-daemon --console=plain build`
  - `./gradlew.bat --no-daemon --console=plain test`
  - targeted validation tests in `compiler` and/or `semantics-core`
  - at least one valid and one invalid M0 fixture executed through the standard compiler path
- Add tests that prove:
  - valid IR is marked semantically valid
  - unresolved or ambiguous references emit deterministic diagnostics
  - duplicate authored semantic keys emit deterministic diagnostics using the fallback identities from Story `1.3`
  - invalid semantic state is surfaced without claiming downstream semantic success
  - diagnostics retain source provenance, semantic identity, and rule/category data
- **Windows verification rule:** avoid parallel Gradle verification runs in this repository. Story `1.3` reproduced Kotlin incremental-cache corruption when build/test tasks ran in parallel. Use sequential `--no-daemon` runs, and if corruption appears again, stop the daemon and remove module `build/` directories before rerunning.

### Previous Story Intelligence

- Story `1.3` is now **done**. It introduced:
  - the first canonical `Engineering IR`
  - reference provenance on `EngineeringReference`
  - fallback semantic identities for duplicate declarations and repeated connections
  - explicit external IR conformance artifact publishing under `examples/`
  - compiler read-failure diagnostics and CLI-only parse summaries
- Validation should treat the Story `1.3` duplicate fallback identities as evidence of authored ambiguity that must be diagnosed, not as acceptable semantic uniqueness.
- Story `1.3` also left one deferred pre-existing parser limitation: syntax parsing still stops at the first syntax error. That is not the primary goal of Story `1.4` unless it directly blocks semantic diagnostic work.
- Story `1.2` remains `in-progress` because its review bookkeeping is not closed, but the concrete parser seam blockers needed by validation are already fixed in code:
  - `owner.port` qualification is enforced
  - full-system spans are preserved
  - `SourceSpan` is documented and emitted as half-open
  - UTF-8 BOM-prefixed files are accepted
  - AST/provenance are preserved through the compiler parse surface
- Current production code inspection shows:
  - `semantics-core/` is still only a module marker and is ready to receive the first real validation contracts
  - `compiler/AthenaCompiler.kt` currently ends at parse and lower entry points; it has no validation result surface yet
  - `compiler/EngineeringIrLowerer.kt` already preserves unresolved references, duplicate fallback identities, and per-reference provenance
  - `examples/m0/demo-cabinet.engineering-ir.txt` is now the published valid IR conformance artifact from Story `1.3`

### Git Intelligence Summary

- Current committed history is still minimal:
  - `ae76b71 Add manifesto submodule`
  - `1e0719f Initial commit`
- The first real semantic contracts are now in the working tree rather than in prior commit history. Story `1.4` should inherit code patterns from the current modules, not from git history.

### Project Structure Notes

- No UX artifact exists for this phase. Keep the story compiler-first and text-first.
- The current module intent remains:
  - `language` owns DSL syntax, parser, and AST
  - `ir` owns canonical semantic objects and provenance
  - `semantics-core` should now begin owning general validation and diagnostic contracts
  - `compiler` owns pass orchestration and compiler-facing result surfaces
  - `renderer-svg` stays downstream and should not shape validation decisions
- `domain-electrical-runtime/` is still only a marker. Story `1.4` may harden the first M0 rule vocabulary, but it should not prematurely turn pluginization into the main task; the real domain plugin proof belongs later in Epic `2`.

### References

- `_bmad-output/planning-artifacts/epics.md` - Epic 1, Story 1.4 acceptance criteria and FR mapping.
- `_bmad-output/planning-artifacts/prds/prd-Athena-2026-07-02/prd.md` - Sections 4.2, 6, 7, 8, and 10.
- `_bmad-output/planning-artifacts/architecture/architecture-Athena-2026-07-02/ARCHITECTURE-SPINE.md` - AD-1 through AD-4 and AD-7, especially semantic validation and diagnostics mapping.
- `_bmad-output/specs/spec-athena/SPEC.md` - CAP-2, CAP-3, Constraints, and Success signal.
- `_bmad-output/specs/spec-athena/glossary.md` - `Engineering IR`, `Engineering Compiler`, `Conformance artifact`, `Electrical/Runtime domain extension`.
- `_bmad-output/implementation-artifacts/1-3-lower-parsed-intent-into-canonical-engineering-ir.md` - current IR boundary, duplicate fallback identity decision, and review-hardened lowering seam.
- `_bmad-output/implementation-artifacts/deferred-work.md` - pre-existing parser limitation deferred from Story `1.3`.
- `docs/compiler/m0-dsl.md` - current M0 authored property surface.
- `docs/compiler/m0-lowering-boundary.md` - current lowering boundary, IR shape, identity rules, and provenance rules.
- `manifesto/docs/architecture/01-compiler.md` - compiler pass responsibilities, validation, diagnostics, and downstream outputs.
- `manifesto/docs/architecture/03-ir.md` - canonical IR authority, stable identity, provenance, and semantic versus layout boundary.
- `manifesto/docs/architecture/09-layout-and-geometry.md` - binding separation from layout and geometry concerns.
- `manifesto/docs/prd/01-kernel-v0.md` - minimal DSL -> IR -> rule -> export proof.
- `manifesto/docs/prd/02-compiler-v1.md` - compiler objectives, explicit passes, and diagnostic expectations.
- `manifesto/docs/rfc/RFC-0005-compiler.md` - explicit pass pipeline framing for validation and diagnostics.
- `draft/0002.md` - M0 thesis that the DSL is the source of truth, the IR is the canonical model, and later stages are compiler backends around that model.

## Story Completion Status

- Status: done
- Completion note: Semantic validation, provenance-rich diagnostics, compiler compile-path gating, deterministic tests, and the review hardening patches for duplicate connection diagnostics and quoted property invalidity are implemented and verified.

## Dev Agent Record

### Agent Model Used

GPT-5 Codex

### Debug Log References

- Sprint status and epic order identify `1-4-validate-engineering-ir-and-emit-provenance-rich-diagnostics` as the next backlog story after Story `1.3`.
- No `project-context.md` file was present in the repository.
- No UX artifact was found under `_bmad-output/planning-artifacts/`.
- Story `1.3` was loaded as the immediate predecessor story and is now complete with review-driven hardening patches applied.
- Current production code inspection confirmed `semantics-core/` is still marker-only, while `compiler/` now ends at parse and lower entry points over canonical `Engineering IR`.
- Red-phase tests were added first in `semantics-core` and `compiler`, then verified failing before semantic contracts and compiler wiring were implemented.
- Sequential Java `25` verification was rerun after implementation to respect the Windows Gradle cache-corruption constraint from Story `1.3`.
- Final verification evidence: `:semantics-core:test --tests com.engineeringood.athena.semantics.core.EngineeringIrValidatorTest`, `:compiler:test --tests com.engineeringood.athena.compiler.AthenaCompilerTest`, `build`, and `test` all completed successfully with `--no-daemon`.
- Code review raised two implementation gaps: duplicate connection-key uniqueness diagnostics and misclassified text-valued `type` or `direction` properties.
- Review follow-up testing initially re-triggered the known Windows Gradle cache-corruption issue when `build` and `test` were launched in parallel; recovery succeeded after stopping the daemon, removing module `build/` directories, and rerunning sequential commands.

### Completion Notes List

- Added the first shared semantic validation contract surface in `semantics-core`, including typed diagnostic severity, category, rule identity, provenance-rich diagnostics, explicit continuation policy, and `SemanticValidationResult`.
- Implemented `EngineeringIrValidator` over canonical `Engineering IR` with deterministic checks for duplicate authored keys, unresolved or ambiguous references, missing or invalid device types, invalid port directions, illegal connection direction, and signal incompatibility.
- Extended `AthenaCompiler` with a unified `compile()` entry path that preserves parse and lowering success while surfacing semantic validity and downstream continuation policy from one result model.
- Added deterministic validation tests in `semantics-core`, compiler-path tests in `compiler`, and the first invalid semantic conformance fixture under `examples/m0/`.
- Documented the M0 validation boundary, rule vocabulary, diagnostic ordering, and invalid-state continuation policy in `docs/compiler/m0-validation-boundary.md`.
- Resolved review hardening patches by adding duplicate connection-key diagnostics, treating text-valued symbolic properties as invalid rather than missing, and extending compiler-path regression coverage for both cases.

### File List

- `_bmad-output/implementation-artifacts/1-4-validate-engineering-ir-and-emit-provenance-rich-diagnostics.md`
- `_bmad-output/implementation-artifacts/sprint-status.yaml`
- `compiler/src/main/kotlin/com/engineeringood/athena/compiler/AthenaCompiler.kt`
- `compiler/src/main/kotlin/com/engineeringood/athena/compiler/CompilerModels.kt`
- `compiler/src/test/kotlin/com/engineeringood/athena/compiler/AthenaCompilerTest.kt`
- `docs/compiler/m0-validation-boundary.md`
- `examples/README.md`
- `examples/m0/invalid-semantic-cabinet.athena`
- `semantics-core/build.gradle.kts`
- `semantics-core/src/main/kotlin/com/engineeringood/athena/semantics/core/EngineeringIrValidator.kt`
- `semantics-core/src/main/kotlin/com/engineeringood/athena/semantics/core/SemanticValidationModel.kt`
- `semantics-core/src/test/kotlin/com/engineeringood/athena/semantics/core/EngineeringIrValidatorTest.kt`

### Change Log

- 2026-07-02: Implemented Story `1.4` semantic validation, compiler compile-path gating, deterministic validation tests, invalid semantic fixture, and M0 validation boundary documentation.
- 2026-07-02: Addressed code review findings - 2 items resolved.
