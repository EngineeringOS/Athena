# Edge Case Review - Athena M40 Projection Reality (PRD + Architecture Spine)

Run: 2026-08-02
Method: exhaustive path walk over every FR consequence and boundary condition in the PRD.
Output: only unhandled paths at review time.

## Findings (all resolved in PRD text)

1. **View with zero sheets** - unhandled in first pass; FR-2 now fails it with a plain
   diagnostic.
2. **Sheet with no occurrences (construct-only or region-only sheet)** - unhandled; FR-2 now
   requires every sheet to contain at least one occurrence directly or transitively.
3. **Reading order with duplicate sheet** - unhandled; FR-5 now requires permutation of declared
   sheets.
4. **Reading order referencing unknown sheet** - unhandled; FR-5 now fails it.
5. **Empty construct (zero members)** - unhandled; FR-9 now fails it.
6. **Duplicate construct identity** - unhandled; FR-9 now fails it.
7. **Construct without source trace** - unhandled; FR-9 now fails it.
8. **Invalid construct nesting (rung containing rail)** - unhandled; FR-9 now fails it.
9. **Same subject in two views** - handled: occurrence identity is view-local per
   `ProjectionReality.identityRules` ("occurrence" = subject + view-local id); no change needed.
10. **Occurrence without engineering source / duplicate occurrence** - handled by FR-3.
11. **Group/strip/coil/rung referencing missing subject** - handled by FR-9.
12. **Empty region / missing occurrence in region** - handled by FR-4.
13. **Projection snapshot carrying spatial/presentation facts** - handled by FR-15.
14. **Both view declarations and projection-policy selection in one source** - unhandled in
    first pass; FR-6 now declares view declarations the sole selection surface.
15. **Metric gamed by label suppression** - unhandled; FR-18 now forbids it and reports label
    count.

## Verdict

Clean. All enumerated boundary paths are now explicitly handled in the PRD consequences.
