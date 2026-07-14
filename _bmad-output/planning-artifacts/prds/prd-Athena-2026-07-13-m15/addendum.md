# M15 Addendum

This addendum sharpens the implementation direction for M15 without replacing the future architecture spine.

## 1. Product Definition

Recommended milestone name:

```text
M15
Guided Semantic Authoring Foundation
```

Do not reduce M15 to:

```text
Palette Authoring
```

because palette is only one authoring surface.

M15 should stay broad enough to cover:

- palette
- inspector
- connect flow
- graph gestures
- forms
- templates
- future AI or agent surfaces

All of those should converge through one authoring-intent boundary.

## 2. Mission Statement

Recommended statement:

> Prove that engineers can create and modify engineering intent without directly authoring canonical DSL while preserving the M8 unified mutation authority.

## 3. Core Architecture Position

The correct chain is:

```text
Palette / Inspector / Connect Flow / Graph
        ->
Authoring Intent
        ->
Authoring Service
        ->
Athena Command Runtime
        ->
Engineering IR
        ->
M14 Component Knowledge
        ->
M9 Knowledge / M13 Presentation
```

Avoid:

```text
Palette
    ->
Graph
```

or:

```text
Inspector
    ->
Frontend State
```

Those paths would erode the architecture.

## 4. Suggested Delivery Components

### 4.1 Component Panel

Recommended first workbench surface:

- Theia left sidebar Athena component panel
- list available components from M14 knowledge packs
- narrow grouping such as PLC, power supply, motor, contactor
- double-click or drag/drop emits `CreateComponentIntent`

Acceptance example:

- user double-clicks `CPU 313C`
- one governed PLC CPU instance appears in the project

### 4.2 Inspector

Recommended second workbench surface:

- Theia right sidebar Athena inspector
- shows component name, concept, vendor implementation
- shows semantic ports and minimal physical traits
- emits `UpdateComponentPropertiesIntent`

Acceptance example:

- user switches vendor implementation from `CPU 313C` to `CPU 314C`
- project definition updates through canonical mutation

### 4.3 Port-Aware Connection Flow

Recommended graph-side guided flow:

- start from one semantic port
- show only compatible targets
- emit `ConnectPortsIntent`
- show connection state in inspector

Acceptance example:

- user connects one `MPI` port to another `MPI` port
- one governed connection is created

### 4.4 Three-Way Synchronization

Required coherence proof:

- panel action -> mutation -> `Engineering IR` -> graph update
- graph action -> mutation -> `Engineering IR` -> panel update
- DSL edit -> compile -> `Engineering IR` -> panel and graph update

### 4.5 Mutation Preview

Required review-first proof:

- every guided action first becomes pending mutation preview
- preview shows semantic consequences
- user approves before commit

This keeps guided authoring aligned with M6 and M8 instead of becoming a hidden local-edit path.

## 5. Suggested Platform Modules

Architecture should evaluate:

```text
kernel/
    authoring-model
    authoring-runtime
```

These belong to platform capability, not to domain extensions.

The first workbench implementation should likely stay inside the current `ide` product grouping rather than introducing a separate top-level UI tree unless a broader repository regrouping is approved later.

## 6. Suggested LSP Requests

Non-binding request names to evaluate during architecture:

```kotlin
athena/listAvailableComponents
athena/createComponent
athena/updateComponentProperties
athena/connectPorts
```

These names are directional guidance only until the architecture spine freezes transport contracts.

## 7. Explicit Exclusions

M15 should not include:

- broad multi-vendor catalogs
- advanced routing engines
- macro libraries
- AI generation as primary workflow
- full EPLAN replacement
- second mutation path

## 8. Recommended Proof Scenario

The best narrow M15 proof remains:

1. open project
2. search `CPU313C`
3. insert PLC
4. edit tag to `PLC1`
5. add 24V supply
6. create one compatible connection
7. preview mutation
8. approve
9. verify source, graph, diagnostics, and inspector all refresh coherently
