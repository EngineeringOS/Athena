# Sprint Change Proposal: Reopen M41 Spatial Reality Recovery

Date: 2026-08-03
Project: Athena
Milestone: M41
Mode: Batch
Status: Approved for implementation

## 1. Issue Summary

### Trigger

M41 implementation and product proof exposed that the completed pass proved field presence and
placeholder behavior, not Spatial Reality. The milestone is reopened before any story remains
accepted.

### Evidence

- `SpatialPlacementCompiler` places occurrences in one row with `y = 0`.
- Construct bounds use a maximum member size instead of the union of member rectangles.
- Route compilation silently drops unresolved routes and falls back to first/last occurrences.
- `label-pressure` and `labelCount` equal route count without label geometry.
- Product E2E accepts a PNG larger than 1024 bytes and hardcodes renderer booleans.
- Focused tests pass while screenshots show a collapsed, unreadable top strip.

Historical evidence remains in
`_bmad-output/implementation-artifacts/m41/m41-delivery-audit-2026-08-03.md` and the PRD rubric.
The failed story files, screenshots, and retrospective are invalid proof and must not be reused.

### Precise Problem

M41 lacks one authoritative, typed, validated Spatial compilation path. It needs deterministic
two-dimensional placement, exact geometry and source trace, obstacle-safe basic routing, truthful
per-sheet metrics, and runtime product proof. Compatibility with the failed pass is not a goal.

## 2. Impact Analysis

### Epic Impact

All five M41 epics are affected. Preserve numbering and intent, but regenerate their stories and
acceptance criteria from atomic PRD consequences. Reset every epic and story to `backlog`; mark no
retrospective complete during recovery.

Epic 1: deterministic two-dimensional placement, bounds, alignment, regions, constructs, and grids.

Epic 2: exact anchors, obstacle-aware orthogonal routes, lanes, complete trace, and fail-closed
routing.

Epic 3: typed geometry facts, stable identity, ownership, source trace, and complete validation.

Epic 4: exact geometry metrics, per-sheet density/occupancy, and a published golden baseline.

Epic 5: dedicated M41 fixture, rebuilt product E2E/screenshots, and evidence-backed closure.

### Story Impact

Delete all 13 failed story files before recreation. Recreate and implement, in order:

1. 1.1 deterministic explainable 2D placement
2. 1.2 occurrence bounds, alignment, region bounds, construct envelopes
3. 1.3 per-sheet grid references
4. 2.1 exact port anchors and route endpoints
5. 2.2 obstacle-aware orthogonal routes and lanes
6. 2.3 complete route trace and no-fallback boundary
7. 3.1 typed geometry identity and source trace
8. 3.2 complete Spatial validation and diagnostics
9. 4.1 truthful geometry quality metrics
10. 4.2 density, occupancy, and golden baseline
11. 5.1 dedicated M41 four-reality example
12. 5.2 product E2E and fresh screenshots
13. 5.3 honest closure after 1.1-5.2 are done

Every story uses `bmad-create-story` followed by `bmad-dev-story`. Code review runs after each
story reaches `review`; status becomes `done` only after acceptance and records are complete.

### Artifact Conflicts And Required Changes

- PRD: replace vague or bundled requirements with atomic consequence IDs, subject-to-fact matrix,
  metric dictionary, observable exit gates, and explicit M41/M42-M45 boundaries. Remove label
  metrics from M41.
- Architecture: bind one `ProjectionSpatialCompiler`, typed per-sheet facts, generic solver
  adapters, fail-closed routes, complete validation before Presentation, and runtime proof gates.
- Epics and stories: regenerate from replacement PRD; preserve 1.1-5.3 numbering; remove weak
  quantifiers, self-comparison tests, script-text proof, and local baseline constants.
- Sprint status: reset all five epics and 13 stories to `backlog`; retrospective is `optional`.
- Product proof: rebuild kernel, LSP, frontend, and Electron; replace stale screenshots with
  desktop and narrow evidence under the M41 artifact folder.
- Source-set hygiene: remove proof/demo/sample/milestone types from `src/main` and delete stale
  bridges after callers migrate.

UI/UX, deployment, and infrastructure have no authority change. Presentation styling and labels
remain M42; rendering/export M43; readability optimization M44; professional routing optimization
M45.

## 3. Options And Recommendation

### Option 1: Direct Adjustment

Not viable. Small story edits cannot repair competing production pipelines, invalid metrics, silent
route loss, and false product proof. Effort would remain high with high regression risk.

### Option 2: Potential Rollback

Selected as artifact-level invalidation only. Delete failed M41 stories, screenshots, and closure
records, then recreate through BMad. Do not destructive-reset unrelated workspace changes or
pretend the failed code is accepted. Effort is medium; risk is controlled by numeric story order
and sequential verification.

### Option 3: PRD MVP Review

Required as scope clarification, not scope reduction. M41 exit bar is coherent Spatial Reality, not
QElectroTech parity. Defer labels, rendering/export, readability optimization, and professional
routing to M42-M45. Effort is high; this reduces long-term architecture risk.

### Recommended Path: Hybrid Option 2 + Option 3

Invalidate failed evidence, repair PRD/architecture/epics/sprint through BMad, then implement all
13 stories sequentially. This preserves business value while making acceptance observable and
preventing another false-green milestone.

## 4. Recovery Contract

- Authority chain: Athena source -> Engineering -> Projection -> Spatial -> Presentation -> Theia.
- Sheet extent: `1200 x 800`; drawing area: `SpatialRect(40, 60, 1120, 640)`; title block starts at
  `y = 740`.
- Region columns are left-to-right with 32-unit gutter; occurrence separation is at least 48;
  region and construct envelope padding is 24.
- Each visible Projection connection produces exactly one route with exact anchor endpoints,
  nonzero orthogonal segments, obstacle avoidance, lane identity, and complete source trace.
- Blocking metrics are occurrence overlap, construct containment failure, route/body intersection,
  and twist; all must be zero for closure.
- Density uses occurrence count divided by drawing-area units. Occupancy uses occurrence rectangle
  union area divided by drawing-area area. Region/construct overlays do not inflate occupancy.
- No fallback endpoint, silent route drop, renderer repair, label metric, or parity claim.

## 5. Handoff And Sequencing

Product/architecture artifact repair runs first: `bmad-prd`, `bmad-architecture`,
`bmad-create-epics-and-stories`, `bmad-sprint-planning`, then PRD validation and implementation
readiness. Delete invalid artifacts only after replacement planning marks them invalid.

For each story, the Developer agent invokes `bmad-create-story`, then `bmad-dev-story`; runs focused
tests, affected module tests, repository tests, audits, and BMad code review; fixes findings through
the story workflow; and commits only the story File List. Gradle commands run sequentially.

Story 5.2 rebuilds every affected runtime surface and produces fresh screenshots. Story 5.3 records
only verified statuses, command results, metrics, and screenshot paths.

## 6. Correct-Course Checklist

- [x] 1.1 Trigger identified: failed M41 proof and Story 1.1-5.3 pass.
- [x] 1.2 Core problem categorized: failed approach requiring different solution.
- [x] 1.3 Concrete evidence captured in delivery audit and PRD rubric.
- [x] 2.1-2.5 Epic impact assessed: all five epics reset, numbering/order preserved.
- [x] 3.1 PRD conflict assessed and replacement contract defined.
- [x] 3.2 Architecture conflict assessed and single Spatial pipeline defined.
- [N/A] 3.3 No UX authority change; labels remain M42.
- [x] 3.4 Testing, product proof, source hygiene, and documentation impacts defined.
- [x] 4.1 Direct adjustment evaluated and rejected.
- [x] 4.2 Artifact rollback/invalidation selected.
- [x] 4.3 MVP boundary reviewed and clarified.
- [x] 4.4 Hybrid path selected.
- [x] 5.1-5.5 Proposal, impact, rationale, sequencing, and handoff documented.
- [x] 6.1-6.2 Checklist and proposal consistency reviewed.
- [x] 6.3 User approval recorded: user confirmed recovery and BMad story workflows in this turn.
- [x] 6.4 Sprint reset is delegated to `bmad-sprint-planning`.
- [x] 6.5 Handoff responsibilities and success criteria defined.

## Approval

Approved by Aaron in the active recovery session on 2026-08-03. Conditions: use BMad for every
story creation and development; redo Story 1.1 through Story 5.3 in numeric order; verify before
status changes; do not claim parity or preserve failed proof.
