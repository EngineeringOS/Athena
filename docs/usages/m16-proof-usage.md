# M16 Semantic Reuse Proof Usage

Updated: 2026-07-14

## Purpose

This guide shows how to exercise the finished M16 proof slice for governed Semantic Macro reuse.

M16 proves that Athena can:

- publish governed reusable electrical assemblies from a checked-in repository
- validate and preview parameterized reuse deterministically
- commit approved reuse through the sole M8 mutation authority
- preserve accepted expansion origin and membership traceability after approval
- rerun the same repository-backed scenario with the same outputs

## Proof Repository

- Example path: `examples/m16/semantic-reuse-proof`
- Primary source file: `examples/m16/semantic-reuse-proof/src/semantic-reuse-proof.athena`
- Governed macro manifest: `examples/m16/semantic-reuse-proof/athena-semantic-macros.properties`

The checked-in proof slice currently includes:

- `DOL Starter`
- `PLC Rack`
- `24V Distribution Unit`

## Why This Is Governed

The proof macros do not treat widget state or graphics as engineering truth.

They derive reusable structure from governed electrical concept and implementation identifiers already established by M14, including:

- `electrical.contactor.power`
- `electrical.relay.overload`
- `electrical.plc.cpu`
- `electrical.power-supply.dc24`
- `impl/electrical/contactor/siemens-proof-3pole`
- `impl/electrical/relay/siemens-proof-overload`
- `impl/electrical/plc-cpu/siemens-proof-cpu313c`
- `impl/electrical/power-supply/siemens-proof-24vdc`

## Proof Flow

1. Open the repository at `examples/m16/semantic-reuse-proof`.
2. Open `src/semantic-reuse-proof.athena`.
3. Open the Athena Reuse Catalog.
4. Choose `DOL Starter`.
5. Configure:
   - `motorPower = 7.5kW`
   - `controlVoltage = 24VDC`
   - `vendorFamily = Siemens`
   - `tagPrefix = M1`
6. Validate the parameter set.
7. Preview the expansion.
8. Approve the preview.
9. Verify:
   - preview consequences are deterministic
   - acceptance was committed through M8
   - inspection and semantic review refreshed
   - origin traceability is visible with accepted expansion id, command id, parameter values, and memberships
10. Dismiss the preview panel and confirm origin traceability remains inspectable.

## What To Expect

- The catalog lists three checked-in governed macros from the proof repository.
- `DOL Starter` previews two governed components plus one governed connection.
- Approval returns one committed command id and one accepted expansion id.
- Origin inspection reveals which accepted expansion produced the structure and which parameter values were used.
- Re-running the same repository and parameter set produces the same preview, accepted structure, and origin outputs.

## Deterministic Verification

Run verification sequentially on this Windows repository:

```powershell
java25; .\gradlew.bat --no-daemon --console=plain :kernel:runtime:test --tests *M16Proof* --tests *origin*
java25; .\gradlew.bat --no-daemon --console=plain :ide:lsp:test --tests *origin*
cd ide
yarn workspace @engineeringood/athena-theia-frontend test
yarn build
yarn start:smoke:reuse-catalog
cd ..
powershell -ExecutionPolicy Bypass -File .\tools\encoding-audit.ps1
```

## Product Position

M16 keeps Athena aligned with the core product rule:

- reusable engineering structure is semantic-first
- package governance stays in the repository
- canonical mutation authority stays in M8
- accepted reuse remains inspectable after the preview interaction ends
