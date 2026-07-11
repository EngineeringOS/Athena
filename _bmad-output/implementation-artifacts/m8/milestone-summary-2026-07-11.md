# M8 Milestone Summary

Milestone: `M8`
Title: `Unified Semantic Mutation Model`
Date: `2026-07-11`
Status: `completed`

## Scope Closed

M8 closed all three planned epics:

1. Freeze the unified mutation authority
2. Route graph-originated editing through Athena commands
3. Unify review and reveal across source and graph

## What M8 Achieved

M8 turned Athena from a graph-capable inspection workbench into the first proof that source and graph are only two clients of one mutation authority.

Delivered:

- explicit runtime-owned mutation contracts and categories above source and graph
- projection ownership contracts for interactive and inspect-only views
- normalized source-originated mutation evaluation in the same governed mutation-result shape
- graph-originated semantic mutation through the existing Athena command runtime
- graph-originated projection mutation through runtime-owned projection metadata
- unified semantic review facts for accepted source and graph mutations
- canonical reveal across source, graph, and semantic SCM surfaces
- published proof corpus and repeatable verification path for the finished mutation milestone

## Proven Chain

```text
source editor or graph workbench
        ->
Athena-owned command or mutation request
        ->
runtime-owned mutation evaluation
        ->
accepted / rejected / validation feedback result
        ->
semantic review and projection consequences
        ->
ide/lsp transport
        ->
source, graph, and semantic SCM surfaces
```

## What M8 Proves

M8 proves:

- source and graph no longer define separate editing semantics
- semantic graph edits can reuse Athena command meaning instead of renderer-local save behavior
- projection graph edits can remain governed metadata without mutating canonical engineering truth
- accepted mutation review remains downstream of `kernel/semantic-scm`
- reveal across source, graph, and review stays anchored on canonical semantic identity

M8 does not yet prove:

- full canonical write-through source editing; the current source path remains preview-first evaluation
- unrestricted graphical authoring
- final notation or symbol-pack depth
- full CAD or EPLAN-class editing breadth
- multi-user approval workflow redesign
- final GLSP-class editing architecture as the only live renderer path

## Verification Evidence

- `cmd /c "call java25 && .\gradlew.bat --no-daemon --console=plain :kernel:runtime:test :ide:lsp:test --tests com.engineeringood.athena.runtime.AthenaGraphCommandIntentServiceTest --tests com.engineeringood.athena.runtime.AthenaRuntimeProjectionSessionTest --tests com.engineeringood.athena.ide.lsp.AthenaSourceMutationRequestTest --tests com.engineeringood.athena.ide.lsp.AthenaProjectionRequestTest"`
- `yarn --cwd integrations/graph-glsp test`
- `yarn --cwd ide/theia-frontend test`
- `yarn --cwd ide verify:m8`

## Published M8 Reading Path

1. `docs/usages/m8-proof-usage.md`
2. `examples/m8/README.md`
3. `_bmad-output/implementation-artifacts/m8/3-3-publish-the-m8-proof-corpus-and-verification-path.md`
4. `_bmad-output/implementation-artifacts/m8/milestone-summary-2026-07-11.md`
5. `docs/usages/athena-workspace-summary.md`

## Main Residual Risks

- no automated desktop E2E coverage yet for the mutation proof flow
- the current graph semantic proof remains intentionally narrow around `connect-ports`
- the current graph projection proof remains intentionally narrow around governed cabinet placement
- Windows verification still requires strict sequential JVM and Node execution

## Conclusion

M8 is complete as the first unified semantic mutation milestone.

Athena now proves not only that engineering meaning can be projected into source and graph, but also that accepted editing, review, and reveal can remain governed by one Athena-owned mutation model instead of splitting into renderer and editor sub-systems.
