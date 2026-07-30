# M36 Spine Rubric Review

## Verdict

Pass after one correction: physical-policy evaluation is compiler-owned, with drawing composition as
a consumer only.

## Findings Applied

1. **High: physical-policy owner was ambiguous.**
   - Fixed AD-8 so `compiler` evaluates `physical-model` policy before accepted facts reach
     `drawing-composition`.

## Checks Passed

- Every M36 PRD capability maps to a module and one or more architecture decisions.
- Source, SVG, planner, physical policy, LSP, and renderer have distinct authority boundaries.
- Connection IR and Layout Graph are explicitly transient.
- ELK is a replaceable adapter, not a semantic or persistence dependency.
- Deferred items do not leave a required M36 core decision unowned.
- Named technology is limited to optional ELK; no current version is bound without a web-verifiable
  dependency decision.
