# Athena M29 Proof Usage

M29 makes interaction a governed semantic contract. Theia, GLSP, SVG, DOM, and CSS remain adapters;
they do not own interaction meaning.

The M29 proof path prioritizes structured Interaction payloads before UI click or visual
assertions.

## Architecture Summary

```text
Semantic model and projection facts
  -> Semantic Capability Registry
  -> Interaction Compiler v0
  -> Interaction IR
  -> Interaction Runtime
  -> LSP/runtime transport
  -> Theia adapter
  -> existing authoring/runtime/source-edit gates
```

## Interaction IR

Interaction IR describes canonical subjects, projection occurrences, reveal targets, previews,
commands, diagnostics, lifecycle state, and provenance.

The important boundary is:

```text
semantic identity + projection facts = authority
frontend widget state = adapter metadata
```

DOM text, SVG geometry, CSS classes, and widget ids may be carried for UI correlation only. They
cannot create, resolve, or mutate engineering truth.

## Semantic Action Intent

`SemanticActionIntent` is the producer-neutral primitive. Human UI gestures, future AI agents,
workflow clients, and APIs can all request the same semantic action shape.

M29 covers these action families:

- select
- hover
- focus
- reveal
- preview
- accept
- reject
- mutate

The runtime owns lifecycle transitions such as requested, discovered, validated, previewing,
accepted, mutation-pending, committed, reprojected, stale, rejected, blocked, and cancelled.

## Reveal And Navigation

Reveal starts from an `InteractionSubjectKey`, not a visible label. The same canonical subject can
resolve to source, graph, inspector, and Problems targets when those targets exist. Missing targets
return structured diagnostics instead of guessed navigation.

## Semantic Relationship Mutation Cleanup

M29 routes relationship authoring through Interaction command discovery while preserving the M28
mutation authority:

```text
SemanticActionIntent
  -> InteractionCommand
  -> SemanticRelationshipIntent(ElectricalConnectionRelationship)
  -> authoring/runtime/source-edit gate
```

`ConnectPortsIntent` is retained only as compatibility inventory. New M29 story code must use
`semantic-relationship` and the retained paths must stay listed in the M29 cleanup ledger until
M30 removes or migrates them.

## Semantic Entity Creation

Component insertion is modeled as semantic entity creation first. Symbol placement is a projection
consequence.

The M29 proof uses:

```text
SemanticActionIntent(entityKind=component)
  -> InteractionCommand
  -> CreateComponentIntent
  -> governed source edit
  -> nested-port .athena anatomy
  -> recompile and reproject
```

Generated component anatomy uses nested device-owned ports.

## Deferred Standards And Visual Fidelity

M29 does not introduce IEC/QElectroTech/EPLAN symbol-library expansion, QET import, EPLAN visual
parity, or standards profile selection. Those belong to future presentation/standards milestones.

M27 visual-density and sheet-frame contracts remain regression constraints for M29, but M29's main
achievement is semantic interaction authority.

## Product Smoke

Build the installed LSP host before running the Electron product smoke:

```powershell
.\gradlew.bat --no-daemon --console=plain :ide:lsp:installDist
```

Then run:

```powershell
yarn --cwd ide start:smoke:m29
```

The smoke opens `examples/m29/sample-project` and validates these `m29.interaction.v1` proof
payloads before graph-workbench UI proof:

- `subject-registry`
- `action-discovery`
- `reveal-source-graph-inspector-problems`
- `relationship-preview`
- `relationship-accept`
- `entity-creation-preview`
- `entity-creation-accept`
- `preview-stale-clearing`
- `legacy-connect-ports-inventory`

The proof treats `.athena` source and Interaction IR as semantic authority. It must not derive
meaning from DOM text, SVG geometry, or CSS.

## Focused Non-Electron Proof

The structured proof inventory can be checked without launching Electron:

```powershell
node --test .\ide\theia-frontend\scripts\athena-m29-product-smoke-wiring.test.mjs
```
