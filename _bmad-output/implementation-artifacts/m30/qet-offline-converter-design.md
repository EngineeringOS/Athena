# M30 Deferred Offline QET Converter Design

## Purpose

This note records the future QElectroTech converter boundary. It is not an M30 product importer,
runtime dependency, or `.athena` syntax extension.

QET `.elmt` files are visual element definitions. A representative folio reference element carries
metadata, translations, QET `link_type`, primitive geometry, style strings, dynamic text bindings,
terminal orientation, element bounds, hotspot coordinates, license text, and UUIDs. That content is
useful visual knowledge, but it is not Athena semantic source.

## Target Data Flow

```text
QET .elmt -> QET Element AST -> Athena Representation Definition IR candidate
```

The output is a candidate Athena-owned representation asset. Human review or a separate validation
step must accept it before it enters a native Athena symbol pack.

The converter must not become `.athena` source.

## Boundary Rules

- The converter is offline tooling only.
- Athena product runtime must not load QET `.elmt` files at product runtime.
- `.athena` semantic source must not reference QET file paths from `.athena` semantic source.
- `.athena` semantic source must not embed QET primitives, SVG paths, style strings, hotspots, or
  QET `link_type` values.
- QET element names, UUIDs, or folder paths must not decide semantic identity.
- Runtime renderers consume Presentation IR only; they do not parse or paint QET assets.

## Converter Pipeline

1. Parse XML into a QET Element AST.
2. Preserve source provenance, element UUID, names, author/license text, and source hash.
3. Normalize supported primitives into Athena Representation Definition IR primitives.
4. Map QET style strings into Athena representation style tokens.
5. Convert QET terminals into Athena anchors with terminal orientation.
6. Convert QET dynamic text into Athena label slots where the binding intent is known.
7. Emit unsupported-feature diagnostics for any QET construct that cannot be represented safely.
8. Emit a deterministic candidate file with stable ordering and formatting.

## Required Conversion Concerns

### Primitive Normalization

The converter may normalize supported QET primitives such as line, polygon, circle, arc, and text
into Athena representation primitives. Coordinate changes must be mechanical and deterministic. The
converter must not infer engineering semantics from primitive shape.

### Style Mapping

QET style strings must be mapped to Athena representation style tokens. Unsupported style details
must produce diagnostics instead of silent approximation when they change visual meaning.

### Terminal Orientation

QET terminal orientation such as north, east, south, and west maps to Athena anchor orientation.
Terminal numbers and anchor ids must be stable. Geometry remains representation-layer data, not
semantic kernel truth.

### Dynamic Text

QET dynamic text can become Athena label slots only when the slot role is understood, such as label,
terminal number, cross-reference, or device tag. Unknown dynamic text bindings must be reported as
unsupported-feature diagnostics.

### Unsupported-Feature Diagnostics

Every unsupported construct must emit a structured diagnostic with:

- source element id or UUID,
- source XML location when available,
- unsupported construct kind,
- impact: dropped, approximated, or blocked,
- recommended manual action.

Blocking diagnostics prevent a candidate from being accepted into a native Athena symbol pack.

### Licensing And Provenance

Candidate assets must retain licensing and provenance metadata:

- original QET element UUID,
- original relative path,
- source hash,
- author/license text,
- converter version,
- conversion timestamp,
- human review status.

The importer must not erase or flatten license text. If license terms are unclear, the candidate is
blocked from product symbol-pack inclusion.

### Deterministic Output

The same input and converter version must produce byte-stable output apart from explicitly excluded
review metadata. Ordering, ids, numeric formatting, and diagnostics must be deterministic so symbol
pack diffs remain reviewable.

## Explicit Non-Goals

- No M30 product importer.
- No QET runtime renderer dependency.
- No `.athena` syntax for QET paths, visual primitives, or symbol geometry.
- No automatic semantic device creation from QET files.
- No claim that QET `.elmt` is compatible with Athena semantic source.
- No full QET compatibility guarantee.

## Acceptance Path For Future Work

Future converter work should add a separate offline tool or plugin that reads QET assets, writes
Athena Representation Definition IR candidates, and runs representation validation. Product runtime
may consume only accepted Athena-owned symbol pack assets.
