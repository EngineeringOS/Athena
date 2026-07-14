# M15 Guided Authoring Proof Usage

## Purpose

This usage note explains the first narrow guided-authoring proof shipped in M15.

The proof demonstrates that an engineer can create and change governed engineering intent without directly editing raw DSL, while Athena still keeps the canonical `.athena` source as the only authored truth.

## Proof Repository

- Example path: `examples/m15/guided-authoring-proof`
- Primary source file: `examples/m15/guided-authoring-proof/src/guided-authoring-proof.athena`

## Proof Flow

1. Open the guided authoring proof repository.
2. Insert a PLC CPU from the governed component panel.
3. Rename the inserted PLC through the inspector.
4. Insert a 24V power supply from the governed component panel.
5. Start a connection from the power-supply output to the PLC `lplus` port.
6. Review the preview, then accept it.
7. Verify that canonical source, semantic inspection, and graph state all reflect the same component and connection identities.

## What To Expect

- Guided placement produces canonical source-backed device and port declarations.
- Inspector rename keeps dependent authored port and connection references coherent.
- Guided connect creates a canonical `connect source -> target` statement only after preview acceptance.
- Semantic inspection reports a ready state when the proof completes successfully.
- The supported AST Outline for `.athena` files is currently the native right-sidebar Outline view. The experimental Explorer-bottom docking path was rolled back because the stock Theia widget lost its render height after reparenting.

## Deterministic Verification

Run the Windows-safe sequential verification path:

1. `cmd /c "call java25 && .\gradlew.bat --no-daemon --console=plain :ide:lsp:test"`
2. `cmd /c "call java25 && .\gradlew.bat --no-daemon --console=plain :kernel:runtime:test"`
3. `cmd /c "call java25 && .\gradlew.bat --no-daemon --console=plain :extensions:domain-electrical:test"`
4. `yarn build` in `ide/theia-frontend`
5. `node --test scripts/athena-component-panel-model.test.mjs scripts/athena-authoring-protocol.test.mjs scripts/athena-inspector-model.test.mjs scripts/athena-guided-connection-model.test.mjs`
6. `powershell -ExecutionPolicy Bypass -File .\tools\encoding-audit.ps1`

## Product Position

M15 keeps Athena aligned with the core product principle:

- engineers do not need to learn raw DSL as the default interface
- DSL remains canonical serialization
- guided authoring remains review-first and semantic-first
