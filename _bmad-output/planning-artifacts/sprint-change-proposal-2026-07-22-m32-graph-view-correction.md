# M32 Sprint Change Proposal: Cabinet-First Graph Authoring Correction

Status: Approved by the user through the 2026-07-22 diagnosis review and "move on" instruction.
Mode: Batch
Scope: Moderate, contained within M32 Epic 7

## 1. Issue Summary

Story 7.5 selected `documentation` as the only customer-visible projection even though Athena's
runtime defaults to `cabinet`. The resulting Graph View hides its active and most useful projection,
then exposes Documentation sheet and cross-reference controls in the primary tool row when the
user switches views.

Story 7.3 proved only that the Create Device panel was visible, frontmost, and structurally
complete. The M32 E2E proof did not preview, accept, persist, reproject, or reopen a created device.
In the default M32 launch, Graph View is the active editor, so the Preview action is disabled and
the control cannot complete its advertised workflow.

Confirmed evidence:

- `AthenaRuntimeProjectionSession.activeProjectionViewId` prefers `cabinet`.
- `AthenaGraphWorkbenchWidget.resolveVisibleProjectionViews` exposes only `documentation`.
- M32 product smoke requires `documentation` as the sole visible projection.
- Live M32 proof reported `previewButtonDisabled=true` and source-editor guidance.
- The create-panel smoke asserts geometry and control presence only.

## 2. Impact Analysis

### Epic And Story Impact

- Epic 7 remains valid but requires Story 7.6 before retrospective.
- Story 7.5 remains an accurate record of the rejected documentation-first implementation and
  stays in `review`; it is not rewritten as if the defect never existed.
- Story 7.6 corrects the product behavior and replaces shell-only acceptance with transaction E2E.
- No new epic is required.

### Artifact Impact

- PRD: no change. UJ-1, FR-30, FR-34, FR-35, SM-4, and M32 core acceptance already require a
  usable customer demo and non-regressing authoring behavior.
- Architecture: no change. Mutation Authority remains the accepted source-change authority; Theia
  continues to submit intent and consume returned edits/proof.
- Epics and sprint status: add Story 7.6.
- Frontend: restore Cabinet as the primary projection control, separate Documentation navigation
  from global tools, and remove the active-source-editor UI gate from graph-origin creation.
- LSP: establish a governed canonical source snapshot when authoring starts without `didOpen`.
- E2E: prove preview, accept, source persistence, reprojection, and reopen from a temporary M32
  workspace without first opening an Athena editor.

## 3. Recommended Approach

Use a direct adjustment in Epic 7.

1. Expose Cabinet as the single primary M32 customer projection and keep unfinished projections as
   compatibility/programmatic surfaces.
2. Render Documentation sheet and cross-reference navigation in a distinct contextual navigation
   region, never in the global tool group.
3. Let Graph View submit create intent without an active source editor. The LSP lazily tracks the
   canonical project source, discovers capability evidence, and keeps Revision Guard and Mutation
   Authority authoritative.
4. Allow acceptance while Graph View is active; local editor guards apply only when the target
   source is actually open. Backend acceptance remains fail-closed for stale revisions.
5. Add a destructive-safe E2E fixture copy under the OS temporary directory and verify the created
   device in source, projection, and a reopened IDE session.

Alternatives rejected:

- Reverting all Epic 7 work would discard valid package-backed rendering and geometry proof.
- Keeping Documentation primary contradicts runtime behavior and the clarified customer priority.
- Auto-opening a source editor as a hidden prerequisite masks the graph-authoring defect.
- Letting Theia serialize or directly write `.athena` violates M31/M32 authority boundaries.

Effort: Medium
Risk: Medium, concentrated at LSP document tracking and Electron E2E boundaries

## 4. Detailed Change Proposal

### Epic 7

Add Story 7.6, `Correct Cabinet-First Graph Authoring UX`, after Story 7.5.

### Product Contract

Old:

- Documentation is the only visible projection control.
- Cabinet is hidden compatibility despite being runtime default.
- Documentation navigation shares the primary tool row.
- Create Device requires an already active Athena source editor.
- Smoke proves only panel presence and geometry.

New:

- Cabinet is the visible and active primary customer projection.
- Documentation remains available through compatibility/programmatic switching until its product
  UX is completed.
- Documentation navigation is contextual and does not resize the global tool group.
- Create Device previews and accepts from Graph View with backend-governed canonical source context.
- E2E proves the complete mutation lifecycle and reopen persistence.

## 5. Implementation Handoff

Owner: M32 Story 7.6 developer agent

Success criteria:

- Cabinet is visible and active on initial M32 Graph View.
- The primary toolbar does not gain sheet or cross-reference text buttons after projection change.
- Create Device can preview and accept when Graph View is active and no Athena editor was opened.
- The accepted device is present in `.athena`, appears after reprojection, and survives IDE reopen.
- Theia does not construct authoritative source text.
- Existing package-backed representation, route-anchor, viewBox, transparency, and Outline proofs
  remain green.
- Final polish/purge and encoding audit pass; `.tools` remains excluded.

## Change Navigation Checklist

- [x] 1.1 Triggering stories identified: 7.3 and 7.5.
- [x] 1.2 Problem classified as failed UX acceptance and misunderstood product priority.
- [x] 1.3 Live proof, source trace, runtime tests, and smoke assertions recorded.
- [x] 2.1 Epic 7 remains viable with one corrective story.
- [x] 2.2 Story 7.6 added; no epic redefinition required.
- [x] 2.3 Future milestones are unaffected except they inherit stronger E2E standards.
- [x] 2.4 No future epic is invalidated and no new epic is required.
- [x] 2.5 Story 7.6 precedes the Epic 7 retrospective.
- [x] 3.1 PRD goals support the correction; no PRD edit required.
- [x] 3.2 Architecture authority boundaries support the correction; no architecture edit required.
- [x] 3.3 Graph View toolbar, navigation, and create flow require UX changes.
- [x] 3.4 Frontend, LSP, smoke, story, status, and cleanup ledger require updates.
- [x] 4.1 Direct adjustment selected; medium effort and medium risk.
- [x] 4.2 Rollback rejected as disproportionate.
- [x] 4.3 MVP remains achievable without scope reduction.
- [x] 4.4 Direct adjustment selected for product value and architectural consistency.
- [x] 5.1 Issue summary completed.
- [x] 5.2 Epic and artifact impacts completed.
- [x] 5.3 Recommended path and alternatives completed.
- [x] 5.4 MVP impact and action sequence completed.
- [x] 5.5 Developer-agent handoff defined.
- [x] 6.1 Applicable checklist sections completed.
- [x] 6.2 Proposal checked for consistency and actionable criteria.
- [x] 6.3 User approval recorded from the diagnosis review and "move on" instruction.
- [x] 6.4 Epic and sprint status receive Story 7.6.
- [x] 6.5 Handoff and success criteria are explicit.
