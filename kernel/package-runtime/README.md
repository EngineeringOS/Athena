# `:kernel:package-runtime`

The package runtime resolves admitted package descriptors, typed binding rules, representation
materials, and package-local resources. Resolution is deterministic and repository-driven.

## Boundaries

- Production code never loads milestone sample sets or demo fixtures.
- Athena source and governed package descriptors remain authoritative; generated snapshots are
  derived evidence.
- Legacy policy-tag adapters are not part of the active runtime.
- SVG resources provide geometry only and cannot become engineering metadata authority.

## Verification

```powershell
.\gradlew.bat --no-daemon --console=plain :kernel:package-runtime:test
```
