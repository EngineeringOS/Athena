# M23 Addendum

## Correction From M22

M22 selected this layout block shape:

```athena
layout schematic-sheet {
  place HMI1 near PLC1
  place XT1 below PLC1
  align HMI1 aligned-with PLC1 axis vertical
  group HMI1 grouped-with PLC1
}
```

But M22 did not admit it into the real language. The current grammar scope is package/import/system
with device, port, and connect declarations only. The current authored AST has no layout declaration.
The M22 sample sources do not use the layout block because doing so would break parser/compiler/LSP
acceptance.

M23 should treat this as a product truth correction, not as optional polish.

## Recommended Architecture Direction

Add syntax support without making generated ANTLR types visible downstream:

```text
ANTLR layoutDecl + Tree-sitter layout node
  -> Athena-owned LayoutDeclaration AST
  -> Layout Intent Model
  -> compiler layout hint admission
  -> layout constraint snapshot
  -> layout facts
  -> renderer
```

The authored AST should stay syntax-only. Semantic meaning and canonical subject binding remain
compiler responsibilities.

ANTLR4 and Tree-sitter must be upgraded together. ANTLR4 owns compiler/LSP parsing. Tree-sitter owns
IDE editor parsing/highlighting/structural feedback. M23 is not done if only one parser accepts the
new syntax.

## Layout Ownership

M23 should admit layout blocks inside `system { ... }` first:

```athena
system MachineNo000 {
  layout schematic-sheet {
    place HMI1 near PLC1
  }
}
```

Reason:

- layout belongs to a system representation in the current language shape
- file-global layout policy, project style, and company standards are later hierarchy levels
- system scope lets M23 keep subject binding and diagnostics local and understandable

## Candidate AST Shape

```kotlin
data class LayoutDeclaration(
    val viewFamily: String,
    val statements: List<LayoutStatement>,
    override val span: SourceSpan,
) : Declaration

sealed interface LayoutStatement {
    val subject: String
    val target: String
    val span: SourceSpan
}
```

Initial statement variants:

- `PlaceNear`
- `PlaceBelow`
- `AlignWith(axis)`
- `GroupWith`

## Candidate Layout Intent Shape

AST should not lower directly to solver constraints. Insert a layout intent model:

```kotlin
data class LayoutIntent(
    val subject: AuthoredSubjectRef,
    val relation: LayoutRelation,
    val target: AuthoredSubjectRef,
    val axis: LayoutAxis? = null,
    val priority: LayoutPriority = LayoutPriority.Preference,
    val sourceSpan: SourceSpan,
)
```

The compiler then maps intent into layout constraints. This preserves the difference between syntax
(`place HMI1 near PLC1`) and solver interpretation (`same region`, `distance preference`, `same page`
or other domain-specific rules).

## Priority Model

M23 should add priority to the model now, even if explicit source keywords remain deferred:

```kotlin
enum class LayoutPriority {
    Hard,
    Soft,
    Preference,
}
```

Default for authored M23 hints:

```text
Preference
```

Future source shape if needed:

```athena
layout schematic-sheet {
  prefer place HMI1 near PLC1
  require PLC1 aligned-with PowerSupply1 axis vertical
}
```

Do not admit `prefer` / `require` in M23 unless it stays small and fully covered by parser/compiler/LSP
tests. The model support is the important architectural foothold.

## Graph Workbench Serialization Rule

The graph workbench should not hand-build layout source strings as its authority. Correct flow:

```text
Graph adjustment event
  -> Layout adjustment intent
  -> Layout intent model
  -> Athena layout source serializer
  -> accepted `.athena` layout block
```

This prevents future syntax changes from breaking the UI layer and keeps Theia a projection consumer.

## Diagnostic Expectations

Syntax diagnostics:

- missing view family after `layout`
- missing `{` or `}`
- malformed `place`, `align`, or `group` statement
- invalid axis value

Semantic diagnostics:

- unknown subject
- unknown target
- duplicate contradictory hint
- hint references a subject outside the active package/source scope

## Sample Project Requirement

M23 must include the selected layout block in real `.athena` source under
`../../../examples/m23/sample-project/src`. It is not enough to mention the block in markdown or
frontend tests.

Required starting source:

- `../../../examples/m23/sample-project/src/01-layout-hints.athena`

This file must contain a real system-scoped layout block and compile cleanly through ANTLR4,
compiler, LSP, and the Theia editor path.

## Parser Parity Fixtures

M23 should add paired ANTLR4 and Tree-sitter fixtures for:

- valid `layout schematic-sheet { ... }`
- valid `place SUBJECT near TARGET`
- valid `place SUBJECT below TARGET`
- valid `align SUBJECT aligned-with TARGET axis horizontal`
- valid `align SUBJECT aligned-with TARGET axis vertical`
- valid `group SUBJECT grouped-with TARGET`
- malformed `place`
- invalid `axis`
- unknown subject semantic diagnostic
- rejected file-global `layout` block

## Boundary Rule

M23 should not introduce advanced layout intelligence. It should make existing layout hints real,
valid, inspectable, and round-trippable. Better layout quality can continue after this source
contract is truthful.
