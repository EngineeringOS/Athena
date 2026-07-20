---
baseline_commit: c7fda3877a76fd8db52a65510e1b123aed4304f1
---

# Story 3.3: Semantic Connection Preview Foundation

Status: done

## Story

As an engineer,
I want to preview a possible semantic connection between endpoints,
so that I can evaluate compatibility and projected route behavior before any source mutation exists.

## Acceptance Criteria

1. Given two semantic endpoints are selected for connection preview, compatibility is evaluated
   using endpoint identity, signal compatibility, direction, port metadata, spatial intent, and
   route geometry preview where available.
2. Incompatible endpoints produce a governed explanation rather than a hidden canvas failure.
3. Given a connection preview is displayed, cancel/change/reload does not modify `.athena` source
   and no hidden connection truth is persisted in Theia state.

## Tasks / Subtasks

- [x] Verify preview remains semantic and transient (AC: 1, 2, 3)
  - [x] Frontend request builders keep connect-preview payloads graph-owned and transport-safe.
  - [x] Compatibility target filtering uses signal family, direction, and protocol metadata.
  - [x] Port connection state maps canonical connection state onto ports without creating canvas truth.
- [x] Preserve mutation deferral (AC: 3)
  - [x] M27 story records and PRD defer auto-connection source mutation acceptance to M28.
  - [x] No new `.athena` syntax is introduced.

## Dev Notes

- M27 proves preview foundation only. Accepting a generated `connect` source mutation remains M28
  scope unless a later story explicitly reuses existing governed mutation authority.

## Dev Agent Record

### Agent Model Used

Codex GPT-5

### Completion Notes List

- Fresh frontend tests include `buildConnectPortsPreviewRequest emits a graph-owned governed
  connect preview request`, compatibility filtering, and canonical port connection state mapping.

### File List

- `_bmad-output/implementation-artifacts/m27/3-3-semantic-connection-preview-foundation.md`

## Change Log

- 2026-07-20: Created and closed Story 3.3 from existing frontend preview contract coverage.

## Verification

- `yarn --cwd ide/theia-frontend test` - passed earlier in M27 graph-view closeout, 133/133 tests.
