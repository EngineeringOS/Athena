# Story 1.2: Derive Presentation IR From Existing Projection Contracts

Status: done

## Implementation Summary

- Added `PresentationModelDeriver` so `PresentationDocument` is rebuilt from projection-owned view family, notation, anchor, endpoint, routing, and sheet context.
- Kept the derivation downstream-only: presentation occurrences and connectors preserve canonical semantic ids and `sourceProjectionIds` instead of inventing a second authority.
- Added `PresentationModelDeriverTest` to prove routing guidance and family-specific composite selection stay downstream of canonical semantics.

## Evidence

- `kernel/compiler/src/main/kotlin/com/engineeringood/athena/compiler/PresentationModelDeriver.kt`
- `kernel/compiler/src/test/kotlin/com/engineeringood/athena/compiler/PresentationModelDeriverTest.kt`

## Verification

- `java25; .\gradlew.bat --no-daemon --console=plain :kernel:compiler:test`
