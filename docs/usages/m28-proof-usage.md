# Athena M28 Proof Usage

M28 introduces governed component anatomy and semantic relationship authoring.

## Canonical Component Anatomy

New Athena source should author ports inside the owning device:

```athena
system M28NestedPortProof {
  device SpareTerminalXT99 {
    type Switch
    model "SPARE-XT"

    port in1 {
      direction in
      signal Digital
    }
  }
}
```

The nested `port in1` block is a first-class port declaration. It is not a device property and it
does not create a new identity scheme. The canonical identity remains:

```text
port:SpareTerminalXT99.in1
```

## Legacy Top-Level Port Policy

M28 keeps this older form accepted for existing fixtures and historical samples:

```athena
port SpareTerminalXT99.in1 {
  direction in
  signal Digital
}
```

That form is legacy-compatible, not canonical. New M28 examples, docs, and generated source should
prefer nested device-owned ports.

If a source file declares the same canonical port in both forms, Athena reports a duplicate authored
declaration for `Device.port`.

## Semantic Relationship Authoring

M28 uses a generic authoring boundary:

```text
SemanticRelationshipIntent
```

Electrical connection is the first specialization:

```text
ElectricalConnectionRelationship
```

For today's `.athena` source, an accepted electrical relationship serializes as the existing
connection syntax:

```athena
connect SourceDevice.out -> TargetDevice.in
```

The architecture is intentionally not named around wires. Later relationships such as flow,
containment, control, communication, mounting, or dependency should fit the same relationship
intent boundary.

## Product Path Proof

The checked-in proof project is:

```text
examples/m28/sample-project
```

The first source file uses canonical nested component anatomy and contains one spare output and one
spare terminal input for authoring:

```text
port:ControllerPLC1.spareDo -> port:SpareTerminalXT99.in1
```

The product-path smoke proves:

- The M28 sample opens through the Theia product script.
- The frontend relationship path selects subjects from projection facts, not DOM text.
- A valid `SemanticRelationshipIntent` returns a governed `.athena` source edit.
- Recompile and reproject expose the committed connection and route corridor.
- Invalid output-output and input-input attempts do not mutate source.
- A stale or malicious accepted invalid request is also blocked at the backend source-edit gate.

## Authority Chain

M28 keeps the Athena authority chain:

```text
.athena source
  -> compiler semantic model
  -> projection facts
  -> Theia selection and preview
  -> SemanticRelationshipIntent
  -> M8 mutation authority
  -> canonical semantic persistence
  -> .athena serialization today
  -> recompile and reproject
```

Theia may select, preview, inspect, and request mutation. It must not own relationship truth, route
truth, source writes, or identity inferred from SVG geometry or DOM text.

## Verification Commands

Run Gradle commands sequentially on Windows:

```powershell
.\gradlew.bat --no-daemon --console=plain :kernel:language:test --tests "com.engineeringood.athena.language.AthenaLanguageParserTest.parses nested device owned ports as first class component anatomy"
.\gradlew.bat --no-daemon --console=plain :kernel:compiler:test --tests "com.engineeringood.athena.compiler.AthenaM28NestedPortCompilerTest" --tests "com.engineeringood.athena.compiler.AthenaM28SampleProjectCompilerTest" --tests "com.engineeringood.athena.compiler.semantic.ProjectSemanticDeclarationIndexerTest"
.\gradlew.bat --no-daemon --console=plain :kernel:authoring-model:test
.\gradlew.bat --no-daemon --console=plain :ide:lsp:test --tests "com.engineeringood.athena.ide.lsp.AthenaAuthoringRequestTest" --tests "com.engineeringood.athena.ide.lsp.AthenaM28ProductAuthoringSmokeTest"
.\gradlew.bat --no-daemon --console=plain :ide:lsp:installDist
```

Tree-sitter and frontend checks:

```powershell
cd ide\tree-sitter-athena
npm test

cd ..\theia-frontend
yarn build; if ($LASTEXITCODE -eq 0) { node --test scripts/athena-authoring-protocol.test.mjs scripts/athena-m28-relationship-authoring-model.test.mjs scripts/athena-m28-product-smoke-wiring.test.mjs } else { exit $LASTEXITCODE }

cd ..\..
cd ide
yarn start:smoke:m28
```

## M29 Boundary

M28 does not implement the full Semantic Interaction Compiler. M29 should own the broader
Interaction IR boundary for hover, selection, focus, reveal, preview, commands, undo, redo, CLI, AI,
Web, 3D, and VR adapters.
