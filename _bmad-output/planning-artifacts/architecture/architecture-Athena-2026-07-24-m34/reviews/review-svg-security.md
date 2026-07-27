# M34 Annotated SVG Security Architecture Review

**Review date:** 2026-07-24  
**Verdict:** REJECT - not implementation-ready for untrusted SVG or external assets  
**Scope:** Planning-contract threat model; no implementation was assessed.

## Documents Reviewed

- `_bmad-output/planning-artifacts/prds/prd-Athena-2026-07-24-m34/prd.md`
- `_bmad-output/planning-artifacts/prds/prd-Athena-2026-07-24-m34/addendum.md`
- `_bmad-output/planning-artifacts/architecture/architecture-Athena-2026-07-24-m34/ARCHITECTURE-SPINE.md`

## Executive Assessment

The planning set recognizes the correct threats. It classifies SVG as untrusted input, requires a
closed subset, names dangerous features, requires repository confinement and resource limits, and
prohibits raw SVG DOM in the renderer. Those are security goals, not yet an enforceable security
profile. The documents do not define the parser contract, complete element/attribute/value
allowlists, URI and CSS decoding rules, canonical file-opening algorithm, numerical budgets,
reference-expansion budget, transform semantics, or module/transport controls that keep raw SVG out
of Electron.

That ambiguity permits multiple implementations to claim conformance while remaining vulnerable to
XXE or local-file disclosure, CSS/URL resource loading, junction or race-based root escape,
CPU/memory exhaustion, `use` amplification, coordinate confusion, and frontend injection. M34 should
not accept untrusted vendor or user SVG until the normative profile and negative tests below are
part of the architecture and acceptance evidence.

## Attacker Model And Trust Boundaries

Assume an attacker can supply or modify:

- an SVG-first annotated carrier;
- a plain external SVG referenced by `.athena.element`;
- an inline opaque SVG payload;
- an imported/vendor package and its directory structure, including links/reparse points where the
  host permits them;
- SVG XML names, namespaces, IDs, attribute values, CSS, numbers, paths, transforms, `defs`, and
  reference graphs;
- files that change between validation and use in a writable workspace;
- package data intended to reach diagnostics, proof payloads, caches, LSP transport, or Electron.

The assets may target local file disclosure, network access, credential access, code or script
execution, renderer DOM injection, CPU/memory/disk exhaustion, visual or anchor spoofing, semantic
authority confusion, and leakage through diagnostics.

Required trust flow:

```text
Untrusted bytes
  -> confined, race-resistant byte acquisition
  -> hardened non-resolving XML parser
  -> closed SVG/metadata/CSS/URI validation
  -> bounded reference and transform normalization
  -> immutable typed Symbol/Element + Graphic Primitive IR
  -> typed proof/diagnostics
  -> paint-only renderer/Electron
```

No DOM node, XML event, source markup, style text, URL, file path, or generic HTML/SVG string may
cross from one stage to a later stage merely because an earlier stage inspected it.

## Findings

### SVG-SEC-01 - Critical - XML parser hardening is not an executable contract

**Evidence:** PRD FR-25 names DTD/entities but no parser behavior
(`prd.md:178-179`). The addendum repeats the rejection goal (`addendum.md:156-158`). AD-12 only says
that validation runs before IR (`ARCHITECTURE-SPINE.md:133-138`).

**Exploit path:** A conforming implementation can disable ordinary external entities but leave
external parameter entities, an external DTD, XInclude, schema resolution, or a parser-specific
fallback enabled. A crafted carrier can then read local files, make network requests, expand an
entity bomb, or bypass the intended element validation before the safe-subset pass sees a DOM.

**Missing enforceable boundary:** No parser/library is selected and no required feature matrix is
defined. There is no requirement for namespace-aware parsing, rejection of every `DOCTYPE`, disabled
external general and parameter entities, disabled external DTD/schema access, disabled XInclude,
disabled entity expansion, secure-processing mode, no network-capable resolver, or fail-closed
startup when a hardening option is unsupported. The same omissions apply to any schema, transform,
or secondary XML factory used later.

**Required decision and proof:** Define one compiler-owned parser construction path. Normatively
require all behaviors above, an input stream sourced only from already-confined bytes, embedded/local
schemas if validation is used, and no caller-supplied resolver. Parser construction must fail if any
required setting cannot be installed. Add fixtures for general and parameter XXE, external DTD,
XInclude, external schema, exponential and quadratic entity expansion, and malformed namespace
games; assert zero file/network access and failure before IR.

### SVG-SEC-02 - Critical - The safe subset is not a closed namespace/element/attribute allowlist

**Evidence:** FR-24 requires a safe subset (`prd.md:175-176`) and FR-25 lists selected rejections
(`prd.md:178-179`). The addendum lists allowed primitive categories (`addendum.md:144-154`) but does
not list allowed attributes or declare that every unknown namespace, element, and attribute fails.

**Exploit path:** A blacklist implementation can accept active or implementation-sensitive SVG such
as animation elements (`animate`, `set`, `animateMotion`, `animateTransform`), links, image/media
elements, filters, masks, patterns, markers, cursors, embedded documents, namespace-confused
lookalikes, or new SVG/browser features not named in the rejection list. Event attributes can also be
smuggled through namespace/case handling if validation compares only raw prefixes or selected names.

**Missing enforceable boundary:** There is no exact allowlist by expanded XML name for the SVG root,
metadata vocabulary, child elements, and attributes; no namespace rule; and no reject-unknown rule.
`defs` is treated as a generic allowed container even though what may appear inside it is undefined.

**Required decision and proof:** Publish versioned tables of allowed expanded element names, allowed
attributes per element, and typed value grammars. Reject unknown elements, attributes, namespaces,
processing instructions, declarations, and non-whitespace content where text is not explicitly
allowed. Restrict `defs` to approved internally referenced geometry. Test every excluded active SVG
family and namespace/prefix/case variant.

### SVG-SEC-03 - Critical - URL and CSS handling leaves resource-loading bypasses

**Evidence:** FR-25 rejects "external URLs" and "unsafe CSS resources" (`prd.md:178-179`), while the
allowed profile includes "governed style tokens" (`addendum.md:148-154`). Neither document defines a
URI grammar, CSS parser, or property/value allowlist.

**Exploit path:** An attacker can hide resource access in `href`, `xlink:href`, `url(...)`, `@import`,
`@font-face`, paint servers, markers, masks, filters, cursor values, image sources, custom properties,
or inherited styles. Schemes and tokens can be obscured with XML character references, CSS escapes,
percent encoding, mixed case, whitespace/control characters, protocol-relative forms, Windows paths,
UNC paths, or `data:`, `blob:`, `file:`, and nested SVG payloads. A browser or SVG library may resolve
what a string blacklist did not recognize.

**Missing enforceable boundary:** "External" and "unsafe" are undefined after decoding. The profile
does not say whether `<style>`, inline `style`, selectors, CSS variables, presentation attributes,
fonts, or fragment-only references are allowed, or which layer resolves them.

**Required decision and proof:** For the initial profile, reject stylesheet elements and inline CSS
syntax. Accept only named presentation attributes mapped by a real parser into finite typed style
tokens and an explicit property/value allowlist. Permit references only through a dedicated fragment
type that accepts exactly a local, decoded, canonical `#id`; reject every other URI form before any
rendering library sees it. Add XML-reference, CSS-escape, percent-encoding, mixed-case, control-byte,
scheme, UNC, and nested-resource fixtures with network/file-access sentinels.

### SVG-SEC-04 - Critical - Repository confinement has no Windows-safe acquisition algorithm

**Evidence:** FR-27 requires repository-root confinement and rejection of absolute paths, traversal,
and symlink escapes (`prd.md:184-185`). AD-12 repeats "repository confinement" without an algorithm
(`ARCHITECTURE-SPINE.md:133-138`). Athena-first definitions explicitly support external SVG
(`addendum.md:23-51`).

**Exploit path:** Lexical `startsWith(root)` checks can be bypassed by `..`, alternate separators,
drive-relative paths, rooted paths, UNC/device paths, alternate data streams, percent/double
encoding, trailing dot/space normalization, case differences, junctions, mount/reparse points, or a
symlinked ancestor. Even a correct pre-check can be raced: replace a checked path or ancestor before
the later open and the compiler reads outside the repository. A hard link can expose external
content without appearing as a symlink.

**Missing enforceable boundary:** The documents do not define accepted path syntax, decoding order,
canonical root identity, reparse-point policy, regular-file requirement, race resistance, or whether
package compilation snapshots bytes once and forbids later path re-open.

**Required decision and proof:** Define external asset names as normalized repository-relative path
segments, not general URIs. Reject empty/dot segments, alternate separators, drive/UNC/device
prefixes, colons/ADS, NUL/control characters, encoded separators, reserved names, and links/reparse
points in every ancestor. Acquire a regular file through a no-follow or handle-based confined walk,
verify final file identity against the trusted root after open, and compile from that immutable byte
snapshot. Define hard-link policy explicitly. Test Windows junctions, symlinks, races, ADS, UNC,
device paths, mixed separators, case, trailing dots/spaces, and single/double encoding.

### SVG-SEC-05 - High - Resource limits are named but have no values or aggregate budget

**Evidence:** FR-26 names file size, DOM depth, element count, path complexity, transform depth, and
referenced-resource size (`prd.md:181-182`). The addendum only says "configured complexity limits"
(`addendum.md:156-158`). SM-3 expects oversized input to fail (`prd.md:287`).

**Exploit path:** Each file can remain under an arbitrary per-file limit while a package supplies
thousands of files, references, IDs, text runs, or repeated occurrences. Tiny source can produce huge
DOM, path, transformed-bounds, `use` expansion, proof, diagnostic, cache, or output-primitive work.
Very long numeric tokens, extreme exponents/precision, NaN/non-finite derived values, and malformed
paths can consume disproportionate CPU or memory.

**Missing enforceable boundary:** No normative byte counts, decompressed-byte counts, package totals,
parse time, cancellation behavior, text/ID/reference/style limits, numeric ranges/precision, output
primitive count, or memory/work-unit budget exists. It is unclear whether counters apply before or
after expansion and normalization.

**Required decision and proof:** Publish a versioned limit profile with fixed defaults and hard
ceilings for source bytes, aggregate package bytes/files, XML depth/nodes/attributes/text, ID and
reference counts, path tokens/segments/points, numeric token length/range/precision, transform depth,
expanded instances, output primitives, diagnostics, elapsed work, and cancellation. Charge work
cumulatively across every dependency and derived object before allocation. Reject NaN, infinity,
overflow, underflow-dependent behavior, and out-of-range coordinates. Test each limit at `N-1`, `N`,
and `N+1`, plus aggregate and cancellation cases.

### SVG-SEC-06 - High - Acyclic `defs`/`use` graphs can still expand exponentially

**Evidence:** The addendum permits internal `defs` and `use` when resolvable and acyclic
(`addendum.md:148-154`) and rejects cycles (`addendum.md:162-165`). AD-13 also rejects cyclic targets
(`ARCHITECTURE-SPINE.md:140-145`).

**Exploit path:** A directed acyclic graph can fan out exponentially: each definition can contain
multiple uses of the next definition. A small document then emits millions of geometry instances,
anchors, bounds operations, or proof records without containing a cycle. Deep but legal reference
chains also consume stack or quadratic lookup work.

**Missing enforceable boundary:** There is no reference-graph node/edge budget, maximum chain depth,
maximum uses per target, cumulative expansion-instance budget, approved `use` target-kind list, or
memoization/accounting rule. "Path complexity" does not clearly cover post-expansion geometry.

**Required decision and proof:** Build and validate an explicit local reference graph before
lowering. Restrict target kinds, detect cycles iteratively, and charge every instantiated subtree,
anchor, primitive, and transformed bound against aggregate budgets. Abort before materialization when
the projected budget is exceeded. Add wide-fanout, deep-chain, repeated-target, forward-reference,
self-cycle, and mutual-cycle fixtures.

### SVG-SEC-07 - High - Transform and viewBox semantics are too ambiguous to fail closed

**Evidence:** FR-28 requires deterministic normalization across transforms, viewBox, `defs`, and
`use` (`prd.md:187-188`). Coordinate rules state only that anchors resolve after supported transforms
(`addendum.md:160-168`). AD-13 does not define the supported grammar or math
(`ARCHITECTURE-SPINE.md:140-145`).

**Exploit path:** Parser and renderer disagreement over transform-list order, matrix multiplication,
viewBox origin/scaling, nested SVG, percentages, units, `preserveAspectRatio`, or `use` offsets can
move a validated anchor away from visible geometry. Singular, near-singular, huge, skewed, reflected,
or non-finite matrices can corrupt bounds, overflow geometry, or create off-screen/duplicated output.
This becomes a terminal-placement spoof even without script execution.

**Missing enforceable boundary:** Supported transform functions, order/convention, units, nested
viewport policy, determinant/range checks, stroke/text scaling, reflection behavior, and exact
rounding are unspecified. Unsupported input is not explicitly required to reject the entire asset
rather than be ignored.

**Required decision and proof:** Specify the transform grammar and coordinate algorithm
normatively, including matrix convention, viewBox and `preserveAspectRatio`, units, precision and
rounding, finite/range checks, singularity threshold, bounds derivation, and `use` composition. Reject
the whole asset on any unsupported transform or viewport feature. Use golden vectors for nested
transform ordering, negative/zero scales, skew, rotation centers, non-zero viewBox origins,
reflection, extreme values, and cross-frontend equivalence.

### SVG-SEC-08 - High - Hidden-content rules do not establish visible-anchor integrity

**Evidence:** The addendum rejects an anchor that resolves only to an object hidden in `defs` without
a governed visible use (`addendum.md:162-165`). AD-13 uses the broader term "hidden-only targets"
without defining visibility (`ARCHITECTURE-SPINE.md:140-145`). FR-33 requires missing/off-screen
content to fail Cabinet smoke (`prd.md:203-205`).

**Exploit path:** An anchor can reference geometry hidden by `display:none`, `visibility:hidden`,
opacity, zero dimensions, clipping/masking, an ancestor style, an off-canvas transform, or an
uninstantiated branch outside `defs`. Conversely, a visible `use` may reference a definition carrying
hidden descendants or ambiguous multiple instances. The compiler may report a valid anchor while
the user sees no corresponding terminal, or bind to the wrong occurrence.

**Missing enforceable boundary:** Visibility is not defined in the accepted style/geometry subset.
There is no rule connecting an anchor to exactly one visible expanded occurrence or defining which
clipping, opacity, and zero-area features are excluded.

**Required decision and proof:** Define compile-time visibility using only the allowed subset.
Require each exported anchor to resolve to a finite point or approved geometry on at least one and,
where identity requires it, exactly one governed visible occurrence. Reject clipping/masking/opacity
features unless their semantics are fully compiled. Test every ancestor/local hidden form, zero-area
geometry, off-bounds coordinates, and multiple `use` instances.

### SVG-SEC-09 - High - ID and fragment canonicalization is underspecified

**Evidence:** FR-5 relies on stable SVG IDs (`prd.md:112-116`); FR-7 rejects missing/duplicate IDs,
unresolved refs, unsupported targets, and namespace/schema mismatches (`prd.md:118-119`); the
addendum says duplicate IDs are invalid (`addendum.md:162-164`).

**Exploit path:** Different components may disagree about case, Unicode normalization, XML `id`
typing, percent decoding, character references, whitespace, or namespace-qualified attributes.
Duplicate-looking or differently decoded fragments can cause metadata validation to bind one node
while lowering/rendering uses another. Generated instances from `use` can further create ambiguous
identity.

**Missing enforceable boundary:** The lexical grammar, comparison semantics, document scope,
fragment decoding count, Unicode policy, and clone identity rules are absent. Cross-document
references are not explicitly forbidden by the ID contract.

**Required decision and proof:** Define an ASCII-safe ID grammar or a precise Unicode normalization
policy; compare canonical IDs exactly and case-sensitively; reject duplicate raw and canonical IDs;
allow one decoding pass only in the dedicated fragment parser; prohibit cross-document fragments;
and assign deterministic internal occurrence identities after `use` expansion. Test confusables,
normalization forms, case variants, encoded delimiters, duplicate attributes, and clone ambiguity.

### SVG-SEC-10 - High - The raw-SVG frontend/Electron boundary is policy-only

**Evidence:** FR-29 says raw DOM shall never be transported to or interpreted by Theia
(`prd.md:190`). FR-33 requires smoke to reject raw SVG bypass (`prd.md:203-205`). The architecture
says the renderer consumes resolved facts only (`ARCHITECTURE-SPINE.md:179-182`) and calls the
renderer paint-only (`ARCHITECTURE-SPINE.md:43-50`).

**Exploit path:** A convenience path can add source markup, serialized DOM, metadata XML, a generic
`markup`/`html` field, or an unvalidated fallback URL to an LSP/proof DTO. The frontend can then feed
it to `innerHTML`, `dangerouslySetInnerHTML`, a browser SVG parser, `data:` URL, or WebView. The
Cabinet happy-path smoke may pass while an error, preview, fallback, or diagnostics path retains the
bypass.

**Missing enforceable boundary:** There is no transport schema that is incapable of representing raw
markup, dependency rule preventing frontend XML/SVG parsing, source scan, Content Security Policy,
or Electron negative E2E test. "Raw DOM" may be interpreted narrowly while raw serialized SVG still
crosses the boundary.

**Required decision and proof:** Define closed immutable IR/DTO unions containing only typed finite
geometry, governed text/style tokens, identities, and sanitized diagnostics/provenance. Prohibit raw
XML/SVG/HTML strings, URLs, DOM nodes, and generic markup fields in all renderer/LSP/proof contracts.
Enforce module dependency rules and scans for DOM parsing/injection APIs, disallow network/file
resource loads with CSP/session policy as defense in depth, and run Electron E2E fixtures through
success, error, fallback, proof, diagnostics, and preview paths. Assert that only compiler-generated
primitives reach painting.

### SVG-SEC-11 - High - Inline opaque payload framing is not bounded or unambiguous

**Evidence:** FR-10 says inline SVG is opaque to the Athena grammar (`prd.md:129-130`). The addendum
uses a triple-quoted payload and delegates parsing to a dedicated SVG frontend
(`addendum.md:53-72`).

**Exploit path:** Embedded delimiter sequences, encoding markers, line-ending transformations, or
very large unterminated payloads can desynchronize ANTLR4 and tree-sitter, produce divergent source
ranges, consume unbounded lexer memory, or cause one frontend to inspect bytes different from those
hashed/validated by another. Diagnostics may accidentally echo attacker-controlled markup.

**Missing enforceable boundary:** No delimiter escaping, maximum payload size before tokenization,
byte-to-character encoding rule, BOM policy, newline normalization, source-span mapping, or
cross-parser conformance requirement is defined.

**Required decision and proof:** Specify payload framing and escaping, UTF-8 handling, pre-lex size
limits, exact byte/character normalization, and source-map behavior. Require ANTLR4 and tree-sitter
to emit the same opaque span and never tokenize XML internals. Test embedded/near delimiters,
unterminated payloads, BOMs, CRLF/LF, invalid UTF-8, huge payloads, and diagnostic redaction.

### SVG-SEC-12 - High - Dual-authority rejection can be bypassed without canonical metadata discovery

**Evidence:** FR-20/FR-21 require exactly one authority and reject overlap (`prd.md:161-165`). The
addendum says an Athena-first external SVG must not contain authoritative Athena metadata
(`addendum.md:50-51`) and AD-10 rejects dual authority (`ARCHITECTURE-SPINE.md:119-124`).

**Exploit path:** A plain external SVG can hide Athena-like metadata under an alternate prefix,
duplicate namespace declaration, encoded namespace URI, nested SVG, case/confusable identity, or
unknown version. If one scan checks literal text/prefixes and another parses expanded names, the
wrapper and carrier can both provide metadata or ambiguous identity. Package aliases can also load
the same content under multiple paths.

**Missing enforceable boundary:** There is no rule that Athena-first external SVG rejects all Athena
namespace declarations/elements/attributes, no canonical identity/version grammar, and no package
graph uniqueness check across paths and aliases.

**Required decision and proof:** Discover metadata only from the hardened namespace-aware parser.
For Athena-first external bodies, reject every Athena namespace declaration or expanded Athena name
anywhere in the document, including unknown versions. Canonicalize and index definition identities
and source content across the complete package before lowering; reject duplicates/aliases rather
than merge. Test alternate prefixes, default namespaces, nested metadata, unknown versions,
confusable IDs, and duplicate-content paths.

### SVG-SEC-13 - Medium - Text and font behavior can trigger external access or resource exhaustion

**Evidence:** Text is allowed together with governed style tokens (`addendum.md:148-154`), while
external URL/CSS rejection is only generic (`prd.md:178-179`).

**Exploit path:** CSS or font declarations can load local/remote fonts. Extremely long text,
combining sequences, bidi controls, complex shaping, huge font sizes, or fallback chains can consume
CPU/memory, obscure labels, spoof proof output, or make bounds platform-dependent.

**Missing enforceable boundary:** No font source policy, built-in font set, text/glyph budget,
Unicode/control-character policy, shaping behavior, maximum font size, or deterministic text-bounds
rule exists.

**Required decision and proof:** Reject `@font-face`, external/local font URLs, and arbitrary font
families. Map a bounded set of style tokens to packaged fonts. Bound code points, grapheme/glyph
work, size, and line count; reject prohibited controls or define explicit bidi handling; and define
deterministic bounds/fallback behavior. Add long/combining/bidi/control/extreme-size fixtures and
offline font-load assertions.

### SVG-SEC-14 - Medium - Cache and proof provenance cannot demonstrate that validated bytes were rendered

**Evidence:** Both source forms promise the same provenance and proof payload (`addendum.md:98-110`),
and visible components require source provenance in proof (`prd.md:200-201`). No security provenance
fields or cache rules are specified.

**Exploit path:** A cache keyed only by path, timestamp, identity, or package version can reuse IR
validated from old bytes after a file changes, or reuse a verdict under a newer safe-subset policy.
A path can be validated, replaced, and later re-opened for rendering. Proof can claim a safe compile
without identifying the bytes or profile that passed.

**Missing enforceable boundary:** Source/dependency hashes, parser/compiler/profile versions, limit
profile, validation result, and cache invalidation are absent. Partial parse/DOM caching is not
forbidden.

**Required decision and proof:** Compile exclusively from the confined immutable byte snapshot. Key
cache and proof by cryptographic source hash, dependency hashes, compiler/parser version, schema and
safe-profile version, and limit profile. Cache only fully validated typed IR; never cache DOM or a
partial result. Include counters and rejected-capability status in structured security proof. Test
same-path replacement, dependency changes, profile upgrades, failed-then-fixed inputs, and concurrent
compilation.

### SVG-SEC-15 - Medium - Diagnostics and importer staging are untrusted output channels

**Evidence:** LSP diagnostics are required for the new source (`prd.md:135-136`); importers may emit
native or annotated definitions that pass the compiler (`prd.md:222-229`; `ARCHITECTURE-SPINE.md:105-110`).
The planning set does not define diagnostic redaction or a quarantine state.

**Exploit path:** XML parser errors, entity names, URLs, source snippets, or path failures can expose
absolute paths, UNC shares, local file content, secrets, or attacker-controlled markup to logs, proof,
LSP, and Electron. An importer may place generated files directly into an active package location,
where watchers or previews consume them before the complete output set validates.

**Missing enforceable boundary:** No structured diagnostic schema limits message size/content, strips
local roots and source snippets, or prevents markup interpretation. Imported output has no explicit
staging/quarantine-to-validated transition or atomic publication rule.

**Required decision and proof:** Emit bounded diagnostic codes plus sanitized relative locations and
typed parameters; never forward parser exception text, expanded entities, source markup, absolute
paths, or URLs. Import into an isolated staging snapshot, compile the full dependency closure under
the same untrusted profile, and atomically publish only fully validated typed artifacts. Test
path/content leakage, markup in errors, diagnostic floods, partial imports, watcher races, and
failure cleanup.

## Required Normative Security Profile

Approval requires a versioned M34 SVG Security Profile that is referenced by FR-24 through FR-29 and
AD-12/AD-13 and contains, at minimum:

1. Exact XML parser construction and fail-closed hardening behavior.
2. Expanded-name allowlists for SVG and Athena metadata elements and attributes.
3. Typed value grammars for numbers, dimensions, paths, transforms, IDs, fragments, text, and styles.
4. A no-stylesheet initial policy or a real CSS parser with property/value allowlists and no URLs.
5. A repository-relative path grammar and race-resistant, no-follow immutable byte acquisition.
6. Fixed per-asset and aggregate budgets, including post-expansion/output work.
7. Deterministic reference-graph, visibility, viewBox, transform, anchor, and bounds semantics.
8. Immutable IR/DTO schemas and dependency controls that cannot carry raw markup or URLs downstream.
9. Cache/proof keys that bind validated source bytes and dependencies to the exact security profile.
10. Structured, bounded, redacted diagnostics and quarantined importer publication.

Security-sensitive configuration may lower budgets but must not enable excluded capabilities or
raise hard ceilings without a new reviewed profile version.

## Minimum Verification Matrix

| Area | Required negative evidence |
| --- | --- |
| XML parser | General/parameter XXE, external DTD/schema, XInclude, entity bombs, malformed namespaces; no file/network access |
| Active SVG | Every excluded element/attribute/namespace, animation, linking, media, embedded content, processing instructions |
| URL/CSS | All schemes, UNC/device paths, `url()`, imports/fonts, XML/CSS/percent encoding, controls, mixed case |
| Paths | `..`, alternate separators, drive-relative/absolute, UNC/device/ADS, symlink/junction/reparse ancestors, hard-link policy, replacement race |
| Resources | Every limit boundary, package aggregates, long numbers/text/IDs, malformed paths, timeout and cancellation |
| `defs`/`use` | Self/mutual cycles, deep chains, exponential DAG fanout, duplicate/ambiguous instances, unsupported targets |
| Coordinates | Golden matrices for transform order, viewBox, offsets, negative/singular/extreme matrices, hidden/off-screen targets |
| Authority | Alternate prefixes/namespaces/versions, duplicate identities, aliased content, Athena metadata in plain external SVG |
| Frontend | Malicious payloads through success/error/fallback/preview/proof/diagnostic paths; no DOM parser/injection or resource load |
| Provenance | Source/dependency replacement, cache/profile-version invalidation, proof hashes/counters, failed compilation not cached |

Each negative test must assert failure before Graphic Primitive IR, no partial IR/cache/publication,
no file or network access, bounded diagnostics, and no raw payload in LSP or Electron messages. The
Electron suite must use process/session instrumentation to prove denied navigation, requests, and
file reads rather than infer safety from a non-executing screenshot.

## Approval Conditions

The verdict can move to approval only when:

- the normative profile resolves SVG-SEC-01 through SVG-SEC-15;
- the profile is bound explicitly to the PRD acceptance requirements and architecture decisions;
- malicious fixtures and boundary tests cover the matrix above in compiler, package, LSP, and fresh
  Cabinet-only Electron evidence;
- proof shows the exact validated bytes, dependencies, profile version, resource counters, and typed
  IR path used by every visible asset; and
- no compatibility, preview, diagnostics, fallback, or importer path can consume or transport raw
  SVG outside the secure compiler boundary.
