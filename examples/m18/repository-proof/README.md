# M18 Repository-Backed Proof

This folder closes the M18 proof corpus with checked-in governed repository fixtures.

## Inventory

- `valid-workspace/` is a local governed repository with:
  - single-package success in `src/single-package-success.athena`
  - cross-package success in `src/cross-package-consumer.athena`
  - invalid import diagnostics in `src/invalid-import.athena`
  - unresolved symbol diagnostics in `src/unresolved-symbol.athena`
  - local vendor/governed package availability in `vendor/controls/`
- `graph-invalid/` is a local governed repository fixture used to prove graph-invalid publication diagnostics.

The corpus is local and repository-backed. It does not imply remote registry, marketplace, publish,
multi-root, frontend-owned semantic resolution, desktop-viewer, or Kotlin Compose behavior.
