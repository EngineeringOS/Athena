---
status: review
baseline_commit: 8d12ba03c05b373b98fbc513e6c7f3f6fce962d6
story_id: 4.2
epic: 4
title: Render Graphic Resources As Descriptor-Backed Resources
---

# Story 4.2: Render Graphic Resources As Descriptor-Backed Resources

## Status

Review

## Story

As an Athena frontend adapter,
I want Graphic Resources rendered through descriptor handles,
so that the renderer draws professional resources without owning semantic meaning.

## Required Context

- PRD: `_bmad-output/planning-artifacts/prds/prd-Athena-2026-07-22-m32/prd.md`
- Architecture: `_bmad-output/planning-artifacts/architecture/architecture-Athena-2026-07-22-m32/ARCHITECTURE-SPINE.md`
- Epics: `_bmad-output/implementation-artifacts/m32/epics.md`
- Previous story: `_bmad-output/implementation-artifacts/m32/4-1-feed-descriptors-into-representation-occurrences.md`
- Epic 3 retro: `_bmad-output/implementation-artifacts/m32/epic-3-retro-2026-07-22.md`

## Acceptance Criteria

1. Given a descriptor-backed Graphic Resource, when presentation/render payloads are produced, then
   they use resolved resource handles, descriptor bounds, anchors, labels, and transient
   interaction state only.
2. Given normal component rendering, when structured DOM/render proof is inspected, then hitboxes
   and backgrounds are transparent, interaction chrome is transient, labels are not duplicated, and
   viewBox derives from presentation bounds and governed margins.
3. Given renderer or adapter code is reviewed, when package-backed resources are active, then no
   semantic meaning is inferred from Graphic Resource internals, CSS classes, labels, or file names.
4. Given the story implementation is complete, when renderer code, CSS, DOM tests, screenshots,
   and docs are reviewed, then stale wrappers, hard-coded canvases, and resource-semantic
   assumptions are removed or ledgered.
5. Mandatory Polish/Purge Gate complete with AC evidence.

## Tasks/Subtasks

- [x] Add RED tests for descriptor-backed render payloads carrying resource handle, descriptor
  bounds, anchors, labels, and transient interaction state. (AC: 1,3)
- [x] Add RED tests or structured proof for transparent normal background/hitbox chrome, no
  duplicate labels, and derived viewBox bounds. (AC: 2)
- [x] Implement descriptor-backed render payload/mapper or adapter contract at the current
  presentation boundary. (AC: 1..3)
- [x] Document renderer adapter authority: paint-only, no package resolution or semantic inference.
  (AC: 3,4)
- [x] Run focused tests sequentially, then full regression sequentially; do not run Gradle
  concurrently on Windows. (AC: 1..4)
- [x] Complete mandatory polish/purge review and record final evidence. (AC: 4,5)

## Dev Notes

- Start with CodeGraph review of existing SVG renderer, presentation payloads, and Theia frontend
  resource/chrome code before editing.
- Prefer structured proof/payload tests first. Screenshot proof remains secondary in M32.
- Do not make Graphic Resource ids, SVG paths, CSS classes, DOM nodes, or labels semantic
  authority.
- If full Theia rendering is too downstream for this story slice, implement the platform payload
  that Theia consumes and document frontend adapter follow-through explicitly.
- Preserve the M27/M30 viewBox rule: derive canvas framing from presentation bounds plus governed
  margins, not hard-coded sample constants.
- Run `powershell -ExecutionPolicy Bypass -File .\tools\encoding-audit.ps1` after text edits.

## Testing Requirements

- Follow TDD: write failing render payload/proof tests before production code.
- Focused command should target the module touched by implementation.
- Full regression command after story completion:
  `.\gradlew.bat --no-daemon --console=plain check`.
- Encoding audit command after docs/text edits:
  `powershell -ExecutionPolicy Bypass -File .\tools\encoding-audit.ps1`.

## Dev Agent Record

### Agent Model Used

Codex GPT-5

### Debug Log References

- RED: `.\gradlew.bat --no-daemon --console=plain :kernel:package-runtime:test`
  failed in `:kernel:package-runtime:compileTestKotlin` with unresolved
  `DescriptorBackedGraphicResourceRenderPayloadMapper` and viewBox payload types.
- GREEN: focused `:kernel:package-runtime:test` passed after adding descriptor-backed Graphic
  Resource render payload DTOs and mapper.
- REFACTOR VERIFY: focused `:kernel:package-runtime:test` passed after docs and CodeGraph review.
- REGRESSION: `.\gradlew.bat --no-daemon --console=plain check` passed sequentially.
- ENCODING: `powershell -ExecutionPolicy Bypass -File .\tools\encoding-audit.ps1`
  passed after text edits.
- PURGE: `git status --short` showed M32 artifacts and no staged/tracked `.tools` addition.

### Completion Notes List

- Added descriptor-backed Graphic Resource render payloads carrying semantic subject correlation,
  resource handle/kind, descriptor bounds, anchors, deduplicated labels, transient interaction
  state, invisible normal chrome flags, derived viewBox, and semantic-inference guard.
- Added mapper from Binding Evidence plus Representation Descriptor into paint-only payload facts.
- Documented renderer adapter authority: consume returned paint facts only, no package resolution
  or semantic inference from Graphic Resource internals, DOM, CSS, labels, or file names.
- AC evidence:
  - AC1: `DescriptorBackedGraphicResourceRenderPayloadTest.render payload carries descriptor
    resource bounds anchors labels and transient chrome`.
  - AC2: same test verifies invisible normal background/hitbox flags and derived viewBox;
    `render payload proof prevents duplicate labels and resource semantic inference` verifies
    deduplicated labels.
  - AC3: payload includes `resourceSemanticInferenceForbidden` and docs define paint-only adapter
    behavior.
  - AC4: docs and touched payload files reviewed; full Theia DOM integration remains downstream
    and no cleanup-ledger entry was required for this slice.
  - AC5: focused runtime test, full `check`, encoding audit, and workspace purge review recorded.

### File List

- `_bmad-output/implementation-artifacts/m32/4-2-render-graphic-resources-as-descriptor-backed-resources.md`
- `_bmad-output/implementation-artifacts/m32/sprint-status.yaml`
- `docs/usages/engineering-package-platform.md`
- `kernel/package-runtime/src/main/kotlin/com/engineeringood/athena/packageruntime/DescriptorBackedGraphicResourceRenderPayloadMapper.kt`
- `kernel/package-runtime/src/main/kotlin/com/engineeringood/athena/packageruntime/DescriptorBackedGraphicResourceRenderPayloads.kt`
- `kernel/package-runtime/src/test/kotlin/com/engineeringood/athena/packageruntime/DescriptorBackedGraphicResourceRenderPayloadTest.kt`

## Change Log

- 2026-07-22: Story created for M32 Epic 4 after descriptor-backed occurrence bridge.
- 2026-07-22: Implemented descriptor-backed Graphic Resource render payload contract and proof.

## Mandatory Final Polish/Purge Gate

- Review touched and adjacent renderer, package runtime, presentation payload, CSS, tests, docs,
  fixtures, and sprint artifacts.
- Remove stale wrappers, hard-coded canvases, duplicate-label paths, or resource-semantic claims.
- Ledger any retained stale item in `_bmad-output/implementation-artifacts/m32/cleanup-ledger.md`
  with owner, reason, target milestone, and verification.
- Record AC-to-evidence mapping before moving the story beyond `review`.
