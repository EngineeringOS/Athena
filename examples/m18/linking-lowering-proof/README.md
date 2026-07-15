# M18 Linking And Lowering Proof

These fixtures prove the Epic 3 semantic path through compiler-owned project snapshots:

- declaration indexing
- import resolution where a fixture imports another governed package
- reference linking
- linked source-unit lowering

## Inventory

- `single-package-success.athena` - one source unit declares and references a local port.
- `cross-source-provider.athena` and `cross-source-consumer.athena` - one package links across source units.
- `cross-package-vendor.athena` and `cross-package-consumer.athena` - a root package imports a governed dependency namespace and links to its port.
- `unresolved-symbol.athena` - an authored endpoint has no declaration candidate.
- `invalid-availability-consumer.athena` - a dependency package exists but no authored resolved import exposes its namespace.

The corpus is local and governed by compiler test setup. It does not imply remote registry,
marketplace, publish, multi-root, frontend-owned semantic resolution, canvas behavior, or
Kotlin Compose desktop-viewer behavior.
