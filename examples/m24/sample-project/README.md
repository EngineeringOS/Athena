# M24 Sample Project

This is the openable Athena workspace for the M24 routing fidelity milestone.

Open this folder in the IDE to verify real `.athena` routing scenarios:

- `src/01-control-route.athena` - PLC-to-HMI control route
- `src/02-terminal-strip-routes.athena` - PLC-to-terminal-strip-to-load route bundle
- `src/03-power-protection-load.athena` - 24V power, protection, contactor, and motor route

Use:

```powershell
yarn --cwd ide start:m24
```

The sample is the product-facing proof path. Review it in the IDE, Graphical View, outline, and
inspection surfaces. No test scripts are required to understand the scenarios.
