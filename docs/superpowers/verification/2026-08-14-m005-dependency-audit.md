# M005 Dependency Boundary Audit

## Scope

This record verifies M005 Task 1 against CAP-M005-001,
GRA-APP-001, GRA-DISPATCH-001, GRA-MSG-001, GRA-FRONTEND-001, and the
`AGENTS.md` latest-stable and platform-neutral core guardrails.

## Toolchain

`rustup update stable` completed successfully on 2026-08-14 and reported:

```text
stable-x86_64-pc-windows-msvc unchanged - rustc 1.97.1 (8bab26f4f 2026-07-14)
```

`rustc --version --verbose` reported:

```text
rustc 1.97.1 (8bab26f4f 2026-07-14)
binary: rustc
commit-hash: 8bab26f4f68e0e26f0bb7960be334d5b520ea452
commit-date: 2026-07-14
host: x86_64-pc-windows-msvc
release: 1.97.1
LLVM version: 22.1.6
```

## Direct Dependency Audit

The required searches returned `gpui 0.2.2` and `gpui-component 0.5.1`.
`cargo info` was run for every direct workspace dependency:

| Dependency | Latest stable | Declared | Rust version | Compatibility evidence |
| --- | --- | --- | --- | --- |
| `serde` | 1.0.229 | 1.0.229 | 1.56 | Supported by Rust 1.97.1. |
| `serde_json` | 1.0.151 | 1.0.151 | 1.71 | Supported by Rust 1.97.1. |
| `uuid` | 1.24.0 | 1.24.0 | 1.85 | Supported by Rust 1.97.1; existing `v4`, `serde`, and `js` features retained. |
| `thiserror` | 2.0.20 | 2.0.20 | 1.71 | Supported by Rust 1.97.1. |
| `wasm-bindgen` | 0.2.127 | 0.2.127 | 1.77 | Supported by Rust 1.97.1. |
| `gpui` | 0.2.2 | 0.2.2 | Not published | The desktop crate compiled in the workspace check. |
| `gpui-component` | 0.5.1 | 0.5.1 | Not published | Its published dependency is `gpui = 0.2.2`; the desktop crate compiled with this pair. |

All third-party declarations remain centralized in `rust/Cargo.toml`.
`Cargo.lock` already resolved the audited direct releases, so its only Task 1
package addition is `athena-application`. A `thiserror 1.0.69` package remains
as a transitive dependency; Athena workspace crates select `thiserror 2.0.20`.

## RED/GREEN Evidence

RED was run before adding the application member. The metadata check required
the new package and exited 1 with the intended failure:

```text
dependency boundary audit: missing workspace package athena-application
```

GREEN used `cargo metadata --format-version 1 --no-deps` to require all four
core packages, reject forbidden platform dependencies, and compare the
application dependency set exactly. It also scanned the four core source trees
for `std::fs`, `std::path`, GPUI, WASM binding, and browser API imports.

```powershell
$metadata = cargo metadata --format-version 1 --no-deps | ConvertFrom-Json
$coreNames = @('athena-domain', 'athena-format', 'athena-editor', 'athena-application')
$forbidden = @('athena-desktop', 'athena-web-core', 'gpui', 'gpui-component', 'wasm-bindgen', 'web-sys', 'js-sys')

foreach ($coreName in $coreNames) {
  $package = $metadata.packages | Where-Object name -eq $coreName
  if (-not $package) { throw "missing workspace package $coreName" }
  $bad = @($package.dependencies | Where-Object { $forbidden -contains $_.name })
  if ($bad.Count -gt 0) { throw "$coreName has forbidden platform dependencies" }
}

$application = $metadata.packages | Where-Object name -eq 'athena-application'
$actual = @($application.dependencies | ForEach-Object name | Sort-Object -Unique)
$expected = @('athena-domain', 'athena-editor', 'athena-format', 'serde', 'serde_json', 'thiserror', 'uuid') | Sort-Object
if (Compare-Object $expected $actual) { throw 'application dependency set differs' }

rg -n 'std::fs|std::path|wasm_bindgen|web_sys|js_sys|gpui|athena_desktop|athena_web' `
  crates/domain/src crates/format/src crates/editor/src crates/application/src
if ($LASTEXITCODE -eq 0) { throw 'forbidden platform API/import found' }
if ($LASTEXITCODE -ne 1) { throw "source audit failed: $LASTEXITCODE" }
```

The GREEN run exited 0:

```text
dependency boundary audit passed: 4 core crates, exact application edge set, no forbidden platform/API matches
```

## Metadata Edges

`cargo metadata --format-version 1 --no-deps` recorded these direct edges:

```text
athena-application -> athena-domain, athena-editor, athena-format, serde, serde_json, thiserror, uuid
athena-desktop -> athena-domain, athena-editor, athena-format, athena-geometry, athena-library, athena-render, gpui, gpui-component, uuid
athena-web-core -> athena-domain, athena-editor, athena-format, athena-geometry, athena-library, athena-render, serde, serde_json, wasm-bindgen
```

The application boundary points only inward. Desktop and web adoption of that
boundary remains scheduled for M005 Tasks 7 and 8; neither platform package is
reachable from domain, format, editor, or application.

## Verification Results

| Command | Result |
| --- | --- |
| `cargo fmt --all -- --check` | PASS |
| `cargo check -p athena-application` | PASS |
| `cargo check --workspace` | PASS; emitted the upstream future-incompatibility warning described below. |
| `cargo test -p athena-application` | PASS; 0 unit tests and 0 doc tests in the intentionally empty crate. |
| `cargo test --workspace` | PASS; 110 tests passed, 0 failed. |
| Dependency metadata and source audit | PASS |

`cargo check --workspace` reports that transitive `proc-macro-error2 2.0.1`
contains code that a future Rust release will reject. Rust 1.97.1 accepts it;
upstream dependency replacement is not part of this boundary-only task. The
workspace test also emitted Windows linker status messages and a non-fatal
incremental-cache access-denied note; all test binaries linked and all tests
completed successfully.
