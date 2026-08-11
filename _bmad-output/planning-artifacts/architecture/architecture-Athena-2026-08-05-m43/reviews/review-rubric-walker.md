# M43 Architecture Good-Spine Rubric Review

## Scope

Target:

- `_bmad-output/planning-artifacts/architecture/architecture-Athena-2026-08-05-m43/ARCHITECTURE-SPINE.md`

Normative sources checked:

- `AGENTS.md`
- `_bmad-output/planning-artifacts/prds/prd-Athena-2026-08-05-m43/prd.md`
- `_bmad-output/planning-artifacts/m43/epics.md`
- `_bmad-output/planning-artifacts/architecture/architecture-Athena-2026-08-03-m41/ARCHITECTURE-SPINE.md`
- `_bmad-output/planning-artifacts/architecture/architecture-Athena-2026-08-04-m42/ARCHITECTURE-SPINE.md`

Supporting intent checked:

- `.memlog.md`
- `UPSTREAM-ADOPTION.md`
- current `AthenaPresentationWidget` brownfield implementation

Mechanical gate:

- `lint_spine.py`: **PASS**, 0 findings.

## Verdict

**FAIL - corrections required before finalization or story execution.** The paradigm and major
authority boundaries are coherent, but the spine currently conflicts with final M43 requirements on
two executable contracts: live renderer composition and Micro Anchor bounds. It also removes the
required title block, leaves missing-companion behavior undecided, and does not carry forward the
M41 Spatial invariants that the PRD says remain valid. Independent story implementers cannot satisfy
the spine and current acceptance artifacts simultaneously.

## Critical Findings

### 1. AD-9 and the final delivery artifacts require mutually exclusive live renderer designs

**Disposition: Discuss, then correct the authoritative upstream artifacts or AD-9 before stories.**

Spine AD-9 requires one imperative `KonvaDiagramAdapter`, one Konva Stage, and one hit-testing owner,
and explicitly retires independent DOM SVG occurrence paint plus Canvas route paint
(`ARCHITECTURE-SPINE.md:159-171`). The current product contract instead requires a hybrid live
surface where semantic SVG and HTML Canvas consume the same Presentation Reality:

- PRD Vision: `prd.md:24-27`.
- PRD FR-7 and its acceptance consequences: `prd.md:165-179`.
- PRD NFR-4 and SM-5: `prd.md:269-272`, `prd.md:299-300`.
- Epic 3 / Stories 3-1 and 3-2: `epics.md:76-97`.
- Current brownfield implementation: `ide/theia-frontend/src/browser/athena-presentation-widget.tsx:108-153`.

This is not an adapter detail. Story 3-1 must either render two layers or delete them for Konva; its
acceptance tests cannot prove both. The memlog acknowledges the required reconciliation at
`.memlog.md:38`, but the PRD and epics remain final and unreconciled.

**Concrete fix:** choose one contract. If the approved Konva direction stands, update PRD Vision,
FR-7, FR-8 Canvas-hit wording, MVP scope, NFR-4, SM-5, and Epic 3 acceptance to require one canonical
scene plus one live adapter/hit authority, then state SVG is an oracle rather than a live layer. If
hybrid SVG/Canvas remains product authority, rewrite AD-9 around one shared revision, transform, and
hit coordinator without claiming one Konva Stage owns all paint and hits.

### 2. AD-2 makes `micro(5,1)` valid where the PRD requires it to fail

**Disposition: Discuss, then reconcile AD-2, PRD, epics, grammar tests, and diagnostics together.**

AD-2 says `cell: N` creates `N x N` subdivisions and permits `micro(x,y)` in `1..N`
(`ARCHITECTURE-SPINE.md:88-98`). The final PRD fixes a 4 x 4 Micro Anchor lattice independently of
`K`, requires `x` and `y` in `1..4`, and explicitly makes `micro(5,1)` a failure:

- Glossary: `prd.md:80-84`.
- FR-2: `prd.md:107-117`.
- FR-3: `prd.md:119-128`.
- SM-3 and Assumptions Index: `prd.md:292-293`, `prd.md:320-323`.
- Stories 1-2 and 2-2: `epics.md:44-51`, `epics.md:67-74`.

For `cell: 8`, one implementation following AD-2 accepts indices `1..8`; another following Story
1-2 rejects `5..8`. Both cannot pass the named M43 acceptance case.

**Concrete fix:** if fixed 4 x 4 anchors remain authoritative, rewrite AD-2 so `K` is macro-cell
logical span, `K` is divisible by 4, valid Micro Anchor indices remain `1..4`, and compiler maps them
at pitch `K/4`. If the approved decision is instead `N x N` anchors, update the PRD glossary, FR-2,
FR-3, SM-3, assumptions, both affected stories, and all validation examples before finalizing.

## High Findings

### 3. Required title intent and visible title block are omitted, then excluded by AD-11

**Disposition: Discuss; restore the capability or revise the final product contract.**

The PRD requires authored title-block intent and a visible title block in the professional page:

- UJ-1: `prd.md:55-59`.
- FR-2: `prd.md:107-110`.
- FR-7: `prd.md:170-179`.
- SM-1: `prd.md:286-295`.
- Stories 1-1, 3-1, and 4-1: `epics.md:35-42`, `epics.md:81-88`, `epics.md:103-110`.

AD-1's companion authority omits title intent (`ARCHITECTURE-SPINE.md:78-86`), the canonical
authoring shape has no title field (`ARCHITECTURE-SPINE.md:284-293`), and AD-11 says normal scene
paint contains no bottom title table (`ARCHITECTURE-SPINE.md:186-196`). M41 AD-21 also reserves a
title-block boundary (`architecture-Athena-2026-08-03-m41/ARCHITECTURE-SPINE.md:101-112`). A Theia
widget header is not the required document title block.

**Concrete fix:** add typed Sheet Companion title intent to AD-1, compiled title-block paint facts to
AD-5/AD-11, and a renderer-neutral scene element with source trace. Keep feature banners and
instruction text excluded. If title-block removal is intentional, update the PRD and all three story
acceptance sets explicitly.

### 4. M41 Spatial invariants silently disappear although the PRD says they remain valid

**Disposition: Autofix inherited coverage; discuss any genuine conflict instead of locally weakening it.**

PRD Section 4.2 says existing M41 engineering and Spatial semantics remain valid and M43 only adds
Sheet Companion mapping and authored-placement application (`prd.md:130-134`). The M43 source list
and Inherited Invariants include selected M39, M40, and M42 decisions but no M41 decision
(`ARCHITECTURE-SPINE.md:13-18`, `ARCHITECTURE-SPINE.md:58-75`). Missing load-bearing M41 rules
include:

- AD-20 typed per-sheet geometry ownership (`M41 ARCHITECTURE-SPINE.md:89-99`).
- AD-22 exact port-anchor routing and fail-closed route coverage (`M41 ARCHITECTURE-SPINE.md:114-124`).
- AD-26 complete Spatial validation as Presentation gate (`M41 ARCHITECTURE-SPINE.md:156-166`).
- AD-27 the single `ProjectionSpatialCompiler` orchestrator (`M41 ARCHITECTURE-SPINE.md:168-176`).
- AD-29 Presentation preserves Spatial geometry (`M41 ARCHITECTURE-SPINE.md:188-197`).
- AD-30 stable identity, trace, and ordering (`M41 ARCHITECTURE-SPINE.md:199-209`).

AD-4 and AD-7 do not recover those stage, coverage, and normalization constraints. Separate M43
stories could therefore add a second placement pipeline, drop an unroutable relationship, recompute
geometry in Presentation, or choose incompatible ID/order rules while still claiming compliance.

**Concrete fix:** add M41 as a source and list every still-applicable M41 AD under Inherited
Invariants. For placement policies changed by authored anchors, name the exact parent rule affected
and reconcile it upstream; preserve the single compiler, complete coverage, geometry-copy, and
normalization rules verbatim.

### 5. Missing Sheet Companion behavior remains an unresolved cross-story choice

**Disposition: Discuss; make this a blocking open question until one behavior is selected.**

PRD FR-1 requires either a deterministic default presentation profile or an explicit absence state
selected by project contract, and rejects ambiguous multiple companions (`prd.md:96-105`). The
Assumptions Index leaves default behavior conditional (`prd.md:318-323`), while Story 2-1 still
requires the explicit default-or-absence contract (`epics.md:58-65`). AD-1 defines colocated file
authority but says nothing about zero or multiple companions (`ARCHITECTURE-SPINE.md:78-86`), and
the spine has no Open Questions section.

**Concrete fix:** amend AD-1 to choose exactly one zero-companion result and define the owner of any
default profile, its version/digest participation, publication status, and diagnostic. State that
multiple matching companions fail before Projection/Spatial compilation. If choice cannot be made
now, record it as a blocking Open Question rather than allowing Story 1-1 and Story 2-1 to decide
independently.

### 6. FR-11 source-set hygiene is mapped but not actually bound or verified

**Disposition: Autofix AD-16/AD-17.**

FR-11 requires no new production Proof, Demo, Sample, milestone-named, compatibility, or hidden
layout-authority classes, plus the source-set and encoding audits (`prd.md:214-222`). Story 4-2
repeats the closure gate (`epics.md:112-119`). Repository policy treats this as a production-outage
class lesson and gives exact prohibitions and the audit command (`AGENTS.md:89-111`). AD-16 only bans
runtime fallbacks and old renderer contracts (`ARCHITECTURE-SPINE.md:246-255`); AD-17's executable
evidence list omits both required audits (`ARCHITECTURE-SPINE.md:257-267`).

**Concrete fix:** extend AD-16 with the production `src/main` naming and ownership exclusions and
direct-migration/deletion rule. Extend AD-17 with explicit sequential execution of
`tools/source-set-hygiene-audit.ps1` and `tools/encoding-audit.ps1`, plus an absence assertion for
retired renderer/layout paths. Keep proof helpers in tests, examples, scripts, or M43 artifacts.

## Medium Findings

### 7. AD-14 has measurements but no enforceable performance budget

**Disposition: Defer the 100,000-element gate or define accepted budgets.**

AD-14 mandates a synthetic 100,000-element fixture and records load, memory, visible-node count,
input latency, and frame-time percentiles, but supplies no named hardware baseline or numerical pass
threshold (`ARCHITECTURE-SPINE.md:221-233`). It then makes Pixi admission depend on missing the
undefined "accepted product budget." PRD NFR-5 binds only responsive behavior for the controlled
rolling-shutter proof and explicitly leaves batching thresholds to validation
(`prd.md:274-277`, `prd.md:311-316`).

**Concrete fix:** either define hardware, viewport, scene composition, warm-up, percentile, latency,
memory, and pass/fail budgets in AD-14, or move the 100,000-element fixture and prescribed tuning
mechanisms to Deferred. Keep M43's enforceable gate on the actual rolling-shutter desktop and narrow
viewports.

### 8. AD-10 adds `ConnectPorts` without a requirement or story

**Disposition: Defer, unless product scope is deliberately expanded upstream.**

AD-10 binds connection gestures and requires `ConnectPorts` to mutate engineering source
(`ARCHITECTURE-SPINE.md:173-184`). M43 journeys, FRs, MVP scope, and stories require move/lock,
selection/trace, and refresh, but no interactive relationship creation (`prd.md:53-67`,
`prd.md:233-243`, `epics.md:30-97`). This adds a cross-authority engineering edit protocol and its
validation/undo surface without acceptance ownership.

**Concrete fix:** limit M43 `DiagramEditCommand` to `MoveOccurrence` and defer `ConnectPorts` with a
revisit condition. If port connection is intended for M43, add a PRD FR, user journey, story,
diagnostic behavior, undo acceptance, and product E2E proof before retaining it in AD-10.

### 9. Scene canonicalization and digest inputs are not complete enough for independent codecs

**Disposition: Autofix AD-7/AD-8.**

AD-5 requires a scene digest, AD-7 promises canonical digest/order, and AD-8 requires ordered arrays
(`ARCHITECTURE-SPINE.md:119-129`, `ARCHITECTURE-SPINE.md:139-157`), but no rule fixes array
comparators, canonical JSON encoding, digest algorithm/input bytes, unsupported-version behavior, or
closed-record handling. PRD NFR-1 requires byte-stable published documents (`prd.md:252-257`). The
spine also claims inheritance of M42 AD-32/AD-33 (`ARCHITECTURE-SPINE.md:70-73`), whose full rules
separate schema shape from canonical bytes and require version rejection and SHA-256 over canonical
bytes (`M42 ARCHITECTURE-SPINE.md:261-296`).

**Concrete fix:** make the scene contract explicitly reuse the canonical codec rules or define a
scene-specific equivalent: `additionalProperties: false`, supported-version rejection before
decode, one declared order per array, exact numeric/string encoding, and lowercase SHA-256 over the
canonical UTF-8 bytes. Bind Kotlin, TypeScript, LSP, CLI/SVG oracle, and tests to the same fixtures.

## Checklist Result

- **Real divergence points fixed:** Fail. Renderer, grid/micro math, title block, missing companion,
  inherited Spatial pipeline, and scene codec still permit incompatible implementations.
- **Rules enforce stated prevention:** Partial. Most authority rules are strong; AD-14 lacks a pass
  budget and AD-8 lacks enough canonical detail.
- **Deferred is safe:** Partial. Deferred items are generally safe, but `ConnectPorts` and the
  100,000-element gate belong there unless requirements expand.
- **Named technology verified:** Pass. Repository-pinned Java/Kotlin/Theia/Electron/React/TypeScript
  versions are present; registry checks confirm `konva@10.3.0` and `ajv@8.20.0`.
- **Brownfield ratification:** Partial. AD-9 deliberately replaces the current hybrid widget, but
  final PRD/epics still ratify that widget shape and M41 Spatial invariants are omitted.
- **PRD capability coverage:** Fail for FR-1, FR-2/FR-3, FR-7/NFR-4, title portions of FR-2/FR-7,
  and FR-11.
- **Inherited invariants preserved:** Fail until applicable M41 ADs are carried explicitly.
- **Altitude dimensions covered:** Pass for local deployment/environment/operations through AD-16;
  no server, database, network authority, telemetry, credentials, or fallback path is introduced.

## Confirmed Strengths

- Engineering, Projection, Spatial, Presentation, renderer, and source mutation authority form a
  short, inspectable chain.
- `AthenaDiagramScene` is renderer-neutral and immutable; frontend inference and renderer-owned truth
  are prohibited.
- Asset sanitation, font-metric stability, local-only execution, deterministic SVG intent, failure
  semantics, and M43-local product evidence are directly addressed.
- Konva is isolated behind one adapter and upstream adoption has version/license/boundary records.
- Deferred items generally resist Pattern, AI, collaboration, export, layout-solver, and proprietary
  compatibility scope expansion.

## Gate Decision

Do not mark the spine `final` and do not execute M43 stories from the current artifact set. Resolve
Critical and High findings, reconcile PRD/epics where approved architecture intentionally changed
the product contract, rerun lint, then rerun the semantic reviewer gate.
