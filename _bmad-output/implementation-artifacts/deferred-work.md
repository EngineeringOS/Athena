# Deferred Work

## Deferred from: code review of M31 Story 2.4 (2026-07-22)

- Replace Graphical View's broad `port:` candidate affordance with registry-discovered compatible/rejected target evidence in M31 Story 4.2. Story 2.4 keeps backend compatibility authoritative and explicitly excludes graphical candidate UX.

## Deferred from: code review of 3-1-add-the-two-sheet-customer-projection-policy.md (2026-07-22)

- Align `create-semantic-relationship` projection ownership advertising with the governed graphical relationship workflow in Story 4.2. The old graph-command mutation bypass rejects direct execution today; M31-CL-009 owns replacing broad frontend affordance with backend registry evidence.

## Deferred from: M31 final closeout (2026-07-22)

- Consolidate `AthenaAuthoringSessionRuntimeService` preview-session compatibility into the governed Semantic Authoring Transaction runtime or explicitly version it as a read-only legacy preview API.
  Owner: M32 authoring-runtime owner.
  Reason: M31 product authoring uses transaction evidence, but older preview/session callers still use the compatibility path.
  Verification: CodeGraph caller review plus `:kernel:runtime:test`.

- Align non-Theia CLI/desktop/Compose relationship mutation surfaces with `SemanticRelationshipIntent` or explicitly retire them.
  Owner: M32 multi-surface authoring owner.
  Reason: Theia product flow is governed, but CLI/desktop compatibility still uses the low-level M8 `AthenaConnectPortsCommand`.
  Verification: fixed-string scan for `AthenaConnectPortsCommand`, runtime command tests, and a migrated multi-surface authoring proof.

- Replace Graphical View's broad `port:` candidate affordance with registry-discovered compatible and rejected target evidence before preview.
  Owner: M32 interaction/capability UX owner.
  Reason: M31 backend preview/accept is governed, but pre-preview highlighting still starts from semantic id prefix and node kind.
  Verification: frontend test proving candidate list comes from capability evidence and no `semanticId.startsWith('port:')` candidate gate remains in the relationship UX.

- Keep the legacy M26 display-title sheet-role fallback versioned and outside M31 payload authority, then remove it when old payload compatibility is no longer needed.
  Owner: M32 projection compatibility owner.
  Reason: M31 payloads use typed sheet-policy evidence; older M26 samples may still exercise the fallback.
  Verification: M31 typed policy tests plus scan proving no M31 sample/product proof depends on display-title parsing.

- Keep `_reference` occurrence fixtures as defensive legacy adapter/model tests only, then remove or rename them when the legacy repeated-reference fixture pack is retired.
  Owner: M32 projection compatibility owner.
  Reason: Normal M31 compiler/runtime/LSP payloads no longer emit duplicate `_reference` visual components, but tests still guard malformed legacy payload handling.
  Verification: M31 no-duplicate projection tests plus scan proving retained `_reference` hits are tests or M19 static history only.
