---
status: done
story_id: 1.3
epic: 1
title: Admit Nested Ports In Tree-Sitter And Syntax UX
baseline_commit: c7fda3877a76fd8db52a65510e1b123aed4304f1
---

# Story 1.3: Admit Nested Ports In Tree-Sitter And Syntax UX

## Story

As an Athena IDE user, I want nested ports to parse and highlight in Tree-sitter, so that source UX
matches compiler syntax.

## Acceptance Criteria

- Tree-sitter grammar accepts nested `port p { ... }` inside `device`.
- Corpus tests prove nested ports are syntax structure, not property assignments.
- Highlight/navigation queries treat nested ports as declarations.
- Generated Tree-sitter artifacts are refreshed if this repo requires committed generated output.

## Tasks/Subtasks

- [x] Add failing Tree-sitter corpus fixture for nested ports.
- [x] Update `ide/tree-sitter-athena/grammar.js`.
- [x] Update highlight/query files if present.
- [x] Regenerate parser artifacts if repo conventions require it.
- [x] Run focused Tree-sitter tests sequentially.

## Dev Notes

- Architecture: M28 AD-4 is binding.
- Do not let Tree-sitter become semantic authority.

## Dev Agent Record

### Debug Log

- RED: `npm test` in `ide/tree-sitter-athena` failed on nested device-owned port corpus; Tree-sitter parsed nested `port` as a top-level declaration plus ERROR.
- GREEN: `npm test` passed after adding `nested_port_declaration`, hiding the device-member wrapper, and updating highlight queries.

### Completion Notes

- Added Tree-sitter syntax parity for nested device-owned ports.
- Kept syntax UX clean by hiding `_device_member` so existing device trees do not gain noisy wrapper nodes.
- Regenerated checked-in Tree-sitter artifacts through the repo test command.

## File List

- `ide/tree-sitter-athena/grammar.js`
- `ide/tree-sitter-athena/queries/highlights.scm`
- `ide/tree-sitter-athena/src/grammar.json`
- `ide/tree-sitter-athena/src/node-types.json`
- `ide/tree-sitter-athena/src/parser.c`
- `ide/tree-sitter-athena/test/corpus/port.txt`

## Change Log

- 2026-07-21: Story created for M28.
- 2026-07-21: Implemented Tree-sitter nested port parity and marked story ready for review.
