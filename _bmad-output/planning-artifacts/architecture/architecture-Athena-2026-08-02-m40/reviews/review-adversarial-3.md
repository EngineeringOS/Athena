# Adversarial Review 3 - Athena M40 Projection Reality (Architecture Spine)

Run: 2026-08-02 (third pass, after PRD finalize and decision resolution)
Scope: `ARCHITECTURE-SPINE.md` only, verified against final `prd-Athena-2026-08-02-m40/prd.md`
(FR-1..FR-22), inherited M39 spine invariants (AD-1..AD-8), and repo evidence via source scan.

## Verdict

Pass. No critical or high finding. Every architecture decision is consistent with the finalized
PRD and the M39 invariants. Two medium findings are binding-completeness gaps in the spine text
(decisions already implied elsewhere), not design conflicts; three low/process items are
housekeeping.

## Findings And Resolution State

1. **[medium - not yet applied] No spine decision binds FR-13..FR-15 (Projection Compiler
   epic).** FR-13 (one thin typed `RealityTransformation<InputReality, OutputReality>`
   Engineering -> Projection), FR-14 (deterministic compilation), and FR-15 (projection boundary
   validation - no coordinate/anchor/lane/route/stroke/label/paint-order fact in a snapshot) have
   no dedicated AD. AD-9 covers "projection compiler is the only creator of projection
   snapshots", and the Consistency Conventions row cites inherited AD-6 for thin transformations,
   but determinism and boundary validation are unbound. Suggested fix: extend AD-9 or add one AD
   binding FR-13..FR-15, plus a Consistency Conventions row naming boundary validation.
2. **[medium - not yet applied] FR-9 (construct validation) and FR-11 (grouped endpoint
   integrity) have no explicit AD binding.** AD-15 binds only FR-12. FR-9's named-diagnostic
   rules (empty construct, duplicate identity, missing trace, invalid nesting) and FR-11's
   "grouped endpoints remain one relationship through Projection" appear only in the Consistency
   Conventions table. Suggested fix: add FR-9 to AD-15 and bind FR-11 explicitly (AD-10 or a new
   line in AD-11).
3. **[low - not yet applied] AD-13 binds FR-16, FR-17; the Spatial Quality metrics portion
   belongs to FR-18.** AD-13's rule text already covers the analyzer and the M39 baseline, so the
   binding list is stale, not the content. Suggested fix: `Binds: FR-16, FR-18`.
4. **[low - not yet applied] Structural Seed still says `examples/m40/ ... (subject TBD - PRD
   Open Decision 4)`.** Decision 4 resolved 2026-08-02: subject is the rolling-shutter control
   system matching `draft/screenshort/equipement_d'un_volet_roulant.png`. Deferred section
   already records the decision; the Structural Seed comment contradicts it.
5. **[process] Spine status is `draft` while the PRD is `final` and all seven decisions are
   resolved.** After the two medium/low text fixes above are applied, run a finalize event and
   flip status to `final`, mirroring the PRD finalize, before epic/story creation.

## Verified Consistent (no action)

- AD-9..AD-18 bindings match final FR titles/meanings (checked FR-1..FR-22 one by one).
- AD-18 retirement evidence matches repo scan: `settings.gradle.kts:60` module wiring;
  `AthenaProfessionalDrawingCompiler.kt:894,910` emits `authority = "drawing-composition"`;
  `AthenaCompilerCompilationSupport.kt:483-495` gates on the `professional-connection-drawing`
  policy; `AthenaCabinetProjectionCompiler.kt` composes via Cabinet compilers; LSP
  `AthenaDrawingCompositionPayload` surface exists; stale M34/M37/M38 + cabinet tests reference
  the retired path.
- AD-11 domain-neutrality matches the M39 `ConnectionVerb` pattern; kernel keeps only the
  `ProjectionConstruct` contract.
- Inherited M39 AD-1..AD-8 quoted verbatim and read-only; no local AD contradicts them.
- Roadmap M41-M46 in the spine matches PRD Decision 7 and Handoff To M41+.
- Density/collision/occupancy/label-pressure live in AD-14 as acceptance evidence (FR-18), not
  an epic - PM feedback honored.
- AD-16 enforces the M40 no-routing boundary consistent with PRD FR-17 and roadmap M45.

## Open Items

None blocking. Items 1-4 are small text fixes; item 5 is the finalize step.
