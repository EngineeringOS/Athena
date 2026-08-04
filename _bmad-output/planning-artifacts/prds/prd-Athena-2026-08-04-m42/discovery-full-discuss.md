# M42 Discovery Extract: FULL-DISCUSS

Source: `draft/20260803-confuse/FULL-DISCUSS.md` only. This source is a conceptual EPLAN course summary, not direct customer research. Requirements below preserve explicit source intent; M42-specific scope is inferred and must be confirmed.

## Product Intent

- Move engineering work from drawing primitives to explicit objects, properties, relationships, and flows. Geometry is a downstream projection, not authority. (lines 9-24, 45-64)
- Build Athena as a cross-domain Engineering Operating System, not another EPLAN or an electrical-only clone. (lines 398-428)
- Preserve one semantic engineering model across design, validation, documentation, manufacturing, installation, operation, and maintenance. (lines 343-357)
- Turn reusable, validated engineering knowledge into parameterized patterns that can generate consistent systems and documents. (lines 234-300)

## User Intent

- Engineers describe real entities, capabilities, relationships, and energy/control/signal/communication flows instead of manually drawing lines. (lines 30-43, 133-149)
- Engineers separate stable design intent from replaceable procurement choices. (lines 177-196)
- Engineering teams curate shared symbols, function definitions, device templates, parts, patterns, variants, and rules as governed knowledge. (lines 155-175, 234-280)
- Engineers instantiate proven patterns from requirements, validate constraints, then derive schematics, BOMs, terminal plans, cable lists, wire lists, and manufacturing data. (lines 215-230, 256-300)

## Product Requirements

1. M42 must provide an object-first knowledge model whose minimum concepts cover engineering model/workspace, entity, capability, implementation, relationship, information flow, engineering pattern, configuration, constraint, and projection. (lines 304-321)
2. M42 must keep `Device`/entity, `Function`/capability, `Part`/implementation, and `Symbol`/presentation distinct and explicitly related. (lines 68-114, 177-196)
3. M42 must support master-data concepts for symbol libraries, function definitions, device templates, and part catalogs without making any one vendor catalog kernel truth. (lines 155-175)
4. M42 must model a connection as an engineering relationship with endpoints and meaning such as signal, potential, and physical realization; a painted line alone is insufficient. (lines 133-149)
5. M42 must support stable engineering identity and organization across functional location, installation location, and device identity. (lines 118-131)
6. M42 must represent reusable engineering patterns as validated semantic units, not copied graphics or opaque template files. (lines 234-254)
7. M42 must support pattern configuration through variants and constraints, followed by deterministic instantiation, relationship generation, validation, and projection. (lines 256-300)
8. M42 must keep a domain-neutral semantic layer/IR ahead of domain-specific electrical, mechanical, software, and other projections. (lines 323-341)
9. M42 knowledge must remain independent of rendering. Reports, diagrams, BOMs, and other views are projections from shared facts. (lines 45-64, 304-341)

## Success Criteria

- One authoritative model can produce multiple consistent projections without re-entering engineering facts.
- Replacing a `Part` implementation leaves the declared `Function`/capability and surrounding design intent intact.
- A semantic connection remains queryable by endpoints, flow meaning, and physical implementation independent of its rendered path.
- A configured engineering pattern instantiates expected entities and relationships, fails on violated constraints, and records which pattern, configuration, and rules produced the result.
- Knowledge concepts can serve more than electrical engineering without weakening domain-specific validation.
- Generated schematics, BOMs, terminal plans, cable lists, wire lists, and manufacturing outputs agree on identity and relationships.

## Constraints

- Source authority chain: user intent -> semantic layer -> neutral IR -> domain processing -> projection -> rendering. (lines 323-341)
- Presentation cannot own engineering facts; symbols and pages are views of semantic objects. (lines 68-114, 335-340)
- Capability and implementation must remain separate; procurement substitution must not rewrite design intent. (lines 177-196)
- Pattern reuse must preserve validated engineering meaning, not merely visual composition. (lines 234-254)
- Core contracts must remain cross-domain and implementation-neutral. EPLAN terminology is evidence, not mandatory Athena syntax. (lines 304-341, 398-428)

## Risks

- Copying EPLAN's taxonomy directly could make Athena electrical-specific or vendor-shaped.
- Conflating entity, capability, implementation, and presentation would recreate drawing-tool ambiguity.
- Treating patterns as files or graphical blocks would lose provenance, validation, and semantic reuse.
- Broad lifecycle scope could exceed M42; knowledge capture, generation, governance, and every output format may need staged delivery.
- Cross-domain abstractions may become vague unless domain invariants and extension boundaries are explicit.
- Rule-driven generation can produce unsafe designs unless validation, explanation, provenance, and deterministic conflict handling are defined.
- Source does not specify knowledge ownership, versioning, review, approval, deprecation, or migration policy.
- Part catalogs create provenance, licensing, normalization, and vendor-lock risks not addressed by source.

## Rejected Directions

- Traditional CAD model where lines, circles, text, or pages are source of truth. (lines 45-64)
- Connection represented only as painted geometry. (lines 133-149)
- Symbol treated as device identity or engineering capability. (lines 88-114)
- Device intent permanently bound to one purchasable part. (lines 177-196)
- Macro treated as copy/paste, image, CAD block, or opaque template. (lines 234-254)
- Projection or renderer promoted above semantic authority. (lines 323-341)
- Athena positioned as another EPLAN rather than a cross-domain EngineeringOS. (lines 398-428)

## Unresolved Questions

1. Which knowledge capabilities belong in M42: vocabulary/modeling only, governed libraries, pattern instantiation, rule execution, or generated outputs?
2. What human-first Athena terms replace or retain `Device`, `Function`, `Part`, `Macro`, `Variant`, and `Rule`?
3. Which facts belong in domain-neutral kernel contracts, and which live in electrical/mechanical/software extensions?
4. What is the lifecycle for knowledge definitions: authoring, validation, approval, versioning, deprecation, and source trace?
5. How are pattern identity, parameters, versions, nested composition, overrides, and instance provenance represented?
6. How are conflicting rules prioritized and explained, and how does generation fail safely?
7. How are part catalogs imported and normalized while Athena source remains engineering authority?
8. What identity format replaces or generalizes EPLAN's function/location/device structure across domains?
9. Which first user workflow and example prove M42 without prematurely implementing M43-M45 projection or routing work?
10. What quantitative proof demonstrates reuse, determinism, substitution safety, cross-projection consistency, and cross-domain viability?
