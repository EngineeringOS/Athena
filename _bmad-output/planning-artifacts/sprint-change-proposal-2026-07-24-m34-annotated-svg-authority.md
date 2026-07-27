---
title: M34 Governed SVG Graphic Body Correction
status: approved
created: '2026-07-24'
approved_by: Aaron
scope: moderate
---

# Sprint Change Proposal - Governed SVG Graphic Body

## 1. Issue Summary

During Story 1.3 planning, the representation-authoring boundary was re-evaluated against real complex
industrial visuals. Requiring every complex visual contract to be split between Athena DSL metadata
and a plain SVG geometry sidecar makes one logical asset harder for humans and AI to author, inspect,
and maintain.

The approved correction is:

- simple Symbols/Elements use native typed Athena geometry;
- complex Symbols/Elements use one Athena definition with `graphic svg "..."` referencing a governed
  SVG resource with a closed `data-athena-*` profile on selected SVG nodes;
- source form follows maintainability: concise textual geometry uses native Athena, while shape-heavy
  or design-tool-authored geometry uses annotated SVG without requiring a DSL rewrite;
- each definition has exactly one Athena metadata authority and one graphic body form;
- every source is parsed, type checked, linted, formatted/canonicalized, and compiled to the same
  `RepresentationDefinition` and `GraphicPrimitiveDocument`;
- project devices, actual ports/connections, Profile, Binding, package policy, and runtime authority
  remain forbidden in SVG;
- raw SVG and annotations never cross the compiler boundary into package runtime, transport, or renderer.

## 2. Impact Analysis

### Epic Impact

- Epic 1 remains valid. Stories 1.1/1.2 and native Element Story 1.3 are unchanged in purpose.
- Epic 2 changes from geometry-only sidecar compilation to a governed SVG graphic-body frontend for
  complex assets referenced by Athena declarations.
- Epic 3 package migration must admit compiled outputs from both source frontends without source-format
  precedence or field merging.
- Epic 4 remains Cabinet-focused and consumes only canonical compiled output.

### Artifact Conflicts

- PRD FR-5..FR-8, FR-11, FR-21, FR-24, FR-38..FR-40 and corresponding goals/non-goals currently
  forbid all SVG metadata and require an Athena sidecar.
- Addendum and architecture AD-3/AD-5/AD-8/AD-10/AD-12 repeat that obsolete rule.
- Stories 2.1, 2.3, and 2.4 need acceptance changes for `data-athena-*`, source spans, IDE support, and
  importer/AI output.
- No UI/UX artifact changes are required; Cabinet remains the only product surface.

### Technical Impact

- The safe SVG frontend becomes both a hardened geometry parser and a typed representation-source
  adapter for an exhaustive `data-athena-*` schema.
- Metadata is not read at runtime. The compiler strips it after lowering.
- Definition-level SSOT is enforced structurally: Athena owns identity/version/kind; SVG owns only
  geometry and node-local contracts, with no duplicate-field merge or override.

## 3. Recommended Approach

Use direct adjustment within the existing M34 epic sequence.

- **Effort:** Medium.
- **Risk:** Medium, concentrated in Story 2.1 parser/schema diagnostics.
- **Rollback:** Not recommended; completed native compiler work remains correct.
- **MVP impact:** No scope expansion in product surfaces. One plain sidecar mode is replaced by a
  typed Athena definition plus one governed SVG body, reducing long-term ambiguity.

## 4. Detailed Changes

1. Replace "SVG is geometry only" with "governed annotated SVG is an optional complex graphic body; unannotated SVG nodes have no Athena meaning."
2. Define a closed lowercase `data-athena-*` schema. The root requires only `data-athena-schema="representation/v1"`. Selected nodes may own only representation anchors, label slots, hotspots, and explicit points/compatibility predicates.
3. Keep engineering/project truth, definition identity/version/kind, lifecycle, Profile, Binding, and policy out of SVG.
4. Require explicit metadata values; no engineering meaning may be guessed from geometry, element type, CSS, DOM order, or unmarked ids.
5. Compile native Athena graphic bodies and referenced annotated SVG bodies into the same canonical contracts and reject duplicate Athena definition identity.
6. Extend Story 2.3 IDE support to annotated SVG diagnostics, completion, highlighting, navigation, and canonical formatting where practical.
7. Require importers and AI to emit valid Athena representation source and, when needed, one valid referenced governed annotated SVG resource, never a foreign runtime schema.
8. Drive SVG compiler and IDE validation from one compiler-owned schema; no XSD, sidecar schema, or SVG-specific runtime IR becomes authority.
9. Treat native Athena graphic bodies and referenced annotated SVG graphic bodies as equal compiled body forms. Annotate only the SVG nodes that carry representation contracts; leave ordinary geometry free of redundant Athena metadata.

## 5. Handoff

- Product/architecture artifacts: update immediately before Story 1.3 development.
- Developer: keep Story 1.3 native-only, then implement `graphic svg` plus the governed SVG compiler
  frontend in Story 2.1.
- Review: enforce one-source-per-definition SSOT, no source merging, safe parser limits, exact source
  diagnostics, and zero raw markup transport/runtime authority.

## Checklist Record

- [x] Trigger, problem, evidence, and affected story identified.
- [x] All M34 epics and dependencies assessed; no resequencing required.
- [x] PRD, architecture, epics, tests, and documentation conflicts identified.
- [x] Direct adjustment selected; rollback and MVP reduction rejected.
- [x] Scope, risk, action plan, handoff, and success criteria documented.
- [x] User approval recorded from the direct `data-athena-*` source-authority instruction.
