# Adversarial Divergence Review - Athena M18 Architecture Spine

Target: `ARCHITECTURE-SPINE.md`
Review lens: adversarial divergence
Reviewer stance: two downstream teams implement separate M18 slices while obeying every AD literally.

## Verdict

Blocked for implementation handoff.

The spine is directionally coherent, but it is not yet a convergence contract. The ADs prohibit the worst wrong implementations, but they do not pin several shared shapes and ownership boundaries tightly enough. Two teams can obey every AD and still build incompatible project semantic graph snapshots, source-unit keys, diagnostic envelopes, import AST shapes, Tree-sitter node semantics, LSP projection behavior, and lowering metadata.

## Divergence Model

Team A implements the compiler slice: repository graph resolution, authored AST adaptation, project semantic graph, import resolver, symbol linker, diagnostics, and lowering.

Team B implements the IDE slice: Tree-sitter grammar and queries, LSP diagnostics, definition, references, symbols, and tests using the proof corpus.

Both teams follow the spine literally:

- Team A never lets Tree-sitter own semantics.
- Team B never resolves imports independently.
- Both use governed repository state.
- Both sort outputs by stable keys.
- Both use Athena-owned diagnostic codes.
- Both execute fixtures or mirrored test data.

They can still diverge because the spine does not define the shared canonical contracts that connect those words.

## Findings

1. **Project semantic graph shape is named, not specified.**

   AD-1 requires a compiler-owned project semantic graph that joins repository state, source availability, AST intent, bindings, diagnostics, and provenance before lowering (lines 61-65). The class diagram later lists only field names: `rootPackage`, `packages`, `sourceUnits`, `namespaces`, `bindings`, and `diagnostics` (lines 176-205). That is not enough to converge downstream implementations.

   Team A can expose in-process Kotlin objects with `PackageIdentifier`, source unit objects, declaration handles, and compiler ranges. Team B can build LSP indexes against a serialized snapshot with URI strings, DTO ranges, and flattened binding IDs. Both are compiler-owned and derived from snapshots, but their data shapes are incompatible.

   Required closure: define the canonical `ProjectSemanticGraph` contract at M18 altitude: snapshot id/version, root package representation, package key type, source unit key type, namespace map shape, binding map shape, diagnostic collection shape, immutability expectations, serialization or projection boundaries, and whether LSP consumes the graph directly or a named compiler-owned index DTO.

2. **Stable identity rules do not define canonical identity algorithms.**

   AD-6 says imported semantic namespaces must preserve package id, source unit id, declaration id, source span, binding provenance, and admitted capability provenance (lines 91-95). The conventions say to use existing `PackageIdentifier` style identity and source-unit keys relative to admitted roots (lines 131-133). This still leaves every important algorithm open.

   Team A can define source unit id as normalized package-relative path without extension. Team B can define it as lockfile package key plus slash-normalized source-root-relative path. Team A can derive declaration id from declaration name and source span. Team B can expect a stable compiler-assigned id. Both avoid display-name-only identity, but definitions, references, diagnostics, and lowered IR will not join.

   Required closure: define canonical package key, source unit id, declaration id, and source span encoding rules, including case sensitivity, path separator normalization, extension handling, root selection, duplicate names, generated or synthetic declarations, and whether declaration ids survive edits.

3. **Import syntax and authored AST contracts can fork.**

   AD-2 requires package/import declarations to parse through ANTLR and adapt into Athena-owned authored AST contracts (lines 67-71). AD-4 narrows the slice to package declaration, package import, and symbol-target import (lines 79-83). The spine does not define the grammar surface, AST node names, fields, optionality, or how package import differs from symbol-target import in the authored AST.

   Team A can model `import foo.bar` as a namespace import and `import foo.bar.Baz` as a symbol import inferred after linking. Team B can mirror Tree-sitter nodes as `package_import` and `symbol_import` based on syntax. Both obey the narrow slice, but Tree-sitter outline, LSP symbols, compiler diagnostics, and AST adaptation expectations split.

   Required closure: add canonical syntax examples and the authored AST contract for package declarations, package imports, and symbol-target imports. Include node names, field names, span ownership, invalid partial forms, and whether import kind is syntactic or linker-resolved.

4. **Tree-sitter is syntax-only, but its semantic-adjacent obligations are underdefined.**

   The spine says Tree-sitter may only mirror package/import syntax for highlighting, folding, outline, and recovery-oriented syntax UX (lines 67-71), and that it has no semantic dependency on LSP (lines 166-174). That prevents Tree-sitter from resolving imports, but it does not define the shared syntax taxonomy needed for LSP and Tree-sitter to agree on visible behavior.

   Team A can have ANTLR treat malformed imports as compiler diagnostics with precise spans. Team B can have Tree-sitter recover them into different nodes for outline and folding. Both are legal. Users then see a symbol outline that suggests a package/import structure the compiler rejects differently.

   Required closure: define the allowed Tree-sitter package/import node taxonomy, query captures, recovery expectations, and which Tree-sitter-derived UX surfaces must be reconciled with compiler diagnostics versus allowed to disagree.

5. **LSP snapshot/index ownership is not a real boundary.**

   AD-8 says LSP behavior must read compiler-owned semantic workspace snapshots or indexes derived from them, and frontend code may render but not resolve imports or symbols (lines 103-107). This permits at least two incompatible designs: LSP calls compiler APIs live per request, or LSP consumes a prebuilt compiler index. Both obey the AD.

   The divergence appears under file edits, lockfile changes, and partial parse failures. One team can return definitions from the last successful graph, while another returns no result until the current snapshot is valid. Both are compiler-owned; neither is defined by the spine.

   Required closure: define the LSP projection contract: snapshot lifecycle, stale snapshot policy, invalid graph behavior, dirty buffer overlay rules, index ownership, request consistency guarantees, and whether diagnostics/definition/references must come from the same snapshot id.

6. **Mutation and invalidation paths are waved away rather than governed.**

   The mutation convention says semantic graph construction is read/analysis oriented and does not create a new mutation path or bypass M8 authority (line 136). That blocks new write authority, but not new in-memory mutation and invalidation behavior.

   Team A can rebuild graphs from disk and repository state only. Team B can apply unsaved LSP document overlays before asking the compiler for a graph. Both can claim they did not bypass M8 mutation authority. They will produce different diagnostics and definitions for the same open editor state.

   Required closure: define whether M18 supports unsaved editor buffers, how document changes enter compiler-owned analysis, which component owns invalidation, and which repository changes force graph rebuilds.

7. **Diagnostic schema is too vague for stable LSP behavior.**

   AD-7 lists diagnostic categories and requires Athena-owned typed compiler diagnostics with stable codes, source/span provenance, and LSP projection (lines 97-101). The convention says stable strings grouped by package/import/linking concern (line 134). That is not a diagnostic contract.

   Team A can emit `ATHENA_IMPORT_MISSING_SYMBOL` with warning severity on the import specifier. Team B can expect `ATHENA-LINK-003` with error severity on the symbol segment and related locations. Both use stable Athena-owned strings and source spans. LSP tests and UX diverge.

   Required closure: define the required M18 diagnostic envelope and code catalog: code strings, severity, primary span rule, related location rule, message parameter contract, cascade/suppression policy, deterministic ordering key, and LSP mapping.

8. **Governed repository authority does not specify availability semantics.**

   AD-3 says import resolution accepts only packages and source units admitted by `athena.yaml`, `athena.lock`, deterministic resolution input, resolved package graph, and compiler-visible source-unit availability (lines 73-77). AD-10 requires vendor/governed package availability fixtures (lines 115-119). The spine does not define what "admitted", "compiler-visible", or "vendor/governed availability" concretely mean.

   Team A can treat lockfile presence as package availability and then discover source units from configured roots. Team B can expect the resolved package graph to enumerate all source units explicitly. Both use governed repository state, but missing source unit, invalid availability, and graph-invalid diagnostics will differ.

   Required closure: define repository graph resolution outputs for M18, including package admission, source root admission, source unit discovery, vendor package handling, lockfile conflict behavior, missing package versus missing source unit boundaries, and cycle representation.

9. **Deterministic ordering lacks canonical sort keys.**

   AD-9 requires deterministic package keys, source-unit keys, sorted output order, and graph explanation (lines 109-113). The convention repeats that packages, source units, candidates, diagnostics, and proof outputs should sort by stable keys (line 135). The actual sort keys are not specified.

   Team A can sort diagnostics by code then span. Team B can sort by source unit then span then code. Both sort by stable keys. Golden tests, LSP publication order, and review outputs drift.

   Required closure: define ordering tuples for packages, source units, namespaces, symbol candidates, diagnostics, bindings, lowered inputs, and graph explanation output.

10. **Lowering handoff can encode bindings incompatibly.**

   AD-5 requires at least one cross-boundary declaration reference to carry binding into canonical lowering without AST paste or hidden include expansion (lines 85-89). AD-1 says the project semantic graph precedes lowering to Engineering IR (lines 61-65). The spine does not define whether the Engineering IR contract changes, whether binding provenance is metadata, or how lowered IR references package-aware identities.

   Team A can embed binding provenance as compiler-only side tables during lowering. Team B or downstream consumers can expect IR nodes to carry namespace-qualified declaration references. Both can say the binding was carried into canonical lowering. Downstream governed consumers then see incompatible IR.

   Required closure: define the M18 lowering contract: exact IR field or metadata location for package-aware binding, provenance retention rules, whether IR identity changes are allowed, and how tests inspect lowered binding.

11. **Proof corpus can certify divergent implementations.**

   AD-10 requires `examples/m18/` governed fixtures and says compiler, LSP, and Tree-sitter tests must execute against those fixtures or equivalent mirrored test data (lines 115-119). "Equivalent mirrored test data" is a divergence loophole.

   Team A can build compiler tests from real governed fixtures. Team B can mirror only the text snippets needed for Tree-sitter and LSP tests, missing lockfile/package availability edge cases. Both satisfy the wording. Closeout can pass without testing one shared repository state across compiler, LSP, and Tree-sitter.

   Required closure: require a single canonical fixture manifest and expected-output set consumed by compiler, LSP, and Tree-sitter tests. Mirroring should be allowed only for generated snapshots from that source, not hand-maintained equivalents.

12. **Open questions still affect implementation scope.**

   The spine asks whether LSP workspace symbol behavior is required for closeout (lines 236-239), while AD-8 already includes "symbol behavior" (lines 103-107) and the capability map lists LSP semantic behavior (lines 219-220). This lets one team implement document symbols only and another implement workspace symbols, both plausibly compliant.

   The second open question asks whether capability provenance must include a downstream governed capability proof (lines 236-239), while AD-6 requires admitted capability provenance (lines 91-95). One team can preserve opaque provenance strings; another can model typed governed capabilities. Both satisfy the words, but downstream consumers cannot interoperate.

   Required closure: resolve both open questions before story split. If unresolved, explicitly mark them out of scope and remove or narrow the corresponding AD language so implementers cannot choose different interpretations.

## Top Divergence Risks

1. The compiler/LSP seam is not contractually defined. Without canonical graph snapshot and diagnostic DTOs, compiler and IDE teams can both comply with AD-1, AD-7, AD-8, and AD-9 while producing incompatible APIs and golden outputs.

2. Identity is not algorithmic. Without exact package/source/declaration/span/provenance encoding rules, linking, diagnostics, references, and lowering can all appear stable locally while failing to join across modules.

## Minimum Fix Before Parallel Implementation

Before splitting M18 across teams, add a small normative contract appendix to the spine or companion architecture note:

- `ProjectSemanticGraph` schema and snapshot lifecycle.
- Canonical package key, source unit id, declaration id, source span, and binding provenance encoding.
- Authored AST contracts for package declaration, package import, and symbol-target import.
- Diagnostic code catalog and envelope.
- LSP projection contract for diagnostics, definition, references, document/workspace symbols, stale snapshots, and dirty buffers.
- Tree-sitter node/query taxonomy for the M18 syntax slice.
- Shared fixture manifest and expected outputs for compiler, LSP, and Tree-sitter tests.

Without those contracts, the spine communicates architectural intent but does not prevent downstream divergence.
