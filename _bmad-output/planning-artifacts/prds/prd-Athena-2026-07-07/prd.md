---
title: Athena M3
status: draft
created: 2026-07-07
updated: 2026-07-07
---

# PRD: Athena M3

*Codename: Kernel Extensibility Proof.*

## 0. Document Purpose

This PRD defines the M3 product requirements for Athena after M0, M1, and M2 have all been completed in the current repository. It is written for founders, product owners, architecture owners, and developers who need one milestone boundary to govern the next implementation cycle.

M3 exists to prove the most important post-M2 architectural claim:

> Athena is not only a compiler and runtime for one engineering domain. It is a kernel platform that can host independent engineering domains through stable extension contracts.

This PRD is capability-first. Technical mechanism notes that are important but too implementation-shaped for the PRD are captured in [`addendum.md`](addendum.md).

## 1. Vision

M0 proved that Athena can compile authored DSL into canonical semantic truth and deterministic downstream artifacts. M1 proved that runtime can own active projects, commands, history, diff, and plugin-hosted extension. M2 proved that one canonical semantic source can derive explicit layout, explicit geometry, and multi-view projection without giving semantic authority to UI or backend state.

M3 must now prove the next platform boundary: a domain can be added, validated, compiled, and rendered through plugin contracts rather than through kernel-owned domain knowledge. The first proof is not "Electrical becomes bigger." The first proof is "the Athena kernel remains small, stable, and domain-agnostic while domains evolve outside it."

If M3 succeeds, Athena stops being interpreted as a specialized electrical compiler with extension hooks and becomes a governed engineering compiler platform.

## 1.1 Why Now

The current technical risk is no longer whether Athena can own semantics, runtime, layout, or geometry. Those milestone claims already have working proof in the repository.

The next risk is whether those kernel layers can remain stable when domain variety grows. If every new engineering domain requires kernel edits, Athena is still a product-specific compiler. If new domains can be introduced through stable SPI, domain-owned semantics, and domain-owned renderer contributions, Athena becomes a real platform.

That is why M3 comes before broader UI/UX expansion, richer workbench flows, or AI features.

## 2. Target User

### 2.1 Jobs To Be Done

- Platform engineers need a small kernel that can host multiple engineering domains without domain logic leaking back into kernel modules.
- Extension authors need stable contracts to declare domain entities, properties, ports, validation rules, compiler contributions, and render contributions.
- Founders need architectural proof that Athena can support more than one domain family without restarting the kernel design for each one.
- Future product teams need confidence that adding new domains such as hydraulic, pneumatic, or process will primarily be extension work rather than kernel surgery.

### 2.2 Non-Users (M3)

- Teams expecting M3 to deliver a full Studio shell or richer operator UX
- Teams expecting dynamic remote plugin marketplaces to be production-ready in this phase
- Teams expecting broad real-world electrical coverage beyond the minimum proof set
- Teams expecting M3 to prioritize new runtime editing features over extension proof

### 2.3 Key User Journeys

- **UJ-1. Aaron adds a new engineering domain without changing kernel domain logic.**
  - **Persona + context:** Aaron is evaluating whether Athena can become an EngineeringOS platform rather than remain tied to one domain.
  - **Entry state:** The kernel builds and runs with the current stable modules from M0 to M2.
  - **Path:** Aaron enables an external domain plugin, the kernel discovers it, the compiler and runtime accept its declared contributions, and domain entities participate in semantic compilation and downstream rendering.
  - **Climax:** Aaron confirms that the new domain behavior came from plugin contracts and contributions rather than from fresh kernel-owned domain code.
  - **Resolution:** Aaron can explain Athena as a stable kernel plus domain extensions.

- **UJ-2. Maya proves that removing one domain does not break the kernel.**
  - **Persona + context:** Maya is checking whether Athena is actually decoupled or only pretending to be extensible.
  - **Entry state:** One or more domain plugins exist.
  - **Path:** Maya builds and tests the kernel with no domain plugin, then with one plugin, then with more than one plugin.
  - **Climax:** The kernel still builds, and domain-specific capabilities appear only when the corresponding plugin is present.
  - **Resolution:** Maya trusts that domain semantics are optional hosted contributions, not hidden kernel dependencies.

- **UJ-3. Priya implements a minimal new proof plugin to validate the SPI.**
  - **Persona + context:** Priya is extending Athena and wants a low-cost way to verify that the extension surface is real.
  - **Entry state:** The SPI has been stabilized and documented.
  - **Path:** Priya creates a minimal `domain-dummy` plugin with synthetic entities and validators, plugs it into the hosted environment, and runs the same extension test suite.
  - **Climax:** The dummy plugin compiles and participates in the pipeline without requiring kernel changes.
  - **Resolution:** Priya treats the SPI as a real product contract rather than an electrical-only convenience layer.

## 3. Glossary

- **Kernel** - The stable core substrate under `kernel/` that owns generic language, canonical semantic model, validation framework, compiler orchestration, runtime orchestration, and downstream model boundaries.
- **Extension SPI** - The stable plugin contract surface that lets domains contribute semantic declarations, validation logic, compiler contributions, and renderer contributions without changing kernel-owned rules.
- **Domain Plugin** - A plugin hosted by Athena that contributes domain-specific meaning through approved extension contracts.
- **Domain Schema** - Plugin-declared domain types, property definitions, port definitions, and related semantic descriptors consumed by the generic kernel pipeline.
- **Compiler Pass Pipeline** - The explicit ordered pass model through which Athena executes parsing, lowering, validation, optimization, projection, and backend work.
- **Hosted Plugin Set** - The approved plugins discovered and loaded by the JVM-first host for a given execution environment.
- **Electrical Proof Domain** - The first real domain plugin used by M3 to prove extensibility with a deliberately small supported vocabulary.
- **Dummy Proof Domain** - A second synthetic domain plugin used only to prove that the SPI is generic rather than electrical-specific.

## 4. Features

### 4.1 Stable Kernel Extension SPI

**Description:** Athena must define and freeze the first stable extension contracts needed to host domain semantics outside the kernel. Realizes UJ-1, UJ-3.

#### FR-1: Support Plugin-Declared Domain Schema

Athena can host plugin-declared domain schema for entities, properties, ports, and related domain capabilities. Realizes UJ-1, UJ-3.

**Consequences (testable):**
- The kernel can consume plugin-declared domain schema without introducing kernel-owned knowledge of `Motor`, `Lamp`, `Switch`, `Wire`, or dummy proof entities.
- Domain schema remains declared through approved plugin contracts rather than ad hoc runtime reflection.
- The kernel parser and canonical semantic pipeline remain generic while plugin-owned schema supplies domain meaning.

#### FR-2: Support Plugin Registration Through Stable Contracts

Athena can register plugin contributions for domain semantics, validation, compiler participation, and renderer contribution through stable contracts. Realizes UJ-1.

**Consequences (testable):**
- Plugin contracts are typed and inspectable.
- The hosted environment can report which contributions each plugin exposes.
- The kernel can reject invalid or overreaching plugins before use.

#### FR-3: Support Plugin Lifecycle In The Hosted Environment

Athena can discover, initialize, inspect, and shut down hosted plugins in a governed way. Realizes UJ-1, UJ-2.

**Consequences (testable):**
- The current JVM-first hosted environment can discover approved plugins through the existing ServiceLoader-based proof path.
- Plugin lifecycle state is inspectable by runtime and tests.
- Future dynamic loading is not required for M3 success, but M3 must not block it architecturally.

### 4.2 Domain-Agnostic Kernel Compilation

**Description:** Athena must prove that domain meaning can enter compilation without making the kernel domain-specific. Realizes UJ-1, UJ-2.

#### FR-4: Keep The Kernel Parser And Core Semantics Generic

Athena keeps parsing and core semantic ownership generic while allowing plugins to provide domain meaning through declared schema and passes. Realizes UJ-1.

**Consequences (testable):**
- The kernel does not embed domain-specific parser branches for individual proof entities.
- The authored DSL remains the source of truth, but domain interpretation is enabled by plugin-owned declarations rather than hard-coded kernel types.
- Removing one domain plugin removes its meaning without destabilizing generic kernel compilation.

#### FR-5: Expose An Explicit Compiler Pass Pipeline

Athena exposes compilation as an explicit pass pipeline rather than one opaque compiler blob. Realizes UJ-1, UJ-3.

**Consequences (testable):**
- Core phases such as parsing, lowering, validation, projection, and backend derivation are represented as explicit passes or pass groups.
- Plugin contributions can participate through governed pass insertion or contribution points.
- The pass pipeline remains deterministic for the same inputs and hosted plugin set.

#### FR-6: Allow Domain Plugins To Contribute Semantic Compilation Behavior

Athena can let a domain plugin contribute domain semantics to lowering or later semantic passes without ceding kernel ownership of canonical model rules. Realizes UJ-1.

**Consequences (testable):**
- The kernel remains owner of canonical semantic contracts and orchestration.
- Domain plugins can contribute meaning needed to interpret their schema and relationships.
- Adding a second domain plugin does not require editing kernel code to recognize that domain.

### 4.3 Domain-Owned Validation

**Description:** M3 must separate kernel-owned generic validation from domain-owned engineering validation. Realizes UJ-1, UJ-2.

#### FR-7: Preserve Kernel-Owned Generic Validation

Athena continues to own generic validation such as duplicate identifiers, missing references, and invalid generic graph structure. Realizes UJ-2.

**Consequences (testable):**
- Generic invariants remain enforced even when no domain plugin is present.
- Generic validation rules remain domain-agnostic.
- Removing a domain plugin does not remove generic kernel validation.

#### FR-8: Support Plugin-Owned Domain Validation

Athena can host plugin-owned validation rules for domain-specific constraints. Realizes UJ-1, UJ-3.

**Consequences (testable):**
- The electrical proof domain can validate electrical-specific rules outside the kernel.
- The dummy proof domain can validate synthetic domain rules outside the kernel.
- Domain validation disappears when the corresponding plugin is removed, while kernel validation remains.

### 4.4 Domain-Owned Rendering Contributions

**Description:** Athena must prove that renderer contribution can remain generic in the kernel while domains provide their own visual meaning. Realizes UJ-1.

#### FR-9: Keep Kernel Renderer Contracts Generic

Athena keeps renderer contracts and backend orchestration generic. Realizes UJ-1.

**Consequences (testable):**
- Kernel renderer modules do not import proof-domain implementation classes.
- Downstream rendering still operates through governed kernel-owned models and contracts.
- Backend behavior remains deterministic for the same semantic and plugin inputs.

#### FR-10: Support Plugin-Contributed Domain Rendering

Athena can host plugin-contributed rendering behavior for domain-specific symbols or visual consequences. Realizes UJ-1.

**Consequences (testable):**
- The electrical proof domain can contribute rendering behavior for the supported proof entities.
- Domain rendering can be disabled by removing the plugin.
- The renderer path remains downstream of canonical semantics and projection layers.

### 4.5 First External Domain Proofs

**Description:** M3 must ship at least one real proof domain and one synthetic proof domain. Realizes UJ-1, UJ-2, UJ-3.

#### FR-11: Refactor The Existing Electrical Extension Into The M3 Proof Shape

Athena refactors the existing `domain-electrical` extension so it proves the stable M3 SPI rather than bypassing it. Realizes UJ-1, UJ-2.

**Consequences (testable):**
- The supported M3 proof set for electrical remains intentionally small: `Motor`, `Lamp`, `Switch`, `Wire`.
- The electrical extension proves hosted schema, validation, and rendering through the stable contracts.
- The M3 electrical proof does not attempt broad real-world electrical coverage.

#### FR-12: Provide A Second Synthetic Proof Plugin

Athena provides a minimal `domain-dummy` plugin to prove the SPI is not electrical-specific. Realizes UJ-2, UJ-3.

**Consequences (testable):**
- The dummy plugin contributes synthetic entities and rules with no engineering meaning.
- The dummy plugin can be discovered, validated, compiled, and tested through the same hosted contracts.
- The dummy proof exists purely to validate platform generality.

### 4.6 Extensibility Test Proof

**Description:** M3 must prove extensibility through repeatable build and test combinations, not only through code structure claims. Realizes UJ-2, UJ-3.

#### FR-13: Verify The Kernel With Zero, One, And Multiple Plugins

Athena can verify kernel behavior across different hosted plugin sets. Realizes UJ-2.

**Consequences (testable):**
- The kernel builds and passes its relevant verification path with zero domain plugins.
- The kernel builds and passes its relevant verification path with the electrical proof plugin.
- The kernel builds and passes its relevant verification path with the dummy proof plugin.
- The kernel builds and passes its relevant verification path with both proof plugins hosted together.

#### FR-14: Prove New Domain Introduction Does Not Require Fresh Kernel Domain Code

Athena can add a new proof domain after the SPI freeze without requiring further kernel domain edits. Realizes UJ-1, UJ-3.

**Consequences (testable):**
- The milestone establishes a clear post-freeze boundary: once the SPI and pass pipeline are stabilized, adding another domain is extension work.
- Tests and documentation make the boundary visible rather than implied.
- Regressions that reintroduce kernel-owned domain logic can be detected.

## 5. Non-Goals (Explicit)

- M3 does not deliver richer Studio UX, emotional design systems, or advanced workbench polish.
- M3 does not deliver production-ready dynamic install, remote download, hot load, or hot unload of plugins.
- M3 does not attempt full industry-grade electrical coverage.
- M3 does not replace the authored DSL as the source of truth.
- M3 does not make plugin code sovereign over canonical semantic ownership, runtime ownership, or backend orchestration.
- M3 does not broaden AI capability scope.

## 6. MVP Scope

### 6.1 In Scope

- stable extension SPI for domain schema, validation, compiler contribution, and rendering contribution
- explicit compiler pass pipeline suitable for governed plugin participation
- ServiceLoader-based hosted plugin proof on the JVM-first path
- refactoring of the current `domain-electrical` extension into the stable M3 proof shape
- minimal electrical proof vocabulary: `Motor`, `Lamp`, `Switch`, `Wire`
- minimal synthetic `domain-dummy` proof plugin
- verification matrix for zero, one, and multiple proof plugins

### 6.2 Out Of Scope For MVP

- plugin marketplace distribution
- dynamic remote plugin fetching
- hot reload or hot unload at runtime
- broad domain authoring DSL expansion beyond what the proof requires
- new end-user editing workflows unrelated to the extensibility proof

## 7. Success Metrics

**Primary**

- **SM-1:** After the M3 SPI and pass pipeline are stabilized, the kernel can host the refactored electrical proof domain without fresh kernel domain edits.
- **SM-2:** The kernel can build and verify with zero domain plugins, with `domain-electrical`, with `domain-dummy`, and with both together.
- **SM-3:** Domain-specific validation and rendering behavior appear only when the corresponding plugin is hosted.

**Secondary**

- **SM-4:** Hosted plugin inspection can show what each plugin contributes through the approved contracts.
- **SM-5:** The compiler structure is visibly pass-oriented rather than a monolithic domain-aware blob.

**Counter-metrics**

- **SM-C1:** Do not optimize for plugin loading sophistication over proof of stable kernel boundaries.
- **SM-C2:** Do not optimize for domain breadth over proof of platform extensibility.

## 8. Cross-Cutting NFRs

- **NFR-1 Small Kernel:** Kernel modules remain generic and domain-agnostic in responsibility.
- **NFR-2 Determinism:** The same authored inputs, hosted plugin set, and pass ordering produce the same outcomes.
- **NFR-3 Inspectability:** Hosted plugin inventory, contribution points, and pass participation are visible enough to debug and govern.
- **NFR-4 JVM-First Delivery:** M3 proves extensibility on the Java 25 JVM-first path before broader dynamic or cross-platform plugin delivery.
- **NFR-5 Forward Compatibility:** M3 must not architecturally block future local-directory or remote-URL plugin loading, even though that is out of scope for this milestone.

## 9. Constraints And Guardrails

### 9.1 Kernel Guardrails

- Kernel modules must not gain direct ownership of proof-domain entities such as `Motor`, `Lamp`, `Switch`, `Wire`, or dummy equivalents.
- Kernel remains owner of generic language, canonical semantic contracts, pass orchestration, runtime orchestration, and backend orchestration.
- Existing M0, M1, and M2 milestone guarantees remain intact.

### 9.2 Plugin Guardrails

- Plugins contribute through approved contracts only.
- Plugins may extend domain meaning, validation, and rendering, but they do not replace kernel ownership of canonical semantic truth.
- Plugins that violate contract boundaries should be rejected before hosted use.

### 9.3 Roadmap Guardrails

- M3 is an extensibility milestone, not a Studio milestone.
- Runtime or UI work is only in scope where needed to prove hosted extensibility or inspectability.
- Dynamic plugin loading remains future-facing architecture, not M3 delivery scope.

## 10. Platform And Delivery

- **Primary platform:** JVM desktop and CLI proof path
- **Plugin loading proof:** ServiceLoader-based hosted discovery in the current local environment
- **Existing proof extension to refactor:** `:extensions:domain-electrical`
- **New synthetic proof extension:** `:extensions:domain-dummy`
- **Primary verification posture:** Java 25, sequential Gradle verification on Windows

## 11. Open Questions

1. Should the minimal M3 electrical vocabulary appear as dedicated authored DSL forms such as `motor M1`, or as generic authored forms whose domain meaning is supplied entirely by plugin schema?
2. Which pass insertion model best balances SPI stability and kernel simplicity: named pass stages, typed contribution points, or a mixed model?
3. How much plugin metadata and inspection output should be surfaced to operators versus kept developer-facing only?

## 12. Assumptions Index

- M3 is allowed to refactor kernel contracts once in order to reach a stable extensibility boundary; the proof obligation applies after that boundary is established.
- The current ServiceLoader-based hosted discovery path is sufficient for the first proof, while future dynamic loading remains intentionally deferred.
- The current `domain-electrical` extension is the first real proof domain and should be aligned to the new stable SPI rather than replaced with a separate parallel implementation.
