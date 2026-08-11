---
input: draft/20260803-confuse/FULL-DISCUSS.md
target: _bmad-output/planning-artifacts/prds/prd-Athena-2026-08-07-m44/prd.md
status: reconciliation
created: 2026-08-07
---

# M44 Input Reconciliation: FULL-DISCUSS

Scope: gaps, contradictions, and freeze decisions only. This document does not restate or edit the
M44 PRD.

## Alignment

The PRD preserves the strongest conclusions from the EPLAN study:

- engineering objects and relationships are authoritative; drawing is a projection;
- Device/Entity, Function, Part, Symbol, Relationship, Sheet, and Route have separate roles;
- library/master data is package-backed and digest governed;
- Part implementation is replaceable without changing engineering identity;
- macro/pattern authoring and automation remain outside M44;
- the renderer cannot become engineering authority or mutate source directly.

## Gaps And Contradictions

### 1. Cross-domain promise has no product proof

FULL-DISCUSS identifies power, control, signal, and communication flows and explicitly positions
Athena as cross-domain. M44 permits broad Port Contract domains, but its vision, fixture, examples,
and success metric all describe IEC/vendor electrical elements. No non-electrical fixture or
domain-neutral operation proof is required.

**Freeze decision:** state whether M44 is electrical-only proof with cross-domain contracts, or add a
small non-electrical fixture. Do not let an electrical fixture silently define Athena ontology.

### 2. Device -> Function -> Symbol cardinality is underspecified

FULL-DISCUSS distinguishes one physical Device from multiple Functions and their Symbols (for
example, a contactor coil, main contact, and auxiliary contact). M44 allows an Entity, Function, or
Relationship occurrence and an Entity `symbolRef`, but does not define whether one Entity may own
many Functions/Symbols, how each occurrence identifies its Function, or how a Part binds across them.

**Freeze decision:** define Function-level representation cardinality, occurrence identity, and
trace precedence. Acceptance must prove one Entity with multiple function/symbol occurrences, or
explicitly defer that model.

### 3. Structured engineering identity is absent

FULL-DISCUSS records EPLAN's `=Function +Location -Device` addressing for large projects. M44 has
package/item identity and source trace, but no structured function-location-device identity or
addressing contract.

**Freeze decision:** mark structured addressing as deferred and prohibit ad hoc label parsing, or
add a typed identity requirement. Do not imply that a display label is equivalent to engineering
identity.

### 4. Relationship semantics stop before physical realization

FULL-DISCUSS treats Connection as a semantic object carrying endpoints, signal, potential, and
physical implementation (Terminal/Cable/Wire). M44 reconnects Ports using direction and flow/signal
compatibility, while manufacturing outputs are non-goals. The PRD does not clearly separate the
M44 semantic Relationship contract from future terminal/cable/wire realizations.

**Freeze decision:** require explicit semantic fields needed for M44 compatibility (`domain`,
`flowKind`, direction, and relationship kind), and state that Terminal/Cable/Wire are later
projections. Do not allow route geometry or a wire label to substitute for Connection truth.

### 5. Master-data governance is named, not fully contracted

FULL-DISCUSS calls out Symbol Library, Function Definition, Device Template, and Part Database as
separate governed master-data roles. M44 has Engineering/Representation packages and paired Symbol
metadata, but no closed item taxonomy or required relationship between Function Definition, Entity
template, SymbolDefinition, and PartDefinition.

The open question on normalized metadata syntax is a phase blocker: M44 requires a representative
fixture, library lookup, Change Symbol, and Bind Part before the descriptor contract is selected.

**Freeze decision:** architecture must freeze descriptor fields, item identity, package pairing,
version/digest/provenance/license, and replacement cardinality before epics/stories are frozen.

### 6. Macro language boundary needs one sharper sentence

FULL-DISCUSS defines Macro as validated reusable engineering knowledge, not a graphical copy/paste
block. M44 correctly excludes full macro authoring/automation but still permits Symbol Macro and
Element Macro references.

**Freeze decision:** make reference-only behavior explicit: referenced macros must be package-resolved
semantic items with metadata, ports, provenance, and digest; no SVG/CAD block may act as a macro
authority in M44.

### 7. Part replacement identity guarantee is incomplete

FULL-DISCUSS separates design intent (Device/Function) from replaceable procurement implementation
(Part). M44 says Part changes trigger validation and preserve trace, but does not explicitly require
Entity, Function, occurrence, and Relationship identity to survive a valid Part replacement.

**Freeze decision:** add round-trip proof that changing Part changes only implementation references
and affected validation/trace, not semantic identity or Relationship endpoints.

### 8. Multi-view lifecycle is intentionally deferred but not indexed

FULL-DISCUSS emphasizes one engineering model producing schematic, terminal, cable, wire, BOM, and
manufacturing projections. M44 excludes those outputs and supports only Canonical Scene, SVG, and
PNG. This is a valid scope cut, but the PRD's generic "export path" wording can be read as a
manufacturing/report promise.

**Freeze decision:** record terminal/cable/wire/BOM/report/manufacturing views as later Projection
milestones. Keep M44 export strictly scene-parity SVG/PNG proof.

### 9. Function/capability semantics are not connected to library metadata

FULL-DISCUSS treats Function as the bridge between what a Device can do and how it is represented.
M44 Port Contracts cover geometry and compatibility, but FR-2/FR-3 do not require a Symbol or Part
to declare which Function/capability roles it represents or satisfies.

**Freeze decision:** either add a typed Function-role binding to the library contract, or state that
M44 only consumes already-resolved Function facts from M42 and does not perform Function selection.

## Acceptance Evidence Needed

Before M44 architecture/story freeze, evidence must cover:

- one Entity with multiple Function/Symbol occurrences, or an explicit deferral record;
- stable Entity/Function/Occurrence/Relationship identity across Symbol and Part replacement;
- package pairing, metadata/asset digest mismatch, provenance, and license diagnostics;
- direction/domain/flow compatibility independent of route paint;
- a package-resolved macro reference without macro authoring or SVG-copy authority;
- an explicit electrical-only fixture versus cross-domain contract decision;
- explicit deferral of structured `= / + / -` identity and lifecycle report projections if not in M44.

## Gate

**Not ready for story freeze** until decisions 1, 2, 4, and 5 are recorded in M44 architecture and
the normalized library metadata contract is fixed. Other items may remain deferred only with owner
and revisit milestone recorded.

## Resolution

PRD now records all gate decisions: electrical-first fixture with one cross-domain relationship;
multiple Function/Symbol occurrences per Entity; typed identity with `= /+/-` addressing deferred;
Relationship fields separated from later Terminal/Cable/Wire projections; normalized metadata fixed to
UTF-8 `symbol.yaml` / `athena-symbol-v1` with canonical digest, required fields, extensions,
provenance, and license; read-only macro references; Part replacement identity preservation; lifecycle
projections deferred. Must/Should metrics are split. Gate open for architecture and story planning.
