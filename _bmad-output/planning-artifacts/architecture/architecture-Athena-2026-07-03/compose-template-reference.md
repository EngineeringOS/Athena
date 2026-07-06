# Compose Template Reference For Athena M1

## Source

- Template root: `D:/Aaron/workspace/projects/2026/desktop/temp/edge-desktop/edge-desktop`
- Version catalog: `D:/Aaron/workspace/projects/2026/desktop/temp/edge-desktop/edge-desktop/gradle/libs.versions.toml`
- Settings: `D:/Aaron/workspace/projects/2026/desktop/temp/edge-desktop/edge-desktop/settings.gradle.kts`

This template should be treated as the local implementation reference when Athena initializes the first Compose Multiplatform modules for M1.

The same reference should also guide Athena's Gradle management style:

- shared dependency and plugin versions belong in `gradle/libs.versions.toml`
- module build files should consume aliases from the catalog
- Compose initialization should not introduce a second parallel version-management style

## Version Reference

Extracted from the template version catalog:

| Concern | Version |
| --- | --- |
| Kotlin | `2.4.0` |
| Compose Multiplatform | `1.11.1` |
| Kotlinx Coroutines | `1.11.0` |
| AndroidX Lifecycle Compose | `2.11.0-beta01` |
| Material 3 | `1.11.0-alpha07` |

For Athena M1, the load-bearing seed is:

- use the template's Compose Multiplatform line as the starting point
- keep Kotlin aligned with the existing Athena workspace
- prefer importing the first Compose-related version catalog entries from this reference rather than inventing a parallel set ad hoc
- keep plugin aliases in the same catalog so Compose, Kotlin, and later runtime modules resolve through one TOML-managed source

## Module Structure Reference

The template splits responsibilities as:

- `:app:desktopApp` - desktop entrypoint and packaging
- `:app:shared` - shared Compose UI/runtime module
- `:core` - platform-neutral core dependency module
- `:server` - optional server-side module

## Athena Mapping Recommendation

When Athena initializes its first Compose M1 modules, use the template's shape as a reference, not as a literal rename:

| Template shape | Athena M1 mapping |
| --- | --- |
| `:app:desktopApp` | `:apps:compose-viewer` |
| `:app:shared` | `:compose-runtime` |
| `:core` | existing runtime/core semantic modules such as `:runtime`, `:graph`, `:command`, `:ir`, `:compiler` |
| `:server` | out of scope for M1 seed unless a later runtime service needs it |

## Why This Matters

This reference prevents two avoidable forms of drift:

1. Compose dependency drift
   Use one known-good version line instead of inventing a separate Compose stack during M1 viewer initialization.

2. Module-shape drift
   Keep a clean separation between:
   - platform app entrypoint
   - shared Compose runtime/view layer
   - semantic/runtime core dependencies

That split fits the M1 architecture rule that Compose remains a consumer of runtime services, not the owner of semantic authority.
