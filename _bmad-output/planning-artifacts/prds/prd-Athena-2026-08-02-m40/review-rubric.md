# PRD Quality Review - Athena M40 Projection Reality

Run: 2026-08-02 (headless rubric walker)
Scope: `prd.md` + `addendum.md`; cross-checked against M39 retrospective, M39 spine, `settings.gradle.kts`, `kernel/drawing-composition`, `kernel/compiler`, and the QElectroTech reference.

## Overall verdict

This is a decision-ready technical-capability PRD: the thesis (Projection becomes the authoritative owner of engineering views; composition is one projection capability) is concrete, every FR carries testable consequences, the seven open decisions are genuinely open and correctly block finalize/epics, and the M39 baseline, `:kernel:drawing-composition` evidence, and reference-image facts all verify against the repo. What is at risk is downstream usability: the M40 architecture spine was written against an earlier FR numbering and the pre-rename "Composition" framing, so its AD-to-FR bindings and deferral buckets no longer resolve against this PRD's FR-1..FR-22 / Projection Reality model. The PRD itself is internally coherent; the spine must be re-aligned before architecture can source-extract safely.

## Decision-readiness - strong

Trade-offs are surfaced, not smoothed: the addendum records Option A/B/C for the authored-vs-derived split with a recommendation and a rejection rationale (the M39 "grammar monster" lesson), and the region-as-logical-section choice is bound to M39 AD-3 rather than re-argued. All seven open decisions are marked `[OPEN]` or `[ASSUMPTION / OPEN]` with an owner named (milestone owner) and a stated consequence (blocks epics creation and PRD finalize). The roadmap is explicitly tagged `[ASSUMPTION: roadmap accepted from PM feedback, pending milestone-owner sign-off]` - honest, not buried.

### Findings
- **medium** Roadmap assumption is load-bearing for multiple FRs (Open Decision 7). The one-reality-per-milestone bucketing (M41 Spatial, M45 Professional Routing, M46 AI Projection) changes where deferrals land versus the M39 handoff wording ("M41+ professional routing"), and FR-17 and the Handoff To M41+ section depend on it. *Fix:* resolve Open Decision 7 before PRD finalize; until then the deferral buckets are provisional.

## Substance over theater - strong

No persona furniture, no innovation theater: the three-sentence thesis ("Engineer writes meaning. Compiler projects facts. Theia paints facts.") is specific to this product and would not swap into another PRD. NFRs are product-shaped rather than boilerplate - NFR-7 (kernel domain-neutrality via domain-contributed contracts like `ConnectionVerb`) and NFR-8 (honest visual claims, no professional-parity claims) carry real constraints. The reference image is used as a composition target, explicitly not a pixel-parity target - earned use, not decoration.

### Findings
- **low** NFR-1/NFR-2 (K.I.S.S., LLM-friendly) are stated without a threshold, but in context each is pinned by the Syntax Target proposal and the no-second-DSL non-goal. Acceptable; no fix required.

## Strategic coherence - adequate

The PRD has a clear arc: M39 fixed ownership boundaries, M40 fills Projection's substance, and the roadmap makes "one reality per milestone" the organizing logic. FRs follow the thesis (model -> constructs -> compiler -> Spatial consumption -> proof). What is missing is a counter-metric: acceptance is all forward-looking (Projection owns views, metrics improve), with no named risk that composition could make authoring or compilation worse.

### Findings
- **medium** No counter-metrics (Strategic coherence, Acceptance Criteria). The thesis bets that composition adds readability without adding authoring burden or compiler complexity; nothing measures the downside. *Fix:* add one counter-metric to Acceptance Criteria, e.g. "average declaration lines per authored relation in `examples/m40` must not exceed the M39 example" or a projection-compile time budget.

## Done-ness clarity - strong

Every FR (FR-1 through FR-22) carries a "Consequences (testable)" block with named diagnostics, identity rules, or measurable outputs - strong source material for story creation. FR-18 and FR-20 are the only FRs gated on the open density-target decision, which is correctly flagged.

### Findings
- **medium** Baseline metric is imprecisely anchored (FR-18, Acceptance Criteria 5). The PRD says "28 label collisions at the reference viewport"; the M39 retrospective and this PRD's own addendum say "28 label collisions at fit-to-screen zoom". Viewport size and zoom level must be pinned, or FR-18's before/after comparison is not reproducible. *Fix:* state the exact viewport (e.g. 1920x1080) and zoom rule ("fit-to-screen") in FR-18 and Acceptance Criteria 5, matching the M39 retrospective wording.

## Scope honesty - strong

The "M40 must not deliver" list is explicit and non-negotiable (no routing optimization, no label engine, no second DSL, no kernel electrical vocabulary, no stale naming), and deferrals are bucketed per the roadmap. `[ASSUMPTION]` tags appear on the two inferences that are not owner-confirmed (roadmap, example subject). Open-items density (7) is appropriate for a draft that explicitly blocks green-light until resolved.

### Findings
- **low** Label-engine deferral is fuzzily bucketed as "M42/M44 territory per the roadmap" (Scope, Handoff To M41+). Honest but ambiguous; resolve when Open Decision 7 lands.

## Downstream usability - broken

The PRD itself is clean - FR/NFR IDs are contiguous and unique, terms (view, sheet, occurrence, region, reading order, projection rules, ProjectionConstruct) are used consistently, and no cross-references dangle. The break is cross-artifact: the M40 architecture spine (`ARCHITECTURE-SPINE.md`, written 09:18:45, after this PRD's 09:18:01 write) still binds its AD-9..AD-18 to the intermediate FR-1..FR-19 numbering and to the pre-rename "Composition" paradigm, so it cannot source-extract from this PRD.

### Findings
- **critical** Spine AD-to-FR bindings do not resolve against this PRD (Downstream usability, ARCHITECTURE-SPINE.md AD-9..AD-18). Examples verified against the current PRD: AD-9 "Binds: FR-1, FR-2, FR-5, FR-6" but constructs are FR-7..FR-12; AD-10 binds FR-2 (View And Sheet) but functional regions are FR-4; AD-16 binds FR-12 (Concrete Types) but routing non-goal is FR-17; AD-18 binds FR-6 (Projection Rules And Selection) but one-composition-authority is FR-10. *Fix:* re-run the spine bindings against the final FR-1..FR-22 numbering before spine finalize.
- **critical** Spine paradigm and deferrals are stale versus the Projection Reality rename (Downstream usability, ARCHITECTURE-SPINE.md Paradigm/Deferred). The spine still frames M40 as "composition adds one idea", with no decisions for view, sheet, occurrence, projection rules, or projection selection, and its Deferred section still says professional routing and label engine are "M41+" with AI auto-layout "not scheduled" - the PRD roadmap moves routing to M45, labels to M42/M44 territory, and AI to M46. *Fix:* update spine Paradigm and Deferred to the Projection Reality model and the approved roadmap, or mark the spine superseded pending re-distillation.

## Shape fit - strong

Capability-spec shape fits this internal technical milestone: no user journeys (correct - single-operator engineering-tool reality, no consumer UX claims), explicit existing-code references (brownfield discipline), and a clear chain-top position (feeds architecture -> stories). Every repo claim I checked verifies: `:kernel:drawing-composition` is wired in `settings.gradle.kts`; `CabinetCompositionCompiler`, `CabinetRoutingCompiler`, `CabinetVisualTransformCompiler`, and `DrawingSheetCompositionCompiler` exist under `kernel/drawing-composition`; `AthenaProfessionalDrawingCompiler` imports `DrawingSheetCompositionCompiler` and emits `authority = "drawing-composition"`; `CabinetCompositionEvidence` is present in `CabinetCompositionCompiler.kt`; the reference image is 1050x720 and `draft/screenshort/README.md` confirms the QElectroTech gallery renderer-layer provenance; the M39 retrospective confirms 8 routes, 0 route/body intersections, 7 placed node boxes, and 28 label collisions at fit-to-screen zoom.

## Mechanical notes

- **ID continuity:** FR-1..FR-22 and NFR-1..NFR-8 contiguous and unique; Acceptance Criteria 1-10 resolve to FRs without gaps.
- **Glossary drift:** "reference viewport" (PRD FR-18 / Acceptance 5) vs "fit-to-screen zoom" (M39 retrospective, addendum) - unify.
- **Terminology:** PRD consistently says "Projection Reality" with "projection constructs"; the spine still says "Composition" as the milestone name - cross-artifact mismatch, see critical findings above.
- **Assumptions index:** inline `[ASSUMPTION]` tags (roadmap, example subject) are both echoed in Open Decisions 7 and 4; roundtrip holds.
- **Sections:** Essential spine sections present (Purpose, Thesis, Scope, FRs, NFRs, Acceptance, Handoffs); Success Metrics section is absent but acceptance metrics carry that role for an internal technical milestone.

## Reviewer files

- `review-rubric.md` (this file)
