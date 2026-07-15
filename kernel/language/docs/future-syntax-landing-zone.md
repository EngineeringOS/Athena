# Future Syntax Landing Zone

This note sketches how Athena intends to absorb post-M17 authored constructs such as
`import`. It is **documentation only**. M17 does not implement import syntax, import
resolution, package-aware authored semantics, or any new compiled `Declaration` /
`ScalarValue` variant.

Cross-reference: Epic 5 Story `5.3` (`publish-the-future-syntax-landing-zone-note-and-verification-path`)
publishes the milestone-level closeout note. This module-level note is the code-adjacent
groundwork that Story `5.3` can cite.

## Alignment

- **AD-110**: M17 prepares for future constructs such as `import` but does not ship final
  semantics.
- **AD-111**: Future syntax growth lands through AST extensibility, not ad hoc grammar
  patches. Parser adaptation stays isolated; lowering stays organized around authored
  semantic categories.
- **AD-106**: Authored AST remains the only lowering input. Engineering IR must not depend
  on generated parser nodes.

## Required landing pattern

1. **Add a sealed `Declaration` variant** in `AthenaLanguageModel.kt` (for example a
   hypothetical `ImportDeclaration` carrying a qualified module/package reference and a
   `span`). Do not widen existing `DeviceDeclaration` / `PortDeclaration` /
   `ConnectionDeclaration` fields to overload them with new meaning.
2. **Adapt source -> AST** inside the internal
   `com.engineeringood.athena.language.antlr` seam established by Story `1.2` plus the
   Epic 2 ANTLR closeout. Never expose parse-tree types through the public facade.
3. **Handle the new variant through an exhaustive `when`** at every consumer that
   classifies or lowers `Declaration` values (`:kernel:compiler` / domain lowering,
   LSP symbol features, etc.). Adding a sealed variant without updating those `when`
   sites must fail compilation - silent `else -> {}` / `filterIsInstance`-only paths that
   drop unknown kinds are the failure mode this landing zone rejects.

`ScalarValue` remains the extension point for **field-level** literal kinds. Top-level
authored constructs such as `import` extend `Declaration`, not `ScalarValue`.

## Dual-parser upgrade standard

Every future language upgrade after M17 must update both parser tracks deliberately, with
different responsibilities:

1. **Compiler/LSP authority (`ANTLR4`)**
   Add or change the authoritative authored syntax in
   `kernel/language/src/main/antlr/com/engineeringood/athena/language/antlr/Athena.g4`.
2. **Authored AST boundary**
   Extend `AthenaLanguageModel.kt` and keep adaptation inside
   `com.engineeringood.athena.language.antlr`; never leak generated parser types outside that
   internal seam.
3. **Lowering / semantic meaning**
   Update lowering and every exhaustive consumer of the authored AST. New syntax is not complete
   until it either lowers intentionally or fails intentionally.
4. **IDE syntax UX (`Tree-sitter`)**
   Mirror the syntax shape in `ide/tree-sitter-athena/grammar.js` and keep the Tree-sitter side
   syntax-only. Do not add semantic meaning, resolution, diagnostics, or `Engineering IR`
   shortcuts there.
5. **Proofs**
   Add or update:
   - valid-source compiler parity proof
   - invalid/incomplete compiler failure proof
   - Tree-sitter grammar/package proof
   - frontend semantic-tokens proof
   - Electron product smoke proof when the editor path changes

If a future construct lands on ANTLR but not Tree-sitter, the compiler path may advance while IDE
syntax UX stays temporarily behind. The inverse is not acceptable: Tree-sitter must never lead the
language surface ahead of the compiler-owned syntax authority.

## Compatibility rule after M17

M17 removed the legacy handwritten compiler parser path. Future syntax upgrades must evolve the
ANTLR path and the authored AST boundary directly; do not reintroduce a fallback parser for
compatibility.

## Explicitly not in M17 (or this note)

- No compiled `ImportDeclaration` type.
- No `import` keyword in the parser grammar.
- No import resolution, module graph, or package-aware authored semantics.
- No ANTLR4 grammar (Epic 2) or Tree-sitter grammar (Epic 3) work by this note alone.
