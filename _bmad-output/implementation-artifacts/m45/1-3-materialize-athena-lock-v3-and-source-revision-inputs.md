---
baseline_commit: 9ca7c80239d38d9e2a4880886e71e27e2923c41b
---

# Story 1.3: Materialize athena-lock-v3 and Source Revision Inputs

Status: review

## Story

As a compiler maintainer,
I want one canonical lock and revision model,
so stale package edits fail safely.

## Acceptance Criteria

1. Given a valid selected `ResolvedPackageGraph` with admitted native PackageItems, when the compiler
   materializes `athena.lock`, then canonical `athena-lock-v3` records sorted package coordinates and
   manifest digests, exported `(packageId, itemId, itemVersion)` identities and item digests, required
   resource digests, concrete dependency edges, semantic dependency/provider edges, and safe asset-profile
   identity using `athena-lock-v3-c14n-v1` and lowercase SHA-256 values.
2. Given equivalent authored package inputs in different filesystem enumeration orders, when materialized,
   then V3 bytes and digest are identical. Given a missing, malformed, old V2, stale, semantically equal
   but noncanonical, or manually changed lock, when repository opening or mutation validation runs, then it
   fails closed with a deterministic plain-language correction. V2 parsing/materialization is absent.
3. Given an `ADD_PACKAGE_DEPENDENCY` transaction or any source transaction that changes package-affecting
   authored intent, when it publishes, then source and regenerated V3 lock are one recoverable transaction:
   validate staged source and expected lock first; publish no scene/journal entry until both files are
   committed and the compiler publishes a READY Canonical Scene; rollback restores both on failure.
4. Given `SourceRevisionService.current()`, when it produces the CAS tuple, then package detail comes only
   from compiler-owned expected V3 lock facts. It carries concrete package item identities/digests and no
   `snapshot`, `source:*`, `resource:*`, direct filesystem rehash, or independent digest ledger. A V3
   package/resource/item change changes the revision deterministically.
5. Given missing, ambiguous, cyclic, or incompatible concrete or semantic dependency resolution, or an
   invalid/incomplete item, when V3 materialization/validation runs, then it fails closed before a lock,
   runtime snapshot, binding, scene, accepted source transaction, or journal record is published.

## Tasks / Subtasks

- [x] Task 1: Replace V2 repository lock contracts with native V3 facts (AC: 1, 2)
  - [x] Replace `RepositoryLock`, `RepositoryLockedPackage`, source/resource hash snapshot contracts, and
        V2 constants with cohesive V3 typed models under `kernel/repository-model`.
  - [x] Model package coordinate/manifest digest, admitted exported item identity/digest, required resource
        digest, concrete edge, semantic contract edge with exactly one provider, and asset profile identity.
  - [x] Keep `PackageIdentifier` and `ResolvedPackageGraph` as the only package graph/identity authority.
  - [x] Delete V2 schema/parser/rendering names, pseudo item ids, and V2 tests. No adapter, dual parser, or
        compatibility read path.

- [x] Task 2: Build canonical V3 output from compiler-owned graph plus admitted items (AC: 1, 2, 5)
  - [x] Extend `AthenaRepositoryLockMaterializer` and result models to build expected V3 only from one
        resolved graph and admitted `PackageItem` publication boundary; do not scan external reference trees.
  - [x] Canonically sort packages, items, resources, concrete edges, semantic edges, and provider facts;
        use `athena-lock-v3-c14n-v1` rules and deterministic renderer/parser validation.
  - [x] Fail before write for non-READY package/item admission, unresolved/ambiguous/cyclic/incompatible
        dependencies, non-local/unsafe resource paths, missing required digest/provenance/profile facts, or
        admission budget errors. Diagnostics name exact package/item/input, problem, correction.
  - [x] Preserve atomic lock writes. Never write partial V3 output; validation must compare exact canonical
        expected bytes, not only semantic object equality.

- [x] Task 3: Move Source Revision to expected V3 facts (AC: 3, 4, 5)
  - [x] Replace `SourceRevisionService.packageItemDigests()` snapshot/source/resource pseudo entries with
        sorted exported V3 `(packageId@version, itemId@itemVersion, digest)` facts supplied by expected lock.
  - [x] Make missing/invalid/stale V3 a controlled unavailable/rejected revision path, never an empty item
        list that lets stale package state publish.
  - [x] Keep authored engineering source, Sheet, style, representation-binding, schema, and profile digests
        in the CAS tuple. Package facts are read from expected lock only; no second filesystem digest path.
  - [x] Regenerate typed protocol/schema output if `SourceRevision` fields or semantics change; never hand
        edit generated Theia TypeScript contracts.

- [x] Task 4: Make source mutation plus V3 materialization one recoverable transaction (AC: 3, 5)
  - [x] Refactor `SourceTransactionEngine`/workspace staging so package-affecting patches stage and validate
        authored files plus canonical V3 before publish, then atomically publish both.
  - [x] A transaction must not emit READY publication, operation acceptance, or journal entry until compiler
        confirms resulting source plus V3 produce READY Canonical Scene.
  - [x] On materialization, scene compilation, I/O, or rollback failure: restore pre-transaction source and
        lock; reject with existing typed reason/diagnostic conventions; journal remains unchanged.
  - [x] Preserve ordinary presentation transaction behavior; do not rewrite raw canvas X/Y, Snap policy, or
        three-layer spatial authority during lock work.

- [x] Task 5: Prove canonicality, drift, transaction safety, and hygiene (AC: 1-5)
  - [x] Add fixed V3 canonical vectors: stable ordering, item/version change, resource/profile change,
        concrete edge change, semantic provider change, and no machine-local absolute path influence.
  - [x] Add repository tests for V2 rejection/removal, missing/stale/noncanonical V3, valid exact validation,
        all dependency failures, invalid/incomplete item block, and no lock write on any rejection.
  - [x] Add LSP/transaction tests for expected-lock-only revision facts, CAS changes on V3 fact changes,
        `ADD_PACKAGE_DEPENDENCY` atomic source+lock publish, and full rollback/no journal on failure.
  - [x] Run Gradle verification strictly sequentially: focused repository model/compiler tests, package-model/
        package-runtime tests, interaction-model tests, LSP tests, then relevant compiler/runtime suites.
        Run encoding and source-set hygiene audits after source changes.
  - [x] Scan production, tests, examples, and generated contracts for `RepositoryLockV2`, `athena-lock-v2`,
        `snapshot`, `source:`, `resource:`, old descriptor paths, `.elmt`, `HTML`, and direct
        `reference/elements` / `reference/elements_contrib` runtime access. Remaining hits may exist only in
        historical/reference documentation outside active product paths.

## Dev Notes

### Architecture Guardrails

- Follow M45 AD-1 through AD-6 and AD-19. One `ResolvedPackageGraph`, one `PackageIdentifier`, one expected
  V3 lock, one Source Revision input path. Do not create registry, resolver, cache, manifest, or digest-ledger
  side authorities.
- `athena.lock` is compiler-owned derived state. `athena.yaml` and package `package.yaml` remain authored
  intent. Never accept hand-edited lock content or write source meaning into a package/library contract.
- Lock V3 cannot be built from raw package files independently of PackageItem admission. `PACKAGE_READY` is
  prerequisite for V3 publication. Story 1.4 owns published `AdmissionReport` and aggregation UI; this story
  must retain its fail-closed publication gate, not preempt its public report contract.
- Separate edge semantics: exact package/resource dependencies and semantic capability/interface provider
  dependencies are distinct records. No version-range selection, provider preference, or equal-digest tie
  break. One semantic edge has one exact provider.
- `PackageItem` metadata provides library facts only. It cannot create Entity, Function, Engineering Port,
  Relationship, FunctionPartBinding, or FunctionRepresentationBinding truth.
- Native Athena formats only. No `.elmt`, HTML, XML, external catalog parser/converter/adapter, or runtime
  access to `reference/elements` and `reference/elements_contrib`. Package-local SVG is deferred to Epic 3;
  lock model may carry governed resource facts but must not implement SVG import/admission here.
- Preserve spatial separation: `SemanticPlacementIntent` -> `LogicalLayoutCoordinate` -> compiler-derived
  `PhysicalGeometryCoordinate`; Snap is separate. This story must not make X/Y, SVG anchors, or mouse state
  revision authority.

### Existing Code To Change

- `kernel/repository-model/.../RepositoryContracts.kt`
  - Current: V2 `RepositoryLock`/`RepositoryLockedPackage` carries snapshot/source/resource hashes.
  - Change: replace V2 with V3 item/resource/edge/profile typed lock models. Preserve `PackageIdentifier`,
    manifest intent, `ResolvedPackage`, and `ResolvedPackageGraph` only where still current.
- `kernel/compiler/.../repository/AthenaRepositoryLockMaterializer.kt`
  - Current: V2 graph-to-lock calculation, renderer, hand parser, expected-lock validation, atomic lock write.
  - Change: V3 canonical builder/parser/renderer from graph plus ready admitted item facts. Preserve single
    resolver authority, deterministic diagnostics, path safety, and atomic write behavior.
- `kernel/compiler/.../repository/AthenaRepositoryLockMaterializationModel.kt`
  - Current: results expose generic `RepositoryLock` expected/actual state.
  - Change: update result types for V3; keep `isValid` strict and inspectable.
- `ide/lsp/.../SourceRevisionService.kt`
  - Current: reads expected V2 lock but creates pseudo `snapshot`, `source:*`, and `resource:*` digests;
    silently returns empty package detail when no expected lock exists.
  - Change: derive sorted V3 item facts only; make absent/invalid expected V3 fail closed through existing
    session/operation unavailable behavior.
- `ide/lsp/.../SourceTransactionEngine.kt`
  - Current: after source publish, materializes lock only for source or `athena.yaml` writable sets.
  - Change: stage expected lock before accepting package-affecting transaction; ensure rollback restores both.
    Preserve preflight CAS, patch staleness checks, READY scene requirement, accepted-journal-after-publication
    order, and root-containment checks.
- Update existing focused tests:
  - `kernel/repository-model/.../RepositoryContractsTest.kt`
  - `kernel/compiler/.../AthenaRepositoryLockMaterializerTest.kt`
  - `kernel/compiler/.../AthenaRepositoryGraphResolverTest.kt`
  - `kernel/compiler/.../AthenaRepositoryContractLoaderTest.kt`
  - `kernel/interaction-model/.../EditOperationContractTest.kt`
  - `ide/lsp/.../SourceTransactionEngineTest.kt`
  - `ide/lsp/.../EditOperationWireMapperTest.kt`
  - `ide/lsp/.../AthenaRepositoryResolverTest.kt`
  Add a focused `SourceRevisionService` test only if no existing test gives direct revision coverage.

### Previous Story Intelligence

- Story 1.1 already introduced direct `packages/<package-name>/package.yaml` discovery with SnakeYAML
  Engine and selected `athena.yaml` dependencies flowing through canonical `ResolvedPackageGraph`.
  `ADD_PACKAGE_DEPENDENCY` exists and presently materializes lock only after source publication; Story 1.3
  must make it atomically staged with V3.
- Story 1.1 deleted `representationPackageRoots`, `LocalPackageRegistry`, `LocalPackageResolver`, root
  priority, old representation snapshots, and related stale tests. Do not restore them indirectly.
- Story 1.2 established `PackageItemKind`, `PackageItemIdentity`, provenance, authored/admitted phases,
  `athena-package-item-c14n-v1`, admission states, `PackageItemAdmission`, and ready-only publication
  gate. Consume these; do not invent parallel lock item models or descriptor adapters.
- Story 1.2 replaced duplicate descriptor/symbol admission production paths. Use native PackageItem facts;
  no symbol YAML/descriptors as a lock input.
- Successful verification pattern: focused tests first, Gradle invocations strictly sequential, then broad
  LSP/compiler suite, encoding audit, source-set hygiene audit.

### Testing Requirements

- Red tests first. Assert bytes and typed model equality independently; a semantic-only equality test cannot
  prove canonical renderer behavior.
- Test no pre-existing lock overwrite when graph/admission fails. Test failed transaction restores both
  `athena.yaml`/engineering source and `athena.lock`, leaves current revision/scene/journal unchanged.
- Test deterministic V3 sorted order across package/item/resource/edge permutations and exclude absolute
  source-root paths from all canonical bytes/digests.
- Test dependency failure at every boundary: missing, duplicate semantic provider, cycle, incompatible
  interface/capability version, invalid item, incomplete item, unsafe resource path.
- Do not retain tests proving V2 read compatibility, old pseudo revision items, or legacy external formats.
- Run every Gradle command sequentially on Windows. On Gradle cache corruption indicators run
  `./gradlew.bat --no-daemon --console=plain clean`, then rerun intended verification sequentially.

### Project Structure Notes

- Keep value contracts in `kernel/repository-model`; package authored/admitted facts in
  `kernel/package-model` and `kernel/package-runtime`; filesystem/YAML and lock orchestration in
  `kernel/compiler`; CAS/transaction orchestration in `ide/lsp`.
- Keep focused, cohesive Kotlin file groups. Do not create M45/V3-named production classes merely to mark
  milestone work; product concepts must own names.
- Generated frontend contracts remain generated. This story changes no Theia UI and must not add proof/demo
  classes to production source sets.
- Active M45 evidence belongs under `_bmad-output/implementation-artifacts/m45/`; no former milestone
  examples/tests are compatibility targets.

### References

- [M45 PRD FR-1, FR-3, FR-11, FR-12 and NFRs](../../planning-artifacts/prds/prd-Athena-2026-08-08-m45/prd.md)
- [M45 Epic 1](../../planning-artifacts/m45/epics.md#epic-1-package-authority-and-resolution)
- [M45 Architecture AD-1 through AD-6, AD-19, AD-20](../../planning-artifacts/architecture/architecture-Athena-2026-08-08-m45/ARCHITECTURE-SPINE.md)
- [Story 1.1](1-1-discover-direct-local-package-catalog.md)
- [Story 1.2](1-2-implement-packageitem-authored-admitted-contracts.md)
- [Repository rules](../../../AGENTS.md)

## Dev Agent Record

### Agent Model Used

GPT-5 Codex

### Debug Log References

- BMad context created from active M45 PRD, architecture spine, epics, readiness report, sprint status,
  current V2 lock/source-revision/transaction seams, Story 1.1 and Story 1.2 records, and repository rules.
- Initial LSP failure traced to active `examples/m44/rolling-shutter/athena.lock` remaining V2; compiler materialized canonical V3 lock.
- Repository model test exposed stale V2 version assertion; updated to V3.
- Transaction tests exposed lock augmentation matching Sheet/Style/Binding companions; narrowed augmentation to engineering source, manifest, and package paths.

### Completion Notes List

- Replaced active repository lock authority with typed `athena-lock-v3` contracts and canonical rendering/validation.
- Source revision now fails closed on missing, invalid, or stale expected V3 lock and reads concrete item facts only from compiler-owned lock output.
- Package-affecting source transactions stage canonical lock bytes in an isolated repository copy, publish authored source plus lock together, and rollback both before journal publication.
- Presentation Sheet, style, and binding transactions remain lock-neutral.
- Active M44 rolling-shutter lock materialized to V3; no `.elmt`, HTML, or reference asset runtime path added.
- Verification passed sequentially: `:kernel:repository-model:test`, `:kernel:compiler:test` (lock materializer), `:kernel:package-model:test`, `:kernel:package-runtime:test`, `:kernel:interaction-model:test`, `:ide:lsp:test`.

### File List

- `_bmad-output/implementation-artifacts/m45/1-3-materialize-athena-lock-v3-and-source-revision-inputs.md`
- `examples/m44/rolling-shutter/athena.lock`
- `kernel/repository-model/src/main/kotlin/com/engineeringood/athena/repository/RepositoryContracts.kt`
- `kernel/repository-model/src/test/kotlin/com/engineeringood/athena/repository/RepositoryContractsTest.kt`
- `kernel/compiler/src/main/kotlin/com/engineeringood/athena/compiler/repository/AthenaRepositoryLockMaterializer.kt`
- `kernel/compiler/src/test/kotlin/com/engineeringood/athena/compiler/repository/AthenaRepositoryLockMaterializerTest.kt`
- `ide/lsp/src/main/kotlin/com/engineeringood/athena/ide/lsp/SourceRevisionService.kt`
- `ide/lsp/src/main/kotlin/com/engineeringood/athena/ide/lsp/SourceTransactionEngine.kt`
- `ide/lsp/src/test/kotlin/com/engineeringood/athena/ide/lsp/SourceTransactionEngineTest.kt`
- `ide/lsp/src/test/kotlin/com/engineeringood/athena/ide/lsp/AthenaDiagramSceneRequestTest.kt`
- `ide/lsp/src/test/kotlin/com/engineeringood/athena/ide/lsp/RepresentationAndEngineeringOperationHandlerTest.kt`

### Change Log

- 2026-08-08: Created Story 1.3 context; status `ready-for-dev`.
- 2026-08-08: Implemented V3 lock/source revision authority and atomic package-affecting transaction staging; status `review`.
