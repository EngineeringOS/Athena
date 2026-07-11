---
baseline_commit: e5f5ef7fb0fbf10f583f0cf9acef52eb6a0e914d
---

# Story 2.4: Publish The M9 Proof Corpus And Verification Path

Status: done

## Story

As an architecture owner,
I want Athena to publish repeatable proof artifacts for the first engineering knowledge runtime,
so that M9 closes with runnable evidence that Athena understands engineering consequence instead of only storing engineering structure.

## FR Traceability

- FR-7: surface the first knowledge-runtime proof through existing semantic delivery surfaces
- FR-8: keep the knowledge-runtime proof independent from renderer and workbench depth
- NFR-3: derived context, capability facts, diagnostics, and impact consequences remain inspectable
- NFR-4: the first proof stays narrow enough to validate architecture honestly
- NFR-6: delivery stays on existing semantic product paths

## Acceptance Criteria

1. Given the first derived-context, capability-fact, rule-slice, diagnostic, and impact paths are implemented, when Athena publishes the M9 proof corpus, then the milestone includes examples, runnable verification, or equivalent proof artifacts that demonstrate deterministic engineering consequence through the fixed electrical knowledge pack, and the proof remains intentionally narrow instead of widening into standards-platform, vendor-catalog, or workbench-depth scope.
2. Given product and architecture owners review the M9 proof, when they inspect the corpus and documentation, then it is clear which engineering sufficiency family was proven, which outputs are typed and inspectable, and which broader knowledge-pack or AI concerns remain deferred.

## Tasks / Subtasks

- [x] Publish the narrow M9 proof fixtures under `examples/m9/`. (AC: 1, 2)
  - [x] Keep one smallest derived-context seed.
  - [x] Publish one governed before/after impact pair for the electrical knowledge proof.
- [x] Publish one operator-facing usage guide for M9. (AC: 1, 2)
  - [x] Document what M9 proves and what it explicitly does not prove.
  - [x] Record the exact Java 25 verification commands that exercised the published M9 surfaces.
- [x] Update example and module index docs so later developers can find the M9 proof corpus. (AC: 1, 2)
  - [x] Update `examples/README.md`.
  - [x] Cross-reference the new usage guide from `examples/m9/README.md`.

## Dev Notes

### Story Intent

- Story `2.4` is proof packaging, not new product behavior.
- The M9 corpus stays deliberately small so the milestone demonstrates kernel-owned engineering consequence instead of hiding uncertainty behind fixture sprawl.

### Completion Notes

- Added one `examples/m9/` proof README in English and Chinese.
- Published one before/after electrical impact pair plus the pre-existing derived-context seed.
- Added `docs/usages/m9-proof-usage.md` with companion-record links, verification commands, and explicit milestone boundaries.
- Updated `examples/README.md` so M9 follows the same milestone-index pattern as earlier proof corpora.

## Testing

- No new code path was added in Story 2.4; it packages the already-verified M9 proof fixtures and usage docs.
- Verified commands recorded in the usage guide:
  - `cmd /c "call java25 && .\gradlew.bat --no-daemon --console=plain :kernel:runtime:test --tests com.engineeringood.athena.runtime.AthenaSemanticReviewServiceTest"`
  - `cmd /c "call java25 && .\gradlew.bat --no-daemon --console=plain :ide:lsp:test --tests com.engineeringood.athena.ide.lsp.AthenaSemanticScmStateRequestTest --tests com.engineeringood.athena.ide.lsp.AthenaSourceMutationRequestTest"`
  - `cmd /c "call java25 && .\gradlew.bat --no-daemon --console=plain :kernel:engineering-model:test :kernel:semantic-scm:test"`

## File List

- `_bmad-output/implementation-artifacts/m9/2-4-publish-the-m9-proof-corpus-and-verification-path.md`
- `docs/usages/m9-proof-usage.md`
- `examples/README.md`
- `examples/m9/README.md`
- `examples/m9/README.zh-CN.md`
- `examples/m9/motor-derived-context.athena`
- `examples/m9/motor-impact-before.athena`
- `examples/m9/motor-impact-after.athena`
