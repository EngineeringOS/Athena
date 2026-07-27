# M34 SVG Security Architecture Follow-up

**Review date:** 2026-07-24  
**Verdict:** REJECT - materially improved, but five high-severity boundaries remain unenforceable  
**Scope:** Revised PRD, addendum, and architecture spine. This is a planning-contract review, not an
implementation review.

## Documents Reviewed

- `_bmad-output/planning-artifacts/prds/prd-Athena-2026-07-24-m34/prd.md`
- `_bmad-output/planning-artifacts/prds/prd-Athena-2026-07-24-m34/addendum.md`
- `_bmad-output/planning-artifacts/architecture/architecture-Athena-2026-07-24-m34/ARCHITECTURE-SPINE.md`
- Prior baseline: `reviews/review-svg-security.md`

## Delta Verdict

The revision materially strengthens the boundary. It now:

- requires namespace-aware parsing and names DOCTYPE, external general/parameter entities, external
  DTD/schema access, XInclude, and entity expansion as disabled (`addendum.md:202-207`;
  `ARCHITECTURE-SPINE.md:137-144`);
- describes the lowering profile and rejects active/resource-bearing SVG classes
  (`ARCHITECTURE-SPINE.md:243-258`);
- declares offline operation and an immutable package snapshot (`ARCHITECTURE-SPINE.md:190-197`,
  `207-213`);
- supplies initial non-increasable product caps for six important counters
  (`ARCHITECTURE-SPINE.md:260-262`); and
- names `GraphicPrimitive` as the sole active Cabinet vocabulary while direct SVG remains
  compatibility-only (`ARCHITECTURE-SPINE.md:180-188`).

Those changes close the prior review's broad absence-of-policy objection. They do not yet define
enough behavior to make the controls implementation-independent or testable as a security contract.
Five high-severity findings remain. No critical finding remains in the revised planning set, provided
the stated prohibitions are implemented fail-closed.

## Boundary Disposition

| Boundary | Follow-up status | Reason |
| --- | --- | --- |
| XML parser hardening | Partial / High | Dangerous features are named, but parser construction, unsupported-setting failure, and no-I/O proof are not mandated. |
| Closed lowering profile | Partial / High | Element mapping exists; expanded-name, per-element attribute, and typed value tables do not. |
| URLs and CSS | Partial / High | Major carriers are rejected, but all URI-bearing attributes and decoding/canonicalization rules are undefined. |
| Immutable snapshot and paths | Partial / High | Snapshot lifecycle exists, but root grammar and race-resistant byte acquisition do not. |
| Resource limits | Partial / High | Six caps exist, but aggregate package, XML work, numeric, reference, output, and time budgets do not. |
| Frontend bypass | Partial / High | Active Cabinet vocabulary is constrained, but transport/type/module controls and malicious Electron proof are absent. |

## Remaining Findings

### M34-SVG-FU-01 - High - Parser hardening can silently degrade or occur after unsafe resolution

**Revised control:** FR-24 requires a namespace-aware, DTD/entity-disabled frontend
(`prd.md:190-193`). The addendum explicitly disables DOCTYPE, external general and parameter
entities, external DTD/schema access, XInclude, and entity expansion (`addendum.md:202-207`). AD-12
repeats the parser capability prohibitions (`ARCHITECTURE-SPINE.md:137-144`).

**Residual exploit:** XML libraries expose these controls through parser-specific flags and resolver
APIs. Some ignore unsupported options, apply them only to one factory, or perform resolution while
building the object inspected by later validation. An implementation can therefore satisfy the prose
in its own validator while a parser, schema factory, transformer, or XInclude layer still reads a
local file or performs a network request. A permissive fallback after a failed feature assignment
reopens XXE and entity-expansion attacks.

**Missing enforceable boundary:** The architecture does not require:

- one compiler-owned parser construction path used by both inline and external SVG;
- fail-closed construction when any mandatory hardening property is unsupported;
- secure-processing mode and a resolver that rejects every external lookup;
- the same no-external-access policy on schema/transform factories, if present;
- parsing only from already-snapshotted bytes rather than a path, URL, or caller stream; or
- no-file/no-network instrumentation in negative tests.

**Required closure:** Add a normative parser profile with required observable behavior and one
factory boundary. Parser setup must fail if any control cannot be installed. Every XML-related
factory must receive an always-deny resolver and no-external-access properties. Fixtures for general
and parameter XXE, external DTD/schema, XInclude, entity bombs, malformed namespaces, and parser
fallback must assert failure before IR plus zero filesystem/network attempts.

### M34-SVG-FU-02 - High - The lowering profile and URL/CSS rules are not closed at attribute/value level

**Revised control:** FR-24 now calls for a closed element/attribute/style allowlist
(`prd.md:192-193`). The addendum rejects `<style>`, images, filters, masks, animation, external URLs,
and unsafe CSS (`addendum.md:194-207`). The V1 architecture maps basic geometry to
`GraphicPrimitive` and says style, image, paint/resource features, and external URLs fail closed
(`ARCHITECTURE-SPINE.md:243-258`).

**Residual exploit:** The table identifies source elements but not their allowed expanded names,
attributes, or value grammars. URL and active content can occur in `href`, `xlink:href`, presentation
attributes such as `fill`/`stroke`, `url(...)`, marker/cursor/paint values, namespace-qualified
attributes, and inline `style`. Tokens can be obscured with XML character references, CSS escapes,
percent encoding, mixed case, whitespace/control characters, or multiple decoding passes. The text
profile still permits "governed text/style attributes" (`ARCHITECTURE-SPINE.md:253`) while
`style` is also said to fail closed (`ARCHITECTURE-SPINE.md:256`), leaving element, attribute, and
token meanings ambiguous. A blacklist or string-prefix implementation can accept a value later
interpreted as a resource or generic style by a downstream adapter.

**Missing enforceable boundary:** There is no versioned table for:

- accepted root/metadata/SVG expanded names and namespaces;
- allowed attributes per element, including whether `style`, `class`, and all `href` forms are
  categorically rejected;
- accepted presentation properties and finite typed values;
- local fragment grammar for `defs`/`use`, decoding count, ID comparison, and cross-document ban;
- handling of unknown elements, attributes, namespaces, processing instructions, and declarations;
  or
- proof that arbitrary strings cannot enter a `GraphicPrimitive` style/resource field.

**Required closure:** Publish an exhaustive expanded-name element/attribute matrix and typed value
grammars. Reject every unknown name. In V1, reject stylesheet syntax, inline `style`, `class`, all
URI-bearing attributes, all paint servers, and all `url(...)` values; represent allowed colors,
strokes, fonts, and local `use` references as closed value types. Parse local references with one
dedicated canonical fragment parser. Add encoded, namespaced, mixed-case, control-character,
`data:`/`file:`/HTTP/UNC, CSS-variable, and nested-resource fixtures and assert no I/O or generic
string reaches IR.

### M34-SVG-FU-03 - High - Snapshot staging does not define repository-root confinement or race-resistant acquisition

**Revised control:** NFR-10 forbids renderer scans/rereads and requires an immutable compiled
snapshot (`prd.md:292-294`). The addendum requires `athena.yaml` package roots, immutable staging,
archive-traversal rejection, and Windows link/reparse-point rejection (`addendum.md:221-227`). AD-18
adds duplicate identity rejection and content-addressed caches (`ARCHITECTURE-SPINE.md:190-197`),
while AD-20 prohibits network fetches (`ARCHITECTURE-SPINE.md:207-213`).

**Residual exploit:** A declared package root or external asset can use an absolute, drive-relative,
UNC/device, alternate-data-stream, mixed-separator, case-normalized, trailing-dot/space, or encoded
path unless a precise grammar rejects it. Checking canonical paths or reparse points and later opening
by pathname leaves a check/use race: an attacker can replace the file or an ancestor after validation
but before snapshot copying. Archive extraction can race or overwrite through duplicate/colliding
names. Hard links can expose bytes outside the intended ownership model without appearing as a
symlink or reparse point.

**Missing enforceable boundary:** The documents do not specify:

- that every `athena.yaml` package root and asset path is a normalized repository-relative path;
- decoding and separator normalization order, Windows case/name collision handling, ADS/device/UNC
  rejection, or reserved-name handling;
- no-follow/handle-based traversal and post-open file-identity verification;
- regular-file-only and hard-link policy;
- collision-safe archive entry normalization before extraction; or
- that parsing, hashing, and compilation all consume the same immutable byte snapshot with no later
  path reopen.

**Required closure:** Define the repository root as a trusted canonical handle and accept only a
strict repository-relative segment grammar for package roots and assets. Reject absolute,
drive-relative, UNC/device, colon/ADS, encoded-separator, dot, empty, reserved, and colliding names.
Acquire regular files through a no-follow confined walk, verify final identity after open, reject all
link/reparse ancestors, define hard-link policy, and snapshot the opened bytes once. Normalize and
validate all archive entries before any write. Test junction/symlink races, hard links, ADS, UNC and
device paths, mixed separators, case/trailing-name collisions, duplicate archive entries, and
same-path replacement.

### M34-SVG-FU-04 - High - Hard caps omit aggregate and derived-work resource bombs

**Revised control:** The architecture sets non-increasable caps of 5 MiB source, 20,000 XML elements,
depth 64, 100,000 normalized path segments, transform depth 64, and 10,000 expanded `use` nodes and
requires measured/allowed diagnostics (`ARCHITECTURE-SPINE.md:260-262`). FR-26 identifies the main
resource categories (`prd.md:198-199`).

**Residual exploit:** An attacker can distribute work across many individually valid files in one
package, or concentrate it in uncapped attributes, IDs, references, text runs, path-number tokens,
transform lists, metadata, diagnostics, and output primitives. Acyclic `defs`/`use` fanout remains
expensive before or while the 10,000-node cap is discovered. Extreme finite coordinates, exponents,
precision, arc parameters, singular matrices, and malformed path alternatives can trigger overflow,
quadratic parsing, expensive bounds calculation, or platform-dependent results. No elapsed-work,
memory, cancellation, or diagnostic-count budget prevents a compile or LSP denial of service.

**Missing enforceable boundary:** Caps are not defined for aggregate package bytes/files, XML
attributes/text/namespaces, IDs and reference graph edges/depth, numeric token length/range/precision,
text/glyph work, transforms per element, generated primitives/points/bounds operations, diagnostics,
cache/proof size, wall/CPU work, or cancellation. It is unclear whether counters are charged before
allocation and cumulatively across dependencies and all derived expansion.

**Required closure:** Extend the profile with fixed per-asset and aggregate package budgets for every
input and derived-work category above. Reject NaN, infinity, overflow, excessive exponent/precision,
out-of-range coordinates, and singular/near-singular transforms. Build and budget the reference graph
before expansion; charge projected instances and output before allocation. Define deterministic
cancellation and bounded diagnostics. Test every limit at `N-1`, `N`, and `N+1`, plus package-wide
fanout, long-token, malformed-path, extreme-number, diagnostic-flood, timeout, and cancellation cases.

### M34-SVG-FU-05 - High - The frontend boundary still relies on routing policy rather than an incapable transport

**Revised control:** FR-29 prohibits raw SVG DOM in Theia (`prd.md:204-207`). AD-17 requires active
M34 Cabinet compilation/rendering to consume only the sealed `GraphicPrimitive` vocabulary and bars
new M34 producers for direct SVG/box paths (`ARCHITECTURE-SPINE.md:180-188`). Source/dependency rules
say the renderer consumes resolved primitive facts (`ARCHITECTURE-SPINE.md:238-241`).

**Residual exploit:** Existing compatibility direct-SVG paths and their browser sinks remain present
until migration/deletion tests pass. A fallback, preview, diagnostics, proof, error, or legacy route
can carry serialized SVG/XML, a generic markup field, URL, or source snippet even if the successful
Cabinet path uses `GraphicPrimitive`. "No new M34 producers" is a producer convention, not a type or
module boundary. A frontend can feed such data to DOM parsing, `innerHTML`, an SVG image URL, WebView,
or other browser resource loader. Happy-path screenshots and nonblank canvas checks do not prove the
absence of these paths.

**Missing enforceable boundary:** The architecture does not require:

- closed transport DTOs incapable of carrying SVG/XML/HTML, URLs, DOM nodes, or generic markup;
- module/dependency rules that prevent Theia from importing XML/SVG parsers or injection helpers;
- deletion or physical isolation of all direct SVG/browser parse sinks from the shipped Cabinet
  product, including error and fallback routes;
- CSP/session/navigation/request denial as defense in depth; or
- malicious Electron E2E through success, failure, fallback, preview, proof, and diagnostics paths
  with file/network/DOM instrumentation.

**Required closure:** Define immutable closed IR and LSP/proof DTO unions containing only bounded
finite geometry, governed style/text tokens, identities, and sanitized diagnostics. Forbid raw
markup, source snippets, URLs, and generic string payloads at schema and source-dependency level.
Remove direct SVG sinks from the shipped Cabinet dependency graph or isolate them in an unshipped
fixture module with deletion tests. Enforce Electron CSP/session request and navigation denial, then
run malicious fixtures through every renderer-adjacent route and assert no DOM parser/injection,
filesystem read, network request, or raw payload transport occurs.

### M34-SVG-FU-06 - Medium - Transform and viewBox behavior remains non-normative

**Evidence:** AD-13 promises deterministic root-coordinate normalization and fail-closed unsupported
targets (`ARCHITECTURE-SPINE.md:146-151`), but the V1 profile only says "supported transforms" and
root-coordinate validation (`ARCHITECTURE-SPINE.md:243-255`).

The supported transform grammar, list order, matrix convention, viewBox and
`preserveAspectRatio` behavior, units, nested viewport policy, rounding, finite/range checks,
singularity threshold, and stroke/text scaling are unspecified. Different parser/lowering
implementations can validate one anchor position and render another or overflow bounds. Publish
golden transform/viewBox semantics and reject the whole asset for every unsupported function, unit,
viewport feature, singular matrix, or non-finite result.

### M34-SVG-FU-07 - Medium - `defs`/`use` graph validity is narrower than reference integrity

**Evidence:** Internal `defs`/`use` must be acyclic, resolvable, and expanded within limits
(`ARCHITECTURE-SPINE.md:256-258`), and duplicate IDs are invalid (`addendum.md:211-216`).

The profile does not define permitted target element kinds, canonical ID/fragment grammar,
reference-graph edge/depth accounting, forward-reference behavior, generated occurrence identity, or
whether an exported anchor may bind multiple visible `use` instances. Define exact local-fragment
and occurrence semantics, reject canonical duplicate IDs and cross-document references, and test
deep/wide DAGs, target-kind confusion, encoded IDs, aliases, and multiple visible instances.

### M34-SVG-FU-08 - Medium - Visibility rules cover `defs` but not the accepted visual state

**Evidence:** An anchor cannot resolve only to an object hidden in `defs` without a governed visible
use (`addendum.md:213-216`), and hidden-only targets fail closed (`ARCHITECTURE-SPINE.md:146-151`).

Visibility is not defined for ancestor/local display or visibility attributes, zero dimensions,
transparent paint, off-viewBox transforms, text with no deterministic bounds, or multiple uses. The
closed attribute profile should either reject all visibility-altering features or define compile-time
visibility and require every exported anchor to resolve to the intended finite visible occurrence.

### M34-SVG-FU-09 - Medium - Cache identity omits the security and limit profile

**Evidence:** Caches key on source bytes, compiler/schema version, and dependency-lock digest
(`prd.md:296-297`; `ARCHITECTURE-SPINE.md:194-197`).

The key does not explicitly include the safe-profile version, parser configuration/version, hard
limit profile, all dependency content hashes, or validation verdict. A policy tightening can reuse IR
accepted under older rules if compiler/schema versions do not change, and a lock digest may not prove
the bytes of local dependencies. Include all security-policy inputs and dependency byte hashes in the
cache/proof key; cache only fully validated typed IR, never DOM or partial results.

### M34-SVG-FU-10 - Medium - Acceptance evidence does not prove security side effects are absent

**Evidence:** SM-3 requires malicious/oversized SVG to fail before IR (`prd.md:316-320`), and Electron
evidence requires fresh user data, zero diagnostics, proof, pixel checks, and visual review
(`prd.md:237-239`). These clauses do not require the negative security instrumentation needed by the
six reviewed boundaries.

Add an AC-level security matrix that asserts failure phase, no partial IR/cache/snapshot publication,
zero file/network/navigation attempts, bounded/redacted diagnostics, no raw payload in LSP/Electron,
and deterministic resource counters. Cover inline, external, annotated, archive, package-root,
success, compile-error, fallback, preview, proof, and diagnostics routes. Screenshots are not security
evidence.

## Required Approval Gate

M34 remains rejected for untrusted SVG until all five high findings are converted into normative,
versioned contracts and demonstrated by negative tests. Approval requires:

1. one fail-closed parser construction path with no-I/O fixtures;
2. exhaustive expanded-name/attribute/value tables and one canonical local-reference parser;
3. race-resistant repository-relative byte acquisition into a single immutable snapshot;
4. per-asset and aggregate input/derived-work caps with cancellation and boundary tests; and
5. closed renderer/LSP/proof transports plus instrumented malicious Electron tests across every
   compatibility, error, fallback, and preview route.

The medium findings should be resolved in the same profile because they determine whether the five
high controls remain deterministic, cache-safe, and testable.
