---
title: Athena M6
status: draft
created: 2026-07-09
updated: 2026-07-09
---

# PRD: Athena M6

*Codename: Athena Semantic SCM Proof.*

## 0. Document Purpose

This PRD defines the M6 product requirements for Athena after the completed M5 milestone.

M6 exists to close the next semantic platform gap intentionally left open by M5:

> Athena can now open a governed repository, resolve deterministic package meaning, validate canonical lock state, and operate one runtime-owned `RepositoryGraphSession`. M6 must now prove that repository change, review, and history can be lifted from raw file mechanics into semantic engineering meaning without replacing Git as the storage substrate.

This PRD is capability-first. It builds on the completed M5 PRD and M5 architecture, the current workspace summary and M5 usage guide, the roadmap note under `docs/roadmap/`, and the M6 draft under `draft/m6/001-draft.md`. Implementation-shaped detail that is useful but too low-level for the PRD body is captured in [`addendum.md`](addendum.md).

## 1. Vision

M0 proved authored DSL to canonical semantic model. M1 proved runtime-owned workspace state, commands, history, and semantic diff inside one active project context. M2 proved explicit layout and geometry as downstream projection layers. M3 proved hosted extensibility through stable plugin contracts. M4 proved the first serious Athena IDE shell on Theia. M5 proved the governed repository/package contract, deterministic package graph, canonical lock behavior, and package-aware IDE operation.

M6 must now prove the next layer that makes Athena credible as an engineering change platform instead of only a semantic editor and repository proof:

- semantic change model above raw file diff
- semantic diff over governed repository/package meaning
- intent-shaped commit preparation above vendor SCM mechanics
- semantic review surfaces over affected contracts, packages, and validation consequences
- publish-oriented semantic history above stable package identity

In other words, M5 proved what an Athena repository means. M6 must prove how Athena understands change to that meaning.

## 1.1 Why Now

The next technical risk is no longer repository/package meaning. M5 already proved that Athena repositories have governed contract, graph, and lock semantics.

The next risk is semantic change and review.

Today the workspace has strong but still incomplete change capabilities:

- M1 already proved command history and semantic diff inside runtime-owned workspace state
- M5 already proved canonical repository/package meaning across repository root, manifest, lock, and package graph
- Git and file-level tools still remain the practical storage/change substrate outside Athena's semantic model

That is good sequencing, but it is not enough for the next platform layer. Until Athena freezes semantic SCM meaning, later milestones cannot safely build:

- semantic review workflows that speak in engineering terms
- intent-shaped commit and release preparation
- publish-oriented package evolution over governed identities
- later graphical projection workflows that need stable change/history semantics

That is why M6 comes before M7 graphical projection.

## 2. Target User

### 2.1 Jobs To Be Done

- Platform engineers need Athena to explain change in semantic repository/package terms rather than only raw file delta.
- Reviewers need Athena to summarize what changed semantically, what contracts moved, and what validation impact exists.
- Release and package owners need commit and publish preparation to be shaped around semantic intent above vendor storage mechanics.
- Language-tooling and runtime engineers need one VCS-neutral semantic SCM boundary that consumes the M5 repository/package model instead of polluting it.
- Founders need proof that Athena can move from semantic authoring into semantic collaboration and controlled evolution.

### 2.2 Non-Users (M6)

- Teams expecting M6 to replace Git or another source-control vendor
- Teams expecting M6 to revisit basic repository/package contract work already frozen by M5
- Teams expecting M6 to become the graphical projection milestone
- Teams expecting M6 to become AI-first authoring or review automation
- Teams expecting M6 to become broad UX polish or final Studio visual design

### 2.3 Key User Journeys

- **UJ-1. Aaron reviews a change and understands engineering meaning instead of only line delta.**
  - **Persona + context:** Aaron is validating that Athena can explain repository change in engineering terms over the governed M5 repository/package graph.
  - **Entry state:** An Athena repository has meaningful current state and a source-control baseline.
  - **Path:** Aaron asks Athena for change summary. Athena compares current semantic state against a chosen baseline, interprets changes over repository/package and engineering meaning, and reports affected packages, dependencies, contracts, and diagnostics.
  - **Climax:** Aaron can explain the change as semantic repository/package and engineering impact, not only raw file edits.
  - **Resolution:** Athena becomes credible as a semantic SCM layer rather than only a compiler and IDE.

- **UJ-2. Maya prepares a commit around semantic intent.**
  - **Persona + context:** Maya is evolving an engineering repository and wants commit preparation that reflects semantic work instead of manual file staging vocabulary.
  - **Entry state:** Maya has repository changes affecting packages, DSL source, and possibly lock state.
  - **Path:** Athena groups semantic changes, describes affected contracts and validation consequences, and prepares a commit intent summary above vendor SCM mechanics.
  - **Climax:** Maya can review and record a change in semantic intent language without Athena pretending Git no longer exists.
  - **Resolution:** Athena proves that semantic commit shaping can sit above Git rather than replacing it.

- **UJ-3. Priya reviews package evolution and publish readiness.**
  - **Persona + context:** Priya is checking whether package and dependency evolution is safe enough for later publish-oriented workflows.
  - **Entry state:** An Athena repository has stable M5 package identity and a semantic change baseline.
  - **Path:** Priya inspects semantic diff, affected package identities, dependency movement, validation impact, and release-facing history summaries.
  - **Climax:** Priya can answer whether the change is package-affecting, contract-breaking, or release-relevant without reverse-engineering raw file diff by hand.
  - **Resolution:** M6 becomes a valid foundation for later governed publish and ecosystem milestones.

## 3. Glossary

- **Semantic SCM** - Athena's VCS-neutral semantic interpretation layer above vendor storage/change mechanics.
- **Vendor SCM Adapter** - A later-boundary adapter that reads or writes baseline/change information from Git or another storage substrate without becoming semantic authority.
- **Semantic Baseline** - The repository state Athena compares against when producing semantic diff, review, or history output.
- **Semantic Diff** - A change description over repository/package and engineering meaning rather than only text delta.
- **Intent Commit Preparation** - Athena-owned commit shaping above vendor staging mechanics using semantic change summaries and governed repository meaning.
- **Semantic Review Summary** - A review-oriented explanation of semantic change, affected contracts, impacted packages, dependency movement, and validation consequences.
- **Publish-Oriented Semantic History** - History views that relate semantic change to package identity and release/publish meaning without making publish transport itself the milestone center.

## 4. Features

### 4.1 Semantic SCM Boundary Above Vendor Storage

**Description:** Athena must define a VCS-neutral semantic SCM boundary that consumes the stable M5 repository/package model from above. Realizes UJ-1, UJ-2, UJ-3.

#### FR-1: Define A Stable Semantic SCM Boundary

Athena can define a stable M6 semantic SCM boundary. Realizes UJ-1, UJ-2, UJ-3.

**Consequences (testable):**
- Athena introduces an explicit VCS-neutral semantic SCM contract above repository/package meaning.
- The new boundary depends on M5 repository/package contracts instead of polluting `repository-model` with vendor terms.
- Later Git or other vendor adapters can consume this boundary without becoming semantic authorities.

#### FR-2: Keep Vendor Storage Mechanics Downstream Of Semantic Meaning

Athena can keep Git or other vendor mechanics as storage/change substrate rather than semantic authority. Realizes UJ-1, UJ-2.

**Consequences (testable):**
- Athena users reason about repositories, packages, semantic change, review, and history instead of vendor-first vocabulary.
- Vendor-specific mechanics may still be used operationally, but they remain adapter concerns.
- M6 does not require replacing Git or reimplementing a full VCS.

### 4.2 Semantic Diff Over Governed Repository And Engineering Meaning

**Description:** Athena must describe change as semantic repository/package and engineering impact rather than only file delta. Realizes UJ-1, UJ-3.

#### FR-3: Compare Repository State Against A Semantic Baseline

Athena can compare current repository state against a semantic baseline. Realizes UJ-1, UJ-3.

**Consequences (testable):**
- Athena can select or receive a baseline state through the semantic SCM boundary.
- The comparison operates over governed repository/package and engineering meaning, not only text lines.
- The same baseline and repository state produce the same semantic diff result.

#### FR-4: Publish Semantic Change Categories

Athena can describe semantic change using stable change categories. Realizes UJ-1.

**Consequences (testable):**
- Athena can report semantic change such as package dependency changed, repository contract changed, declaration added or removed, connection changed, or validation impact introduced.
- Change categories are inspectable and stable enough for later review and history flows.
- Change output stays downstream of canonical semantic compilation/runtime interpretation.

#### FR-5: Surface Validation And Contract Consequences Of Change

Athena can surface semantic validation and contract impact as part of change analysis. Realizes UJ-1, UJ-3.

**Consequences (testable):**
- Athena can explain whether a change affects package dependencies, manifest/lock semantics, or engineering validation outcomes.
- Reviewers can identify whether a change introduces new violations or changes governed contracts.
- These consequences remain compiler/runtime-derived rather than frontend-guessed.

### 4.3 Intent Commit Preparation

**Description:** Athena must shape commit preparation around semantic intent above vendor SCM mechanics. Realizes UJ-2.

#### FR-6: Prepare Commit Intent From Semantic Change

Athena can prepare commit intent from semantic change summaries. Realizes UJ-2.

**Consequences (testable):**
- Athena can produce a commit preparation result organized around semantic meaning instead of only raw file lists.
- Commit preparation can include affected packages, dependency movement, changed contracts, and validation consequences.
- The result remains compatible with later vendor adapter execution rather than pretending Athena stores history alone.

#### FR-7: Keep Commit Preparation Governed And Inspectable

Athena can keep commit preparation deterministic and inspectable. Realizes UJ-2.

**Consequences (testable):**
- Commit preparation results are inspectable objects, not opaque UI-only text.
- The same semantic change set yields the same structured preparation output.
- M6 does not widen into final UX-heavy commit tooling or broad workflow automation.

### 4.4 Semantic Review Workflow

**Description:** Athena must support review-oriented understanding of repository/package and engineering change. Realizes UJ-1, UJ-3.

#### FR-8: Produce Semantic Review Summaries

Athena can produce semantic review summaries over current change. Realizes UJ-1, UJ-3.

**Consequences (testable):**
- Athena can answer what changed semantically, what packages were affected, what dependencies moved, and what validations changed.
- Review summaries remain VCS-neutral and semantic-first.
- The same semantic baseline and repository state yield the same review summary.

#### FR-9: Surface Review Output Through Existing Athena Product Boundaries

Athena can expose semantic review output through current runtime/LSP/IDE seams without rewriting M4 and M5 product shape. Realizes UJ-1, UJ-3.

**Consequences (testable):**
- Athena workbench and language-service surfaces can consume semantic SCM output through existing additive boundaries.
- Theia frontend remains downstream of `frontend -> LSP -> runtime/compiler/semantic-scm`.
- M6 review surfaces stay additive rather than becoming a shell rewrite.

### 4.5 Publish-Oriented Semantic History

**Description:** Athena must prove that stable package identity and semantic change can support release/publish-oriented history views. Realizes UJ-3.

#### FR-10: Relate Semantic Change To Package Identity And Version Meaning

Athena can relate semantic history to package identity and version meaning. Realizes UJ-3.

**Consequences (testable):**
- Athena can express history and release relevance in package-aware terms above M5 identity and lock semantics.
- Publish-oriented history remains semantic and governed even when actual publish transport is deferred.
- Later ecosystem and release milestones can build on stable package evolution meaning instead of inventing it.

#### FR-11: Keep Publish-Oriented History Narrow In M6

Athena can prepare publish-oriented history without becoming a full package registry or release platform. Realizes UJ-3.

**Consequences (testable):**
- M6 may summarize release-relevant semantic history but does not implement remote registry or package distribution systems.
- The milestone remains focused on semantic history meaning rather than transport or cloud workflows.
- M6 does not collapse into package ecosystem productization prematurely.

### 4.6 Growth Safety For M7 And Later Milestones

**Description:** M6 must prepare M7 graphical projection and later ecosystem work without widening into either one. Realizes UJ-1, UJ-3.

#### FR-12: Preserve A Later Graphical Projection Path Without Expanding Into It

Athena can preserve later graphical projection work while keeping M6 semantic-SCM-first. Realizes UJ-1, UJ-3.

**Consequences (testable):**
- Semantic history/review becomes a stable upstream input for later graphical review or visualization work.
- M6 does not turn into a visual-workbench or GLSP milestone.
- M7 can consume M6 semantic history meaning rather than helping define it.

## 5. Non-Goals (Explicit)

- M6 does not revisit or widen basic repository/package contract work that belongs to M5.
- M6 does not replace Git or implement a full vendor SCM.
- M6 does not become the graphical projection milestone, diagram canvas milestone, or GLSP-class delivery milestone.
- M6 does not become AI-first review or authoring automation.
- M6 does not become broad product-shell redesign or UI-polish work.
- M6 does not implement remote registry, package publishing infrastructure, or cloud collaboration as milestone center.

## 6. MVP Scope

### 6.1 In Scope

- stable semantic SCM boundary above vendor storage mechanics
- semantic baseline handling for repository comparison
- semantic diff over repository/package and engineering meaning
- package-aware and contract-aware change consequences
- deterministic intent commit preparation
- semantic review summaries
- narrow publish-oriented semantic history tied to package identity/version meaning
- additive IDE/runtime/LSP surfaces for semantic SCM output

### 6.2 Out Of Scope For MVP

- replacing Git or implementing a full source-control engine
- revisiting M5 package contract or lock semantics except where consumed by semantic SCM
- remote registry, package publishing infrastructure, or ecosystem distribution transport
- graphical projection, visual review canvases, or GLSP-class work
- broad UX redesign or final visual system work

## 7. Success Metrics

**Primary**

- **SM-1:** Athena can produce deterministic semantic diff from a repository baseline over governed repository/package and engineering meaning.
- **SM-2:** Athena can publish semantic review summaries that explain affected packages, dependency movement, contract change, and validation impact.
- **SM-3:** Athena can prepare inspectable commit intent above vendor SCM mechanics without replacing the vendor substrate.
- **SM-4:** Athena preserves the VCS-neutral user-facing semantic model while keeping Git or another vendor as downstream storage/change substrate.

**Secondary**

- **SM-5:** M6 creates a clear foundation for later release/publish and graphical-review workflows.
- **SM-6:** The existing Athena IDE shell can consume semantic SCM output without an architectural rewrite.

**Counter-metrics**

- **SM-C1:** Do not optimize for vendor-specific Git workflow surface over stable semantic SCM meaning.
- **SM-C2:** Do not optimize for broad review UI polish over freezing semantic diff/review semantics.
- **SM-C3:** Do not optimize for graphical experimentation over semantic history correctness.

## 8. Cross-Cutting NFRs

- **NFR-1 Semantic Authority Preservation:** Semantic SCM must remain downstream of compiler/runtime-owned repository/package and engineering meaning.
- **NFR-2 Vendor Neutrality:** User-facing semantic SCM nouns must stay VCS-neutral even if Git is the first practical adapter.
- **NFR-3 Determinism:** The same baseline plus repository state must yield the same semantic diff, review, and commit-preparation output.
- **NFR-4 Inspectability:** Semantic diff, review, and commit-preparation outputs must remain inspectable for development and architecture debugging.
- **NFR-5 IDE Continuity:** M6 must extend the current Athena shell rather than forcing a shell rewrite.
- **NFR-6 Growth Safety:** M6 must prepare later publish and graphical work without widening into either one.

## 9. Constraints And Guardrails

### 9.1 Product Guardrails

- Athena continues to present repository and package meaning first, not vendor SCM vocabulary first.
- Review and commit work must stay semantic-first rather than UI-workflow-first.
- Supporting IDE work is allowed only where it directly improves semantic SCM operability.
- Theia SCM may be used as the existing product-shell contribution surface for SCM views, commands, and provider presentation, but it must remain a downstream integration seam rather than the semantic SCM core.

### 9.2 Architectural Guardrails

- Compiler and runtime remain semantic authorities for repository/package and engineering meaning.
- `ide/lsp` remains the sole semantic entry point for the IDE path.
- Any SCM-facing abstraction belongs above M5 `repository-model`; `repository-model` remains vendor-neutral.
- Workbench additions must stay additive through Athena-owned product boundaries.
- Athena semantic SCM contracts must not be shaped around Theia provider types or UI lifecycle.

### 9.3 Roadmap Guardrails

- M6 owns semantic SCM, semantic diff, semantic review, and publish-oriented semantic history concerns.
- M7 owns real graphical projection and visual-workbench concerns.
- Parser evolution remains a separate kernel-language watchpoint, not M6 core.

## 10. Platform And Delivery

- **Primary platform:** desktop-first Athena Theia product inherited from M4 and M5
- **Primary delivery target:** local developer-run product shell plus deterministic JVM and workspace verification
- **Primary runtime authority:** runtime-backed repository graph session plus M6 semantic SCM interpretation
- **Primary language-service foundation:** Athena LSP
- **Primary storage/change substrate:** existing vendor SCM, with Git as the likely first adapter but not the user-facing semantic authority

## 11. Open Questions

1. What is the narrowest useful baseline model for M6: working tree versus HEAD-like baseline only, or a broader revision/reference abstraction immediately?
2. Which semantic change categories must be first-class in M6 to stay useful without overfitting to one domain extension?
3. How much commit preparation should M6 prove: summary only, structured preparation plus adapter handoff, or first executable vendor integration?
4. How much publish-oriented history belongs in M6 before it starts to widen into package ecosystem transport or registry concerns?
5. Which existing M1 semantic diff concepts should be reused directly versus generalized for repository/package change?

## 12. Assumptions Index

- M6 should consume the completed M5 repository/package model rather than reopen it.
- M6 should remain VCS-neutral in public semantics even if Git is the first practical adapter.
- M6 should prove semantic diff/review/commit meaning first and keep broader workflow UI or publish transport secondary.
- M6 should preserve the single-window / single-repository-session rule unless semantic SCM itself forces a broader session model.
