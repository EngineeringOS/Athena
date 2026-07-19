---
status: review
epic: 1
story: 1.3
title: Add port presentation policy and terminal anchors
baseline_commit: e4b243ab9441d585e79c2d8effceb1a6692a3be2
---

# Story 1.3: Add port presentation policy and terminal anchors

As a layout/routing engineer, I want port sides and terminal anchors derived from policy, so that
renderer code does not hardcode universal input/output side rules.

## Acceptance Criteria

- `PortPresentationPolicy` selects preferred sides for input, output, power, ground, bidirectional,
  and terminal-block ports in the M24 sample.
- `TerminalAnchorFact` carries subject, port, occurrence, side, grid point, and policy source.
- Component centers are not used as route endpoints in the accepted M24 proof.
- Tests prove side selection is policy-owned and deterministic.
- Renderer code receives terminal anchors as facts and does not infer side meaning.

## References

- `../../planning-artifacts/prds/prd-Athena-2026-07-18-m24/prd.md`
- `../../planning-artifacts/architecture/architecture-Athena-2026-07-18-m24/ARCHITECTURE-SPINE.md`

## Notes

Honor architecture AD-3 and AD-4. Defaults may be simple, but must not be renderer-owned.

## Tasks/Subtasks

- [x] Add policy-owned preferred side selection for input, output, power, ground, bidirectional, and terminal roles.
- [x] Add terminal anchor facts carrying subject, port, occurrence, side, grid point, and policy source.
- [x] Keep component centers out of the accepted route endpoint contract.
- [x] Prove side selection is deterministic and policy-owned.

## Dev Agent Record

### Implementation Notes

- Extended `PortPresentationPolicy` with a policy source, deterministic terminal-anchor factory, and explicit side resolution for all M24 port roles.
- Extended `TerminalAnchorFact` with grid-point and policy-source facts while preserving the earlier route-model contract.
- Kept the contract kernel-only; no renderer or Theia inference participates.

### Debug Log

- Initial focused test run failed because the policy did not yet expose a terminal-anchor factory or policy source, and the anchor contract lacked grid-point facts.
- Added the missing policy-owned anchor derivation and contract fields.
- `:kernel:routing-model:test --tests "com.engineeringood.athena.routing.PortPresentationPolicyTest"` passed.
- `:kernel:routing-model:test` passed.

### Completion Notes

- Implemented policy-owned terminal-anchor derivation for Story 1.3.
- Confirmed the routing-model suite remains green after the new contract additions.

## File List

- `_bmad-output/implementation-artifacts/m24/1-3-add-port-presentation-policy-and-terminal-anchors.md`
- `_bmad-output/implementation-artifacts/m24/sprint-status.yaml`
- `kernel/routing-model/src/main/kotlin/com/engineeringood/athena/routing/PortPresentationPolicy.kt`
- `kernel/routing-model/src/test/kotlin/com/engineeringood/athena/routing/PortPresentationPolicyTest.kt`

## Change Log

- Added policy-owned side selection and terminal-anchor derivation for M24 routing contracts.
- Added grid-point and policy-source anchor facts.
- Verified the routing-model test suite passes.

## Status

review
