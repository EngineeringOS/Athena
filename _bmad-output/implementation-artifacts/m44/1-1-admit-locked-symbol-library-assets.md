---
baseline_commit: 9ca7c80239d38d9e2a4880886e71e27e2923c41b
---

# Story 1.1: Admit Locked Symbol Library Assets

Status: done

## Story

As a library maintainer,
I want Athena to admit `symbol.yaml` plus SVG library items through the package lock,
so that projects consume governed IEC/vendor symbols instead of copied reference files.

## Acceptance Criteria

1. Given a governed M44 example depends on a Representation Package with one SVG and one `symbol.yaml`
   using `athena-symbol-v1`, when package resolution runs, then the Symbol item is admitted with package
   identity, item identity, version, digest, provenance, and license trace.
2. Given the admitted Symbol item, when its descriptor digest is computed, then YAML is parsed into
   canonical JSON with sorted object keys, explicit units, normalized numbers, and stable SHA-256 digest.
3. Given a descriptor claims port direction/domain/flow, when admission validates it, then those fields
   are treated as compatibility envelope only and do not mutate Engineering Port authority.
4. Given missing SVG, missing descriptor, unknown unnamespaced field, digest mismatch, or runtime
   reference-directory dependency, when package admission runs, then publication is rejected with a
   subject/problem/correction diagnostic and no scene asset is published.

## Tasks / Subtasks

- [x] Add failing package-model/runtime tests for `symbol.yaml` admission (AC: 1, 2, 4)
  - [x] valid `athena-symbol-v1` descriptor plus SVG admits with package/item/version/digest/provenance/license
  - [x] missing SVG, missing descriptor, digest mismatch, and unknown unnamespaced field reject
  - [x] namespaced vendor extension is preserved
- [x] Implement `symbol.yaml` descriptor model and canonical digest path (AC: 1, 2)
  - [x] prefer extending `kernel/package-model` representation descriptor contracts over adding parallel models
  - [x] canonicalize through deterministic JSON for digesting
- [x] Add runtime admission and diagnostics (AC: 1, 3, 4)
  - [x] integrate with existing package resolver/snapshot path
  - [x] enforce no `reference/elements` or `reference/elements_contrib` runtime dependency
  - [x] preserve Engineering Port authority; descriptor compatibility cannot rewrite Port semantics
- [x] Add M44 package fixture under example/test fixture ownership, not production demo code (AC: 1, 4)
  - [x] small copied/normalized SVG allowed only inside fixture/example package with provenance/license
  - [x] do not parse QElectroTech `.html` at runtime
- [x] Run sequential validation (AC: all)
  - [x] targeted Gradle tests for package model/runtime
  - [x] `powershell -ExecutionPolicy Bypass -File .\tools\encoding-audit.ps1`
  - [x] `powershell -ExecutionPolicy Bypass -File .\tools\source-set-hygiene-audit.ps1`

## Dev Notes

### Architecture Guardrails

- M44 paradigm: governed projection pipeline. Package-admitted library facts plus engineering source
  compile into one canonical scene; frontend is not authority.
- `AD-2`: `SymbolDefinition` is exactly one canonical SVG resource plus one UTF-8 `symbol.yaml`
  descriptor, schema `athena-symbol-v1`; loader parses YAML to canonical JSON for digesting. No
  alternate syntax, fallback parser, or `reference/` runtime dependency.
- `AD-3`: Engineering source owns Port identity, direction, domain, flow kind, semantic properties.
  Library metadata owns keyed geometry, orientation, label/hit zones, compatibility envelope only.
- `AD-12`: This is foundational golden-loop work. Do not start broader edit/style/export scope here.

### Current Code Reality

- Existing descriptor model: `kernel/package-model/src/main/kotlin/com/engineeringood/athena/packageplatform/RepresentationDescriptorModels.kt`
  has `RepresentationDescriptor` with descriptor id, resource binding, bounds, anchors, label slots,
  hotspots, transforms, variants, style refs, validation refs, forbidden semantic authority claims, and
  optional representation package id.
- Existing runtime selection: `kernel/package-runtime/src/main/kotlin/com/engineeringood/athena/packageruntime/BindingResolver.kt`.
  Reuse/extend this area for admitted descriptor binding; do not invent a second package resolver.
- Existing tests around descriptor binding, anchor route evidence, and render payload live under
  `kernel/package-runtime/src/test/kotlin/com/engineeringood/athena/packageruntime/`.

### File Structure Requirements

- Likely touched:
  - `kernel/package-model/src/main/kotlin/com/engineeringood/athena/packageplatform/RepresentationDescriptorModels.kt`
  - `kernel/package-runtime/src/main/kotlin/com/engineeringood/athena/packageruntime/`
  - `kernel/package-runtime/src/test/kotlin/com/engineeringood/athena/packageruntime/`
  - M44 fixture/example files under `examples/m44/` or package-runtime test fixtures
- Do not put proof/demo helper classes in production `src/main`.
- Do not use milestone names in production class names.

### Testing Requirements

- Start with failing tests before implementation.
- Gradle commands must run sequentially; never parallel.
- Minimum expected validation:
  - `.\gradlew.bat --no-daemon --console=plain :kernel:package-model:test`
  - `.\gradlew.bat --no-daemon --console=plain :kernel:package-runtime:test`
  - `powershell -ExecutionPolicy Bypass -File .\tools\encoding-audit.ps1`
  - `powershell -ExecutionPolicy Bypass -File .\tools\source-set-hygiene-audit.ps1`

### References

- PRD: `_bmad-output/planning-artifacts/prds/prd-Athena-2026-08-07-m44/prd.md`
- Architecture: `_bmad-output/planning-artifacts/architecture/architecture-Athena-2026-08-07-m44/ARCHITECTURE-SPINE.md`
- Epics: `_bmad-output/planning-artifacts/m44/epics.md`
- Sprint status: `_bmad-output/implementation-artifacts/m44/sprint-status.yaml`

## Dev Agent Record

### Agent Model Used

GPT-5 Codex.

### Debug Log References

- Red test: `.\gradlew.bat --no-daemon --console=plain :kernel:package-model:test --tests com.engineeringood.athena.packageplatform.SymbolYamlDescriptorAdmissionTest` failed before implementation because admission types were missing.
- Red test: `.\gradlew.bat --no-daemon --console=plain :kernel:package-runtime:test --tests com.engineeringood.athena.packageruntime.SymbolLibraryAssetAdmissionTest` failed before implementation because runtime admission wrapper was missing.
- Green targeted tests:
  - `.\gradlew.bat --no-daemon --console=plain :kernel:package-model:test --tests com.engineeringood.athena.packageplatform.SymbolYamlDescriptorAdmissionTest`
  - `.\gradlew.bat --no-daemon --console=plain :kernel:package-runtime:test --tests com.engineeringood.athena.packageruntime.SymbolLibraryAssetAdmissionTest`
- Module tests:
  - `.\gradlew.bat --no-daemon --console=plain :kernel:package-model:test`
  - `.\gradlew.bat --no-daemon --console=plain :kernel:package-runtime:test`
- Regression: `.\gradlew.bat --no-daemon --console=plain test`
- Audits:
  - `powershell -ExecutionPolicy Bypass -File .\tools\encoding-audit.ps1`
  - `powershell -ExecutionPolicy Bypass -File .\tools\source-set-hygiene-audit.ps1`

### Completion Notes List

- Added strict `symbol.yaml` / `athena-symbol-v1` admission in package model without adding a YAML dependency.
- Added canonical JSON digesting with sorted keys and normalized numeric scalars.
- Added port compatibility envelope model and explicit authority notice that Engineering source owns Port semantics.
- Added package-runtime admission wrapper that ties a descriptor to `RepresentationPackageDescriptor`, package entry, vector resource, package version, provenance, license, and runtime reference-directory rejection.
- Confirmed no runtime QElectroTech/reference parser is introduced.

### File List

- `kernel/package-model/src/main/kotlin/com/engineeringood/athena/packageplatform/SymbolYamlDescriptorAdmission.kt`
- `kernel/package-model/src/test/kotlin/com/engineeringood/athena/packageplatform/SymbolYamlDescriptorAdmissionTest.kt`
- `kernel/package-runtime/src/main/kotlin/com/engineeringood/athena/packageruntime/SymbolLibraryAssetAdmission.kt`
- `kernel/package-runtime/src/test/kotlin/com/engineeringood/athena/packageruntime/SymbolLibraryAssetAdmissionTest.kt`
- `_bmad-output/implementation-artifacts/m44/1-1-admit-locked-symbol-library-assets.md`
- `_bmad-output/implementation-artifacts/m44/sprint-status.yaml`

## Change Log

- 2026-08-07: Story created via BMad create-story flow for M44 Story 1.1.
- 2026-08-07: Implemented `symbol.yaml` admission and package-runtime asset admission; all story tasks validated and moved to review.
