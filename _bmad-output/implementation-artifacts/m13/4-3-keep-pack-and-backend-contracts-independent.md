# Story 4.3: Keep Pack And Backend Contracts Independent

Status: done

## Implementation Summary

- Presentation packs publish stable ids, slots, anchors, bounds, and neutral shape commands only.
- Backend consumers interpret those commands but do not redefine pack structure, semantic identity, or upstream projection truth.

## Evidence

- `kernel/presentation-model/src/main/kotlin/com/engineeringood/athena/presentation/PresentationShapeModels.kt`
- `kernel/presentation-model/src/main/kotlin/com/engineeringood/athena/presentation/PresentationPackModels.kt`
- `integrations/graph-glsp/src/athena-glsp-projection-adapter.ts`
- `ide/theia-frontend/src/browser/athena-graph-workbench-presentation-node.tsx`

## Verification

- `java25; .\gradlew.bat --no-daemon --console=plain :kernel:presentation-model:test`
- `yarn test` in `integrations/graph-glsp`
- `yarn test` in `ide/theia-frontend`
