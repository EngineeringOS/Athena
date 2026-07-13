# Story 2.3: Make Primitive Packs Extension-Compatible

Status: done

## Implementation Summary

- Kept primitive and composite presentation packs behind plugin-owned extension contracts instead of frontend-only registration.
- The electrical runtime plugin now contributes presentation packs through the same approved plugin runtime used elsewhere in Athena.

## Evidence

- `kernel/plugins/plugin-api/src/main/kotlin/com/engineeringood/athena/plugin/AthenaPresentationPackContracts.kt`
- `kernel/plugins/plugin-api/src/main/kotlin/com/engineeringood/athena/plugin/AthenaPluginManifestModel.kt`
- `kernel/plugins/plugin-host/src/main/kotlin/com/engineeringood/athena/plugin/host/AthenaPluginApproval.kt`
- `extensions/domain-electrical/src/main/kotlin/com/engineeringood/athena/domain/electricalruntime/ElectricalRuntimeDomainPlugin.kt`

## Verification

- `java25; .\gradlew.bat --no-daemon --console=plain :kernel:compiler:test`
- `java25; .\gradlew.bat --no-daemon --console=plain :extensions:domain-electrical:test`
