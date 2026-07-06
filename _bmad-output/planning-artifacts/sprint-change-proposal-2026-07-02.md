# Sprint Change Proposal

**Date:** 2026-07-02  
**Project:** Athena  
**Mode:** Batch  
**Change Trigger:** Implementation readiness assessment reported `NEEDS WORK`

## 1. Issue Summary

The current planning set is close to implementation-ready, but `epics.md` still has three structural defects that will create avoidable execution risk:

1. Stories do not carry explicit per-story FR traceability.
2. The plan is greenfield, but there is no bootstrap/setup story or equivalent pre-sprint implementation task for the JVM/Kotlin/Gradle foundation.
3. Stories `2.4` and `2.5` describe governance and boundary intent at too high a level for a single developer agent to implement safely.

### Triggering Stories And Artifacts

- `Story 1.1` revealed the missing greenfield bootstrap path because parsing was scheduled before workspace establishment.
- `Stories 2.4` and `2.5` revealed under-specified implementation boundaries.
- The broader `epics.md` document revealed the missing story-level FR traceability.

### Evidence

- Readiness finding: 鈥淢issing story-level FR traceability鈥?- Readiness finding: 鈥淢issing greenfield project bootstrap story鈥?- Readiness finding: 鈥淪tories 2.4 and 2.5 are under-specified for single-agent implementation鈥?
## 2. Impact Analysis

### Epic Impact

**Epic 1: End-to-End Semantic Compilation Proof**
- Still viable as the first M0 proving wedge.
- Requires insertion of a bootstrap/setup story ahead of parsing.
- Existing Epic 1 stories should be renumbered down one slot after bootstrap insertion.
- All Epic 1 stories should gain explicit FR references.

**Epic 2: Governed Extension And External Boundary Proof**
- Still viable with no epic-level scope change.
- Existing story sequence remains directionally correct.
- `Story 2.4` should be split into package-definition and package-resolution slices.
- Existing `Story 2.5` should be rewritten as a concrete boundary-contract descriptor story and renumbered to `2.6`.
- All Epic 2 stories should gain explicit FR references.

### Artifact Impact

**Requires update**
- `_bmad-output/planning-artifacts/epics.md`

**No direct update required**
- PRD: no conflict with current goals, scope, or MVP definition
- Architecture spine: no invariant or stack change required
- UX artifacts: none exist; no direct artifact change possible

### Secondary Impact

- Future story IDs referenced in sprint planning must use the new numbering.
- The implementation readiness report becomes stale after the correction and should be re-run.
- If a `sprint-status.yaml` is later created, it must use the corrected story list.

### Technical Impact

- No rollback is needed because this is still a planning-stage correction.
- No code, infrastructure, or deployment artifact is being invalidated.
- Timeline impact is low to medium and concentrated in planning cleanup rather than implementation rework.

## 3. Recommended Approach

### Option Evaluation

**Option 1: Direct Adjustment**  
Viable  
Effort: Medium  
Risk: Low

Directly revise `epics.md` to add traceability, insert bootstrap/setup, and decompose the abstract governance/boundary stories into concrete implementation slices.

**Option 2: Potential Rollback**  
Not viable  
Effort: High  
Risk: Medium

There is no implemented sprint work to roll back, so rollback adds process overhead without simplifying the problem.

**Option 3: PRD MVP Review**  
Not viable  
Effort: Medium  
Risk: Medium

The issue does not invalidate the M0 MVP. The problem is planning precision, not product direction.

### Selected Approach

**Option 1: Direct Adjustment**

### Rationale

- Lowest-risk path with no strategic reset
- Preserves the approved M0 architecture and epic structure
- Fixes implementation-safety problems exactly where they exist
- Avoids distorting the PRD or architecture to solve a story-writing problem

## 4. Detailed Change Proposals

### A. Add Explicit Story-Level FR Traceability

**Artifact:** `epics.md`  
**Section:** Every story block

**OLD**
- Stories contain title, user story, and acceptance criteria only.

**NEW**
- Add a line directly under each story title:

```md
**FRs implemented:** FRx, FRy
```

**Proposed mapping**

- `Story 1.1` bootstrap: no direct PRD FR; foundational enabler for M0 stack constraints and AD-1
- `Story 1.2` parse DSL: `FR1`
- `Story 1.3` lower to IR: `FR2`, `FR3`
- `Story 1.4` validate IR and diagnostics: `FR4`, `FR5`
- `Story 1.5` deterministic compiler pipeline: `FR4`
- `Story 1.6` render model plus SVG: `FR6`, `FR12`, `FR13`
- `Story 1.7` conformance examples: `FR6`
- `Story 2.1` typed plugin contracts: `FR9`
- `Story 2.2` plugin discovery and compatibility: `FR9`, `FR10`
- `Story 2.3` real domain plugin: `FR9`
- `Story 2.4` governed knowledge package format: `FR7`, `FR8`
- `Story 2.5` governed knowledge resolution: `FR7`, `FR8`
- `Story 2.6` external boundary contract descriptors: `FR10`, `FR11`

**Rationale**

This removes ambiguity during implementation and review and aligns the stories with the readiness requirement for direct traceability.

### B. Insert Greenfield Bootstrap Story Ahead Of Parsing

**Artifact:** `epics.md`  
**Section:** Epic 1 stories

**OLD**

```md
### Story 1.1: Author And Parse The M0 Electrical/Runtime DSL
```

**NEW**

```md
### Story 1.1: Establish The M0 JVM Compiler Workspace

**FRs implemented:** None directly; foundational implementation enabler for M0 stack constraints

As a platform builder,
I want Athena to start from a working Kotlin/JVM compiler workspace with baseline modules, build logic, and CLI entrypoint wiring,
So that the M0 proof can be implemented and verified on a deterministic foundation before semantic behavior is added.

**Acceptance Criteria:**

**Given** the approved M0 stack and module shape
**When** the initial workspace is created
**Then** the repository contains the baseline modules needed for `cli`, `language`, `semantics-core`, `ir`, `compiler`, `domain-electrical-runtime`, `renderer-svg`, and `examples`
**And** the workspace builds successfully on Java `25`, Kotlin `2.4.0`, and Gradle `9.6.1`

**Given** the baseline workspace
**When** a developer runs the standard build and test entrypoints
**Then** dependency resolution, compilation, and baseline test execution succeed deterministically
**And** the workspace exposes a minimal CLI entry path suitable for later compiler wiring

**Given** the greenfield starting point
**When** implementation proceeds to semantic stories
**Then** later stories can modify only the modules they need
**And** no later story needs to invent project structure ad hoc
```

**Follow-on renumbering**

- Existing `Story 1.1` becomes `Story 1.2`
- Existing `Story 1.2` becomes `Story 1.3`
- Existing `Story 1.3` becomes `Story 1.4`
- Existing `Story 1.4` becomes `Story 1.5`
- Existing `Story 1.5` becomes `Story 1.6`
- Existing `Story 1.6` becomes `Story 1.7`

**Rationale**

This satisfies the greenfield setup requirement without changing epic scope or architecture.

### C. Replace Abstract Story 2.4 With Two Concrete Governed-Knowledge Stories

**Artifact:** `epics.md`  
**Section:** Epic 2 stories

**OLD**

```md
### Story 2.4: Package Governed Knowledge As Reviewable Artifacts
```

**NEW**

```md
### Story 2.4: Define Governed Knowledge Artifact Packages

**FRs implemented:** FR7, FR8

As a platform builder,
I want Athena to define versioned package and manifest formats for governed ontology, mapping, and rule artifacts,
So that reviewed knowledge can be published as reusable compiler inputs with explicit provenance and compatibility.

**Acceptance Criteria:**

**Given** reviewed ontology, mapping, or rule content approved for operational use
**When** that content is packaged for Athena
**Then** the result includes a typed artifact package and manifest declaring artifact kind, version, provenance, and compatible core or contract range
**And** the package format remains distinct from project-authored engineering input

**Given** a malformed or incomplete governed knowledge package
**When** Athena validates the package
**Then** it rejects the package before operational use
**And** it emits diagnostics describing the packaging or manifest defect
```

```md
### Story 2.5: Resolve Governed Knowledge Artifacts Into Compilation Context

**FRs implemented:** FR7, FR8

As a platform builder,
I want Athena to resolve approved governed knowledge artifacts into the effective compilation context,
So that compiler behavior can use reviewed knowledge and trace conclusions back to exact artifact versions.

**Acceptance Criteria:**

**Given** one or more compatible governed knowledge packages
**When** a compilation run begins
**Then** Athena resolves the active artifacts into the effective compilation context
**And** the active artifact identities, versions, and provenance remain inspectable

**Given** diagnostics or derived outcomes influenced by governed knowledge
**When** those results are reported
**Then** they can reference the responsible governed artifact versions
**And** incompatible knowledge packages are rejected before they change compiler behavior
```

**Rationale**

The old Story `2.4` bundled package format, operational resolution, and boundary policy into one abstract unit. Splitting it produces two concrete single-agent slices.

### D. Rewrite Boundary Story As A Concrete Contract-Descriptor Story

**Artifact:** `epics.md`  
**Section:** Epic 2 stories

**OLD**

```md
### Story 2.5: Define External Tool And Runtime Boundary Contracts
```

**NEW**

```md
### Story 2.6: Define External Boundary Contract Descriptors

**FRs implemented:** FR10, FR11

As a platform builder,
I want Athena to define machine-readable boundary contract descriptors for external tools, standards, and runtime or enterprise contexts,
So that future integrations can be added as sources, targets, or compatibility layers without becoming alternate semantic authorities.

**Acceptance Criteria:**

**Given** a candidate external boundary such as a standards interchange, runtime connector, or enterprise bridge
**When** Athena defines the boundary descriptor
**Then** the descriptor declares the boundary direction, owned semantic authority, expected exchanged forms, and compatibility assumptions
**And** the descriptor keeps `Engineering IR` as the upstream semantic authority

**Given** a boundary descriptor for a standards concept such as `AutomationML`
**When** it is validated in M0
**Then** Athena can represent it as a reference or compatibility boundary without requiring a production importer, exporter, or live connector
**And** validation fixtures prove that the descriptor does not relocate authority out of the semantic core
```

**Rationale**

This converts Story `2.5` from a broad policy statement into an implementable descriptor-plus-fixture slice.

### E. No PRD Or Architecture Rewrite

**Artifact:** PRD and architecture spine  
**Change:** None

**Rationale**

The readiness issues are planning-detail problems inside `epics.md`, not requirement or architecture contradictions.

## 5. Implementation Handoff

### Scope Classification

**Moderate**

Backlog reorganization is required because story insertion, renumbering, and story decomposition all change downstream planning references.

### Handoff Recipients

- **Product Owner / planning agent**
  - Apply the approved `epics.md` corrections
  - Preserve epic scope while updating story numbering and traceability
- **Developer / implementation agent**
  - Consume the corrected story sequence once planning artifacts are updated
- **Readiness reviewer**
  - Re-run implementation readiness after the artifact updates

### Success Criteria

- `epics.md` contains explicit FR references per story
- Epic 1 includes a bootstrap/setup story before semantic implementation stories
- Epic 2 contains concrete governed-knowledge and boundary-contract stories that are independently implementable
- A follow-up readiness assessment upgrades or narrows the remaining findings

## 6. Checklist Execution Summary

- **1. Understand trigger and context**
  - `1.1` Done
  - `1.2` Done
  - `1.3` Done
- **2. Epic impact assessment**
  - `2.1` Done
  - `2.2` Done
  - `2.3` Done
  - `2.4` Done
  - `2.5` Done
- **3. Artifact conflict and impact analysis**
  - `3.1` Done
  - `3.2` Done
  - `3.3` Action-needed only as future UX warning, not current blocker
  - `3.4` Done
- **4. Path forward evaluation**
  - `4.1` Viable
  - `4.2` Not viable
  - `4.3` Not viable
  - `4.4` Done
- **5. Sprint Change Proposal components**
  - `5.1` Done
  - `5.2` Done
  - `5.3` Done
  - `5.4` Done
  - `5.5` Done

## 7. Approval And Handoff Log

- **User approval:** Approved on 2026-07-02
- **Scope classification:** Moderate
- **Sprint status artifact:** `sprint-status.yaml` not present yet, so no sprint-status update was required at this stage
- **Primary artifact to change:** `_bmad-output/planning-artifacts/epics.md`
- **Handoff recipients:**
  - Product Owner / planning agent: apply approved `epics.md` corrections and renumbering
  - Developer / implementation agent: consume corrected stories after planning update
  - Readiness reviewer: rerun implementation readiness after corrections land

