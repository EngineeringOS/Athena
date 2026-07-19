# M24 Examples

- `sample-project/` is the openable IDE workspace for M24.
- `sample-project/src/01-control-route.athena` shows a PLC-to-HMI control route.
- `sample-project/src/02-terminal-strip-routes.athena` shows ordered PLC-to-terminal-strip-to-load routes.
- `sample-project/src/03-power-protection-load.athena` shows a narrow 24V power/protection/load route.
- `../../docs/usages/m24-routing-acceptance-proof.md` is the routing acceptance proof; it records
  the M23-vs-M24 comparison and deferred boundaries.
- `../../docs/usages/m24-proof-usage.md` records the IDE usage and verification path.

Open the sample with:

```powershell
yarn --cwd ide start:m24
```

The sample uses real `.athena` source files. It is not a `.mjs` test fixture.
