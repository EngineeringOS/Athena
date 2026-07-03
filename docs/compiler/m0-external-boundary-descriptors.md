# M0 External Boundary Descriptors

## Purpose

Story `2.6` defines Athena's first compiler-owned contract surface for passive external boundaries.

The goal is to let the compiler describe standards, runtime, and enterprise perimeter contracts in machine-readable form without allowing those boundaries to become semantic authorities or execution surfaces in M0.

## What The Core Owns

The compiler now owns an external boundary descriptor surface under `compiler.boundary`.

That surface includes:

- a core-owned descriptor source model for local boundary roots
- explicit vocabulary for boundary category, direction, upstream semantic authority, exchanged-form kinds, compatibility assumptions, and M0 mode
- a deterministic local `.properties` descriptor loader
- stable diagnostics for malformed, incomplete, duplicate, sovereign, invalid-assumption, or operational descriptors
- a deterministic validation report containing candidates, valid descriptors, and rejected descriptors
- compiler-facing exposure of boundary validation results through `AthenaCompiler.compile(...)`

This keeps external boundary metadata inside compiler-owned contracts rather than in plugin manifests, ad hoc runtime adapters, or standards-specific scripts.

## Boundary Posture

In M0, valid external boundary descriptors must keep `ENGINEERING_IR` as the upstream semantic authority.

Standards boundary descriptors must remain `REFERENCE` or `COMPATIBILITY` boundaries in M0.

That means descriptors may describe:

- standards references
- compatibility boundaries
- passive runtime bridges
- passive enterprise bridges

They may not describe:

- an external schema as the canonical internal model
- a live importer or exporter
- a live runtime or enterprise connector
- a replacement for compiler pass ownership

## Descriptor Shape

The M0 proof format is intentionally narrow:

- one local directory per descriptor
- one manifest file named `athena-boundary.properties`
- one manifest declaring:
  - `descriptor.id`
  - `descriptor.category`
  - `descriptor.direction`
  - `authority.upstream`
  - `exchange.forms`
  - `compatibility.assumptions`
  - `m0.mode`

The manifest stays as `.properties` to preserve the JVM-first, deterministic, zero-extra-dependency proof shape already used elsewhere in Athena's M0 compiler work.

## What This Boundary Is Not

External boundary descriptors are not:

- `ServiceLoader` plugins
- governed knowledge packages
- authored project DSL inputs
- a fifth public compiler pass
- `AutomationML` import or export logic
- `OPC UA`, ERP, MES, or cloud connector implementations

That separation matters.

Plugins extend compiler behavior through declared extension points.
Governed knowledge packages contribute reviewed artifacts into compiler-owned context.
Authored DSL expresses project instance intent.
Boundary descriptors only describe the perimeter contracts around the semantic core.

## Current Proof Shape

Story `2.6` now proves:

- `AutomationML` can be represented as a standards reference boundary
- a runtime context such as `OPC UA` can be represented as passive boundary metadata
- descriptors are rejected if they try to move semantic authority outside `Engineering IR`
- descriptors are rejected if they imply operational importer, exporter, or connector behavior in M0
- boundary validation results are inspectable through the compiler facade without changing the public pass order

## Non-Goals

The current M0 external boundary descriptor surface still does not include:

- standards import or export execution
- runtime transport stacks
- enterprise system orchestration
- descriptor-driven plugin activation
- descriptor-driven semantic rewrites
- external registries or distribution workflows

Those remain later work beyond the M0 compiler proof.
