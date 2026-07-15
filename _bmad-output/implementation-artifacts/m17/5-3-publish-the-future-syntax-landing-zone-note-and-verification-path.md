# Story 5.3: Publish The Future Syntax Landing-Zone Note And Verification Path

Status: done

## Story

As an architecture owner,
I want Athena to record how future syntax such as imports should land after M17,
so that the milestone closes with a durable next step instead of premature language widening.

## FR Traceability

- FR-9: Athena can define a durable landing zone for future syntax.
- FR-10: Athena can publish a verification corpus for parser parity and IDE behavior.
- NFR-5: The M17 architecture must make future syntax additions cheaper, not harder.

## Acceptance Criteria

1. Given future language growth is reviewed, when architecture and milestone closeout are inspected, then a new `docs/usages/m17-proof-usage.md` (following the structure of `docs/usages/m16-proof-usage.md`) explicitly records that future constructs such as `import` land through the same chain: compiler parser (`ANTLR4`) -> authored AST (`SourceFileAst` and its future extensions from Story `1.3`) -> lowering (`EngineeringIrLowerer`) -> canonical `Engineering IR`, and that this chain is the only sanctioned landing path, never a Tree-sitter-CST-to-`Engineering IR` shortcut.
2. Given the M17 verification path is reviewed, when the scenario is inspected, then `docs/usages/m17-proof-usage.md` describes one end-to-end verification narrative covering, in order: valid-source parity (Story `5.1`'s `examples/m17/parser-parity-proof` and repository-backed fixture), malformed-source diagnostics (Story `5.2`'s `examples/m17/invalid-and-incomplete-proof` fixtures and typed-diagnostic tests), Tree-sitter-backed syntax UX (Epic 3's highlighting/folding/outline proof, referenced even if implemented in a separate story), and preserved LSP semantic authority (Epic 4's diagnostics-boundary and navigation/utility continuity guarantees from Stories `4.1`-`4.3`).
3. Given the milestone-closing document is reviewed for scope discipline, when its explicit-exclusions section is inspected, then it states that M17 does not ship final `import` semantics, full package-aware authored behavior, or a general macro-use language, consistent with the PRD's Non-Goals and the architecture spine's Deferred section.
4. Given the `examples/m17/README.md` (from Stories `5.1`/`5.2`) and `_bmad-output/implementation-artifacts/m17/README.md` are reviewed together with the new usage note, then all three documents describe the same verification path and fixture set without contradicting each other on scope, folder names, or verification commands.

## Tasks / Subtasks

- [x] Author the future-syntax landing-zone note. (AC: 1, 3)
  - [x] Write a "Future Syntax Landing Zone" section in `docs/usages/m17-proof-usage.md` stating explicitly that `import` and any other future authored construct must land through compiler parser (`ANTLR4`) -> authored AST -> lowering -> `Engineering IR`, citing AD-104 and AD-111 by name, and stating that AST contracts must remain extensible (per Story `1.3`) rather than requiring parser-generator-specific hooks in lowering.
  - [x] Explicitly list what M17 defers: final `import` resolution semantics, full package-aware authored semantics, full macro-use language syntax, full expression language, and any architecture that would make Tree-sitter or generated parse trees canonical truth, matching the PRD's Non-Goals (Section 7) and the architecture spine's Deferred section.
  - [x] Cross-reference `_bmad-output/planning-artifacts/prds/prd-Athena-2026-07-14-m17/prd.md`'s Open Question 2 ("Should M17 include one narrow parse-only future-syntax seed such as `import`...") and record whatever decision was actually made during Epic 1-3 implementation (if `import` was seeded as a parse-only placeholder, document it here; if not, state explicitly that no `import` token or grammar rule exists yet and that this is intentional).
- [x] Author the unified M17 verification path. (AC: 2, 4)
  - [x] Write a "Verification Path" section in `docs/usages/m17-proof-usage.md` listing, in order: (a) valid-source parity via `examples/m17/parser-parity-proof` and the repository-backed fixture from Story `5.1`, with the exact `kernel/compiler` test command; (b) malformed/incomplete-source diagnostics via `examples/m17/invalid-and-incomplete-proof` from Story `5.2`, with the exact test command; (c) Tree-sitter-backed syntax UX proof from Epic 3, referencing whichever verification command or manual proof flow Epic 3's stories established; (d) LSP semantic-authority continuity from Epic 4 (Stories `4.1`-`4.3`), referencing their regression test commands.
  - [x] Cross-check this section against `examples/m17/README.md` (written in Stories `5.1`/`5.2`) and `_bmad-output/implementation-artifacts/m17/README.md` for naming/command consistency; fix any drift found (e.g. folder name mismatches, stale test filter patterns) in whichever document is out of date.
  - [x] Follow the deterministic-verification-command style already used in `docs/usages/m16-proof-usage.md` (a single fenced PowerShell block with sequential `java25; .\gradlew.bat ...` invocations, plus the encoding audit as the final line).
- [x] Record the M17 final-statement framing for closeout. (AC: 1, 3)
  - [x] Quote or closely paraphrase the architecture spine's Final Statement ("Athena can grow its authored language on a durable parser architecture...") and the PRD's Section 12 Final Statement in the usage note's closing section, so the milestone-closing artifact is traceable to the same framing used in planning.
- [x] Keep Story `5.3` narrow. (AC: 1, 2, 3, 4)
  - [x] Do not implement any new `import` grammar rule, AST node, or lowering behavior in this story; this story only records the landing-zone decision and verification path in documentation.
  - [x] Do not duplicate the full content of `examples/m17/README.md` or the Story `5.1`/`5.2` fixture listings; reference them instead of re-describing every fixture in full.
  - [x] Do not widen scope into a general project-wide documentation audit; this story's edits are scoped to the M17 usage note and the consistency check against the two existing M17 README files.
- [x] Run the documentation/encoding verification appropriate for this story. (AC: 1, 2, 3, 4)

## Dev Notes

### Story Intent

- Story `5.3` is M17's closeout-documentation story: it records, in one place, both the durable future-syntax landing zone (so imports and similar constructs have an explicit, architecturally-sanctioned path after M17) and the unified verification narrative spanning Stories `5.1`, `5.2`, and Epics `3`/`4`.
- The success condition is not "a new markdown file exists." The success condition is "a future contributor who wants to add `import` (or any other future construct) can read this one note and know exactly which chain to extend (compiler parser -> authored AST -> lowering -> `Engineering IR`) and which milestone artifacts prove M17 already closed cleanly, without having to reconstruct that from five separate story files."
- This story depends on Stories `5.1` and `5.2` already having published their fixtures and READMEs, and references Epic 3 (Tree-sitter syntax UX) and Epic 4 (LSP semantic authority) outputs; if any of those are not yet implemented when this story starts, write the note against their planned shape from the epics/architecture spine and mark the corresponding verification step as "pending Epic N" rather than fabricating a command that does not yet exist.
- This is the last story in M17's epic order; it is the natural place to confirm the whole milestone's proof path is internally consistent before the milestone is considered closed.

### Architecture Guardrails

- Align to AD-104: M17 freezes one language architecture before language breadth expands, and every future authored construct must land on compiler parser -> authored AST -> lowering -> `Engineering IR`. Story `5.3` is the explicit, milestone-closing record of this rule for future contributors, not a new architectural decision. [Source: _bmad-output/planning-artifacts/architecture/architecture-Athena-2026-07-14-m17/ARCHITECTURE-SPINE.md#AD-104---M17-Freezes-One-Language-Architecture-Before-Language-Breadth-Expands]
- Align to AD-111: future syntax growth lands through AST extensibility, not ad hoc grammar patches; AST contracts remain extensible, parser adaptation remains isolated, and lowering remains organized around authored semantic categories rather than parser token sequences. Story `5.3`'s landing-zone note must state this explicitly, pointing back to Story `1.3`'s AST-extensibility work as the concrete mechanism. [Source: _bmad-output/planning-artifacts/architecture/architecture-Athena-2026-07-14-m17/ARCHITECTURE-SPINE.md#AD-111---Future-Syntax-Growth-Lands-Through-AST-Extensibility-Not-Ad-Hoc-Grammar-Patches]
- Align to AD-110 and AD-113 by cross-referencing Story `5.1`'s parity corpus and Story `5.2`'s malformed/incomplete corpus as the executable evidence backing this note's verification-path claims, rather than restating unverified claims. [Source: _bmad-output/planning-artifacts/architecture/architecture-Athena-2026-07-14-m17/ARCHITECTURE-SPINE.md#AD-110---The-First-M17-Proof-Stays-Parity-First-On-The-Current-Supported-Syntax-Subset]

### Current Code State To Preserve

- `docs/usages/m16-proof-usage.md` already establishes the exact document shape this story should follow: `Purpose`, a proof-repository/fixture-location section, a `Proof Flow` or verification-scenario section, a `Deterministic Verification` section with one fenced sequential PowerShell command block, and a closing `Product Position` section.
- `_bmad-output/implementation-artifacts/m17/README.md` already records the M17 milestone's planned scope, included stories, current status, milestone intent, and product position; Story `5.3`'s usage note complements this file rather than replacing it (the README is the implementation-artifact-folder index; the usage note is the product-docs proof narrative, matching the `docs/usages/` convention used by every other milestone).
- `_bmad-output/planning-artifacts/prds/prd-Athena-2026-07-14-m17/prd.md` already contains Section 7 (`Non-Goals (Explicit)`), Section 11 (`Open Questions`, including Question 2 about a possible narrow `import` parse-only seed), and Section 12 (`Final Statement`); this story's landing-zone note must stay consistent with, and may directly cite, all three.
- There is no `docs/usages/m17-proof-usage.md` yet. There is no `examples/m17/` folder yet outside of what Stories `5.1` and `5.2` will have already published by the time this story starts (per epic/story ordering, `5.1` and `5.2` precede `5.3`).

### Technical Requirements

- Write `docs/usages/m17-proof-usage.md` as UTF-8 (no BOM required for `.md` files per the workspace encoding rule, which only mandates BOM for `*.zh-CN.md`); if a Chinese counterpart is added, save it as UTF-8 with BOM.
- Keep verification commands in the note copy-pasteable and accurate; if any referenced Epic 3/Epic 4 command does not exist yet at the time this story is written, mark it clearly as "pending Epic 3" / "pending Epic 4" rather than inventing a plausible-looking but nonexistent command.
- Do not introduce new Kotlin, TypeScript, or grammar code in this story; it is documentation-only.

### Architecture Compliance

- The story is only successful if `docs/usages/m17-proof-usage.md`, `examples/m17/README.md`, and `_bmad-output/implementation-artifacts/m17/README.md` tell one consistent story about M17's scope, fixtures, and verification commands, with no contradictions a reviewer would need to resolve manually.
- Prevent these failure modes:
  - Recording a future-syntax landing zone that quietly implies Tree-sitter or generated parse trees could become canonical truth "just for imports," which would violate AD-104/AD-111/AD-106 simultaneously.
  - Writing verification-path claims that are not backed by an actual test or fixture from Stories `5.1`/`5.2`, turning the closeout note into unverified narrative (the exact problem AD-113 and FR-10 exist to prevent).
  - Letting this story creep into implementing the `import` seed itself instead of only documenting the landing zone for it.

### Library / Framework Requirements

- Use the repo-approved stack references already frozen by the workspace when citing versions in the note:
  - Java `25`
  - Kotlin `2.4.0`
  - Gradle `9.6.1`
  - Node.js `22+`, Yarn `1.22.22`, Eclipse Theia `1.73.1` (per the architecture spine's Stack table, for the Tree-sitter/Theia verification step)
- No new dependencies are introduced by this story.

### File Structure Requirements

- Expected new files:
  - `docs/usages/m17-proof-usage.md`
- Expected update files (consistency pass only, no scope widening):
  - `examples/m17/README.md` (if the cross-check in Task 2 finds drift)
  - `_bmad-output/implementation-artifacts/m17/README.md` (if the cross-check finds drift)
- Do not create a Chinese counterpart unless the team's existing `docs/usages/` convention requires it; check whether other `docs/usages/mXX-proof-usage.md` files have `.zh-CN.md` siblings before deciding, and follow whatever the existing convention is.

### Testing Requirements

- This story is documentation-only; there is no dedicated Gradle test target.
- Re-run the verification commands actually referenced in the new usage note to confirm they are accurate and current, sequentially on Windows with Java 25, before finalizing the note:
  - the exact `kernel:compiler` test command(s) referenced from Story `5.1`
  - the exact `kernel:language`/`kernel:compiler` test command(s) referenced from Story `5.2`
  - any Epic 3/Epic 4 command already established by the time this story is written
- Run `powershell -ExecutionPolicy Bypass -File .\tools\encoding-audit.ps1` after adding the new documentation file.

### Explicit Non-Goals

- No `import` grammar, AST node, or lowering implementation in this story.
- No new Kotlin, TypeScript, `ANTLR4`, or Tree-sitter code in this story.
- No re-authoring of the Story `5.1`/`5.2` fixture sets; this story references them.
- No general documentation audit beyond the M17 usage note and the two existing M17 README files.

### Previous Milestone Intelligence

- Every prior milestone from M2 onward published a `docs/usages/mXX-proof-usage.md` closeout note (`m16-proof-usage.md` is the most recent and most directly reusable template); M17 should not deviate from this established documentation convention.
- The M17 PRD explicitly flags future syntax (imports, package-aware declarations) as the reason M17 exists; Story `5.3` is the story that turns that stated motivation into a concrete, citable landing-zone record rather than leaving it only in planning documents that later milestones might not re-read.

### References

- [Source: _bmad-output/planning-artifacts/epics-M17-2026-07-14.md]
- [Source: _bmad-output/planning-artifacts/prds/prd-Athena-2026-07-14-m17/prd.md]
- [Source: _bmad-output/planning-artifacts/prds/prd-Athena-2026-07-14-m17/addendum.md]
- [Source: _bmad-output/planning-artifacts/architecture/architecture-Athena-2026-07-14-m17/ARCHITECTURE-SPINE.md]
- [Source: _bmad-output/implementation-artifacts/m17/README.md]
- [Source: docs/usages/m16-proof-usage.md]
- [Source: examples/README.md]

## Dev Agent Record

### Agent Model Used

Sonnet 5 (Cursor subagent)

### Debug Log References

- None. Documentation-only verification; no code changes.

### Completion Notes List

- Confirmed `docs/usages/m17-proof-usage.md` exists and follows the `docs/usages/m16-proof-usage.md` shape (`Purpose`, `Future Syntax Landing Zone`, `Verification Path`, `Product Position`).
- Confirmed the "Future Syntax Landing Zone" section states the compiler-parser -> authored-AST -> lowering -> `Engineering IR` chain, cites AD-104/AD-111/AD-106/AD-107 by name, references Story 1.3's AST extensibility, and records that M17 did **not** seed an `import` token or grammar rule (resolving the PRD's Open Question 2 explicitly), plus an explicit exclusions list matching the PRD Non-Goals and architecture spine Deferred section.
- Confirmed the "Verification Path" section lists, in order: (a) valid-source parity (Story 5.1, `:kernel:compiler:test --tests *M17*`), (b) malformed/incomplete diagnostics (Story 5.2, `:kernel:language:test --tests *M17*`), (c) Tree-sitter syntax UX (Epic 3, `yarn --cwd ide/tree-sitter-athena test`), and (d) LSP semantic-authority continuity (Epic 4, Stories 4.1-4.3 test commands), followed by a full sequential run block ending in the encoding audit.
- Cross-checked `docs/usages/m17-proof-usage.md`, `examples/m17/README.md`, and `_bmad-output/implementation-artifacts/m17/README.md` for naming/command consistency; found no drift in fixture folder names or test-class names across the three documents.
- Confirmed the closing "Product Position" section paraphrases the architecture spine's and PRD's Final Statement framing.
- Verified the (c) Tree-sitter command references Epic 3 artifacts (grammar, corpus tests) that exist in `ide/tree-sitter-athena`, while noting the compiled `.wasm` artifact was still pending (SDK download in progress) at this verification pass; this does not block Story 5.3's documentation-only scope since the command and Epic 3 stories are otherwise already recorded correctly.
- No `import` grammar, AST node, or lowering code was introduced; no re-authoring of Story 5.1/5.2 fixtures occurred.

### File List

- `docs/usages/m17-proof-usage.md` (verified, no changes needed)

## Story Completion Status

- Status: done
- Completion note: Verified by reading `docs/usages/m17-proof-usage.md` end to end against `examples/m17/README.md` and `_bmad-output/implementation-artifacts/m17/README.md`. All four acceptance criteria are satisfied: the future-syntax landing zone, the four-part verification path, the explicit exclusions, and cross-document consistency are all present and non-contradictory.
