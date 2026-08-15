# M007 Graphite Source-base Transplant Verification

**Date:** 2026-08-15
**Branch:** `next-001`
**Graphite revision:** `461ddbc8726c587a8abc536cab301b0b2206a54c`
**Result:** PASS

## Source And Cleanup Gates

- Copy Gate: PASS. SHA-256 verification found 1,035 matching files, zero
  missing files, and zero mismatches.
- License Gate: PASS. Graphite's Apache-2.0 `LICENSE.txt`, notices, and source
  provenance remain present.
- Legacy Cleanup Gate: PASS. The pre-M007 Athena runtime and custom shell are
  absent and remain recoverable from commit `45af3e6` and earlier.
- Source Integrity Gate: PASS. Starting and exercising Web and desktop caused
  no tracked Graphite source changes.

The detailed copy manifest is
`2026-08-15-m007-graphite-source-manifest.csv`; cleanup evidence is
`2026-08-15-m007-legacy-cleanup.md`.

## Toolchain Gate

Graphite's requirement checker passed with:

- Rust and Cargo `1.97.1`
- `wasm32-unknown-unknown`
- `wasm-bindgen-cli 0.2.121`
- `cargo-about 0.9.0`, the version pinned by Graphite's Windows CI
- Node.js `24.15.0`
- Vite `8.0.3`
- CMake `3.30.5-msvc23`
- Ninja `1.12.1`

`cargo-about` must run without a PowerShell ancestor on Windows because it
rejects redirected output from PowerShell. The verified launch uses Git Bash,
matching the shell requirement documented in Graphite's Windows CI.

## Web Baseline Gate

The imported root's documented `cargo run` command is running the original
Graphite Vite/WASM application at `http://127.0.0.1:8080/`.

- HTTP status: `200`
- HTML title: `Graphite`
- Listening process: Vite PID `58812`
- Welcome DOM: 24,060 HTML bytes, 12 buttons, 7 SVG elements
- Editor interaction: `New Document -> OK`
- Editor DOM: 93,640 HTML bytes, 68 buttons, 81 SVG elements, 2 canvases
- Runtime exceptions: 0
- Failed network requests: 0
- HTTP responses at or above 400: 0

Chrome reported only expected environment warnings: Windows ignores WebGPU's
power-preference hint, and the isolated headless profile did not grant durable
OPFS storage. Neither warning blocked editor rendering or interaction.

Evidence:

- `assets/m007/graphite-web-1440x900.png`
- `assets/m007/graphite-web-editor-1440x900.png`

## Desktop Baseline Gate

The imported root's documented `cargo run desktop` command completed its first
native build in 65 minutes 16 seconds and launched Graphite's CEF desktop app.

- Main process: `Graphite.exe`, PID `58240`
- Main window title: `Graphite`
- Window responding: true
- Renderer: DirectX 12 on Intel Iris Xe Graphics
- Interaction: `Ctrl+N`, then click the original `OK` button
- Result: responsive `Untitled Document` editor with toolbar, rulers, canvas,
  Properties, Layers, and status bar

Vulkan discovery logged that no Vulkan driver was installed, then Graphite
selected its available DirectX 12 adapter and continued successfully.

Evidence:

- `assets/m007/graphite-desktop-1440x900.png`
- `assets/m007/graphite-desktop-editor-1440x900.png`

The Windows display uses 200% scaling. Desktop captures were taken with
per-monitor DPI awareness from the complete physical window and downsampled to
the required 1440x900 evidence size.

## Visual And Runtime Gates

All four captures are exactly 1440x900. An 8-pixel sample grid found between
114 and 494 distinct colors per image and visible non-background content in
every image. Manual inspection confirms the imported Graphite menu bar,
maximized canvas workspace, tool shelf, Properties/Layers panels, document
tabs, and status bar are present without Athena's removed custom shell.

- Visual Gate: PASS
- Web Baseline Gate: PASS
- Desktop Baseline Gate: PASS
- User Gate: PASS by the direct 2026-08-15 instruction to finish and prove the
  Graphite baseline, then proceed immediately to a minimal QElectroTech proof.

At verification time, both the Web service and desktop application remain
running for inspection.
