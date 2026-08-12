# M46 Verification Log

Date: 2026-08-12
Repository: `examples/m46/rolling-shutter`

All Gradle commands ran sequentially. Affected Kotlin suites, `:ide:lsp:test`, and
`:ide:lsp:installDist` passed. Tree-sitter generated/tests/WASM passed with 23/23 parses.
Frontend contracts passed; frontend tests passed with 88/88; full Theia/Electron product build passed.

Product proofs passed from rebuilt output:

- `verify:m46-performance`
- `verify:m46-authoring`
- `verify:m46-export`

Final closure rerun:

- `tools/start-athena-clean.ps1`: rebuilt LSP distribution and all Theia packages, then opened
  `examples/m46/rolling-shutter`.
- `ide/theia-frontend` `node --test scripts/athena-presentation-layout.test.mjs`: 8 passed, 0 failed.
- `ide/theia-product` `node --test scripts/athena-m46-authoring.test.mjs`: 8 passed, 0 failed.
- `verify:m46-authoring`: passed, regenerating
  `operation-transcripts/4-3-author-reopen-product-proof.json` and desktop/narrow/reopen screenshots.

Export evidence:

- SVG: 23,837 bytes, `sha256:879dea71487695aae4f3cedf44d4ce2e01e9de467e780b7dd6c53adf043e09d7`
- PNG: 27,104 bytes, `sha256:cc4d832ef40b19813d79ef4fe0089923ef874f6a240f48bd2542e340859ebc66`
- Viewport: 1700x1600, DPR 1
- Visual facts: 9 admitted images, 11 orthogonal route segments, 4 one-pixel frame segments, 9 compact labels,
  2 topology markers, 0 construction-grid patterns, 0 bottom-table lines

Hygiene:

- `tools/encoding-audit.ps1`: passed
- `tools/source-set-hygiene-audit.ps1`: passed
- `git diff --check`: passed

Correction recorded:

M46 export first failed because verifier inherited stale M45 `routes == 10` expectation. Generated M46
Canonical Scene publishes 11 route segments due to orthogonal bends. M46 verifier now asserts 11;
renderer and scene authority unchanged. Rerun passed.

Resolver and editor corrections recorded:

- Stale `examples/m46/rolling-shutter/athena.lock` blocked LSP source revision validation. The lock was
  rematerialized by compiler authority, never hand-edited.
- Concrete `.sheet.athena` pages no longer render folio child-page controls. The widget requests Folio pages and
  renders the folio bar only for `.folio.athena`; frontend regression coverage passed.
