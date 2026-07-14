---
baseline_commit: c04b3eb
---

# Story 4.1: Publish Allowed Connection Targets From Semantic Ports

Status: done

## Story

As an engineer,  
I want Athena to show only compatible connection targets,  
so that I can connect components through port meaning rather than by guessing graph shapes.

## FR Traceability

- FR-7: Athena can start connection authoring from semantic ports
- FR-8: Athena can filter allowed targets by port meaning rather than graph shape
- NFR-2: workbench surfaces remain consumers of platform-owned authoring contracts
- NFR-3: available ports derive from active component knowledge

## Acceptance Criteria

1. Given a user starts a connection from one semantic port, when allowed targets are requested, then Athena returns targets filtered by stable direction, signal family, and narrow protocol-bearing metadata.
2. Given incompatible targets exist on the same canvas, when the connect flow is active, then those targets are not presented as valid completions.

## Tasks / Subtasks

- [x] Publish a shared frontend compatibility model for semantic-port connection filtering.
- [x] Filter graph connect targets by signal family, direction, and protocol metadata from active component knowledge.
- [x] Keep connection filtering compatible with canonical inspection-derived authored port metadata when full component knowledge is not present for a port.
- [x] Add focused model tests for allowed-target filtering.

## Story Completion Status

- Status: done
- Completion note: Athena now computes compatible graph connect targets from semantic-port meaning instead of treating every graph node as an equally valid edge endpoint, and the filter remains grounded in active component knowledge plus canonical inspection state.
- Verification:
  - `yarn build`
  - `node --test scripts/athena-guided-connection-model.test.mjs`

