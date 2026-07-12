---
baseline_commit: c278a71
---

# Story 2.2: Publish A Larger Electrical Proof Repository And Verification Path

Status: done

## Completion Summary

- Published `examples/m11/dense-electrical-proof` with repository contract files, a 16-component / 48-port / 29-connection electrical fixture, and M9 knowledge-bearing motor properties.
- Added `examples/m11/README.md` and `README.zh-CN.md` to document purpose and usage.
- Added focused compiler, runtime, and LSP proof tests that execute the dense repository through the normal JVM path.

## Acceptance Outcome

1. M11 now ships a materially denser electrical proof fixture than earlier toy graphs.
2. The verification path is explicit and repeatable.
3. Known density is now part of the proof corpus instead of hidden in ad hoc manual demos.

## Verification

- `cmd /c "call java25 && .\\gradlew.bat --no-daemon --console=plain :kernel:compiler:test --tests com.engineeringood.athena.compiler.AthenaCompilerM11DepthTest"`
- `cmd /c "call java25 && .\\gradlew.bat --no-daemon --console=plain :kernel:runtime:test --tests com.engineeringood.athena.runtime.AthenaRuntimeProjectionDepthTest"`
- `cmd /c "call java25 && .\\gradlew.bat --no-daemon --console=plain :ide:lsp:test --tests com.engineeringood.athena.ide.lsp.AthenaProjectionM11DepthRequestTest"`

## Key Files

- `examples/m11/dense-electrical-proof/athena.yaml`
- `examples/m11/dense-electrical-proof/athena.lock`
- `examples/m11/dense-electrical-proof/src/assembly-line.athena`
- `examples/m11/README.md`
- `examples/m11/README.zh-CN.md`
