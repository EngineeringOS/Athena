---
title: Athena M34 PRD Addendum
status: draft
created: '2026-07-24'
updated: '2026-07-24'
---

# M34 Addendum

## One Compiler, One Athena Authority Per Definition

M34 does not replace fragmented XML with another precedence/merge system. It supports one Athena
definition entry point with two graphic body forms that lower to one canonical model:

```text
simple visual                         complex visual
*.athena declaration                  *.athena declaration
  graphic { ... }                       graphic svg "./asset.svg"
ANTLR4 + type/lint/formatter           hardened SVG parser + node annotation lint
               \                      /
                RepresentationDefinition / GraphicPrimitive
```

Project engineering, Profile, Binding, Symbol, and Element declarations remain Athena language. A
reusable Symbol or Element chooses exactly one graphic body form. Duplicate `(library, identity,
version)` across Athena declarations fails; there is no duplicate-field merge, override, or
precedence.

Annotated SVG may own only complex geometry plus node-local anchors, labels, hotspots, explicit
points, and compatibility predicates through the closed `data-athena-*` profile. It cannot own
representation identity, version, kind, lifecycle, actual project ports, devices, connections,
classification, layout, Profile, Binding, package resolution, or runtime behavior.

## Abstraction Boundary

```text
Project device/component instance
        -> typed binding
        -> view-specific Element
        -> atomic Symbol
        -> typed visual primitive
```

M34 `Element` is visual composition only. It does not own vendor product parameters, engineering
functions, actual ports, validation rules, behavior, or a multi-view representation set. Those
belong to a future Engineering Component System above binding. This prevents one Element from
becoming both engineering truth and drawing truth.

## Language Surface Discipline

The public source language stays small: `symbol`, `element`, `profile`, and `binding`, with only the
nested constructs required for typed geometry, anchors, labels, composition, and selection. Names of
compiled implementation contracts such as `RepresentationDefinition`, descriptor, occurrence,
renderer, transport, DOM, and IR are not Athena keywords. ANTLR4, tree-sitter, formatter, completion,
and documentation tests enforce the same bounded vocabulary for human and AI authors.

## XML Decision

Athena does not use XML as a package manifest, general product language, transport model, or runtime
authority. SVG uses XML syntax, so M34 parses it only inside an isolated compile-time representation
frontend. No XML DOM, manifest, raw SVG, or `data-athena-*` attribute crosses into package runtime,
LSP presentation transport, Electron, or rendering.

The normal simple-authoring path is one typed Athena definition with a native `graphic` body. A
complex visual is the same Athena definition with `graphic svg "..."` pointing at one annotated SVG
resource. Both paths compile to the same canonical `RepresentationDefinition` and
`GraphicPrimitive` body and pass deterministic parse, type, lint, bounds, reference, and resource
checks before package admission.

## Symbol With Native Typed Geometry

Small and normal atomic visuals remain one physical, type-safe Athena source file:

```athena
symbol iec.switch_contact {
  identity "iec.switch_contact"
  version "1.0.0"

  graphic {
    bounds (0, 0, 80, 80)
    line line from (40, 0) to (40, 20) style conductor
    line load from (40, 60) to (40, 80) style conductor
  }

  anchor line {
    ref line
    role terminal
    direction in
    signal Power
  }

  anchor load {
    ref load
    role terminal
    direction out
    signal Power
  }
}
```

ANTLR4 and tree-sitter parse the entire declaration. The semantic type checker validates primitive
types, ids, coordinates, styles, bounds, and anchor references. AI-authored definitions use this
same path; there is no weaker generated-asset format.

## Complex Element With Referenced Annotated SVG

Large vendor/user visuals keep definition metadata in Athena and node-local visual contracts beside
the exact SVG nodes they govern:

```athena
package com.schneider.cabinet

element schneider_gv2_cabinet {
  identity "com.schneider.gv2.cabinet"
  version "1.0.0"

  graphic svg "./schneider-gv2-cabinet.svg"
}
```

```svg
<svg xmlns="http://www.w3.org/2000/svg"
     viewBox="0 0 120 180"
     data-athena-schema="representation/v1">
  <rect id="body" x="8" y="8" width="104" height="164" />

  <g id="terminal-line"
     data-athena-anchor="line"
     data-athena-point="60 0"
     data-athena-role="terminal"
     data-athena-directions="in"
     data-athena-signals="Power">
    <circle cx="60" cy="8" r="3" />
  </g>

  <g id="terminal-load"
     data-athena-anchor="load"
     data-athena-point="60 180"
     data-athena-role="terminal"
     data-athena-directions="out"
     data-athena-signals="Power">
    <circle cx="60" cy="172" r="3" />
  </g>
</svg>
```

The compiler reads the Athena definition plus only the governed SVG attributes, validates explicit
points and predicates, then discards source markup after lowering. It never guesses an anchor from
`circle`, geometry endpoints, DOM order, CSS, or an unmarked `id`.

Normative metadata profile:

| Location | Allowed `data-athena-*` fields |
| --- | --- |
| SVG root | required `schema=representation/v1`; no identity, version, kind, lifecycle, profile, or binding |
| Selected node with required SVG `id` | one of `anchor`, `label-slot`, or `hotspot` |
| Anchor node | required `point`, `role`; direction/signal token lists required for terminal anchors; optional terminal predicate |
| Label-slot node | required `point`, `label-role` |
| Hotspot node | required `point` |

Names are lowercase kebab-case. Values use exhaustive typed grammars. Unknown fields, wrong-node
fields, missing companions, duplicate ids/contracts, and project/policy vocabulary fail closed.
Attribute diagnostics preserve complete source spans. IDE completion and validation derive from the
same compiler-owned schema; no XSD or frontend-maintained schema becomes a second authority.

## Element Composition V1

M34 Element composition is intentionally small and deterministic:

- Symbol is atomic and cannot contain another Symbol or Element.
- Element may compose Symbols or reference one governed SVG graphic body.
- Element cannot contain another Element in V1.
- Every child has a unique local id, explicit transform, and explicit integer z-order.
- Child transforms use translate, rotate, and scale only and normalize at compile time.
- Exported Element anchors explicitly map to one child anchor or one governed SVG node contract.
- Composition cycles, duplicate child ids, duplicate z-order within one parent, and unexported
  connectable anchors fail lint and compilation.
- Element owns intrinsic child layout only. `drawing-composition` and the spatial compiler own
  project occurrence placement, rails, lanes, routes, frame, and document bounds.

## Representation Selection

```text
PresentationProfileDescriptor
  -> projection, standard/style, compatibility, fallback

BindingManifest
  -> engineering concept/package admitted to representation packages

RepresentationBindingRule
  -> typed semantic predicates, target definition/version/variant, priority

BindingResolver
  -> sole selector of generated descriptor + variant

ResolvedRepresentationSelection
  -> derived RepresentationPolicy

RepresentationBindingCompiler
  -> sole constructor of RepresentationOccurrence
```

Profile and binding declarations are also Athena source:

```athena
profile CabinetIEC {
  projection cabinet
  standard IEC
  style athena-industrial-iec-v1
  fallback fail-closed
}

binding SchneiderGV2Cabinet {
  profile CabinetIEC
  priority 100

  select device where {
    type Switch
    model "GV2"
  }

  use element "com.schneider.gv2" version "1.0.0"
  variant "standard"
}
```

Selection rules:

1. Package snapshot resolves exact versions before binding.
2. Candidate must satisfy profile, manifest, projection, semantic selectors, and lifecycle.
3. Highest explicit priority wins.
4. Equal highest-priority candidates are ambiguous and fail; no filename or lexical tie-break exists.
5. Requested variant must exist. Without a request, exactly one default variant must exist.
6. The `where` block reads compiled semantic facts and cannot declare or mutate them.

`RepresentationBindingRule` is owned by `kernel/package-model` and compiled from each Athena
`binding` declaration. `BindingResolutionRequest` carries the active profile, manifest, exact package
snapshot, generated descriptors, and binding rules to `BindingResolver`.

Legacy `BindingManifest.policyTags` is not a second selector. M34 removes every active product read
of `policyTags`; compatibility adapters may translate legacy fixtures into explicit
`RepresentationBindingRule` values before resolution. The field is deleted after zero active
callers and compatibility tests confirm equivalent diagnostics.

## Canonical Compiled Model

`RepresentationDefinition` is the single canonical reusable representation contract. M34 extends it
with Symbol/Element kind, Graphic Primitive body, intrinsic child composition, anchor compatibility,
and source provenance.

`RepresentationDescriptor` is generated from `RepresentationDefinition` for package indexing and
selection. It is never independently authored. Native graphic bodies and referenced annotated SVG
graphic bodies are exclusive source forms inside one Athena declaration, not peer metadata
authorities. `DrawingSymbolAnatomy`, M33-specific symbol models, and XML manifests are deleted from
the product/runtime path.

## Safe SVG Geometry Profile

The XML parser is namespace-aware and fails initialization if required hardening features cannot be
set. It disables DOCTYPE, external general/parameter entities, external DTD/schema access, XInclude,
and entity expansion. The parser uses a no-I/O resolver that fails any external resource request.

Allowed SVG geometry lowers only into `GraphicPrimitive`:

- line, polyline, polygon, rectangle, circle, ellipse;
- normalized path move/line/cubic/quadratic/arc/close segments;
- text with governed attributes;
- group plus translate/rotate/scale;
- internal acyclic `defs`/`use` within expansion limits.

All attributes use explicit value grammars. URL-valued attributes permit only internal `#id` on the
supported `use` path. `<style>`, CSS `url(...)`, image, filter, mask, animation, script, event
attribute, `foreignObject`, data/http/file URLs, and unknown namespaces fail closed.

Normative SVG V1 grammar:

| Subject | Allowed values |
| --- | --- |
| Root | `svg` with exact SVG namespace, optional `id`, required positive `viewBox`, and exact `data-athena-schema="representation/v1"` only; `metadata` is rejected. |
| Identifier | `[A-Za-z_][A-Za-z0-9_.:-]*`, unique after `use` expansion. |
| Number | Finite unitless decimal; NaN, infinity, exponent overflow, and unit suffixes rejected. |
| Paint | `none`, `currentColor`, `#RGB`, `#RRGGBB`, or `rgb(0..255,0..255,0..255)`. |
| Stroke | Non-negative finite `stroke-width`; cap `butt|round|square`; join `miter|round|bevel`; bounded finite dash list. |
| Transform | Ordered `translate`, `rotate`, `scale` only with finite arguments; matrix/skew rejected in V1. |
| `line` | `x1,y1,x2,y2` plus governed paint/stroke attributes. |
| `polyline`,`polygon` | Bounded finite point pairs plus governed paint/stroke attributes. |
| `rect` | `x,y,width,height,rx,ry`; width/height positive, radii non-negative. |
| `circle`,`ellipse` | Finite center; positive radius/radii. |
| `path` | SVG commands `M/L/H/V/C/S/Q/T/A/Z` and relative forms; finite numbers; arc flags exactly `0|1`; normalized before IR. |
| `text` | Plain UTF-8 text node, `x,y`, positive unitless font size, anchor `start|middle|end`, baseline `alphabetic|central|hanging`; no nested markup. |
| `g`,`defs` | Optional `id` and supported transform; children must be allowed elements; only eligible selected nodes may carry governed `data-athena-*`. |
| `use` | Internal `href="#id"`, optional finite `x,y`, supported transform; target must be acyclic and allowed. |

Any element, attribute, namespace, child node, CSS declaration, metadata field, or value outside this
table and the normative metadata profile is an error. Presentation attributes are mapped to governed
`GraphicStyleToken` values; source attributes never pass through as raw strings to the renderer.

Compiler product caps:

- 5 MiB per SVG source;
- 20,000 XML elements per SVG;
- XML/transform depth 64;
- 100,000 normalized path segments per SVG;
- 10,000 expanded `use` nodes per SVG;
- 50 MiB aggregate SVG bytes and 1,000,000 normalized segments per package snapshot.
- 512 SVG files, 200,000 XML elements, 100,000 expanded `use` nodes, and 1,000,000 emitted
  Graphic Primitives per package snapshot.
- 5,000,000 deterministic compiler work units per package, where each parsed element, normalized
  path segment, expanded `use` node, and emitted primitive consumes one unit.

Limit diagnostics report measured and allowed values. Raw XML/markup never enters transport payloads,
LSP presentation payloads, or Electron renderer APIs.

## Package Snapshot Rule

Repository `athena.yaml` explicitly declares local package roots. Secure ingestion copies regular
files into an immutable staging snapshot while rejecting absolute/archive traversal paths, symlinks,
Windows junctions/reparse points, and identity collisions. All compilation and hashing reads occur
from the staged snapshot, not from mutable source paths.

The stager walks each source path with `NOFOLLOW_LINKS`, verifies every component is a regular
non-link/non-reparse entry, captures file key/size/mtime before opening, opens with no-follow options,
reads and hashes bytes, then rechecks file key/size/mtime and root containment after read. Any change,
missing stable file key, or unsupported no-follow guarantee rejects the package. Providers supporting
`SecureDirectoryStream` use handle-relative traversal. Staged destinations are newly created private
directories and never reuse source paths.

Cache identity includes source bytes, Athena compiler/schema version, and dependency-lock digest.
Caches, generated descriptors, proof payloads, and screenshots can be deleted and reproduced without
changing engineering or representation truth.

## Migration Classification

| Existing Contract | M34 Disposition |
| --- | --- |
| `RepresentationDefinition` | Extend; canonical compiled reusable definition. |
| `RepresentationDescriptor` | Derived projection only; independent authoring rejected. |
| `DrawingSymbolAnatomy` / M33 symbol models | Compatibility input, then delete after zero active callers. |
| `PresentationProfileDescriptor` | Reuse for profile constraints. |
| `BindingManifest` | Reuse for package/concept admission only. |
| `RepresentationBindingRule` | New typed input compiled from Athena `binding`; owned by `package-model`. |
| `BindingManifest.policyTags` | Remove from active resolution; legacy fixtures translate to explicit rules, then field is deleted. |
| `BindingResolver` | Extend as sole selection authority. |
| `RepresentationPolicy` | Derived from resolver result only. |
| `RepresentationBindingCompiler` | Sole occurrence constructor. |
| `GraphicPrimitive` | Extend with ellipse/path; sole Cabinet drawing vocabulary. |
| `PresentationPrimitive` | No new producers; migrate and delete. |
| XML package assets/loaders | Delete from product/runtime; no compatibility path is required. |
| Direct SVG/box rendering | No active Cabinet callers; delete after E2E deletion gates pass. |
