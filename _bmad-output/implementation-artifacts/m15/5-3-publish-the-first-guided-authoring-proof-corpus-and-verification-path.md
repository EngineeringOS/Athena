---
baseline_commit: c04b3eb
---

# Story 5.3: Publish The First Guided Authoring Proof Corpus And Verification Path

Status: done

## Story

As a platform owner,  
I want Athena to publish one narrow repository-backed proof and deterministic verification path for guided authoring,  
so that M15 closes with real product evidence rather than UI-only demos.

## FR Traceability

- FR-10: Athena can keep source, graph, and guided authoring in sync
- FR-13: Athena can commit accepted guided mutations into canonical state
- NFR-5: preview and approval remain inspectable and deterministic
- NFR-6: the first proof stays narrow, Siemens-first, and electrical only

## Acceptance Criteria

1. Given the first M15 proof corpus is reviewed, when the scenario is inspected, then it demonstrates open repository -> insert PLC -> edit tag -> add supply -> connect compatible ports -> preview -> approve -> verify source and graph coherence.
2. Given the milestone closes, when verification evidence is recorded, then the proof path documents the exact targeted runtime, LSP, frontend, and repository-backed validation steps used to support M15 claims.

## Tasks / Subtasks

- [x] Publish a narrow repository-backed example under `examples/m15/guided-authoring-proof`.
- [x] Add an LSP proof test that executes guided placement, rename, insert, and connect through the review-first authoring path.
- [x] Keep verification deterministic and sequential for Windows and Java 25.
- [x] Record the proof usage and verification path in milestone docs.

## Story Completion Status

- Status: done
- Completion note: Athena now ships a real repository-backed guided authoring proof corpus and an LSP-driven end-to-end verification path that proves placement, inspector editing, connect preview, acceptance, and canonical rebuild without requiring direct DSL editing.
- Verification:
  - `cmd /c "call java25 && .\gradlew.bat --no-daemon --console=plain :ide:lsp:test"`
  - `cmd /c "call java25 && .\gradlew.bat --no-daemon --console=plain :kernel:runtime:test"`
  - `cmd /c "call java25 && .\gradlew.bat --no-daemon --console=plain :extensions:domain-electrical:test"`
  - `yarn build`
  - `node --test scripts/athena-component-panel-model.test.mjs scripts/athena-authoring-protocol.test.mjs scripts/athena-inspector-model.test.mjs scripts/athena-guided-connection-model.test.mjs`
  - `powershell -ExecutionPolicy Bypass -File .\tools\encoding-audit.ps1`

