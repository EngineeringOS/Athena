# Story 4.1: Define The Renderer Backend Interface Above Presentation IR

Status: done

## Implementation Summary

- Kept an explicit backend seam above `Presentation IR` through backend descriptors and transport models rather than embedding backend draw trees into pack definitions.
- Frontend/adapter code consumes normalized presentation commands generically, keeping backend concerns downstream of pack contracts.

## Evidence

- `kernel/presentation-model/src/main/kotlin/com/engineeringood/athena/presentation/PresentationBackendModels.kt`
- `integrations/graph-glsp/src/athena-glsp-projection-source.ts`
- `integrations/graph-glsp/src/athena-glsp-projection-adapter.ts`

## Verification

- `yarn test` in `integrations/graph-glsp`
