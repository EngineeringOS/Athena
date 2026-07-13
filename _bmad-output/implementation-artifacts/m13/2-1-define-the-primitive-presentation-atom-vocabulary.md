# Story 2.1: Define The Primitive Presentation Atom Vocabulary

Status: done

## Implementation Summary

- Defined stable primitive presentation vocabulary with ids, anchors, text slots, token defaults, orientation support, and backend-neutral shape commands.
- Added generic `PresentationSvgPath` so richer IEC-like symbols can live in `Presentation IR` without promoting backend draw trees into the kernel contract.

## Evidence

- `kernel/presentation-model/src/main/kotlin/com/engineeringood/athena/presentation/PresentationCommon.kt`
- `kernel/presentation-model/src/main/kotlin/com/engineeringood/athena/presentation/PresentationPackModels.kt`
- `kernel/presentation-model/src/main/kotlin/com/engineeringood/athena/presentation/PresentationShapeModels.kt`
- `kernel/presentation-model/src/test/kotlin/com/engineeringood/athena/presentation/PresentationModelContractTest.kt`

## Verification

- `java25; .\gradlew.bat --no-daemon --console=plain :kernel:presentation-model:test`
