# M31 Final Retrospective Summary: Governed Engineering Model Authoring

## Milestone Outcome

M31 completed the first customer-usable Athena engineering model authoring loop:

```text
Graphical action
  -> capability discovery
  -> Semantic Action Intent
  -> Semantic Authoring Transaction
  -> revision-bound preview
  -> backend-owned source edit
  -> Mutation Authority
  -> compile/reproject
  -> two-sheet professional product proof
```

The milestone aligns with Athena's semantic standard: `.athena` remains semantic persistence,
Theia remains an adapter, representation and document outputs are re-derived, and source edits are
planned by the backend rather than the frontend.

## What Changed

- Authoring capabilities extend the M29 `SemanticCapabilityRegistry`.
- Mutable actions are represented by single-intent Semantic Authoring Transactions.
- Entity creation uses semantic Engineering Concept Templates with nested ports.
- Relationship authoring uses `SemanticRelationshipIntent`; the legacy `ConnectPortsIntent`
  product path is removed.
- Source edit planning is backend-owned and Revision Guard bound.
- The M31 sample projects to exactly two governed sheets with semantic cross-sheet reference proof.
- Graphical View supports the create/connect/inspect/switch/reveal/reopen product workflow.
- Product smoke proves the workflow structurally and captures secondary screenshot evidence.

## Effective Practices

- RED-GREEN-REFACTOR worked best when tests asserted authority boundaries, not just happy payloads.
- CodeGraph review helped separate true production paths from compatibility or test-only paths.
- The mandatory polish/purge gate prevented stale names, generated residue, and false documentation
  claims from surviving each story.
- Structured proof avoided visual guessing and made customer-demo confidence repeatable.

## Blockers And Root Causes

- Authority boundaries were close together: preview session, transaction runtime, source planning,
  mutation authority, and reprojection all touch the same workflow. The fix was explicit contracts
  and stage-by-stage tests.
- Frontend races were frequent because preview, selected subject, editor revision, source edit
  evidence, sheet state, and async backend results can change independently. The fix was current
  token/revision/source checks at accept time.
- Legacy examples and fixtures looked like current product truth. The fix was cleanup-ledger
  classification and post-M31 deferred ownership.

## Cleanup Outcome

- M31 cleanup-ledger has no unowned open item.
- M31-CL-002, M31-CL-003, M31-CL-005, and M31-CL-007 were resolved in earlier stories.
- M31-CL-004, M31-CL-006, M31-CL-010, and M31-CL-011 were resolved or reclassified during final
  closeout.
- M31-CL-001, M31-CL-008, and M31-CL-009 are explicitly deferred beyond M31 with owner, reason,
  target, and verification in both `cleanup-ledger.md` and `deferred-work.md`.

## Prevention Actions

1. Keep every future story's final task as polish/purge plus AC-to-evidence.
2. Treat compatibility code as versioned contract or remove it; never leave it as anonymous residue.
3. Make structured proof authoritative for product claims; screenshots remain supporting evidence.
4. Run stale-name scans against active source/docs/examples/tests while excluding generated build
   output and reference mirrors.
5. Keep Gradle verification sequential on Windows.

## M32 Handoff

M32 planning should explicitly decide whether the next milestone focuses on:

- multi-surface authoring alignment for CLI/desktop/Compose compatibility;
- richer professional representation/library depth;
- semantic agent/runtime consumption of the M31 authoring transaction contract.

The concrete deferred inputs are in `_bmad-output/implementation-artifacts/deferred-work.md`.
