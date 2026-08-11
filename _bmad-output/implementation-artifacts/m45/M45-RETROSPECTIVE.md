# M45 Retrospective

Status: complete
Date: 2026-08-10

## What Worked

- Lock V3 became the real package authority. Package digests, item digests, resource digests, and
  semantic edges now form one deterministic compiler-owned contract.
- Common PackageItem metadata reduced duplication across Symbol, Element, Part, Macro, Variant, and
  Placeholder.
- Explicit `FunctionRepresentationBinding` stopped `Function -> Symbol` shortcuts. Source-owned
  `FunctionPartBinding` kept procurement facts projection-independent.
- Transaction discipline held: accepted Move, Change Symbol, Bind Part, and Reconnect operations were
  journaled source transactions; stale and invalid operations failed closed.
- Product proof improved after hard failures: workspace root, Repository Session, LSP root, READY state,
  exact occurrence/route counts, screenshots, and exports are now checked directly.
- Visual proof now uses package assets and a clean page surface: edge rulers, blank white interior,
  one-pixel frame, compact labels, thin routes, and no title table.
- Internal `anchor:sheet=...` port/source identities once leaked into paint, and selection switched
  style authority automatically. Paint now keeps these values as invisible interaction metadata;
  selection never changes style target, and the golden sheet defaults to regular black IEC linework.

## What Failed First

- Early canvas work drifted into trial-and-error rendering. The fix was to pin the visual Golden Rule
  and prove it through product screenshots.
- The first M45 proof accepted too little. Later review found stale evidence, hardcoded PASS logic,
  and missing positive Macro/Variant/Placeholder proof.
- Companion files polluted Project Semantic Graph at one point. Sheet, style, and binding companions
  had to be excluded from engineering source indexing.
- Function-owned Ports were not all indexed by canonical authored path, causing semantic inspection
  failures for nested ports.
- Route planner originally chose first valid orthogonal detours, producing large ugly rectangles.
  Deterministic shortest legal route selection corrected the active page.

## Corrections Made

- Added native positive Macro, Variant, Placeholder, composite Element, and dual-projection proof.
- Removed stale M44 fixture assumptions from M45 LSP tests instead of preserving compatibility.
- Regenerated product/export evidence from current outputs and made proof scripts reject stale files.
- Rebuilt Theia product and LSP before Electron screenshots and export proof.
- Added forbidden-path and production-name scans for external formats, retired package contracts,
  deprecations, compatibility paths, and milestone/demo production names.
- Re-ran full LSP with `--rerun-tasks` after detecting Gradle reused filtered test results.

## Carry Forward

- Keep this boundary as law:

```text
Athena source owns engineering meaning.
Package metadata owns reusable library facts.
SVG owns geometry.
Canonical Scene owns derived render data.
Theia/Konva owns disposable paint and user intent only.
```

- Do not close future milestones on story status alone. Closure requires regenerated artifact evidence.
- Never accept product proof unless it opens the intended workspace root and verifies Repository Session
  plus LSP root before canvas checks.
- Macro is not Pattern. Variant is not semantic change. Placeholder is not decision logic. Part is not
  solution intelligence.
- Keep external reference trees as authoring references only. No `.elmt`, HTML, XML, or reference-tree
  parser can enter runtime authority.
- Route and visual quality need a dedicated next milestone. Do not bury drafting polish inside package
  authority work.

## M45 Usage

M45 is the package-authoring base layer for later milestones.

- M46 can rely on M45 for package resolution, bindings, provenance, and lock-backed scene truth.
- M47 can build richer planning, routing, and interaction on top of the same canonical scene without changing source authority.
- New asset packages should follow the M45 package contract: source owns meaning, package metadata owns reusable facts, SVG owns geometry.
- Any new visual or editing work should reuse the M45 proof chain first, then add only the next missing capability.

## M46/M47 Handoff

- Next work should focus on professional connection planning, visual refinement, and richer editor
  interaction using the M45 package-backed scene as the substrate.
- Remote registry, package signing, trust policy, marketplace, and update governance remain platform
  work after local package semantics stabilize further.
- AI/pattern generation should consume M45 package facts but must stay behind explicit Engineering
  Pattern authority, not Macro authority.

## Residual Risk

- The rendered page is good enough for M45 proof, not final EPLAN-grade drafting.
- Current package registry is local and deterministic; no remote dependency lifecycle exists.
- Performance evidence covers the active M45 profile, not CAD-scale universal workloads.
