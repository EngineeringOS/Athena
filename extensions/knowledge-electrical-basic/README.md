# `extensions/knowledge-electrical-basic`

English | [Chinese (Simplified)](README.zh-CN.md)

`extensions/knowledge-electrical-basic` is Athena's first governed M9 knowledge pack. It is a directory-backed reviewed artifact, not a Gradle code module. The pack contributes fixed electrical capability-promotion semantics and the first fixed sufficiency rule slice through the existing compiler knowledge-package seam.

## Responsibilities

- Publish the first fixed knowledge-pack manifest in `athena-knowledge.properties`.
- Contribute the first governed capability-promotion semantics payload.
- Contribute the first governed constraint-slice payload.
- Keep the M9 proof narrow: electrical only, no vendor catalog, no standards platform, no end-user rule authoring.

## Current Scope

- Promotes `FULL_LOAD_CURRENT` into:
  - `REQUIRED_PROTECTION_CURRENT`
  - `REQUIRED_CABLE_CURRENT`
  - `REQUIRED_RELAY_SIZING_CURRENT`
- Uses fixed multiplier and rounding semantics from `payload/capability-semantics.properties`.
- Evaluates fixed sufficiency checks from `payload/constraint-slice.properties`:
  - protection sufficiency
  - cable sufficiency
  - relay sufficiency

## Boundaries

This pack does not execute code directly, does not own the compiler or runtime execution model, and does not provide renderer or IDE behavior. Athena kernel/compiler code loads the pack, interprets the payload, and emits typed capability facts, constraint-evaluation results, and sufficiency diagnostics.
