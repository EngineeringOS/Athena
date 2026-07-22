# M31 Cleanup Ledger

This ledger records temporary compatibility and migration paths retained during M31. Each entry must
be removed, migrated, re-verified, or explicitly transferred to a post-M31 owner before the
milestone closes.

| ID | Artifact | Owner | Reason retained | Target | Verification |
| --- | --- | --- | --- | --- | --- |
| _None_ | All M31-targeted cleanup entries are resolved or transferred below. | M31 Story 5.3 implementation agent | M31 closeout requires no unowned open cleanup item. | Story 5.3 | Final purge scans, regression commands, retrospectives, and shared deferred-work updates. |

## Deferred Beyond M31

These retained paths are not M31 customer-acceptance blockers, but they remain owned compatibility
work and are mirrored in `_bmad-output/implementation-artifacts/deferred-work.md`.

| ID | Artifact | Owner | Reason retained | Target | Verification |
| --- | --- | --- | --- | --- | --- |
| M31-CL-001 | `AthenaAuthoringSessionRuntimeService` preview-only session compatibility path | M32 authoring-runtime owner | It performs no canonical mutation and remains a compatibility bridge for older preview/session callers while M31 product authoring uses governed transaction evidence. | M32 authoring-runtime consolidation | CodeGraph review; `:kernel:runtime:test`; Story 4.3 lifecycle tests. |
| M31-CL-008 | CLI/desktop/Compose paths using low-level `AthenaConnectPortsCommand` | M32 multi-surface authoring owner | Theia product authoring is governed by `SemanticRelationshipIntent`, but non-Theia surfaces still rely on the older low-level M8 runtime command and are outside M31 customer proof. | M32 multi-surface semantic authoring alignment | Fixed-string scan finds use in CLI/desktop/runtime tests; M31 product scan has no `ConnectPortsIntent` path. |
| M31-CL-009 | Graphical View's broad `port:` candidate affordance before backend preview | M32 interaction/capability UX owner | Backend preview/accept is governed, but candidate highlighting still starts from port label identity before rejected-reason evidence is displayed. | M32 relationship candidate discovery UX | CodeGraph `isConnectablePortNode` review; M31 relationship preview and product smoke prove backend authority. |

## Resolved Items

| ID | Resolution | Story | Evidence |
| --- | --- | --- | --- |
| M31-CL-002 | Reverified `AthenaSourceMutationRuntimeService` as the downstream dirty-source semantic diff evaluator. Source planning now has a distinct compiler-owned planner and parser gate; no duplicate semantic-diff evaluator was added. | Story 2.2 | `:kernel:runtime:test`, `BackendAuthoringSourceEditPlannerTest`, CodeGraph authority review. |
| M31-CL-003 | Removed `ConnectPortsIntent`, its compatibility adapter, legacy production transport/file/function names, direct graph-command mutation bypass, and frontend-local relationship preview authority. Active authoring now uses `SemanticRelationshipIntent` and backend-governed evidence. | Story 2.4 | Authoring/runtime/LSP/frontend tests, generic-name source scan, and CodeGraph authority review. |
| M31-CL-001 | Reclassified as versioned preview-session compatibility outside M31 acceptance. It is not a source, relationship, or projection mutation authority for the M31 product flow. | Story 5.3 | CodeGraph review; M31 lifecycle/product proof; transferred to Deferred Beyond M31. |
| M31-CL-004 | Resolved by consuming backend-provided `authoringTemplateIds` from component knowledge payloads rather than hard-coding a frontend concept-template mapping. | Story 5.3 | CodeGraph `athena-component-panel-model.ts` review; frontend component-panel model tests. |
| M31-CL-005 | Removed component-named create/update planner APIs, deleted hard-coded concept/type/port tables, renamed the update protocol, and routed create/relationship/layout serialization through backend authority. | Story 2.2 | `:ide:lsp:test`, frontend 166-test suite, no-old-name and no-frontend-serializer scans. |
| M31-CL-006 | Resolved for M31 product authoring: governed entity creation previews retain backend source-edit evidence through decision, while older non-M31 compatibility flows are covered by the backend planner rather than a frontend serializer. | Story 5.3 | `GovernedEntityCreationPreviewService` and product smoke evidence; no frontend source serializer scan. |
| M31-CL-007 | Removed a semantically false `up`-to-`U1` terminal binding from the M31 motor adapter. The adapter now resolves the M30 definition/policy/composition facts without claiming unsupported port anatomy. | Story 2.3 | Governed LSP motor proof, `:extensions:domain-electrical:test`, and `:kernel:representation-model:test`. |
| M31-CL-008 | Transferred to M32 because it concerns non-Theia CLI/desktop/Compose compatibility surfaces outside the M31 product proof. | Story 5.3 | Fixed-string scan shows remaining low-level command use only in runtime/CLI/desktop/test history, not `ConnectPortsIntent` product transport. |
| M31-CL-009 | Transferred to M32 because backend relationship preview/accept is governed but candidate highlighting still has a broad pre-preview affordance. | Story 5.3 | CodeGraph `isConnectablePortNode` review; M31 Story 4.2 and 5.2 relationship proof. |
| M31-CL-010 | Reclassified as an explicitly versioned legacy compatibility contract for pre-M31/M26 payloads. M31 payloads use typed sheet-policy evidence and do not parse sheet labels. | Story 5.3 | Story 3.1 typed policy tests; stale M31 three-sheet scan; no active M31 display-title fallback hit outside ledger/history. |
| M31-CL-011 | Reclassified as defensive legacy fixture coverage. Normal compiler/runtime/LSP M31 documentation payloads do not emit duplicate `_reference` visual components. | Story 5.3 | LSP/runtime/compiler tests assert no `_reference` projection components; retained hits are tests or M19 static example history. |
