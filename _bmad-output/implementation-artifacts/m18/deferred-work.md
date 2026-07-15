# M18 Deferred Work

## Deferred from: M18 closeout scope boundary (2026-07-15)

- Remote registry resolution, cloud package resolution, package marketplace behavior, and package publish flows remain deferred to later ecosystem milestones.
- Full export/visibility semantics, broad authored-language redesign, and broad declaration-family expansion remain deferred to later language milestones.
- Multi-root sessions and package-local manifest redesign remain deferred to later repository/workspace milestones.
- Frontend-owned semantic resolution remains excluded. The Theia IDE may render compiler/LSP results, but compiler/LSP remains the package/import/symbol authority.
- `apps/desktop-viewer`, desktop-viewer canvas behavior, and Kotlin Compose UI work remain excluded from M18.
- Future package-aware growth should extend the compiler-owned project semantic graph foundation documented in `m18-closeout-boundaries.md`.

## Deferred from: code review of 1-1-parse-package-declarations-into-authored-ast (2026-07-15)

- Strengthen `AthenaCompilerComponentKnowledgeIntegrationTest` so the active implementation inventory is asserted by identity rather than count alone. This is pre-existing M15 component-knowledge test completeness and is not package-syntax behavior.
