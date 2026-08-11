# Reality And Upstream Review - Athena M43 Architecture Spine

- **Review lens:** current technology, repository reality, framework fit, licensing, performance
  claims, and implementation boundaries
- **Reviewed:** 2026-08-06
- **Primary inputs:** `ARCHITECTURE-SPINE.md`, `UPSTREAM-ADOPTION.md`
- **Reality inputs:** active Gradle/Yarn configuration, current LSP/frontend presentation path,
  active M43 proof harness, M43 PRD, pinned `reference/` repositories, npm registry metadata, and
  upstream license text
- **Mechanical gate:** `lint_spine.py` passed with zero findings

## Verdict

**HOLD - not ready to finalize.** Technology selection is broadly viable: repository pins are real,
Konva 10.3.0 fits the imperative Theia host boundary, and most upstream commit/version claims are
accurate. Finalization is blocked by one unreconciled product-contract reversal. Five further high
risks leave the replacement path, direct schema validation dependency, asset supply chain, edit
transaction, and performance gate open to incompatible implementations.

Finding count: **1 critical, 5 high, 4 medium, 1 low**.

## Verification Summary

### Repository Stack

| Technology | Claimed | Verified reality | Result |
| --- | --- | --- | --- |
| Java | 25 | Root build enforces `JavaVersion.VERSION_25` and `jvmToolchain(25)`. | Match |
| Kotlin | 2.4.0 | `gradle/libs.versions.toml` pins `2.4.0`. | Match |
| Theia | 1.73.1 | Product and frontend manifests pin `1.73.1`. npm latest is `1.74.0`; `1.73.1` peers accept React 18.3.1. | Compatible brownfield pin |
| Electron | 39.8.7 | Product manifest and Yarn lock pin `39.8.7`. npm latest is `43.3.0`. | Compatible brownfield pin |
| React | 18.3.1 | Manifests declare `^18.3.1`; lock resolves `18.3.1`; widget uses Theia's shared React import. | Match, but manifest is a range |
| TypeScript | 5.9.3 | Root IDE manifest declares `^5.9.2`; Yarn lock resolves `5.9.3`. | Match, but manifest is a range |
| Ajv | 8.20.0 | Yarn lock resolves transitive Ajv ranges to `8.20.0`; no Athena package declares Ajv directly. | Contract mismatch |
| Konva | 10.3.0 exact | npm latest is `10.3.0`; local commit `914acaf8bc00...` has package version `10.3.0`, MIT, and a clean worktree. It is not yet in Athena manifests or lock. | Valid planned adoption, not yet pinned in product |

Registry latest versions are not a reason to upgrade Theia, Electron, React, or TypeScript inside
M43. Existing pins are mutually compatible and reduce brownfield risk. The document should call
them repository-pinned rather than imply that all are registry-latest.

### Upstream Evidence

| Upstream | Version/commit verification | License reality | Result |
| --- | --- | --- | --- |
| Konva | `10.3.0`, local `914acaf8bc00...`, clean | MIT; no npm runtime dependencies reported | Match |
| PixiJS | `8.19.0`, local `1d90a20c6243...`, clean | MIT | Match |
| Excalidraw | registry `0.18.1`, local `e4ab626739f5...`, clean | MIT | Match |
| tldraw | registry `5.3.0`; remote HEAD `6850000c4a2a...` | Custom source-available tldraw license, not an OSS license; production use prohibited without a separate license | Version match, license classification incomplete |
| diagrams.net | remote HEAD `3b4210678207...` | Apache-2.0 project license | Match |
| Structurizr | local `9ff16634c3b8...`, exact tag `v2026.06.28`, clean | Apache-2.0 | Match |
| Eclipse GLSP examples | local `9b5cc2fbc2ad...`, clean | EPL-2.0 with secondary GPL-2.0 plus Classpath Exception and secondary MIT choices | License row incomplete |
| ELK/elkjs | registry `0.12.0` | `EPL-2.0 OR GPL-3.0-or-later` | Match |
| QElectroTech | local HEAD matches `6d7b38c7a17b...` | Source headers are GPL-2.0-or-later | License row imprecise; worktree not pristine |

## Tier 0 - Freeze Blocker

### R-01 - Spine silently reverses the governing M43 rendering and page contract

- **Disposition:** Discuss and reconcile before finalization.
- **Evidence:** The PRD explicitly requires a live hybrid surface where semantic SVG owns selectable
  occurrence targets and Canvas owns dense routes/geometry (`prd.md:24-26`, `165-188`, `242`,
  `269-272`). It repeatedly requires a visible title block (`20-21`, `55-58`, `170-173`, `286-289`).
  The active widget implements that hybrid at
  `ide/theia-frontend/src/browser/athena-presentation-widget.tsx:112-133`, and the active product proof
  requires both `svgOccurrenceCount` and nonblank Canvas pixels at
  `ide/theia-product/scripts/verify-athena-m43-product-proof.js:100-102`.
- **Contradiction:** AD-9 instead selects one Konva Canvas adapter and explicitly retires independent
  DOM SVG occurrence paint plus Canvas route paint. AD-11 explicitly excludes a bottom title table.
  Both are marked `[ADOPTED]`, but neither governing input nor active proof was reconciled.
- **Risk:** Stories can be compliant with either the PRD or the spine, but not both. Acceptance tests
  will encode opposite renderer ownership. Keeping the old layer to satisfy PRD/proof would violate
  AD-9 and the no-fallback rule; deleting it would fail current M43 acceptance evidence.
- **Required closure:** Record the product decision once. If Konva-only and no title table are the
  adopted direction, update the PRD, success metrics, proof contract, and memlog before freezing the
  spine. If the PRD remains authoritative, revise AD-9/AD-11 and re-evaluate whether Konva is still
  the right first adapter. Do not retain both paths as compatibility.

## Tier 1 - High Risks

### R-02 - Replacement boundary does not name the active contracts that must die

- **Disposition:** Autofix the spine after R-01 is resolved.
- **Evidence:** Current LSP publishes raw Projection plus Spatial DTOs through
  `athena/projectionSession`. `AthenaProjectionDocumentPayload` exists independently as a Kotlin data
  class and a hand-written TypeScript type. The current widget calculates coordinate-frame metrics,
  row labels, occurrence SVG, and Canvas routes from those raw payloads. That is exactly the frontend
  reconstruction AD-5 and inherited M42 AD-33 prohibit.
- **Gap:** AD-5 and AD-16 state the desired invariant, but Structural Seed and closure rules do not
  explicitly retire `AthenaProjectionSessionProtocol.kt`, `AthenaProjectionPayloads.kt`, the
  hand-written bridge payloads/request, the hybrid widget paint path, or the old proof selectors.
- **Risk:** Separate backend, frontend, and proof stories can add `AthenaDiagramScene` while leaving
  the old request alive for convenience. That creates two transport/render authorities while still
  appearing to satisfy the new happy path.
- **Required closure:** Add a named replacement ledger: the canonical scene request/publication
  replaces the raw projection-session presentation payload; generated TypeScript replaces the
  duplicate type; current paint code and hybrid proof selectors are deleted or rewritten in the same
  story that activates Konva. Preserve a projection query only if a non-presentation consumer is
  identified and its boundary is explicitly independent.

### R-03 - Ajv is only a transitive lock entry despite a binding direct-dependency invariant

- **Disposition:** Autofix.
- **Evidence:** Inherited M42 AD-33 says Ajv is a direct frontend dependency and that no transitive
  dependency may supply the validation contract. `ide/theia-frontend/package.json` declares neither
  `ajv` nor `konva`; `ide/yarn.lock` contains Ajv 8.20.0 only under combined ranges
  `^8.0.0`, `^8.17.1`, `^8.6.3`, and `^8.9.0`. M43 Stack labels this merely `8.20.0 lockfile`.
- **Risk:** A Theia transitive upgrade can remove or move Ajv without any Athena manifest diff.
  Runtime schema validation would then depend on undeclared hoisting and violate inherited AD-33.
- **Required closure:** Bind `ajv: "8.20.0"` and `konva: "10.3.0"` as exact direct dependencies of
  `@engineeringood/athena-theia-frontend`; state that the generated scene validator and committed
  schema are packaged with that extension. Lockfile resolution alone is not a dependency contract.

### R-04 - `AssetRef` identifies an asset but does not define how any adapter obtains safe bytes

- **Disposition:** Discuss; then bind one compiler/runtime asset pipeline.
- **Evidence:** AD-12 gives `AssetRef`, digest, media kind, view bounds, and port anchors, and says
  package compilation admits a sanitized profile. AD-16 forbids network asset fetch. No rule defines
  canonical admitted bytes, secure parser settings, byte/decoded-size limits, runtime/LSP transfer,
  local URI handling, cache ownership, or adapter lookup. Current repository has no presentation
  asset service to ratify.
- **Risk:** Konva, SVG, and PNG implementers can independently choose absolute `file:` URLs, Theia
  file-service reads, data URLs, embedded bytes, or renderer-local parsing. Those choices differ in
  portability, CSP behavior, lifetime, memory, sanitization timing, and deterministic output. A
  blacklist of scripts/event handlers/`foreignObject` is not a complete SVG profile: URL-bearing
  CSS, `href`, entities, animation, filters, and oversized raster decode still need closed handling.
- **Required closure:** Choose one owner and payload. Recommended shape: compiler securely parses a
  closed SVG profile with DTD/external entity access disabled, validates all URL-bearing constructs,
  enforces encoded/decoded limits, emits canonical sanitized bytes keyed by digest, and publishes a
  revision-scoped asset bundle or digest resolver. Adapters receive only admitted bytes plus media
  facts; they never reopen package paths. Define cache eviction/blob URL revocation and font-byte
  delivery under the same boundary.

### R-05 - Edit command ownership and atomic undo protocol remain ambiguous across TS, LSP, and Kotlin

- **Disposition:** Discuss and make the process boundary explicit.
- **Evidence:** AD-10 names a sealed `DiagramEditCommand`, an application service, structured source
  edits, and an existing editor/workspace undo transaction. Structural Seed places interaction
  commands in TypeScript and a source-command protocol in LSP, but does not say where the sealed
  algebra is authoritative, which process resolves source spans, or what response Theia applies.
  Current code has formatting `TextEdit` support but no diagram mutation/`WorkspaceEdit` service.
- **Risk:** One story can parse and edit source in the frontend while another edits in Kotlin; both
  can claim the rule. Scene revision alone does not specify unsaved document version/digest
  preconditions. `ConnectPorts` also leaves "required authored relationship intent" untyped, so UI
  and compiler can disagree on relationship definition, roles, and target source.
- **Required closure:** Bind a generated discriminated command contract and one LSP/application
  owner for semantic resolution. Request must carry scene revision plus version/digest preconditions
  for every source document. Response must be either typed rejection/diagnostics or a versioned
  `WorkspaceEdit` applied as one Theia undo unit. Frontend must not parse or perform string surgery.
  Close the first `ConnectPorts` intent algebra or defer that command instead of leaving an open
  payload slot.

### R-06 - The 100,000-element scale gate records data but has no pass/fail budget

- **Disposition:** Discuss; either define a product budget or mark the fixture exploratory.
- **Evidence:** AD-14 requires a synthetic 100,000-element fixture and records load, settled memory,
  visible-node count, input latency, and frame-time percentiles. It admits Pixi only after Konva
  misses an "accepted product budget", but no budget, reference hardware, warm-up, interaction mix,
  node expansion ratio, or percentile threshold is defined. The PRD requires responsiveness only for
  the controlled rolling-shutter proof.
- **Framework reality:** Konva 10.3.0 provides delegated events, caching, hit canvases, and
  `batchDraw`, but it has no built-in viewport culler in its source. Event delegation reduces handler
  count, not hit-canvas or draw cost. Caching many nodes and high-DPI backing canvases can increase
  memory substantially.
- **Risk:** A run can record unusable results and still pass, or story teams can invent incompatible
  budgets. Conversely, making 100,000 a hidden acceptance threshold expands M43 beyond its PRD.
- **Required closure:** Define named hardware, Electron viewport/DPR, scene-to-Konva node mapping,
  visible percentage, gesture script, warm-up/sample count, and explicit load/memory/input/frame
  thresholds. Otherwise label this fixture non-gating research and gate M43 only on a quantified
  controlled page. The future Pixi trigger needs the same numeric budget.

## Tier 2 - Medium Risks

### R-07 - AD-9 can be read as applying device pixel ratio to layout fit

- **Disposition:** Autofix wording.
- **Evidence:** AD-9 says `ResizeObserver` plus `devicePixelRatio` fits page bounds. Konva already
  obtains device pixel ratio (`reference/konva/src/Canvas.ts:8,60`), sets CSS canvas width in logical
  pixels (`:121`), and scales its backing context (`:125,133`). Its own documentation states that it
  automatically handles pixel-ratio adjustments (`:97`).
- **Risk:** Multiplying Stage dimensions or page-fit scale by DPR duplicates Konva's backing-store
  scaling, producing wrong hit coordinates and excessive memory.
- **Required closure:** State that fit/pan/zoom uses host CSS pixels only. Konva owns backing-store
  DPR. DPR changes trigger backing canvas/cache refresh but never alter `SceneUnit` conversion or the
  viewport transform.

### R-08 - License fields are metadata placeholders or imprecise SPDX classifications

- **Disposition:** Autofix `UPSTREAM-ADOPTION.md`.
- **Evidence:** The exact tldraw license prohibits production use without a trial/commercial license,
  requires license-key enforcement, and permits compliance telemetry; `SEE LICENSE IN LICENSE.md`
  does not communicate that it is source-available and non-OSS. GLSP examples offer EPL-2.0 with
  secondary GPL-2.0 plus Classpath Exception and secondary MIT choices, not merely
  `EPL-2.0 / GPL-2.0`. QElectroTech source headers say version 2 **or later**, so
  `GPL-2.0-or-later` is more accurate than `GPL-2.0`.
- **Risk:** AD-15 promises explicit license classification, but a future reviewer cannot determine
  production eligibility or applicable choice from the ledger.
- **Required closure:** Record `LicenseRef-tldraw` (custom source-available; production prohibited
  without separate license), full GLSP alternatives/exceptions, and `GPL-2.0-or-later` for
  QElectroTech. Keep all three as idea-only/no-copy sources unless separately approved. Also record
  Theia's existing `EPL-2.0 OR GPL-2.0-only WITH Classpath-exception-2.0` notice obligations in the
  product's normal third-party license process; M43 need not upgrade it.

### R-09 - QElectroTech is not currently read-only pinned evidence on disk

- **Disposition:** Repair or explicitly qualify before relying on local-file evidence.
- **Evidence:** `reference/qelectrotech-source-mirror` reports the claimed HEAD
  `6d7b38c7a17b...`, but its worktree has 1,679 staged deletions and 41 untracked files. Other named
  local references are clean.
- **Risk:** Reviewers reading current files are not necessarily reading the pinned commit even though
  the Git object remains available. This contradicts AD-15's "read-only evidence" claim and makes
  future source/feature citations non-reproducible.
- **Required closure:** Restore/re-clone the reference outside this review or state that evidence is
  taken only through `git show <commit>:<path>`. Add a cleanliness check to upstream evidence refresh.
  Do not treat this dirty worktree as proof of current source shape.

### R-10 - PNG proof is drawn as an adapter but no implementation boundary selects its pixels

- **Disposition:** Clarify and align diagram, Structural Seed, and proof contract.
- **Evidence:** The architecture diagram names a `PNG Proof Adapter`; Structural Seed names only an
  SVG renderer. Current proof obtains an Electron window screenshot. Konva can also export its Stage,
  while a JVM/SVG rasterizer would be a third pixel path. AD-13 records environment metadata but does
  not choose which path produces the PNG.
- **Risk:** Teams can compare fundamentally different images while claiming one pinned proof. SVG
  canonical bytes can be a deterministic structural oracle, but rasterized SVG appearance still
  depends on the selected engine, fonts, and admitted assets.
- **Required closure:** If PNG means Electron E2E capture, call it a proof harness, pin Electron/
  Chromium, viewport, DPR, fonts, and capture timing, and remove the adapter implication. If it means
  scene export, add the adapter/module and keep it test-only because the PRD excludes product export.
  Keep SVG byte-golden proof separate from raster parity proof.

## Tier 3 - Low Risk

### R-11 - "Current" version wording obscures intentional brownfield pins

- **Disposition:** Autofix wording; no upgrade requested.
- **Evidence:** On review date npm reported Theia `1.74.0`, Electron `43.3.0`, React `19.2.8`, and
  TypeScript `7.0.2` as latest, while Athena intentionally resolves 1.73.1, 39.8.7, 18.3.1, and
  5.9.3. Konva 10.3.0, PixiJS 8.19.0, Excalidraw 0.18.1, tldraw 5.3.0, elkjs 0.12.0, and Ajv 8.20.0
  matched their registry latest tags.
- **Risk:** Readers can mistake "verified-current" for "latest" and either challenge valid pins or
  bundle unrelated framework upgrades into M43.
- **Required closure:** Label each Stack row `repository-pinned` or `registry-selected`, with the
  verification date. Preserve existing Theia/Electron/React/TypeScript pins for M43 unless a separate
  compatibility upgrade is approved.

## Positive Fit Findings

- Imperative Konva is a sound match for a Theia `ReactWidget` that supplies one host element. Avoiding
  `react-konva` prevents a second scene lifecycle and matches AD-9.
- Named Konva APIs are present at the pinned commit: Stage/Layer/Group/Shape, delegated events,
  hit-canvas behavior, node caching, and `batchDraw`.
- Konva 10.3.0 is MIT and reports no npm runtime dependency tree, so its direct licensing surface is
  small.
- Existing Theia 1.73.1 and React 18.3.1 peer requirements fit. M43 does not need a framework upgrade.
- The ledger correctly keeps PixiJS, Excalidraw, tldraw, GLSP, ELK, and QElectroTech out of M43
  runtime. That contains current copyleft/custom-license risk.
- AD-14 correctly refuses to claim EPLAN-scale readiness without measurements. The remaining problem
  is making the proposed measurement gate enforceable.

## Minimum Closure Before Finalization

1. Resolve R-01 and reconcile PRD, memlog, spine, and active product proof around one renderer/title
   contract.
2. Add the explicit legacy-presentation replacement/deletion ledger from R-02.
3. Bind Ajv and Konva as exact direct frontend dependencies and package generated schema validation.
4. Close asset byte delivery/sanitization and edit-command transaction boundaries.
5. Quantify the scale gate or demote the 100,000-element fixture to non-gating research.
6. Correct license classifications, qualify/repair QElectroTech evidence, and clarify DPR/PNG proof
   behavior.

