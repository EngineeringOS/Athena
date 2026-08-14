# M004 Reference Certification Baseline

## Status

`EVIDENCED`, not implementation-authorized. This specification starts a new
round after the M001-M003 prototype was found not to establish QElectroTech
feature parity or a Graphite editor-shell architecture.

## Why

Athena must become an electrical-schematic product with QElectroTech-equivalent
user-observable electrical workflows, built on a Graphite-style editor
architecture and shell. The previous prototype cannot certify either claim
because it was implemented before a complete reference-to-Athena mapping.

## Capabilities

### CAP-M004-001: Evidence Registry

Every future requirement has a stable QET or GRA ID, a primary-source path and
line, an observable outcome, an Athena owner, and a certification status.

Success: no code/task/claim can enter a milestone without at least one source
ID and a listed verification method.

### CAP-M004-002: QElectroTech Electrical Behavior Inventory

Record QElectroTech's project, folio, title-block, element, conductor,
numbering, cross-reference, terminal, report, library, editing, persistence,
and export workflows before implementing their Athena equivalent.

Success: every functional family in the QElectroTech user documentation has a
QET ID and status in `qet-electrical-inventory.md`.

### CAP-M004-003: Graphite Editor Architecture Inventory

Record the Graphite application, dispatcher, typed message, tool, document,
viewport, overlay, layout/panel, frontend-message, persistence/history, and
WASM boundary responsibilities.

Success: every proposed Athena application/shell module is mapped to a GRA ID
or is explicitly rejected as a non-goal.

### CAP-M004-004: Prototype Gap Audit

Classify every existing Athena crate and shell entry point as `RETAIN`,
`EXTRACT`, `REWRITE`, `DELETE`, or `UNDECIDED` against the two inventories.

Success: the next architecture milestone can identify which current code is
allowed to survive, and why, without treating prototype code as an authority.

### CAP-M004-005: Certification Gates

Require reference, mapping, architecture, behavior, persistence,
cross-platform, visual, adversarial-review, and user-acceptance gates before a
future slice can be called certified.

Success: `complete`, `parity`, `mirrored`, and `professional` are prohibited
unless the required gate evidence exists in the milestone verification report.

## Constraints

- QElectroTech source and documentation define electrical-domain behavior,
  terminology, workflow, and observable outcome. They do not define Athena's
  C++/Qt architecture, XML format, or legacy visual shell.
- Graphite defines the architecture and shell reference: application dispatch,
  typed messages, tool state machines, document ownership, viewport/overlay
  separation, workspace panels, frontend protocol, and modern visual UX.
- Graphite owns **where and how** panels are laid out: dock layout, panel
  lifecycle, viewport framing, tool shelf, shell density, and interaction.
  QElectroTech owns **what electrical content** panels show: element,
  conductor, folio, title-block, cross-reference, terminal, numbering, and
  report property plates. Athena must combine these without copying QET's Qt
  chrome or inventing generic panel fields.
- Zed and `gpui-component` guide native GPUI application and component work.
- Browser and desktop share Rust domain/application behavior. Browser HTML/JS
  may adapt platform events and render frontend messages but may not own a
  second schematic state machine.
- No implementation changes are authorized by M004. It is a research,
  traceability, and acceptance-contract milestone.
- Unknown requirements remain `UNKNOWN`; no inferred implementation is allowed.

## Non-goals

- Declaring current M001-M003 behavior as QElectroTech parity.
- Declaring current M003 layout as Graphite-equivalent or professional.
- Copying reference source files, dependencies, C++/Qt types, or generic image
  editing features.
- Designing new electrical behavior where QElectroTech evidence has not yet
  been examined.

## Certification Vocabulary

| State | Meaning |
| --- | --- |
| `UNRESEARCHED` | No primary-source evidence collected. |
| `EVIDENCED` | Source/document behavior is cited. |
| `MAPPED` | Athena target owner and boundary are approved. |
| `SPECIFIED` | Inputs, outputs, states, failures, and acceptance checks are written. |
| `IMPLEMENTED` | Code exists; no product claim follows. |
| `BEHAVIOR-VERIFIED` | Reference workflow tests pass. |
| `PERSISTENCE-VERIFIED` | Save/reopen preserves the relevant semantics. |
| `CROSS-PLATFORM-VERIFIED` | Desktop and web complete the same workflow. |
| `VISUALLY-VERIFIED` | Screenshot comparison against Graphite shell reference is approved. |
| `USER-ACCEPTED` | User reviewed the demonstrated workflow. |
| `CERTIFIED` | All required gates are passed. |
| `FAILED` | Direct evidence shows a requirement is not met. |

## Claim Rules

- `implemented` requires a code path and a targeted test.
- `mirrored` requires a reference-to-Athena mapping plus behavior proof.
- `parity` requires all in-scope QET IDs to be `CERTIFIED`; it may never mean
  "a few similar editor commands exist".
- `professional` requires `VISUALLY-VERIFIED` comparison and
  `USER-ACCEPTED`, not CSS/DOM or process-liveness tests alone.
- Each verification report separates `CONFIRMED`, `DEDUCED`, `UNKNOWN`,
  `FAILED`, and `SKIPPED` findings.

## Required Gates

1. `Reference Gate`: cited QET/GRA primary sources establish the requirement.
2. `Mapping Gate`: source behavior maps to a named Athena owner and contract.
3. `Architecture Gate`: state ownership matches the Graphite-derived boundary.
4. `Behavior Gate`: a user workflow runs through real UI and core commands.
5. `Persistence Gate`: relevant state survives save/reopen.
6. `Cross-platform Gate`: desktop and browser complete the same workflow.
7. `Visual Gate`: Graphite/Athena screenshots are measured and reviewed.
8. `Adversarial Gate`: test-only, stub, empty-command, duplicated-state, and
   unsupported-claim checks find no unresolved violation.
9. `User Gate`: user accepts the evidence and outcome.

## Success Signal

M004 is complete only when its three companion inventories are reviewed and
approved by the user, all existing prototype modules have a gap classification,
and a future implementation plan can cite specific QET and GRA IDs for every
task. M004 itself authorizes no product-code changes.

## Companions

- [QElectroTech Electrical Inventory](2026-08-14-m004-qet-electrical-inventory.md)
- [QElectroTech Behavior Evidence](../research/2026-08-14-m004-qelectrotech-behavior-inventory.md)
- [Graphite Architecture Inventory](2026-08-14-m004-graphite-architecture-inventory.md)
- [Athena Prototype Gap Audit](2026-08-14-m004-athena-prototype-gap-audit.md)
