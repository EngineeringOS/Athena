# M35 Architecture Spine Divergence Follow-up

## Verdict

**FREEZE BLOCKED.** The updated spine incorporates the requested D-1 through D-12 remedies. All
twelve original findings are closed against their stated required fixes. A second attack found ten
new or residual shared-contract holes that can still make independent stories produce incompatible
physical contracts, placement, resources, transforms, SVG geometry, routes, or source syntax.

## D-1 Through D-12 Retest

| Finding | Status | Evidence in updated spine |
| --- | --- | --- |
| D-1 contract resolution | **Closed** | AD-3 names one `PhysicalInstallationContractResolver`, field-level precedence, same-precedence ambiguity, required-field failure, provenance, and canonical digest ownership. |
| D-2 occurrence join | **Closed** | AD-4 and AD-11 define one `InstallationOccurrenceKey`, pass it to both paths, and require exactly one physical and one representation occurrence. |
| D-3 topology | **Closed against original finding** | AD-4 replaces generic parentage with typed containment and mount targets and rejects illegal edges/cycles. A separate PRD conflict remains under N-2. |
| D-4 coordinate frame | **Closed** | AD-5 defines origin, axes, discrete orientation, placement point, rotation centre, nested transform order, units, and physical/drawing ownership. |
| D-5 visual mapping | **Closed against original finding** | AD-11 defines one aspect-preserving, centred `CabinetVisualTransform` and one transform id for all visual material. Its matrix order remains ambiguous under N-3. |
| D-6 package/resource identity | **Closed** | AD-7 and AD-9 separate package coordinate, Athena namespace, source unit, resource key, definition version, and portable path equality. |
| D-7 lock digest cycle | **Closed against original finding** | AD-9 explicitly excludes generated lock bytes from snapshot identity. Bound wording still conflicts under N-5. |
| D-8 aggregate snapshot | **Closed** | AD-8 defines one ordered repository snapshot, one admitted snapshot per coordinate, overlap/file-identity rejection, and package plus repository budgets. |
| D-9 SVG lookup namespace | **Closed against original finding** | AD-10 makes XML `id` the sole key and constrains the optional hint to equal it. Repeated `<use>` cardinality remains under N-6. |
| D-10 constraint geometry | **Closed** | AD-6 defines canonical rectangles, edge contact, clearance inflation, fit, exact compatibility, and exclusion of visual bounds. |
| D-11 routing ownership | **Closed against original finding** | AD-12 separates engineering endpoint identity, Physical IR topology/intent, and composition-owned `CabinetRouteFact`. Route derivation remains open under N-7. |
| D-12 trace and mutation | **Closed** | AD-13 and AD-14 define occurrence/primitive identity, subject union, normalized trace table, selectable coverage, and governed future mutation targets. |

## New Divergence Holes

### N-1 - `PhysicalInstallationContract v0` has no canonical field schema or merge granularity

AD-3 resolves "each field independently," but neither the spine nor its seed defines whether
footprint, four-sided clearance, allowed orientations, mounting compatibility, and container
compatibility are scalar leaves or indivisible records. One story can override only `clearance.top`;
another can replace the complete clearance record. They produce different contracts, provenance,
required-field diagnostics, and digests while obeying AD-3.

**Required fix:** Publish the closed v0 contract type, required/optional fields, leaf-level override
granularity, canonical ordering, units, empty-set rules, and digest encoding.

### N-2 - Mounted placement is not assigned to either the enclosure frame or mount-target frame

AD-4 gives every occurrence both `containerId` and `mountTarget`; AD-5 mentions container-local and
nested transforms but never states which frame owns the authored placement. A rail story can treat
position as rail-local while a composition story treats it as enclosure-local. The bound PRD also
says a duct may be a component container (FR-20), while AD-4 forbids ducts as mount targets and makes
the enclosure the sole container.

**Required fix:** Define the placement frame for each mount-target kind and its transform into the
enclosure frame. Reconcile FR-20 with AD-4 so duct/container/mount-target legality has one answer.

### N-3 - `CabinetVisualTransform` does not define rotation and fit matrix order

AD-5 rotates intrinsic points around the unrotated footprint centre. AD-11 maps intrinsic bounds
into the post-orientation footprint and then applies the physical-to-drawing transform. One
implementation can rotate before aspect-fit; another can fit unrotated geometry into the rotated
AABB and rotate later or not at all. Bodies and anchors then disagree at 90/270 degrees.

**Required fix:** Publish the exact matrix composition order, including intrinsic-bound
normalization, uniform scale, centring, discrete orientation, physical placement, and drawing-unit
mapping. Add 0/90/180/270 golden transform contracts for bodies and anchors.

### N-4 - Resource path base and resource-reference name resolution are not closed in the spine

AD-8 says package-local, AD-9 keys resources by declaring source unit, and AD-15 references a bare
resource id. The bound PRD says paths are source-file-relative (FR-5), but the spine does not carry
that rule or define whether a bare id is lexical to one source, package-wide, imported, or exported.
Two source files may legally declare the same id and a third file cannot resolve it deterministically.

**Required fix:** State that path resolution is relative to the declaring source file, define bare-id
scope, define any qualified cross-source/package form, and define duplicate/shadowing diagnostics.

### N-5 - Lock-state identity remains contradictory across bound contracts

AD-9 and the cache convention say generated lock bytes never feed snapshot identity. AD-16 lists
"lock digest" among deterministic compilation inputs, while PRD FR-6 includes dependency lock digest
in package resource identity. A repository story can key resources/caches by lock bytes while a
package-runtime story excludes them, recreating a higher-level form of D-7.

**Required fix:** Introduce one named `ValidatedLockStateDigest` over canonical resolved lock facts,
with an unlocked-mode sentinel. Remove lock digest from logical resource identity and replace the
ambiguous AD-16/FR-6 wording. Generated lock bytes, formatting, and ordering must never affect it.

### N-6 - Repeated SVG `<use>` expansion has undefined geometry-reference cardinality

AD-10 requires ids to be unique after safe `<use>` expansion and says lookup returns one node plus
one accumulated transform. A legal SVG can instantiate one id through `<use>` more than once. One
frontend can reject it, another can index the definition once, and another can create multiple
transformed occurrences. Each interpretation satisfies part of AD-10 but attaches Athena geometry
to different rendered nodes.

**Required fix:** Choose one v0 rule: forbid references to multiply-instantiated definitions, or
define canonical expanded occurrence ids/paths and explicit occurrence selection. Specify whether
uniqueness is checked on source definitions or expanded occurrences.

### N-7 - `CabinetRoutingCompiler` has no deterministic path derivation policy

AD-12 fixes ownership and validates channel containment, but an ordered channel sequence still
admits many valid orthogonal routes. Stories can choose channel centre lines, nearest edges, shortest
Manhattan paths, different bend order, different endpoint stubs, or different lane allocation. All
can pass the stated proof while producing different facts and screenshots.

**Required fix:** Define a deterministic v0 routing algorithm and tie-break order for channel
entry/exit points, centre/lane selection, bends, endpoint stubs, obstacle rejection, shared-channel
ordering, and impossible-route diagnostics. This is deterministic compilation, not general solving.

### N-8 - Duct and `RouteChannel` have no declared relationship

Both are enclosure children, but the spine does not say whether a route channel is authored inside a
duct, derived from duct interior, independent of it, or forbidden to overlap its walls. One physical
story can derive channels from ducts while another requires separate rectangles; routing proof and
visible Cabinet composition then disagree.

**Required fix:** Define the v0 relationship explicitly. If independent, forbid implicit derivation.
If related, add a typed relation and one rule for deriving/validating channel geometry against duct
interior.

### N-9 - `ClearanceZone` exists in topology but has no semantics

AD-4 includes `ClearanceZone`, while AD-6 evaluates only occurrence-owned four-sided clearance.
Teams can interpret a zone as a keep-out region, a rendered annotation, a container reserve, or a
materialized occurrence clearance. Collision and fit results will differ.

**Required fix:** Either remove `ClearanceZone` from v0 or define its owner, frame, geometry,
applicability, collision/fit rules, rendering status, and diagnostic provenance.

### N-10 - Public physical-installation syntax and AST are not frozen

AD-15 freezes exact resource syntax but describes physical installation additions only as "minimum
domain-facing installation intent." Language, compiler, formatter, LSP, and Tree-sitter stories can
independently invent incompatible declarations for enclosure, mount, placement, channel sequence,
and constraints while still following the ownership ADs.

**Required fix:** Publish the minimum source grammar and typed AST contract, including accepted and
rejected examples, before splitting implementation stories. Internal IR names must remain private.

## Freeze Gate

Do not freeze the architecture until N-1 through N-10 are resolved in the spine and conflicting
bound PRD wording is corrected. D-1 through D-12 do not need reopening; their exact requested fixes
are present. The remaining work is to close the newly exposed contracts around schema, frames,
resource lookup, lock state, SVG expansion, route derivation, physical zones, and public syntax.
