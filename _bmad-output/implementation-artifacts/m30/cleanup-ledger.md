# M30 Cleanup Ledger

## Purpose

Track stale code, docs, samples, screenshots, tests, compatibility paths, and design claims removed
or intentionally retained during M30.

## Required Entry Format

```text
ID:
Story:
Area:
Action: removed | updated | retained | deferred
Owner:
Reason:
Target milestone:
Verification:
```

## Entries

ID: M30-CL-001
Story: 1.2
Area: `extensions/domain-electrical/src/main/kotlin/com/engineeringood/athena/domain/electricalruntime/ElectricalRuntimePresentationPacks.kt`
Action: retained | deferred
Owner: M30 representation implementation
Reason: Existing pre-M30 electrical presentation packs contain viewBox/path/stroke visual vocabulary. They are not semantic kernel truth, but they remain temporarily as a bridge until native M30 representation definitions, binding, and renderer integration replace the old path.
Target milestone: M30 Stories 2.1-5.3
Verification: Boundary audit recorded in `_bmad-output/implementation-artifacts/m30/representation-semantic-contract-audit.md`; no renderer fallback or source syntax dependency added in Story 1.2.

ID: M30-CL-002
Story: 6.3
Area: `_bmad-output/implementation-artifacts/m30/screenshots/m30-graph-workbench-smoke.png`
Action: retained
Owner: M30 product smoke guard
Reason: Human-inspectable screenshot proof artifact required by Story 6.3 acceptance criteria. The structured product smoke remains the authoritative proof; this PNG is retained only as visual regression evidence.
Target milestone: M31 or the next visual proof storage policy milestone
Verification: `yarn --cwd ide start:smoke:m30` passed and regenerated the PNG at 51038 bytes.

ID: M30-CL-003
Story: 7.2
Area: `_bmad-output/implementation-artifacts/m30/cleanup-ledger.md`
Action: updated
Owner: M30 closeout
Reason: Cross-checked retained/deferred artifacts and confirmed each ledger entry carries owner, reason, target milestone, and verification. No undocumented removed artifact was found during Story 7.2.
Target milestone: M30 closeout
Verification: `node --test ide\theia-frontend\scripts\athena-m30-retrospective-cleanup-ledger.test.mjs` validates ledger entry completeness.

ID: M30-CL-004
Story: 7.3
Area: `ide/theia-product/scripts/verify-athena-m30-sample-project.js`; `_bmad-output/implementation-artifacts/m30/5-4-preserve-projection-and-sheet-switch-controls.md`
Action: updated
Owner: M30 closeout
Reason: Final regression found stale Documentation-default wording in both the executable M30 smoke path and Story 5.4, while the current M29/M30 product contract is Cabinet as the default Graphical View. Both the executable assertion and the story artifact were aligned to the live contract.
Target milestone: M30 closeout
Verification: `yarn --cwd ide start:smoke:m29` and `yarn --cwd ide start:smoke:m30` both passed after the executable assertion update.
