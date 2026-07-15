---
baseline_commit: f4510fd64041548ad8f064f6396db92f2de0178b
---

# Story 3.2: Integrate Tree-sitter Into One Supported Theia Syntax UX Path

Status: done

## Story

As an IDE engineer,
I want Athena to use Tree-sitter for one real syntax UX path in Theia,
so that M17 proves editor value instead of shipping a detached grammar only.

## FR Traceability

- FR-5: Athena can publish a Tree-sitter grammar for Athena source (consumed here, not re-defined).
- FR-6: Athena can use Tree-sitter for syntax UX rather than semantic truth.
- NFR-4: Editor syntax parsing remains error tolerant and low latency.
- NFR-5: The M17 architecture must make future syntax additions cheaper, not harder.

## Acceptance Criteria

1. Given Story `3.1`'s compiled `ide/tree-sitter-athena/tree-sitter-athena.wasm` and `queries/highlights.scm`, when `ide/theia-frontend` loads an `.athena` document, then Athena registers a Tree-sitter-backed `monaco.languages.DocumentSemanticTokensProvider` for `ATHENA_LANGUAGE_ID` inside the existing `AthenaLspEditorBridgeService.registerAthenaLanguageProviders()` seam, so keyword/identifier/string/qualified-name/operator highlighting is driven by the real Tree-sitter parse tree rather than only the current regex-based Monarch tokenizer.
2. Given the integration path is reviewed, when ownership is inspected, then all new frontend code lives inside `ide/theia-frontend/src/browser/` (or a small dedicated file alongside it), the existing Monarch tokenizer/`athenaLanguageConfiguration` in `athena-language-definition.ts` remain in place unmodified as the baseline tokenizer, and no new Theia widget, no reparented stock outline/editor widget, and no direct `ide/theia-frontend` import of `kernel/*` Kotlin code is introduced.
3. Given `ide/lsp` already owns document symbols (`AthenaLanguageFeatures.documentSymbols`, wired through `registerDocumentSymbolProvider` in `athena-lsp-editor-bridge-service.ts`), when Story `3.2`'s UX choice is made, then Tree-sitter is used for highlighting only in this story — outline/document-symbol behavior explicitly continues to come from `ide/lsp`, and this story does not register a competing Tree-sitter-backed outline or folding provider.
4. Given the chosen UX path is exercised end to end, when a `.athena` file is opened, edited, and re-highlighted in the running Theia product, then highlighting updates incrementally as Tree-sitter re-parses on each edit, the `.wasm` grammar loads correctly inside the Electron/browser frontend runtime (not just in Node test scripts), and at least one automated frontend test and one Electron smoke script prove the tokens provider is registered and produces non-empty, keyword-aware token output for a real fixture.

## Tasks / Subtasks

- [ ] Depend on the Story `3.1` grammar package the same way `athena-graph-glsp` is consumed today. (AC: 1, 2)
  - [ ] Add `"@engineeringood/athena-tree-sitter-grammar": "link:../tree-sitter-athena"` to `ide/theia-frontend/package.json` dependencies, mirroring the existing `"@engineeringood/athena-graph-glsp": "link:../../integrations/graph-glsp"` pattern.
  - [ ] Add `web-tree-sitter` (`^0.26`) as a runtime dependency of `ide/theia-frontend`.
  - [ ] Ensure the compiled `tree-sitter-athena.wasm` ships as a static asset the frontend bundle can fetch at runtime (browser `fetch`/Theia backend static file serving), and use `Parser.init({ locateFile })` so the loader resolves both `web-tree-sitter`'s own runtime `.wasm` and the grammar `.wasm` correctly under Theia's bundled asset paths rather than assuming a same-directory default.
- [ ] Add one small, focused Tree-sitter highlighting bridge inside `ide/theia-frontend`. (AC: 1, 2)
  - [ ] Create `ide/theia-frontend/src/browser/athena-tree-sitter-highlighting-service.ts` that lazily initializes `web-tree-sitter`, loads the grammar `.wasm`, parses the current Monaco model text on open/change (debounced to stay off the UI thread's critical path), runs `queries/highlights.scm` over the resulting tree, and maps captures to Monaco `SemanticTokensLegend`/token data.
  - [ ] Keep this file a thin adapter: it must not re-implement grammar rules, must not fabricate semantic meaning from capture names, and must degrade gracefully (fall back to the existing Monarch highlighting only, never crash the editor) if the `.wasm` fails to load or a parse throws.
  - [ ] Bind the new service alongside the existing `FrontendApplicationContribution`/singleton bindings in `athena-frontend-module.ts`, following the same `bind(...).toSelf().inSingletonScope()` style already used for `AthenaLspEditorBridgeService`, `AthenaGraphAdapterService`, etc.
- [ ] Register the Tree-sitter-backed semantic tokens provider in the existing language-registration seam. (AC: 1, 2, 3)
  - [ ] Extend `AthenaLspEditorBridgeService.registerAthenaLanguageProviders()` in `ide/theia-frontend/src/browser/athena-lsp-editor-bridge-service.ts` to additionally call `monaco.languages.registerDocumentSemanticTokensProvider(ATHENA_LANGUAGE_ID, ...)`, backed by the new highlighting service, and push the returned disposable into the same `this.languageProviderListeners` collection already used for the Monarch tokens provider, completion, document-symbol, definition, and reference providers.
  - [ ] Do not remove or replace `monaco.languages.setMonarchTokensProvider(ATHENA_LANGUAGE_ID, athenaMonarchLanguage)`; semantic tokens layer on top of (they do not replace) Monaco's base tokenizer, which keeps this change additive per AD-112 and keeps bracket-matching/auto-closing behavior from `athenaLanguageConfiguration` untouched.
  - [ ] Do not touch `registerDocumentSymbolProvider`, `registerDefinitionProvider`, or `registerReferenceProvider` in this story; those stay backed by `ide/lsp` per AC 3.
- [ ] Prove the chosen UX path works, not just compiles. (AC: 4)
  - [ ] Add a frontend unit test (e.g. `ide/theia-frontend/scripts/athena-tree-sitter-highlighting-service.test.mjs`, following the existing `scripts/*.test.mjs` convention) that loads the real `.wasm`, parses a real fixture such as `examples/m0/demo-cabinet.athena`, and asserts the resulting token data is non-empty and classifies at least the `system`/`device`/`port`/`connect` keywords and one string literal correctly.
  - [ ] Extend one existing Electron smoke script (or add a small dedicated one under `ide/theia-product/scripts/`, following the pattern of `verify-athena-reuse-catalog.js`) that opens a real `.athena` fixture in the running product and asserts the semantic tokens provider produced tokens for the visible editor, proving the `.wasm` actually loads inside the Electron/browser runtime and not only inside a Node test harness.
  - [ ] Wire the new smoke script into `ide/package.json` (`start:smoke:tree-sitter` or similar, following the existing `start:smoke:reuse-catalog` naming) so it is discoverable the same way other M-series smoke paths are.
- [ ] Keep Story `3.2` narrow. (AC: 1, 2, 3, 4)
  - [ ] Do not add folding-range behavior in this story; AD-107 allows it later, but the epic only requires "at least one" serious UX capability, and highlighting is this story's chosen one.
  - [ ] Do not reparent, hide, or replace any stock Theia outline/editor widget; keep the M15 carry-forward guardrail against unsupported Theia layout hacks in force.
  - [ ] Do not change `ide/lsp` diagnostics, completion, or navigation behavior.
  - [ ] Do not modify `ide/tree-sitter-athena/grammar.js` or its queries beyond what is strictly needed to consume them (any grammar fix belongs back in Story `3.1`'s scope, filed as a follow-up if discovered here).

## Dev Notes

### Story Intent

- Story `3.2` is the "prove it in the product" story for Epic 3: it takes the already-tested grammar package from Story `3.1` and wires exactly one real, visible syntax UX capability into the actual Theia product shell.
- The success condition is not "the grammar is imported somewhere." It is "opening a real `.athena` file in the running Athena desktop product shows Tree-sitter-driven syntax highlighting, and an automated test plus an Electron smoke script both prove that, independent of manual inspection."
- Highlighting was chosen over outline or folding for this story because `ide/lsp` already owns a working, tested outline path (`documentSymbols`), and M15's carry-forward guardrail explicitly warns against unsupported Theia layout experiments if outline is part of any proof — reusing the already-supported LSP outline path and adding Tree-sitter-backed highlighting alongside it is the lower-risk, architecture-aligned choice. Folding remains a plausible future addition but is not required to satisfy "at least one serious UX capability."
- Story `3.3` reuses this exact highlighting path (not a new one) to prove incomplete/malformed-source tolerance, so the highlighting service built here must already be resilient to parse errors by construction, not hardened only after the fact.

### Architecture Guardrails

- Align to AD-107: Tree-sitter is introduced only for syntax-oriented editor behavior such as highlighting. Story `3.2` must keep the new tokens provider a pure syntax classifier and must not let it grow into resolution, diagnostics, or engineering meaning. [Source: _bmad-output/planning-artifacts/architecture/architecture-Athena-2026-07-14-m17/ARCHITECTURE-SPINE.md#AD-107---Tree-sitter-Owns-Syntax-UX-Only]
- Align to AD-108: `ide/lsp` remains the sole semantic entry point for IDE language meaning; syntax/semantic diagnostics exposed through LSP continue to derive from compiler-owned parsing. Story `3.2` must not let the new highlighting path emit diagnostics, markers, or problem-panel entries — that stays on the existing `sendLanguageRequest('textDocument/...')` / `problemManager` path already owned by `AthenaLspEditorBridgeService`. [Source: _bmad-output/planning-artifacts/architecture/architecture-Athena-2026-07-14-m17/ARCHITECTURE-SPINE.md#AD-108---LSP-Semantic-Diagnostics-Stay-On-The-Compiler-Parser-Path]
- Align to AD-110: the first M17 IDE proof stays parity-first on the current supported syntax subset. Story `3.2` integrates the Story `3.1` grammar as-is; it must not motivate widening the grammar to make highlighting "look nicer" for hypothetical future syntax. [Source: _bmad-output/planning-artifacts/architecture/architecture-Athena-2026-07-14-m17/ARCHITECTURE-SPINE.md#AD-110---The-First-M17-Proof-Stays-Parity-First-On-The-Current-Supported-Syntax-Subset]
- Align to AD-112: IDE integration remains additive to the existing product path; Tree-sitter integration must enter through existing `ide/theia-frontend` and `ide/lsp` seams, and this story does not justify direct kernel imports into frontend code or unsupported widget-layout hacks. Story `3.2`'s entire change surface is the existing `AthenaLspEditorBridgeService` provider-registration seam plus one new adapter file — no new widget, no new panel, no framework fork. [Source: _bmad-output/planning-artifacts/architecture/architecture-Athena-2026-07-14-m17/ARCHITECTURE-SPINE.md#AD-112---IDE-Integration-Remains-Additive-To-The-Existing-Product-Path]
- Preserve inherited AD-88/AD-90 (from M15): workbench surfaces stay consumers of shared platform services, and cross-surface synchronization stays canonical-state-first. The new highlighting service must stay a pure, disposable, editor-local rendering aid — it must never become a second source of truth about document structure that other Athena surfaces start depending on. [Source: _bmad-output/planning-artifacts/architecture/architecture-Athena-2026-07-14-m17/ARCHITECTURE-SPINE.md#Inherited-Invariants]

### Current Code State To Preserve

- `ide/theia-frontend/src/browser/athena-language-definition.ts` already defines `ATHENA_LANGUAGE_ID = 'athena'`, `athenaLanguageConfiguration` (brackets, auto-closing pairs, colorized bracket pairs), and `athenaMonarchLanguage` (the current regex-based Monarch tokenizer for keywords/strings/operators/qualified identifiers). This story must not delete or rewrite this file; the new semantic tokens provider is additive.
- `ide/theia-frontend/src/browser/athena-lsp-editor-bridge-service.ts` already owns `registerAthenaLanguage()` (registers the language id once, sets language configuration, ensures model language on create) and `registerAthenaLanguageProviders()` (sets the Monarch tokens provider, and registers completion/document-symbol/definition/reference providers backed by `sendLanguageRequest(...)` calls into `ide/lsp`), all tracked through `this.languageProviderListeners: DisposableCollection`. This is the exact, only seam Story `3.2` should extend — do not create a second, parallel registration path.
- `ide/lsp/src/main/kotlin/com/engineeringood/athena/ide/lsp/AthenaLanguageFeatures.kt` already implements `documentSymbols(...)` from the compiler-owned `SourceFileAst`, and `athena-lsp-editor-bridge-service.ts` already wires that through `registerDocumentSymbolProvider`. This is the existing, working outline path this story must not duplicate or compete with.
- `ide/theia-frontend/src/browser/athena-frontend-module.ts` already shows the exact Inversify binding style (`bind(X).toSelf().inSingletonScope()`, `bind(FrontendApplicationContribution).toService(X)`) used for every existing frontend service; the new highlighting service should follow this same style rather than inventing a new bootstrap mechanism.
- `ide/package.json` already links `integrations/graph-glsp` into `ide/theia-frontend` via `"@engineeringood/athena-graph-glsp": "link:../../integrations/graph-glsp"` without adding that sibling package to the `workspaces` array; this is the exact precedent for how `ide/tree-sitter-athena` should be consumed once Story `3.1` exists.
- `ide/theia-product/package.json` already exposes `start:smoke` and `start:smoke:reuse-catalog` scripts backed by `ide/theia-product/scripts/verify-athena-start.js` and `verify-athena-reuse-catalog.js`; a new `start:smoke:tree-sitter`-style script should follow that same Electron smoke-test shape.

### Technical Requirements

- Use `web-tree-sitter` (`^0.26`) in the browser/Electron renderer context; call `Parser.init({ locateFile(scriptName, scriptDirectory) { ... } })` once at frontend startup so both the `web-tree-sitter` runtime wasm and the grammar wasm resolve correctly under Theia's asset-serving paths, not the library's same-directory default (which breaks under bundler-rewritten asset URLs, as documented by the upstream project).
- Debounce re-parse-on-edit (e.g. via Monaco's `onDidChangeModelContent` with a short delay) so Tree-sitter's incremental parsing benefits are realized without re-tokenizing on every keystroke synchronously on the UI thread.
- Treat any Tree-sitter load/parse failure as a soft failure: log through the existing `AthenaLspEditorBridgeService` error-reporting path (`reportBridgeFailure` or equivalent) and leave the Monarch tokenizer as the visible fallback; never let a Tree-sitter failure break editing.
- Keep the new service's public surface small and typed: one method to (re)highlight a given Monaco model, returning the semantic tokens data Monaco expects.

### Architecture Compliance

- The story is only successful if, after this change, a reviewer can point to exactly one additional call inside `registerAthenaLanguageProviders()` and one new adapter file as "the Tree-sitter integration," with no new widescreen panel, no duplicated LSP responsibility, and no kernel import.
- Prevent these failure modes:
  - Tree-sitter output silently becoming the input to a new diagnostics/markers path (that would violate AD-108).
  - A second, Tree-sitter-backed outline/document-symbol provider competing with the existing LSP-backed one.
  - Reparenting or restyling Theia's built-in outline/problems widgets to "make room" for Tree-sitter UI (explicitly forbidden by the M15 carry-forward guardrail).
  - Loading the grammar `.wasm` synchronously on the UI thread in a way that blocks editor startup, or failing to handle a load error gracefully.
  - Quietly depending on `ide/tree-sitter-athena` internals (e.g. reaching into `src/parser.c`) instead of the package's public `tree-sitter-athena.wasm` + `queries/highlights.scm` artifacts.

### Library / Framework Requirements

- `web-tree-sitter` `^0.26` as a new runtime dependency of `ide/theia-frontend`.
- `@engineeringood/athena-tree-sitter-grammar` (Story `3.1`'s package) as a `link:../tree-sitter-athena` dependency of `ide/theia-frontend`, mirroring the existing `athena-graph-glsp` link pattern.
- Continue using `@theia/monaco` `1.73.1` and the existing Monaco `languages.*` API surface already used throughout `athena-lsp-editor-bridge-service.ts`; do not add a competing editor-integration library.
- No change to `@theia/core`, `@theia/editor`, or any other pinned Theia package version.

### File Structure Requirements

- New files:
  - `ide/theia-frontend/src/browser/athena-tree-sitter-highlighting-service.ts`
  - `ide/theia-frontend/scripts/athena-tree-sitter-highlighting-service.test.mjs`
  - `ide/theia-product/scripts/verify-athena-tree-sitter-highlighting.js` (or an extension of an existing smoke script, if that proves cleaner once the highlighting service exists)
- Updated files:
  - `ide/theia-frontend/package.json` (new dependencies)
  - `ide/theia-frontend/src/browser/athena-frontend-module.ts` (bind the new service)
  - `ide/theia-frontend/src/browser/athena-lsp-editor-bridge-service.ts` (register the semantic tokens provider inside `registerAthenaLanguageProviders()`)
  - `ide/package.json` (new smoke script entry)
  - `ide/theia-frontend/README.md` / `README.zh-CN.md` (document the new highlighting capability and its Tree-sitter dependency)
- Files that must remain behaviorally unchanged by this story:
  - `ide/theia-frontend/src/browser/athena-language-definition.ts`
  - `ide/lsp/src/main/kotlin/com/engineeringood/athena/ide/lsp/AthenaLanguageFeatures.kt`
  - `ide/tree-sitter-athena/grammar.js` and `queries/highlights.scm` (consume, do not modify)

### Testing Requirements

- `yarn workspace @engineeringood/athena-theia-frontend test` (runs `tsc -b --force` then `node --test scripts/*.test.mjs`, including the new highlighting test).
- `yarn build` at the `ide/` root to prove the full product still builds with the new dependency graph.
- The new (or extended) `yarn start:smoke:...` Electron script, proving the tokens provider produces real output against a real fixture inside the actual running product.
- Keep the JVM Gradle suite untouched by this story; no new Gradle verification command is expected here since no Kotlin code changes.
- Run `powershell -ExecutionPolicy Bypass -File .\tools\encoding-audit.ps1` after any README updates.

### Explicit Non-Goals

- No folding-range provider in this story.
- No new or competing outline/document-symbol provider; outline stays on the existing `ide/lsp` path.
- No new Theia widget or panel.
- No reparenting or restyling of stock Theia widgets.
- No diagnostics, markers, or problem-panel entries derived from Tree-sitter output.
- No changes to `ide/tree-sitter-athena`'s grammar or queries beyond consuming the published artifacts.
- No incomplete/malformed-source-specific proof work; that is Story `3.3`, though this story's implementation must already be resilient enough (per Technical Requirements) for `3.3` to prove it, not to first harden it.

### Previous Milestone Intelligence

- M15's carry-forward guardrail (documented in the M17 addendum, section `11.2`) explicitly warns against "unsupported Theia layout experiments such as reparenting stock outline widgets" and says "if outline is part of the proof, keep it on a supported path" — this story satisfies that by deliberately not touching outline at all and reusing the already-supported LSP outline path.
- M16's `3.3` story (`Support Parameter Editing And Review-First Acceptance In The Workbench`) is the closest prior precedent for "extend an existing frontend service/widget plus add typed protocol/adapter code plus add both a Node test and an Electron smoke script" — Story `3.2` follows the same shape: extend `AthenaLspEditorBridgeService` rather than build a parallel service, and pair a Node unit test with an Electron smoke script rather than relying on either alone.
- `integrations/graph-glsp` proves the "sibling package linked via `link:` without joining the `ide/package.json` workspaces array" pattern already works cleanly for `ide/theia-frontend`'s build; Story `3.2` reuses that exact precedent instead of inventing a new dependency-wiring approach.

### Latest Technical Information

- `web-tree-sitter` `^0.26` requires an explicit `Parser.init()` call before any `Parser`/`Language` use, and supports a `locateFile` option specifically to handle bundler/asset-path mismatches (the documented Next.js/Webpack failure mode of the loader looking for the `.wasm` file next to the bundled JS chunk instead of at the expected static-asset path) — Theia's Webpack-based frontend bundling has the same class of path mismatch, so `locateFile` must be wired deliberately rather than left at its default.
- `DocumentSemanticTokensProvider` in Monaco is designed to layer on top of (not replace) a Monarch/basic tokens provider; this is the API-level confirmation that adding Tree-sitter-backed semantic tokens alongside the existing `athenaMonarchLanguage` Monarch provider is the additive, non-destructive integration path AD-112 requires.

### Project Structure Notes

- This story's entire diff should be explainable as: "one new dependency link, one new adapter file, one new registration call in an existing method, one test, one smoke script." Any diff shape larger than that is a signal the story has drifted beyond its scope.
- The story should make the eventual M17 narrative easy to state: "the grammar exists (`3.1`), it lights up real syntax highlighting in the real product (`3.2`), and it keeps doing so even on broken source (`3.3`) — while `ide/lsp` never stopped owning semantic truth."

### References

- [Source: _bmad-output/planning-artifacts/epics-M17-2026-07-14.md]
- [Source: _bmad-output/planning-artifacts/prds/prd-Athena-2026-07-14-m17/prd.md]
- [Source: _bmad-output/planning-artifacts/prds/prd-Athena-2026-07-14-m17/addendum.md]
- [Source: _bmad-output/planning-artifacts/architecture/architecture-Athena-2026-07-14-m17/ARCHITECTURE-SPINE.md]
- [Source: _bmad-output/implementation-artifacts/m17/3-1-publish-the-tree-sitter-grammar-for-athena-syntax-ux.md]
- [Source: _bmad-output/implementation-artifacts/m16/3-3-support-parameter-editing-and-review-first-acceptance-in-the-workbench.md]
- [Source: ide/theia-frontend/src/browser/athena-language-definition.ts]
- [Source: ide/theia-frontend/src/browser/athena-lsp-editor-bridge-service.ts]
- [Source: ide/theia-frontend/src/browser/athena-frontend-module.ts]
- [Source: ide/lsp/src/main/kotlin/com/engineeringood/athena/ide/lsp/AthenaLanguageFeatures.kt]
- [Source: ide/theia-frontend/package.json]
- [Source: ide/package.json]
- [Source: ide/theia-product/package.json]
- [Source: integrations/graph-glsp/package.json]
- [Source: examples/m0/demo-cabinet.athena]

## Story Completion Status

- Status: done
- Completion note: Story `3.2` closed on 2026-07-15. `AthenaTreeSitterHighlightingService` is bound and registered through the existing `AthenaLspEditorBridgeService` seam, the default Node/Electron asset-resolution bug was fixed, `ide/theia-product/scripts/copy-tree-sitter-assets.js` now ships the grammar/query assets into the packaged frontend, `ide/theia-frontend/scripts/athena-tree-sitter-highlighting-service.test.mjs` proves the real semantic-tokens path, and `yarn --cwd ide start:smoke:tree-sitter` proves the packaged Electron product starts and loads Tree-sitter-backed highlighting successfully.
