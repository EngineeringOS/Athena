# M005 Application Spine and Folio Foundation Verification

## Result

`PARTIALLY VERIFIED` on 2026-08-14. The deterministic application, domain,
desktop adapter, WASM adapter, persistence, and browser workflow gates passed.
The M005 shell was not visually certified and is superseded by the approved
M006 Graphite shell replacement.

## Confirmed

- `canonical_m005_messages` carries explicit project, initial-folio, control,
  and I/O folio identities.
- Native `AthenaEditor` and JSON-routed `WebEditorCore` replay produce identical
  serialized application state, state hashes, and ordered semantic effects.
- `Project::new_with_ids` and `Folio::with_id` make creation identity explicit
  without moving identity generation into browser or desktop presentation code.
- Desktop, browser, and WASM protocol call sites supply the initial folio ID.

## Fresh Automated Evidence

Run from `rust/` on 2026-08-14:

```powershell
cargo fmt --all -- --check
cargo clippy --workspace --all-targets -- -D warnings
cargo test --workspace -- --test-threads=1
cargo check -p athena-domain -p athena-format -p athena-editor -p athena-application -p athena-web-core --target wasm32-unknown-unknown
```

Result: exit `0`. All workspace tests passed, including:

- `canonical_m005_messages_have_a_deterministic_native_state_and_trace`;
- `native_and_web_protocols_produce_identical_state_hashes_and_effect_traces`;
- 6 application dispatch tests and 6 application plate tests;
- 7 GPUI adapter unit tests and 8 desktop adapter integration tests;
- 3 web-core protocol tests;
- all existing domain, editor, format, geometry, library, render, and persistence
  regression suites.

The Windows toolchain emitted non-failing linker informational messages, a
`proc-macro-error2` future-incompatibility notice, and one denied incremental
cache finalization notice. The next compilation cannot reuse that one cache;
no test or compiler gate failed.

Run from `rust/` with the locally matched Chrome 150 driver:

```powershell
wasm-pack test --headless --chrome --chromedriver C:\Users\admin\AppData\Local\Temp\athena-chromedriver-150.0.7871.124\chromedriver-win64\chromedriver.exe crates/web-core
```

Result: exit `0`, `1 passed`, `0 failed`. An earlier attempt without the
explicit `--chromedriver` option selected cached ChromeDriver 152 against Chrome
150 and failed session creation; it is recorded as a failed attempt, not a pass.

Run from repository root:

```powershell
npm test
```

Result: exit `0`, `3 passed`, `0 failed`, using one Playwright worker.

## Open and Superseded Gates

- `SKIPPED/SUPERSEDED`: M005 desktop 1440x900 screenshot. The desktop process
  exited before the earlier capture attempt, so no desktop visual proof exists.
- `SKIPPED/SUPERSEDED`: M005 web 1440x900 and 390x844 visual acceptance. The old
  static/invented shell is intentionally removed by M006.
- `OPEN`: no user acceptance was claimed for the M005 shell.
- `OPEN`: no professional/reproduced/mirrored visual claim applies to M005.

M006 now owns the source ledger, recursive shell, native/web equivalence,
screenshots, metric comparison, desktop runtime, and explicit user gate.
