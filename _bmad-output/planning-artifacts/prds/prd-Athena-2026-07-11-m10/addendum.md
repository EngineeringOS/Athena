# M10 Addendum

This addendum captures useful M10 planning detail that informs architecture and epic shaping but is intentionally more implementation-shaped than the main PRD body.

## 1. Agreed Milestone Position

The active sequence after the completed M9 milestone is now:

- **M9** - engineering knowledge runtime
- **M10** - AI-assisted reasoning above governed knowledge outputs

This means M10 is not:

- source apply or source persist by default
- autonomous engineering mutation
- knowledge-pack ecosystem expansion
- standards or company-policy reasoning depth

It is the first milestone where Athena must prove that an AI layer can assist engineers without becoming the owner of engineering meaning.

## 2. Why M10 Must Sit Above M9

M9 already gave Athena:

- derived engineering context
- capability facts
- constraint evaluations
- typed knowledge diagnostics
- engineering impact consequences
- review-ready semantic consequence surfaces

That is the right substrate for AI assistance.

Without M9, AI would be forced to infer meaning from raw source text, frontend state, or vague natural-language prompts. That would be architecturally weak and hard to audit.

## 3. Theia AI Foundation Decision

M10 should not rebuild generic IDE AI infrastructure from scratch.

The local Theia IDE reference already includes a broad AI package family, including:

- `@theia/ai-core`
- `@theia/ai-chat`
- `@theia/ai-chat-ui`
- `@theia/ai-core-ui`
- `@theia/ai-ide`
- provider integrations such as `@theia/ai-openai`, `@theia/ai-copilot`, and others

The Theia docs describe these modules as:

- `@theia/ai-core` as the basis of AI integration and core concepts like language models, prompts, agents, and skills
- `@theia/ai-chat` as the language-model chat foundation
- `@theia/ai-core-ui` as the UI for core AI integration
- `@theia/ai-openai` as model integration configurable through preferences and environment variables

That is exactly the generic foundation Athena should reuse for:

- provider configuration
- provider management
- reusable chat or assistant UI
- future MCP-backed tool integration where appropriate

Athena should not reuse Theia AI as the source of engineering meaning.

The boundary should stay:

- Athena assembles governed engineering reasoning context
- Athena decides proposal categories and lifecycle
- Theia AI supplies generic IDE AI infrastructure

## 4. Narrowed Proof Shape

The first M10 proof should stay within three jobs:

1. explain a governed engineering diagnostic
2. summarize the impact of a governed change
3. suggest a review-ready next-check list or remediation direction

That keeps M10 architectural rather than aspirational.

## 5. Reasoning Context Shape

The first reasoning context package should likely include:

- repository and package identity
- affected semantic subject identities
- derived engineering context values
- capability facts
- constraint evaluations
- typed diagnostics
- engineering impact consequences
- semantic review entries when present

The core rule is:

- Athena assembles context deterministically
- the provider may generate a non-deterministic response
- Athena still records the context and proposal state explicitly

## 6. Provider Boundary Shape

The first M10 proof should introduce an explicit provider boundary that can later admit:

- hosted remote model providers
- local model providers
- test or mock providers for deterministic verification

The first milestone should not hard-code product semantics to one vendor transport.

Recommended split:

- Athena-owned side:
  - reasoning context assembly
  - proposal categories
  - audit and decision state
  - semantic identity and fact references
- Theia AI side:
  - provider registration
  - provider configuration
  - generic chat or assistant surface
  - optional MCP and future model tooling

## 7. Product Delivery Strategy

M10 should reuse current product seams:

- semantic inspection
- semantic SCM or review output
- additive workbench affordances around diagnostics and review
- Theia AI chat or assistant UI where generic AI interaction chrome is needed

That is enough to prove the governed AI layer.

M10 should avoid:

- a broad chat-first shell redesign
- prompt playground scope
- a second review system beside semantic SCM

## 8. Renderer And Scale Backlog Boundary

M10 should not become the renderer-polish milestone.

However, the current graphical proof has not yet really demonstrated dense engineering cases such as:

- more than 10 components
- more than 20 connections
- denser selection and reveal traffic across source, graph, outline, and semantic panels

So M10 should carry an explicit supporting backlog item:

- create at least one larger proof fixture for renderer and workbench scale validation
- verify projection refresh, fit-to-view, reveal, and panel responsiveness on a denser graph
- record limits and issues without turning M10 into a graph-workbench redesign

That keeps the roadmap honest:

- M10 core is AI-assisted reasoning
- graph-density polish remains later
- but scale risk is not ignored

## 9. Example Proof Statements

### Example A

Input state:

```text
ERROR:
QF1 rated current insufficient for M1
```

AI output:

```text
Explanation:
Motor M1 now requires more protection current than breaker QF1 can provide.
The insufficiency comes from the derived current demand and the current breaker rating.

Relevant facts:
- M1 full-load current increased
- required protection current exceeds QF1 rated current
- affected review subjects include QF1 and relay follow-up
```

### Example B

Input change:

```text
motor power
5.5kw -> 7.5kw
```

AI output:

```text
Impact summary:
- breaker sizing review required
- cable margin review recommended
- overload relay review required
```

### Example C

Review assistance:

```text
Suggested next checks:
1. Confirm breaker current margin
2. Confirm cable utilization remains within target margin
3. Confirm relay rating still matches required protection demand
```

## 10. Risks That Must Stay Explicit

- AI suggestion quality can look impressive while hiding weak semantic grounding
- over-customizing Theia AI too early can create a second UI system Athena must maintain
- a chat-first UI can tempt the milestone away from governed review and inspection paths
- provider lock-in can leak transport concerns into product semantics
- autonomous apply pressure can blur the line between explanation and mutation authority
- raw source prompting can silently bypass the M9 knowledge runtime
- dense graph cases may expose renderer or workbench limits that are orthogonal to M10 but still relevant to later milestones

## 11. Recommended Carry-Forward Split

If M10 succeeds, the recommended next ownership split is:

- **M10** - AI-assisted reasoning above governed knowledge outputs
- **M11** - knowledge-pack ecosystem
- **M12** - company policy and standards packs
- **M13** - deeper AI-assisted authoring or workflow automation only after the earlier governance proofs hold

That keeps the roadmap honest:

- first prove assisted reasoning
- then widen governed knowledge packaging
- then widen standards and policy depth
- only then consider deeper AI-driven workflow power
