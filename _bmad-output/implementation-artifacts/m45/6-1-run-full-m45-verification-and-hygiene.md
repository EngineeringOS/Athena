---
baseline_commit: 9ca7c80239d38d9e2a4880886e71e27e2923c41b
---

# Story 6.1: Run Full M45 Verification and Hygiene

Status: done

## Story

As a maintainer,
I want sequential product verification and architecture hygiene,
so that M45 closure is backed by reproducible evidence rather than accumulated story status.

## Acceptance Criteria

1. Every M45-affected Gradle module passes its complete test suite in strict sequence on Windows. At minimum this covers `kernel:language`, `kernel:repository-model`, `kernel:package-model`, `kernel:package-runtime`, `kernel:compiler`, `kernel:interaction-model`, `kernel:presentation-model`, `kernel:svg-renderer`, and `ide:lsp`; no two `gradlew` processes overlap.
2. Frontend contract generation/checks, complete frontend tests, full IDE/Product build, real Electron M45 product proof, and self-regenerating export proof pass from rebuilt outputs. Product proof opens `examples/m45/rolling-shutter` as workspace first, then proves Repository Session `READY`, exact LSP root, 17x16 frame, 13 package-backed occurrences, 10 routes, source trace, desktop screenshot, and narrow screenshot.
3. Acceptance audit maps every Story 1.1-5.4 AC and M45 success metric to an actually passing test or regenerated product artifact. Valid Macro, Variant, and typed Placeholder resolution must be proven through native admitted package items and the active compiler/runtime path; rejection-only or model-only tests do not satisfy this criterion.
4. M45 golden artifacts are regenerated under `_bmad-output/implementation-artifacts/m45/` and validated: canonical lock, admission/package snapshot evidence, lineage, operation transcript, reopen evidence, deterministic SVG/PNG exports, proof JSON, and desktop/narrow screenshots. Repeated export stays deterministic and visible output follows Engineering Document Visual Golden Rule.
5. Encoding audit, source-set hygiene audit, and targeted stale-path scans pass. Production compiler/runtime contains no `.elmt`, HTML/XML catalog parser or converter, `reference/elements` access, `reference/elements_contrib` access, `@Deprecated`/`DEPRECATION` compatibility path, Lock V2, `LocalPackageRegistry`, `representationPackageRoots`, `symbol.yaml`, `athena-symbol-v1`, or milestone/demo/proof/sample production class.
6. Any verification failure is fixed at current M45 authority, covered by a failing-then-passing test, and followed by rerunning affected and downstream verification. No compatibility shim, fallback asset, alias, skipped test, weakened assertion, stale evidence acceptance, or fabricated status is introduced.
7. Story records contain exact commands/results, evidence paths, changed files, and residual risks. Stories 1.1-5.3 and Epics 1-5 move to `done` only after their AC evidence passes this audit; otherwise they remain open with a recorded blocking gap.

## Tasks / Subtasks

- [x] Task 1 - Build a truthful acceptance and evidence inventory (AC: 3, 7)
  - [x] Read every M45 Story 1.1-5.4 record and extract AC-to-test/evidence claims; reject unchecked tasks, missing records, or claims without a runnable test/artifact.
  - [x] Inspect active `examples/m45/rolling-shutter` packages, lock, source, Sheet, binding companion, generated scene, and proof artifacts against PRD success metrics.
  - [x] Prove at least three direct packages, ten admitted Symbol/Element definitions, five compatible Parts, one composite Element, three valid Variants, one valid typed Placeholder value set, one Function with two projection bindings, visible lineage, 10+ occurrences, and fail-closed invalid cases.
  - [x] Add focused RED tests and native example/package declarations for any uncovered required path. Do not weaken PRD, architecture, or existing assertions.

- [x] Task 2 - Run complete affected Kotlin verification sequentially (AC: 1, 3, 6)
  - [x] Run one Gradle invocation at a time with `--no-daemon --console=plain`; capture command, exit status, and test count/output in M45 test evidence.
  - [x] Run complete affected module suites, not only focused test filters. If a failure exposes stale pre-M42 behavior, delete/refactor the stale production/test path instead of adding compatibility.
  - [x] After any Kotlin fix, rerun focused RED/GREEN proof, complete affected module, and every downstream module influenced by the change.

- [x] Task 3 - Rebuild and verify frontend/Product (AC: 2, 4, 6)
  - [x] Regenerate/check typed frontend contracts and run complete `ide/theia-frontend` tests.
  - [x] Run full `ide` build before Electron proof so no stale frontend bundle can mask behavior.
  - [x] Run real `verify:m45-proof`, then self-regenerating `verify:m45-export`; no embedded long-running IDE shell is used.
  - [x] Inspect both regenerated screenshots and compare frame, rulers, blank canvas, asset proportions, compact labels, thin routes, and tiny connection points to `draft/screenshort/equipement_d'un_volet_roulant.png`.

- [x] Task 4 - Run architecture and repository hygiene gates (AC: 5, 6)
  - [x] Run `tools/encoding-audit.ps1` and `tools/source-set-hygiene-audit.ps1` after all source/doc edits.
  - [x] Scan active production roots and M45 example/package inputs for forbidden external-format/reference access, retired package contracts, compatibility aliases/fallbacks, deprecations, and proof/demo/sample/milestone production names.
  - [x] Classify matches by executable production path. Reference source trees and historical BMad artifacts are not runtime inputs, but active production references to them fail the story.
  - [x] Confirm repository text remains UTF-8 and no `.zh-CN.md` BOM rule regresses.

- [x] Task 5 - Publish verification evidence and close verified work (AC: 1-7)
  - [x] Save a machine-readable verification summary and readable command log under `_bmad-output/implementation-artifacts/m45/` with artifact digests and AC mapping.
  - [x] Complete this story's Tasks, Debug Log, Completion Notes, File List, and Change Log with exact evidence.
  - [x] Mark verified Stories 1.1-5.3 and Epics 1-5 `done`; keep any unverified item open and fix it before continuing.
  - [x] Move Story 6.1 to `review` only after all criteria pass, then update sprint status without invalid epic states.

### Review Follow-ups (AI)

- [x] [AI-Review][HIGH] Prove active native Macro insertion through admitted lock/runtime transaction; no unit-only resolver evidence. (AC: 3)
- [x] [AI-Review][HIGH] Prove typed Placeholder substitution changes visible Canonical Scene and export output. (AC: 3, 4)
- [x] [AI-Review][HIGH] Prove five compatible Parts through lock-backed `BindPart` transactions. (AC: 3)
- [x] [AI-Review][HIGH] Run every affected complete Kotlin suite, including `kernel:runtime`, strictly sequentially. (AC: 1)
- [x] [AI-Review][MEDIUM] Make acceptance audit machine-verifiable per Story 1.1-5.4 AC and M45 success metric. (AC: 3, 7)
- [x] [AI-Review][MEDIUM] Prove actual painted occurrence selection opens exact M45 source and creates Monaco trace decoration. (AC: 2)
- [x] [AI-Review][MEDIUM] Measure bottom-title-table absence and regenerate unobstructed desktop/narrow screenshots. (AC: 4)
- [x] [AI-Review][MEDIUM] Record exact forbidden scan roots, patterns, command, and zero-result evidence. (AC: 5, 7)

## Dev Notes

### Authority Guardrails

- Source owns Entity, Function, Engineering Port, Relationship, `FunctionPartBinding`, and `FunctionRepresentationBinding`.
- Package metadata owns reusable Symbol, Element, Part, Macro, Variant, Placeholder facts. SVG owns geometry only.
- Only `PACKAGE_READY` enters lock, ready snapshot, binding, Source Revision, Canonical Scene, export, or Theia paint.
- Macro is representation reuse, Variant preserves exact normalized interface fingerprint, Placeholder performs typed non-interface substitution. None may reason, select a Part, infer a Function, or create a Relationship.
- `SemanticPlacementIntent -> LogicalLayoutCoordinate -> PhysicalGeometryCoordinate`; compiler owns physical geometry and Snap remains separate.
- Canonical Scene is renderer-neutral. Theia/Konva is a typed read/intent client.

### Verification Order

Never overlap Gradle processes. Preferred sequence:

```powershell
.\gradlew.bat --no-daemon --console=plain :kernel:language:test
.\gradlew.bat --no-daemon --console=plain :kernel:repository-model:test
.\gradlew.bat --no-daemon --console=plain :kernel:package-model:test
.\gradlew.bat --no-daemon --console=plain :kernel:package-runtime:test
.\gradlew.bat --no-daemon --console=plain :kernel:compiler:test
.\gradlew.bat --no-daemon --console=plain :kernel:interaction-model:test
.\gradlew.bat --no-daemon --console=plain :kernel:presentation-model:test
.\gradlew.bat --no-daemon --console=plain :kernel:svg-renderer:test
.\gradlew.bat --no-daemon --console=plain :ide:lsp:test

Push-Location ide\theia-frontend
yarn test
Pop-Location

Push-Location ide
yarn build
yarn verify:m45-proof
yarn workspace @engineeringood/athena-theia-product verify:m45-export
Pop-Location

powershell -ExecutionPolicy Bypass -File .\tools\encoding-audit.ps1
powershell -ExecutionPolicy Bypass -File .\tools\source-set-hygiene-audit.ps1
```

If Gradle cache corruption symptoms appear, run the repository-mandated sequential clean once, then restart intended commands:

```powershell
.\gradlew.bat --no-daemon --console=plain clean
```

### Existing Proof Seams To Reuse

- `kernel/compiler/src/test/kotlin/com/engineeringood/athena/compiler/M45RollingShutterPageTest.kt`
- `ide/lsp/src/test/kotlin/com/engineeringood/athena/ide/lsp/M45ExportStabilityTest.kt`
- `ide/lsp/src/test/kotlin/com/engineeringood/athena/ide/lsp/RepresentationAndEngineeringOperationHandlerTest.kt`
- `ide/theia-product/scripts/athena-m45-proof-main.js`
- `ide/theia-product/scripts/verify-athena-m45-product-proof.js`
- `ide/theia-product/scripts/verify-athena-m45-export.js`
- `ide/theia-frontend/scripts/generate-diagram-contracts.mjs`
- `examples/m45/rolling-shutter/`

Do not create a second closure runner if current Gradle/Yarn/audit seams can produce the required evidence. A small M45 test-evidence aggregator under implementation artifacts is acceptable; production `src/main` proof code is forbidden.

### Previous Story Intelligence

- Story 5.4 already proved accepted Move/Change Symbol/Bind Part/Reconnect transactions, fail-closed stale/invalid edits, reopen identity stability, deterministic SVG/PNG, actual workspace/LSP roots, READY state, 17x16 frame, 13 occurrences, and 10 routes.
- Product proof and export proof delete stale evidence and regenerate it. Preserve this behavior.
- Companion files must remain excluded from the Project Semantic Graph; Function-owned Ports must remain indexed by canonical authored path.
- Story 5.4 review exposed the main closure risk: hardcoded PASS over stale evidence. Every Story 6.1 claim must parse current output or come from a passing test.
- Current unresolved audit risk: positive native Macro/Variant/Placeholder integration may be weaker than model/rejection coverage. Treat this as a required proof, not an assumption.

### Project Structure And Versions

- Stack remains repository-pinned: Kotlin 2.4.0, LSP4J 0.23.1, TypeScript 5.9.2, Node >=22, Yarn 1.22.22, Theia 1.73.1, Konva 10.3.0, React 18.3.1.
- No dependency upgrade or web research is required for this verification story.
- Product evidence belongs only under `_bmad-output/implementation-artifacts/m45/`.
- Active local packages remain direct children of `examples/m45/rolling-shutter/packages/`; no `registry/representation/engineering` nesting.

### References

- [M45 PRD](_bmad-output/planning-artifacts/prds/prd-Athena-2026-08-08-m45/prd.md)
- [M45 Addendum](_bmad-output/planning-artifacts/prds/prd-Athena-2026-08-08-m45/addendum.md)
- [M45 Architecture Spine](_bmad-output/planning-artifacts/architecture/architecture-Athena-2026-08-08-m45/ARCHITECTURE-SPINE.md)
- [M45 Epics](_bmad-output/planning-artifacts/m45/epics.md)
- [Story 5.4](_bmad-output/implementation-artifacts/m45/5-4-prove-edit-reopen-export-stability.md)
- [Repository Rules](AGENTS.md)

## Dev Agent Record

### Agent Model Used

GPT-5 Codex

### Debug Log References

- Ultimate context engine analysis completed - comprehensive developer guide created.
- Initial full LSP run exposed 12 failures: ten stale M44 fixture paths and two stale Variant/Placeholder expectations.
- Replaced stale fixtures with active M45 package-backed identities; no M44 compatibility path retained.
- Fixed `ChangeSymbol` as a complete Representation choice transaction: old Variant/Placeholder clauses clear or replace atomically.
- A nominal full LSP run was rejected as evidence because Gradle reused focused filter outputs; `--rerun-tasks` then executed all 51 tests.
- Full command/results and artifact digests recorded in `m45-verification-log.md` and `m45-verification-summary.json`.
- Independent acceptance and blind reviews rejected the earlier closure claim: Macro/Placeholder/Part product paths, complete runtime verification, source-trace UI proof, screenshot cleanliness, and machine-verifiable AC mapping lacked sufficient evidence. Story remains `in-progress` until these follow-ups pass.
- Closure rerun caught two visual authority leaks: `ScenePort.portId` was painted as label text, and occurrence selection automatically switched style authority. Removed both paths and added focused regression tests.
- Golden style source explicitly contained blue/bold/dotted values. Replaced them with black, one-pixel, solid, regular-weight values and regenerated desktop/narrow proof.
- Full sequential verification exposed one stale style-companion assertion. Corrected expected complete companion output, then passed focused and full LSP suites.

### Completion Notes List

- Story context created from final M45 PRD/addendum, architecture spine, epics, readiness report, complete sprint state, Story 5.4 review record, existing proof seams, and current git baseline.
- Positive native Macro/Variant/Placeholder integration is an explicit closure gate; rejection-only evidence cannot close M45.
- Added native Macro, three Variants, typed Placeholder, composite Element, and active Variant/Placeholder binding coverage through grammar, compiler, lock, LSP, and product paths.
- Sequential Kotlin verification passed: 30 language, 5 repository-model, 17 package-model, 25 package-runtime, 223 compiler, 5 interaction-model, 15 presentation-model, 3 SVG renderer, 27 runtime, and 55 LSP tests.
- Frontend passed 52/52; full IDE build, exact-root Electron proof, deterministic SVG/PNG export, encoding audit, source-set audit, and forbidden authority scans passed.
- Lock-backed Macro insertion, five compatible `BindPart` transactions, and exact `KM1-MAIN` Placeholder output in active LSP Canonical Scene and SVG export passed.
- Regenerated screenshots visually satisfy M45 golden rules at 1920x1080 and 720x900. Connection planning remains intentionally future scope.
- Acceptance audit passed and Stories/Epics 1-5 moved to `done`.

### File List

- `_bmad-output/implementation-artifacts/m45/6-1-run-full-m45-verification-and-hygiene.md`
- `_bmad-output/implementation-artifacts/m45/sprint-status.yaml`
- `_bmad-output/implementation-artifacts/m45/m45-acceptance-audit.md`
- `_bmad-output/implementation-artifacts/m45/m45-verification-log.md`
- `_bmad-output/implementation-artifacts/m45/m45-verification-summary.json`
- `_bmad-output/implementation-artifacts/m45/m45-product-proof.json`
- `_bmad-output/implementation-artifacts/m45/m45-story-5-4-proof.json`
- `_bmad-output/implementation-artifacts/m45/evidence/m45-lock-lineage-snapshot.txt`
- `_bmad-output/implementation-artifacts/m45/evidence/m45-operation-transcript.txt`
- `_bmad-output/implementation-artifacts/m45/evidence/m45-reopen-evidence.txt`
- `_bmad-output/implementation-artifacts/m45/exports/m45-export-proof.json`
- `_bmad-output/implementation-artifacts/m45/exports/m45-rolling-shutter.png`
- `_bmad-output/implementation-artifacts/m45/exports/m45-rolling-shutter.svg`
- `_bmad-output/implementation-artifacts/m45/screenshots/m45-rolling-shutter-desktop-1920x1080.png`
- `_bmad-output/implementation-artifacts/m45/screenshots/m45-rolling-shutter-narrow-720x900.png`
- `examples/m45/rolling-shutter/athena.lock`
- `examples/m45/rolling-shutter/packages/com.athena.iec/package.yaml`
- `examples/m45/rolling-shutter/packages/com.athena.iec/src/reuse.athena`
- `examples/m45/rolling-shutter/packages/com.vendor.siemens/package.yaml`
- `examples/m45/rolling-shutter/packages/com.vendor.siemens/src/variants.athena`
- `examples/m45/rolling-shutter/src/com/engineeringood/m45/rollingshutter/rolling-shutter.binding.athena`
- `kernel/language/src/main/antlr/com/engineeringood/athena/language/Athena.g4`
- `kernel/language/src/main/kotlin/com/engineeringood/athena/language/AthenaLanguageModel.kt`
- `kernel/language/src/main/kotlin/com/engineeringood/athena/language/antlr/AthenaAntlrParseAdapter.kt`
- `kernel/language/src/test/kotlin/com/engineeringood/athena/language/LanguageFacadeBoundaryTest.kt`
- `kernel/language/src/test/kotlin/com/engineeringood/athena/language/PackageRepresentationReuseSyntaxTest.kt`
- `kernel/compiler/src/main/kotlin/com/engineeringood/athena/compiler/repository/AthenaRepositoryLockMaterializer.kt`
- `kernel/compiler/src/main/kotlin/com/engineeringood/athena/compiler/repository/NativePackageItemIndexCompiler.kt`
- `kernel/compiler/src/test/kotlin/com/engineeringood/athena/compiler/repository/M45NativePackageCatalogTest.kt`
- `kernel/repository-model/src/main/kotlin/com/engineeringood/athena/repository/RepositoryContracts.kt`
- `kernel/package-runtime/src/test/kotlin/com/engineeringood/athena/packageruntime/FunctionRepresentationBindingAdmissionTest.kt`
- `ide/lsp/src/main/kotlin/com/engineeringood/athena/ide/lsp/AthenaDiagramProtocol.kt`
- `ide/lsp/src/main/kotlin/com/engineeringood/athena/ide/lsp/AthenaLanguageFeatures.kt`
- `ide/lsp/src/main/kotlin/com/engineeringood/athena/ide/lsp/RepresentationBindingCompanion.kt`
- `ide/lsp/src/test/kotlin/com/engineeringood/athena/ide/lsp/AthenaDiagramSceneRequestTest.kt`
- `ide/lsp/src/test/kotlin/com/engineeringood/athena/ide/lsp/JournalUndoRedoTest.kt`
- `ide/lsp/src/test/kotlin/com/engineeringood/athena/ide/lsp/M45ExportStabilityTest.kt`
- `ide/lsp/src/test/kotlin/com/engineeringood/athena/ide/lsp/PlacementOperationHandlerTest.kt`
- `ide/lsp/src/test/kotlin/com/engineeringood/athena/ide/lsp/RepresentationAndEngineeringOperationHandlerTest.kt`
- `ide/lsp/src/test/kotlin/com/engineeringood/athena/ide/lsp/AthenaDiagramSceneRequestTest.kt`
- `ide/theia-frontend/src/browser/diagram/konva-diagram-adapter.ts`
- `ide/theia-frontend/src/browser/athena-presentation-widget.tsx`
- `ide/theia-frontend/src/browser/style/index.css`
- `ide/theia-frontend/scripts/athena-konva-adapter.test.mjs`
- `ide/theia-frontend/scripts/athena-presentation-layout.test.mjs`
- `ide/theia-frontend/scripts/athena-editor-rulers.test.mjs`
- `ide/theia-frontend/scripts/athena-product-layout.test.mjs`
- `ide/theia-product/scripts/athena-m45-proof-main.js`
- `ide/theia-product/scripts/verify-athena-m45-product-proof.js`
- `ide/theia-product/scripts/verify-athena-m45-export.js`
- `examples/m45/rolling-shutter/src/com/engineeringood/m45/rollingshutter/rolling-shutter.sheet.style.athena`

### Change Log

- 2026-08-10: Created comprehensive BMad Story 6.1 context; status ready-for-dev.
- 2026-08-10: Added native positive Macro/Variant/Placeholder/composite coverage and Lock V3 attributes.
- 2026-08-10: Removed stale M44 LSP fixtures, corrected full Representation choice mutation, and passed complete sequential verification.
- 2026-08-10: Published acceptance, command, digest, product, export, and screenshot evidence; moved verified M45 work to done and Story 6.1 to review.
- 2026-08-10: Resolved all review follow-ups, removed canvas source-link/style leaks, regenerated black IEC visual proof, added exact Placeholder export proof, and closed Story 6.1.
