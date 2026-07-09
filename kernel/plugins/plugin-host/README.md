# `:kernel:plugins:plugin-host`

English | [Chinese (Simplified)](README.zh-CN.md)

The `:kernel:plugins:plugin-host` module owns Athena's hosted plugin governance boundary. It separates plugin source enumeration from plugin approval, and it exposes the host-owned lifecycle and inventory registry so compiler and runtime can consume one deterministic approved inventory without inheriting `ServiceLoader` or activation policy as private implementation details.

## Responsibilities

- Define the hosted plugin source contract.
- Provide the JVM-first `ServiceLoader` source implementation.
- Materialize deterministic plugin candidate and rejection records.
- Validate manifest compatibility, typed-contract conformance, extension-point legality, and forbidden ownership claims.
- Build the approved plugin inventory shared by compiler and runtime.
- Govern optional semantic review-enrichment contributors with the same approval and hosted-runtime contract rules used for other hosted extension points.
- Expose host-owned `loaded`, `initialized`, and `shutdown` lifecycle state over one approved inventory.
- Keep source enumeration and approval as separate explicit layers.

## Main Types

- `AthenaPluginSource`
- `ServiceLoaderAthenaPluginSource`
- `AthenaPluginCandidateSource`
- `AthenaPluginApprovalService`
- `AthenaPluginValidator`
- `AthenaPluginDiscovery`
- `AthenaHostedPluginRegistry`
- `AthenaApprovedPluginInventory`

## Dependencies

- `:kernel:plugins:plugin-api`

## Boundaries

This module does not own canonical engineering semantics, compiler pass orchestration, or runtime lifecycle orchestration. It exists to govern which hosted plugins are available to those layers and why.

## Verification

```bash
./gradlew :kernel:plugins:plugin-host:test
```

Windows PowerShell:

```powershell
java25; .\gradlew.bat :kernel:plugins:plugin-host:test
```

