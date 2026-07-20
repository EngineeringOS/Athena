# M27 Stale Artifact Purge And Retention Ledger

Date: 2026-07-20

## Removed

| Artifact | Reason |
| --- | --- |
| `_bmad-output/implementation-artifacts/m27/ide-start-m27.stderr.log` | Generated run log, stale after smoke/build verification. |
| `_bmad-output/implementation-artifacts/m27/ide-start-m27.stdout.log` | Generated run log, stale after smoke/build verification. |

## Retained

| Artifact | Owner | Reason | Target |
| --- | --- | --- | --- |
| `_bmad-output/implementation-artifacts/m27/ide-start-m27.cmd` | Athena M27 | Reviewer convenience launch helper. | Keep through M27 review. |
| `_bmad-output/implementation-artifacts/m27/ide-debug-m27.cmd` | Athena M27 | Debug helper for live DOM/SVG inspection if the visual proof regresses again. | Keep through M27/M28 UI work. |
| `_bmad-output/implementation-artifacts/m27/proofs/m27-graph-workbench-smoke.png` | Athena M27 | Current product-path screenshot proof. | Replace on next accepted visual proof update. |
| `_bmad-output/implementation-artifacts/m27/M27-GRAPH-VIEW-FAILURE-NOTE.md` | Athena M27 | Incident record and prevention checklist for SVG bounds and projection-scope mistakes. | Keep as M27 retrospective evidence. |
| `_bmad-output/planning-artifacts/prds/prd-Athena-2026-07-20-m27` | Athena planning | Planning source of truth for M27 scope and exclusions. | Keep. |
| `_bmad-output/planning-artifacts/architecture/architecture-Athena-2026-07-20-m27` | Athena planning | Architecture source of truth for M27 authority chain. | Keep. |

## Corrected Claims

- M27 usage documentation now states QElectroTech is reference-only, not import/runtime authority.
- M27 story records state no new `.athena` syntax was introduced.
- M27 story records state auto-connection mutation acceptance remains deferred.
- M27 failure note now distinguishes publication sheet frame, live SVG scene bounds, component
  bounds, and DOM viewport bounds.

## Untouched Out-Of-Scope Areas

- Deprecated desktop viewer
- Compose workbench
- KMP frontend modules
- QElectroTech source mirror
