---
baseline_commit: 4681606
---

# Story 1.3: Preserve Narrow Syntax Scope

Status: done

## Story

As a compiler engineer,
I want M18 syntax support limited to the approved package/import slice,
so that package-aware semantics do not become a broad language redesign.

## Acceptance Criteria

1. The supported M18 header remains limited to one optional `package`, zero or more plain qualified-target `import` declarations, and the existing `system` block.
2. Parser tests prove valid package/import syntax, contextual `package`/`import` identifiers, and existing M17 system-body declarations remain accepted.
3. Alias, wildcard, export, re-export, visibility, module, namespace, include, using, and unrelated declaration-family forms fail deterministically as typed syntax diagnostics.
4. No new authored AST variant, grammar keyword/rule, parser path, dependency, or public facade type is added by this story.
5. Existing exact malformed package/import diagnostics and the public facade allow-list remain green.
6. This story performs no Tree-sitter, repository lookup, semantic graph, linking, lowering, LSP, frontend, canvas, or proof-fixture work.

## Tasks / Subtasks

- [x] Add narrow-scope parser guardrails first (AC: 1-3, 5)
  - [x] Add a valid control case for the complete supported header and existing system-body declarations.
  - [x] Add table-driven rejection for broad file-header syntax such as alias, wildcard, export, visibility, module, namespace, include, and using forms.
  - [x] Add table-driven rejection for unrelated system-body declaration families and visibility-prefixed declarations.
  - [x] Assert typed, deterministic, positioned diagnostics rather than exceptions or parser-internal results.
- [x] Preserve authored syntax and facade boundaries (AC: 4-6)
  - [x] Keep production grammar, AST, adapter, compiler, LSP, frontend, and canvas code unchanged unless a failing regression proves an existing boundary defect.
  - [x] Retain the exact public language facade allow-list established by Stories 1.1 and 1.2.
- [x] Run scoped verification sequentially
  - [x] Run `:kernel:language:test`, `:kernel:compiler:test`, `:kernel:plugins:plugin-api:test`, and `:ide:lsp:test` without concurrent Gradle invocations.
  - [x] Run the encoding audit after text edits.

### Review Findings

- [x] [Review][Patch] Preserve the earliest authored parser or split-target diagnostic by source position [kernel/language/src/main/kotlin/com/engineeringood/athena/language/antlr/AthenaAntlrParseAdapter.kt]
- [x] [Review][Patch] Tolerate incomplete recovered import contexts without throwing [kernel/language/src/main/kotlin/com/engineeringood/athena/language/antlr/AthenaAntlrParseAdapter.kt]
- [x] [Review][Patch] Cover split-target precedence and bare-import recovery through the public parser [kernel/language/src/test/kotlin/com/engineeringood/athena/language/AthenaM18SyntaxScopeTest.kt]

## Dev Notes

- This is a boundary-proof story, not a syntax implementation story. Prefer test-only changes.
- Build on the public `AthenaLanguageParser` path. Generated ANTLR types remain internal and are not the acceptance surface.
- Use representative broadening attempts, not speculative implementations: `as`, `*`, `export`, `public`, `private`, `protected`, `internal`, `module`, `namespace`, `include`, `using`, and unrelated declarations such as `function`, `type`, `enum`, or `service`.
- Do not reject contextual keyword use already permitted by `ident`; scope tests must distinguish file/declaration position from identifier position.
- Keep diagnostics deterministic and Athena-owned. Exact message text is required only where Stories 1.1/1.2 already freeze it; new matrix cases must at least prove file, positive line/column, valid half-open span, and repeated-result equality.
- Add no dependency. Keep Java 25, Kotlin 2.4.0, Gradle 9.6.1, and ANTLR 4.13.2.
- The Theia frontend, EPLAN-style canvas, and Kotlin Compose desktop viewer are not involved.

### References

- [Source: `epics.md` - Epic 1, Story 1.3]
- [Source: `../../planning-artifacts/prds/prd-Athena-2026-07-15-m18/prd.md` - FR-2, NFR-4, NFR-5]
- [Source: `../../planning-artifacts/prds/prd-Athena-2026-07-15-m18/addendum.md` - Sections 4, 6, 12]
- [Source: `../../planning-artifacts/architecture/architecture-Athena-2026-07-15-m18/ARCHITECTURE-SPINE.md` - AD-2, AD-4]
- [Source: `1-2-parse-import-declarations-into-authored-ast.md` - parser diagnostics and review learnings]

## Dev Agent Record

### Agent Model Used

GPT-5 Codex

### Debug Log References

- Characterization: broad header/body syntax cases already failed through the public parser as required.
- Regression RED: the valid contextual-`import` control failed because Story 1.2's token guard scanned system-body identifier tokens.
- GREEN: scoping the same-line target guard to parsed file-header `importDecl()` contexts restored contextual identifiers while preserving missing-target diagnostics.
- Verification: `:kernel:language:test`, `:kernel:compiler:test`, `:kernel:plugins:plugin-api:test`, and `:ide:lsp:test` passed sequentially.
- Encoding: `tools/encoding-audit.ps1` passed.
- Review: Blind and Edge reviewers identified diagnostic precedence/recovery hardening; Acceptance Auditor found no AC violations. All three patch actions were applied and verified.

### Completion Notes List

- Ultimate context engine analysis completed - narrow-scope test contract prepared.
- Added public-parser characterization coverage for the supported M18 header and existing declarations.
- Added deterministic rejection matrices for broad header, visibility, and unrelated declaration-family forms.
- Fixed the import-target guard to inspect only file-header import declarations, preserving contextual `import` identifiers in the system body.
- Preserved first-error ordering across parser and split-target diagnostics and hardened incomplete recovery contexts.

### File List

- `_bmad-output/implementation-artifacts/m18/1-3-preserve-narrow-syntax-scope.md`
- `_bmad-output/implementation-artifacts/m18/sprint-status.yaml`
- `kernel/language/src/main/kotlin/com/engineeringood/athena/language/antlr/AthenaAntlrParseAdapter.kt`
- `kernel/language/src/test/kotlin/com/engineeringood/athena/language/AthenaM18SyntaxScopeTest.kt`

## Change Log

- 2026-07-15: Added M18 syntax-scope guardrails and corrected contextual import guard scoping; completed required sequential verification.
- 2026-07-15: Applied all code-review findings and repeated full scoped verification.
