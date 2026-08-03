# Adversarial Review - Athena M40 PRD (Projection Reality)

Reviewer: prd_m40_adv4 (restarted after prior run interrupted)
Reviewed: `_bmad-output/planning-artifacts/prds/prd-Athena-2026-08-02-m40/prd.md` (status: final)
Date: 2026-08-02

Method: bmad-review-adversarial-general. Every claim below was checked against the repo with `rg`
before being reported. No guesswork.

## Findings

1. **CRITICAL - `ConnectionVerb` does not exist in the repo. The PRD cites a fabricated contract.**
   `rg -l "ConnectionVerb"` across the whole Athena tree returns zero files. The M39 mechanism is
   "domain relation verbs": `AthenaLanguageParserTest.kt:311` (`parses domain relation verbs as
   human first relationship declarations`), `DomainRelationVerbCompilationTest.kt`, and
   `AstExtensibilityLandingZoneTest.kt:35` (`word = SymbolIdentifierField("power", span)`). The M39
   PRD folder contains no `ConnectionVerb` text either. The M40 PRD repeats "exactly like
   `ConnectionVerb` in M39" in What Changes, FR-8, NFR-7, and Decisions 2. Either the PRD must name
   the real mechanism (domain relation verbs, with the exact registration site), or M40 is silently
   promising a new kernel contract - which would contradict "no new kernel vocabulary" and "same
   pattern as M39".

2. **CRITICAL - Stale "pending sign-off, Open Decision 7" text contradicts the Decisions section.**
   Handoff To M41+ says "Per the roadmap (pending sign-off, Open Decision 7)" while the Decisions
   section declares all seven decisions resolved 2026-08-02, including Decision 7 (roadmap
   accepted). Leftover pre-decision wording. The PRD cannot be final with an internal contradiction
   like this.

3. **MAJOR - Reading order is defined twice, incompatibly.** FR-5 says reading order is "a stable,
   repeatable projection of source order plus region order" AND "a permutation of the declared
   sheets". These are different objects. The Syntax Target shows only `reading-order [S1]` - a
   sheet-level permutation - with no authored surface for occurrence/region reading order. With one
   sheet the permutation is trivially `[S1]` and the region/occurrence order is unverifiable. The
   model must be pinned to one object (e.g. ordered regions, each an ordered occurrence list, with
   sheet membership derived) or the acceptance is untestable.

4. **MAJOR - "Projection rules" have no definition and no authored surface.** FR-6 says projection
   rules are projection facts with identity, and simultaneously that "View declarations are the
   sole authoring surface for view selection". No syntax example shows a rule. If view declarations
   are the only surface, rules are redundant or derived; if rules are first-class, their
   declaration and semantics must be specified. As written, a developer cannot implement FR-6
   without inventing a model.

5. **MAJOR - Diagnostic contract is too vague to test.** "Plain diagnostic" / "named diagnostic"
   appears roughly ten times (FR-2, FR-3, FR-4, FR-9, FR-15) with no pinned message text or code
   shape. AGENTS.md requires diagnostics to name the exact subject, problem, and correction. A
   verifier cannot assert "fails with a plain diagnostic" without a message contract. The PRD
   should table the exact diagnostics for: empty sheet, sheet without occurrences, view without
   sheets, duplicate occurrence identity, incomplete region, missing occurrence in group, coil
   without device, strip missing terminal, rung without occurrence, empty/duplicate/untraced
   construct, invalid nesting, and boundary-validation violation.

6. **MAJOR - FR-10 blast radius is under-specified; the real inventory is larger than the PRD
   states.** Verified affected surfaces beyond what FR-10 lists: `settings.gradle.kts:60` (module
   include), `kernel/compiler/build.gradle.kts:21` (module dependency - the PRD does not mention
   this line), `AthenaCompilerCompilationSupport.kt` (`deriveProfessionalControlDrawing` call at
   line 306, definition at 479, `professional-connection-drawing` gate at 483/492,
   `AthenaProfessionalDrawingCompiler().compile(...)` at 495), `AthenaProfessionalDrawingCompiler.kt`
   (authority sites at 894 and 910), `AthenaCabinetProjectionCompiler.kt:171`, LSP
   `AthenaDrawingCompositionPayloads.kt` (and `AthenaPresentationPayloads.kt` matched the payload
   pattern - the PRD names only `AthenaDrawingCompositionPayload`), tests
   `AthenaM34ProfessionalControlDrawingCompilerTest`, `AthenaM36DedicatedCabinetSampleTest`,
   `CabinetRenderPathDeletionGateTest`, `M37ProfessionalDrawingSurfaceTest`,
   `M37ProfessionalDrawingTraceEvidenceTest`, the whole drawing-composition module test set, and
   M34-M38 examples using the `professional-connection-drawing` policy (never enumerated). The
   story must start with a repo-wide inventory (`rg -l "drawing-composition|Cabinet|ProfessionalDrawing"`)
   and a zero-hit gate at closure.

7. **MAJOR - FR-7's audit premise depends on FR-10 landing first, but the ordering is not stated.**
   FR-7 says "No `Rail`, `Rung`, ... in kernel production source (audited after the FR-10
   retirement lands in the same epic)". Today the kernel violates this: the drawing-composition
   module's production source contains `CabinetCompositionCompiler.kt`, and `kernel/compiler`
   depends on it (`build.gradle.kts:21`). If a developer audits before FR-10, the acceptance fails
   wrongly; if after, it must be guaranteed by story order. The epic breakdown must pin FR-10
   before the FR-7 audit story.

8. **MAJOR - Acceptance 11 counter-metric is undefined and likely unpassable as written.** "Average
   number of declaration lines per authored relation in examples/m40 must not exceed the M39
   example": there is no counting rule. What counts as a declaration line - a `region` block line?
   a multi-line `occurrences [...]` list? each construct word? M39 has 6 authored relations; M40
   adds a `view` block, regions, and seven constructs, so the per-relation line count will
   mechanically exceed M39 under any reasonable counting. As written the metric is disputable and
   gameable. Define the exact counting rule or replace with a qualitative authoring-burden check.

9. **MAJOR - The Spatial Quality metric inputs are deleted by the same milestone.** FR-18 defines
   density = occurrences per sheet area and occupancy = used/available sheet-region ratio. The only
   sheet frame/title-block geometry in the repo lives in the module FR-10 retires:
   `DrawingSheetFrameFact` / `DrawingSheetTitleBlockFact` (`DrawingSheetCompositionModels.kt:40,48`)
   with `"A3"` and `projection-sheet-publication` authority (see
   `DrawingSheetReferenceCompilerTest.kt:217-220`). FR-18 says metrics are "computed from
   Presentation Document label and route bounds (no pixels)" - bounds are not sheet area, and the
   frame authority is being deleted. The PRD must pin the sheet-area input contract after
   retirement, or the metric definitions cannot be implemented.

10. **MAJOR - Transition of existing M39 authoring surfaces is unspecified.** The M39 example
    authors `projection RealityChainView { target connection-drawing; layout orthogonal-grid;
    drawingProfile ControlDrawingIEC; routeQuality ControlDrawingRouteQuality }` and
    `layout schematic { place Supply at (1, 1) ... }`. M40 FR-6 declares view declarations the
    "sole authoring surface" and Decision 1 says "Geometry, placement, and paint stay derived" -
    yet the M39 example authors coordinates with `place`. The PRD never says whether the existing
    `projection` block and `layout`/`place` syntax are rewritten, renamed, or retired in M40.
    Without this, a developer must guess the migration path.

11. **MAJOR - Syntax Target covers only 3 of 7 required constructs.** The syntax example shows
    `power-rail`, `rung`, and `terminal-strip`. FR-19 and Acceptance 7 require at least one of each
    of seven constructs (rail, rung, branch, wire bundle, terminal strip, contact group, coil
    group) in the example, and FR-8 names all seven implementations. `branch`, `wire-bundle`,
    `contact-group`, and `coil-group` have no example declaration form. Four grammar surfaces are
    left for a developer to invent mid-milestone.

12. **MEDIUM - Label-engine ownership is split across M42/M44 with no single authority.** FR-18
    says "No label engine is added in M40 (deferred to M42/M44)"; Handoff To M41+ says "the label
    engine contract lands in M42 and label readability metrics in M44"; the roadmap gives M44
    "Readability, density, metrics", duplicating the Spatial Quality metrics M40 owns in FR-18.
    State one long-term owner for the label engine and one for metric authority, or the M44 story
    will conflict with M40's FR-18.

## Verdict

The refactor direction (Projection Reality, domain-neutral constructs, metrics as acceptance
criteria) is sound. The PRD is not final-safe: findings 1 and 2 are factual errors that must be
fixed in text, findings 3-11 are contract gaps that will force invention during development, and
finding 12 is a roadmap-ownership ambiguity. All twelve must be resolved before epics are
frozen and dev stories start.
