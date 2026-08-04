---
title: M42 Brownfield Code And Stack Reconciliation
type: architecture-input-reconciliation
status: complete
created: 2026-08-04
reviewed:
  - ../ARCHITECTURE-SPINE.md
  - current CodeGraph index
  - repository build and lock files
---

# M42 Brownfield Code And Stack Reconciliation

## Verdict

**PASS WITH MIGRATION RISK.** M42 architecture names current ownership debt and assigns one target
owner for every surviving responsibility. Existing contracts are migration evidence, not interfaces
to preserve. No compatibility layer is required or allowed.

## CodeGraph Evidence

- `EngineeringDocument` still publishes `EngineeringComponent`, `EngineeringConnection`, and
  connection-network vocabulary. AD-21, AD-28, AD-34, and AD-35 require direct replacement across
  Engineering, Projection, and Spatial inputs.
- `EngineeringFunction` is currently narrow, Entity-owned through a generic reference, and has no
  direct covering test reported by CodeGraph. AD-22 and AD-37 require contract tests before migration.
- `EngineeringKnowledgeState` has compiler, runtime semantic-diff/review, and semantic-SCM consumers.
  AD-23, AD-33, and AD-35 preserve those independent product responsibilities while replacing the
  old mixed snapshot with Knowledge and Validation Documents.
- `EngineeringConceptDefinition` lives in `component-model` and has compiler, plugin, package,
  runtime, and test consumers. AD-21, AD-25, AD-35, and AD-38 move definition authority into
  `knowledge-model` and package-local Athena source before deleting the module.
- `part-model` contains thin marker/prototype ownership while Part consumers span compiler, plugin,
  package, and runtime paths. AD-35 permits deletion only after surviving Part definition and binding
  responsibilities migrate.
- Runtime and product surfaces still expose Semantic Macro and Component/Connection meaning. AD-33
  and AD-35 require direct canonical-document adapters and deletion of macro-specific protocol/UI.
- Current GLSP transport exposes `components` and `connections`. AD-33 and AD-34 require generated
  Entity/Relationship contracts and prohibit frontend knowledge inference or a Connection adapter.

## Stack Reality

Repository and lock files verify Java toolchain 25, Kotlin 2.4.0, Gradle 9.6.1, ANTLR 4.13.2,
Node 24.15.0 locally with project contract `>=22`, Yarn 1.22.22, TypeScript 5.9.3 in lockfile,
Theia 1.73.1, Electron 39.8.7, and Ajv 8.20.0 in lockfile. Maven Central metadata verified
`kotlinx-serialization-json` 1.11.0; npm metadata verified `json-schema-to-typescript` 15.0.4.
AD-33 correctly requires Ajv to become a direct frontend dependency.

## Required Story Controls

- Use CodeGraph before each legacy-contract edit and include all reported consumers in story tasks.
- Delete superseded contracts in the same story that installs their replacement; do not defer a
  compatibility bridge to closure.
- Add direct tests for Function ownership and cross-Entity Function participation before removing
  the old model.
- Run Gradle verification sequentially and rebuild frontend bundles before product E2E.
- Run source-set hygiene and explicit retired-contract inventory audits at closure.

## Result

No architecture correction remains from brownfield evidence. Story decomposition must expose the
migration blast radius and order it behind target contracts.
