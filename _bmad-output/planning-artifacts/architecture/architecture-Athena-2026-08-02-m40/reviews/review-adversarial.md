# Adversarial Review - Athena M40 Projection Reality (PRD + Architecture Spine)

Run: 2026-08-02 (second pass, after decision resolution)
Scope: `prd-Athena-2026-08-02-m40/prd.md` + `addendum.md` + `ARCHITECTURE-SPINE.md`; repo
evidence via CodeGraph and source scan.

## Verdict

Pass. Every critical/high finding from the first pass is resolved in the PRD text. The milestone
is decision-complete: all seven open decisions are recorded with evidence, the stale
`:kernel:drawing-composition` authority has a decided retirement disposition, and the remaining
findings are low/info with no action.

## Findings And Resolution

1. **[critical - resolved] FR-10 retirement left the professional drawing policy branch live.**
   `AthenaCompilerCompilationSupport.kt:deriveProfessionalControlDrawing()` gates on the
   `professional-connection-drawing` projection policy and calls `AthenaProfessionalDrawingCompiler`.
   *Fix applied:* FR-10 consequences now require removing the branch and the policy target
   surface, not leaving silent no-ops.
2. **[high - resolved] No conflict rule between M40 `view` declarations and the existing
   projection-policy selection surface.** `AthenaProjectionPolicyCompiler` selects views today.
   *Fix applied:* FR-6 consequences state view declarations are the sole authoring surface for
   view selection; the policy surface is retired or rewritten, not shimmed.
3. **[high - resolved] Density target gameable by label suppression.** *Fix applied:* FR-18
   forbids label suppression, measures the full emitted label set, and requires label count
   reporting.
4. **[high - resolved] Fit-to-screen screenshots are not a deterministic metric source.**
   *Fix applied:* FR-18 metrics are computed deterministically from Presentation Document
   bounds; screenshots confirm visual state only.
5. **[medium - resolved] FR-9 missed empty constructs, duplicate construct identity, invalid
   nesting, missing source trace.** *Fix applied:* all four fail with named diagnostics before
   Spatial.
6. **[medium - resolved] FR-5 reading order lacked duplicate/unknown-entry diagnostics.**
   *Fix applied:* reading order must be a permutation of declared sheets; duplicates/unknowns
   fail.
7. **[medium - resolved] FR-2 lacked view-with-no-sheets and sheet-with-no-occurrence rules.**
   *Fix applied:* both fail with plain diagnostics.
8. **[medium - resolved] M39 baseline vs M40 example comparison was apples-to-oranges.**
   *Fix applied:* FR-18 primary comparison is the same M40 source flat vs composed; M39 baseline
   is cross-reference only.
9. **[medium - resolved] Density/occupancy/label pressure were undefined.**
   *Fix applied:* FR-18 defines each metric.
10. **[medium - resolved] FR-7 kernel-neutrality audit would fail until the FR-10 retirement
    lands.** *Fix applied:* FR-7 pins the audit scope (`kernel/*/src/main`, after retirement in
    the same epic) and story ordering is planned retirement-first in Epic 2.
11. **[medium - resolved] FR-19 proof could pass without exercising every construct.**
    *Fix applied:* example must declare at least one of each electrical construct plus one
    region; verifier asserts presence in the snapshot.
12. **[medium - resolved] Retirement of the Cabinet path left dangling tests/docs.**
    *Fix applied:* FR-10 consequences require rewriting/deleting the deletion-gate and M36/M37
    cabinet tests, cleaning all references, and recording the retirement in the retrospective.
13. **[low - resolved] Compile-time counter-metric was machine-dependent.**
    *Fix applied:* Acceptance 11 defines median of three runs on the same machine vs the M39
    example.
14. **[low - resolved] LSP surface scope for constructs was unstated.**
    *Fix applied:* FR-20 states LSP recompiles/validates M40 source with no regression and no new
    LSP protocol surface.
15. **[info] `reading-order [S1]` redundancy in the syntax proposal.** Sheet order is already
    declaration order; the optional `reading-order` clause is a permutation check. No action:
    acceptable, useful as an explicit diagnostic surface.

## Open Items

None. All decisions recorded; no blockers remain for epics/story creation.
