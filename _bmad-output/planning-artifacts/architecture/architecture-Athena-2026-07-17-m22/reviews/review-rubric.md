---
review: good-spine-rubric
target: ../ARCHITECTURE-SPINE.md
source_prd: ../../../prds/prd-Athena-2026-07-17-m22/prd.md
source_addendum: ../../../prds/prd-Athena-2026-07-17-m22/addendum.md
date: 2026-07-17
verdict: needs-revision-before-story-handoff
---

# Good-Spine Rubric Review - Athena M22

## Verdict

Needs revision before story handoff.

The spine is mechanically clean and directionally consistent with the PRD, addendum, M21 inherited invariants, and the observed brownfield repo seams. It captures the core M22 authority model well: upstream semantics, constraints before facts, optional ELK behind an adapter, paint-only rendering, and governed source mutation. The remaining issues are not structural bloat; they are missing or under-enforceable invariants that could let independent story builders diverge on the acceptance proof, deterministic optimization behavior, and ELK execution/dependency containment.

## Review Inputs

- Target spine: `ARCHITECTURE-SPINE.md`
- Source PRD: `prd.md`
- Source addendum: `addendum.md`
- Parent spine checked: `architecture-Athena-2026-07-17-m21/ARCHITECTURE-SPINE.md`
- Brownfield spot-check: CodeGraph confirmed the existing seams named by the spine exist in the repo shape, including `kernel/layout-model`, `kernel/layout-engine`, `ide/theia-frontend`, runtime source mutation services, and example-project conventions.
- Deterministic lint pass: `lint_spine.py --workspace ...architecture-Athena-2026-07-17-m22` returned `ok: true`, `total_findings: 0`.

## Findings

### High - AD-7 does not make professional layout acceptance enforceable enough

Location: `ARCHITECTURE-SPINE.md:129`

Rubric impact: every AD's Rule must be enforceable and must actually prevent its stated divergence; if a spec drove the spine, it must cover the spec's capabilities.

Evidence:

- AD-7 says the sample must keep power, protection, controller, HMI, terminals, and load path "visually identifiable" and that routing/labels are judged by "schematic scanability" (`ARCHITECTURE-SPINE.md:133`).
- PRD FR-2 requires acceptance references and checks covering spacing, grouping, routing, label readability, and sheet scanability (`prd.md:153`, `prd.md:161`).
- Addendum screenshot guidance says the references should define sheet density expectations, professional spacing/alignment cues, basic orthogonal edge routing, label overlap avoidance, and visible engineering zones (`addendum.md:190`).
- The spine has an open question for which screenshots define the comparison set (`ARCHITECTURE-SPINE.md:248`), but it does not bind the acceptance artifact shape or minimum reviewable criteria.

Why this matters:

Two story teams could both satisfy AD-7 while using incompatible or weak definitions of "professional" and "scanable." One could optimize for no overlap, another for zone grouping, another for visually tidy ELK output. That is exactly the divergence AD-7 is supposed to prevent, and it maps to the PRD's primary visible success.

Recommendation:

Tighten AD-7 or add a convention/open item that binds the M22 visual acceptance artifact before optimization stories close. It does not need numeric perfection, but it should require a named comparison set and review checklist for at least: power/protection/controller/HMI/terminal/load zones, spacing, grouping, orthogonal edge routing, label overlap, and M21 baseline comparison.

### Medium - Stable replay is required but not operationally specified

Location: `ARCHITECTURE-SPINE.md:86`

Rubric impact: every AD's Rule must be enforceable and actually prevent its stated divergence.

Evidence:

- AD-2 requires the same governed input to produce stable replayable output (`ARCHITECTURE-SPINE.md:90`).
- PRD SM-6 requires repeated runs on the same governed input to produce stable layout facts and stable visible presentation (`prd.md:357`).
- The spine does not bind canonical ordering, tie-breakers, solver seed policy, adapter normalization determinism, or output snapshot comparison rules.

Why this matters:

Optimization systems can be nondeterministic even when the architecture boundary is correct. Without a deterministic ordering/tie-breaker invariant, independent implementations could emit different facts for equivalent inputs and still claim to satisfy "normalized Athena facts."

Recommendation:

Add a small determinism rule or convention: optimizer inputs are canonicalized; subject/occurrence/sheet ordering is stable; equal-cost placements use deterministic tie-breakers; any adapter output is normalized before comparison; replay tests compare layout facts, not only screenshots.

### Medium - ELK execution and dependency containment is too deferred for an M22 spike

Location: `ARCHITECTURE-SPINE.md:94`, `ARCHITECTURE-SPINE.md:231`, `ARCHITECTURE-SPINE.md:247`

Rubric impact: nothing under Deferred should let two units one level down diverge; every owned dimension must be decided, deferred, or opened with a revisit condition.

Evidence:

- AD-3 correctly makes ELK optional and subordinate (`ARCHITECTURE-SPINE.md:99`).
- The stack table avoids binding a final ELK version or dependency, which is appropriate for "no final stack decision."
- Deferred says final ELK dependency, package, or solver-stack selection waits until a later technology-selection milestone (`ARCHITECTURE-SPINE.md:231`).
- Open Questions separately ask whether M22 should include an ELK dependency or isolate it behind an experimental adapter package before the adapter story starts (`ARCHITECTURE-SPINE.md:247`).
- The addendum explicitly allows ELK for graph arrangement, hierarchy layout, orthogonal edge routing assistance, spacing, and collision avoidance, while forbidding authority/persistence/direct renderer truth (`addendum.md:48`, `addendum.md:55`).

Why this matters:

The PRD includes an optional experimental ELK adapter spike in M22, so story builders will need an execution envelope even if the final stack is deferred. Without a binding containment rule, one team could add ELK to frontend runtime code, another to JVM backend layout, another behind a local package, and another behind a network/service process. All could claim "adapter boundary" while creating incompatible operations and build implications.

Recommendation:

Keep the final dependency/version decision deferred, but bind the M22 spike envelope before the adapter story starts: local-only, no remote service, no renderer persistence/direct truth, isolated adapter package/module, normalized deterministic output, and removable without changing layout facts or renderer contracts.

### Low - Round-trip syntax is correctly open, but story sequencing must treat it as a blocker

Location: `ARCHITECTURE-SPINE.md:104`, `ARCHITECTURE-SPINE.md:246`

Rubric impact: open questions are acceptable only when their revisit condition protects the next build level from divergence.

Evidence:

- AD-4 and AD-5 correctly constrain round-trip scope to placement, alignment, and grouping through governed mutation authority.
- The exact `.athena` layout-hint syntax is open until implementation stories that mutate source (`ARCHITECTURE-SPINE.md:246`).
- The addendum recommends starting with a small layout block or projection hint and deferring route/label persistence unless mechanically simple (`addendum.md:152`).

Why this matters:

This is not a current spine failure if the revisit condition is honored. It becomes a story-handoff risk if mutation stories begin before the syntax direction is resolved, because source shape and IDE preview semantics would diverge.

Recommendation:

Carry this as a blocking precondition on source-mutating stories: choose layout block vs projection hint vs subject-local hint before any implementation persists layout adjustments.

## Checklist Walk

| Good-spine criterion | Result | Notes |
| --- | --- | --- |
| Fixes real divergence points for the next level | Partial | Authority, scope, ELK posture, and mutation path are fixed. Acceptance criteria, deterministic replay mechanics, and ELK execution envelope still leave story-level divergence. |
| Misses no major divergence point | Partial | Operational/build implications of the optional ELK spike are not sufficiently bound for M22 implementation. |
| AD Rules are enforceable | Partial | AD-1 through AD-6 and AD-8 are mostly enforceable. AD-7 is too subjective without an acceptance artifact/checklist. AD-2 needs deterministic replay mechanics. |
| Deferred items are safe | Partial | Most Deferred items are safe scope guards. The final ELK stack decision is safe only if a narrower M22 spike envelope is bound first. |
| Named tech verified-current | Pass with caveat | The spine intentionally binds no ELK version. Existing Theia and renderer boundaries are brownfield references rather than new tech choices. |
| Ratifies brownfield codebase | Pass | Repo seams align with the structural seed at this altitude. No contradiction found in the spot-check. |
| Covers PRD capabilities | Pass with caveat | FR-1 through FR-12 are mapped. FR-2/FR-6 need stronger acceptance enforceability; FR-5 needs adapter containment. |
| Parent spine consistency | Pass | M22 preserves M21 semantic authority, layout strategy boundary, normalized facts, paint-only renderer, Theia proof, and deferred ecosystem/physical-routing boundaries. |
| Owned dimensions decided/deferred/open | Partial | Domain/data/UI/proof dimensions are covered. Operations/build/runtime envelope for the optional ELK spike is under-specified. |

## Positive Notes

- The spine avoids the common failure of outsourcing authority to ELK. AD-3 and the diagrams consistently normalize helper output into Athena layout facts.
- The inherited M21 invariants are treated as binding constraints rather than re-decided local preferences.
- Scope guardrails are strong: public ecosystem work, broad IEC/QElectroTech ingestion, cabinet authoring, physical routing, AI layout, full EPLAN parity, and free-form drawing behavior are repeatedly excluded.
- The Theia coherence and governed mutation path are aligned with the PRD and existing repo seams.

## Recommended Gate Decision

Do not edit the spine during this review. Before using it as story substrate, revise the spine to:

1. Bind a reviewable professional layout acceptance artifact/checklist for the M22 sample and M21/reference comparison.
2. Add deterministic replay rules for optimizer and adapter output.
3. Bind the M22 ELK spike execution/dependency containment envelope while still deferring the final stack decision.
4. Treat `.athena` layout-hint syntax selection as a blocker for source-mutating round-trip stories.

