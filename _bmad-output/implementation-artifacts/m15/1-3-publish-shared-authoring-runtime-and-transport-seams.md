---
baseline_commit: c04b3eb
---

# Story 1.3: Publish Shared Authoring Runtime And Transport Seams

Status: done

## Story

As a runtime engineer,  
I want guided authoring to flow through shared runtime and transport seams,  
so that M15 workbench surfaces stay thin consumers of one authoring service.

## FR Traceability

- FR-1: Athena can publish authoring intent as a first-class platform contract
- FR-2: Athena can preserve M8 as the only mutation authority for guided authoring
- NFR-1: guided authoring introduces no second write path outside M8
- NFR-2: workbench surfaces remain consumers of platform-owned authoring contracts
- NFR-4: canonical semantic identity remains stronger than graph element ids, widget ids, or presentation occurrence ids
- NFR-5: preview and approval remain inspectable and deterministic

## Acceptance Criteria

1. Given the first M15 workbench proof is implemented, when the service boundaries are reviewed, then authoring operations are exposed through runtime-owned services and Athena-owned transport rather than through ad hoc frontend mutation paths.
2. Given future forms or AI surfaces are considered, when the authoring service contract is inspected, then the contract can be reused without depending on one specific workbench widget.

## Tasks / Subtasks

- [x] Publish a runtime-owned authoring session service. (AC: 1, 2)
  - [x] Add a runtime-owned service that accepts guided authoring intents and records preview state without mutating canonical engineering truth directly.
  - [x] Keep preview submission and decision handling deterministic and inspectable.
  - [x] Keep all public or core Kotlin types documented with clean KDoc.
- [x] Thread the authoring session service through the shared runtime seams. (AC: 1, 2)
  - [x] Register the service in `AthenaServiceRegistry`.
  - [x] Expose the service through `AthenaExecutionContext`.
  - [x] Preserve the boundary that M8 remains the only future mutation authority.
- [x] Publish one Athena-owned LSP seam for authoring preview and state. (AC: 1, 2)
  - [x] Add transport payloads and params for authoring preview, authoring state, and preview decision.
  - [x] Route requests through the active runtime session rather than frontend-local logic.
  - [x] Keep the transport surface reusable for future Theia, graph, form, template, or AI consumers.
- [x] Keep Story `1.3` narrow and foundational. (AC: 1, 2)
  - [x] Do not build Theia widgets, GLSP tools, or direct UI behavior in this story.
  - [x] Do not execute canonical mutation commits in this story.
  - [x] Do not widen Story `1.3` into domain-specific authoring UX or Siemens-specific panel behavior.
- [x] Add focused runtime and transport tests. (AC: 1, 2)
  - [x] Verify runtime preview submission preserves canonical runtime state and command history.
  - [x] Verify preview decisions update runtime-owned preview state without mutating canonical state.
  - [x] Verify LSP preview, state, and decision requests expose the same runtime-owned seam.
  - [x] Run Gradle verification sequentially on Windows with Java 25; do not run build or test tasks concurrently.

## Dev Notes

### Story Intent

- Story `1.3` is the first runtime and transport bridge for guided authoring.
- The success condition is not "Athena already has a working component panel."
- The success condition is "Athena now has one runtime-owned authoring seam that later panel, inspector, graph, form, template, and AI surfaces can all consume."

### Architecture Guardrails

- Align to AD-84: authoring intent stays above M8 and is not a frontend mutation shortcut.
- Align to AD-88: palette, inspector, and connect flow stay consumers of shared authoring services.
- Align to AD-90: synchronization remains canonical-state-first.
- Align to AD-91: guided authoring remains review-first.
- Preserve inherited AD-34, AD-39, AD-80, and AD-82.

### Technical Requirements

- Current state already contains:
  - typed authoring intents in `:kernel:authoring-model`
  - preview and decision contracts in `:kernel:authoring-model`
  - one mutation authority through M8
  - `ide/lsp` request patterns for runtime-owned services
- Story `1.3` should mirror the existing runtime and LSP seam style used by component knowledge, projection, and AI reasoning.
- The story may add runtime-owned preview state, but it must not commit canonical mutation yet.

### File Structure Requirements

- Expected update files likely include:
  - `kernel/runtime/src/main/kotlin/...`
  - `kernel/runtime/src/test/kotlin/...`
  - `ide/lsp/src/main/kotlin/...`
  - `ide/lsp/src/test/kotlin/...`
  - `kernel/runtime/build.gradle.kts`
- Explicit non-goals:
  - no Theia widget implementation yet
  - no GLSP connect tool implementation yet
  - no canonical mutation commit path yet

### Testing Requirements

- Minimum verification commands should target runtime and `ide/lsp` tests directly first.
- Required proof checks:
  - preview submission stays runtime-owned
  - decisions stay inspectable
  - command history remains unchanged
  - LSP requests reflect the same runtime-owned preview state

### Current Code State To Preserve

- `Engineering IR` remains canonical authored engineering truth.
- M8 remains the only write authority.
- M14 remains the source of authorable component knowledge.
- M15 `1.1` and `1.2` already froze intent, preview, and decision contracts.

### References

- [Source: _bmad-output/planning-artifacts/epics-M15-2026-07-13.md]
- [Source: _bmad-output/planning-artifacts/prds/prd-Athena-2026-07-13-m15/prd.md]
- [Source: _bmad-output/planning-artifacts/prds/prd-Athena-2026-07-13-m15/addendum.md]
- [Source: _bmad-output/planning-artifacts/architecture/architecture-Athena-2026-07-13-m15/ARCHITECTURE-SPINE.md]

## Story Completion Status

- Status: done
- Completion note: runtime-owned guided authoring preview state now flows through `kernel/runtime` and Athena LSP without introducing a second write path. Verification was rerun from a clean state after one Windows `EOFException` cache incident, following the repository Gradle rule.
