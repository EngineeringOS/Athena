# M29 Retrospective

Date: 2026-07-21

## Milestone Intent

M29 set out to make interaction a semantic Athena-owned layer instead of a Theia-owned behavior:

```text
Semantic model and projection facts
  -> Semantic Capability Registry
  -> Interaction Compiler v0
  -> Interaction IR
  -> Interaction Runtime
  -> frontend adapter
  -> existing mutation authority
```

This completes the missing third pillar after M27 spatial intelligence and M28 semantic mutation.

## Wins

- `kernel/interaction-model` now owns Interaction IR, Semantic Action Intent, lifecycle,
  diagnostics, provenance, reveal, preview, and envelope contracts.
- The Semantic Capability Registry starts from canonical semantic subjects and projection
  occurrences, not frontend objects.
- Reveal/navigation contracts preserve source, graph, inspector, and Problems targets without DOM
  text or SVG geometry authority.
- M28 relationship mutation is routed toward `semantic-relationship` and
  `SemanticRelationshipIntent(ElectricalConnectionRelationship)` while retained `connect-ports`
  compatibility paths are ledgered.
- Semantic entity creation is proven through backend-owned source impact, governed acceptance, and
  nested-port `.athena` anatomy.
- `examples/m29/sample-project` opens through the real product path and compiles/projects without
  `STOP_DOWNSTREAM`.
- M29 product smoke emits and validates the full `m29.interaction.v1` structured proof payload
  inventory before graph-workbench UI proof.

## Mistakes And Late Findings

- Sprint status briefly contained a duplicate Story 1.3 key with conflicting status. The fix was to
  keep the `done` entry, remove the stale `ready-for-dev` entry, and add a duplicate-key audit to
  closeout checks.
- Earlier M29 work relied on focused model/LSP tests before the product proof existed. Story 6.2
  corrected this by adding a real `start:smoke:m29` path.
- The first Story 6.2 RED test failed because the test regex itself was invalid. That was a test
  authoring error, not product behavior. The prevention rule is to rerun the RED test after fixing
  test syntax and confirm it fails for the intended missing feature.
- Story 6.3 reran the M29 product smoke after docs/build cleanup and exposed a shared Electron
  opener race. The opener assumed either renderer `require` or a visible Home quick action would be
  available before revealing Graphical View. In one real run neither was available, so the smoke
  timed out before proof collection. The fix was a narrow Theia adapter smoke hook that executes the
  existing `athena.revealGraphicalView` command and is attempted before DOM fallback.
- Retained `connect-ports` compatibility is still present in older runtime/LSP/frontend paths. This
  is acceptable only because the cleanup ledger names each path, owner, reason, and M30 target.

## Blockers

- Broad CodeGraph queries sometimes matched unrelated reference fixtures. The workable path was to
  try CodeGraph first, then narrow with targeted `rg` scoped away from generated/reference output.
- The real product smoke depends on a built/installed LSP host. The closeout path must run
  `:ide:lsp:installDist` before Electron smoke if the launcher is missing.
- Generated Theia `lib` output can pollute searches and diffs. Closeout review must focus on source,
  tests, docs, and package scripts unless generated output is intentionally part of the task.
- Product smoke startup must not depend on Home quick-action visibility. Use a governed adapter hook
  or command registry path first, and keep DOM activation as fallback only.

## Prevention Rules

1. Every story ends with a polish/purge gate: remove stale artifacts or ledger retained paths with
   owner, reason, and target milestone.
2. Sprint status gets a duplicate-key audit after status edits.
3. Product smoke must assert structured semantic payloads before UI click or visual assertions.
4. Frontend tests may assert adapter behavior, but semantic truth must come from `.athena`,
   Interaction IR, projection facts, and backend runtime/source-edit gates.
5. Compatibility names like `connect-ports` must not be introduced by new M29 code. If retained,
   they need ledger ownership and a removal/migration milestone.
6. Regression verification must be sequential for Gradle tasks on Windows.

## Verification Evidence

- `.\gradlew.bat --no-daemon --console=plain :kernel:interaction-model:test`
- `.\gradlew.bat --no-daemon --console=plain :kernel:authoring-model:test`
- `.\gradlew.bat --no-daemon --console=plain :kernel:runtime:test --tests "com.engineeringood.athena.runtime.AthenaAuthoringSessionRuntimeServiceTest"`
- `.\gradlew.bat --no-daemon --console=plain :ide:lsp:test --tests "com.engineeringood.athena.ide.lsp.AthenaAuthoringRequestTest"`
- `.\gradlew.bat --no-daemon --console=plain :kernel:compiler:test --tests "com.engineeringood.athena.compiler.AthenaM29SampleProjectCompilerTest"`
- `node --test .\ide\theia-frontend\scripts\athena-m29-product-smoke-wiring.test.mjs`
- `node --test .\ide\theia-frontend\scripts\athena-m28-relationship-authoring-model.test.mjs`
- `yarn --cwd .\ide\theia-frontend build`
- `yarn --cwd .\ide build`
- `yarn --cwd .\ide start:smoke:m27`
- `yarn --cwd .\ide start:smoke:m28`
- `yarn --cwd .\ide start:smoke:m29`
- `powershell -ExecutionPolicy Bypass -File .\tools\encoding-audit.ps1`

The evidence list is updated by Story 6.3 closeout after the final sequential verification run.

## Next Risks

- M30 should migrate or remove the ledgered `connect-ports` compatibility paths instead of letting
  them become permanent.
- Future AI/action milestones should build on `SemanticActionIntent` rather than adding agent-only
  command shapes.
- Standards/library visual fidelity should remain a presentation/standards milestone, not a hidden
  M29 interaction responsibility.
