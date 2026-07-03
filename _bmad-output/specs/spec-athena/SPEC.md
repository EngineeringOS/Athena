---
id: SPEC-athena
companions:
  - glossary.md
  - ../planning-artifacts/architecture/architecture-Athena-2026-07-02/ARCHITECTURE-SPINE.md
sources:
  - ../planning-artifacts/prds/prd-Athena-2026-07-02/prd.md
  - ../planning-artifacts/prds/prd-Athena-2026-07-02/addendum.md
---

> **Canonical contract.** This SPEC and the files in `companions:` are the complete, preservation-validated contract for what to build, test, and validate. Source documents listed in frontmatter are for traceability only.

# Athena M0 Semantic Compiler Proof

## Why

Athena exists to prove the EngineeringOS claim that engineering meaning can become the source of truth above drawings, vendor files, and tool-shaped models. The immediate force is a vision to realize and a thesis to test: if a minimal Electrical/Runtime language can compile into a stable semantic model and produce deterministic downstream output, then the platform's core architectural bet is real rather than rhetorical.

## Capabilities

- **CAP-1**
  - **intent:** An author can express a minimal Electrical/Runtime project in `Engineering Language` without encoding drawing mechanics or target-specific file structures.
  - **success:** A valid source file using the M0 keyword set can be parsed into AST and accepted as the canonical source input for compilation.

- **CAP-2**
  - **intent:** The system can normalize authored input into a stable `Engineering IR` through semantic validation of references, types, ports, and connections.
  - **success:** For each valid example, compilation produces one canonical `Engineering IR` shape with stable semantic identity; for each invalid example, diagnostics identify the semantic failure explicitly.

- **CAP-3**
  - **intent:** The compiler can execute deterministic validation and rule logic on `Engineering IR` as the only semantic authority.
  - **success:** Re-running the same input with the same governed contracts yields the same validation and rule outcomes, and no rule execution depends on AST or renderer internals.

- **CAP-4**
  - **intent:** The system can derive a simple `SVG` diagram from compiled semantics without making rendering the source of truth.
  - **success:** Each conforming example produces the expected class of `SVG` output from a render-facing model derived from `Engineering IR`, with no semantic recovery in the renderer.

- **CAP-5**
  - **intent:** The proof substrate can load real extensions through typed local plugin contracts.
  - **success:** Domain, renderer, and rule extension points can be discovered from local manifest-declared classpath plugins, validated for compatibility, and executed without redefining core semantic contracts.

- **CAP-6**
  - **intent:** The example set can serve as the conformance truth set for the language and compiler proof.
  - **success:** `5-10` examples under `examples/` each carry stable expectations for semantic validation, `Engineering IR` shape, and `SVG` output class, and those expectations can be checked automatically.

## Constraints

- M0 is JVM-first, local, and single-process.
- The implementation substrate is Kotlin/JVM with Java 25 LTS, Kotlin 2.4.0, and Gradle 9.6.1 as the pinned greenfield seed.
- The semantic core stays general; Electrical/Runtime is the first domain extension rather than the permanent core vocabulary.
- `Engineering IR` is the first and only canonical semantic authority in M0.
- Semantic truth, view truth, and render output remain separated; a full durable `Layout IR` may be deferred, but the separation rule is binding now.
- Plugins are typed, discovered locally, and non-sovereign: they extend core-owned contracts and may not redefine them.
- `AutomationML` is a standards and ontology reference concept only for M0, not an implementation target and not an internal model substitute.

## Non-goals

- AI-assisted authoring or knowledge workflows in M0
- `OPC UA`, cloud, enterprise, or product-shell concerns in M0
- A CAD-like visual authoring application
- Distributed or service-shaped decomposition
- Remote plugin distribution, hot loading, or marketplace mechanics

## Success signal

Athena M0 is successful when a reviewer can take any conformance example, trace it from `Engineering Language` through AST, semantic validation, canonical `Engineering IR`, rule execution, and simple `SVG`, and see that the DSL is the source of truth, the IR is the canonical model, and everything else behaves like a backend around that model.

## Assumptions

- The first M0 keyword set can stay within a minimal Electrical/Runtime language of roughly `20-30` core keywords.
- A simple render-facing model is sufficient for M0 before a first-class durable `Layout IR` becomes necessary.

## Open Questions

- Which exact M0 keyword set belongs in the first Electrical/Runtime language cut?
- Which rule extension point, if any, must be implemented in M0 beyond the built-in semantic validation path?
