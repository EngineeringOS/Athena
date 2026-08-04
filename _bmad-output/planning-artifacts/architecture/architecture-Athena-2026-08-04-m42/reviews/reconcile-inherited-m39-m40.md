---
title: M42 Reconciliation With M39 And M40 Architecture
type: architecture-input-reconciliation
status: complete
created: 2026-08-04
reviewed:
  - ../ARCHITECTURE-SPINE.md
  - ../../architecture-Athena-2026-08-01-m39/ARCHITECTURE-SPINE.md
  - ../../architecture-Athena-2026-08-02-m40/ARCHITECTURE-SPINE.md
---

# M42 Reconciliation With M39 And M40

## Verdict

**CONDITIONAL PASS.** M42 preserves the central Reality pipeline, keeps Engineering, Projection,
Spatial, and Presentation ownership separate, retains Projection ownership of A1/B3 grid references,
and defers drawing presentation/rendering work. Three wording/coverage corrections are required before
the M42 spine is frozen; none requires a different architecture.

## Required Findings

### R1 - M40 Inheritance Is Selective And Leaves Composition Authority Implicit

**Severity:** High

M42 lines 57-74 say parent decisions bind read-only, but the table names only M40 AD-9, AD-13, and
AD-19. M40 explicitly established more continuing Projection invariants: composition remains one
Projection capability (AD-10), `ProjectionConstruct` remains a domain-neutral kernel contract with
domain-owned concrete types (AD-11), regions remain logical rather than spatial (AD-12), constructs
remain concrete and boundary-validated (AD-15), and the retired drawing-composition path must not
return as a second authority (AD-18).

M42's Relationship migration touches Projection directly, so omission is material. A future story
could interpret Knowledge Package relationship definitions as a replacement composition authority.

**Required correction:** State that all still-applicable M40 AD-9 through AD-19 decisions remain
read-only, then identify milestone-scoped decisions that are already fulfilled or not reopened.
Explicitly preserve AD-10, AD-11, AD-12, AD-15, and AD-18. Knowledge Packages may provide engineering
definition/admission facts, but may not own `ProjectionConstruct`, Projection grouping, regions, or
composition validation.

### R2 - "Definition-Owned Pairing" Can Be Read As Spatial Route Ownership

**Severity:** Medium

M42 AD-34 lines 269-276 correctly preserves one semantic Relationship and one Projection group, but
"Definition-owned pairing may produce several Spatial route legs" blurs the M39 AD-4 and M40 AD-13
boundary. A definition can govern logical participant pairing or connectivity admission. Only the
Spatial compiler can create route-leg facts, choose paths, or own geometry.

**Required correction:** Say definitions produce only typed logical endpoint pairing/admission.
Projection preserves that grouping without coordinates. Spatial alone derives zero or more route
legs and all route geometry. No knowledge compiler or definition emits Spatial facts.

### R3 - M39 Engineering Constraint Ownership Needs An Explicit Three-Way Split

**Severity:** Medium

M39 AD-2 assigns project engineering truth, including engineering constraints, to Engineering
Reality. M42 AD-22 moves Constraint definitions to the Knowledge Document and Judgements to the
Validation Document, but does not explicitly place authored project constraint applications or
requirements. Without that sentence, M42 appears to move project-owned truth out of Engineering
Reality rather than separate definition, application, and judgement.

**Required correction:** State that authored project Requirement/Constraint applications and their
subject bindings are Engineering Reality facts; reusable Constraint definitions belong to the
Knowledge Document; evaluated outcomes and Judgements belong to the Validation Document. Evaluation
must never mutate the authored application or its subject.

## Verified Alignment

- **Reality ownership:** M42 AD-20 through AD-23 keeps immutable staged compilation and does not add
  an Engineering Knowledge Reality. Engineering Reality remains project-subject authority.
- **Projection ownership:** M42 AD-21 and AD-34 prevent Projection from importing `knowledge-model`
  or validation contracts. Relationship migration changes identity/type inputs, not view ownership.
- **A1/B3 grid language:** M42 inherited table explicitly keeps M40 AD-9. Combined with inherited M39
  AD-3, grid rows, columns, and references such as A1/B3 remain Projection facts without coordinates;
  derived placement remains Spatial-owned.
- **Grouped relationships:** M42 AD-34 preserves one multi-participant engineering Relationship and
  one Projection group. This matches M40 AD-9 and its grouped-endpoint invariant.
- **Paint authority:** M42 preserves Presentation as sole paint-fact owner and keeps Theia/SVG
  paint-only. Validation facts are displayed by adapters, not converted into engineering or geometry
  truth.
- **Rendering deferred:** M42 AD-34 excludes rendering scope and the Deferred section assigns
  knowledge-aware Presentation/Rendering to M43. M42 Inspector, Problems, and source-navigation work
  is product-surface adaptation, not drawing-renderer work.
- **Renderer cannot repair:** M42 inherited M39 AD-7 and AD-33 keep UI surfaces read-only and forbid
  frontend inference or evaluation.
- **Legacy retirement:** M42 AD-35 deletes Component/Connection and Semantic Macro authorities rather
  than adding compatibility adapters. This does not weaken Reality ownership if the surviving M40
  Projection contracts are named as required by R1.

## Clarification Recommended

M42's Deferred statement says it "changes no rendering behavior," while AD-33 requires Theia
Inspector and Problems to display Validation facts. Clarify this as "no engineering drawing
Presentation-to-Theia/SVG paint behavior changes in M42." This removes ambiguity without changing
scope.

M39's source spine still carries `status: draft`; M40 is final and explicitly adopts M39 AD-1 through
AD-8 read-only. Treat M40's final adoption as the frozen inheritance link unless M39 itself is
finalized. This is provenance hygiene, not a design blocker.

## Freeze Gate

M42 may be marked final after R1-R3 are incorporated and the clarification is resolved. No changes
are needed to A1/B3 ownership, Reality ordering, composition direction, or M43 rendering deferral.
