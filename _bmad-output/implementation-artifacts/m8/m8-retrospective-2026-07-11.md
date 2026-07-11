# M8 Retrospective

Milestone: `M8`
Title: `Unified Semantic Mutation Model`
Date: `2026-07-11`
Status: `completed`

## Reviewed Scope

- Epic 1: freeze the unified mutation authority
- Epic 2: prove the first graph-originated mutation paths
- Epic 3: unify review and reveal across source and graph

## Outcome

M8 achieved its core architectural goal: Athena now proves that source and graph are downstream clients of one Athena-owned mutation model instead of separate editing worlds.

The completed milestone now includes:

- explicit mutation categories across semantic mutation, projection mutation, and transient interaction
- one real graph semantic mutation path through Athena command intent
- one real graph projection mutation path through runtime-owned projection metadata
- one shared semantic review model for accepted source and graph consequences
- one canonical reveal path across source, graph, and semantic SCM
- one published proof corpus and verification path for repeatable inspection

## Review Verdict

No blocking implementation findings were identified in the final M8 closeout review.

Important scope clarification:

- M8 does prove source-originated mutation normalization, shared review, and shared reveal coherence
- M8 does not yet prove full canonical write-through execution for source editing
- the current source path is still preview-first, which matches Story `1.3` but should stay explicit in later roadmap and milestone discussions

## What Worked Well

- The milestone stayed narrow and resisted scope drift into broad graphical authoring.
- Reusing existing runtime command, projection, and semantic SCM services prevented a second semantics stack from emerging in the IDE.
- The graph workbench remained downstream of Athena-owned meaning even after real editing interactions were introduced.
- Publishing proof corpus artifacts alongside verification commands made the milestone easier to inspect and trust.

## What Hurt

- The phrase "source mutation" can be read too broadly; without explicit wording, people may assume M8 already includes full source write-through execution.
- Verification on this Windows workstation still depends on strict sequential JVM and Node execution with Java 25 activation.
- Review quality depends on keeping milestone docs synchronized with the exact proved behavior, not just the architectural intent.

## Key Lessons

1. Mutation-category discipline is foundational.
   - Separating semantic mutation, projection mutation, and transient interaction prevented the graph workbench from becoming an ungoverned edit surface.
2. Review coherence must stay downstream of semantic authority.
   - `kernel/semantic-scm` was the right place to preserve shared review language across source and graph.
3. Reveal coherence is a real architecture concern.
   - If source, graph, and SCM cannot reveal the same subject through one identity model, users will experience semantic drift even when the backend is technically correct.
4. Proof corpus publication is not optional milestone polish.
   - A milestone like M8 is materially stronger once the examples, usage path, and verification entrypoints are published together.

## Residual Risks

- Source editing still lacks a later-stage canonical apply path beyond preview-first evaluation.
- The graph mutation proofs remain intentionally narrow around `connect-ports` and governed cabinet placement.
- No automated desktop E2E coverage yet proves the complete product-facing mutation and reveal loop.
- Broader notation depth, domain authoring breadth, and richer workflow semantics remain post-M8 work.

## Recommended Follow-Through

1. Keep future milestone planning explicit about the difference between source preview evaluation and source apply or persist behavior.
2. Preserve the current runtime and `ide/lsp` authority boundaries when expanding graphical editing depth.
3. Add desktop-level automated proof coverage once the next mutation or workflow milestone needs stronger regression protection.

## Bottom Line

M8 is a valid completed milestone.

Athena now has a credible unified mutation architecture across source and graph, with review and reveal coherence preserved through canonical semantic identity and Athena-owned runtime semantics.
