---
status: ready-for-dev
epic: 1
story: 1.1
title: Create routing-model contract home
---

# Story 1.1: Create routing-model contract home

As an Athena architect, I want `kernel/routing-model` to own routing contracts, so that route
semantics do not leak into presentation, renderer, or Theia code.

## Acceptance Criteria

- `kernel/routing-model` exists and is wired into the Gradle build if a new module is required.
- It exposes contracts for electrical connection intent, routing policy, port presentation policy,
  terminal anchors, route constraints, route facts, route segments, route labels, and route quality.
- It has no dependency on Theia, renderer DOM, canvas state, or frontend code.
- Kotlin files are grouped by responsibility and avoid a large mixed dump file.
- Tests prove the model can represent the M24 sample route contracts.

## References

- `../../planning-artifacts/prds/prd-Athena-2026-07-18-m24/prd.md`
- `../../planning-artifacts/architecture/architecture-Athena-2026-07-18-m24/ARCHITECTURE-SPINE.md`

## Notes

Honor architecture AD-1. Keep this as contract foundation only; do not implement route rendering here.
