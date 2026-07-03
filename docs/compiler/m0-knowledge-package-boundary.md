# M0 Governed Knowledge Package Boundary

## Purpose

Stories `2.4` and `2.5` define Athena's first local package format and the first active compilation-context resolution boundary for governed knowledge artifacts.

The goal is to prove that reviewed ontology additions, standards mappings, and rule artifacts can exist as explicit reusable packages with core-owned manifests, provenance, and compatibility metadata before they are allowed to influence project compilation, and then resolve into an inspectable compiler-owned context without becoming a second semantic authority.

## What The Core Owns

The compiler now owns the first governed knowledge package boundary under `compiler.knowledge`.

That boundary now includes:

- governed knowledge artifact kind vocabulary for `ONTOLOGY`, `STANDARDS_MAPPING`, and `RULE`
- a core-owned manifest model for package identity, package format version, artifact version, provenance, and core compatibility
- typed payload entry modeling
- stable validation diagnostics for malformed or incomplete packages
- a deterministic local directory loader for package manifests and payload files
- a deterministic compiler-owned resolver that evaluates local package roots into candidates, active artifacts, and rejected packages
- a compiler-facing knowledge context attached to `AthenaCompiler.compile(...)` results

This keeps package meaning and package validation inside core-owned contracts rather than inside ad hoc scripts or plugin-local formats.

## Active Compilation Context

Story `2.5` adds the thinnest acceptable active-resolution step for M0.

- one local source lists reviewed package roots for the compilation run
- core loads those roots through `AthenaKnowledgePackageLoader`
- valid packages become candidates for compatibility evaluation
- compatible candidates become active artifacts in the effective compilation context
- malformed or incompatible packages become rejected packages with inspectable diagnostics

The active context is attached to compiler-facing results so reviewers can inspect:

- artifact id
- artifact kind
- artifact version
- provenance
- package root
- rejection diagnostics for excluded packages

This is a traceability surface, not a second semantic model.

## Deterministic Resolution

For identical local package inputs, M0 governed knowledge resolution must produce the same:

- candidate ordering
- active artifact ordering
- rejected package ordering
- rejection diagnostics

The current implementation keeps active artifacts ordered by artifact id, artifact version, then package root. Rejected packages sort identified artifacts before anonymous malformed packages, then remain stable by artifact id, artifact version, and package root.

## Package Shape

The M0 proof shape is intentionally narrow:

- one local directory per package
- one manifest file named `athena-knowledge.properties`
- one or more typed payload entries declared in the manifest
- payload files referenced by relative path inside the package root

The manifest is a `.properties` file to keep the proof local, deterministic, and zero-extra-dependency on the JVM-first substrate.

## What The Package Path Is Not

Governed knowledge packages and their active context are not:

- authored project DSL input under `examples/`
- classpath plugins discovered through `ServiceLoader`
- a fifth public compiler pass
- standards importers or exporters

That separation matters.

Project-authored DSL expresses instance-level engineering intent.
Governed knowledge packages express reviewed reusable knowledge artifacts.
Classpath plugins extend runtime behavior through declared extension points.
These are different boundaries and must stay distinct.

## Current Proof Shape

Stories `2.4` and `2.5` now prove:

- valid ontology, mapping, and rule packages can load from deterministic local directory fixtures
- malformed manifests are rejected with stable rule ids and inspectable subjects
- missing payload files are rejected before operational use
- unsupported artifact kinds and invalid compatibility ranges are rejected
- runtime-incompatible but otherwise valid packages are rejected before they affect compiler behavior
- compiler results can expose the active governed artifact set and rejected package diagnostics without changing the declared public pass order
- authored DSL files and plugin-like directory layouts are not mistaken for governed knowledge packages

## Non-Goals

The current M0 governed knowledge boundary still does not include:

- archive formats, repositories, registries, or marketplace distribution
- broad knowledge-driven semantic rule execution
- a separate durable `Knowledge Compiler` output model
- AI extraction workflows or human review tooling
- `AutomationML` import/export implementation
- external boundary descriptors for standards exchange

Those remain later work, especially Story `2.6` for external boundary descriptors.
