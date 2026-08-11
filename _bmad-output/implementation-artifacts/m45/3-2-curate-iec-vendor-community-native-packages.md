---
baseline_commit: 9ca7c80239d38d9e2a4880886e71e27e2923c41b
---

# Story 3.2: Curate IEC, Vendor, Community Native Packages

Status: review

## Story

As an engineer,
I want local IEC, vendor, and community packages with real Symbols, Elements, and Parts,
so the rolling-shutter project proves a complete governed asset ecosystem.

## Acceptance Criteria

1. M45 example has at least three direct packages under `packages/<package-name>/`: IEC, vendor, and
   community; each has native `package.yaml` metadata and package-local copied SVG resources.
2. At least ten native Symbol/Element definitions and five Part definitions admit `PACKAGE_READY` with
   stable PackageItem identities, center/anchors, compatibility metadata, provenance, and license.
3. Packages use one common PackageItem envelope and explicit package/semantic dependencies; no nested
   registry/representation/engineering directory and no duplicate descriptor format exists.
4. Compiler/runtime never reads `.elmt`, HTML, XML, `reference/elements`, or `reference/elements_contrib`.
5. Lock output and package admission are deterministic; invalid/incomplete package items remain outside
   runtime snapshot and are visible only through diagnostics.

## Tasks / Subtasks

- [x] Task 1: Curate three direct native package manifests and package-local SVG resources.
- [x] Task 2: Author ten+ Symbol/Element items and five compatible Part items with provenance/license.
- [x] Task 3: Add golden package admission/lock tests and stale external-path scans.

## Dev Notes

- This is fixture/product work, not an external-format importer. Reference assets may be copied during
  fixture preparation only; runtime sees only package-local SVG plus Athena-native metadata.
- Future third-party tooling may precompile package metadata/resources into a governed package index;
  runtime will consume admitted index/package snapshots by stable identity and digest, never filename
  matching. M45 keeps this derived index future-only and proves native local package source.
- Keep package names as direct child directory names. Root `athena.yaml` remains dependency authority;
  `athena.lock` is compiler-owned.
- Use existing PackageItem, SymbolGeometry, Element, Part, admission, repository graph, and lock seams.
- No SVG-to-engineering semantic inference. Port compatibility metadata constrains source-owned ports.
- Evidence belongs under `_bmad-output/implementation-artifacts/m45/`.

### References

- `_bmad-output/planning-artifacts/m45/epics.md` Epic 3 / Story 3.2.
- `_bmad-output/planning-artifacts/prds/prd-Athena-2026-08-08-m45/prd.md` Sections 2, 3, 4, 9, 10.
- `_bmad-output/planning-artifacts/architecture/architecture-Athena-2026-08-08-m45/ARCHITECTURE-SPINE.md` AD-2, AD-3, AD-4, AD-9, AD-15.
- `AGENTS.md` reference-only and package-local source rules.

## Dev Agent Record

### Agent Model Used

GPT-5 Codex

### Debug Log References

- Story 3.1 established safe package-local SVG admission and existing lock/resource authority.
- M45 fixture now has direct IEC, vendor, community packages, native package-local source/SVG, and lock proof.
- Compiler test verifies package catalog shape, item/resource counts, lock materialization, and no external-format input.

### Completion Notes List

- Added direct package manifests under `examples/m45/rolling-shutter/packages/<package-name>`.
- Curated ten package-local SVG assets and native Symbol/Element/Part source definitions.
- Added deterministic M45 catalog/lock regression test. Compiler test plus audits pass.

### File List

- `examples/m45/rolling-shutter/athena.yaml`
- `examples/m45/rolling-shutter/athena.lock`
- `examples/m45/rolling-shutter/packages/com.athena.iec/package.yaml`
- `examples/m45/rolling-shutter/packages/com.vendor.siemens/package.yaml`
- `examples/m45/rolling-shutter/packages/com.community.elements/package.yaml`
- `examples/m45/rolling-shutter/packages/*/src/*.athena`
- `examples/m45/rolling-shutter/packages/*/resources/*.svg`
- `examples/m45/rolling-shutter/src/com/engineeringood/m45/rollingshutter/*.athena`
- `kernel/compiler/src/main/kotlin/com/engineeringood/athena/compiler/repository/AthenaRepositoryContractLoader.kt`
- `kernel/compiler/src/test/kotlin/com/engineeringood/athena/compiler/repository/M45NativePackageCatalogTest.kt`
- `_bmad-output/implementation-artifacts/m45/3-2-curate-iec-vendor-community-native-packages.md`

### Change Log

- 2026-08-09: Created Story 3.2 context; status `ready-for-dev`.
- 2026-08-09: Curated native package fixture and lock/catalog proof; status `review`.
