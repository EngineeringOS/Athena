# Story 5.1: Preserve Canonical Traceability Across Primitive And Composite Occurrences

Status: done

## Implementation Summary

- Primitive and composite occurrences now carry canonical `semanticId`, explicit anchor bindings, and `sourceProjectionIds`.
- Connectors preserve canonical port ids, anchor ids, and projection provenance so selection and reveal can stay semantic-first.

## Evidence

- `kernel/presentation-model/src/main/kotlin/com/engineeringood/athena/presentation/PresentationOccurrenceModels.kt`
- `kernel/compiler/src/main/kotlin/com/engineeringood/athena/compiler/PresentationModelDeriver.kt`
- `kernel/compiler/src/test/kotlin/com/engineeringood/athena/compiler/PresentationModelDeriverTest.kt`

## Verification

- `java25; .\gradlew.bat --no-daemon --console=plain :kernel:compiler:test`
- `java25; .\gradlew.bat --no-daemon --console=plain :kernel:runtime:test`
