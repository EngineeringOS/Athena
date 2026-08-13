# M002 Corrective Operability Design

## Status

Implementation started after reproducing a real browser failure in the
packaged WASM shell. This corrective slice remains part of M002 and blocks any
new milestone until the actual desktop and browser workflows are operable.

## Evidence

The browser regression test reproduced:

- symbol library button rendered correctly;
- selecting `Resistor` succeeded;
- clicking the canvas raised `Cannot convert 117 to a BigInt`;
- the scene remained `0 interactive regions`.

The cause was a WASM ABI mismatch: browser-facing exports used Rust `i64`,
which `wasm-bindgen` exposes as JavaScript `BigInt`, while `bootstrap.js`
passed ordinary JavaScript numbers from pointer coordinates and grid controls.

## Design

All JavaScript-facing numeric WASM inputs use `i32` and are converted at the
adapter boundary into the core's `i64` document coordinates. The shared core
model and command contracts remain unchanged. Browser event handlers retain
their existing thin-adapter role and do not own schematic state.

The corrective verification loop includes:

- a browser regression test that asserts library boot, scene initialization,
  placement, and absence of page errors;
- the existing browser authoring smoke;
- web-core contracts, strict clippy, WASM packaging, and JavaScript syntax;
- native desktop authoring tests and a native process smoke.

## Acceptance

- Browser placement changes the scene through normal pointer/click input.
- Browser console and page errors remain empty during the regression flow.
- Existing desktop and web-core contracts remain green.
- The correction is documented and committed before further UI work.
