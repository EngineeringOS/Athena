# M35 Architecture Spine Brownfield Freeze Decision Recheck

**Target:** `ARCHITECTURE-SPINE.md` and the normative M35 source example  
**Review type:** focused brownfield freeze decision recheck  
**Date:** 2026-07-27  
**Verdict:** **PASS - no freeze blockers found.**

## Aliased Connection Replacement

PASS.

Current brownfield syntax is correctly identified as migration input, not claimed as finished M35
substrate:

- ANTLR currently accepts alias-free ungrouped edges and alias-free grouped edges
  (`kernel/language/src/main/antlr/com/engineeringood/athena/language/antlr/Athena.g4:93-103`).
- Tree-sitter mirrors those existing forms
  (`ide/tree-sitter-athena/grammar.js:538-559`).
- Current `ConnectionDeclaration` contains only `from`, `to`, and `span`; grouped edges reuse that
  alias-free node (`kernel/language/src/main/kotlin/com/engineeringood/athena/language/AthenaLanguageModel.kt:372-395`).
- Existing compiler proof derives connection identity from endpoints and treats the group name as
  non-semantic (`kernel/compiler/src/test/kotlin/com/engineeringood/athena/compiler/AthenaGroupedConnectLoweringTest.kt:82-119`).

AD-15 and the Brownfield Contract Ledger correctly require one replacement:

- every grouped and ungrouped connection gains an individual source-unit-unique alias;
- `EngineeringConnectionId` derives from `(SourceUnitId, connectionAlias)`;
- grammar, AST, semantic lowering, formatter, LSP, Tree-sitter, examples, and tests migrate in one
  owning story;
- alias-free forms and unreleased fixtures are deleted;
- no compatibility parser or endpoint-plus-ordinal identity remains
  (`ARCHITECTURE-SPINE.md:378-395`, `505`).

This matches Athena's unreleased-language migration policy and introduces no parallel grammar or
connection identity authority.

## System-scoped Example

PASS.

The normative example in the M35 PRD addendum is complete and scope-consistent
(`prd-Athena-2026-07-27-m35/addendum.md:302-385`):

- one `system M35PhysicalCabinet` owns devices, the grouped aliased connection, and the installation;
- `installation cabinet MainCabinet` is a system member;
- `route MainSupplyConnection through [CH1]` is an installation member;
- the route resolves the same-source-unit individual connection alias;
- the group name remains organizational and is not connection identity;
- enclosure, surface, rail, duct, channel, terminal group, mounts, physical contracts, and route are
  present in one accepted proof;
- channel coordinates are valid as duct-wall-inset-interior-local coordinates under AD-4/AD-5.

The example and AD-15 use the same forms:

```text
connect <alias> <from> -> <to>
connect <group> { <alias>: <from> -> <to> }
installation cabinet <id> { ... route <alias> through [...] ... }
```

## Prior Freeze Fixes

PASS.

- `RepositoryManifest` owns authored coordinate intent; `ResolvedPackageCoordinate` remains derived
  graph/snapshot/lock state (`ARCHITECTURE-SPINE.md:195-203`, `488`).
- The M31 flow remains exact:
  `SemanticActionIntent -> governed translation -> AuthoringIntent ->
  SemanticAuthoringTransaction -> AuthoringPreview + AuthoringSourceEditEvidence`
  (`ARCHITECTURE-SPINE.md:370-373`).
- Dependency Direction remains explicitly a load-bearing subgraph rather than a complete Gradle
  graph (`ARCHITECTURE-SPINE.md:441-445`).
- Legacy package scanners, lock v1, alias-free connection forms, old Cabinet producers, and parallel
  Cabinet IR paths all have mandatory replacement/deletion gates.

## New Blocker Scan

No new ownership, identity, parser-authority, scope, dependency, compatibility, or example-contract
blocker was introduced. Deterministic `lint_spine.py` reports zero findings.

## Freeze Decision

**PASS.** The reviewed M35 architecture and normative grammar migration example are ready to freeze.
