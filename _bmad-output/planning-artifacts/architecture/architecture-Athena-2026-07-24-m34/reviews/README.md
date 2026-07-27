# M34 Review Evidence

These files are dated review snapshots, not current architecture authority. They intentionally retain
the findings and verdicts that caused later revisions.

Current implementation authority is, in order:

1. `../../../prds/prd-Athena-2026-07-24-m34/prd.md` and `addendum.md`
2. `../ARCHITECTURE-SPINE.md`
3. `../../../../implementation-artifacts/m34/epics.md`

The current approved direction is one compiler authority and one exclusive source authority per
definition: native typed Athena graphics for simple visuals or governed annotated SVG with the
versioned closed `data-athena-*` profile for complex visuals. Both lower to the same canonical model;
sidecar merge, raw SVG runtime, XML manifests, SVG-specific runtime IR, and project/policy truth in
SVG are forbidden. See
`../../../sprint-change-proposal-2026-07-24-m34-annotated-svg-authority.md`.

Review findings are considered resolved only when reflected in the current PRD/spine/stories and
covered by architecture lint, requirements coverage validation, and implementation acceptance
evidence. Review text itself is never product authority.
