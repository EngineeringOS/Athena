# Story 3.2: Support Multiple Composite Variants For One Canonical Subject

Status: done

## Implementation Summary

- Added family-filtered electrical composite packs so the same canonical subject can resolve to different downstream composite variants across cabinet and schematic families.
- Kept semantic identity stable while allowing family-specific presentation definitions to change parts and local layout.

## Evidence

- `extensions/domain-electrical/src/main/kotlin/com/engineeringood/athena/domain/electricalruntime/ElectricalRuntimePresentationPacks.kt`
- `kernel/compiler/src/test/kotlin/com/engineeringood/athena/compiler/PresentationModelDeriverTest.kt`

## Verification

- `java25; .\gradlew.bat --no-daemon --console=plain :kernel:compiler:test`
