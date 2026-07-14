# M16 Addendum

This addendum sharpens the implementation direction for M16 without replacing the future architecture spine.

## 1. Product Definition

Recommended milestone name:

```text
M16
Semantic Macro And Reuse Foundation
```

Do not reduce M16 to:

```text
package system milestone
graphic macro milestone
```

because M16 exists above M5 package governance and above graphic projection.

## 2. Mission Statement

Recommended statement:

> Prove that Athena can reuse parameterized engineering assemblies as governed semantic modules without turning graphics, copy-paste, or package metadata into the source of truth.

## 3. Core Architecture Position

The correct chain is:

```text
Reuse Catalog / Forms / Templates / AI / DSL / API
        ->
Semantic Macro Instantiation
        ->
Preview
        ->
Athena Command Runtime
        ->
Engineering IR
        ->
M14 Component Knowledge
        ->
M13 Presentation / Workbench / Docs
```

Avoid:

```text
graphic block
    ->
engineering truth
```

or:

```text
package feature
    ->
semantic reuse authority
```

Those paths would erode the architecture.

## 4. Recommended Contract Shape

The reusable unit in M16 should be:

```text
Semantic Macro
```

It should contain:

```text
component template set
connection template set
parameter contract
default knowledge metadata
presentation hints
documentation hints
```

It should not contain:

```text
pixel truth
SVG truth
manual layout truth
graphic copy block
```

### 4.1 Suggested Types To Evaluate

Architecture should evaluate a narrow contract family such as:

```text
SemanticMacroIdentity
SemanticMacroParameter
SemanticMacroTemplate
SemanticMacroInstantiation
SemanticMacroExpansion
InstantiationOrigin
```

This naming stays above M5 package ownership and below runtime expansion.

### 4.2 Example Mental Model

Example reusable macro:

```text
electrical.dol-starter
```

Parameters:

```text
motorPower
controlVoltage
vendorFamily
tagPrefix
```

Expansion result:

```text
QF1
KM1
FR1
M1
power wiring
control wiring
presentation occurrences
knowledge-ready structure
```

The engineer inserts:

```text
one assembly
```

Athena expands:

```text
many governed semantic objects
```

## 5. Runtime And Compiler Responsibility

The compiler/runtime responsibility is:

```text
resolve reusable semantic assembly
validate parameters
build preview expansion
publish accepted expansion through M8
preserve origin traceability
```

This means:

```text
Macro Selection
        ->
Parameter Validation
        ->
Preview
        ->
Accept
        ->
Mutation Expansion
        ->
Engineering IR update
```

M16 should not introduce:

```text
another mutation path
another package core
another frontend truth model
```

## 6. Suggested Delivery Components

### 6.1 Guided Reuse Catalog

Recommended first workbench surface:

- module catalog or assembly palette
- list available Semantic Macros from governed repository context
- narrow proof entries such as `DOL Starter`, `PLC Rack`, and `24V Distribution Unit`
- choosing one entry starts configured instantiation rather than graphic paste

### 6.2 Parameter Editor And Preview

Recommended second surface behavior:

- user edits meaningful engineering parameters
- Athena validates parameter values before preview
- preview shows semantic consequences instead of only a confirmation dialog

### 6.3 Accepted Expansion

Required coherence proof:

- catalog action -> preview -> mutation acceptance -> `Engineering IR` -> graph update
- accepted expansion -> source update -> semantic inspection update -> review update
- no partial commit when preview is rejected

## 7. Suggested Platform Modules And Transport Direction

Architecture should evaluate:

```text
kernel/
    reuse-model
    template-model
```

and a runtime/service layer above them rather than a repurposed package module.

Non-binding transport names to evaluate later:

```kotlin
athena/listAvailableSemanticMacros
athena/previewSemanticMacroExpansion
athena/acceptSemanticMacroExpansion
athena/inspectExpansionOrigin
```

These are directional names only until the architecture spine freezes transport contracts.

## 8. Traceability Requirements

Every accepted expansion should preserve:

- Semantic Macro identity
- explicit instantiation identity
- parameter values used for that expansion
- membership mapping for expanded semantic subjects

This is what later enables:

- update
- replace
- diff
- review
- lint
- AI reasoning

## 9. Explicit Exclusions

M16 should not include:

- symbol editor
- SVG editor
- graphic block library
- full package marketplace
- multi-company federation
- final automatic schematic generation
- final routing engine
- unrestricted rule-engine expansion

## 10. Recommended Proof Scenario

The best narrow M16 proof remains:

1. Open a governed repository.
2. Choose `DOL Starter` from the reuse catalog.
3. Configure `motorPower = 7.5kW`, `controlVoltage = 24VDC`, `vendorFamily = Siemens`, `tagPrefix = M1`.
4. Review a preview showing components added, ports added, connections added, and presentation consequences.
5. Accept the preview.
6. Verify source, graph, semantic inspection, and review state all refresh coherently.
7. Verify the resulting structure preserves origin traceability to `electrical.dol-starter` and the selected parameter set.
