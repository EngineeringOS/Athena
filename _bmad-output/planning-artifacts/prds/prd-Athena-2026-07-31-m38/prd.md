---
title: Athena M38 - Engineering Drawing Trust Foundation
status: final
date: 2026-07-31
milestone: M38
product: Athena
---

# Athena M38 - Engineering Drawing Trust Foundation

## Vision

Athena is an open-source EngineeringOS for a world where humans and AI express, compile, inspect, and
change engineering truth together.

M38 makes one promise credible:

> Every visible engineering connection attaches to the exact Element Anchor selected by Athena
> source. No guessing. No renderer repair. Complete source trace.

## Product Philosophy

- Human first.
- Natural, concrete, AI-friendly language.
- K.I.S.S.
- Athena source is engineering SSOT.
- SVG supplies geometry, never engineering truth.
- Compiler derives presentation.
- Theia renders and inspects.
- Open contracts, inspectable facts, no vendor lock.
- Pre-public code is replaced directly when wrong; no compatibility debt.

Engineers write:

```text
Element
Port
Connection
engineering intent
```

They do not write:

```text
route bend coordinates
transform matrices
stroke mechanics
label offsets
renderer commands
internal compiler IR
```

## Problem

M37 can carry Anchor and route IDs while visible lines still start or end at unrelated computed
points. Multiple representation, placement, route, presentation, and frontend paths can each claim
authority. The result may compile but cannot be trusted visually.

More syntax will not fix this. M38 fixes the compiler chain between engineering meaning and visible
attachment.

## Scope

M38 owns:

1. one `RepresentationDefinition` geometry/Anchor authority for SVG, native, and composite geometry;
2. one compiler-produced `GraphicOccurrence` using the same transform for body and Anchors;
3. one strict `PresentationConnector` with exact endpoint equality;
4. explicit line, junction, crossing, label, and trace facts for Theia;
5. a dedicated M38 example and structural/Electron proof;
6. hard deletion of stale and fallback authorities.

M38 does not improve global placement or route quality. M39 and M40 own those outcomes.

## Functional Requirements

### Human-First Authoring

**FR-1:** M38 shall add no normal-source syntax for placement coordinates, transforms, route bends,
line paint, label placement, renderer behavior, or internal compiler facts.

### Intrinsic Element

**FR-2:** Every selected SVG-backed, native, or composite Element shall compile to one intrinsic
geometry contract before placement.

**FR-3:** Intrinsic geometry shall contain local body, bounds, geometric Anchors, hit geometry, label
slots, and source/resource trace.

**FR-4:** Engineering Port meaning shall remain in Athena source. Anchor shall own geometry only.

**FR-5:** Port-to-Anchor binding shall be explicit and fail on missing, duplicate, incompatible, or
unknown references.

**FR-6:** Package-local SVG shall allow neutral `data-athena-ref` geometry IDs and reject SVG metadata
that attempts to define Port, signal, direction, role, component, or compatibility facts.

### Placed Element

**FR-7:** Compiler shall create one Graphic Occurrence from one selected Representation Definition
and one current placement.

**FR-8:** Body, bounds, Anchors, hit geometry, and label slots shall use the same placement transform
exactly once.

**FR-9:** Downstream code shall consume placed coordinates and shall not recalculate endpoints from
body centers, graph nodes, global offsets, SVG DOM, or route points.

**FR-10:** Missing, duplicate, stale, or conflicting placement/Anchor evidence shall block drawing
publication with plain source-spanned diagnostics.

### Attached Connection

**FR-11:** Every visible engineering Connection shall resolve source and target Ports, bindings,
Graphic Occurrences, Anchors, and exact drawing points.

**FR-12:** Visible route first and last points shall equal the resolved placed Anchor points exactly.

**FR-13:** M38 shall normalize current route geometry only enough to replace endpoints, remove
zero-length points, and merge redundant orthogonal segments. Invalid remaining geometry shall fail;
M38 shall not search for a better route.

**FR-14:** Junction, no-connect crossing, bus tap, sheet continuation, line appearance, and label
facts shall be explicit before Theia paint. Shared markers shall occur once at document level and be
referenced by connectors. Visual intersection shall never create topology.

### Theia And Trace

**FR-15:** Theia shall paint supplied occurrence, route, marker, and text facts without endpoint,
topology, style, or semantic inference. `PresentationDocument.connectors` shall be the only visible
connection path; route candidates stay compiler-internal. SVG export shall serialize the same
Presentation Document, not lower connections independently. Text shaping and paint remain renderer
work.

**FR-16:** Endpoint inspection shall trace Connection -> Port -> binding -> Element -> Anchor ->
placed point -> route point -> Athena source span.

The complete Presentation Document shall publish atomically. Failed latest compilation invalidates
the current drawing; partial occurrence/connector merge and stale connector retention are forbidden.

### Proof And Cleanup

**FR-17:** M38 shall provide a dedicated `examples/m38/professional-control-drawing` project with one
complex SVG-backed Element, one native Element, explicit bindings, power/control/PE routes, labels,
and junction/crossing proof.

**FR-18:** Structural proof shall assert zero detached endpoints, zero fallback endpoints, zero
renderer repairs, exact endpoint equality, explicit crossing meaning, and complete trace.

**FR-19:** Electron proof shall capture desktop 1920x1080, desktop 1280x900, and narrow screenshots
under `_bmad-output/implementation-artifacts/m38/screenshots` after fresh kernel/LSP/frontend builds.

**FR-20:** M38 shall delete stale representation anatomy, compatibility shells, accepted RouteFact
authority, duplicate connector lowering, endpoint/body-center fallback, renderer repair, hardcoded
sample policy, milestone-named production classes, and stale tests/docs. No shim or deprecated path.

## Non-Functional Requirements

**NFR-1 Human-first:** Normal Athena source stays natural, concrete, AI-friendly, and K.I.S.S.

**NFR-2 Type safety:** Missing or ambiguous Port, binding, Element, Anchor, placement, route, or line
facts fail before presentation.

**NFR-3 Determinism:** Same source, package snapshot, placement, and route candidate produce the same
placed Anchor and route endpoint coordinates.

**NFR-4 Renderer purity:** Theia and SVG export paint explicit facts and do not recover engineering
truth.

**NFR-5 Visual trust:** Accepted drawing has no loose, center-fallback, repaired, or untraceable
engineering endpoints.

**NFR-6 Explainability:** Human and AI can trace every visible endpoint to source meaning.

**NFR-7 Cleanup:** Pre-public legacy paths are deleted rather than preserved.

**NFR-8 Performance:** Dedicated example refresh completes within 10 seconds after IDE readiness on
the supported development workstation.

## Success Metrics

**SM-1:** SVG-backed and native Elements compile through the same intrinsic contract.

**SM-2:** Every required Port has one valid Anchor binding in the selected Element.

**SM-3:** Body and Anchors share one placement transform.

**SM-4:** Every visible route first/last point equals its placed Anchor point.

**SM-5:** Theia paints zero guessed or repaired endpoints and zero inferred topology.

**SM-6:** IDE inspection reaches Athena source for every visible endpoint.

**SM-7:** Dedicated M38 structural tests and screenshots pass after fresh builds.

**SM-8:** Repository audit finds no stale authority, fallback, compatibility, milestone production
name, or obsolete M38-adjacent documentation.

## Counter-Metrics

**CM-1:** Zero new normal-source keywords for layout, routing, paint, labels, or renderer mechanics.

**CM-2:** Zero second renderer, font engine, universal schema layer, or speculative abstraction added.

**CM-3:** Zero compatibility adapter or deprecated parallel contract retained.

**CM-4:** M38 makes no claim that placement or routing is professional quality; M39/M40 own that work.

## Handoff

### M39 - Projection Composition Engine

- internal composition graph;
- ordering, grouping, flow, and alignment;
- placement quality;
- one current Graphic Occurrence producer;
- no new engineer-facing layout language by default.

### M40 - Professional Layout And Routing Engine

- route lanes and channels;
- obstacle avoidance;
- bend/crossing minimization;
- bundles and separation;
- label optimization;
- optional planner adapters behind Athena validation.

Both milestones consume M38 Graphic Occurrences and exact attachments. Neither may weaken Athena source
SSOT or move engineering truth into renderer/planner state.
