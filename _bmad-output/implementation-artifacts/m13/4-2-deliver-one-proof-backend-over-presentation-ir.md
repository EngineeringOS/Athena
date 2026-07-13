# Story 4.2: Deliver One Proof Backend Over Presentation IR

Status: done

## Implementation Summary

- The Theia/GLSP proof workbench now consumes `Presentation IR` as the primary node and connector source when present.
- Added neutral SVG-path rendering support in the proof backend and removed the electrical proof grid so the canvas stays clean and presentation-led.

## Evidence

- `ide/theia-frontend/src/browser/athena-graph-workbench-model.ts`
- `ide/theia-frontend/src/browser/athena-graph-workbench-presentation-node.tsx`
- `ide/theia-frontend/src/browser/style/index.css`
- `ide/theia-frontend/scripts/athena-graph-workbench-model.test.mjs`

## Verification

- `yarn test` in `ide/theia-frontend`
- `yarn start:smoke` in `ide/theia-product`
