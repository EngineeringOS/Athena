# M35 Architecture Rubric Freeze Check

## Verdict

**CHANGES REQUIRED - FREEZE BLOCKED.** The three requested blockers are closed consistently across
the spine, PRD, and addendum. One newly exposed language/identity blocker remains.

Deterministic spine lint: **PASS**, zero findings.

## Prior Blocker Verification

| Prior blocker | Result | Evidence |
| --- | --- | --- |
| Dependency scope | **PASS** | The diagram is now explicitly an M35 load-bearing subgraph rather than a complete Gradle graph. Unshown existing edges remain code-owned. Required M35 edges and their ledger dispositions are present, including `repository-model -> package-runtime`, `physical-model -> drawing-composition`, and typed presentation dependencies. |
| Terminal ownership and ordering | **PASS** | PRD FR-20, spine AD-4, and the addendum all define terminals as ordinary `MountedOccurrence`s targeting `TerminalGroup`. No `TerminalOccurrence` type remains. TerminalGroup owns deterministic ordering by along-axis position, cross-axis position, then occurrence key. |
| Depth/orientation/target fit | **PASS** | PRD FR-23/FR-24, spine AD-6, and the addendum consistently require selected-orientation membership, occurrence depth within authored enclosure depth, enclosure fit, bounded surface/terminal-group fit, rail along-axis fit with zero normal offset, and exact typed mounting compatibility. |

## New Freeze Blocker

### N1 - Connection alias syntax, migration, and route-reference scope are inconsistent

**Severity: High**

M35 now requires stable authored connection aliases:

- PRD FR-28 requires every routed connection to have a source-unit-unique alias;
- spine AD-12 defines `EngineeringConnectionId` as `(SourceUnitId, connectionAlias)`;
- the addendum makes these forms normative:
  `connect <connection-alias> <from-port> -> <to-port>`,
  `connect <group-id> { <connection-alias> : <from-port> -> <to-port> ... }`, and
  `route <connection-alias> through [...]`.

But spine AD-15 says public syntax additions are limited to installation intent and resource forms,
binds only FR-1..FR-3 and FR-9, and does not name the connection-alias grammar. The Brownfield
Contract Ledger also has no row extending/replacing the frozen `ConnectionDeclaration` contract.

Current brownfield syntax confirms this is a real migration, not documentation of existing behavior:

```text
connectDecl      : CONNECT twoPartName ARROW twoPartName
connectGroupEdge : twoPartName ARROW twoPartName
ConnectionDeclaration(from, to, span)
```

There is also no lookup rule for `route <connection-alias>` when aliases are source-unit-local. A
route in another source unit cannot identify which `(SourceUnitId, alias)` it references.

Independent grammar, semantic-lowering, and physical-routing stories can therefore choose
incompatible ASTs, preserve or reject old forms differently, and resolve route aliases with
different scopes.

**Required fix:**

1. Extend AD-15 to bind FR-28 and list the exact aliased grouped/ungrouped forms as public M35 syntax.
2. Add a Brownfield Ledger row for extending/replacing `ConnectionDeclaration`, ANTLR4, Tree-sitter,
   formatter, LSP, examples, and semantic lowering together.
3. Decide whether old alias-free connect forms are removed or retained only for non-routed
   connections.
4. Define route lookup scope. The minimal M35 rule is that `route <alias>` resolves only a connection
   alias declared in the same `SourceUnitId`; cross-source routing references fail with a stable
   diagnostic. A qualified cross-source form can be designed later.

## Newly Introduced Blocker Sweep

| Dimension | Result |
| --- | --- |
| Semantic/physical/representation authority | **Pass** |
| Package/resource limits and SVG security | **Pass** |
| Lock modes, atomic materialization, and recovery | **Pass** |
| Physical topology, transforms, constraints, and routing geometry | **Pass** |
| Terminal composition and trace | **Pass** |
| Dependency and brownfield module direction | **Pass** |
| Language/AST/parser parity | **Fail - N1** |
| Offline operations and E2E acceptance | **Pass** |

## Gate Summary

```text
Critical: 0
High:     1
Medium:   0
Low:      0
Verdict:  CHANGES REQUIRED - FREEZE BLOCKED
```

Freeze after N1 is made singular across the spine, PRD, addendum, and Brownfield Contract Ledger,
then rerun deterministic lint and this focused rubric check.
