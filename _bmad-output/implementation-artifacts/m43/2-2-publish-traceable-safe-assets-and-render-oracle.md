---
baseline_commit: 9681345816d2659abf7a70b68151eacfef0944c2
---

# Story 2.2: Publish Traceable Safe Assets And Render Oracle

Status: done

## Story

As a renderer and reviewer,
I want a revision-scoped asset bundle and deterministic SVG oracle,
so that live paint, SVG, and proof consume the same admitted bytes and metrics.

## Acceptance Criteria

1. `svg-safe-1` admits only its committed allow-list, canonicalizes admitted SVG bytes, and rejects DTDs, entities, scripts, event attributes, CSS, animation, filters, `foreignObject`, `text`, `use`, `image`, external/non-local URLs, malformed input, and profile byte/decoded limits with human-first diagnostics.
2. SVG, PNG, and WOFF2 asset bytes are local, digest-verified, revision-scoped, deduplicated, and limited by the committed profile; adapters receive publication bytes only and never reopen paths or fetch network resources.
3. `AthenaScenePublication` validates the complete relationship between scene assets and its `AssetBundle`: one accepted revision, unique bundle entries, matching IDs/digests/bytes, no missing or surplus admitted entry, and no partial `READY` publication on asset failure.
4. Scene asset trace origins use `ASSET_DEFINITION`, preserve exactly one primary origin per trace, and retain the established primary navigation roles for occurrences, Ports, routes, labels, frame, and coordinates.
5. One pure renderer-neutral SVG adapter consumes only `AthenaDiagramScene` plus its admitted `AssetBundle`, serializes canonical SVG in scene order with fixed namespace/attribute order, integer formatting, escaping, and embedded admitted bytes, and matches the committed rolling-shutter golden bytes exactly across repeated and reordered compilation.
6. PNG remains an Electron/Chromium proof harness only. No product export, PDF surface, Konva implementation, frontend sanitizer, raw Projection/Spatial transport, renderer fallback, or new Athena source syntax is introduced.
7. Affected Kotlin/LSP/frontend conformance tests, full Gradle test, frontend test/build, encoding audit, and source-set hygiene audit pass sequentially.

## Tasks / Subtasks

- [x] Establish secure asset admission and contract completion (AC: 1, 2, 3, 4)
  - [x] Read the committed `svg-safe-1` profile and shared asset corpus; implement a structured XML parser with DTD/external-entity processing disabled. Do not use a regex sanitizer.
  - [x] Canonicalize admitted SVG bytes before SHA-256 digest and `AssetId` construction. Enforce SVG source, PNG dimensions/decoded bytes, WOFF2 bytes, and aggregate bundle limits exactly from the profile.
  - [x] Extend renderer-neutral presentation contracts only where committed schema requires it. SVG/PNG carry intrinsic visual bounds; WOFF2 is a font asset with no fabricated visual bounds, so the field is omitted rather than `null`. Regenerate frontend contracts and add one schema-aligned LSP payload mapper; it must not become an independent scene model or reconstruct geometry.
  - [x] Validate asset bundle revision, entry uniqueness, scene coverage, digest-to-byte equality, deduplication, and trace admission before `READY` publication.
  - [x] Keep asset definition provenance portable and preserve one-primary-origin rules without changing existing occurrence, Port, route, label, or sheet navigation precedence.
- [x] Publish admitted bundles through the runtime and LSP boundary (AC: 2, 3, 4)
  - [x] Replace the current empty-only bundle publication with compiler-owned admitted bundle construction from declared local package inputs; invalid, missing, tampered, or over-limit assets return `UNAVAILABLE` and publish no partial scene or bundle.
  - [x] Extend input-revision construction with admitted asset/font digests plus profile/schema/compiler records; formatting-only source edits still change revision.
  - [x] Keep `AthenaLanguageServer` as transport only. Its one schema-aligned mapper base64-encodes verified bundle bytes as `bytesBase64`; it must not reopen assets, resolve paths for adapters, or introduce raw Projection/Spatial payloads.
  - [x] Reuse committed schemas and generated frontend validators for nonempty bundle transport; no network request, filesystem reference, fallback asset, or compatibility reader may cross into the adapter contract.
- [x] Build the deterministic SVG oracle (AC: 5, 6)
  - [x] Create or replace only the M43-aligned `kernel:svg-renderer` source with a pure scene-plus-bundle serializer. Do not revive deleted legacy renderer source or any old presentation model.
  - [x] Serialize page, clean frame, coordinates, routes, occurrences, Ports, labels, styles, and admitted assets from canonical scene order. Never read source, Projection, Spatial, frontend, path, clock, viewport, or process state.
  - [x] Use fixed namespace and attribute ordering, UTF-8 output, exact integer formatting, XML escaping, and canonical embedded asset representation. No timestamps, runtime metadata, title table, interior grid, or instructional text.
  - [x] Replace the shared render golden only with output generated from the active M43 rolling-shutter scene; its bytes become the cross-language oracle.
- [x] Add shared conformance and regression proof (AC: 1, 2, 3, 4, 5, 7)
  - [x] Complete the shared asset corpus with expected digest/identity/trace data and deterministic rejection codes, then cover admitted SVG canonicalization and every committed rejected vector, plus DTD/entity, unknown element/attribute, external URL, malformed, and oversize refusal.
  - [x] Cover PNG/WOFF2 limits, bundle aggregate limits, duplicate admitted bytes, wrong/missing/duplicate bundle entries, tampered bytes/digests, asset revision replacement, and no partial publication.
  - [x] Assert `ASSET_DEFINITION` trace behavior and existing primary trace-role vectors; verify no renderer/path/network authority leaks through Kotlin or TypeScript.
  - [x] Assert exact shared SVG golden bytes, scene ordering, source reorder determinism, repeated process determinism, and the clean-page constraint.
  - [x] Run focused presentation/compiler/svg-renderer/LSP tests, generated frontend contract tests, full Gradle tests, full IDE build, encoding audit, and source-set hygiene audit sequentially.
- [x] Verify and record (AC: 7)
  - [x] Complete all story records, exact file list, change log, and sprint state only after every acceptance check has passing evidence.

## Dev Notes

### Authority And Scope

- `SpatialDocument` remains the sole geometry authority. This story may admit assets, resolve paint, copy/validate canonical scene facts, and serialize them. It must never move, reroute, relabel, or infer engineering meaning.
- `AthenaDiagramScene` and a same-revision `AssetBundle` are one immutable Presentation publication. A failed asset admission has no new `READY` state and must not mix old scene facts with new asset bytes.
- Assets are compiler-owned local governed inputs. Adapters receive digest-verified bytes from the bundle only. They never reopen package paths, inspect project source, or fetch a network resource.
- SVG is a deterministic renderer-neutral oracle. PNG stays Electron/Chromium proof only. Neither becomes user-facing export, PDF, layout authority, or a second live interaction surface.
- Do not add a dependency or reuse source from `reference/`. The structured XML parser must use existing JDK facilities with secure configuration, unless a new dependency is explicitly approved.

### Contract Requirements

- Use `kernel/presentation-model/src/main/resources/profile/svg-safe-1.json` as the only admission authority. The limits are SVG `2 MiB`, PNG `4096 x 4096` and `64 MiB` decoded, WOFF2 `4 MiB`, and `32 MiB` encoded total bundle.
- SVG v1 allow-list is profile-owned. Reject scripts, handlers, `style`, animation, filters, `foreignObject`, `text`, `use`, `image`, DTD/entities, external references, and forbidden URL schemes. Canonical admitted bytes, not raw path bytes, determine the digest and identity.
- `SceneAsset` is content-addressed by media kind, profile ID, and canonical admitted bytes. Bundle entries are content-addressed, deduplicated, declared, revision-scoped, and referenced by `bundleEntryId`.
- `AssetBundleEntry` retains verified canonical bytes in the kernel. One explicit schema-aligned LSP mapper encodes them as the contract's `bytesBase64` field; the mapper has no independent scene semantics, no geometry derivation, and no raw `ByteArray` transport.
- Asset/font digests, selected profile, schema/compiler records, project source, and Sheet Companion belong in `inputRevision`. Do not keep the current source-plus-companion-only revision calculation.
- `ResolvedStyle` and the committed schemas must carry all paint/text facts required by the contract pack before any renderer relies on a system default. Do not invent font geometry or silently substitute fonts.
- Asset traces use `ASSET_DEFINITION`; all traces retain exactly one primary origin. Existing navigation roles remain: occurrence/label semantic declaration, Port declaration, route relationship declaration, frame/coordinate Sheet declaration.
- SVG consumes scene and bundle only. Canonical serialization uses fixed namespace/attribute order, integer formatting, escaping, embedded admitted bytes, and no time/process/device metadata. The active M43 scene produces `contracts/presentation/v1/render/rolling-shutter.svg` exactly.

### Existing Implementation To Reuse Or Repair

- Reuse `PresentationContracts.kt`, `AthenaDiagramSceneContract.kt`, `AthenaDiagramSceneCompiler`, the `athena/diagramScene` LSP request, generated TypeScript contracts, and `contracts/presentation/v1` as the only authority.
- `AssetBundle` and `SceneAsset` types already exist, but no sanitizer/admission pipeline or SVG oracle exists; `diagramScenePublication()` currently supplies an empty bundle. Repair this path without raw Projection/Spatial transport.
- Existing `kernel/svg-renderer` source was removed under pre-1.0 cleanup; stale build output is not reusable implementation. New source must be M43 contract-aligned and have no milestone/demo/proof class names.
- Do not reopen 0-1 through 2-1. Adopt their valid scene/LSP work; make only the asset and SVG contract delta required here.

### Testing Requirements

- Tests belong in affected `src/test` source sets and consume the committed vectors under `contracts/presentation/v1`. No private equivalent fixtures or `Proof`/`Demo`/milestone production helpers.
- Test XML hardening before happy path. Assert refusal has a plain subject, problem, correction, and stable secondary code.
- Verify `READY` only with matching scene/bundle revision; invalid asset leaves no partial new publication. Test repeated/reordered output gives byte-identical SVG and stable identity/digest.
- Test generated TypeScript validators against nonempty bundle fixtures. The frontend is a validator/consumer, never an asset parser, geometry source, or asset-path resolver.
- Gradle commands are strictly sequential on Windows. Final gates include affected module tests, `:kernel:compiler:test`, `:ide:lsp:test`, root `test`, frontend tests/build, encoding audit, and source-set hygiene audit.

### Project Structure Notes

- Keep renderer-neutral asset/publication models in `kernel/presentation-model`; place secure admission and presentation orchestration in cohesive compiler/runtime support types; place pure SVG serialization in `kernel/svg-renderer`.
- Keep Kotlin files grouped by role. Do not revive a large legacy renderer dump file or split tiny model values into one file each.
- No Konva import, Theia paint behavior, PDF/export command, or direct `reference/` import belongs in this story.

### References

- [Source: `_bmad-output/planning-artifacts/m43/epics.md`#Story-2-2---Publish-Traceable-Safe-Assets-And-Render-Oracle]
- [Source: `_bmad-output/planning-artifacts/prds/prd-Athena-2026-08-05-m43/prd.md`#FR-7-Render-One-Presentation-Reality]
- [Source: `_bmad-output/planning-artifacts/prds/prd-Athena-2026-08-05-m43/prd.md`#NFR-1-Determinism]
- [Source: `_bmad-output/planning-artifacts/architecture/architecture-Athena-2026-08-05-m43/ARCHITECTURE-SPINE.md`#AD-12---Assets-And-Fonts-Are-Governed-Inputs]
- [Source: `_bmad-output/planning-artifacts/architecture/architecture-Athena-2026-08-05-m43/ARCHITECTURE-SPINE.md`#AD-13---SVG-Is-Deterministic-Oracle-PNG-Is-Environment-Pinned-Proof]
- [Source: `_bmad-output/planning-artifacts/architecture/architecture-Athena-2026-08-05-m43/M43-CONTRACT-PACK.md`#8-Paint-Assets-And-Fonts]
- [Source: `_bmad-output/planning-artifacts/architecture/architecture-Athena-2026-08-05-m43/M43-CONTRACT-PACK.md`#9-Rendering-And-Proof]
- [Source: `_bmad-output/planning-artifacts/architecture/architecture-Athena-2026-08-05-m43/LEGACY-REPLACEMENT.md`]
- [Source: `kernel/presentation-model/src/main/kotlin/com/engineeringood/athena/presentation/PresentationContracts.kt`]
- [Source: `kernel/presentation-model/src/main/kotlin/com/engineeringood/athena/presentation/AthenaDiagramSceneContract.kt`]
- [Source: `ide/lsp/src/main/kotlin/com/engineeringood/athena/ide/lsp/AthenaDiagramProtocol.kt`]
- [Source: `contracts/presentation/v1/assets/`]
- [Source: `contracts/presentation/v1/trace/expected-origins.json`]
- [Source: `contracts/presentation/v1/render/rolling-shutter.svg`]
- [Previous story: `_bmad-output/implementation-artifacts/m43/2-1-compile-canonical-athena-diagram-scene.md`]

## Dev Agent Record

### Agent Model Used

Codex GPT-5

### Debug Log References

- Initial red phase: `PresentationAssetPackageCompilerTest` failed to compile because package resource admission boundary was missing.
- Fixed lock-backed resource path traversal, no-follow checks, digest verification, and compiler/LSP publication wiring. Focused tests passed after each correction.
- Final evidence: root `test` passed; frontend `yarn test` passed (27/27); encoding and source-set
  hygiene audits passed. The active rolling-shutter SVG golden remains the only renderer-neutral
  oracle; no stale pre-M43 golden or compatibility renderer was reopened.

### Completion Notes List

- `PresentationAssetCompiler` remains sole admission authority; semantic source does not define SVG shape, center, or visual anchors.
- `PresentationAssetPackageCompiler` consumes only lock-admitted local `resources/` bytes, verifies SHA-256, and emits `ASSET_DEFINITION` traces.
- `AthenaDiagramProtocol` publishes one revision-scoped scene/bundle and fails closed on lock/resource errors; LSP mapper emits verified `bytesBase64` only.
- `AthenaSvgRenderer` and committed rolling-shutter golden remain deterministic and renderer-neutral.

### File List

- `kernel/compiler/src/main/kotlin/com/engineeringood/athena/compiler/PresentationAssetPackageCompiler.kt`
- `kernel/compiler/src/main/kotlin/com/engineeringood/athena/compiler/AthenaCompiler.kt`
- `kernel/compiler/src/test/kotlin/com/engineeringood/athena/compiler/PresentationAssetPackageCompilerTest.kt`
- `ide/lsp/src/main/kotlin/com/engineeringood/athena/ide/lsp/AthenaDiagramProtocol.kt`
- `kernel/presentation-model/src/main/kotlin/com/engineeringood/athena/presentation/PresentationAssetCompiler.kt`
- `kernel/presentation-model/src/main/kotlin/com/engineeringood/athena/presentation/AthenaDiagramSceneContract.kt`
- `kernel/presentation-model/src/main/kotlin/com/engineeringood/athena/presentation/PresentationContracts.kt`
- `kernel/presentation-model/src/test/kotlin/com/engineeringood/athena/presentation/PresentationAssetCompilerTest.kt`
- `kernel/svg-renderer/src/main/kotlin/com/engineeringood/athena/svg/AthenaSvgRenderer.kt`
- `kernel/svg-renderer/src/test/kotlin/com/engineeringood/athena/svg/AthenaSvgRendererTest.kt`
- `contracts/presentation/v1/render/rolling-shutter.svg`
- `ide/theia-frontend/scripts/generate-diagram-contracts.mjs`
- `ide/theia-frontend/scripts/athena-diagram-contracts.test.mjs`

### Change Log

- 2026-08-06: Created through the M43 BMad story flow from the active Epic 2 asset and SVG contract.
- 2026-08-06: Completed secure lock-backed asset publication, deterministic SVG oracle, conformance
  coverage, and verification gates; status moved to `done`.
