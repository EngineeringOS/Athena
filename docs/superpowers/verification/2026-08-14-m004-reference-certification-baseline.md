# M004 Reference Certification Baseline Verification

## Verdict

`USER-ACCEPTED` on 2026-08-14, not `CERTIFIED`.

M004 is a research/governance milestone. This report verifies the evidence
package and M005 proposal; it does not claim product implementation, QET parity,
Graphite mirroring, or professional visual quality.

## CONFIRMED

- The QET registry defines 31 stable IDs, including the M005 dependencies
  QET-PROJ-001, QET-FOLIO-001/002, QET-TITLE-001/002, and QET-VAR-001.
- The Graphite registry defines 17 stable IDs, including the platform-adapter
  distinction GRA-PLATFORM-001.
- The QET and Graphite line-level research reports separate confirmed behavior,
  non-transferable/limit findings, and unknown areas.
- The Athena gap audit classifies 18 current crate/platform/generated-artifact
  ownership groups as `RETAIN`, `EXTRACT`, `REWRITE`, `DELETE`, or
  `UNDECIDED`, each with exact QET/GRA dependencies.
- The M005 spec and plan cite exactly the 22 authorized QET/GRA IDs and contain
  no reference outside that set.
- M005 defines field ownership, typed messages, handler ownership, frontend
  effects, desktop/web workflow, persistence, visual evidence, adversarial
  checks, and all nine certification gates without changing product code.

### Citation Check

Command: a PowerShell citation audit over every M004/M005 spec, plan, and
research document. It extracted each
`reference/`, `rust/`, or `web/` path/line citation, verified the file exists,
and checked the line range against the local file. A separate pass checked
`AGENTS.md` ranges.

Result:

```text
DOCS=9
CITATIONS=391
CITATIONS_REFERENCE=348
CITATIONS_RUST=34
CITATIONS_WEB=9
CITATION_ERRORS=0
AGENTS_CITATIONS=8
AGENTS_CITATION_ERRORS=0
```

The Graphite research companion independently contributes 105 checked citation
occurrences; its local source checkout records commit
`461ddbc8726c587a8abc536cab301b0b2206a54c`.

### ID Check

Command: extract definitions from both concise registries, extract every QET/GRA
reference from the gap audit and M005 artifacts, then compare sets.

Result:

```text
QET_DEFS=31
GRA_DEFS=17
REFERENCED_IDS=30
UNDEFINED_IDS=0
M005_IDS=22
M005_OUTSIDE_ALLOWED=0
M005_ALLOWED_UNUSED=0
```

### Document Integrity Check

Command: resolve local Markdown links, count explicit audit classifications,
and scan M004/M005 artifacts for placeholder terms.

Result:

```text
MARKDOWN_LINKS=6
BROKEN_LINKS=0
AUDIT_CLASSIFIED_ROWS=18
PLACEHOLDER_MATCHES=0
TABLE_ERRORS=0
M005_REQUIREMENTS=11
M005_CAPABILITIES=5
M005_TASKS=10
VISUAL_ASSETS=3
MISSING_VISUAL_ASSETS=0
```

### Change-Scope Check

The intended M004/M005 change set is limited to `AGENTS.md` history already
committed at `1de6fbd` and the milestone documentation under
`docs/superpowers/`. No Rust, GPUI, WASM, HTML, CSS, or JavaScript product file
is authorized by this verification.

## DEDUCED

- The current direct `WebEditor` methods and desktop `DesktopEditor` facade do
  not satisfy GRA-APP-001/GRA-DISPATCH-001/GRA-MSG-001/GRA-FRONTEND-001 because
  platform adapters can directly invoke document operations instead of routing
  all intent/effects through one application spine.
- The current monolithic GPUI panel implementation does not satisfy
  GRA-LAYOUT-001/GRA-PANEL-001/GRA-PROPS-001 because layout, panel lifecycle,
  electrical form content, and platform rendering are not separate owners.
- A new `athena-application` crate is the smallest explicit boundary that can
  preserve useful platform-neutral domain/editor behavior while replacing both
  platform facades with one typed protocol. This is an M005 proposal, not an
  implementation fact.

## UNKNOWN

- The seven Graphite unknown areas recorded in the research companion remain
  unresolved: initialization ordering, async scheduling, layout migration,
  full widget semantics, native/web overlay equivalence, history storage
  details, and exact visual behavior.
- QET potential traversal, routing, terminal-strip behavior, full numbering
  grammar, title-block template grammar, collection edge cases, and complete
  export behavior remain outside the confirmed inventory.
- M005's exact final UI geometry, icons, density, focus/hover timing, mobile
  behavior, and user acceptance cannot be known before implementation and
  runtime evidence.
- Latest compatible stable dependency versions will be established by M005
  Task 1 at implementation time, as required by `AGENTS.md`.

## FAILED

The first independent review found 14 evidence, scope, terminology, and
traceability defects. After correction, the strict re-review confirmed all 14
were resolved and found two remaining defects: stale verification counts and an
unspecified save/history coupling. Focused follow-up then exposed an asynchronous
save-revision race and an incomplete active-save terminal lifecycle. The final
contract binds immutable bytes and results to request/project/revision identity,
preserves post-request edits, releases the slot only for matching terminal
results, and requires retry/stale-result tests. All findings were corrected
before the final fresh verification pass. No current documentation consistency
check remains failed.

The user accepted the M004 evidence package and authorized M005 implementation
on 2026-08-14. M004 remains not `CERTIFIED`; acceptance does not prove any M005
runtime, visual, cross-platform, or behavior gate.

## SKIPPED

- Rust compilation, Cargo tests, WASM builds, Playwright workflows, and product
  runtime checks were intentionally skipped because M004 authorizes no product
  code changes.
- Desktop/browser screenshots and professional visual comparison were skipped;
  source evidence certifies shell responsibilities only.
- M005 implementation, code review, cross-platform persistence fixtures, and
  behavior demonstrations remain blocked on user approval of the M005 spec.

## User Review Checklist

- [x] Accept M004 as the reference-certification baseline.
- [x] Accept the rule `Graphite panel system + QET electrical plate content`.
- [x] Approve the M005 bounded vertical slice and implementation plan.
- [x] Authorize product-code work for M005.
