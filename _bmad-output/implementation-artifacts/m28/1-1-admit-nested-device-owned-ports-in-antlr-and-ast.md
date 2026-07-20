---
status: done
story_id: 1.1
epic: 1
title: Admit Nested Device-Owned Ports In ANTLR And AST
baseline_commit: c7fda3877a76fd8db52a65510e1b123aed4304f1
---

# Story 1.1: Admit Nested Device-Owned Ports In ANTLR And AST

## Story

As an Athena author, I want to write ports inside the owning device block, so that component anatomy
is compact and straightforward.

## Acceptance Criteria

- `device D { port p { direction in signal Digital } }` parses through ANTLR without syntax errors.
- The authored AST represents nested `p` as a first-class `PortDeclaration` owned by `D`.
- Nested ports are not represented as device property assignments.
- Malformed nested ports produce diagnostics/source spans at the nested port location.
- Existing legacy top-level `port D.p { ... }` remains accepted.

## Tasks/Subtasks

- [x] Add a failing language test for nested device-owned ports.
- [x] Update `Athena.g4` so `deviceDecl` accepts device members, including property assignments and nested `port` declarations.
- [x] Update `AthenaLanguageModel.kt` so `DeviceDeclaration` can carry nested ports without losing existing fields.
- [x] Update `AthenaAntlrParseAdapter.kt` to adapt nested ports with owner context and source span.
- [x] Verify legacy top-level ports still parse.
- [x] Run focused language/parser tests sequentially.

## Dev Notes

- Architecture: M28 AD-2 and AD-4 are binding.
- Primary files:
  - `kernel/language/src/main/antlr/com/engineeringood/athena/language/antlr/Athena.g4`
  - `kernel/language/src/main/kotlin/com/engineeringood/athena/language/AthenaLanguageModel.kt`
  - `kernel/language/src/main/kotlin/com/engineeringood/athena/language/antlr/AthenaAntlrParseAdapter.kt`
- Use TDD. Write the failing test before changing production code.
- Do not remove top-level `port Device.port` syntax in this story.

## Dev Agent Record

### Debug Log

- RED: `.\gradlew.bat --no-daemon --console=plain :kernel:language:test --tests "com.engineeringood.athena.language.AthenaLanguageParserTest.parses nested device owned ports as first class component anatomy"` failed at compile time because `DeviceDeclaration.nestedPorts` did not exist.
- GREEN: the same focused test passed after adding nested device member grammar, AST field, and adapter mapping.
- Regression: `.\gradlew.bat --no-daemon --console=plain :kernel:language:test` passed.
- Downstream compile check: `.\gradlew.bat --no-daemon --console=plain :kernel:compiler:compileKotlin` passed.

### Completion Notes

- Added first-class nested port parsing for `device D { port p { ... } }`.
- Added `DeviceDeclaration.nestedPorts` with a default empty list so existing callers stay source-compatible.
- Nested ports adapt to canonical qualified name parts `[Device, port]`; deeper semantic provenance remains Story 1.2.
- Updated grammar smoke coverage for the new `deviceMember` parse-tree shape.

## File List

- `kernel/language/src/main/antlr/com/engineeringood/athena/language/antlr/Athena.g4`
- `kernel/language/src/main/kotlin/com/engineeringood/athena/language/AthenaLanguageModel.kt`
- `kernel/language/src/main/kotlin/com/engineeringood/athena/language/antlr/AthenaAntlrParseAdapter.kt`
- `kernel/language/src/test/kotlin/com/engineeringood/athena/language/AthenaLanguageParserTest.kt`
- `kernel/language/src/test/kotlin/com/engineeringood/athena/language/antlr/AthenaGrammarSmokeTest.kt`

## Change Log

- 2026-07-21: Story created for M28.
- 2026-07-21: Implemented nested device-owned port parsing and marked story ready for review.
