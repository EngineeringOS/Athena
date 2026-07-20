# Rubric Walker Review - Architecture Spine Athena M18

Target: `ARCHITECTURE-SPINE.md`
Review lens: good-spine checklist
Reviewer: independent BMad architecture rubric walker
Date: 2026-07-15

## Verdict

Needs revision before story-level implementation handoff.

The spine is directionally strong: it fixes the main M18 divergence points around compiler-owned semantic authority, governed package graph authority, ANTLR vs Tree-sitter boundaries, deterministic linking, typed diagnostics, executable proof corpus, and local operational scope. The brownfield module names and stack versions are mostly ratified against the repository: `kernel/repository-model`, `kernel/language`, `kernel/compiler`, `kernel/engineering-model`, `ide/lsp`, `ide/tree-sitter-athena`, Gradle 9.6.1, Kotlin 2.4.0, ANTLR 4.13.2, LSP4J 0.23.1, Java toolchain 25, `tree-sitter-cli >=0.26.1`, and `web-tree-sitter ^0.26.0` all match local declarations.

It is not yet a clean build substrate because two PRD-loaded capability decisions are still ambiguous while the capability map claims they are governed: semantic namespace proof strength for FR-6/SM-7 and LSP symbol/workspace closeout for FR-8. Several AD rules also use subjective escape hatches that two implementation teams could interpret differently.

Mechanical lint result: pass, 0 findings.

## Checklist Judgment

| Checklist Item | Judgment |
| --- | --- |
| Fixes real divergence points for the level below | Mostly yes. Compiler authority, repository graph authority, parser split, diagnostics, deterministic ordering, proof corpus, and operational non-goals are real divergence points. |
| Misses no real divergence point | No. Semantic namespace proof strength, LSP workspace/document symbol closeout, semantic graph snapshot lifecycle, and minimum graph-explanation surface remain insufficiently decided. |
| Every AD rule enforceable and prevents stated divergence | Partly. AD-1, AD-2, AD-3, AD-5, AD-7, AD-8, AD-11 are largely enforceable. AD-6, AD-9, AD-10, and part of AD-4 contain vague or conditional language. |
| Deferred items cannot let two units diverge | No. Workspace symbol search, alias support, and downstream capability proof are deferred or conditional in ways that can change implementation shape. |
| Named tech verified-current | Brownfield-current locally, yes. Verification-current as a documented architecture fact, no: the spine has pinned versions but no version provenance in the spine or memlog. |
| Ratifies brownfield repo boundaries | Mostly yes. The structural seed matches existing `kernel/*`, `ide/lsp`, and `ide/tree-sitter-athena` boundaries, and CodeGraph confirms repository graph resolution and AST-backed LSP authority already live under those ownership lines. |
| Covers PRD capabilities | Mostly, but with two holes. FR-6 and FR-8 are mapped while still open enough to produce incompatible implementations. |
| Parent spine inheritance conflicts | Not applicable from provided artifact. No parent spine is declared in the target. |
| Every feature-altitude dimension decided/deferred/open | Mostly yes. Syntax, parsing, repository authority, semantic graph, linking, diagnostics, LSP, Tree-sitter, proof corpus, stack, structural seed, and operational envelope are covered. Snapshot lifecycle and minimum inspection/proof surfaces need stronger decisions. |
| Operational/environmental envelope | Yes at milestone scope. AD-11 explicitly excludes new service, registry, cloud, deployment topology, and multi-root authority. It should add local runtime/session assumptions only if implementation stories need them. |

## Top Findings

### High 1 - FR-6 semantic namespace proof is asserted but not enforceably decided

Evidence:
- PRD defines semantic namespaces as carrying downstream admitted engineering knowledge, not just declarations (`prd.md` lines 175, 357-368, 466, 494).
- Spine AD-6 says namespaces must preserve "admitted capability provenance strongly enough" (`ARCHITECTURE-SPINE.md` line 95).
- Capability map claims FR-6 is governed by AD-6 (`ARCHITECTURE-SPINE.md` line 217).
- Deferred says deep downstream proof is later, but M18 "may include one component-knowledge proof if needed" (`ARCHITECTURE-SPINE.md` line 234).
- Open question asks whether declaration availability is sufficient or component knowledge is required (`ARCHITECTURE-SPINE.md` line 239).

Problem:
The spine maps FR-6 as covered while leaving the actual proof threshold open. Two teams could both claim compliance while implementing incompatible semantics: one stores only declaration IDs and provenance labels; another carries a capability-fact bridge into governed consumers. That is a real M18 divergence point because FR-6 and SM-7 are the difference between "code import" and "engineering capability import."

Required correction:
Decide the minimum FR-6 closeout rule. Either require one executable downstream governed capability proof, such as component knowledge provenance surviving import/link/lowering, or explicitly downgrade FR-6 for M18 to declaration availability plus typed provenance with a named later milestone and non-claim condition. Do not keep FR-6 mapped as fully governed while the proof threshold is open.

### High 2 - FR-8 LSP symbol behavior and workspace snapshot closeout can diverge

Evidence:
- PRD says document or workspace symbol behavior can reflect the package-aware authored workspace (`prd.md` lines 386-398).
- Spine AD-8 requires diagnostics, definition, references, and "symbol behavior" to read compiler-owned snapshots or indexes (`ARCHITECTURE-SPINE.md` line 107).
- Deferred says rich workspace symbol search may follow diagnostics, definition, and references unless closeout promotes it (`ARCHITECTURE-SPINE.md` line 230).
- Open question asks whether workspace symbol behavior is required for closeout (`ARCHITECTURE-SPINE.md` line 238).
- Brownfield LSP today tracks a single document and builds an AST-backed navigation index from that document, so package-aware workspace behavior needs an explicit new snapshot/index model.

Problem:
The spine names "symbol behavior" but does not decide the minimum required LSP symbol surface or the lifecycle of compiler-owned semantic workspace snapshots/indexes. One story could implement package-aware document symbols only; another could build workspace symbol search; another could cache a project graph in LSP; another could rebuild per request. Those choices are incompatible at feature altitude.

Required correction:
Decide the M18 LSP closeout surface in AD-8: diagnostics, definition, references, and either document symbols only or workspace symbols too. Add a rule for snapshot/index ownership and invalidation at the architecture level: compiler-owned project semantic graph snapshots are built from governed repository state plus tracked source units, and LSP may cache only derived immutable snapshots/indexes with deterministic invalidation inputs.

## Additional Findings

### Medium 3 - AD-10 weakens the repository-backed proof corpus with an escape hatch

Evidence:
- AD-10 requires `examples/m18/` fixtures, then allows tests to execute against those fixtures "or equivalent mirrored test data" (`ARCHITECTURE-SPINE.md` line 119).
- PRD FR-10 requires a repository-backed proof corpus and closeout on executable package-aware proof inputs (`prd.md` lines 410-425).

Problem:
"Equivalent mirrored test data" can become a bypass: compiler, LSP, and Tree-sitter tests can drift from the canonical repository-backed examples while still claiming AD-10 compliance. That undercuts the closeout evidence the PRD asks for.

Required correction:
Make `examples/m18/` the canonical corpus. Mirrored test data may exist only if generated from, checked against, or explicitly traced to the canonical fixtures.

### Medium 4 - AD-9 graph explanation is too subjective to enforce

Evidence:
- AD-9 requires the compiler to expose "enough graph explanation" for tests and review (`ARCHITECTURE-SPINE.md` line 113).

Problem:
"Enough" is not a rule. One team could expose a debug string; another could expose typed graph explanation models; another could only assert through test helpers. This affects FR-3, FR-5, FR-7, FR-10, and NFR-3 because explainability is how deterministic package graph and symbol binding are reviewed.

Required correction:
Name the minimum explanation surface: resolved package keys, source-unit keys, namespace availability, import edges, candidate symbol set, selected binding, rejected candidates with diagnostic code, and stable sorted rendering for tests.

### Medium 5 - Project semantic graph lifecycle is not decided

Evidence:
- AD-1 requires a compiler-owned project semantic graph before lowering (`ARCHITECTURE-SPINE.md` line 65).
- AD-8 requires LSP to read snapshots or indexes derived from compiler semantics (`ARCHITECTURE-SPINE.md` line 107).
- Mutation convention says M18 is read/analysis oriented and does not create a new mutation path (`ARCHITECTURE-SPINE.md` line 136).

Problem:
The spine does not decide whether the graph is built per compile, per repository session, per LSP workspace, or as an immutable snapshot keyed by manifest/lock/source versions. This is a feature-altitude divergence point because diagnostics, navigation, references, and deterministic ordering depend on the same graph identity.

Required correction:
Add a snapshot invariant: project semantic graph construction is compiler-owned, immutable per deterministic input set, and identified by governed repository state plus source-unit content identities. LSP and workbench code may cache derived indexes but not mutate semantic truth.

### Medium 6 - Alias support is conditionally deferred in a way implementers can self-promote

Evidence:
- AD-4 says alias support is deferred unless needed to disambiguate proof fixtures (`ARCHITECTURE-SPINE.md` line 83).
- Deferred repeats that alias import support is added only if implementation finds unavoidable ambiguity in proof fixtures (`ARCHITECTURE-SPINE.md` line 228).

Problem:
The architecture delegates a syntax-surface decision to implementation discovery without naming who decides or what qualifies as unavoidable ambiguity. Two syntax/parser stories could land different grammar and AST contracts.

Required correction:
Either hard-defer alias support out of M18 or define the promotion gate: the proof corpus must contain a named failing ambiguity case, reviewed before grammar/AST expansion, and alias semantics must receive a new AD or an amended AD-4.

### Medium 7 - Source-unit admission is underspecified

Evidence:
- AD-3 admits only packages and source units from governed repository state and compiler-visible source-unit availability (`ARCHITECTURE-SPINE.md` line 77).
- Source unit convention says source units are keyed relative to admitted package/source roots (`ARCHITECTURE-SPINE.md` line 132).

Problem:
The spine does not say what makes a source unit compiler-visible: manifest-declared roots, lock state, package-local layout, file extension, generated source inclusion, or test fixture admission. That is exactly where raw filesystem traversal can sneak back in under a nicer name.

Required correction:
Add an admission rule: compiler-visible source units are enumerated from governed package/source roots admitted by repository resolution, normalized to stable source-unit keys, and never discovered by resolving import text as an arbitrary path.

### Medium 8 - Diagnostic taxonomy is enumerated but not tied to stable code ownership

Evidence:
- AD-7 lists required failure classes and stable codes (`ARCHITECTURE-SPINE.md` line 101).
- Diagnostics convention says codes use Athena-owned stable strings grouped by package/import/linking concern (`ARCHITECTURE-SPINE.md` line 134).

Problem:
The rule does not specify whether diagnostics are emitted by repository graph resolution, project semantic graph construction, import resolution, symbol linking, or LSP projection. Without ownership, duplicate or conflicting diagnostic codes can emerge across layers.

Required correction:
Bind diagnostic ownership by phase: repository graph diagnostics remain repository-owned; import/linking diagnostics are compiler-owned package-aware diagnostics; LSP only projects compiler diagnostics without inventing new semantic codes.

### Low 9 - Stack versions are locally ratified but not recorded as verified-current architecture facts

Evidence:
- Stack table pins Java 25, Gradle 9.6.1, Kotlin 2.4.0, ANTLR 4.13.2, LSP4J 0.23.1, Tree-sitter CLI >=0.26.1, and web-tree-sitter ^0.26.0 (`ARCHITECTURE-SPINE.md` lines 139-149).
- Local repository declarations match those versions in the Gradle wrapper, Gradle version catalog, root build, and tree-sitter package.
- The memlog contains constraints and decisions but no version entries.

Problem:
The good-spine checklist asks that named technology be verified-current. For a brownfield spine, matching local declarations may be enough, but the architecture record should make that explicit. Otherwise future readers cannot tell whether these were intentionally ratified, copied from memory, or intended as upgrades.

Required correction:
Record version provenance in the memlog or add a short stack note: "Versions ratify current repository declarations as of 2026-07-15; M18 does not upgrade the stack."

### Low 10 - Operational envelope is covered, but local session assumptions remain implicit

Evidence:
- AD-11 excludes remote service, cloud registry, publish transport, deployment topology, and multi-root authority (`ARCHITECTURE-SPINE.md` line 125).
- PRD excludes remote registry, publish, marketplace, multi-root, and package-manager redesign (`prd.md` lines 444-453, 472-478).

Problem:
The negative operational envelope is good, but the positive local envelope is thin. If stories touch LSP/workbench sessions, they may diverge on whether M18 supports only the current single-root local workbench, how repository changes are refreshed, and whether package graph rebuilds are automatic or command-driven.

Required correction:
If implementation stories include LSP/workbench refresh behavior, extend AD-11 or AD-8 with the local session envelope: existing local Gradle/JVM plus existing workbench/LSP process, single governed repository root, no new daemon/service, and deterministic rebuild triggers from manifest/lock/source changes.

### Low 11 - File organization convention is generic and not M18-specific

Evidence:
- File organization convention repeats broad Kotlin grouping guidance (`ARCHITECTURE-SPINE.md` line 137).

Problem:
This is harmless but weak as a feature spine invariant. It does not fix an M18 divergence point unless the milestone is at risk of dumping parser models, graph models, diagnostics, and protocol mapping into one file.

Required correction:
Make it M18-specific or drop it: keep project graph models, diagnostics, import/linking protocol, and LSP projection mapping separated by role when files cross the local readability threshold.

### Low 12 - Capability map overstates finality for items still open

Evidence:
- Capability map lists FR-6 and FR-8 as governed (`ARCHITECTURE-SPINE.md` lines 217, 219).
- Open questions keep their decisive proof/scope open (`ARCHITECTURE-SPINE.md` lines 238-239).

Problem:
Readers scanning the map may treat FR-6 and FR-8 as implementation-ready even though they are the two highest-risk open questions.

Required correction:
Annotate the map or resolve the questions. A capability should not appear fully governed when its closeout threshold is still unsettled.

## What The Spine Gets Right

- It correctly names the paradigm as a compiler-owned semantic workspace pipeline, which matches the M18 PRD and prevents import-as-include drift.
- It ratifies the existing brownfield split: governed repository graph under compiler/kernel ownership, ANTLR/authored AST for compiler semantics, Tree-sitter for syntax UX only, and LSP as projection.
- It covers every PRD FR/NFR at least once in the capability map.
- AD-3 and AD-8 are the strongest rules: they directly block raw path import resolution, JVM classpath coincidence, IDE heuristics, frontend semantic resolution, and Tree-sitter semantic authority.
- AD-11 explicitly covers the operational envelope dimension instead of silently skipping it.
- The structural seed is appropriately minimal for a feature spine and mostly matches existing repository boundaries.

## Recommended Disposition

Do not treat this as failed architecture. Treat it as a strong draft that needs targeted tightening before story generation.

Resolve or explicitly defer the two high findings first. Then tighten AD-9 and AD-10 so test authors cannot satisfy the letter while missing the proof intent. After that, the spine should be suitable as an M18 build substrate.
