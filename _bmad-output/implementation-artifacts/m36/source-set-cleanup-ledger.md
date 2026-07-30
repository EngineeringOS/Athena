# M36 Production Source-Set Cleanup Ledger

## Root Cause

The pollution was a process failure, not an attributable individual decision. Milestone acceptance
criteria asked for structured proof payloads and demo slices but did not require source-set
segregation. Reviews checked behavior and passing tests without a repository-wide naming or
production-source guard. Later stories reused convenient milestone classes, turning fixtures into
live compiler and LSP dependencies.

Git history confirms the files entered through milestone delivery commits: M30 in `cbe65c3`, M32
in `0311ad6`, M34/M35 integration in `14ad495`, and M35 completion in `a419b27`.

## Dependency And Disposition Map

| Original production file | Runtime impact before cleanup | Disposition |
| --- | --- | --- |
| `compiler/M32PackageBackedPresentationFactDeriver.kt` | `PresentationModelDeriver` called a hardcoded sample package set during live derivation. | Deleted; production derivation no longer has the sample fallback. |
| `package-runtime/M32SamplePackageSet.kt` | Supplied hardcoded package/profile/binding material to the compiler fallback and smoke runners. | Moved to `src/test`; no production callers. |
| `package-runtime/M32ProductSmokeProof.kt` | Test runner and evidence payload were compiled into the runtime artifact. | Moved to `src/test`. |
| `package-runtime/M32DemoLayoutDensityProof.kt` | Demo density assertions were compiled into the runtime artifact. | Moved to `src/test`. |
| `representation-model/M30DemoRepresentationBinder.kt` | No production caller; tests depended on it as a fixture binder. | Moved to `src/test`. |
| `representation-model/M30ControlSheetCompositionProof.kt` | No production caller; milestone composition evidence helper. | Moved to `src/test`. |
| `representation-model/M34CabinetRenderPathProof.kt` | Compiler tests used it, but the representation artifact exported it. | Moved to compiler `src/test`. |
| `compiler/AthenaM35CabinetProjectionCompiler.kt` | Live LSP Cabinet projection depended on a milestone-named compiler and hardcoded example branches. | Renamed `AthenaCabinetProjectionCompiler`; LSP selects it from typed installation declarations. |
| `runtime/AthenaAiDeterministicProofProvider.kt` | A mock provider was the production LSP default and could emit fake AI answers. | Deleted; unconfigured production sessions report provider unavailable. |
| `package-runtime/LegacyBindingPolicyTagRuleAdapter.kt` | Unused fixture bridge remained in the production artifact. | Deleted; no compatibility shim. |
| `apps/desktop-viewer` | Excluded Gradle module retained only historical proof/smoke flows. | Deleted as stale; Theia is the active IDE surface. |

## Production Renames

- `AthenaRouteEngineV0` -> `AthenaRouteEngine`
- `PhysicalConstraintEvaluatorV0` -> `PhysicalConstraintEvaluator`
- `AthenaIndustrialControlV0Profile` -> `AthenaIndustrialControlProfile`
- Production `*Proof` contracts with real runtime value were renamed to responsibility-based
  `*Evidence`, `*Validation`, or `*Report` contracts.

## Prevention

`tools/source-set-hygiene-audit.ps1` scans Athena-owned `src/main` roots, excluding imported
`reference/` mirrors. Root Gradle `check`, `test`, and `build` depend on `sourceSetHygieneAudit`.
The companion review checklist must be applied to every story that adds or moves production files.
