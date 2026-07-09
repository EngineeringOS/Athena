---
title: Athena M4
status: draft
created: 2026-07-08
updated: 2026-07-08
---

# PRD: Athena M4

*Codename: Athena Theia Platform Proof.*

## 0. Document Purpose

This PRD defines the M4 product requirements for Athena after the completed M0 to M3 milestone sequence. It is written for founders, product owners, architecture owners, and developers who need one narrow milestone boundary for the next implementation cycle.

M4 exists to prove the first serious Athena IDE platform boundary:

> Athena is not only a semantic kernel plus runtime. It can also ship as a custom Theia-based engineering product shell that hosts Athena language tooling, repository opening, and a professional workbench foundation without moving semantic authority back into UI state.

This PRD is capability-first. It builds on the current workspace summary in `docs/usages/athena-workspace-summary.md`, the finished M3 proof usage in `docs/usages/m3-proof-usage.md`, the earlier M4 discussion in `draft/m4/001-draft.md`, the GLSP-related patch in `draft/m4/002-glsp.md`, and the manifesto technology and architecture chapters around `LSP`, `Studio`, `Engineering IR`, and plugin governance. Implementation-shaped details that are too mechanism-heavy for the PRD are captured in [`addendum.md`](addendum.md).

## 1. Vision

M0 proved the authored DSL to canonical semantic model thesis. M1 proved runtime-hosted project state, command execution, history, diff, and application hosting. M2 proved explicit layout and geometry boundaries. M3 proved that the kernel can host real external domains through stable plugin contracts.

M4 must now prove the next layer above that stack: Athena can become a real engineering development environment rather than remain only a compiler, runtime, and proof viewer. The first product shell for that claim must be based on Eclipse Theia, not on a VS Code extension mindset and not on a one-off custom desktop shell. Athena should become a Theia-based product that bundles the Athena language, curated platform capabilities, and an engineering workbench shaped around semantic workflows.

The milestone must stay deliberately narrow. M4 is not the package-management milestone, not the semantic-SCM milestone, and not the graphical projection milestone. Its job is to prove the product shell, repository session, language-service integration, and workbench foundation that later milestones can build on.

M4 must also preserve a future graphical semantic-projection path under the same Theia product boundary. Text authoring is proven now through Athena LSP; later graph or diagram surfaces must remain downstream of canonical semantic state rather than becoming a second source of truth.

## 1.1 Why Now

The current technical risk is no longer whether Athena can hold semantic truth, runtime state, layout projection, geometry projection, or hosted domain extensions. Those claims already have proof in the repository.

The next risk is product embodiment. If Athena cannot become a serious engineering IDE shell, the semantic platform remains difficult to adopt. If Athena can boot as a custom Theia-based product with language tooling, repository lifecycle, and workbench composition while keeping semantic authority in kernel and runtime layers, the platform becomes operationally credible.

That is why M4 comes before package-graph resolution, semantic SCM, richer UI emotion systems, broader AI workflows, or any graphical projection server implementation. M4 should leave a clean attachment point for that next layer without trying to solve it now.

## 2. Target User

### 2.1 Jobs To Be Done

- Platform engineers need Athena to run as a real IDE product shell rather than as a collection of CLI and proof modules.
- Language-tooling engineers need a stable place to host diagnostics, completion, navigation, and semantic inspection through Athena language services.
- Founders need proof that the EngineeringOS thesis can become a professional developer tool and not only a semantic backend.
- Future product teams need a Theia-first workbench foundation that can host richer engineering workflows without turning UI state into semantic truth.

### 2.2 Non-Users (M4)

- Teams expecting M4 to deliver a full package registry, dependency resolver, or final `athena.lock` workflow
- Teams expecting M4 to deliver semantic SCM, publish flows, or review workflows above Git
- Teams expecting M4 to deliver the final Athena visual language, emotion system, or advanced UX polish
- Teams expecting M4 to deliver a graphical editor, diagram canvas, or GLSP-class graph service in this phase
- Teams expecting M4 to replace the kernel or runtime as the semantic authority
- Teams expecting browser-first delivery in this phase

### 2.3 Key User Journeys

- **UJ-1. Aaron opens Athena and gets a real engineering IDE shell instead of a toy viewer.**
  - **Persona + context:** Aaron is testing whether Athena is becoming a professional engineering developer tool.
  - **Entry state:** Athena is installed locally on the desktop-first proof path.
  - **Path:** Aaron launches Athena, sees a branded welcome experience, opens an Athena repository, and lands in a workbench with editor, repository tree, diagnostics, and semantic panels.
  - **Climax:** Aaron can explain the product as a real Theia-based workbench for engineering authoring and inspection rather than as a Compose proof app.
  - **Resolution:** Athena becomes credible as the host shell for later repository, package, and SCM milestones.

- **UJ-2. Maya edits Athena source and gets serious language tooling inside the workbench.**
  - **Persona + context:** Maya is validating whether Athena language authoring can behave like a first-class programmable environment.
  - **Entry state:** An Athena repository is open in the Theia-based product shell.
  - **Path:** Maya opens a source file, edits authored DSL, receives diagnostics, triggers completion or navigation, and inspects semantic information without leaving the workbench.
  - **Climax:** Athena language tooling is clearly hosted by Athena services inside the Theia shell rather than bolted on as an afterthought.
  - **Resolution:** Maya can treat Athena as the beginning of a professional engineering IDE.

- **UJ-3. Priya confirms that workbench behavior stays downstream of semantic authority.**
  - **Persona + context:** Priya is checking for architectural drift before more UI investment happens.
  - **Entry state:** Athena is running as the M4 Theia product.
  - **Path:** Priya opens a repository, edits source, observes diagnostics and semantic views update, and confirms that workbench state is driven by runtime and language-service boundaries instead of by hidden UI-owned models.
  - **Climax:** Priya can trace the product shell back to kernel and runtime boundaries without finding a second semantic authority in the frontend.
  - **Resolution:** Priya approves M4 as a safe foundation for later platform growth.

## 3. Glossary

- **Engineering Repository** - The root directory Athena opens as the primary unit of local engineering work. It contains authored source and Athena-owned local state for the active product shell. In M4 this is a repository-shaped project root, not yet the full M5 package graph contract.
- **Athena Theia Product** - The custom Athena application built on Eclipse Theia and distributed as the primary M4 IDE shell.
- **Workbench** - The user-facing arrangement of editor, navigation, diagnostics, semantic inspection, console, and related panels inside the Athena Theia Product.
- **Repository Session** - The active runtime-backed session created when Athena opens or creates one Engineering Repository in the workbench.
- **Athena LSP** - The Athena language-service boundary exposed to the Theia client for diagnostics, completion, navigation, and semantic inspection.
- **Semantic Inspection** - Read-only user-facing inspection of canonical semantic state or derived diagnostics without granting the UI its own semantic authority.
- **Graphical Projection** - A future downstream view derived from canonical semantic state and projection metadata. It is not semantic truth and is not implemented in M4.
- **Bundled Capability Set** - The curated Theia capabilities, Athena-owned integrations, and preinstalled extensions that ship inside the Athena Theia Product.
- **Desktop-First Proof Path** - The primary delivery posture for M4 in which Athena is packaged and run first as a desktop product, while richer web delivery remains deferred.

## 4. Features

### 4.1 Athena Theia Product Shell

**Description:** Athena must ship a custom Theia-based product shell, not only a plugin or an embedded demo surface. Realizes UJ-1, UJ-3.

#### FR-1: Launch A Custom Athena Theia Product

Athena can launch as a branded custom Theia-based product shell on the desktop-first proof path. Realizes UJ-1.

**Consequences (testable):**
- Athena boots into a custom product shell that is clearly Athena-owned rather than a generic upstream Theia demo.
- The shell can be packaged and launched locally through a deterministic development and verification path.
- The product identity, menus, and core workbench entry flow are controlled by Athena.

#### FR-2: Bundle A Curated Capability Set

Athena can ship with a preselected bundled capability set appropriate for the M4 proof. Realizes UJ-1.

**Consequences (testable):**
- The M4 shell includes only the capabilities needed for repository opening, Athena editing, diagnostics, semantic inspection, and basic workbench operation.
- Bundled capabilities are curated by Athena rather than delegated to ad hoc end-user extension installation.
- Athena remains Theia-based without being reduced to "just another extension."

#### FR-3: Keep The Product Shell Downstream Of Kernel And Runtime Authority

Athena keeps the Theia product shell downstream of runtime and semantic authority. Realizes UJ-3.

**Consequences (testable):**
- Workbench state never becomes the canonical semantic model.
- Semantic mutations, diagnostics, and inspection results continue to flow through kernel and runtime boundaries.
- UI composition changes do not redefine engineering meaning.

### 4.2 Engineering Repository Session

**Description:** M4 must make Engineering Repository opening and creation real inside the new product shell. Realizes UJ-1, UJ-3.

#### FR-4: Open An Existing Engineering Repository

Athena can open an existing Engineering Repository into an active Repository Session. Realizes UJ-1, UJ-3.

**Consequences (testable):**
- The workbench can open a repository root and activate it as the current Repository Session.
- The opened repository exposes authored source files and repository-local Athena state needed for the M4 shell.
- Repository opening remains compatible with the current Athena source conventions. `[ASSUMPTION: formal manifest and lockfile contracts are deferred to M5, so M4 may use a lighter repository bootstrap shape.]`

#### FR-5: Create A New Engineering Repository

Athena can create a new Engineering Repository through the workbench. Realizes UJ-1.

**Consequences (testable):**
- The product shell can create a minimal repository root suitable for immediate Athena editing.
- The new repository contains the minimum initial structure required by the M4 proof path.
- Repository creation does not prematurely hard-code the final M5 package or dependency contract. `[ASSUMPTION: the initial structure may be intentionally small and evolve in M5.]`

#### FR-6: Maintain A Runtime-Backed Repository Session

Athena can keep one active Repository Session aligned with runtime ownership while the workbench is open. Realizes UJ-3.

**Consequences (testable):**
- Repository lifecycle remains explicit and inspectable.
- The active session can be queried by Athena workbench features without duplicating canonical project state in frontend-only models.
- Closing or switching repositories follows product-managed session rules instead of ad hoc UI state mutation.

### 4.3 Athena Language Service Integration

**Description:** Athena must prove that serious language tooling can live inside the Theia shell through the Athena LSP boundary. Realizes UJ-2, UJ-3.

#### FR-7: Provide Diagnostics In The Workbench Through Athena LSP

Athena can surface authored-source diagnostics in the workbench through Athena LSP. Realizes UJ-2.

**Consequences (testable):**
- Editing a source file updates diagnostics visible in the editor and Problems-style workbench surfaces.
- Diagnostics remain derived from Athena-owned parsing, semantic analysis, and validation boundaries.
- The Theia client does not invent an independent diagnostic engine.

#### FR-8: Provide Core Language Navigation And Editing Support

Athena can provide core language authoring support through Athena LSP. Realizes UJ-2.

**Consequences (testable):**
- The M4 shell supports at least the minimum serious language features required for a professional proof, such as completion, document symbols, go-to-definition, or references. `[ASSUMPTION: the exact minimum feature set will be finalized in architecture and story planning.]`
- Language-service behavior is available inside the Theia workbench rather than only through CLI tooling.
- The product shell can grow richer language behavior later without changing the semantic authority boundary.

#### FR-9: Use An Incremental Semantic Service Model Rather Than UI-Owned AST State

Athena can host language tooling on top of Athena-owned incremental semantic service state rather than on direct UI-owned AST authority. Realizes UJ-2, UJ-3.

**Consequences (testable):**
- The long-lived language-service substrate is semantic-service-oriented and stable enough for navigation, diagnostics, and inspection.
- Short-lived syntax structures may still exist, but they do not become the frontdoor product contract for professional tooling.
- M4 creates a safe foundation for later refactoring, search, and semantic review work.

### 4.4 Professional Workbench Foundation

**Description:** M4 must prove a serious engineering workbench layout and interaction baseline without claiming final UX maturity or pretending to deliver the later graphical projection layer. Realizes UJ-1, UJ-2.

#### FR-10: Provide A Multi-Panel Workbench Baseline

Athena can provide a professional baseline workbench layout for engineering authoring and inspection. Realizes UJ-1, UJ-2.

**Consequences (testable):**
- The product shell includes an editor area plus repository navigation and diagnostics-facing panels.
- The workbench can host semantic inspection, console, and future engineering-facing panes through deliberate panel boundaries.
- The workbench shape leaves room for later graphical semantic-projection surfaces under the same product shell without moving semantic truth into canvas state.
- The baseline feels structurally like a serious IDE, even though later milestones will deepen the UX.

#### FR-11: Support Basic Command And View Orchestration

Athena can route core workbench commands and views through explicit product boundaries. Realizes UJ-1.

**Consequences (testable):**
- Core actions such as open repository, create repository, reveal diagnostics, and invoke Athena-facing commands are available through intentional workbench entry points.
- View composition is controlled by Athena rather than left to a raw default shell.
- The shell creates room for later engineering-centric views without requiring architecture replacement.

#### FR-12: Surface Semantic Inspection Beside Source Editing

Athena can place semantic inspection beside authored source editing inside the same workbench. Realizes UJ-2, UJ-3.

**Consequences (testable):**
- Users can inspect semantic information or related diagnostics while editing the corresponding source.
- Semantic inspection remains read-only in this milestone unless a path already exists through runtime-owned commands.
- The workbench proves that Athena is oriented around engineering meaning, not only text editing.

### 4.5 Desktop-First Delivery Boundary

**Description:** M4 must ship a desktop-first proof that keeps future web expansion open without making web scope part of the milestone. Realizes UJ-1.

#### FR-13: Deliver The Primary Proof On Desktop First

Athena can deliver the primary M4 product proof on the desktop-first path. Realizes UJ-1.

**Consequences (testable):**
- M4 verification and developer workflow are centered on the desktop build and launch path.
- Desktop delivery is sufficient to prove product-shell architecture and language-service integration.
- Web enrichment remains possible later without becoming a blocking M4 dependency.

#### FR-14: Preserve Theia-Based Growth Toward Future Surfaces

Athena can keep future Theia-based evolution open while staying narrow in M4. Realizes UJ-1, UJ-3.

**Consequences (testable):**
- The M4 shell does not hard-code product architecture to a throwaway proof layout.
- Future browser delivery, richer collaboration surfaces, graphical semantic-projection surfaces, and additional engineering panels remain possible extensions of the same Theia foundation.
- M4 can end without solving M5 package management or M6 semantic SCM.

## 5. Non-Goals (Explicit)

- M4 does not deliver the final `athena.yaml` repository manifest contract.
- M4 does not deliver the final `athena.lock` contract, dependency resolver, or semantic package graph.
- M4 does not deliver semantic SCM, intent commit flows, review flows, or publish flows above Git.
- M4 does not deliver the final Athena emotion system, token system, or complete visual language.
- M4 does not deliver a graphical editor, diagram canvas, or GLSP-style graph server.
- M4 does not replace Theia with a custom editor stack and does not treat Athena as a VS Code extension product.
- M4 does not move semantic authority into frontend-only state, canvas state, or editor-local models.
- M4 does not make web delivery the primary success path for this milestone.

## 6. MVP Scope

### 6.1 In Scope

- custom Theia-based Athena product shell
- desktop-first launch and packaging proof
- repository open and create flow for the current Athena source conventions
- runtime-backed active Repository Session
- Athena LSP integration for diagnostics and core language tooling
- baseline professional workbench composition
- semantic inspection surfaces inside the workbench
- curated bundled capability set for the M4 proof

### 6.2 Out Of Scope For MVP

- `athena.yaml`, `athena.lock`, and full package graph design, which are deferred to M5
- registry, dependency resolution, and package import contracts, which are deferred to M5
- semantic SCM over Git, which is deferred to M6
- final visual design system and advanced UX polish
- browser-first productization
- graphical projection or diagram editing implementation
- AI workflow expansion unrelated to the Theia platform proof

## 7. Success Metrics

**Primary**

- **SM-1:** Athena launches as a custom Theia-based desktop product shell rather than as a generic upstream shell or a thin extension wrapper.
- **SM-2:** A user can create or open an Engineering Repository and enter a functional Athena workbench in one coherent product flow.
- **SM-3:** Athena-authored source editing inside the workbench produces serious language tooling signals such as diagnostics and core navigation through Athena LSP.
- **SM-4:** The workbench can surface semantic inspection beside source editing without creating a second semantic authority in the frontend.

**Secondary**

- **SM-5:** The bundled capability set remains intentionally narrow and Athena-owned.
- **SM-6:** The M4 shell creates an obvious foundation for M5 repository/package work and M6 semantic SCM work without having to be rewritten.

**Counter-metrics**

- **SM-C1:** Do not optimize for broad feature count over product-shell architectural correctness.
- **SM-C2:** Do not optimize for visual polish over proof of Theia foundation and semantic boundary safety.
- **SM-C3:** Do not optimize for packaging novelty over a stable desktop-first proof path.

## 8. Cross-Cutting NFRs

- **NFR-1 Product Boundary Clarity:** Athena must be legible as a custom Theia-based product, not as a loose assembly of demos.
- **NFR-2 Semantic Authority Preservation:** Kernel and runtime remain the semantic authorities; the workbench remains downstream.
- **NFR-3 Inspectability:** Repository session state, diagnostics, and semantic inspection paths must remain inspectable enough for architecture and implementation debugging.
- **NFR-4 Desktop-First Practicality:** The milestone must be buildable and runnable on the current local development environment with the existing Java 25 and workstation constraints.
- **NFR-5 Growth Safety:** M4 must not block the later repository/package graph milestone or the later semantic-SCM milestone.

## 9. Constraints And Guardrails

### 9.1 Product Guardrails

- Athena is based on Eclipse Theia as the foundation for this milestone.
- Athena is not treated as a VS Code product or as a simple extension inside another editor.
- The workbench must remain professional in structure even if the visual system is intentionally early.

### 9.2 Architectural Guardrails

- M4 keeps semantic authority in kernel and runtime boundaries.
- M4 must build on the completed M0 to M3 stack rather than bypass it with frontend-local logic.
- Theia product composition should not collapse repository lifecycle, language tooling, and inspection into inseparable frontend code.

### 9.3 Roadmap Guardrails

- M4 proves product shell and language-service foundation only.
- M5 will own repository manifest, lockfile, package resolver, and semantic package graph concerns.
- M6 will own semantic SCM concerns over Git and review-oriented history flows.
- Later graphical projection work must remain downstream of canonical semantic state rather than establishing canvas-owned engineering truth.

## 10. Platform And Delivery

- **Primary platform:** desktop-first Theia product proof
- **Primary delivery target:** local developer-run product shell with deterministic build and launch path
- **Primary editor foundation:** Eclipse Theia workbench and client model
- **Primary language-service foundation:** Athena LSP
- **Current semantic backend foundation:** the existing Athena kernel and runtime from M0 to M3

## 11. Open Questions

1. What is the minimum repository bootstrap structure M4 should create before M5 defines the final manifest and lockfile contracts?
2. Which core language-service features are mandatory for the M4 proof versus desirable but deferrable?
3. Which semantic inspection views should be first-class in the M4 workbench: diagnostics only, semantic entity inspection, projection inspection, or a small curated subset before later graphical projection work begins?
4. How much of the welcome flow and repository-selection UX should be built in M4 versus deferred after the platform proof?

## 12. Assumptions Index

- M4 may use a lighter repository bootstrap shape before M5 freezes the formal repository manifest and lockfile contracts.
- The minimum serious language-tooling feature set for M4 will be finalized during architecture and story planning rather than frozen in this PRD.
- Desktop-first delivery is sufficient for the M4 proof even though richer web delivery remains strategically important later.
