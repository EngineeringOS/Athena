# M45 Verification Log

Date: 2026-08-10
Result: PASS

All Gradle commands ran sequentially with no overlapping Gradle process.

## Kotlin

| Command | Tests | Result |
| --- | ---: | --- |
| `.\gradlew.bat --no-daemon --console=plain :kernel:language:test` | 30 | PASS |
| `.\gradlew.bat --no-daemon --console=plain :kernel:repository-model:test` | 5 | PASS |
| `.\gradlew.bat --no-daemon --console=plain :kernel:package-model:test` | 17 | PASS |
| `.\gradlew.bat --no-daemon --console=plain :kernel:package-runtime:test` | 25 | PASS |
| `.\gradlew.bat --no-daemon --console=plain :kernel:compiler:test` | 223 | PASS |
| `.\gradlew.bat --no-daemon --console=plain :kernel:interaction-model:test` | 5 | PASS |
| `.\gradlew.bat --no-daemon --console=plain :kernel:presentation-model:test` | 15 | PASS |
| `.\gradlew.bat --no-daemon --console=plain :kernel:svg-renderer:test` | 3 | PASS |
| focused LSP RED/GREEN regression | 18 | PASS |
| `.\gradlew.bat --no-daemon --console=plain :kernel:runtime:test` | 27 | PASS |
| `.\gradlew.bat --no-daemon --console=plain :ide:lsp:test` | 55 | PASS |

The final full LSP invocation executed 19 test classes and 55 tests with zero failures, errors, or skips. A RED run caught one stale style-companion expectation after the golden style changed; the corrected focused test and full LSP suite then passed.

## Frontend And Product

| Command | Result |
| --- | --- |
| `yarn test` in `ide/theia-frontend` | PASS, 52/52 |
| `yarn build` in `ide` | PASS, frontend/backend/product + fresh LSP install |
| `yarn verify:m45-proof` in `ide` | PASS |
| `yarn workspace @engineeringood/athena-theia-product verify:m45-export` in `ide` | PASS |

Product proof facts: exact workspace root and LSP root `examples/m45/rolling-shutter`; Repository Session READY; publication READY; frame 17x16; 13 occurrences; 10 routes; desktop and narrow paint/source-trace proof.

## Hygiene

| Command | Result |
| --- | --- |
| `powershell -ExecutionPolicy Bypass -File .\tools\source-set-hygiene-audit.ps1` | PASS |
| `powershell -ExecutionPolicy Bypass -File .\tools\encoding-audit.ps1` | PASS |
| targeted active production/M45 forbidden authority scan | PASS, zero matches |
| production filename scan for milestone/demo/proof/sample classes | PASS, zero matches |

## Evidence Digests

| Artifact | SHA-256 |
| --- | --- |
| `examples/m45/rolling-shutter/athena.lock` | `8fc4e7c3b5b1810020850cad44eef061d0dcb2a118bf6c2fb869953f92f71850` |
| `m45-product-proof.json` | `d09211093cfd44ee780a1b085dafaa0d4d4c64daea7685a876588178c86ab504` |
| `m45-story-5-4-proof.json` | `b2ced5cdb1d75532c517902c8ed24b58e497313464afa7d52a30274ee378e9d0` |
| `exports/m45-export-proof.json` | `308932c839aad4bd99087076d2cd9e86c88f381fef2a8c189e9a34b331147548` |
| `evidence/m45-operation-transcript.txt` | `60c0a8144c0bd929dcc3f2ab71c5b2c6019c5fbeb426658d7b7b328979e61de3` |
| `evidence/m45-reopen-evidence.txt` | `9abdc2df021753fb7fe8050be5e08bfa514f6bc7182c58145debe7aa8f8571e7` |
| `evidence/m45-lock-lineage-snapshot.txt` | `059b2c2e9b4a5a7d0e97a735319bc4792f892dc6810bae5c62b417e2851747a0` |
