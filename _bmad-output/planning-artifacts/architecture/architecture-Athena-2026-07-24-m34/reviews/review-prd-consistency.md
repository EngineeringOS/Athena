# Athena M34 PRD Consistency Review

- **Review date:** 2026-07-24
- **Artifacts reviewed:** M34 `prd.md`, `addendum.md`, and PRD memlog
- **Authority inputs checked:** M32 PRD authority model, M33 retrospective/handoff, M33 visual
  review checklist, M33 cleanup ledger, installed party-mode memory, and the M34 architecture spine
  and memlog
- **Review focus:** product and requirements quality, SSOT consistency, source authority, measurable
  Cabinet visual acceptance, scope control, and single-agent story readiness

## Verdict

**POOR - revise before epic or story generation.**

The party review corrected the most obvious SSOT defect: project ports remain authored truth while
Element and SVG direction/signal fields are compatibility predicates. The one-definition/one-source
rule, typed binding boundary, compiler-owned SVG interpretation, and paint-only renderer direction
are also coherent.

The PRD is not yet a reliable implementation contract. It does not record whether its
Symbol/Element-system thesis is authorized to supersede the M33 handoff's Cabinet Professional
Product Surface thesis; it weakens the party's every-epic Cabinet-outcome decision; and its Cabinet
visual gate is qualitative and circular. Forty-three equally mandatory FRs span two source
frontends, package migration, compiler security, six IDE/tooling surfaces, renderer migration,
visual design, E2E, and process governance. Several individual FRs are too compound for one agent to
own and accept without borrowing work from later stories.

## Gate Summary

| Dimension | Verdict | Reason |
| --- | --- | --- |
| Product thesis and decision-readiness | broken | The prior handoff and current PRD define different M34 theses without an explicit supersession decision or trade-off. |
| SSOT consistency | thin | Project-versus-representation truth is improved, but package, vocabulary, composition, generated-artifact, and source-form authorities are incomplete or ambiguous. |
| Source authority | broken | No document precedence is declared, and the canonical PRD memlog does not log an override of the M33 handoff. |
| Cabinet visual acceptance | broken | "Professional," "readable," and "visually credible" have no fixed fixture, capture conditions, thresholds, or pass authority. |
| Scope control | thin | All 43 FRs are mandatory; there is no minimum Cabinet path, priority tier, capacity boundary, or funded cut line. |
| Single-agent story readiness | broken | Multiple FRs combine independent modules and acceptance surfaces, while FR-41 imposes undefined cross-story process work. |
| Party recommendation adoption | partial | Compatibility-predicate guidance landed; Cabinet outcome and measurable visual guidance did not land faithfully. |

## Findings

### Critical

#### F-01 - The milestone thesis has conflicting authorities and no recorded override

**Evidence**

- The M33 handoff says M34 should be **"Cabinet Professional Product Surface"** and should finish
  one customer-demo-ready Cabinet flow before broader platform work
  (`m33-retrospective-and-m34-handoff.md`, lines 56-60).
- The current PRD instead defines **"Annotated Symbol And Element System"** and centers M34 on a
  dual-front-end representation compiler (`prd.md`, lines 8-18).
- The M34 PRD memlog records the new direction but does not say that it supersedes the handoff, who
  authorized the change, why the broader compiler scope is necessary for the Cabinet outcome, or
  what handoff work was deferred.
- The architecture memlog repeats the new direction, but a downstream architecture artifact cannot
  silently resolve an upstream product-scope conflict.

**Impact**

There is no defensible source-of-authority answer when scope pressure appears. One team can optimize
for customer-demo visual quality while another can legitimately spend the milestone on source
frontends and compiler infrastructure. Both can claim to be following an M34 authority.

**Required correction**

Add an explicit product decision to the PRD memlog and PRD:

1. Name the authority order among M33 handoff, party decisions, M34 PRD memlog, PRD/addendum, and
   architecture.
2. State whether the M33 handoff is retained, narrowed, or superseded.
3. If superseded, record the decision owner, rationale, trade-off, and which Cabinet Professional
   Product Surface acceptance remains mandatory in M34.
4. Make the title, executive summary, goals, Core Acceptance Scope, and Success Metrics express the
   same milestone thesis.

### High

#### F-02 - The party's Cabinet-outcome requirement was weakened into an escape clause

**Evidence**

- Installed party memory says every M34 epic must prove a Cabinet outcome and explicitly rejects
  architecture-shaped epics (`party-mode/memories/installed/.memlog.md`, line 19).
- The PRD memlog says each epic may produce a "Cabinet-visible **or structured** end-to-end proof."
- FR-42 repeats that disjunction (`prd.md`, line 237).

**Impact**

"Or structured" allows the canonical-contract and portable-authoring epics to close with internal
payloads and no visible Cabinet consequence. That is the exact planning failure the party
recommendation was intended to prevent. Product proof can still be concentrated in the final epic
despite the sentence that says it cannot.

**Required correction**

Replace the disjunction with a vertical rule: each epic must use the canonical M34 Cabinet fixture
and prove a visible Cabinet consequence paired with structured evidence. A structured-only outcome
may be a story proof, not an epic acceptance gate. If an exception is genuinely required, record it
as an explicit rejection of the party decision with rationale.

#### F-03 - Cabinet visual acceptance is not measurable or reproducible

**Evidence**

- FR-35 uses "professional element proportions," "readable labels," and "restrained drawing frame"
  without definitions (`prd.md`, lines 210-212).
- FR-36 refers to "approved Cabinet reference criteria" without a path, version, approver, or pass
  rule (`prd.md`, lines 214-215).
- SM-6 says screenshots must "satisfy the professional visual checklist," but does not identify the
  checklist or convert it into a measurable gate (`prd.md`, lines 293-294).
- The only located checklist is explicitly qualitative and belongs to M33
  (`m33/visual-review-checklist.md`, line 3).
- Desktop and narrow capture dimensions, device scale factor, application zoom, document zoom,
  fixture revision, and expected visible inventory are unspecified.

**Impact**

Two reviewers can reach opposite verdicts from the same screenshot. An agent cannot derive stable
acceptance tests, and a visually weak Cabinet can pass by satisfying only no-fallback structured
checks. Screenshot evidence is not reproducible across machines.

**Required correction**

Publish a versioned M34 Cabinet acceptance contract and reference it directly from FR-35, FR-36,
Core Acceptance Scope, and SM-6. At minimum it must define:

- one frozen Cabinet fixture and the expected occurrence, rail, route, terminal, label, frame, and
  title-block inventory;
- exact desktop and narrow viewport sizes, device scale factor, application zoom, document zoom,
  font setup, and screenshot naming;
- zero generic fallback, unresolved body, duplicate occurrence, clipped required occurrence,
  raw-SVG bypass, and normal-state generated component border;
- geometry-backed checks for label/body and label/route overlap, route endpoint-to-anchor alignment,
  occurrence containment, and derived document bounds, with explicit tolerances;
- a minimum captured text size or equivalent legibility threshold;
- a binary human-review rubric for proportions, density, hierarchy, and first-glance credibility,
  including reviewer role, required pass count, and where the signed verdict is recorded.

Pixel comparison may supplement this contract, but it must not be the only visual-quality test.

#### F-04 - The SSOT matrix omits inherited package and vocabulary authorities

**Evidence**

- The PRD Authority Model lists project instances, Symbol, Element, selection, and visual output
  only (`prd.md`, lines 37-45).
- M32 explicitly separates Engineering Packages, Presentation Profiles, Representation Packages,
  Binding Manifest/Policy, and package resolution. M34 says it preserves those boundaries but does
  not identify the authoritative source for Engineering Package/catalog facts, package identity,
  compatibility vocabularies, or package resolution configuration.
- The product problem names engineering catalog XML as part of the fragmentation, but FR-9 names
  source only for Symbol, Element, Presentation Profile, and binding policy.
- Direction, signal, role, and terminal are called compatibility predicates, but the PRD does not
  identify the canonical vocabulary/type authority those predicates reference.

**Impact**

Implementation can accidentally move engineering catalog truth into `.athena.element`, duplicate
package identity, or introduce local strings for direction/signal/role. The SSOT correction then
removes field-level duplication while retaining model-level duplicate authorities.

**Required correction**

Expand the authority model to cover at least:

- project semantic source and compiled semantic snapshot;
- Engineering Package/catalog facts;
- Symbol/Element reusable representation contracts;
- Presentation Profile;
- binding policy/manifest and Binding Resolver output;
- package identity, registry roots, and resolution configuration;
- canonical direction/signal/role/terminal vocabularies;
- authored layout intent versus derived composition, routing, and bounds facts;
- generated Symbol/Element IR, Representation Descriptor/Occurrence, Graphic Primitive IR, proof,
  and screenshots as derived evidence rather than authored authority;
- renderer and Workbench as consumers with no source authority.

For each row, name the authored source form, compiled derivative, consumer, and forbidden ownership.

#### F-05 - Source-form ownership is internally inconsistent

**Evidence**

- FR-9 introduces Athena source for Symbol, Element, Presentation Profile, and binding policy
  (`prd.md`, lines 126-127).
- The addendum says the active source form for **package/profile/binding** declarations uses the
  Athena compiler toolchain (`addendum.md`, line 140), adding package but not clearly naming Symbol
  or Element in that statement.
- FR-20 says "each reusable definition" chooses Athena-first or SVG-first form, which can be read to
  include profiles or policies (`prd.md`, lines 161-162).
- The addendum's SVG-first form defines an Element, but neither artifact explicitly says whether an
  SVG-first carrier may own Symbol, Element, or both, and explicitly excludes profiles, policies,
  and package metadata.

**Impact**

Grammar, identity collision, package layout, and compiler routing stories cannot agree on which
artifacts can exist in which carrier. The one-authority rule is strong in principle but not closed
over the actual artifact set.

**Required correction**

Add a normative source-form matrix. For every artifact kind, specify allowed source forms, file
extension, identity scope, duplicate-detection scope, package membership, and whether SVG metadata
is forbidden. Explicitly state whether SVG-first supports Symbol, Element, or both. Align FR-9,
FR-20..FR-22, the addendum examples, and the package migration requirement to that matrix.

#### F-06 - M34 has no funded cut line despite carrying several milestone-sized initiatives

**Evidence**

The PRD makes all 43 FRs mandatory and combines:

- Symbol/Element domain contracts;
- two source frontends and normalized IR equivalence;
- ANTLR4, tree-sitter, diagnostics, completion, highlighting, and outline;
- hostile SVG parsing, complexity limits, filesystem confinement, and coordinate normalization;
- package/profile/binding serialization migration;
- legacy model inventory and cleanup;
- active Cabinet pipeline migration;
- professional visual design and Electron E2E;
- importer boundaries and retrospective/process requirements.

No requirement is labeled core, conditional, or stretch. No capacity assumption or drop order is
given. The handoff's product priority is therefore not enforceable when compiler work expands.

**Impact**

The milestone can be declared late only after all work is attempted, or declared complete by
quietly interpreting broad FRs minimally. Cabinet visual quality is most likely to be squeezed
because it is downstream of every platform dependency.

**Required correction**

Define the minimum Cabinet proving path and a funded cut line. Preserve as mandatory only the
source form, safe-SVG subset, IDE support, and migration behavior needed by that path. Mark the
second authoring form, advanced SVG constructs, broad package grammar, or nonessential IDE
affordances as conditional unless the milestone explicitly funds them. State the drop order and
prohibit dropping Cabinet visual acceptance.

#### F-07 - Several requirements cannot map cleanly to single-agent stories

**Evidence**

- FR-12 combines six independently implemented and verified surfaces: ANTLR4, tree-sitter, LSP
  diagnostics, completion, highlighting, and outline.
- FR-24..FR-29 combine parser allowlisting, denial rules, resource budgets, filesystem/symlink
  security, transform/reference evaluation, IR lowering, and frontend transport boundaries.
- FR-30..FR-36 combine backend migration, policy proof, composition bounds, visual asset creation,
  renderer behavior, responsive Electron behavior, and human visual review.
- FR-23 and FR-37 require repository-wide M30-M33 inventory and cleanup, not one cohesive product
  behavior.
- FR-41 requires every story to perform "three-layer review" without defining the layers and mixes
  product requirements with workflow governance.

**Impact**

Story authors must either create oversized stories, split a single FR across multiple owners, or
borrow acceptance from later stories. This recreates the ownership overlap the installed room has
repeatedly flagged in prior milestones.

**Required correction**

Run an FR atomicity pass before story generation:

- split FR-12 by compiler grammar, parser parity, and LSP/IDE behavior;
- split SVG safety into syntax/content rejection, resource limits and repository confinement,
  reference/transform normalization, and typed IR lowering;
- separate Cabinet backend migration, occurrence/proof contract, deterministic geometry gates,
  visual asset/polish work, and Electron capture/human approval;
- turn migration-map production into an early inventory story, then assign each replace/delete
  action to the story that changes that path;
- move FR-41 to the milestone Definition of Done and define its review layers there.

Each resulting story should have one primary module/owner, one canonical fixture, acceptance it can
complete itself, and a Cabinet-visible consequence plus structured evidence. No story should depend
on a later story to make its own acceptance true.

### Medium

#### F-08 - Compatibility matching semantics are not closed

FR-16 and FR-17 say binding fails closed and validates direction, signal, role, and terminal, but do
not define required versus optional predicates, wildcard behavior, subtype/alias rules, unknown
values, multi-anchor cardinality, or diagnostic identity. "Compatible" is therefore not a
testable relation. Define a canonical matching table and stable diagnostic codes, including what
happens when either side omits a field.

#### F-09 - Dual-frontend equivalence has no observable equality rule

FR-22 requires equivalent validation and proof payloads but does not say whether equivalence means
byte-identical IR, semantic equality excluding provenance/source spans, equal diagnostics, or equal
rendered primitives. Define a paired fixture corpus, normalized IR equality fields, provenance
exceptions, diagnostic-code parity, and Graphic Primitive IR/render equivalence. Include negative
pairs for identity collision and dual authority.

#### F-10 - SVG security and resource NFRs lack thresholds and configuration authority

FR-26 says file size, DOM depth, element count, path complexity, transform depth, and resource size
are bounded, but gives no values, named profile, configuration source, or timeout/memory outcome.
"Configured complexity limits" can pass with arbitrary values and cannot anchor tests. Specify
defaults, hard ceilings, whether projects may lower or raise them, stable failure diagnostics, and
Windows-specific symlink/junction/reparse-point confinement cases.

#### F-11 - Cabinet proof requirements disagree about what must have a binding policy

FR-32 requires every visible reusable Cabinet component to identify a selected binding policy,
while SM-4 narrows that to every visible device. Frames, rails, route channels, label bands, and
title blocks may be composition-owned occurrences rather than semantically bound devices. Define
occurrence classes and their proof contract: device-bound Element, composition Element, route,
label, frame, and decoration. Do not force fake semantic bindings onto composition-owned visuals.

#### F-12 - SVG id validation is overbroad as written

FR-7 says the compiler rejects "missing or duplicate SVG ids." That can mean every SVG node must
have an id, rather than every metadata reference requiring one resolvable unique target. State
which nodes require stable ids, whether unreferenced geometry may omit ids, and whether uniqueness
is document-wide after `use` expansion or only in source DOM identity.

#### F-13 - Process controls are presented as product behavior

FR-41 and part of FR-43 specify RED/GREEN evidence, three-layer review, polish/purge, retrospective,
and workspace hygiene. These may be valid delivery controls, but they are not externally observable
product requirements and they obscure the product acceptance set. Move them to a named M34
Definition of Done. Define "three-layer review," required reviewers, evidence location, and failure
handling. Keep only product-observable E2E/evidence behavior in FRs.

#### F-14 - Downstream terminology and source references are insufficiently anchored

The PRD has no glossary or authoritative source index despite relying on inherited terms such as
Engineering Package, Representation Package, Representation Descriptor, Presentation Profile,
Binding Policy, Element occurrence, visual body, compatibility predicate, authored layout intent,
and Graphic Primitive IR. It also references "approved Cabinet reference criteria" without a link.
Add a compact glossary and a normative source list with exact paths and versions. This is necessary
for story agents to source-extract consistently.

## Cabinet Acceptance Contract Required Before Approval

The PRD does not need to prescribe visual implementation, but it must prescribe observable
acceptance. A sufficient contract would have four layers:

1. **Frozen fixture:** exact sample revision and expected typed inventory for devices, Elements,
   anchors, labels, routes, rails, frame, zones, and title block.
2. **Deterministic structured gate:** zero fallback/unresolved/duplicate/off-sheet violations;
   derived document bounds; route endpoints aligned to declared anchors; required labels present;
   occurrence and provenance proof complete by occurrence class.
3. **Deterministic capture gate:** exact desktop/narrow viewport, device scale factor, zoom, font,
   theme, and screenshot names; no clipping or toolbar/drawing overlap at either capture.
4. **Human product gate:** versioned binary checklist for first-glance hierarchy, proportions,
   density, terminal readability, line discipline, and frame restraint, with named approval role and
   recorded pass/fail decision.

"No generic fallback" is necessary but insufficient. The M33 handoff exists because M33 already
proved nonblank rendering, derived bounds, package-backed symbols, and no generic fallback while
still failing the customer-demo visual bar.

## Single-Agent Story Readiness Test

Before accepting the epic/story breakdown, apply this test to every proposed story:

- One agent can identify one primary ownership boundary and complete all acceptance without another
  story changing the same contract concurrently.
- The story consumes a stable predecessor contract rather than defining and migrating both sides of
  a boundary opportunistically.
- The story runs the canonical Cabinet fixture and produces a visible Cabinet consequence plus
  structured proof.
- Security and malformed-input stories may use negative Cabinet compilation as the visible outcome;
  they still identify the affected Cabinet asset and fail before Graphic Primitive IR.
- Language/tooling stories name the exact syntax slice and IDE behaviors they own; "full support"
  across six tools is not one story.
- Cleanup acceptance belongs to the story that makes the old path obsolete. A final purge story may
  verify the ledger, but it must not own migrations deferred by earlier stories.
- Visual polish owns assets/layout/style and the human gate; pipeline migration owns data flow and
  structured proof. Neither borrows the other's acceptance.

The current FR set fails this test at FR-12, FR-23, FR-24..FR-29, FR-30..FR-36, FR-37, and FR-41.

## Strengths To Preserve

- FR-13..FR-19 correctly separate authored project truth, representation contracts, policy, and
  binding.
- FR-20..FR-22 reject merge/precedence behavior for dual metadata authority.
- FR-24, FR-25, and FR-29 correctly treat SVG as hostile compiler input rather than frontend DOM.
- FR-34 clearly separates intrinsic asset bounds from Cabinet document bounds.
- Non-goals correctly hold Documentation, Schematic/Wiring, marketplace, standards compliance,
  symbol-editor UI, and runtime QET outside M34.
- The migration map and cleanup ledger are appropriate controls once ownership and scope are made
  precise.

## Approval Conditions

M34 is ready for epic/story generation only when all of the following are true:

1. The PRD records the authoritative M34 thesis and explicitly resolves the M33 handoff conflict.
2. FR-42 requires a Cabinet outcome from every epic, matching the party recommendation.
3. A versioned, measurable M34 Cabinet visual acceptance contract is normative.
4. The SSOT/source-form matrices cover packages, vocabularies, authored versus derived layout and
   composition, generated evidence, and every allowed carrier.
5. Scope has a minimum Cabinet path, priority tiers, and a funded cut line that cannot drop visual
   acceptance.
6. Compound FRs are split until each can be wholly accepted by one single-agent story.
7. Process governance is moved out of product FRs into a defined milestone Definition of Done.

