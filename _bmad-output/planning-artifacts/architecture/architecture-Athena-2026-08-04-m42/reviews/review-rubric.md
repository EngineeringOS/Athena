# M42 Architecture Spine Rubric Review

## Verdict

**CORRECTIONS REQUIRED.** No critical architecture conflict was found. Spine has strong authority
boundaries, broad PRD coverage, explicit legacy deletion, and a complete local operational envelope.
Two high-severity divergence points remain under-specified before implementation stories can safely
split: canonical document bytes/digests and surviving brownfield module dependencies. One medium
editor-language gap also needs an explicit boundary.

## Findings

### High - Canonical byte and digest contracts are not fully fixed

**Evidence:** AD-32 says the adapter uses "deterministic key ordering and escaping" and
"contract-defined array ordering" but does not state those rules (`ARCHITECTURE-SPINE.md:251`). AD-33
publishes document digests without selecting the digest algorithm or representation
(`ARCHITECTURE-SPINE.md:265`). Deferred does not assign either choice to one prerequisite story.

**Divergence:** Independent protocol, runtime, LSP, CLI, and TypeScript stories can all comply with
the current words while choosing different object-key comparison, Unicode handling, string escaping,
whitespace, nested-array ordering, hash algorithm, or digest encoding. Golden fixtures cover known
examples but do not define canonical behavior for every valid document. That breaks FR-21, NFR-1,
AD-32's stated prevention, and the cross-surface digest contract in AD-33.

**Required correction:** Bind one exact canonicalization contract in AD-32, including object-key
ordering, whitespace, escaping, Unicode normalization policy, and ordering rule for every array
category. Bind digest algorithm, input bytes, and text encoding in AD-33 (for example, SHA-256 over
canonical UTF-8 bytes encoded as lowercase hexadecimal). Alternatively name an exact versioned
canonical JSON standard and state only Athena-specific array ordering. Make the canonical codec/schema
story a prerequisite for every transport consumer.

### High - Acyclic dependency graph omits surviving brownfield owners

**Evidence:** AD-21 declares that reverse dependencies not shown in its graph are forbidden
(`ARCHITECTURE-SPINE.md:105`), but the graph omits `authoring-model`, semantic diff/review/SCM,
plugin API, routing consumers, and other current owners affected by AD-35. AD-35 deletes
`EngineeringKnowledgeState`, five Gradle modules, and Semantic Macro contracts while preserving or
moving selected responsibilities into `authoring-model`/runtime and existing semantic modules
(`ARCHITECTURE-SPINE.md:295`). `authoring-model` is also absent from Structural Seed
(`ARCHITECTURE-SPINE.md:402`). Current repository build files show `authoring-model` depends on
retired `component-model` and `part-model`; compiler, runtime, presentation, and LSP consume
`authoring-model`; routing and plugin API consume other modules marked for deletion.

**Divergence:** Stories can choose incompatible replacement dependencies: make authoring depend on
`knowledge-model`, duplicate validation slices in review/SCM, preserve retired DTOs inside plugin API,
or bypass the declared model graph. The rule is simultaneously exhaustive in wording and incomplete
for modules AD-35 explicitly keeps, so it is not enforceable as written.

**Required correction:** Expand AD-21's target dependency graph or explicitly scope the diagram and
add a migration-boundary table for every surviving direct consumer of a retired module or
`EngineeringKnowledgeState`. Bind which immutable document or model each survivor may consume and
forbid copied replacement DTOs. Include `authoring-model` in Structural Seed and state whether its
knowledge-aware queries live in compiler/runtime adapters or may depend on `knowledge-model`.

### Medium - Existing Tree-sitter editor grammar is outside the one-frontend rule

**Evidence:** AD-24 binds editor support but names only the ANTLR frontend and prohibits a second
knowledge parser (`ARCHITECTURE-SPINE.md:151`). Brownfield IDE highlighting uses a separate checked-in
`ide/tree-sitter-athena` grammar, highlight query, WASM artifact, and frontend service with its own
tests. Neither AD-24, AD-37, Stack, nor Structural Seed states its permitted role or synchronization
contract.

**Divergence:** One story can update ANTLR only, leaving package-local M42 source unparseable or
mis-highlighted in Theia; another can add semantic behavior to Tree-sitter, creating the second
language authority AD-24 intends to prevent.

**Required correction:** State that ANTLR remains the sole semantic parser while Tree-sitter is an
editor-only syntax/highlighting projection. Require shared accepted/rejected M42 syntax fixtures,
regenerated WASM, query tests, and frontend rebuild before language stories close. Add current
Tree-sitter versions only if the spine binds them as M42 stack.

### Low - Structure Assignment domain ownership lacks an explicit closure check

**Evidence:** FR-3 is mapped to `engineering-model`, language, and compiler
(`ARCHITECTURE-SPINE.md:449`), and identity independence is fixed in Consistency Conventions. However,
no Rule explicitly binds functional, installation, and product/device aspect definitions plus
EPLAN-style `=`, `+`, and `-` display designations to Knowledge Packages. AD-35's domain-leak audit
does not list structure aspect/designation constants (`ARCHITECTURE-SPINE.md:303`).

**Divergence:** A kernel story can hardcode electrical structure kinds or display symbols while still
passing the named domain-constant audit.

**Required correction:** Extend the package-ownership Rule and closure audit to structure aspect and
display-designation constants. Keep only assignment identity/reference mechanics in
`engineering-model`.

## Good-Spine Checklist

### 1. Real divergence points for the level below

**Needs correction.** Most high-value forks are fixed: staged compiler ownership, exact arithmetic,
definition identity, failure classification, Relationship/Flow separation, source authority,
Projection boundary, transport shape, and direct legacy deletion. Canonical bytes/digests and
surviving brownfield dependencies remain real unresolved forks.

### 2. Every AD Rule is enforceable and prevents its stated divergence

**Needs correction.** AD-20, AD-22 through AD-31, and AD-34 through AD-40 provide concrete ownership
and rejection rules. AD-32 delegates essential canonical details to an unnamed future contract.
AD-21's dependency diagram claims exhaustive prohibition while omitting explicitly surviving
modules. AD-24 omits the existing editor grammar boundary.

### 3. Deferred contains no divergence leakage

**Pass.** Grammar surface detail is safely assigned to the first Knowledge Source story before
dependent compiler work. Registry lifecycle, optimization, Pattern/AI, PhysicalAsset, M43 rendering,
and distributed operations are outside M42 with valid revisit conditions. None is required to
implement an in-scope capability. Canonicalization and module migration gaps are not Deferred items;
they must be fixed in the spine.

### 4. Named technology is verified-current

**Pass.** Stack pins repository versions and dates external verification for M42 additions:
Java 25, Kotlin 2.4.0, Gradle 9.6.1, ANTLR 4.13.2,
`kotlinx-serialization-json` 1.11.0, Node 24.15.0 with project contract `>=22`, Yarn 1.22.22,
TypeScript 5.9.3, `json-schema-to-typescript` 15.0.4, Ajv 8.20.0, Theia 1.73.1, and Electron 39.8.7.
Ajv is correctly changed from transitive availability to a direct contract dependency.

### 5. Brownfield fit

**Needs correction.** Direct deletion rather than adapters matches pre-1.0 repository policy, and
the spine correctly reuses package graph, ANTLR, runtime, LSP, CLI, Theia, Projection, and Spatial
owners. AD-35 recognizes major legacy blast radius. Fit remains incomplete because current surviving
module dependencies and Tree-sitter editor grammar are absent from the binding migration model.

### 6. PRD capability coverage

**Pass with one low correction.** Capability map covers FR-1 through FR-29. Rules preserve all three
user journeys: cross-domain controlled-conveyor validation, governed package authoring, and open
contract integration. NFR determinism, fail-closed behavior, human diagnostics, open contracts,
bounded execution, cross-domain integrity, and product agreement all have owners and proof gates.
Structure Assignment's domain-owned designations need the low correction above.

### 7. Inherited M39/M40 consistency

**Pass.** M39 ownership remains intact: Engineering owns project truth, Projection owns view facts,
Spatial owns geometry, Presentation owns paint, and renderer performs no repair. M40 Projection
construct, logical-region, A1/B3 grid, thin transformation, and no-second-composition boundaries are
preserved. AD-34 correctly keeps one multi-participant engineering Relationship and Projection group
while allowing Spatial to derive multiple route legs without creating Relationships.

### 8. Operational and environmental envelope

**Pass.** AD-36 explicitly fixes local JVM execution in desktop Theia/Electron and CLI, locked local
package resolution, pure deterministic evaluation, digest-keyed non-authoritative caching, and no
server/database/network/credentials/telemetry/background authority. Deferred gives a distributed-state
revisit trigger. AD-37 binds sequential Windows Gradle verification, frontend rebuild, product E2E,
M41 regression, source-set hygiene, and milestone-local proof.

## Gate Summary

- Critical: 0
- High: 2
- Medium: 1
- Low: 1
- Recommended gate state: hold finalization until high and medium corrections land; apply low fix in
  same spine revision.
