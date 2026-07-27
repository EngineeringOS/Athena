---
baseline_commit: 0311ad67c08779db3d1def271d0ac948a4683128
---

# Story 3.1: Compile Deterministic Profile And Binding Rules

Status: review

## Story

As a Cabinet package author,
I want typed Profile and Binding declarations with deterministic resolution,
so that one governed Element is selected without hidden tag or file-order behavior.

## Acceptance Criteria

1. **Given** valid Athena `profile` and `binding` declarations, **when** compilation runs, **then**
   typed profile, manifest admission, and `RepresentationBindingRule` values are produced in
   `kernel/package-model`.
2. **Given** exact snapshot versions and candidate rules, **when** `BindingResolver` evaluates
   profile, manifest, projection, lifecycle, selectors, priority, and variant, **then** exactly one
   `ResolvedRepresentationSelection` is emitted or stable missing/ambiguous diagnostics result.
3. **Given** legacy `BindingManifest.policyTags`, **when** the active M34 path runs, **then** no active
   selector reads them; fixture adapters translate legacy tags to explicit rules before resolution
   and deletion-gate tests track remaining callers.
4. **Given** all previous criteria are green, **when** mandatory polish/purge runs, **then** source,
   tests, fixtures, docs, generated outputs, and workspace state are deeply reviewed; stale/duplicate
   artifacts are removed; and RED/GREEN, AC-to-evidence, and three-layer review are recorded.

**Implements:** FR-18..FR-21, FR-41; NFR-1, NFR-4, NFR-6, NFR-7, NFR-9.

## Tasks / Subtasks

- [x] Add Story 3.1 RED contracts before production edits (AC: 1..3)
  - [x] Add a failing package-model test for `RepresentationBindingRule` typed selector, exact target
        definition/version/variant, priority, lifecycle, and source provenance.
  - [x] Add a failing resolver test proving highest explicit rule priority selects the descriptor and
        requested variant deterministically.
  - [x] Add failing resolver diagnostics for missing rule, equal highest-priority ambiguity, and
        missing requested variant.
  - [x] Add a failing deletion-gate test proving the M34 resolver path ignores
        `BindingManifest.policyTags`.
- [x] Implement typed binding rule contracts in `kernel/package-model` (AC: 1)
  - [x] Add small strongly-related model types in package-model, not runtime.
  - [x] Keep rule values domain-facing: profile id, projection context, concept id, semantic selector
        facts, target representation package/descriptor/version/variant, priority, lifecycle, and
        provenance.
  - [x] Do not let a rule declare or mutate project devices, ports, connections, or layout.
- [x] Extend `BindingResolver` as the sole selector (AC: 2, 3)
  - [x] Add binding rules to `BindingResolutionRequest`.
  - [x] Filter candidates by active profile, manifest admission, projection context, concept selector,
        package id, descriptor id, exact package/descriptor version, lifecycle, and requested variant.
  - [x] Select only by explicit priority; equal highest-priority candidates fail closed.
  - [x] Preserve anchor/label validation and zero renderer fallback.
  - [x] Remove active descriptor selection from `BindingManifest.policyTags`; use a legacy adapter only
        in tests/fixtures that still need M33 compatibility.
- [x] Add the Epic 3 Cabinet-visible selection proof seed (AC: 1..3)
  - [x] Add or extend M34 sample package binding source under the Java-style package hierarchy.
  - [x] Prove the selected element and variant are named by typed binding rule provenance, not XML,
        tag, filename, or package order.
- [x] Run sequential verification (AC: 1..3)
  - [x] Run focused package-model and package-runtime tests.
  - [x] Run focused M34 compiler/Cabinet proof tests affected by binding selection.
  - [x] Run full Gradle `test` sequentially after focused suites pass.
  - [x] Run `powershell -ExecutionPolicy Bypass -File .\tools\encoding-audit.ps1` after text/doc edits.
- [x] Perform mandatory deep polish/purge and evidence review (AC: 4)
  - [x] Audit binding vocabulary, selector ownership, policyTags callers, XML remnants, fixtures,
        generated outputs, docs, encoding, diff, and dirty-worktree boundaries.
  - [x] Remove stale duplicate rules, hidden file-order/tag selection, and generated artifacts not
        meant for source control.
  - [x] Record RED/GREEN, AC-to-evidence, independent blind/edge/acceptance reviews, review
        dispositions, and every touched file before changing the story to review.

## Dev Notes

### Scope Boundary

This story introduces typed binding rules and deterministic resolver selection. It does not implement
full project authoring, an Engineering Component System, remote packages, broad Documentation or
Schematic product views, a symbol editor, or Cabinet visual polish beyond the selection proof seed.

### Required Architecture

- `RepresentationBindingRule` belongs in `kernel/package-model`.
- `BindingResolver` is the sole selector. It emits one resolved selection or stable diagnostics.
- `RepresentationBindingCompiler` remains the sole occurrence builder and must not search/select.
- `BindingManifest` remains package/concept/profile admission only.
- `BindingManifest.policyTags` is legacy; active M34 resolution must ignore it.
- Rule priority is the only winner tie-break. Equal highest priority is ambiguous.
- Requested variants must exist. If no variant is requested, exactly one default or available variant
  must be selected deterministically; ambiguous variants fail closed.

### Existing Code To Extend

- `kernel/package-model/src/main/kotlin/com/engineeringood/athena/packageplatform/BindingManifestModels.kt`
- `kernel/package-runtime/src/main/kotlin/com/engineeringood/athena/packageruntime/BindingResolverModels.kt`
- `kernel/package-runtime/src/main/kotlin/com/engineeringood/athena/packageruntime/BindingResolver.kt`
- `kernel/package-runtime/src/test/kotlin/com/engineeringood/athena/packageruntime/BindingResolverSelectionTest.kt`
- `kernel/package-runtime/src/test/kotlin/com/engineeringood/athena/packageruntime/M33IecRepresentationPackageTest.kt`
- M34 package source under `examples/m34/sample-project/packages/representation/athena/`

### Previous Story Intelligence

- Story 2.1 and 2.2 established governed SVG compilation and immutable snapshots. Do not bypass those
  by letting rules point at raw SVG or mutable files.
- Story 2.3 added IDE support for `profile` and `binding` surface terms. Keep source vocabulary small
  and do not expose descriptor/occurrence/IR terms as public declarations.
- Story 2.4 proved importer/AI output must be canonical Athena source plus package-local governed SVG.
  Binding rules follow the same source-authority principle.
- Package resources should live beside or below the owning `.athena` source. Avoid `../`, global pools,
  and hidden runtime file scans.

### Testing Requirements

- Capture genuine RED before production edits.
- Assert diagnostic codes for missing rule, ambiguous rule, and missing variant.
- Include a deletion-gate assertion that `policyTags` can be wrong while explicit rules still select,
  proving they are not active authority.
- Run Gradle verification sequentially only.

### References

- [M34 PRD](../../planning-artifacts/prds/prd-Athena-2026-07-24-m34/prd.md)
- [M34 Addendum](../../planning-artifacts/prds/prd-Athena-2026-07-24-m34/addendum.md)
- [M34 Architecture Spine](../../planning-artifacts/architecture/architecture-Athena-2026-07-24-m34/ARCHITECTURE-SPINE.md)
- [M34 Epics](epics.md)
- [Story 2.4](2-4-prove-the-importer-and-ai-authoring-boundary.md)

## Dev Agent Record

### Agent Model Used

Codex GPT-5

### Debug Log References

- RED: `:kernel:package-model:test --tests "com.engineeringood.athena.packageplatform.RepresentationBindingRuleModelsTest"`
  failed before production code because `RepresentationBindingRule` contracts did not exist.
- RED: `:kernel:package-runtime:test --tests "com.engineeringood.athena.packageruntime.BindingResolverSelectionTest"`
  failed before resolver changes because the request had no typed rule input and selection still depended on legacy policy-tag behavior.
- RED: `:kernel:compiler:test --tests "com.engineeringood.athena.compiler.AthenaRepresentationPackageSnapshotCompilerTest"`
  failed before grammar/lowering work because `profile` / `binding` declarations were not compiled from Athena source.
- GREEN: `.\gradlew.bat --no-daemon --console=plain :kernel:package-model:test --tests "com.engineeringood.athena.packageplatform.RepresentationBindingRuleModelsTest"` passed.
- GREEN: `.\gradlew.bat --no-daemon --console=plain :kernel:package-runtime:test --tests "com.engineeringood.athena.packageruntime.BindingResolverSelectionTest" --tests "com.engineeringood.athena.packageruntime.M33IecRepresentationPackageTest"` passed.
- GREEN: `.\gradlew.bat --no-daemon --console=plain :kernel:compiler:test --tests "com.engineeringood.athena.compiler.AthenaRepresentationPackageSnapshotCompilerTest" --tests "com.engineeringood.athena.compiler.AthenaM34ElementCabinetProofTest"` passed.
- GREEN: `.\gradlew.bat --no-daemon --console=plain :kernel:package-model:test :kernel:package-runtime:test :kernel:language:test :kernel:compiler:test` passed.
- GREEN: `.\gradlew.bat --no-daemon --console=plain test` passed after focused suites.
- GREEN: `powershell -ExecutionPolicy Bypass -File .\tools\encoding-audit.ps1` passed after story text edits.

### Completion Notes List

- Ultimate context engine analysis completed from M34 PRD/addendum, architecture spine, epics, Story
  2.4, current resolver code, package-model contracts, and recent git history.
- AC-1 evidence: `RepresentationBindingRuleModels.kt` defines typed rule, selector, target,
  lifecycle, priority, and provenance contracts in `kernel/package-model`; compiler lowering emits
  rules from Athena `binding` source.
- AC-2 evidence: `BindingResolver` consumes explicit typed rules, exact descriptor/package versions,
  projection/profile/concept selectors, lifecycle, variants, and priority; missing/equal-priority/
  missing-variant cases fail closed with stable diagnostics.
- AC-3 evidence: active resolver no longer reads `BindingManifest.policyTags`; fixture translation is
  isolated in `LegacyBindingPolicyTagRuleAdapter`; deletion-gate tests prove wrong legacy tags cannot
  override explicit rules.
- AC-4 evidence: story was polished with AC-to-evidence mapping, RED/GREEN log, file list, full
  sequential Gradle `test`, encoding audit, and three-layer review.
- Blind review: no second selection authority found in the active M34 resolver path; binding rules are
  the selected source of truth.
- Edge review: missing rule, ambiguous equal priority, missing requested variant, and legacy tag
  mismatch are covered by tests.
- Acceptance review: FR-18..FR-21 and FR-41 are represented by typed contracts, compiler lowering,
  resolver behavior, deletion gate, sample source, and verification evidence.

### File List

- `_bmad-output/implementation-artifacts/m34/3-1-compile-deterministic-profile-and-binding-rules.md`
- `_bmad-output/implementation-artifacts/m34/sprint-status.yaml`
- `examples/m34/sample-project/packages/representation/athena/iec/cabinet-bindings.athena`
- `ide/lsp/src/main/kotlin/com/engineeringood/athena/ide/lsp/AthenaLanguageFeatures.kt`
- `ide/lsp/src/main/kotlin/com/engineeringood/athena/ide/lsp/AthenaLanguageServer.kt`
- `kernel/compiler/src/main/kotlin/com/engineeringood/athena/compiler/AthenaRepresentationPackageSnapshotCompiler.kt`
- `kernel/compiler/src/main/kotlin/com/engineeringood/athena/compiler/AthenaRepresentationSourceCompiler.kt`
- `kernel/compiler/src/main/kotlin/com/engineeringood/athena/compiler/AthenaSymbolSourceFormatter.kt`
- `kernel/compiler/src/main/kotlin/com/engineeringood/athena/compiler/AthenaSymbolSourceModels.kt`
- `kernel/compiler/src/test/kotlin/com/engineeringood/athena/compiler/AthenaRepresentationPackageSnapshotCompilerTest.kt`
- `kernel/language/src/main/antlr/com/engineeringood/athena/language/antlr/Athena.g4`
- `kernel/language/src/main/kotlin/com/engineeringood/athena/language/AthenaLanguageModel.kt`
- `kernel/language/src/main/kotlin/com/engineeringood/athena/language/antlr/AthenaAntlrParseAdapter.kt`
- `kernel/language/src/test/kotlin/com/engineeringood/athena/language/LanguageFacadeBoundaryTest.kt`
- `kernel/package-model/src/main/kotlin/com/engineeringood/athena/packageplatform/RepresentationBindingRuleModels.kt`
- `kernel/package-model/src/test/kotlin/com/engineeringood/athena/packageplatform/RepresentationBindingRuleModelsTest.kt`
- `kernel/package-runtime/src/main/kotlin/com/engineeringood/athena/packageruntime/BindingResolver.kt`
- `kernel/package-runtime/src/main/kotlin/com/engineeringood/athena/packageruntime/BindingResolverModels.kt`
- `kernel/package-runtime/src/main/kotlin/com/engineeringood/athena/packageruntime/LegacyBindingPolicyTagRuleAdapter.kt`
- `kernel/package-runtime/src/main/kotlin/com/engineeringood/athena/packageruntime/M32SamplePackageSet.kt`
- `kernel/package-runtime/src/main/kotlin/com/engineeringood/athena/packageruntime/M33CabinetPackageSet.kt`
- `kernel/package-runtime/src/test/kotlin/com/engineeringood/athena/packageruntime/BindingEvidencePayloadTest.kt`
- `kernel/package-runtime/src/test/kotlin/com/engineeringood/athena/packageruntime/BindingResolverSelectionTest.kt`
- `kernel/package-runtime/src/test/kotlin/com/engineeringood/athena/packageruntime/M33IecRepresentationPackageTest.kt`

### Change Log

- 2026-07-25: Created Story 3.1 with deterministic binding-rule scope and M34 selector guardrails.
- 2026-07-25: Implemented typed binding rule contracts, deterministic resolver selection, Athena
  profile/binding lowering, legacy policyTags isolation, sample binding source, tests, and evidence.
