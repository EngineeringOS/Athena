---
baseline_commit: 0b43cbe
---

# Story 5.4: Add M22 Boundary and Deferred-Scope Regression Checks

Status: done

## Story

As a product reviewer,
I want executable checks and documentation for M22 boundaries,
so that the milestone does not drift into deferred domains.

## Acceptance Criteria

1. Given the M22 PRD, architecture, sample project, and implementation artifacts, when boundary checks run, then they confirm no public repository/import ecosystem, broad IEC/QElectroTech ingestion, cabinet authoring, physical routing, AI layout, final solver-stack decision, or full EPLAN parity is claimed by M22.
2. Given layout round-trip is active, when boundary checks run, then they confirm no hidden canvas state persists layout truth.
3. Given M22 closes, when usage or retrospective handoff is read, then it records the deferred domains for future milestones.

## Tasks / Subtasks

- [x] Add M22 boundary regression coverage (AC: 1, 2, 3)
  - [x] Add failing frontend/static test over PRD, architecture, epics, usage docs, and sample files.
  - [x] Check deferred domains and no hidden canvas persistence.
- [x] Document deferred-scope handoff (AC: 1, 2, 3)
  - [x] Update usage docs with explicit M22 boundary and future-domain handoff language.
  - [x] Keep the boundary statement aligned with M22 PRD and architecture.
- [x] Run validation (AC: 1, 2, 3)
  - [x] Run affected boundary test.
  - [x] Run all M22 frontend script tests.
  - [x] Run frontend build.
  - [x] Run encoding audit.

## Dev Notes

### Current State

- M22 intentionally evaluates governed layout optimization and round-trip without becoming a public package repository, full IEC library, cabinet authoring, physical routing, AI layout, or EPLAN parity milestone.
- ELK remains optional and experimental behind Athena facts; M22 must not claim final solver-stack selection.

### Guardrails

- Do not add implementation scope while writing boundary checks.
- Do not weaken M22 achievements; distinguish "not in M22" from "never".
- Keep sample `.athena` files free of public registry, marketplace, cabinet authoring, physical routing, and AI layout claims.

### Testing Requirements

- `node --test ide/theia-frontend/scripts/athena-m22-boundary.test.mjs`
- `node --test ide/theia-frontend/scripts/athena-m22-*.test.mjs`
- `yarn workspace @engineeringood/athena-theia-frontend build`
- `powershell -ExecutionPolicy Bypass -File .\tools\encoding-audit.ps1`

### References

- [Source: `_bmad-output/implementation-artifacts/m22/epics.md` - Epic 5, Story 5.4]
- [Source: `_bmad-output/planning-artifacts/prds/prd-Athena-2026-07-17-m22/prd.md`]
- [Source: `_bmad-output/planning-artifacts/prds/prd-Athena-2026-07-17-m22/addendum.md`]
- [Source: `_bmad-output/planning-artifacts/architecture/architecture-Athena-2026-07-17-m22/ARCHITECTURE-SPINE.md`]
- [Source: `docs/usages/m22-proof-usage.md`]

## Dev Agent Record

### Agent Model Used

GPT-5 Codex

### Debug Log References

- `node --test ide/theia-frontend/scripts/athena-m22-boundary.test.mjs`
- `node --test ide/theia-frontend/scripts/athena-m22-*.test.mjs`
- `yarn workspace @engineeringood/athena-theia-frontend build`
- `powershell -ExecutionPolicy Bypass -File .\tools\encoding-audit.ps1`

### Completion Notes List

- Added an executable M22 boundary regression over PRD, architecture, epics, usage docs, sample sources, and layout source-edit tests.
- Documented the deferred-domain handoff for public repository/import ecosystem, full IEC/QElectroTech library ingestion, cabinet authoring, physical routing, AI layout, final solver-stack decision, and full EPLAN parity.
- Confirmed no hidden canvas state persists layout truth and approved adjustments remain reviewable `.athena` intent.

### File List

- `_bmad-output/implementation-artifacts/m22/5-4-add-m22-boundary-and-deferred-scope-regression-checks.md`
- `_bmad-output/implementation-artifacts/m22/sprint-status.yaml`
- `docs/usages/m22-proof-usage.md`
- `ide/theia-frontend/scripts/athena-m22-boundary.test.mjs`
- `ide/theia-frontend/scripts/athena-m22-layout-mutation-preview.test.mjs`

## Change Log

- 2026-07-18: Created M22 Story 5.4 with deferred-scope guardrail requirements.
- 2026-07-18: Added M22 boundary regression and deferred-domain usage handoff.
