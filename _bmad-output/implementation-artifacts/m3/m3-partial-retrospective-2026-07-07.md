# M3 Partial Retrospective - 2026-07-07

## Status

- This is a **partial milestone retrospective**, not a close-out retrospective.
- Reason:
  - `epic-3` is still `backlog`
  - M3 stories are mostly at `review`, not `done`
  - the sprint tracker should **not** be mutated to claim M3 completion yet

## Reviewed State

- Epic 1: platform boundary and pass-pipeline refactor implemented to review state
- Epic 2: external proof domains and proof corpus implemented to review state
- Epic 3: not started
- Latest verification evidence:
  - `java25; .\gradlew.bat --no-daemon --console=plain build` passed

## What Went Well

- The M3 implementation stayed aligned with the manifesto and architecture spine:
  - canonical semantics stayed kernel-owned
  - plugins stayed typed and non-sovereign
  - layout, geometry, and rendering remained downstream
- The physical workspace structure improved:
  - `kernel/plugins/plugin-api`
  - `kernel/plugins/plugin-host`
  - `extensions/domain-electrical`
  - `extensions/domain-dummy`
- Examples were treated as architecture contracts instead of demo clutter:
  - `examples/m0/`
  - `examples/m2/`
  - `examples/m3/`
- Java 25 and sequential Windows Gradle verification became an explicit operating rule instead of tribal knowledge.

## What Hurt

- Sprint bookkeeping lagged the actual engineering state.
  - M3 is reviewable, but the tracker still does not describe a clean “done” sequence.
  - That makes milestone-level closure ambiguous.
- Documentation discipline still needs tightening.
  - The dummy-domain README documents the wrong authored marker syntax.
  - Some Chinese documentation surfaces still need careful encoding validation.
- The most important M3 proof is still deferred.
  - The zero/electrical/dummy/both hosted verification matrix is an Epic 3 contract, not optional polish.

## Key Lessons

1. Hosted-plugin presence is not the same thing as hosted-domain coverage.
   - The compiler must distinguish “a lowerer exists” from “the active hosted set actually claimed the authored declarations.”
2. Examples are the right place to publish architecture proof.
   - `examples/m3/` is already the correct seed for Epic 3 matrix automation.
3. Windows build discipline must stay explicit.
   - Sequential Java 25 verification is not a convenience rule; it is required for reliable milestone evidence in this workspace.
4. Story and sprint tracking must be closed immediately after review.
   - Otherwise the implementation reality and BMAD artifact state drift apart.

## Strengths To Preserve For Epic 3

- Keep the authored DSL generic.
- Keep `Engineering IR` canonical.
- Keep hosted discovery governed through source + approval.
- Keep runtime ownership of mutation and active projection state.
- Keep milestone-local examples and artifacts under `_bmad-output/implementation-artifacts/m3/`.

## Risks Carrying Into Epic 3

- If the domain-coverage gap is not fixed first, the upcoming zero/one/multi-plugin matrix can produce misleading “green” results.
- If sprint statuses remain at `review`, Epic 3 can start on top of unclear closure criteria.
- If dummy-domain docs are left inaccurate, future extension authors will copy the wrong syntax into new fixtures and tests.

## Action Items

1. Fix the compiler’s hosted-domain coverage diagnostic before starting Epic 3 matrix work.
   - Owner: development
2. Correct dummy-domain README syntax in both English and Chinese.
   - Owner: documentation/development
3. Normalize M3 story closure after review fixes land.
   - Owner: development / project lead
4. Start Epic 3 with matrix automation as the first-class proof target.
   - Required states: zero plugins, electrical only, dummy only, both together

## Next-Epic Preparation

- Epic 3 should start from the review-corrected M3 baseline, not from the current ambiguous review state.
- The first Epic 3 story should treat the hosted verification matrix as an architecture contract with explicit Java 25 commands and deterministic fixture reuse from `examples/m3/`.

## Bottom Line

- M3 has already proven the architectural direction.
- M3 has **not** yet proven the full kernel-boundary claim end-to-end, because Epic 3’s verification matrix is still pending.
- The right next move is not new surface area. It is:
  - close the two current review findings
  - normalize sprint closure
  - automate the hosted verification matrix
