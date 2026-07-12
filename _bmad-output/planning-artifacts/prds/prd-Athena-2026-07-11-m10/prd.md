---
title: Athena M10
status: draft
created: 2026-07-11
updated: 2026-07-11
---

# PRD: Athena M10

*Codename: Athena AI-Assisted Engineering Reasoning.*

## 0. Document Purpose

This PRD defines the M10 product requirements for Athena after the completed M9 milestone.

M10 exists to close the next platform gap intentionally left open by M9:

> Athena can now derive a narrow slice of engineering knowledge and consequence from canonical semantic state, but it still does not assist engineers in interpreting, reviewing, and acting on that knowledge through a governed AI layer.

This PRD is assistance-first, not autonomy-first. It builds on the completed M9 milestone summary, retrospective, proof usage, and the M10 brief under [`draft/m10/001-draft.md`](../../../../draft/m10/001-draft.md). Implementation-shaped technical detail that belongs in architecture or later execution planning is captured in [`addendum.md`](addendum.md).

## 1. Vision

M0 through M8 proved authored semantics, runtime authority, projection authority, review authority, and one mutation authority across source and graph. M9 then proved that Athena can derive engineering context, promote capability facts, evaluate a fixed governed knowledge pack, and publish typed engineering consequence through the existing semantic path.

M10 must now prove the next layer that makes Athena more than a knowledge evaluator:

- governed AI reasoning context built from Athena-owned semantic outputs
- explainable AI assistance over diagnostics, impact, and review facts
- explicit proposal and review states instead of opaque assistant chatter
- Theia-hosted AI configuration, chat UI, and provider management reused where they fit
- additive IDE delivery through existing Athena surfaces
- preserved kernel ownership of engineering truth even when AI is present

In other words, M9 proved that Athena can understand a narrow slice of engineering truth. M10 must prove that Athena can assist engineers with that truth without giving AI semantic authority.

## 1.1 Why Now

The next technical risk is no longer whether Athena can compute engineering consequence.

Today the workspace already has the needed upstream proof:

- M9 already provides derived engineering context, capability facts, constraint evaluations, diagnostics, and engineering impact consequences
- M6 already provides semantic review, commit, and history vocabulary
- M8 already provides a unified mutation and reveal path across source and graph
- M4 through M8 already provide the product shell and existing semantic delivery surfaces

That is exactly why M10 can now become the AI-assisted reasoning milestone:

- governed semantic inputs already exist
- review and diagnostics surfaces already exist
- Theia now already offers a reusable AI foundation for provider configuration and chat interaction
- the next missing proof is assisted interpretation and recommendation, not another semantic foundation

Starting M10 earlier would have risked letting AI infer meaning from incomplete or frontend-owned context. After M9, Athena has the first strong semantic base for a governed AI layer.

## 2. Target User

### 2.1 Jobs To Be Done

- Engineers need Athena to explain why a knowledge diagnostic happened in engineering terms instead of only listing a typed failure.
- Reviewers need Athena to summarize semantic impact and suggest what to inspect next when a governed engineering change propagates across affected subjects.
- Platform engineers need one governed AI request path built from Athena-owned semantic outputs rather than raw unstructured source prompts.
- Product and architecture owners need proof that AI can sit above Athena knowledge outputs without becoming a second semantic authority.

### 2.2 Non-Users (M10)

- Teams expecting M10 to become an autonomous engineering agent that rewrites truth without review
- Teams expecting M10 to become a source-apply or source-persist milestone
- Teams expecting M10 to widen into standards packs, company policy packs, or vendor-catalog reasoning
- Teams expecting M10 to become a broad natural-language-to-project generation milestone
- Teams expecting M10 to replace M9 kernel evaluation with freeform LLM judgement

### 2.3 Key User Journeys

- **UJ-1. Aaron asks why a diagnostic happened.**
  - **Persona + context:** Aaron has a governed engineering insufficiency in the current M9 proof slice and wants Athena to explain it clearly.
  - **Entry state:** The repository compiles, M9 knowledge outputs exist, and a typed knowledge diagnostic is already available.
  - **Path:** Aaron selects the diagnostic or affected subject. Athena builds a governed reasoning context package from the relevant semantic identities, derived context, capability facts, constraint results, and impact consequences, then requests an AI explanation.
  - **Climax:** Aaron sees a grounded explanation of why the issue happened and which facts and affected subjects matter.
  - **Resolution:** Athena proves it can explain governed engineering consequence without inventing new semantic truth.

- **UJ-2. Maya reviews an engineering change.**
  - **Persona + context:** Maya is reviewing a changed design and wants help understanding what to inspect next.
  - **Entry state:** A current semantic review or accepted change already exists through the M6 and M8 review path.
  - **Path:** Maya opens the review context. Athena builds an AI reasoning context from the review facts, engineering impact consequences, and current diagnostics, then asks the AI layer for a summary and review-ready checklist.
  - **Climax:** Maya sees a compact impact summary plus suggested next checks, still grounded in canonical review facts.
  - **Resolution:** Athena proves it can assist engineering review without creating a second review subsystem.

- **UJ-3. Priya audits whether AI stayed governed.**
  - **Persona + context:** Priya is validating that M10 did not silently move semantic ownership into an LLM.
  - **Entry state:** One or more AI outputs have been generated from M10.
  - **Path:** Priya inspects the recorded semantic inputs, cited facts, response category, and user decision state.
  - **Climax:** Priya can reconstruct what facts the AI saw, what it proposed, and whether the user accepted or dismissed it.
  - **Resolution:** Athena proves that AI assistance stayed downstream of governed semantic authority.

## 3. Glossary

- **AI Reasoning Context** - the deterministic, Athena-owned input package built from canonical semantic identities, derived engineering context, capability facts, constraint evaluations, diagnostics, impact consequences, and review facts before invoking an AI provider.
- **Grounded Explanation** - an AI-generated explanation explicitly tied to governed Athena semantic facts rather than raw freeform source interpretation.
- **Review Suggestion** - an AI-generated inspection hint, checklist, or remediation direction intended for human review, not direct semantic mutation.
- **Reasoning Proposal** - the typed record of one AI output, including category, cited facts, response content, provider metadata, and user decision state.
- **Proposal Decision State** - the explicit accepted, dismissed, unresolved, or unavailable status attached to a reasoning proposal.
- **Reasoning Session** - the bounded interaction in which Athena assembles one AI reasoning context, invokes one provider, and stores the result for later inspection.
- **Provider Boundary** - the replaceable contract that allows Athena to call a local or remote AI backend without changing semantic ownership rules.

## 4. Features

### 4.1 Governed AI Reasoning Context

**Description:** Athena must build AI requests from governed semantic outputs instead of handing raw authored source or UI state directly to the model. Realizes UJ-1, UJ-2, UJ-3.

#### FR-1: Build Typed AI Reasoning Context From M9 Outputs

Athena can assemble a deterministic reasoning context package from existing semantic outputs. Realizes UJ-1, UJ-2, UJ-3.

**Consequences (testable):**
- Athena builds AI reasoning context from governed semantic identities, derived engineering context, capability facts, constraint evaluations, diagnostics, impact consequences, and semantic review facts.
- The same semantic state yields the same reasoning context package for the same request type.
- M10 does not require the AI layer to reconstruct engineering meaning from raw source text alone.

#### FR-2: Preserve Traceable Reasoning Audit Data

Athena can preserve the semantic evidence behind every AI response. Realizes UJ-3.

**Consequences (testable):**
- Each reasoning proposal records its request category, cited semantic identities or facts, provider result status, and user decision state.
- Athena can distinguish between successful, unavailable, failed, and user-dismissed reasoning results.
- M10 keeps enough audit data to explain why a proposal was generated without recording a second semantic truth model.

### 4.2 AI Explanation And Review Assistance

**Description:** Athena must assist with explanation and review before attempting broader AI workflows. Realizes UJ-1, UJ-2.

#### FR-3: Explain Knowledge Diagnostics In Engineering Terms

Athena can generate grounded explanations for M9 knowledge diagnostics. Realizes UJ-1.

**Consequences (testable):**
- Athena can explain why a governed engineering sufficiency diagnostic was emitted.
- Explanations reference the relevant derived context, capability facts, or constraint results rather than generic advice only.
- Explanations remain downstream of typed diagnostics and do not replace them.

#### FR-4: Summarize Engineering Impact For Review

Athena can summarize the downstream consequence of a governed engineering change. Realizes UJ-2.

**Consequences (testable):**
- Athena can produce an AI-assisted impact summary from semantic review and engineering-impact facts.
- Summaries distinguish direct edits from downstream affected subjects.
- M10 reuses the current review model instead of inventing a separate AI review language.

#### FR-5: Suggest Review-Ready Next Checks Or Remediation Direction

Athena can propose narrow review-ready follow-up actions without applying them automatically. Realizes UJ-2.

**Consequences (testable):**
- Athena can produce a remediation direction or checklist for the current proof slice, such as checking breaker sizing, cable margin, or relay sufficiency.
- Suggestions remain advisory and review-ready rather than executable semantic mutations.
- M10 keeps the first proposal surface narrow enough to validate governance honestly.

### 4.3 Governance And Acceptance

**Description:** AI outputs must remain additive, inspectable, and explicitly governed. Realizes UJ-2, UJ-3.

#### FR-6: Keep AI Outputs Assistive And Non-Authoritative

Athena can ensure that AI assistance does not become a second semantic authority. Realizes UJ-3.

**Consequences (testable):**
- AI outputs cannot directly mutate canonical engineering truth.
- AI outputs remain proposals, explanations, or summaries until a human accepts or dismisses them.
- M10 does not allow provider responses to bypass runtime, review, or mutation governance.

#### FR-7: Preserve Explicit Proposal Decision States

Athena can keep accepted, dismissed, unresolved, and unavailable AI results explicit. Realizes UJ-2, UJ-3.

**Consequences (testable):**
- Each reasoning proposal has an explicit user-facing decision or status state.
- Athena can surface whether a proposal was accepted, dismissed, unresolved, or unavailable.
- Proposal state remains inspectable through existing semantic delivery seams.

### 4.4 Product Surface And Provider Boundary

**Description:** M10 must prove AI assistance through the current product path and keep the provider contract replaceable. Realizes UJ-1, UJ-2, UJ-3.

#### FR-8: Reuse Existing Athena Semantic Surfaces

Athena can surface AI reasoning through existing workbench and semantic surfaces. Realizes UJ-1, UJ-2.

**Consequences (testable):**
- M10 delivers the first AI proof through existing semantic inspection, semantic SCM or review, Problems-adjacent flows, or other existing Athena panels.
- M10 may reuse Theia AI UI surfaces for chat or proposal presentation instead of building generic AI shell infrastructure from scratch.
- M10 does not require a new giant chat-first workbench redesign.
- Supporting IDE work remains additive and focused on displaying governed proposals clearly.

#### FR-9: Keep The AI Provider Boundary Replaceable

Athena can call an AI backend through a replaceable contract without changing semantic ownership rules. Realizes UJ-3.

**Consequences (testable):**
- Athena may reuse Theia AI provider registration, configuration, and model-management facilities as the generic IDE-side foundation.
- M10 can support a provider boundary that does not hardwire product semantics to one model vendor or one deployment style.
- Provider-specific request or transport logic remains downstream of Athena-owned reasoning context assembly.
- The provider boundary can be reviewed independently from canonical semantic and review models.

## 5. Non-Goals (Explicit)

- M10 does not become an autonomous engineering editor.
- M10 does not become the source apply or source persist milestone.
- M10 does not widen into a knowledge-pack ecosystem, standards-pack platform, or company-policy platform.
- M10 does not replace typed diagnostics, semantic review, or runtime mutation governance with AI text.
- M10 does not become a broad chat-with-your-repository product milestone.
- M10 does not attempt multi-user collaboration or cloud-native engineering workflow redesign.

## 6. MVP Scope

### 6.1 In Scope

- one typed AI reasoning context package built from M9 outputs
- one narrow explanation flow for knowledge diagnostics
- one narrow impact-summary flow for semantic review or semantic SCM context
- one narrow review-ready suggestion or remediation-checklist flow
- explicit proposal state and evidence traceability
- reuse of current Athena product seams for delivery
- one replaceable AI provider boundary

### 6.2 Out Of Scope For MVP

- direct AI-authored semantic mutation apply paths
- autonomous source editing or graph editing
- broad natural-language project generation
- company standards reasoning packs
- broad prompt playground or generic chat workstation
- full enterprise governance, approval workflow, or collaboration redesign

## 7. Success Metrics

**Primary**
- **SM-1:** Athena can build a deterministic AI reasoning context package from governed semantic outputs. Validates FR-1.
- **SM-2:** Athena can preserve traceable audit data for each AI reasoning proposal. Validates FR-2.
- **SM-3:** Athena can generate grounded explanations for M9 knowledge diagnostics. Validates FR-3.
- **SM-4:** Athena can summarize engineering impact for governed review flows. Validates FR-4.
- **SM-5:** Athena can produce narrow review-ready suggestions without bypassing semantic governance. Validates FR-5, FR-6.
- **SM-6:** Athena can keep explicit proposal decision states visible through existing product seams. Validates FR-7, FR-8.
- **SM-7:** Athena can expose the first AI proof through a replaceable provider boundary. Validates FR-9.

**Secondary**
- **SM-8:** M10 proves that AI can amplify the value of M9 without reopening semantic ownership or mutation authority.
- **SM-9:** M10 prepares later richer AI workflows without pretending to solve autonomous engineering authoring.

**Counter-metrics**
- **SM-C1:** Do not optimize for a flashy chat surface over governed evidence and semantic traceability.
- **SM-C2:** Do not optimize for autonomous action if it weakens the explicit human decision boundary.
- **SM-C3:** Do not optimize for one specific AI vendor if it weakens the provider boundary.

## 8. Cross-Cutting NFRs

- **NFR-1 Semantic Authority Preservation:** Canonical engineering truth, diagnostics, review facts, and mutation authority remain Athena-owned and upstream of the AI layer.
- **NFR-2 Deterministic Context Assembly:** The same semantic inputs yield the same reasoning context package for the same request type.
- **NFR-3 Traceability:** Athena can inspect which semantic facts, identities, and request category produced a reasoning proposal.
- **NFR-4 Provider Replaceability:** Provider-specific transport or deployment logic remains replaceable behind a stable Athena-owned boundary.
- **NFR-5 Narrowness:** The first AI proof must stay small enough to validate governance honestly rather than widening into general assistant sprawl.
- **NFR-6 Delivery Reuse:** M10 must prefer existing Athena semantic surfaces over opening a new product-shell frontier.
- **NFR-7 Foundation Reuse:** M10 should prefer Theia AI foundation modules for generic AI UI, provider configuration, and provider management rather than re-implementing those IDE concerns in Athena.

## 9. Constraints And Guardrails

### 9.1 Product Guardrails

- The current Athena workbench remains the product host for M10.
- Supporting IDE work is allowed only where it directly improves governed AI proposal delivery and inspection.
- M10 should prove assisted reasoning, not broad chat UX or workflow redesign.
- M10 should reuse Theia AI for generic IDE concerns such as provider configuration, chat framing, and provider management whenever that does not weaken Athena semantic ownership.

### 9.2 Architectural Guardrails

- `Engineering IR` and M9 knowledge outputs remain the only semantic authority for the first AI proof.
- AI request context must be assembled from Athena-owned typed semantic outputs rather than raw frontend state.
- Runtime remains the owner of reasoning-context assembly and proposal lifecycle orchestration.
- AI outputs remain proposals, summaries, or explanations until a human decision is recorded.
- `ide/lsp` remains the sole IDE semantic and reasoning transport entry point.
- Theia AI may host the generic provider and chat infrastructure, but it must not become the source of engineering reasoning context or proposal governance.
- Provider logic remains downstream of Athena-owned reasoning contracts.

### 9.3 Roadmap Guardrails

- M10 owns AI-assisted reasoning above governed knowledge outputs.
- Source apply/persist remains backlog unless later milestone planning reassigns it explicitly.
- Knowledge-pack ecosystem growth remains later than M10.
- Company policy and standards packs remain later than M10.
- M10 does not reopen M9 knowledge-runtime scope.

## 10. Platform And Delivery

- **Primary platform:** desktop-first Athena Theia product inherited from M4 through M9
- **Primary delivery target:** local developer-run product shell with deterministic JVM semantic context plus provider-backed AI reasoning
- **Primary semantic foundation:** M9 derived context, capability facts, constraint evaluations, diagnostics, and engineering impact consequences
- **Primary text/language-service foundation:** Athena LSP
- **Primary generic AI shell foundation:** Theia AI packages for model configuration, provider management, and reusable chat or assistant UI
- **Primary product-delivery surfaces:** semantic inspection, semantic review or SCM context, and other existing Athena workbench panels

## 11. Open Questions

1. Which M10 proof slice should come first: diagnostic explanation, impact summary, or review-ready remediation checklist?
2. What is the minimum provider-neutral request and response contract that still proves real AI assistance value?
3. Should the first M10 provider path support only hosted models, only local models, or a boundary that can admit either even if one path ships later?
4. What user-facing proposal states are sufficient for M10: accepted, dismissed, unresolved, unavailable, or a smaller set?
5. How much provider metadata should Athena record in M10 without letting transport detail leak into product semantics?
6. Which existing Athena surface should own the first visible AI proposal interaction: semantic inspection, semantic SCM/review, or a narrow additive assistant panel?

## 12. Assumptions Index

- M10 should build on the completed M9 knowledge-runtime proof instead of mixing in source apply or deeper authoring scope.
- The first honest AI proof is explanation and review assistance, not autonomous mutation.
- Existing Athena semantic surfaces are sufficient for the first product delivery of AI-assisted reasoning.
- Athena should reuse Theia AI packages for generic AI shell capabilities instead of rebuilding provider-management and chat-UI foundations in-house.
- The reasoning context package should be deterministic even if provider responses are not.
- Provider choice matters, but the first milestone should freeze the provider boundary before optimizing one deployment style.
- Later milestones can widen into richer AI workflows only if M10 first proves governed assistance above Athena-owned semantic evidence.
