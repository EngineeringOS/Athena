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

## Deferred from: code review of M34 Story 1.1 (2026-07-24)

- Resolve composition children, reject nested Elements and cycles, and validate exported anchors in
  Story 1.3. Owner: M34 Story 1.3 implementation agent. Verification: intrinsic composition compiler
  tests with stable source diagnostics.
- Reject ambiguous Profile/Binding selection in Story 3.1. Owner: M34 Story 3.1 implementation agent.
  Verification: deterministic binding resolver tests across reversed rule order.
- Evaluate project-port direction and signal compatibility against canonical Element anchors in Story
  3.2. Owner: M34 Story 3.2 implementation agent. Verification: accepted/rejected binding tests with
  source-spanned diagnostics.
- Remove independently authored descriptors and descriptor-to-definition reverse construction in Story
  3.3. Owner: M34 Story 3.3 implementation agent. Verification: generated-descriptor admission tests and
  the exact migration-ledger search gate.
- Remove active legacy anatomy and Presentation Primitive Cabinet producers in Story 3.4. Owner: M34
  Story 3.4 implementation agent. Verification: Graphic Primitive-only structured Cabinet proof and
  migration-ledger source gates.
- Establish a tracked M33/M34 prerequisite baseline before final M34 handoff; Story 1.1 currently builds
  over preserved uncommitted M33 workspace dependencies. Owner: M34 Story 4.3 implementation agent.
  Verification: clean-worktree checkout runs the full M34 suite and product smoke from committed sources.

## Deferred from: M34 post-E2E review and M35 refactor input (2026-07-27)

- Align `.athena` project source package declarations with filesystem hierarchy, using the same
  discipline as Java package layout. Example: `package com.engineeringood.m34.professional` should
  live under the matching `src/com/engineeringood/m34/professional/...` path instead of a flat sample
  source folder. Owner: M35 package/source-layout owner. Verification: repository lint that rejects
  package/path mismatch for governed project and package sources.

- Add a complex vendor element proof where Athena source references a package-local SVG geometry
  resource and owns all exposed metadata: identity, version, anchors, labels, compatibility, roles,
  direction/signal predicates, and binding exposure. Owner: M35 representation-material owner.
  Verification: compiler tests and product proof using a non-trivial SVG-backed element, not only
  native primitive symbols.

- Design package/resource resolution for third-party representation libraries so resources are
  addressed relative to their owning package hierarchy, not by fragile workspace-relative paths.
  Owner: M35 package-platform owner. Verification: fixture package laid out like
  `com/vendor/product/...` with colocated `.athena` and SVG resources, plus dependency-style lookup
  tests for IEC standard libraries and vendor/user composite elements.

- Sketch a future resource URI schema for package-local SVG assets only after M35. Default lookup
  stays package-local and classpath-like; remote resource fetching or other URI authorities stay
  deferred to a later milestone so M35 keeps one admitted source path.

- Define the editable graphics contract: every rendered element occurrence must trace back to the
  authoritative `.athena` source declaration, and graphic-side edits must round-trip through governed
  Athena source mutation, compile, lint, and verification. Owner: M35 authoring/representation owner.
  Verification: source-to-graphic and graphic-to-source tests proving no renderer-side metadata
  authority and no unverified direct presentation mutation.
