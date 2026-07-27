---
title: M34 Professional Control Drawing Sprint Change Proposal
date: 2026-07-26
status: approved
approval: User approved the proposed language and semantic correction with "ok" on 2026-07-26.
---

# M34 Professional Control Drawing Sprint Change Proposal

## 1. Issue Summary

M34 Story 4.3 exposed that the implemented Cabinet row is not credible customer-demo evidence and
does not match `draft/screenshort/equipement_d'un_volet_roulant.png`. The failure is architectural,
not a renderer styling defect:

- the active path emits one occurrence per semantic component;
- the semantic model cannot represent one physical device with coil/contact functional units;
- authored layout admits only weak `near`, `below`, `aligned-with`, and `grouped-with` relations;
- structural geometry still places components by list index;
- native Athena graphics expose only line primitives even though Graphic Primitive IR is richer;
- the sample models enclosure, rails, route channels, and title labels as fake engineering devices;
- some layout role derivation infers meaning from names such as `QF`, `XT`, and `M`.

The target and supporting reference evidence are concrete:

- approved target: `draft/screenshort/equipement_d'un_volet_roulant.png`;
- reference project: `reference/qelectrotech-source-mirror/examples/schema_indus.qet`;
- target structure: 17 columns, 8 rows, 48 element occurrences, 69 conductors;
- repeated physical subjects include KM1/KM2 power contacts, coils, NO contacts, and NC contacts.

QElectroTech is design evidence only. Its XML page model, runtime element collection, and
Master/Slave object hierarchy are not Athena architecture.

## 2. Impact Analysis

### Epic Impact

- Epics 1-3 remain valid foundations: typed representation source, governed SVG compilation,
  package snapshots, binding, and Graphic Primitive IR are retained.
- Epic 4 remains historical evidence of the failed Cabinet-only product assumption. Story 4.3 stays
  open until the corrective proof succeeds.
- Corrective Epic 5 is added to close M34 against the approved professional control-drawing target.

### Artifact Conflicts

- The PRD statements that Cabinet is the only M34 customer surface are superseded.
- Architecture AD-7 and the deferred Engineering Component System note require a narrow exception:
  M34 adds project-owned Engineering Functions, not a reusable component-definition platform.
- The sample and active derivation must stop treating drawing structure as devices.
- E2E evidence must target the professional control drawing rather than the Cabinet strip.

### Technical Impact

- `:kernel:language`: add minimum function grouping, richer native drawing primitives, and explicit
  projection occurrence placement.
- `:kernel:engineering-model` / compiler: preserve one physical component identity while exposing
  typed functional units and their existing device-owned ports.
- representation binding: resolve function occurrences to Symbols/Elements without duplicating a
  device or guessing from its name.
- layout/composition/routing: consume explicit grid placement and terminal anchors.
- Theia/Electron: expose one focused drawing surface and prove it with fresh screenshots.

## 3. Recommended Approach

Use a direct corrective Epic inside M34. Do not roll back the valid representation compiler work,
and do not move failed product acceptance to M35.

Effort: high. Risk: high, but bounded by one drawing, one package, and one product surface.

Alternatives rejected:

- renderer-only restyling cannot create missing functional occurrences or deterministic placement;
- copying QET would introduce foreign page/XML authority;
- a full Engineering Component System would exceed M34 and is not required for this proof;
- screenshot backgrounds or mocked SVG sheets would bypass Athena semantics and are forbidden.

## 4. Detailed Changes

### PRD

- Add FR-44 through FR-50 for the professional drawing target, Engineering Functions, explicit grid
  placement, native primitive coverage, repeated occurrences, semantic-authority cleanup, and E2E.
- Supersede Cabinet-only completion language for final M34 acceptance.

### Architecture

- Add a generic `EngineeringFunction` owned by one `EngineeringComponent` and referencing existing
  component-owned ports.
- Keep terminal identity on semantic ports (`A1`, `A2`, `13`, `14`, `21`, `22`, etc.).
- Keep sheet coordinates, occurrence orientation, frame, zones, title block, and routes outside the
  engineering semantic kernel.
- Add function-aware representation selection and occurrence identity while retaining canonical
  physical device identity for interaction and cross-reference.
- Extend native graphics only with the concise primitives already represented in Graphic Primitive
  IR. Complex shape-heavy assets remain package-local governed SVG.

### Stories

1. Story 5.1 freezes the target contract and focused product surface.
2. Story 5.2 adds typed Engineering Functions and explicit occurrence placement.
3. Story 5.3 builds the IEC package and semantic rolling-shutter sample.
4. Story 5.4 composes and routes the complete professional drawing.
5. Story 5.5 runs real Electron visual proof and performs mandatory polish/purge.

## 5. Implementation Handoff

Classification: Moderate backlog correction with high implementation effort.

The M34 implementation agent owns all five stories sequentially. Every story follows RED/GREEN,
records AC-to-evidence, runs Gradle verification sequentially, and ends with a deep workspace
polish/purge. M34 is complete only after actual Electron output satisfies the approved target
contract without mocks, foreign runtime dependencies, or hidden fallback rendering.

## 6. Checklist Record

- [x] Trigger and evidence identified: Story 4.3 and fresh screenshot mismatch.
- [x] Epic impact assessed: retain Epics 1-3, correct Epic 4 through Epic 5.
- [x] PRD, architecture, UX, sample, compiler, renderer, and E2E impacts identified.
- [x] Direct adjustment selected; rollback and milestone deferral rejected.
- [x] Scope and agent handoff defined.
- [x] User approval recorded on 2026-07-26.

