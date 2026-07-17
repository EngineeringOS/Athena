# M22 Deterministic Layout Replay Proof

This proof records how M22 layout optimization is verified before reviewers judge the visual sheet.
The rule is simple: layout facts are compared across repeated runs before screenshot or manual visual
checks are considered.

## Fact-Level Replay Gate

The kernel replay gate lives in `kernel/layout-engine/src/test/kotlin/com/engineeringood/athena/layout/engine/SchematicLayoutEngineTest.kt`.

It verifies:

- optimizer input canonicalization for unordered intent and constraint snapshots
- stable placement facts across repeated runs
- stable region facts across repeated runs
- stable group facts derived from governed constraints
- stable applied constraint ids through deterministic tie-breakers

The relevant test is `optimization boundary canonicalizes inputs and emits stable Athena layout
facts`, which compares `first` and `second` optimizer results before any visual proof is used.

## Adapter-Normalized Facts

M22 does not require an external layout adapter. If adapter-normalized facts are present in a later
M22 story, they must be compared only after Athena normalization, using normalized Athena layout
facts rather than raw helper output.

## Visual Acceptance Checks

After the fact-level gate, reviewers use `M22-LAYOUT-ACCEPTANCE.md` to inspect the named checklist
items:

- zones
- spacing
- grouping
- basic orthogonal edge routing
- label overlap avoidance
- M21 baseline comparison

Screenshots or IDE inspection support the review, but they do not replace fact-level replay.
