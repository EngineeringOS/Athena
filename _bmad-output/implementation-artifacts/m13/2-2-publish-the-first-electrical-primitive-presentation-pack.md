# Story 2.2: Publish The First Electrical Primitive Presentation Pack

Status: done

## Implementation Summary

- Published the first electrical primitive pack with device frame, contact mark, motor mark, breaker mark, coil mark, terminal label, orthogonal conductor, and reference mark primitives.
- Upgraded contact and breaker marks to use neutral SVG-path commands so the first IEC-like symbol proof is pack-driven instead of frontend-hardcoded.

## Evidence

- `extensions/domain-electrical/src/main/kotlin/com/engineeringood/athena/domain/electricalruntime/ElectricalRuntimePresentationPacks.kt`

## Verification

- `java25; .\gradlew.bat --no-daemon --console=plain :extensions:domain-electrical:test`
