---
baseline_commit: c7fda3877a76fd8db52a65510e1b123aed4304f1
---

# Story 4.4: Stale Artifact Purge And Retention Ledger

Status: done

## Story

As an Athena maintainer,
I want stale code, docs, sample references, screenshots, and design claims purged or explicitly retained,
so that the project remains clean and accurate after M27.

## Acceptance Criteria

1. Stale M27 code, docs, screenshots, sample references, and design claims are removed or corrected.
2. Intentionally retained deferred artifacts are recorded with owner, reason, and target milestone.
3. Encoding audit, relevant tests, and milestone documentation checks pass.
4. Deprecated desktop-viewer, Compose, and KMP frontend modules remain untouched unless a specific
   cleanup item documents safe removal.

## Completion Notes List

- Removed stale generated M27 IDE start logs:
  - `_bmad-output/implementation-artifacts/m27/ide-start-m27.stderr.log`
  - `_bmad-output/implementation-artifacts/m27/ide-start-m27.stdout.log`
- Retained current M27 launch/debug helper scripts because they provide reviewer workflow.
- Retained screenshot proof and graph-view failure note as current evidence and prevention record.
- Added cleanup/retention ledger.

## File List

- `_bmad-output/implementation-artifacts/m27/4-4-stale-artifact-purge-and-retention-ledger.md`
- `_bmad-output/implementation-artifacts/m27/m27-stale-artifact-retention-ledger.md`
- Removed `_bmad-output/implementation-artifacts/m27/ide-start-m27.stderr.log`
- Removed `_bmad-output/implementation-artifacts/m27/ide-start-m27.stdout.log`

## Verification

- `powershell -ExecutionPolicy Bypass -File .\tools\encoding-audit.ps1` - must pass after cleanup documentation.
