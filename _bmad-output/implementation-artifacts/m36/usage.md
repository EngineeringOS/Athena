# M36 Usage

## Scope

M36 uses the dedicated Cabinet example only:

```text
examples/m36/connectivity-cabinet
```

Do not use M34 or M35 examples as M36 proof projects.

## Run The IDE

Build the affected IDE surface before launching:

```powershell
yarn --cwd ide build
yarn --cwd ide start:m36
```

The IDE should open the M36 Cabinet sample and show the Cabinet product surface.

## Run The E2E Smoke

```powershell
yarn --cwd ide build
yarn --cwd ide start:smoke:m36
```

Expected proof:

- Cabinet remains the only visible product surface.
- 21 governed graphic occurrences render.
- 31 orthogonal terminal-anchored routes render.
- SVG-backed ABB PFEA112 renders from the package-local resource path.
- Zero center-anchor fallback.
- Zero route/body intersections.
- Complete occurrence trace evidence.
- No XML or HTML runtime authority.

## Screenshot Evidence

Current M36 screenshots:

```text
_bmad-output/implementation-artifacts/m36/screenshots/m36-connectivity-cabinet-desktop-1920x1080.png
_bmad-output/implementation-artifacts/m36/screenshots/m36-connectivity-cabinet-desktop-1280x900.png
_bmad-output/implementation-artifacts/m36/screenshots/m36-connectivity-cabinet-narrow.png
```

## Verification Notes

- Rebuild both LSP/kernel outputs and the Theia/Electron frontend before final UI E2E.
- If the renderer behavior looks stale, rebuild `ide` before changing tests.
- Use `tools/source-set-hygiene-audit.ps1` after cleanup or architecture refactors.
