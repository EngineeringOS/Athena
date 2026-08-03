---
story_key: 6-1-harden-package-local-geometry-references
epic: m37-e6
requirements: [FR-28, FR-29, FR-30, NFR-1, NFR-6, NFR-8]
baseline_commit: 1b2e8b7a6854bc186c82f2e796cf8adf9d5efc62
---

# Story 6.1: Harden Package-Local Geometry References

Status: review

## Story

As a representation package author,
I want native Symbols and complex SVG geometry admitted through one safe contract,
so that visual complexity does not create another engineering language or resource authority.

## Acceptance Criteria

1. Native Athena primitives and package-local SVG-backed geometry resolve through the same Athena-owned Anchor contract and preserve source/package trace.
2. SVG accepts only geometry-reference hints shaped as `data-athena-ref="anchor:<id>"`; the hint may point to geometry but cannot define Port, signal, direction, compatibility, label, intent, lifecycle, or binding facts.
3. Duplicate references, unsupported `data-athena-*` keys, malformed geometry refs, missing referenced geometry, path escape, absolute path, remote URI, external resources, scripts, event handlers, unsafe entities, unsupported namespaces, and raw-markup transport fail closed with typed diagnostics.
4. Package-local resource resolution remains the only active scheme. Remote resource URI, registry, Maven/npm-like resource lookup, and compatibility fallback are deferred and not implemented in M37.
5. One complex vendor-like SVG fixture and one simple native Symbol fixture both compile into Graphic Primitive IR with complete package/source trace and no XML/SVG metadata authority.
6. Focused SVG security, package-local resource, geometry-reference, native/SVG parity, LSP or compiler diagnostic, source-set hygiene, encoding, and `git diff --check` gates pass.

## Tasks / Subtasks

- [x] Add RED tests for strict geometry-reference schema (AC: 2, 3)
  - [x] Add compiler tests rejecting `data-athena-ref` values without `anchor:` prefix.
  - [x] Add compiler tests rejecting `data-athena-ref="port:*"`, `signal:*`, `direction:*`, raw names, blank values, and malformed anchor refs.
  - [x] Assert diagnostics use stable codes and source spans and do not lower a document on failure.
- [x] Harden SVG admission code (AC: 2, 3, 4)
  - [x] Validate `data-athena-ref` values in `AthenaSvgGraphicBodySupport` before geometry lowering.
  - [x] Keep the allowed SVG Athena key list minimal: only `data-athena-ref`.
  - [x] Keep all forbidden metadata keys blocked, including Port, signal, direction, role, label, binding, profile, lifecycle, intent, and compatibility concepts.
  - [x] Keep package-local path checks fail-closed for `..`, absolute paths, symlinks, missing files, remote URLs, `data:`, `file:`, and `url(...)`.
- [x] Prove Athena-owned Anchor contract for SVG and native symbols (AC: 1, 5)
  - [x] Extend or add tests showing an SVG-backed Symbol can expose an Athena-authored Anchor only when that Anchor references a valid `anchor:<id>` geometry hint.
  - [x] Extend or add tests showing missing SVG geometry referenced by an Athena Anchor fails before representation output.
  - [x] Keep native Symbol fixture compiling through the same anchor/export contract without SVG.
  - [x] Ensure package resource trace includes both `.athena` source and package-local SVG file where applicable.
- [x] Prove no new authority or compatibility path (AC: 3, 4, 6)
  - [x] Do not add remote resource runtime, XML/QET parser, registry lookup, renderer repair, or compatibility shim.
  - [x] Do not add `Proof`, `Demo`, `Sample`, milestone-named production classes, or `V0`/`V1` names under `src/main`.
  - [x] Remove any stale code encountered in this path that violates M37 authority directly rather than wrapping it.
- [x] Run verification and update record (AC: 6)
  - [x] Run focused SVG/resource compiler tests.
  - [x] Run any touched LSP or parser tests if diagnostics or syntax surfaces change.
  - [x] Run `powershell -ExecutionPolicy Bypass -File .\tools\source-set-hygiene-audit.ps1`.
  - [x] Run `powershell -ExecutionPolicy Bypass -File .\tools\encoding-audit.ps1`.
  - [x] Run `git diff --check`.
  - [x] Update File List, Debug Log, Completion Notes, Change Log, story status, and sprint status only after evidence exists.

## Dev Notes

### Authority Boundary

- Athena source is the only engineering metadata authority. SVG may point to geometry; Athena defines meaning.
- Allowed SVG hint:

```xml
<path id="terminal-l1" data-athena-ref="anchor:drive.power.l1" />
```

- Disallowed SVG metadata examples:

```xml
data-athena-port="L1"
data-athena-signal="electrical.ac"
data-athena-direction="input"
data-athena-role="terminal"
data-athena-binding="..."
```

- `data-athena-ref` is a geometry reference only. It is not a Port, not a signal, not a direction, not intent, not compatibility, and not a binding rule.

### Current Code Intelligence

- SVG body admission lives in `kernel/compiler/src/main/kotlin/com/engineeringood/athena/compiler/AthenaSvgGraphicBodyCompiler.kt`.
- SVG security/schema support lives in `kernel/compiler/src/main/kotlin/com/engineeringood/athena/compiler/AthenaSvgGraphicBodySupport.kt`.
- SVG-backed Symbol/Element lowering flows through `AthenaSymbolSourceLowerer` and `AthenaElementSourceLowerer`.
- Existing focused tests live in `kernel/compiler/src/test/kotlin/com/engineeringood/athena/compiler/AthenaReferencedSvgGraphicCompilerTest.kt`.
- Current code already blocks scripts, `foreignObject`, event handlers, unsafe URLs, unsupported namespaces, duplicate ids, duplicate refs, path escape, absolute paths, missing files, symlinks, and unsafe XML. The likely M37 gap is strict validation of `data-athena-ref` value shape and proof that package-local SVG remains geometry-only.

### Previous Story Intelligence

- Story 5.4 proved stale frontend bundles can lie; rebuild affected surfaces before E2E stories, but Story 6.1 should stay focused on compiler/resource gates unless UI diagnostics change.
- Story 5.4 final smoke evidence showed package resource trace in occurrence payloads. Do not create another proof schema for SVG; use existing compiled facts and diagnostics.
- M36 cleanup lesson applies: no milestone-named production classes, no proof/demo/sample code in `src/main`, no compatibility wrappers.

### Implementation Guidance

- Prefer extending existing SVG compiler support, not creating a second SVG validator.
- Diagnostic codes should be stable and specific. Suggested code for bad ref value: `svg.geometry-ref.invalid`.
- Keep schema small. Do not introduce `data-athena-schema`, version negotiation, or remote resource declaration in M37.
- If an SVG element has no `data-athena-ref`, it can still render as geometry. Only Athena-authored Anchors may bind engineering meaning to a geometry reference.
- If an Athena-authored Anchor references an absent SVG geometry reference, compile must fail before `RepresentationDefinition` is accepted.

### Required Commands

- `.\gradlew.bat --no-daemon --console=plain :kernel:compiler:test --tests com.engineeringood.athena.compiler.AthenaReferencedSvgGraphicCompilerTest`
- `powershell -ExecutionPolicy Bypass -File .\tools\source-set-hygiene-audit.ps1`
- `powershell -ExecutionPolicy Bypass -File .\tools\encoding-audit.ps1`
- `git diff --check`

### References

- [Source: `_bmad-output/implementation-artifacts/m37/epics.md` - Story 6.1]
- [Source: `_bmad-output/planning-artifacts/prds/prd-Athena-2026-07-30-m37/prd.md` - FR-28, FR-29, FR-30, NFR-1, NFR-6, NFR-8]
- [Source: `_bmad-output/planning-artifacts/prds/prd-Athena-2026-07-30-m37/addendum.md` - SVG Bridge Rule]
- [Source: `_bmad-output/implementation-artifacts/m37/5-4-prove-m37-end-to-end-with-screenshots.md` - E2E and package trace lessons]
- [Source: `kernel/compiler/src/main/kotlin/com/engineeringood/athena/compiler/AthenaSvgGraphicBodyCompiler.kt` - active SVG lowering]
- [Source: `kernel/compiler/src/main/kotlin/com/engineeringood/athena/compiler/AthenaSvgGraphicBodySupport.kt` - active SVG validation]
- [Source: `kernel/compiler/src/test/kotlin/com/engineeringood/athena/compiler/AthenaReferencedSvgGraphicCompilerTest.kt` - focused SVG tests]

## Dev Agent Record

### Agent Model Used

Codex GPT-5

### Debug Log References

- 2026-07-31: RED `.\gradlew.bat --no-daemon --console=plain :kernel:compiler:test --tests com.engineeringood.athena.compiler.AthenaReferencedSvgGraphicCompilerTest` failed on missing `svg.geometry-ref.invalid` validation for malformed `data-athena-ref`.
- 2026-07-31: GREEN focused compiler test passed after adding strict `anchor:<id>` geometry-reference validation.
- 2026-07-31: Focused compiler test passed again after adding SVG provenance trace assertion.
- 2026-07-31: `powershell -ExecutionPolicy Bypass -File .\tools\source-set-hygiene-audit.ps1` passed.
- 2026-07-31: `powershell -ExecutionPolicy Bypass -File .\tools\encoding-audit.ps1` passed.
- 2026-07-31: `git diff --check` passed.

### Completion Notes List

- Ultimate context engine analysis completed - comprehensive developer guide created.
- Hardened SVG `data-athena-ref` value validation to accept only `anchor:<id>` geometry hints.
- Added negative tests for blank refs, raw refs, `port:*`, `signal:*`, `direction:*`, empty anchors, whitespace, path-like, and traversal-like values.
- Kept SVG authority narrow: no Port, signal, direction, role, label, binding, profile, lifecycle, intent, compatibility, remote resource, XML/QET runtime, renderer repair, or compatibility path was added.
- Strengthened SVG-backed Symbol trace proof so accepted SVG geometry carries both `.athena` source and package-local SVG provenance.

### File List

- _bmad-output/implementation-artifacts/m37/6-1-harden-package-local-geometry-references.md
- _bmad-output/implementation-artifacts/m37/sprint-status.yaml
- kernel/compiler/src/main/kotlin/com/engineeringood/athena/compiler/AthenaSvgGraphicBodySupport.kt
- kernel/compiler/src/test/kotlin/com/engineeringood/athena/compiler/AthenaReferencedSvgGraphicCompilerTest.kt

## Change Log

- 2026-07-31: Created implementation-ready Story 6.1 from finalized M37 PRD, addendum SVG Bridge Rule, epics, current SVG compiler/test intelligence, and Story 5.4 lessons.
- 2026-07-31: Implemented strict package-local SVG geometry-reference validation and moved story to review after focused compiler, hygiene, encoding, and diff gates passed.
