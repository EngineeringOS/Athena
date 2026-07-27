---
baseline_commit: 0311ad67c08779db3d1def271d0ac948a4683128
---

# Story 3.2: Bind Project Ports To Element Anchors

Status: review

## Story

As an engineer,
I want authored device ports validated against selected Element anchors,
so that Cabinet connections remain semantically correct without duplicate port truth.

## Acceptance Criteria

1. **Given** one resolved Element and authored project ports, **when**
   `RepresentationBindingCompiler` constructs an occurrence, **then** every terminal binding validates
   role, direction, signal, terminal, labels, and source identity.
2. **Given** missing, ambiguous, or incompatible anchors or labels, **when** binding runs, **then** no
   occurrence or renderer fallback is produced and stable diagnostics identify both project and
   representation provenance.
3. **Given** a representation rule attempts to add or reclassify a device or port, **when** semantic
   authority validation runs, **then** compilation fails before mutation, occurrence creation, or
   rendering.
4. **Given** all previous criteria are green, **when** mandatory polish/purge runs, **then** source,
   tests, fixtures, docs, generated outputs, and workspace state are deeply reviewed; stale/duplicate
   artifacts are removed; and RED/GREEN, AC-to-evidence, and three-layer review are recorded.

**Implements:** FR-13..FR-17, FR-20, FR-41; NFR-1, NFR-4, NFR-5, NFR-9.

## Tasks / Subtasks

- [x] Add Story 3.2 RED contracts before production edits (AC: 1..3)
  - [x] Add a failing occurrence-binding test for compatible authored project port to Element anchor.
  - [x] Add failing diagnostics for missing anchor, ambiguous compatible anchors, incompatible
        role/direction/signal/terminal, and missing required label.
  - [x] Add a semantic-authority guard test that rejects representation source trying to declare
        project port truth or device reclassification.
- [x] Extend occurrence binding validation without adding a second selector (AC: 1, 2)
  - [x] Keep `BindingResolver` as the only Element/variant selector.
  - [x] Keep `RepresentationBindingCompiler` as the only occurrence builder.
  - [x] Validate project semantic port facts against selected Element anchor predicates.
  - [x] Return stable source-spanned diagnostics and no fallback occurrence on failure.
- [x] Preserve project and representation SSOT boundaries (AC: 3)
  - [x] Ensure representation definitions/rules cannot add, rename, or reclassify project ports.
  - [x] Keep project `.athena` device ports as the only actual port truth.
  - [x] Record retained compatibility path explicitly: M30/package descriptor fixtures now synthesize
        project-port facts from existing evidence; they do not bypass the canonical binder.
- [x] Add the Epic 3 Cabinet-visible anchor proof seed (AC: 1..3)
  - [x] Extend the M34 sample proof with a bound device/Element proof.
  - [x] Prove selected Element anchors bind to authored device ports by provenance, not name guessing,
        SVG shape inference, XML, or renderer fallback.
- [x] Run sequential verification (AC: 1..3)
  - [x] Run focused representation/package/compiler tests for occurrence binding.
  - [x] Run focused M34 Cabinet proof tests affected by anchor binding.
  - [x] Run full Gradle `test` sequentially after focused suites pass.
  - [x] Run `powershell -ExecutionPolicy Bypass -File .\tools\encoding-audit.ps1` after text/doc edits.
- [x] Perform mandatory deep polish/purge and evidence review (AC: 4)
  - [x] Audit anchor vocabulary, project-port ownership, SVG data-athena bridge, XML remnants, fixtures,
        generated outputs, docs, encoding, diff, and dirty-worktree boundaries.
  - [x] Remove stale duplicate binding facts, hidden anchor guessing, and generated artifacts not meant
        for source control.
  - [x] Record RED/GREEN, AC-to-evidence, independent blind/edge/acceptance reviews, review
        dispositions, and every touched file before changing the story to review.

## Dev Notes

### Scope Boundary

This story validates binding from project ports to selected Element anchors. It does not implement
Engineering Component definitions, a symbol editor, broad Schematic/Documentation surfaces, or
renderer-driven inference.

### Required Architecture

- Project `.athena` remains SSOT for actual devices, ports, connections, and layout.
- Symbol/Element anchors declare reusable compatibility predicates only.
- `BindingResolver` selects a descriptor/variant; `RepresentationBindingCompiler` constructs and
  validates the occurrence. Neither may perform the other's job.
- Missing or incompatible anchors fail closed with stable diagnostics and no renderer fallback.
- SVG `data-athena-*` can mark reusable anchor contracts on contract-bearing nodes only; it cannot
  declare project ports.

### Existing Code To Extend

- `kernel/representation-model/src/main/kotlin/com/engineeringood/athena/representation/RepresentationDefinitionModels.kt`
- `kernel/representation-model/src/main/kotlin/com/engineeringood/athena/representation/RepresentationContracts.kt`
- `kernel/representation-model/src/main/kotlin/com/engineeringood/athena/representation/RepresentationValidation.kt`
- `kernel/package-runtime/src/main/kotlin/com/engineeringood/athena/packageruntime/BindingResolver.kt`
- `kernel/compiler/src/main/kotlin/com/engineeringood/athena/compiler/AthenaRepresentationPackageSnapshotCompiler.kt`
- `kernel/compiler/src/test/kotlin/com/engineeringood/athena/compiler/AthenaM34ElementCabinetProofTest.kt`

### Previous Story Intelligence

- Story 3.1 made typed `RepresentationBindingRule` the sole active selection input.
- `BindingManifest.policyTags` is legacy fixture input only; do not add new active reads.
- Package resources stay beside or below owning `.athena` sources. Avoid `../` and global resource pools.

### Testing Requirements

- Capture genuine RED before production edits.
- Assert diagnostic codes, not only message text.
- Include one positive and several fail-closed binding cases.
- Run Gradle verification sequentially only.

### References

- [M34 PRD](../../planning-artifacts/prds/prd-Athena-2026-07-24-m34/prd.md)
- [M34 Addendum](../../planning-artifacts/prds/prd-Athena-2026-07-24-m34/addendum.md)
- [M34 Architecture Spine](../../planning-artifacts/architecture/architecture-Athena-2026-07-24-m34/ARCHITECTURE-SPINE.md)
- [M34 Epics](epics.md)
- [Story 3.1](3-1-compile-deterministic-profile-and-binding-rules.md)

## Dev Agent Record

### Agent Model Used

Codex GPT-5

### Debug Log References

- RED: `.\gradlew.bat --no-daemon --console=plain :kernel:representation-model:test --tests "com.engineeringood.athena.representation.RepresentationBindingCompilerTest"`
  failed before production edits because `RepresentationBindingRequest.projectPorts` and
  `RepresentationProjectPortFact` did not exist.
- RED: the same focused test then failed on invalid/absent compatibility behavior until typed
  project-port facts were checked against Element anchor predicates.
- GREEN: `.\gradlew.bat --no-daemon --console=plain :kernel:representation-model:test --tests "com.engineeringood.athena.representation.RepresentationBindingCompilerTest"` passed.
- GREEN: `.\gradlew.bat --no-daemon --console=plain :kernel:representation-model:test` passed.
- GREEN: `.\gradlew.bat --no-daemon --console=plain :kernel:compiler:test --tests "com.engineeringood.athena.compiler.AthenaM34ElementCabinetProofTest"` passed.
- GREEN: `.\gradlew.bat --no-daemon --console=plain :kernel:package-runtime:test --tests "com.engineeringood.athena.packageruntime.PackageBackedRepresentationOccurrenceFactoryTest" --tests "com.engineeringood.athena.packageruntime.BindingEvidencePayloadTest"` passed.
- GREEN: `.\gradlew.bat --no-daemon --console=plain test` passed after all Story 3.2 code/test edits.
- GREEN: `powershell -ExecutionPolicy Bypass -File .\tools\encoding-audit.ps1` passed after story/sprint text edits.

### Completion Notes List

- Ultimate context engine analysis completed from M34 PRD/addendum, architecture spine, epics, and
  Story 3.1 resolver implementation notes.
- AC-1 evidence: `RepresentationProjectPortFact` and `projectPorts` were added to
  `RepresentationBindingRequest`; `RepresentationBindingCompiler` computes compatibility from
  authored project-port facts and selected Element anchor predicates.
- AC-2 evidence: missing anchors, missing project-port proof, incompatible direction/signal/terminal,
  and missing required labels produce diagnostics and no occurrence.
- AC-3 evidence: `RepresentationDefinitionForbiddenAuthority.PROJECT_PORT` is rejected before
  occurrence creation; M30/package fixtures now feed explicit project-port facts instead of creating a
  second selector or renderer fallback.
- AC-4 evidence: full Gradle `test`, encoding audit, AC mapping, file list, and three-layer review are
  recorded.
- Blind review: no new active selector was added; `BindingResolver` still selects and
  `RepresentationBindingCompiler` only validates/builds.
- Edge review: missing anchor, missing proof, direction mismatch, signal mismatch, terminal mismatch,
  label omission, and source-authority violation are covered.
- Acceptance review: FR-13..FR-17 and FR-20 are represented by typed project-port facts, anchor
  predicate validation, fail-closed diagnostics, and M34 Cabinet proof tests.

### File List

- `_bmad-output/implementation-artifacts/m34/3-2-bind-project-ports-to-element-anchors.md`
- `_bmad-output/implementation-artifacts/m34/sprint-status.yaml`
- `kernel/representation-model/src/main/kotlin/com/engineeringood/athena/representation/RepresentationBindingCompiler.kt`
- `kernel/representation-model/src/main/kotlin/com/engineeringood/athena/representation/RepresentationDefinitionModels.kt`
- `kernel/representation-model/src/main/kotlin/com/engineeringood/athena/representation/RepresentationValidation.kt`
- `kernel/representation-model/src/main/kotlin/com/engineeringood/athena/representation/M30DemoRepresentationBinder.kt`
- `kernel/representation-model/src/test/kotlin/com/engineeringood/athena/representation/RepresentationBindingCompilerTest.kt`
- `kernel/package-runtime/src/main/kotlin/com/engineeringood/athena/packageruntime/PackageBackedRepresentationOccurrenceFactory.kt`
- `kernel/compiler/src/test/kotlin/com/engineeringood/athena/compiler/AthenaM34ElementCabinetProofTest.kt`

### Change Log

- 2026-07-25: Created Story 3.2 with project-port to Element-anchor binding guardrails.
- 2026-07-25: Implemented typed project-port binding facts, Element anchor compatibility validation,
  fail-closed diagnostics with provenance, package/M30 fixture proof conversion, M34 Cabinet proof
  label binding, tests, and evidence.
