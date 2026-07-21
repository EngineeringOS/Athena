# M30 Sample Project

This project is the M30 rolling shutter control customer-demo workspace.

The Athena source is semantic source only. It defines devices, nested ports,
relationships, and layout intent for a professional control-circuit projection.
It does not contain symbol geometry, QElectroTech `.elmt` content, or renderer
asset syntax.

Open the sample:

```powershell
yarn --cwd ide start:m30
```

Run structured proof and screenshot guard:

```powershell
yarn --cwd ide start:smoke:m30
```

The screenshot guard writes:

```text
_bmad-output/implementation-artifacts/m30/screenshots/m30-graph-workbench-smoke.png
```

QElectroTech and EPLAN are qualitative visual references only. M30 does not
claim QET/EPLAN parity and does not use QET runtime assets.
