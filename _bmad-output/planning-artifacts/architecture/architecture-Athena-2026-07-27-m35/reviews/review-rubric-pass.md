# M35 Architecture Rubric Freeze Decision

## Verdict

**PASS.** The M35 architecture spine is ready to freeze. All prior blockers are closed consistently
across `ARCHITECTURE-SPINE.md`, `prd.md`, and `addendum.md`; the latest lane, adjacency, and source
changes introduce no new freeze blocker.

Deterministic spine lint: **PASS**, zero findings.

## Required Recheck

| Contract | Result | Evidence |
| --- | --- | --- |
| Aliased connect syntax | **Pass** | PRD FR-28, spine AD-12/AD-15, and the normative addendum require aliases for grouped and ungrouped connections. Canonical identity is `(SourceUnitId, connectionAlias)`; group names remain organizational. |
| Migration and deletion | **Pass** | AD-12/AD-15 require alias-free forms and fixtures to be migrated and deleted with no compatibility parser. The Brownfield Contract Ledger marks `ConnectionDeclaration`, grouped edge syntax, and endpoint/ordinal identity as `REPLACE`, covering grammar, AST, semantic lowering, formatter, LSP, examples, and tests. |
| Same-source route scope | **Pass** | PRD FR-28, AD-12/AD-15, and the addendum all require `route <alias>` to resolve only in its own `SourceUnitId`; cross-source lookup fails closed with a stable diagnostic. |
| AD-15 parser parity | **Pass** | AD-15 binds FR-28 and lists exact grouped/ungrouped alias forms, system-owned `installation cabinet`, installation-owned `route`, ANTLR4 authority, Tree-sitter parity, formatter, LSP, examples, and shared accepted/rejected corpus delivery. |
| Ledger ownership | **Pass** | The ledger distinguishes existing, extend, replace, and new M35 contracts without weakening retained M34 paths or creating parallel authority. |

## Latest Edit Sweep

| Area | Result | Judgment |
| --- | --- | --- |
| Lane allocation | **Pass** | One exact rational formula fixes usable span, lane centre, ordering, capacity, and no-rounding behavior before drawing transformation. |
| Channel adjacency | **Pass** | One same-duct predicate fixes disjoint interiors, shared boundary segment, margin trimming, exact midpoint, and rejection of cross-duct, corner-only, overlapping, gapped, or multiply intersecting transitions. |
| Routing determinism | **Pass** | Ordered authored channels, stable connection sorting, fixed bends/ties/stubs, body-intersection rejection, and no alternate-route search are singular. |
| Package/source authority | **Pass** | Manifest owns authored coordinate/root/dependency intent; resolved graph/snapshot and lock own derived coordinates; Athena source owns resources/exports; source-unit and resource identity are deterministic. |
| Physical source surface | **Pass** | `installation cabinet` and its members have one normative scope and accepted example; internal IR names remain non-language concepts. |
| Terminal and fit contracts | **Pass** | Ordinary mounted terminals, deterministic group ordering, orientation/depth/target-fit rules, and mounting compatibility remain aligned. |
| Security and operations | **Pass** | Fixed package limits, hardened SVG lowering, immutable snapshots, lock validation/update modes, atomic replacement, offline operation, and deterministic E2E evidence remain intact. |
| Dependency direction | **Pass** | The graph is correctly scoped as the M35 load-bearing subgraph; unchanged Gradle edges remain code-owned and all M35 authority edges have ledger/AD ownership. |

## Good-Spine Checklist

| Check | Result |
| --- | --- |
| Fixes real story-level divergence points | **Pass** |
| Every AD is enforceable and prevents its stated divergence | **Pass** |
| Deferred items are safe and have revisit conditions | **Pass** |
| Named technology and executable pins are current repository reality | **Pass** |
| Brownfield contracts are ratified or explicitly replaced | **Pass** |
| PRD/addendum capabilities are covered | **Pass** |
| Inherited M14/M34 invariants remain intact | **Pass** |
| Operational/environmental envelope is covered | **Pass** |

## Gate Summary

```text
Critical: 0
High:     0
Medium:   0
Low:      0
Verdict:  PASS - READY TO FREEZE
```
