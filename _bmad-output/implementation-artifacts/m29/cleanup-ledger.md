# M29 Cleanup Ledger

This ledger records stale, legacy, or intentionally retained paths exposed during M29. Items are
removed when the target milestone lands or when an equivalent Interaction IR path fully replaces
them.

## Legacy Connect-Ports Inventory

Structured proof payload:

```text
payloadKind: legacy-connect-ports-inventory
schemaVersion: m29.interaction.v1
activeSourceContext: M29 relationship mutation cleanup
```

| Path | Classification | Owner | Reason | Target |
| --- | --- | --- | --- | --- |
| `kernel/authoring-model/src/main/kotlin/com/engineeringood/athena/authoring/AuthoringIntentModels.kt` | compatibility-adapter | authoring kernel | `ConnectPortsIntent` remains only to lift older callers into `SemanticRelationshipIntent`. | M30 relationship/action cleanup |
| `kernel/authoring-model/src/test/kotlin/com/engineeringood/athena/authoring/AuthoringIntentContractTest.kt` | compatibility-adapter | authoring kernel | Keeps regression coverage that legacy connect intent lifts into semantic relationship intent. | M30 relationship/action cleanup |
| `kernel/runtime/src/main/kotlin/com/engineeringood/athena/runtime/AthenaAuthoringSessionRuntimeService.kt` | compatibility-adapter | runtime authoring | Runtime still accepts existing authoring previews while M29 routes new work through Interaction IR. | M30 relationship/action cleanup |
| `kernel/runtime/src/test/kotlin/com/engineeringood/athena/runtime/AthenaAuthoringSessionRuntimeServiceTest.kt` | compatibility-adapter | runtime authoring | Covers retained runtime compatibility behavior. | M30 relationship/action cleanup |
| `ide/lsp/src/main/kotlin/com/engineeringood/athena/ide/lsp/AthenaAuthoringProtocol.kt` | compatibility-adapter | LSP authoring | Retains `connect-ports` transport shape for existing callers; new M29 work uses `semantic-relationship`. | M30 relationship/action cleanup |
| `ide/lsp/src/main/kotlin/com/engineeringood/athena/ide/lsp/AthenaConnectPortsSourceEditProtocol.kt` | compatibility-adapter | LSP authoring | Backend source-edit gate converts `ConnectPortsIntent` into `SemanticRelationshipIntent` for validation. | M30 relationship/action cleanup |
| `ide/lsp/src/main/kotlin/com/engineeringood/athena/ide/lsp/AthenaLanguageServer.kt` | compatibility-adapter | LSP authoring | Decision handler still dispatches retained legacy intent to the governed backend source-edit gate. | M30 relationship/action cleanup |
| `ide/lsp/src/test/kotlin/com/engineeringood/athena/ide/lsp/AthenaAuthoringRequestTest.kt` | compatibility-adapter | LSP authoring | Retains regression coverage for old connect-port accept while semantic relationship path is primary. | M30 relationship/action cleanup |
| `ide/lsp/src/main/kotlin/com/engineeringood/athena/ide/lsp/AthenaGraphCommandIntentProtocol.kt` | retained-with-owner-target-milestone | graph command runtime | Earlier graph-command proof still exposes `connect-ports` as a projection command id. | M30 graph command migration |
| `kernel/runtime/src/main/kotlin/com/engineeringood/athena/runtime/AthenaGraphCommandIntentRuntimeService.kt` | retained-with-owner-target-milestone | graph command runtime | Existing projection command runtime still validates the old command id. | M30 graph command migration |
| `kernel/runtime/src/test/kotlin/com/engineeringood/athena/runtime/AthenaGraphCommandIntentServiceTest.kt` | retained-with-owner-target-milestone | graph command runtime | Covers legacy graph command behavior until the command id moves to Interaction IR. | M30 graph command migration |
| `ide/lsp/src/test/kotlin/com/engineeringood/athena/ide/lsp/AthenaProjectionRequestTest.kt` | retained-with-owner-target-milestone | graph command runtime | Projection command tests still assert the legacy command id. | M30 graph command migration |
| `ide/theia-frontend/src/browser/athena-graph-command-intent-protocol.ts` | retained-with-owner-target-milestone | Theia adapter | Existing adapter transport for old graph command id; not used by new M29 relationship story code. | M30 graph command migration |
| `ide/theia-frontend/src/browser/athena-graph-adapter-service.ts` | retained-with-owner-target-milestone | Theia adapter | Provides the old graph command request bridge. | M30 graph command migration |
| `ide/theia-frontend/src/browser/athena-graph-workbench-widget.tsx` | retained-with-owner-target-milestone | Theia adapter | Existing workbench command path remains until replaced by Interaction IR command dispatch. | M30 graph command migration |
| `ide/theia-frontend/scripts/athena-graph-command-intent-protocol.test.mjs` | retained-with-owner-target-milestone | Theia adapter | Covers old graph command protocol while retained. | M30 graph command migration |
| `ide/theia-frontend/scripts/athena-guided-connection-model.test.mjs` | retained-with-owner-target-milestone | Theia adapter | Legacy guided connection test still checks old authoring request shape. | M30 graph command migration |
| `kernel/compiler/src/test/kotlin/com/engineeringood/athena/compiler/AthenaCompilerTest.kt` | retained-with-owner-target-milestone | compiler projection | Historical command id assertion retained until graph command migration. | M30 graph command migration |
| `kernel/authoring-model/README.md` | retained-with-owner-target-milestone | docs | Documents compatibility intent while migration remains incomplete. | M30 docs cleanup |
| `kernel/authoring-model/README.zh-CN.md` | retained-with-owner-target-milestone | docs | Chinese compatibility documentation mirrors English doc. | M30 docs cleanup |

## Story 4.3 Cleanup Result

- The M29 relationship preview model no longer synthesizes authored `connect` source text in Theia.
- Preview source impact is marked as backend-runtime/source-edit owned.
- Preview invalidation now returns stale state for source reload, projection refresh, active source
  change, and accepted mutation; cancel clears the transient preview.

## Final M29 Polish/Purge Review

Structured proof payload:

```text
payloadKind: final-polish-purge-review
schemaVersion: m29.interaction.v1
activeSourceContext: M29 closeout cleanup
```

| Area | Result | Owner | Follow-up |
| --- | --- | --- | --- |
| Story files and sprint tracking | Duplicate Story 1.3 sprint key removed; duplicate-key audit added to closeout verification. | M29 closeout | Keep duplicate-key audit for future milestone closeout. |
| M29 product smoke | Retained as required proof path; no temporary smoke output files are produced. | interaction/product proof | None. |
| Shared Electron opener smoke hook | Retained as adapter-only smoke control so product proof reveals Graphical View through a registered Athena command before DOM fallback. It carries no semantic identity. | Theia adapter/product smoke | Reassess if Theia exposes a stable non-DOM command execution seam for smoke. |
| Legacy `connect-ports` paths | Retained only as listed compatibility/target-milestone inventory above. | authoring/runtime/Theia owners listed above | M30 relationship/action cleanup. |
| M29 docs and design | Usage, retrospective, architecture, PRD, and cleanup ledger retained as milestone truth. | documentation | Remove or supersede only when M30 updates the architecture. |
