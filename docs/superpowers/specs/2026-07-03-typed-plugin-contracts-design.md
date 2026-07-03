# Story 2.1 Design: Core-Owned Typed Plugin Contracts

## Purpose

Define the first core-owned plugin contract surface for Athena M0 so domain, rule, and renderer extensions can attach through typed boundaries without replacing `Engineering IR` as semantic authority.

This design covers Story `2.1` only. It does not include classpath discovery, activation order, or dynamic loading. Those remain in Story `2.2`.

## Scope

In scope:

- publish core-owned plugin contract types
- publish a core-owned plugin manifest model
- publish a core-owned compatibility-range model
- publish a core-owned extension-point model
- add manifest validation with inspectable diagnostics
- implement one minimal sample `domain` plugin in `domain-electrical-runtime`
- prove the contract by instantiating that sample plugin directly in tests

Out of scope:

- plugin discovery from classpath
- `ServiceLoader` or equivalent activation mechanics
- plugin dependency resolution between sibling plugins
- plugin-contributed pass scheduling
- converting `renderer-svg` into a real plugin
- moving Electrical/Runtime semantics fully out of the core validator

## Design Goals

1. Keep plugin authority non-sovereign.
2. Keep the M0 module graph stable.
3. Make the contract usable now and discoverable later.
4. Reuse existing core types instead of creating a second semantic center.

## Recommended Structure

The existing module graph stays in place.

- `compiler`
  - owns plugin governance types because it owns extension boundaries and activation rules
  - adds `com.engineeringood.athena.compiler.plugin`
- `domain-electrical-runtime`
  - becomes the sample plugin module for Story `2.1`
  - implements the new `domain` plugin contract directly
- `semantics-core` and `ir`
  - remain the only semantic authority surfaces
- `renderer-svg`
  - stays unchanged for now except for compatibility with the new published contract shape

This avoids creating a new plugin-api module in `2.1` and keeps the story focused on contract definition rather than module reshaping.

## Core Contract Model

### Base Contract

Every plugin implements:

```kotlin
interface AthenaPlugin {
    val manifest: AthenaPluginManifest
}
```

### Typed Plugin Contracts

The core publishes three typed subcontracts:

- `AthenaDomainPlugin`
- `AthenaRulePlugin`
- `AthenaRendererPlugin`

These subcontracts exist to prove typed plugin classes now, even if most behavior remains shape-first in Story `2.1`.

The initial contract surface should stay narrow:

- expose identity and declared capability metadata
- expose only core-owned contribution seams
- do not expose any hook that can replace `EngineeringIrDocument`
- do not expose any hook that can alter compiler pass ordering

### Plugin Type Model

```kotlin
enum class AthenaPluginType {
    DOMAIN,
    RULE,
    RENDERER,
}
```

## Manifest Model

The manifest format is owned by the core and represented as typed Kotlin data.

```kotlin
data class AthenaPluginManifest(
    val pluginId: String,
    val pluginVersion: String,
    val pluginType: AthenaPluginType,
    val coreCompatibility: CoreVersionRange,
    val requiredExtensionPoints: Set<AthenaExtensionPoint>,
)
```

Required manifest fields for Story `2.1`:

- `pluginId`
- `pluginVersion`
- `pluginType`
- `coreCompatibility`
- `requiredExtensionPoints`

This manifest is a model first in `2.1`. Story `2.2` will decide how it is published on the classpath.

## Compatibility Model

The core owns a minimal compatibility-range type:

```kotlin
data class CoreVersionRange(
    val minimumInclusive: String,
    val maximumInclusive: String? = null,
)
```

`2.1` only needs structural compatibility validation. Full runtime compatibility evaluation against discovered plugins belongs in `2.2`.

## Extension Point Model

The core owns the extension-point vocabulary.

```kotlin
enum class AthenaExtensionPoint {
    DOMAIN_SEMANTICS,
    RULE_EVALUATION,
    RENDERING,
}
```

Each plugin type is allowed to require only the extension points mapped to that type:

- `DOMAIN` -> `DOMAIN_SEMANTICS`
- `RULE` -> `RULE_EVALUATION`
- `RENDERER` -> `RENDERING`

This keeps plugin declarations explicit and blocks plugins from claiming arbitrary surfaces.

## Validation Model

Story `2.1` includes direct validation of manifests and plugin objects before discovery exists.

### Validator Responsibilities

The validator should check:

1. `pluginId` is present and well-formed
2. `pluginVersion` is present and non-blank
3. `pluginType` is a supported core-owned type
4. `coreCompatibility` is structurally valid
5. `requiredExtensionPoints` is non-empty when required by the plugin contract
6. extension points are legal for the declared plugin type

### Validation Output

Validation should return inspectable diagnostics rather than throwing on ordinary invalid input.

Recommended model:

```kotlin
data class PluginValidationResult(
    val diagnostics: List<PluginValidationDiagnostic>,
) {
    val isValid: Boolean
        get() = diagnostics.isEmpty()
}
```

Diagnostics should include:

- severity
- stable rule id
- manifest field or contract area
- message

This keeps plugin validation consistent with the existing inspectable compiler style.

## Sample Plugin

Story `2.1` uses `domain-electrical-runtime` as the proof plugin module.

Recommended class:

```kotlin
class ElectricalRuntimeDomainPlugin : AthenaDomainPlugin
```

Its manifest should declare:

- stable plugin id, for example `com.engineeringood.athena.domain.electrical-runtime`
- plugin version `0.0.1-SNAPSHOT`
- type `DOMAIN`
- a Java-25-era compatible core range
- required extension point `DOMAIN_SEMANTICS`

The sample plugin is intentionally minimal:

- no discovery metadata file yet
- no runtime activation flow
- no plugin-to-plugin linkage
- no plugin-owned compiler scheduling

Its job is only to prove that a real module can implement the published core contract cleanly.

## Testing Strategy

Tests should live primarily in `compiler` because `compiler` owns the contract and validator.

Required tests:

1. manifest validation succeeds for a valid domain plugin
2. invalid manifest fields produce stable diagnostics
3. plugin type and extension point mismatches are rejected
4. the sample plugin can be instantiated directly and treated as `AthenaDomainPlugin`
5. sibling-plugin linkage is not required to satisfy the contract

Suggested additional test:

- contract types remain accessible from `domain-electrical-runtime` without depending on any concrete sibling plugin

## File Impact

Primary expected touch points:

- `compiler/src/main/kotlin/com/engineeringood/athena/compiler/plugin/**`
- `compiler/src/test/kotlin/com/engineeringood/athena/compiler/**`
- `domain-electrical-runtime/src/main/kotlin/com/engineeringood/athena/domain/electricalruntime/**`
- `docs/compiler/**`
- `_bmad-output/implementation-artifacts/**`

Files that should stay semantically unchanged in `2.1`:

- parser and AST files under `language`
- canonical IR model under `ir`
- render derivation and SVG emission logic

## Risks And Controls

### Risk: Plugin contracts become an escape hatch

Control:
Keep typed contracts narrow and core-owned. Do not expose plugin hooks that mutate core semantic authority or pass order.

### Risk: Story `2.1` bleeds into discovery

Control:
Do not add classpath scanning, manifest resource loading, or activation sorting in this story.

### Risk: Electrical/Runtime remains accidentally core-owned forever

Control:
Use `domain-electrical-runtime` as the first proof plugin now so Story `2.3` can move semantic behavior behind the contract instead of inventing the boundary later.

## Success Criteria

Story `2.1` is successful when:

- Athena publishes typed contracts for `domain`, `rule`, and `renderer`
- the plugin manifest and compatibility models are core-owned
- a validator can reject malformed or mismatched plugin declarations with inspectable diagnostics
- `domain-electrical-runtime` implements one real sample domain plugin
- tests prove that plugin contracts are usable directly without discovery

## Implementation Recommendation

Proceed with Story `2.1` using the existing module graph, shape-first typed contracts, direct test instantiation of the sample domain plugin, and no discovery plumbing.
